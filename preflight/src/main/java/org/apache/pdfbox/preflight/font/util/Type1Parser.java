/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.pdfbox.preflight.font.util;

import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_MAC;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_PDFDOC;
import static org.apache.pdfbox.preflight.PreflightConstants.FONT_DICTIONARY_VALUE_ENCODING_WIN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.fontbox.cff.IndexData;
import org.apache.fontbox.cff.Type1CharStringParser;
import org.apache.fontbox.cff.Type1FontUtil;
import org.apache.log4j.Logger;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.MacRomanEncoding;
import org.apache.pdfbox.encoding.PdfDocEncoding;
import org.apache.pdfbox.encoding.StandardEncoding;
import org.apache.pdfbox.encoding.WinAnsiEncoding;

public final class Type1Parser
{

    public static final Logger LOGGER = Logger.getLogger(Type1Parser.class);

    protected static final char NAME_START = '/';
    protected static final String NOTDEF = NAME_START + ".notdef";
    protected static final int DEFAULT_LEN_IV = 4;

    private static final String PS_STANDARD_ENCODING = "StandardEncoding";
    private static final String PS_ISOLATIN_ENCODING = "ISOLatin1Encoding";

    private static final String TOKEN_ENCODING = "US-ASCII";

    /**
     * The PostScript font stream.
     */
    private PeekInputStream fontProgram = null;
    /**
     * The length in bytes of the clear-text portion of the Type1 font program.
     */
    private int clearTextSize = 0;
    /**
     * The length in bytes of the eexec encrypted portion of the type1 font program.
     */
    private int eexecSize = 0;

    /**
     * This counter is used to know how many byte have been read. It is used to read the clear part of the font. this
     * computer is updated during the parsing of the encoding part too but it is not used.
     */
    private int numberOfReadBytes = 0;

    /**
     * Object which contains information coming from the parsing.
     */
    private Type1 type1Font = null;

    private Type1Parser(InputStream type1, int length1, int length2, Encoding enc) throws IOException
    {
        super();
        this.fontProgram = new PeekInputStream(type1);
        this.clearTextSize = length1;
        this.eexecSize = length2;
        // ---- Instantiate the Encoding Map
        if (enc != null)
        {
            this.type1Font = new Type1(enc);
        }
        else
        {
            this.type1Font = new Type1(new StandardEncoding());
        }
        this.type1Font.addCidWithLabel(-1, NOTDEF);
    }

    /**
     * 
     * @param fontProgram
     *            the stream of the font program extracted from the PDF file.
     * @param clearTextLength
     *            the length in bytes of the clear part of the font program.
     * @param eexecLength
     *            the length in bytes of the encoded part.
     * @return
     * @throws IOException
     */
    public static Type1Parser createParser(InputStream fontProgram, int clearTextLength, int eexecLength)
            throws IOException
    {

        Encoding encoding = getEncodingObject("");
        return createParserWithEncodingObject(fontProgram, clearTextLength, eexecLength, encoding);
    }

    /**
     * 
     * @param fontProgram
     *            the stream of the font program extracted from the PDF file.
     * @param clearTextLength
     *            the length in bytes of the clear part of the font program.
     * @param eexecLength
     *            the length in bytes of the encoded part.
     * @param encodingName
     *            The name of encoding which is used by this font program.
     * @return
     * @throws IOException
     */
    public static Type1Parser createParserWithEncodingName(InputStream fontProgram, int clearTextLength,
            int eexecLength, String encodingName) throws IOException
    {

        Encoding encoding = getEncodingObject(encodingName);
        return createParserWithEncodingObject(fontProgram, clearTextLength, eexecLength, encoding);
    }

    private static Encoding getEncodingObject(String encodingName)
    {
        Encoding encoding = new StandardEncoding();
        if (FONT_DICTIONARY_VALUE_ENCODING_MAC.equals(encodingName))
        {
            encoding = new MacRomanEncoding();
        }
        else if (FONT_DICTIONARY_VALUE_ENCODING_MAC_EXP.equals(encodingName))
        {
            encoding = new MacRomanEncoding();
        }
        else if (FONT_DICTIONARY_VALUE_ENCODING_WIN.equals(encodingName))
        {
            encoding = new WinAnsiEncoding();
        }
        else if (FONT_DICTIONARY_VALUE_ENCODING_PDFDOC.equals(encodingName))
        {
            encoding = new PdfDocEncoding();
        }
        return encoding;
    }

    /**
     * 
     * @param fontProgram
     *            the stream of the font program extracted from the PDF file.
     * @param clearTextLength
     *            the length in bytes of the clear part of the font program.
     * @param eexecLength
     *            the length in bytes of the encoded part.
     * @param encodingName
     *            The encoding object which is used by this font program.
     * @return
     * @throws IOException
     */
    public static Type1Parser createParserWithEncodingObject(InputStream fontProgram, int clearTextLength,
            int eexecLength, Encoding encoding) throws IOException
    {

        return new Type1Parser(fontProgram, clearTextLength, eexecLength, encoding);
    }

