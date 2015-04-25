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
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.io.PushBackInputStream;
import org.apache.pdfbox.cos.COSObjectKey;
import static org.apache.pdfbox.util.Charsets.ISO_8859_1;

/**
 * This class is used to contain parsing logic that will be used by both the
 * PDFParser and the COSStreamParser.
 *
 * @author Ben Litchfield
 */
public abstract class BaseParser implements Closeable
{

    private static final long OBJECT_NUMBER_THRESHOLD = 10000000000L;

    private static final long GENERATION_NUMBER_THRESHOLD = 65535;
    
    /**
     * system property allowing to define size of push back buffer.
     */
    public static final String PROP_PUSHBACK_SIZE = "org.apache.pdfbox.baseParser.pushBackSize";

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(BaseParser.class);

    private static final int E = 'e';
    private static final int N = 'n';
    private static final int D = 'd';

    private static final int S = 's';
    private static final int T = 't';
    private static final int R = 'r';
    private static final int A = 'a';
    private static final int M = 'm';

    private static final int O = 'o';
    private static final int B = 'b';
    private static final int J = 'j';

    private static final int STRMBUFLEN = 2048;
    private final byte[] strmBuf    = new byte[ STRMBUFLEN ];

    /**
     * This is a byte array that will be used for comparisons.
     */
    public static final byte[] ENDSTREAM =
        new byte[] { E, N, D, S, T, R, E, A, M };

    /**
     * This is a byte array that will be used for comparisons.
     */
    public static final byte[] ENDOBJ =
        new byte[] { E, N, D, O, B, J };

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
    protected PushBackInputStream pdfSource;

    /**
     * This is the document that will be parsed.
     */
    protected COSDocument document;

    /**
     * Default constructor.
     */
    public BaseParser()
    {
    }

    /**
     * Constructor.
     *
     * @param input The input stream to read the data from.
     * @throws IOException If there is an error reading the input stream.
     */
    public BaseParser(InputStream input) throws IOException
    {
        int pushbacksize = 65536;
        try
        {
            pushbacksize = Integer.getInteger(PROP_PUSHBACK_SIZE, 65536);
        }
        catch (SecurityException e) 
        {
            // PDFBOX-1946 getInteger calls System.getProperties, 
            // which can get exception in an applet
            // ignore and use default
        }
        this.pdfSource = new PushBackInputStream(
                new BufferedInputStream(input, 16384), pushbacksize);
    }

    private static boolean isHexDigit(char ch)
    {
        return (ch >= ASCII_ZERO && ch <= ASCII_NINE) ||
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
        COSBase retval = null;
        long numOffset = pdfSource.getOffset();
        COSBase number = parseDirObject();
        skipSpaces();
        char next = (char)pdfSource.peek();
        if( next >= ASCII_ZERO && next <= ASCII_NINE )
        {
            long genOffset = pdfSource.getOffset();
            COSBase generationNumber = parseDirObject();
            skipSpaces();
            readExpectedChar('R');
            if (!(number instanceof COSInteger))
            {
                throw new IOException("expected number, actual=" + number + " at offset " + numOffset);
            }
            if (!(generationNumber instanceof COSInteger))
            {
                throw new IOException("expected number, actual=" + number + " at offset " + genOffset);
            }
            COSObjectKey key = new COSObjectKey(((COSInteger) number).longValue(),
                    ((COSInteger) generationNumber).intValue());
            retval = getObjectFromPool(key);
        }
        else
        {
            retval = number;
        }
        return retval;
    }

    private COSBase getObjectFromPool(COSObjectKey key) throws IOException
    {
        if (document == null)
        {
            throw new IOException("object reference " + key + " at offset " + pdfSource.getOffset() + " in content stream");
        }
        return document.getObjectFromPool(key);
    }

