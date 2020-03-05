/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.sql;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class DataSourceProxyVerifierTest {

    @Test public void
    quick_perf_has_built_several_data_source_proxies_if_several_listener_identifiers() {

        DataSourceProxyVerifier dataSourceProxyVerifier = new DataSourceProxyVerifier();

        dataSourceProxyVerifier.addListenerIdentifier(1);
        dataSourceProxyVerifier.addListenerIdentifier(2);

        assertThat(dataSourceProxyVerifier.hasQuickPerfBuiltSeveralDataSourceProxies())
        .isTrue();

    }

    @Test public void
    quick_perf_has_NOT_built_several_data_source_proxies_if_one_listener_identifier() {

        DataSourceProxyVerifier dataSourceProxyVerifier = new DataSourceProxyVerifier();

        int listenerIdentifier = 1;

        dataSourceProxyVerifier.addListenerIdentifier(listenerIdentifier);
        dataSourceProxyVerifier.addListenerIdentifier(listenerIdentifier);

        assertThat(dataSourceProxyVerifier.hasQuickPerfBuiltSeveralDataSourceProxies())
        .isFalse();

    }

}