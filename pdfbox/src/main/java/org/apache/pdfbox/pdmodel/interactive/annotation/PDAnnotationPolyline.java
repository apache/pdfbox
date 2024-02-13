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
    public PDAnnotationPolyline(COSDictionary dict)
    {
        super(dict);
    }

    /**
     * This will set the line ending style for the start point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setStartPointEndingStyle(String style)
    {
        String actualStyle = style == null ? PDAnnotationLine.LE_NONE : style;
        COSArray array = getCOSObject().getCOSArray(COSName.LE);
        if (array == null || array.isEmpty())
        {
            array = new COSArray();
            array.add(COSName.getPDFName(actualStyle));
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            getCOSObject().setItem(COSName.LE, array);
        }
        else
        {
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
        COSArray array = getCOSObject().getCOSArray(COSName.LE);
        if (array != null && array.size() >= 2)
        {
            return array.getName(0, PDAnnotationLine.LE_NONE);
        }
        return PDAnnotationLine.LE_NONE;
    }

    /**
     * This will set the line ending style for the end point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setEndPointEndingStyle(String style)
    {
        String actualStyle = style == null ? PDAnnotationLine.LE_NONE : style;
        COSArray array = getCOSObject().getCOSArray(COSName.LE);
        if (array == null || array.size() < 2)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            array.add(COSName.getPDFName(actualStyle));
            getCOSObject().setItem(COSName.LE, array);
        }
        else
        {
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
        COSArray array = getCOSObject().getCOSArray(COSName.LE);
        if (array != null && array.size() >= 2)
        {
            return array.getName(1, PDAnnotationLine.LE_NONE);
        }
        return PDAnnotationLine.LE_NONE;
    }

    /**
     * This will set interior color of the line endings defined in the LE entry.
     *
     * @param ic color.
     */
    public void setInteriorColor(PDColor ic)
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
        COSArray vertices = getCOSObject().getCOSArray(COSName.VERTICES);
        return vertices != null ? vertices.toFloatArray() : null;
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
            PDPolylineAppearanceHandler appearanceHandler = new PDPolylineAppearanceHandler(this, document);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}
