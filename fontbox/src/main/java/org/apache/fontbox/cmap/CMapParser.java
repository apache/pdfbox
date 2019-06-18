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
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.util.Charsets;

/**
 * Parses a CMap stream.
 *
 * @author Ben Litchfield
 */
public class CMapParser
{
    private static final String MARK_END_OF_DICTIONARY = ">>";
    private static final String MARK_END_OF_ARRAY = "]";

    private final byte[] tokenParserByteBuffer = new byte[512];

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
     * @return A parsed CMAP file.
     * @throws IOException If there is an issue while parsing the CMAP.
     */
    public CMap parse(File file) throws IOException
    {
        FileInputStream input = null;
        try
        {
            input = new FileInputStream(file);
            return parse(input);
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }
    }

    /**
     * Parses a predefined CMap.
     *
     * @param name CMap name.
     * @return The parsed predefined CMap as a java object, never null.
     * @throws IOException If the CMap could not be parsed.
     */
    public CMap parsePredefined(String name) throws IOException
    {
        InputStream input = null;
        try
        {
            input = getExternalCMap(name);
            return parse(input);
        }
        finally
        {
            if (input != null)
            {
                input.close();
            }
        }
    }

    /**
     * This will parse the stream and create a cmap object.
     *
     * @param input The CMAP stream to parse.
     * @return The parsed stream as a java object, never null.
     * @throws IOException If there is an error parsing the stream.
     */
    public CMap parse(InputStream input) throws IOException
    {
        PushbackInputStream cmapStream = new PushbackInputStream(input);
        CMap result = new CMap();
        Object previousToken = null;
        Object token;
        while ((token = parseNextToken(cmapStream)) != null)
        {
            if (token instanceof Operator)
            {
                Operator op = (Operator) token;
                if (op.op.equals("usecmap"))
                {
                    parseUsecmap((LiteralName) previousToken, result);
                }
                else if (op.op.equals("endcmap"))
                {
                    // end of CMap reached, stop reading as there isn't any interesting info anymore
                    break;
                }
                else if (op.op.equals("begincodespacerange"))
                {
                    parseBegincodespacerange((Number) previousToken, cmapStream, result);
                }
                else if (op.op.equals("beginbfchar"))
                {
                    parseBeginbfchar((Number) previousToken, cmapStream, result);
                }
                else if (op.op.equals("beginbfrange"))
                {
                    parseBeginbfrange((Number) previousToken, cmapStream, result);
                }
                else if (op.op.equals("begincidchar"))
                {
                    parseBegincidchar((Number) previousToken, cmapStream, result);
                }
                else if (op.op.equals("begincidrange"))
                {
                    parseBegincidrange((Integer) previousToken, cmapStream, result);
                }
            }
            else if (token instanceof LiteralName)
            {
                parseLiteralName((LiteralName) token, cmapStream, result);
            }
            previousToken = token;
        }
        return result;
    }

    private void parseUsecmap(LiteralName useCmapName, CMap result) throws IOException
    {
        InputStream useStream = getExternalCMap(useCmapName.name);
        CMap useCMap = parse(useStream);
        result.useCmap(useCMap);
    }

    private void parseLiteralName(LiteralName literal, PushbackInputStream cmapStream, CMap result) throws IOException
    {
        if ("WMode".equals(literal.name))
        {
            Object next = parseNextToken(cmapStream);
            if (next instanceof Integer)
            {
                result.setWMode((Integer) next);
            }
        }
        else if ("CMapName".equals(literal.name))
        {
            Object next = parseNextToken(cmapStream);
            if (next instanceof LiteralName)
            {
                result.setName(((LiteralName) next).name);
            }
        }
        else if ("CMapVersion".equals(literal.name))
        {
            Object next = parseNextToken(cmapStream);
            if (next instanceof Number)
            {
                result.setVersion(next.toString());
            }
            else if (next instanceof String)
            {
                result.setVersion((String) next);
            }
        }
        else if ("CMapType".equals(literal.name))
        {
            Object next = parseNextToken(cmapStream);
            if (next instanceof Integer)
            {
                result.setType((Integer) next);
            }
        }
        else if ("Registry".equals(literal.name))
        {
            Object next = parseNextToken(cmapStream);
            if (next instanceof String)
            {
                result.setRegistry((String) next);
            }
        }
        else if ("Ordering".equals(literal.name))
        {
            Object next = parseNextToken(cmapStream);
            if (next instanceof String)
            {
                result.setOrdering((String) next);
            }
        }
        else if ("Supplement".equals(literal.name))
        {
            Object next = parseNextToken(cmapStream);
            if (next instanceof Integer)
            {
                result.setSupplement((Integer) next);
            }
        }
    }

