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
package org.apache.fontbox.afm;

import java.io.InputStream;
import java.io.IOException;

import java.util.StringTokenizer;

import org.apache.fontbox.util.BoundingBox;

/**
 * This class is used to parse AFM(Adobe Font Metrics) documents.
 *
 * @see <A href="http://partners.adobe.com/asn/developer/type/">AFM Documentation</A>
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * 
 */
public class AFMParser
{
    /**
     * This is a comment in a AFM file.
     */
    public static final String COMMENT = "Comment";
    /**
     * This is the constant used in the AFM file to start a font metrics item.
     */
    public static final String START_FONT_METRICS = "StartFontMetrics";
    /**
     * This is the constant used in the AFM file to end a font metrics item.
     */
    public static final String END_FONT_METRICS = "EndFontMetrics";
    /**
     * This is the font name.
     */
    public static final String FONT_NAME = "FontName";
    /**
     * This is the full name.
     */
    public static final String FULL_NAME = "FullName";
    /**
     * This is the Family name.
     */
    public static final String FAMILY_NAME = "FamilyName";
    /**
     * This is the weight.
     */
    public static final String WEIGHT = "Weight";
    /**
     * This is the font bounding box.
     */
    public static final String FONT_BBOX = "FontBBox";
    /**
     * This is the version of the font.
     */
    public static final String VERSION = "Version";
    /**
     * This is the notice.
     */
    public static final String NOTICE = "Notice";
    /**
     * This is the encoding scheme.
     */
    public static final String ENCODING_SCHEME = "EncodingScheme";
    /**
     * This is the mapping scheme.
     */
    public static final String MAPPING_SCHEME = "MappingScheme";
    /**
     * This is the escape character.
     */
    public static final String ESC_CHAR = "EscChar";
    /**
     * This is the character set.
     */
    public static final String CHARACTER_SET = "CharacterSet";
    /**
     * This is the characters attribute.
     */
    public static final String CHARACTERS = "Characters";
    /**
     * This will determine if this is a base font.
     */
    public static final String IS_BASE_FONT = "IsBaseFont";
    /**
     * This is the V Vector attribute.
     */
    public static final String V_VECTOR = "VVector";
    /**
     * This will tell if the V is fixed.
     */
    public static final String IS_FIXED_V = "IsFixedV";
    /**
     * This is the cap height attribute.
     */
    public static final String CAP_HEIGHT = "CapHeight";
    /**
     * This is the X height.
     */
    public static final String X_HEIGHT = "XHeight";
    /**
     * This is ascender attribute.
     */
    public static final String ASCENDER = "Ascender";
    /**
     * This is the descender attribute.
     */
    public static final String DESCENDER = "Descender";

