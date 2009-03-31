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
package org.apache.pdfbox.util;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * This represents a string and a position on the screen of those characters.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.12 $
 */
public class TextPosition
{
    /* TextMatrix for the start of the text object.  Coordinates
     * are in display units and have not been adjusted. */
    private Matrix textPos;

    // ending X and Y coordinates in display units
    private float endX;
    private float endY;

    private float maxTextHeight; // maximum height of text, in display units
    private int rot; // 0, 90, 180, 270 degrees of page rotation
    private float pageHeight;
    private float pageWidth;
    private float[] widths;
    private float widthOfSpace; // width of a space, in display units
    private String str; 
    private PDFont font;
    private float fontSize;
    private float wordSpacing;  // word spacing value, in display units

    protected TextPosition()
    {

    }

    /**
     * Constructor.
     *
     * @param page Page that the text is located in
     * @param textPositionSt TextMatrix for start of text (in display units)
     * @param textPositionEnd TextMatrix for end of text (in display units)
     * @param maxFontH Maximum height of text (in display units)
     * @param individualWidths The width of each individual character. (in ? units)
     * @param spaceWidth The width of the space character. (in display units)
     * @param string The character to be displayed.
     * @param currentFont The current for for this text position.
     * @param fontSizeValue The new font size.
     * @param ws The word spacing parameter (in display units)
     */
    public TextPosition(
            PDPage page,
            Matrix textPositionSt,
            Matrix textPositionEnd,
            float maxFontH,
            float[] individualWidths,
            float spaceWidth,
            String string,
            PDFont currentFont,
            float fontSizeValue,
            float ws
    )
    {
        this.textPos = textPositionSt;

        this.endX = textPositionEnd.getXPosition();
        this.endY = textPositionEnd.getYPosition();

        this.rot = page.findRotation();
        // make sure it is 0 to 270 and no negative numbers
        if(this.rot < 0)
            rot += 360;

        this.maxTextHeight = maxFontH;
        this.pageHeight = page.findMediaBox().getHeight();
        this.pageWidth = page.findMediaBox().getWidth();

        this.widths = individualWidths;
        this.widthOfSpace = spaceWidth;
        this.str = string;
        this.font = currentFont;
        this.fontSize = fontSizeValue;
        this.wordSpacing = ws;
    }

    /**
     * Return the string of characters stored in this object.
     *
     * @return The string on the screen.
     */
    public String getCharacter()
    {
        return str;
    }

    /**
     * Return the Matrix textPos stored in this object
     *  
     * @return The Matrix containing all infos of the starting textposition
     */
    public Matrix getTextPos() {
        return textPos;
    }

    /**
     * Return the direction/orientation of the string in this object
     * based on its text matrix.
     * @return The direction of the text (0, 90, 180, or 270)
     */
    public float getDir() {
        float a = textPos.getValue(0,0);
        float b = textPos.getValue(0,1);
        float c = textPos.getValue(1,0);
        float d = textPos.getValue(1,1);

        // 12 0   left to right
        // 0 12 
        if ((a > 0) && (Math.abs(b) < d) && (Math.abs(c) < a) && (d > 0))
            return 0;
        // -12 0   right to left (upside down)
        // 0 -12
        else if ((a < 0) && (Math.abs(b) < Math.abs(d)) && (Math.abs(c) < Math.abs(a)) && (d < 0))
            return 180;
        // 0  12    up
        // -12 0 
        else if ((Math.abs(a) < Math.abs(c)) && (b > 0) && (c < 0) && (Math.abs(d) < b))
            return 90;
        // 0  -12   down
        // 12 0 
        else if ((Math.abs(a) < c) && (b < 0) && (c > 0) && (Math.abs(d) < Math.abs(b)))
            return 270;

        return 0;
    }

    /**
     * Return the X starting coordinate of the text, adjusted by 
     * the given rotation amount.  The rotation adjusts where the 0,0
     * location is relative to the text. 
     *  
     * @param a_rot Rotation to apply (0, 90, 180, or 270).  0 will perform no adjustments. 
     * @return X coordinate
     */
    private float getX_rot(float a_rot)
    {
        if (a_rot == 0)
            return textPos.getValue(2,0);
        else if (a_rot == 90)
            return textPos.getValue(2,1);
        else if (a_rot == 180)
            return pageWidth - textPos.getValue(2,0);
        else if (a_rot == 270)
            return pageHeight - textPos.getValue(2,1);
        else 
            return 0;
    }

    /**
     * This will get the page rotation adjusted x position of the character.
     * This is adjusted based on page rotation so that the upper 
     * left is 0,0. 
     *
     * @return The x coordinate of the character.
     */
    public float getX()
    {
        return getX_rot(rot);
    }

