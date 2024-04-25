package org.apache.pdfbox.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class StringUtilTest
{

    @Test
    void testSplitOnSpace_happyPath()
    {
        String[] result = StringUtil.splitOnSpace("a b c");
        assertArrayEquals(new String[] {"a", "b", "c"}, result);
    }

    @Test
    void testSplitOnSpace_emptyString()
    {
        String[] result = StringUtil.splitOnSpace("");
        assertArrayEquals(new String[] {""}, result);
    }

    @Test
    void testSplitOnSpace_onlySpaces()
    {
        String[] result = StringUtil.splitOnSpace("   ");
        assertArrayEquals(new String[] {}, result);
    }

    @Test
    void testTokenizeOnSpace_happyPath()
    {
        String[] result = StringUtil.tokenizeOnSpace("a b c");
        assertArrayEquals(new String[] {"a", " ", "b", " ", "c"}, result);
    }

    @Test
    void testTokenizeOnSpace_emptyString()
    {
        String[] result = StringUtil.tokenizeOnSpace("");
        assertArrayEquals(new String[] {""}, result);
    }

    @Test
    void testTokenizeOnSpace_onlySpaces()
    {
        String[] result = StringUtil.tokenizeOnSpace("   ");
        assertArrayEquals(new String[] {" ", " ", " "}, result);
    }

    @Test
    void testTokenizeOnSpace_onlySpacesWithText()
    {
        String[] result = StringUtil.tokenizeOnSpace("  a  ");
        assertArrayEquals(new String[] {" ", " ", "a", " ", " "}, result);
    }

}