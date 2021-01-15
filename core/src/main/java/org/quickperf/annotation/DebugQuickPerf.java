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

package org.quickperf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>DebugQuickPerf</code> annotation allows to debug QuickPerf execution by displaying information on the
 * console. For example, this annotation gives the JVM options added by QuickPerf.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *     <b>&#064;DebugQuickPerf</b>
 *      <b>&#064;RunWith(QuickPerfJUnitRunner.class)</b>
 *      public static class ADebuggedClass extends SqlTest
 *
 *           <b>&#064;Test</b>
 *           <b>&#064;ExpectMaxQueryExecutionTime(thresholdInMilliSeconds = 2)</b>
 *           <b>&#064;ExpectMaxSelect(5)</b>
 *           <b>&#064;ProfileJvm</b>
 *           public void retrieveSomething() {
 *              <code>..</code>
 *           }
 *
 *       }
 * </pre>
 * <pre>
 * QuickPerf will give the following feedback on the console:
 * [QUICK PERF] [DEBUG]
 *
 * JVM OPTIONS
 * -XX:+FlightRecorder
 * -XX:+UnlockDiagnosticVMOptions
 * -XX:+DebugNonSafepoints
 * -XX:+HeapDumpOnOutOfMemoryError
 * -XX:HeapDumpPath=/tmp/QuickPerf-7240108834516423370/heap-dump.hprof
 *
 * PRIORITY OF RECORDERS EXECUTED BEFORE TEST METHOD
 * ---- | -----------------------------------------
 * Prio | Recorder
 * ---- | -----------------------------------------
 * 2000 | org.quickperf.sql.PersistenceSqlRecorder
 * 2001 | org.quickperf.sql.display.DisplaySqlRecorder
 * 2002 | org.quickperf.sql.display.DisplaySqlOfTestMethodBodyRecorder
 * 2003 | org.quickperf.sql.batch.SqlStatementBatchRecorder
 * 5070 | org.quickperf.jvm.rss.ProcessStatusRecorder
 * 6000 | org.quickperf.jvm.jfr.JfrEventsRecorder
 * 6030 | org.quickperf.jvm.allocation.bytewatcher.ByteWatcherRecorder
 *
 * PRIORITY OF RECORDERS EXECUTED AFTER TEST METHOD
 * ---- | -----------------------------------------
 * Prio | Recorder
 * ---- | -----------------------------------------
 * 3000 | org.quickperf.jvm.allocation.bytewatcher.ByteWatcherRecorder
 * 3030 | org.quickperf.jvm.jfr.JfrEventsRecorder
 * 3060 | org.quickperf.jvm.rss.ProcessStatusRecorder
 * 7000 | org.quickperf.sql.PersistenceSqlRecorder
 * 7001 | org.quickperf.sql.display.DisplaySqlRecorder
 * 7002 | org.quickperf.sql.display.DisplaySqlOfTestMethodBodyRecorder
 * 7003 | org.quickperf.sql.batch.SqlStatementBatchRecorder
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DebugQuickPerf {
}
