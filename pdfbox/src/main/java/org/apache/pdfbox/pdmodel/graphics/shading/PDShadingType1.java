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



import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;
import org.apache.pdfbox.util.Matrix;

/**
 * This represents resources for a function based shading.
 *
 * @version $Revision: 1.0 $
 */
public class PDShadingType1 extends PDShadingResources
{
    
    private COSArray domain = null;
    private PDFunction function = null;
    
    /**
     * Constructor using the given shading dictionary.
     *
     * @param shadingDictionary The dictionary for this shading.
     */
    public PDShadingType1( COSDictionary shadingDictionary )
    {
        super(shadingDictionary);
    }

    /**
     * {@inheritDoc}
     */
    public int getShadingType()
    {
        return PDShadingResources.SHADING_TYPE1;
    }

    /**
     * This will get the optional Matrix of a function based shading.
     * 
     * @return the matrix
     */
    public Matrix getMatrix()
    {
        Matrix retval = null;
        COSArray array = (COSArray)getCOSDictionary().getDictionaryObject( COSName.MATRIX );
        if( array != null )
        {
            retval = new Matrix();
            retval.setValue(0, 0, ((COSNumber) array.get(0)).floatValue());
            retval.setValue(0, 1, ((COSNumber) array.get(1)).floatValue());
            retval.setValue(1, 0, ((COSNumber) array.get(2)).floatValue());
            retval.setValue(1, 1, ((COSNumber) array.get(3)).floatValue());
            retval.setValue(2, 0, ((COSNumber) array.get(4)).floatValue());
            retval.setValue(2, 1, ((COSNumber) array.get(5)).floatValue());
        }
        return retval;
    }

    /**
     * Sets the optional Matrix entry for the function based shading.
     * 
     * @param transform the transformation matrix
     */
    public void setMatrix(AffineTransform transform)
    {
        COSArray matrix = new COSArray();
        double[] values = new double[6];
        transform.getMatrix(values);
        for (double v : values)
        {
            matrix.add(new COSFloat((float)v));
        }
        getCOSDictionary().setItem(COSName.MATRIX, matrix);
    }

    /**
     * This will get the optional Domain values of a function based shading.
     * 
     * @return the domain values
     */
    public COSArray getDomain()
    {
        if (domain == null)
        {
            domain = (COSArray)getCOSDictionary().getDictionaryObject( COSName.DOMAIN );
        }
        return domain;
    }

    /**
     * Sets the optional Domain entry for the function based shading.
     * 
     * @param newDomain the domain array
     */
    public void setDomain(COSArray newDomain)
    {
        domain = newDomain;
        getCOSDictionary().setItem(COSName.DOMAIN, newDomain);
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

}