    /**
     * This will parse a PDF dictionary.
     *
     * @return The parsed dictionary.
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
        while( !done )
        {
            skipSpaces();
            char c = (char)pdfSource.peek();
            if( c == '>')
            {
                done = true;
            }
            else
                if(c != '/')
                {
                    //an invalid dictionary, we are expecting
                    //the key, read until we can recover
                    LOG.warn("Invalid dictionary, found: '" + c + "' but expected: '/'");
                    int read = pdfSource.read();
                    while(read != -1 && read != '/' && read != '>')
                    {
                        // in addition to stopping when we find / or >, we also want
                        // to stop when we find endstream or endobj.
                        if(read==E) 
                        {
                            read = pdfSource.read();
                            if(read==N) 
                            {
                                read = pdfSource.read();
                                if(read==D)
                                {
                                    read = pdfSource.read();
                                    boolean isStream = read == S && pdfSource.read() == T && pdfSource.read() == R
                                            && pdfSource.read() == E && pdfSource.read() == A && pdfSource.read() == M;

                                    boolean isObj = !isStream && read == O && pdfSource.read() == B && pdfSource.read() == J;
                                    if (isStream || isObj)
                                    {
                                        return obj; // we're done reading this object!
                                    }
                                }
                            }
                        }
                        read = pdfSource.read();
                    }
                    if(read != -1)
                    {
                        pdfSource.unread(read);
                    }
                    else
                    {
                        return obj;
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
                        pdfSource.unread( potentialDEF.getBytes(ISO_8859_1) );
                    }
                    else
                    {
                        skipSpaces();
                    }
                }

                if( value == null )
                {
                    LOG.warn("Bad Dictionary Declaration " + pdfSource );
                }
                else
                {
                    value.setDirect(true);
                    obj.setItem( key, value );
                }
            }
        }
        readExpectedChar('>');
        readExpectedChar('>');
        return obj;
    }

    protected void skipWhiteSpaces() throws IOException
    {
        //PDF Ref 3.2.7 A stream must be followed by either
        //a CRLF or LF but nothing else.

        int whitespace = pdfSource.read();

        //see brother_scan_cover.pdf, it adds whitespaces
        //after the stream but before the start of the
        //data, so just read those first
        while (ASCII_SPACE == whitespace)
        {
            whitespace = pdfSource.read();
        }

        if (ASCII_CR == whitespace)
        {
            whitespace = pdfSource.read();
            if (ASCII_LF != whitespace)
            {
                pdfSource.unread(whitespace);
                //The spec says this is invalid but it happens in the real
                //world so we must support it.
            }
        }
        else if (ASCII_LF != whitespace)
        {
            //we are in an error.
            //but again we will do a lenient parsing and just assume that everything
            //is fine
            pdfSource.unread(whitespace);
        }
    }

    /**
     * This method will read through the current stream object until
     * we find the keyword "endstream" meaning we're at the end of this
     * object. Some pdf files, however, forget to write some endstream tags
     * and just close off objects with an "endobj" tag so we have to handle
     * this case as well.
     * 
     * This method is optimized using buffered IO and reduced number of
     * byte compare operations.
     * 
     * @param out  stream we write out to.
     * 
     * @throws IOException if something went wrong
     */
    protected void readUntilEndStream( final OutputStream out ) throws IOException
    {
        int bufSize;
        int charMatchCount = 0;
        byte[] keyw = ENDSTREAM;
        
        // last character position of shortest keyword ('endobj')
        final int quickTestOffset = 5;
        
        // read next chunk into buffer; already matched chars are added to beginning of buffer
        while ( ( bufSize = pdfSource.read( strmBuf, charMatchCount, STRMBUFLEN - charMatchCount ) ) > 0 ) 
        {
            bufSize += charMatchCount;
            
            int bIdx = charMatchCount;
            int quickTestIdx;
        
            // iterate over buffer, trying to find keyword match
            for ( int maxQuicktestIdx = bufSize - quickTestOffset; bIdx < bufSize; bIdx++ ) 
            {
                // reduce compare operations by first test last character we would have to
                // match if current one matches; if it is not a character from keywords
                // we can move behind the test character;
                // this shortcut is inspired by the Boyer-Moore string search algorithm
                // and can reduce parsing time by approx. 20%
                if ( ( charMatchCount == 0 ) &&
                         ( ( quickTestIdx = bIdx + quickTestOffset ) < maxQuicktestIdx ) ) 
                {
                    
                    final byte ch = strmBuf[quickTestIdx];
                    if ( ( ch > 't' ) || ( ch < 'a' ) ) 
                    {
                        // last character we would have to match if current character would match
                        // is not a character from keywords -> jump behind and start over
                        bIdx = quickTestIdx;
                        continue;
                    }
                }
                
                // could be negative - but we only compare to ASCII
                final byte ch = strmBuf[bIdx];
            
                if ( ch == keyw[ charMatchCount ] ) 
                {
                    if ( ++charMatchCount == keyw.length ) 
                    {
                        // match found
                        bIdx++;
                        break;
                    }
                } 
                else 
                {
                    if ( ( charMatchCount == 3 ) && ( ch == ENDOBJ[ charMatchCount ] ) ) 
                    {
                        // maybe ENDSTREAM is missing but we could have ENDOBJ
                        keyw = ENDOBJ;
                        charMatchCount++;
                    } 
                    else 
                    {
                        // no match; incrementing match start by 1 would be dumb since we already know matched chars
                        // depending on current char read we may already have beginning of a new match:
                        // 'e': first char matched;
                        // 'n': if we are at match position idx 7 we already read 'e' thus 2 chars matched
                        // for each other char we have to start matching first keyword char beginning with next 
                        // read position
                        charMatchCount = ( ch == E ) ? 1 : ( ( ch == N ) && ( charMatchCount == 7 ) ) ? 2 : 0;
                        // search again for 'endstream'
                        keyw = ENDSTREAM;
                    }
                } 
            }  // for
            
            int contentBytes = Math.max( 0, bIdx - charMatchCount );
            
            // write buffer content until first matched char to output stream
            if ( contentBytes > 0 )
            {
                out.write( strmBuf, 0, contentBytes );
            }
            if ( charMatchCount == keyw.length ) 
            {
                // keyword matched; unread matched keyword (endstream/endobj) and following buffered content
                pdfSource.unread( strmBuf, contentBytes, bufSize - contentBytes );
                break;
            } 
            else 
            {
                // copy matched chars at start of buffer
                System.arraycopy( keyw, 0, strmBuf, 0, charMatchCount );
            }
            
        }
        // this writes a lonely CR or drops trailing CR LF and LF
        out.flush();
    }
    
