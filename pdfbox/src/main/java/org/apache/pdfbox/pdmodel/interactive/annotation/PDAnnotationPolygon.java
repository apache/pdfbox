/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDPolygonAppearanceHandler;

/**
 *
 * @author Paul King
 */
public class PDAnnotationPolygon extends PDAnnotationMarkup
{
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Polygon";

    private PDAppearanceHandler customAppearanceHandler;
    
    /**
     * Constructor.
     */
    public PDAnnotationPolygon()
    {
        getCOSObject().setName(COSName.SUBTYPE, SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param dict The annotations dictionary.
     */
    public PDAnnotationPolygon(COSDictionary dict)
    {
        super(dict);
    }

    // PDF 32000 specification has "the interior color with which to fill the annotation’s line endings"
    // but it is the inside of the polygon.
    
    /**
     * This will set interior color.
     *
     * @param ic color.
     */
    public void setInteriorColor(PDColor ic)
    {
        getCOSObject().setItem(COSName.IC, ic.toCOSArray());
    }

    /**
     * This will retrieve the interior color.
     *
     * @return object representing the color.
     */
    public PDColor getInteriorColor()
    {
        return getColor(COSName.IC);
    }

    /**
     * This will set the border effect dictionary, specifying effects to be applied when drawing the
     * line. This is supported by PDF 1.5 and higher.
     *
     * @param be The border effect dictionary to set.
     *
     */
    public void setBorderEffect(PDBorderEffectDictionary be)
    {
        getCOSObject().setItem(COSName.BE, be);
    }

    /**
     * This will retrieve the border effect dictionary, specifying effects to be applied used in
     * drawing the line.
     *
     * @return The border effect dictionary
     */
    public PDBorderEffectDictionary getBorderEffect()
    {
        COSDictionary be = getCOSObject().getCOSDictionary(COSName.BE);
        return be != null ? new PDBorderEffectDictionary(be) : null;
    }

    /**
     * This will retrieve the numbers that shall represent the alternating horizontal and vertical
     * coordinates.
     *
     * @return An array of floats representing the alternating horizontal and vertical coordinates.
     */
    public float[] getVertices()
    {
        COSArray array = getCOSObject().getCOSArray(COSName.VERTICES);
        return array != null ? array.toFloatArray() : null;
    }

    /**
     * This will set the numbers that shall represent the alternating horizontal and vertical
     * coordinates.
     *
     * @param points an array with the numbers that shall represent the alternating horizontal and
     * vertical coordinates.
     */
    public void setVertices(float[] points)
    {
        getCOSObject().setItem(COSName.VERTICES, COSArray.of(points));
    }

    /**
     * PDF 2.0: This will retrieve the arrays that shall represent the alternating horizontal
     * and vertical coordinates for path building.
     *
     * @return An array of float arrays, each supplying the operands for a path building operator
     * (m, l or c). The first array should have 2 elements, the others should have 2 or 6 elements.
     */
    public float[][] getPath()
    {
        COSArray array = getCOSObject().getCOSArray(COSName.PATH);
        if (array != null)
        {
            float[][] pathArray = new float[array.size()][];
            for (int i = 0; i < array.size(); ++i)
            {
                COSBase base2 = array.getObject(i);
                if (base2 instanceof COSArray)
                {
                    pathArray[i] = ((COSArray) base2).toFloatArray();
                }
                else
                {
                    pathArray[i] = new float[0];
                }
            }
            return pathArray;
        }
        return null;
    }

    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param appearanceHandler custom appearance handler
     */
    public void setCustomAppearanceHandler(PDAppearanceHandler appearanceHandler)
    {
        customAppearanceHandler = appearanceHandler;
    }

    @Override
    public void constructAppearances()
    {
        this.constructAppearances(null);
    }

    @Override
    public void constructAppearances(PDDocument document)
    {
        if (customAppearanceHandler == null)
        {
            PDPolygonAppearanceHandler appearanceHandler = new PDPolygonAppearanceHandler(this, document);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}
