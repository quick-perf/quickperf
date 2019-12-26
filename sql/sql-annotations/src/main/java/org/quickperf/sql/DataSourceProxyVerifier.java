package org.quickperf.sql;

public class DataSourceProxyVerifier {

    public static final String SEVERAL_PROXIES_WARNING = "[WARNING] QuickPerf has built several datasource proxies";

    private int listenerIdentifier;

    private boolean quickPerfBuiltSeveralDataSourceProxies;

    public void addListenerIdentifier(int newListenerIdentifier) {
        initListenerIdentifierIfNotAlreadyInitialized(newListenerIdentifier);
        if(listenerIdentifier != newListenerIdentifier) {
            quickPerfBuiltSeveralDataSourceProxies = true;
        }
    }

    private void initListenerIdentifierIfNotAlreadyInitialized(int newListenerIdentifier) {
        if(this.listenerIdentifier == 0) {
            this.listenerIdentifier = newListenerIdentifier;
        }
    }

    public boolean hasQuickPerfBuiltSeveralDataSourceProxies() {
        return quickPerfBuiltSeveralDataSourceProxies;
    }

}
