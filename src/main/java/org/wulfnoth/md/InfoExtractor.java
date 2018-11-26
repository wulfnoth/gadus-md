package org.wulfnoth.md;

public class InfoExtractor {

    public static String extractHeader1(String line) {
        if (line.startsWith("# ") && line.length() > 2)
            return line.substring(2);
        return null;
    }

}
