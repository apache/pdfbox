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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.WrappedIOException;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.PushBackInputStream;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.persistence.util.COSObjectKey;

/**
 * This class is used to contain parsing logic that will be used by both the
 * PDFParser and the COSStreamParser.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public abstract class BaseParser
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

    private final int    strmBufLen = 2048;
    private final byte[] strmBuf    = new byte[ strmBufLen ];

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
    private static final String ENDOBJ_STRING = "endobj";
    /**
     * This is a string constant that will be used for comparisons.
     */
    private static final String ENDSTREAM_STRING = "endstream";
    /**
     * This is a string constant that will be used for comparisons.
     */
    private static final String STREAM_STRING = "stream";
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
     * Default value of the {@link #forceParsing} flag.
     */
    protected static final boolean FORCE_PARSING =
        Boolean.getBoolean("org.apache.pdfbox.forceParsing");

    /**
     * This is the stream that will be read from.
     */
    protected PushBackInputStream pdfSource;

    /**
     * This is the document that will be parsed.
     */
    protected COSDocument document;

    /**
     * Flag to skip malformed or otherwise unparseable input where possible.
     */
    protected final boolean forceParsing;

    /**
     * Default constructor.
     */
    public BaseParser()
    {
        this.forceParsing = FORCE_PARSING;
    }

    /**
     * Constructor.
     *
     * @since Apache PDFBox 1.3.0
     * @param input The input stream to read the data from.
     * @param forceParsingValue flag to skip malformed or otherwise unparseable
     *                     input where possible
     * @throws IOException If there is an error reading the input stream.
     */
    public BaseParser(InputStream input, boolean forceParsingValue)
            throws IOException
    {
        this.pdfSource = new PushBackInputStream(
                new BufferedInputStream(input, 16384),  Integer.getInteger( PROP_PUSHBACK_SIZE, 65536 ) );
        this.forceParsing = forceParsingValue;
    }

    /**
     * Constructor.
     *
     * @param input The input stream to read the data from.
     * @throws IOException If there is an error reading the input stream.
     */
    public BaseParser(InputStream input) throws IOException 
    {
        this(input, FORCE_PARSING);
    }

    /**
     * Constructor.
     *
     * @param input The array to read the data from.
     * @throws IOException If there is an error reading the byte data.
     */
    protected BaseParser(byte[] input) throws IOException 
    {
        this(new ByteArrayInputStream(input));
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
                                    if(read==S) 
                                    {
                                        read = pdfSource.read();
                                        if(read==T) 
                                        {
                                            read = pdfSource.read();
                                            if(read==R) 
                                            {
                                                read = pdfSource.read();
                                                if(read==E) 
                                                {
                                                    read = pdfSource.read();
                                                    if(read==A) 
                                                    {
                                                        read = pdfSource.read();
                                                        if(read==M) 
                                                        {
                                                            return obj; // we're done reading this object!
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } 
                                    else if(read==O) 
                                    {
                                        read = pdfSource.read();
                                        if(read==B) 
                                        {
                                            read = pdfSource.read();
                                            if(read==J) 
                                            {
                                                return obj; // we're done reading this object!
                                            }
                                        }
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
                        pdfSource.unread( potentialDEF.getBytes("ISO-8859-1") );
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

            if (!streamString.equals(STREAM_STRING))
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

            // try to read stream length - even if it is an indirect object
            int length = -1;
            if ( streamLength instanceof COSNumber )
            {
                length = ( (COSNumber) streamLength).intValue();
            }
// commented out next chunk since for the sequentially working PDFParser
// we do not know if length object is redefined later on and the currently
// read indirect object might be obsolete (e.g. not referenced in xref table);
// this would result in reading wrong number of bytes;
// Thus the only reliable information is a direct length. 
// This exclusion shouldn't harm much since in case of indirect objects they will
// typically be defined after the stream object, thus keeping the directly
// provided length will fix most cases
//            else if ( ( streamLength instanceof COSObject ) &&
//                      ( ( (COSObject) streamLength ).getObject() instanceof COSNumber ) )
//            {
//                length = ( (COSNumber) ( (COSObject) streamLength ).getObject() ).intValue();
//            } 
            
            if ( length == -1 )
            {
                // Couldn't determine length from dict: just
                // scan until we find endstream:
                readUntilEndStream( out );
            }
            else
            {
                // Copy length bytes over:
                int left = length;
                while ( left > 0 )
                {
                    final int chunk = Math.min( left, strmBufLen );
                    final int readCount = pdfSource.read( strmBuf, 0, chunk );
                    if ( readCount == -1 )
                    {
                        break;
                    }
                    out.write( strmBuf, 0, readCount );
                    left -= readCount;
                }
                
                // in order to handle broken documents we test if 'endstream' is reached
                // if not, length value possibly was wrong, fall back to scanning for endstream
                
                // fill buffer with next bytes and test for 'endstream' (with leading whitespaces)
                int readCount = pdfSource.read( strmBuf, 0, 20 );
                if ( readCount > 0 )
                {
                    boolean foundEndstream    = false;
                    int     nextEndstreamCIdx = 0;
                    for ( int cIdx = 0; cIdx < readCount; cIdx++ )
                    {
                        final int ch = strmBuf[ cIdx ] & 0xff; 
                        if ( ch == ENDSTREAM[ nextEndstreamCIdx ] )
                        {
                            if ( ++nextEndstreamCIdx >= ENDSTREAM.length )
                            {
                                foundEndstream = true;
                                break;
                            }
                        }
                        else if ( ( nextEndstreamCIdx > 0 ) || ( ! isWhitespace( ch ) ) )
                        {
                            // not found
                            break;
                        }
                    }
                    
                    // push back test bytes
                    pdfSource.unread( strmBuf, 0, readCount );
                    
                    // if 'endstream' was not found fall back to scanning
                    if ( ! foundEndstream )
                    {
                        LOG.warn("Specified stream length " + length 
                                + " is wrong. Fall back to reading stream until 'endstream'.");
                        
                        // push back all read stream bytes
                        // we got a buffered stream wrapper around filteredStream thus first flush to underlying stream
                        out.flush();
                        InputStream writtenStreamBytes = stream.getFilteredStream();
                        ByteArrayOutputStream     bout = new ByteArrayOutputStream( length );
                        
                        while ( ( readCount = writtenStreamBytes.read( strmBuf ) ) >= 0 )
                        {
                            bout.write( strmBuf, 0, readCount );
                        }
                        try
                        {
                            pdfSource.unread( bout.toByteArray() );
                        }
                        catch ( IOException ioe )
                        {
                            throw new WrappedIOException( "Could not push back " + bout.size() + 
                                                          " bytes in order to reparse stream. " +
                                                          "Try increasing push back buffer using system property " +
                                                          PROP_PUSHBACK_SIZE, ioe );
                        }
                        // create new filtered stream
                        if (out != null)
                        {
                        	IOUtils.closeQuietly(out);
                        }
                        out = stream.createFilteredStream( streamLength );
                        // scan until we find endstream:
                        readUntilEndStream( out );
                    }
                }
            }
            
            skipSpaces();
            String endStream = readString();

            if (!endStream.equals(ENDSTREAM_STRING))
            {
                /*
                 * Sometimes stream objects don't have an endstream tag so readUntilEndStream(out)
                 * also can stop on endobj tags. If that's the case we need to make sure to unread
                 * the endobj so parseObject() can handle that case normally.
                 */
                if (endStream.startsWith(ENDOBJ_STRING))
                {
                    byte[] endobjarray = endStream.getBytes("ISO-8859-1");
                    pdfSource.unread(endobjarray);
                }
                /*
                 * Some PDF files don't contain a new line after endstream so we
                 * need to make sure that the next object number is getting read separately
                 * and not part of the endstream keyword. Ex. Some files would have "endstream8"
                 * instead of "endstream"
                 */
                else if(endStream.startsWith(ENDSTREAM_STRING))
                {
                    String extra = endStream.substring(9, endStream.length());
                    endStream = endStream.substring(0, 9);
                    byte[] array = extra.getBytes("ISO-8859-1");
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
                    if( !endStream.equals( ENDSTREAM_STRING ) )
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
     * 
     * This method is optimized using buffered IO and reduced number of
     * byte compare operations.
     * 
     * @param out  stream we write out to.
     * 
     * @throws IOException
     */
    private void readUntilEndStream( final OutputStream out ) throws IOException
    {

        int bufSize;
        int charMatchCount = 0;
        byte[] keyw = ENDSTREAM;
        
        final int quickTestOffset = 5;  // last character position of shortest keyword ('endobj')
        
        // read next chunk into buffer; already matched chars are added to beginning of buffer
        while ( ( bufSize = pdfSource.read( strmBuf, charMatchCount, strmBufLen - charMatchCount ) ) > 0 ) 
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
                // this shortcut is inspired by Boyer–Moore string search algorithm
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
                
                final byte ch = strmBuf[bIdx];  // could be negative - but we only compare to ASCII
            
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
            
        }  // while
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
        if (amountRead == 3)
        {
            if (( nextThreeBytes[0] == 0x0d        // Look for a carriage return
                    && nextThreeBytes[1] == 0x0a   // Look for a new line
                    && nextThreeBytes[2] == 0x2f ) // Look for a slash /
                                                   // Add a second case without a new line
                    || (nextThreeBytes[0] == 0x0d  // Look for a carriage return
                            && nextThreeBytes[1] == 0x2f ))  // Look for a slash /
            {
                braces = 0;
            }
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
     * @param isDictionary indicates if the stream is a dictionary or not
     * @return The parsed PDF string.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected COSString parseCOSString(boolean isDictionary) throws IOException
    {
        char nextChar = (char)pdfSource.read();
        COSString retval = new COSString(isDictionary);
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
                    case ')':
                        // PDFBox 276 /Title (c:\)
                        braces = checkForMissingCloseParen(braces);
                        if( braces != 0 )
                        {
                            retval.append( next );
                        }
                        else
                        {
                            retval.append('\\');
                        }
                        break;
                    case '(':
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
                        // dropping the backslash
                        // see 7.3.4.2 Literal Strings for further information
                        retval.append( next );
                    }
                }
            }
            else
            {
                retval.append( ch );
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
        return retval;
    }

    /**
     * This will parse a PDF HEX string with fail fast semantic
     * meaning that we stop if a not allowed character is found.
     * This is necessary in order to detect malformed input and
     * be able to skip to next object start.
     *
     * We assume starting '<' was already read.
     * 
     * @return The parsed PDF string.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    private final COSString parseCOSHexString() throws IOException
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
                } while ( c != '>' && c >= 0 );
                
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
        return COSString.createFromHexString( sBuf.toString(), forceParsing );
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
                //it could be a bad object in the array which is just skipped
                LOG.warn("Corrupt object reference" );

                // This could also be an "endobj" or "endstream" which means we can assume that
                // the array has ended.
                String isThisTheEnd = readString();
                pdfSource.unread(isThisTheEnd.getBytes("ISO-8859-1"));
                if(ENDOBJ_STRING.equals(isThisTheEnd) || ENDSTREAM_STRING.equals(isThisTheEnd))
                {
                    return po;
                }
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
        StringBuilder buffer = new StringBuilder();
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
            String trueString = new String( pdfSource.readFully( 4 ), "ISO-8859-1" );
            if( !trueString.equals( TRUE ) )
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
            String falseString = new String( pdfSource.readFully( 5 ), "ISO-8859-1" );
            if( !falseString.equals( FALSE ) )
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
                retval = parseCOSString(true);
            }
            break;
        }
        case '[': // array
        {
            retval = parseCOSArray();
            break;
        }
        case '(':
            retval = parseCOSString(true);
            break;
        case '/':   // name
            retval = parseCOSName();
            break;
        case 'n':   // null
        {
            String nullString = readString();
            if( !nullString.equals( NULL) )
            {
                throw new IOException("Expected='null' actual='" + nullString + "'");
            }
            retval = COSNull.NULL;
            break;
        }
        case 't':
        {
            String trueString = new String( pdfSource.readFully(4), "ISO-8859-1" );
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
            String falseString = new String( pdfSource.readFully(5), "ISO-8859-1" );
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
                //throw new IOException( "Unknown dir object c='" + c +
                //"' peek='" + (char)pdfSource.peek() + "' " + pdfSource );
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
                    pdfSource.unread(badString.getBytes("ISO-8859-1"));
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
        StringBuilder buffer = new StringBuilder( theString.length() );
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
                pdfSource.unread(buffer.toString().getBytes("ISO-8859-1"));
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

        StringBuilder buffer = new StringBuilder( 11 );

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
     * This will tell if the next byte is whitespace or not.  These values are
     * specified in table 1 (page 12) of ISO 32000-1:2008.
     * @param c The character to check against whitespace
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
     * This will read a long from the Stream and throw an {@link IllegalArgumentException} if the long value
     * has more than 10 digits (i.e. : bigger than {@link #OBJECT_NUMBER_THRESHOLD})
     * @return
     * @throws IOException
     */
    protected long readObjectNumber() throws IOException
    {
        long retval = readLong();
        if(retval < 0 || retval >= OBJECT_NUMBER_THRESHOLD) {
            throw new IOException("Object Number '" + retval + "' has more than 10 digits or is negative");
        }
        return retval;
    }
    
    /**
     * This will read a integer from the Stream and throw an {@link IllegalArgumentException} if the integer value
     * has more than the maximum object revision (i.e. : bigger than {@link #GENERATION_NUMBER_THRESHOLD})
     * @return
     * @throws IOException
     */
    protected int readGenerationNumber() throws IOException
    {
        int retval = readInt();
        if(retval < 0 || retval >= GENERATION_NUMBER_THRESHOLD) {
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
            pdfSource.unread(intBuffer.toString().getBytes("ISO-8859-1"));
            throw new IOException( "Error: Expected an integer type, actual='" + intBuffer + "'" );
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
            pdfSource.unread(longBuffer.toString().getBytes("ISO-8859-1"));
            throw new IOException( "Error: Expected a long type, actual='" + longBuffer + "'" );
        }
        return retval;
    }

    /**
     * This method is used to read a token by the {@linkplain #readInt()} method and the {@linkplain #readLong()} method.
     *  
     * @return the token to parse as integer or long by the calling method.
     * @throws IOException throws by the {@link #pdfSource} methods.
     */
    protected final StringBuilder readStringNumber() throws IOException
    {
        int lastByte = 0;
        StringBuilder buffer = new StringBuilder();
        while( (lastByte = pdfSource.read() ) != 32 &&
                lastByte != 10 &&
                lastByte != 13 &&
                lastByte != 60 && //see sourceforge bug 1714707
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

    /**
     * Release all used resources.
     */
    public void clearResources()
    {
    	document = null;
    	if (pdfSource != null)
    	{
    		IOUtils.closeQuietly(pdfSource);
    		pdfSource = null;
    	}
    }
}
