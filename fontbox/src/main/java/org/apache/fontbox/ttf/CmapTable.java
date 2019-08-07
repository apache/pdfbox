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
 * The "cmap" table of a true type font.
 * 
 * @author Ben Litchfield
 */
public class CmapTable extends TTFTable
{
    /**
     * A tag used to identify this table.
     */
    public static final String TAG = "cmap";

    // platform
    public static final int PLATFORM_UNICODE = 0;
    public static final int PLATFORM_MACINTOSH = 1;
    public static final int PLATFORM_WINDOWS = 3;

    // Mac encodings
    public static final int ENCODING_MAC_ROMAN = 0;

    // Windows encodings
    public static final int ENCODING_WIN_SYMBOL = 0; // Unicode, non-standard character set
    public static final int ENCODING_WIN_UNICODE_BMP = 1; // Unicode BMP (UCS-2)
    public static final int ENCODING_WIN_SHIFT_JIS = 2;
    public static final int ENCODING_WIN_BIG5 = 3;
    public static final int ENCODING_WIN_PRC = 4;
    public static final int ENCODING_WIN_WANSUNG = 5;
    public static final int ENCODING_WIN_JOHAB = 6;
    public static final int ENCODING_WIN_UNICODE_FULL = 10; // Unicode Full (UCS-4)

    // Unicode encodings
    public static final int ENCODING_UNICODE_1_0 = 0;
    public static final int ENCODING_UNICODE_1_1 = 1;
    public static final int ENCODING_UNICODE_2_0_BMP = 3;
    public static final int ENCODING_UNICODE_2_0_FULL = 4;

    private CmapSubtable[] cmaps;

    CmapTable(TrueTypeFont font)
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
    void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        @SuppressWarnings({"unused", "squid:S1854", "squid:S1481"})
        int version = data.readUnsignedShort();
        int numberOfTables = data.readUnsignedShort();
        cmaps = new CmapSubtable[numberOfTables];
        for (int i = 0; i < numberOfTables; i++)
        {
            CmapSubtable cmap = new CmapSubtable();
            cmap.initData(data);
            cmaps[i] = cmap;
        }
        for (int i = 0; i < numberOfTables; i++)
        {
            cmaps[i].initSubtable(this, ttf.getNumberOfGlyphs(), data);
        }
        initialized = true;
    }

    /**
     * @return Returns the cmaps.
     */
    public CmapSubtable[] getCmaps()
    {
        return cmaps;
    }

    /**
     * @param cmapsValue The cmaps to set.
     */
    public void setCmaps(CmapSubtable[] cmapsValue)
    {
        cmaps = cmapsValue;
    }

    /**
     * Returns the subtable, if any, for the given platform and encoding.
     */
    public CmapSubtable getSubtable(int platformId, int platformEncodingId)
    {
        for (CmapSubtable cmap : cmaps)
        {
            if (cmap.getPlatformId() == platformId &&
                cmap.getPlatformEncodingId() == platformEncodingId)
            {
                return cmap;
            }
        }
        return null;
    }
}