    /**
     * This will get the text direction adjusted x position of the character.
     * This is adjusted based on text direction so that the first character
     * in that direction is in the upper left at 0,0.
     *
     * @return The x coordinate of the text.
     */
    public float getXDirAdj() {
        return getX_rot(getDir());  
    }

    /** 
     * This will get the y position of the character with 0,0 in lower left. 
     * This will be adjusted by the given rotation. 
     * @param a_rot Rotation to apply to text to adjust the 0,0 location (0,90,180,270)
     * 
     * @return The y coordinate of the text
     */
    private float getY_ll_rot(float a_rot)
    {
        if (a_rot == 0)
            return textPos.getValue(2,1);
        else if (a_rot == 90)
            return pageWidth - textPos.getValue(2,0);
        else if (a_rot == 180)
            return pageHeight - textPos.getValue(2,1);
        else if (a_rot == 270)
            return textPos.getValue(2,0);
        else 
            return 0;
    }

    /**
     * This will get the y position of the text, adjusted so that 0,0 is upper left and 
     * it is adjusted based on the page rotation. 
     *
     * @return The adjusted y coordinate of the character.
     */
    public float getY()
    {
        if ((rot == 0) || (rot == 180))
            return pageHeight - getY_ll_rot(rot);
        else
            return pageWidth - getY_ll_rot(rot);
    }

    /**
     * This will get the y position of the text, adjusted so that 0,0 is upper left and 
     * it is adjusted based on the text direction. 
     *
     * @return The adjusted y coordinate of the character.
     */
    public float getYDirAdj()
    {
        float dir = getDir();
        // some PDFBox code assumes that the 0,0 point is in upper left, not lower left
        if ((dir == 0) || (dir == 180))
            return pageHeight - getY_ll_rot(dir);
        else
            return pageWidth - getY_ll_rot(dir);
    }



    /**
     * Get the length or width of the text, based on a given rotation. 
     * 
     * @param a_rot Rotation that was used to determine coordinates (0,90,180,270)
     * @return Width of text in display units
     */
    private float getWidth_rot(float a_rot)
    {
        if ((a_rot == 90) || (a_rot == 270)) {
            return Math.abs(endY - textPos.getYPosition());
        }
        else {
            return Math.abs(endX - textPos.getXPosition());
        }
    }

    /**
     * This will get the width of the string when page rotation adjusted coordinates are used.
     *
     * @return The width of the text in display units.
     */
    public float getWidth() {
        return getWidth_rot(rot);
    }

    /**
     * This will get the width of the string when text direction adjusted coordinates are used.
     *
     * @return The width of the text in display units.
     */
    public float getWidthDirAdj() {
        return getWidth_rot(getDir());
    }

    /**
     * This will get the maximum height of all characters in this string.
     *
     * @return The maximum height of all characters in this string.
     */
    public float getHeight() {
        return maxTextHeight;
    }

    /**
     * This will get the maximum height of all characters in this string.
     *
     * @return The maximum height of all characters in this string.
     */
    public float getHeightDir() {
        // this is not really a rotation-dependent calculation, but this is defined for symmetry.
        return maxTextHeight;
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
        return textPos.getXScale();
    }

