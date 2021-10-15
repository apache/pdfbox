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

package org.apache.fontbox.ttf.advanced.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.fontbox.ttf.CFFTable;
import org.apache.fontbox.ttf.GlyphSubstitutionTable;
import org.apache.fontbox.ttf.GlyphTable;
import org.apache.fontbox.ttf.OTLTable;
import org.apache.fontbox.ttf.TTFDataStream;
import org.apache.fontbox.ttf.TTFTable;
import org.apache.fontbox.ttf.TrueTypeFont;

/**
 * OpenType font file parser.
 */
public final class AdvancedOTFParser extends org.apache.fontbox.ttf.OTFParser
{
    /**
     * Constructor.
     */
    public AdvancedOTFParser()
    {
        this(false);
    }

    /**
     * Constructor.
     *
     * @param isEmbedded true if the font is embedded in PDF
     */
    public AdvancedOTFParser(boolean isEmbedded)
    {
        this(isEmbedded, false);
    }

    /**
     *  Constructor.
     *
     * @param isEmbedded true if the font is embedded in PDF
     * @param parseOnDemand true if the tables of the font should be parsed on demand
     */
    public AdvancedOTFParser(boolean isEmbedded, boolean parseOnDemand)
    {
        super(isEmbedded, parseOnDemand);
    }

    @Override
    public AdvancedOpenTypeFont parse(String file) throws IOException
    {
        return (AdvancedOpenTypeFont) super.parse(file);
    }

    @Override
    public AdvancedOpenTypeFont parse(File file) throws IOException
    {
        return (AdvancedOpenTypeFont) super.parse(file);
    }

    @Override
    public AdvancedOpenTypeFont parse(InputStream data) throws IOException
    {
        return (AdvancedOpenTypeFont) super.parse(data);
    }

    @Override
    protected AdvancedOpenTypeFont parse(TTFDataStream raf) throws IOException
    {
        return (AdvancedOpenTypeFont) super.parse(raf);
    }
    
    @Override
    protected AdvancedOpenTypeFont newFont(TTFDataStream raf)
    {
        return new AdvancedOpenTypeFont(raf);
    }

    @Override
    protected TTFTable readTable(TrueTypeFont font, String tag)
    {
        assert font instanceof AdvancedOpenTypeFont;
        switch (tag)
        {
            case "BASE":
            case "JSTF":
                return new OTLTable(font);
            case "GDEF":
                return new org.apache.fontbox.ttf.advanced.GlyphDefinitionTable((AdvancedOpenTypeFont) font);
            case "GPOS":
                return new org.apache.fontbox.ttf.advanced.GlyphPositioningTable((AdvancedOpenTypeFont) font);
            case "GSUB":
                return new org.apache.fontbox.ttf.advanced.GlyphSubstitutionTable((AdvancedOpenTypeFont) font);
            case "CFF ":
                return new CFFTable(font);
            default:
                return super.readTable(font, tag);
        }
    }

    @Override
    protected TTFTable createTable(TrueTypeFont font, String tag) 
    {
        if (tag.equals(GlyphSubstitutionTable.TAG)) {
            // we need to special case GSUB table as TTFParser tries to handle it
            return null;
        }

        return super.createTable(font, tag);
    }

}