    private void parseBegincodespacerange(Number cosCount, PushbackInputStream cmapStream, CMap result) throws IOException
    {
        for (int j = 0; j < cosCount.intValue(); j++)
        {
            Object nextToken = parseNextToken(cmapStream);
            if (nextToken instanceof Operator)
            {
                if (!((Operator) nextToken).op.equals("endcodespacerange"))
                {
                    throw new IOException("Error : ~codespacerange contains an unexpected operator : "
                            + ((Operator) nextToken).op);
                }
                break;
            }
            byte[] startRange = (byte[]) nextToken;
            byte[] endRange = (byte[]) parseNextToken(cmapStream);
            CodespaceRange range = new CodespaceRange();
            range.setStart(startRange);
            range.setEnd(endRange);
            result.addCodespaceRange(range);
        }
    }

    private void parseBeginbfchar(Number cosCount, PushbackInputStream cmapStream, CMap result) throws IOException
    {
        for (int j = 0; j < cosCount.intValue(); j++)
        {
            Object nextToken = parseNextToken(cmapStream);
            if (nextToken instanceof Operator)
            {
                if (!((Operator) nextToken).op.equals("endbfchar"))
                {
                    throw new IOException("Error : ~bfchar contains an unexpected operator : "
                            + ((Operator) nextToken).op);
                }
                break;
            }
            byte[] inputCode = (byte[]) nextToken;
            nextToken = parseNextToken(cmapStream);
            if (nextToken instanceof byte[])
            {
                byte[] bytes = (byte[]) nextToken;
                String value = createStringFromBytes(bytes);
                result.addCharMapping(inputCode, value);
            }
            else if (nextToken instanceof LiteralName)
            {
                result.addCharMapping(inputCode, ((LiteralName) nextToken).name);
            }
            else
            {
                throw new IOException("Error parsing CMap beginbfchar, expected{COSString "
                        + "or COSName} and not " + nextToken);
            }
        }
    }

    private void parseBegincidrange(int numberOfLines, PushbackInputStream cmapStream, CMap result) throws IOException
    {
        for (int n = 0; n < numberOfLines; n++)
        {
            Object nextToken = parseNextToken(cmapStream);
            if (nextToken instanceof Operator)
            {
                if (!((Operator) nextToken).op.equals("endcidrange"))
                {
                    throw new IOException("Error : ~cidrange contains an unexpected operator : "
                            + ((Operator) nextToken).op);
                }
                break;
            }
            byte[] startCode = (byte[]) nextToken;
            int start = createIntFromBytes(startCode);
            byte[] endCode = (byte[]) parseNextToken(cmapStream);
            int end = createIntFromBytes(endCode);
            int mappedCode = (Integer) parseNextToken(cmapStream);
            if (startCode.length <= 2 && endCode.length <= 2)
            {
                // some CMaps are using CID ranges to map single values
                if (end == start)
                {
                    result.addCIDMapping(mappedCode, start);
                }
                else
                {
                    result.addCIDRange((char) start, (char) end, mappedCode);
                }
            }
            else
            {
                // TODO Is this even possible?
                int endOfMappings = mappedCode + end - start;
                while (mappedCode <= endOfMappings)
                {
                    int mappedCID = createIntFromBytes(startCode);
                    result.addCIDMapping(mappedCode++, mappedCID);
                    increment(startCode);
                }
            }
        }
    }

    private void parseBegincidchar(Number cosCount, PushbackInputStream cmapStream, CMap result) throws IOException
    {
        for (int j = 0; j < cosCount.intValue(); j++)
        {
            Object nextToken = parseNextToken(cmapStream);
            if (nextToken instanceof Operator)
            {
                if (!((Operator) nextToken).op.equals("endcidchar"))
                {
                    throw new IOException("Error : ~cidchar contains an unexpected operator : "
                            + ((Operator) nextToken).op);
                }
                break;
            }
            byte[] inputCode = (byte[]) nextToken;
            int mappedCode = (Integer) parseNextToken(cmapStream);
            int mappedCID = createIntFromBytes(inputCode);
            result.addCIDMapping(mappedCode, mappedCID);
        }
    }