    /**
     * This is really a bug in the Document creators code, but it caused a crash
     * in PDFBox, the first bug was in this format:
     * /Title ( (5)
     * /Creator which was patched in 1 place.
     * However it missed the case where the Close Paren was escaped
     *
     * The second bug was in this format
     * /Title (c:\)
     * /Producer
     *
     * This patch  moves this code out of the parseCOSString method, so it can be used twice.
     *
     *
     * @param bracesParameter the number of braces currently open.
     *
     * @return the corrected value of the brace counter
     * @throws IOException
     */
    private int checkForMissingCloseParen(final int bracesParameter) throws IOException
    {
        int braces = bracesParameter;
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
        //
        if (amountRead == 3 &&
               (( nextThreeBytes[0] == ASCII_CR  // Look for a carriage return
               && nextThreeBytes[1] == ASCII_LF  // Look for a new line
               && nextThreeBytes[2] == 0x2f ) // Look for a slash /
                                              // Add a second case without a new line
               || (nextThreeBytes[0] == ASCII_CR  // Look for a carriage return
                && nextThreeBytes[1] == 0x2f )))  // Look for a slash /
            {
                braces = 0;
            }
        if (amountRead > 0)
        {
            pdfSource.unread( nextThreeBytes, 0, amountRead );
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
        char nextChar = (char)pdfSource.read();
        char openBrace;
        char closeBrace;
        if( nextChar == '(' )
        {
            openBrace = '(';
            closeBrace = ')';
        }
        else if( nextChar == '<' )
        {
            return parseCOSHexString();
        }
        else
        {
            throw new IOException( "parseCOSString string should start with '(' or '<' and not '" +
                    nextChar + "' " + pdfSource );
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //This is the number of braces read
        //
        int braces = 1;
        int c = pdfSource.read();
        while( braces > 0 && c != -1)
        {
            char ch = (char)c;
            int nextc = -2; // not yet read

            if(ch == closeBrace)
            {

                braces--;
                braces = checkForMissingCloseParen(braces);
                if( braces != 0 )
                {
                    out.write(ch);
                }
            }
            else if( ch == openBrace )
            {
                braces++;
                out.write(ch);
            }
            else if( ch == '\\' )
            {
                //patched by ram
                char next = (char)pdfSource.read();
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
                        braces = checkForMissingCloseParen(braces);
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
                c = pdfSource.read();
            }
        }
        if (c != -1)
        {
            pdfSource.unread(c);
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
            int c = pdfSource.read();
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
                    c = pdfSource.read();
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
                LOG.warn("Corrupt object reference at offset " + pdfSource.getOffset());

                // This could also be an "endobj" or "endstream" which means we can assume that
                // the array has ended.
                String isThisTheEnd = readString();
                pdfSource.unread(isThisTheEnd.getBytes(ISO_8859_1));
                if(ENDOBJ_STRING.equals(isThisTheEnd) || ENDSTREAM_STRING.equals(isThisTheEnd))
                {
                    return po;
                }
            }
            skipSpaces();
        }
        // read ']'
        pdfSource.read(); 
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
        return (ch == ASCII_SPACE || ch == ASCII_CR || ch == ASCII_LF || ch == 9 || ch == '>' || ch == '<'
            || ch == '[' || ch =='/' || ch ==']' || ch ==')' || ch =='('
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
        readExpectedChar('/');
        // costruisce il nome
        StringBuilder buffer = new StringBuilder();
        int c = pdfSource.read();
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
                        throw new IOException("Error: expected hex number, actual='" + hex + "'", e);
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
        return COSName.getPDFName( buffer.toString() );
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
            String trueString = new String( pdfSource.readFully( 4 ), ISO_8859_1 );
            if( !trueString.equals( TRUE ) )
            {
                throw new IOException( "Error parsing boolean: expected='true' actual='" + trueString 
                        + "' at offset " + pdfSource.getOffset());
            }
            else
            {
                retval = COSBoolean.TRUE;
            }
        }
        else if( c == 'f' )
        {
            String falseString = new String( pdfSource.readFully( 5 ), ISO_8859_1 );
            if( !falseString.equals( FALSE ) )
            {
                throw new IOException( "Error parsing boolean: expected='true' actual='" + falseString 
                        + "' at offset " + pdfSource.getOffset());
            }
            else
            {
                retval = COSBoolean.FALSE;
            }
        }
        else
        {
            throw new IOException( "Error parsing boolean expected='t or f' actual='" + c 
                    + "' at offset " + pdfSource.getOffset());
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
            // pull off first left bracket
            int leftBracket = pdfSource.read();
            // check for second left bracket
            c = (char)pdfSource.peek(); 
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
            String trueString = new String( pdfSource.readFully(4), ISO_8859_1 );
            if( trueString.equals( TRUE ) )
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
            String falseString = new String( pdfSource.readFully(5), ISO_8859_1 );
            if( falseString.equals( FALSE ) )
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
                StringBuilder buf = new StringBuilder();
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
                if( badString == null || badString.length() == 0 )
                {
                    int peek = pdfSource.peek();
                    // we can end up in an infinite loop otherwise
                    throw new IOException( "Unknown dir object c='" + c +
                            "' cInt=" + (int)c + " peek='" + (char)peek 
                            + "' peekInt=" + peek + " " + pdfSource.getOffset() );
                }

                // if it's an endstream/endobj, we want to put it back so the caller will see it
                if(ENDOBJ_STRING.equals(badString) || ENDSTREAM_STRING.equals(badString))
                {
                    pdfSource.unread(badString.getBytes(ISO_8859_1));
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
        StringBuilder buffer = new StringBuilder();
        int c = pdfSource.read();
        while( !isEndOfName((char)c) && c != -1 )
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
     * Reads given pattern from {@link #pdfSource}. Skipping whitespace at start and end if wanted.
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
            if (pdfSource.read() != c)
            {
                throw new IOException("Expected string '" + new String(expectedString)
                        + "' but missed at character '" + c + "' at offset "
                        + pdfSource.getOffset());
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
        char c = (char) pdfSource.read();
        if (c != ec)
        {
            throw new IOException("expected='" + ec + "' actual='" + c + "' at offset " + pdfSource.getOffset());
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

        int c = pdfSource.read();

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
     * NOTE: The EOL marker may consists of 1 (CR or LF) or 2 (CR and CL) bytes
     * which is an important detail if one wants to unread the line.
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

        StringBuilder buffer = new StringBuilder( 11 );

        int c;
        while ((c = pdfSource.read()) != -1)
        {
            // CR and LF are valid EOLs
            if (isEOL(c))
            {
                break;
            }
            buffer.append( (char)c );
        }
        // CR+LF is also a valid EOL 
        if (isCR(c) && isLF(pdfSource.peek()))
        {
            pdfSource.read();
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
        return isWhitespace( pdfSource.peek() );
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
        return isSpace( pdfSource.peek() );
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
        return isDigit( pdfSource.peek() );
    }

    /**
     * This will tell if the given value is a digit or not.
     * 
     * @param c The character to be checked
     * @return true if the next byte in the stream is a digit.
     */
    protected boolean isDigit(int c)
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
        int c = pdfSource.read();
        // 37 is the % character, a comment
        while( isWhitespace(c) || c == 37)
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
            pdfSource.unread(intBuffer.toString().getBytes(ISO_8859_1));
            throw new IOException( "Error: Expected an integer type at offset "+pdfSource.getOffset(), e);
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
            pdfSource.unread(longBuffer.toString().getBytes(ISO_8859_1));
            throw new IOException( "Error: Expected a long type at offset "
                    + pdfSource.getOffset() + ", instead got '" + longBuffer + "'", e);
        }
        return retval;
    }

    /**
     * This method is used to read a token by the {@linkplain #readInt()} method
     * and the {@linkplain #readLong()} method.
     *
     * @return the token to parse as integer or long by the calling method.
     * @throws IOException throws by the {@link #pdfSource} methods.
     */
    protected final StringBuilder readStringNumber() throws IOException
    {
        int lastByte = 0;
        StringBuilder buffer = new StringBuilder();
        while( (lastByte = pdfSource.read() ) != ASCII_SPACE &&
                lastByte != ASCII_LF &&
                lastByte != ASCII_CR &&
                lastByte != 60 && //see sourceforge bug 1714707
                lastByte != '[' && // PDFBOX-1845
                lastByte != '(' && // PDFBOX-2579
                lastByte != 0 && //See sourceforge bug 853328
                lastByte != -1 )
        {
            buffer.append( (char)lastByte );
        }
        if( lastByte != -1 )
        {
            pdfSource.unread( lastByte );
        }
        return buffer;
    }
    
    @Override
    public void close() throws IOException
    {
        if (pdfSource != null)
        {
            pdfSource.close();
        }
    }
}
