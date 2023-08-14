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

import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * This class will hold the current state of the text parameters when executing a
 * content stream.
 *
 * @author Ben Litchfield
 */
public class PDTextState implements Cloneable
{
    private float characterSpacing = 0;
    private float wordSpacing = 0;
    private float horizontalScaling = 100;
    private float leading = 0;
    private PDFont font;
    private float fontSize;
    private RenderingMode renderingMode = RenderingMode.FILL;
    private float rise = 0;
    private boolean knockout = true;

    /**
     * Get the value of the characterSpacing.
     *
     * @return The current characterSpacing.
     */
    public float getCharacterSpacing()
    {
        return characterSpacing;
    }

    /**
     * Set the value of the characterSpacing.
     *
     * @param value The characterSpacing.
     */
    public void setCharacterSpacing(float value)
    {
        characterSpacing = value;
    }

    /**
     * Get the value of the wordSpacing.
     *
     * @return The wordSpacing.
     */
    public float getWordSpacing()
    {
        return wordSpacing;
    }

    /**
     * Set the value of the wordSpacing.
     *
     * @param value The wordSpacing.
     */
    public void setWordSpacing(float value)
    {
        wordSpacing = value;
    }

    /**
     * Get the value of the horizontalScaling.  The default is 100.  This value
     * is the percentage value 0-100 and not 0-1.  So for mathematical operations
     * you will probably need to divide by 100 first.
     *
     * @return The horizontalScaling.
     */
    public float getHorizontalScaling()
    {
        return horizontalScaling;
    }

    /**
     * Set the value of the horizontalScaling.
     *
     * @param value The horizontalScaling.
     */
    public void setHorizontalScaling(float value)
    {
        horizontalScaling = value;
    }

    /**
     * Get the value of the leading.
     *
     * @return The leading.
     */
    public float getLeading()
    {
        return leading;
    }

    /**
     * Set the value of the leading.
     *
     * @param value The leading.
     */
    public void setLeading(float value)
    {
        leading = value;
    }

    /**
     * Get the value of the font.
     *
     * @return The font.
     */
    public PDFont getFont()
    {
        return font;
    }

    /**
     * Set the value of the font.
     *
     * @param value The font.
     */
    public void setFont(PDFont value)
    {
        font = value;
    }

    /**
     * Get the value of the fontSize.
     *
     * @return The fontSize.
     */
    public float getFontSize()
    {
        return fontSize;
    }

    /**
     * Set the value of the fontSize.
     *
     * @param value The fontSize.
     */
    public void setFontSize(float value)
    {
        fontSize = value;
    }

    /**
     * Get the value of the renderingMode.
     *
     * @return The renderingMode.
     */
    public RenderingMode getRenderingMode()
    {
        return renderingMode;
    }

    /**
     * Set the value of the renderingMode.
     *
     * @param renderingMode The renderingMode.
     */
    public void setRenderingMode(RenderingMode renderingMode)
    {
        this.renderingMode = renderingMode;
    }

    /**
     * Get the value of the rise.
     *
     * @return The rise.
     */
    public float getRise()
    {
        return rise;
    }

    /**
     * Set the value of the rise.
     *
     * @param value The rise.
     */
    public void setRise(float value)
    {
        rise = value;
    }

    /**
     * Get the value of the knockout.
     *
     * @return The knockout.
     */
    public boolean getKnockoutFlag()
    {
        return knockout;
    }

    /**
     * Set the value of the knockout.
     *
     * @param value The knockout.
     */
    public void setKnockoutFlag(boolean value)
    {
        knockout = value;
    }

    @Override
    public PDTextState clone()
    {
        try
        {
            return (PDTextState)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // should not happen
            throw new RuntimeException(e);
        }
    }
}
