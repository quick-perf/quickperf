package org.quickperf.time;

class ExecutionTimeWarning {

    static final ExecutionTimeWarning INSTANCE = new ExecutionTimeWarning();

    private ExecutionTimeWarning() { }

    @Override
    public String toString() {
        String warning = "\u26A0";
        String tilde = "\u007E";
        return    System.lineSeparator() + warning + " " + "Be cautious with this result. It is a rough and first level result."
                + System.lineSeparator() + "Data has no meaning below the " + tilde + " second/millisecond."
                + System.lineSeparator() + "JIT warm-up, GC, or safe point scan impact the measure and its reproducibility."
                + System.lineSeparator() + "We recommend JMH to do more in-depth experiments: https://openjdk.java.net/projects/code-tools/jmh.";
    }

}
