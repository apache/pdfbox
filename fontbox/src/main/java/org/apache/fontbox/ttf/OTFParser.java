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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * OpenType font file parser.
 */
public final class OTFParser extends TTFParser
{
    /**
     * Constructor.
     */
    public OTFParser()
    {
        this(false);
    }

    /**
     * Constructor.
     *
     * @param isEmbedded true if the font is embedded in PDF
     */
    public OTFParser(boolean isEmbedded)
    {
        this(isEmbedded, false);
    }

    /**
     *  Constructor.
     *
     * @param isEmbedded true if the font is embedded in PDF
     * @param parseOnDemand true if the tables of the font should be parsed on demand
     */
    public OTFParser(boolean isEmbedded, boolean parseOnDemand)
    {
        this(isEmbedded, parseOnDemand, false);
    }

    /**
     *  Constructor.
     *  
     * @param isEmbedded true if the font is embedded in PDF
     * @param parseOnDemand true if the tables of the font should be parsed on demand
     * @param useAlternateATT true if using alternate ATT (advanced typograph tables) implementation
     */
    public OTFParser(boolean isEmbedded, boolean parseOnDemand, boolean useAlternateATT)
    {
        super(isEmbedded, parseOnDemand, useAlternateATT);
    }

    @Override
    public OpenTypeFont parse(String file) throws IOException
    {
        return (OpenTypeFont)super.parse(file);
    }

    @Override
    public OpenTypeFont parse(File file) throws IOException
    {
        return (OpenTypeFont)super.parse(file);
    }

    @Override
    public OpenTypeFont parse(InputStream data) throws IOException
    {
        return (OpenTypeFont)super.parse(data);
    }

    @Override
    OpenTypeFont parse(TTFDataStream raf) throws IOException
    {
        return (OpenTypeFont)super.parse(raf);
    }
    
    @Override
    OpenTypeFont newFont(TTFDataStream raf)
    {
        return new OpenTypeFont(raf, useAlternateATT);
    }

    @Override
    protected TTFTable readTable(TrueTypeFont font, String tag)
    {
        // todo: this is a stub, a full implementation is needed
        assert font instanceof OpenTypeFont;
        switch (tag)
        {
            case "BASE":
            case "JSTF":
                return new OTLTable(font);
            case "GDEF":
                return useAlternateATT ? new org.apache.fontbox.ttf.advanced.GlyphDefinitionTable((OpenTypeFont) font) : super.readTable(font, tag);
            case "GPOS":
                return useAlternateATT ? new org.apache.fontbox.ttf.advanced.GlyphPositioningTable((OpenTypeFont) font) : super.readTable(font, tag);
            case "GSUB":
                return useAlternateATT ? new org.apache.fontbox.ttf.advanced.GlyphSubstitutionTable((OpenTypeFont) font) : super.readTable(font, tag);
            case "CFF ":
                return new CFFTable(font);
            default:
                return super.readTable(font, tag);
        }
    }

    @Override
    protected boolean allowCFF()
    {
        return true;
    }
}
