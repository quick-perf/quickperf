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
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.annotation.DisableLikeWithLeadingWildcard;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class DisableLikeWithLeadingWildcardTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithDisableLikeWithLeadingWildcardAndGeneratingLikePercentage extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_who_started_with_like_wildcard() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE  '%Ja'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_if_select_containing_like_with_percentage_leading_wildcard() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithDisableLikeWithLeadingWildcardAndGeneratingLikePercentage.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithDisableLikeWithLeadingWildcardAndGeneratingLikeUnderscore extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_who_started_with_like_wildcard() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE  '_Ja'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_if_select_containing_like_with_underscore_leading_wildcard() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithDisableLikeWithLeadingWildcardAndGeneratingLikeUnderscore.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithLeadingWildcardAndTabSeparator extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_wildcard_and_tab_separator() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE\t'%foo'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_if_select_containing_like_with_leading_wildcard_and_tab_separator() {

        // GIVEN
        Class<?> testClass = LikeWithLeadingWildcardAndTabSeparator.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithLeadingWildcardInsideParentheses extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_wildcard_inside_parentheses() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE ('%foo')");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_if_select_containing_like_with_leading_wildcard_inside_parentheses() {

        // GIVEN
        Class<?> testClass = LikeWithLeadingWildcardInsideParentheses.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithLeadingWildcardInBindParameter extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_wildcard_in_bind_parameter() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE ?");
            nativeQuery.setParameter(1, "%foo");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_if_like_leading_wildcard_is_in_bind_parameter_value() {

        // GIVEN
        Class<?> testClass = LikeWithLeadingWildcardInBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeInsideStringLiteralShouldNotTrigger extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_in_string_literal() {

            EntityManager em = emf.createEntityManager();

            // The string "LIKE '%something'" appears inside a literal value, not as a SQL keyword
            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.title = 'I LIKE ''%wildcards'''");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_not_fail_when_like_with_wildcard_appears_inside_string_literal() {

        // GIVEN
        Class<?> testClass = LikeInsideStringLiteralShouldNotTrigger.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithBindParamButWildcardInOtherParam extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_wildcard_in_non_like_param() {

            EntityManager em = emf.createEntityManager();

            // Param 1 (isbn =) is "%fiction" which starts with %, but is NOT a LIKE param.
            // Param 2 (title LIKE) is "Java" which has no leading wildcard.
            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.isbn = ? AND b.title LIKE ?");
            nativeQuery.setParameter(1, "%fiction");
            nativeQuery.setParameter(2, "Java");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_not_fail_when_wildcard_is_in_non_like_bind_parameter() {

        // GIVEN
        Class<?> testClass = LikeWithBindParamButWildcardInOtherParam.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeBindParamWithQuestionMarkInStringLiteral extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_question_mark_in_string_literal_before_like_bind_param() {

            EntityManager em = emf.createEntityManager();

            // The '?' inside 'really?' is string content, not a bind parameter.
            // The only bind param is the one after LIKE.
            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.isbn = 'really?' AND b.title LIKE ?");
            nativeQuery.setParameter(1, "%foo");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_when_like_bind_param_has_wildcard_and_string_literal_contains_question_mark() {

        // GIVEN
        Class<?> testClass = LikeBindParamWithQuestionMarkInStringLiteral.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithEscapedQuoteThenWildcard extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_pattern_starting_with_escaped_quote() {

            EntityManager em = emf.createEntityManager();

            // Pattern '''%foo' means the 5-char string: '%foo (apostrophe + %foo)
            // The leading char is a literal apostrophe, NOT a wildcard
            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.title LIKE '''%foo'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_not_fail_when_like_pattern_starts_with_escaped_quote_then_wildcard() {

        // GIVEN
        Class<?> testClass = LikeWithEscapedQuoteThenWildcard.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithBindParamSetOutOfOrder extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_bind_param_set_out_of_order() throws Exception {

            try (Connection connection = getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT * FROM Book b WHERE b.isbn = ? AND b.title LIKE ?")) {
                ps.setString(1, "123");
                ps.setString(2, "%foo");
                ps.executeQuery();
            }

        }

    }

    @Test public void
    should_fail_when_like_bind_param_with_wildcard_is_set_out_of_order() {

        // GIVEN
        Class<?> testClass = LikeWithBindParamSetOutOfOrder.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class FunctionNameContainingLikeAfterUnderscore extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_function_name_containing_like_after_underscore() throws Exception {

            try (Connection connection = getConnection();
                 java.sql.Statement stmt = connection.createStatement()) {
                // Create a function whose name contains _like — not the LIKE keyword
                stmt.execute("CREATE ALIAS IF NOT EXISTS x_like FOR \"java.lang.String.valueOf(java.lang.Object)\"");
                stmt.executeQuery("SELECT x_like('%foo') FROM Book b");
            }

        }

    }

    @Test public void
    should_not_fail_when_function_name_contains_like_after_underscore() {

        // GIVEN
        Class<?> testClass = FunctionNameContainingLikeAfterUnderscore.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithLeadingWildcardAndNewlineSeparator extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_wildcard_and_newline_separator() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE\n'%foo'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_if_select_containing_like_with_leading_wildcard_and_newline_separator() {

        // GIVEN
        Class<?> testClass = LikeWithLeadingWildcardAndNewlineSeparator.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithBindParamSetToEmptyString extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_bind_param_set_to_empty_string() throws Exception {

            try (Connection connection = getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT * FROM Book b WHERE b.title LIKE ?")) {
                ps.setString(1, "");
                ps.executeQuery();
            }

        }

    }

    @Test public void
    should_not_fail_when_like_bind_parameter_is_empty_string() {

        // GIVEN
        Class<?> testClass = LikeWithBindParamSetToEmptyString.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LowercaseLikeWithLeadingWildcard extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_lowercase_like_and_leading_wildcard() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title like '%foo'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_if_select_containing_lowercase_like_with_leading_wildcard() {

        // GIVEN
        Class<?> testClass = LowercaseLikeWithLeadingWildcard.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class MixedCaseLikeWithLeadingWildcard extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_mixed_case_like_and_leading_wildcard() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LiKe '%foo'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_if_select_containing_mixed_case_like_with_leading_wildcard() {

        // GIVEN
        Class<?> testClass = MixedCaseLikeWithLeadingWildcard.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class IdentifierContainingLikeAsSuffix extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_identifier_ending_in_like() throws Exception {

            try (Connection connection = getConnection();
                 java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS reaction(id INT, dislike VARCHAR(100))");
                // "dislike" is a column name, not the LIKE keyword
                stmt.executeQuery("SELECT * FROM reaction WHERE dislike = '%foo'");
            }

        }

    }

    @Test public void
    should_not_fail_when_identifier_contains_like_as_suffix() {

        // GIVEN
        Class<?> testClass = IdentifierContainingLikeAsSuffix.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class IdentifierContainingLikeAsPrefix extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_identifier_starting_with_like() throws Exception {

            try (Connection connection = getConnection();
                 java.sql.Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS stats(id INT, likelihood DOUBLE)");
                // "likelihood" is a column name, not the LIKE keyword
                stmt.executeQuery("SELECT * FROM stats WHERE likelihood > 0");
            }

        }

    }

    @Test public void
    should_not_fail_when_identifier_contains_like_as_prefix() {

        // GIVEN
        Class<?> testClass = IdentifierContainingLikeAsPrefix.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class MultipleLikeFirstHasLeadingWildcard extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_two_likes_where_first_has_wildcard() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.title LIKE '%foo' AND b.isbn LIKE 'bar%'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_when_first_of_two_likes_has_leading_wildcard() {

        // GIVEN
        Class<?> testClass = MultipleLikeFirstHasLeadingWildcard.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class MultipleLikeSecondHasLeadingWildcard extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_two_likes_where_second_has_wildcard() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.title LIKE 'foo%' AND b.isbn LIKE '%bar'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_when_second_of_two_likes_has_leading_wildcard() {

        // GIVEN
        Class<?> testClass = MultipleLikeSecondHasLeadingWildcard.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class MultipleLikeNoneHasLeadingWildcard extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_two_likes_neither_has_leading_wildcard() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.title LIKE 'foo%' AND b.isbn LIKE 'bar%'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_not_fail_when_no_like_has_leading_wildcard() {

        // GIVEN
        Class<?> testClass = MultipleLikeNoneHasLeadingWildcard.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithBindParamSetToNull extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_bind_param_set_to_null() throws Exception {

            try (Connection connection = getConnection();
                 PreparedStatement ps = connection.prepareStatement(
                         "SELECT * FROM Book b WHERE b.title LIKE ?")) {
                ps.setString(1, null);
                ps.executeQuery();
            }

        }

    }

    @Test public void
    should_not_fail_when_like_bind_parameter_is_null() {

        // GIVEN
        Class<?> testClass = LikeWithBindParamSetToNull.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeAfterLineCommentWithQuote extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_after_line_comment_containing_quote() {

            EntityManager em = emf.createEntityManager();

            // The -- comment contains an apostrophe (don't) which must not toggle inString
            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b\n-- don't touch\nWHERE b.title LIKE '%foo'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_when_like_follows_line_comment_containing_quote() {

        // GIVEN
        Class<?> testClass = LikeAfterLineCommentWithQuote.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeAfterBlockCommentWithQuote extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_after_block_comment_containing_quote() {

            EntityManager em = emf.createEntityManager();

            // The /* */ comment contains an apostrophe which must not toggle inString
            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b /* it's a comment */ WHERE b.title LIKE '%foo'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_fail_when_like_follows_block_comment_containing_quote() {

        // GIVEN
        Class<?> testClass = LikeAfterBlockCommentWithQuote.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeInsideLineComment extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_inside_line_comment() {

            EntityManager em = emf.createEntityManager();

            // LIKE '%foo' is inside the comment
            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.title = 'bar' -- LIKE '%foo'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_not_fail_when_like_is_inside_line_comment() {

        // GIVEN
        Class<?> testClass = LikeInsideLineComment.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeInsideBlockComment extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_inside_block_comment() {

            EntityManager em = emf.createEntityManager();

            // LIKE '%foo' is inside the block comment — should NOT trigger
            Query nativeQuery = em.createNativeQuery(
                    "SELECT * FROM Book b WHERE b.title = 'bar' /* LIKE '%foo' */");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_not_fail_when_like_is_inside_block_comment() {

        // GIVEN
        Class<?> testClass = LikeInsideBlockComment.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class LikeWithTrailingWildcardOnly extends SqlTestBase {

        @Test
        @DisableLikeWithLeadingWildcard
        public void execute_select_with_like_trailing_wildcard_only() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE 'foo%'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    should_not_fail_when_like_has_only_trailing_wildcard() {

        // GIVEN
        Class<?> testClass = LikeWithTrailingWildcardOnly.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

}
