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
package org.apache.pdfbox.pdmodel.graphics.pattern;


import java.awt.geom.AffineTransform;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDPatternResources;
import org.apache.pdfbox.util.Matrix;

/**
 * This represents the resources for a shading pattern.
 *
 * @version $Revision: 1.0 $
 */
public class PDShadingPatternResources extends PDPatternResources
{
    private COSDictionary patternDictionary;
    private PDExtendedGraphicsState extendedGraphicsState;
    private COSArray matrix = null;
    
    /**
     * Default constructor.
     */
    public PDShadingPatternResources()
    {
        super();
        patternDictionary.setInt(COSName.PATTERN_TYPE, PDPatternResources.SHADING_PATTERN);
    }

    /**
     * Prepopulated pattern resources.
     *
     * @param resourceDictionary The COSDictionary for this pattern resource.
     */
    public PDShadingPatternResources( COSDictionary resourceDictionary )
    {
        patternDictionary = resourceDictionary;
    }

    /**
     * {@inheritDoc}
     */
    public int getPatternType()
    {
        return PDPatternResources.SHADING_PATTERN;
    }

    /**
     * This will get the optional Matrix of a Pattern.
     * It maps the form space into the user space
     * @return the form matrix
     */
    public Matrix getMatrix()
    {
        Matrix returnMatrix = null;
        if (matrix == null)
        {
            matrix = (COSArray)patternDictionary.getDictionaryObject( COSName.MATRIX );
        }
        if( matrix != null )
        {
            returnMatrix = new Matrix();
            returnMatrix.setValue(0, 0, ((COSNumber) matrix.get(0)).floatValue());
            returnMatrix.setValue(0, 1, ((COSNumber) matrix.get(1)).floatValue());
            returnMatrix.setValue(1, 0, ((COSNumber) matrix.get(2)).floatValue());
            returnMatrix.setValue(1, 1, ((COSNumber) matrix.get(3)).floatValue());
            returnMatrix.setValue(2, 0, ((COSNumber) matrix.get(4)).floatValue());
            returnMatrix.setValue(2, 1, ((COSNumber) matrix.get(5)).floatValue());
        }
        return returnMatrix;
    }

    /**
     * Sets the optional Matrix entry for the Pattern.
     * @param transform the transformation matrix
     */
    public void setMatrix(AffineTransform transform)
    {
        matrix = new COSArray();
        double[] values = new double[6];
        transform.getMatrix(values);
        for (double v : values)
        {
            matrix.add(new COSFloat((float)v));
        }
        patternDictionary.setItem(COSName.MATRIX, matrix);
    }

    /**
     * This will get the extended graphics state for this pattern.
     *
     * @return The extended graphics state for this pattern.
     */
    public PDExtendedGraphicsState getExtendedGraphicsState()
    {
        if (extendedGraphicsState == null) 
        {
            COSDictionary dictionary = (COSDictionary)patternDictionary.getDictionaryObject( COSName.EXT_G_STATE );
            if( dictionary != null )
            {
                extendedGraphicsState = new PDExtendedGraphicsState( dictionary );
            }
        }
        return extendedGraphicsState;
    }

    /**
     * This will set the extended graphics state for this pattern.
     *
     * @param extendedGraphicsState The new extended graphics state for this pattern.
     */
    public void setExtendedGraphicsState( PDExtendedGraphicsState extendedGraphicsState )
    {
        this.extendedGraphicsState = extendedGraphicsState;
        if (extendedGraphicsState != null)
        {
            patternDictionary.setItem( COSName.EXT_G_STATE, extendedGraphicsState );
        }
        else
        {
            patternDictionary.removeItem(COSName.EXT_G_STATE);
        }
    }

}