    /**
     * The underline position.
     */
    public static final String UNDERLINE_POSITION = "UnderlinePosition";
    /**
     * This is the Underline thickness.
     */
    public static final String UNDERLINE_THICKNESS = "UnderlineThickness";
    /**
     * This is the italic angle.
     */
    public static final String ITALIC_ANGLE = "ItalicAngle";
    /**
     * This is the char width.
     */
    public static final String CHAR_WIDTH = "CharWidth";
    /**
     * This will determine if this is fixed pitch.
     */
    public static final String IS_FIXED_PITCH = "IsFixedPitch";
    /**
     * This is the start of character metrics.
     */
    public static final String START_CHAR_METRICS = "StartCharMetrics";
    /**
     * This is the end of character metrics.
     */
    public static final String END_CHAR_METRICS = "EndCharMetrics";
    /**
     * The character metrics c value.
     */
    public static final String CHARMETRICS_C = "C";
    /**
     * The character metrics c value.
     */
    public static final String CHARMETRICS_CH = "CH";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_WX = "WX";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_W0X = "W0X";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_W1X = "W1X";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_WY = "WY";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_W0Y = "W0Y";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_W1Y = "W1Y";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_W = "W";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_W0 = "W0";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_W1 = "W1";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_VV = "VV";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_N = "N";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_B = "B";
    /**
     * The character metrics value.
     */
    public static final String CHARMETRICS_L = "L";
    /**
     * The character metrics value.
     */
    public static final String STD_HW = "StdHW";
    /**
     * The character metrics value.
     */
    public static final String STD_VW = "StdVW";
    /**
     * This is the start of track kern data.
     */
    public static final String START_TRACK_KERN = "StartTrackKern";
    /**
     * This is the end of track kern data.
     */
    public static final String END_TRACK_KERN = "EndTrackKern";
    /**
     * This is the start of kern data.
     */
    public static final String START_KERN_DATA = "StartKernData";
    /**
     * This is the end of kern data.
     */
    public static final String END_KERN_DATA = "EndKernData";
    /**
     * This is the start of kern pairs data.
     */
    public static final String START_KERN_PAIRS = "StartKernPairs";
    /**
     * This is the end of kern pairs data.
     */
    public static final String END_KERN_PAIRS = "EndKernPairs";
    /**
     * This is the start of kern pairs data.
     */
    public static final String START_KERN_PAIRS0 = "StartKernPairs0";
    /**
     * This is the start of kern pairs data.
     */
    public static final String START_KERN_PAIRS1 = "StartKernPairs1";
    /**
     * This is the start compisites data section.
     */
    public static final String START_COMPOSITES = "StartComposites";
    /**
     * This is the end compisites data section.
     */
    public static final String END_COMPOSITES = "EndComposites";
    /**
     * This is a composite character.
     */
    public static final String CC = "CC";
    /**
     * This is a composite charater part.
     */
    public static final String PCC = "PCC";
    /**
     * This is a kern pair.
     */
    public static final String KERN_PAIR_KP = "KP";
    /**
     * This is a kern pair.
     */
    public static final String KERN_PAIR_KPH = "KPH";
    /**
     * This is a kern pair.
     */
    public static final String KERN_PAIR_KPX = "KPX";
    /**
     * This is a kern pair.
     */
    public static final String KERN_PAIR_KPY = "KPY";

    private static final int BITS_IN_HEX = 16;


    private InputStream input;

    /**
     * A method to test parsing of all AFM documents in the resources
     * directory.
     *
     * @param args Ignored.
     *
     * @throws IOException If there is an error parsing one of the documents.
     */
    public static void main( String[] args ) throws IOException
    {
        java.io.File afmDir = new java.io.File( "Resources/afm" );
        java.io.File[] files = afmDir.listFiles();
        for( int i=0; i< files.length; i++ )
        {
            if( files[i].getPath().toUpperCase().endsWith( ".AFM" ) )
            {
                long start = System.currentTimeMillis();
                java.io.FileInputStream input = new java.io.FileInputStream( files[i] );
                AFMParser parser = new AFMParser( input );
                parser.parse();
                long stop = System.currentTimeMillis();
                System.out.println( "Parsing:" + files[i].getPath() + " " + (stop-start) );
            }
        }
    }

    /**
     * Constructor.
     *
     * @param in The input stream to read the AFM document from.
     */
    public AFMParser( InputStream in )
    {
        input = in;
    }

    /**
     * This will parse the AFM document.  This will close the Input stream
     * when the parsing is finished.
     *
     * @return the parsed FontMetric
     * 
     * @throws IOException If there is an IO error reading the document.
     */
    public FontMetric parse() throws IOException
    {
    	return parseFontMetric();
    }

