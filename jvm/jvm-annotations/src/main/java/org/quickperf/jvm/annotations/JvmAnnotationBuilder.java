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

package org.quickperf.jvm.annotations;

import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.gc.GC;
import org.quickperf.writer.DefaultWriterFactory;
import org.quickperf.writer.WriterFactory;

import java.lang.annotation.Annotation;

/**
 * <p>This class helps to build JVM annotations with a global scope.</p>
 *
 *@see <a href="https://github.com/quick-perf/doc/wiki/QuickPerf#annotation-scopes"><i>QuickPerf annotations
 *scopes</i></a>
 *@see org.quickperf.config.SpecifiableGlobalAnnotations
 */
public class JvmAnnotationBuilder {

    private JvmAnnotationBuilder() {}

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.HeapSize} annotation.
     */
    public static HeapSize heapSize(final int value, final AllocationUnit unit) {
        return new HeapSize() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return HeapSize.class;
            }
            @Override
            public int value() {
                return value;
            }
            @Override
            public AllocationUnit unit() {
                return unit;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.JvmOptions} annotation.
     */
    public static JvmOptions jvmOptions(final String value) {
        return new JvmOptions() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return JvmOptions.class;
            }
            @Override
            public String value() {
                return value;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.UseGC} annotation.
     */
    public static UseGC useGC(final GC gc) {
        return new UseGC() {
            @Override
            public GC value() {
                return gc;
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return UseGC.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.EnableGcLogging} annotation.
     */
    public static EnableGcLogging enableGcLogging() {
        return new EnableGcLogging(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return EnableGcLogging.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.ExpectMaxHeapAllocation} annotation.
     */
    public static ExpectMaxHeapAllocation expectMaxHeapAllocation(final double value, final AllocationUnit unit) {
        return new ExpectMaxHeapAllocation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxHeapAllocation.class;
            }
            @Override
            public double value() {
                return value;
            }
            @Override
            public AllocationUnit unit() {
                return unit;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.MeasureHeapAllocation} annotation.
     */
    public static MeasureHeapAllocation measureHeapAllocation(
              final String format
            , final Class<? extends WriterFactory> writerFactoryClass) {
        return new MeasureHeapAllocation() {
            @Override
            public String format() {
                return format;
            }

            @Override
            public Class<? extends WriterFactory> writerFactory() {
                return writerFactoryClass;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return MeasureHeapAllocation.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.MeasureHeapAllocation} annotation.
     */
    public static MeasureHeapAllocation measureHeapAllocation() {
        return measureHeapAllocation(MeasureHeapAllocation.QUICK_PERF_MEASURED_HEAP_ALLOCATION_DEFAULT_FORMAT, DefaultWriterFactory.class);
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.MeasureHeapAllocation} annotation.
     */
    public static MeasureHeapAllocation measureHeapAllocation(final String format) {
        return measureHeapAllocation(format, DefaultWriterFactory.class);
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.MeasureHeapAllocation} annotation.
     */
    public static MeasureHeapAllocation measureHeapAllocation(final Class<? extends WriterFactory> writerFactoryClass) {
        return measureHeapAllocation(MeasureHeapAllocation.QUICK_PERF_MEASURED_HEAP_ALLOCATION_DEFAULT_FORMAT, writerFactoryClass);
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.MeasureRSS} annotation.
     */
    public static MeasureRSS measureRSS(){
        return  new MeasureRSS() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return MeasureRSS.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.ExpectMaxRSS} annotation.
     */
    public static ExpectMaxRSS expectMaxRSS(final double value, final AllocationUnit unit) {
        return new ExpectMaxRSS() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxRSS.class;
            }
            @Override
            public double value() {
                return value;
            }
            @Override
            public AllocationUnit unit() {
                return unit;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.ExpectNoHeapAllocation} annotation.
     */
    public static ExpectNoHeapAllocation expectNoHeapAllocation() {
        return new ExpectNoHeapAllocation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectNoHeapAllocation.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.Xms} annotation.
     */
    public static Xms xms(final int value, final AllocationUnit unit) {
        return new Xms() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Xms.class;
            }
            @Override
            public int value() {
                return value;
            }
            @Override
            public AllocationUnit unit() {
                return unit;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.jvm.annotations.Xmx} annotation.
     */
    public static Xmx xmx(final int value, final AllocationUnit unit) {
        return new Xmx() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return Xmx.class;
            }
            @Override
            public int value() {
                return value;
            }
            @Override
            public AllocationUnit unit() {
                return unit;
            }
        };
    }

}
