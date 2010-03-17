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
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class OS2WindowsMetricsTable extends TTFTable
{
    
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_THIN = 100;
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_ULTRA_LIGHT = 200;
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_LIGHT = 300;
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_NORMAL = 400;
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_MEDIUM = 500;
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_SEMI_BOLD = 600;
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_BOLD = 700;
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_EXTRA_BOLD = 800;
    /**
     * Weight class constant.
     */
    public static final int WEIGHT_CLASS_BLACK = 900;
    
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_ULTRA_CONDENSED = 1;
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_EXTRA_CONDENSED = 2;
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_CONDENSED = 3;
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_SEMI_CONDENSED = 4;
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_MEDIUM = 5;
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_SEMI_EXPANDED = 6;
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_EXPANDED = 7;
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_EXTRA_EXPANDED = 8;
    /**
     * Width class constant.
     */
    public static final int WIDTH_CLASS_ULTRA_EXPANDED = 9;
    
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_NO_CLASSIFICATION = 0;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_OLDSTYLE_SERIFS = 1;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_TRANSITIONAL_SERIFS = 2;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_MODERN_SERIFS = 3;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_CLAREDON_SERIFS = 4;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_SLAB_SERIFS = 5;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_FREEFORM_SERIFS = 7;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_SANS_SERIF = 8;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_ORNAMENTALS = 9;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_SCRIPTS = 10;
    /**
     * Family class constant.
     */
    public static final int FAMILY_CLASS_SYMBOLIC = 12;
    
    /**
     * @return Returns the achVendId.
     */
    public String getAchVendId()
    {
        return achVendId;
    }
    /**
     * @param achVendIdValue The achVendId to set.
     */
    public void setAchVendId(String achVendIdValue)
    {
        this.achVendId = achVendIdValue;
    }
    /**
     * @return Returns the averageCharWidth.
     */
    public short getAverageCharWidth()
    {
        return averageCharWidth;
    }
    /**
     * @param averageCharWidthValue The averageCharWidth to set.
     */
    public void setAverageCharWidth(short averageCharWidthValue)
    {
        this.averageCharWidth = averageCharWidthValue;
    }
    /**
     * @return Returns the codePageRange1.
     */
    public long getCodePageRange1()
    {
        return codePageRange1;
    }
    /**
     * @param codePageRange1Value The codePageRange1 to set.
     */
    public void setCodePageRange1(long codePageRange1Value)
    {
        this.codePageRange1 = codePageRange1Value;
    }
    /**
     * @return Returns the codePageRange2.
     */
    public long getCodePageRange2()
    {
        return codePageRange2;
    }
    /**
     * @param codePageRange2Value The codePageRange2 to set.
     */
    public void setCodePageRange2(long codePageRange2Value)
    {
        this.codePageRange2 = codePageRange2Value;
    }
    /**
     * @return Returns the familyClass.
     */
    public short getFamilyClass()
    {
        return familyClass;
    }
    /**
     * @param familyClassValue The familyClass to set.
     */
    public void setFamilyClass(short familyClassValue)
    {
        this.familyClass = familyClassValue;
    }
    /**
     * @return Returns the firstCharIndex.
     */
    public int getFirstCharIndex()
    {
        return firstCharIndex;
    }
    /**
     * @param firstCharIndexValue The firstCharIndex to set.
     */
    public void setFirstCharIndex(int firstCharIndexValue)
    {
        this.firstCharIndex = firstCharIndexValue;
    }
    /**
     * @return Returns the fsSelection.
     */
    public int getFsSelection()
    {
        return fsSelection;
    }
    /**
     * @param fsSelectionValue The fsSelection to set.
     */
    public void setFsSelection(int fsSelectionValue)
    {
        this.fsSelection = fsSelectionValue;
    }
    /**
     * @return Returns the fsType.
     */
    public short getFsType()
    {
        return fsType;
    }
    /**
     * @param fsTypeValue The fsType to set.
     */
    public void setFsType(short fsTypeValue)
    {
        this.fsType = fsTypeValue;
    }
    /**
     * @return Returns the lastCharIndex.
     */
    public int getLastCharIndex()
    {
        return lastCharIndex;
    }
    /**
     * @param lastCharIndexValue The lastCharIndex to set.
     */
    public void setLastCharIndex(int lastCharIndexValue)
    {
        this.lastCharIndex = lastCharIndexValue;
    }
    /**
     * @return Returns the panose.
     */
    public byte[] getPanose()
    {
        return panose;
    }
    /**
     * @param panoseValue The panose to set.
     */
    public void setPanose(byte[] panoseValue)
    {
        this.panose = panoseValue;
    }
    /**
     * @return Returns the strikeoutPosition.
     */
    public short getStrikeoutPosition()
    {
        return strikeoutPosition;
    }
    /**
     * @param strikeoutPositionValue The strikeoutPosition to set.
     */
    public void setStrikeoutPosition(short strikeoutPositionValue)
    {
        this.strikeoutPosition = strikeoutPositionValue;
    }
    /**
     * @return Returns the strikeoutSize.
     */
    public short getStrikeoutSize()
    {
        return strikeoutSize;
    }
    /**
     * @param strikeoutSizeValue The strikeoutSize to set.
     */
    public void setStrikeoutSize(short strikeoutSizeValue)
    {
        this.strikeoutSize = strikeoutSizeValue;
    }
    /**
     * @return Returns the subscriptXOffset.
     */
    public short getSubscriptXOffset()
    {
        return subscriptXOffset;
    }
    /**
     * @param subscriptXOffsetValue The subscriptXOffset to set.
     */
    public void setSubscriptXOffset(short subscriptXOffsetValue)
    {
        this.subscriptXOffset = subscriptXOffsetValue;
    }
    /**
     * @return Returns the subscriptXSize.
     */
    public short getSubscriptXSize()
    {
        return subscriptXSize;
    }
    /**
     * @param subscriptXSizeValue The subscriptXSize to set.
     */
    public void setSubscriptXSize(short subscriptXSizeValue)
    {
        this.subscriptXSize = subscriptXSizeValue;
    }
    /**
     * @return Returns the subscriptYOffset.
     */
    public short getSubscriptYOffset()
    {
        return subscriptYOffset;
    }
    /**
     * @param subscriptYOffsetValue The subscriptYOffset to set.
     */
    public void setSubscriptYOffset(short subscriptYOffsetValue)
    {
        this.subscriptYOffset = subscriptYOffsetValue;
    }
    /**
     * @return Returns the subscriptYSize.
     */
    public short getSubscriptYSize()
    {
        return subscriptYSize;
    }
    /**
     * @param subscriptYSizeValue The subscriptYSize to set.
     */
    public void setSubscriptYSize(short subscriptYSizeValue)
    {
        this.subscriptYSize = subscriptYSizeValue;
    }
    /**
     * @return Returns the superscriptXOffset.
     */
    public short getSuperscriptXOffset()
    {
        return superscriptXOffset;
    }
    /**
     * @param superscriptXOffsetValue The superscriptXOffset to set.
     */
    public void setSuperscriptXOffset(short superscriptXOffsetValue)
    {
        this.superscriptXOffset = superscriptXOffsetValue;
    }
    /**
     * @return Returns the superscriptXSize.
     */
    public short getSuperscriptXSize()
    {
        return superscriptXSize;
    }
    /**
     * @param superscriptXSizeValue The superscriptXSize to set.
     */
    public void setSuperscriptXSize(short superscriptXSizeValue)
    {
        this.superscriptXSize = superscriptXSizeValue;
    }
    /**
     * @return Returns the superscriptYOffset.
     */
    public short getSuperscriptYOffset()
    {
        return superscriptYOffset;
    }
    /**
     * @param superscriptYOffsetValue The superscriptYOffset to set.
     */
    public void setSuperscriptYOffset(short superscriptYOffsetValue)
    {
        this.superscriptYOffset = superscriptYOffsetValue;
    }
    /**
     * @return Returns the superscriptYSize.
     */
    public short getSuperscriptYSize()
    {
        return superscriptYSize;
    }
    /**
     * @param superscriptYSizeValue The superscriptYSize to set.
     */
    public void setSuperscriptYSize(short superscriptYSizeValue)
    {
        this.superscriptYSize = superscriptYSizeValue;
    }
    /**
     * @return Returns the typeLineGap.
     */
    public int getTypeLineGap()
    {
        return typeLineGap;
    }
    /**
     * @param typeLineGapValue The typeLineGap to set.
     */
    public void setTypeLineGap(int typeLineGapValue)
    {
        this.typeLineGap = typeLineGapValue;
    }
    /**
     * @return Returns the typoAscender.
     */
    public int getTypoAscender()
    {
        return typoAscender;
    }
    /**
     * @param typoAscenderValue The typoAscender to set.
     */
    public void setTypoAscender(int typoAscenderValue)
    {
        this.typoAscender = typoAscenderValue;
    }
    /**
     * @return Returns the typoDescender.
     */
    public int getTypoDescender()
    {
        return typoDescender;
    }
    /**
     * @param typoDescenderValue The typoDescender to set.
     */
    public void setTypoDescender(int typoDescenderValue)
    {
        this.typoDescender = typoDescenderValue;
    }
    /**
     * @return Returns the unicodeRange1.
     */
    public long getUnicodeRange1()
    {
        return unicodeRange1;
    }
    /**
     * @param unicodeRange1Value The unicodeRange1 to set.
     */
    public void setUnicodeRange1(long unicodeRange1Value)
    {
        this.unicodeRange1 = unicodeRange1Value;
    }
    /**
     * @return Returns the unicodeRange2.
     */
    public long getUnicodeRange2()
    {
        return unicodeRange2;
    }
    /**
     * @param unicodeRange2Value The unicodeRange2 to set.
     */
    public void setUnicodeRange2(long unicodeRange2Value)
    {
        this.unicodeRange2 = unicodeRange2Value;
    }
    /**
     * @return Returns the unicodeRange3.
     */
    public long getUnicodeRange3()
    {
        return unicodeRange3;
    }
    /**
     * @param unicodeRange3Value The unicodeRange3 to set.
     */
    public void setUnicodeRange3(long unicodeRange3Value)
    {
        this.unicodeRange3 = unicodeRange3Value;
    }
    /**
     * @return Returns the unicodeRange4.
     */
    public long getUnicodeRange4()
    {
        return unicodeRange4;
    }
    /**
     * @param unicodeRange4Value The unicodeRange4 to set.
     */
    public void setUnicodeRange4(long unicodeRange4Value)
    {
        this.unicodeRange4 = unicodeRange4Value;
    }
    /**
     * @return Returns the version.
     */
    public int getVersion()
    {
        return version;
    }
    /**
     * @param versionValue The version to set.
     */
    public void setVersion(int versionValue)
    {
        this.version = versionValue;
    }
    /**
     * @return Returns the weightClass.
     */
    public int getWeightClass()
    {
        return weightClass;
    }
    /**
     * @param weightClassValue The weightClass to set.
     */
    public void setWeightClass(int weightClassValue)
    {
        this.weightClass = weightClassValue;
    }
    /**
     * @return Returns the widthClass.
     */
    public int getWidthClass()
    {
        return widthClass;
    }
    /**
     * @param widthClassValue The widthClass to set.
     */
    public void setWidthClass(int widthClassValue)
    {
        this.widthClass = widthClassValue;
    }
    /**
     * @return Returns the winAscent.
     */
    public int getWinAscent()
    {
        return winAscent;
    }
    /**
     * @param winAscentValue The winAscent to set.
     */
    public void setWinAscent(int winAscentValue)
    {
        this.winAscent = winAscentValue;
    }
    /**
     * @return Returns the winDescent.
     */
    public int getWinDescent()
    {
        return winDescent;
    }
    /**
     * @param winDescentValue The winDescent to set.
     */
    public void setWinDescent(int winDescentValue)
    {
        this.winDescent = winDescentValue;
    }
    private int version;
    private short averageCharWidth;
    private int weightClass;
    private int widthClass;
    private short fsType;
    private short subscriptXSize;
    private short subscriptYSize;
    private short subscriptXOffset;
    private short subscriptYOffset;
    private short superscriptXSize;
    private short superscriptYSize;
    private short superscriptXOffset;
    private short superscriptYOffset;
    private short strikeoutSize;
    private short strikeoutPosition;
    private short familyClass;
    private byte[] panose = new byte[10];
    private long unicodeRange1;
    private long unicodeRange2;
    private long unicodeRange3;
    private long unicodeRange4;
    private String achVendId;
    private int fsSelection;
    private int firstCharIndex;
    private int lastCharIndex;
    private int typoAscender;
    private int typoDescender;
    private int typeLineGap;
    private int winAscent;
    private int winDescent;
    private long codePageRange1 = -1;
    private long codePageRange2 = -1;
    
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "OS/2";
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        version = data.readUnsignedShort();
        averageCharWidth = data.readSignedShort();
        weightClass = data.readUnsignedShort();
        widthClass = data.readUnsignedShort();
        fsType = data.readSignedShort();
        subscriptXSize = data.readSignedShort();
        subscriptYSize = data.readSignedShort();
        subscriptXOffset = data.readSignedShort();
        subscriptYOffset = data.readSignedShort();
        superscriptXSize = data.readSignedShort();
        superscriptYSize = data.readSignedShort();
        superscriptXOffset = data.readSignedShort();
        superscriptYOffset = data.readSignedShort();
        strikeoutSize = data.readSignedShort();
        strikeoutPosition = data.readSignedShort();
        familyClass = data.readSignedShort();
        panose = data.read( 10 );
        unicodeRange1 = data.readUnsignedInt();
        unicodeRange2 = data.readUnsignedInt();
        unicodeRange3 = data.readUnsignedInt();
        unicodeRange4 = data.readUnsignedInt();
        achVendId = data.readString( 4 );
        fsSelection = data.readUnsignedShort();
        firstCharIndex = data.readUnsignedShort();
        lastCharIndex = data.readUnsignedShort();
        typoAscender = data.readSignedShort();
        typoDescender = data.readSignedShort();
        typeLineGap = data.readSignedShort();
        winAscent = data.readUnsignedShort();
        winDescent = data.readUnsignedShort();
        if( version >= 1 )
        {
            codePageRange1 = data.readUnsignedInt();
            codePageRange2 = data.readUnsignedInt();
        }
    }
}
