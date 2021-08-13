/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql.connection;

import org.quickperf.perfrecording.ExtractablePerfRecorderParametersFromAnnotation;
import org.quickperf.sql.annotation.ProfileConnection;
import org.quickperf.sql.connection.stack.*;
import org.quickperf.sql.framework.ClassPath;
import org.quickperf.writer.PrintWriterBuilder;
import org.quickperf.writer.WriterFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import static java.util.Collections.emptyList;

public class ProfilingParamsExtractor implements ExtractablePerfRecorderParametersFromAnnotation<ProfileConnection, AnnotationProfilingParameters> {

    @Override
    public AnnotationProfilingParameters extractFrom(ProfileConnection annotation) {

        Class<? extends WriterFactory> writerFactoryClass = annotation.writerFactory();
        PrintWriter printWriter = PrintWriterBuilder.INSTANCE.buildPrintWriterFrom(writerFactoryClass);

        StackTraceDisplayConfig stacktracedisplayConfig = buildStackTraceDisplayConfigFrom(annotation);

        ProfilingParameters profilingParameters = new ProfilingParameters( annotation.level()
                                                                         , stacktracedisplayConfig
                                                                         , printWriter);

        return new AnnotationProfilingParameters( profilingParameters
                                                , annotation.beforeAndAfterTestMethodExecution());

    }

    private StackTraceDisplayConfig buildStackTraceDisplayConfigFrom(ProfileConnection annotation) {
        if (annotation.displayStackTrace()) {
            Collection<StackTraceFilter> stackTraceFilters = buildStackTraceFilters(annotation);
            StackTrace.StackDepth stackDepth = extractStackDepthFrom(annotation);
            return StackTraceDisplayConfig.of(stackDepth, stackTraceFilters);
        }
        return StackTraceDisplayConfig.noStackTrace();
    }

    private Collection<StackTraceFilter> buildStackTraceFilters(ProfileConnection annotation) {
        if (annotation.filterStackTrace()) {
            return buildStackTraceFilters();
        }
        return emptyList();
    }

    private Collection<StackTraceFilter> buildStackTraceFilters() {
        Collection<StackTraceFilter> stackTraceFilters = new ArrayList<>();
        stackTraceFilters.add(QuickPerfStackTraceTraceFilter.INSTANCE);
        if (ClassPath.INSTANCE.containsSpringCore()) {
            stackTraceFilters.add(SpringStackTraceTraceFilter.INSTANCE);
        }
        stackTraceFilters.add(JUnit4StackTraceFilter.INSTANCE);
        stackTraceFilters.add(JUnit5StackTraceFilter.INSTANCE);
        stackTraceFilters.add(TestNGStackTraceFilter.INSTANCE);
        return stackTraceFilters;
    }

    private StackTrace.StackDepth extractStackDepthFrom(ProfileConnection annotation) {
        short annotationStackDepth = annotation.stackDepth();
        if (annotationStackDepth == -1) {
            return StackTrace.StackDepth.ALL;
        }
        return new StackTrace.StackDepth(annotationStackDepth);
    }

}
