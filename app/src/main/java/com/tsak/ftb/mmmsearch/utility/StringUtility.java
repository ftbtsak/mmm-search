package com.tsak.ftb.mmmsearch.utility;

import java.util.regex.Pattern;

public class StringUtility {

    private StringUtility() {
        throw new AssertionError();
    }

    public static String substringByByteLen(String target, int byteLen) {
        if (1 > byteLen) {
            return "";
        }
        int sum = 0;
        char[] chars = target.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int len = (1 == String.valueOf(chars[i]).getBytes().length
                    || Pattern.matches("^[ｦ-ﾟ]*$", String.valueOf(chars[i])))
                    ? 1 : 2;
            if(sum + len >= byteLen) {
                return target.substring(0, i + 1);
            }
            sum += len;
        }
        return target;
    }

    public static String toHexString(int target) {
        return "0x" + String.format("%08x", target).toUpperCase();
    }
}
