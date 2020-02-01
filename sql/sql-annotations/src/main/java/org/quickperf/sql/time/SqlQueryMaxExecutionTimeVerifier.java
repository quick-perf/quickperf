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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.sql.time;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxQueryExecutionTime;

public class SqlQueryMaxExecutionTimeVerifier implements VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> {
	
	public static final SqlQueryMaxExecutionTimeVerifier INSTANCE = new SqlQueryMaxExecutionTimeVerifier();
	

	private SqlQueryMaxExecutionTimeVerifier() {
	}

	@Override
	public PerfIssue verifyPerfIssue(ExpectMaxQueryExecutionTime annotation, ExecutionTime measure) {
		
		ExecutionTime expectedSqlExecutionTime = new ExecutionTime(annotation.value(), annotation.unit());
		
		if(measure.isGreaterThan(expectedSqlExecutionTime)) {
			
			String message = "Expected sql execution time <" + expectedSqlExecutionTime + "> but was <" + measure + ">.";
			
			return new PerfIssue(message);
		}
		
		return PerfIssue.NONE;
	}

}
