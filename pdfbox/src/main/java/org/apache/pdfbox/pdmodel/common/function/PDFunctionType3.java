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
import org.apache.pdfbox.pdmodel.common.PDRange;

import java.io.IOException;

/**
 * This class represents a type 3 function in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class PDFunctionType3 extends PDFunction
{

    private COSArray functions = null;
    private COSArray encode = null;
    private COSArray bounds = null;
    
    /**
     * Constructor.
     *
     * @param functionStream The function .
     */
    public PDFunctionType3(COSBase function)
    {
        super( function );
    }

    /**
     * {@inheritDoc}
     */
    public int getFunctionType()
    {
        return 3;
    }
    
    /**
    * {@inheritDoc}
    */
    public COSArray eval(COSArray input) throws IOException
    {
        //This function is known as a "stitching" function. Based on the input, it decides which child function to call.
        //See PDF Reference section 3.9.3.
        PDFunction function = null;
        float x = ((COSNumber)input.get(0)).floatValue();
        PDRange domain = getDomainForInput(1);
        // clip input value to domain
        x = clipToRange(x, domain.getMin(), domain.getMax());

        float[] boundsValues = getBounds().toFloatArray();
        int boundsSize = boundsValues.length;
        if (boundsSize == 0 || x < boundsValues[0])
        {
            function = PDFunction.create(getFunctions().get(0));
            PDRange encode = getEncodeForParameter(0);
            if (boundsSize == 0)
            {
                x = interpolate(x, domain.getMin(), domain.getMax(), encode.getMin(), encode.getMax());
            }
            else
            {
                x = interpolate(x, domain.getMin(), boundsValues[0], encode.getMin(), encode.getMax());
            }
        }
        else
        {
            for (int i=0; i<boundsSize-1; i++)
            {
                if ( x >= boundsValues[i] && x < boundsValues[i+1] )
                {
                    function = PDFunction.create(getFunctions().get(i+1));
                    PDRange encode = getEncodeForParameter(i+1);
                    x = interpolate(x, boundsValues[i], boundsValues[i+1], encode.getMin(), encode.getMax());
                    break;
                }
            }
            if(function==null) //must be in last partition
            {
                function = PDFunction.create(getFunctions().get(boundsSize+1));
                PDRange encode = getEncodeForParameter(boundsSize+1);
                x = interpolate(x, boundsValues[boundsSize-1], domain.getMax(), encode.getMin(), encode.getMax());
            }
        }
        COSArray functionValues = new COSArray();
        functionValues.add(new COSFloat(x));
        COSArray functionResult = function.eval(functionValues);
        // clip to range if available
        return clipToRange(functionResult);
    }
    
    /**
     * Returns all functions values as COSArray.
     * 
     * @return the functions array. 
     */
    public COSArray getFunctions()
    {
        if (functions == null)
        {
            functions = (COSArray)(getDictionary().getDictionaryObject( COSName.FUNCTIONS ));
        }
        return functions;
    }
    
    /**
     * Returns all bounds values as COSArray.
     * 
     * @return the bounds array. 
     */
    public COSArray getBounds()
    {
        if (bounds == null) 
        {
            bounds = (COSArray)(getDictionary().getDictionaryObject( COSName.BOUNDS ));
        }
        return bounds;
    }
    
    /**
     * Returns all encode values as COSArray.
     * 
     * @return the encode array. 
     */
    public COSArray getEncode()
    {
        if (encode == null)
        {
            encode = (COSArray)(getDictionary().getDictionaryObject( COSName.ENCODE ));
        }
        return encode;
    }
    
    /**
     * Get the encode for the input parameter.
     *
     * @param paramNum The function parameter number.
     *
     * @return The encode parameter range or null if none is set.
     */
    private PDRange getEncodeForParameter(int n) 
    {
        COSArray encodeValues = getEncode();
        return new PDRange( encodeValues, n );
    }

}
