package org.quickperf.sql.time;

import java.util.concurrent.TimeUnit;

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;

public class SqlQueryExecutionTimeExtractor implements ExtractablePerformanceMeasure<SqlExecutions, ExecutionTime> {
	
	public static final SqlQueryExecutionTimeExtractor INSTANCE = new SqlQueryExecutionTimeExtractor();
	
	private SqlQueryExecutionTimeExtractor() {}
	
	@Override
	public ExecutionTime extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
		
		long maxExecutionTime = 0;
		
		for (SqlExecution sqlExecution : sqlExecutions) {
			
			long executionTime = sqlExecution.getElapsedTime();
			
			if(executionTime > maxExecutionTime) {
				maxExecutionTime = executionTime;
			}
		}
		
		return new ExecutionTime(maxExecutionTime, TimeUnit.MILLISECONDS);
		
	}

}
