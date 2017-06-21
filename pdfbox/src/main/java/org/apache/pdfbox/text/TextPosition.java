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
package org.apache.pdfbox.text;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Matrix;

/**
 * This represents a string and a position on the screen of those characters.
 *
 * @author Ben Litchfield
 */
public final class TextPosition
{
    private static final Log LOG = LogFactory.getLog(TextPosition.class);

    private static final Map<Integer, String> DIACRITICS = createDiacritics();

    // text matrix for the start of the text object, coordinates are in display units
    // and have not been adjusted
    private final Matrix textMatrix;

    // ending X and Y coordinates in display units
    private final float endX;
    private final float endY;

    private final float maxHeight; // maximum height of text, in display units
    private final int rotation; // 0, 90, 180, 270 degrees of page rotation
    private final float x;
    private final float y;
    private final float pageHeight;
    private final float pageWidth;

    private final float widthOfSpace; // width of a space, in display units

    private final int[] charCodes; // internal PDF character codes
    private final PDFont font;
    private final float fontSize;
    private final int fontSizePt;

    // mutable
    private float[] widths;
    private String unicode;
    private float direction = -1;

    /**
     * Constructor.
     *
     * @param pageRotation rotation of the page that the text is located in
     * @param pageWidth width of the page that the text is located in
     * @param pageHeight height of the page that the text is located in
     * @param textMatrix text rendering matrix for start of text (in display units)
     * @param endX x coordinate of the end position
     * @param endY y coordinate of the end position
     * @param maxHeight Maximum height of text (in display units)
     * @param individualWidth The width of the given character/string. (in text units)
     * @param spaceWidth The width of the space character. (in display units)
     * @param unicode The string of Unicode characters to be displayed.
     * @param charCodes An array of the internal PDF character codes for the glyphs in this text.
     * @param font The current font for this text position.
     * @param fontSize The new font size.
     * @param fontSizeInPt The font size in pt units (see {@link #getFontSizeInPt()} for details).
     */
    public TextPosition(int pageRotation, float pageWidth, float pageHeight, Matrix textMatrix,
                        float endX, float endY, float maxHeight, float individualWidth,
                        float spaceWidth, String unicode, int[] charCodes, PDFont font,
                        float fontSize, int fontSizeInPt)
    {
        this.textMatrix = textMatrix;

        this.endX = endX;
        this.endY = endY;

        int rotationAngle = pageRotation;
        this.rotation = rotationAngle;

        this.maxHeight = maxHeight;
        this.pageHeight = pageHeight;
        this.pageWidth = pageWidth;

        this.widths = new float[] { individualWidth };
        this.widthOfSpace = spaceWidth;
        this.unicode = unicode;
        this.charCodes = charCodes;
        this.font = font;
        this.fontSize = fontSize;
        this.fontSizePt = fontSizeInPt;

        x = getXRot(rotationAngle);
        if (rotationAngle == 0 || rotationAngle == 180)
        {
            y = this.pageHeight - getYLowerLeftRot(rotationAngle);
        }
        else
        {
            y = this.pageWidth - getYLowerLeftRot(rotationAngle);
        }
    }

