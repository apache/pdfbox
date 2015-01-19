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
 * A name record in the name table.
 * 
 * @author Ben Litchfield
 */
public class NameRecord
{
    // platform ids
    public static final int PLATFORM_UNICODE = 0;
    public static final int PLATFORM_MACINTOSH = 1;
    public static final int PLATFORM_ISO = 2;
    public static final int PLATFORM_WINDOWS = 3;

    // Unicode encoding ids
    public static final int ENCODING_UNICODE_1_0 = 0;
    public static final int ENCODING_UNICODE_1_1 = 1;
    public static final int ENCODING_UNICODE_2_0_BMP = 3;
    public static final int ENCODING_UNICODE_2_0_FULL = 4;

    // Unicode encoding ids
    public static final int LANGUGAE_UNICODE = 0;

    // Windows encoding ids
    public static final int ENCODING_WINDOWS_SYMBOL = 0;
    public static final int ENCODING_WINDOWS_UNICODE_BMP = 1;
    public static final int ENCODING_WINDOWS_UNICODE_UCS4 = 10;

    // Windows language ids
    public static final int LANGUGAE_WINDOWS_EN_US = 0x0409;

    // Macintosh encoding ids
    public static final int ENCODING_MACINTOSH_ROMAN = 0;

    // Macintosh language ids
    public static final int LANGUGAE_MACINTOSH_ENGLISH = 0;

    // name ids
    public static final int NAME_COPYRIGHT = 0;
    public static final int NAME_FONT_FAMILY_NAME = 1;
    public static final int NAME_FONT_SUB_FAMILY_NAME = 2;
    public static final int NAME_UNIQUE_FONT_ID = 3;
    public static final int NAME_FULL_FONT_NAME = 4;
    public static final int NAME_VERSION = 5;
    public static final int NAME_POSTSCRIPT_NAME = 6;
    public static final int NAME_TRADEMARK = 7;
    
    private int platformId;
    private int platformEncodingId;
    private int languageId;
    private int nameId;
    private int stringLength;
    private int stringOffset;
    private String string;
    
    /**
     * @return Returns the stringLength.
     */
    public int getStringLength()
    {
        return stringLength;
    }
    /**
     * @param stringLengthValue The stringLength to set.
     */
    public void setStringLength(int stringLengthValue)
    {
        this.stringLength = stringLengthValue;
    }
    /**
     * @return Returns the stringOffset.
     */
    public int getStringOffset()
    {
        return stringOffset;
    }
    /**
     * @param stringOffsetValue The stringOffset to set.
     */
    public void setStringOffset(int stringOffsetValue)
    {
        this.stringOffset = stringOffsetValue;
    }
    
    /**
     * @return Returns the languageId.
     */
    public int getLanguageId()
    {
        return languageId;
    }
    /**
     * @param languageIdValue The languageId to set.
     */
    public void setLanguageId(int languageIdValue)
    {
        this.languageId = languageIdValue;
    }
    /**
     * @return Returns the nameId.
     */
    public int getNameId()
    {
        return nameId;
    }
    /**
     * @param nameIdValue The nameId to set.
     */
    public void setNameId(int nameIdValue)
    {
        this.nameId = nameIdValue;
    }
    /**
     * @return Returns the platformEncodingId.
     */
    public int getPlatformEncodingId()
    {
        return platformEncodingId;
    }
    /**
     * @param platformEncodingIdValue The platformEncodingId to set.
     */
    public void setPlatformEncodingId(int platformEncodingIdValue)
    {
        this.platformEncodingId = platformEncodingIdValue;
    }
    /**
     * @return Returns the platformId.
     */
    public int getPlatformId()
    {
        return platformId;
    }
    /**
     * @param platformIdValue The platformId to set.
     */
    public void setPlatformId(int platformIdValue)
    {
        this.platformId = platformIdValue;
    }
    
    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData( TrueTypeFont ttf, TTFDataStream data ) throws IOException
    {
        platformId = data.readUnsignedShort();
        platformEncodingId = data.readUnsignedShort();
        languageId = data.readUnsignedShort();
        nameId = data.readUnsignedShort();
        stringLength = data.readUnsignedShort();
        stringOffset = data.readUnsignedShort();
    }
    
    /**
     * Return a string representation of this class.
     * 
     * @return A string for this class.
     */
    public String toString()
    {
        return 
            "platform=" + platformId + 
            " pEncoding=" + platformEncodingId + 
            " language=" + languageId + 
            " name=" + nameId + 
            " " + string;
    }
    /**
     * @return Returns the string.
     */
    public String getString()
    {
        return string;
    }
    /**
     * @param stringValue The string to set.
     */
    public void setString(String stringValue)
    {
        this.string = stringValue;
    }
}
