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
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;

/**
 * PostScript font program (compact font format).
 */
public class CFFTable extends TTFTable
{
    /**
     * A tag that identifies this table type.
     */
    public static final String TAG = "CFF ";

    private CFFFont cffFont;

    CFFTable(TrueTypeFont font)
    {
        super(font);
    }

    /**
     * This will read the required data from the stream.
     *
     * @param ttf The font that is being read.
     * @param data The stream to read the data from.
     * @throws java.io.IOException If there is an error reading the data.
     */
    void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        byte[] bytes = data.read((int)getLength());

        CFFParser parser = new CFFParser();
        cffFont = parser.parse(bytes, new CFFBytesource(font)).get(0);

        initialized = true;
    }

    /**
     * Returns the CFF font, which is a compact representation of a PostScript Type 1, or CIDFont
     */
    public CFFFont getFont()
    {
        return cffFont;
    }
    
    /**
     * Allows bytes to be re-read later by CFFParser.
     */
    private static class CFFBytesource implements CFFParser.ByteSource
    {
        private final TrueTypeFont ttf;
        
        CFFBytesource(TrueTypeFont ttf)
        {
           this.ttf = ttf; 
        }
        
        @Override
        public byte[] getBytes() throws IOException
        {
            return ttf.getTableBytes(ttf.getTableMap().get(CFFTable.TAG));
        }
    }
}
