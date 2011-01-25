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
package org.apache.fontbox.cmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.util.ResourceLoader;

/**
 * This will parser a CMap stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.9 $
 */
public class CMapParser
{
    private static final String BEGIN_CODESPACE_RANGE = "begincodespacerange";
    private static final String BEGIN_BASE_FONT_CHAR = "beginbfchar";
    private static final String BEGIN_BASE_FONT_RANGE = "beginbfrange";
    private static final String BEGIN_CID_CHAR = "begincidchar";
    private static final String BEGIN_CID_RANGE = "begincidrange";
    private static final String USECMAP = "usecmap";
    
    private static final String WMODE = "WMode";
    private static final String CMAP_NAME = "CMapName";
    private static final String CMAP_VERSION = "CMapVersion";
    private static final String CMAP_TYPE = "CMapType";
    private static final String REGISTRY = "Registry";
    private static final String ORDERING = "Ordering";
    private static final String SUPPLEMENT = "Supplement";
    
    private static final String MARK_END_OF_DICTIONARY = ">>";
    private static final String MARK_END_OF_ARRAY = "]";
    
    
    private byte[] tokenParserByteBuffer = new byte[512];

    /**
     * Creates a new instance of CMapParser.
     */
    public CMapParser()
    {
    }
    
    /**
     * Parse a CMAP file on the file system.
     * 
     * @param file The file to parse.
     * 
     * @return A parsed CMAP file.
     * 
     * @throws IOException If there is an issue while parsing the CMAP.
     */
    public CMap parse( File file ) throws IOException
    {
        String rootDir = file.getParent() + File.separator;
        FileInputStream input = null;
        try
        {
            input = new FileInputStream( file );
            return parse( rootDir, input );
        }
        finally
        {
            if( input != null )
            {
                input.close();
            }
        }
        
    }

