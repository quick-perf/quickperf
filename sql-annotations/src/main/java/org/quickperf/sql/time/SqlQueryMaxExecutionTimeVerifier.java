package org.quickperf.sql.time;

import org.quickperf.PerfIssue;
import org.quickperf.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxQueryExecutionTime;

public class SqlQueryMaxExecutionTimeVerifier implements VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> {

	public static final VerifiablePerformanceIssue INSTANCE = new SqlQueryMaxExecutionTimeVerifier();
	
	private SqlQueryMaxExecutionTimeVerifier() {}
	
	@Override
	public PerfIssue verifyPerfIssue(ExpectMaxQueryExecutionTime annotation, ExecutionTime measuredExecutionTime) {
		// TODO
		return null;
	}
	
	

}
