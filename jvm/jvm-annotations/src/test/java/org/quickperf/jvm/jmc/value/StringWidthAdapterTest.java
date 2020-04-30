package org.quickperf.jvm.jmc.value;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringWidthAdapterTest {

    @Test
    public void should_add_spaces() {
        StringWidthAdapter stringWidthAdapter = new StringWidthAdapter(3);
        assertThat(stringWidthAdapter.adapt("s")).isEqualTo("s  ");
    }

}