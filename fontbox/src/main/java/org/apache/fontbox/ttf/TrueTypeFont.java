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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A class to hold true type font information.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 */
public class TrueTypeFont 
{
    private final Log log = LogFactory.getLog(TrueTypeFont.class);

    private float version; 

    private int numberOfGlyphs = -1;
    
    private int unitsPerEm = -1;

    private int[] advanceWidths = null;
    
    private Map<String,TTFTable> tables = new HashMap<String,TTFTable>();
    
    private TTFDataStream data;
    
    /**
     * Constructor.  Clients should use the TTFParser to create a new TrueTypeFont object.
     * 
     * @param fontData The font data.
     */
    TrueTypeFont( TTFDataStream fontData )
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
     * @param versionValue The version to set.
     */
    public void setVersion(float versionValue) 
    {
        version = versionValue;
    }
    
    /**
     * Add a table definition.
     * 
     * @param table The table to add.
     */
    public void addTable( TTFTable table )
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
     * This will get the naming table for the true type font.
     * 
     * @return The naming table.
     */
    public NamingTable getNaming()
    {
        NamingTable naming = (NamingTable)tables.get( NamingTable.TAG );
        if (naming != null && !naming.getInitialized())
        {
            initializeTable(naming);
        }
        return naming;
    }
    
    /**
     * Get the postscript table for this TTF.
     * 
     * @return The postscript table.
     */
    public PostScriptTable getPostScript()
    {
        PostScriptTable postscript = (PostScriptTable)tables.get( PostScriptTable.TAG );
        if (postscript != null && !postscript.getInitialized())
        {
            initializeTable(postscript);
        }
        return postscript;
    }
    
    /**
     * Get the OS/2 table for this TTF.
     * 
     * @return The OS/2 table.
     */
    public OS2WindowsMetricsTable getOS2Windows()
    {
        OS2WindowsMetricsTable os2WindowsMetrics = (OS2WindowsMetricsTable)tables.get( OS2WindowsMetricsTable.TAG );
        if (os2WindowsMetrics != null && !os2WindowsMetrics.getInitialized())
        {
            initializeTable(os2WindowsMetrics);
        }
        return os2WindowsMetrics;
    }
    
    /**
     * Get the maxp table for this TTF.
     * 
     * @return The maxp table.
     */
    public MaximumProfileTable getMaximumProfile()
    {
        MaximumProfileTable maximumProfile = (MaximumProfileTable)tables.get( MaximumProfileTable.TAG );
        if (maximumProfile != null && !maximumProfile.getInitialized())
        {
            initializeTable(maximumProfile);
        }
        return maximumProfile;
    }
    
    /**
     * Get the head table for this TTF.
     * 
     * @return The head table.
     */
    public HeaderTable getHeader()
    {
        HeaderTable header = (HeaderTable)tables.get( HeaderTable.TAG );
        if (header != null && !header.getInitialized())
        {
            initializeTable(header);
        }
        return header;
    }
    
    /**
     * Get the hhea table for this TTF.
     * 
     * @return The hhea table.
     */
    public HorizontalHeaderTable getHorizontalHeader()
    {
        HorizontalHeaderTable horizontalHeader = (HorizontalHeaderTable)tables.get( HorizontalHeaderTable.TAG );
        if (horizontalHeader != null && !horizontalHeader.getInitialized())
        {
            initializeTable(horizontalHeader);
        }
        return horizontalHeader;
    }
    
    /**
     * Get the hmtx table for this TTF.
     * 
     * @return The hmtx table.
     */
    public HorizontalMetricsTable getHorizontalMetrics()
    {
        HorizontalMetricsTable horizontalMetrics = (HorizontalMetricsTable)tables.get( HorizontalMetricsTable.TAG );
        if (horizontalMetrics != null && !horizontalMetrics.getInitialized())
        {
            initializeTable(horizontalMetrics);
        }
        return horizontalMetrics;
    }
    
    /**
     * Get the loca table for this TTF.
     * 
     * @return The loca table.
     */
    public IndexToLocationTable getIndexToLocation()
    {
        IndexToLocationTable indexToLocation = (IndexToLocationTable)tables.get( IndexToLocationTable.TAG );
        if (indexToLocation != null && !indexToLocation.getInitialized())
        {
            initializeTable(indexToLocation);
        }
        return indexToLocation;
    }
    
    /**
     * Get the glyf table for this TTF.
     * 
     * @return The glyf table.
     */
    public GlyphTable getGlyph()
    {
        GlyphTable glyph = (GlyphTable)tables.get( GlyphTable.TAG );
        if (glyph != null && !glyph.getInitialized())
        {
            initializeTable(glyph);
        }
        return glyph;
    }
    
    /**
     * Get the cmap table for this TTF.
     * 
     * @return The cmap table.
     */
    public CMAPTable getCMAP()
    {
        CMAPTable cmap = (CMAPTable)tables.get( CMAPTable.TAG );
        if (cmap != null && !cmap.getInitialized())
        {
            initializeTable(cmap);
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
     * Initialize the given table if necessary.
     * 
     * @param table the table to be initialized
     */
    public void initializeTable(TTFTable table)
    {
        try
        {
            // save current position
            long currentPosition = data.getCurrentPosition();
            data.seek(table.getOffset());
            table.initData(this, data);
            // restore current position
            data.seek(currentPosition);
        }
        catch (IOException exception)
        {
            log.error("An error occured when reading table " + table.getTag(), exception);
        }
    }

    /**
     * Returns the number of glyphs (MaximuProfile.numGlyphs).
     * 
     * @return the number of glyphs
     */
    public int getNumberOfGlyphs()
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
    public int getUnitsPerEm()
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
     * Returns the width for the given glyph code.
     * 
     * @param code the glyph code
     * @return the width
     */
    public int getAdvanceWidth(int code)
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
        if (advanceWidths.length > code)
        {
            return advanceWidths[code];
        }
        else
        {
            // monospaced fonts may not have a width for every glyph
            // the last one is for subsequent glyphs
            return advanceWidths[advanceWidths.length-1];
        }
    }

}