    // Adds non-decomposing diacritics to the hash with their related combining character.
    // These are values that the unicode spec claims are equivalent but are not mapped in the form
    // NFKC normalization method. Determined by going through the Combining Diacritical Marks
    // section of the Unicode spec and identifying which characters are not  mapped to by the
    // normalization.
    private static Map<Integer, String> createDiacritics()
    {
        Map<Integer, String> map = new HashMap<>(31);
        map.put(0x0060, "\u0300");
        map.put(0x02CB, "\u0300");
        map.put(0x0027, "\u0301");
        map.put(0x02B9, "\u0301");
        map.put(0x02CA, "\u0301");
        map.put(0x005e, "\u0302");
        map.put(0x02C6, "\u0302");
        map.put(0x007E, "\u0303");
        map.put(0x02C9, "\u0304");
        map.put(0x00B0, "\u030A");
        map.put(0x02BA, "\u030B");
        map.put(0x02C7, "\u030C");
        map.put(0x02C8, "\u030D");
        map.put(0x0022, "\u030E");
        map.put(0x02BB, "\u0312");
        map.put(0x02BC, "\u0313");
        map.put(0x0486, "\u0313");
        map.put(0x055A, "\u0313");
        map.put(0x02BD, "\u0314");
        map.put(0x0485, "\u0314");
        map.put(0x0559, "\u0314");
        map.put(0x02D4, "\u031D");
        map.put(0x02D5, "\u031E");
        map.put(0x02D6, "\u031F");
        map.put(0x02D7, "\u0320");
        map.put(0x02B2, "\u0321");
        map.put(0x02CC, "\u0329");
        map.put(0x02B7, "\u032B");
        map.put(0x02CD, "\u0331");
        map.put(0x005F, "\u0332");
        map.put(0x204E, "\u0359");
        return map;
    }

    /**
     * Return the string of characters stored in this object. The length can be different than the
     * CharacterCodes length e.g. if ligatures are used ("fi", "fl", "ffl") where one glyph
     * represents several unicode characters.
     *
     * @return The string on the screen.
     */
    public String getUnicode()
    {
        return unicode;
    }

    /**
     * Return the internal PDF character codes of the glyphs in this text.
     *
     * @return an array of internal PDF character codes
     */
    public int[] getCharacterCodes()
    {
        return charCodes;
    }

    /**
     * The matrix containing the starting text position and scaling. Despite the name, it is not the
     * text matrix set by the "Tm" operator, it is really the effective text rendering matrix (which
     * is dependent on the current transformation matrix (set by the "cm" operator), the text matrix
     * (set by the "Tm" operator), the font size (set by the "Tf" operator) and the page cropbox).
     *
     * @return The Matrix containing the starting text position
     */
    public Matrix getTextMatrix()
    {
        return textMatrix;
    }

    /**
     * Return the direction/orientation of the string in this object based on its text matrix.
     * @return The direction of the text (0, 90, 180, or 270)
     */
    public float getDir()
    {
        if (direction < 0)
        {
            float a = textMatrix.getScaleY();
            float b = textMatrix.getShearY();
            float c = textMatrix.getShearX();
            float d = textMatrix.getScaleX();
    
            // 12 0   left to right
            // 0 12
            if (a > 0 && Math.abs(b) < d && Math.abs(c) < a && d > 0)
            {
                direction = 0;
            }
            // -12 0   right to left (upside down)
            // 0 -12
            else if (a < 0 && Math.abs(b) < Math.abs(d) && Math.abs(c) < Math.abs(a) && d < 0)
            {
                direction = 180;
            }
            // 0  12    up
            // -12 0
            else if (Math.abs(a) < Math.abs(c) && b > 0 && c < 0 && Math.abs(d) < b)
            {
                direction = 90;
            }
            // 0  -12   down
            // 12 0
            else if (Math.abs(a) < c && b < 0 && c > 0 && Math.abs(d) < Math.abs(b))
            {
                direction = 270;
            }
            else
            {
                direction = 0;
            }
        }
        return direction;
    }

    /**
     * Return the X starting coordinate of the text, adjusted by the given rotation amount.
     * The rotation adjusts where the 0,0 location is relative to the text.
     *
     * @param rotation Rotation to apply (0, 90, 180, or 270).  0 will perform no adjustments.
     * @return X coordinate
     */
    private float getXRot(float rotation)
    {
        if (rotation == 0)
        {
            return textMatrix.getTranslateX();
        }
        else if (rotation == 90)
        {
            return textMatrix.getTranslateY();
        }
        else if (rotation == 180)
        {
            return pageWidth - textMatrix.getTranslateX();
        }
        else if (rotation == 270)
        {
            return pageHeight - textMatrix.getTranslateY();
        }
        return 0;
    }

