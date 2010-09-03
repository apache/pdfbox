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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.afm.AFMParser;
import org.apache.fontbox.afm.FontMetric;
import org.apache.fontbox.cmap.CMapParser;
import org.apache.fontbox.cmap.CMap;

import org.apache.pdfbox.encoding.AFMEncoding;
import org.apache.pdfbox.encoding.DictionaryEncoding;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.EncodingManager;
import org.apache.pdfbox.encoding.Type1Encoding;
import org.apache.pdfbox.encoding.conversion.CMapSubstitution;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.apache.pdfbox.util.ResourceLoader;

import java.awt.Graphics;
import java.awt.geom.AffineTransform;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This is the base class for all PDF fonts.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.46 $
 */
public abstract class PDFont implements COSObjectable
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDFont.class);

    /**
     * The cos dictionary for this font.
     */
    protected COSDictionary font;

    /**
     * This is only used if this is a font object and it has an encoding.
     */
    private Encoding fontEncoding = null;

    /**
     * This is only used if this is a font object and it has an encoding and it is
     * a type0 font with a cmap.
     */
    protected CMap cmap = null;

    private static Map<String, CMap> cmapObjects =
        Collections.synchronizedMap( new HashMap<String, CMap>() );

    /**
     * The static map of the default Adobe font metrics.
     */
    private static final Map<String, FontMetric> afmObjects =
        Collections.unmodifiableMap( getAdobeFontMetrics() );

    private static Map<String, FontMetric> getAdobeFontMetrics() {
        Map<String, FontMetric> metrics = new HashMap<String, FontMetric>();
        addAdobeFontMetric( metrics, "Courier-Bold" );
        addAdobeFontMetric( metrics, "Courier-BoldOblique" );
        addAdobeFontMetric( metrics, "Courier" );
        addAdobeFontMetric( metrics, "Courier-Oblique" );
        addAdobeFontMetric( metrics, "Helvetica" );
        addAdobeFontMetric( metrics, "Helvetica-Bold" );
        addAdobeFontMetric( metrics, "Helvetica-BoldOblique" );
        addAdobeFontMetric( metrics, "Helvetica-Oblique" );
        addAdobeFontMetric( metrics, "Symbol" );
        addAdobeFontMetric( metrics, "Times-Bold" );
        addAdobeFontMetric( metrics, "Times-BoldItalic" );
        addAdobeFontMetric( metrics, "Times-Italic" );
        addAdobeFontMetric( metrics, "Times-Roman" );
        addAdobeFontMetric( metrics, "ZapfDingbats" );
        return metrics;
    }

    private static String resourceRootCMAP = "org/apache/pdfbox/resources/cmap/";
    private static String resourceRootAFM = "org/apache/pdfbox/resources/afm/";

    private static void addAdobeFontMetric(
            Map<String, FontMetric> metrics, String name ) {
        try {
            String resource = resourceRootAFM + name + ".afm";
            InputStream afmStream = ResourceLoader.loadResource( resource );
            if( afmStream != null )
            {
                try {
                    AFMParser parser = new AFMParser( afmStream );
                    parser.parse();
                    metrics.put( name, parser.getResult() );
                } finally {
                    afmStream.close();
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * This will clear AFM resources that are stored statically.
     * This is usually not a problem unless you want to reclaim
     * resources for a long running process.
     *
     * SPECIAL NOTE: The font calculations are currently in COSObject, which
     * is where they will reside until PDFont is mature enough to take them over.
     * PDFont is the appropriate place for them and not in COSObject but we need font
     * calculations for text extraction.  THIS METHOD WILL BE MOVED OR REMOVED
     * TO ANOTHER LOCATION IN A FUTURE VERSION OF PDFBOX.
     */
    public static void clearResources()
    {
        cmapObjects.clear();
    }

    /**
     * Constructor.
     */
    public PDFont()
    {
        font = new COSDictionary();
        font.setItem( COSName.TYPE, COSName.FONT );
    }

    /**
     * Constructor.
     *
     * @param fontDictionary The font dictionary according to the PDF specification.
     */
    public PDFont( COSDictionary fontDictionary )
    {
        font = fontDictionary;
        determineEncoding();
    }

    private void determineEncoding()
    {
        String cmapName = null;
        COSName encodingName = null;
        COSBase toUnicode = font.getDictionaryObject( COSName.TO_UNICODE );
        COSBase encoding = getEncodingObject(); 
        if( toUnicode != null )
        {
            if ( toUnicode instanceof COSStream )
            {
                try {
                    parseCmap(null, ((COSStream)toUnicode).getUnfilteredStream(), null);
                }
                catch(IOException exception) 
                {
                    log.error("Error: Could not load embedded CMAP" );
                }
            }
            else if ( toUnicode instanceof COSName)
            {
                encodingName = (COSName)toUnicode;
                cmap = cmapObjects.get( encodingName.getName() );
                if (cmap == null) 
                {
                    cmapName = encodingName.getName();
                }
            }
        }
        if (encoding != null) 
        {
            if (encoding instanceof COSName) 
            {
                if (cmap == null)
                {
                    encodingName = (COSName)encoding;
                    cmap = cmapObjects.get( encodingName.getName() );
                    if (cmap == null) 
                    {
                        cmapName = encodingName.getName();
                    }
                }
                if (cmap == null && cmapName != null)
                {
                    try 
                    {
                        fontEncoding =
                            EncodingManager.INSTANCE.getEncoding(encodingName);
                    }
                    catch(IOException exception) 
                    {
                        log.debug("Debug: Could not find encoding for " + encodingName );
                    }
                }
            }
            else if (encoding instanceof COSDictionary) 
            {
                try 
                {
                    fontEncoding = new DictionaryEncoding((COSDictionary)encoding);
                }
                catch(IOException exception) 
                {
                    log.error("Error: Could not create the DictionaryEncoding" );
                }
            }
            else if(encoding instanceof COSStream )
            {
                if (cmap == null)
                {
                    COSStream encodingStream = (COSStream)encoding;
                    try 
                    {
                        parseCmap( null, encodingStream.getUnfilteredStream(), null );
                    }
                    catch(IOException exception) 
                    {
                        log.error("Error: Could not parse the embedded CMAP" );
                    }
                }
            }
        }
        COSDictionary cidsysteminfo = (COSDictionary)font.getDictionaryObject(COSName.CIDSYSTEMINFO);
        if (cidsysteminfo != null) 
        {
            String ordering = cidsysteminfo.getString(COSName.ORDERING);
            String registry = cidsysteminfo.getString(COSName.REGISTRY);
            int supplement = cidsysteminfo.getInt(COSName.SUPPLEMENT);
            cmapName = registry + "-" + ordering+ "-" + supplement;
            cmapName = CMapSubstitution.substituteCMap( cmapName );
            cmap = cmapObjects.get( cmapName );
        }
        FontMetric metric = getAFM();
        if( metric != null )
        {
            fontEncoding = new AFMEncoding( metric );
        }
        
        if (cmap == null && cmapName != null) 
        {
            String resourceName = resourceRootCMAP + cmapName;
            try {
                parseCmap( resourceRootCMAP, ResourceLoader.loadResource( resourceName ), encodingName );
                if( cmap == null && encodingName == null)
                {
                    log.error("Error: Could not parse predefined CMAP file for '" + cmapName + "'" );
                }
            }
            catch(IOException exception) 
            {
                log.error("Error: Could not find predefined CMAP file for '" + cmapName + "'" );
            }
        }
//        if (fontEncoding == null)
//        {
            getEncodingFromFont();
//        }
    }

    /**
     * {@inheritDoc}
     */
    public COSBase getCOSObject()
    {
        return font;
    }

    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public abstract float getFontWidth( byte[] c, int offset, int length ) throws IOException;

    /**
     * This will get the font width for a character.
     *
     * @param c The character code to get the width for.
     * @param offset The offset into the array.
     * @param length The length of the data.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public abstract float getFontHeight( byte[] c, int offset, int length ) throws IOException;

    /**
     * This will get the width of this string for this font.
     *
     * @param string The string to get the width of.
     *
     * @return The width of the string in 1000 units of text space, ie 333 567...
     *
     * @throws IOException If there is an error getting the width information.
     */
    public float getStringWidth( String string ) throws IOException
    {
        byte[] data = string.getBytes();
        float totalWidth = 0;
        for( int i=0; i<data.length; i++ )
        {
            totalWidth+=getFontWidth( data, i, 1 );
        }
        return totalWidth;
    }

    /**
     * This will get the average font width for all characters.
     *
     * @return The width is in 1000 unit of text space, ie 333 or 777
     *
     * @throws IOException If an error occurs while parsing.
     */
    public abstract float getAverageFontWidth() throws IOException;

    /**
     * This will draw a string on a canvas using the font.
     *
     * @param string The string to draw.
     * @param g The graphics to draw onto.
     * @param fontSize The size of the font to draw.
     * @param at The transformation matrix with all infos for scaling and shearing of the font.
     * @param x The x coordinate to draw at.
     * @param y The y coordinate to draw at.
     *
     * @throws IOException If there is an error drawing the specific string.
     */
    public abstract void drawString( String string, Graphics g, float fontSize,
        AffineTransform at, float x, float y ) throws IOException;

    /**
     * Used for multibyte encodings.
     *
     * @param data The array of data.
     * @param offset The offset into the array.
     * @param length The number of bytes to use.
     *
     * @return The int value of data from the array.
     */
    protected int getCodeFromArray( byte[] data, int offset, int length )
    {
        int code = 0;
        for( int i=0; i<length; i++ )
        {
            code <<= 8;
            code |= (data[offset+i]+256)%256;
        }
        return code;
    }

    /**
     * This will attempt to get the font width from an AFM file.
     *
     * @param code The character code we are trying to get.
     *
     * @return The font width from the AFM file.
     *
     * @throws IOException if we cannot find the width.
     */
    protected float getFontWidthFromAFMFile( int code ) throws IOException
    {
        float retval = 0;
        FontMetric metric = getAFM();
        if( metric != null )
        {
            Encoding encoding = getEncoding();
            String characterName = encoding.getName( code );
            retval = metric.getCharacterWidth( characterName );
        }
        return retval;
    }

    /**
     * This will attempt to get the average font width from an AFM file.
     *
     * @return The average font width from the AFM file.
     *
     * @throws IOException if we cannot find the width.
     */
    protected float getAverageFontWidthFromAFMFile() throws IOException
    {
        float retval = 0;
        FontMetric metric = getAFM();
        if( metric != null )
        {
            retval = metric.getAverageCharacterWidth();
        }
        return retval;
    }

    /**
     * This will get an AFM object if one exists.
     *
     * @return The afm object from the name.
     *
     */
    protected FontMetric getAFM()
    {
        if(afm==null){
            COSBase baseFont = font.getDictionaryObject( COSName.BASE_FONT );
            String name = null;
            if( baseFont instanceof COSName )
            {
                name = ((COSName)baseFont).getName();
                if (name.indexOf("+") > -1)
                {
                    name = name.substring(name.indexOf("+")+1);
                }

            }
            else if( baseFont instanceof COSString )
            {
                COSString string = (COSString)baseFont;
                name = string.getString();
            }
            if( name != null )
            {
                afm = afmObjects.get( name );
            }
        }
        return afm;
    }

    private FontMetric afm = null;
    
    private COSBase encodingObject = null;
    /**
     * cache the {@link COSName#ENCODING} object from
     * the font's dictionary since it is called so often.
     * <p>
     * Use this method instead of
     * <pre>
     *   font.getDictionaryObject(COSName.ENCODING);
     * </pre>
     * @return
     */
    private COSBase getEncodingObject(){
    	if(encodingObject==null){
    		encodingObject = font.getDictionaryObject( COSName.ENCODING );
    	}
    	return encodingObject;
    }
    
    /**
     * This will perform the encoding of a character if needed.
     *
     * @param c The character to encode.
     * @param offset The offset into the array to get the data
     * @param length The number of bytes to read.
     *
     * @return The value of the encoded character.
     *
     * @throws IOException If there is an error during the encoding.
     */
    public String encode( byte[] c, int offset, int length ) throws IOException
    {
        String retval = null;
        if( cmap != null )
        {
            if (length == 1 && cmap.hasOneByteMappings()) 
            {
                retval = cmap.lookup( c, offset, length );
            }
            else if (length == 2 && cmap.hasTwoByteMappings())
            {
                retval = cmap.lookup( c, offset, length );
            }
        }
        
        // there is no cmap but probably an encoding with a suitable mapping
        if( retval == null && length == 1)
        {
            Encoding encoding = getEncoding();
            if( encoding != null )
            {
                retval = encoding.getCharacter( getCodeFromArray( c, offset, length ) );
            }
            if( retval == null && cmap == null)
            {
                retval = getStringFromArray( c, offset, length );
            }
        }
        return retval;
    }

    private static final String[] SINGLE_CHAR_STRING = new String[256];
    private static final String[][] DOUBLE_CHAR_STRING = new String[256][256];
    static
    {
        for( int i=0; i<256; i++ )
        {
            SINGLE_CHAR_STRING[i] = new String( new byte[] {(byte)i} );
            for( int j=0; j<256; j++ )
            {
                DOUBLE_CHAR_STRING[i][j] = new String( new byte[] {(byte)i, (byte)j} );
            }
        }
    }

    private static String getStringFromArray( byte[] c, int offset, int length ) throws IOException
    {
        String retval = null;
        if( length == 1 )
        {
            retval = SINGLE_CHAR_STRING[(c[offset]+256)%256];
        }
        else if( length == 2 )
        {
            retval = DOUBLE_CHAR_STRING[(c[offset]+256)%256][(c[offset+1]+256)%256];
        }
        else
        {
            throw new IOException( "Error:Unknown character length:" + length );
        }
        return retval;
    }

    private void parseCmap( String cmapRoot, InputStream cmapStream, COSName encodingName )
    {
        if( cmapStream != null )
        {
            CMapParser parser = new CMapParser();
            try 
            {
                cmap = parser.parse( cmapRoot, cmapStream );
                if( encodingName != null )
                {
                    cmapObjects.put( encodingName.getName(), cmap );
                }
            }
            catch (IOException exception) {}
        }
    }

    /**
     * The will set the encoding for this font.
     *
     * @param enc The font encoding.
     */
    public void setEncoding( Encoding enc )
    {
        font.setItem( COSName.ENCODING, enc );
        fontEncoding = enc;
    }

    /**
     * This will get or create the encoder.
     *
     * modified by Christophe Huault : DGBS Strasbourg huault@free.fr october 2004
     *
     * @return The encoding to use.
     *
     * @throws IOException If there is an error getting the encoding.
     */
    public Encoding getEncoding() throws IOException
    {
        return fontEncoding;
    }

    /**
     * This will always return "Font" for fonts.
     *
     * @return The type of object that this is.
     */
    public String getType()
    {
        return font.getNameAsString( COSName.TYPE );
    }

    // Memorized values to avoid repeated dictionary lookups
    private String subtype = null;
    private boolean type1Font;
    private boolean trueTypeFont;
    private boolean typeFont;

    /**
     * This will get the subtype of font, Type1, Type3, ...
     *
     * @return The type of font that this is.
     */
    public String getSubType()
    {
        if (subtype == null) {
            subtype = font.getNameAsString( COSName.SUBTYPE );
            type1Font = "Type1".equals(subtype);
            trueTypeFont = "TrueType".equals(subtype);
            typeFont = type1Font || "Type0".equals(subtype) || trueTypeFont;
        }
        return subtype;
    }

    private boolean isType1Font() {
        getSubType();
        return type1Font;
    }

    private boolean isTrueTypeFont() {
        getSubType();
        return trueTypeFont;
    }

    private boolean isTypeFont() {
        getSubType();
        return typeFont;
    }

    /**
     * The PostScript name of the font.
     *
     * @return The postscript name of the font.
     */
    public String getBaseFont()
    {
        return font.getNameAsString( COSName.BASE_FONT );
    }

    /**
     * Set the PostScript name of the font.
     *
     * @param baseFont The postscript name for the font.
     */
    public void setBaseFont( String baseFont )
    {
        font.setName( COSName.BASE_FONT, baseFont );
    }

    /**
     * The code for the first char or -1 if there is none.
     *
     * @return The code for the first character.
     */
    public int getFirstChar()
    {
        return font.getInt( COSName.FIRST_CHAR, -1 );
    }

    /**
     * Set the first character this font supports.
     *
     * @param firstChar The first character.
     */
    public void setFirstChar( int firstChar )
    {
        font.setInt( COSName.FIRST_CHAR, firstChar );
    }

    /**
     * The code for the last char or -1 if there is none.
     *
     * @return The code for the last character.
     */
    public int getLastChar()
    {
        return font.getInt( COSName.LAST_CHAR, -1 );
    }

    /**
     * Set the last character this font supports.
     *
     * @param lastChar The last character.
     */
    public void setLastChar( int lastChar )
    {
        font.setInt( COSName.LAST_CHAR, lastChar );
    }

    /**
     * The widths of the characters.  This will be null for the standard 14 fonts.
     *
     * @return The widths of the characters.
     */
    public List getWidths()
    {
        COSArray array = (COSArray)font.getDictionaryObject( COSName.WIDTHS );
        return COSArrayList.convertFloatCOSArrayToList( array );
    }

    /**
     * Set the widths of the characters code.
     *
     * @param widths The widths of the character codes.
     */
    public void setWidths( List widths )
    {
        font.setItem( COSName.WIDTHS, COSArrayList.converterToCOSArray( widths ) );
    }

    /**
     * This will get the matrix that is used to transform glyph space to
     * text space.  By default there are 1000 glyph units to 1 text space
     * unit, but type3 fonts can use any value.
     *
     * Note:If this is a type3 font then it can be modified via the PDType3Font.setFontMatrix, otherwise this
     * is a read-only property.
     *
     * @return The matrix to transform from glyph space to text space.
     */
    public PDMatrix getFontMatrix()
    {
        PDMatrix matrix = null;
        COSArray array = (COSArray)font.getDictionaryObject( COSName.FONT_MATRIX );
        if( array == null )
        {
            array = new COSArray();
            array.add( new COSFloat( 0.001f ) );
            array.add( COSInteger.ZERO );
            array.add( COSInteger.ZERO );
            array.add( new COSFloat( 0.001f ) );
            array.add( COSInteger.ZERO );
            array.add( COSInteger.ZERO );
        }
        matrix = new PDMatrix(array);

        return matrix;
    }

    /**
     * Tries to get the encoding for the type1 font.
     *
     */
    private void getEncodingFromFont()
    {
        // This whole section of code needs to be replaced with an actual type1 font parser!!
        // Get the font program from the embedded type font.
        if (isType1Font()) {
            COSDictionary fontDescriptor = (COSDictionary) font.getDictionaryObject(
                COSName.FONT_DESC);
            if( fontDescriptor != null )
            {
                COSStream fontFile = (COSStream) fontDescriptor.getDictionaryObject(
                    COSName.FONT_FILE);
                if( fontFile != null )
                {
                    try 
                    {
                        BufferedReader in =
                                new BufferedReader(new InputStreamReader(fontFile.getUnfilteredStream()));
                        
                        // this section parses the font program stream searching for a /Encoding entry
                        // if it contains an array of values a Type1Encoding will be returned
                        // if it encoding contains an encoding name the corresponding Encoding will be returned
                        String line = "";
                        Type1Encoding encoding = null;
                        while( (line = in.readLine()) != null)
                        {
                            if (line.startsWith("currentdict end")) {
                                if (encoding != null)
                                    fontEncoding = encoding;
                                break;
                            }
                            if (line.startsWith("/Encoding")) 
                            {
                                if(line.endsWith("array")) 
                                {
                                    StringTokenizer st = new StringTokenizer(line);
                                    // ignore the first token
                                    st.nextElement();
                                    int arraySize = Integer.parseInt(st.nextToken());
                                    encoding = new Type1Encoding(arraySize);
                                }
                                // if there is already an encoding, we don't need to
                                // assign another one
                                else if (fontEncoding == null)
                                {
                                    StringTokenizer st = new StringTokenizer(line);
                                    // ignore the first token
                                    st.nextElement();
                                    String type1Encoding = st.nextToken();
                                    fontEncoding =
                                        EncodingManager.INSTANCE.getEncoding(
                                                COSName.getPDFName(type1Encoding));
                                    break;
                                }
                            }
                            else if (line.startsWith("dup")) {
                                StringTokenizer st = new StringTokenizer(line);
                                // ignore the first token
                                st.nextElement();
                                int index = Integer.parseInt(st.nextToken());
                                String name = st.nextToken();
                                encoding.addCharacterEncoding(index, name.replace("/", ""));
                            }
                        }
                        in.close();
                    }
                    catch(IOException exception) 
                    {
                        log.error("Error: Could not extract the encoding from the embedded type1 font.");
                    }
                }
            }
        }
    }

    /**
     * This will get the fonts bounding box.
     *
     * @return The fonts bounding box.
     *
     * @throws IOException If there is an error getting the bounding box.
     */
    public abstract PDRectangle getFontBoundingBox() throws IOException;

    /**
     * {@inheritDoc}
     */
    public boolean equals( Object other )
    {
        return other instanceof PDFont && ((PDFont)other).getCOSObject() == this.getCOSObject();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return this.getCOSObject().hashCode();
    }

}