    /**
     * This will parse a font metrics item.
     *
     * @return The parse font metrics item.
     *
     * @throws IOException If there is an error reading the AFM file.
     */
    private FontMetric parseFontMetric() throws IOException
    {
        FontMetric fontMetrics = new FontMetric();
        String startFontMetrics = readString();
        if( !START_FONT_METRICS.equals( startFontMetrics ) )
        {
            throw new IOException( "Error: The AFM file should start with " + START_FONT_METRICS +
                                   " and not '" + startFontMetrics + "'" );
        }
        fontMetrics.setAFMVersion( readFloat() );
        String nextCommand = null;
        while( !END_FONT_METRICS.equals( (nextCommand = readString() ) ) )
        {
            if( FONT_NAME.equals( nextCommand ) )
            {
                fontMetrics.setFontName( readLine() );
            }
            else if( FULL_NAME.equals( nextCommand ) )
            {
                fontMetrics.setFullName( readLine() );
            }
            else if( FAMILY_NAME.equals( nextCommand ) )
            {
                fontMetrics.setFamilyName( readLine() );
            }
            else if( WEIGHT.equals( nextCommand ) )
            {
                fontMetrics.setWeight( readLine() );
            }
            else if( FONT_BBOX.equals( nextCommand ) )
            {
                BoundingBox bBox = new BoundingBox();
                bBox.setLowerLeftX( readFloat() );
                bBox.setLowerLeftY( readFloat() );
                bBox.setUpperRightX( readFloat() );
                bBox.setUpperRightY( readFloat() );
                fontMetrics.setFontBBox( bBox );
            }
            else if( VERSION.equals( nextCommand ) )
            {
                fontMetrics.setFontVersion( readLine() );
            }
            else if( NOTICE.equals( nextCommand ) )
            {
                fontMetrics.setNotice( readLine() );
            }
            else if( ENCODING_SCHEME.equals( nextCommand ) )
            {
                fontMetrics.setEncodingScheme( readLine() );
            }
            else if( MAPPING_SCHEME.equals( nextCommand ) )
            {
                fontMetrics.setMappingScheme( readInt() );
            }
            else if( ESC_CHAR.equals( nextCommand ) )
            {
                fontMetrics.setEscChar( readInt() );
            }
            else if( CHARACTER_SET.equals( nextCommand ) )
            {
                fontMetrics.setCharacterSet( readLine() );
            }
            else if( CHARACTERS.equals( nextCommand ) )
            {
                fontMetrics.setCharacters( readInt() );
            }
            else if( IS_BASE_FONT.equals( nextCommand ) )
            {
                fontMetrics.setIsBaseFont( readBoolean() );
            }
            else if( V_VECTOR.equals( nextCommand ) )
            {
                float[] vector = new float[2];
                vector[0] = readFloat();
                vector[1] = readFloat();
                fontMetrics.setVVector( vector );
            }
            else if( IS_FIXED_V.equals( nextCommand ) )
            {
                fontMetrics.setIsFixedV( readBoolean() );
            }
            else if( CAP_HEIGHT.equals( nextCommand ) )
            {
                fontMetrics.setCapHeight( readFloat() );
            }
            else if( X_HEIGHT.equals( nextCommand ) )
            {
                fontMetrics.setXHeight( readFloat() );
            }
            else if( ASCENDER.equals( nextCommand ) )
            {
                fontMetrics.setAscender( readFloat() );
            }
            else if( DESCENDER.equals( nextCommand ) )
            {
                fontMetrics.setDescender( readFloat() );
            }
            else if( STD_HW.equals( nextCommand ) )
            {
                fontMetrics.setStandardHorizontalWidth( readFloat() );
            }
            else if( STD_VW.equals( nextCommand ) )
            {
                fontMetrics.setStandardVerticalWidth( readFloat() );
            }
            else if( COMMENT.equals( nextCommand ) )
            {
                fontMetrics.addComment( readLine() );
            }
            else if( UNDERLINE_POSITION.equals( nextCommand ) )
            {
                fontMetrics.setUnderlinePosition( readFloat() );
            }
            else if( UNDERLINE_THICKNESS.equals( nextCommand ) )
            {
                fontMetrics.setUnderlineThickness( readFloat() );
            }
            else if( ITALIC_ANGLE.equals( nextCommand ) )
            {
                fontMetrics.setItalicAngle( readFloat() );
            }
            else if( CHAR_WIDTH.equals( nextCommand ) )
            {
                float[] widths = new float[2];
                widths[0] = readFloat();
                widths[1] = readFloat();
                fontMetrics.setCharWidth( widths );
            }
            else if( IS_FIXED_PITCH.equals( nextCommand ) )
            {
                fontMetrics.setFixedPitch( readBoolean() );
            }
            else if( START_CHAR_METRICS.equals( nextCommand ) )
            {
                int count = readInt();
                for( int i=0; i<count; i++ )
                {
                    CharMetric charMetric = parseCharMetric();
                    fontMetrics.addCharMetric( charMetric );
                }
                String end = readString();
                if( !end.equals( END_CHAR_METRICS ) )
                {
                    throw new IOException( "Error: Expected '" + END_CHAR_METRICS + "' actual '" +
                                                end + "'" );
                }
            }
            else if( START_COMPOSITES.equals( nextCommand ) )
            {
                int count = readInt();
                for( int i=0; i<count; i++ )
                {
                    Composite part = parseComposite();
                    fontMetrics.addComposite( part );
                }
                String end = readString();
                if( !end.equals( END_COMPOSITES ) )
                {
                    throw new IOException( "Error: Expected '" + END_COMPOSITES + "' actual '" +
                                                end + "'" );
                }
            }
            else if( START_KERN_DATA.equals( nextCommand ) )
            {
                parseKernData( fontMetrics );
            }
            else
            {
                throw new IOException( "Unknown AFM key '" + nextCommand + "'" );
            }
        }
        return fontMetrics;
    }

