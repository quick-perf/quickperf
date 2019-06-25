package org.quickperf.sql.time;

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.sql.SqlExecutions;

public class SqlQueryExecutionTimeExtractor implements ExtractablePerformanceMeasure<SqlExecutions, ExecutionTime> {
	
	public static final SqlQueryExecutionTimeExtractor INSTANCE = new SqlQueryExecutionTimeExtractor();
	
	private SqlQueryExecutionTimeExtractor() {}
	
	@Override
	public ExecutionTime extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
		
		return null;
	}

}
