package org.quickperf.testng.sql;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestNGMethodFailing {

    @Test
    public void a_failing_test() {
        Assert.assertEquals(true, false);
    }

}
