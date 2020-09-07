/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2020 the original author or authors.
 */

package org.quickperf.sql;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickPerfJUnit5SqlDynamicTest {

    @QuickPerfTest
    public static class SqlSelectJUnit5OneDynamicTest extends SqlTestBaseJUnit5 {

        @ExpectSelect(2)
        @TestFactory
        public DynamicTest execute_one_select_but_five_select_expected() {
            return DynamicTest.dynamicTest("Dynamic test", () -> {
                EntityManager em = emf.createEntityManager();
                Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            });
        }
    }

    @QuickPerfTest
    public static class SqlSelectJUnit5MultipleDynamicTest extends SqlTestBaseJUnit5 {
        // The @ExpectSelect(5) will be used by each test, so we will have 2 failing tests here
        @ExpectSelect(2)
        @TestFactory
        public List<DynamicTest> execute_two_select_but_five_select_expected() {
            List<DynamicTest> dynamicTests = new ArrayList<>();
            dynamicTests.add(DynamicTest.dynamicTest("Dynamic test 1", () -> {
                EntityManager em = emf.createEntityManager();
                Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            }));
            dynamicTests.add(DynamicTest.dynamicTest("Dynamic test 2", () -> {
                EntityManager em = emf.createEntityManager();
                Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            }));
            return dynamicTests;
        }
    }

    @QuickPerfTest
    public static class SqlSelectJUnit5OneDynamicContainer extends SqlTestBaseJUnit5 {
        // The @ExpectSelect(5) will be used by each test, so we will have 2 failing tests here
        @ExpectSelect(2)
        @TestFactory
        public DynamicNode execute_two_select_but_five_select_expected() {
            List<DynamicTest> dynamicTests = new ArrayList<>();
            dynamicTests.add(DynamicTest.dynamicTest("Dynamic test 1", () -> {
                EntityManager em = emf.createEntityManager();
                Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            }));
            dynamicTests.add(DynamicTest.dynamicTest("Dynamic test 2", () -> {
                EntityManager em = emf.createEntityManager();
                Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            }));
            return DynamicContainer.dynamicContainer("Dynamic container", dynamicTests);
        }
    }

    @QuickPerfTest
    public static class SqlSelectJUnit5MultipleDynamicContainer extends SqlTestBaseJUnit5 {
        // The @ExpectSelect(5) will be used by each test, so we will have 4 failing tests here
        @ExpectSelect(2)
        @TestFactory
        public List<DynamicNode> execute_four_select_but_five_select_expected() {
            List<DynamicNode> dynamicNodes = new ArrayList<>();
            List<DynamicTest> dynamicTests = new ArrayList<>();
            dynamicTests.add(DynamicTest.dynamicTest("Dynamic test 1", () -> {
                EntityManager em = emf.createEntityManager();
                Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            }));
            dynamicTests.add(DynamicTest.dynamicTest("Dynamic test 2", () -> {
                EntityManager em = emf.createEntityManager();
                Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            }));
            dynamicNodes.add(DynamicContainer.dynamicContainer("Dynamic container 1", dynamicTests));
            dynamicNodes.add(DynamicContainer.dynamicContainer("Dynamic container 2", dynamicTests));
            return dynamicNodes;
        }
    }

    @Test public void
    should_fail_if_a_sql_performance_property_is_un_respected_on_a_single_dynamic_test() {

        // GIVEN
        Class<?> testClass = SqlSelectJUnit5OneDynamicTest.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("You may think that <2> select statements were sent to the database")
                               .contains("But in fact <1>...")
                               .contains("select")
                               .contains("book0_.id")
                               .contains("from")
                               .contains("Book book0_");

    }

    @Test public void
    should_fail_if_a_sql_performance_property_is_un_respected_on_all_tests_dynamic_test() {

        // GIVEN
        Class<?> testClass = SqlSelectJUnit5MultipleDynamicTest.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(2);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("You may think that <2> select statements were sent to the database")
                .contains("But in fact <1>...")
                .contains("select")
                .contains("book0_.id")
                .contains("from")
                .contains("Book book0_");

    }

    @Test public void
    should_fail_if_a_sql_performance_property_is_un_respected_on_all_tests_dynamic_test_of_a_single_container() {

        // GIVEN
        Class<?> testClass = SqlSelectJUnit5OneDynamicContainer.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(2);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("You may think that <2> select statements were sent to the database")
                .contains("But in fact <1>...")
                .contains("select")
                .contains("book0_.id")
                .contains("from")
                .contains("Book book0_");

    }

    @Test public void
    should_fail_if_a_sql_performance_property_is_un_respected_on_all_tests_dynamic_test_of_all_containers() {

        // GIVEN
        Class<?> testClass = SqlSelectJUnit5MultipleDynamicContainer.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(4);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("You may think that <2> select statements were sent to the database")
                .contains("But in fact <1>...")
                .contains("select")
                .contains("book0_.id")
                .contains("from")
                .contains("Book book0_");

    }

}
