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

package org.quickperf.jvm.allocation.bytewatcher;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * From https://github.com/danielshaya/ByteWatcher/blob/master/src/main/java/org/octtech/bw/ByteWatcherSingleThread.java
 * A class to measure how much allocation there has been on an
 * individual thread.  The class would be useful to embed into
 * regression tests to make sure that there has been no
 * unintended allocation.
 */
public class ByteWatcherSingleThread {
  private static final String ALLOCATED = " allocated ";
  private static final String GET_THREAD_ALLOCATED_BYTES =
      "getThreadAllocatedBytes";
  private static final String[] SIGNATURE =
      new String[]{long.class.getName()};
  private static final MBeanServer mBeanServer;
  private static final ObjectName name;

  static {
    try {
      name = new ObjectName(
          ManagementFactory.THREAD_MXBEAN_NAME);
      mBeanServer = ManagementFactory.getPlatformMBeanServer();
    } catch (MalformedObjectNameException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private final Thread thread;
  private final Object[] PARAMS;
  private final AtomicLong allocated = new AtomicLong();
  private final long MEASURING_COST_IN_BYTES; // usually 336
  private final long tid;
  private final boolean checkThreadSafety;

  public ByteWatcherSingleThread() {
    this(Thread.currentThread(), true);
  }

  public ByteWatcherSingleThread(Thread thread) {
    this(thread, false);
  }

  private ByteWatcherSingleThread(
          Thread thread, boolean checkThreadSafety) {
    this.checkThreadSafety = checkThreadSafety;
    this.tid = thread.getId();
    this.thread = thread;
    PARAMS = new Object[]{tid};

    long calibrate = threadAllocatedBytes();
    // calibrate
    for (int repeats = 0; repeats < 10; repeats++) {
      for (int i = 0; i < 10_000; i++) {
        // run a few loops to allow for startup anomalies
        calibrate = threadAllocatedBytes();
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    MEASURING_COST_IN_BYTES = threadAllocatedBytes() - calibrate;
    reset();
  }

  public long getMeasuringCostInBytes() {
    return MEASURING_COST_IN_BYTES;
  }

  public void reset() {
    checkThreadSafety();

    allocated.set(threadAllocatedBytes());
  }

  long threadAllocatedBytes() {
    try {
      return (long) mBeanServer.invoke(
          name,
          GET_THREAD_ALLOCATED_BYTES,
          PARAMS,
          SIGNATURE
      );
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Calculates the number of bytes allocated since the last
   * reset().
   */
  public long calculateAllocations() {
    checkThreadSafety();
    long mark1 = ((threadAllocatedBytes() -
        MEASURING_COST_IN_BYTES) - allocated.get());
    return mark1;
  }

  private void checkThreadSafety() {
    if (checkThreadSafety &&
        tid != Thread.currentThread().getId())
      throw new IllegalStateException(
          "AllocationMeasure must not be " +
              "used over more than 1 thread.");
  }

  public Thread getThread() {
    return thread;
  }

  public String toString() {
    return thread.getName() + ALLOCATED + calculateAllocations();
  }
}