    /**
     * This will get the page rotation adjusted x position of the character.
     * This is adjusted based on page rotation so that the upper left is 0,0.
     *
     * @return The x coordinate of the character.
     */
    public float getX()
    {
        return x;
    }

    /**
     * This will get the text direction adjusted x position of the character.
     * This is adjusted based on text direction so that the first character
     * in that direction is in the upper left at 0,0.
     *
     * @return The x coordinate of the text.
     */
    public float getXDirAdj()
    {
        return getXRot(getDir());
    }

    /**
     * This will get the y position of the character with 0,0 in lower left.
     * This will be adjusted by the given rotation.
     *
     * @param rotation Rotation to apply to text to adjust the 0,0 location (0,90,180,270)
     * @return The y coordinate of the text
     */
    private float getYLowerLeftRot(float rotation)
    {
        if (rotation == 0)
        {
            return textMatrix.getTranslateY();
        }
        else if (rotation == 90)
        {
            return pageWidth - textMatrix.getTranslateX();
        }
        else if (rotation == 180)
        {
            return pageHeight - textMatrix.getTranslateY();
        }
        else if (rotation == 270)
        {
            return textMatrix.getTranslateX();
        }
        return 0;
    }

    /**
     * This will get the y position of the text, adjusted so that 0,0 is upper left and it is
     * adjusted based on the page rotation.
     *
     * @return The adjusted y coordinate of the character.
     */
    public float getY()
    {
        return y;
    }

    /**
     * This will get the y position of the text, adjusted so that 0,0 is upper left and it is
     * adjusted based on the text direction.
     *
     * @return The adjusted y coordinate of the character.
     */
    public float getYDirAdj()
    {
        float dir = getDir();
        // some PDFBox code assumes that the 0,0 point is in upper left, not lower left
        if (dir == 0 || dir == 180)
        {
            return pageHeight - getYLowerLeftRot(dir);
        }
        else
        {
            return pageWidth - getYLowerLeftRot(dir);
        }
    }

    /**
     * Get the length or width of the text, based on a given rotation.
     *
     * @param rotation Rotation that was used to determine coordinates (0,90,180,270)
     * @return Width of text in display units
     */
    private float getWidthRot(float rotation)
    {
        if (rotation == 90 || rotation == 270)
        {
            return Math.abs(endY - textMatrix.getTranslateY());
        }
        else
        {
            return Math.abs(endX - textMatrix.getTranslateX());
        }
    }

    /**
     * This will get the width of the string when page rotation adjusted coordinates are used.
     *
     * @return The width of the text in display units.
     */
    public float getWidth()
    {
        return getWidthRot(rotation);
    }

    /**
     * This will get the width of the string when text direction adjusted coordinates are used.
     *
     * @return The width of the text in display units.
     */
    public float getWidthDirAdj()
    {
        return getWidthRot(getDir());
    }

    /**
     * This will get the maximum height of all characters in this string.
     *
     * @return The maximum height of all characters in this string.
     */
    public float getHeight()
    {
        return maxHeight;
    }

    /**
     * This will get the maximum height of all characters in this string.
     *
     * @return The maximum height of all characters in this string.
     */
    public float getHeightDir()
    {
        // this is not really a rotation-dependent calculation, but this is defined for symmetry
        return maxHeight;
    }

    /**
     * This will get the font size that has been set with the "Tf" operator (Set text font and
     * size). When the text is rendered, it may appear bigger or smaller depending on the current
     * transformation matrix (set by the "cm" operator) and the text matrix (set by the "Tm"
     * operator).
     *
     * @return The font size.
     */
    public float getFontSize()
    {
        return fontSize;
    }