    public Type1 parse() throws IOException
    {
        parseClearPartOfFontProgram(this.fontProgram);
        decodeAndParseEExecPart(this.fontProgram);
        return this.type1Font;
    }

    private void parseClearPartOfFontProgram(PeekInputStream stream) throws IOException
    {
        skipComments(stream);
        parseFontInformationUntilEncodingPart(stream);
    }

    private void decodeAndParseEExecPart(PeekInputStream stream) throws IOException
    {
        byte[] eexecPart = readEexec(stream);
        byte[] decodedEExecPart = decodeEexec(eexecPart);
        PeekInputStream eexecStream = new PeekInputStream(new ByteArrayInputStream(decodedEExecPart));
        parseEExecPart(eexecStream);
    }

    private void skipComments(PeekInputStream stream) throws IOException
    {
        int nextChar = stream.peek();
        while (nextChar == '%')
        {
            if (nextChar == -1)
            {
                throw new IOException("Unexpected End Of File during a comment parsing");
            }
            readLine(stream);
            nextChar = stream.peek();
        }
    }

    private void parseFontInformationUntilEncodingPart(PeekInputStream stream) throws IOException
    {
        byte[] token = readToken(stream);
        while (!isEExecKeyWord(token))
        {
            // add here specific operation to memorize useful information
            if (isEncodingKeyWord(token))
            {
                parseEncodingDefinition(stream);
            }
            token = readToken(stream);
        }

        while (!isStartOfEExecReached())
        {
            readNextCharacter(stream);
        }
    }

    private void parseEncodingDefinition(PeekInputStream stream) throws IOException
    {
        byte[] token = readToken(stream);
        String readableToken = new String(token, TOKEN_ENCODING);
        if (PS_ISOLATIN_ENCODING.equals(readableToken))
        {
            this.type1Font.initEncodingWithISOLatin1Encoding();
        }
        else if (PS_STANDARD_ENCODING.equals(readableToken))
        {
            this.type1Font.initEncodingWithStandardEncoding();
        }
        else
        {
            try
            {
                Integer.parseInt(readableToken);
                throwExceptionIfUnexpectedToken("array", readToken(stream));
                readEndSetEncodingValues(stream);
            }
            catch (NumberFormatException e)
            {
                throw new IOException("Invalid encoding : Expected int value before \"array\" "
                        + "key word if the Encoding isn't Standard or ISOLatin");
            }
        }
    }

    private void parseEExecPart(PeekInputStream stream) throws IOException
    {
        int lenIV = DEFAULT_LEN_IV;
        byte[] previousToken = new byte[0];
        while (!isEndOfStream(stream))
        {
            byte[] token = readToken(stream);
            if (isLenIVKeyWord(token))
            {
                // lenIV belong to Private Dictionary.
                // If you create a method to parse PrivateDict, please update this function
                byte[] l = readToken(stream);
                lenIV = Integer.parseInt(new String(l, TOKEN_ENCODING));
            }
            else if (isBeginOfBinaryPart(token))
            {
                try
                {
                    int lengthOfBinaryPart = Integer.parseInt(new String(previousToken, TOKEN_ENCODING));
                    skipSingleBlankSeparator(stream);
                    stream.read(new byte[lengthOfBinaryPart], 0, lengthOfBinaryPart);
                    token = readToken(stream); // read the end of binary part
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Binary part found but previous token wasn't an integer");
                }
            }
            else if (isCharStringKeyWord(token))
            {
                parseCharStringArray(stream, lenIV);
            }
            previousToken = token;
        }
    }

    private void parseCharStringArray(PeekInputStream stream, int lenIV) throws IOException
    {
        int numberOfElements = readNumberOfCharStrings(stream);
        goToBeginOfCharStringElements(stream);

        while (numberOfElements > 0)
        {
            byte[] labelToken = readToken(stream);
            String label = new String(labelToken, TOKEN_ENCODING);

            if (label.equals("end"))
            {
                // TODO thrown exception ? add an error/warning in the PreflightContext ??
                LOGGER.warn("[Type 1] Invalid number of elements in the CharString");
                break;
            }

            byte[] sizeOfCharStringToken = readToken(stream);
            int sizeOfCharString = Integer.parseInt(new String(sizeOfCharStringToken, TOKEN_ENCODING));

            readToken(stream); // skip "RD" or "-|" token
            skipSingleBlankSeparator(stream); // "RD" or "-|" are followed by a space

            byte[] descBinary = new byte[sizeOfCharString];
            stream.read(descBinary, 0, sizeOfCharString);
            byte[] description = Type1FontUtil.charstringDecrypt(descBinary, lenIV);
            Type1CharStringParser t1p = new Type1CharStringParser();
            // TODO provide the local subroutine indexes
            List<Object> operations = t1p.parse(description, new IndexData(0));
            type1Font.addGlyphDescription(label, new GlyphDescription(operations));

            readToken(stream); // skip "ND" or "|-" token
            --numberOfElements;
        }
    }

