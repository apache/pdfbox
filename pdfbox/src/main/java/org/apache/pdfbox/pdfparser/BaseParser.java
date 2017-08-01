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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.util.Charsets;


import static org.apache.pdfbox.util.Charsets.ISO_8859_1;

/**
 * This class is used to contain parsing logic that will be used by both the
 * PDFParser and the COSStreamParser.
 *
 * @author Ben Litchfield
 */
public abstract class BaseParser
{
    private static final long OBJECT_NUMBER_THRESHOLD = 10000000000L;

    private static final long GENERATION_NUMBER_THRESHOLD = 65535;

    static final int MAX_LENGTH_LONG = Long.toString(Long.MAX_VALUE).length();

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(BaseParser.class);

    protected static final int E = 'e';
    protected static final int N = 'n';
    protected static final int D = 'd';

    protected static final int S = 's';
    protected static final int T = 't';
    protected static final int R = 'r';
    protected static final int A = 'a';
    protected static final int M = 'm';

    protected static final int O = 'o';
    protected static final int B = 'b';
    protected static final int J = 'j';

    /**
     * This is a string constant that will be used for comparisons.
     */
    public static final String DEF = "def";
    /**
     * This is a string constant that will be used for comparisons.
     */
    protected static final String ENDOBJ_STRING = "endobj";
    /**
     * This is a string constant that will be used for comparisons.
     */
    protected static final String ENDSTREAM_STRING = "endstream";
    /**
     * This is a string constant that will be used for comparisons.
     */
    protected static final String STREAM_STRING = "stream";
    /**
     * This is a string constant that will be used for comparisons.
     */
    private static final String TRUE = "true";
    /**
     * This is a string constant that will be used for comparisons.
     */
    private static final String FALSE = "false";
    /**
     * This is a string constant that will be used for comparisons.
     */
    private static final String NULL = "null";

    /**
     * ASCII code for line feed.
     */
    protected static final byte ASCII_LF = 10;
    /**
     * ASCII code for carriage return.
     */
    protected static final byte ASCII_CR = 13;
    private static final byte ASCII_ZERO = 48;
    private static final byte ASCII_NINE = 57;
    private static final byte ASCII_SPACE = 32;
    
    /**
     * This is the stream that will be read from.
     */
    protected final SequentialSource seqSource;

    /**
     * This is the document that will be parsed.
     */
    protected COSDocument document;

    /**
     * Default constructor.
     */
    public BaseParser(SequentialSource pdfSource)
    {
        this.seqSource = pdfSource;
    }

    private static boolean isHexDigit(char ch)
    {
        return isDigit(ch) ||
        (ch >= 'a' && ch <= 'f') ||
        (ch >= 'A' && ch <= 'F');
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
        long numOffset = seqSource.getPosition();
        COSBase value = parseDirObject();
        skipSpaces();
        // proceed if the given object is a number and the following is a number as well
        if (!(value instanceof COSNumber) || !isDigit())
        {
            return value;
        }
        // read the remaining information of the object number
        long genOffset = seqSource.getPosition();
        COSBase generationNumber = parseDirObject();
        skipSpaces();
        readExpectedChar('R');
        if (!(value instanceof COSInteger))
        {
            throw new IOException("expected number, actual=" + value + " at offset " + numOffset);
        }
        if (!(generationNumber instanceof COSInteger))
        {
            throw new IOException("expected number, actual=" + value + " at offset " + genOffset);
        }
        COSObjectKey key = new COSObjectKey(((COSInteger) value).longValue(),
                ((COSInteger) generationNumber).intValue());
        // dereference the object
        return getObjectFromPool(key);
    }

    private COSBase getObjectFromPool(COSObjectKey key) throws IOException
    {
        if (document == null)
        {
            throw new IOException("object reference " + key + " at offset " + seqSource.getPosition()
                    + " in content stream");
        }
        return document.getObjectFromPool(key);
    }

