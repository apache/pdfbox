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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import org.apache.fontbox.util.BoundingBox;

/**
 * This class is used to parse AFM(Adobe Font Metrics) documents.
 *
 * @see <A href="http://partners.adobe.com/asn/developer/type/">AFM Documentation</A>
 *
 * @author Ben Litchfield
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
     * This is the start composites data section.
     */
    public static final String START_COMPOSITES = "StartComposites";
    /**
     * This is the end composites data section.
     */
    public static final String END_COMPOSITES = "EndComposites";
    /**
     * This is a composite character.
     */
    public static final String CC = "CC";
    /**
     * This is a composite character part.
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


    private final InputStream input;

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
     * This will parse the AFM document. The input stream is closed
     * when the parsing is finished.
     *
     * @return the parsed FontMetric
     * 
     * @throws IOException If there is an IO error reading the document.
     */
    public FontMetrics parse() throws IOException
    {
        return parseFontMetric(false);
    }

    /**
     * This will parse the AFM document. The input stream is closed
     * when the parsing is finished.
     *
     * @param reducedDataset parse a reduced subset of data if set to true
     * @return the parsed FontMetric
     * 
     * @throws IOException If there is an IO error reading the document.
     */
    public FontMetrics parse(boolean reducedDataset) throws IOException
    {
        return parseFontMetric(reducedDataset);
    }
    /**
     * This will parse a font metrics item.
     *
     * @return The parse font metrics item.
     *
     * @throws IOException If there is an error reading the AFM file.
     */
    private FontMetrics parseFontMetric(boolean reducedDataset) throws IOException
    {
        readCommand(START_FONT_METRICS);
        FontMetrics fontMetrics = new FontMetrics();
        fontMetrics.setAFMVersion( readFloat() );
        String nextCommand;
        boolean charMetricsRead = false;
        while (!END_FONT_METRICS.equals(nextCommand = readString()))
        {
            switch (nextCommand)
            {
            case FONT_NAME:
                fontMetrics.setFontName( readLine() );
                break;
            case FULL_NAME:
                fontMetrics.setFullName( readLine() );
                break;
            case FAMILY_NAME:
                fontMetrics.setFamilyName( readLine() );
                break;
            case WEIGHT:
                fontMetrics.setWeight( readLine() );
                break;
            case FONT_BBOX:
                BoundingBox bBox = new BoundingBox();
                bBox.setLowerLeftX( readFloat() );
                bBox.setLowerLeftY( readFloat() );
                bBox.setUpperRightX( readFloat() );
                bBox.setUpperRightY( readFloat() );
                fontMetrics.setFontBBox( bBox );
                break;
            case VERSION:
                fontMetrics.setFontVersion( readLine() );
                break;
            case NOTICE:
                fontMetrics.setNotice( readLine() );
                break;
            case ENCODING_SCHEME:
                fontMetrics.setEncodingScheme( readLine() );
                break;
            case MAPPING_SCHEME:
                fontMetrics.setMappingScheme( readInt() );
                break;
            case ESC_CHAR:
                fontMetrics.setEscChar( readInt() );
                break;
            case CHARACTER_SET:
                fontMetrics.setCharacterSet( readLine() );
                break;
            case CHARACTERS:
                fontMetrics.setCharacters( readInt() );
                break;
            case IS_BASE_FONT:
                fontMetrics.setIsBaseFont( readBoolean() );
                break;
            case V_VECTOR:
                float[] vector = new float[2];
                vector[0] = readFloat();
                vector[1] = readFloat();
                fontMetrics.setVVector( vector );
                break;
            case IS_FIXED_V:
                fontMetrics.setIsFixedV( readBoolean() );
                break;
            case CAP_HEIGHT:
                fontMetrics.setCapHeight( readFloat() );
                break;
            case X_HEIGHT:
                fontMetrics.setXHeight( readFloat() );
                break;
            case ASCENDER:
                fontMetrics.setAscender( readFloat() );
                break;
            case DESCENDER:
                fontMetrics.setDescender( readFloat() );
                break;
            case STD_HW:
                fontMetrics.setStandardHorizontalWidth( readFloat() );
                break;
            case STD_VW:
                fontMetrics.setStandardVerticalWidth( readFloat() );
                break;
            case COMMENT:
                fontMetrics.addComment( readLine() );
                break;
            case UNDERLINE_POSITION:
                fontMetrics.setUnderlinePosition( readFloat() );
                break;
            case UNDERLINE_THICKNESS:
                fontMetrics.setUnderlineThickness( readFloat() );
                break;
            case ITALIC_ANGLE:
                fontMetrics.setItalicAngle( readFloat() );
                break;
            case CHAR_WIDTH:
                float[] widths = new float[2];
                widths[0] = readFloat();
                widths[1] = readFloat();
                fontMetrics.setCharWidth( widths );
                break;
            case IS_FIXED_PITCH:
                fontMetrics.setFixedPitch( readBoolean() );
                break;
            case START_CHAR_METRICS:
                charMetricsRead = parseCharMetrics(fontMetrics);
                break;
            case START_KERN_DATA:
                if( !reducedDataset)
                {
                    parseKernData(fontMetrics);
                }
                break;
            case START_COMPOSITES:
                if( !reducedDataset)
                {
                    parseComposites(fontMetrics);
                }
                break;
            default:
                if (!reducedDataset || !charMetricsRead)
                {
                    throw new IOException("Unknown AFM key '" + nextCommand + "'");
                }
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
    private void parseKernData( FontMetrics fontMetrics ) throws IOException
    {
        String nextCommand;
        while( !(nextCommand = readString()).equals( END_KERN_DATA ) )
        {
            switch(nextCommand)
            {
            case START_TRACK_KERN:
                int countTrackKern = readInt();
                for (int i = 0; i < countTrackKern; i++)
                {
                    fontMetrics.addTrackKern(new TrackKern(readInt(), readFloat(), readFloat(),
                            readFloat(), readFloat()));
                }
                readCommand(END_TRACK_KERN);
                break;
            case START_KERN_PAIRS:
                parseKernPairs(fontMetrics);
                break;
            case START_KERN_PAIRS0:
                parseKernPairs0(fontMetrics);
                break;
            case START_KERN_PAIRS1:
                parseKernPairs1(fontMetrics);
                break;
            default:
                throw new IOException( "Unknown kerning data type '" + nextCommand + "'" );
            }
        }
    }

    private void parseKernPairs(FontMetrics fontMetrics) throws IOException
    {
        int countKernPairs = readInt();
        for (int i = 0; i < countKernPairs; i++)
        {
            fontMetrics.addKernPair(parseKernPair());
        }
        readCommand(END_KERN_PAIRS);
    }

    private void parseKernPairs0(FontMetrics fontMetrics) throws IOException
    {
        int countKernPairs = readInt();
        for (int i = 0; i < countKernPairs; i++)
        {
            fontMetrics.addKernPair0(parseKernPair());
        }
        readCommand(END_KERN_PAIRS);
    }

    private void parseKernPairs1(FontMetrics fontMetrics) throws IOException
    {
        int countKernPairs = readInt();
        for (int i = 0; i < countKernPairs; i++)
        {
            fontMetrics.addKernPair1(parseKernPair());
        }
        readCommand(END_KERN_PAIRS);
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
        String cmd = readString();
        switch (cmd)
        {
        case KERN_PAIR_KP:
            return new KernPair(readString(), readString(), //
                    readFloat(), readFloat());
        case KERN_PAIR_KPH:
            return new KernPair(hexToString(readString()), hexToString(readString()), //
                    readFloat(), readFloat());
        case KERN_PAIR_KPX:
            return new KernPair(readString(), readString(), //
                    readFloat(), 0);
        case KERN_PAIR_KPY:
            return new KernPair(readString(), readString(), //
                    0, readFloat());
        default:
            throw new IOException( "Error expected kern pair command actual='" + cmd + "'" );
        }
    }

    /**
     * This will convert and angle bracket hex string to a string.
     *
     * @param hexToString An angle bracket string.
     *
     * @return The bytes of the hex string.
     *
     * @throws IOException If the string is in an invalid format.
     */
    private String hexToString(String hexToString) throws IOException
    {
        if (hexToString.length() < 2)
        {
            throw new IOException("Error: Expected hex string of length >= 2 not='" + hexToString);
        }
        if (hexToString.charAt(0) != '<' || hexToString.charAt(hexToString.length() - 1) != '>')
        {
            throw new IOException(
                    "String should be enclosed by angle brackets '" + hexToString + "'");
        }
        String hexString = hexToString.substring(1, hexToString.length() - 1);
        byte[] data = new byte[hexString.length() / 2];
        for( int i=0; i<hexString.length(); i+=2 )
        {
            String hex = Character.toString(hexString.charAt(i)) + hexString.charAt(i + 1);
            data[i / 2] = (byte) parseInt(hex, BITS_IN_HEX);
        }
        return new String( data, StandardCharsets.ISO_8859_1 );
    }

    private void parseComposites(FontMetrics fontMetrics) throws IOException
    {
        int countComposites = readInt();
        for (int i = 0; i < countComposites; i++)
        {
            fontMetrics.addComposite(parseComposite());
        }
        readCommand(END_COMPOSITES);
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
        String partData = readLine();
        StringTokenizer tokenizer = new StringTokenizer( partData, " ;" );


        String cc = tokenizer.nextToken();
        if( !cc.equals( CC ) )
        {
            throw new IOException( "Expected '" + CC + "' actual='" + cc + "'" );
        }
        String name = tokenizer.nextToken();
        Composite composite = new Composite(name);

        int partCount = parseInt(tokenizer.nextToken());
        for( int i=0; i<partCount; i++ )
        {
            String pcc = tokenizer.nextToken();
            if( !pcc.equals( PCC ) )
            {
                throw new IOException( "Expected '" + PCC + "' actual='" + pcc + "'" );
            }
            String partName = tokenizer.nextToken();
            int x = parseInt(tokenizer.nextToken());
            int y = parseInt(tokenizer.nextToken());
            composite.addPart(new CompositePart(partName, x, y));
        }
        return composite;
    }

    private boolean parseCharMetrics(FontMetrics fontMetrics) throws IOException
    {
        int countMetrics = readInt();
        for (int i = 0; i < countMetrics; i++)
        {
            fontMetrics.addCharMetric(parseCharMetric());
        }
        readCommand(END_CHAR_METRICS);
        return true;
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
        while (metricsTokenizer.hasMoreTokens())
        {
            String nextCommand = metricsTokenizer.nextToken();
            switch (nextCommand)
            {
            // top 5 most used first
            case CHARMETRICS_C:
                String charCodeC = metricsTokenizer.nextToken();
                charMetric.setCharacterCode(parseInt(charCodeC));
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_WX:
                charMetric.setWx(parseFloat(metricsTokenizer.nextToken()));
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_N:
                charMetric.setName(metricsTokenizer.nextToken());
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_B:
                BoundingBox box = new BoundingBox();
                box.setLowerLeftX(parseFloat(metricsTokenizer.nextToken()));
                box.setLowerLeftY(parseFloat(metricsTokenizer.nextToken()));
                box.setUpperRightX(parseFloat(metricsTokenizer.nextToken()));
                box.setUpperRightY(parseFloat(metricsTokenizer.nextToken()));
                charMetric.setBoundingBox(box);
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_L:
                Ligature lig = new Ligature(metricsTokenizer.nextToken(),
                        metricsTokenizer.nextToken());
                charMetric.addLigature(lig);
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_CH:
                // Is the hex string <FF> or FF, the spec is a little
                // unclear, wait and see if it breaks anything.
                String charCodeCH = metricsTokenizer.nextToken();
                charMetric.setCharacterCode(parseInt(charCodeCH, BITS_IN_HEX));
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_W0X:
                charMetric.setW0x(parseFloat(metricsTokenizer.nextToken()));
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_W1X:
                charMetric.setW1x(parseFloat(metricsTokenizer.nextToken()));
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_WY:
                charMetric.setWy(parseFloat(metricsTokenizer.nextToken()));
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_W0Y:
                charMetric.setW0y(parseFloat(metricsTokenizer.nextToken()));
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_W1Y:
                charMetric.setW1y(parseFloat(metricsTokenizer.nextToken()));
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_W:
                float[] w = new float[2];
                w[0] = parseFloat(metricsTokenizer.nextToken());
                w[1] = parseFloat(metricsTokenizer.nextToken());
                charMetric.setW(w);
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_W0:
                float[] w0 = new float[2];
                w0[0] = parseFloat(metricsTokenizer.nextToken());
                w0[1] = parseFloat(metricsTokenizer.nextToken());
                charMetric.setW0(w0);
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_W1:
                float[] w1 = new float[2];
                w1[0] = parseFloat(metricsTokenizer.nextToken());
                w1[1] = parseFloat(metricsTokenizer.nextToken());
                charMetric.setW1(w1);
                verifySemicolon(metricsTokenizer);
                break;
            case CHARMETRICS_VV:
                float[] vv = new float[2];
                vv[0] = parseFloat(metricsTokenizer.nextToken());
                vv[1] = parseFloat(metricsTokenizer.nextToken());
                charMetric.setVv(vv);
                verifySemicolon(metricsTokenizer);
                break;
            default:
                throw new IOException("Unknown CharMetrics command '" + nextCommand + "'");
            }
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
            if (!";".equals(semicolon))
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
        return Boolean.parseBoolean(readString());
    }

    /**
     * This will read an integer from the stream.
     *
     * @return The integer in the stream.
     */
    private int readInt() throws IOException
    {
        return parseInt(readString(), 10);
    }

    private int parseInt(String intValue) throws IOException
    {
        return parseInt(intValue, 10);
    }

    private int parseInt(String intValue, int radix) throws IOException
    {
        try
        {
            return Integer.parseInt(intValue, radix);
        }
        catch (NumberFormatException e)
        {
            throw new IOException("Error parsing AFM document:" + e, e);
        }
    }

    /**
     * This will read a float from the stream.
     *
     * @return The float in the stream.
     */
    private float readFloat() throws IOException
    {
        return parseFloat(readString());
    }

    private float parseFloat(String floatValue) throws IOException
    {
        try
        {
            return Float.parseFloat(floatValue);
        }
        catch (NumberFormatException e)
        {
            throw new IOException("Error parsing AFM document:" + e, e);
        }
    }

    /**
     * This will read until the end of a line.
     *
     * @return The string that is read.
     */
    private String readLine() throws IOException
    {
        //First skip the whitespace
        StringBuilder buf = new StringBuilder(60);
        int nextByte = input.read();
        while( isWhitespace( nextByte ) )
        {
            nextByte = input.read();
            //do nothing just skip the whitespace.
        }
        buf.append( (char)nextByte );

        //now read the data
        nextByte = input.read();
        while (nextByte != -1 && !isEOL(nextByte))
        {
            buf.append((char) nextByte);
            nextByte = input.read();
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
        StringBuilder buf = new StringBuilder(24);
        int nextByte = input.read();
        while( isWhitespace( nextByte ) )
        {
            nextByte = input.read();
            //do nothing just skip the whitespace.
        }
        buf.append( (char)nextByte );

        //now read the data
        nextByte = input.read();
        while (nextByte != -1 && !isWhitespace(nextByte))
        {
            buf.append((char) nextByte);
            nextByte = input.read();
        }
        return buf.toString();
    }

    /**
     * Read the next string. Throw an exception if it differs from the expected command.
     * 
     * @param expectedCommand the expected command
     * @throws IOException IF the read string differs from the expected command
     */
    private void readCommand(String expectedCommand) throws IOException
    {
        String command = readString();
        if (!expectedCommand.equals(command))
        {
            throw new IOException(
                    "Error: Expected '" + expectedCommand + "' actual '" + command + "'");
        }
    }

    /**
     * This will determine if the byte is a whitespace character or not.
     *
     * @param character The character to test for whitespace.
     *
     * @return true If the character is whitespace as defined by the AFM spec.
     */
    private static boolean isEOL( int character )
    {
        return character == 0x0D || character == 0x0A;
    }

    /**
     * This will determine if the byte is a whitespace character or not.
     *
     * @param character The character to test for whitespace.
     *
     * @return true If the character is whitespace as defined by the AFM spec.
     */
    private static boolean isWhitespace( int character )
    {
        switch (character)
        {
        case ' ':
        case '\t':
        case 0x0D:
        case 0x0A:
            return true;
        default:
            return false;
        }
    }
}
