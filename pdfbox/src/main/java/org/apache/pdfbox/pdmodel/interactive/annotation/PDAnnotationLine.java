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
package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDAppearanceHandler;
import org.apache.pdfbox.pdmodel.interactive.annotation.handlers.PDLineAppearanceHandler;

/**
 * This is the class that represents a line annotation. Introduced in PDF 1.3 specification
 *
 * @author Paul King
 */
public class PDAnnotationLine extends PDAnnotationMarkup
{
    private PDAppearanceHandler customAppearanceHandler;

    /*
     * The various values for intent (get/setIT, see the PDF 1.6 reference Table 8.22
     */

    /**
     * Constant for annotation intent of Arrow.
     */
    public static final String IT_LINE_ARROW = "LineArrow";

    /**
     * Constant for annotation intent of a dimension line.
     */
    public static final String IT_LINE_DIMENSION = "LineDimension";

    /*
     * The various values for line ending styles, see the PDF 1.6 reference Table 8.23
     */

    /**
     * Constant for a square line ending.
     */
    public static final String LE_SQUARE = "Square";

    /**
     * Constant for a circle line ending.
     */
    public static final String LE_CIRCLE = "Circle";

    /**
     * Constant for a diamond line ending.
     */
    public static final String LE_DIAMOND = "Diamond";

    /**
     * Constant for a open arrow line ending.
     */
    public static final String LE_OPEN_ARROW = "OpenArrow";

    /**
     * Constant for a closed arrow line ending.
     */
    public static final String LE_CLOSED_ARROW = "ClosedArrow";

    /**
     * Constant for no line ending.
     */
    public static final String LE_NONE = "None";

    /**
     * Constant for a butt line ending.
     */
    public static final String LE_BUTT = "Butt";

    /**
     * Constant for a reversed open arrow line ending.
     */
    public static final String LE_R_OPEN_ARROW = "ROpenArrow";

    /**
     * Constant for a reversed closed arrow line ending.
     */
    public static final String LE_R_CLOSED_ARROW = "RClosedArrow";

    /**
     * Constant for a slash line ending.
     */
    public static final String LE_SLASH = "Slash";

    /**
     * The type of annotation.
     */
    public static final String SUB_TYPE = "Line";

    /**
     * Constructor.
     */
    public PDAnnotationLine()
    {
        getCOSObject().setName(COSName.SUBTYPE, SUB_TYPE);
        // Dictionary value L is mandatory, fill in with arbitrary value
        setLine(new float[] { 0, 0, 0, 0 });
    }

    /**
     * Creates a Line annotation from a COSDictionary, expected to be a correct object definition.
     *
     * @param field the PDF object to represent as a field.
     */
    public PDAnnotationLine(COSDictionary field)
    {
        super(field);
    }

    /**
     * This will set start and end coordinates of the line (or leader line if LL entry is set).
     *
     * @param l array of 4 floats [x1, y1, x2, y2] line start and end points in default user space.
     */
    public void setLine(float[] l)
    {
        COSArray newL = new COSArray();
        newL.setFloatArray(l);
        getCOSObject().setItem(COSName.L, newL);
    }