    /**
     * This will parse a PDF dictionary.
     *
     * @return The parsed dictionary, never null.
     *
     * @throws IOException If there is an error reading the stream.
     */
    protected COSDictionary parseCOSDictionary() throws IOException
    {
        readExpectedChar('<');
        readExpectedChar('<');
        skipSpaces();
        COSDictionary obj = new COSDictionary();
        boolean done = false;
        while (!done)
        {
            skipSpaces();
            char c = (char) seqSource.peek();
            if (c == '>')
            {
                done = true;
            }
            else if (c == '/')
            {
                parseCOSDictionaryNameValuePair(obj);
            }
            else
            {
                // invalid dictionary, we were expecting a /Name, read until the end or until we can recover
                LOG.warn("Invalid dictionary, found: '" + c + "' but expected: '/' at offset " + seqSource.getPosition());
                if (readUntilEndOfCOSDictionary())
                {
                    // we couldn't recover
                    return obj;
                }
            }
        }
        readExpectedChar('>');
        readExpectedChar('>');
        return obj;
    }

    /**
     * Keep reading until the end of the dictionary object or the file has been hit, or until a '/'
     * has been found.
     *
     * @return true if the end of the object or the file has been found, false if not, i.e. that the
     * caller can continue to parse the dictionary at the current position.
     *
     * @throws IOException if there is a reading error.
     */
    private boolean readUntilEndOfCOSDictionary() throws IOException
    {
        int c = seqSource.read();
        while (c != -1 && c != '/' && c != '>')
        {
            // in addition to stopping when we find / or >, we also want
            // to stop when we find endstream or endobj.
            if (c == E)
            {
                c = seqSource.read();
                if (c == N)
                {
                    c = seqSource.read();
                    if (c == D)
                    {
                        c = seqSource.read();
                        boolean isStream = c == S && seqSource.read() == T && seqSource.read() == R
                                && seqSource.read() == E && seqSource.read() == A && seqSource.read() == M;
                        boolean isObj = !isStream && c == O && seqSource.read() == B && seqSource.read() == J;
                        if (isStream || isObj)
                        {
                            // we're done reading this object!
                            return true;
                        }
                    }
                }
            }
            c = seqSource.read();
        }
        if (c == -1)
        {
            return true;
        }
        seqSource.unread(c);
        return false;
    }

    private void parseCOSDictionaryNameValuePair(COSDictionary obj) throws IOException
    {
        COSName key = parseCOSName();
        COSBase value = parseCOSDictionaryValue();
        skipSpaces();
        if (((char) seqSource.peek()) == 'd')
        {
            // if the next string is 'def' then we are parsing a cmap stream
            // and want to ignore it, otherwise throw an exception.
            String potentialDEF = readString();
            if (!potentialDEF.equals(DEF))
            {
                seqSource.unread(potentialDEF.getBytes(ISO_8859_1));
            }
            else
            {
                skipSpaces();
            }
        }

        if (value == null)
        {
            LOG.warn("Bad Dictionary Declaration " + seqSource);
        }
        else
        {
            // label this item as direct, to avoid signature problems.
            value.setDirect(true);
            obj.setItem(key, value);
        }
    }

    protected void skipWhiteSpaces() throws IOException
    {
        //PDF Ref 3.2.7 A stream must be followed by either
        //a CRLF or LF but nothing else.

        int whitespace = seqSource.read();

        //see brother_scan_cover.pdf, it adds whitespaces
        //after the stream but before the start of the
        //data, so just read those first
        while (ASCII_SPACE == whitespace)
        {
            whitespace = seqSource.read();
        }

        if (ASCII_CR == whitespace)
        {
            whitespace = seqSource.read();
            if (ASCII_LF != whitespace)
            {
                seqSource.unread(whitespace);
                //The spec says this is invalid but it happens in the real
                //world so we must support it.
            }
        }
        else if (ASCII_LF != whitespace)
        {
            //we are in an error.
            //but again we will do a lenient parsing and just assume that everything
            //is fine
            seqSource.unread(whitespace);
        }
    }

