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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.Test;

class TestCOSParser
{
    @Test
    void testCheckForEndOfString() throws IOException
    {
        // (Test)
        byte[] inputBytes = { 40, 84, 101, 115, 116, 41 };

        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSString cosString = cosParser.parseCOSLiteralString();
        assertEquals("Test", cosString.getString());

        String output = "(Test";
        // ((Test) + LF + "/ "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 10, '/', ' ' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        cosString = cosParser.parseCOSLiteralString();
        assertEquals(output, cosString.getString());

        // ((Test) + CR + "/ "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 13, '/', ' ' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        cosString = cosParser.parseCOSLiteralString();
        assertEquals(output, cosString.getString());

        // ((Test) + CR + LF + "/ "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 13, 10, '/' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        cosString = cosParser.parseCOSLiteralString();
        assertEquals(output, cosString.getString());

        // ((Test) + LF + "> "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 10, '>', ' ' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        cosString = cosParser.parseCOSLiteralString();
        assertEquals(output, cosString.getString());

        // ((Test) + CR + "> "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 13, '>', ' ' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        cosString = cosParser.parseCOSLiteralString();
        assertEquals(output, cosString.getString());

        // ((Test) + CR + LF + "> "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 13, 10, '>' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        cosString = cosParser.parseCOSLiteralString();
        assertEquals(output, cosString.getString());
    }

    // COSName parsing tests based on examples from PDF 32000-1:2008, Table 4, Section 7.3.5

