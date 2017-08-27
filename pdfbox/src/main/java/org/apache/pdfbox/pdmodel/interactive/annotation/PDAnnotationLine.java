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
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;

/**
 * This is the class that represents a line annotation. Introduced in PDF 1.3 specification
 *
 * @author Paul King
 */
public class PDAnnotationLine extends PDAnnotationMarkup
{

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
     * Constant for a revered closed arrow line ending.
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
        getCOSObject().setItem(COSName.SUBTYPE, COSName.getPDFName(SUB_TYPE));
        // Dictionary value L is mandatory, fill in with arbitary value
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
        COSArray l = (COSArray) getCOSObject().getDictionaryObject(COSName.L);
        return l.toFloatArray();
    }

    /**
     * This will set the line ending style for the start point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public void setStartPointEndingStyle(String style)
    {
        if (style == null)
        {
            style = LE_NONE;
        }
        COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        COSArray array;
        if (!(base instanceof COSArray) || ((COSArray) base).size() == 0)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(style));
            array.add(COSName.getPDFName(LE_NONE));
            getCOSObject().setItem(COSName.LE, array);
        }
        else
        {
            array = (COSArray) base;
            array.setName(0, style);
        }
    }

    /**
     * This will retrieve the line ending style for the start point, possible values shown in the LE_ constants section.
     *
     * @return The ending style for the start point.
     */
    public String getStartPointEndingStyle()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        if (base instanceof COSArray && ((COSArray) base).size() >= 2)
        {
            return ((COSArray) base).getName(0);
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
        if (style == null)
        {
            style = LE_NONE;
        }
        COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        COSArray array;
        if (!(base instanceof COSArray) || ((COSArray) base).size() < 2)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(LE_NONE));
            array.add(COSName.getPDFName(style));
            getCOSObject().setItem(COSName.LE, array);
        }
        else
        {
            array = (COSArray) base;
            array.setName(1, style);
        }
    }

    /**
     * This will retrieve the line ending style for the end point, possible values shown in the LE_ constants section.
     *
     * @return The ending style for the end point.
     */
    public String getEndPointEndingStyle()
    {
        COSBase base = getCOSObject().getDictionaryObject(COSName.LE);
        if (base instanceof COSArray && ((COSArray) base).size() >= 2)
        {
            return ((COSArray) base).getName(1);
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
     * This will retrieve if the contents are shown as a caption or not.
     *
     * @return boolean if the content is shown as a caption.
     */
    public boolean getCaption()
    {
        return getCOSObject().getBoolean(COSName.CAP, false);
    }

    /**
     * This will set the border style dictionary, specifying the width and dash pattern used in drawing the line.
     *
     * @param bs the border style dictionary to set.
     *
     */
    @Override
    public void setBorderStyle(PDBorderStyleDictionary bs)
    {
        this.getCOSObject().setItem(COSName.BS, bs);
    }

    /**
     * This will retrieve the border style dictionary, specifying the width and dash pattern used in drawing the line.
     *
     * @return the border style dictionary.
     */
    @Override
    public PDBorderStyleDictionary getBorderStyle()
    {
        COSBase bs = getCOSObject().getDictionaryObject(COSName.BS);
        if (bs instanceof COSDictionary)
        {
            return new PDBorderStyleDictionary((COSDictionary) bs);
        }
        return null;
    }

    /**
     * This will retrieve the length of the leader line.
     * 
     * @return the length of the leader line
     */
    public float getLeaderLineLength()
    {
        return this.getCOSObject().getFloat(COSName.LL);
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
        return this.getCOSObject().getFloat(COSName.LLE);
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
        return this.getCOSObject().getFloat(COSName.LLO);
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
        return this.getCOSObject().getString(COSName.CP);
    }

    /**
     * This will set the caption positioning. Allowed values are: "Inline" and "Top"
     * 
     * @param captionPositioning caption positioning
     */
    public void setCaptionPositioning(String captionPositioning)
    {
        this.getCOSObject().setString(COSName.CP, captionPositioning);
    }

    /**
     * This will set the horizontal offset of the caption.
     * 
     * @param offset the horizontal offset of the caption
     */
    public void setCaptionHorizontalOffset(float offset)
    {
        COSArray array = (COSArray) this.getCOSObject().getDictionaryObject(COSName.CO);
        if (array == null)
        {
            array = new COSArray();
            array.setFloatArray(new float[] { offset, 0.f });
            this.getCOSObject().setItem(COSName.CO, array);
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
        float retval = 0.f;
        COSArray array = (COSArray) this.getCOSObject().getDictionaryObject(COSName.CO);
        if (array != null)
        {
            retval = array.toFloatArray()[0];
        }

        return retval;
    }

    /**
     * This will set the vertical offset of the caption.
     * 
     * @param offset vertical offset of the caption
     */
    public void setCaptionVerticalOffset(float offset)
    {
        COSArray array = (COSArray) this.getCOSObject().getDictionaryObject(COSName.CO);
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
        float retval = 0.f;
        COSArray array = (COSArray) this.getCOSObject().getDictionaryObject(COSName.CO);
        if (array != null)
        {
            retval = array.toFloatArray()[1];
        }
        return retval;
    }

}
