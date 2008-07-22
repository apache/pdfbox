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

/**
 * A name record in the name table.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class NameRecord
{
    /**
     * A constant for the platform.
     */
    public static final int PLATFORM_APPLE_UNICODE = 0;
    /**
     * A constant for the platform.
     */
    public static final int PLATFORM_MACINTOSH = 1;
    /**
     * A constant for the platform.
     */
    public static final int PLATFORM_ISO = 2;
    /**
     * A constant for the platform.
     */
    public static final int PLATFORM_WINDOWS = 3;
    
    /**
     * Platform specific encoding.
     */
    public static final int PLATFORM_ENCODING_WINDOWS_UNDEFINED = 0;
    /**
     * Platform specific encoding.
     */
    public static final int PLATFORM_ENCODING_WINDOWS_UNICODE = 1;
    
    /**
     * A name id.
     */
    public static final int NAME_COPYRIGHT = 0;
    /**
     * A name id.
     */
    public static final int NAME_FONT_FAMILY_NAME = 1;
    /**
     * A name id.
     */
    public static final int NAME_FONT_SUB_FAMILY_NAME = 2;
    /**
     * A name id.
     */
    public static final int NAME_UNIQUE_FONT_ID = 3;
    /**
     * A name id.
     */
    public static final int NAME_FULL_FONT_NAME = 4;
    /**
     * A name id.
     */
    public static final int NAME_VERSION = 5;
    /**
     * A name id.
     */
    public static final int NAME_POSTSCRIPT_NAME = 6;
    /**
     * A name id.
     */
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
            " name=" + nameId;
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
