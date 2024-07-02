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
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
import org.apache.pdfbox.io.RandomAccessRead;

/**
 * This class is used to contain parsing logic that will be used by all parsers.
 *
 * @author Ben Litchfield
 */
public abstract class BaseParser
{
    /**
     * Log instance.
     */
    private static final Logger LOG = LogManager.getLogger(BaseParser.class);

    private static final long OBJECT_NUMBER_THRESHOLD = 10000000000L;

    private static final long GENERATION_NUMBER_THRESHOLD = 65535;

    private static final int MAX_LENGTH_LONG = Long.toString(Long.MAX_VALUE).length();

    private static final Charset ALTERNATIVE_CHARSET;

    private final Map<Long, COSObjectKey> keyCache = new HashMap<>();

    static
    {
        Charset cs;
        String charsetName = "Windows-1252";
        try
        {
            cs = Charset.forName(charsetName);
        }
        catch (IllegalArgumentException | UnsupportedOperationException e)
        {
            cs = StandardCharsets.ISO_8859_1;
            LOG.warn(() -> "Charset is not supported: " + charsetName + ", falling back to " +
                    StandardCharsets.ISO_8859_1.name(), e);
        }
        ALTERNATIVE_CHARSET = cs;
    }

    // CharSetDecoders are not threadsafe so not static
    private final CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);

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
    private static final char[] TRUE = { 't', 'r', 'u', 'e' };
    /**
     * This is a string constant that will be used for comparisons.
     */
    private static final char[] FALSE = { 'f', 'a', 'l', 's', 'e' };
    /**
     * This is a string constant that will be used for comparisons.
     */
    private static final char[] NULL = { 'n', 'u', 'l', 'l' };

    /**
     * ASCII code for Null.
     */
    private static final byte ASCII_NULL = 0;
    /**
     * ASCII code for horizontal tab.
     */
    private static final byte ASCII_TAB = 9;
    /**
     * ASCII code for line feed.
     */
    private static final byte ASCII_LF = 10;
    /**
     * ASCII code for form feed.
     */
    private static final byte ASCII_FF = 12;
    /**
     * ASCII code for carriage return.
     */
    private static final byte ASCII_CR = 13;
    private static final byte ASCII_ZERO = 48;
    private static final byte ASCII_NINE = 57;
    private static final byte ASCII_SPACE = 32;
    
    /**
     * This is the stream that will be read from.
     */
    protected final RandomAccessRead source;

    /**
     * This is the document that will be parsed.
     */
    protected COSDocument document;

    /**
     * Default constructor.
     */
    BaseParser(RandomAccessRead pdfSource)
    {
        this.source = pdfSource;
    }

    private static boolean isHexDigit(char ch)
    {
        return isDigit(ch) ||
        (ch >= 'a' && ch <= 'f') ||
        (ch >= 'A' && ch <= 'F');
    }

    /**
     * Returns the object key for the given combination of object and generation number. The object key from the cross
     * reference table/stream will be reused if available. Otherwise a newly created object will be returned.
     * 
     * @param num the given object number
     * @param gen the given generation number
     * 
     * @return the COS object key
     */
    protected COSObjectKey getObjectKey(long num, int gen)
    {
        if (document == null || document.getXrefTable().isEmpty())
        {
            return new COSObjectKey(num, gen);
        }
        // use a cache to get the COSObjectKey as iterating over the xref-table-map gets slow for big pdfs
        // in the long run we have to overhaul the object pool or even better remove it
        Map<COSObjectKey, Long> xrefTable = document.getXrefTable();
        if (xrefTable.size() > keyCache.size())
        {
            for (COSObjectKey key : xrefTable.keySet())
            {
                keyCache.putIfAbsent(key.getInternalHash(), key);
            }
        }
        long internalHashCode = COSObjectKey.computeInternalHash(num, gen);
        COSObjectKey foundKey = keyCache.get(internalHashCode);
        return foundKey != null ? foundKey : new COSObjectKey(num, gen);
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
        long numOffset = source.getPosition();
        COSBase value = parseDirObject();
        skipSpaces();
        // proceed if the given object is a number and the following is a number as well
        if (!(value instanceof COSNumber) || !isDigit())
        {
            return value;
        }
        // read the remaining information of the object number
        long genOffset = source.getPosition();
        COSBase generationNumber = parseDirObject();
        skipSpaces();
        readExpectedChar('R');
        if (!(value instanceof COSInteger))
        {
            LOG.error("expected number, actual={} at offset {}", value, numOffset);
            return COSNull.NULL;
        }
        if (!(generationNumber instanceof COSInteger))
        {
            LOG.error("expected number, actual={} at offset {}", generationNumber, genOffset);
            return COSNull.NULL;
        }
        long objNumber = ((COSInteger) value).longValue();
        if (objNumber <= 0)
        {
            LOG.warn("invalid object number value ={} at offset {}", objNumber, numOffset);
            return COSNull.NULL;
        }
        int genNumber = ((COSInteger) generationNumber).intValue();
        if (genNumber < 0)
        {
            LOG.error("invalid generation number value ={} at offset {}", genNumber, numOffset);
            return COSNull.NULL;
        }
        // dereference the object
        return getObjectFromPool(getObjectKey(objNumber, genNumber));
    }

    private COSBase getObjectFromPool(COSObjectKey key) throws IOException
    {
        if (document == null)
        {
            throw new IOException("object reference " + key + " at offset " + source.getPosition()
                    + " in content stream");
        }
        return document.getObjectFromPool(key);
    }

    /**
     * This will parse a PDF dictionary.
     *
     * @param isDirect indicates whether the dictionary to be read is a direct object
     * @return The parsed dictionary, never null.
     *
     * @throws IOException If there is an error reading the stream.
     */
    protected COSDictionary parseCOSDictionary(boolean isDirect) throws IOException
    {
        readExpectedChar('<');
        readExpectedChar('<');
        skipSpaces();
        COSDictionary obj = new COSDictionary();
        obj.setDirect(isDirect);
        while (true)
        {
            skipSpaces();
            char c = (char) source.peek();
            if (c == '>')
            {
                break;
            }
            else if (c == '/')
            {
                // something went wrong, most likely the dictionary is corrupted
                // stop immediately and return everything read so far
                if (!parseCOSDictionaryNameValuePair(obj))
                {
                    return obj;
                }
            }
            else
            {
                // invalid dictionary, we were expecting a /Name, read until the end or until we can recover
                LOG.warn("Invalid dictionary, found: '{}' but expected: '/' at offset {}", c,
                        source.getPosition());
                if (readUntilEndOfCOSDictionary())
                {
                    // we couldn't recover
                    return obj;
                }
            }
        }
        try
        {
            readExpectedChar('>');
            readExpectedChar('>');
        }
        catch (IOException exception)
        {
            LOG.warn("Invalid dictionary, can't find end of dictionary at offset {}",
                    source.getPosition());
        }
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
        int c = source.read();
        while (c != -1 && c != '/' && c != '>')
        {
            // in addition to stopping when we find / or >, we also want
            // to stop when we find endstream or endobj.
            if (c == E)
            {
                c = source.read();
                if (c == N)
                {
                    c = source.read();
                    if (c == D)
                    {
                        c = source.read();
                        boolean isStream = c == S && source.read() == T && source.read() == R
                                && source.read() == E && source.read() == A && source.read() == M;
                        boolean isObj = !isStream && c == O && source.read() == B
                                && source.read() == J;
                        if (isStream || isObj)
                        {
                            // we're done reading this object!
                            return true;
                        }
                    }
                }
            }
            c = source.read();
        }
        if (c == -1)
        {
            return true;
        }
        source.rewind(1);
        return false;
    }

    private boolean parseCOSDictionaryNameValuePair(COSDictionary obj) throws IOException
    {
        COSName key = parseCOSName();
        if (key == null || key.getName().isEmpty())
        {
            LOG.warn("Empty COSName at offset {}", source.getPosition());
        }
        COSBase value = parseCOSDictionaryValue();
        skipSpaces();
        if (value == null)
        {
            LOG.warn("Bad dictionary declaration at offset {}", source.getPosition());
            return false;
        }
        else if (value instanceof COSInteger && !((COSInteger) value).isValid())
        {
            LOG.warn("Skipped out of range number value at offset {}", source.getPosition());
        }
        else
        {
            // label this item as direct, to avoid signature problems.
            value.setDirect(true);
            obj.setItem(key, value);
        }
        return true;
    }

    /**
     * Skip the upcoming CRLF or LF which are supposed to follow a stream.
     * 
     * @throws IOException
     */
    protected void skipWhiteSpaces() throws IOException
    {
        //PDF Ref 3.2.7 A stream must be followed by either
        //a CRLF or LF but nothing else.
        int whitespace = source.read();
        //see brother_scan_cover.pdf, it adds whitespaces
        //after the stream but before the start of the
        //data, so just read those first
        while (isSpace(whitespace))
        {
            whitespace = source.read();
        }

        if (isCR(whitespace))
        {
            whitespace = source.read();
            if (!isLF(whitespace))
            {
                source.rewind(1);
                //The spec says this is invalid but it happens in the real
                //world so we must support it.
            }
        }
        else if (!isLF(whitespace))
        {
            //we are in an error.
            //but again we will do a lenient parsing and just assume that everything
            //is fine
            source.rewind(1);
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
     * @param bracesParameter the number of braces currently open.
     *
     * @return the corrected value of the brace counter
     * @throws IOException
     */
    private int checkForEndOfString(final int bracesParameter) throws IOException
    {
        if (bracesParameter == 0)
        {
            return 0;
        }
        // Check the next 3 bytes if available
        byte[] nextThreeBytes = new byte[3];
        int amountRead = source.read(nextThreeBytes);
        if (amountRead > 0)
        {
            source.rewind(amountRead);
        }
        if (amountRead < 3)
        {
            return bracesParameter;
        }
        // The following cases are valid indicators for the end of the string
        // 1. Next line contains another COSObject: CR + LF + '/'
        // 2. COSDictionary ends in the next line: CR + LF + '>'
        // 3. Next line contains another COSObject: LF + '/'
        // 4. COSDictionary ends in the next line: LF + '>'
        // 5. Next line contains another COSObject: CR + '/'
        // 6. COSDictionary ends in the next line: CR + '>'
        if (((isCR(nextThreeBytes[0]) || isLF(nextThreeBytes[0]))
                && (nextThreeBytes[1] == '/' || nextThreeBytes[1] == '>')) //
                || //
                (isCR(nextThreeBytes[0]) && isLF(nextThreeBytes[1])
                        && (nextThreeBytes[2] == '/' || nextThreeBytes[2] == '>')) //
        )
        {
            return 0;
        }
        return bracesParameter;
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
        char nextChar = (char) source.read();
        if (nextChar == '<')
        {
            return parseCOSHexString();
        }
        else if (nextChar != '(')
        {
            throw new IOException( "parseCOSString string should start with '(' or '<' and not '" +
                    nextChar + "' at offset " + source.getPosition());
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // This is the number of braces read
        int braces = 1;
        int c = source.read();
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
                char next = (char) source.read();
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
                        c = source.read();
                        while( isEOL(c) && c != -1)
                        {
                            c = source.read();
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
                        StringBuilder octal = new StringBuilder();
                        octal.append( next );
                        c = source.read();
                        char digit = (char)c;
                        if( digit >= '0' && digit <= '7' )
                        {
                            octal.append( digit );
                            c = source.read();
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
                    default:
                        // dropping the backslash
                        // see 7.3.4.2 Literal Strings for further information
                        out.write(next);
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
                c = source.read();
            }
        }
        if (c != -1)
        {
            source.rewind(1);
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
            int c = source.read();
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
                    c = source.read();
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
        long startPosition = source.getPosition();
        readExpectedChar('[');
        COSArray po = new COSArray();
        COSBase pbo;
        skipSpaces();
        int i;
        while (((i = source.peek()) > 0) && ((char) i != ']'))
        {
            pbo = parseDirObject();
            if( pbo instanceof COSObject )
            {
                // the current empty COSObject is replaced with the correct one
                pbo = null;
                // We have to check if the expected values are there or not PDFBOX-385
                if (po.size() > 1 && po.get(po.size() - 1) instanceof COSInteger)
                {
                    COSInteger genNumber = (COSInteger)po.remove( po.size() -1 );
                    if (po.size() > 0 && po.get(po.size() - 1) instanceof COSInteger)
                    {
                        COSInteger number = (COSInteger)po.remove( po.size() -1 );
                        if (number.longValue() >= 0 && genNumber.intValue() >= 0)
                        {
                            COSObjectKey key = getObjectKey(number.longValue(),
                                    genNumber.intValue());
                            pbo = getObjectFromPool(key);
                        }
                        else
                        {
                            LOG.warn("Invalid value(s) for an object key {} {}", number.longValue(),
                                    genNumber.intValue());
                        }
                    }
                }
            }
            // something went wrong
            if (pbo == null)
            {
                //it could be a bad object in the array which is just skipped
                LOG.warn("Corrupt array element at offset {}, start offset: {}",
                        source.getPosition(), startPosition);
                String isThisTheEnd = readString();
                // return immediately if a corrupt element is followed by another array
                // to avoid a possible infinite recursion as most likely the whole array is corrupted
                if (isThisTheEnd.isEmpty() && source.peek() == '[')
                {
                    return po;
                }
                source.rewind(isThisTheEnd.getBytes(StandardCharsets.ISO_8859_1).length);
                // This could also be an "endobj" or "endstream" which means we can assume that
                // the array has ended.
                if(ENDOBJ_STRING.equals(isThisTheEnd) || ENDSTREAM_STRING.equals(isThisTheEnd))
                {
                    return po;
                }
            }
            else
            {
                po.add(pbo);
            }
            skipSpaces();
        }
        // read ']'
        source.read();
        skipSpaces();
        return po;
    }

    /**
     * Determine if a character terminates a PDF name.
     *
     * @param ch The character
     * @return true if the character terminates a PDF name, otherwise false.
     */
    protected static boolean isEndOfName(int ch)
    {
        switch (ch)
        {
        case ASCII_SPACE:
        case ASCII_CR:
        case ASCII_LF:
        case ASCII_TAB:
        case '>':
        case '<':
        case '[':
        case '/':
        case ']':
        case ')':
        case '(':
        case ASCII_NULL:
        case '\f':
        case '%':
        case -1:
            return true;
        default:
            return false;
        }
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
        int c = source.read();
        while (!isEndOfName(c))
        {
            final int ch = c;
            if (ch == '#')
            {
                int ch1 = source.read();
                int ch2 = source.read();
                // Prior to PDF v1.2, the # was not a special character.  Also,
                // it has been observed that various PDF tools do not follow the
                // spec with respect to the # escape, even though they report
                // PDF versions of 1.2 or later.  The solution here is that we
                // interpret the # as an escape only when it is followed by two
                // valid hex digits.
                if (isHexDigit((char)ch1) && isHexDigit((char)ch2))
                {
                    String hex = Character.toString((char) ch1) + (char) ch2;
                    try
                    {
                        buffer.write(Integer.parseInt(hex, 16));
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IOException("Error: expected hex digit, actual='" + hex + "'", e);
                    }
                    c = source.read();
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
                    source.rewind(1);
                    c = ch1;
                    buffer.write(ch);
                }
            }
            else
            {
                buffer.write(ch);
                c = source.read();
            }
        }
        if (c != -1)
        {
            source.rewind(1);
        }

        return COSName.getPDFName(decodeBuffer(buffer));
    }

    /**
     * Tries to decode the buffer content to an UTF-8 String. If that fails, tries the alternative Encoding.
     * 
     * @param buffer the {@link ByteArrayOutputStream} containing the bytes to decode
     * @return the decoded String
     */
    private String decodeBuffer(ByteArrayOutputStream buffer)
    {
        try
        {
            return utf8Decoder.decode(ByteBuffer.wrap(buffer.toByteArray())).toString();
        }
        catch (CharacterCodingException e)
        {
            // some malformed PDFs don't use UTF-8 see PDFBOX-3347
            LOG.debug(() -> "Buffer could not be decoded using StandardCharsets.UTF_8 - trying " + 
                    ALTERNATIVE_CHARSET.name(), e);
            return buffer.toString(ALTERNATIVE_CHARSET);
        }
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
        skipSpaces();
        char c = (char) source.peek();
        switch(c)
        {
        case '<':
            // pull off first left bracket
            source.read();
            // check for second left bracket
            c = (char) source.peek();
            source.rewind(1);
            return c == '<' ? parseCOSDictionary(true) : parseCOSString();
        case '[':
            // array
            return parseCOSArray();
        case '(':
            return parseCOSString();
        case '/':   
            // name
            return parseCOSName();
        case 'n':   
            // null
            readExpectedString(NULL, false);
            return COSNull.NULL;
        case 't':
            readExpectedString(TRUE, false);
            return COSBoolean.TRUE;
        case 'f':
            readExpectedString(FALSE, false);
            return COSBoolean.FALSE;
        case 'R':
            source.read();
            return new COSObject(null);
        case (char)-1:
            return null;
        default:
            if (isDigit(c) || c == '-' || c == '+' || c == '.')
            {
                return parseCOSNumber();
            }
            // This is not suppose to happen, but we will allow for it
            // so we are more compatible with POS writers that don't
            // follow the spec
            long startOffset = source.getPosition();
            String badString = readString();
            if (badString.isEmpty())
            {
                int peek = source.peek();
                // we can end up in an infinite loop otherwise
                throw new IOException("Unknown dir object c='" + c + "' cInt=" + (int) c + " peek='"
                        + (char) peek + "' peekInt=" + peek + " at offset " + source.getPosition()
                        + " (start offset: " + startOffset + ")");
            }

            // if it's an endstream/endobj, we want to put it back so the caller will see it
            if (ENDOBJ_STRING.equals(badString) || ENDSTREAM_STRING.equals(badString))
            {
                source.rewind(badString.getBytes(StandardCharsets.ISO_8859_1).length);
            }
            else
            {
                LOG.warn("Skipped unexpected dir object = '{}' at offset {} (start offset: {})",
                        badString, source.getPosition(), startOffset);
                return this instanceof PDFStreamParser ? null : COSNull.NULL;
            }
        }
        return null;
    }

    private COSNumber parseCOSNumber() throws IOException
    {
        StringBuilder buf = new StringBuilder();
        int ic = source.read();
        char c = (char) ic;
        while (Character.isDigit(c) || c == '-' || c == '+' || c == '.' || c == 'E' || c == 'e')
        {
            buf.append(c);
            ic = source.read();
            c = (char) ic;
        }
        if (ic != -1)
        {
            source.rewind(1);
        }
        return COSNumber.get(buf.toString());
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
        int c = source.read();
        while (!isEndOfName(c))
        {
            buffer.append( (char)c );
            c = source.read();
        }
        if (c != -1)
        {
            source.rewind(1);
        }
        return buffer.toString();
    }
    
    /**
     * Reads given pattern from {@link #source}. Skipping whitespace at start and end if wanted.
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
            if (source.read() != c)
            {
                throw new IOException("Expected string '" + new String(expectedString)
                        + "' but missed at character '" + c + "' at offset "
                        + source.getPosition());
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
        char c = (char) source.read();
        if (c != ec)
        {
            throw new IOException(
                    "expected='" + ec + "' actual='" + c + "' at offset " + source.getPosition());
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

        int c = source.read();

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
            c = source.read();
        }
        if (c != -1)
        {
            source.rewind(1);
        }
        return buffer.toString();
    }

    /**
     * This will tell if the next character is a closing brace( close of PDF array ).
     *
     * @param c The character to check against end of line
     * @return true if the next byte is ']', false otherwise.
     */
    protected static boolean isClosing(int c)
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
        if (source.isEOF())
        {
            throw new IOException( "Error: End-of-File, expected line at offset " +
                    source.getPosition());
        }

        StringBuilder buffer = new StringBuilder( 11 );

        int c;
        while ((c = source.read()) != -1)
        {
            // CR and LF are valid EOLs
            if (isEOL(c))
            {
                break;
            }
            buffer.append( (char)c );
        }
        // CR+LF is also a valid EOL 
        if (isCR(c) && isLF(source.peek()))
        {
            source.read();
        }
        return buffer.toString();
    }

    /**
     * This will tell if the end of the data is reached.
     * 
     * @return true if the end of the data is reached.
     * @throws IOException If there is an error reading from the stream.
     */
    protected boolean isEOF() throws IOException
    {
        return source.isEOF();
    }

    /**
     * This will tell if the next byte to be read is an end of line byte.
     *
     * @param c The character to check against end of line
     * @return true if the next byte is 0x0A or 0x0D.
     */
    protected static boolean isEOL(int c)
    {
        return isLF(c) || isCR(c);
    }

    /**
     * This will tell if the next byte to be read is a line feed.
     *
     * @param c The character to check against line feed
     * @return true if the next byte is 0x0A.
     */
    private static boolean isLF(int c)
    {
        return ASCII_LF == c;
    }

    /**
     * This will tell if the next byte to be read is a carriage return.
     *
     * @param c The character to check against carriage return
     * @return true if the next byte is 0x0D.
     */
    private static boolean isCR(int c)
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
        return isWhitespace(source.peek());
    }

    /**
     * This will tell if a character is whitespace or not.  These values are
     * specified in table 1 (page 12) of ISO 32000-1:2008.
     * @param c The character to check against whitespace
     * @return true if the character is a whitespace character.
     */
    protected static boolean isWhitespace( int c )
    {
        switch (c)
        {
        case ASCII_NULL:
        case ASCII_TAB:
        case ASCII_FF:
        case ASCII_LF:
        case ASCII_CR:
        case ASCII_SPACE:
            return true;
        default:
            return false;
        }
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
        return isSpace(source.peek());
    }
    
    /**
     * This will tell if the given value is a space or not.
     * 
     * @param c The character to check against space
     * @return true if the next byte in the stream is a space character.
     */
    private static boolean isSpace(int c)
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
        return isDigit(source.peek());
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
        int c = source.read();
        // 37 is the % character, a comment
        while( isWhitespace(c) || c == 37)
        {
            if ( c == 37 )
            {
                // skip past the comment section
                c = source.read();
                while(!isEOL(c) && c != -1)
                {
                    c = source.read();
                }
            }
            else
            {
                c = source.read();
            }
        }
        if (c != -1)
        {
            source.rewind(1);
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
            throw new IOException(
                    "Generation Number '" + retval + "' has more than 5 digits or is negative");
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
            source.rewind(intBuffer.toString().getBytes(StandardCharsets.ISO_8859_1).length);
            throw new IOException("Error: Expected an integer type at offset " +
                    source.getPosition() +
                                  ", instead got '" + intBuffer + "'", e);
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
            source.rewind(longBuffer.toString().getBytes(StandardCharsets.ISO_8859_1).length);
            throw new IOException( "Error: Expected a long type at offset "
                    + source.getPosition() + ", instead got '" + longBuffer + "'", e);
        }
        return retval;
    }

    /**
     * This method is used to read a token by the {@linkplain #readInt()} and the {@linkplain #readLong()} method. Valid
     * delimiters are any non digit values.
     *
     * @return the token to parse as integer or long by the calling method.
     * @throws IOException throws by the {@link #source} methods.
     */
    protected final StringBuilder readStringNumber() throws IOException
    {
        int lastByte;
        StringBuilder buffer = new StringBuilder();
        while (isDigit(lastByte = source.read()))
        {
            buffer.append( (char)lastByte );
            if (buffer.length() > MAX_LENGTH_LONG)
            {
                throw new IOException("Number '" + buffer + 
                        "' is getting too long, stop reading at offset " + source.getPosition());
            }
        }
        if( lastByte != -1 )
        {
            source.rewind(1);
        }
        return buffer;
    }
}
