/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.cos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This will test all of the filters in the PDFBox system.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision$
 */
public class TestCOSString extends TestCOSBase
{
    private final static String ESC_CHAR_STRING =
            "( test#some) escaped< \\chars>!~1239857 ";
    private final static String ESC_CHAR_STRING_PDF_FORMAT =
            "\\( test#some\\) escaped< \\\\chars>!~1239857 ";

    /**
     * This will get the suite of test that this class holds.
     *
     * @return All of the tests that this class holds.
     */
    public static Test suite()
    {
        return new TestSuite(TestCOSString.class);
    }

    public void setUp()
    {
        testCOSBase = new COSString("test cos string");
    }

    /**
     * infamous main method.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args)
    {
        String[] arg = {TestCOSString.class.getName()};
        junit.textui.TestRunner.main(arg);
    }

    /**
     * Tests the public static members within the class that are purely PDF format string objects 
     * like open/closing strings, escape characters etc...
     */
    public void testStaticMembers()
    {
        stringByteArrayComparison("(", COSString.STRING_OPEN);
        stringByteArrayComparison(")", COSString.STRING_CLOSE);
        stringByteArrayComparison("<", COSString.HEX_STRING_OPEN);
        stringByteArrayComparison(">", COSString.HEX_STRING_CLOSE);
        stringByteArrayComparison("\\", COSString.ESCAPE);
        stringByteArrayComparison("\\r", COSString.CR_ESCAPE);
        stringByteArrayComparison("\\n", COSString.LF_ESCAPE);
        stringByteArrayComparison("\\t", COSString.HT_ESCAPE);
        stringByteArrayComparison("\\b", COSString.BS_ESCAPE);
        stringByteArrayComparison("\\f", COSString.FF_ESCAPE);
    }

    /**
     * Helper method for comparing a string to it's PDF byte array.
     * 
     * @param expected the String expected
     * @param member the byte array being tested
     */
    private void stringByteArrayComparison(String expected, byte[] member)
    {
        byte[] expectedBytes = null;
        try
        {
            expectedBytes = expected.getBytes("ISO-8859-1");
        }
        catch (UnsupportedEncodingException e)
        {
            fail("ISO-8859-1 encoding threw an exception: " + e.getMessage());
        }
        testByteArrays(expectedBytes, member);
    }

    /**
     * Test setForceHexForm() and setForceLiteralForm() - tests these two methods do enforce the
     * different String output forms within PDF. 
     */
    public void testSetForceHexLiteralForm()
    {
        String inputString = "Test with a text and a few numbers 1, 2 and 3";
        String pdfLiteral = "(" + inputString + ")";
        String pdfHex = "<" + createHex(inputString) + ">";
        COSString cosStr = new COSString(inputString);
        cosStr.setForceLiteralForm(true);
        writePDFTests(pdfLiteral, cosStr);
        cosStr.setForceHexForm(true);
        writePDFTests(pdfHex, cosStr);
        cosStr.setForceLiteralForm(true);
        writePDFTests(pdfLiteral, cosStr);

        COSString escStr = new COSString(ESC_CHAR_STRING);
        writePDFTests("(" + ESC_CHAR_STRING_PDF_FORMAT + ")", escStr);
        escStr.setForceHexForm(true);
        // Escape characters not escaped in hex version
        writePDFTests("<" + createHex(ESC_CHAR_STRING) + ">", escStr);
    }

