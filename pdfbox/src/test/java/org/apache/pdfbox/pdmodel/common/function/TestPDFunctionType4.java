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
package org.apache.pdfbox.pdmodel.common.function;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.RandomAccessBuffer;

import junit.framework.TestCase;

/**
 * Tests the {@link PDFunctionType4} class.
 *
 * @version $Revision$
 */
public class TestPDFunctionType4 extends TestCase
{

    private PDFunctionType4 createFunction(String function, float[] domain, float[] range)
            throws IOException
    {
        COSDictionary dict = new COSDictionary();
        dict.setInt("FunctionType", 4);
        COSArray domainArray = new COSArray();
        domainArray.setFloatArray(domain);
        dict.setItem("Domain", domainArray);
        COSArray rangeArray = new COSArray();
        rangeArray.setFloatArray(range);
        dict.setItem("Range", rangeArray);

        COSStream functionStream = new COSStream(dict, new RandomAccessBuffer());
        OutputStream out = functionStream.createUnfilteredStream();
        byte[] data = function.getBytes("US-ASCII");
        out.write(data, 0, data.length);
        out.flush();

        return new PDFunctionType4(functionStream);
    }

    /**
     * Checks the {@link PDFunctionType4}.
     * @throws Exception if an error occurs
     */
    public void testFunctionSimple() throws Exception
    {
        String functionText = "{ add }";
        //Simply adds the two arguments and returns the result

        PDFunctionType4 function = createFunction(functionText,
                new float[] {-1.0f, 1.0f, -1.0f, 1.0f},
                new float[] {-1.0f, 1.0f});

        COSArray input = new COSArray();
        input.setFloatArray(new float[] {0.8f, 0.1f});
        COSArray output = function.eval(input);

        assertEquals(1, output.size());
        assertEquals(0.9f, ((COSFloat)output.get(0)).floatValue(), 0.0001f);

        input = new COSArray();
        input.setFloatArray(new float[] {0.8f, 0.3f}); //results in 1.1f being outside Range
        output = function.eval(input);

        assertEquals(1, output.size());
        assertEquals(new COSFloat(1.0f), output.get(0));

        input = new COSArray();
        input.setFloatArray(new float[] {0.8f, 1.2f}); //input argument outside Dimension
        output = function.eval(input);

        assertEquals(1, output.size());
        assertEquals(new COSFloat(1.0f), output.get(0));
    }

    /**
     * Checks the handling of the argument order for a {@link PDFunctionType4}.
     * @throws Exception if an error occurs
     */
    public void testFunctionArgumentOrder() throws Exception
    {
        String functionText = "{ pop }";
        //pops the top-most argument and returns the second as is.

        PDFunctionType4 function = createFunction(functionText,
                new float[] {-1.0f, 1.0f, -1.0f, 1.0f},
                new float[] {-1.0f, 1.0f});

        COSArray input = new COSArray();
        input.setFloatArray(new float[] {-0.7f, 0.0f});
        COSArray output = function.eval(input);

        assertEquals(1, output.size());
        assertEquals(0.0f, ((COSFloat)output.get(0)).floatValue(), 0.0001f);
        //TODO not sure if this is really correct
    }

}
