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
package org.apache.pdfbox.pdfparser;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.ByteArrayPushBackInputStream;
import org.apache.pdfbox.io.PushBackInputStream;
import org.apache.pdfbox.io.RandomAccess;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;

import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This class is used to contain parsing logic that will be used by both the
 * PDFParser and the COSStreamParser.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.61 $
 */
public abstract class BaseParser
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(BaseParser.class);

    /**
     * This is a byte array that will be used for comparisons.
     */
    public static final byte[] ENDSTREAM =
        new byte[] {101,110,100,115,116,114,101,97,109};//"endstream".getBytes( "ISO-8859-1" );

    /**
     * This is a byte array that will be used for comparisons.
     */
    public static final byte[] ENDOBJ =
        new byte[] {101,110,100,111,98,106};//"endobj".getBytes( "ISO-8859-1" );
    /**
     * This is a byte array that will be used for comparisons.
     */
    public static final String DEF = "def";

    /**
     * This is the stream that will be read from.
     */
    protected PushBackInputStream pdfSource;

    /**
     * This is the document that will be parsed.
     */
    protected COSDocument document;
    
    /**
     * Constructor.
     *
     * @param input The input stream to read the data from.
     *
     * @throws IOException If there is an error reading the input stream.
     */
    public BaseParser( InputStream input) throws IOException
    {
        //pdfSource = new PushBackByteArrayStream( input );
        pdfSource = new PushBackInputStream( new BufferedInputStream( input, 16384 ), 4096 );
    }

    /**
     * Constructor.
     *
     * @param input The array to read the data from.
     *
     * @throws IOException If there is an error reading the byte data.
     */
    protected BaseParser(byte[] input) throws IOException
    {
        pdfSource = new ByteArrayPushBackInputStream(input);
    }

    /**
     * Set the document for this stream.
     *
     * @param doc The current document.
     */
    public void setDocument( COSDocument doc )
    {
        document = doc;
    }

    private static boolean isHexDigit(char ch)
    {
        return (ch >= '0' && ch <= '9') ||
        (ch >= 'a' && ch <= 'f') ||
        (ch >= 'A' && ch <= 'F');
        // the line below can lead to problems with certain versions of the IBM JIT compiler
        // (and is slower anyway)
        //return (HEXDIGITS.indexOf(ch) != -1);
    }

    /**
     * This will parse a PDF dictionary value.
     *
     * @return The parsed Dictionary object.
     *
     * @throws IOException If there is an error parsing the dictionary object.
     */
    private COSBase parseCOSDictionaryValue() throws IOException
    {
        COSBase retval = null;
        COSBase number = parseDirObject();
        skipSpaces();
        char next = (char)pdfSource.peek();
        if( next >= '0' && next <= '9' )
        {
            COSBase generationNumber = parseDirObject();
            skipSpaces();
            char r = (char)pdfSource.read();
            if( r != 'R' )
            {
                throw new IOException( "expected='R' actual='" + r + "' " + pdfSource );
            }
            COSObjectKey key = new COSObjectKey(((COSInteger) number).intValue(),
                    ((COSInteger) generationNumber).intValue());
            retval = document.getObjectFromPool(key);
        }
        else
        {
            retval = number;
        }
        return retval;
    }

    /**
     * This will parse a PDF dictionary.
     *
     * @return The parsed dictionary.
     *
     * @throws IOException IF there is an error reading the stream.
     */
    protected COSDictionary parseCOSDictionary() throws IOException
    {
        char c = (char)pdfSource.read();
        if( c != '<')
        {
            throw new IOException( "expected='<' actual='" + c + "'" );
        }
        c = (char)pdfSource.read();
        if( c != '<')
        {
            throw new IOException( "expected='<' actual='" + c + "' " + pdfSource );
        }
        skipSpaces();
        COSDictionary obj = new COSDictionary();
        boolean done = false;
        while( !done )
        {
            skipSpaces();
            c = (char)pdfSource.peek();
            if( c == '>')
            {
                done = true;
            } 
            else 
                if(c != '/') 
                {
                    //an invalid dictionary, we are expecting
                    //the key, read until we can recover
                    log.warn("Invalid dictionary, found:" + (char)c + " but expected:\''");
                    int read = pdfSource.read();
                    while(read != -1 && read != '/' && read != '>')
                    {
                        read = pdfSource.read();
                    }
                    if(read != -1) 
                    {
                        pdfSource.unread(read);
                    }
                }
            else
            {
                COSName key = parseCOSName();
                COSBase value = parseCOSDictionaryValue();
                skipSpaces();
                if( ((char)pdfSource.peek()) == 'd' )
                {
                    //if the next string is 'def' then we are parsing a cmap stream
                    //and want to ignore it, otherwise throw an exception.
                    String potentialDEF = readString();
                    if( !potentialDEF.equals( DEF ) )
                    {
                        pdfSource.unread( potentialDEF.getBytes() );
                    }
                    else
                    {
                        skipSpaces();
                    }
                }

                if( value == null )
                {
                    log.warn("Bad Dictionary Declaration " + pdfSource );
                }
                else
                {
                    obj.setItem( key, value );
                }
            }
        }
        char ch = (char)pdfSource.read();
        if( ch != '>' )
        {
            throw new IOException( "expected='>' actual='" + ch + "'" );
        }
        ch = (char)pdfSource.read();
        if( ch != '>' )
        {
            throw new IOException( "expected='>' actual='" + ch + "'" );
        }
        return obj;
    }

    /**
     * This will read a COSStream from the input stream.
     *
     * @param file The file to write the stream to when reading.
     * @param dic The dictionary that goes with this stream.
     *
     * @return The parsed pdf stream.
     *
     * @throws IOException If there is an error reading the stream.
     */
    protected COSStream parseCOSStream( COSDictionary dic, RandomAccess file ) throws IOException
    {
        COSStream stream = new COSStream( dic, file );
        OutputStream out = null;
        try
        {
            String streamString = readString();
            //long streamLength;

            if (!streamString.equals("stream"))
            {
                throw new IOException("expected='stream' actual='" + streamString + "'");
            }

            //PDF Ref 3.2.7 A stream must be followed by either
            //a CRLF or LF but nothing else.

            int whitespace = pdfSource.read();

            //see brother_scan_cover.pdf, it adds whitespaces
            //after the stream but before the start of the
            //data, so just read those first
            while (whitespace == 0x20)
            {
                whitespace = pdfSource.read();
            }

            if( whitespace == 0x0D )
            {
                whitespace = pdfSource.read();
                if( whitespace != 0x0A )
                {
                    pdfSource.unread( whitespace );
                    //The spec says this is invalid but it happens in the real
                    //world so we must support it.
                }
            }
            else if (whitespace == 0x0A)
            {
                //that is fine
            }
            else
            {
                //we are in an error.
                //but again we will do a lenient parsing and just assume that everything
                //is fine
                pdfSource.unread( whitespace );
            }

            /*This needs to be dic.getItem because when we are parsing, the underlying object
             * might still be null.
             */
            COSBase streamLength = dic.getItem(COSName.LENGTH);

            //Need to keep track of the
            out = stream.createFilteredStream( streamLength );

            String endStream = null;
            readUntilEndStream(out);
            skipSpaces();
            endStream = readString(); 

            if (!endStream.equals("endstream"))
            {
                /*
                 * Sometimes stream objects don't have an endstream tag so readUntilEndStream(out)
                 * also can stop on endobj tags. If that's the case we need to make sure to unread
                 * the endobj so parseObject() can handle that case normally. 
                 */
                if (endStream.startsWith("endobj"))
                {
                    byte[] endobjarray = endStream.getBytes();
                    pdfSource.unread(endobjarray);
                }
                /*
                 * Some PDF files don't contain a new line after endstream so we 
                 * need to make sure that the next object number is getting read separately
                 * and not part of the endstream keyword. Ex. Some files would have "endstream8"
                 * instead of "endstream"
                 */
                else if(endStream.startsWith("endstream"))
                {
                    String extra = endStream.substring(9, endStream.length());
                    endStream = endStream.substring(0, 9);
                    byte[] array = extra.getBytes();
                    pdfSource.unread(array);
                }
                else
                {
                    /*
                     * If for some reason we get something else here, Read until we find the next
                     * "endstream"
                     */
                    readUntilEndStream( out );
                    endStream = readString();
                    if( !endStream.equals( "endstream" ) )
                    {
                        throw new IOException("expected='endstream' actual='" + endStream + "' " + pdfSource);
                    }
                }
            }
        }
        finally
        {
            if( out != null )
            {
                out.close();
            }
        }
        return stream;
    }
    /**
     * This method will read through the current stream object until
     * we find the keyword "endstream" meaning we're at the end of this
     * object. Some pdf files, however, forget to write some endstream tags
     * and just close off objects with an "endobj" tag so we have to handle
     * this case as well.
     * @param out The stream we write out to. 
     * @throws IOException
     */
    private void readUntilEndStream( OutputStream out ) throws IOException
    {
        int byteRead = 0;
        byte[] buffer = new byte[ENDSTREAM.length];
        int nextIdx = pdfSource.read(buffer) % buffer.length; 
        if (nextIdx == -1)
        { 
            return;
        }
        while(byteRead != -1 ) 
        {
            if (cmpCircularBuffer( buffer, (nextIdx-ENDSTREAM.length + buffer.length)%buffer.length, ENDSTREAM )) 
            {
                pdfSource.unread( ENDSTREAM );
                return;
            }
            /*
             * occasionally steam objects do not write the endstream tag and just terminate
             * the object with an endobj tag so we want to stop there as well. 
             */
            int endObjStart = (nextIdx-ENDOBJ.length+ buffer.length)%buffer.length;
            if (cmpCircularBuffer( buffer, endObjStart, ENDOBJ )) 
            {
                // data is written to out only when it is going to be overwritten.
                // write out the rest of the data in the buffer since ENDOBJ is smaller then the buffer
                for (int i = nextIdx; i < buffer.length && i < endObjStart; i++ ) 
                {
                    out.write(buffer[i]);
                }
                pdfSource.unread( ENDOBJ );
                return;
            }

            out.write( buffer[nextIdx] );

            byteRead = pdfSource.read();
            buffer[nextIdx] = (byte)byteRead;

            if (++nextIdx == buffer.length) 
            {
                nextIdx = 0;
            }
        }   
    }

    /**
     * This basically checks to see if the next compareTo.length bytes of the
     * buffer match the compareTo byte array.
     * @param buffer Circular buffer to look for string in
     * @param currentIndex Index in buffer to start comparison from
     * @param compareTo String to find in circular buffer at index
     */
    private boolean cmpCircularBuffer( byte[] buffer, int currentIndex, byte[] compareTo )
    {
        int cmpLen = compareTo.length;
        int buflen = buffer.length;
        boolean match = true;
        for( int i=0; match && i<cmpLen; ++i ) 
        {
            match = buffer[(currentIndex+i)%buflen] == compareTo[i];
        }
        return match;
    }

    /**
     * This will parse a PDF string.
     *
     * @return The parsed PDF string.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected COSString parseCOSString() throws IOException
    {
        char nextChar = (char)pdfSource.read();
        COSString retval = new COSString();
        char openBrace;
        char closeBrace;
        if( nextChar == '(' )
        {
            openBrace = '(';
            closeBrace = ')';
        }
        else if( nextChar == '<' )
        {
            openBrace = '<';
            closeBrace = '>';
        }
        else
        {
            throw new IOException( "parseCOSString string should start with '(' or '<' and not '" +
                    nextChar + "' " + pdfSource );
        }

        //This is the number of braces read
        //
        int braces = 1;
        int c = pdfSource.read();
        while( braces > 0 && c != -1)
        {
            char ch = (char)c;
            int nextc = -2; // not yet read
            //if( log.isDebugEnabled() )
            //{
            //    log.debug( "Parsing COSString character '" + c + "' code=" + (int)c );
            //}

            if(ch == closeBrace)
            {
                braces--;
                byte[] nextThreeBytes = new byte[3];
                int amountRead = pdfSource.read(nextThreeBytes);

                //lets handle the special case seen in Bull  River Rules and Regulations.pdf
                //The dictionary looks like this
                //    2 0 obj
                //    <<
                //        /Type /Info
                //        /Creator (PaperPort http://www.scansoft.com)
                //        /Producer (sspdflib 1.0 http://www.scansoft.com)
                //        /Title ( (5)
                //        /Author ()
                //        /Subject ()
                //
                // Notice the /Title, the braces are not even but they should
                // be.  So lets assume that if we encounter an this scenario
                //   <end_brace><new_line><opening_slash> then that
                // means that there is an error in the pdf and assume that
                // was the end of the document.
                if( amountRead == 3 )
                {
                    if( nextThreeBytes[0] == 0x0d &&
                            nextThreeBytes[1] == 0x0a &&
                            nextThreeBytes[2] == 0x2f )
                    {
                        braces = 0;
                    }
                }
                if (amountRead > 0) 
                {
                    pdfSource.unread( nextThreeBytes, 0, amountRead );
                }
                if( braces != 0 )
                {
                    retval.append( ch );
                }
            }
            else if( ch == openBrace )
            {
                braces++;
                retval.append( ch );
            }
            else if( ch == '\\' )
            {
                //patched by ram
                char next = (char)pdfSource.read();
                switch(next)
                {
                case 'n':
                    retval.append( '\n' );
                    break;
                case 'r':
                    retval.append( '\r' );
                    break;
                case 't':
                    retval.append( '\t' );
                    break;
                case 'b':
                    retval.append( '\b' );
                    break;
                case 'f':
                    retval.append( '\f' );
                    break;
                case '(':
                case ')':
                case '\\':
                    retval.append( next );
                    break;
                case 10:
                case 13:
                    //this is a break in the line so ignore it and the newline and continue
                    c = pdfSource.read();
                    while( isEOL(c) && c != -1)
                    {
                        c = pdfSource.read();
                    }
                    nextc = c;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                {
                    StringBuffer octal = new StringBuffer();
                    octal.append( next );
                    c = pdfSource.read();
                    char digit = (char)c;
                    if( digit >= '0' && digit <= '7' )
                    {
                        octal.append( digit );
                        c = pdfSource.read();
                        digit = (char)c;
                        if( digit >= '0' && digit <= '7' )
                        {
                            octal.append( digit );
                        }
                        else
                        {
                            nextc = c;
                        }
                    }
                    else
                    {
                        nextc = c;
                    }

                    int character = 0;
                    try
                    {
                        character = Integer.parseInt( octal.toString(), 8 );
                    }
                    catch( NumberFormatException e )
                    {
                        throw new IOException( "Error: Expected octal character, actual='" + octal + "'" );
                    }
                    retval.append( character );
                    break;
                }
                default:
                {
                    retval.append( '\\' );
                    retval.append( next );
                    //another ficken problem with PDF's, sometimes the \ doesn't really
                    //mean escape like the PDF spec says it does, sometimes is should be literal
                    //which is what we will assume here.
                    //throw new IOException( "Unexpected break sequence '" + next + "' " + pdfSource );
                }
                }
            }
            else
            {
                if( openBrace == '<' )
                {
                    if( isHexDigit(ch) )
                    {
                        retval.append( ch );
                    }
                }
                else
                {
                    retval.append( ch );
                }
            }
            if (nextc != -2)
            {
                c = nextc;
            }
            else
            {
                c = pdfSource.read();
            }
        }
        if (c != -1)
        {
            pdfSource.unread(c);
        }
        if( openBrace == '<' )
        {
            retval = COSString.createFromHexString( retval.getString() );
        }
        return retval;
    }

    /**
     * This will parse a PDF array object.
     *
     * @return The parsed PDF array.
     *
     * @throws IOException If there is an error parsing the stream.
     */
    protected COSArray parseCOSArray() throws IOException
    {
        char ch = (char)pdfSource.read();
        if( ch != '[')
        {
            throw new IOException( "expected='[' actual='" + ch + "'" );
        }
        COSArray po = new COSArray();
        COSBase pbo = null;
        skipSpaces();
        int i = 0;
        while( ((i = pdfSource.peek()) > 0) && ((char)i != ']') )
        {
            pbo = parseDirObject();
            if( pbo instanceof COSObject )
            {
                // We have to check if the expected values are there or not PDFBOX-385 
                if (po.get(po.size()-1) instanceof COSInteger) 
                {
                    COSInteger genNumber = (COSInteger)po.remove( po.size() -1 );
                    if (po.get(po.size()-1) instanceof COSInteger) 
                    {
                        COSInteger number = (COSInteger)po.remove( po.size() -1 );
                        COSObjectKey key = new COSObjectKey(number.intValue(), genNumber.intValue());
                        pbo = document.getObjectFromPool(key);
                    }
                    else 
                    {
                        // the object reference is somehow wrong
                        pbo = null;
                    }
                }
                else 
                {
                    pbo = null;
                }
            }
            if( pbo != null )
            {
                po.add( pbo );
            }
            else
            {
                log.warn("Corrupt object reference" );
                //it could be a bad object in the array which is just skipped
            }
            skipSpaces();
        }
        pdfSource.read(); //read ']'
        skipSpaces();
        return po;
    }

    /**
     * Determine if a character terminates a PDF name.
     *
     * @param ch The character
     * @return <code>true</code> if the character terminates a PDF name, otherwise <code>false</code>.
     */
    protected boolean isEndOfName(char ch)
    {
        return (ch == ' ' || ch == 13 || ch == 10 || ch == 9 || ch == '>' || ch == '<'
            || ch == '[' || ch =='/' || ch ==']' || ch ==')' || ch =='(' ||
            ch == -1 //EOF
        );
    }

    /**
     * This will parse a PDF name from the stream.
     *
     * @return The parsed PDF name.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected COSName parseCOSName() throws IOException
    {
        COSName retval = null;
        int c = pdfSource.read();
        if( (char)c != '/')
        {
            throw new IOException("expected='/' actual='" + (char)c + "'-" + c + " " + pdfSource );
        }
        // costruisce il nome
        StringBuffer buffer = new StringBuffer();
        c = pdfSource.read();
        while( c != -1 )
        {
            char ch = (char)c;
            if(ch == '#')
            {
                char ch1 = (char)pdfSource.read();
                char ch2 = (char)pdfSource.read();

                // Prior to PDF v1.2, the # was not a special character.  Also,
                // it has been observed that various PDF tools do not follow the
                // spec with respect to the # escape, even though they report
                // PDF versions of 1.2 or later.  The solution here is that we
                // interpret the # as an escape only when it is followed by two
                // valid hex digits.
                //
                if (isHexDigit(ch1) && isHexDigit(ch2))
                {
                    String hex = "" + ch1 + ch2;
                    try
                    {
                        buffer.append( (char) Integer.parseInt(hex, 16));
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IOException("Error: expected hex number, actual='" + hex + "'");
                    }
                    c = pdfSource.read();
                }
                else
                {
                    pdfSource.unread(ch2);
                    c = ch1;
                    buffer.append( ch );
                }
            }
            else if (isEndOfName(ch))
            {
                break;
            }
            else
            {
                buffer.append( ch );
                c = pdfSource.read();
            }
        }
        if (c != -1)
        {
            pdfSource.unread(c);
        }
        retval = COSName.getPDFName( buffer.toString() );
        return retval;
    }

    /**
     * This will parse a boolean object from the stream.
     *
     * @return The parsed boolean object.
     *
     * @throws IOException If an IO error occurs during parsing.
     */
    protected COSBoolean parseBoolean() throws IOException
    {
        COSBoolean retval = null;
        char c = (char)pdfSource.peek();
        if( c == 't' )
        {
            String trueString = new String( pdfSource.readFully( 4 ) );
            if( !trueString.equals( "true" ) )
            {
                throw new IOException( "Error parsing boolean: expected='true' actual='" + trueString + "'" );
            }
            else
            {
                retval = COSBoolean.TRUE;
            }
        }
        else if( c == 'f' )
        {
            String falseString = new String( pdfSource.readFully( 5 ) );
            if( !falseString.equals( "false" ) )
            {
                throw new IOException( "Error parsing boolean: expected='true' actual='" + falseString + "'" );
            }
            else
            {
                retval = COSBoolean.FALSE;
            }
        }
        else
        {
            throw new IOException( "Error parsing boolean expected='t or f' actual='" + c + "'" );
        }
        return retval;
    }

    /**
     * This will parse a directory object from the stream.
     *
     * @return The parsed object.
     *
     * @throws IOException If there is an error during parsing.
     */
    protected COSBase parseDirObject() throws IOException
    {
        COSBase retval = null;

        skipSpaces();
        int nextByte = pdfSource.peek();
        char c = (char)nextByte;
        switch(c)
        {
        case '<':
        {
            int leftBracket = pdfSource.read();//pull off first left bracket
            c = (char)pdfSource.peek(); //check for second left bracket
            pdfSource.unread( leftBracket );
            if(c == '<')
            {

                retval = parseCOSDictionary();
                skipSpaces();
            }
            else
            {
                retval = parseCOSString();
            }
            break;
        }
        case '[': // array
        {
            retval = parseCOSArray();
            break;
        }
        case '(':
            retval = parseCOSString();
            break;
        case '/':   // name
            retval = parseCOSName();
            break;
        case 'n':   // null
        {
            String nullString = readString();
            if( !nullString.equals( "null") )
            {
                throw new IOException("Expected='null' actual='" + nullString + "'");
            }
            retval = COSNull.NULL;
            break;
        }
        case 't':
        {
            String trueString = new String( pdfSource.readFully(4) );
            if( trueString.equals( "true" ) )
            {
                retval = COSBoolean.TRUE;
            }
            else
            {
                throw new IOException( "expected true actual='" + trueString + "' " + pdfSource );
            }
            break;
        }
        case 'f':
        {
            String falseString = new String( pdfSource.readFully(5) );
            if( falseString.equals( "false" ) )
            {
                retval = COSBoolean.FALSE;
            }
            else
            {
                throw new IOException( "expected false actual='" + falseString + "' " + pdfSource );
            }
            break;
        }
        case 'R':
            pdfSource.read();
            retval = new COSObject(null);
            break;
        case (char)-1:
            return null;
        default:
        {
            if( Character.isDigit(c) || c == '-' || c == '+' || c == '.')
            {
                StringBuffer buf = new StringBuffer();
                int ic = pdfSource.read();
                c = (char)ic;
                while( Character.isDigit( c )||
                        c == '-' ||
                        c == '+' ||
                        c == '.' ||
                        c == 'E' ||
                        c == 'e' )
                {
                    buf.append( c );
                    ic = pdfSource.read();
                    c = (char)ic;
                }
                if( ic != -1 )
                {
                    pdfSource.unread( ic );
                }
                retval = COSNumber.get( buf.toString() );
            }
            else
            {
                //This is not suppose to happen, but we will allow for it
                //so we are more compatible with POS writers that don't
                //follow the spec
                String badString = readString();
                //throw new IOException( "Unknown dir object c='" + c +
                //"' peek='" + (char)pdfSource.peek() + "' " + pdfSource );
                if( badString == null || badString.length() == 0 )
                {
                    int peek = pdfSource.peek();
                    // we can end up in an infinite loop otherwise
                    throw new IOException( "Unknown dir object c='" + c +
                            "' cInt=" + (int)c + " peek='" + (char)peek + "' peekInt=" + peek + " " + pdfSource );
                }

            }
        }
        }
        return retval;
    }

    /**
     * This will read the next string from the stream.
     *
     * @return The string that was read from the stream.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected String readString() throws IOException
    {
        skipSpaces();
        StringBuffer buffer = new StringBuffer();
        int c = pdfSource.read();
        while( !isEndOfName((char)c) && !isClosing(c) && c != -1 )
        {
            buffer.append( (char)c );
            c = pdfSource.read();
        }
        if (c != -1)
        {
            pdfSource.unread(c);
        }
        return buffer.toString();
    }

    /**
     * This will read bytes until the end of line marker occurs.
     *
     * @param theString The next expected string in the stream.
     *
     * @return The characters between the current position and the end of the line.
     *
     * @throws IOException If there is an error reading from the stream or theString does not match what was read.
     */
    protected String readExpectedString( String theString ) throws IOException
    {
        int c = pdfSource.read();
        while( isWhitespace(c) && c != -1)
        {
            c = pdfSource.read();
        }
        StringBuffer buffer = new StringBuffer( theString.length() );
        int charsRead = 0;
        while( !isEOL(c) && c != -1 && charsRead < theString.length() )
        {
            char next = (char)c;
            buffer.append( next );
            if( theString.charAt( charsRead ) == next )
            {
                charsRead++;
            }
            else
            {
                pdfSource.unread(buffer.toString().getBytes());
                throw new IOException( "Error: Expected to read '" + theString +
                        "' instead started reading '" +buffer.toString() + "'" );
            }
            c = pdfSource.read();
        }
        while( isEOL(c) && c != -1 )
        {
            c = pdfSource.read();
        }
        if (c != -1)
        {
            pdfSource.unread(c);
        }
        return buffer.toString();
    }

    /**
     * This will read the next string from the stream up to a certain length.
     *
     * @param length The length to stop reading at.
     *
     * @return The string that was read from the stream of length 0 to length.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected String readString( int length ) throws IOException
    {
        skipSpaces();

        int c = pdfSource.read();

        //average string size is around 2 and the normal string buffer size is
        //about 16 so lets save some space.
        StringBuffer buffer = new StringBuffer(length);
        while( !isWhitespace(c) && !isClosing(c) && c != -1 && buffer.length() < length &&
                c != '[' &&
                c != '<' &&
                c != '(' &&
                c != '/' )
        {
            buffer.append( (char)c );
            c = pdfSource.read();
        }
        if (c != -1)
        {
            pdfSource.unread(c);
        }
        return buffer.toString();
    }

    /**
     * This will tell if the next character is a closing brace( close of PDF array ).
     *
     * @return true if the next byte is ']', false otherwise.
     *
     * @throws IOException If an IO error occurs.
     */
    protected boolean isClosing() throws IOException
    {
        return isClosing(pdfSource.peek());
    }

    /**
     * This will tell if the next character is a closing brace( close of PDF array ).
     *
     * @param c The character to check against end of line
     * @return true if the next byte is ']', false otherwise.
     */
    protected boolean isClosing(int c)
    {
        return c == ']';
    }

    /**
     * This will read bytes until the first end of line marker occurs.
     * Note: if you later unread the results of this function, you'll
     * need to add a newline character to the end of the string.
     *
     * @return The characters between the current position and the end of the line.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected String readLine() throws IOException 
    {
        if (pdfSource.isEOF())
        {
            throw new IOException( "Error: End-of-File, expected line");
        }

        StringBuffer buffer = new StringBuffer( 11 );
       
        int c;
        while ((c = pdfSource.read()) != -1) 
        {
            if (isEOL(c)) 
            {
                break;
            }
            buffer.append( (char)c );
        }
        return buffer.toString();
    }

    /**
     * This will tell if the next byte to be read is an end of line byte.
     *
     * @return true if the next byte is 0x0A or 0x0D.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected boolean isEOL() throws IOException
    {
        return isEOL(pdfSource.peek());
    }

    /**
     * This will tell if the next byte to be read is an end of line byte.
     *
     * @param c The character to check against end of line
     * @return true if the next byte is 0x0A or 0x0D.
     */
    protected boolean isEOL(int c)
    {
        return c == 10 || c == 13;
    }

    /**
     * This will tell if the next byte is whitespace or not.
     *
     * @return true if the next byte in the stream is a whitespace character.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected boolean isWhitespace() throws IOException
    {
        return isWhitespace( pdfSource.peek() );
    }

    /**
     * This will tell if the next byte is whitespace or not.
     *
     * @param c The character to check against whitespace
     *
     * @return true if the next byte in the stream is a whitespace character.
     */
    protected boolean isWhitespace( int c )
    {
        return c == 0 || c == 9 || c == 12  || c == 10
        || c == 13 || c == 32;
    }

    /**
     * This will skip all spaces and comments that are present.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected void skipSpaces() throws IOException
    {
        //log( "skipSpaces() " + pdfSource );
        int c = pdfSource.read();
        // identical to, but faster as: isWhiteSpace(c) || c == 37
        while(c == 0 || c == 9 || c == 12  || c == 10
                || c == 13 || c == 32 || c == 37)//37 is the % character, a comment
        {
            if ( c == 37 )
            {
                // skip past the comment section
                c = pdfSource.read();
                while(!isEOL(c) && c != -1)
                {
                    c = pdfSource.read();
                }
            }
            else
            {
                c = pdfSource.read();
            }
        }
        if (c != -1)
        {
            pdfSource.unread(c);
        }
        //log( "skipSpaces() done peek='" + (char)pdfSource.peek() + "'" );
    }

    /**
     * This will read an integer from the stream.
     *
     * @return The integer that was read from the stream.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected int readInt() throws IOException
    {
        skipSpaces();
        int retval = 0;

        int lastByte = 0;
        StringBuffer intBuffer = new StringBuffer();
        while( (lastByte = pdfSource.read() ) != 32 &&
                lastByte != 10 &&
                lastByte != 13 &&
                lastByte != 60 && //see sourceforge bug 1714707
                lastByte != 0 && //See sourceforge bug 853328
                lastByte != -1 )
        {
            intBuffer.append( (char)lastByte );
        }
        if( lastByte != -1 )
        {
            pdfSource.unread( lastByte );
        }

        try
        {
            retval = Integer.parseInt( intBuffer.toString() );
        }
        catch( NumberFormatException e )
        {
            pdfSource.unread(intBuffer.toString().getBytes());
            throw new IOException( "Error: Expected an integer type, actual='" + intBuffer + "'" );
        }
        return retval;
    }
}