    private void parseBeginbfrange(Number cosCount, PushbackInputStream cmapStream, CMap result) throws IOException
    {
        for (int j = 0; j < cosCount.intValue(); j++)
        {
            Object nextToken = parseNextToken(cmapStream);
            if (nextToken instanceof Operator)
            {
                if (!((Operator) nextToken).op.equals("endbfrange"))
                {
                    throw new IOException("Error : ~bfrange contains an unexpected operator : "
                            + ((Operator) nextToken).op);
                }
                break;
            }
            byte[] startCode = (byte[]) nextToken;
            byte[] endCode = (byte[]) parseNextToken(cmapStream);
            int start = CMap.toInt(startCode, startCode.length);
            int end = CMap.toInt(endCode, endCode.length);
            // end has to be bigger than start or equal
            if (end < start)
            {
                // PDFBOX-4550: likely corrupt stream
                break;
            }
            nextToken = parseNextToken(cmapStream);
            if (nextToken instanceof List<?>)
            {
                List<byte[]> array = (List<byte[]>) nextToken;
                // ignore empty and malformed arrays
                if (!array.isEmpty() && array.size() >= end - start)
                {
                    addMappingFrombfrange(result, startCode, array);
                }
            }
            // PDFBOX-3807: ignore null
            else if (nextToken instanceof byte[])
            {
                // the range can not represent more that 255 values
                if ((end - start) > 255)
                {
                    // PDFBOX-4550: likely corrupt stream
                    break;
                }
                byte[] tokenBytes = (byte[]) nextToken;
                // PDFBOX-3450: ignore <>
                if (tokenBytes.length > 0)
                {
                    addMappingFrombfrange(result, startCode, end - start + 1, tokenBytes);
                }
            }
        }
    }

    private void addMappingFrombfrange(CMap cmap, byte[] startCode, List<byte[]> tokenBytesList)
    {
        for (byte[] tokenBytes : tokenBytesList)
        {
            String value = createStringFromBytes(tokenBytes);
            cmap.addCharMapping(startCode, value);
            increment(startCode);
        }
    }

    private void addMappingFrombfrange(CMap cmap, byte[] startCode, int values,
            byte[] tokenBytes)
    {
        for (int i = 0; i < values; i++)
        {
            String value = createStringFromBytes(tokenBytes);
            cmap.addCharMapping(startCode, value);
            increment(startCode);
            increment(tokenBytes);
        }
    }

    /**
     * Returns an input stream containing the given "use" CMap.
     *
     * @param name Name of the given "use" CMap resource.
     * @throws IOException if the CMap resource doesn't exist or if there is an error opening its
     * stream.
     */
    protected InputStream getExternalCMap(String name) throws IOException
    {
        InputStream is = getClass().getResourceAsStream(name);
        if (is == null)
        {
            throw new IOException("Error: Could not find referenced cmap stream " + name);
        }
        return is;
    }

