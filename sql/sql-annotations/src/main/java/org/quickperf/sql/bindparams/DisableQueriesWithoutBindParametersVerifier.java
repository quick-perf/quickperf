package org.quickperf.sql.bindparams;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.annotation.DisableQueriesWithoutBindParameters;

public class DisableQueriesWithoutBindParametersVerifier implements VerifiablePerformanceIssue<DisableQueriesWithoutBindParameters, BooleanMeasure> {

    public static final DisableQueriesWithoutBindParametersVerifier INSTANCE = new DisableQueriesWithoutBindParametersVerifier();

    private DisableQueriesWithoutBindParametersVerifier() {
    }

    @Override
    public PerfIssue verifyPerfIssue(final DisableQueriesWithoutBindParameters annotation, final BooleanMeasure areAllParametersBound) {
        if(!areAllParametersBound.getValue()) {
            return new PerfIssue("Unbound parameters detected");
        }

        return PerfIssue.NONE;
    }
}
