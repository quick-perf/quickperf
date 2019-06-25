package org.quickperf.sql.time;

import java.util.concurrent.TimeUnit;

import org.quickperf.measure.AbstractComparablePerfMeasure;

public class ExecutionTime extends AbstractComparablePerfMeasure<ExecutionTime>  {
	
	private static final String NO_COMMENT = "";
	
	private final Long value;
	
	private final TimeUnit unit;
	
	public ExecutionTime(Long value, TimeUnit unit) {
		this.value = value;
		this.unit = unit;
	}

	@Override
	public Long getValue() {
		return value;
	}

	@Override
	public TimeUnit getUnit() {
		return unit;
	}

	@Override
	public String getComment() {
		return NO_COMMENT;
	}

	@Override
	public int compareTo(ExecutionTime other) {
		long currentTimeInMs =  unit.toMillis(value);
		long otherTimeInMs = other.unit.toMillis(other.value);
		return (int)(currentTimeInMs - otherTimeInMs);
	}

}
