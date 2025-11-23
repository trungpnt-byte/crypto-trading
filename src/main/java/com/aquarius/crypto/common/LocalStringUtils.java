package com.aquarius.crypto.common;

import java.util.function.Predicate;

public class LocalStringUtils {

    /**
     * Strips characters from the start of the string until the predicate returns true.
     * example : stripUntil("Bearer abcdef", Character::isWhitespace) -> "abcdef"
     *
     * @param str
     * @param predicate
     * @return
     */
    public static String stripUntil(String str, Predicate<Character> predicate) {
        int len = str.length(), i = 0;
        char[] result = new char[len];
        for (; i < len; i++) {
            if (predicate.test(str.charAt(i))) {
                while (i < len && predicate.test(str.charAt(i))) {
                    i++;
                }
                break;
            }
        }
        int idx = 0;
        for (; i < len; i++) {
            result[idx++] = str.charAt(i);
        }
        return new String(result, 0, idx);
    }

    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        for (int i = 0, n = str.length(); i < n; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNoneBlank(String str) {
        return str != null && !isBlank(str);
    }
}