    @Test
    void testTable4Example_Name1() throws IOException
    {
        // /Name1 → "Name1"
        byte[] inputBytes = "/Name1 ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name1", name.getName());
    }

    @Test
    void testTable4Example_ASomewhatLongerName() throws IOException
    {
        // /ASomewhatLongerName → "ASomewhatLongerName"
        byte[] inputBytes = "/ASomewhatLongerName ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("ASomewhatLongerName", name.getName());
    }

    @Test
    void testTable4Example_WithSpecialCharacters() throws IOException
    {
        // /A;Name_With-Various***Characters? → "A;Name_With-Various***Characters?"
        byte[] inputBytes = "/A;Name_With-Various***Characters? ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("A;Name_With-Various***Characters?", name.getName());
    }

    @Test
    void testTable4Example_Numeric() throws IOException
    {
        // /1.2 → "1.2"
        byte[] inputBytes = "/1.2 ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("1.2", name.getName());
    }

    @Test
    void testTable4Example_DollarSigns() throws IOException
    {
        // /$$ → "$$"
        byte[] inputBytes = "/$$ ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("$$", name.getName());
    }

    @Test
    void testTable4Example_AtPattern() throws IOException
    {
        // /@pattern → "@pattern"
        byte[] inputBytes = "/@pattern ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("@pattern", name.getName());
    }

    @Test
    void testTable4Example_DotNotdef() throws IOException
    {
        // /.notdef → ".notdef" (space is 0x20, hex-encoded as #20)
        byte[] inputBytes = "/#2Enotdef ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals(".notdef", name.getName());
    }

    @Test
    void testTable4Example_HexEncodedSpace() throws IOException
    {
        // /lime#20Green → "lime Green"
        byte[] inputBytes = "/lime#20Green ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("lime Green", name.getName());
    }

    @Test
    void testTable4Example_HexEncodedParentheses() throws IOException
    {
        // /paired#28#29parentheses → "paired()parentheses"
        // (#28 = '(', #29 = ')')
        byte[] inputBytes = "/paired#28#29parentheses ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("paired()parentheses", name.getName());
    }

    @Test
    void testTable4Example_HexEncodedNumberSign() throws IOException
    {
        // /The_Key_of_F#23_Minor → "The_Key_of_F#_Minor"
        // (#23 = '#')
        byte[] inputBytes = "/The_Key_of_F#23_Minor ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("The_Key_of_F#_Minor", name.getName());
    }

    @Test
    void testTable4Example_HexEncodedLetter() throws IOException
    {
        // /A#42 → "AB" (note #42 = 'B')
        byte[] inputBytes = "/A#42 ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("AB", name.getName());
    }

    @Test
    void testTable4Example_EmptyName() throws IOException
    {
        // / → "" (empty name is valid per spec)
        byte[] inputBytes = "/ ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("", name.getName());
    }

    @Test
    void testNullCharacterTermination() throws IOException
    {
        // /Name\0Extra should parse as "Name" and stop at null
        byte[] inputBytes = new byte[] { '/', 'N', 'a', 'm', 'e', 0, 'E', 'x', 't', 'r', 'a', ' ' };
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name", name.getName());
    }

    @Test
    void testInvalidHexSequence() throws IOException
    {
        // /Name#GG should keep #G literally since G is not a valid hex digit
        byte[] inputBytes = "/Name#GG ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        // When # is not followed by two hex digits, both chars are kept literally
        assertEquals("Name#GG", name.getName());
    }

    @Test
    void testHexEscapeLowercase() throws IOException
    {
        // /Name#2fTest (lowercase hex #2f = '/')
        byte[] inputBytes = "/Name#2fTest ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name/Test", name.getName());
    }

    @Test
    void testHexEscapeUppercase() throws IOException
    {
        // /Name#2FTest (uppercase hex #2F = '/')
        byte[] inputBytes = "/Name#2FTest ".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name/Test", name.getName());
    }

    @Test
    void testNameTerminationByDelimiters() throws IOException
    {
        // Test termination by '>'
        byte[] inputBytes = "/Name1>".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        assertEquals("Name1", name.getName());

        // Test termination by '<'
        inputBytes = "/Name2<".getBytes(StandardCharsets.US_ASCII);
        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name2", name.getName());

        // Test termination by '['
        inputBytes = "/Name3[".getBytes(StandardCharsets.US_ASCII);
        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name3", name.getName());

        // Test termination by ']'
        inputBytes = "/Name4]".getBytes(StandardCharsets.US_ASCII);
        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name4", name.getName());

        // Test termination by '('
        inputBytes = "/Name5(".getBytes(StandardCharsets.US_ASCII);
        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name5", name.getName());

        // Test termination by ')'
        inputBytes = "/Name6)".getBytes(StandardCharsets.US_ASCII);
        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name6", name.getName());

        // Test termination by '/'
        inputBytes = "/Name7/".getBytes(StandardCharsets.US_ASCII);
        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name7", name.getName());

        // Test termination by '%'
        inputBytes = "/Name8%".getBytes(StandardCharsets.US_ASCII);
        buffer = new RandomAccessReadBuffer(inputBytes);
        cosParser = new COSParser(buffer);
        name = cosParser.parseCOSName();
        assertEquals("Name8", name.getName());
    }

    @Test
    void testASCIIRegularCharacters() throws IOException
    {
        // Test a range of ASCII characters that are not delimiters
        // PDF delimiters that terminate name parsing: whitespace, <, >, [, ], {, }, /, %, (, )
        byte[] inputBytes = "/!\"$'*+-._:;=@~^`|\\".getBytes(StandardCharsets.US_ASCII);
        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        COSParser cosParser = new COSParser(buffer);
        COSName name = cosParser.parseCOSName();
        // All these non-delimiter characters should be preserved
        assertEquals("!\"$'*+-._:;=@~^`|\\", name.getName());
    }

    @Test
    void testUTF8InNames()
    {
        // Create a name with UTF-8 encoded characters
        String nameStr = "Test中国";
        byte[] nameBytes = nameStr.getBytes(StandardCharsets.UTF_8);
        COSName name = COSName.getPDFName(nameBytes);
        
        // The name should preserve the UTF-8 bytes
        byte[] retrievedBytes = name.getBytes();
        // Verify by recreating the string
        String retrievedStr = new String(retrievedBytes, StandardCharsets.UTF_8);
        assertEquals(nameStr, retrievedStr);
    }

    @Test
    void testNameCanonicaliation()
    {
        byte[] bytes1 = "TestName".getBytes(StandardCharsets.US_ASCII);
        byte[] bytes2 = "TestName".getBytes(StandardCharsets.US_ASCII);
        
        COSName name1 = COSName.getPDFName(bytes1);
        COSName name2 = COSName.getPDFName(bytes2);
        
        // Same bytes should return references to identical object
        assertEquals(name1, name2);
    }
}
