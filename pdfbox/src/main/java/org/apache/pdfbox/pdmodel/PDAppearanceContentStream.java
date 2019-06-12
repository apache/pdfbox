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
package org.apache.pdfbox.pdmodel;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSArray;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

/**
 * Provides the ability to write to an appearance content stream.
 *
 * @author Ben Litchfield
 */
public final class PDAppearanceContentStream extends PDAbstractContentStream implements Closeable
{
    /**
     * Create a new appearance stream.
     *
     * @param appearance
     *            The appearance stream to write to.
     * @throws IOException If there is an error writing to the content stream.
     */
    public PDAppearanceContentStream(PDAppearanceStream appearance) throws IOException
    {
        this(appearance, appearance.getStream().createOutputStream());
    }

    /**
     * Create a new appearance stream.
     *
     * @param appearance The appearance stream to write to.
     * @param compress whether the content stream is to be compressed. Set this to true when
     * creating long content streams.
     * @throws IOException If there is an error writing to the content stream.
     */
    public PDAppearanceContentStream(PDAppearanceStream appearance, boolean compress) throws IOException
    {
        this(appearance, appearance.getStream().createOutputStream(compress ? COSName.FLATE_DECODE : null));
    }

    /**
     * Create a new appearance stream.
     *
     * @param appearance
     *            The appearance stream to add to.
     * @param outputStream
     *            The appearances output stream to write to.
     */
    public PDAppearanceContentStream(PDAppearanceStream appearance, OutputStream outputStream)
    {
        super(null, outputStream, appearance.getResources());
    }

    /**
     * Set the stroking color.
     * 
     * <p>
     * The command is only emitted if the color is not null and the number of
     * components is &gt; 0.
     * 
     * @param color The colorspace to write.
     * @throws IOException If there is an error writing to the content stream.
     * @see PDAbstractContentStream#setStrokingColor(PDColor)
     */
    public boolean setStrokingColorOnDemand(PDColor color) throws IOException
    {
        if (color != null)
        {
            float[] components = color.getComponents();
            if (components.length > 0)
            {
                setStrokingColor(components);
                return true;
            }
        }
        return false;
    }

    /**
     * Set the stroking color.
     * 
     * @see PDAbstractContentStream#setStrokingColor(java.awt.Color)
     * @param components
     *            the color components dependent on the color space being used.
     * @throws IOException If there is an error writing to the content stream.
     */
    public void setStrokingColor(float[] components) throws IOException
    {
        for (float value : components)
        {
            writeOperand(value);
        }

        int numComponents = components.length;
        switch (numComponents)
        {
        case 1:
            writeOperator(OperatorName.STROKING_COLOR_GRAY);
            break;
        case 3:
            writeOperator(OperatorName.STROKING_COLOR_RGB);
            break;
        case 4:
            writeOperator(OperatorName.STROKING_COLOR_CMYK);
            break;
        default:
            break;
        }
        //TODO shouldn't we set the stack?
        //Or call the appropriate setStrokingColor() method from the base class?
    }

    /**
     * Set the non stroking color.
     * 
     * <p>
     * The command is only emitted if the color is not null and the number of
     * components is &gt; 0.
     * 
     * @param color The colorspace to write.
     * @throws IOException If there is an error writing to the content stream.
     * @see PDAbstractContentStream#setNonStrokingColor(PDColor)
     */
    public boolean setNonStrokingColorOnDemand(PDColor color) throws IOException
    {
        if (color != null)
        {
            float[] components = color.getComponents();
            if (components.length > 0)
            {
                setNonStrokingColor(components);
                return true;
            }
        }
        return false;
    }

    /**
     * Set the non stroking color.
     * 
     * @see PDAbstractContentStream#setNonStrokingColor(java.awt.Color)
     * @param components
     *            the color components dependent on the color space being used.
     * @throws IOException If there is an error writing to the content stream.
     */
    public void setNonStrokingColor(float[] components) throws IOException
    {
        for (float value : components)
        {
            writeOperand(value);
        }

        int numComponents = components.length;
        switch (numComponents)
        {
        case 1:
            writeOperator(OperatorName.NON_STROKING_GRAY);
            break;
        case 3:
            writeOperator(OperatorName.NON_STROKING_RGB);
            break;
        case 4:
            writeOperator(OperatorName.NON_STROKING_CMYK);
            break;
        default:
            break;
        }
        //TODO shouldn't we set the stack?
        //Or call the appropriate setNonStrokingColor() method from the base class?
    }

    /**
     * Convenience method for annotations: sets the line with and dash style.
     *
     * @param lineWidth The line width.
     * @param bs The border style, may be null.
     * @param border The border array, must have at least three entries. This is
     * only used if the border style is null.
     *
     * @throws IOException If there is an error writing to the content stream.
     */
    public void setBorderLine(float lineWidth, PDBorderStyleDictionary bs,
                                               COSArray border) throws IOException
    {
        // Can't use PDBorderStyleDictionary.getDashStyle() as
        // this will return a default dash style if non is existing
        if (bs != null && bs.getCOSObject().containsKey(COSName.D) && 
                          bs.getStyle().equals(PDBorderStyleDictionary.STYLE_DASHED))
        {
            setLineDashPattern(bs.getDashStyle().getDashArray(), 0);
        }
        else if (bs == null && border.size() > 3 && border.getObject(3) instanceof COSArray)
        {
            setLineDashPattern(((COSArray) border.getObject(3)).toFloatArray(), 0);
        }
        setLineWidthOnDemand(lineWidth);
    }

    /**
     * Sets the line width. The command is only emitted if the lineWidth is
     * different to 1.
     * 
     * @param lineWidth the line width of the path.
     * @throws IOException If there is an error writing to the content stream.
     * @see PDAbstractContentStream#setLineWidth(float)
     */
    public void setLineWidthOnDemand(float lineWidth) throws IOException
    {
        // Acrobat doesn't write a line width command
        // for a line width of 1 as this is default.
        // Will do the same.
        if (!(Math.abs(lineWidth - 1) < 1e-6))
        {
            setLineWidth(lineWidth);
        }
    }
    
    /**
     * Draw a shape.
     *
     * <p>
     * Dependent on the lineWidth and whether or not there is a background to be generated there are
     * different commands to be used for draw a shape.
     *
     * @param lineWidth the line width of the path.
     * @param hasStroke shall there be a stroking color.
     * @param hasFill shall there be a fill color.
     * @throws IOException If there is an error writing to the content stream.
     */
    public void drawShape(float lineWidth, boolean hasStroke, boolean hasFill) throws IOException
    {
        // initial setting if stroking shall be done
        boolean resolvedHasStroke = hasStroke;

        // no stroking for very small lines
        if (lineWidth < 1e-6)
        {
            resolvedHasStroke = false;
        }
        if (hasFill && resolvedHasStroke)
        {
            fillAndStroke();
        }
        else if (resolvedHasStroke)
        {
            stroke();
        }
        else if (hasFill)
        {
            fill();
        }
        else
        {
            writeOperator(OperatorName.ENDPATH);
        }
    }
}
