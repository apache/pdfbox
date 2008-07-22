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
        List initialized = new ArrayList();
        //need to initialize a couple tables in a certain order
        HeaderTable head = font.getHeader();
        raf.seek( head.getOffset() );
        head.initData( font, raf );
        initialized.add( head );
        
        
        HorizontalHeaderTable hh = font.getHorizontalHeader();
        raf.seek( hh.getOffset() );
        hh.initData( font, raf );
        initialized.add( hh );
        
        MaximumProfileTable maxp = font.getMaximumProfile();
        raf.seek( maxp.getOffset() );
        maxp.initData( font, raf );
        initialized.add( maxp );
        
        PostScriptTable post = font.getPostScript();
        raf.seek( post.getOffset() );
        post.initData( font, raf );
        initialized.add( post );
        
        IndexToLocationTable loc = font.getIndexToLocation();
        raf.seek( loc.getOffset() );
        loc.initData( font, raf );
        initialized.add( loc );
        
        Iterator iter = font.getTables().iterator();
        while( iter.hasNext() )
        {
            TTFTable table = (TTFTable)iter.next();
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
        else if( tag.equals( GlyphTable.TAG ) )
        {
            retval = new GlyphTable();
        }
        else if( tag.equals( GlyphTable.TAG ) )
        {
            retval = new GlyphTable();
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