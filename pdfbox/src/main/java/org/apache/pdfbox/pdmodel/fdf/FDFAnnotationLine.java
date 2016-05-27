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
package org.apache.pdfbox.pdmodel.fdf;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine;
import org.w3c.dom.Element;

/**
 * This represents a Line FDF annotation.
 *
 * @author Ben Litchfield
 * @author Johanneke Lamberink
 */
public class FDFAnnotationLine extends FDFAnnotation
{
    /**
     * COS Model value for SubType entry.
     */
    public static final String SUBTYPE = "Line";

    /**
     * Default constructor.
     */
    public FDFAnnotationLine()
    {
        super();
        annot.setName(COSName.SUBTYPE, SUBTYPE);
    }

    /**
     * Constructor.
     *
     * @param a An existing FDF Annotation.
     */
    public FDFAnnotationLine(COSDictionary a)
    {
        super(a);
    }

    /**
     * Constructor.
     *
     * @param element An XFDF element.
     *
     * @throws IOException If there is an error extracting information from the element.
     */
    public FDFAnnotationLine(Element element) throws IOException
    {
        super(element);
        annot.setName(COSName.SUBTYPE, SUBTYPE);

        String startCoords = element.getAttribute("start");
        if (startCoords == null || startCoords.isEmpty())
        {
            throw new IOException("Error: missing attribute 'start'");
        }
        String endCoords = element.getAttribute("end");
        if (endCoords == null || endCoords.isEmpty())
        {
            throw new IOException("Error: missing attribute 'end'");
        }
        String line = startCoords + "," + endCoords;
        String[] lineValues = line.split(",");
        if (lineValues.length != 4)
        {
            throw new IOException("Error: wrong amount of line coordinates");
        }
        float[] values = new float[4];
        for (int i = 0; i < 4; i++)
        {
            values[i] = Float.parseFloat(lineValues[i]);
        }
        setLine(values);

        String leaderLine = element.getAttribute("leaderLength");
        if (leaderLine != null && !leaderLine.isEmpty())
        {
            setLeaderLength(Float.parseFloat(leaderLine));
        }

        String leaderLineExtension = element.getAttribute("leaderExtend");
        if (leaderLineExtension != null && !leaderLineExtension.isEmpty())
        {
            setLeaderExtend(Float.parseFloat(leaderLineExtension));
        }

        String leaderLineOffset = element.getAttribute("leaderOffset");
        if (leaderLineOffset != null && !leaderLineOffset.isEmpty())
        {
            setLeaderOffset(Float.parseFloat(leaderLineOffset));
        }

        String startStyle = element.getAttribute("head");
        if (startStyle != null && !startStyle.isEmpty())
        {
            setStartPointEndingStyle(startStyle);
        }
        String endStyle = element.getAttribute("tail");
        if (endStyle != null && !endStyle.isEmpty())
        {
            setEndPointEndingStyle(endStyle);
        }

        String color = element.getAttribute("interior-color");
        if (color != null && color.length() == 7 && color.charAt(0) == '#')
        {
            int colorValue = Integer.parseInt(color.substring(1, 7), 16);
            setInteriorColor(new Color(colorValue));
        }

        String caption = element.getAttribute("caption");
        if (caption != null && !caption.isEmpty())
        {
            setCaption("yes".equals(caption));
        }

        String captionH = element.getAttribute("caption-offset-h");
        if (captionH != null && !captionH.isEmpty())
        {
            setCaptionHorizontalOffset(Float.parseFloat(captionH));
        }

        String captionV = element.getAttribute("caption-offset-v");
        if (captionV != null && !captionV.isEmpty())
        {
            setCaptionVerticalOffset(Float.parseFloat(captionV));
        }

        String captionStyle = element.getAttribute("caption-style");
        if (captionStyle != null && !captionStyle.isEmpty())
        {
            setCaptionStyle(captionStyle);
        }
    }

    /**
     * This will set start and end coordinates of the line (or leader line if LL entry is set).
     *
     * @param line array of 4 floats [x1, y1, x2, y2] line start and end points in default user space.
     */
    public final void setLine(float[] line)
    {
        COSArray newLine = new COSArray();
        newLine.setFloatArray(line);
        annot.setItem(COSName.L, newLine);
    }

