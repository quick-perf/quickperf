/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.sql.time;

import java.util.concurrent.TimeUnit;

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.time.ExecutionTime;

public class SqlQueryExecutionTimeExtractor implements ExtractablePerformanceMeasure<SqlExecutions, ExecutionTime> {

	public static final SqlQueryExecutionTimeExtractor INSTANCE = new SqlQueryExecutionTimeExtractor();
	
	private SqlQueryExecutionTimeExtractor() {}

	@Override
	public ExecutionTime extractPerfMeasureFrom(SqlExecutions perfRecord) {
		
		long maxExecutionTime = 0;
		
		for (SqlExecution execution : perfRecord) {

			long executionTime = execution.getElapsedTime();

			if(executionTime > maxExecutionTime) {
				maxExecutionTime = executionTime;
			}

		}
		
		return new ExecutionTime(maxExecutionTime, TimeUnit.MILLISECONDS);
	}

}
