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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorName;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.io.RandomAccessReadBuffer;

/**
 * This will parse a PDF byte stream and extract operands and such.
 *
 * @author Ben Litchfield
 */
public class PDFStreamParser extends BaseParser
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDFStreamParser.class);

    private static final int MAX_BIN_CHAR_TEST_LENGTH = 10;
    private final byte[] binCharTestArr = new byte[MAX_BIN_CHAR_TEST_LENGTH];
    
    /**
     * Constructor.
     *
     * @param pdContentstream The content stream to parse.
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFStreamParser(PDContentStream pdContentstream) throws IOException
    {
        super(pdContentstream.getContentsForRandomAccess());
    }

    /**
     * Constructor.
     *
     * @param bytes the bytes to parse.
     * @throws IOException If there is an error initializing the stream.
     */
    public PDFStreamParser(byte[] bytes) throws IOException
    {
        super(new RandomAccessReadBuffer(bytes));
    }

    /**
     * This will parse all the tokens in the stream. This will close the stream when it is finished parsing.
     *
     * @return All of the tokens in the stream.
     * @throws IOException If there is an error while parsing the stream.
     */
    public List<Object> parse() throws IOException
    {
        List<Object> streamObjects = new ArrayList<>(100);
        Object token;
        while( (token = parseNextToken()) != null )
        {
            streamObjects.add( token );
        }
        return streamObjects;
    }

    /**
     * This will parse the next token in the stream.
     *
     * @return The next token in the stream or null if there are no more tokens in the stream.
     *
     * @throws IOException If an io error occurs while parsing the stream.
     */
    public Object parseNextToken() throws IOException
    {
        skipSpaces();
        if (source.isEOF())
        {
            source.close();
            return null;
        }
        char c = (char) source.peek();
        switch (c)
        {
            case '<':
                // pull off first left bracket
                source.read();

                // check for second left bracket
                c = (char) source.peek();

                // put back first bracket
                source.rewind(1);

                if (c == '<')
                {
                    return parseCOSDictionary(true);
                }
                else
                {
                    return parseCOSString();
                }
            case '[':
                // array
                return parseCOSArray();
            case '(':
                // string
                return parseCOSString();
            case '/':
                // name
                return parseCOSName();
            case 'n':   
                // null
                String nullString = readString();
                if( nullString.equals( "null") )
                {
                    return COSNull.NULL;
                }
                else
                {
                    return Operator.getOperator(nullString);
                }
            case 't':
            case 'f':
                String next = readString();
                if( next.equals( "true" ) )
                {
                    return COSBoolean.TRUE;
                }
                else if( next.equals( "false" ) )
                {
                    return COSBoolean.FALSE;
                }
                else
                {
                    return Operator.getOperator(next);
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
            case '-':
            case '+':
            case '.':
                /* We will be filling buf with the rest of the number.  Only
                 * allow 1 "." and "-" and "+" at start of number. */
                StringBuilder buf = new StringBuilder();
                buf.append( c );
                source.read();
                
                // Ignore double negative (this is consistent with Adobe Reader)
                if (c == '-' && source.peek() == c)
                {
                    source.read();
                }

                boolean dotNotRead = c != '.';
                while (Character.isDigit(c = (char) source.peek()) || dotNotRead && c == '.'
                        || c == '-')
                {
                    if (c != '-')
                    {
                        // PDFBOX-4064: ignore "-" in the middle of a number
                        buf.append(c);
                    }
                    source.read();

                    if (dotNotRead && c == '.')
                    {
                        dotNotRead = false;
                    }
                }
                return COSNumber.get(buf.toString());
            case 'B':
                String nextOperator = readString();
                Operator beginImageOP = Operator.getOperator(nextOperator);
                if (nextOperator.equals(OperatorName.BEGIN_INLINE_IMAGE))
                {
                    COSDictionary imageParams = new COSDictionary();
                    beginImageOP.setImageParameters( imageParams );
                    Object nextToken = null;
                    while( (nextToken = parseNextToken()) instanceof COSName )
                    {
                        Object value = parseNextToken();
                        if (!(value instanceof COSBase))
                        {
                            LOG.warn("Unexpected token in inline image dictionary at offset " +
                                    source.getPosition());
                            break;
                        }
                        imageParams.setItem( (COSName)nextToken, (COSBase)value );
                    }
                    //final token will be the image data, maybe??
                    if (nextToken instanceof Operator)
                    {
                        Operator imageData = (Operator) nextToken;
                        if (imageData.getImageData() == null || imageData.getImageData().length == 0)
                        {
                            LOG.warn("empty inline image at stream offset " + source.getPosition());
                        }
                        beginImageOP.setImageData(imageData.getImageData());
                    }
                }
                return beginImageOP;
            case 'I':
                //Special case for ID operator
                String id = Character.toString((char) source.read()) + (char) source.read();
                if (!id.equals(OperatorName.BEGIN_INLINE_IMAGE_DATA))
                {
                    throw new IOException( "Error: Expected operator 'ID' actual='" + id +
                            "' at stream offset " + source.getPosition());
                }
                ByteArrayOutputStream imageData = new ByteArrayOutputStream();
                if( isWhitespace() )
                {
                    //pull off the whitespace character
                    source.read();
                }
                int lastByte = source.read();
                int currentByte = source.read();
                // PDF spec is kinda unclear about this. Should a whitespace
                // always appear before EI? Not sure, so that we just read
                // until EI<whitespace>.
                // Be aware not all kind of whitespaces are allowed here. see PDFBOX-1561
                while( !(lastByte == 'E' &&
                         currentByte == 'I' &&
                         hasNextSpaceOrReturn() &&
                    hasNoFollowingBinData()) &&
                    !isEOF())
                {
                    imageData.write( lastByte );
                    lastByte = currentByte;
                    currentByte = source.read();
                }
                // the EI operator isn't unread, as it won't be processed anyway
                Operator beginImageDataOP = Operator
                        .getOperator(OperatorName.BEGIN_INLINE_IMAGE_DATA);
                // save the image data to the operator, so that it can be accessed later
                beginImageDataOP.setImageData(imageData.toByteArray());
                return beginImageDataOP;
            case ']':
                // some ']' around without its previous '['
                // this means a PDF is somewhat corrupt but we will continue to parse.
                source.read();
                
                // must be a better solution than null...
                return COSNull.NULL;
            default:
                // we must be an operator
                String operator = readOperator().trim();
                if (operator.length() > 0)
                {
                    return Operator.getOperator(operator);
                }
        }
        return null;
    }

    /**
     * Looks up an amount of bytes if they contain only ASCII characters (no
     * control sequences etc.), and that these ASCII characters begin with a
     * sequence of 1-3 non-blank characters between blanks
     *
     * @return <code>true</code> if next bytes are probably printable ASCII
     * characters starting with a PDF operator, otherwise <code>false</code>
     */
    private boolean hasNoFollowingBinData() throws IOException
    {
        // as suggested in PDFBOX-1164
        final int readBytes = source.read(binCharTestArr, 0, MAX_BIN_CHAR_TEST_LENGTH);
        boolean noBinData = true;
        int startOpIdx = -1;
        int endOpIdx = -1;
        
        if (readBytes > 0)
        {
            for (int bIdx = 0; bIdx < readBytes; bIdx++)
            {
                final byte b = binCharTestArr[bIdx];
                if (b != 0 && b < 0x09 || b > 0x0a && b < 0x20 && b != 0x0d)
                {
                    // control character or > 0x7f -> we have binary data
                    noBinData = false;
                    break;
                }
                // find the start of a PDF operator
                if (startOpIdx == -1 && !(b == 0 || b == 9 || b == 0x20 || b == 0x0a || b == 0x0d))
                {
                    startOpIdx = bIdx;
                }
                else if (startOpIdx != -1 && endOpIdx == -1 &&
                         (b == 0 || b == 9 || b == 0x20 || b == 0x0a || b == 0x0d))
                {
                    endOpIdx = bIdx;
                }
            }

            // PDFBOX-3742: just assuming that 1-3 non blanks is a PDF operator isn't enough
            if (endOpIdx != -1 && startOpIdx != -1)
            {
                // usually, the operator here is Q, sometimes EMC (PDFBOX-2376), S (PDFBOX-3784).
                String s = new String(binCharTestArr, startOpIdx, endOpIdx - startOpIdx);
                if (!"Q".equals(s) && !"EMC".equals(s) && !"S".equals(s))
                {
                    noBinData = false;
                }
            }

            // only if not close to eof
            if (readBytes == MAX_BIN_CHAR_TEST_LENGTH) 
            {
                // a PDF operator is 1-3 bytes long
                if (startOpIdx != -1 && endOpIdx == -1)
                {
                    endOpIdx = MAX_BIN_CHAR_TEST_LENGTH;
                }
                if (endOpIdx != -1 && startOpIdx != -1 && endOpIdx - startOpIdx > 3)
                {
                    noBinData = false;
                }
            }
            source.rewind(readBytes);
        }
        if (!noBinData)
        {
            LOG.warn("ignoring 'EI' assumed to be in the middle of inline image at stream offset " + 
                    source.getPosition());
        }
        return noBinData;
    }

    /**
     * This will read an operator from the stream.
     *
     * @return The operator that was read from the stream.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    private String readOperator() throws IOException
    {
        skipSpaces();

        //average string size is around 2 and the normal string buffer size is
        //about 16 so lets save some space.
        StringBuilder buffer = new StringBuilder(4);
        int nextChar = source.peek();
        while(
            nextChar != -1 && // EOF
            !isWhitespace(nextChar) &&
            !isClosing(nextChar) &&
            nextChar != '[' &&
            nextChar != '<' &&
            nextChar != '(' &&
            nextChar != '/' &&
            (nextChar < '0' ||
             nextChar > '9' ) )
        {
            char currentChar = (char) source.read();
            nextChar = source.peek();
            buffer.append( currentChar );
            // Type3 Glyph description has operators with a number in the name
            if (currentChar == 'd' && (nextChar == '0' || nextChar == '1') ) 
            {
                buffer.append((char) source.read());
                nextChar = source.peek();
            }
        }
        return buffer.toString();
    }
    
    
    private boolean isSpaceOrReturn( int c )
    {
        return c == 10 || c == 13 || c == 32;
    }

    /**
     * Checks if the next char is a space or a return.
     * 
     * @return true if the next char is a space or a return
     * @throws IOException if something went wrong
     */
    private boolean hasNextSpaceOrReturn() throws IOException
    {
        return isSpaceOrReturn(source.peek());
    }
}
