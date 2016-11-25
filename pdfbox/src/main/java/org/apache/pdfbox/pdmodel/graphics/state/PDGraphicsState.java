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
package org.apache.pdfbox.pdmodel.graphics.state;

import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import org.apache.pdfbox.cos.COSBase;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendComposite;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.util.Matrix;

/**
 * The current state of the graphics parameters when executing a content stream.
 *
 * @author Ben Litchfield
 */
public class PDGraphicsState implements Cloneable
{
    private boolean isClippingPathDirty;
    private Area clippingPath;
    private Matrix currentTransformationMatrix = new Matrix();
    private PDColor strokingColor = PDDeviceGray.INSTANCE.getInitialColor();
    private PDColor nonStrokingColor = PDDeviceGray.INSTANCE.getInitialColor();
    private PDColorSpace strokingColorSpace = PDDeviceGray.INSTANCE;
    private PDColorSpace nonStrokingColorSpace = PDDeviceGray.INSTANCE;
    private PDTextState textState = new PDTextState();
    private float lineWidth = 1;
    private int lineCap = BasicStroke.CAP_BUTT;
    private int lineJoin = BasicStroke.JOIN_MITER;
    private float miterLimit = 10;
    private PDLineDashPattern lineDashPattern = new PDLineDashPattern();
    private RenderingIntent renderingIntent;
    private boolean strokeAdjustment = false;
    private BlendMode blendMode = BlendMode.COMPATIBLE;
    private PDSoftMask softMask;
    private double alphaConstant = 1.0;
    private double nonStrokingAlphaConstant = 1.0;
    private boolean alphaSource = false;

    // DEVICE-DEPENDENT parameters
    private boolean overprint = false;
    private boolean nonStrokingOverprint = false;
    private double overprintMode = 0;
    //black generation
    //undercolor removal
    private COSBase transfer = null;
    //halftone
    private double flatness = 1.0;
    private double smoothness = 0;

    /**
     * Constructor with a given page size to initialize the clipping path.
     * @param page the size of the page
     */
    public PDGraphicsState(PDRectangle page)
    {
        clippingPath = new Area(page.toGeneralPath());
    }

    /**
     * Get the value of the CTM.
     *
     * @return The current transformation matrix.
     */
    public Matrix getCurrentTransformationMatrix()
    {
        return currentTransformationMatrix;
    }

    /**
     * Set the value of the CTM.
     *
     * @param value The current transformation matrix.
     */
    public void setCurrentTransformationMatrix(Matrix value)
    {
        currentTransformationMatrix = value;
    }

    /**
     * Get the value of the line width.
     *
     * @return The current line width.
     */
    public float getLineWidth()
    {
        return lineWidth;
    }

    /**
     * set the value of the line width.
     *
     * @param value The current line width.
     */
    public void setLineWidth(float value)
    {
        lineWidth = value;
    }

    /**
     * Get the value of the line cap.
     *
     * @return The current line cap.
     */
    public int getLineCap()
    {
        return lineCap;
    }

    /**
     * set the value of the line cap.
     *
     * @param value The current line cap.
     */
    public void setLineCap(int value)
    {
        lineCap = value;
    }

    /**
     * Get the value of the line join.
     *
     * @return The current line join value.
     */
    public int getLineJoin()
    {
        return lineJoin;
    }

    /**
     * Get the value of the line join.
     *
     * @param value The current line join
     */
    public void setLineJoin(int value)
    {
        lineJoin = value;
    }

    /**
     * Get the value of the miter limit.
     *
     * @return The current miter limit.
     */
    public float getMiterLimit()
    {
        return miterLimit;
    }

    /**
     * set the value of the miter limit.
     *
     * @param value The current miter limit.
     */
    public void setMiterLimit(float value)
    {
        miterLimit = value;
    }

    /**
     * Get the value of the stroke adjustment parameter.
     *
     * @return The current stroke adjustment.
     */
    public boolean isStrokeAdjustment()
    {
        return strokeAdjustment;
    }

    /**
     * set the value of the stroke adjustment.
     *
     * @param value The value of the stroke adjustment parameter.
     */
    public void setStrokeAdjustment(boolean value)
    {
        strokeAdjustment = value;
    }

    /**
     * Get the value of the stroke alpha constant property.
     *
     * @return The value of the stroke alpha constant parameter.
     */
    public double getAlphaConstant()
    {
        return alphaConstant;
    }

    /**
     * set the value of the stroke alpha constant property.
     *
     * @param value The value of the stroke alpha constant parameter.
     */
    public void setAlphaConstant(double value)
    {
        alphaConstant = value;
    }

