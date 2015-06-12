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
package org.apache.fontbox.ttf;

import java.io.IOException;

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield
 */
public class PostScriptTable extends TTFTable
{
    private float formatType;
    private float italicAngle;
    private short underlinePosition;
    private short underlineThickness;
    private long isFixedPitch;
    private long minMemType42;
    private long maxMemType42;
    private long mimMemType1;
    private long maxMemType1;
    private String[] glyphNames = null;

    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "post";

    PostScriptTable(TrueTypeFont font)
    {
        super(font);
    }

    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    @Override
    public void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        formatType = data.read32Fixed();
        italicAngle = data.read32Fixed();
        underlinePosition = data.readSignedShort();
        underlineThickness = data.readSignedShort();
        isFixedPitch = data.readUnsignedInt();
        minMemType42 = data.readUnsignedInt();
        maxMemType42 = data.readUnsignedInt();
        mimMemType1 = data.readUnsignedInt();
        maxMemType1 = data.readUnsignedInt();

        if (formatType == 1.0f)
        {
            /*
             * This TrueType font file contains exactly the 258 glyphs in the standard Macintosh TrueType.
             */
            glyphNames = new String[WGL4Names.NUMBER_OF_MAC_GLYPHS];
            System.arraycopy(WGL4Names.MAC_GLYPH_NAMES, 0, glyphNames, 0, WGL4Names.NUMBER_OF_MAC_GLYPHS);
        }
        else if (formatType == 2.0f)
        {
            int numGlyphs = data.readUnsignedShort();
            int[] glyphNameIndex = new int[numGlyphs];
            glyphNames = new String[numGlyphs];
            int maxIndex = Integer.MIN_VALUE;
            for (int i = 0; i < numGlyphs; i++)
            {
                int index = data.readUnsignedShort();
                glyphNameIndex[i] = index;
                // PDFBOX-808: Index numbers between 32768 and 65535 are
                // reserved for future use, so we should just ignore them
                if (index <= 32767)
                {
                    maxIndex = Math.max(maxIndex, index);
                }
            }
            String[] nameArray = null;
            if (maxIndex >= WGL4Names.NUMBER_OF_MAC_GLYPHS)
            {
                nameArray = new String[maxIndex - WGL4Names.NUMBER_OF_MAC_GLYPHS + 1];
                for (int i = 0; i < maxIndex - WGL4Names.NUMBER_OF_MAC_GLYPHS + 1; i++)
                {
                    int numberOfChars = data.readUnsignedByte();
                    nameArray[i] = data.readString(numberOfChars);
                }
            }
            for (int i = 0; i < numGlyphs; i++)
            {
                int index = glyphNameIndex[i];
                if (index < WGL4Names.NUMBER_OF_MAC_GLYPHS)
                {
                    glyphNames[i] = WGL4Names.MAC_GLYPH_NAMES[index];
                }
                else if (index >= WGL4Names.NUMBER_OF_MAC_GLYPHS && index <= 32767)
                {
                    glyphNames[i] = nameArray[index - WGL4Names.NUMBER_OF_MAC_GLYPHS];
                }
                else
                {
                    // PDFBOX-808: Index numbers between 32768 and 65535 are
                    // reserved for future use, so we should just ignore them
                    glyphNames[i] = ".undefined";
                }
            }
        }
        else if (formatType == 2.5f)
        {
            int[] glyphNameIndex = new int[ttf.getNumberOfGlyphs()];
            for (int i = 0; i < glyphNameIndex.length; i++)
            {
                int offset = data.readSignedByte();
                glyphNameIndex[i] = i + 1 + offset;
            }
            glyphNames = new String[glyphNameIndex.length];
            for (int i = 0; i < glyphNames.length; i++)
            {
                String name = WGL4Names.MAC_GLYPH_NAMES[glyphNameIndex[i]];
                if (name != null)
                {
                    glyphNames[i] = name;
                }
            }

        }
        else if (formatType == 3.0f)
        {
            // no postscript information is provided.
        }
        initialized = true;
    }

    /**
     * @return Returns the formatType.
     */
    public float getFormatType()
    {
        return formatType;
    }

    /**
     * @param formatTypeValue The formatType to set.
     */
    public void setFormatType(float formatTypeValue)
    {
        this.formatType = formatTypeValue;
    }

    /**
     * @return Returns the isFixedPitch.
     */
    public long getIsFixedPitch()
    {
        return isFixedPitch;
    }

    /**
     * @param isFixedPitchValue The isFixedPitch to set.
     */
    public void setIsFixedPitch(long isFixedPitchValue)
    {
        this.isFixedPitch = isFixedPitchValue;
    }

    /**
     * @return Returns the italicAngle.
     */
    public float getItalicAngle()
    {
        return italicAngle;
    }

    /**
     * @param italicAngleValue The italicAngle to set.
     */
    public void setItalicAngle(float italicAngleValue)
    {
        this.italicAngle = italicAngleValue;
    }

    /**
     * @return Returns the maxMemType1.
     */
    public long getMaxMemType1()
    {
        return maxMemType1;
    }

    /**
     * @param maxMemType1Value The maxMemType1 to set.
     */
    public void setMaxMemType1(long maxMemType1Value)
    {
        this.maxMemType1 = maxMemType1Value;
    }

    /**
     * @return Returns the maxMemType42.
     */
    public long getMaxMemType42()
    {
        return maxMemType42;
    }

    /**
     * @param maxMemType42Value The maxMemType42 to set.
     */
    public void setMaxMemType42(long maxMemType42Value)
    {
        this.maxMemType42 = maxMemType42Value;
    }

    /**
     * @return Returns the mimMemType1.
     */
    public long getMinMemType1()
    {
        return mimMemType1;
    }

    /**
     * @param mimMemType1Value The mimMemType1 to set.
     */
    public void setMimMemType1(long mimMemType1Value)
    {
        this.mimMemType1 = mimMemType1Value;
    }

    /**
     * @return Returns the minMemType42.
     */
    public long getMinMemType42()
    {
        return minMemType42;
    }

    /**
     * @param minMemType42Value The minMemType42 to set.
     */
    public void setMinMemType42(long minMemType42Value)
    {
        this.minMemType42 = minMemType42Value;
    }

    /**
     * @return Returns the underlinePosition.
     */
    public short getUnderlinePosition()
    {
        return underlinePosition;
    }

    /**
     * @param underlinePositionValue The underlinePosition to set.
     */
    public void setUnderlinePosition(short underlinePositionValue)
    {
        this.underlinePosition = underlinePositionValue;
    }

    /**
     * @return Returns the underlineThickness.
     */
    public short getUnderlineThickness()
    {
        return underlineThickness;
    }

    /**
     * @param underlineThicknessValue The underlineThickness to set.
     */
    public void setUnderlineThickness(short underlineThicknessValue)
    {
        this.underlineThickness = underlineThicknessValue;
    }

    /**
     * @return Returns the glyphNames.
     */
    public String[] getGlyphNames()
    {
        return glyphNames;
    }

    /**
     * @param glyphNamesValue The glyphNames to set.
     */
    public void setGlyphNames(String[] glyphNamesValue)
    {
        this.glyphNames = glyphNamesValue;
    }

    /**
     * @return Returns the glyph name.
     */
    public String getName(int gid)
    {
        if (gid < 0 || glyphNames == null || gid > glyphNames.length)
        {
            return null;
        }
        return glyphNames[gid];
    }
}
