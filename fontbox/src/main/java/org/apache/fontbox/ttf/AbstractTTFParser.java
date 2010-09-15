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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

abstract class AbstractTTFParser {
	
    protected boolean isEmbedded = false;

	public AbstractTTFParser(boolean isEmbedded) {
		this.isEmbedded = isEmbedded;
	}
	
    /**
     * Parse a file and get a true type font.
     * @param ttfFile The TTF file.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    public TrueTypeFont parseTTF( String ttfFile ) throws IOException
    {
        RAFDataStream raf = new RAFDataStream( ttfFile, "r" );
        return parseTTF( raf );
    }
    
    /**
     * Parse a file and get a true type font.
     * @param ttfFile The TTF file.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    public TrueTypeFont parseTTF( File ttfFile ) throws IOException
    {
        RAFDataStream raf = new RAFDataStream( ttfFile, "r" );
        return parseTTF( raf );
    }
    
    /**
     * Parse a file and get a true type font.
     * @param ttfData The TTF data to parse.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    public TrueTypeFont parseTTF( InputStream ttfData ) throws IOException
    {
        return parseTTF( new MemoryTTFDataStream( ttfData ));
    }
    
    /**
     * Parse a file and get a true type font.
     * @param raf The TTF file.
     * @return A true type font.
     * @throws IOException If there is an error parsing the true type font.
     */
    public TrueTypeFont parseTTF( TTFDataStream raf ) throws IOException
    {
        TrueTypeFont font = new TrueTypeFont( raf );
        font.setVersion( raf.read32Fixed() );
        int numberOfTables = raf.readUnsignedShort();
        int searchRange = raf.readUnsignedShort();
        int entrySelector = raf.readUnsignedShort();
        int rangeShift = raf.readUnsignedShort();
        for( int i=0; i<numberOfTables; i++ )
        {
            TTFTable table = readTableDirectory( raf );   
            font.addTable( table );
        }

        //need to initialize a couple tables in a certain order
        parseTables(font, raf);

        return font;
    }

    /**
     * Parse all tables and check if all needed tables are present.
     * @param font the TrueTypeFont instance holding the parsed data.
     * @param raf the data stream of the to be parsed ttf font
     * @throws IOException If there is an error parsing the true type font.
     */
    protected void parseTables(TrueTypeFont font, TTFDataStream raf)
    throws IOException {
        List<TTFTable> initialized = new ArrayList<TTFTable>();
        HeaderTable head = font.getHeader();
        if (head == null) 
        {
            throw new IOException("head is mandatory");
        }
        raf.seek( head.getOffset() );
        head.initData( font, raf );
        initialized.add( head );

        HorizontalHeaderTable hh = font.getHorizontalHeader();
        if (hh == null) 
        {
            throw new IOException("hhead is mandatory");
        }
        raf.seek( hh.getOffset() );
        hh.initData( font, raf );
        initialized.add( hh );

        MaximumProfileTable maxp = font.getMaximumProfile();
        if (maxp != null) 
        {
            raf.seek( maxp.getOffset() );
            maxp.initData( font, raf );
            initialized.add( maxp );
        } 
        else 
        {
            throw new IOException("maxp is mandatory");
        }

        PostScriptTable post = font.getPostScript();
        if (post != null) {
            raf.seek( post.getOffset() );
            post.initData( font, raf );
            initialized.add( post );
        } 
        else if ( !isEmbedded ) 
        {
            // in an embedded font this table is optional
            throw new IOException("post is mandatory");
        }

        IndexToLocationTable loc = font.getIndexToLocation();
        if (loc == null) 
        {
            throw new IOException("loca is mandatory");
        }
        raf.seek( loc.getOffset() );
        loc.initData( font, raf );
        initialized.add( loc );

        boolean cvt = false, prep = false, fpgm = false;
        Iterator<TTFTable> iter = font.getTables().iterator();
        while( iter.hasNext() )
        {
            TTFTable table = iter.next();
            if( !initialized.contains( table ) )
            {
                raf.seek( table.getOffset() );
                table.initData( font, raf );
            }
            if (table.getTag().startsWith("cvt")) 
            {
                cvt = true;
            } 
            else if ("prep".equals(table.getTag())) 
            {
                prep = true;
            } 
            else if ("fpgm".equals(table.getTag())) 
            {
                fpgm = true;
            }
        }   

        // check others mandatory tables
        if ( font.getGlyph() == null )
        {
            throw new IOException("glyf is mandatory");
        }
        if ( font.getNaming() == null && !isEmbedded )
        {
            throw new IOException("name is mandatory");
        }
        if ( font.getHorizontalMetrics() == null )
        {
            throw new IOException("hmtx is mandatory");
        }

        if (isEmbedded) {
            // in a embedded truetype font prep, cvt_ and fpgm tables 
            // are mandatory
            if (!fpgm) 
            {
                throw new IOException("fpgm is mandatory");
            }
            if (!prep)
            {
                throw new IOException("prep is mandatory");
            }
            if (!cvt) 
            {
                throw new IOException("cvt_ is mandatory");
            }
        }
    }

    private TTFTable readTableDirectory( TTFDataStream raf ) throws IOException
    {
        TTFTable retval = null;
        String tag = raf.readString( 4 );
        if( tag.equals( CMAPTable.TAG ) )
        {
            retval = new CMAPTable();
        }
        else if( tag.equals( GlyphTable.TAG ) )
        {
            retval = new GlyphTable();
        }
        else if( tag.equals( HeaderTable.TAG ) )
        {
            retval = new HeaderTable();
        }
        else if( tag.equals( HorizontalHeaderTable.TAG ) )
        {
            retval = new HorizontalHeaderTable();
        }
        else if( tag.equals( HorizontalMetricsTable.TAG ) )
        {
            retval = new HorizontalMetricsTable();
        }
        else if( tag.equals( IndexToLocationTable.TAG ) )
        {
            retval = new IndexToLocationTable();
        }
        else if( tag.equals( MaximumProfileTable.TAG ) )
        {
            retval = new MaximumProfileTable();
        }
        else if( tag.equals( NamingTable.TAG ) )
        {
            retval = new NamingTable();
        }
        else if( tag.equals( OS2WindowsMetricsTable.TAG ) )
        {
            retval = new OS2WindowsMetricsTable();
        }
        else if( tag.equals( PostScriptTable.TAG ) )
        {
            retval = new PostScriptTable();
        }
        else if( tag.equals( DigitalSignatureTable.TAG ) )
        {
            retval = new DigitalSignatureTable();
        }
        else
        {
            //unknown table type but read it anyway.
            retval = new TTFTable();
        }
        retval.setTag( tag );
        retval.setCheckSum( raf.readUnsignedInt() );
        retval.setOffset( raf.readUnsignedInt() );
        retval.setLength( raf.readUnsignedInt() );
        return retval;
    }
}
