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
 * Copyright 2019-2022 the original author or authors.
 */
package org.quickperf.perfrecording;

import org.quickperf.issue.PerfIssue;

/**
 * A {@link PerfRecord} able to tell whether the recording itself can be
 * trusted, independently of the annotations verified against it. A result
 * other than {@link PerfIssue#NONE} fails the test even if every
 * annotation-level verification passes. The issue is reported before the
 * annotation-level issues; an issue with the same description reported by
 * several records of a test is reported once, preferably with a record
 * formatting its perf issues itself.
 */
public interface SelfVerifiablePerfRecord extends PerfRecord {

    PerfIssue verifyRecord();

}
