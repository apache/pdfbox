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
import java.lang.Math;

/**
 * This class represents a Type 2 (exponential interpolation) function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDFunctionType2 extends PDFunction
{
    
    /**
     * The C0 values of the exponential function.
     */
    private COSArray C0;
    /**
     * The C1 values of the exponential function.
     */
    private COSArray C1;

    /**
     * Constructor.
     *
     * @param function The function .
     */
    public PDFunctionType2(COSBase function)
    {
        super( function );
    }

    /**
     * {@inheritDoc}
     */
    public int getFunctionType()
    {
        return 2;
    }

    /**
     * Performs exponential interpolation
     *
    * {@inheritDoc}
    */
    public float[] eval(float[] input) throws IOException
    {
        COSArray c0 = getC0();
        COSArray c1 = getC1();

        // exponential interpolation
        float xToN = (float)Math.pow(input[0], getN()); // x^N

        float[] result = new float[c0.size()];
        for (int j = 0; j < result.length; j++)
        {
            float C0j = ((COSNumber)c0.get(j)).floatValue();
            float C1j = ((COSNumber)c1.get(j)).floatValue();
            result[j] = C0j + xToN * (C1j - C0j);
        }

        return clipToRange(result);
    }
    
    /**
     * Returns the C0 values of the function, 0 if empty.
     * @return a COSArray with the C0 values
     */
    public COSArray getC0()
    {
        if(C0 == null)
        {
            C0 = (COSArray)getDictionary().getDictionaryObject( COSName.C0 );
            if ( C0 == null )
            {
                // C0 is optional, default = 0
                C0 = new COSArray();
                C0.add( new COSFloat( 0 ) );
            }
        }
        return C0;
    }
    
    /**
     * Returns the C1 values of the function, 1 if empty.
     * @return a COSArray with the C1 values
     */
    public COSArray getC1()
    {
        if(C1 == null)
        {
            C1 = (COSArray)getDictionary().getDictionaryObject( COSName.C1 );
            if( C1 == null )
            {
                // C1 is optional, default = 1
                C1 = new COSArray();
                C1.add( new COSFloat( 1 ) );
            }      
        }            
        return C1;
    }
    
    /**
     * Returns the exponent of the function.
     * @return the float value of the exponent
     */
    public float getN()
    {
        return getDictionary().getFloat(COSName.N);
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "FunctionType2{" +
                "C0:" + getC0() + " " +
                "C1:" + getC1() + "}";
    }
}
