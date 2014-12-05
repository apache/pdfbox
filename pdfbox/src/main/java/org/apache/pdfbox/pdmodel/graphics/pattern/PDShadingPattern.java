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
import org.apache.pdfbox.pdmodel.graphics.state.PDExternalGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.util.Matrix;

/**
 * A shading pattern dictionary.
 *
 * @author Andreas Lehmkühler
 */
public class PDShadingPattern extends PDAbstractPattern
{
    private PDExternalGraphicsState externalGraphicsState;
    private PDShading shading;

    /**
     * Creates a new shading pattern.
     */
    public PDShadingPattern()
    {
        super();
        getCOSDictionary().setInt(COSName.PATTERN_TYPE, PDAbstractPattern.TYPE_SHADING_PATTERN);
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
        return PDAbstractPattern.TYPE_SHADING_PATTERN;
    }

    /**
     * Returns the pattern matrix.
     */
    public Matrix getMatrix()
    {
        Matrix matrix = null;
        COSArray array = (COSArray)getCOSDictionary().getDictionaryObject(COSName.MATRIX);
        if (array != null)
        {
            matrix = new Matrix();
            matrix.setValue(0, 0, ((COSNumber) array.get(0)).floatValue());
            matrix.setValue(0, 1, ((COSNumber) array.get(1)).floatValue());
            matrix.setValue(1, 0, ((COSNumber) array.get(2)).floatValue());
            matrix.setValue(1, 1, ((COSNumber) array.get(3)).floatValue());
            matrix.setValue(2, 0, ((COSNumber) array.get(4)).floatValue());
            matrix.setValue(2, 1, ((COSNumber) array.get(5)).floatValue());
        }
        else
        {
            // default value is the identity matrix
            matrix = new Matrix();
        }
        return matrix;
    }

    /**
     * Sets the optional Matrix entry for the Pattern.
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
     * This will get the external graphics state for this pattern.
     * @return The extended graphics state for this pattern.
     */
    public PDExternalGraphicsState getExternalGraphicsState()
    {
        if (externalGraphicsState == null)
        {
            COSDictionary dictionary = (COSDictionary)getCOSDictionary()
                    .getDictionaryObject(COSName.EXT_G_STATE);

            if( dictionary != null )
            {
                externalGraphicsState = new PDExternalGraphicsState( dictionary );
            }
        }
        return externalGraphicsState;
    }

    /**
     * This will set the external graphics state for this pattern.
     * @param externalGraphicsState The new extended graphics state for this pattern.
     */
    public void setExternalGraphicsState( PDExternalGraphicsState externalGraphicsState )
    {
        this.externalGraphicsState = externalGraphicsState;
        if (externalGraphicsState != null)
        {
            getCOSDictionary().setItem( COSName.EXT_G_STATE, externalGraphicsState );
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
    public PDShading getShading() throws IOException
    {
        if (shading == null) 
        {
            COSDictionary dictionary = (COSDictionary)getCOSDictionary()
                    .getDictionaryObject(COSName.SHADING);

            if( dictionary != null )
            {
                shading = PDShading.create(dictionary);
            }
        }
        return shading;
    }

    /**
     * This will set the shading resources for this pattern.
     * @param shadingResources The new shading resources for this pattern.
     */
    public void setShading( PDShading shadingResources )
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
