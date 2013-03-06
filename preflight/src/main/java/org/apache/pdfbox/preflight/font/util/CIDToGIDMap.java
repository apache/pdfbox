/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.font.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.cos.COSStream;

public class CIDToGIDMap
{

    public final int NOTDEF_GLYPH_INDEX = 0;
    private byte[] map = null;

    /**
     * Copy the unfiltered stream content in a byte array.
     * 
     * @param stream
     * @throws IOException
     *             if the stream can't be copied
     */
    public void parseStream(COSStream stream) throws IOException
    {
        InputStream is = stream.getUnfilteredStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try
        {
            IOUtils.copy(stream.getUnfilteredStream(), os);
            map = os.toByteArray();
        }
        catch (IOException e)
        {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    /**
     * Return the glyph index according to the CID.
     * 
     * @param cid
     * @return
     */
    public int getGID(int cid)
    {
        if (map == null || (cid * 2 + 1) >= map.length)
        {
            return NOTDEF_GLYPH_INDEX;
        }
        int index = cid * 2;
        return ((map[index] & 0xFF) << 8) ^ (map[index + 1] & 0xFF);
    }
}