    /**
     * @return Returns the yScale.
     */
    public float getYScale()
    {
        return textPos.getYScale();
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
     * Show the string data for this text position.
     *
     * @return A human readable form of this object.
     */
    public String toString()
    {
        return getCharacter();
    }


    /**
     * Determine if this TextPosition logically contains
     * another (i.e. they overlap and should be rendered on top
     * of each other).
     * @param tp2 The other TestPosition to compare against
     *
     * @return True if tp2 is contained in the bounding box of this text.
     */
    public boolean contains( TextPosition tp2)
    {
        double thisXstart = getXDirAdj();
        double thisXend = getXDirAdj() + getWidthDirAdj();

        double tp2Xstart = tp2.getXDirAdj();
        double tp2Xend = tp2.getXDirAdj() + tp2.getWidthDirAdj();

        /*
         * No X overlap at all so return as soon as possible. 
         */
        if(tp2Xend <= thisXstart || tp2Xstart >= thisXend){
            return false;
        }   
        /*
         * No Y overlap at all so return as soon as possible. 
         * Note: 0.0 is in the upper left and y-coordinate is 
         * top of TextPosition
         */
        if((tp2.getYDirAdj() + tp2.getHeightDir() < getYDirAdj()) ||
                (tp2.getYDirAdj() > getYDirAdj() + getHeightDir())){
            return false; 
        }          
        /* We're going to calculate the percentage of overlap. If its less
         * than a 15% x-coordinate overlap then we'll return false because its negligible.
         * .15 was determined by trial and error in the regression test files.
         */
        else if((tp2Xstart > thisXstart) && (tp2Xend > thisXend)){
            double overlap = thisXend - tp2Xstart;
            double overlapPercent = overlap/getWidthDirAdj();
            return (overlapPercent > .15);
        }
        else if((tp2Xstart < thisXstart) && (tp2Xend < thisXend)){
            double overlap = tp2Xend - thisXstart;
            double overlapPercent = overlap/getWidthDirAdj();
            return (overlapPercent > .15);
        }
        return true; 
    }

    /**
     * Merge a single character TextPosition into the current object.
     * This is to be used only for cases where we have a diacritic that
     * overlaps an existing TextPosition.  In a graphical display, we could
     * overlay them, but for text extraction we need to merge them. Use the
     * contains() method to test if two objects overlap. 
     * 
     * @param diacritic TextPosition to merge into the current TextPosition.
     */
    public void mergeDiacritic (TextPosition diacritic)
    {
        if (diacritic.getCharacter().length() > 1)
            return;

        float diacXStart = diacritic.getXDirAdj();
        float diacXEnd = diacXStart + diacritic.widths[0];

        float currCharXStart = getXDirAdj();

        int strLen = str.length();
        boolean wasAdded = false;

        for (int i = 0; i < strLen && wasAdded == false; i++) {

            float currCharXEnd = currCharXStart + widths[i];

            /*
             * This is the case where there is an overlap of the diacritic character with
             * the current character and the previous character. If no previous character,
             * just append the diacritic after the current one. 
             */
            if(diacXStart < currCharXStart && diacXEnd <= currCharXEnd){               
                if(i == 0){
                    insertDiacritic(i, diacritic);
                }
                else{    
                    float distanceOverlapping1 = diacXEnd - currCharXStart;
                    float percentage1 = distanceOverlapping1/widths[i];

                    float distanceOverlapping2 = currCharXStart - diacXStart;
                    float percentage2 = distanceOverlapping2/widths[i-1];

                    if(percentage1 >= percentage2){
                        insertDiacritic(i, diacritic);
                    }
                    else{
                        insertDiacritic(i-1, diacritic);
                    }
                }
                wasAdded = true;
            }
            //diacritic completely covers this character and therefore we assume that
            //this is the character the diacritic belongs to
            else if(diacXStart < currCharXStart && diacXEnd > currCharXEnd){
                insertDiacritic(i, diacritic);
                wasAdded = true;
            }
            //Otherwise, The diacritic modifies this character because its completely
            //contained by the character width
            else if(diacXStart >= currCharXStart && diacXEnd <= currCharXEnd) {
                insertDiacritic(i, diacritic);
                wasAdded = true;   
            }
            /*
             * Last character in the TextPosition so we add diacritic to the end
             */
            else if(diacXStart >= currCharXStart && diacXEnd > currCharXEnd && i == (strLen - 1)){
                insertDiacritic(i, diacritic);
                wasAdded = true;
            }        
            /*
             * Couldn't find anything useful so we go to the next character in the 
             * TextPosition 
             */
            currCharXStart += widths[i];
        }
    }
    /**
     * Inserts the diacritic TextPosition to the str of this TextPosition
     * and updates the widths array to include the extra character width.
     * @param i current character
     * @param diacritic The diacritic TextPosition
     */
    private void insertDiacritic(int i, TextPosition diacritic){
        /* we add the diacritic to the right or left of the character
         * depending on the direction of the character.  Note that this
         * is only required because the text is currently stored in 
         * presentation order and not in logical order. 
         */
        int dir = Character.getDirectionality(str.charAt(i));
        StringBuffer buf = new StringBuffer();

        buf.append(str.substring(0,i));

        float[] widths2 = new float[widths.length+1];
        System.arraycopy(widths, 0, widths2, 0, i);

        if ((dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT)
                || (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC)
                || (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING)
                || (dir == Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE)) {
            buf.append(diacritic.getCharacter());
            widths2[i] = 0;
            buf.append(str.charAt(i));
            widths2[i+1] = widths[i];
        } 
        else {
            buf.append(str.charAt(i));
            widths2[i] = widths[i];
            buf.append(diacritic.getCharacter());
            widths2[i+1] = 0;
        }

        // Get the rest of the string
        buf.append(str.substring(i+1, str.length()));
        System.arraycopy(widths, i+1, widths2, i+2, widths.length-i-1);

        str = buf.toString();
        widths = widths2;
    }

    /**
     * 
     * @return True if the current character is a diacritic char. 
     */
    public boolean isDiacritic() {
        String cText = this.getCharacter();
        return (cText.length() == 1 &&  (Character.getType(cText.charAt(0)) == Character.NON_SPACING_MARK
                || Character.getType(cText.charAt(0)) == Character.MODIFIER_SYMBOL
                || Character.getType(cText.charAt(0)) == Character.MODIFIER_LETTER));
    }
}
