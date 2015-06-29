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

import java.awt.geom.GeneralPath;
import java.io.IOException;

/**
 * An OpenType (OTF/TTF) font.
 */
public class OpenTypeFont extends TrueTypeFont
{
    private boolean isPostScript;
    
    /**
     * Constructor. Clients should use the OTFParser to create a new OpenTypeFont object.
     *
     * @param fontData The font data.
     */
    OpenTypeFont(TTFDataStream fontData)
    {
        super(fontData);
    }

    @Override
    void setVersion(float versionValue)
    {
        isPostScript = versionValue != 1.0;
        super.setVersion(versionValue);
    }
    
    /**
     * Get the "CFF" table for this OTF.
     *
     * @return The "CFF" table.
     */
    public synchronized CFFTable getCFF() throws IOException
    {
        if (!isPostScript)
        {
            throw new UnsupportedOperationException("TTF fonts do not have a CFF table");
        }
        CFFTable cff = (CFFTable)tables.get(CFFTable.TAG);
        if (cff != null && !cff.getInitialized())
        {
            readTable(cff);
        }
        return cff;
    }

    @Override
    public synchronized GlyphTable getGlyph() throws IOException
    {
        if (isPostScript)
        {
            throw new UnsupportedOperationException("OTF fonts do not have a glyf table");
        }
        return super.getGlyph();
    }

    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        int gid = nameToGID(name);
        return getCFF().getFont().getType2CharString(gid).getPath();
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
