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

import org.quickperf.measure.AbstractComparablePerfMeasure;

public class ExecutionTime extends AbstractComparablePerfMeasure<ExecutionTime> {
	
	private final Long value;
	
	private final TimeUnit unit;
	
	private static final String NO_COMMENT = "";
	
	public ExecutionTime(Long expectedTimeValue, TimeUnit timeUnit) {
		this.value = expectedTimeValue;
		this.unit = timeUnit;
	}
	
	@Override
	public String getComment() {
		return NO_COMMENT;
	}
	
	@Override
	public Object getUnit() {
		return this.unit;
	}
	
	@Override
	public Object getValue() {
		return this.value;
	}
	
	@Override
	public int compareTo(ExecutionTime o) {
		long currentTimeInNanos =  TimeUnit.NANOSECONDS.convert(value, unit);
		long otherTimeInNanos = TimeUnit.NANOSECONDS.convert(o.value, o.unit);
		return Long.compare(currentTimeInNanos, otherTimeInNanos);
	}
	
	@Override
	public String toString() {
		return this.unit.toMillis(value) + " ms";
	}

}
