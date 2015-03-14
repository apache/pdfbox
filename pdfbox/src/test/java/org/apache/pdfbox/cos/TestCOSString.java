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
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.pdfbox.pdfwriter.COSWriter;

/**
 * This will test all of the filters in the PDFBox system.
 *
 * Ben Litchfield
 */
public class TestCOSString extends TestCOSBase
{
    private static final String ESC_CHAR_STRING =
            "( test#some) escaped< \\chars>!~1239857 ";
    private static final String ESC_CHAR_STRING_PDF_FORMAT =
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

    @Override
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
     * Test setForceHexForm() and setForceLiteralForm() - tests these two methods do enforce the
     * different String output forms within PDF. 
     */
    public void testSetForceHexLiteralForm()
    {
        String inputString = "Test with a text and a few numbers 1, 2 and 3";
        String pdfHex = "<" + createHex(inputString) + ">";
        COSString cosStr = new COSString(inputString);
        cosStr.setForceHexForm(true);
        writePDFTests(pdfHex, cosStr);

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
            COSWriter.writeString(testSubj, outStream);
        }
        catch (IOException e)
        {
            fail("IOException: " + e.getMessage());
        }
        assertEquals(expected, outStream.toString());
    }

    /**
     * Test parseHex() - tests that the proper String is created from a hex string input.
     */
    public void testFromHex()
    {
        String expected = "Quick and simple test";
        String hexForm = createHex(expected);
        try
        {
            COSString test1 = COSString.parseHex(hexForm);
            writePDFTests("(" + expected + ")", test1);
            COSString test2 = COSString.parseHex(createHex(ESC_CHAR_STRING));
            writePDFTests("(" + ESC_CHAR_STRING_PDF_FORMAT + ")", test2);
        }
        catch (IOException e)
        {
            fail("IOException thrown: " + e.getMessage());
        }
        try
        {
            COSString.parseHex(hexForm + "xx");
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
        assertEquals(hexForm, test1.toHexString());
        COSString escCS = new COSString(ESC_CHAR_STRING);
        // Not sure whether the escaped characters should be escaped or not, presumably since 
        // writePDF() gives you the proper formatted text, getHex() should ONLY convert to hex. 
        assertEquals(createHex(ESC_CHAR_STRING), escCS.toHexString());
    }

    /**
     * Test testGetString() - ensure getString() are returned in the correct format.
     */
    public void testGetString()
    {
        try
        {
            String testStr = "Test subject for getString()";
            COSString test1 = new COSString(testStr);
            assertEquals(testStr, test1.getString());

            COSString hexStr = COSString.parseHex(createHex(testStr));
            assertEquals(testStr, hexStr.getString());

            COSString escapedString = new COSString(ESC_CHAR_STRING);
            assertEquals(ESC_CHAR_STRING, escapedString.getString());

            testStr = "Line1\nLine2\nLine3\n";
            COSString lineFeedString = new COSString(testStr);
            assertEquals(testStr, lineFeedString.getString());
        }
        catch (IOException e)
        {
            fail("IOException thrown: " + e.getMessage());
        }
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
        
        String textAscii = "This is some regular text. It should all be expressable in ASCII";
        /** En français où les choses sont accentués. En español, así */
        String text8Bit = "En fran\u00e7ais o\u00f9 les choses sont accentu\u00e9s. En espa\u00f1ol, as\u00ed";
         /** をクリックしてく */
        String textHighBits =  "\u3092\u30af\u30ea\u30c3\u30af\u3057\u3066\u304f";

        // Testing the getString method
        COSString stringAscii = new COSString( textAscii );
        assertEquals( stringAscii.getString(), textAscii );
        
        COSString string8Bit = new COSString( text8Bit );
        assertEquals( string8Bit.getString(), text8Bit );

        COSString stringHighBits = new COSString( textHighBits );
        assertEquals( stringHighBits.getString(), textHighBits );
        

        // Testing the getBytes method
        // The first two strings should be stored as ISO-8859-1 because they only contain chars in the range 0..255
        assertEquals(textAscii, new String(stringAscii.getBytes(), "ISO-8859-1"));
        // likewise for the 8bit characters.
        assertEquals(text8Bit, new String(string8Bit.getBytes(), "ISO-8859-1"));
        
        // The japanese text contains high bits so must be stored as big endian UTF-16
        assertEquals(textHighBits, new String(stringHighBits.getBytes(), "UnicodeBig"));
        
        
        // Test the writePDF method to ensure that the Strings are correct when written into PDF.
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        COSWriter.writeString(stringAscii, out);
        assertEquals("(" + textAscii + ")", new String(out.toByteArray(), "ASCII"));
        
        out.reset();
        COSWriter.writeString(string8Bit, out);
        StringBuffer hex = new StringBuffer();
        for(char c : text8Bit.toCharArray())
        {
           hex.append( Integer.toHexString(c).toUpperCase() );
        }
        assertEquals("<"+hex.toString()+">", new String(out.toByteArray(), "ASCII"));
        
        out.reset();
        COSWriter.writeString(stringHighBits, out);
        hex = new StringBuffer();
        hex.append("FEFF"); // Byte Order Mark
        for(char c : textHighBits.toCharArray())
        {
           hex.append( Integer.toHexString(c).toUpperCase() );
        }
        assertEquals("<"+hex.toString()+">", new String(out.toByteArray(), "ASCII")); 
    }

    @Override
    public void testAccept() throws IOException
    {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        ICOSVisitor visitor = new COSWriter(outStream);
        COSString testSubj = new COSString(ESC_CHAR_STRING);
        testSubj.accept(visitor);
        assertEquals("(" + ESC_CHAR_STRING_PDF_FORMAT + ")", outStream.toString());
        outStream.reset();
        testSubj.setForceHexForm(true);
        testSubj.accept(visitor);
        assertEquals("<" + createHex(ESC_CHAR_STRING) + ">", outStream.toString());
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
            assertFalse(x1 == null);
            assertFalse(y1 == null);
            assertFalse(z1 == null);
            assertFalse(x2 == null);
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
    }

    /**
     * Test testCompareFromHexString() - tests that Strings created from hex
     * compare correctly (PDFBOX-2401)
     */
    public void testCompareFromHexString() throws IOException
    {
        COSString test1 = COSString.parseHex("000000FF000000");
        COSString test2 = COSString.parseHex("000000FF00FFFF");
        assertEquals(test1, test1);
        assertEquals(test2, test2);
        assertFalse(test1.toHexString().equals(test2.toHexString()));
        assertFalse(Arrays.equals(test1.getBytes(), test2.getBytes()));
        assertFalse(test1.equals(test2));
        assertFalse(test2.equals(test1));
        assertFalse(test1.getString().equals(test2.getString()));
    }
}
