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

package org.quickperf.sql.config;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class PersistenceUnitInfoBuilder {

    private PersistenceUnitInfoBuilder() {}

    public static PersistenceUnitInfoBuilder aPersistenceUnitInfo() {
        return new PersistenceUnitInfoBuilder();
    }

    public PersistenceUnitInfo build(final DataSource dataSource
                                   , final Properties config
                                   , final Class<?>... persistentClasses) {
        return new PersistenceUnitInfo(){

            @Override
            public String getPersistenceUnitName() {
                return "my pu";
            }

            @Override
            public String getPersistenceProviderClassName() {
                return "org.hibernate.jpa.HibernatePersistenceProvider";
            }

            @Override
            public PersistenceUnitTransactionType getTransactionType() {
                return PersistenceUnitTransactionType.RESOURCE_LOCAL;
            }

            @Override
            public DataSource getJtaDataSource() {
                return null;
            }

            @Override
            public DataSource getNonJtaDataSource() {
                return dataSource;
            }

            @Override
            public List<String> getMappingFileNames() {
                return emptyList();
            }

            @Override
            public List<URL> getJarFileUrls() {
                return emptyList();
            }

            @Override
            public URL getPersistenceUnitRootUrl() {
                return null;
            }

            @Override
            public List<String> getManagedClassNames() {
                return Arrays.stream(persistentClasses)
                                        .map(c -> c.getName())
                                        .collect(toList());
            }

            @Override
            public boolean excludeUnlistedClasses() {
                return false;
            }

            @Override
            public SharedCacheMode getSharedCacheMode() {
                return SharedCacheMode.NONE;
            }

            @Override
            public ValidationMode getValidationMode() {
                return ValidationMode.NONE;
            }

            @Override
            public Properties getProperties() {
                return config;
            }

            @Override
            public String getPersistenceXMLSchemaVersion() {
                return "2.1";
            }

            @Override
            public ClassLoader getClassLoader() {
                return this.getClass().getClassLoader();
            }

            @Override
            public void addTransformer(ClassTransformer transformer) {

            }

            @Override
            public ClassLoader getNewTempClassLoader() {
                return null;
            }

        };
    }

}
