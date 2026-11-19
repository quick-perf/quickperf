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
package org.quickperf.sql.framework.quickperf;

import org.junit.Test;
import org.quickperf.sql.framework.ClassPath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.quickperf.sql.framework.quickperf.QuickPerfDependency.QUICKPERF_SPRING_BOOT_1_SQL_STARTER;
import static org.quickperf.sql.framework.quickperf.QuickPerfDependency.QUICKPERF_SPRING_BOOT_2_SQL_STARTER;

public class SpringDataSourceConfigTest {

    @Test public void
    should_suggest_the_spring_boot_1_starter_if_it_is_not_on_the_class_path() {

        // GIVEN
        ClassPath classPath = mock(ClassPath.class);
        when(classPath.containsSpringBoot1()).thenReturn(true);

        // WHEN
        String message = new SpringDataSourceConfig(classPath).getMessage();

        // THEN
        assertThat(message).contains("add the following dependency")
                           .contains(QUICKPERF_SPRING_BOOT_1_SQL_STARTER.getArtifactId());

    }

    @Test public void
    should_not_suggest_the_spring_boot_1_starter_if_it_is_on_the_class_path() {

        // GIVEN
        ClassPath classPath = mock(ClassPath.class);
        when(classPath.containsSpringBoot1()).thenReturn(true);
        when(classPath.contains(QUICKPERF_SPRING_BOOT_1_SQL_STARTER)).thenReturn(true);

        // WHEN
        String message = new SpringDataSourceConfig(classPath).getMessage();

        // THEN
        assertThat(message).doesNotContain(QUICKPERF_SPRING_BOOT_1_SQL_STARTER.getArtifactId());

    }

    @Test public void
    should_suggest_the_spring_boot_2_starter_if_it_is_not_on_the_class_path() {

        // GIVEN
        ClassPath classPath = mock(ClassPath.class);
        when(classPath.containsSpringBoot3()).thenReturn(true);

        // WHEN
        String message = new SpringDataSourceConfig(classPath).getMessage();

        // THEN
        assertThat(message).contains("add the following dependency")
                           .contains(QUICKPERF_SPRING_BOOT_2_SQL_STARTER.getArtifactId());

    }

    @Test public void
    should_not_suggest_the_spring_boot_2_starter_if_it_is_on_the_class_path() {

        // GIVEN
        ClassPath classPath = mock(ClassPath.class);
        when(classPath.containsSpringBoot2()).thenReturn(true);
        when(classPath.contains(QUICKPERF_SPRING_BOOT_2_SQL_STARTER)).thenReturn(true);

        // WHEN
        String message = new SpringDataSourceConfig(classPath).getMessage();

        // THEN
        assertThat(message).doesNotContain(QUICKPERF_SPRING_BOOT_2_SQL_STARTER.getArtifactId());

    }

}