    /**
     * This will retrieve the start and end coordinates of the line (or leader line if LL entry is set).
     *
     * @return array of floats [x1, y1, x2, y2] line start and end points in default user space.
     */
    public float[] getLine()
    {
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.L);
        if (array != null)
        {
            return array.toFloatArray();
        }
        else
        {
            return null; // Should never happen as this is a required item
        }
    }

    /**
     * This will set the line ending style for the start point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public final void setStartPointEndingStyle(String style)
    {
        if (style == null)
        {
            style = PDAnnotationLine.LE_NONE;
        }
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.LE);
        if (array == null)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(style));
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            annot.setItem(COSName.LE, array);
        }
        else
        {
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
        String retval = PDAnnotationLine.LE_NONE;
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.LE);
        if (array != null)
        {
            retval = array.getName(0);
        }

        return retval;
    }

    /**
     * This will set the line ending style for the end point, see the LE_ constants for the possible values.
     *
     * @param style The new style.
     */
    public final void setEndPointEndingStyle(String style)
    {
        if (style == null)
        {
            style = PDAnnotationLine.LE_NONE;
        }
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.LE);
        if (array == null)
        {
            array = new COSArray();
            array.add(COSName.getPDFName(PDAnnotationLine.LE_NONE));
            array.add(COSName.getPDFName(style));
            annot.setItem(COSName.LE, array);
        }
        else
        {
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
        String retval = PDAnnotationLine.LE_NONE;
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.LE);
        if (array != null)
        {
            retval = array.getName(1);
        }

        return retval;
    }

    /**
     * This will set interior color of the line endings defined in the LE entry.
     *
     * @param color The interior color of the line endings.
     */
    public final void setInteriorColor(Color color)
    {
        COSArray array = null;
        if (color != null)
        {
            float[] colors = color.getRGBColorComponents(null);
            array = new COSArray();
            array.setFloatArray(colors);
        }
        annot.setItem(COSName.IC, array);
    }

    /**
     * This will retrieve the interior color of the line endings defined in the LE entry.
     *
     * @return object representing the color.
     */
    public Color getInteriorColor()
    {
        Color retval = null;
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.IC);
        if (array != null)
        {
            float[] rgb = array.toFloatArray();
            if (rgb.length >= 3)
            {
                retval = new Color(rgb[0], rgb[1], rgb[2]);
            }
        }
        return retval;
    }

    /**
     * This will set if the contents are shown as a caption to the line.
     *
     * @param cap Boolean value.
     */
    public final void setCaption(boolean cap)
    {
        annot.setBoolean(COSName.CAP, cap);
    }

    /**
     * This will retrieve if the contents are shown as a caption or not.
     *
     * @return boolean if the content is shown as a caption.
     */
    public boolean getCaption()
    {
        return annot.getBoolean(COSName.CAP, false);
    }

    /**
     * This will retrieve the length of the leader line.
     * 
     * @return the length of the leader line
     */
    public float getLeaderLength()
    {
        return annot.getFloat(COSName.LL);
    }

    /**
     * This will set the length of the leader line.
     * 
     * @param leaderLength length of the leader line
     */
    public final void setLeaderLength(float leaderLength)
    {
        annot.setFloat(COSName.LL, leaderLength);
    }

    /**
     * This will retrieve the length of the leader line extensions.
     * 
     * @return the length of the leader line extensions
     */
    public float getLeaderExtend()
    {
        return annot.getFloat(COSName.LLE);
    }

    /**
     * This will set the length of the leader line extensions.
     * 
     * @param leaderExtend length of the leader line extensions
     */
    public final void setLeaderExtend(float leaderExtend)
    {
        annot.setFloat(COSName.LLE, leaderExtend);
    }

    /**
     * This will retrieve the length of the leader line offset.
     * 
     * @return the length of the leader line offset
     */
    public float getLeaderOffset()
    {
        return annot.getFloat(COSName.LLO);
    }

    /**
     * This will set the length of the leader line offset.
     * 
     * @param leaderOffset length of the leader line offset
     */
    public final void setLeaderOffset(float leaderOffset)
    {
        annot.setFloat(COSName.LLO, leaderOffset);
    }

    /**
     * This will retrieve the caption positioning.
     * 
     * @return the caption positioning
     */
    public String getCaptionStyle()
    {
        return annot.getString(COSName.CP);
    }

    /**
     * This will set the caption positioning. Allowed values are: "Inline" and "Top"
     * 
     * @param captionStyle caption positioning
     */
    public final void setCaptionStyle(String captionStyle)
    {
        annot.setString(COSName.CP, captionStyle);
    }

    /**
     * This will set the horizontal offset of the caption.
     * 
     * @param offset the horizontal offset of the caption
     */
    public final void setCaptionHorizontalOffset(float offset)
    {
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.CO);
        if (array == null)
        {
            array = new COSArray();
            array.setFloatArray(new float[] { offset, 0.f });
            annot.setItem(COSName.CO, array);
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
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.CO);
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
    public final void setCaptionVerticalOffset(float offset)
    {
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.CO);
        if (array == null)
        {
            array = new COSArray();
            array.setFloatArray(new float[] { 0.f, offset });
            annot.setItem(COSName.CO, array);
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
        COSArray array = (COSArray) annot.getDictionaryObject(COSName.CO);
        if (array != null)
        {
            retval = array.toFloatArray()[1];
        }
        return retval;
    }
}
