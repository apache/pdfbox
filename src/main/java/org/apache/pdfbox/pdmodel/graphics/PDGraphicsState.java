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
package org.apache.pdfbox.pdmodel.graphics;

import org.apache.pdfbox.util.Matrix;

import org.apache.pdfbox.pdmodel.text.PDTextState;

import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceInstance;

/**
 * This class will hold the current state of the graphics parameters when executing a
 * content stream.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDGraphicsState implements Cloneable
{
    private Matrix currentTransformationMatrix = new Matrix();

    //Here are some attributes of the Graphics state, but have not been created yet.
    //
    //clippingPath
    private PDColorSpaceInstance strokingColorSpace = new PDColorSpaceInstance();
    private PDColorSpaceInstance nonStrokingColorSpace = new PDColorSpaceInstance();
    private PDTextState textState = new PDTextState();
    private double lineWidth = 0;
    private int lineCap = 0;
    private int lineJoin = 0;
    private double miterLimit = 0;
    private PDLineDashPattern lineDashPattern;
    private String renderingIntent;
    private boolean strokeAdjustment = false;
    //blend mode
    //soft mask
    private double alphaConstants = 0;
    private boolean alphaSource = false;

    //DEVICE DEPENDENT parameters
    private boolean overprint = false;
    private double overprintMode = 0;
    //black generation
    //undercolor removal
    //transfer
    //halftone
    private double flatness = 1.0;
    private double smoothness = 0;

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
    public double getLineWidth()
    {
        return lineWidth;
    }

    /**
     * set the value of the line width.
     *
     * @param value The current line width.
     */
    public void setLineWidth(double value)
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
    public double getMiterLimit()
    {
        return miterLimit;
    }

    /**
     * set the value of the miter limit.
     *
     * @param value The current miter limit.
     */
    public void setMiterLimit(double value)
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
     * Get the value of the alpha constants property.
     *
     * @return The value of the alpha constants parameter.
     */
    public double getAlphaConstants()
    {
        return alphaConstants;
    }

    /**
     * set the value of the alpha constants property.
     *
     * @param value The value of the alpha constants parameter.
     */
    public void setAlphaConstants(double value)
    {
        alphaConstants = value;
    }

    /**
     * get the value of the alpha source property.
     *
     * @return The value of the alpha source parameter.
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
    public String getRenderingIntent()
    {
        return renderingIntent;
    }

    /**
     * This will set the rendering intent.
     *
     * @param value The new rendering intent.
     */
    public void setRenderingIntent(String value)
    {
        renderingIntent = value;
    }

    /**
     * {@inheritDoc}
     */
    public Object clone()
    {
        PDGraphicsState clone = null;
        try
        {
            clone = (PDGraphicsState)super.clone();
            clone.setTextState( (PDTextState)textState.clone() );
            clone.setCurrentTransformationMatrix( currentTransformationMatrix.copy() );
            clone.setStrokingColorSpace( (PDColorSpaceInstance)strokingColorSpace.clone() );
            clone.setNonStrokingColorSpace( (PDColorSpaceInstance)nonStrokingColorSpace.clone() );
            if( lineDashPattern != null )
            {
                clone.setLineDashPattern( (PDLineDashPattern)lineDashPattern.clone() );
            }
        }
        catch( CloneNotSupportedException e )
        {
            e.printStackTrace();
        }
        return clone;
    }

    /**
     * This will get the current stroking colorspace.
     *
     * @return The current stroking colorspace.
     */
    public PDColorSpaceInstance getStrokingColorSpace()
    {
        return strokingColorSpace;
    }

    /**
     * This will set the current stroking colorspace.
     *
     * @param value The new stroking colorspace instance.
     */
    public void setStrokingColorSpace(PDColorSpaceInstance value)
    {
        strokingColorSpace = value;
    }

    /**
     * This will get the nonstroking color space instance.
     *
     * @return The colorspace instance.
     */
    public PDColorSpaceInstance getNonStrokingColorSpace()
    {
        return nonStrokingColorSpace;
    }

    /**
     * This will set the non-stroking colorspace instance.
     *
     * @param value The non-stroking colorspace instance.
     */
    public void setNonStrokingColorSpace(PDColorSpaceInstance value)
    {
        nonStrokingColorSpace = value;
    }
}
