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

package org.apache.pdfbox.pdmodel.fdf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.junit.jupiter.api.Test;

/*
 * Test some characteristics of FDFFields
 */
class FDFFieldTest
{
    @Test
    void testCOSStringValue() throws IOException
    {
        final String testString = "Test value";
        final COSString testCOSString = new COSString(testString);
        
        final FDFField field = new FDFField();
        field.setValue(testCOSString);
        
        assertEquals(testCOSString, (COSString) field.getCOSValue());
        assertEquals(testString, field.getValue());
    }

    
    @Test
    void testTextAsCOSStreamValue() throws IOException
    {
        final String testString = "Test value";
        final byte[] testBytes = testString.getBytes(StandardCharsets.US_ASCII);
        final COSStream stream = createStream(testBytes, null);
        
        final FDFField field = new FDFField();
        field.setValue(stream);
        
        assertEquals(testString, field.getValue());
    }
        
    @Test
    void testCOSNameValue() throws IOException
    {
        final String testString = "Yes";
        final COSName testCOSSName = COSName.getPDFName(testString);
        
        final FDFField field = new FDFField();
        field.setValue(testCOSSName);
        
        assertEquals(testCOSSName, (COSName) field.getCOSValue());
        assertEquals(testString, field.getValue());
    }

    @Test
    void testCOSArrayValue() throws IOException
    {
        final List<String> testList = new ArrayList<>();
        testList.add("A");
        testList.add("B");
        
        final COSArray testCOSArray = COSArray.ofCOSStrings(testList);
        
        final FDFField field = new FDFField();
        field.setValue(testCOSArray);
        
        assertEquals(testCOSArray, (COSArray) field.getCOSValue());
        assertEquals(testList, field.getValue());
    }
    
    
    private COSStream createStream(final byte[] testString, final COSBase filters) throws IOException
    {
        final COSStream stream = new COSStream();
        final OutputStream output = stream.createOutputStream(filters);
        output.write(testString);
        output.close();
        return stream;
    }
}
