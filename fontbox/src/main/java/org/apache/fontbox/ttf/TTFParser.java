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
 * TrueType font file parser.
 * 
 * @author Ben Litchfield
 */
public class TTFParser
{
    private boolean isEmbedded = false;
    private boolean parseOnDemandOnly = false;

    /**
     * Constructor.
     */
    public TTFParser()
    {
        this(false);
    }

    /**
     * Constructor.
     *  
     * @param isEmbedded true if the font is embedded in PDF
     */
    public TTFParser(boolean isEmbedded)
    {
        this(isEmbedded, false);
    }

    /**
     *  Constructor.
     *  
     * @param isEmbedded true if the font is embedded in PDF
     * @param parseOnDemand true if the tables of the font should be parsed on demand
     */
    public TTFParser(boolean isEmbedded, boolean parseOnDemand)
    {
        this.isEmbedded = isEmbedded;
        parseOnDemandOnly = parseOnDemand;
    }

    /**
     * Parse a file and return a TrueType font.
     *
     * @param ttfFile The TrueType font filename.
     * @return A TrueType font.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    public TrueTypeFont parse(String ttfFile) throws IOException
    {
        return parse(new File(ttfFile));
    }

    /**
     * Parse a file and return a TrueType font.
     *
     * @param ttfFile The TrueType font file.
     * @return A TrueType font.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    public TrueTypeFont parse(File ttfFile) throws IOException
    {
        RAFDataStream raf = new RAFDataStream(ttfFile, "r");
        try
        {
            return parse(raf);
        }
        catch (IOException ex)
        {
            // close only on error (file is still being accessed later)
            raf.close();
            throw ex;
        }
    }

    /**
     * Parse an input stream and return a TrueType font.
     *
     * @param inputStream The TTF data stream to parse from. It will be closed before returning.
     * @return A TrueType font.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    public TrueTypeFont parse(InputStream inputStream) throws IOException
    {
        return parse(new MemoryTTFDataStream(inputStream));
    }

    /**
     * Parse an input stream and return a TrueType font that is to be embedded.
     *
     * @param inputStream The TTF data stream to parse from. It will be closed before returning.
     * @return A TrueType font.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    public TrueTypeFont parseEmbedded(InputStream inputStream) throws IOException
    {
        this.isEmbedded = true;
        return parse(new MemoryTTFDataStream(inputStream));
    }

    /**
     * Parse a file and get a true type font.
     *
     * @param raf The TTF file.
     * @return A TrueType font.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    TrueTypeFont parse(TTFDataStream raf) throws IOException
    {
        TrueTypeFont font = newFont(raf);
        font.setVersion(raf.read32Fixed());
        int numberOfTables = raf.readUnsignedShort();
        int searchRange = raf.readUnsignedShort();
        int entrySelector = raf.readUnsignedShort();
        int rangeShift = raf.readUnsignedShort();
        for (int i = 0; i < numberOfTables; i++)
        {
            TTFTable table = readTableDirectory(font, raf);
            
            // skip tables with zero length
            if (table != null)
            {
                font.addTable(table);
            }
        }
        // parse tables if wanted
        if (!parseOnDemandOnly)
        {
            parseTables(font);
        }

        return font;
    }

    TrueTypeFont newFont(TTFDataStream raf)
    {
        return new TrueTypeFont(raf);
    }

    /**
     * Parse all tables and check if all needed tables are present.
     *
     * @param font the TrueTypeFont instance holding the parsed data.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    private void parseTables(TrueTypeFont font) throws IOException
    {
        for (TTFTable table : font.getTables())
        {
            if (!table.getInitialized())
            {
                font.readTable(table);
            }
        }
        
        boolean isPostScript = allowCFF() && font.tables.containsKey(CFFTable.TAG);
        
        HeaderTable head = font.getHeader();
        if (head == null)
        {
            throw new IOException("head is mandatory");
        }

        HorizontalHeaderTable hh = font.getHorizontalHeader();
        if (hh == null)
        {
            throw new IOException("hhead is mandatory");
        }

        MaximumProfileTable maxp = font.getMaximumProfile();
        if (maxp == null)
        {
            throw new IOException("maxp is mandatory");
        }

        PostScriptTable post = font.getPostScript();
        if (post == null && !isEmbedded)
        {
            // in an embedded font this table is optional
            throw new IOException("post is mandatory");
        }

        if (!isPostScript)
        {
            IndexToLocationTable loc = font.getIndexToLocation();
            if (loc == null)
            {
                throw new IOException("loca is mandatory");
            }

            if (font.getGlyph() == null)
            {
                throw new IOException("glyf is mandatory");
            }
        }

        if (font.getNaming() == null && !isEmbedded)
        {
            throw new IOException("name is mandatory");
        }

        if (font.getHorizontalMetrics() == null)
        {
            throw new IOException("hmtx is mandatory");
        }
        
        if (!isEmbedded && font.getCmap() == null)
        {
            throw new IOException("cmap is mandatory");
        }
    }

    protected boolean allowCFF()
    {
        return false;
    }
    
    private TTFTable readTableDirectory(TrueTypeFont font, TTFDataStream raf) throws IOException
    {
        TTFTable table;
        String tag = raf.readString(4);
        if (tag.equals(CmapTable.TAG))
        {
            table = new CmapTable(font);
        }
        else if (tag.equals(GlyphTable.TAG))
        {
            table = new GlyphTable(font);
        }
        else if (tag.equals(HeaderTable.TAG))
        {
            table = new HeaderTable(font);
        }
        else if (tag.equals(HorizontalHeaderTable.TAG))
        {
            table = new HorizontalHeaderTable(font);
        }
        else if (tag.equals(HorizontalMetricsTable.TAG))
        {
            table = new HorizontalMetricsTable(font);
        }
        else if (tag.equals(IndexToLocationTable.TAG))
        {
            table = new IndexToLocationTable(font);
        }
        else if (tag.equals(MaximumProfileTable.TAG))
        {
            table = new MaximumProfileTable(font);
        }
        else if (tag.equals(NamingTable.TAG))
        {
            table = new NamingTable(font);
        }
        else if (tag.equals(OS2WindowsMetricsTable.TAG))
        {
            table = new OS2WindowsMetricsTable(font);
        }
        else if (tag.equals(PostScriptTable.TAG))
        {
            table = new PostScriptTable(font);
        }
        else if (tag.equals(DigitalSignatureTable.TAG))
        {
            table = new DigitalSignatureTable(font);
        }
        else if (tag.equals(KerningTable.TAG))
        {
            table = new KerningTable(font);
        }
        else if (tag.equals(VerticalHeaderTable.TAG))
        {
            table = new VerticalHeaderTable(font);
        }
        else if (tag.equals(VerticalMetricsTable.TAG))
        {
            table = new VerticalMetricsTable(font);
        }
        else if (tag.equals(VerticalOriginTable.TAG))
        {
            table = new VerticalOriginTable(font);
        }
        else if (tag.equals(GlyphSubstitutionTable.TAG))
        {
            table = new GlyphSubstitutionTable(font);
        }
        else
        {
            table = readTable(font, tag);
        }
        table.setTag(tag);
        table.setCheckSum(raf.readUnsignedInt());
        table.setOffset(raf.readUnsignedInt());
        table.setLength(raf.readUnsignedInt());
        
        // skip tables with zero length (except glyf)
        if (table.getLength() == 0 && !tag.equals(GlyphTable.TAG))
        {
            return null;
        }

        return table;
    }

    protected TTFTable readTable(TrueTypeFont font, String tag)
    {
        // unknown table type but read it anyway.
        return new TTFTable(font);
    }
}
