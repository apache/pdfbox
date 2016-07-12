/*
 * Copyright 2016 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Michael Doswald
 */
public class TestHexUtil extends TestCase
{
    
    /**
     * Test conversion from short to char[]
     */
    public void testGetCharsFromShortWithoutPassingInABuffer()
    {
        assertArrayEquals(new char[]{'0','0','0','0'}, Hex.getChars((short)0x0000));
        assertArrayEquals(new char[]{'0','0','0','F'}, Hex.getChars((short)0x000F));
        assertArrayEquals(new char[]{'A','B','C','D'}, Hex.getChars((short)0xABCD));
        assertArrayEquals(new char[]{'B','A','B','E'}, Hex.getChars((short)0xCAFEBABE));
    }

    /**
     * Check conversion from String to a char[] which contains the UTF16-BE encoded
     * bytes of the string as hex digits
     *
     */
    public void testGetCharsUTF16BE()
    {
        assertArrayEquals(new char[]{'0','0','6','1','0','0','6','2'}, Hex.getCharsUTF16BE("ab"));
        assertArrayEquals(new char[]{'5','E','2','E','5','2','A','9'}, Hex.getCharsUTF16BE("帮助"));
    }

    private void assertArrayEquals(char[] expected, char[] actual)
    {
        assertEquals("Length of char array not equal", expected.length, actual.length);
        for (int idx = 0; idx < expected.length; idx++)
        {
            if (expected[idx] != actual[idx])
            {
                fail(String.format("Character at index %d not equal. Expected '%c' but got '%c'", 
                        idx, expected[idx], actual[idx]));
            }
        }
    }

    /**
     * Set the tests in the suite for this test class.
     *
     * @return the Suite.
     */
    public static Test suite()
    {
        return new TestSuite(TestHexUtil.class);
    }

    /**
     * Command line execution.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args)
    {
        String[] arg =
        {
            TestHexUtil.class.getName()
        };
        junit.textui.TestRunner.main(arg);
    }
}
