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
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.graphics.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShadingResources;
import org.apache.pdfbox.util.Matrix;

/**
 * A shading pattern dictionary.
 * @author Andreas Lehmkühler
 */
public class PDShadingPattern extends PDPatternDictionary
{
    private PDExtendedGraphicsState extendedGraphicsState;
    private PDShadingResources shading;
    private COSArray matrix = null;

    /**
     * Creates a new shading pattern.
     */
    public PDShadingPattern()
    {
        super();
        getCOSDictionary().setInt(COSName.PATTERN_TYPE, PDPatternDictionary.TYPE_SHADING_PATTERN);
    }

    /**
     * Creates a new shading pattern from the given COS dictionary.
     * @param resourceDictionary The COSDictionary for this pattern resource.
     */
    public PDShadingPattern(COSDictionary resourceDictionary)
    {
        super(resourceDictionary);
    }

    @Override
    public int getPatternType()
    {
        return PDPatternDictionary.TYPE_SHADING_PATTERN;
    }

    /**
     * This will get the optional Matrix of a Pattern.
     * It maps the form space into the user space.
     * @return the form matrix
     */
    public Matrix getMatrix()
    {
        Matrix returnMatrix = null;
        if (matrix == null)
        {
            matrix = (COSArray)getCOSDictionary().getDictionaryObject( COSName.MATRIX );
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
        getCOSDictionary().setItem(COSName.MATRIX, matrix);
    }

    /**
     * This will get the extended graphics state for this pattern.
     * @return The extended graphics state for this pattern.
     */
    public PDExtendedGraphicsState getExtendedGraphicsState()
    {
        if (extendedGraphicsState == null) 
        {
            COSDictionary dictionary = (COSDictionary)getCOSDictionary().getDictionaryObject( COSName.EXT_G_STATE );
            if( dictionary != null )
            {
                extendedGraphicsState = new PDExtendedGraphicsState( dictionary );
            }
        }
        return extendedGraphicsState;
    }

    /**
     * This will set the extended graphics state for this pattern.
     * @param extendedGraphicsState The new extended graphics state for this pattern.
     */
    public void setExtendedGraphicsState( PDExtendedGraphicsState extendedGraphicsState )
    {
        this.extendedGraphicsState = extendedGraphicsState;
        if (extendedGraphicsState != null)
        {
            getCOSDictionary().setItem( COSName.EXT_G_STATE, extendedGraphicsState );
        }
        else
        {
            getCOSDictionary().removeItem(COSName.EXT_G_STATE);
        }
    }

    /**
     * This will get the shading resources for this pattern.
     * @return The shading resources for this pattern.
     * @throws IOException if something went wrong
     */
    public PDShadingResources getShading() throws IOException
    {
        if (shading == null) 
        {
            COSDictionary dictionary = (COSDictionary)getCOSDictionary().getDictionaryObject( COSName.SHADING );
            if( dictionary != null )
            {
                shading = PDShadingResources.create(dictionary);
            }
        }
        return shading;
    }

    /**
     * This will set the shading resources for this pattern.
     * @param shadingResources The new shading resources for this pattern.
     */
    public void setShading( PDShadingResources shadingResources )
    {
        shading = shadingResources;
        if (shadingResources != null)
        {
            getCOSDictionary().setItem( COSName.SHADING, shadingResources );
        }
        else
        {
            getCOSDictionary().removeItem(COSName.SHADING);
        }
    }
}
