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
import java.util.Calendar;

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield
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
    public void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
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
        initialized = true;
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
