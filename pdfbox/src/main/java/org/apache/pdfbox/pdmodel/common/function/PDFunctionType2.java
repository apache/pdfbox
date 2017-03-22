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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import java.io.IOException;

/**
 * This class represents a Type 2 (exponential interpolation) function in a PDF
 * document.
 *
 * @author Ben Litchfield
 */
public class PDFunctionType2 extends PDFunction
{

    /**
     * The C0 values of the exponential function.
     */
    private final COSArray c0;
    /**
     * The C1 values of the exponential function.
     */
    private final COSArray c1;
    /**
     * The exponent value of the exponential function.
     */
    private final float exponent;

    /**
     * Constructor.
     *
     * @param function The function.
     */
    public PDFunctionType2(COSBase function)
    {
        super(function);

        if (getCOSObject().getDictionaryObject(COSName.C0) instanceof COSArray)
        {
            c0 = (COSArray) getCOSObject().getDictionaryObject(COSName.C0);
        }
        else
        {
            c0 = new COSArray();
        }
        if (c0.size() == 0)
        {
            c0.add(new COSFloat(0));
        }

        if (getCOSObject().getDictionaryObject(COSName.C1) instanceof COSArray)
        {
            c1 = (COSArray) getCOSObject().getDictionaryObject(COSName.C1);
        }
        else
        {
            c1 = new COSArray();
        }
        if (c1.size() == 0)
        {
            c1.add(new COSFloat(1));
        }

        exponent = getCOSObject().getFloat(COSName.N);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFunctionType()
    {
        return 2;
    }

    /**
     * Performs exponential interpolation
     *
     * {@inheritDoc}
     */
    @Override
    public float[] eval(float[] input) throws IOException
    {
        // exponential interpolation
        float xToN = (float) Math.pow(input[0], exponent); // x^exponent

        float[] result = new float[Math.min(c0.size(),c1.size())];
        for (int j = 0; j < result.length; j++)
        {
            float c0j = ((COSNumber) c0.get(j)).floatValue();
            float c1j = ((COSNumber) c1.get(j)).floatValue();
            result[j] = c0j + xToN * (c1j - c0j);
        }

        return clipToRange(result);
    }

    /**
     * Returns the C0 values of the function, 0 if empty.
     *
     * @return a COSArray with the C0 values
     */
    public COSArray getC0()
    {
        return c0;
    }

    /**
     * Returns the C1 values of the function, 1 if empty.
     *
     * @return a COSArray with the C1 values
     */
    public COSArray getC1()
    {
        return c1;
    }

    /**
     * Returns the exponent of the function.
     *
     * @return the float value of the exponent
     */
    public float getN()
    {
        return exponent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "FunctionType2{"
                + "C0: " + getC0() + " "
                + "C1: " + getC1() + " "
                + "N: " + getN() + "}";
    }
}
