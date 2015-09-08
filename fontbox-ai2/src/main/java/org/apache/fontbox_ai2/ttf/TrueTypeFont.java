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
package org.apache.fontbox_ai2.ttf;

import java.awt.geom.GeneralPath;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fontbox_ai2.FontBoxFont;
import org.apache.fontbox_ai2.util.BoundingBox;

/**
 * A TrueType font file.
 * 
 * @author Ben Litchfield
 */
public class TrueTypeFont implements FontBoxFont, Closeable
{
    private float version;
    private int numberOfGlyphs = -1;
    private int unitsPerEm = -1;
    protected Map<String,TTFTable> tables = new HashMap<String,TTFTable>();
    private final TTFDataStream data;
    private Map<String, Integer> postScriptNames;
    
    /**
     * Constructor.  Clients should use the TTFParser to create a new TrueTypeFont object.
     * 
     * @param fontData The font data.
     */
    TrueTypeFont(TTFDataStream fontData)
    {
        data = fontData;
    }
    
    @Override
    public void close() throws IOException
    {
        data.close();
    }

    /**
     * @return Returns the version.
     */
    public float getVersion() 
    {
        return version;
    }

    /**
     * Set the version. Package-private, used by TTFParser only.
     * @param versionValue The version to set.
     */
    void setVersion(float versionValue)
    {
        version = versionValue;
    }
    
    /**
     * Add a table definition. Package-private, used by TTFParser only.
     * 
     * @param table The table to add.
     */
    void addTable( TTFTable table )
    {
        tables.put( table.getTag(), table );
    }
    
    /**
     * Get all of the tables.
     * 
     * @return All of the tables.
     */
    public Collection<TTFTable> getTables()
    {
        return tables.values();
    }

    /**
     * Get all of the tables.
     *
     * @return All of the tables.
     */
    public Map<String, TTFTable> getTableMap()
    {
        return tables;
    }

    /**
     * Returns the war bytes of the given table.
     * @param table the table to read.
     * @throws IOException if there was an error accessing the table.
     */
    public synchronized byte[] getTableBytes(TTFTable table) throws IOException
    {
        // save current position
        long currentPosition = data.getCurrentPosition();
        data.seek(table.getOffset());

        // read all data
        byte[] bytes = data.read((int)table.getLength());

        // restore current position
        data.seek(currentPosition);
        return bytes;
    }
    
