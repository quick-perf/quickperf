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

package org.quickperf.jvm.heap;

import com.sun.management.HotSpotDiagnosticMXBean;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class HeapDumper {

    private HeapDumper() { }

    /**
     * Dumps the heap.
     * @param fileName
     *        Heap dump file name
     */
    public static void dumpHeap(String fileName) {
        dumpHeap(false, fileName);
    }

    /**
     * Dumps the heap by only keeping the live objects in memory.
     * @param fileName
     *        Heap dump file name
     */
    public static void dumpHeapWithOnlyLiveObjects(String fileName) {
        dumpHeap(true, fileName);
    }

    private static void dumpHeap(boolean live, String heapDumpFilePath) {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean;
        try {
            hotSpotDiagnosticMXBean =
                    ManagementFactory.newPlatformMXBeanProxy(mBeanServer,
                            "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        try {
            hotSpotDiagnosticMXBean.dumpHeap(heapDumpFilePath, live);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        System.out.println();

        String pointingRight = "\uD83D\uDC49";
        System.out.println("[QUICK PERF] Heap dump file " + (live ? "(live=true) " : "")
                         + System.lineSeparator()
                         + pointingRight + " " + heapDumpFilePath);

    }

}
