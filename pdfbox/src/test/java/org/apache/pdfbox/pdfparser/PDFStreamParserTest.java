/*
 * Copyright 2014 The Apache Software Foundation.
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
package org.apache.pdfbox.pdfparser;

import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for PDFStreamParser.
 *
 * @author Tilman Hausherr
 */
class PDFStreamParserTest
{

    /**
     * Tests for inline images, whether the EI is correctly identified as
     * ending. To test hasNoFollowingBinData(), the amount of data after EI nust
     * be at least PDFStreamParser.MAX_BIN_CHAR_TEST_LENGTH
     *
     * @throws IOException
     */
    @Test
    void testInlineImages() throws IOException
    {
        testInlineImage2ops("ID\n12345EI Q", "12345", "Q");
        testInlineImage2ops("ID\n12345EI EMC", "12345", "EMC");
        testInlineImage2ops("ID\n12345EI Q ", "12345", "Q");
        testInlineImage2ops("ID\n12345EI EMC ", "12345", "EMC");
        testInlineImage2ops("ID\n12345EI  Q", "12345", "Q");
        testInlineImage2ops("ID\n12345EI  EMC", "12345", "EMC");
        testInlineImage2ops("ID\n12345EI  Q ", "12345", "Q");
        testInlineImage2ops("ID\n12345EI  EMC ", "12345", "EMC");

        testInlineImage2ops("ID\n12345EI \000Q", "12345", "Q");

        testInlineImage2ops("ID\n12345EI Q                             ", "12345", "Q");
        testInlineImage2ops("ID\n12345EI EMC                           ", "12345", "EMC");

        testInlineImage1op("ID\n12345EI", "12345");
        testInlineImage1op("ID\n12345EI                               ", "12345");

        testInlineImage2ops("ID\n12345EI                               Q ", "12345", "Q");
        testInlineImage2ops("ID\n12345EI                               EMC ", "12345", "EMC");
        testInlineImage2ops("ID\n12345EI                               Q", "12345", "Q");
        testInlineImage2ops("ID\n12345EI                               EMC", "12345", "EMC");

        testInlineImage1op("ID\n12EI5EI", "12EI5");
        testInlineImage1op("ID\n12EI5EI ", "12EI5");
        testInlineImage1op("ID\n12EI5EIQEI", "12EI5EIQ");
        testInlineImage2ops("ID\n12EI5EIQEI Q", "12EI5EIQ", "Q");
        testInlineImage2ops("ID\n12EI5EI Q", "12EI5", "Q");
        testInlineImage2ops("ID\n12EI5EI Q ", "12EI5", "Q");
        testInlineImage2ops("ID\n12EI5EI EMC", "12EI5", "EMC");
        testInlineImage2ops("ID\n12EI5EI EMC ", "12EI5", "EMC");
        testInlineImage2ops("ID\n12EI5EI                                Q", "12EI5", "Q");
        testInlineImage2ops("ID\n12EI5EI                                Q ", "12EI5", "Q");
        testInlineImage2ops("ID\n12EI5EI                                EMC", "12EI5", "EMC");
        testInlineImage2ops("ID\n12EI5EI                                EMC ", "12EI5", "EMC");

        // MAX_BIN_CHAR_TEST_LENGTH is currently 10, test boundaries
        //                              1234567890
        testInlineImage2ops("ID\n12EI5EI       EMC ", "12EI5", "EMC");
        testInlineImage2ops("ID\n12EI5EI        EMC ", "12EI5", "EMC");
        testInlineImage2ops("ID\n12EI5EI         EMC ", "12EI5", "EMC");
        testInlineImage2ops("ID\n12EI5EI          EMC ", "12EI5", "EMC");
        testInlineImage2ops("ID\n12EI5EI       Q   ", "12EI5", "Q");
        testInlineImage2ops("ID\n12EI5EI        Q   ", "12EI5", "Q");
        testInlineImage2ops("ID\n12EI5EI         Q   ", "12EI5", "Q");
        testInlineImage2ops("ID\n12EI5EI          Q   ", "12EI5", "Q");
    }

    // checks whether there are two operators, one inline image and the named operator
    private void testInlineImage2ops(final String s, final String imageDataString, final String opName) throws IOException
    {
        final List<Object> tokens = parseTokenString(s);

        assertEquals(2, tokens.size());

        assertEquals(OperatorName.BEGIN_INLINE_IMAGE_DATA, ((Operator) tokens.get(0)).getName());
        assertEquals(imageDataString.length(), ((Operator) tokens.get(0)).getImageData().length);
        assertArrayEquals(imageDataString.getBytes(), ((Operator) tokens.get(0)).getImageData());

        assertEquals(opName, ((Operator) tokens.get(1)).getName());
    }

    // checks whether there is one operator, one inline image
    private void testInlineImage1op(final String s, final String imageDataString) throws IOException
    {
        final List<Object> tokens = parseTokenString(s);

        assertEquals(1, tokens.size());

        assertEquals(OperatorName.BEGIN_INLINE_IMAGE_DATA, ((Operator) tokens.get(0)).getName());
        assertEquals(imageDataString.length(), ((Operator) tokens.get(0)).getImageData().length);
        assertArrayEquals(imageDataString.getBytes(), ((Operator) tokens.get(0)).getImageData());
    }

    // parse string and return list of tokens
    private List<Object> parseTokenString(final String s) throws IOException
    {
        final PDFStreamParser pdfStreamParser = new PDFStreamParser(s.getBytes());
        return pdfStreamParser.parse();
    }

}
