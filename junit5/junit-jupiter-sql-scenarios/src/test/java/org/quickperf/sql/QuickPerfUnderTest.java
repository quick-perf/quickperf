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
package org.quickperf.sql;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers the QuickPerf extension under test on a test class of the shared SQL scenarios. This annotation exists for
 * the tests of QuickPerf only: it is not part of the QuickPerf API, and users keep registering QuickPerf with the
 * <code>@QuickPerfTest</code> annotation of their extension.
 * <p>
 * The scenarios of this module run twice: in junit5-sql-test with quick-perf-junit5 (JUnit 5.6.2), and in
 * junit6-sql-test with quick-perf-junit6 (JUnit 6). So they cannot declare the <code>@QuickPerfTest</code> annotation
 * of one extension: they would only test this extension, and this module, compiled once to Java 8, cannot depend on
 * quick-perf-junit6, which requires Java 17.
 * <p>
 * The annotation registers {@link QuickPerfExtensionForwarder} instead. The forwarder instantiates the extension named
 * by the <code>quickperf-extension-under-test.properties</code> test resource of the module running the scenarios, and
 * forwards every callback and interception to it. QuickPerfExtensionForwarderCompletenessTest checks that it forwards
 * everything the extension under test implements.
 * <p>
 * Declare it where a scenario would declare <code>@QuickPerfTest</code>: the extensions keep their order of
 * registration, which some scenarios depend on, such as InterceptorRegisteredAfterQuickPerfTest, or the extension
 * registered after QuickPerf in ThreadsExecutingTestCodeJUnit5Test.
 *
 * @see QuickPerfExtensionForwarder
 */
@ExtendWith(QuickPerfExtensionForwarder.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface QuickPerfUnderTest {

}
