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
 * An OpenType font.
 */
public class OpenTypeFont extends TrueTypeFont
{
    /**
     * Constructor. Clients should use the OTFParser to create a new OpenTypeFont object.
     *
     * @param fontData The font data.
     */
    OpenTypeFont(TTFDataStream fontData)
    {
        super(fontData);
    }

    /**
     * Get the "cmap" table for this TTF.
     *
     * @return The "cmap" table.
     */
    public synchronized CFFTable getCFF() throws IOException
    {
        CFFTable cmap = (CFFTable)tables.get(CFFTable.TAG);
        if (cmap != null && !cmap.getInitialized())
        {
            readTable(cmap);
        }
        return cmap;
    }

    /**
     * Returns true if this font is a PostScript outline font.
     */
    public boolean isPostScript()
    {
        return tables.containsKey(CFFTable.TAG);
    }

    /**
     * Returns true if this font uses OpenType Layout (Advanced Typographic) tables.
     */
    public boolean hasLayoutTables()
    {
        return tables.containsKey("BASE") ||
               tables.containsKey("GDEF") ||
               tables.containsKey("GPOS") ||
               tables.containsKey("GSUB") ||
               tables.containsKey("JSTF");
    }
}