    private void goToBeginOfCharStringElements(PeekInputStream stream) throws IOException
    {
        byte[] token = new byte[0];
        do
        {
            token = readToken(stream);
        } while (isNotBeginKeyWord(token));
    }

    private boolean isNotBeginKeyWord(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return !"begin".equals(word);
    }

    private boolean isBeginOfBinaryPart(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return ("RD".equals(word) || "-|".equals(word));
    }

    private boolean isLenIVKeyWord(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return "/lenIV".equals(word);
    }

    private boolean isCharStringKeyWord(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return "/CharStrings".equals(word);
    }

    private int readNumberOfCharStrings(PeekInputStream stream) throws IOException
    {
        byte[] token = readToken(stream);
        String word = new String(token, TOKEN_ENCODING);
        try
        {
            return Integer.parseInt(word);
        }
        catch (NumberFormatException e)
        {
            throw new IOException("Number of CharStrings elements is expected.");
        }
    }

    private void throwExceptionIfUnexpectedToken(String expectedValue, byte[] token) throws IOException
    {
        String valueToCheck = new String(token, TOKEN_ENCODING);
        if (!expectedValue.equals(valueToCheck))
        {
            throw new IOException(expectedValue + " was expected but we received " + valueToCheck);
        }
    }

    private void readEndSetEncodingValues(PeekInputStream stream) throws IOException
    {
        byte[] token = readToken(stream);
        boolean lastTokenWasReadOnly = false;
        while (!(lastTokenWasReadOnly && isDefKeyWord(token)))
        {
            if (isDupKeyWord(token))
            {
                byte[] cidToken = readToken(stream);
                byte[] labelToken = readToken(stream);
                String cid = new String(cidToken, TOKEN_ENCODING);
                String label = new String(labelToken, TOKEN_ENCODING);
                try
                {
                    this.type1Font.addCidWithLabel(Integer.parseInt(cid), label);
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Invalid encoding : Expected CID value before \"" + label + "\" label");
                }
            }
            else
            {
                lastTokenWasReadOnly = isReadOnlyKeyWord(token);
            }
            token = readToken(stream);
        }
    }

    private byte[] readEexec(PeekInputStream stream) throws IOException
    {
        int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        ByteArrayOutputStream eexecPart = new ByteArrayOutputStream();
        int lr = 0;
        int total = 0;
        do
        {
            lr = stream.read(buffer, 0, BUFFER_SIZE);
            if (lr == BUFFER_SIZE && (total + BUFFER_SIZE < eexecSize))
            {
                eexecPart.write(buffer, 0, BUFFER_SIZE);
                total += BUFFER_SIZE;
            }
            else if (lr > 0 && (total + lr < eexecSize))
            {
                eexecPart.write(buffer, 0, lr);
                total += lr;
            }
            else if (lr > 0 && (total + lr >= eexecSize))
            {
                eexecPart.write(buffer, 0, eexecSize - total);
                total += (eexecSize - total);
            }
        } while (eexecSize > total && lr > 0);
        IOUtils.closeQuietly(eexecPart);
        return eexecPart.toByteArray();
    }

    private byte[] decodeEexec(byte[] eexec)
    {
        return Type1FontUtil.eexecDecrypt(eexec);
    }

    private byte[] readLine(PeekInputStream stream) throws IOException
    {
        ArrayList<Byte> bytes = new ArrayList<Byte>();
        int currentCharacter = 0;

        do
        {
            currentCharacter = readNextCharacter(stream);
            bytes.add((byte) (currentCharacter & 0xFF));
        } while (!('\n' == currentCharacter || '\r' == currentCharacter));

        if ('\r' == currentCharacter && '\n' == stream.peek())
        {
            currentCharacter = readNextCharacter(stream);
            bytes.add((byte) (currentCharacter & 0xFF));
        }

        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); ++i)
        {
            result[i] = bytes.get(i);
        }
        return result;
    }

    private byte[] readToken(PeekInputStream stream) throws IOException
    {
        byte[] token = new byte[0];
        skipBlankSeparators(stream);

        int nextByte = stream.peek();
        if (nextByte < 0)
        {
            throw new IOException("Unexpected End Of File");
        }

        if (nextByte == '(')
        {
            token = readStringLiteral(stream);
        }
        else if (nextByte == '[')
        {
            token = readArray(stream);
        }
        else if (nextByte == '{')
        {
            token = readProcedure(stream);
        }
        else
        {
            token = readNameOrArgument(stream);
        }

        return token;
    }

    private byte[] readStringLiteral(PeekInputStream stream) throws IOException
    {
        int opened = 0;
        List<Integer> buffer = new ArrayList<Integer>();

        int currentByte = 0;
        do
        {
            currentByte = readNextCharacter(stream);
            if (currentByte < 0)
            {
                throw new IOException("Unexpected End Of File");
            }

            if (currentByte == '(')
            {
                opened++;
            }
            else if (currentByte == ')')
            {
                opened--;
            }

            buffer.add(currentByte);
        } while (opened != 0);

        return convertListOfIntToByteArray(buffer);
    }

    private byte[] readArray(PeekInputStream stream) throws IOException
    {
        int opened = 0;
        List<Integer> buffer = new ArrayList<Integer>();

        int currentByte = 0;
        do
        {
            currentByte = readNextCharacter(stream);
            if (currentByte < 0)
            {
                throw new IOException("Unexpected End Of File");
            }

            if (currentByte == '[')
            {
                opened++;
            }
            else if (currentByte == ']')
            {
                opened--;
            }

            buffer.add(currentByte);
        } while (opened != 0);

        return convertListOfIntToByteArray(buffer);
    }

    private byte[] readProcedure(PeekInputStream stream) throws IOException
    {
        int opened = 0;
        List<Integer> buffer = new ArrayList<Integer>();

        int currentByte = 0;
        do
        {
            currentByte = readNextCharacter(stream);
            if (currentByte < 0)
            {
                throw new IOException("Unexpected End Of File");
            }

            if (currentByte == '{')
            {
                opened++;
            }
            else if (currentByte == '}')
            {
                opened--;
            }

            buffer.add(currentByte);
        } while (opened != 0);

        return convertListOfIntToByteArray(buffer);
    }

    private byte[] readNameOrArgument(PeekInputStream stream) throws IOException
    {
        List<Integer> buffer = new ArrayList<Integer>();
        int nextByte = 0;
        do
        {
            int currentByte = readNextCharacter(stream);
            if (currentByte < 0)
            {
                throw new IOException("Unexpected End Of File");
            }
            buffer.add(currentByte);
            nextByte = stream.peek();
        } while (isNotBlankSperator(nextByte) && isNotBeginOfName(nextByte) && isNotSeparator(nextByte));

        return convertListOfIntToByteArray(buffer);
    }

    private boolean isNotBeginOfName(int character)
    {
        return ('/' != character);
    }

    private boolean isNotSeparator(int character)
    {
        return !('{' == character || '}' == character || '[' == character || ']' == character);
    }

    private byte[] convertListOfIntToByteArray(List<Integer> input)
    {
        byte[] res = new byte[input.size()];
        for (int i = 0; i < res.length; ++i)
        {
            res[i] = input.get(i).byteValue();
        }
        return res;
    }

    private int readNextCharacter(PeekInputStream stream) throws IOException
    {
        int currentByte = stream.read();
        this.numberOfReadBytes++;
        return currentByte;
    }

    private void skipBlankSeparators(PeekInputStream stream) throws IOException
    {
        int nextByte = stream.peek();
        while (isBlankSperator(nextByte))
        {
            readNextCharacter(stream);
            nextByte = stream.peek();
        }
    }

    private void skipSingleBlankSeparator(PeekInputStream stream) throws IOException
    {
        int nextByte = stream.peek();
        if (isBlankSperator(nextByte))
        {
            readNextCharacter(stream);
        }
    }

    private boolean isBlankSperator(int character)
    {
        return (character == ' ' || character == '\n' || character == '\r');
    }

    private boolean isNotBlankSperator(int character)
    {
        return !isBlankSperator(character);
    }

    private boolean isEExecKeyWord(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return "eexec".equals(word);
    }

    private boolean isDefKeyWord(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return "def".equals(word);
    }

    private boolean isReadOnlyKeyWord(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return "readonly".equals(word);
    }

    private boolean isEncodingKeyWord(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return "/Encoding".equals(word);
    }

    private boolean isDupKeyWord(byte[] token) throws IOException
    {
        String word = new String(token, TOKEN_ENCODING);
        return "dup".equals(word);
    }

    private boolean isStartOfEExecReached()
    {
        return (this.numberOfReadBytes == this.clearTextSize);
    }

    private boolean isEndOfStream(PeekInputStream stream)
    {
        try
        {
            skipBlankSeparators(stream);
            return false;
        }
        catch (IOException e)
        {
            return true;
        }
    }
}
