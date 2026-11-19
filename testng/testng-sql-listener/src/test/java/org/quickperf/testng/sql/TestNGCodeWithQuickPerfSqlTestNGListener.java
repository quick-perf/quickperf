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
package org.quickperf.testng.sql;

import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.perfrecording.TestThreads;
import org.quickperf.testng.QuickPerfSqlTestNGListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Listeners({QuickPerfSqlTestNGListener.class})
public class TestNGCodeWithQuickPerfSqlTestNGListener {

    @BeforeClass
    public void before_class() {
        assertIsATestThread("@BeforeClass method");
    }

    @BeforeMethod
    public void before_method() {
        assertIsATestThread("@BeforeMethod method");
    }

    @Test
    public void a_test() {
        assertIsATestThread("test method");
    }

    @DisableQuickPerf
    @Test
    public void a_test_with_quickperf_disabled() {
        assertIsATestThread("test method with QuickPerf disabled");
    }

    @AfterMethod
    public void after_method() {
        assertIsATestThread("@AfterMethod method");
    }

    @AfterClass
    public void after_class() {
        assertIsATestThread("@AfterClass method");
    }

    private static void assertIsATestThread(String testCode) {
        assertThat(TestThreads.isCurrentThreadMarked())
                .as("test thread executing the " + testCode + ": " + Thread.currentThread().getName())
                .isTrue();
    }

}