    /**
     * Get the value of the non-stroke alpha constant property.
     *
     * @return The value of the non-stroke alpha constant parameter.
     * @deprecated use {@link #getNonStrokeAlphaConstant() }
     */
    @Deprecated
    public double getNonStrokeAlphaConstants()
    {
        return nonStrokingAlphaConstant;
    }

    /**
     * set the value of the non-stroke alpha constant property.
     *
     * @param value The value of the non-stroke alpha constant parameter.
     * @deprecated use {@link #setNonStrokeAlphaConstant(double) }
     */
    @Deprecated
    public void setNonStrokeAlphaConstants(double value)
    {
        nonStrokingAlphaConstant = value;
    }

    /**
     * Get the value of the non-stroke alpha constant property.
     *
     * @return The value of the non-stroke alpha constant parameter.
     */
    public double getNonStrokeAlphaConstant()
    {
        return nonStrokingAlphaConstant;
    }

    /**
     * set the value of the non-stroke alpha constant property.
     *
     * @param value The value of the non-stroke alpha constant parameter.
     */
    public void setNonStrokeAlphaConstant(double value)
    {
        nonStrokingAlphaConstant = value;
    }

    /**
     * get the value of the stroke alpha source property.
     *
     * @return The value of the stroke alpha source parameter.
     */
    public boolean isAlphaSource()
    {
        return alphaSource;
    }

    /**
     * set the value of the alpha source property.
     *
     * @param value The value of the alpha source parameter.
     */
    public void setAlphaSource(boolean value)
    {
        alphaSource = value;
    }

    /**
     * returns the current softmask
     *
     * @return softMask
     */
    public PDSoftMask getSoftMask() 
    {
        return softMask;
    }


    /**
     * Sets the current soft mask
     *
     * @param softMask
     */
    public void setSoftMask(PDSoftMask softMask)
    {
        this.softMask = softMask;
    }

    /**
     * Returns the current blend mode
     *
     * @return the current blend mode
     */
    public BlendMode getBlendMode()
    {
        return blendMode;
    }

    /**
     * Sets the blend mode in the current graphics state
     *
     * @param blendMode
     */
    public void setBlendMode(BlendMode blendMode)
    {
        this.blendMode = blendMode;
    }

    /**
     * get the value of the overprint property.
     *
     * @return The value of the overprint parameter.
     */
    public boolean isOverprint()
    {
        return overprint;
    }

    /**
     * set the value of the overprint property.
     *
     * @param value The value of the overprint parameter.
     */
    public void setOverprint(boolean value)
    {
        overprint = value;
    }

    /**
     * get the value of the non stroking overprint property.
     *
     * @return The value of the non stroking overprint parameter.
     */
    public boolean isNonStrokingOverprint()
    {
        return nonStrokingOverprint;
    }

    /**
     * set the value of the non stroking overprint property.
     *
     * @param value The value of the non stroking overprint parameter.
     */
    public void setNonStrokingOverprint(boolean value)
    {
        nonStrokingOverprint = value;
    }

    /**
     * get the value of the overprint mode property.
     *
     * @return The value of the overprint mode parameter.
     */
    public double getOverprintMode()
    {
        return overprintMode;
    }

    /**
     * set the value of the overprint mode property.
     *
     * @param value The value of the overprint mode parameter.
     */
    public void setOverprintMode(double value)
    {
        overprintMode = value;
    }

    /**
     * get the value of the flatness property.
     *
     * @return The value of the flatness parameter.
     */
    public double getFlatness()
    {
        return flatness;
    }

    /**
     * set the value of the flatness property.
     *
     * @param value The value of the flatness parameter.
     */
    public void setFlatness(double value)
    {
        flatness = value;
    }

    /**
     * get the value of the smoothness property.
     *
     * @return The value of the smoothness parameter.
     */
    public double getSmoothness()
    {
        return smoothness;
    }

    /**
     * set the value of the smoothness property.
     *
     * @param value The value of the smoothness parameter.
     */
    public void setSmoothness(double value)
    {
        smoothness = value;
    }

    /**
     * This will get the graphics text state.
     *
     * @return The graphics text state.
     */
    public PDTextState getTextState()
    {
        return textState;
    }

    /**
     * This will set the graphics text state.
     *
     * @param value The graphics text state.
     */
    public void setTextState(PDTextState value)
    {
        textState = value;
    }

    /**
     * This will get the current line dash pattern.
     *
     * @return The line dash pattern.
     */
    public PDLineDashPattern getLineDashPattern()
    {
        return lineDashPattern;
    }

    /**
     * This will set the current line dash pattern.
     *
     * @param value The new line dash pattern.
     */
    public void setLineDashPattern(PDLineDashPattern value)
    {
        lineDashPattern = value;
    }

