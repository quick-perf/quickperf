package org.quickperf.jvm.jmc.value;

class StringWidthAdapter {

    private final int expectedStringSize;

    StringWidthAdapter(int expectedStringSize) {
        this.expectedStringSize = expectedStringSize;
    }

    String adapt(String string) {
        String spacesToAdd = "";
        int stringLength = string.length();
        if(stringLength < expectedStringSize) {
            for (int i = 0; i < expectedStringSize - stringLength ; i++) {
                spacesToAdd += " ";
            }
        }
        return string + spacesToAdd;
    }

}
