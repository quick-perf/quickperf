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

package org.quickperf.spring;import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.test.context.ContextConfiguration;

@RunWith(QuickPerfSpringRunner.class)
@ContextConfiguration(initializers = TestApplicationContextInitializer.class)
public class ClassAnnotatedWithQPSpring5RunnerAndWithTwoMethodsHavingFunctionalAndPerfIssues {

    @ExpectSelect(1)
    @Test public void
    a_first_failing_test() {
        throw new AssertionError("Failing assertion of first test!");
    }

    @ExpectSelect(1)
    @Test public void
    a_second_failing_test() {
        throw new AssertionError("Failing assertion of second test!");
    }

}