    /**
     * This is really a bug in the Document creators code, but it caused a crash in PDFBox, the first bug was in this
     * format: /Title ( (5) /Creator which was patched in 1 place.
     *
     * However it missed the case where the number of opening and closing parenthesis isn't balanced
     *
     * The second bug was in this format /Title (c:\) /Producer
     *
     * This patch moves this code out of the parseCOSString method, so it can be used twice.
     *
     * @param bracesParameter the number of braces currently open.
     *
     * @return the corrected value of the brace counter
     * @throws IOException
     */
    private int checkForEndOfString(final int bracesParameter) throws IOException
    {
        int braces = bracesParameter;
        byte[] nextThreeBytes = new byte[3];
        int amountRead = seqSource.read(nextThreeBytes);

        // Check the next 3 bytes if available
        // The following cases are valid indicators for the end of the string
        // 1. Next line contains another COSObject: CR + LF + '/'
        // 2. COSDictionary ends in the next line: CR + LF + '>'
        // 3. Next line contains another COSObject: CR + '/'
        // 4. COSDictionary ends in the next line: CR + '>'
        if (amountRead == 3 && nextThreeBytes[0] == ASCII_CR)
        {
            if ( (nextThreeBytes[1] == ASCII_LF && (nextThreeBytes[2] == '/') || nextThreeBytes[2] == '>')
                    || nextThreeBytes[1] == '/' || nextThreeBytes[1] == '>')
            {
                braces = 0;
            }
        }
        if (amountRead > 0)
        {
            seqSource.unread(nextThreeBytes, 0, amountRead);
        }
        return braces;
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
        char nextChar = (char) seqSource.read();
        if (nextChar == '<')
        {
            return parseCOSHexString();
        }
        else if (nextChar != '(')
        {
            throw new IOException( "parseCOSString string should start with '(' or '<' and not '" +
                    nextChar + "' " + seqSource);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // This is the number of braces read
        int braces = 1;
        int c = seqSource.read();
        while( braces > 0 && c != -1)
        {
            char ch = (char)c;
            int nextc = -2; // not yet read

            if (ch == ')')
            {

                braces--;
                braces = checkForEndOfString(braces);
                if( braces != 0 )
                {
                    out.write(ch);
                }
            }
            else if (ch == '(')
            {
                braces++;
                out.write(ch);
            }
            else if( ch == '\\' )
            {
                //patched by ram
                char next = (char) seqSource.read();
                switch(next)
                {
                    case 'n':
                        out.write('\n');
                        break;
                    case 'r':
                        out.write('\r');
                        break;
                    case 't':
                        out.write('\t');
                        break;
                    case 'b':
                        out.write('\b');
                        break;
                    case 'f':
                        out.write('\f');
                        break;
                    case ')':
                        // PDFBox 276 /Title (c:\)
                    braces = checkForEndOfString(braces);
                        if( braces != 0 )
                        {
                            out.write(next);
                        }
                        else
                        {
                            out.write('\\');
                        }
                        break;
                    case '(':
                    case '\\':
                        out.write(next);
                        break;
                    case ASCII_LF:
                    case ASCII_CR:
                        //this is a break in the line so ignore it and the newline and continue
                        c = seqSource.read();
                        while( isEOL(c) && c != -1)
                        {
                            c = seqSource.read();
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
                        StringBuilder octal = new StringBuilder();
                        octal.append( next );
                        c = seqSource.read();
                        char digit = (char)c;
                        if( digit >= '0' && digit <= '7' )
                        {
                            octal.append( digit );
                            c = seqSource.read();
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
                            throw new IOException( "Error: Expected octal character, actual='" + octal + "'", e );
                        }
                        out.write(character);
                        break;
                    }
                    default:
                    {
                        // dropping the backslash
                        // see 7.3.4.2 Literal Strings for further information
                        out.write(next);
                    }
                }
            }
            else
            {
                out.write(ch);
            }
            if (nextc != -2)
            {
                c = nextc;
            }
            else
            {
                c = seqSource.read();
            }
        }
        if (c != -1)
        {
            seqSource.unread(c);
        }
        return new COSString(out.toByteArray());
    }

    /**
     * This will parse a PDF HEX string with fail fast semantic
     * meaning that we stop if a not allowed character is found.
     * This is necessary in order to detect malformed input and
     * be able to skip to next object start.
     *
     * We assume starting '&lt;' was already read.
     * 
     * @return The parsed PDF string.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    private COSString parseCOSHexString() throws IOException
    {
        final StringBuilder sBuf = new StringBuilder();
        while( true )
        {
            int c = seqSource.read();
            if ( isHexDigit((char)c) )
            {
                sBuf.append( (char) c );
            }
            else if ( c == '>' )
            {
                break;
            }
            else if ( c < 0 ) 
            {
                throw new IOException( "Missing closing bracket for hex string. Reached EOS." );
            }
            else if ( ( c == ' ' ) || ( c == '\n' ) ||
                    ( c == '\t' ) || ( c == '\r' ) ||
                    ( c == '\b' ) || ( c == '\f' ) )
            {
                continue;
            }
            else
            {
                // if invalid chars was found: discard last
                // hex character if it is not part of a pair
                if (sBuf.length()%2!=0)
                {
                    sBuf.deleteCharAt(sBuf.length()-1);
                }
                
                // read till the closing bracket was found
                do 
                {
                    c = seqSource.read();
                } 
                while ( c != '>' && c >= 0 );
                
                // might have reached EOF while looking for the closing bracket
                // this can happen for malformed PDFs only. Make sure that there is
                // no endless loop.
                if ( c < 0 ) 
                {
                    throw new IOException( "Missing closing bracket for hex string. Reached EOS." );
                }
                
                // exit loop
                break;
            }
        }
        return COSString.parseHex(sBuf.toString());
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
        readExpectedChar('[');
        COSArray po = new COSArray();
        COSBase pbo;
        skipSpaces();
        int i;
        while( ((i = seqSource.peek()) > 0) && ((char)i != ']') )
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
                        COSObjectKey key = new COSObjectKey(number.longValue(), genNumber.intValue());
                        pbo = getObjectFromPool(key);
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
                //it could be a bad object in the array which is just skipped
                LOG.warn("Corrupt object reference at offset " + seqSource.getPosition());

                // This could also be an "endobj" or "endstream" which means we can assume that
                // the array has ended.
                String isThisTheEnd = readString();
                seqSource.unread(isThisTheEnd.getBytes(ISO_8859_1));
                if(ENDOBJ_STRING.equals(isThisTheEnd) || ENDSTREAM_STRING.equals(isThisTheEnd))
                {
                    return po;
                }
            }
            skipSpaces();
        }
        // read ']'
        seqSource.read(); 
        skipSpaces();
        return po;
    }

    /**
     * Determine if a character terminates a PDF name.
     *
     * @param ch The character
     * @return true if the character terminates a PDF name, otherwise false.
     */
    protected boolean isEndOfName(int ch)
    {
        return ch == ASCII_SPACE || ch == ASCII_CR || ch == ASCII_LF || ch == 9 || ch == '>' ||
               ch == '<' || ch == '[' || ch =='/' || ch ==']' || ch ==')' || ch =='(' || 
               ch == 0 || ch == '\f';
    }

    /**
     * This will parse a PDF name from the stream.
     *
     * @return The parsed PDF name.
     * @throws IOException If there is an error reading from the stream.
     */
    protected COSName parseCOSName() throws IOException
    {
        readExpectedChar('/');
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int c = seqSource.read();
        while (c != -1)
        {
            int ch = c;
            if (ch == '#')
            {
                int ch1 = seqSource.read();
                int ch2 = seqSource.read();
                // Prior to PDF v1.2, the # was not a special character.  Also,
                // it has been observed that various PDF tools do not follow the
                // spec with respect to the # escape, even though they report
                // PDF versions of 1.2 or later.  The solution here is that we
                // interpret the # as an escape only when it is followed by two
                // valid hex digits.
                if (isHexDigit((char)ch1) && isHexDigit((char)ch2))
                {
                    String hex = "" + (char)ch1 + (char)ch2;
                    try
                    {
                        buffer.write(Integer.parseInt(hex, 16));
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IOException("Error: expected hex digit, actual='" + hex + "'", e);
                    }
                    c = seqSource.read();
                }
                else
                {
                    // check for premature EOF
                    if (ch2 == -1 || ch1 == -1)
                    {
                        LOG.error("Premature EOF in BaseParser#parseCOSName");
                        c = -1;
                        break;
                    }
                    seqSource.unread(ch2);
                    c = ch1;
                    buffer.write(ch);
                }
            }
            else if (isEndOfName(ch))
            {
                break;
            }
            else
            {
                buffer.write(ch);
                c = seqSource.read();
            }
        }
        if (c != -1)
        {
            seqSource.unread(c);
        }
        
        byte[] bytes = buffer.toByteArray();
        String string;
        if (isValidUTF8(bytes))
        {
            string = new String(buffer.toByteArray(), Charsets.UTF_8);
        }
        else
        {
            // some malformed PDFs don't use UTF-8 see PDFBOX-3347
            string = new String(buffer.toByteArray(), Charsets.WINDOWS_1252);
        }
        return COSName.getPDFName(string);
    }

    /**
     * Returns true if a byte sequence is valid UTF-8.
     */
    private boolean isValidUTF8(byte[] input)
    {
        CharsetDecoder cs = Charsets.UTF_8.newDecoder();
        try
        {
            cs.decode(ByteBuffer.wrap(input));
            return true;
        }
        catch (CharacterCodingException e)
        {
            return false;
        }
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
        char c = (char) seqSource.peek();
        if( c == 't' )
        {
            String trueString = new String( seqSource.readFully( 4 ), ISO_8859_1 );
            if( !trueString.equals( TRUE ) )
            {
                throw new IOException( "Error parsing boolean: expected='true' actual='" + trueString 
                        + "' at offset " + seqSource.getPosition());
            }
            else
            {
                retval = COSBoolean.TRUE;
            }
        }
        else if( c == 'f' )
        {
            String falseString = new String( seqSource.readFully( 5 ), ISO_8859_1 );
            if( !falseString.equals( FALSE ) )
            {
                throw new IOException( "Error parsing boolean: expected='true' actual='" + falseString 
                        + "' at offset " + seqSource.getPosition());
            }
            else
            {
                retval = COSBoolean.FALSE;
            }
        }
        else
        {
            throw new IOException( "Error parsing boolean expected='t or f' actual='" + c 
                    + "' at offset " + seqSource.getPosition());
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
        int nextByte = seqSource.peek();
        char c = (char)nextByte;
        switch(c)
        {
        case '<':
        {
            // pull off first left bracket
            int leftBracket = seqSource.read();
            // check for second left bracket
            c = (char) seqSource.peek();
            seqSource.unread(leftBracket);
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
        case '[':
        {
            // array
            retval = parseCOSArray();
            break;
        }
        case '(':
            retval = parseCOSString();
            break;
        case '/':   
            // name
            retval = parseCOSName();
            break;
        case 'n':   
        {
            // null
            readExpectedString(NULL);
            retval = COSNull.NULL;
            break;
        }
        case 't':
        {
            String trueString = new String( seqSource.readFully(4), ISO_8859_1 );
            if( trueString.equals( TRUE ) )
            {
                retval = COSBoolean.TRUE;
            }
            else
            {
                throw new IOException( "expected true actual='" + trueString + "' " + seqSource + 
                        "' at offset " + seqSource.getPosition());
            }
            break;
        }
        case 'f':
        {
            String falseString = new String( seqSource.readFully(5), ISO_8859_1 );
            if( falseString.equals( FALSE ) )
            {
                retval = COSBoolean.FALSE;
            }
            else
            {
                throw new IOException( "expected false actual='" + falseString + "' " + seqSource + 
                        "' at offset " + seqSource.getPosition());
            }
            break;
        }
        case 'R':
            seqSource.read();
            retval = new COSObject(null);
            break;
        case (char)-1:
            return null;
        default:
        {
            if( Character.isDigit(c) || c == '-' || c == '+' || c == '.')
            {
                StringBuilder buf = new StringBuilder();
                int ic = seqSource.read();
                c = (char)ic;
                while( Character.isDigit( c )||
                        c == '-' ||
                        c == '+' ||
                        c == '.' ||
                        c == 'E' ||
                        c == 'e' )
                {
                    buf.append( c );
                    ic = seqSource.read();
                    c = (char)ic;
                }
                if( ic != -1 )
                {
                    seqSource.unread(ic);
                }
                retval = COSNumber.get( buf.toString() );
            }
            else
            {
                //This is not suppose to happen, but we will allow for it
                //so we are more compatible with POS writers that don't
                //follow the spec
                String badString = readString();
                if (badString.isEmpty())
                {
                    int peek = seqSource.peek();
                    // we can end up in an infinite loop otherwise
                    throw new IOException( "Unknown dir object c='" + c +
                            "' cInt=" + (int)c + " peek='" + (char)peek 
                            + "' peekInt=" + peek + " at offset " + seqSource.getPosition() );
                }

                // if it's an endstream/endobj, we want to put it back so the caller will see it
                if(ENDOBJ_STRING.equals(badString) || ENDSTREAM_STRING.equals(badString))
                {
                    seqSource.unread(badString.getBytes(ISO_8859_1));
                }
            }
        }
        }
        return retval;
    }

    /**
     * This will read the next string from the stream.
     *
     * @return The string that was read from the stream, never null.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected String readString() throws IOException
    {
        skipSpaces();
        StringBuilder buffer = new StringBuilder();
        int c = seqSource.read();
        while( !isEndOfName((char)c) && c != -1 )
        {
            buffer.append( (char)c );
            c = seqSource.read();
        }
        if (c != -1)
        {
            seqSource.unread(c);
        }
        return buffer.toString();
    }
    
    /**
     * Read one String and throw an exception if it is not the expected value.
     *
     * @param expectedString the String value that is expected.
     * @throws IOException if the String char is not the expected value or if an
     * I/O error occurs.
     */
    protected void readExpectedString(String expectedString) throws IOException
    {
        readExpectedString(expectedString.toCharArray(), false);
    }

    /**
     * Reads given pattern from {@link #seqSource}. Skipping whitespace at start and end if wanted.
     * 
     * @param expectedString pattern to be skipped
     * @param skipSpaces if set to true spaces before and after the string will be skipped
     * @throws IOException if pattern could not be read
     */
    protected final void readExpectedString(final char[] expectedString, boolean skipSpaces) throws IOException
    {
        skipSpaces();
        for (char c : expectedString)
        {
            if (seqSource.read() != c)
            {
                throw new IOException("Expected string '" + new String(expectedString)
                        + "' but missed at character '" + c + "' at offset "
                        + seqSource.getPosition());
            }
        }
        skipSpaces();
    }

    /**
     * Read one char and throw an exception if it is not the expected value.
     *
     * @param ec the char value that is expected.
     * @throws IOException if the read char is not the expected value or if an
     * I/O error occurs.
     */
    protected void readExpectedChar(char ec) throws IOException
    {
        char c = (char) seqSource.read();
        if (c != ec)
        {
            throw new IOException("expected='" + ec + "' actual='" + c + "' at offset " + seqSource.getPosition());
        }
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

        int c = seqSource.read();

        //average string size is around 2 and the normal string buffer size is
        //about 16 so lets save some space.
        StringBuilder buffer = new StringBuilder(length);
        while( !isWhitespace(c) && !isClosing(c) && c != -1 && buffer.length() < length &&
                c != '[' &&
                c != '<' &&
                c != '(' &&
                c != '/' )
        {
            buffer.append( (char)c );
            c = seqSource.read();
        }
        if (c != -1)
        {
            seqSource.unread(c);
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
        return isClosing(seqSource.peek());
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
     * NOTE: The EOL marker may consists of 1 (CR or LF) or 2 (CR and CL) bytes
     * which is an important detail if one wants to unread the line.
     *
     * @return The characters between the current position and the end of the line.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected String readLine() throws IOException
    {
        if (seqSource.isEOF())
        {
            throw new IOException( "Error: End-of-File, expected line");
        }

        StringBuilder buffer = new StringBuilder( 11 );

        int c;
        while ((c = seqSource.read()) != -1)
        {
            // CR and LF are valid EOLs
            if (isEOL(c))
            {
                break;
            }
            buffer.append( (char)c );
        }
        // CR+LF is also a valid EOL 
        if (isCR(c) && isLF(seqSource.peek()))
        {
            seqSource.read();
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
        return isEOL(seqSource.peek());
    }

    /**
     * This will tell if the next byte to be read is an end of line byte.
     *
     * @param c The character to check against end of line
     * @return true if the next byte is 0x0A or 0x0D.
     */
    protected boolean isEOL(int c)
    {
        return isLF(c) || isCR(c);
    }

    private boolean isLF(int c)
    {
        return ASCII_LF == c;
    }

    private boolean isCR(int c)
    {
        return ASCII_CR == c;
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
        return isWhitespace(seqSource.peek());
    }

    /**
     * This will tell if a character is whitespace or not.  These values are
     * specified in table 1 (page 12) of ISO 32000-1:2008.
     * @param c The character to check against whitespace
     * @return true if the character is a whitespace character.
     */
    protected boolean isWhitespace( int c )
    {
        return c == 0 || c == 9 || c == 12  || c == ASCII_LF
        || c == ASCII_CR || c == ASCII_SPACE;
    }

    /**
     * This will tell if the next byte is a space or not.
     *
     * @return true if the next byte in the stream is a space character.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected boolean isSpace() throws IOException
    {
        return isSpace(seqSource.peek());
    }
    
    /**
     * This will tell if the given value is a space or not.
     * 
     * @param c The character to check against space
     * @return true if the next byte in the stream is a space character.
     */
    protected boolean isSpace(int c)
    {
        return ASCII_SPACE == c;
    }

    /**
     * This will tell if the next byte is a digit or not.
     *
     * @return true if the next byte in the stream is a digit.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected boolean isDigit() throws IOException
    {
        return isDigit(seqSource.peek());
    }

    /**
     * This will tell if the given value is a digit or not.
     * 
     * @param c The character to be checked
     * @return true if the next byte in the stream is a digit.
     */
    protected static boolean isDigit(int c)
    {
        return c >= ASCII_ZERO && c <= ASCII_NINE;
    }

    /**
     * This will skip all spaces and comments that are present.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected void skipSpaces() throws IOException
    {
        int c = seqSource.read();
        // 37 is the % character, a comment
        while( isWhitespace(c) || c == 37)
        {
            if ( c == 37 )
            {
                // skip past the comment section
                c = seqSource.read();
                while(!isEOL(c) && c != -1)
                {
                    c = seqSource.read();
                }
            }
            else
            {
                c = seqSource.read();
            }
        }
        if (c != -1)
        {
            seqSource.unread(c);
        }
    }

    /**
     * This will read a long from the Stream and throw an {@link IOException} if
     * the long value is negative or has more than 10 digits (i.e. : bigger than
     * {@link #OBJECT_NUMBER_THRESHOLD})
     *
     * @return the object number being read.
     * @throws IOException if an I/O error occurs
     */
    protected long readObjectNumber() throws IOException
    {
        long retval = readLong();
        if (retval < 0 || retval >= OBJECT_NUMBER_THRESHOLD)
        {
            throw new IOException("Object Number '" + retval + "' has more than 10 digits or is negative");
        }
        return retval;
    }

    /**
     * This will read a integer from the Stream and throw an {@link IllegalArgumentException} if the integer value
     * has more than the maximum object revision (i.e. : bigger than {@link #GENERATION_NUMBER_THRESHOLD})
     * @return the generation number being read.
     * @throws IOException if an I/O error occurs
     */
    protected int readGenerationNumber() throws IOException
    {
        int retval = readInt();
        if(retval < 0 || retval > GENERATION_NUMBER_THRESHOLD)
        {
            throw new IOException("Generation Number '" + retval + "' has more than 5 digits");
        }
        return retval;
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

        StringBuilder intBuffer = readStringNumber();

        try
        {
            retval = Integer.parseInt( intBuffer.toString() );
        }
        catch( NumberFormatException e )
        {
            seqSource.unread(intBuffer.toString().getBytes(ISO_8859_1));
            throw new IOException( "Error: Expected an integer type at offset "+ seqSource.getPosition(), e);
        }
        return retval;
    }
    

    /**
     * This will read an long from the stream.
     *
     * @return The long that was read from the stream.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected long readLong() throws IOException
    {
        skipSpaces();
        long retval = 0;

        StringBuilder longBuffer = readStringNumber();

        try
        {
            retval = Long.parseLong( longBuffer.toString() );
        }
        catch( NumberFormatException e )
        {
            seqSource.unread(longBuffer.toString().getBytes(ISO_8859_1));
            throw new IOException( "Error: Expected a long type at offset "
                    + seqSource.getPosition() + ", instead got '" + longBuffer + "'", e);
        }
        return retval;
    }

    /**
     * This method is used to read a token by the {@linkplain #readInt()} method
     * and the {@linkplain #readLong()} method.
     *
     * @return the token to parse as integer or long by the calling method.
     * @throws IOException throws by the {@link #seqSource} methods.
     */
    protected final StringBuilder readStringNumber() throws IOException
    {
        int lastByte = 0;
        StringBuilder buffer = new StringBuilder();
        while( (lastByte = seqSource.read() ) != ASCII_SPACE &&
                lastByte != ASCII_LF &&
                lastByte != ASCII_CR &&
                lastByte != 60 && //see sourceforge bug 1714707
                lastByte != '[' && // PDFBOX-1845
                lastByte != '(' && // PDFBOX-2579
                lastByte != 0 && //See sourceforge bug 853328
                lastByte != -1 )
        {
            buffer.append( (char)lastByte );
            if (buffer.length() > MAX_LENGTH_LONG)
            {
                throw new IOException("Number '" + buffer + 
                        "' is getting too long, stop reading at offset " + seqSource.getPosition());
            }
        }
        if( lastByte != -1 )
        {
            seqSource.unread(lastByte);
        }
        return buffer;
    }
}