    /**
     * This will get the naming table for the true type font.
     * 
     * @return The naming table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized NamingTable getNaming() throws IOException
    {
        NamingTable naming = (NamingTable)tables.get( NamingTable.TAG );
        if (naming != null && !naming.getInitialized())
        {
            readTable(naming);
        }
        return naming;
    }
    
    /**
     * Get the postscript table for this TTF.
     * 
     * @return The postscript table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized PostScriptTable getPostScript() throws IOException
    {
        PostScriptTable postscript = (PostScriptTable)tables.get( PostScriptTable.TAG );
        if (postscript != null && !postscript.getInitialized())
        {
            readTable(postscript);
        }
        return postscript;
    }
    
    /**
     * Get the OS/2 table for this TTF.
     * 
     * @return The OS/2 table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized OS2WindowsMetricsTable getOS2Windows() throws IOException
    {
        OS2WindowsMetricsTable os2WindowsMetrics = (OS2WindowsMetricsTable)tables.get( OS2WindowsMetricsTable.TAG );
        if (os2WindowsMetrics != null && !os2WindowsMetrics.getInitialized())
        {
            readTable(os2WindowsMetrics);
        }
        return os2WindowsMetrics;
    }
    
    /**
     * Get the maxp table for this TTF.
     * 
     * @return The maxp table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized MaximumProfileTable getMaximumProfile() throws IOException
    {
        MaximumProfileTable maximumProfile = (MaximumProfileTable)tables.get( MaximumProfileTable.TAG );
        if (maximumProfile != null && !maximumProfile.getInitialized())
        {
            readTable(maximumProfile);
        }
        return maximumProfile;
    }
    
    /**
     * Get the head table for this TTF.
     * 
     * @return The head table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized HeaderTable getHeader() throws IOException
    {
        HeaderTable header = (HeaderTable)tables.get( HeaderTable.TAG );
        if (header != null && !header.getInitialized())
        {
            readTable(header);
        }
        return header;
    }
    
    /**
     * Get the hhea table for this TTF.
     * 
     * @return The hhea table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized HorizontalHeaderTable getHorizontalHeader() throws IOException
    {
        HorizontalHeaderTable horizontalHeader = (HorizontalHeaderTable)tables.get( HorizontalHeaderTable.TAG );
        if (horizontalHeader != null && !horizontalHeader.getInitialized())
        {
            readTable(horizontalHeader);
        }
        return horizontalHeader;
    }
    
    /**
     * Get the hmtx table for this TTF.
     * 
     * @return The hmtx table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized HorizontalMetricsTable getHorizontalMetrics() throws IOException
    {
        HorizontalMetricsTable horizontalMetrics = (HorizontalMetricsTable)tables.get( HorizontalMetricsTable.TAG );
        if (horizontalMetrics != null && !horizontalMetrics.getInitialized())
        {
            readTable(horizontalMetrics);
        }
        return horizontalMetrics;
    }
    
    /**
     * Get the loca table for this TTF.
     * 
     * @return The loca table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized IndexToLocationTable getIndexToLocation() throws IOException
    {
        IndexToLocationTable indexToLocation = (IndexToLocationTable)tables.get( IndexToLocationTable.TAG );
        if (indexToLocation != null && !indexToLocation.getInitialized())
        {
            readTable(indexToLocation);
        }
        return indexToLocation;
    }
    
    /**
     * Get the glyf table for this TTF.
     * 
     * @return The glyf table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized GlyphTable getGlyph() throws IOException
    {
        GlyphTable glyph = (GlyphTable)tables.get( GlyphTable.TAG );
        if (glyph != null && !glyph.getInitialized())
        {
            readTable(glyph);
        }
        return glyph;
    }
    
    /**
     * Get the "cmap" table for this TTF.
     * 
     * @return The "cmap" table.
    * @throws IOException if there was an error reading the table.
      */
    public synchronized CmapTable getCmap() throws IOException
    {
        CmapTable cmap = (CmapTable)tables.get( CmapTable.TAG );
        if (cmap != null && !cmap.getInitialized())
        {
            readTable(cmap);
        }
        return cmap;
    }
    
    /**
     * Get the vhea table for this TTF.
     * 
     * @return The vhea table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized VerticalHeaderTable getVerticalHeader() throws IOException
    {
        VerticalHeaderTable verticalHeader = (VerticalHeaderTable)tables.get( VerticalHeaderTable.TAG );
        if (verticalHeader != null && !verticalHeader.getInitialized())
        {
            readTable(verticalHeader);
        }
        return verticalHeader;
    }
    
    /**
     * Get the vmtx table for this TTF.
     * 
     * @return The vmtx table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized VerticalMetricsTable getVerticalMetrics() throws IOException
    {
        VerticalMetricsTable verticalMetrics = (VerticalMetricsTable)tables.get( VerticalMetricsTable.TAG );
        if (verticalMetrics != null && !verticalMetrics.getInitialized())
        {
            readTable(verticalMetrics);
        }
        return verticalMetrics;
    }
    
    /**
     * Get the VORG table for this TTF.
     * 
     * @return The VORG table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized VerticalOriginTable getVerticalOrigin() throws IOException
    {
        VerticalOriginTable verticalOrigin = (VerticalOriginTable)tables.get( VerticalOriginTable.TAG );
        if (verticalOrigin != null && !verticalOrigin.getInitialized())
        {
            readTable(verticalOrigin);
        }
        return verticalOrigin;
    }
    
    /**
     * Get the "kern" table for this TTF.
     * 
     * @return The "kern" table.
     * @throws IOException if there was an error reading the table.
     */
    public synchronized KerningTable getKerning() throws IOException
    {
        KerningTable kerning = (KerningTable)tables.get( KerningTable.TAG );
        if (kerning != null && !kerning.getInitialized())
        {
            readTable(kerning);
        }
        return kerning;
    }
    
