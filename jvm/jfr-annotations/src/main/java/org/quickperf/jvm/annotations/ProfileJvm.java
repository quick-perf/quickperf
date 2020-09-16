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

package org.quickperf.jvm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>ProfileJvm</code> annotation To profile the JVM with the JDK Flight Recorder (JFR). The console displays
 * the path to the recording file. You can open this file with Java Mission Control.<p>
 * &#064;ProfileJvm also displays some JVM profiling data (GC times, heap allocation estimation, exception numbers, ...)
 * in standard output.
 * <p>
 * <h4>Example:</h4>
 * <pre>
 *      <b>&#064;ProfileJvm
 *      &#064;Test</b>
 *      public void execute() {
 *          <code>...</code>
 *      }
 * </pre>
 * <p>
 * <p>
 * QuickPerf will give the following type of feedback on the console:
 * <code>
 * <p>------------------------------------------------------------------------------
 * <p> ALLOCATION (estimations)     |   GARBAGE COLLECTION           |  THROWABLE
 * <p> Total       : 3,68 GiB       |   Total pause     : 1,264 s    |  Exception: 0
 * <p> Inside TLAB : 3,67 GiB       |   Longest GC pause: 206,519 ms |  Error    : 36
 * <p> Outside TLAB: 12,7 MiB       |   Young: 13                    |  Throwable: 36
 * <p> Allocation rate: 108.1 MiB/s |   Old  : 3                     |
 * <p>------------------------------------------------------------------------------
 * <p> COMPILATION                  |   CODE CACHE
 * <p> Number : 157                 |   The number of full code cache events: 0
 * <p> Longest: 1,615 s             |
 * <p>------------------------------------------------------------------------------
 * <p> JVM
 * <p> Name     : OpenJDK 64-Bit Server VM
 * <p> Version  : OpenJDK 64-Bit Server VM (11.0.1+13) for windows-amd64 JRE (11.0.1+13), built on Oct  6 2018 13:18:13 by "mach5one" with MS VC++ 15.5 (VS2017)
 * <p> Arguments: -Xms6g -Xmx6g -XX:+FlightRecorder -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=C:\Users\JEANBI~1\AppData\Local\Temp\QuickPerf-1155358826815951142\heap-dump.hprof -DquickPerfToExecInASpecificJvm=true -DquickPerfWorkingFolder=C:\Users\JEANBI~1\AppData\Local\Temp\QuickPerf-1155358826815951142
 * <p>------------------------------------------------------------------------------
 * <p> HARDWARE
 * <p> Hardware threads: 8
 * <p> Cores           : 4
 * <p> Sockets         : 1
 * <p> CPU
 * <p>		Brand: Intel(R) Core(TM) i7-8550U CPU @ 1.80GHz, Vendor: GenuineIntel
 * <p>		Family: <unknown> (0x6), Model: <unknown> (0x8e), Stepping: 0xa
 * <p>		Ext. family: 0x0, Ext. model: 0x8, Type: 0x0, Signature: 0x000806ea
 * <p>		Features: ebx: 0x03100800, ecx: 0xfedaf387, edx: 0xbfebfbff
 * <p>		Ext. features: eax: 0x00000000, ebx: 0x00000000, ecx: 0x00000121, edx: 0x2c100800
 * <p>		Supports: On-Chip FPU, Virtual Mode Extensions, Debugging Extensions, Page Size Extensions, Time Stamp Counter, Model Specific Registers, Physical Address Extension, Machine Check Exceptions, CMPXCHG8B Instruction, On-Chip APIC, Fast System Call, Memory Type Range Registers, Page Global Enable, Machine Check Architecture, Conditional Mov Instruction, Page Attribute Table, 36-bit Page Size Extension, CLFLUSH Instruction, Debug Trace Store feature, ACPI registers in MSR space, Intel Architecture MMX Technology, Fast Float Point Save and Restore, Streaming SIMD extensions, Streaming SIMD extensions 2, Self-Snoop, Hyper Threading, Thermal Monitor, Streaming SIMD Extensions 3, PCLMULQDQ, 64-bit DS Area, Enhanced Intel SpeedStep technology, Thermal Monitor 2, Supplemental Streaming SIMD Extensions 3, Fused Multiply-Add, CMPXCHG16B, xTPR Update Control, Perfmon and Debug Capability, Process-context identifiers, Streaming SIMD extensions 4.1, Streaming SIMD extensions 4.2, MOVBE, Popcount instruction, AESNI, XSAVE, OSXSAVE, AVX, F16C, LAHF/SAHF instruction support, Advanced Bit Manipulations: LZCNT, SYSCALL/SYSRET, Execute Disable Bit, RDTSCP, Intel 64 Architecture, Invariant TSC
 * <p>------------------------------------------------------------------------------
 * </code>
 * <h4>Note:</h4>
 * Where to find JDK Mission Control (JMC)?
 * There are several ways to get JMC:<p>
 * - A jmc executable is available in the bin folder of some Oracle JDK (10 >= version >= 1.7u40),<p>
 * - With Oracle,<p>
 * - With <a href="https://www.azul.com/products/zulu-mission-control/">Azul Zulu</a> Mission Control,<p>
 * - With <a href="https://www.google.com/search?q=Download+Liberica+Mission+Control">Liberica</a> Mission Control,<p>
 * - With Red Hat:<p>
 * <code>yum install rh-jmc</code><p>
 * The last JMC release can be found <a href="https://adoptopenjdk.net/jmc.html">here</a> (AdoptOpenJDK).
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ProfileJvm {
}