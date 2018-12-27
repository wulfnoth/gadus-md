package org.wulfnoth.md;

import java.io.Serializable;

public class CustomLabel implements Serializable {

    public static final int KEEP = 0;

    private String label;
    private boolean inline;
    private int behave;

    public CustomLabel(String label, boolean inline, int behave) {
        this.label = label;
        this.inline = inline;
        this.behave = behave;
    }

    public String getLabel() {
        return label;
    }

    public boolean isInline() {
        return inline;
    }

    public int getBehave() {
        return behave;
    }
}
