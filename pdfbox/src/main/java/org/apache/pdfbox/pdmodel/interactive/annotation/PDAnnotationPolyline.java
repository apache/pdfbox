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
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDPolylineAppearanceHandler;

/**
 *
 * @author Paul King
 */
public class PDAnnotationPolyline extends PDAnnotationMarkup
{
    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "PolyLine";

    private PDAppearanceHandler customAppearanceHandler;
    
    /**
     * Constructor.
     */
    public PDAnnotationPolyline()
    {
        getCOSObject().setName(COSName.SUBTYPE, SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param dict The annotations dictionary.
     */
    public PDAnnotationPolyline(final COSDictionary dict)
    {
        super(dict);
    }

    /**
     * This will set the line ending style for the start point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setStartPointEndingStyle(final String style)
    {
        final String actualStyle = style == null ? PDAnnotationLine.LE_NONE : style;
        final COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        final COSArray array;
        if (!(base instanceof COSArray) || ((COSArray) base).size() == 0)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(actualStyle));
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            getCOSObject().setItem(COSName.LE, array);
        }
        else
        {
            array = (COSArray) base;
            array.setName(0, actualStyle);
        }
    }

    /**
     * This will retrieve the line ending style for the start point, possible values shown in the LE_ constants section.
     *
     * @return The ending style for the start point, LE_NONE if missing, never null.
     */
    public String getStartPointEndingStyle()
    {
        final COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        if (base instanceof COSArray && ((COSArray) base).size() >= 2)
        {
            return ((COSArray) base).getName(0, PDAnnotationLine.LE_NONE);
        }
        return PDAnnotationLine.LE_NONE;
    }

    /**
     * This will set the line ending style for the end point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setEndPointEndingStyle(final String style)
    {
        final String actualStyle = style == null ? PDAnnotationLine.LE_NONE : style;
        final COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        final COSArray array;
        if (!(base instanceof COSArray) || ((COSArray) base).size() < 2)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            array.add(COSName.getPDFName(actualStyle));
            getCOSObject().setItem(COSName.LE, array);
        }
        else
        {
            array = (COSArray) base;
            array.setName(1, actualStyle);
        }
    }

    /**
     * This will retrieve the line ending style for the end point, possible values shown in the LE_ constants section.
     *
     * @return The ending style for the end point, LE_NONE if missing, never null.
     */
    public String getEndPointEndingStyle()
    {
        final COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        if (base instanceof COSArray && ((COSArray) base).size() >= 2)
        {
            return ((COSArray) base).getName(1, PDAnnotationLine.LE_NONE);
        }
        return PDAnnotationLine.LE_NONE;
    }

    /**
     * This will set interior color of the line endings defined in the LE entry.
     *
     * @param ic color.
     */
    public void setInteriorColor(final PDColor ic)
    {
        getCOSObject().setItem(COSName.IC, ic.toCOSArray());
    }

    /**
     * This will retrieve the interior color with which to fill the annotation’s line endings.
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
        final COSBase base = getCOSObject().getDictionaryObject(COSName.VERTICES);
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
    public void setVertices(final float[] points)
    {
        final COSArray ar = new COSArray();
        ar.setFloatArray(points);
        getCOSObject().setItem(COSName.VERTICES, ar);
    }

    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param appearanceHandler
     */
    public void setCustomAppearanceHandler(final PDAppearanceHandler appearanceHandler)
    {
        customAppearanceHandler = appearanceHandler;
    }

    @Override
    public void constructAppearances()
    {
        this.constructAppearances(null);
    }

    @Override
    public void constructAppearances(final PDDocument document)
    {
        if (customAppearanceHandler == null)
        {
            final PDPolylineAppearanceHandler appearanceHandler = new PDPolylineAppearanceHandler(this, document);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}
