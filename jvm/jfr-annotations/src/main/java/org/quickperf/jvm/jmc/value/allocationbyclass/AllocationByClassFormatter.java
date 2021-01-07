package org.quickperf.jvm.jmc.value.allocationbyclass;

import org.quickperf.jvm.jmcrule.HtmlToPlainTextTransformer;

public class AllocationByClassFormatter {
    public static final AllocationByClassFormatter INSTANCE = new AllocationByClassFormatter();

    private AllocationByClassFormatter() {}

    public String format(AllocationByClassResult result) {
        final String shortDescription = result.getResult().getShortDescription();
        return HtmlToPlainTextTransformer.INSTANCE.convertHtmlToPlainText(shortDescription);
    }
}