    /**
     * This will parse the kern data.
     *
     * @param fontMetrics The metrics class to put the parsed data into.
     *
     * @throws IOException If there is an error parsing the data.
     */
    private void parseKernData( FontMetric fontMetrics ) throws IOException
    {
        String nextCommand = null;
        while( !(nextCommand = readString()).equals( END_KERN_DATA ) )
        {
            if( START_TRACK_KERN.equals( nextCommand ) )
            {
                int count = readInt();
                for( int i=0; i<count; i++ )
                {
                    TrackKern kern = new TrackKern();
                    kern.setDegree( readInt() );
                    kern.setMinPointSize( readFloat() );
                    kern.setMinKern( readFloat() );
                    kern.setMaxPointSize( readFloat() );
                    kern.setMaxKern( readFloat() );
                    fontMetrics.addTrackKern( kern );
                }
                String end = readString();
                if( !end.equals( END_TRACK_KERN ) )
                {
                    throw new IOException( "Error: Expected '" + END_TRACK_KERN + "' actual '" +
                                                end + "'" );
                }
            }
            else if( START_KERN_PAIRS.equals( nextCommand ) )
            {
                int count = readInt();
                for( int i=0; i<count; i++ )
                {
                    KernPair pair = parseKernPair();
                    fontMetrics.addKernPair( pair );
                }
                String end = readString();
                if( !end.equals( END_KERN_PAIRS ) )
                {
                    throw new IOException( "Error: Expected '" + END_KERN_PAIRS + "' actual '" +
                                                end + "'" );
                }
            }
            else if( START_KERN_PAIRS0.equals( nextCommand ) )
            {
                int count = readInt();
                for( int i=0; i<count; i++ )
                {
                    KernPair pair = parseKernPair();
                    fontMetrics.addKernPair0( pair );
                }
                String end = readString();
                if( !end.equals( END_KERN_PAIRS ) )
                {
                    throw new IOException( "Error: Expected '" + END_KERN_PAIRS + "' actual '" +
                                                end + "'" );
                }
            }
            else if( START_KERN_PAIRS1.equals( nextCommand ) )
            {
                int count = readInt();
                for( int i=0; i<count; i++ )
                {
                    KernPair pair = parseKernPair();
                    fontMetrics.addKernPair1( pair );
                }
                String end = readString();
                if( !end.equals( END_KERN_PAIRS ) )
                {
                    throw new IOException( "Error: Expected '" + END_KERN_PAIRS + "' actual '" +
                                                end + "'" );
                }
            }
            else
            {
                throw new IOException( "Unknown kerning data type '" + nextCommand + "'" );
            }
        }
    }

