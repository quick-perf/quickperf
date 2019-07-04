package org.quickperf.sql.time;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;

public class SqlQueryExecutionTimeExtractor implements ExtractablePerformanceMeasure<SqlExecutions, ExecutionTime> {
	
	public static final SqlQueryExecutionTimeExtractor INSTANCE = new SqlQueryExecutionTimeExtractor();
	
	private SqlQueryExecutionTimeExtractor() {}
	
	@Override
	public ExecutionTime extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
		
		long maxExcutionTime = 0;
		
		Iterator<SqlExecution> sqlExecutionsIt = sqlExecutions.iterator();
		
		while (sqlExecutionsIt.hasNext()) {
			
			SqlExecution sqlExecution = sqlExecutionsIt.next();
			
			long executionTime = sqlExecution.getElapsedTime();
			
			if(executionTime > maxExcutionTime) {
				maxExcutionTime = executionTime;
			}
		}
		
		return new ExecutionTime(maxExcutionTime, TimeUnit.MILLISECONDS);
	}

}
