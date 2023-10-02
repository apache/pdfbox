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

import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.Test;

class TestBaseParser
{
    @Test
    void testCheckForEndOfString() throws IOException
    {
        // (Test)
        byte[] inputBytes = { 40, 84, 101, 115, 116, 41 };

        RandomAccessReadBuffer buffer = new RandomAccessReadBuffer(inputBytes);
        BaseParser baseParser = new COSParser(buffer);
        COSString cosString = baseParser.parseCOSString();
        assertEquals("Test", cosString.getString());

        String output = "(Test";
        // ((Test) + LF + "/ "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 10, '/', ' ' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        baseParser = new COSParser(buffer);
        cosString = baseParser.parseCOSString();
        assertEquals(output, cosString.getString());

        // ((Test) + CR + "/ "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 13, '/', ' ' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        baseParser = new COSParser(buffer);
        cosString = baseParser.parseCOSString();
        assertEquals(output, cosString.getString());

        // ((Test) + CR + LF + "/ "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 13, 10, '/' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        baseParser = new COSParser(buffer);
        cosString = baseParser.parseCOSString();
        assertEquals(output, cosString.getString());

        // ((Test) + LF + "> "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 10, '>', ' ' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        baseParser = new COSParser(buffer);
        cosString = baseParser.parseCOSString();
        assertEquals(output, cosString.getString());

        // ((Test) + CR + "> "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 13, '>', ' ' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        baseParser = new COSParser(buffer);
        cosString = baseParser.parseCOSString();
        assertEquals(output, cosString.getString());

        // ((Test) + CR + LF + "> "
        inputBytes = new byte[] { '(', '(', 'T', 'e', 's', 't', ')', 13, 10, '>' };

        buffer = new RandomAccessReadBuffer(inputBytes);
        baseParser = new COSParser(buffer);
        cosString = baseParser.parseCOSString();
        assertEquals(output, cosString.getString());
    }

}