    /**
     * This will get the rendering intent.
     *
     * @see PDExtendedGraphicsState
     *
     * @return The rendering intent
     */
    public RenderingIntent getRenderingIntent()
    {
        return renderingIntent;
    }

    /**
     * This will set the rendering intent.
     *
     * @param value The new rendering intent.
     */
    public void setRenderingIntent(RenderingIntent value)
    {
        renderingIntent = value;
    }

    @Override
    public PDGraphicsState clone()
    {
        try
        {
            PDGraphicsState clone = (PDGraphicsState)super.clone();
            clone.textState = textState.clone();
            clone.currentTransformationMatrix = currentTransformationMatrix.clone();
            clone.strokingColor = strokingColor; // immutable
            clone.nonStrokingColor = nonStrokingColor; // immutable
            clone.lineDashPattern = lineDashPattern; // immutable
            clone.clippingPath = clippingPath; // not cloned, see intersectClippingPath
            clone.isClippingPathDirty = false;
            return clone;
        }
        catch (CloneNotSupportedException e)
        {
            // should not happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the stroking color.
     *
     * @return stroking color
     */
    public PDColor getStrokingColor()
    {
        return strokingColor;
    }

    /**
     * Sets the stroking color.
     *
     * @param color The new stroking color
     */
    public void setStrokingColor(PDColor color)
    {
        strokingColor = color;
    }

    /**
     * Returns the non-stroking color.
     *
     * @return The non-stroking color
     */
    public PDColor getNonStrokingColor()
    {
        return nonStrokingColor;
    }

    /**
     * Sets the non-stroking color.
     *
     * @param color The new non-stroking color
     */
    public void setNonStrokingColor(PDColor color)
    {
        nonStrokingColor = color;
    }

    /**
     * Returns the stroking color space.
     *
     * @return The stroking color space.
     */
    public PDColorSpace getStrokingColorSpace()
    {
        return strokingColorSpace;
    }

    /**
     * Sets the the stroking color space.
     *
     * @param colorSpace The new stroking color space.
     */
    public void setStrokingColorSpace(PDColorSpace colorSpace)
    {
        strokingColorSpace = colorSpace;
    }

    /**
     * Returns the non-stroking color space.
     *
     * @return The non-stroking color space.
     */
    public PDColorSpace getNonStrokingColorSpace()
    {
        return nonStrokingColorSpace;
    }

    /**
     * Sets the the non-stroking color space.
     *
     * @param colorSpace The new non-stroking color space.
     */
    public void setNonStrokingColorSpace(PDColorSpace colorSpace)
    {
        nonStrokingColorSpace = colorSpace;
    }

    /**
     * Modify the current clipping path by intersecting it with the given path.
     * @param path path to intersect with the clipping path
     */
    public void intersectClippingPath(GeneralPath path)
    {
        intersectClippingPath(new Area(path));
    }

    /**
     * Modify the current clipping path by intersecting it with the given path.
     * @param area area to intersect with the clipping path
     */
    public void intersectClippingPath(Area area)
    {
        // lazy cloning of clipping path for performance
        if (!isClippingPathDirty)
        {
            // deep copy (can't use clone() as it performs only a shallow copy)
            Area cloned = new Area();
            cloned.add(clippingPath);
            clippingPath = cloned;

            isClippingPathDirty = true;
        }

        // intersection as usual
        clippingPath.intersect(area);
    }

    /**
     * This will get the current clipping path. Do not modify this Area object!
     *
     * @return The current clipping path.
     */
    public Area getCurrentClippingPath()
    {
        return clippingPath;
    }

    public Composite getStrokingJavaComposite()
    {
        return BlendComposite.getInstance(blendMode, (float) alphaConstant);
    }

    public Composite getNonStrokingJavaComposite()
    {
        return BlendComposite.getInstance(blendMode, (float) nonStrokingAlphaConstant);
    }

    /**
     * This will get the transfer function.
     *
     * @return The transfer function. According to the PDF specification, this is either a single
     * function (which applies to all process colorants) or an array of four functions (which apply
     * to the process colorants individually). The name Identity may be used to represent the
     * identity function, and the name Default denotes the transfer function that was in effect at
     * the start of the page.
     */
    public COSBase getTransfer()
    {
        return transfer;
    }

    /**
     * This will set the transfer function.
     *
     * @param transfer The transfer function. According to the PDF specification, this is either a
     * single function (which applies to all process colorants) or an array of four functions (which
     * apply to the process colorants individually). The name Identity may be used to represent the
     * identity function, and the name Default denotes the transfer function that was in effect at
     * the start of the page.
     */
    public void setTransfer(COSBase transfer)
    {
        this.transfer = transfer;
    }
}
