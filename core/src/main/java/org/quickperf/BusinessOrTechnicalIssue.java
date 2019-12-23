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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf;

import java.io.Serializable;
import java.util.List;

public class BusinessOrTechnicalIssue implements Serializable {

    public static final BusinessOrTechnicalIssue NONE = new BusinessOrTechnicalIssue(null);

    private final Throwable throwable;

    private BusinessOrTechnicalIssue(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public static BusinessOrTechnicalIssue buildFrom(Throwable throwable) {
        return new BusinessOrTechnicalIssue(throwable);
    }

    public static BusinessOrTechnicalIssue buildFrom(List<Throwable> throwables) {

        if (noThrowables(throwables)) {
            return BusinessOrTechnicalIssue.NONE;
        }

        if(throwables.size() == 1) {
            Throwable throwable = throwables.get(0);
            return new BusinessOrTechnicalIssue(throwable);
        }

        return convertThrowablesIntoToBusinessOrTechnicalIssue(throwables);

    }

    private static boolean noThrowables(List<Throwable> throwables) {
        return throwables == null || throwables.isEmpty();
    }

    private static BusinessOrTechnicalIssue convertThrowablesIntoToBusinessOrTechnicalIssue(List<Throwable> throwables) {
        Throwable firstThrowable = throwables.get(0);

        for (int i = 2; i < throwables.size(); i++) {
            Throwable throwable = throwables.get(i);
            firstThrowable.addSuppressed(throwable);
        }

        return new BusinessOrTechnicalIssue(firstThrowable);
    }

    public boolean isNone() {
        return throwable == null;
    }

}
