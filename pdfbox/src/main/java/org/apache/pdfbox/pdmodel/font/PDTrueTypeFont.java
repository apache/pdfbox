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
package org.apache.pdfbox.pdmodel.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.ttf.CMAPEncodingEntry;
import org.apache.fontbox.ttf.CMAPTable;
import org.apache.fontbox.ttf.GlyphData;
import org.apache.fontbox.ttf.GlyphTable;
import org.apache.fontbox.ttf.HeaderTable;
import org.apache.fontbox.ttf.HorizontalHeaderTable;
import org.apache.fontbox.ttf.HorizontalMetricsTable;
import org.apache.fontbox.ttf.NameRecord;
import org.apache.fontbox.ttf.NamingTable;
import org.apache.fontbox.ttf.OS2WindowsMetricsTable;
import org.apache.fontbox.ttf.PostScriptTable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TTFSubFont;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.ResourceLoader;

/**
 * This is the TrueType implementation of fonts.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class PDTrueTypeFont extends PDSimpleFont
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDTrueTypeFont.class);

    /**
     * This is the key to a property in the PDFBox_External_Fonts.properties
     * file to load a Font when a mapping does not exist for the current font.
     */
    public static final String UNKNOWN_FONT = "UNKNOWN_FONT";

    private Font awtFont = null;

    private static Properties externalFonts = new Properties();
    private static Map<String,TrueTypeFont> loadedExternalFonts = new HashMap<String,TrueTypeFont>();

    static
    {
        try
        {
            ResourceLoader.loadProperties(
                    "org/apache/pdfbox/resources/PDFBox_External_Fonts.properties",
                    externalFonts );
        }
        catch( IOException io )
        {
            throw new RuntimeException( "Error loading font resources", io );
        }
    }


    /**
     * Constructor.
     */
    public PDTrueTypeFont()
    {
        super();
        font.setItem( COSName.SUBTYPE, COSName.TRUE_TYPE );
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     * 
     * @throws IOException exception if something went wrong when loading the font.
     */
    public PDTrueTypeFont( COSDictionary fontDictionary ) throws IOException
    {
        super( fontDictionary );
        ensureFontDescriptor();
    }

    /**
     * This will load a TTF font from a font file.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param file The file on the filesystem that holds the font file.
     * @return A true type font.
     * @throws IOException If there is an error loading the file data.
     */
    public static PDTrueTypeFont loadTTF( PDDocument doc, String file ) throws IOException
    {
        return loadTTF( doc, new File( file ) );
    }

    /**
     * This will load a TTF to be embedded into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param file a ttf file.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDTrueTypeFont loadTTF( PDDocument doc, File file ) throws IOException
    {
        return loadTTF( doc, new FileInputStream( file ) );
    } 
    
    /**
     * This will load a TTF to be embedded into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param stream a ttf input stream.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDTrueTypeFont loadTTF( PDDocument doc, InputStream stream ) throws IOException
    { 
        return PDTrueTypeFont.loadTTF(doc,stream,new WinAnsiEncoding());
    }

    /**
     * This will load a TTF to be embedded into a document.
     *
     * @param doc The PDF document that will hold the embedded font.
     * @param stream a ttf input stream.
     * @param enc The font encoding.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDTrueTypeFont loadTTF( PDDocument doc, InputStream stream, Encoding enc ) throws IOException
    { 
        PDStream fontStream = new PDStream(doc, stream, false );
        fontStream.getStream().setInt( COSName.LENGTH1, fontStream.getByteArray().length );
        fontStream.addCompression();
        //only support winansi encoding right now, should really
        //just use Identity-H with unicode mapping
        return PDTrueTypeFont.loadTTF(fontStream,enc);
    }

    /**
     * This will load a TTF to be embedded into a document.
     *
     * @param fontStream a ttf input stream.
     * @param enc The font encoding.
     * @return a PDTrueTypeFont instance.
     * @throws IOException If there is an error loading the data.
     */
    public static PDTrueTypeFont loadTTF( PDStream fontStream, Encoding enc ) throws IOException
    { 
        PDTrueTypeFont retval = new PDTrueTypeFont();
        retval.setFontEncoding( enc );
        retval.setEncoding(enc.getCOSObject());

        PDFontDescriptorDictionary fd = new PDFontDescriptorDictionary();
        retval.setFontDescriptor( fd );
        fd.setFontFile2( fontStream );
        // As the stream was close within the PDStream constructor, we have to recreate it
        InputStream stream = fontStream.createInputStream();
        try
        {
            retval.loadDescriptorDictionary(fd, stream); 
        }
        finally
        {
            stream.close();
        }
        return retval;
    }

    private void ensureFontDescriptor() throws IOException
    {
        if( getFontDescriptor() == null )
        {
            PDFontDescriptorDictionary fdd = new PDFontDescriptorDictionary(); 
            setFontDescriptor(fdd);
            InputStream ttfData = getExternalTTFData();
            if( ttfData != null )
            {
                try
                {
                    loadDescriptorDictionary(fdd, ttfData);
                }
                finally
                {
                    ttfData.close();
                }
            }
        }
    }

    private void loadDescriptorDictionary(PDFontDescriptorDictionary fd, InputStream ttfData) throws IOException
    {
        TrueTypeFont ttf = null;
        try
        {
            TTFParser parser = new TTFParser();
            ttf = parser.parseTTF( ttfData );
            NamingTable naming = ttf.getNaming();
            List<NameRecord> records = naming.getNameRecords();
            for( int i=0; i<records.size(); i++ )
            {
                NameRecord nr = records.get( i );
                if( nr.getNameId() == NameRecord.NAME_POSTSCRIPT_NAME )
                {
                    setBaseFont( nr.getString() );
                    fd.setFontName( nr.getString() );
                }
                else if( nr.getNameId() == NameRecord.NAME_FONT_FAMILY_NAME )
                {
                    fd.setFontFamily( nr.getString() );
                }
            }

            OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
            boolean isSymbolic = false;
            switch( os2.getFamilyClass() )
            {
                case OS2WindowsMetricsTable.FAMILY_CLASS_SYMBOLIC:
                    isSymbolic = true;
                    break;
                case OS2WindowsMetricsTable.FAMILY_CLASS_SCRIPTS:
                    fd.setScript( true );
                    break;
                case OS2WindowsMetricsTable.FAMILY_CLASS_CLAREDON_SERIFS:
                case OS2WindowsMetricsTable.FAMILY_CLASS_FREEFORM_SERIFS:
                case OS2WindowsMetricsTable.FAMILY_CLASS_MODERN_SERIFS:
                case OS2WindowsMetricsTable.FAMILY_CLASS_OLDSTYLE_SERIFS:
                case OS2WindowsMetricsTable.FAMILY_CLASS_SLAB_SERIFS:
                    fd.setSerif( true );
                    break;
                default:
                    //do nothing
            }
            switch( os2.getWidthClass() )
            {
                case OS2WindowsMetricsTable.WIDTH_CLASS_ULTRA_CONDENSED:
                    fd.setFontStretch( "UltraCondensed" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_EXTRA_CONDENSED:
                    fd.setFontStretch( "ExtraCondensed" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_CONDENSED:
                    fd.setFontStretch( "Condensed" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_SEMI_CONDENSED:
                    fd.setFontStretch( "SemiCondensed" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_MEDIUM:
                    fd.setFontStretch( "Normal" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_SEMI_EXPANDED:
                    fd.setFontStretch( "SemiExpanded" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_EXPANDED:
                    fd.setFontStretch( "Expanded" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_EXTRA_EXPANDED:
                    fd.setFontStretch( "ExtraExpanded" );
                    break;
                case OS2WindowsMetricsTable.WIDTH_CLASS_ULTRA_EXPANDED:
                    fd.setFontStretch( "UltraExpanded" );
                    break;
                default:
                    //do nothing
            }
            fd.setFontWeight( os2.getWeightClass() );
            fd.setSymbolic( isSymbolic );
            fd.setNonSymbolic( !isSymbolic );

            //todo retval.setItalic
            //todo retval.setAllCap
            //todo retval.setSmallCap
            //todo retval.setForceBold

            HeaderTable header = ttf.getHeader();
            PDRectangle rect = new PDRectangle();
            float scaling = 1000f/header.getUnitsPerEm();
            rect.setLowerLeftX( header.getXMin() * scaling );
            rect.setLowerLeftY( header.getYMin() * scaling );
            rect.setUpperRightX( header.getXMax() * scaling );
            rect.setUpperRightY( header.getYMax() * scaling );
            fd.setFontBoundingBox( rect );

            HorizontalHeaderTable hHeader = ttf.getHorizontalHeader();
            fd.setAscent( hHeader.getAscender() * scaling );
            fd.setDescent( hHeader.getDescender() * scaling );

            GlyphTable glyphTable = ttf.getGlyph();
            GlyphData[] glyphs = glyphTable.getGlyphs();

            PostScriptTable ps = ttf.getPostScript();
            fd.setFixedPitch( ps.getIsFixedPitch() > 0 );
            fd.setItalicAngle( ps.getItalicAngle() );

            String[] names = ps.getGlyphNames();
            
            if( names != null )
            {
                for( int i=0; i<names.length; i++ )
                {
                     //if we have a capital H then use that, otherwise use the
                    //tallest letter
                    if( names[i].equals( "H" ) )
                    {
                        fd.setCapHeight( glyphs[i].getBoundingBox().getUpperRightY()/scaling );
                    }
                    if( names[i].equals( "x" ) )
                    {
                        fd.setXHeight( glyphs[i].getBoundingBox().getUpperRightY()/scaling );
                    }
                }
            }

            //hmm there does not seem to be a clear definition for StemV,
            //this is close enough and I am told it doesn't usually get used.
            fd.setStemV( (fd.getFontBoundingBox().getWidth() * .13f) );

            CMAPTable cmapTable = ttf.getCMAP();
            CMAPEncodingEntry[] cmaps = cmapTable.getCmaps();
            CMAPEncodingEntry uniMap = null;
            
            for( int i=0; i<cmaps.length; i++ )
            {
                if( cmaps[i].getPlatformId() == CMAPTable.PLATFORM_WINDOWS) 
                {
                    int platformEncoding = cmaps[i].getPlatformEncodingId();
                    if ( CMAPTable.ENCODING_UNICODE == platformEncoding )
                    {
                        uniMap = cmaps[i];
                        break;
                    }
                }
            }

            Map<Integer, String> codeToName = this.getFontEncoding().getCodeToNameMap();
             
            int firstChar = Collections.min(codeToName.keySet());
            int lastChar = Collections.max(codeToName.keySet());
            
            HorizontalMetricsTable hMet = ttf.getHorizontalMetrics();
            int[] widthValues = hMet.getAdvanceWidth();
            // some monospaced fonts provide only one value for the width 
            // instead of an array containing the same value for every glyphid 
            boolean isMonospaced = fd.isFixedPitch();
            int nWidths=lastChar-firstChar+1;
            List<Float> widths = new ArrayList<Float>(nWidths);
            // use the first width as default
            // proportional fonts -> width of the .notdef character
            // monospaced-fonts -> the first width
            int defaultWidth = Math.round(widthValues[0] * scaling);
            for (int i = 0; i < nWidths; i++)
            {
                widths.add((float)defaultWidth);
            }
            // Encoding singleton to have acces to the chglyph name to
            // unicode cpoint point mapping of Adobe's glyphlist.txt
            Encoding glyphlist = WinAnsiEncoding.INSTANCE;

            // A character code is mapped to a glyph name via the provided
            // font encoding. Afterwards, the glyph name is translated to a
            // glyph ID.
            // For details, see PDFReference16.pdf, Section 5.5.5, p.401
            //
            for (Entry<Integer, String> e : codeToName.entrySet()) 
            {
                String name = e.getValue();
                // pdf code to unicode by glyph list.
                String c = glyphlist.getCharacter(name);
                int charCode = c.codePointAt(0);
                int gid = uniMap.getGlyphId(charCode);
                if (gid != 0) 
                {
                    if (isMonospaced)
                    {
                        widths.set(e.getKey().intValue() - firstChar, (float)defaultWidth);
                    }
                    else
                    {
                        widths.set(e.getKey().intValue() - firstChar, (float)Math.round(widthValues[gid] * scaling));
                    }
                }
            }
            setWidths( widths );
            setFirstChar( firstChar );
            setLastChar( lastChar );
        }
        finally
        {
            if( ttf != null )
            {
                ttf.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Font getawtFont() throws IOException
    {
         PDFontDescriptorDictionary fd = (PDFontDescriptorDictionary)getFontDescriptor();
        if( awtFont == null )
        {
            PDStream ff2Stream = fd.getFontFile2();
            if( ff2Stream != null )
            {
                try
                {
                    // create a font with the embedded data
                    awtFont = Font.createFont( Font.TRUETYPE_FONT, ff2Stream.createInputStream() );
                }
                catch( FontFormatException f )
                {
                    try
                    {
                        // as a workaround we try to rebuild the embedded subsfont
                        byte[] fontData = rebuildTTF(fd, ff2Stream.createInputStream());
                        if (fontData != null)
                        {
                            ByteArrayInputStream bais = new ByteArrayInputStream(fontData);
                            awtFont = Font.createFont( Font.TRUETYPE_FONT,bais);
                        }
                    } 
                    catch (FontFormatException e)
                    {
                        log.info("Can't read the embedded font " + fd.getFontName() );
                    }
                }
                if (awtFont == null)
                {
                    awtFont = FontManager.getAwtFont(fd.getFontName());
                    if (awtFont != null)
                    {
                        log.info("Using font "+awtFont.getName()+ " instead");
                    }
                    setIsFontSubstituted(true);
                }
            }
            else
            {
                // check if the font is part of our environment
                awtFont = FontManager.getAwtFont(fd.getFontName());
                if (awtFont == null)
                {
                    log.info("Can't find the specified font " + fd.getFontName() );
                    // check if there is a font mapping for an external font file
                    TrueTypeFont ttf = getExternalFontFile2( fd );
                    if( ttf != null )
                    {
                        try
                        {
                            awtFont = Font.createFont( Font.TRUETYPE_FONT, ttf.getOriginalData() );
                        }
                        catch( FontFormatException f )
                        {
                            log.info("Can't read the external fontfile " + fd.getFontName() );
                        }
                    }
                }
            }
            if (awtFont == null)
            {
                // we can't find anything, so we have to use the standard font
                awtFont = FontManager.getStandardFont();
                log.info("Using font "+awtFont.getName()+ " instead");
                setIsFontSubstituted(true);
            }
        }
        return awtFont;
    }

    private byte[] rebuildTTF(PDFontDescriptorDictionary fd, InputStream inputStream) throws IOException
    {
        // this is one possible case of an incomplete subfont which leads to a font exception
        if (getFontEncoding() instanceof WinAnsiEncoding)
        {
            TTFParser ttfParser = new TTFParser(true);
            TrueTypeFont ttf = ttfParser.parseTTF(inputStream);
            TTFSubFont ttfSub = new TTFSubFont(ttf, "PDFBox-Rebuild");
            for (int i=getFirstChar();i<=getLastChar();i++)
            {
                ttfSub.addCharCode(i);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ttfSub.writeToStream(baos);
            return baos.toByteArray();
        }
        return null;
    }

    private InputStream getExternalTTFData() throws IOException
    {
        String ttfResource = externalFonts.getProperty( UNKNOWN_FONT );
        String baseFont = getBaseFont();
        if( baseFont != null && externalFonts.containsKey(baseFont) )
        {
            ttfResource = externalFonts.getProperty( baseFont );
        }
        return (ttfResource != null ? ResourceLoader.loadResource(ttfResource) : null);
    }

    /**
     * Permit to load an external TTF Font program file
     *
     * Created by Pascal Allain
     * Vertical7 Inc.
     *
     * @param fd The font descriptor currently used
     * @return A PDStream with the Font File program, null if fd is null
     * @throws IOException If the font is not found
     */
    private TrueTypeFont getExternalFontFile2(PDFontDescriptorDictionary fd)
        throws IOException
    {
        TrueTypeFont retval = null;

        if ( fd != null )
        {
            String baseFont = getBaseFont();
            String fontResource = externalFonts.getProperty( UNKNOWN_FONT );
            if( (baseFont != null) &&
                 (externalFonts.containsKey(baseFont)) )
            {
                fontResource = externalFonts.getProperty(baseFont);
            }
            if( fontResource != null )
            {
                retval = (TrueTypeFont)loadedExternalFonts.get( baseFont );
                if( retval == null )
                {
                    TTFParser ttfParser = new TTFParser();
                    InputStream fontStream = ResourceLoader.loadResource( fontResource );
                    if( fontStream == null )
                    {
                        throw new IOException( "Error missing font resource '" + externalFonts.get(baseFont) + "'" );
                    }
                    retval = ttfParser.parseTTF( fontStream );
                    loadedExternalFonts.put( baseFont, retval );
                }
            }
        }

        return retval;
    }
}
