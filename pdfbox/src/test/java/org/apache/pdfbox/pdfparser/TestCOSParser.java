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

package org.apache.pdfbox.pdfparser;


import java.io.IOException;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.util.Charsets;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestCOSParser
{
    // COSName parsing tests based on examples from PDF 32000-1:2008, Table 4, Section 7.3.5

    @Test
    public void testTable4Example_Name1() throws IOException
    {
        // /Name1 → "Name1"
        byte[] inputBytes = "/Name1 ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name1", name.getName());
    }

    @Test
    public void testTable4Example_ASomewhatLongerName() throws IOException
    {
        // /ASomewhatLongerName → "ASomewhatLongerName"
        byte[] inputBytes = "/ASomewhatLongerName ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("ASomewhatLongerName", name.getName());
    }

    @Test
    public void testTable4Example_WithSpecialCharacters() throws IOException
    {
        // /A;Name_With-Various***Characters? → "A;Name_With-Various***Characters?"
        byte[] inputBytes = "/A;Name_With-Various***Characters? ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("A;Name_With-Various***Characters?", name.getName());
    }

    @Test
    public void testTable4Example_Numeric() throws IOException
    {
        // /1.2 → "1.2"
        byte[] inputBytes = "/1.2 ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("1.2", name.getName());
    }

    @Test
    public void testTable4Example_DollarSigns() throws IOException
    {
        // /$$ → "$$"
        byte[] inputBytes = "/$$ ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("$$", name.getName());
    }

    @Test
    public void testTable4Example_AtPattern() throws IOException
    {
        // /@pattern → "@pattern"
        byte[] inputBytes = "/@pattern ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("@pattern", name.getName());
    }

    @Test
    public void testTable4Example_DotNotdef() throws IOException
    {
        // /.notdef → ".notdef" (space is 0x20, hex-encoded as #20)
        byte[] inputBytes = "/#2Enotdef ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals(".notdef", name.getName());
    }

    @Test
    public void testTable4Example_HexEncodedSpace() throws IOException
    {
        // /lime#20Green → "lime Green"
        byte[] inputBytes = "/lime#20Green ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("lime Green", name.getName());
    }

    @Test
    public void testTable4Example_HexEncodedParentheses() throws IOException
    {
        // /paired#28#29parentheses → "paired()parentheses"
        // (#28 = '(', #29 = ')')
        byte[] inputBytes = "/paired#28#29parentheses ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("paired()parentheses", name.getName());
    }

    @Test
    public void testTable4Example_HexEncodedNumberSign() throws IOException
    {
        // /The_Key_of_F#23_Minor → "The_Key_of_F#_Minor"
        // (#23 = '#')
        byte[] inputBytes = "/The_Key_of_F#23_Minor ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("The_Key_of_F#_Minor", name.getName());
    }

    @Test
    public void testTable4Example_HexEncodedLetter() throws IOException
    {
        // /A#42 → "AB" (note #42 = 'B')
        byte[] inputBytes = "/A#42 ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("AB", name.getName());
    }

    @Test
    public void testTable4Example_EmptyName() throws IOException
    {
        // / → "" (empty name is valid per spec)
        byte[] inputBytes = "/ ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("", name.getName());
    }

    @Test
    public void testNullCharacterTermination() throws IOException
    {
        // /Name\0Extra should parse as "Name" and stop at null
        byte[] inputBytes = new byte[] { '/', 'N', 'a', 'm', 'e', 0, 'E', 'x', 't', 'r', 'a', ' ' };
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name", name.getName());
    }

    @Test
    public void testInvalidHexSequence() throws IOException
    {
        // /Name#GG should keep #G literally since G is not a valid hex digit
        byte[] inputBytes = "/Name#GG ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        // When # is not followed by two hex digits, both chars are kept literally
        assertEquals("Name#GG", name.getName());
    }

    @Test
    public void testHexEscapeLowercase() throws IOException
    {
        // /Name#2fTest (lowercase hex #2f = '/')
        byte[] inputBytes = "/Name#2fTest ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name/Test", name.getName());
    }

    @Test
    public void testHexEscapeUppercase() throws IOException
    {
        // /Name#2FTest (uppercase hex #2F = '/')
        byte[] inputBytes = "/Name#2FTest ".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name/Test", name.getName());
    }

    @Test
    public void testNameTerminationByDelimiters() throws IOException
    {
        // Test termination by '>'
        byte[] inputBytes = "/Name1>".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name1", name.getName());

        // Test termination by '<'
        inputBytes = "/Name2<".getBytes(Charsets.US_ASCII);
        buffer = new RandomAccessBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name2", name.getName());

        // Test termination by '['
        inputBytes = "/Name3[".getBytes(Charsets.US_ASCII);
        buffer = new RandomAccessBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name3", name.getName());

        // Test termination by ']'
        inputBytes = "/Name4]".getBytes(Charsets.US_ASCII);
        buffer = new RandomAccessBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name4", name.getName());

        // Test termination by '('
        inputBytes = "/Name5(".getBytes(Charsets.US_ASCII);
        buffer = new RandomAccessBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name5", name.getName());

        // Test termination by ')'
        inputBytes = "/Name6)".getBytes(Charsets.US_ASCII);
        buffer = new RandomAccessBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name6", name.getName());

        // Test termination by '/'
        inputBytes = "/Name7/".getBytes(Charsets.US_ASCII);
        buffer = new RandomAccessBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name7", name.getName());

        // Test termination by '%'
        inputBytes = "/Name8%".getBytes(Charsets.US_ASCII);
        buffer = new RandomAccessBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name8", name.getName());
    }

    @Test
    public void testASCIIRegularCharacters() throws IOException
    {
        // Test a range of ASCII characters that are not delimiters
        // PDF delimiters that terminate name parsing: whitespace, <, >, [, ], {, }, /, %, (, )
        byte[] inputBytes = "/!\"$'*+-._:;=@~^`|\\".getBytes(Charsets.US_ASCII);
        RandomAccessBuffer buffer = new RandomAccessBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        // All these non-delimiter characters should be preserved
        assertEquals("!\"$'*+-._:;=@~^`|\\", name.getName());
    }

    @Test
    public void testUTF8InNames()
    {
        // Create a name with UTF-8 encoded characters
        String nameStr = "Test中国";
        byte[] nameBytes = nameStr.getBytes(Charsets.UTF_8);
        COSName name = COSName.getPDFName(nameBytes);
        
        // The name should preserve the UTF-8 bytes
        byte[] retrievedBytes = name.getBytes();
        // Verify by recreating the string
        String retrievedStr = new String(retrievedBytes, Charsets.UTF_8);
        assertEquals(nameStr, retrievedStr);
    }

    @Test
    public void testNameCanonicaliation()
    {
        byte[] bytes1 = "TestName".getBytes(Charsets.US_ASCII);
        byte[] bytes2 = "TestName".getBytes(Charsets.US_ASCII);
        
        COSName name1 = COSName.getPDFName(bytes1);
        COSName name2 = COSName.getPDFName(bytes2);
        
        // Same bytes should return references to identical object
        assertEquals(name1, name2);
    }
}
