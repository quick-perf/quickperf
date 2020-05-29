package org.quickperf.testng.jvm.jmc;

import org.quickperf.jvm.annotations.ProfileJvm;
import org.testng.annotations.Test;

public class AllocationRateProfileJVMTest {

  @ProfileJvm
  @Test
  public void allocationRateOutputFormattedWithProfileJVMAnnotation() {
    for (int i = 0; i < 1_000_000; i++) {
      int[] arr = new int[256];
    }
  }

  @ProfileJvm
  @Test
  public void allocationRateProfileJVMAnnotationNoAllocationInMethod() {
    //nothing
  }

}