    /**
     * This will get the font size in pt. To get this size we have to multiply the font size from
     * {@link #getFontSize() getFontSize()} with the text matrix (set by the "Tm" operator)
     * horizontal scaling factor and truncate the result to integer. The actual rendering may appear
     * bigger or smaller depending on the current transformation matrix (set by the "cm" operator).
     * To get the size in rendering, use {@link #getXScale() getXScale()}.
     *
     * @return The font size in pt.
     */
    public float getFontSizeInPt()
    {
        return fontSizePt;
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
     * This will get the width of a space character. This is useful for some algorithms such as the
     * text stripper, that need to know the width of a space character.
     *
     * @return The width of a space character.
     */
    public float getWidthOfSpace()
    {
        return widthOfSpace;
    }

    /**
     * This will get the X scaling factor. This is dependent on the current transformation matrix
     * (set by the "cm" operator), the text matrix (set by the "Tm" operator) and the font size (set
     * by the "Tf" operator).
     *
     * @return The X scaling factor.
     */
    public float getXScale()
    {
        return textMatrix.getScalingFactorX();
    }

    /**
     * This will get the Y scaling factor. This is dependent on the current transformation matrix
     * (set by the "cm" operator), the text matrix (set by the "Tm" operator) and the font size (set
     * by the "Tf" operator).
     *
     * @return The Y scaling factor.
     */
    public float getYScale()
    {
        return textMatrix.getScalingFactorY();
    }

    /**
     * Get the widths of each individual character.
     *
     * @return An array that has the same length as the CharacterCodes array.
     */
    public float[] getIndividualWidths()
    {
        return widths;
    }

    /**
     * Determine if this TextPosition logically contains another (i.e. they overlap and should be
     * rendered on top of each other).
     *
     * @param tp2 The other TestPosition to compare against
     * @return True if tp2 is contained in the bounding box of this text.
     */
    public boolean contains(TextPosition tp2)
    {
        double thisXstart = getXDirAdj();
        double thisWidth = getWidthDirAdj();
        double thisXend = thisXstart + thisWidth;

        double tp2Xstart = tp2.getXDirAdj();
        double tp2Xend = tp2Xstart + tp2.getWidthDirAdj();

        // no X overlap at all so return as soon as possible
        if (tp2Xend <= thisXstart || tp2Xstart >= thisXend)
        {
            return false;
        }

        // no Y overlap at all so return as soon as possible. Note: 0.0 is in the upper left and
        // y-coordinate is top of TextPosition
        double thisYstart = getYDirAdj();
        double tp2Ystart = tp2.getYDirAdj();
        if (tp2Ystart + tp2.getHeightDir() < thisYstart ||
                tp2Ystart > thisYstart + getHeightDir())
        {
            return false;
        }
        // we're going to calculate the percentage of overlap, if its less than a 15% x-coordinate
        // overlap then we'll return false because its negligible, .15 was determined by trial and
        // error in the regression test files
        else if (tp2Xstart > thisXstart && tp2Xend > thisXend)
        {
            double overlap = thisXend - tp2Xstart;
            double overlapPercent = overlap/thisWidth;
            return overlapPercent > .15;
        }
        else if (tp2Xstart < thisXstart && tp2Xend < thisXend)
        {
            double overlap = tp2Xend - thisXstart;
            double overlapPercent = overlap/thisWidth;
            return overlapPercent > .15;
        }
        return true;
    }

    /**
     * Merge a single character TextPosition into the current object. This is to be used only for
     * cases where we have a diacritic that overlaps an existing TextPosition. In a graphical
     * display, we could overlay them, but for text extraction we need to merge them. Use the
     * contains() method to test if two objects overlap.
     *
     * @param diacritic TextPosition to merge into the current TextPosition.
     */
    public void mergeDiacritic(TextPosition diacritic)
    {
        if (diacritic.getUnicode().length() > 1)
        {
            return;
        }

        float diacXStart = diacritic.getXDirAdj();
        float diacXEnd = diacXStart + diacritic.widths[0];

        float currCharXStart = getXDirAdj();

        int strLen = unicode.length();
        boolean wasAdded = false;

        for (int i = 0; i < strLen && !wasAdded; i++)
        {
            if (i >= widths.length)
            {
                LOG.info("diacritic " + diacritic.getUnicode() + " on ligature " + unicode + 
                        " is not supported yet and is ignored (PDFBOX-2831)");
                break;
            }
            float currCharXEnd = currCharXStart + widths[i];

             // this is the case where there is an overlap of the diacritic character with the
             // current character and the previous character. If no previous character, just append
             // the diacritic after the current one
            if (diacXStart < currCharXStart && diacXEnd <= currCharXEnd)
            {
                if (i == 0)
                {
                    insertDiacritic(i, diacritic);
                }
                else
                {
                    float distanceOverlapping1 = diacXEnd - currCharXStart;
                    float percentage1 = distanceOverlapping1/widths[i];

                    float distanceOverlapping2 = currCharXStart - diacXStart;
                    float percentage2 = distanceOverlapping2/widths[i - 1];

                    if (percentage1 >= percentage2)
                    {
                        insertDiacritic(i, diacritic);
                    }
                    else
                    {
                        insertDiacritic(i - 1, diacritic);
                    }
                }
                wasAdded = true;
            }
            // diacritic completely covers this character and therefore we assume that this is the
            // character the diacritic belongs to
            else if (diacXStart < currCharXStart && diacXEnd > currCharXEnd)
            {
                insertDiacritic(i, diacritic);
                wasAdded = true;
            }
            // otherwise, The diacritic modifies this character because its completely
            // contained by the character width
            else if (diacXStart >= currCharXStart && diacXEnd <= currCharXEnd)
            {
                insertDiacritic(i, diacritic);
                wasAdded = true;
            }
            // last character in the TextPosition so we add diacritic to the end
            else if (diacXStart >= currCharXStart && diacXEnd > currCharXEnd && i == strLen - 1)
            {
                insertDiacritic(i, diacritic);
                wasAdded = true;
            }

            // couldn't find anything useful so we go to the next character in the TextPosition
            currCharXStart += widths[i];
        }
    }

    /**
     * Inserts the diacritic TextPosition to the str of this TextPosition and updates the widths
     * array to include the extra character width.
     *
     * @param i current character
     * @param diacritic The diacritic TextPosition
     */
    private void insertDiacritic(int i, TextPosition diacritic)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(unicode.substring(0, i));

        float[] widths2 = new float[widths.length + 1];
        System.arraycopy(widths, 0, widths2, 0, i);

        // Unicode combining diacritics always go after the base character, regardless of whether
        // the string is in presentation order or logical order
        sb.append(unicode.charAt(i));
        widths2[i] = widths[i];
        sb.append(combineDiacritic(diacritic.getUnicode()));
        widths2[i + 1] = 0;

        // get the rest of the string
        sb.append(unicode.substring(i + 1, unicode.length()));
        System.arraycopy(widths, i + 1, widths2, i + 2, widths.length - i - 1);

        unicode = sb.toString();
        widths = widths2;
    }

