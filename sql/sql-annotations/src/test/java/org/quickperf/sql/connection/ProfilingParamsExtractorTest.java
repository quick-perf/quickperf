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
package org.quickperf.sql.connection;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.quickperf.sql.annotation.ProfileConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class ProfilingParamsExtractorTest {

    @ProfileConnection(displayStackTrace = true, stackDepth = -2)
    private static class ProfileConnectionWithStackDepthLowerThanMinusOne { }

    @Test public void
    should_throw_an_illegal_argument_exception_if_the_stack_depth_is_lower_than_minus_one() {

        // GIVEN
        final ProfileConnection profileConnection =
                ProfileConnectionWithStackDepthLowerThanMinusOne.class.getAnnotation(ProfileConnection.class);
        final ProfilingParamsExtractor profilingParamsExtractor = new ProfilingParamsExtractor();

        // WHEN
        Throwable throwable = catchThrowable(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() {
                profilingParamsExtractor.extractFrom(profileConnection);
            }
        });

        // THEN
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class)
                             .hasMessage("The stackDepth of @ProfileConnection should be a positive value"
                                       + " or -1 to not limit the stack depth, but it is -2.");

    }

}
