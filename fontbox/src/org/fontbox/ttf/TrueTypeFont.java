/**
 * Copyright (c) 2005, www.fontbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of fontbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.fontbox.org
 *
 */
package org.fontbox.ttf;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;

/**
 * A class to hold true type font information.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.2 $
 */
public class TrueTypeFont 
{
    private float version; 
    
    private Map tables = new HashMap();
    
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
    public Collection getTables()
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
        return (NamingTable)tables.get( NamingTable.TAG );
    }
    
    /**
     * Get the postscript table for this TTF.
     * 
     * @return The postscript table.
     */
    public PostScriptTable getPostScript()
    {
        return (PostScriptTable)tables.get( PostScriptTable.TAG );
    }
    
    /**
     * Get the OS/2 table for this TTF.
     * 
     * @return The OS/2 table.
     */
    public OS2WindowsMetricsTable getOS2Windows()
    {
        return (OS2WindowsMetricsTable)tables.get( OS2WindowsMetricsTable.TAG );
    }
    
    /**
     * Get the maxp table for this TTF.
     * 
     * @return The maxp table.
     */
    public MaximumProfileTable getMaximumProfile()
    {
        return (MaximumProfileTable)tables.get( MaximumProfileTable.TAG );
    }
    
    /**
     * Get the head table for this TTF.
     * 
     * @return The head table.
     */
    public HeaderTable getHeader()
    {
        return (HeaderTable)tables.get( HeaderTable.TAG );
    }
    
    /**
     * Get the hhea table for this TTF.
     * 
     * @return The hhea table.
     */
    public HorizontalHeaderTable getHorizontalHeader()
    {
        return (HorizontalHeaderTable)tables.get( HorizontalHeaderTable.TAG );
    }
    
    /**
     * Get the hmtx table for this TTF.
     * 
     * @return The hmtx table.
     */
    public HorizontalMetricsTable getHorizontalMetrics()
    {
        return (HorizontalMetricsTable)tables.get( HorizontalMetricsTable.TAG );
    }
    
    /**
     * Get the loca table for this TTF.
     * 
     * @return The loca table.
     */
    public IndexToLocationTable getIndexToLocation()
    {
        return (IndexToLocationTable)tables.get( IndexToLocationTable.TAG );
    }
    
    /**
     * Get the glyf table for this TTF.
     * 
     * @return The glyf table.
     */
    public GlyphTable getGlyph()
    {
        return (GlyphTable)tables.get( GlyphTable.TAG );
    }
    
    /**
     * Get the cmap table for this TTF.
     * 
     * @return The cmap table.
     */
    public CMAPTable getCMAP()
    {
        return (CMAPTable)tables.get( CMAPTable.TAG );
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
}
