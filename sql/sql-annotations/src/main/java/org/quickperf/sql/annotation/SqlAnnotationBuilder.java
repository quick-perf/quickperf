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

package org.quickperf.sql.annotation;

import java.lang.annotation.Annotation;

/**
 * <p>This class helps to build SQL annotations with a global scope.</p>
 *
 *@see <a href="https://github.com/quick-perf/doc/wiki/QuickPerf#annotation-scopes"><i>QuickPerf annotations
 *scopes</i></a>
 *@see org.quickperf.config.SpecifiableGlobalAnnotations
 */
public class SqlAnnotationBuilder {

    private SqlAnnotationBuilder() {}

    /**
     *Allows to build {@link org.quickperf.sql.annotation.DisableLikeWithLeadingWildcard} annotation.
     */
    public static DisableLikeWithLeadingWildcard disableLikeWithLeadingWildcard() {
        return new DisableLikeWithLeadingWildcard() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableLikeWithLeadingWildcard.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.DisableSameSelectTypesWithDifferentParamValues} annotation.
     */
    public static DisableSameSelectTypesWithDifferentParamValues disableSameSelectTypesWithDifferentParams() {
        return new DisableSameSelectTypesWithDifferentParamValues() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableSameSelectTypesWithDifferentParamValues.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.DisableExactlySameSelects} annotation.
     */
    public static DisableExactlySameSelects disableExactlySameSelects() {
        return new DisableExactlySameSelects() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableExactlySameSelects.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.DisableStatements} annotation.
     */
    public static DisableStatements disableStatements() {
        return new DisableStatements() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableStatements.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.DisableQueriesWithoutBindParameters} annotation.
     */
    public static DisableQueriesWithoutBindParameters disableQueriesWithoutBindParameters() {
        return new DisableQueriesWithoutBindParameters() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableQueriesWithoutBindParameters.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectJdbcBatching} annotation.
     */
    public static ExpectJdbcBatching expectJdbcBatching() {
        return new ExpectJdbcBatching() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectJdbcBatching.class;
            }
            @Override
            public int batchSize() {
                return -1;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectJdbcBatching} annotation.
     */
    public static ExpectJdbcBatching expectJdbcBatching(final int batchSize) {
        return new ExpectJdbcBatching() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectJdbcBatching.class;
            }
            @Override
            public int batchSize() {
                return batchSize;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectJdbcQueryExecution} annotation.
     */
    public static ExpectJdbcQueryExecution expectJdbcQueryExecution(final int value) {
        return new ExpectJdbcQueryExecution() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectJdbcQueryExecution.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectMaxJdbcQueryExecution} annotation.
     */
    public static ExpectMaxJdbcQueryExecution expectMaxJdbcQueryExecution(final int value) {
        return new ExpectMaxJdbcQueryExecution() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxJdbcQueryExecution.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectMaxSelectedColumn} annotation.
     */
    public static ExpectMaxSelectedColumn expectMaxSelectedColumn(final int value) {
        return new ExpectMaxSelectedColumn() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxSelectedColumn.class;
            }
            @Override

            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectSelectedColumn} annotation.
     */
    public static ExpectSelectedColumn expectSelectedColumn(final int value) {
        return new ExpectSelectedColumn() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectSelectedColumn.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectMaxInsert} annotation.
     */
    public static ExpectMaxInsert expectMaxInsert(final int value) {
        return new ExpectMaxInsert() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxInsert.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectMaxSelect} annotation.
     */
    public static ExpectMaxSelect expectMaxSelect(final int value) {
        return new ExpectMaxSelect() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxSelect.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectDelete} annotation.
     */
    public static ExpectDelete expectDelete(final int value) {
        return new ExpectDelete() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectDelete.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectInsert} annotation.
     */
    public static ExpectInsert expectInsert(final int value) {
        return new ExpectInsert() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectInsert.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectSelect} annotation.
     */
    public static ExpectSelect expectSelect(final int value) {
        return new ExpectSelect() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectSelect.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectUpdate} annotation.
     */
    public static ExpectUpdate expectUpdate(final int value) {
        return new ExpectUpdate() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectUpdate.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectMaxUpdate} annotation.
     */
    public static ExpectMaxUpdate expectMaxUpdate(final int value) {
        return new ExpectMaxUpdate() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxUpdate.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectMaxUpdatedColumn} annotation.
     */
    public static ExpectMaxUpdatedColumn expectMaxUpdatedColumn(final int value) {
        return new ExpectMaxUpdatedColumn() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxUpdatedColumn.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectMaxQueryExecutionTime} annotation.
     */
    public static ExpectMaxQueryExecutionTime expectMaxQueryExecutionTime(final int thresholdInMilliSeconds) {
        return new ExpectMaxQueryExecutionTime() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxQueryExecutionTime.class;
            }

            @Override
            public int thresholdInMilliSeconds() {
                return thresholdInMilliSeconds;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.DisplaySql} annotation.
     */
    public static DisplaySql displaySql() {
        return new DisplaySql() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisplaySql.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.DisplaySqlOfTestMethodBody} annotation.
     */
    public static DisplaySqlOfTestMethodBody displaySqlOfTestMethodBody() {
        return new DisplaySqlOfTestMethodBody() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisplaySqlOfTestMethodBody.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectUpdatedColumn} annotation.
     */
    public static ExpectUpdatedColumn expectUpdatedColumn(final int value) {
        return new ExpectUpdatedColumn() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectUpdatedColumn.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.sql.annotation.ExpectMaxDelete} annotation.
     */
    public static ExpectMaxDelete expectMaxDelete(final int value) {
        return new ExpectMaxDelete() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxDelete.class;
            }
            @Override
            public int value() {
                return value;
            }
        };
    }

}
