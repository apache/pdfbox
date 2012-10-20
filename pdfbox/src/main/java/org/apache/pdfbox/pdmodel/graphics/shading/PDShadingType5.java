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
package org.apache.pdfbox.pdmodel.graphics.shading;



import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;

/**
 * This represents resources for a shading type 5 (Lattice-Form Gouraud-Shaded Triangle Meshes).
 *
 * @version $Revision: 1.0 $
 */
public class PDShadingType5 extends PDShadingResources
{
    
    private PDFunction function = null;
    /**
     * An array of 2 × n numbers specifying the linear mapping of sample values 
     * into the range appropriate for the function’s output values. 
     * Default value: same as the value of Range
     */
    private COSArray decode = null;
    
    /**
     * Constructor using the given shading dictionary.
     *
     * @param shadingDictionary The dictionary for this shading.
     */
    public PDShadingType5( COSDictionary shadingDictionary )
    {
        super(shadingDictionary);
    }

    /**
     * {@inheritDoc}
     */
    public int getShadingType()
    {
        return PDShadingResources.SHADING_TYPE5;
    }

    /**
     * This will set the function for the color conversion.
     *
     * @param newFunction The new function.
     */
    public void setFunction(PDFunction newFunction)
    {
        function = newFunction;
        getCOSDictionary().setItem(COSName.FUNCTION, newFunction);
    }

    /**
     * This will return the function used to convert the color values.
     *
     * @return The function
     * @exception IOException If we are unable to create the PDFunction object. 
     */
    public PDFunction getFunction() throws IOException
    {
        if (function == null)
        {
            function = PDFunction.create(getCOSDictionary().getDictionaryObject(COSName.FUNCTION));
        }
        return function;
    }

    /**
     * The bits per component of this shading.  
     * This will return -1 if one has not been set.
     *
     * @return The number of bits per component.
     */
    public int getBitsPerComponent()
    {
        return getCOSDictionary().getInt( COSName.BITS_PER_COMPONENT, -1 );
    }

    /**
     * Set the number of bits per component.
     *
     * @param bpc The number of bits per component.
     */
    public void setBitsPerComponent( int bpc )
    {
        getCOSDictionary().setInt( COSName.BITS_PER_COMPONENT, bpc );
    }

    /**
     * The bits per coordinate of this shading.  
     * This will return -1 if one has not been set.
     *
     * @return The number of bits per coordinate.
     */
    public int getBitsPerCoordinate()
    {
        return getCOSDictionary().getInt( COSName.BITS_PER_COORDINATE, -1 );
    }

    /**
     * Set the number of bits per coordinate.
     *
     * @param bpc The number of bits per coordinate.
     */
    public void setBitsPerCoordinate( int bpc )
    {
        getCOSDictionary().setInt( COSName.BITS_PER_COORDINATE, bpc );
    }

    /**
     * The vertices per row of this shading.  
     * This will return -1 if one has not been set.
     *
     * @return The number of vertices per row.
     */
    public int getVerticesPerRow()
    {
        return getCOSDictionary().getInt( COSName.VERTICES_PER_ROW, -1 );
    }

    /**
     * Set the number of vertices per row.
     *
     * @param vpr The number of vertices per row.
     */
    public void setVerticesPerRow( int vpr )
    {
        getCOSDictionary().setInt( COSName.VERTICES_PER_ROW, vpr );
    }

    /**
     * Returns all decode values as COSArray.
     * 
     * @return the decode array. 
     */
    private COSArray getDecodeValues() 
    {
        if (decode == null)
        {
            decode = (COSArray)getCOSDictionary().getDictionaryObject( COSName.DECODE );
        }
        return decode;
    }

    /**
     * This will set the decode values.
     *
     * @param decodeValues The new decode values.
     */
    public void setDecodeValues(COSArray decodeValues)
    {
        decode = decodeValues;
        getCOSDictionary().setItem(COSName.DECODE, decodeValues);
    }

    /**
     * Get the decode for the input parameter.
     *
     * @param paramNum The function parameter number.
     *
     * @return The decode parameter range or null if none is set.
     */
    public PDRange getDecodeForParameter( int paramNum )
    {
        PDRange retval = null;
        COSArray decodeValues = getDecodeValues();
        if( decodeValues != null && decodeValues.size() >= paramNum*2+1 )
        {
            retval = new PDRange(decodeValues, paramNum );
        }
        return retval;
    }

}
