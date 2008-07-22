/**
 * Copyright (c) 2005, www.fontbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of fontbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.fontbox.org
 *
 */
package org.fontbox.ttf;

import java.io.IOException;
import java.util.Calendar;

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class HeaderTable extends TTFTable
{
    /**
     * Tag to identify this table.
     */
    public static final String TAG = "head";
    
    private float version;
    private float fontRevision;
    private long checkSumAdjustment;
    private long magicNumber;
    private int flags;
    private int unitsPerEm;
    private Calendar created;
    private Calendar modified;
    private short xMin;
    private short yMin;
    private short xMax;
    private short yMax;
    private int macStyle;
    private int lowestRecPPEM;
    private short fontDirectionHint;
    private short indexToLocFormat;
    private short glyphDataFormat;
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        version = data.read32Fixed();
        fontRevision = data.read32Fixed();
        checkSumAdjustment = data.readUnsignedInt();
        magicNumber = data.readUnsignedInt();
        flags = data.readUnsignedShort();
        unitsPerEm = data.readUnsignedShort();
        created = data.readInternationalDate();
        modified = data.readInternationalDate();
        xMin = data.readSignedShort();
        yMin = data.readSignedShort();
        xMax = data.readSignedShort();
        yMax = data.readSignedShort();
        macStyle = data.readUnsignedShort();
        lowestRecPPEM = data.readUnsignedShort();
        fontDirectionHint = data.readSignedShort();
        indexToLocFormat = data.readSignedShort();
        glyphDataFormat = data.readSignedShort();
    }
    /**
     * @return Returns the checkSumAdjustment.
     */
    public long getCheckSumAdjustment()
    {
        return checkSumAdjustment;
    }
    /**
     * @param checkSumAdjustmentValue The checkSumAdjustment to set.
     */
    public void setCheckSumAdjustment(long checkSumAdjustmentValue)
    {
        this.checkSumAdjustment = checkSumAdjustmentValue;
    }
    /**
     * @return Returns the created.
     */
    public Calendar getCreated()
    {
        return created;
    }
    /**
     * @param createdValue The created to set.
     */
    public void setCreated(Calendar createdValue)
    {
        this.created = createdValue;
    }
    /**
     * @return Returns the flags.
     */
    public int getFlags()
    {
        return flags;
    }
    /**
     * @param flagsValue The flags to set.
     */
    public void setFlags(int flagsValue)
    {
        this.flags = flagsValue;
    }
    /**
     * @return Returns the fontDirectionHint.
     */
    public short getFontDirectionHint()
    {
        return fontDirectionHint;
    }
    /**
     * @param fontDirectionHintValue The fontDirectionHint to set.
     */
    public void setFontDirectionHint(short fontDirectionHintValue)
    {
        this.fontDirectionHint = fontDirectionHintValue;
    }
    /**
     * @return Returns the fontRevision.
     */
    public float getFontRevision()
    {
        return fontRevision;
    }
    /**
     * @param fontRevisionValue The fontRevision to set.
     */
    public void setFontRevision(float fontRevisionValue)
    {
        this.fontRevision = fontRevisionValue;
    }
    /**
     * @return Returns the glyphDataFormat.
     */
    public short getGlyphDataFormat()
    {
        return glyphDataFormat;
    }
    /**
     * @param glyphDataFormatValue The glyphDataFormat to set.
     */
    public void setGlyphDataFormat(short glyphDataFormatValue)
    {
        this.glyphDataFormat = glyphDataFormatValue;
    }
    /**
     * @return Returns the indexToLocFormat.
     */
    public short getIndexToLocFormat()
    {
        return indexToLocFormat;
    }
    /**
     * @param indexToLocFormatValue The indexToLocFormat to set.
     */
    public void setIndexToLocFormat(short indexToLocFormatValue)
    {
        this.indexToLocFormat = indexToLocFormatValue;
    }
    /**
     * @return Returns the lowestRecPPEM.
     */
    public int getLowestRecPPEM()
    {
        return lowestRecPPEM;
    }
    /**
     * @param lowestRecPPEMValue The lowestRecPPEM to set.
     */
    public void setLowestRecPPEM(int lowestRecPPEMValue)
    {
        this.lowestRecPPEM = lowestRecPPEMValue;
    }
    /**
     * @return Returns the macStyle.
     */
    public int getMacStyle()
    {
        return macStyle;
    }
    /**
     * @param macStyleValue The macStyle to set.
     */
    public void setMacStyle(int macStyleValue)
    {
        this.macStyle = macStyleValue;
    }
    /**
     * @return Returns the magicNumber.
     */
    public long getMagicNumber()
    {
        return magicNumber;
    }
    /**
     * @param magicNumberValue The magicNumber to set.
     */
    public void setMagicNumber(long magicNumberValue)
    {
        this.magicNumber = magicNumberValue;
    }
    /**
     * @return Returns the modified.
     */
    public Calendar getModified()
    {
        return modified;
    }
    /**
     * @param modifiedValue The modified to set.
     */
    public void setModified(Calendar modifiedValue)
    {
        this.modified = modifiedValue;
    }
    /**
     * @return Returns the unitsPerEm.
     */
    public int getUnitsPerEm()
    {
        return unitsPerEm;
    }
    /**
     * @param unitsPerEmValue The unitsPerEm to set.
     */
    public void setUnitsPerEm(int unitsPerEmValue)
    {
        this.unitsPerEm = unitsPerEmValue;
    }
    /**
     * @return Returns the version.
     */
    public float getVersion()
    {
        return version;
    }
    /**
     * @param versionValue The version to set.
     */
    public void setVersion(float versionValue)
    {
        this.version = versionValue;
    }
    /**
     * @return Returns the xMax.
     */
    public short getXMax()
    {
        return xMax;
    }
    /**
     * @param maxValue The xMax to set.
     */
    public void setXMax(short maxValue)
    {
        xMax = maxValue;
    }
    /**
     * @return Returns the xMin.
     */
    public short getXMin()
    {
        return xMin;
    }
    /**
     * @param minValue The xMin to set.
     */
    public void setXMin(short minValue)
    {
        xMin = minValue;
    }
    /**
     * @return Returns the yMax.
     */
    public short getYMax()
    {
        return yMax;
    }
    /**
     * @param maxValue The yMax to set.
     */
    public void setYMax(short maxValue)
    {
        yMax = maxValue;
    }
    /**
     * @return Returns the yMin.
     */
    public short getYMin()
    {
        return yMin;
    }
    /**
     * @param minValue The yMin to set.
     */
    public void setYMin(short minValue)
    {
        yMin = minValue;
    }
}