    /**
     * Combine the diacritic, for example, convert non-combining diacritic characters to their
     * combining counterparts.
     *
     * @param str String to normalize
     * @return Normalized string
     */
    private String combineDiacritic(String str)
    {
        // Unicode contains special combining forms of the diacritic characters which we want to use
        int codePoint = str.codePointAt(0);

        // convert the characters not defined in the Unicode spec
        if (DIACRITICS.containsKey(codePoint))
        {
            return DIACRITICS.get(codePoint);
        }
        else
        {
            return Normalizer.normalize(str, Normalizer.Form.NFKC).trim();
        }
    }

    /**
     * @return True if the current character is a diacritic char.
     */
    public boolean isDiacritic()
    {
        String text = this.getUnicode();
        if (text.length() != 1)
        {
            return false;
        }
        if ("ー".equals(text))
        {
            // PDFBOX-3833: ー is not a real diacritic like ¨ or ˆ, it just changes the 
            // pronunciation of the previous sound, and is printed after the previous glyph
            // http://www.japanesewithanime.com/2017/04/prolonged-sound-mark.html
            // Ignoring it as diacritic avoids trouble if it slightly overlaps with the next glyph.
            return false;
        }
        int type = Character.getType(text.charAt(0));
        return type == Character.NON_SPACING_MARK ||
               type == Character.MODIFIER_SYMBOL ||
               type == Character.MODIFIER_LETTER;

  }

