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
 * The CMAP table of a true type font.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * 
 */
public class CMAPTable extends TTFTable
{
    /**
     * A tag used to identify this table.
     */
    public static final String TAG = "cmap";

    /**
     * A constant for the platform.
     */
    public static final int PLATFORM_MISC = 0;

    /**
     * A constant for the platform.
     */
    public static final int PLATFORM_MACINTOSH = 1;

    /**
     * A constant for the platform.
     */
    public static final int PLATFORM_WINDOWS = 3;

    /**
     * An encoding constant.
     */
    public static final int ENCODING_SYMBOL = 0;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_UNICODE = 1;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_SHIFT_JIS = 2;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_BIG5 = 3;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_PRC = 4;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_WANSUNG = 5;
    /**
     * An encoding constant.
     */
    public static final int ENCODING_JOHAB = 6;

    private CMAPEncodingEntry[] cmaps;

    /**
     * This will read the required data from the stream.
     * 
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    public void initData(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        int version = data.readUnsignedShort();
        int numberOfTables = data.readUnsignedShort();
        cmaps = new CMAPEncodingEntry[numberOfTables];
        for (int i = 0; i < numberOfTables; i++)
        {
            CMAPEncodingEntry cmap = new CMAPEncodingEntry();
            cmap.initData(ttf, data);
            cmaps[i] = cmap;
        }
        for (int i = 0; i < numberOfTables; i++)
        {
            cmaps[i].initSubtable(ttf, data);
        }

    }

    /**
     * @return Returns the cmaps.
     */
    public CMAPEncodingEntry[] getCmaps()
    {
        return cmaps;
    }

    /**
     * @param cmapsValue The cmaps to set.
     */
    public void setCmaps(CMAPEncodingEntry[] cmapsValue)
    {
        cmaps = cmapsValue;
    }
}