    private Object parseNextToken(PushbackInputStream is) throws IOException
    {
        Object retval = null;
        int nextByte = is.read();
        // skip whitespace
        while (nextByte == 0x09 || nextByte == 0x20 || nextByte == 0x0D || nextByte == 0x0A)
        {
            nextByte = is.read();
        }
        switch (nextByte)
        {
        case '%':
        {
            // header operations, for now return the entire line
            // may need to smarter in the future
            StringBuilder buffer = new StringBuilder();
            buffer.append((char) nextByte);
            readUntilEndOfLine(is, buffer);
            retval = buffer.toString();
            break;
        }
        case '(':
        {
            StringBuilder buffer = new StringBuilder();
            int stringByte = is.read();

            while (stringByte != -1 && stringByte != ')')
            {
                buffer.append((char) stringByte);
                stringByte = is.read();
            }
            retval = buffer.toString();
            break;
        }
        case '>':
        {
            int secondCloseBrace = is.read();
            if (secondCloseBrace == '>')
            {
                retval = MARK_END_OF_DICTIONARY;
            }
            else
            {
                throw new IOException("Error: expected the end of a dictionary.");
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

            Object nextToken = parseNextToken(is);
            while (nextToken != null && !MARK_END_OF_ARRAY.equals(nextToken))
            {
                list.add(nextToken);
                nextToken = parseNextToken(is);
            }
            retval = list;
            break;
        }
        case '<':
        {
            int theNextByte = is.read();
            if (theNextByte == '<')
            {
                Map<String, Object> result = new HashMap<String, Object>();
                // we are reading a dictionary
                Object key = parseNextToken(is);
                while (key instanceof LiteralName && !MARK_END_OF_DICTIONARY.equals(key))
                {
                    Object value = parseNextToken(is);
                    result.put(((LiteralName) key).name, value);
                    key = parseNextToken(is);
                }
                retval = result;
            }
            else
            {
                // won't read more than 512 bytes

                int multiplyer = 16;
                int bufferIndex = -1;
                while (theNextByte != -1 && theNextByte != '>')
                {
                    int intValue = 0;
                    if (theNextByte >= '0' && theNextByte <= '9')
                    {
                        intValue = theNextByte - '0';
                    }
                    else if (theNextByte >= 'A' && theNextByte <= 'F')
                    {
                        intValue = 10 + theNextByte - 'A';
                    }
                    else if (theNextByte >= 'a' && theNextByte <= 'f')
                    {
                        intValue = 10 + theNextByte - 'a';
                    }
                    // all kind of whitespaces may occur in malformed CMap files
                    // see PDFBOX-2035
                    else if (isWhitespaceOrEOF(theNextByte))
                    {
                        // skipping whitespaces
                        theNextByte = is.read();
                        continue;
                    }
                    else
                    {
                        throw new IOException("Error: expected hex character and not " + (char) theNextByte + ":"
                                + theNextByte);
                    }
                    intValue *= multiplyer;
                    if (multiplyer == 16)
                    {
                        bufferIndex++;
                        tokenParserByteBuffer[bufferIndex] = 0;
                        multiplyer = 1;
                    }
                    else
                    {
                        multiplyer = 16;
                    }
                    tokenParserByteBuffer[bufferIndex] += intValue;
                    theNextByte = is.read();
                }
                byte[] finalResult = new byte[bufferIndex + 1];
                System.arraycopy(tokenParserByteBuffer, 0, finalResult, 0, bufferIndex + 1);
                retval = finalResult;
            }
            break;
        }
        case '/':
        {
            StringBuilder buffer = new StringBuilder();
            int stringByte = is.read();

            while (!isWhitespaceOrEOF(stringByte) && !isDelimiter(stringByte))
            {
                buffer.append((char) stringByte);
                stringByte = is.read();
            }
            if (isDelimiter( stringByte)) 
            {
                is.unread(stringByte);
            }
            retval = new LiteralName(buffer.toString());
            break;
        }
        case -1:
        {
            // EOF returning null
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
            StringBuilder buffer = new StringBuilder();
            buffer.append((char) nextByte);
            nextByte = is.read();

            while (!isWhitespaceOrEOF(nextByte) && (Character.isDigit((char) nextByte) || nextByte == '.'))
            {
                buffer.append((char) nextByte);
                nextByte = is.read();
            }
            is.unread(nextByte);
            String value = buffer.toString();
            if (value.indexOf('.') >= 0)
            {
                retval = Double.valueOf(value);
            }
            else
            {
                retval = Integer.valueOf(value);
            }
            break;
        }
        default:
        {
            StringBuilder buffer = new StringBuilder();
            buffer.append((char) nextByte);
            nextByte = is.read();

            // newline separator may be missing in malformed CMap files
            // see PDFBOX-2035
            while (!isWhitespaceOrEOF(nextByte) && !isDelimiter(nextByte) && !Character.isDigit(nextByte))
            {
                buffer.append((char) nextByte);
                nextByte = is.read();
            }
            if (isDelimiter(nextByte) || Character.isDigit(nextByte))
            {
                is.unread(nextByte);
            }
            retval = new Operator(buffer.toString());

            break;
        }
        }
        return retval;
    }

    private void readUntilEndOfLine(InputStream is, StringBuilder buf) throws IOException
    {
        int nextByte = is.read();
        while (nextByte != -1 && nextByte != 0x0D && nextByte != 0x0A)
        {
            buf.append((char) nextByte);
            nextByte = is.read();
        }
    }

    private boolean isWhitespaceOrEOF(int aByte)
    {
        return aByte == -1 || aByte == 0x20 || aByte == 0x0D || aByte == 0x0A;
    }

    /** Is this a standard PDF delimiter character? */
    private boolean isDelimiter(int aByte) 
    {
        switch (aByte) 
        {
            case '(':
            case ')':
            case '<':
            case '>':
            case '[':
            case ']':
            case '{':
            case '}':
            case '/':
            case '%':
                return true;
            default:
                return false;
        }
    }

    private void increment(byte[] data)
    {
        increment(data, data.length - 1);
    }

    private void increment(byte[] data, int position)
    {
        if (position > 0 && (data[position] & 0xFF) == 255)
        {
            data[position] = 0;
            increment(data, position - 1);
        }
        else
        {
            data[position] = (byte) (data[position] + 1);
        }
    }

    private int createIntFromBytes(byte[] bytes)
    {
        int intValue = bytes[0] & 0xFF;
        if (bytes.length == 2)
        {
            intValue <<= 8;
            intValue += bytes[1] & 0xFF;
        }
        return intValue;
    }

    private String createStringFromBytes(byte[] bytes)
    {
        return new String(bytes, bytes.length == 1 ? Charsets.ISO_8859_1 : Charsets.UTF_16BE);
    }

    /**
     * Internal class.
     */
    private static final class LiteralName
    {
        private String name;

        private LiteralName(String theName)
        {
            name = theName;
        }
    }

    /**
     * Internal class.
     */
    private static final class Operator
    {
        private String op;

        private Operator(String theOp)
        {
            op = theOp;
        }
    }
}
