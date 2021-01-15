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

package org.quickperf.jvm.jmc.value;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openjdk.jmc.common.item.*;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.QuantityConversionException;
import org.openjdk.jmc.common.unit.StructContentType;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationRateTest {

  private IItemCollection mockedJfrEvents;
  private Iterator mockedJfrEventsIterator;
  private IItemIterable mockedAllocationEvents;
  private Iterator mockedAllocationEventsIterator;
  private IQuantity mockedTotalAlloc;
  private IItem mockedEvent;
  private IItem mockedEvent2;
  private IItem mockedEvent3;
  private StructContentType mockedIType;
  private IMemberAccessor mockedIMemberAccessor;
  private IQuantity mockedIQuantity;

  /**
   * Set up an IItemCollection of jfr events.
   * <p>
   * Set up the jfrEvents with a total allocation value of 1 KiB.
   * <p>
   * The first three values returned from mockedIQuantity.longValueIn() are used in minTimeStamp()
   * of allocation events inside Tlab.The next three elements are used in minTimeStamp() of
   * allocation events outside Tlab.
   * <p>
   * The next three elements are used in maxTimeStamp() of allocation events inside Tlab. The next
   * three elements are used in maxTimeStamp() of allocation events outside Tlab.
   * <p>
   * Example for test 1:
   * <p>
   * minInside = min(1000,2000,3000)
   * minOutside = min(10_000, 10_000, 11_000)
   * min of both = 1000
   * <p>
   * maxInside = max(1000,2000,3000)
   * maxOutside = max(10_000, 10_000, 11_000)
   * max of both = 11_000
   * <p>
   * duration = 11_000 - 1000 = 10_000 ms
   * <p>
   * Allocation rate = 1024 bytes / 10 seconds
   * 102.4 KiB/s
   * <p>
   */

  @Before
  @SuppressWarnings("unchecked")
  public void setUpIItemCollection() throws QuantityConversionException {
    mockedJfrEvents = Mockito.mock(IItemCollection.class);
    mockedJfrEventsIterator = Mockito.mock(Iterator.class);
    mockedTotalAlloc = Mockito.mock(IQuantity.class);
    mockedAllocationEvents = Mockito.mock(IItemIterable.class);
    mockedAllocationEventsIterator = Mockito.mock(Iterator.class);
    mockedEvent = Mockito.mock(IItem.class);
    mockedEvent2 = Mockito.mock(IItem.class);
    mockedEvent3 = Mockito.mock(IItem.class);
    mockedIType = Mockito.mock(StructContentType.class);
    mockedIMemberAccessor = Mockito.mock(IMemberAccessor.class);
    mockedIQuantity = Mockito.mock(IQuantity.class);

    Mockito.when(mockedJfrEvents.hasItems()).thenReturn(true);

    Mockito.when(mockedTotalAlloc.longValue()).thenReturn(1024L);

    Mockito.when(mockedJfrEvents.getAggregate(ArgumentMatchers.any(IAggregator.class))).thenReturn(mockedTotalAlloc);

    Mockito.when(mockedJfrEvents.apply(JdkFilters.ALLOC_INSIDE_TLAB)).thenReturn(mockedJfrEvents);
    Mockito.when(mockedJfrEvents.apply(JdkFilters.ALLOC_OUTSIDE_TLAB)).thenReturn(mockedJfrEvents);

    Mockito.when(mockedJfrEvents.iterator())
            .thenReturn(mockedJfrEventsIterator, mockedJfrEventsIterator, mockedJfrEventsIterator,
                    mockedJfrEventsIterator);
    Mockito.when(mockedJfrEventsIterator.next())
            .thenReturn(mockedAllocationEvents, mockedAllocationEvents, mockedAllocationEvents,
                    mockedAllocationEvents);
    Mockito.when(mockedJfrEventsIterator.hasNext())
            .thenReturn(true, false, true, false, true, false, true, false);

    Mockito.when(mockedAllocationEvents.iterator())
            .thenReturn(mockedAllocationEventsIterator, mockedAllocationEventsIterator,
                    mockedAllocationEventsIterator,
                    mockedAllocationEventsIterator);
    Mockito.when(mockedAllocationEventsIterator.next())
            .thenReturn(mockedEvent, mockedEvent2, mockedEvent3, mockedEvent, mockedEvent2,
                    mockedEvent3, mockedEvent, mockedEvent2, mockedEvent3);
    Mockito.when(mockedAllocationEventsIterator.hasNext())
            .thenReturn(true, true, true, false, true, true, true, false, true, true, true, false, true,
                    true, true, false);

    Mockito.when(mockedEvent.getType()).thenReturn(mockedIType);
    Mockito.when(mockedEvent2.getType()).thenReturn(mockedIType);
    Mockito.when(mockedEvent3.getType()).thenReturn(mockedIType);

    Mockito.when(mockedIType.getAccessor(ArgumentMatchers.any(IAccessorKey.class))).thenReturn(mockedIMemberAccessor);

    Mockito.when(mockedIMemberAccessor.getMember(ArgumentMatchers.any(IItem.class))).thenReturn(mockedIQuantity);

    Mockito.when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
            .thenReturn(1000L, 2000L, 3000L, 10_000L, 10_000L, 11_000L, 1000L, 2000L, 3000L, 10000L,
                    11000L, 11000L);
  }

  /**
   * 1 KiB over 10 seconds.
   */
  @Test
  public void should_format_100_bytes_per_second_as_string() {

    Mockito.when(mockedTotalAlloc.longValue()).thenReturn(1024L);

    assertThat(ProfilingInfo.ALLOCATION_RATE.formatAsString(mockedJfrEvents))
            .isEqualTo("102 B/s");

  }

  /**
   * 1 MiB over 10 seconds.
   */
  @Test
  public void should_format_100_ki_b_per_second_as_string() {

    Mockito.when(mockedTotalAlloc.longValue()).thenReturn(1024L * 1024L);

    assertThat(ProfilingInfo.ALLOCATION_RATE.formatAsString(mockedJfrEvents))
            .isEqualTo("102 KiB/s");

  }

  /**
   * 1 GiB over 10 seconds.
   */
  @Test
  public void should_format_100_mi_b_per_second_as_string() {

    Mockito.when(mockedTotalAlloc.longValue()).thenReturn((long) Math.pow(1024L, 3));

    assertThat(ProfilingInfo.ALLOCATION_RATE.formatAsString(mockedJfrEvents))
            .isEqualTo("102 MiB/s");

  }

  /**
   * 1 TiB over 10 seconds.
   */
  @Test
  public void should_format_100_giga_bytes_per_second_as_string() {

    Mockito.when(mockedTotalAlloc.longValue()).thenReturn((long) Math.pow(1024, 4));

    assertThat(ProfilingInfo.ALLOCATION_RATE.formatAsString(mockedJfrEvents))
            .isEqualTo("102 GiB/s");

  }

  /**
   * Difference between allocation time stamps is zero, therefore the rate should return " ".
   */
  @Test
  public void should_return_an_empty_string_if_all_zero_time_stamps() throws QuantityConversionException {

    Mockito.when(mockedTotalAlloc.longValue()).thenReturn(1000L);
    Mockito.when(mockedIQuantity.longValueIn(UnitLookup.EPOCH_MS))
            .thenReturn(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L,
                    0L);

    assertThat(ProfilingInfo.ALLOCATION_RATE.formatAsString(mockedJfrEvents))
            .isEqualTo(" ");

  }

}