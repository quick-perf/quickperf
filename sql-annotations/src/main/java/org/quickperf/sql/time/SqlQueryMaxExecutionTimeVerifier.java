package org.quickperf.sql.time;

import org.quickperf.PerfIssue;
import org.quickperf.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxQueryExecutionTime;

public class SqlQueryMaxExecutionTimeVerifier implements VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> {

	public static final VerifiablePerformanceIssue INSTANCE = new SqlQueryMaxExecutionTimeVerifier();
	
	private SqlQueryMaxExecutionTimeVerifier() {}
	
	@Override
	public PerfIssue verifyPerfIssue(ExpectMaxQueryExecutionTime annotation, ExecutionTime measuredExecutionTime) {
		ExecutionTime expectedSqlExecutionTime = new ExecutionTime(annotation.value(), annotation.unit());
		
		if(expectedSqlExecutionTime.isGreaterThan(measuredExecutionTime)) {
			String message = "Expected sql execution time <" + expectedSqlExecutionTime + "> but was <" + measuredExecutionTime + ">.";
			return new PerfIssue(message);
		}
		return PerfIssue.NONE;
	}
	
	

}
