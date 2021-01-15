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

package org.quickperf.time;

import org.quickperf.measure.AbstractComparablePerfMeasure;

import java.util.concurrent.TimeUnit;

public class ExecutionTime extends AbstractComparablePerfMeasure<ExecutionTime> {
	
	private final Long value;
	
	private final TimeUnit unit;

	public ExecutionTime(Long value, TimeUnit timeUnit) {
		this.value = value;
		this.unit = timeUnit;
	}

	public ExecutionTime(int value, TimeUnit unit) {
		this.value = (long) value;
		this.unit = unit;
	}
	
	@Override
	public String getComment() {
		return "";
	}
	
	@Override
	public TimeUnit getUnit() {
		return this.unit;
	}
	
	@Override
	public Long getValue() {
		return this.value;
	}
	
	@Override
	public int compareTo(ExecutionTime other) {
		long currentTimeInNanos =  TimeUnit.NANOSECONDS.convert(value, unit);
		long otherTimeInNanos = TimeUnit.NANOSECONDS.convert(other.value, other.unit);
		return Long.compare(currentTimeInNanos, otherTimeInNanos);
	}
	
	@Override
	public String toString() {
		return this.unit.toMillis(value) + " ms";
	}

}
