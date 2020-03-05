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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxQueryExecutionTime;

public class SqlQueryMaxExecutionTimeVerifierTest {
	
	@Test
	public void should_return_a_perf_issue_if_query_execution_time_is_greater_than_expected () {
		
		VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> verifier = SqlQueryMaxExecutionTimeVerifier.INSTANCE;
		
		ExpectMaxQueryExecutionTime expectedMaxExecutionTime = new ExpectMaxQueryExecutionTime() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return ExpectMaxQueryExecutionTime.class;
			}
			
			@Override
			public long value() {
				return 10L;
			}
			
			@Override
			public TimeUnit unit() {
				return TimeUnit.MILLISECONDS;
			}
		};
		
		ExecutionTime sqlExecTime = new ExecutionTime(50L, TimeUnit.MILLISECONDS);
		
		PerfIssue perfIssue = verifier.verifyPerfIssue(expectedMaxExecutionTime, sqlExecTime);
		
		assertNotEquals(PerfIssue.NONE.getDescription(), perfIssue.getDescription());
		
		assertNotEquals(PerfIssue.NONE, perfIssue);
	}

	@Test
	public void should_return_no_perf_issue_if_query_execution_time_is_less_than_expected () {
		VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> verifier = SqlQueryMaxExecutionTimeVerifier.INSTANCE;
		
		ExpectMaxQueryExecutionTime expectedMaxExecutionTime = new ExpectMaxQueryExecutionTime() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return ExpectMaxQueryExecutionTime.class;
			}
			
			@Override
			public long value() {
				return 1L;
			}
			
			@Override
			public TimeUnit unit() {
				return TimeUnit.MILLISECONDS;
			}
		};
		
		ExecutionTime sqlExecTime = new ExecutionTime(5L, TimeUnit.NANOSECONDS);
		
		PerfIssue perfIssue = verifier.verifyPerfIssue(expectedMaxExecutionTime, sqlExecTime);
		
		assertEquals(PerfIssue.NONE, perfIssue);
		
		assertEquals(PerfIssue.NONE.getDescription(), perfIssue.getDescription());
	}
	
	@Test
	public void should_return_no_perf_issue_if_query_execution_time_is_same_as_expected () {
		VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> verifier = SqlQueryMaxExecutionTimeVerifier.INSTANCE;
		
		ExpectMaxQueryExecutionTime expectedMaxExecutionTime = new ExpectMaxQueryExecutionTime() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return ExpectMaxQueryExecutionTime.class;
			}
			
			@Override
			public long value() {
				return 1L;
			}
			
			@Override
			public TimeUnit unit() {
				return TimeUnit.SECONDS;
			}
		};
		
		ExecutionTime sqlExecTime = new ExecutionTime(1000L, TimeUnit.MILLISECONDS);
		
		PerfIssue perfIssue = verifier.verifyPerfIssue(expectedMaxExecutionTime, sqlExecTime);
		
		assertEquals(PerfIssue.NONE.getDescription(), perfIssue.getDescription());
		
		assertEquals(PerfIssue.NONE, perfIssue);
	}

}
