package org.quickperf.sql.insert;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxInsert;
import org.quickperf.sql.framework.HibernateSuggestion;
import org.quickperf.sql.framework.JdbcSuggestion;
import org.quickperf.sql.framework.SqlFrameworksInClassPath;
import org.quickperf.unit.Count;

public class MaxOfInsertsPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectMaxInsert, Count> {

    public static final MaxOfInsertsPerfIssueVerifier INSTANCE = new MaxOfInsertsPerfIssueVerifier();

    private MaxOfInsertsPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectMaxInsert annotation, Count measuredCount) {

        Count expectedCount = new Count(annotation.value());

         if(measuredCount.isGreaterThan(expectedCount)) {
            return buildPerfIssue(measuredCount, expectedCount);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(Count measuredCount, Count expectedCount) {

        String description = "You may think that at most <" + expectedCount.getValue() + "> insert statement"
                           + (expectedCount.getValue() > 1 ? "s were" : " was" )
                           + " sent to the database"
                           + System.lineSeparator()
                           + "       " + "But in fact <" + measuredCount.getValue() + ">..."
                           + System.lineSeparator()
                           + System.lineSeparator()
                           + JdbcSuggestion.SERVER_ROUND_TRIPS.getMessage()
                           + System.lineSeparator()
                           ;

        if(SqlFrameworksInClassPath.INSTANCE.containsHibernate()) {
            String nPlusOneInsertMessage = HibernateSuggestion.N_PLUS_ONE_INSERT
                                                              .getMessage();
            description += nPlusOneInsertMessage;
        }

        return new PerfIssue(description);

    }

}