    /**
     * This will retrieve the start and end coordinates of the line (or leader line if LL entry is set).
     *
     * @return array of floats [x1, y1, x2, y2] line start and end points in default user space.
     */
    public float[] getLine()
    {
        COSArray l = getCOSObject().getCOSArray(COSName.L);
        return l != null ? l.toFloatArray() : null;
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
        if (array == null || array.size() == 0)
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
            return array.getName(0, LE_NONE);
        }
        return LE_NONE;
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
            return array.getName(1, LE_NONE);
        }
        return LE_NONE;
    }

    /**
     * This will set interior color of the line endings defined in the LE entry. color is in DeviceRGB color space.
     *
     * @param ic color in the DeviceRGB color space.
     */
    public void setInteriorColor(PDColor ic)
    {
        getCOSObject().setItem(COSName.IC, ic.toCOSArray());
    }

    /**
     * This will retrieve the interior color of the line endings defined in the LE entry. color is in DeviceRGB color
     * space.
     *
     * @return object representing the color.
     */
    public PDColor getInteriorColor()
    {
        return getColor(COSName.IC);
    }

    /**
     * This will set if the contents are shown as a caption to the line.
     *
     * @param cap Boolean value.
     */
    public void setCaption(boolean cap)
    {
        getCOSObject().setBoolean(COSName.CAP, cap);
    }

    /**
     * This will retrieve whether the text specified by the /Contents or /RC entries shall be
     * shown as a caption in the appearance of the line.
     *
     * @return boolean if the contents shall be shown as a caption (default: false).
     */
    public boolean hasCaption()
    {
        return getCOSObject().getBoolean(COSName.CAP, false);
    }

    /**
     * This will retrieve the length of the leader line.
     * 
     * @return the length of the leader line
     */
    public float getLeaderLineLength()
    {
        return this.getCOSObject().getFloat(COSName.LL, 0);
    }

    /**
     * This will set the length of the leader line.
     * 
     * @param leaderLineLength length of the leader line
     */
    public void setLeaderLineLength(float leaderLineLength)
    {
        this.getCOSObject().setFloat(COSName.LL, leaderLineLength);
    }

    /**
     * This will retrieve the length of the leader line extensions.
     * 
     * @return the length of the leader line extensions
     */
    public float getLeaderLineExtensionLength()
    {
        return this.getCOSObject().getFloat(COSName.LLE, 0);
    }

    /**
     * This will set the length of the leader line extensions.
     * 
     * @param leaderLineExtensionLength length of the leader line extensions
     */
    public void setLeaderLineExtensionLength(float leaderLineExtensionLength)
    {
        this.getCOSObject().setFloat(COSName.LLE, leaderLineExtensionLength);
    }

    /**
     * This will retrieve the length of the leader line offset.
     * 
     * @return the length of the leader line offset
     */
    public float getLeaderLineOffsetLength()
    {
        return this.getCOSObject().getFloat(COSName.LLO, 0);
    }

    /**
     * This will set the length of the leader line offset.
     * 
     * @param leaderLineOffsetLength length of the leader line offset
     */
    public void setLeaderLineOffsetLength(float leaderLineOffsetLength)
    {
        this.getCOSObject().setFloat(COSName.LLO, leaderLineOffsetLength);
    }

    /**
     * This will retrieve the caption positioning.
     * 
     * @return the caption positioning
     */
    public String getCaptionPositioning()
    {
        return this.getCOSObject().getNameAsString(COSName.CP);
    }

    /**
     * This will set the caption positioning. Allowed values are: "Inline" and "Top"
     * 
     * @param captionPositioning caption positioning
     */
    public void setCaptionPositioning(String captionPositioning)
    {
        this.getCOSObject().setName(COSName.CP, captionPositioning);
    }

    /**
     * This will set the horizontal offset of the caption.
     * 
     * @param offset the horizontal offset of the caption
     */
    public void setCaptionHorizontalOffset(float offset)
    {
        COSArray array = getCOSObject().getCOSArray(COSName.CO);
        if (array == null)
        {
            array = new COSArray();
            array.setFloatArray(new float[] { offset, 0.f });
            getCOSObject().setItem(COSName.CO, array);
        }
        else
        {
            array.set(0, new COSFloat(offset));
        }
    }

    /**
     * This will retrieve the horizontal offset of the caption.
     * 
     * @return the horizontal offset of the caption
     */
    public float getCaptionHorizontalOffset()
    {
        COSArray array = getCOSObject().getCOSArray(COSName.CO);
        return array != null ? array.toFloatArray()[0] : 0.f;
    }

    /**
     * This will set the vertical offset of the caption.
     * 
     * @param offset vertical offset of the caption
     */
    public void setCaptionVerticalOffset(float offset)
    {
        COSArray array = getCOSObject().getCOSArray(COSName.CO);
        if (array == null)
        {
            array = new COSArray();
            array.setFloatArray(new float[] { 0.f, offset });
            this.getCOSObject().setItem(COSName.CO, array);
        }
        else
        {
            array.set(1, new COSFloat(offset));
        }
    }

    /**
     * This will retrieve the vertical offset of the caption.
     * 
     * @return the vertical offset of the caption
     */
    public float getCaptionVerticalOffset()
    {
        COSArray array = getCOSObject().getCOSArray(COSName.CO);
        return array != null ? array.toFloatArray()[1] : 0.f;
    }

    /**
     * Set a custom appearance handler for generating the annotations appearance streams.
     * 
     * @param appearanceHandler
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
            PDLineAppearanceHandler appearanceHandler = new PDLineAppearanceHandler(this, document);
            appearanceHandler.generateAppearanceStreams();
        }
        else
        {
            customAppearanceHandler.generateAppearanceStreams();
        }
    }
}
