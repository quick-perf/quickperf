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
import java.util.concurrent.TimeUnit;

public class SqlAnnotationBuilder {

    private SqlAnnotationBuilder() {}

    public static DisableLikeWithLeadingWildcard disableLikeWithLeadingWildcard() {
        return new DisableLikeWithLeadingWildcard() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableLikeWithLeadingWildcard.class;
            }
        };
    }

    public static DisableCrossJoin disableCrossJoin() {
        return new DisableCrossJoin() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableCrossJoin.class;
            }
        };
    }

    public static DisableSameSelectTypesWithDifferentParamValues disableSameSelectTypesWithDifferentParams() {
        return new DisableSameSelectTypesWithDifferentParamValues() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableSameSelectTypesWithDifferentParamValues.class;
            }
        };
    }

    public static DisableExactlySameSelects disableExactlySameSelects() {
        return new DisableExactlySameSelects() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableExactlySameSelects.class;
            }
        };
    }

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

    public static ExpectMaxQueryExecutionTime expectMaxQueryExecutionTime(final int value, final TimeUnit unit) {
        return new ExpectMaxQueryExecutionTime() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxQueryExecutionTime.class;
            }
            @Override
            public long value() {
                return value;
            }
            @Override
            public TimeUnit unit() {
                return unit;
            }
        };
    }

    public static DisplaySql displaySql() {
        return new DisplaySql() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisplaySql.class;
            }
        };
    }

    public static DisplaySqlOfTestMethodBody displaySqlOfTestMethodBody() {
        return new DisplaySqlOfTestMethodBody() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisplaySqlOfTestMethodBody.class;
            }
        };
    }

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
}
