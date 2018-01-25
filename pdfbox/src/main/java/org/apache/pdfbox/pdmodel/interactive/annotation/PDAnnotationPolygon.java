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
import org.apache.pdfbox.io.ScratchFile;
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

    private PDAppearanceHandler polygonAppearanceHandler;
    
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
     * This will retrieve the numbers that shall represent the alternating horizontal and vertical
     * coordinates.
     *
     * @return An array of floats representing the alternating horizontal and vertical coordinates.
     */
    public float[] getVertices()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.VERTICES);
        if (base instanceof COSArray)
        {
            return ((COSArray) base).toFloatArray();
        }
        return null;
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
        COSArray ar = new COSArray();
        ar.setFloatArray(points);
        getCOSObject().setItem(COSName.VERTICES, ar);
    }

    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param polygonAppearanceHandler
     */
    public void setCustomPolygonAppearanceHandler(PDAppearanceHandler polygonAppearanceHandler)
    {
        this.polygonAppearanceHandler = polygonAppearanceHandler;
    }

    @Override
    public void constructAppearances(ScratchFile scratchFile)
    {
        if (polygonAppearanceHandler == null)
        {
            PDPolygonAppearanceHandler appearanceHandler = new PDPolygonAppearanceHandler(this);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            polygonAppearanceHandler.generateAppearanceStreams();
        }
    }
}