    /**
     * This will parse a kern pair from the data stream.
     *
     * @return The kern pair that was parsed from the stream.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    private KernPair parseKernPair() throws IOException
    {
        KernPair kernPair = new KernPair();
        String cmd = readString();
        if( KERN_PAIR_KP.equals( cmd ) )
        {
            String first = readString();
            String second = readString();
            float x = readFloat();
            float y = readFloat();
            kernPair.setFirstKernCharacter( first );
            kernPair.setSecondKernCharacter( second );
            kernPair.setX( x );
            kernPair.setY( y );
        }
        else if( KERN_PAIR_KPH.equals( cmd ) )
        {
            String first = hexToString( readString() );
            String second = hexToString( readString() );
            float x = readFloat();
            float y = readFloat();
            kernPair.setFirstKernCharacter( first );
            kernPair.setSecondKernCharacter( second );
            kernPair.setX( x );
            kernPair.setY( y );
        }
        else if( KERN_PAIR_KPX.equals( cmd ) )
        {
            String first = readString();
            String second = readString();
            float x = readFloat();
            kernPair.setFirstKernCharacter( first );
            kernPair.setSecondKernCharacter( second );
            kernPair.setX( x );
            kernPair.setY( 0 );
        }
        else if( KERN_PAIR_KPY.equals( cmd ) )
        {
            String first = readString();
            String second = readString();
            float y = readFloat();
            kernPair.setFirstKernCharacter( first );
            kernPair.setSecondKernCharacter( second );
            kernPair.setX( 0 );
            kernPair.setY( y );
        }
        else
        {
            throw new IOException( "Error expected kern pair command actual='" + cmd + "'" );
        }
        return kernPair;
    }

    /**
     * This will convert and angle bracket hex string to a string.
     *
     * @param hexString An angle bracket string.
     *
     * @return The bytes of the hex string.
     *
     * @throws IOException If the string is in an invalid format.
     */
    private String hexToString( String hexString ) throws IOException
    {
        if( hexString.length() < 2 )
        {
            throw new IOException( "Error: Expected hex string of length >= 2 not='" + hexString );
        }
        if( hexString.charAt( 0 ) != '<' ||
            hexString.charAt( hexString.length() -1 ) != '>' )
        {
            throw new IOException( "String should be enclosed by angle brackets '" + hexString+ "'" );
        }
        hexString = hexString.substring( 1, hexString.length() -1 );
        byte[] data = new byte[ (hexString.length() / 2) ];
        for( int i=0; i<hexString.length(); i+=2 )
        {
            String hex = "" + hexString.charAt( i ) + hexString.charAt( i+1 );
            try
            {
                data[ i / 2 ] = (byte)Integer.parseInt( hex, BITS_IN_HEX );
            }
            catch( NumberFormatException e )
            {
                throw new IOException( "Error parsing AFM file:" + e );
            }
        }
        return new String( data, "ISO-8859-1" );
    }

    /**
     * This will parse a composite part from the stream.
     *
     * @return The composite.
     *
     * @throws IOException If there is an error parsing the composite.
     */
    private Composite parseComposite() throws IOException
    {
        Composite composite = new Composite();
        String partData = readLine();
        StringTokenizer tokenizer = new StringTokenizer( partData, " ;" );


        String cc = tokenizer.nextToken();
        if( !cc.equals( CC ) )
        {
            throw new IOException( "Expected '" + CC + "' actual='" + cc + "'" );
        }
        String name = tokenizer.nextToken();
        composite.setName( name );

        int partCount;
        try
        {
            partCount = Integer.parseInt( tokenizer.nextToken() );
        }
        catch( NumberFormatException e )
        {
            throw new IOException( "Error parsing AFM document:" + e );
        }
        for( int i=0; i<partCount; i++ )
        {
            CompositePart part = new CompositePart();
            String pcc = tokenizer.nextToken();
            if( !pcc.equals( PCC ) )
            {
                throw new IOException( "Expected '" + PCC + "' actual='" + pcc + "'" );
            }
            String partName = tokenizer.nextToken();
            try
            {
                int x = Integer.parseInt( tokenizer.nextToken() );
                int y = Integer.parseInt( tokenizer.nextToken() );

                part.setName( partName );
                part.setXDisplacement( x );
                part.setYDisplacement( y );
                composite.addPart( part );
            }
            catch( NumberFormatException e )
            {
                throw new IOException( "Error parsing AFM document:" + e );
            }
        }
        return composite;
    }

