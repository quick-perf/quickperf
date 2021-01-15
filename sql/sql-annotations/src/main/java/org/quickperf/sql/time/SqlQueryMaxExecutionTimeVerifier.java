/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql.time;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxQueryExecutionTime;
import org.quickperf.time.ExecutionTime;

import java.util.concurrent.TimeUnit;

public class SqlQueryMaxExecutionTimeVerifier implements VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> {
	
	public static final SqlQueryMaxExecutionTimeVerifier INSTANCE = new SqlQueryMaxExecutionTimeVerifier();

	private SqlQueryMaxExecutionTimeVerifier() {}

	@Override
	public PerfIssue verifyPerfIssue(ExpectMaxQueryExecutionTime annotation, ExecutionTime measure) {
		
		ExecutionTime maxExpectedSqlExecutionTime = new ExecutionTime(annotation.thresholdInMilliSeconds(), TimeUnit.MILLISECONDS);
		
		if(measure.isGreaterThan(maxExpectedSqlExecutionTime)) {
			return buildPerfIssue(measure, maxExpectedSqlExecutionTime);
		}
		
		return PerfIssue.NONE;
	}

	private PerfIssue buildPerfIssue(ExecutionTime effectiveExecutionTime, ExecutionTime maxExecutionTime) {
		String description =
				"Query execution time expected to be less than <" +maxExecutionTime.toString() + ">"
						+ "\n	At least one query has a greater execution time. The greater query execution time is <" + effectiveExecutionTime.toString() + ">";
		return new PerfIssue(description);
	}

}
