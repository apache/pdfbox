package org.apache.fontbox.ttf.gsub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class CompoundCharacterTokenizerTest
{

    @Test
    public void testTokenize_happyPath()
    {

        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "HrkJj", "68RetP", "Yx!23uyt" })));
        String text = "12345HrkJjxabbcc68RetPxxxcfb1245678Yx!23uyt889000";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        StringBuilder sb = new StringBuilder();
        for (String token : tokens)
        {
            sb.append(token);
        }

        assertEquals(text, sb.toString());
        assertEquals(tokens,
                Arrays.asList("12345", "HrkJj", "xabbcc", "68RetP", "xxxcfb1245678", "Yx!23uyt",
                        "889000"));
    }

    @Test
    public void testTokenize_regexAtStart()
    {
        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "HrkJj", "68RetP", "Yx!23uyt" })));
        String text = "Yx!23uyte12345HrkJjxabbcc68RetPxxxcfb1245678Yx!23uyt889000";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        StringBuilder sb = new StringBuilder();
        for (String token : tokens)
        {
            sb.append(token);
        }

        assertEquals(text, sb.toString());

        List<String> tokenList = tokens;

        assertEquals(0, tokenList.indexOf("Yx!23uyt"));
    }

    @Test
    public void testTokenize_regexAtEnd()
    {
        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(new String[] { "HrkJj", "68RetP", "Yx!23uyt" })));
        String text = "Yx!23uyte12345HrkJjxabbcc68RetPxxxcfb1245678Yx!23uyt889000HrkJj";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        StringBuilder sb = new StringBuilder();
        for (String token : tokens)
        {
            sb.append(token);
        }

        assertEquals(text, sb.toString());
        assertEquals(0, tokens.indexOf("Yx!23uyt"));
        assertEquals(2, tokens.indexOf("HrkJj"));
        assertEquals(tokens.size() - 1, tokens.lastIndexOf("HrkJj"));
    }

    @Test
    public void testTokenize_Bangla()
    {
        // given
        CompoundCharacterTokenizer tokenizer = new CompoundCharacterTokenizer(
                new HashSet<>(Arrays.asList(
                        new String[] { "\u0995\u09cd\u09b7", "\u09aa\u09c1\u09a4\u09c1" })));
        String text = "\u0986\u09ae\u09bf \u0995\u09cb\u09a8 \u09aa\u09a5\u09c7  \u0995\u09cd\u09b7\u09c0\u09b0\u09c7\u09b0 \u09b7\u09a8\u09cd\u09a1  \u09aa\u09c1\u09a4\u09c1\u09b2 \u09b0\u09c1\u09aa\u09cb  \u0997\u0999\u09cd\u0997\u09be \u098b\u09b7\u09bf";

        // when
        List<String> tokens = tokenizer.tokenize(text);

        // then
        StringBuilder sb = new StringBuilder();
        for (String token : tokens)
        {
            sb.append(token);
        }

        assertEquals(text, sb.toString());

        List<String> tokenList = tokens;

        assertTrue(tokenList.contains("\u0995\u09cd\u09b7"));
        assertTrue(tokenList.contains("\u09aa\u09c1\u09a4\u09c1"));
    }

}