    /**
     * Helper method for testing writePDF().
     * 
     * @param expected the String expected when writePDF() is invoked
     * @param testSubj the test subject
     */
    private void writePDFTests(String expected, COSString testSubj)
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try
        {
            testSubj.writePDF(outStream);
        }
        catch (IOException e)
        {
            fail("IOException: " + e.getMessage());
        }
        assertEquals(expected, outStream.toString());
    }

    /**
     * Test createFromHexString() - tests that the proper String is created from a hex string input.
     */
    public void testCreateFromHexString()
    {
        String expected = "Quick and simple test";
        String hexForm = createHex(expected);
        try
        {
            COSString test1 = COSString.createFromHexString(hexForm);
            writePDFTests("(" + expected + ")", test1);
            COSString test2 = COSString.createFromHexString(createHex(ESC_CHAR_STRING));
            writePDFTests("(" + ESC_CHAR_STRING_PDF_FORMAT + ")", test2);
            COSString test3 = COSString.createFromHexString(hexForm + "xx", true);
            writePDFTests("(" + expected + "?)", test3);
        }
        catch (IOException e)
        {
            fail("IOException thrown: " + e.getMessage());
        }
        try
        {
            COSString test4 = COSString.createFromHexString(hexForm + "xx", false);
            fail("Should have thrown an IOException here");
        }
        catch (IOException e)
        {
            // PASS
        }
    }

    private String createHex(String str)
    {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray())
        {
            sb.append(Integer.toString(c, 16));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * Tests getHex() - ensure the hex String returned is properly formatted.
     */
    public void testGetHex()
    {
        String expected = "Test subject for testing getHex";
        COSString test1 = new COSString(expected);
        String hexForm = createHex(expected);
        assertEquals(hexForm, test1.getHexString());
        test1.setForceLiteralForm(true);
        assertEquals(hexForm, test1.getHexString());
        COSString escCS = new COSString(ESC_CHAR_STRING);
        // Not sure whether the escaped characters should be escaped or not, presumably since 
        // writePDF() gives you the proper formatted text, getHex() should ONLY convert to hex. 
        assertEquals(createHex(ESC_CHAR_STRING), escCS.getHexString());
    }

    /**
     * Test getString() - ensure string are returned in the correct format.
     */
    public void testGetString()
    {
        COSString nullStr = new COSString();
        assertEquals("", nullStr.getString());
        try
        {
            String testStr = "Test subject for getString()";
            COSString test1 = new COSString(testStr);
            assertEquals(testStr, test1.getString());

            String appendedStr = "appended text";
            test1.append(appendedStr.getBytes());
            assertEquals(testStr + appendedStr, test1.getString());

            test1.append(ESC_CHAR_STRING.getBytes());
            assertEquals(testStr + appendedStr + ESC_CHAR_STRING, test1.getString());

            COSString hexStr = COSString.createFromHexString(createHex(testStr));
            assertEquals(testStr, hexStr.getString());

            COSString escapedString = new COSString(ESC_CHAR_STRING);
            assertEquals(ESC_CHAR_STRING, escapedString.getString());
        }
        catch (IOException e)
        {
            fail("IOException thrown: " + e.getMessage());
        }
    }

    /**
     * Test append(int) and append(byte[]) - test both code paths. 
     */
    public void testAppend()
    {
        try
        {
            // Mostly tested in testGetString()
            COSString testSubj = new COSString();
            StringBuilder sb = new StringBuilder();
            assertEquals(sb.toString(), testSubj.getString());
            // Arbitrary int but makes it easy to test
            testSubj.append('a');
            sb.append("a");
            assertEquals(sb.toString(), testSubj.getString());
            testSubj.append(ESC_CHAR_STRING.getBytes());
            sb.append(ESC_CHAR_STRING);
            assertEquals(sb.toString(), testSubj.getString());
            try
            {
                testSubj.append(null);
                assertEquals(sb.toString(), testSubj.getString());
                fail("NullPointerException not thrown.");
            }
            catch (NullPointerException e)
            {
                // PASS
            }
        }
        catch (IOException e)
        {
            fail("IOException thrown: " + e.getMessage());
        }
    }

    /**
     * Test reset() - tests that the internal buffer is reset. Not a great deal to test here...
     */
    public void testReset()
    {
        String str = "This string is going to be reset";
        COSString testSubj = new COSString(str);
        assertEquals(str, testSubj.getString());
        testSubj.reset();
        assertEquals("", testSubj.getString());
    }

    /**
     * Test getBytes() - again not much to test, just ensure the proper byte array is returned.
     */
    public void testGetBytes()
    {
        COSString str = new COSString(ESC_CHAR_STRING);
        testByteArrays(ESC_CHAR_STRING.getBytes(), str.getBytes());
    }

    /**
     * Tests writePDF() - tests that the string is in PDF format.
     */
    public void testWritePDF()
    {
        // This has been tested quite thorougly above but do a couple tests anyway
        COSString testSubj = new COSString(ESC_CHAR_STRING);
        writePDFTests("(" + ESC_CHAR_STRING_PDF_FORMAT + ")", testSubj);
        String textString = "This is just an arbitrary piece of text for testing";
        COSString testSubj2 = new COSString(textString);
        writePDFTests("(" + textString + ")", testSubj2);
    }

    /**
     * This will test all of the filters in the system.
     *
     * @throws IOException If there is an exception while encoding.
     */
    public void testUnicode() throws IOException
    {
        String theString = "\u4e16";
        COSString string = new COSString(theString);
        assertTrue(string.getString().equals(theString));
    }

    @Override
    public void testAccept()
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ICOSVisitor visitor = new COSWriter(outStream);
        COSString testSubj = new COSString(ESC_CHAR_STRING);
        try
        {
            testSubj.accept(visitor);
            assertEquals("(" + ESC_CHAR_STRING_PDF_FORMAT + ")", outStream.toString());
            outStream.reset();
            testSubj.setForceHexForm(true);
            testSubj.accept(visitor);
            assertEquals("<" + createHex(ESC_CHAR_STRING) + ">", outStream.toString());
        }
        catch (COSVisitorException e)
        {
            fail(e.getMessage());
        }
    }

    /**
     * Tests equals(Object) - ensure that the Object.equals() contract is obeyed.
     */
    public void testEquals()
    {
        // Check all these several times for consistency
        for (int i = 0; i < 10; i++)
        {
            // Reflexive
            COSString x1 = new COSString("Test");
            assertTrue(x1.equals(x1));

            // Symmetry i.e. if x == y then y == x
            COSString y1 = new COSString("Test");
            assertTrue(x1.equals(y1));
            assertTrue(y1.equals(x1));
            COSString x2 = new COSString("Test");
            x2.setForceHexForm(true);
            // also if x != y then y != x
            assertFalse(x1.equals(x2));
            assertFalse(x2.equals(x1));

            // Transitive if x == y && y == z then x == z
            COSString z1 = new COSString("Test");
            assertTrue(x1.equals(y1));
            assertTrue(y1.equals(z1));
            assertTrue(x1.equals(z1));
            // Test the negative as well if x1 == y1 && y1 != x2 then x1 != x2
            assertTrue(x1.equals(y1));
            assertFalse(y1.equals(x2));
            assertFalse(x1.equals(x2));

            // Non-nullity
            assertFalse(x1.equals(null));
            assertFalse(y1.equals(null));
            assertFalse(z1.equals(null));
            assertFalse(x2.equals(null));

            // Also check other state
            COSString y2 = new COSString("Test");
            y2.setForceLiteralForm(true);
            assertFalse(y2.equals(x2));
            assertTrue(y2.equals(x1));
        }
    }

    /**
     * Test hashCode() - tests that the Object.hashCode() contract is obeyed.
     */
    public void testHashCode()
    {
        COSString str1 = new COSString("Test1");
        COSString str2 = new COSString("Test2");
        assertFalse(str1.hashCode() == str2.hashCode());
        COSString str3 = new COSString("Test1");
        assertTrue(str1.hashCode() == str3.hashCode());
        str3.setForceHexForm(true);
        assertFalse(str1.hashCode() == str3.hashCode());
        str3.setForceLiteralForm(true);
        assertTrue(str1.hashCode() == str3.hashCode());
    }
}
