package org.quickperf;

public interface RetrievableFailure {

    RetrievableFailure NONE = new RetrievableFailure() {
        @Override
        public Throwable find(WorkingFolder workingFolder) {
            return null;
        }
    };

    Throwable find(WorkingFolder workingFolder);

}
