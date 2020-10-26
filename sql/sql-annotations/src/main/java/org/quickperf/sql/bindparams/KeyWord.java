package org.quickperf.sql.bindparams;

public enum KeyWord {
    IN(" in "),
    BETWEEN(" between "),
    VALUES(" values "),
    SET(" set "),
    OTHER(" other ");
    private final String keyWord;

    KeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getValue() {
        return keyWord;
    }
}
