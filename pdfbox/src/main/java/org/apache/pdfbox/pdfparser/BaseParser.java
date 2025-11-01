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

import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.io.RandomAccessRead;

/**
 * This class is used to contain parsing logic that will be used by all parsers.
 *
 * @author Ben Litchfield
 */
public abstract class BaseParser
{
    private static final int MAX_LENGTH_LONG = Long.toString(Long.MAX_VALUE).length();

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
     * Default constructor.
     */
    BaseParser(RandomAccessRead pdfSource)
    {
        this.source = pdfSource;
    }

    /**
     * Skip the upcoming CRLF or LF which are supposed to follow a stream. Trailing spaces are removed as well.
     * 
     * @throws IOException if something went wrong
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
        if (!skipLinebreak(whitespace))
        {
            source.rewind(1);
        }
    }

    /**
     * Skip one line break, such as CR, LF or CRLF.
     * 
     * @return true if a line break was found and removed.
     * 
     * @throws IOException if something went wrong
     */
    protected boolean skipLinebreak() throws IOException
    {
        // a line break is a CR, or LF or CRLF
        if (!skipLinebreak(source.read()))
        {
            source.rewind(1);
            return false;
        }
        return true;
    }

    /**
     * Skip one line break, such as CR, LF or CRLF.
     * 
     * @param linebreak the first character to be checked.
     * 
     * @return true if a line break was found and removed.
     * 
     * @throws IOException if something went wrong
     */
    private boolean skipLinebreak(int linebreak) throws IOException
    {
        // a line break is a CR, or LF or CRLF
        if (isCR(linebreak))
        {
            int next = source.read();
            if (!isLF(next))
            {
                source.rewind(1);
            }
        }
        else if (!isLF(linebreak))
        {
            return false;
        }
        return true;
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
     * This will parse a PDF string.
     *
     * @return The parsed PDF string.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    protected byte[] readLiteralString() throws IOException
    {
        readExpectedChar('(');
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
                if (braces != 0)
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
                switch (next)
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
                    if (braces != 0)
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
                    // this is a break in the line so ignore it and the newline and continue
                    c = source.read();
                    while (isEOL(c) && c != -1)
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
                    octal.append(next);
                    c = source.read();
                    char digit = (char) c;
                    if (digit >= '0' && digit <= '7')
                    {
                        octal.append(digit);
                        c = source.read();
                        digit = (char) c;
                        if (digit >= '0' && digit <= '7')
                        {
                            octal.append(digit);
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
                        character = Integer.parseInt(octal.toString(), 8);
                    }
                    catch (NumberFormatException e)
                    {
                        throw new IOException(
                                "Error: Expected octal character, actual='" + octal + "'", e);
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
        return out.toByteArray();
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
        if (skipSpaces)
        {
            skipSpaces();
        }
        for (char c : expectedString)
        {
            if (source.read() != c)
            {
                throw new IOException("Expected string '" + new String(expectedString)
                        + "' but missed at character '" + c + "' at offset "
                        + source.getPosition());
            }
        }
        if (skipSpaces)
        {
            skipSpaces();
        }
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
    protected static boolean isLF(int c)
    {
        return ASCII_LF == c;
    }

    /**
     * This will tell if the next byte to be read is a carriage return.
     *
     * @param c The character to check against carriage return
     * @return true if the next byte is 0x0D.
     */
    protected static boolean isCR(int c)
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
