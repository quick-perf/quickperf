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
package org.quickperf.junit6;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Detects the QuickPerf JUnit 5 extension (quick-perf-junit5) registered for the same test, during a migration for
 * example. Both extensions would record the test: their SQL recorders would displace each other, and each extension
 * would report the failure of the other one. JUnit cannot prevent the double registration, and no public API lists the
 * registered extensions: quick-perf-junit5 puts a marker in the store of the extension context, in beforeAll and
 * beforeEach. Store.get also searches the parent contexts. This class does not depend on quick-perf-junit5: the
 * namespace and the key are strings.
 */
final class JUnit5ExtensionDetector {

    private static final ExtensionContext.Namespace QUICK_PERF_EXTENSIONS =
            ExtensionContext.Namespace.create("org.quickperf.junit");

    private static final String JUNIT5_EXTENSION = "org.quickperf.junit5.QuickPerfTestExtension";

    private JUnit5ExtensionDetector() { }

    /**
     * @throws ExtensionConfigurationException if quick-perf-junit5 has marked the context or one of its parents
     */
    static void failIfRegisteredFor(ExtensionContext context) {
        if (context.getStore(QUICK_PERF_EXTENSIONS).get(JUNIT5_EXTENSION) != null) {
            throw new ExtensionConfigurationException(Messages.bothQuickPerfExtensionsRegistered());
        }
    }

}
