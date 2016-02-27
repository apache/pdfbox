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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.util.Charsets;

/**
 * A table in a true type font.
 * 
 * @author Ben Litchfield
 */
public class NamingTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "name";
    
    private List<NameRecord> nameRecords;

    private Map<Integer, Map<Integer, Map<Integer, Map<Integer, String>>>> lookupTable;

    private String fontFamily = null;
    private String fontSubFamily = null;
    private String psName = null;

    NamingTable(TrueTypeFont font)
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
        int formatSelector = data.readUnsignedShort();
        int numberOfNameRecords = data.readUnsignedShort();
        int offsetToStartOfStringStorage = data.readUnsignedShort();
        nameRecords = new ArrayList<NameRecord>(numberOfNameRecords);
        for (int i=0; i< numberOfNameRecords; i++)
        {
            NameRecord nr = new NameRecord();
            nr.initData(ttf, data);
            nameRecords.add(nr);
        }

        for (NameRecord nr : nameRecords)
        {
            // don't try to read invalid offsets, see PDFBOX-2608
            if (nr.getStringOffset() > getLength())
            {
                nr.setString(null);
                continue;
            }
            
            data.seek(getOffset() + (2*3)+numberOfNameRecords*2*6+nr.getStringOffset());
            int platform = nr.getPlatformId();
            int encoding = nr.getPlatformEncodingId();
            Charset charset = Charsets.ISO_8859_1;
            if (platform == NameRecord.PLATFORM_WINDOWS && (encoding == NameRecord.ENCODING_WINDOWS_SYMBOL || encoding == NameRecord.ENCODING_WINDOWS_UNICODE_BMP))
            {
                charset = Charsets.UTF_16;
            }
            else if (platform == NameRecord.PLATFORM_UNICODE)
            {
                charset = Charsets.UTF_16;
            }
            else if (platform == NameRecord.PLATFORM_ISO)
            {
                if (encoding == 0)
                {
                    charset = Charsets.US_ASCII;
                }
                else if (encoding == 1)
                {
                    //not sure is this is correct??
                    charset = Charsets.ISO_10646;
                }
                else if (encoding == 2)
                {
                    charset = Charsets.ISO_8859_1;
                }
            }
            String string = data.readString(nr.getStringLength(), charset);
            nr.setString(string);
        }

        // build multi-dimensional lookup table
        lookupTable = new HashMap<Integer, Map<Integer, Map<Integer, Map<Integer, String>>>>(nameRecords.size());
        for (NameRecord nr : nameRecords)
        {
            // name id
            Map<Integer, Map<Integer, Map<Integer, String>>> platformLookup = lookupTable.get(nr.getNameId());
            if (platformLookup == null)
            {
                platformLookup = new HashMap<Integer, Map<Integer, Map<Integer, String>>>(); 
                lookupTable.put(nr.getNameId(), platformLookup);
            }
            // platform id
            Map<Integer, Map<Integer, String>> encodingLookup = platformLookup.get(nr.getPlatformId());
            if (encodingLookup == null)
            {
                encodingLookup = new HashMap<Integer, Map<Integer, String>>();
                platformLookup.put(nr.getPlatformId(), encodingLookup);
            }
            // encoding id
            Map<Integer, String> languageLookup = encodingLookup.get(nr.getPlatformEncodingId());
            if (languageLookup == null)
            {
                languageLookup = new HashMap<Integer, String>();
                encodingLookup.put(nr.getPlatformEncodingId(), languageLookup);
            }
            // language id / string
            languageLookup.put(nr.getLanguageId(), nr.getString());
        }

        // extract strings of interest
        fontFamily = getEnglishName(NameRecord.NAME_FONT_FAMILY_NAME);
        fontSubFamily = getEnglishName(NameRecord.NAME_FONT_SUB_FAMILY_NAME);

        // extract PostScript name, only these two formats are valid
        psName = getName(NameRecord.NAME_POSTSCRIPT_NAME,
                         NameRecord.PLATFORM_MACINTOSH,
                         NameRecord.ENCODING_MACINTOSH_ROMAN,
                         NameRecord.LANGUGAE_MACINTOSH_ENGLISH);
        if (psName == null)
        {
            psName = getName(NameRecord.NAME_POSTSCRIPT_NAME,
                             NameRecord.PLATFORM_WINDOWS,
                             NameRecord.ENCODING_WINDOWS_UNICODE_BMP,
                             NameRecord.LANGUGAE_WINDOWS_EN_US);
        }
        if (psName != null)
        {
            psName = psName.trim();
        }

        initialized = true;
    }

    /**
     * Helper to get English names by best effort.
     */
    private String getEnglishName(int nameId)
    {
        // Unicode, Full, BMP, 1.1, 1.0
        for (int i = 4; i >= 0; i--)
        {
            String nameUni =
                    getName(nameId,
                            NameRecord.PLATFORM_UNICODE,
                            i,
                            NameRecord.LANGUGAE_UNICODE);
            if (nameUni != null)
            {
                return nameUni;
            }
        }

        // Windows, Unicode BMP, EN-US
        String nameWin =
                getName(nameId,
                        NameRecord.PLATFORM_WINDOWS,
                        NameRecord.ENCODING_WINDOWS_UNICODE_BMP,
                        NameRecord.LANGUGAE_WINDOWS_EN_US);
        if (nameWin != null)
        {
            return nameWin;
        }

        // Macintosh, Roman, English
        String nameMac =
                getName(nameId,
                        NameRecord.PLATFORM_MACINTOSH,
                        NameRecord.ENCODING_MACINTOSH_ROMAN,
                        NameRecord.LANGUGAE_MACINTOSH_ENGLISH);
        if (nameMac != null)
        {
            return nameMac;
        }

        return null;
    }

    /**
     * Returns a name from the table, or null it it does not exist.
     *
     * @param nameId Name ID from NameRecord constants.
     * @param platformId Platform ID from NameRecord constants.
     * @param encodingId Platform Encoding ID from NameRecord constants.
     * @param languageId Language ID from NameRecord constants.
     * @return name, or null
     */
    public String getName(int nameId, int platformId, int encodingId, int languageId)
    {
        Map<Integer, Map<Integer, Map<Integer, String>>> platforms = lookupTable.get(nameId);
        if (platforms == null)
        {
            return null;
        }
        Map<Integer, Map<Integer, String>> encodings = platforms.get(platformId);
        if (encodings == null)
        {
            return null;
        }
        Map<Integer, String> languages = encodings.get(encodingId);
        if (languages == null)
        {
            return null;
        }
        return languages.get(languageId);
    }

    /**
     * This will get the name records for this naming table.
     *
     * @return A list of NameRecord objects.
     */
    public List<NameRecord> getNameRecords()
    {
        return nameRecords;
    }
    
    /**
     * Returns the font family name, in English.
     *
     * @return the font family name, in English
     */
    public String getFontFamily()
    {
        return fontFamily;
    }

    /**
     * Returns the font sub family name, in English.
     *
     * @return the font sub family name, in English
     */
    public String getFontSubFamily()
    {
        return fontSubFamily;
    }

    /**
     * Returns the PostScript name.
     *
     * @return the PostScript name
     */
    public String getPostScriptName()
    {
        return psName;
    }
}