    /**
     * This permit to get the data of the True Type Font
     * program representing the stream used to build this 
     * object (normally from the TTFParser object).
     * 
     * @return COSStream True type font program stream
     * 
     * @throws IOException If there is an error getting the font data.
     */
    public InputStream getOriginalData() throws IOException 
    {
       return data.getOriginalData(); 
    }
    
    /**
     * Read the given table if necessary. Package-private, used by TTFParser only.
     * 
     * @param table the table to be initialized
     * 
     * @throws IOException if there was an error reading the table.
     */
    void readTable(TTFTable table) throws IOException
    {
        // save current position
        long currentPosition = data.getCurrentPosition();
        data.seek(table.getOffset());
        table.read(this, data);
        // restore current position
        data.seek(currentPosition);
    }

    /**
     * Returns the number of glyphs (MaximuProfile.numGlyphs).
     * 
     * @return the number of glyphs
     * @throws IOException if there was an error reading the table.
     */
    public int getNumberOfGlyphs() throws IOException
    {
        if (numberOfGlyphs == -1)
        {
            MaximumProfileTable maximumProfile = getMaximumProfile();
            if (maximumProfile != null)
            {
                numberOfGlyphs = maximumProfile.getNumGlyphs();
            }
            else
            {
                // this should never happen
                numberOfGlyphs = 0;
            }
        }
        return numberOfGlyphs;
    }

    /**
     * Returns the units per EM (Header.unitsPerEm).
     * 
     * @return units per EM
     * @throws IOException if there was an error reading the table.
     */
    public int getUnitsPerEm() throws IOException
    {
        if (unitsPerEm == -1)
        {
            HeaderTable header = getHeader();
            if (header != null)
            {
                unitsPerEm = header.getUnitsPerEm();
            }
            else
            {
                // this should never happen
                unitsPerEm = 0;
            }
        }
        return unitsPerEm;
    }

    /**
     * Returns the width for the given GID.
     * 
     * @param gid the GID
     * @return the width
     * @throws IOException if there was an error reading the metrics table.
     */
    public int getAdvanceWidth(int gid) throws IOException
    {
        HorizontalMetricsTable hmtx = getHorizontalMetrics();
        if (hmtx != null)
        {
            return hmtx.getAdvanceWidth(gid);
        }
        else
        {
            // this should never happen
            return 250;
        }
    }

    /**
     * Returns the height for the given GID.
     * 
     * @param gid the GID
     * @return the height
     * @throws IOException if there was an error reading the metrics table.
     */
    public int getAdvanceHeight(int gid) throws IOException
    {
        VerticalMetricsTable vmtx = getVerticalMetrics();
        if (vmtx != null)
        {
            return vmtx.getAdvanceHeight(gid);
        }
        else
        {
            // this should never happen
            return 250;
        }
    }

    @Override
    public String getName() throws IOException
    {
        if (getNaming() != null)
        {
            return getNaming().getPostScriptName();
        }
        else
        {
            return null;
        }
    }

    private synchronized void readPostScriptNames() throws IOException
    {
        if (postScriptNames == null)
        {
            postScriptNames = new HashMap<String, Integer>();
            if (getPostScript() != null)
            {
                String[] names = getPostScript().getGlyphNames();
                if (names != null)
                {
                    for (int i = 0; i < names.length; i++)
                    {
                        postScriptNames.put(names[i], i);
                    }
                }
            }
        }
    }

    /**
     * Returns the best Unicode from the font (the most general). The PDF spec says that "The means
     * by which this is accomplished are implementation-dependent."
     * 
     * @throws IOException if the font could not be read
     */
    public CmapSubtable getUnicodeCmap() throws IOException
    {
        return getUnicodeCmap(true);
    }