    /**
     * This will parse a single CharMetric object from the stream.
     *
     * @return The next char metric in the stream.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    private CharMetric parseCharMetric() throws IOException
    {
        CharMetric charMetric = new CharMetric();
        String metrics = readLine();
        StringTokenizer metricsTokenizer = new StringTokenizer( metrics );
        try
        {
            while( metricsTokenizer.hasMoreTokens() )
            {
                String nextCommand = metricsTokenizer.nextToken();
                if( nextCommand.equals( CHARMETRICS_C ) )
                {
                    String charCode = metricsTokenizer.nextToken();
                    charMetric.setCharacterCode( Integer.parseInt( charCode ) );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_CH ) )
                {
                    //Is the hex string <FF> or FF, the spec is a little
                    //unclear, wait and see if it breaks anything.
                    String charCode = metricsTokenizer.nextToken();
                    charMetric.setCharacterCode( Integer.parseInt( charCode, BITS_IN_HEX ) );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_WX ) )
                {
                    String wx = metricsTokenizer.nextToken();
                    charMetric.setWx( Float.parseFloat( wx ) );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_W0X ) )
                {
                    String w0x = metricsTokenizer.nextToken();
                    charMetric.setW0x( Float.parseFloat( w0x ) );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_W1X ) )
                {
                    String w1x = metricsTokenizer.nextToken();
                    charMetric.setW0x( Float.parseFloat( w1x ) );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_WY ) )
                {
                    String wy = metricsTokenizer.nextToken();
                    charMetric.setWy( Float.parseFloat( wy ) );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_W0Y ) )
                {
                    String w0y = metricsTokenizer.nextToken();
                    charMetric.setW0y( Float.parseFloat( w0y ) );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_W1Y ) )
                {
                    String w1y = metricsTokenizer.nextToken();
                    charMetric.setW0y( Float.parseFloat( w1y ) );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_W ) )
                {
                    String w0 = metricsTokenizer.nextToken();
                    String w1 = metricsTokenizer.nextToken();
                    float[] w = new float[2];
                    w[0] = Float.parseFloat( w0 );
                    w[1] = Float.parseFloat( w1 );
                    charMetric.setW( w );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_W0 ) )
                {
                    String w00 = metricsTokenizer.nextToken();
                    String w01 = metricsTokenizer.nextToken();
                    float[] w0 = new float[2];
                    w0[0] = Float.parseFloat( w00 );
                    w0[1] = Float.parseFloat( w01 );
                    charMetric.setW0( w0 );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_W1 ) )
                {
                    String w10 = metricsTokenizer.nextToken();
                    String w11 = metricsTokenizer.nextToken();
                    float[] w1 = new float[2];
                    w1[0] = Float.parseFloat( w10 );
                    w1[1] = Float.parseFloat( w11 );
                    charMetric.setW1( w1 );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_VV ) )
                {
                    String vv0 = metricsTokenizer.nextToken();
                    String vv1 = metricsTokenizer.nextToken();
                    float[] vv = new float[2];
                    vv[0] = Float.parseFloat( vv0 );
                    vv[1] = Float.parseFloat( vv1 );
                    charMetric.setVv( vv );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_N ) )
                {
                    String name = metricsTokenizer.nextToken();
                    charMetric.setName( name );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_B ) )
                {
                    String llx = metricsTokenizer.nextToken();
                    String lly = metricsTokenizer.nextToken();
                    String urx = metricsTokenizer.nextToken();
                    String ury = metricsTokenizer.nextToken();
                    BoundingBox box = new BoundingBox();
                    box.setLowerLeftX( Float.parseFloat( llx ) );
                    box.setLowerLeftY( Float.parseFloat( lly ) );
                    box.setUpperRightX( Float.parseFloat( urx ) );
                    box.setUpperRightY( Float.parseFloat( ury ) );
                    charMetric.setBoundingBox( box );
                    verifySemicolon( metricsTokenizer );
                }
                else if( nextCommand.equals( CHARMETRICS_L ) )
                {
                    String successor = metricsTokenizer.nextToken();
                    String ligature = metricsTokenizer.nextToken();
                    Ligature lig = new Ligature();
                    lig.setSuccessor( successor );
                    lig.setLigature( ligature );
                    charMetric.addLigature( lig );
                    verifySemicolon( metricsTokenizer );
                }
                else
                {
                    throw new IOException( "Unknown CharMetrics command '" + nextCommand + "'" );
                }
            }
        }
        catch( NumberFormatException e )
        {
            throw new IOException( "Error: Corrupt AFM document:"  + e );
        }
        return charMetric;
    }

    /**
     * This is used to verify that a semicolon is the next token in the stream.
     *
     * @param tokenizer The tokenizer to read from.
     *
     * @throws IOException If the semicolon is missing.
     */
    private void verifySemicolon( StringTokenizer tokenizer ) throws IOException
    {
        if( tokenizer.hasMoreTokens() )
        {
            String semicolon = tokenizer.nextToken();
            if( !semicolon.equals( ";" ) )
            {
                throw new IOException( "Error: Expected semicolon in stream actual='" +
                                            semicolon + "'" );
            }
        }
        else
        {
            throw new IOException( "CharMetrics is missing a semicolon after a command" );
        }
    }

