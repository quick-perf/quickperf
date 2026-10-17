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
package org.quickperf.sql.annotation;

import org.junit.Test;
import org.quickperf.writer.DefaultWriterFactory;
import org.quickperf.writer.WriterFactory;

import java.io.StringWriter;
import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlAnnotationBuilderTest {

    @Test public void
    should_build_an_analyze_sql_annotation_with_the_default_writer_factory() {

        // WHEN
        AnalyzeSql analyzeSql = SqlAnnotationBuilder.analyzeSql(DefaultWriterFactory.class);

        // THEN
        assertThat(analyzeSql.annotationType()).isEqualTo(AnalyzeSql.class);
        assertThat(analyzeSql.writerFactory()).isEqualTo(DefaultWriterFactory.class);

    }

    @Test public void
    should_build_an_analyze_sql_annotation_with_a_custom_writer_factory() {

        // WHEN
        AnalyzeSql analyzeSql = SqlAnnotationBuilder.analyzeSql(StringWriterFactory.class);

        // THEN
        assertThat(analyzeSql.annotationType()).isEqualTo(AnalyzeSql.class);
        assertThat(analyzeSql.writerFactory()).isEqualTo(StringWriterFactory.class);

    }

    public static class StringWriterFactory implements WriterFactory {

        @Override
        public Writer buildWriter() {
            return new StringWriter();
        }

    }

}