    /**
     * Returns the best Unicode from the font (the most general). The PDF spec says that "The means
     * by which this is accomplished are implementation-dependent."
     * 
     * @param isStrict False if we allow falling back to any cmap, even if it's not Unicode.
     * @throws IOException if the font could not be read, or there is no Unicode cmap
     */
    public CmapSubtable getUnicodeCmap(boolean isStrict) throws IOException
    {
        CmapTable cmapTable = getCmap();
        if (cmapTable == null)
        {
            return null;
        }

        CmapSubtable cmap = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE,
                                                  CmapTable.ENCODING_UNICODE_2_0_FULL);
        if (cmap == null)
        {
            cmap = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE,
                                         CmapTable.ENCODING_UNICODE_2_0_BMP);
        }
        if (cmap == null)
        {
            cmap = cmapTable.getSubtable(CmapTable.PLATFORM_WINDOWS,
                                         CmapTable.ENCODING_WIN_UNICODE_BMP);
        }
        if (cmap == null)
        {
            // Microsoft's "Recommendations for OpenType Fonts" says that "Symbol" encoding
            // actually means "Unicode, non-standard character set"
            cmap = cmapTable.getSubtable(CmapTable.PLATFORM_WINDOWS,
                                         CmapTable.ENCODING_WIN_SYMBOL);
        }
        if (cmap == null)
        {
            if (isStrict)
            {
                throw new IOException("The TrueType font does not contain a Unicode cmap");
            }
            else
            {
                // fallback to the first cmap (may not be Unicode, so may produce poor results)
                cmap = cmapTable.getCmaps()[0];
            }
        }
        return cmap;
    }

    /**
     * Returns the GID for the given PostScript name, if the "post" table is present.
     * @param name the PostScript name.
     */
    public int nameToGID(String name) throws IOException
    {
        // look up in 'post' table
        readPostScriptNames();
        Integer gid = postScriptNames.get(name);
        if (gid != null && gid > 0 && gid < getMaximumProfile().getNumGlyphs())
        {
            return gid;
        }

        // look up in 'cmap'
        int uni = parseUniName(name);
        if (uni > -1)
        {
            CmapSubtable cmap = getUnicodeCmap(false);
            return cmap.getGlyphId(uni);
        }
        
        return 0;
    }

    /**
     * Parses a Unicode PostScript name in the format uniXXXX.
     */
    private int parseUniName(String name) throws IOException
    {
        if (name.startsWith("uni") && name.length() == 7)
        {
            int nameLength = name.length();
            StringBuilder uniStr = new StringBuilder();
            try
            {
                for (int chPos = 3; chPos + 4 <= nameLength; chPos += 4)
                {
                    int codePoint = Integer.parseInt(name.substring(chPos, chPos + 4), 16);
                    if (codePoint <= 0xD7FF || codePoint >= 0xE000) // disallowed code area
                    {
                        uniStr.append((char) codePoint);
                    }
                }
                String unicode = uniStr.toString();
                if (unicode.length() == 0)
                {
                    return -1;
                }
                return unicode.codePointAt(0);
            }
            catch (NumberFormatException e)
            {
                return -1;
            }
        }
        return -1;
    }
    
    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        int gid = nameToGID(name);

        // some glyphs have no outlines (e.g. space, table, newline)
        GlyphData glyph = getGlyph().getGlyph(gid);
        if (glyph == null)
        {
            return new GeneralPath();
        }
        else
        {
            // must scaled by caller using FontMatrix
            return glyph.getPath();
        }
    }

    @Override
    public float getWidth(String name) throws IOException
    {
        Integer gid = nameToGID(name);
        return getAdvanceWidth(gid);
    }

    @Override
    public boolean hasGlyph(String name) throws IOException
    {
        return nameToGID(name) != 0;
    }

    @Override
    public BoundingBox getFontBBox() throws IOException
    {
        short xMin = getHeader().getXMin();
        short xMax = getHeader().getXMax();
        short yMin = getHeader().getYMin();
        short yMax = getHeader().getYMax();
        float scale = 1000f / getUnitsPerEm();
        return new BoundingBox(xMin * scale, yMin * scale, xMax * scale, yMax * scale);
    }

    @Override
    public List<Number> getFontMatrix() throws IOException
    {
        float scale = 1000f / getUnitsPerEm();
        return Arrays.<Number>asList(0.001f * scale, 0, 0, 0.001f * scale, 0, 0);
    }

    @Override
    public String toString()
    {
        try
        {
            if (getNaming() != null)
            {
                return getNaming().getPostScriptName();
            }
            else
            {
                return "(null)";
            }
        }
        catch (IOException e)
        {
            return "(null - " + e.getMessage() + ")";
        }
    }
}
