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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSStream;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link PDFunctionType4} class.
 */
class TestPDFunctionType4
{

    private PDFunctionType4 createFunction(final String function, final float[] domain, final float[] range)
            throws IOException
    {
        final COSStream stream = new COSStream();
        stream.setInt("FunctionType", 4);
        final COSArray domainArray = new COSArray();
        domainArray.setFloatArray(domain);
        stream.setItem("Domain", domainArray);
        final COSArray rangeArray = new COSArray();
        rangeArray.setFloatArray(range);
        stream.setItem("Range", rangeArray);
        
        try (OutputStream out = stream.createOutputStream())
        {
            final byte[] data = function.getBytes(StandardCharsets.US_ASCII);
            out.write(data, 0, data.length);
        }

        return new PDFunctionType4(stream);
    }

    /**
     * Checks the {@link PDFunctionType4}.
     * @throws Exception if an error occurs
     */
    @Test
    void testFunctionSimple() throws Exception
    {
        final String functionText = "{ add }";
        //Simply adds the two arguments and returns the result

        final PDFunctionType4 function = createFunction(functionText,
                new float[] {-1.0f, 1.0f, -1.0f, 1.0f},
                new float[] {-1.0f, 1.0f});

        float[] input = new float[] {0.8f, 0.1f};
        float[] output = function.eval(input);

        assertEquals(1, output.length);
        assertEquals(0.9f, output[0], 0.0001f);

        input = new float[] {0.8f, 0.3f}; //results in 1.1f being outside Range
        output = function.eval(input);

        assertEquals(1, output.length);
        assertEquals(1f, output[0]);

        input = new float[] {0.8f, 1.2f}; //input argument outside Dimension
        output = function.eval(input);

        assertEquals(1, output.length);
        assertEquals(1f, output[0]);
    }

    /**
     * Checks the handling of the argument order for a {@link PDFunctionType4}.
     * @throws Exception if an error occurs
     */
    @Test
    void testFunctionArgumentOrder() throws Exception
    {
        final String functionText = "{ pop }";
        // pops an argument (2nd) and returns the next argument (1st)

        final PDFunctionType4 function = createFunction(functionText,
                new float[] {-1.0f, 1.0f, -1.0f, 1.0f},
                new float[] {-1.0f, 1.0f});

        final float[] input = new float[] {-0.7f, 0.0f };
        final float[] output = function.eval(input);

        assertEquals(1, output.length);
        assertEquals(-0.7f, output[0], 0.0001f);
    }

}
