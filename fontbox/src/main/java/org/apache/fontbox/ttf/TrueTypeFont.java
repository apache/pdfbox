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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.util.BoundingBox;

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
    private final List<String> enabledGsubFeatures = new ArrayList<String>();

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

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        // PDFBOX-4963: risk of memory leaks due to SoftReference in FontCache 
        close();
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
     * Returns the raw bytes of the given table.
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
     * This will get the table for the given tag.
     * 
     * @param tag the name of the table to be returned
     * @return The table with the given tag.
     * @throws IOException if there was an error reading the table.
     */
    protected synchronized TTFTable getTable(String tag) throws IOException
    {
        TTFTable ttfTable = tables.get(tag);
        if (ttfTable != null && !ttfTable.getInitialized())
        {
            readTable(ttfTable);
        }
        return ttfTable;
    }

    /**
     * This will get the naming table for the true type font.
     * 
     * @return The naming table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public NamingTable getNaming() throws IOException
    {
        return (NamingTable) getTable(NamingTable.TAG);
    }
    
    /**
     * Get the postscript table for this TTF.
     * 
     * @return The postscript table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public PostScriptTable getPostScript() throws IOException
    {
        return (PostScriptTable) getTable(PostScriptTable.TAG);
    }
    
    /**
     * Get the OS/2 table for this TTF.
     * 
     * @return The OS/2 table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public OS2WindowsMetricsTable getOS2Windows() throws IOException
    {
        return (OS2WindowsMetricsTable) getTable(OS2WindowsMetricsTable.TAG);
    }

    /**
     * Get the maxp table for this TTF.
     * 
     * @return The maxp table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public MaximumProfileTable getMaximumProfile() throws IOException
    {
        return (MaximumProfileTable) getTable(MaximumProfileTable.TAG);
    }
    
    /**
     * Get the head table for this TTF.
     * 
     * @return The head table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public HeaderTable getHeader() throws IOException
    {
        return (HeaderTable) getTable(HeaderTable.TAG);
    }
    
    /**
     * Get the hhea table for this TTF.
     * 
     * @return The hhea table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public HorizontalHeaderTable getHorizontalHeader() throws IOException
    {
        return (HorizontalHeaderTable) getTable(HorizontalHeaderTable.TAG);
    }
    
    /**
     * Get the hmtx table for this TTF.
     * 
     * @return The hmtx table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public HorizontalMetricsTable getHorizontalMetrics() throws IOException
    {
        return (HorizontalMetricsTable) getTable(HorizontalMetricsTable.TAG);
    }
    
    /**
     * Get the loca table for this TTF.
     * 
     * @return The loca table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public IndexToLocationTable getIndexToLocation() throws IOException
    {
        return (IndexToLocationTable) getTable(IndexToLocationTable.TAG);
    }
    
    /**
     * Get the glyf table for this TTF.
     * 
     * @return The glyf table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public GlyphTable getGlyph() throws IOException
    {
        return (GlyphTable) getTable(GlyphTable.TAG);
    }
    
    /**
     * Get the "cmap" table for this TTF.
     * 
     * @return The "cmap" table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public CmapTable getCmap() throws IOException
    {
        return (CmapTable) getTable(CmapTable.TAG);
    }
    
    /**
     * Get the vhea table for this TTF.
     * 
     * @return The vhea table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public VerticalHeaderTable getVerticalHeader() throws IOException
    {
        return (VerticalHeaderTable) getTable(VerticalHeaderTable.TAG);
    }
    
    /**
     * Get the vmtx table for this TTF.
     * 
     * @return The vmtx table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public VerticalMetricsTable getVerticalMetrics() throws IOException
    {
        return (VerticalMetricsTable) getTable(VerticalMetricsTable.TAG);
    }
    
    /**
     * Get the VORG table for this TTF.
     * 
     * @return The VORG table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public VerticalOriginTable getVerticalOrigin() throws IOException
    {
        return (VerticalOriginTable) getTable(VerticalOriginTable.TAG);
    }
    
    /**
     * Get the "kern" table for this TTF.
     * 
     * @return The "kern" table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public KerningTable getKerning() throws IOException
    {
        return (KerningTable) getTable(KerningTable.TAG);
    }

    /**
     * Get the "gsub" table for this TTF.
     *
     * @return The "gsub" table or null if it doesn't exist.
     * @throws IOException if there was an error reading the table.
     */
    public GlyphSubstitutionTable getGsub() throws IOException
    {
        return (GlyphSubstitutionTable) getTable(GlyphSubstitutionTable.TAG);
    }

    /**
     * Get the data of the TrueType Font
     * program representing the stream used to build this 
     * object (normally from the TTFParser object).
     * 
     * @return COSStream TrueType font program stream
     * 
     * @throws IOException If there is an error getting the font data.
     */
    public InputStream getOriginalData() throws IOException 
    {
       return data.getOriginalData(); 
    }

    /**
     * Get the data size of the TrueType Font program representing the stream used to build this
     * object (normally from the TTFParser object).
     * 
     * @return the size.
     */
    public long getOriginalDataSize()
    {
       return data.getOriginalDataSize(); 
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
        // PDFBOX-4219: synchronize on data because it is accessed by several threads
        // when PDFBox is accessing a standard 14 font for the first time
        synchronized (data)
        {
            // save current position
            long currentPosition = data.getCurrentPosition();
            data.seek(table.getOffset());
            table.read(this, data);
            // restore current position
            data.seek(currentPosition);
        }
    }

    /**
     * Returns the number of glyphs (MaximumProfile.numGlyphs).
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
        if (postScriptNames == null && getPostScript() != null)
        {
            String[] names = getPostScript().getGlyphNames();
            if (names != null)
            {
                postScriptNames = new HashMap<String, Integer>(names.length);
                for (int i = 0; i < names.length; i++)
                {
                    postScriptNames.put(names[i], i);
                }
            }
            else
            {
                postScriptNames = new HashMap<String, Integer>();
            }                    
        }
    }

    /**
     * Returns the best Unicode from the font (the most general). The PDF spec says that "The means
     * by which this is accomplished are implementation-dependent."
     * 
     * @throws IOException if the font could not be read
     * @deprecated Use {@link #getUnicodeCmapLookup()} instead
     */
    @Deprecated
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
     * @deprecated Use {@link #getUnicodeCmapLookup(boolean)} instead
     */
    @Deprecated
    public CmapSubtable getUnicodeCmap(boolean isStrict) throws IOException
    {
        return getUnicodeCmapImpl(isStrict);
    }

    /**
     * Returns the best Unicode from the font (the most general). The PDF spec says that "The means
     * by which this is accomplished are implementation-dependent."
     *
     * The returned cmap will perform glyph substitution.
     *
     * @throws IOException if the font could not be read
     */
    public CmapLookup getUnicodeCmapLookup() throws IOException
    {
        return getUnicodeCmapLookup(true);
    }

    /**
     * Returns the best Unicode from the font (the most general). The PDF spec says that "The means
     * by which this is accomplished are implementation-dependent."
     *
     * The returned cmap will perform glyph substitution.
     *
     * @param isStrict False if we allow falling back to any cmap, even if it's not Unicode.
     * @throws IOException if the font could not be read, or there is no Unicode cmap
     */
    public CmapLookup getUnicodeCmapLookup(boolean isStrict) throws IOException
    {
        CmapSubtable cmap = getUnicodeCmapImpl(isStrict);
        if (!enabledGsubFeatures.isEmpty())
        {
            GlyphSubstitutionTable table = getGsub();
            if (table != null)
            {
                return new SubstitutingCmapLookup(cmap, (GlyphSubstitutionTable) table,
                        Collections.unmodifiableList(enabledGsubFeatures));
            }
        }
        return cmap;
    }

    private CmapSubtable getUnicodeCmapImpl(boolean isStrict) throws IOException
    {
        CmapTable cmapTable = getCmap();
        if (cmapTable == null)
        {
            if (isStrict)
            {
                throw new IOException("The TrueType font " + getName() + " does not contain a 'cmap' table");
            }
            else
            {
                return null;
            }
        }

        CmapSubtable cmap = cmapTable.getSubtable(CmapTable.PLATFORM_UNICODE,
                                                  CmapTable.ENCODING_UNICODE_2_0_FULL);
        if (cmap == null)
        {
            cmap = cmapTable.getSubtable(CmapTable.PLATFORM_WINDOWS,
                                         CmapTable.ENCODING_WIN_UNICODE_FULL);
        }
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
            else if (cmapTable.getCmaps().length > 0)
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
        if (postScriptNames != null)
        {
            Integer gid = postScriptNames.get(name);
            if (gid != null && gid > 0 && gid < getMaximumProfile().getNumGlyphs())
            {
                return gid;
            }
        }

        // look up in 'cmap'
        int uni = parseUniName(name);
        if (uni > -1)
        {
            CmapLookup cmap = getUnicodeCmapLookup(false);
            return cmap.getGlyphId(uni);
        }
        
        return 0;
    }

    /**
     * Parses a Unicode PostScript name in the format uniXXXX.
     */
    private int parseUniName(String name)
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
        int gid = nameToGID(name);
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
        HeaderTable headerTable = getHeader();
        short xMin = headerTable.getXMin();
        short xMax = headerTable.getXMax();
        short yMin = headerTable.getYMin();
        short yMax = headerTable.getYMax();
        float scale = 1000f / getUnitsPerEm();
        return new BoundingBox(xMin * scale, yMin * scale, xMax * scale, yMax * scale);
    }

    @Override
    public List<Number> getFontMatrix() throws IOException
    {
        float scale = 1000f / getUnitsPerEm();
        return Arrays.<Number>asList(0.001f * scale, 0, 0, 0.001f * scale, 0, 0);
    }

    /**
     * Enable a particular glyph substitution feature. This feature might not be supported by the
     * font, or might not be implemented in PDFBox yet.
     *
     * @param featureTag The GSUB feature to enable
     */
    public void enableGsubFeature(String featureTag)
    {
        enabledGsubFeatures.add(featureTag);
    }

    /**
     * Disable a particular glyph substitution feature.
     *
     * @param featureTag The GSUB feature to disable
     */
    public void disableGsubFeature(String featureTag)
    {
        enabledGsubFeatures.remove(featureTag);
    }

    /**
     * Enable glyph substitutions for vertical writing.
     */
    public void enableVerticalSubstitutions()
    {
        enableGsubFeature("vrt2");
        enableGsubFeature("vert");
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
