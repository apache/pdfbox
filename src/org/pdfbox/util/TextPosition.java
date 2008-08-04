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
package org.pdfbox.util;

import org.pdfbox.pdmodel.font.PDFont;

/**
 * This represents a character and a position on the screen of those characters.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.12 $
 */
public class TextPosition
{
    private float x;
    private float y;
    private float xScale;
    private float yScale;
    private float totalWidth;
    private float[] widths;
    private float height;
    private float widthOfSpace;
    private String c;
    private PDFont font;
    private float fontSize;
    private float wordSpacing;
    
    protected TextPosition()
    {
        
    }

    /**
     * Constructor.
     *
     * @param xPos The x coordinate of the character.
     * @param yPos The y coordinate of the character.
     * @param xScl The x scaling of the character.
     * @param yScl The y scaling of the character.
     * @param totalWidthValue The width of all the characters.
     * @param individualWidths The width of each individual character.
     * @param heightValue The height of the character.
     * @param spaceWidth The width of the space character.
     * @param string The character to be displayed.
     * @param currentFont The current for for this text position.
     * @param fontSizeValue The new font size.
     * @param ws The word spacing parameter
     */
    public TextPosition(
        float xPos,
        float yPos,
        float xScl,
        float yScl,
        float totalWidthValue,
        float[] individualWidths,
        float heightValue,
        float spaceWidth,
        String string,
        PDFont currentFont,
        float fontSizeValue,
        float ws
        )
    {
        this.x = xPos;
        this.y = yPos;
        this.xScale = xScl;
        this.yScale = yScl;
        this.totalWidth = totalWidthValue;
        this.widths = individualWidths;
        this.height = heightValue;
        this.widthOfSpace = spaceWidth;
        this.c = string;
        this.font = currentFont;
        this.fontSize = fontSizeValue;
        this.wordSpacing = ws;
    }

    /**
     * This will the character that will be displayed on the screen.
     *
     * @return The character on the screen.
     */
    public String getCharacter()
    {
        return c;
    }

    /**
     * This will get the x position of the character.
     *
     * @return The x coordinate of the character.
     */
    public float getX()
    {
        return x;
    }

    /**
     * This will get the y position of the character.
     *
     * @return The y coordinate of the character.
     */
    public float getY()
    {
        return y;
    }

    /**
     * This will get the width of this character.
     *
     * @return The width of this character.
     */
    public float getWidth()
    {
        return totalWidth;
    }
    
    /**
     * This will get the maximum height of all characters in this string.
     *
     * @return The maximum height of all characters in this string.
     */
    public float getHeight()
    {
        return height;
    }

    /**
     * This will get the font size that this object is
     * suppose to be drawn at.
     *
     * @return The font size.
     */
    public float getFontSize()
    {
        return fontSize;
    }

    /**
     * This will get the font for the text being drawn.
     *
     * @return The font size.
     */
    public PDFont getFont()
    {
        return font;
    }

    /**
     * This will get the current word spacing.
     *
     * @return The current word spacing.
     */
    public float getWordSpacing()
    {
        return wordSpacing;
    }

    /**
     * This will get the width of a space character.  This is useful for some
     * algorithms such as the text stripper, that need to know the width of a
     * space character.
     *
     * @return The width of a space character.
     */
    public float getWidthOfSpace()
    {
        return widthOfSpace;
    }
    /**
     * @return Returns the xScale.
     */
    public float getXScale()
    {
        return xScale;
    }
    /**
     * @param scale The xScale to set.
     */
    public void setXScale(float scale)
    {
        xScale = scale;
    }
    /**
     * @return Returns the yScale.
     */
    public float getYScale()
    {
        return yScale;
    }
    /**
     * @param scale The yScale to set.
     */
    public void setYScale(float scale)
    {
        yScale = scale;
    }
 
    /**
     * Get the widths of each individual character.
     * 
     * @return An array that is the same length as the length of the string.
     */
    public float[] getIndividualWidths()
    {
        return widths;
    }
    
    /**
     * Set the individual widths of every character.
     * 
     * @param individualWidths The individual widths of characters.
     */
    public void setIndividualWidths( float[] individualWidths )
    {
        widths = individualWidths;
    }
    
    /**
     * Show the string data for this text position.
     * 
     * @return A human readable form of this object.
     */
    public String toString()
    {
        return getCharacter();
    }
}