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
import org.quickperf.writer.PrintWriterBuilder;
import org.quickperf.writer.WriterFactory;

import java.io.PrintWriter;

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
            StackDepth stackDepth = extractStackDepthFrom(annotation);
            return new StackTraceDisplayConfig( annotation.filterStackTrace()
                                              , stackDepth);
        }
        return StackTraceDisplayConfig.NONE;
    }

    private StackDepth extractStackDepthFrom(ProfileConnection annotation) {
        short annotationStackDepth = annotation.stackDepth();
        if (annotationStackDepth == -1) {
            return StackDepth.ALL;
        }
        return new StackDepth(annotationStackDepth);
    }

}
