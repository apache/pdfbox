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

import org.apache.pdfbox.io.RandomAccessRead;

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
        super();
    }

    /**
     * Constructor.
     *
     * @param isEmbedded true if the font is embedded in PDF
     */
    public OTFParser(boolean isEmbedded)
    {
        super(isEmbedded);
    }

    @Override
    public OpenTypeFont parse(RandomAccessRead randomAccessRead) throws IOException
    {
        return (OpenTypeFont) super.parse(randomAccessRead);
    }

    @Override
    OpenTypeFont parse(TTFDataStream raf) throws IOException
    {
        return (OpenTypeFont)super.parse(raf);
    }
    
    @Override
    OpenTypeFont newFont(TTFDataStream raf)
    {
        return new OpenTypeFont(raf);
    }

    protected TTFTable readTable(String tag)
    {
        // todo: this is a stub, a full implementation is needed
        switch (tag)
        {
            case "BASE":
            case "GDEF":
            case "GPOS":
            case GlyphSubstitutionTable.TAG:
            case OTLTable.TAG:
                return new OTLTable();
            case CFFTable.TAG:
                return new CFFTable();
            default:
                return new TTFTable();
        }
    }

    @Override
    protected boolean allowCFF()
    {
        return true;
    }
}
