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
public class OTFParser extends TTFParser
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
        super(isEmbedded, parseOnDemand);
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
    public OpenTypeFont parse(TTFDataStream raf) throws IOException
    {
        return (OpenTypeFont)super.parse(raf);
    }

    @Override
    protected OpenTypeFont newFont(TTFDataStream raf)
    {
        return new OpenTypeFont(raf);
    }

    @Override
    protected TTFTable readTable(String tag)
    {
        // todo: this is a stub, a full implementation is needed

        if (tag.equals("BASE") || tag.equals("GDEF") || tag.equals("GPOS") ||
            tag.equals("GSUB") || tag.equals("JSTF"))
        {
            return new OTLTable();
        }
        else if (tag.equals("CFF "))
        {
            return new CFFTable();
        }
        else
        {
            return super.readTable(tag);
        }
    }
}
