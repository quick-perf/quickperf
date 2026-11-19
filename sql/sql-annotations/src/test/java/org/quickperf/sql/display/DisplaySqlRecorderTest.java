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
package org.quickperf.sql.display;

import org.junit.After;
import org.junit.Test;
import org.quickperf.perfrecording.TestThreads;
import org.quickperf.sql.SqlRecorder;
import org.quickperf.sql.SqlRecorderRegistry;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class DisplaySqlRecorderTest {

    // The thread building the test execution context, which registers the
    // recorder. The test thread plays the thread executing the test method.
    private final ExecutorService contextBuildingThread = Executors.newSingleThreadExecutor();

    @After
    public void tearDown() {
        contextBuildingThread.shutdownNow();
        // Surefire runs this module with parallel=all, threadCount=5; clear
        // any state this test left behind so it cannot leak into siblings.
        SqlRecorderRegistry.INSTANCE.clear();
    }

    @Test public void
    should_be_a_recorder_of_the_recording_thread_while_recording_if_another_thread_registered_it() throws Exception {

        // GIVEN a test method executed on another thread than the one that
        // built the test execution context, as under a JUnit 4 timeout
        DisplaySqlRecorder displaySqlRecorder = buildOnContextBuildingThread();
        TestThreads.markCurrentThread();

        try {

            // WHEN
            displaySqlRecorder.startRecording(null);

            // THEN
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorders()).contains(displaySqlRecorder);

        } finally {
            displaySqlRecorder.stopRecording(null);
            cleanResourcesOnContextBuildingThread(displaySqlRecorder);
        }

    }

    @Test public void
    should_only_stay_a_recorder_of_the_registering_thread_after_recording_on_another_thread() throws Exception {

        // GIVEN
        DisplaySqlRecorder displaySqlRecorder = buildOnContextBuildingThread();
        TestThreads.markCurrentThread();

        try {

            // WHEN
            displaySqlRecorder.startRecording(null);
            displaySqlRecorder.stopRecording(null);

            // THEN the registering thread keeps it, for the SQL executed after the test method
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorders()).doesNotContain(displaySqlRecorder);
            assertThat(sqlRecordersOfContextBuildingThread()).contains(displaySqlRecorder);

        } finally {
            cleanResourcesOnContextBuildingThread(displaySqlRecorder);
        }

    }

    @Test public void
    should_stay_a_recorder_of_the_registering_thread_after_recording_on_it() {

        // GIVEN a test method executed on the thread that built the test execution context
        DisplaySqlRecorder displaySqlRecorder = new DisplaySqlRecorder();

        try {

            // WHEN
            displaySqlRecorder.startRecording(null);
            displaySqlRecorder.stopRecording(null);

            // THEN it still displays the SQL executed after the test method
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorders()).contains(displaySqlRecorder);

        } finally {
            displaySqlRecorder.cleanResources();
        }

    }

    @Test public void
    should_be_a_recorder_of_a_thread_it_is_bound_to() throws Exception {

        // GIVEN code executed before the test method on another thread than
        // the one that built the test execution context, as under a JUnit 4
        // Timeout rule
        DisplaySqlRecorder displaySqlRecorder = buildOnContextBuildingThread();
        TestThreads.markCurrentThread();

        try {

            // WHEN
            displaySqlRecorder.bindToCurrentThread();

            // THEN
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorders()).contains(displaySqlRecorder);

        } finally {
            cleanResourcesOnContextBuildingThread(displaySqlRecorder);
        }

    }

    @Test public void
    should_stay_a_recorder_of_a_thread_it_is_bound_to_after_recording_on_it() throws Exception {

        // GIVEN
        DisplaySqlRecorder displaySqlRecorder = buildOnContextBuildingThread();
        TestThreads.markCurrentThread();
        displaySqlRecorder.bindToCurrentThread();

        try {

            // WHEN
            displaySqlRecorder.startRecording(null);
            displaySqlRecorder.stopRecording(null);

            // THEN it still displays the SQL executed after the test method
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorders()).contains(displaySqlRecorder);

        } finally {
            cleanResourcesOnContextBuildingThread(displaySqlRecorder);
        }

    }

    @Test public void
    should_no_longer_be_a_recorder_of_a_thread_it_is_bound_to_once_its_resources_are_cleaned_on_it() throws Exception {

        // GIVEN
        DisplaySqlRecorder displaySqlRecorder = buildOnContextBuildingThread();
        TestThreads.markCurrentThread();
        displaySqlRecorder.bindToCurrentThread();

        // WHEN
        displaySqlRecorder.cleanResources();

        // THEN
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorders()).doesNotContain(displaySqlRecorder);

    }

    @Test public void
    should_not_be_a_recorder_of_worker_threads_while_its_test_is_not_recording() throws Exception {

        // GIVEN a recorder registered when the test execution context is
        // built, before its test records SQL
        DisplaySqlRecorder displaySqlRecorder = buildOnContextBuildingThread();

        try {

            // WHEN
            Collection<SqlRecorder> sqlRecordersOfAWorkerThread = sqlRecordersOfAWorkerThread();

            // THEN it does not display the SQL of the worker threads of the
            // tests recording at that time
            assertThat(sqlRecordersOfAWorkerThread).doesNotContain(displaySqlRecorder);

        } finally {
            cleanResourcesOnContextBuildingThread(displaySqlRecorder);
        }

    }

    @Test public void
    should_be_a_recorder_of_worker_threads_while_its_test_is_recording() throws Exception {

        // GIVEN
        DisplaySqlRecorder displaySqlRecorder = buildOnContextBuildingThread();
        TestThreads.markCurrentThread();

        try {

            // WHEN
            displaySqlRecorder.startRecording(null);

            // THEN
            assertThat(sqlRecordersOfAWorkerThread()).contains(displaySqlRecorder);

        } finally {
            displaySqlRecorder.stopRecording(null);
            cleanResourcesOnContextBuildingThread(displaySqlRecorder);
        }

    }

    @Test public void
    should_not_be_a_recorder_of_worker_threads_once_its_test_stopped_recording() throws Exception {

        // GIVEN
        DisplaySqlRecorder displaySqlRecorder = buildOnContextBuildingThread();
        TestThreads.markCurrentThread();

        try {

            // WHEN
            displaySqlRecorder.startRecording(null);
            displaySqlRecorder.stopRecording(null);

            // THEN
            assertThat(sqlRecordersOfAWorkerThread()).doesNotContain(displaySqlRecorder);

        } finally {
            cleanResourcesOnContextBuildingThread(displaySqlRecorder);
        }

    }

    private static Collection<SqlRecorder> sqlRecordersOfAWorkerThread() throws Exception {
        ExecutorService workerThread = Executors.newSingleThreadExecutor();
        try {
            return workerThread.submit(new Callable<Collection<SqlRecorder>>() {
                @Override
                public Collection<SqlRecorder> call() {
                    return SqlRecorderRegistry.INSTANCE.getSqlRecorders();
                }
            }).get(5, TimeUnit.SECONDS);
        } finally {
            workerThread.shutdownNow();
        }
    }

    private DisplaySqlRecorder buildOnContextBuildingThread() throws Exception {
        return contextBuildingThread.submit(new Callable<DisplaySqlRecorder>() {
            @Override
            public DisplaySqlRecorder call() {
                return new DisplaySqlRecorder();
            }
        }).get(5, TimeUnit.SECONDS);
    }

    private Collection<SqlRecorder> sqlRecordersOfContextBuildingThread() throws Exception {
        return contextBuildingThread.submit(new Callable<Collection<SqlRecorder>>() {
            @Override
            public Collection<SqlRecorder> call() {
                return SqlRecorderRegistry.INSTANCE.getSqlRecorders();
            }
        }).get(5, TimeUnit.SECONDS);
    }

    private void cleanResourcesOnContextBuildingThread(final DisplaySqlRecorder displaySqlRecorder) throws Exception {
        contextBuildingThread.submit(new Runnable() {
            @Override
            public void run() {
                displaySqlRecorder.cleanResources();
            }
        }).get(5, TimeUnit.SECONDS);
    }

}
