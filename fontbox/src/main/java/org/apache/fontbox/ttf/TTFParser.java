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

/**
 * A true type font file parser.
 * 
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.2 $
 */
public class TTFParser
{   
    /**
     * A simple command line program to test parsing of a TTF file. <br/>
     * usage: java org.pdfbox.ttf.TTFParser &lt;ttf-file&gt;
     * 
     * @param args The command line arguments.
     * 
     * @throws IOException If there is an error while parsing the font file.
     */
    public static void main( String[] args ) throws IOException
    {
        if( args.length != 1 )
        {
            System.err.println( "usage: java org.pdfbox.ttf.TTFParser <ttf-file>" );
            System.exit( -1 );
        }
        TTFParser parser = new TTFParser();
        TrueTypeFont font = parser.parseTTF( args[0] );
        System.out.println( "Font:" + font );
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
        List<TTFTable> initialized = new ArrayList<TTFTable>();
        //need to initialize a couple tables in a certain order
        HeaderTable head = font.getHeader();
        if (head == null) {
        	throw new IOException("head is mandatory");
        }
        raf.seek( head.getOffset() );
        head.initData( font, raf );
        initialized.add( head );
        
        HorizontalHeaderTable hh = font.getHorizontalHeader();
        if (hh == null) {
        	throw new IOException("hhead is mandatory");
        }
        raf.seek( hh.getOffset() );
        hh.initData( font, raf );
        initialized.add( hh );
        
        MaximumProfileTable maxp = font.getMaximumProfile();
        if (maxp == null) {
        	throw new IOException("maxp is mandatory");
        }
        raf.seek( maxp.getOffset() );
        maxp.initData( font, raf );
        initialized.add( maxp );
        
        PostScriptTable post = font.getPostScript();
        if (post == null) {
        	throw new IOException("post is mandatory");
        }
        raf.seek( post.getOffset() );
        post.initData( font, raf );
        initialized.add( post );
        
        IndexToLocationTable loc = font.getIndexToLocation();
        if (loc == null) {
        	throw new IOException("loca is mandatory");
        }
        raf.seek( loc.getOffset() );
        loc.initData( font, raf );
        initialized.add( loc );
        
        Iterator<TTFTable> iter = font.getTables().iterator();
        while( iter.hasNext() )
        {
            TTFTable table = iter.next();
            if( !initialized.contains( table ) )
            {
                raf.seek( table.getOffset() );
                table.initData( font, raf );
            }
        }
        return font;
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