    /**
     * Show the string data for this text position.
     *
     * @return A human readable form of this object.
     */
    @Override
    public String toString()
    {
        return getUnicode();
    }

    /**
     * This will get the x coordinate of the end position. This is the unadjusted value passed into
     * the constructor.
     *
     * @return The unadjusted x coordinate of the end position
     */
    public float getEndX()
    {
        return endX;
    }

    /**
     * This will get the y coordinate of the end position. This is the unadjusted value passed into
     * the constructor.
     *
     * @return The unadjusted y coordinate of the end position
     */
    public float getEndY()
    {
        return endY;
    }

    /**
     * This will get the rotation of the page that the text is located in. This is the unadjusted
     * value passed into the constructor.
     *
     * @return The unadjusted rotation of the page that the text is located in
     */
    public int getRotation()
    {
        return rotation;
    }

    /**
     * This will get the height of the page that the text is located in. This is the unadjusted
     * value passed into the constructor.
     *
     * @return The unadjusted height of the page that the text is located in
     */
    public float getPageHeight()
    {
        return pageHeight;
    }

    /**
     * This will get the width of the page that the text is located in. This is the unadjusted value
     * passed into the constructor.
     *
     * @return The unadjusted width of the page that the text is located in
     */
    public float getPageWidth()
    {
        return pageWidth;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof TextPosition))
        {
            return false;
        }

        TextPosition that = (TextPosition) o;

        if (Float.compare(that.endX, endX) != 0)
        {
            return false;
        }
        if (Float.compare(that.endY, endY) != 0)
        {
            return false;
        }
        if (Float.compare(that.maxHeight, maxHeight) != 0)
        {
            return false;
        }
        if (rotation != that.rotation)
        {
            return false;
        }
        if (Float.compare(that.x, x) != 0)
        {
            return false;
        }
        if (Float.compare(that.y, y) != 0)
        {
            return false;
        }
        if (Float.compare(that.pageHeight, pageHeight) != 0)
        {
            return false;
        }
        if (Float.compare(that.pageWidth, pageWidth) != 0)
        {
            return false;
        }
        if (Float.compare(that.widthOfSpace, widthOfSpace) != 0)
        {
            return false;
        }
        if (Float.compare(that.fontSize, fontSize) != 0)
        {
            return false;
        }
        if (fontSizePt != that.fontSizePt)
        {
            return false;
        }
        if (Float.compare(that.direction, direction) != 0)
        {
            return false;
        }
        if (textMatrix != null ? !textMatrix.equals(that.textMatrix) : that.textMatrix != null)
        {
            return false;
        }
        if (!Arrays.equals(charCodes, that.charCodes))
        {
            return false;
        }
        if (font != null ? !font.equals(that.font) : that.font != null)
        {
            return false;
        }
        if (!Arrays.equals(widths, that.widths))
        {
            return false;
        }
        return unicode != null ? unicode.equals(that.unicode) : that.unicode == null;

    }

    @Override
    public int hashCode()
    {
        int result = textMatrix != null ? textMatrix.hashCode() : 0;
        result = 31 * result + Float.floatToIntBits(endX);
        result = 31 * result + Float.floatToIntBits(endY);
        result = 31 * result + Float.floatToIntBits(maxHeight);
        result = 31 * result + rotation;
        result = 31 * result + Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        result = 31 * result + Float.floatToIntBits(pageHeight);
        result = 31 * result + Float.floatToIntBits(pageWidth);
        result = 31 * result + Float.floatToIntBits(widthOfSpace);
        result = 31 * result + Arrays.hashCode(charCodes);
        result = 31 * result + (font != null ? font.hashCode() : 0);
        result = 31 * result + Float.floatToIntBits(fontSize);
        result = 31 * result + fontSizePt;
        return result;
    }
}
