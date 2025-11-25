package com.aquarius.crypto.common;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocalStringUtilsTest {
    @Test
    void testBasicExample() {
        String input = "Bearer abcdef";
        String output = LocalStringUtils.stripUntil(input, Character::isWhitespace);
        assertEquals("abcdef", output);
    }

    @Test
    void testNoPredicateMatchReturnsEmpty() {
        String input = "BearerToken";
        String output = LocalStringUtils.stripUntil(input, Character::isWhitespace);
        assertEquals("", output);
    }

    @Test
    void testPredicateAtStartSkipsLeadingCharacters() {
        String input = "   abc";
        String output = LocalStringUtils.stripUntil(input, Character::isWhitespace);
        assertEquals("abc", output);
    }

    @Test
    void testMultipleWhitespaceSegments() {
        String input = "Bearer    abc   def";
        String output = LocalStringUtils.stripUntil(input, Character::isWhitespace);
        assertEquals("abc   def", output);
    }

    @Test
    void testPredicateMatchAtEndMeansEmptyResult() {
        String input = "Bearer ";
        String output = LocalStringUtils.stripUntil(input, Character::isWhitespace);
        assertEquals("", output);
    }

    @Test
    void testEmptyStringReturnsEmpty() {
        String input = "";
        String output = LocalStringUtils.stripUntil(input, Character::isWhitespace);
        assertEquals("", output);
    }

    @Test
    void testPredicateNonWhitespace() {
        String input = "xyzabc123DEF";
        Predicate<Character> isDigit = Character::isDigit;

        // Strips until '1', then skips all digits, then returns "DEF"
        String output = LocalStringUtils.stripUntil(input, isDigit);
        assertEquals("DEF", output);
    }

    @Test
    void testPredicateImmediatelyMatches() {
        String input = "1abc";
        Predicate<Character> isDigit = Character::isDigit;

        String output = LocalStringUtils.stripUntil(input, isDigit);
        assertEquals("abc", output);
    }

    @Test
    void testPredicateMatchesOnlyAtEnd() {
        String input = "abc1";
        Predicate<Character> isDigit = Character::isDigit;

        // last char is digit → all skipped, nothing left
        String output = LocalStringUtils.stripUntil(input, isDigit);
        assertEquals("", output);
    }

    @Test
    void testNullStringThrowsNullPointer() {
        assertThrows(NullPointerException.class,
                () -> LocalStringUtils.stripUntil(null, Character::isWhitespace));
    }

    @Test
    void testNullPredicateThrowsNullPointer() {
        assertThrows(NullPointerException.class,
                () -> LocalStringUtils.stripUntil("abc", null));
    }
}
