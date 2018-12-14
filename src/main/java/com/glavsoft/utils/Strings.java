package com.glavsoft.utils;

public class Strings {
    public Strings() {
    }

    public static String toString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder("[");
        boolean notFirst = false;
        byte[] arr$ = byteArray;
        int len$ = byteArray.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            byte b = arr$[i$];
            if (notFirst) {
                sb.append(", ");
            } else {
                notFirst = true;
            }

            sb.append(b);
        }

        return sb.append("]").toString();
    }

    public static boolean isTrimmedEmpty(String s) {
        return null == s || s.trim().length() == 0;
    }
}
