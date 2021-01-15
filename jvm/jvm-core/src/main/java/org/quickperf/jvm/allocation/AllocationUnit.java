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

package org.quickperf.jvm.allocation;

/**
 * An <code>AllocationUnit</code> represents RAM allocations at a given unit of granularity.
 */
public enum AllocationUnit {

    /**
     * Allocation unit representing one byte
     */
    BYTE(1) {
          @Override
          public String toShortString() {
              return "B";
          }
          @Override
          public String toString() {
              return "bytes";
          }
      }
    ,
    /**
     * Allocation unit representing 1024 bytes
     */
    KILO_BYTE(1024) {
        @Override
        public String toShortString() {
            return "KiB";
        }
        @Override
        public String toString() {
            return "Kilo bytes";
        }
    }
    ,
    /**
     * Allocation unit representing 1024<sup>2</sup> bytes
     */
    MEGA_BYTE(1024 * 1024) {
        @Override
        public String toShortString() {
            return "MiB";
        }
        @Override
        public String toString() {
            return "Mega bytes";
        }
    }
    ,
    /**
     * Allocation unit representing 1024<sup>3</sup> bytes
     */
    GIGA_BYTE(1024 * 1024 * 1024) {
        @Override
        public String toShortString() {
            return "GiB";
        }
        @Override
        public String toString() {
            return "Giga bytes";
        }
    }
    ;

    private final int valueInBytes;

    AllocationUnit(int valueInBytes) {
        this.valueInBytes = valueInBytes;
    }

    public int getValueInBytes() {
        return valueInBytes;
    }

    public abstract String toShortString();

}