    /**
     * This will parse the stream and create a cmap object.
     *
     * @param resourceRoot The root path to the cmap file.  This will be used
     *                     to find referenced cmap files.  It can be null.
     * @param input The CMAP stream to parse.
     * 
     * @return The parsed stream as a java object.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    public CMap parse( String resourceRoot, InputStream input ) throws IOException
    {
        PushbackInputStream cmapStream = new PushbackInputStream( input );
        CMap result = new CMap();
        Object previousToken = null;
        Object token = null;
        while( (token = parseNextToken( cmapStream )) != null )
        {
            if( token instanceof Operator )
            {
                Operator op = (Operator)token;
                if( op.op.equals( USECMAP ) )
                {
                    LiteralName useCmapName = (LiteralName)previousToken;
                    InputStream useStream = ResourceLoader.loadResource( resourceRoot + useCmapName.name );
                    if( useStream == null )
                    {
                        throw new IOException( "Error: Could not find referenced cmap stream " + useCmapName.name );
                    }
                    CMap useCMap = parse( resourceRoot, useStream );
                    result.useCmap( useCMap );
                }
                else if( op.op.equals( BEGIN_CODESPACE_RANGE ) )
                {
                    Number cosCount = (Number)previousToken;
                    for( int j=0; j<cosCount.intValue(); j++ )
                    {
                        byte[] startRange = (byte[])parseNextToken( cmapStream );
                        byte[] endRange = (byte[])parseNextToken( cmapStream );
                        CodespaceRange range = new CodespaceRange();
                        range.setStart( startRange );
                        range.setEnd( endRange );
                        result.addCodespaceRange( range );
                    }
                }
                else if( op.op.equals( BEGIN_BASE_FONT_CHAR ) )
                {
                    Number cosCount = (Number)previousToken;
                    for( int j=0; j<cosCount.intValue(); j++ )
                    {
                        byte[] inputCode = (byte[])parseNextToken( cmapStream );
                        Object nextToken = parseNextToken( cmapStream );
                        if( nextToken instanceof byte[] )
                        {
                            byte[] bytes = (byte[])nextToken;
                            String value = createStringFromBytes( bytes );
                            result.addMapping( inputCode, value );
                        }
                        else if( nextToken instanceof LiteralName )
                        {
                            result.addMapping( inputCode, ((LiteralName)nextToken).name );
                        }
                        else
                        {
                            throw new IOException( "Error parsing CMap beginbfchar, expected{COSString " +
                                                   "or COSName} and not " + nextToken );
                        }
                    }
                }
                else if( op.op.equals( BEGIN_BASE_FONT_RANGE ) )
                {
                    Number cosCount = (Number)previousToken;
                    
                    for( int j=0; j<cosCount.intValue(); j++ )
                    {
                        byte[] startCode = (byte[])parseNextToken( cmapStream );
                        byte[] endCode = (byte[])parseNextToken( cmapStream );
                        Object nextToken = parseNextToken( cmapStream );
                        List<byte[]> array = null;
                        byte[] tokenBytes = null;
                        if( nextToken instanceof List<?> )
                        {
                            array = (List<byte[]>)nextToken;
                            tokenBytes = array.get( 0 );
                        }
                        else
                        {
                            tokenBytes = (byte[])nextToken;
                        }
                        
                        String value = null;
                        
                        int arrayIndex = 0;
                        boolean done = false;
                        while( !done )
                        {
                            if( compare( startCode, endCode ) >= 0 )
                            {
                                done = true;
                            }
                            value = createStringFromBytes( tokenBytes );
                            result.addMapping( startCode, value );
                            increment( startCode );
                            
                            if( array == null )
                            {
                                increment( tokenBytes );
                            }
                            else
                            {
                                arrayIndex++;
                                if( arrayIndex < array.size() )
                                {
                                    tokenBytes = (byte[])array.get( arrayIndex );
                                }
                            }
                        }
                    }
                }
                else if( op.op.equals( BEGIN_CID_CHAR ) )
                {
                    Number cosCount = (Number)previousToken;
                    for( int j=0; j<cosCount.intValue(); j++ )
                    {
                        byte[] inputCode = (byte[])parseNextToken( cmapStream );
                        int mappedCode = (Integer)parseNextToken( cmapStream );
                        String mappedStr = createStringFromBytes(inputCode);
                        result.addCIDMapping(mappedCode, mappedStr);
                    }
                }
                else if( op.op.equals( BEGIN_CID_RANGE ) )
                {
                    int numberOfLines = (Integer)previousToken;
                    for (int n=0; n < numberOfLines;n++) {
                        byte[] startCode = (byte[])parseNextToken( cmapStream );
                        int start = createIntFromBytes(startCode);
                        byte[] endCode = (byte[])parseNextToken( cmapStream );
                        int end = createIntFromBytes(endCode);
                        int mappedCode = (Integer)parseNextToken( cmapStream );
                        if (startCode.length <= 2 && endCode.length <= 2) {
                            result.addCIDRange(
                                    (char) start, (char) end, mappedCode);
                        } else {
                            // TODO: Is this even possible?
                            int endOfMappings = mappedCode + end-start;
                            while (mappedCode<=endOfMappings) {
                                String mappedStr = createStringFromBytes(startCode);
                                result.addCIDMapping(mappedCode++, mappedStr);
                                increment(startCode);
                            }
                        }
                    }
                }
            }
            else if (token instanceof LiteralName){
                LiteralName literal = (LiteralName)token;
                if (WMODE.equals(literal.name)) 
                {
                    Object next = parseNextToken(cmapStream);
                    if (next instanceof Integer)
                    {
                        result.setWMode((Integer)next);
                    }
                }
                else if (CMAP_NAME.equals(literal.name)) 
                {
                    Object next = parseNextToken(cmapStream);
                    if (next instanceof LiteralName)
                    {
                        result.setName(((LiteralName)next).name);
                    }
                }
                else if (CMAP_VERSION.equals(literal.name)) 
                {
                    Object next = parseNextToken(cmapStream);
                    if (next instanceof String)
                    {
                        result.setVersion((String)next);
                    }
                }
                else if (CMAP_TYPE.equals(literal.name)) 
                {
                    Object next = parseNextToken(cmapStream);
                    if (next instanceof Integer)
                    {
                        result.setType((Integer)next);
                    }
                }
                else if (REGISTRY.equals(literal.name)) 
                {
                    Object next = parseNextToken(cmapStream);
                    if (next instanceof String)
                    {
                        result.setRegistry((String)next);
                    }
                }
                else if (ORDERING.equals(literal.name)) 
                {
                    Object next = parseNextToken(cmapStream);
                    if (next instanceof String)
                    {
                        result.setOrdering((String)next);
                    }
                }
                else if (SUPPLEMENT.equals(literal.name)) 
                {
                    Object next = parseNextToken(cmapStream);
                    if (next instanceof Integer)
                    {
                        result.setSupplement((Integer)next);
                    }
                }
            }
            previousToken = token;
        }
        return result;
    }
    
    private Object parseNextToken( PushbackInputStream is ) throws IOException
    {
        Object retval = null;
        int nextByte = is.read();
        //skip whitespace
        while( nextByte == 0x09 || nextByte == 0x20 || nextByte == 0x0D || nextByte == 0x0A )
        {
            nextByte = is.read();
        }
        switch( nextByte )
        {
            case '%':
            {
                //header operations, for now return the entire line 
                //may need to smarter in the future
                StringBuffer buffer = new StringBuffer();
                buffer.append( (char)nextByte );
                readUntilEndOfLine( is, buffer );
                retval = buffer.toString();
                break;
            }
            case '(':
            {
                StringBuffer buffer = new StringBuffer();
                int stringByte = is.read();
                
                while( stringByte != -1 && stringByte != ')' )
                {
                    buffer.append( (char)stringByte );
                    stringByte = is.read();
                }
                retval = buffer.toString();
                break;
            }
            case '>':
            {
                int secondCloseBrace = is.read();
                if( secondCloseBrace == '>' )
                {
                    retval = MARK_END_OF_DICTIONARY;
                }
                else
                {
                    throw new IOException( "Error: expected the end of a dictionary.");
                }
                break;
            }
            case ']':
            {
                retval = MARK_END_OF_ARRAY;
                break;
            }
            case '[':
            {
                List<Object> list = new ArrayList<Object>();
                
                Object nextToken = parseNextToken( is ); 
                while( nextToken != null && nextToken != MARK_END_OF_ARRAY )
                {
                    list.add( nextToken );
                    nextToken = parseNextToken( is );
                }
                retval = list;
                break;
            }
            case '<':
            {
                int theNextByte = is.read();
                if( theNextByte == '<' )
                {
                    Map<String,Object> result = new HashMap<String,Object>();
                    //we are reading a dictionary
                    Object key = parseNextToken( is ); 
                    while( key instanceof LiteralName && key != MARK_END_OF_DICTIONARY )
                    {
                        Object value = parseNextToken( is );
                        result.put( ((LiteralName)key).name, value );
                        key = parseNextToken( is );
                    }
                    retval = result;
                }
                else
                {
                    //won't read more than 512 bytes
                    
                    int multiplyer = 16;
                    int bufferIndex = -1;
                    while( theNextByte != -1 && theNextByte != '>' )
                    {
                        int intValue = 0;
                        if( theNextByte >= '0' && theNextByte <= '9' )
                        {
                            intValue = theNextByte - '0';
                        }
                        else if( theNextByte >= 'A' && theNextByte <= 'F' )
                        {
                            intValue = 10 + theNextByte - 'A';
                        }
                        else if( theNextByte >= 'a' && theNextByte <= 'f' )
                        {
                            intValue = 10 + theNextByte - 'a';
                        }
                        else if( theNextByte == 0x20 )
                        {
                            // skipping whitespaces
                            theNextByte = is.read();
                            continue;
                        }
                        else
                        {
                            throw new IOException( "Error: expected hex character and not " + 
                                (char)theNextByte + ":" + theNextByte );
                        }
                        intValue *= multiplyer;
                        if( multiplyer == 16 )
                        {
                            bufferIndex++;
                            tokenParserByteBuffer[bufferIndex] = 0;
                            multiplyer = 1;
                        }
                        else
                        {
                            multiplyer = 16;
                        }
                        tokenParserByteBuffer[bufferIndex]+= intValue;
                        theNextByte = is.read();
                    }
                    byte[] finalResult = new byte[bufferIndex+1];
                    System.arraycopy(tokenParserByteBuffer,0,finalResult, 0, bufferIndex+1);
                    retval = finalResult;
                }
                break;
            }
            case '/':
            {
                StringBuffer buffer = new StringBuffer();
                int stringByte = is.read();
                
                while( !isWhitespaceOrEOF( stringByte ) )
                {
                    buffer.append( (char)stringByte );
                    stringByte = is.read();
                }
                retval = new LiteralName( buffer.toString() );
                break;
            }
            case -1:
            {
                //EOF return null;
                break;
            }
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            {
                StringBuffer buffer = new StringBuffer();
                buffer.append( (char)nextByte );
                nextByte = is.read();
                
                while( !isWhitespaceOrEOF( nextByte ) &&
                        (Character.isDigit( (char)nextByte )||
                         nextByte == '.' ) )
                {
                    buffer.append( (char)nextByte );
                    nextByte = is.read();
                }
                is.unread( nextByte );
                String value = buffer.toString();
                if( value.indexOf( '.' ) >=0 )
                {
                    retval = new Double( value );
                }
                else
                {
                    retval = new Integer( buffer.toString() );
                }
                break;
            }
            default:
            {
                StringBuffer buffer = new StringBuffer();
                buffer.append( (char)nextByte );
                nextByte = is.read();
                
                while( !isWhitespaceOrEOF( nextByte ) )
                {
                    buffer.append( (char)nextByte );
                    nextByte = is.read();
                }
                retval = new Operator( buffer.toString() );                        
                
                break;
            }
        }
        return retval;
    }
    
    private void readUntilEndOfLine( InputStream is, StringBuffer buf ) throws IOException
    {
        int nextByte = is.read();
        while( nextByte != -1 && nextByte != 0x0D && nextByte != 0x0A )
        {
            buf.append( (char)nextByte );
            nextByte = is.read();
        }
    }
    
    private boolean isWhitespaceOrEOF( int aByte )
    {
        return aByte == -1 || aByte == 0x20 || aByte == 0x0D || aByte == 0x0A; 
    }
    

    private void increment( byte[] data )
    {
        increment( data, data.length-1 );
    }

    private void increment( byte[] data, int position )
    {
        if( position > 0 && (data[position]+256)%256 == 255 )
        {
            data[position]=0;
            increment( data, position-1);
        }
        else
        {
            data[position] = (byte)(data[position]+1);
        }
    }
    
    private int createIntFromBytes(byte[] bytes) 
    {
        int intValue = (bytes[0]+256)%256;
        if (bytes.length == 2) 
        {
            intValue <<= 8;
            intValue += (bytes[1]+256)%256;
        }
        return intValue;
    }
    
    private String createStringFromBytes( byte[] bytes ) throws IOException
    {
        String retval = null;
        if( bytes.length == 1 )
        {
            retval = new String( bytes, "ISO-8859-1" );
        }
        else
        {
            retval = new String( bytes, "UTF-16BE" );
        }
        return retval;
    }

    private int compare( byte[] first, byte[] second )
    {
        int retval = 1;
        int firstLength = first.length;
        for( int i=0; i<firstLength; i++ )
        {
            if( first[i] == second[i] )
            {
                continue;
            }
            else if( ((first[i]+256)%256) < ((second[i]+256)%256) )
            {
                retval = -1;
                break;
            }
            else
            {
                retval = 1;
                break;
            }
        }
        return retval;
    }
    
    /**
     * Internal class.
     */
    private class LiteralName
    {
        private String name;
        private LiteralName( String theName )
        {
            name = theName;
        }
    }
    
    /**
     * Internal class.
     */
    private class Operator
    {
        private String op;
        private Operator( String theOp )
        {
            op = theOp;
        }
    }
    
    /**
     * A simple class to test parsing of cmap files.
     * 
     * @param args Some command line arguments.
     * 
     * @throws Exception If there is an error parsing the file.
     */
    public static void main( String[] args ) throws Exception
    {
        if( args.length != 1 )
        {
            System.err.println( "usage: java org.pdfbox.cmapparser.CMapParser <CMAP File>" );
            System.exit( -1 );
        }
        CMapParser parser = new CMapParser(  );
        File cmapFile = new File( args[0] );
        CMap result = parser.parse( cmapFile );
        System.out.println( "Result:" + result );
    }
}