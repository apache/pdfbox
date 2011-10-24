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
 * This class represents a type 2 function in a PDF document.
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
     * @param functionStream The function .
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
    * {@inheritDoc}
    */
    public float[] eval(float[] input) throws IOException
    {
        //This function performs exponential interpolation.
        //It uses only a single value as its input, but may produce a multi-valued output.
        //See PDF Reference section 3.9.2.
                
        double inputValue = input[0];
        double exponent = getN();
        COSArray c0 = getC0();
        COSArray c1 = getC1();
        int c0Size = c0.size();
        float[] functionResult = new float[c0Size];
        for (int j=0;j<c0Size;j++)
        {
            //y[j] = C0[j] + x^N*(C1[j] - C0[j])
            functionResult[j] = ((COSNumber)c0.get(j)).floatValue() + (float)Math.pow(inputValue,exponent)*(((COSNumber)c1.get(j)).floatValue() - ((COSNumber)c0.get(j)).floatValue());
        }
        // clip to range if available
        return clipToRange(functionResult);
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
}