    /**
     * This will read a boolean from the stream.
     *
     * @return The boolean in the stream.
     */
    private boolean readBoolean() throws IOException
    {
        String theBoolean = readString();
        return Boolean.valueOf( theBoolean ).booleanValue();
    }

    /**
     * This will read an integer from the stream.
     *
     * @return The integer in the stream.
     */
    private int readInt() throws IOException
    {
        String theInt = readString();
        try
        {
            return Integer.parseInt( theInt );
        }
        catch( NumberFormatException e )
        {
            throw new IOException( "Error parsing AFM document:" + e );
        }
    }

    /**
     * This will read a float from the stream.
     *
     * @return The float in the stream.
     */
    private float readFloat() throws IOException
    {
        String theFloat = readString();
        return Float.parseFloat( theFloat );
    }

    /**
     * This will read until the end of a line.
     *
     * @return The string that is read.
     */
    private String readLine() throws IOException
    {
        //First skip the whitespace
        StringBuffer buf = new StringBuffer();
        int nextByte = input.read();
        while( isWhitespace( nextByte ) )
        {
            nextByte = input.read();
            //do nothing just skip the whitespace.
        }
        buf.append( (char)nextByte );

        //now read the data
        while( !isEOL(nextByte = input.read()) )
        {
            buf.append( (char)nextByte );
        }
        return buf.toString();
    }

    /**
     * This will read a string from the input stream and stop at any whitespace.
     *
     * @return The string read from the stream.
     *
     * @throws IOException If an IO error occurs when reading from the stream.
     */
    private String readString() throws IOException
    {
        //First skip the whitespace
        StringBuffer buf = new StringBuffer();
        int nextByte = input.read();
        while( isWhitespace( nextByte ) )
        {
            nextByte = input.read();
            //do nothing just skip the whitespace.
        }
        buf.append( (char)nextByte );

        //now read the data
        while( !isWhitespace(nextByte = input.read()) )
        {
            buf.append( (char)nextByte );
        }
        return buf.toString();
    }

    /**
     * This will determine if the byte is a whitespace character or not.
     *
     * @param character The character to test for whitespace.
     *
     * @return true If the character is whitespace as defined by the AFM spec.
     */
    private boolean isEOL( int character )
    {
        return character == 0x0D ||
               character == 0x0A;
    }

    /**
     * This will determine if the byte is a whitespace character or not.
     *
     * @param character The character to test for whitespace.
     *
     * @return true If the character is whitespace as defined by the AFM spec.
     */
    private boolean isWhitespace( int character )
    {
        return character == ' ' ||
               character == '\t' ||
               character == 0x0D ||
               character == 0x0A;
    }
}