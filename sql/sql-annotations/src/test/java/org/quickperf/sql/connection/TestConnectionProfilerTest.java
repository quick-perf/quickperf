/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2022 the original author or authors.
 */
package org.quickperf.sql.connection;

import org.junit.After;
import org.junit.Test;
import org.quickperf.perfrecording.TestThreads;
import org.quickperf.sql.connection.stack.StackTraceDisplayConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class TestConnectionProfilerTest {

    // The thread building the test execution context, which registers the
    // profiler. The test thread plays the thread executing the test method.
    private final ExecutorService contextBuildingThread = Executors.newSingleThreadExecutor();

    @After
    public void tearDown() {
        contextBuildingThread.shutdownNow();
        // Surefire parallel=all: clear so this test never leaks into siblings.
        ConnectionListenerRegistry.INSTANCE.clear();
    }

    @Test public void
    should_profile_the_connections_of_the_recording_thread_if_another_thread_registered_the_profiler() throws Exception {

        // GIVEN a test method executed on another thread than the one that
        // built the test execution context, as under a JUnit 4 timeout
        TestConnectionProfiler testConnectionProfiler = buildOnContextBuildingThread();
        ConnectionEventsProfiler profiler = profilerOfContextBuildingThread();
        TestThreads.markCurrentThread();

        try {

            // WHEN
            testConnectionProfiler.startRecording(null);

            // THEN
            assertThat(profiler).isNotNull();
            assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                    .isSameAs(profiler);

        } finally {
            testConnectionProfiler.stopRecording(null);
            cleanResourcesOnContextBuildingThread(testConnectionProfiler);
        }

    }

    @Test public void
    should_only_stay_a_listener_of_the_registering_thread_after_recording_on_another_thread() throws Exception {

        // GIVEN
        TestConnectionProfiler testConnectionProfiler = buildOnContextBuildingThread();
        ConnectionEventsProfiler profiler = profilerOfContextBuildingThread();
        TestThreads.markCurrentThread();

        try {

            // WHEN
            testConnectionProfiler.startRecording(null);
            testConnectionProfiler.stopRecording(null);

            // THEN
            assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                    .isNotSameAs(profiler);
            assertThat(profiler).isNotNull();
            assertThat(profilerOfContextBuildingThread()).isSameAs(profiler);

        } finally {
            cleanResourcesOnContextBuildingThread(testConnectionProfiler);
        }

    }

    @Test public void
    should_profile_the_connections_of_a_thread_it_is_bound_to() throws Exception {

        // GIVEN code executed before the test method on another thread than
        // the one that built the test execution context, as under a JUnit 4
        // Timeout rule
        TestConnectionProfiler testConnectionProfiler = buildOnContextBuildingThread();
        ConnectionEventsProfiler profiler = profilerOfContextBuildingThread();
        TestThreads.markCurrentThread();

        try {

            // WHEN
            testConnectionProfiler.bindToCurrentThread();

            // THEN
            assertThat(profiler).isNotNull();
            assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                    .isSameAs(profiler);

        } finally {
            cleanResourcesOnContextBuildingThread(testConnectionProfiler);
        }

    }

    @Test public void
    should_no_longer_profile_the_connections_of_a_thread_it_is_bound_to_once_its_resources_are_cleaned_on_it() throws Exception {

        // GIVEN
        TestConnectionProfiler testConnectionProfiler = buildOnContextBuildingThread();
        ConnectionEventsProfiler profiler = profilerOfContextBuildingThread();
        TestThreads.markCurrentThread();
        testConnectionProfiler.bindToCurrentThread();

        // WHEN
        testConnectionProfiler.cleanResources();

        // THEN
        assertThat(profiler).isNotNull();
        assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                .isNotSameAs(profiler);

    }

    private TestConnectionProfiler buildOnContextBuildingThread() throws Exception {
        return contextBuildingThread.submit(new Callable<TestConnectionProfiler>() {
            @Override
            public TestConnectionProfiler call() {
                ProfilingParameters profilingParameters = new ProfilingParameters( Level.INFO
                                                                                 , StackTraceDisplayConfig.noStackTrace()
                                                                                 , new PrintWriter(new StringWriter()));
                return new TestConnectionProfiler(new AnnotationProfilingParameters(profilingParameters, false));
            }
        }).get(5, TimeUnit.SECONDS);
    }

    private ConnectionEventsProfiler profilerOfContextBuildingThread() throws Exception {
        return contextBuildingThread.submit(new Callable<ConnectionEventsProfiler>() {
            @Override
            public ConnectionEventsProfiler call() {
                return ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class);
            }
        }).get(5, TimeUnit.SECONDS);
    }

    private void cleanResourcesOnContextBuildingThread(final TestConnectionProfiler testConnectionProfiler) throws Exception {
        contextBuildingThread.submit(new Runnable() {
            @Override
            public void run() {
                testConnectionProfiler.cleanResources();
            }
        }).get(5, TimeUnit.SECONDS);
    }

}
