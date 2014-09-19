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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;

import org.apache.fontbox.encoding.Encoding;
import org.apache.fontbox.util.BoundingBox;

/**
 * A TrueType font file.
 * 
 * @author Ben Litchfield
 */
public class TrueTypeFont implements Type1Equivalent
{
    private float version;
    private int numberOfGlyphs = -1;
    private int unitsPerEm = -1;
    private int[] advanceWidths = null;
    protected Map<String,TTFTable> tables = new HashMap<String,TTFTable>();
    private TTFDataStream data;
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
    
    /**
     * Close the underlying resources.
     * 
     * @throws IOException If there is an error closing the resources.
     */
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
     * This will get the naming table for the true type font.
     * 
     * @return The naming table.
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
     */
    public int getAdvanceWidth(int gid) throws IOException
    {
        if (advanceWidths == null)
        {
            HorizontalMetricsTable hmtx = getHorizontalMetrics();
            if (hmtx != null)
            {
                advanceWidths = hmtx.getAdvanceWidth();
            }
            else
            {
                // this should never happen
                advanceWidths = new int[]{250};
            }
        }
        if (advanceWidths.length > gid)
        {
            return advanceWidths[gid];
        }
        else
        {
            // monospaced fonts may not have a width for every glyph
            // the last one is for subsequent glyphs
            return advanceWidths[advanceWidths.length-1];
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
     * Returns the GID for the given PostScript name, if the "post" table is present.
     */
    public int nameToGID(String name) throws IOException
    {
        readPostScriptNames();

        Integer gid = postScriptNames.get(name);
        if (gid == null || gid < 0 || gid >= getMaximumProfile().getNumGlyphs())
        {
            return 0;
        }
        return gid;
    }

    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        readPostScriptNames();

        int gid = nameToGID(name);
        if (gid < 0 || gid >= getMaximumProfile().getNumGlyphs())
        {
            gid = 0;
        }

        // some glyphs have no outlines (e.g. space, table, newline)
        GlyphData glyph = getGlyph().getGlyph(gid);
        if (glyph == null)
        {
            return new GeneralPath();
        }
        else
        {
            GeneralPath path = glyph.getPath();

            // scale to 1000upem, per PostScript convention
            float scale = 1000f / getUnitsPerEm();
            AffineTransform atScale = AffineTransform.getScaleInstance(scale, scale);
            path.transform(atScale);

            return path;
        }
    }

    @Override
    public float getWidth(String name) throws IOException
    {
        readPostScriptNames();

        Integer gid = nameToGID(name);

        int width = getAdvanceWidth(gid);
        int unitsPerEM = getUnitsPerEm();
        if (unitsPerEM != 1000)
        {
            width *= 1000f / unitsPerEM;
        }
        return width;
    }

    @Override
    public boolean hasGlyph(String name) throws IOException
    {
        readPostScriptNames();

        Integer gid = postScriptNames.get(name);
        return !(gid == null || gid < 0 || gid >= getMaximumProfile().getNumGlyphs());
    }

    @Override
    public Encoding getEncoding()
    {
        return null;
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
