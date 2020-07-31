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

package org.quickperf.sql.framework;

public enum MicronautSuggestion implements QuickPerfSuggestion {

        N_PLUS_ONE_SELECT() {

            @Override
            public String getMessage() {
                String lightBulb = "\uD83D\uDCA1";
                String message =  System.lineSeparator()
                        + lightBulb + " Perhaps you are facing an N+1 select issue"
                        + System.lineSeparator()
                        + "\t With Micronaut Data, you may fix it by using the @Join annotation on your repository interface:"
                        + System.lineSeparator()
                        + "\t https://micronaut-projects.github.io/micronaut-data/latest/guide/#joinQueries";
                return message;
            }

        }
}
