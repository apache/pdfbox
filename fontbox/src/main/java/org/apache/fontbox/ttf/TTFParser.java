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
import java.io.InputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.pdfbox.io.RandomAccessRead;

/**
 * TrueType font file parser.
 * 
 * @author Ben Litchfield
 */
public class TTFParser
{
    private static final Logger LOG = LogManager.getLogger(TTFParser.class);

    private boolean isEmbedded = false;

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
        this.isEmbedded = isEmbedded;
    }

    /**
     * Parse a RandomAccessRead and return a TrueType font.
     *
     * @param randomAccessRead The RandomAccessREad to be read from. It will be closed before returning.
     * @return A TrueType font.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    public TrueTypeFont parse(RandomAccessRead randomAccessRead) throws IOException
    {
        RandomAccessReadDataStream dataStream = new RandomAccessReadDataStream(randomAccessRead);
        try (randomAccessRead)
        {
            return parse(dataStream);
        }
        catch (IOException ex)
        {
            // close only on error (source is still being accessed later)
            dataStream.close();
            throw ex;
        }
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
        RandomAccessReadDataStream dataStream = new RandomAccessReadDataStream(inputStream);
        try (inputStream)
        {
            return parse(dataStream);
        }
        catch (IOException ex)
        {
            // close only on error (source is still being accessed later)
            dataStream.close();
            throw ex;
        }
    }

    /**
     * Parse a RandomAccessRead and return a TrueType font.
     *
     * @param randomAccessRead The RandomAccessREad to be read from. It will be closed before returning.
     * @return TrueType font headers.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    public FontHeaders parseTableHeaders(RandomAccessRead randomAccessRead) throws IOException
    {
        try (TTFDataStream dataStream = new RandomAccessReadUnbufferedDataStream(randomAccessRead))
        {
            return parseTableHeaders(dataStream);
            // dataStream closes randomAccessRead
        }
    }

    /**
     * Parse a file and get a true type font.
     *
     * @param raf The TTF file.
     * @return A TrueType font.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    private TrueTypeFont createFontWithTables(TTFDataStream raf) throws IOException
    {
        TrueTypeFont font = newFont(raf);
        font.setVersion(raf.read32Fixed());
        int numberOfTables = raf.readUnsignedShort();
        int searchRange = raf.readUnsignedShort();
        int entrySelector = raf.readUnsignedShort();
        int rangeShift = raf.readUnsignedShort();
        for (int i = 0; i < numberOfTables; i++)
        {
            TTFTable table = readTableDirectory(raf);
            
            // skip tables with zero length
            if (table != null)
            {
                if (table.getOffset() + table.getLength() > font.getOriginalDataSize())
                {
                    // PDFBOX-5285 if we're lucky, this is an "unimportant" table, e.g. vmtx
                    LOG.warn(
                            "Skip table '{}' which goes past the file size; offset: {}, size: {}, font size: {}",
                            table.getTag(), table.getOffset(), table.getLength(),
                            font.getOriginalDataSize());
                }
                else
                {
                    font.addTable(table);
                }
            }
        }
        return font;
    }

    TrueTypeFont parse(TTFDataStream raf) throws IOException
    {
        TrueTypeFont font = createFontWithTables(raf);
        parseTables(font);
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

        boolean hasCFF = font.tables.containsKey(CFFTable.TAG);
        boolean isOTF = font instanceof OpenTypeFont;
        boolean isPostScript = isOTF ? ((OpenTypeFont) font).isPostScript() : hasCFF;
        
        HeaderTable head = font.getHeader();
        if (head == null)
        {
            throw new IOException("'head' table is mandatory");
        }

        HorizontalHeaderTable hh = font.getHorizontalHeader();
        if (hh == null)
        {
            throw new IOException("'hhea' table is mandatory");
        }

        MaximumProfileTable maxp = font.getMaximumProfile();
        if (maxp == null)
        {
            throw new IOException("'maxp' table is mandatory");
        }

        PostScriptTable post = font.getPostScript();
        if (post == null && !isEmbedded)
        {
            // in an embedded font this table is optional
            throw new IOException("'post' table is mandatory");
        }

        if (!isPostScript)
        {
            if (font.getIndexToLocation() == null)
            {
                throw new IOException("'loca' table is mandatory");
            }
            if (font.getGlyph() == null)
            {
                throw new IOException("'glyf' table is mandatory");
            }
        }
        else if (!isOTF)
        {
            throw new IOException("True Type fonts using CFF outlines are not supported");
        }
        
        if (font.getNaming() == null && !isEmbedded)
        {
            throw new IOException("'name' table is mandatory");
        }
        
        if (font.getHorizontalMetrics() == null)
        {
            throw new IOException("'hmtx' table is mandatory");
        }
        
        if (!isEmbedded && font.getCmap() == null)
        {
            throw new IOException("'cmap' table is mandatory");
        }
    }

    /**
     * Based on {@link #parseTables()}.
     * Parse all table headers and check if all needed tables are present.
     * 
     * This method can be optimized further by skipping unused portions inside each individual table parser
     *
     * @param font the TrueTypeFont instance holding the parsed data.
     * @throws IOException If there is an error parsing the TrueType font.
     */
    FontHeaders parseTableHeaders(TTFDataStream raf) throws IOException
    {
        FontHeaders outHeaders = new FontHeaders();
        try (TrueTypeFont font = createFontWithTables(raf))
        {
            font.readTableHeaders(NamingTable.TAG, outHeaders); // calls NamingTable.readHeaders();
            font.readTableHeaders(HeaderTable.TAG, outHeaders); // calls HeaderTable.readHeaders();

            // only these 5 are used
            //   sFamilyClass = os2WindowsMetricsTable.getFamilyClass();
            //   usWeightClass = os2WindowsMetricsTable.getWeightClass();
            //   ulCodePageRange1 = (int) os2WindowsMetricsTable.getCodePageRange1();
            //   ulCodePageRange2 = (int) os2WindowsMetricsTable.getCodePageRange2();
            //   panose = os2WindowsMetricsTable.getPanose();
            outHeaders.setOs2Windows(font.getOS2Windows());

            boolean isOTFAndPostScript;
            if (font instanceof OpenTypeFont && ((OpenTypeFont) font).isPostScript())
            {
                isOTFAndPostScript = true;
                if (((OpenTypeFont) font).isSupportedOTF())
                {
                    font.readTableHeaders(CFFTable.TAG, outHeaders); // calls CFFTable.readHeaders();
                }
            }
            else if (!(font instanceof OpenTypeFont) && font.tables.containsKey(CFFTable.TAG))
            {
                outHeaders.setError("True Type fonts using CFF outlines are not supported");
                return outHeaders;
            }
            else
            {
                isOTFAndPostScript = false;
                TTFTable gcid = font.getTableMap().get("gcid");
                if (gcid != null && gcid.getLength() >= FontHeaders.BYTES_GCID)
                {
                    outHeaders.setNonOtfGcid142(font.getTableNBytes(gcid, FontHeaders.BYTES_GCID));
                }
            }
            outHeaders.setIsOTFAndPostScript(isOTFAndPostScript);

            // list taken from parseTables(), detect them, but don't spend time parsing
            final String[] mandatoryTables = {
                HeaderTable.TAG,
                HorizontalHeaderTable.TAG,
                MaximumProfileTable.TAG,
                isEmbedded ? null : PostScriptTable.TAG, // in an embedded font this table is optional
                isOTFAndPostScript ? null : IndexToLocationTable.TAG,
                isOTFAndPostScript ? null : GlyphTable.TAG,
                isEmbedded ? null : NamingTable.TAG,
                HorizontalMetricsTable.TAG,
                isEmbedded ? null : CmapTable.TAG,
            };

            for (String tag : mandatoryTables)
            {
                if (tag != null && !font.tables.containsKey(tag))
                {
                    outHeaders.setError("'" + tag + "' table is mandatory");
                    return outHeaders;
                }
            }
        }
        return outHeaders;
    }

    protected boolean allowCFF()
    {
        return false;
    }

    private TTFTable readTableDirectory(TTFDataStream raf) throws IOException
    {
        TTFTable table;
        String tag = raf.readString(4);
        switch (tag)
        {
            case CmapTable.TAG:
                table = new CmapTable();
                break;
            case GlyphTable.TAG:
                table = new GlyphTable();
                break;
            case HeaderTable.TAG:
                table = new HeaderTable();
                break;
            case HorizontalHeaderTable.TAG:
                table = new HorizontalHeaderTable();
                break;
            case HorizontalMetricsTable.TAG:
                table = new HorizontalMetricsTable();
                break;
            case IndexToLocationTable.TAG:
                table = new IndexToLocationTable();
                break;
            case MaximumProfileTable.TAG:
                table = new MaximumProfileTable();
                break;
            case NamingTable.TAG:
                table = new NamingTable();
                break;
            case OS2WindowsMetricsTable.TAG:
                table = new OS2WindowsMetricsTable();
                break;
            case PostScriptTable.TAG:
                table = new PostScriptTable();
                break;
            case DigitalSignatureTable.TAG:
                table = new DigitalSignatureTable();
                break;
            case KerningTable.TAG:
                table = new KerningTable();
                break;
            case VerticalHeaderTable.TAG:
                table = new VerticalHeaderTable();
                break;
            case VerticalMetricsTable.TAG:
                table = new VerticalMetricsTable();
                break;
            case VerticalOriginTable.TAG:
                table = new VerticalOriginTable();
                break;
            case GlyphSubstitutionTable.TAG:
                table = new GlyphSubstitutionTable();
                break;
            default:
                table = readTable(tag);
                break;
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

    protected TTFTable readTable(String tag)
    {
        // unknown table type but read it anyway.
        return new TTFTable();
    }
}
