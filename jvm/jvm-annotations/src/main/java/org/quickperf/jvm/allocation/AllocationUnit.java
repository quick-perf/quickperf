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

package org.quickperf.jvm.allocation;

public enum AllocationUnit {

      BYTE(1) {
          @Override
          public String shortFormat() {
              return "bytes";
          }
          @Override
          public String toString() {
              return "bytes";
          }
      }
    , KILO_BYTE(1024) {
        @Override
        public String shortFormat() {
            return "KiB";
        }
        @Override
        public String toString() {
            return "Kilo bytes";
        }
    }
    , MEGA_BYTE(1024 * 1024) {
        @Override
        public String shortFormat() {
            return "MiB";
        }
        @Override
        public String toString() {
            return "Mega bytes";
        }
    }
    , GIGA_BYTE(1024 * 1024 * 1024) {
        @Override
        public String shortFormat() {
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

    public abstract String shortFormat();

}
