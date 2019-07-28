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