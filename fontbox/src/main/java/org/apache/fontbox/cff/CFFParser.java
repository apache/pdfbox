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
package org.apache.fontbox.cff;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.fontbox.ttf.FontHeaders;
import org.apache.pdfbox.io.RandomAccessRead;


/**
 * This class represents a parser for a CFF font. 
 * @author Villu Ruusmann
 */
public class CFFParser
{
    /**
     * Log instance.
     */
    private static final Logger LOG = LogManager.getLogger(CFFParser.class);

    private static final String TAG_OTTO = "OTTO";
    private static final String TAG_TTCF = "ttcf";
    private static final String TAG_TTFONLY = "\u0000\u0001\u0000\u0000";

    private String[] stringIndex = null;
    private ByteSource source;
    
    // for debugging only
    private String debugFontName;

    /**
     * Source from which bytes may be read in the future.
     */
    public interface ByteSource
    {
        /**
         * Returns the source bytes. May be called more than once.
         * 
         * @return the source data as byte array
         * @throws IOException if the data could not be read
         */
        byte[] getBytes() throws IOException;
    }

    /**
     * Extract "Registry", "Ordering" and "Supplement" properties from the first CFF subfont.
     * 
     * @param randomAccessRead the source to be parsed
     * @param outHeaders where to put results
     * @throws IOException If there is an error reading from the stream
     */
    public void parseFirstSubFontROS(RandomAccessRead randomAccessRead, FontHeaders outHeaders) throws IOException
    {
        // this method is a simplified and merged version of parse(RandomAccessRead) > parse(DataInput) > parseFont(...)

        // start code from parse(RandomAccessRead)
        randomAccessRead.seek(0);
        DataInput input = new DataInputRandomAccessRead(randomAccessRead);

        // start code from parse(DataInput)
        input = skipHeader(input);
        String[] nameIndex = readStringIndexData(input);
        if (nameIndex.length == 0)
        {
            outHeaders.setError("Name index missing in CFF font");
            return;
        }
        byte[][] topDictIndex = readIndexData(input);
        if (topDictIndex.length == 0)
        {
            outHeaders.setError("Top DICT INDEX missing in CFF font");
            return;
        }

        // 'stringIndex' is required by 'parseROS() > readString()'
        stringIndex = readStringIndexData(input);

        // start code from parseFont(...)
        DataInputByteArray topDictInput = new DataInputByteArray(topDictIndex[0]);
        DictData topDict = readDictData(topDictInput);

        DictData.Entry syntheticBaseEntry = topDict.getEntry("SyntheticBase");
        if (syntheticBaseEntry != null)
        {
            outHeaders.setError("Synthetic Fonts are not supported");
            return;
        }

        CFFCIDFont cffCIDFont = parseROS(topDict);
        if (cffCIDFont != null)
        {
            outHeaders.setOtfROS(
                    cffCIDFont.getRegistry(),
                    cffCIDFont.getOrdering(),
                    cffCIDFont.getSupplement());
        }
    }

    /**
     * Parse CFF font using byte array, also passing in a byte source for future use.
     * 
     * @param bytes source bytes
     * @param source source to re-read bytes from in the future
     * @return the parsed CFF fonts
     * @throws IOException If there is an error reading from the stream
     */
    public List<CFFFont> parse(byte[] bytes, ByteSource source) throws IOException
    {
        // TODO do we need to store the source data of the font? It isn't used at all
        this.source = source;
        return parse(new DataInputByteArray(bytes));
    }
    
    /**
     * Parse CFF font using a RandomAccessRead as input.
     * 
     * @param randomAccessRead the source to be parsed
     * @return the parsed CFF fonts
     * @throws IOException If there is an error reading from the stream
     */
    public List<CFFFont> parse(RandomAccessRead randomAccessRead) throws IOException
    {
        // TODO do we need to store the source data of the font? It isn't used at all
        byte[] bytes = new byte[(int) randomAccessRead.length()];
        randomAccessRead.seek(0);
        int remainingBytes = bytes.length;
        int amountRead;
        while ((amountRead = randomAccessRead.read(bytes, bytes.length - remainingBytes,
                remainingBytes)) > 0)
        {
            remainingBytes -= amountRead;
        }
        randomAccessRead.seek(0);
        this.source = new CFFBytesource(bytes);
        return parse(new DataInputRandomAccessRead(randomAccessRead));
    }

    private DataInput skipHeader(DataInput input) throws IOException
    {
        String firstTag = readTagName(input);
        // try to determine which kind of font we have
        switch (firstTag)
        {
            case TAG_OTTO:
                input = createTaggedCFFDataInput(input);
                break;
            case TAG_TTCF:
                throw new IOException("True Type Collection fonts are not supported.");
            case TAG_TTFONLY:
                throw new IOException("OpenType fonts containing a true type font are not supported.");
            default:
                input.setPosition(0);
                break;
        }

        @SuppressWarnings("unused")
        Header header = readHeader(input);
        return input;
    }

    /**
     * Parse CFF font using a DataInput as input.
     * 
     * @param input the source to be parsed
     * @return the parsed CFF fonts
     * @throws IOException If there is an error reading from the stream
     */
    private List<CFFFont> parse(DataInput input) throws IOException
    {
        input = skipHeader(input);
        String[] nameIndex = readStringIndexData(input);
        if (nameIndex.length == 0)
        {
            throw new IOException("Name index missing in CFF font");
        }
        byte[][] topDictIndex = readIndexData(input);
        if (topDictIndex.length == 0)
        {
            throw new IOException("Top DICT INDEX missing in CFF font");
        }
        
        stringIndex = readStringIndexData(input);
        byte[][] globalSubrIndex = readIndexData(input);

        List<CFFFont> fonts = new ArrayList<>(nameIndex.length);
        for (int i = 0; i < nameIndex.length; i++)
        {
            CFFFont font = parseFont(input, nameIndex[i], topDictIndex[i]);
            font.setGlobalSubrIndex(globalSubrIndex);
            font.setData(source);
            fonts.add(font);
        }
        return fonts;
    }

    private DataInput createTaggedCFFDataInput(DataInput input) throws IOException
    {
        // this is OpenType font containing CFF data
        // so find CFF tag
        short numTables = input.readShort();
        @SuppressWarnings({"unused", "squid:S1854"})
        short searchRange = input.readShort();
        @SuppressWarnings({"unused", "squid:S1854"})
        short entrySelector = input.readShort();
        @SuppressWarnings({"unused", "squid:S1854"})
        short rangeShift = input.readShort();
        for (int q = 0; q < numTables; q++)
        {
            String tagName = readTagName(input);
            @SuppressWarnings("unused")
            long checksum = readLong(input);
            long offset = readLong(input);
            long length = readLong(input);
            if ("CFF ".equals(tagName))
            {
                input.setPosition((int)offset);
                byte[] bytes2 = input.readBytes((int) length);
                return new DataInputByteArray(bytes2);
            }
        }
        throw new IOException("CFF tag not found in this OpenType font.");
    }

    private static String readTagName(DataInput input) throws IOException
    {
        byte[] b = input.readBytes(4);
        return new String(b, StandardCharsets.ISO_8859_1);
    }

    private static long readLong(DataInput input) throws IOException
    {
        return (input.readUnsignedShort() << 16) | input.readUnsignedShort();
    }

    private static int readOffSize(DataInput input) throws IOException
    {
        int offSize = input.readUnsignedByte();
        if (offSize < 1 || offSize > 4)
        {
            throw new IOException("Illegal (< 1 or > 4) offSize value " + offSize
                    + " in CFF font at position " + (input.getPosition() - 1));
        }
        return offSize;
    }

    private static Header readHeader(DataInput input) throws IOException
    {
        int major = input.readUnsignedByte();
        int minor = input.readUnsignedByte();
        int hdrSize = input.readUnsignedByte();
        int offSize = readOffSize(input);
        return new Header(major, minor, hdrSize, offSize);
    }

    private static int[] readIndexDataOffsets(DataInput input) throws IOException
    {
        int count = input.readUnsignedShort();
        if (count == 0)
        {
            return new int[0];
        }
        int offSize = readOffSize(input);
        int[] offsets = new int[count + 1];
        for (int i = 0; i <= count; i++)
        {
            int offset = input.readOffset(offSize);
            if (offset > input.length())
            {
                throw new IOException("illegal offset value " + offset + " in CFF font");
            }
            offsets[i] = offset;
        }
        return offsets;
    }

    private static byte[][] readIndexData(DataInput input) throws IOException
    {
        int[] offsets = readIndexDataOffsets(input);
        if (offsets.length == 0)
        {
            return new byte[0][];
        }
        int count = offsets.length-1;
        byte[][] indexDataValues = new byte[count][];
        for (int i = 0; i < count; i++)
        {
            int length = offsets[i + 1] - offsets[i];
            indexDataValues[i] = input.readBytes(length);
        }
        return indexDataValues;
    }

    private static String[] readStringIndexData(DataInput input) throws IOException
    {
        int[] offsets = readIndexDataOffsets(input);
        if (offsets.length == 0)
        {
            return new String[0];
        }
        int count = offsets.length-1;
        String[] indexDataValues = new String[count];
        for (int i = 0; i < count; i++)
        {
            int length = offsets[i + 1] - offsets[i];
            if (length < 0)
            {
                throw new IOException("Negative index data length + " + length + " at " + 
                        i + ": offsets[" + (i + 1) + "]=" + offsets[i + 1] + 
                        ", offsets[" + i + "]=" + offsets[i]);
            }
            indexDataValues[i] = new String(input.readBytes(length), StandardCharsets.ISO_8859_1);
        }
        return indexDataValues;
    }

    private static DictData readDictData(DataInput input) throws IOException
    {
        DictData dict = new DictData();
        while (input.hasRemaining())
        {
            dict.add(readEntry(input));
        }
        return dict;
    }

    private static DictData readDictData(DataInput input, int offset, int dictSize)
            throws IOException
    {
        DictData dict = new DictData();
        if (dictSize > 0)
        {
            input.setPosition(offset);
            int endPosition = offset + dictSize;
            while (input.getPosition() < endPosition)
            {
                dict.add(readEntry(input));
            }
        }
        return dict;
    }

    private static DictData.Entry readEntry(DataInput input) throws IOException
    {
        DictData.Entry entry = new DictData.Entry();
        while (true)
        {
            int b0 = input.readUnsignedByte();

            if (b0 >= 0 && b0 <= 21)
            {
                entry.operatorName = readOperator(input, b0);
                break;
            }
            else if (b0 == 28 || b0 == 29)
            {
                entry.addOperand(readIntegerNumber(input, b0));
            }
            else if (b0 == 30)
            {
                entry.addOperand(readRealNumber(input));
            }
            else if (b0 >= 32 && b0 <= 254)
            {
                entry.addOperand(readIntegerNumber(input, b0));
            }
            else
            {
                throw new IOException("invalid DICT data b0 byte: " + b0);
            }
        }
        return entry;
    }

    private static String readOperator(DataInput input, int b0) throws IOException
    {
        if (b0 == 12)
        {
            int b1 = input.readUnsignedByte();
            return CFFOperator.getOperator(b0, b1);
        }
        return CFFOperator.getOperator(b0);

    }

    private static Integer readIntegerNumber(DataInput input, int b0) throws IOException
    {
        if (b0 == 28)
        {
            return (int) input.readShort();
        }
        else if (b0 == 29)
        {
            return input.readInt();
        }
        else if (b0 >= 32 && b0 <= 246)
        {
            return b0 - 139;
        }
        else if (b0 >= 247 && b0 <= 250)
        {
            int b1 = input.readUnsignedByte();
            return (b0 - 247) * 256 + b1 + 108;
        }
        else if (b0 >= 251 && b0 <= 254)
        {
            int b1 = input.readUnsignedByte();
            return -(b0 - 251) * 256 - b1 - 108;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private static Double readRealNumber(DataInput input) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        boolean done = false;
        boolean exponentMissing = false;
        boolean hasExponent = false;
        int[] nibbles = new int[2];
        while (!done)
        {
            int b = input.readUnsignedByte();
            nibbles[0] = b / 16;
            nibbles[1] = b % 16;
            for (int nibble : nibbles)
            {
                switch (nibble)
                {
                case 0x0:
                case 0x1:
                case 0x2:
                case 0x3:
                case 0x4:
                case 0x5:
                case 0x6:
                case 0x7:
                case 0x8:
                case 0x9:
                    sb.append(nibble);
                    exponentMissing = false;
                    break;
                case 0xa:
                    sb.append('.');
                    break;
                case 0xb:
                    if (hasExponent)
                    {
                        LOG.warn("duplicate 'E' ignored after {}", sb);
                        break;
                    }
                    sb.append('E');
                    exponentMissing = true;
                    hasExponent = true;
                    break;
                case 0xc:
                    if (hasExponent)
                    {
                        LOG.warn("duplicate 'E-' ignored after {}", sb);
                        break;
                    }
                    sb.append("E-");
                    exponentMissing = true;
                    hasExponent = true;
                    break;
                case 0xd:
                    break;
                case 0xe:
                    sb.append('-');
                    break;
                case 0xf:
                    done = true;
                    break;
                default:
                    // can only be a programming error because a nibble is between 0 and F 
                    throw new IllegalArgumentException("illegal nibble " + nibble);
                }
            }
        }
        if (exponentMissing)
        {
            // the exponent is missing, just append "0" to avoid an exception
            // not sure if 0 is the correct value, but it seems to fit
            // see PDFBOX-1522
            sb.append('0');
        }
        if (sb.length() == 0)
        {
            return 0d;
        }
        try
        {
            return Double.valueOf(sb.toString());
        }
        catch (NumberFormatException ex)
        {
            throw new IOException(ex);
        }
    }

    /**
     * Extracts Registry, Ordering and Supplement from {@code topDict["ROS"]}.
     */
    private CFFCIDFont parseROS(DictData topDict) throws IOException
    {
        // determine if this is a Type 1-equivalent font or a CIDFont
        DictData.Entry rosEntry = topDict.getEntry("ROS");
        if (rosEntry != null)
        {
            if (rosEntry.size() < 3)
            {
                throw new IOException("ROS entry must have 3 elements");
            }
            CFFCIDFont cffCIDFont = new CFFCIDFont();
            cffCIDFont.setRegistry(readString(rosEntry.getNumber(0).intValue()));
            cffCIDFont.setOrdering(readString(rosEntry.getNumber(1).intValue()));
            cffCIDFont.setSupplement(rosEntry.getNumber(2).intValue());
            return cffCIDFont;
        }
        return null;
    }

    private CFFFont parseFont(DataInput input, String name, byte[] topDictIndex) throws IOException
    {
        // top dict
        DataInputByteArray topDictInput = new DataInputByteArray(topDictIndex);
        DictData topDict = readDictData(topDictInput);

        // we don't support synthetic fonts
        DictData.Entry syntheticBaseEntry = topDict.getEntry("SyntheticBase");
        if (syntheticBaseEntry != null)
        {
            throw new IOException("Synthetic Fonts are not supported");
        }

        // determine if this is a Type 1-equivalent font or a CIDFont
        CFFFont font;
        CFFCIDFont cffCIDFont = parseROS(topDict);
        // determine if this is a Type 1-equivalent font or a CIDFont
        boolean isCIDFont = cffCIDFont != null;
        if (cffCIDFont != null)
        {
            font = cffCIDFont;
        }
        else
        {
            font = new CFFType1Font();
        }

        // name
        debugFontName = name;
        font.setName(name);

        // top dict
        font.addValueToTopDict("version", getString(topDict, "version"));
        font.addValueToTopDict("Notice", getString(topDict, "Notice"));
        font.addValueToTopDict("Copyright", getString(topDict, "Copyright"));
        font.addValueToTopDict("FullName", getString(topDict, "FullName"));
        font.addValueToTopDict("FamilyName", getString(topDict, "FamilyName"));
        font.addValueToTopDict("Weight", getString(topDict, "Weight"));
        font.addValueToTopDict("isFixedPitch", topDict.getBoolean("isFixedPitch", false));
        font.addValueToTopDict("ItalicAngle", topDict.getNumber("ItalicAngle", 0));
        font.addValueToTopDict("UnderlinePosition", topDict.getNumber("UnderlinePosition", -100));
        font.addValueToTopDict("UnderlineThickness", topDict.getNumber("UnderlineThickness", 50));
        font.addValueToTopDict("PaintType", topDict.getNumber("PaintType", 0));
        font.addValueToTopDict("CharstringType", topDict.getNumber("CharstringType", 2));
        font.addValueToTopDict("FontMatrix", topDict.getArray("FontMatrix", List.of(
                                                      0.001, 0.0, 0.0, 0.001,
                                                      0.0, 0.0)));
        font.addValueToTopDict("UniqueID", topDict.getNumber("UniqueID", null));
        font.addValueToTopDict("FontBBox", topDict.getArray("FontBBox",
                                                    List.of(0, 0, 0, 0)));
        font.addValueToTopDict("StrokeWidth", topDict.getNumber("StrokeWidth", 0));
        font.addValueToTopDict("XUID", topDict.getArray("XUID", null));

        // charstrings index
        DictData.Entry charStringsEntry = topDict.getEntry("CharStrings");
        if (charStringsEntry == null || !charStringsEntry.hasOperands())
        {
            throw new IOException("CharStrings is missing or empty");
        }
        int charStringsOffset = charStringsEntry.getNumber(0).intValue();
        input.setPosition(charStringsOffset);
        byte[][] charStringsIndex = readIndexData(input);
        
        // charset
        DictData.Entry charsetEntry = topDict.getEntry("charset");
        CFFCharset charset;
        if (charsetEntry != null && charsetEntry.hasOperands())
        {
            int charsetId = charsetEntry.getNumber(0).intValue();
            if (!isCIDFont && charsetId == 0)
            {
                charset = CFFISOAdobeCharset.getInstance();
            }
            else if (!isCIDFont && charsetId == 1)
            {
                charset = CFFExpertCharset.getInstance();
            }
            else if (!isCIDFont && charsetId == 2)
            {
                charset = CFFExpertSubsetCharset.getInstance();
            }
            else if (charStringsIndex.length > 0)
            {
                input.setPosition(charsetId);
                charset = readCharset(input, charStringsIndex.length, isCIDFont);
            }
            // that should not happen
            else
            {
                LOG.debug("Couldn't read CharStrings index - returning empty charset instead");
                charset = new EmptyCharsetType1();
            }            
        }
        else
        {
            if (isCIDFont)
            {
                // a CID font with no charset does not default to any predefined charset
                charset = new EmptyCharsetCID(charStringsIndex.length);
            }
            else
            {
                charset = CFFISOAdobeCharset.getInstance();
            }
        }
        font.setCharset(charset);

        // charstrings dict
        font.charStrings = charStringsIndex;

        // format-specific dictionaries
        if (isCIDFont)
        {

            // CharStrings index could be null if the index data couldn't be read
            int numEntries = 0;
            if (charStringsIndex.length == 0)
            {
                LOG.debug("Couldn't read CharStrings index - parsing CIDFontDicts with number of char strings set to 0");
            }
            else
            {
                numEntries = charStringsIndex.length;
            }

            parseCIDFontDicts(input, topDict, (CFFCIDFont) font, numEntries);

            List<Number> privMatrix = null;
            List<Map<String, Object>> fontDicts = ((CFFCIDFont) font).getFontDicts();
            if (!fontDicts.isEmpty() && fontDicts.get(0).containsKey("FontMatrix"))
            {
                privMatrix = (List<Number>) fontDicts.get(0).get("FontMatrix");
            }
            // some malformed fonts have FontMatrix in their Font DICT, see PDFBOX-2495
            List<Number> matrix = topDict.getArray("FontMatrix", null);
            if (matrix == null)
            {
                if (privMatrix != null)
                {
                    font.addValueToTopDict("FontMatrix", privMatrix);
                }
                else
                {
                    // default
                    font.addValueToTopDict("FontMatrix", topDict.getArray("FontMatrix",
                            List.of(0.001, 0.0, 0.0, 0.001, 0.0, 0.0)));
                }
            }
            else if (privMatrix != null)
            {
                // we have to multiply the font matrix from the top directory with the font matrix
                // from the private directory. This should be done for synthetic fonts only but in
                // case of PDFBOX-3579 it's needed as well to get the right scaling
                concatenateMatrix(matrix, privMatrix);
            }

        }
        else
        {
            parseType1Dicts(input, topDict, (CFFType1Font) font, charset);
        }

        return font;
    }

    private void concatenateMatrix(List<Number> matrixDest, List<Number> matrixConcat)
    {
        // concatenate matrices
        // (a b 0)
        // (c d 0)
        // (x y 1)
        double a1 = matrixDest.get(0).doubleValue();
        double b1 = matrixDest.get(1).doubleValue();
        double c1 = matrixDest.get(2).doubleValue();
        double d1 = matrixDest.get(3).doubleValue();
        double x1 = matrixDest.get(4).doubleValue();
        double y1 = matrixDest.get(5).doubleValue();

        double a2 = matrixConcat.get(0).doubleValue();
        double b2 = matrixConcat.get(1).doubleValue();
        double c2 = matrixConcat.get(2).doubleValue();
        double d2 = matrixConcat.get(3).doubleValue();
        double x2 = matrixConcat.get(4).doubleValue();
        double y2 = matrixConcat.get(5).doubleValue();

        matrixDest.set(0, a1 * a2 + b1 * c2);
        matrixDest.set(1, a1 * b2 + b1 * d1);
        matrixDest.set(2, c1 * a2 + d1 * c2);
        matrixDest.set(3, c1 * b2 + d1 * d2);
        matrixDest.set(4, x1 * a2 + y1 * c2 + x2);
        matrixDest.set(5, x1 * b2 + y1 * d2 + y2);
    }

    /**
     * Parse dictionaries specific to a CIDFont.
     */
    private void parseCIDFontDicts(DataInput input, DictData topDict, CFFCIDFont font,
            int nrOfcharStrings)
            throws IOException
    {
        // In a CIDKeyed Font, the Private dictionary isn't in the Top Dict but in the Font dict
        // which can be accessed by a lookup using FDArray and FDSelect
        DictData.Entry fdArrayEntry = topDict.getEntry("FDArray");
        if (fdArrayEntry == null || !fdArrayEntry.hasOperands())
        {
            throw new IOException("FDArray is missing for a CIDKeyed Font.");
        }

        // font dict index
        int fontDictOffset = fdArrayEntry.getNumber(0).intValue();
        input.setPosition(fontDictOffset);
        byte[][] fdIndex = readIndexData(input);
        if (fdIndex.length == 0)
        {
            throw new IOException("Font dict index is missing for a CIDKeyed Font");
        }

        List<Map<String, Object>> privateDictionaries = new LinkedList<>();
        List<Map<String, Object>> fontDictionaries = new LinkedList<>();

        boolean privateDictPopulated = false;
        for (byte[] bytes : fdIndex)
        {
            DataInputByteArray fontDictInput = new DataInputByteArray(bytes);
            DictData fontDict = readDictData(fontDictInput);

            // font dict
            Map<String, Object> fontDictMap = new LinkedHashMap<>(4);
            fontDictMap.put("FontName", getString(fontDict, "FontName"));
            fontDictMap.put("FontType", fontDict.getNumber("FontType", 0));
            fontDictMap.put("FontBBox", fontDict.getArray("FontBBox", null));
            fontDictMap.put("FontMatrix", fontDict.getArray("FontMatrix", null));
            // TODO OD-4 : Add here other keys
            fontDictionaries.add(fontDictMap);

            // read private dict
            DictData.Entry privateEntry = fontDict.getEntry("Private");
            if (privateEntry == null || privateEntry.size() < 2)
            {
                // PDFBOX-5843 don't abort here, and don't skip empty bytes entries, because
                // getLocalSubrIndex() expects subr at a specific index
                privateDictionaries.add(new HashMap<>());
                continue;
            }

            int privateOffset = privateEntry.getNumber(1).intValue();
            int privateSize = privateEntry.getNumber(0).intValue();
            DictData privateDict = readDictData(input, privateOffset, privateSize);

            // populate private dict
            privateDictPopulated = true;
            Map<String, Object> privDict = readPrivateDict(privateDict);
            privateDictionaries.add(privDict);

            // local subrs
            Number localSubrOffset = privateDict.getNumber("Subrs", 0);
            if (localSubrOffset instanceof Integer && ((int) localSubrOffset) > 0)
            {
                input.setPosition(privateOffset + (int) localSubrOffset);
                privDict.put("Subrs", readIndexData(input));
            }
        }

        if (!privateDictPopulated)
        {
            throw new IOException("Font DICT invalid without \"Private\" entry");
        }

        // font-dict (FD) select
        DictData.Entry fdSelectEntry = topDict.getEntry("FDSelect");
        if (fdSelectEntry == null || !fdSelectEntry.hasOperands())
        {
            throw new IOException("FDSelect is missing or empty");
        }
        int fdSelectPos = fdSelectEntry.getNumber(0).intValue();
        input.setPosition(fdSelectPos);
        FDSelect fdSelect = readFDSelect(input, nrOfcharStrings);

        // TODO almost certainly erroneous - CIDFonts do not have a top-level private dict
        // font.addValueToPrivateDict("defaultWidthX", 1000);
        // font.addValueToPrivateDict("nominalWidthX", 0);

        font.setFontDict(fontDictionaries);
        font.setPrivDict(privateDictionaries);
        font.setFdSelect(fdSelect);
    }

    private Map<String, Object> readPrivateDict(DictData privateDict)
    {
        Map<String, Object> privDict = new LinkedHashMap<>(17);
        privDict.put("BlueValues", privateDict.getDelta("BlueValues", null));
        privDict.put("OtherBlues", privateDict.getDelta("OtherBlues", null));
        privDict.put("FamilyBlues", privateDict.getDelta("FamilyBlues", null));
        privDict.put("FamilyOtherBlues", privateDict.getDelta("FamilyOtherBlues", null));
        privDict.put("BlueScale", privateDict.getNumber("BlueScale", 0.039625));
        privDict.put("BlueShift", privateDict.getNumber("BlueShift", 7));
        privDict.put("BlueFuzz", privateDict.getNumber("BlueFuzz", 1));
        privDict.put("StdHW", privateDict.getNumber("StdHW", null));
        privDict.put("StdVW", privateDict.getNumber("StdVW", null));
        privDict.put("StemSnapH", privateDict.getDelta("StemSnapH", null));
        privDict.put("StemSnapV", privateDict.getDelta("StemSnapV", null));
        privDict.put("ForceBold", privateDict.getBoolean("ForceBold", false));
        privDict.put("LanguageGroup", privateDict.getNumber("LanguageGroup", 0));
        privDict.put("ExpansionFactor", privateDict.getNumber("ExpansionFactor", 0.06));
        privDict.put("initialRandomSeed", privateDict.getNumber("initialRandomSeed", 0));
        privDict.put("defaultWidthX", privateDict.getNumber("defaultWidthX", 0));
        privDict.put("nominalWidthX", privateDict.getNumber("nominalWidthX", 0));
        return privDict;
    }

    /**
     * Parse dictionaries specific to a Type 1-equivalent font.
     */
    private void parseType1Dicts(DataInput input, DictData topDict, CFFType1Font font,
            CFFCharset charset)
            throws IOException
    {
        // encoding
        DictData.Entry encodingEntry = topDict.getEntry("Encoding");
        CFFEncoding encoding;
        int encodingId = encodingEntry != null && encodingEntry.hasOperands() ?
                encodingEntry.getNumber(0).intValue() : 0;
        switch (encodingId)
        {
            case 0:
                encoding = CFFStandardEncoding.getInstance();
                break;
            case 1:
                encoding = CFFExpertEncoding.getInstance();
                break;
            default:
                input.setPosition(encodingId);
                encoding = readEncoding(input, charset);
                break;
        }
        font.setEncoding(encoding);

        // read private dict
        DictData.Entry privateEntry = topDict.getEntry("Private");
        if (privateEntry == null || privateEntry.size() < 2)
        {
            throw new IOException("Private dictionary entry missing for font " + font.getName());
        }
        int privateOffset = privateEntry.getNumber(1).intValue();
        int privateSize = privateEntry.getNumber(0).intValue();
        DictData privateDict = readDictData(input, privateOffset, privateSize);

        // populate private dict
        Map<String, Object> privDict = readPrivateDict(privateDict);
        privDict.forEach(font::addToPrivateDict);

        // local subrs
        Number localSubrOffset = privateDict.getNumber("Subrs", 0);
        if (localSubrOffset instanceof Integer && ((int) localSubrOffset) > 0)
        {
            input.setPosition(privateOffset + (int) localSubrOffset);
            font.addToPrivateDict("Subrs", readIndexData(input));
        }
    }

    private String readString(int index) throws IOException
    {
        if (index < 0)
        {
            throw new IOException("Invalid negative index when reading a string");
        }
        if (index <= 390)
        {
            return CFFStandardString.getName(index);
        }
        if (stringIndex != null && index - 391 < stringIndex.length)
        {
            return stringIndex[index - 391];
        }
        // technically this maps to .notdef, but we need a unique sid name
        return "SID" + index;
    }

    private String getString(DictData dict, String name) throws IOException
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null && entry.hasOperands() ? readString(entry.getNumber(0).intValue()) : null;
    }

    private CFFEncoding readEncoding(DataInput dataInput, CFFCharset charset) throws IOException
    {
        int format = dataInput.readUnsignedByte();
        int baseFormat = format & 0x7f;

        switch (baseFormat)
        {
            case 0:
                return readFormat0Encoding(dataInput, charset, format);
            case 1:
                return readFormat1Encoding(dataInput, charset, format);
            default:
                throw new IOException("Invalid encoding base format " + baseFormat);
        }
    }

    private Format0Encoding readFormat0Encoding(DataInput dataInput, CFFCharset charset,
            int format)
            throws IOException
    {
        Format0Encoding encoding = new Format0Encoding(dataInput.readUnsignedByte());
        encoding.add(0, 0, ".notdef");
        for (int gid = 1; gid <= encoding.nCodes; gid++)
        {
            int code = dataInput.readUnsignedByte();
            int sid = charset.getSIDForGID(gid);
            encoding.add(code, sid, readString(sid));
        }
        if ((format & 0x80) != 0)
        {
            readSupplement(dataInput, encoding);
        }
        return encoding;
    }

    private Format1Encoding readFormat1Encoding(DataInput dataInput, CFFCharset charset,
            int format) throws IOException
    {
        Format1Encoding encoding = new Format1Encoding(dataInput.readUnsignedByte());
        encoding.add(0, 0, ".notdef");
        int gid = 1;
        for (int i = 0; i < encoding.nRanges; i++)
        {
            int rangeFirst = dataInput.readUnsignedByte(); // First code in range
            int rangeLeft = dataInput.readUnsignedByte(); // Codes left in range (excluding first)
            for (int j = 0; j <= rangeLeft; j++)
            {
                int sid = charset.getSIDForGID(gid);
                encoding.add(rangeFirst + j, sid, readString(sid));
                gid++;
            }
        }
        if ((format & 0x80) != 0)
        {
            readSupplement(dataInput, encoding);
        }
        return encoding;
    }

    private void readSupplement(DataInput dataInput, CFFBuiltInEncoding encoding)
            throws IOException
    {
        int nSups = dataInput.readUnsignedByte();
        encoding.supplement = new CFFBuiltInEncoding.Supplement[nSups];
        for (int i = 0; i < nSups; i++)
        {
            int code = dataInput.readUnsignedByte();
            int sid = dataInput.readUnsignedShort();
            encoding.supplement[i] = new CFFBuiltInEncoding.Supplement(code, sid, readString(sid));
            encoding.add(encoding.supplement[i]);
        }
    }

    /**
     * Read the FDSelect Data according to the format.
     * @param dataInput
     * @param nGlyphs
     * @return the FDSelect data
     * @throws IOException
     */
    private static FDSelect readFDSelect(DataInput dataInput, int nGlyphs) throws IOException
    {
        int format = dataInput.readUnsignedByte();
        switch (format)
        {
            case 0:
                return readFormat0FDSelect(dataInput, nGlyphs);
            case 3:
                return readFormat3FDSelect(dataInput);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Read the Format 0 of the FDSelect data structure.
     * @param dataInput
     * @param nGlyphs
     * @return the Format 0 of the FDSelect data
     * @throws IOException
     */
    private static Format0FDSelect readFormat0FDSelect(DataInput dataInput, int nGlyphs)
            throws IOException
    {
        int[] fds = new int[nGlyphs];
        for (int i = 0; i < nGlyphs; i++)
        {
            fds[i] = dataInput.readUnsignedByte();
        }
        return new Format0FDSelect(fds);
    }

    /**
     * Read the Format 3 of the FDSelect data structure.
     * 
     * @param dataInput
     * @return the Format 3 of the FDSelect data
     * @throws IOException
     */
    private static Format3FDSelect readFormat3FDSelect(DataInput dataInput)
            throws IOException
    {
        int nbRanges = dataInput.readUnsignedShort();

        Range3[] range3 = new Range3[nbRanges];
        for (int i = 0; i < nbRanges; i++)
        {
            range3[i] = new Range3(dataInput.readUnsignedShort(), dataInput.readUnsignedByte());
        }
        return new Format3FDSelect(range3, dataInput.readUnsignedShort());
    }

    /**
     *  Format 3 FDSelect data.
     */
    private static final class Format3FDSelect implements FDSelect
    {
        private final Range3[] range3;
        private final int sentinel;

        private Format3FDSelect(Range3[] range3, int sentinel)
        {
            this.range3 = range3;
            this.sentinel = sentinel;
        }

        @Override
        public int getFDIndex(int gid)
        {
            for (int i = 0; i < range3.length; ++i)
            {
                if (range3[i].first <= gid)
                {
                    if (i + 1 < range3.length)
                    {
                        if (range3[i + 1].first > gid)
                        {
                            return range3[i].fd;
                        }
                        // go to next range
                    }
                    else
                    {
                        // last range reach, the sentinel must be greater than gid
                        if (sentinel > gid)
                        {
                            return range3[i].fd;
                        }
                        return -1;
                    }
                }
            }
            return 0;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[nbRanges=" + range3.length + ", range3="
                    + Arrays.toString(range3) + " sentinel=" + sentinel + "]";
        }
    }

    /**
     * Structure of a Range3 element.
     */
    private static final class Range3
    {
        private final int first;
        private final int fd;

        private Range3(int first, int fd)
        {
            this.first = first;
            this.fd = fd;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[first=" + first + ", fd=" + fd + "]";
        }
    }

    /**
     *  Format 0 FDSelect.
     */
    private static class Format0FDSelect implements FDSelect
    {
        private final int[] fds;

        private Format0FDSelect(int[] fds)
        {
            this.fds = fds;
        }

        @Override
        public int getFDIndex(int gid)
        {
            if (gid < fds.length)
            {
                return fds[gid];
            }
            return 0;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[fds=" + Arrays.toString(fds) + "]";
        }
    }

    private CFFCharset readCharset(DataInput dataInput, int nGlyphs, boolean isCIDFont)
            throws IOException
    {
        int format = dataInput.readUnsignedByte();
        switch (format)
        {
            case 0:
                return readFormat0Charset(dataInput, nGlyphs, isCIDFont);
            case 1:
                return readFormat1Charset(dataInput, nGlyphs, isCIDFont);
            case 2:
                return readFormat2Charset(dataInput, nGlyphs, isCIDFont);
            default:
                // we can't return new EmptyCharset(0), because this will bring more mayhem
                throw new IOException("Incorrect charset format " + format);
        }
    }

    private Format0Charset readFormat0Charset(DataInput dataInput, int nGlyphs,
                                              boolean isCIDFont) throws IOException
    {
        Format0Charset charset = new Format0Charset(isCIDFont);
        if (isCIDFont)
        {
            charset.addCID(0, 0);
            for (int gid = 1; gid < nGlyphs; gid++)
            {
                charset.addCID(gid, dataInput.readUnsignedShort());
            }
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
            for (int gid = 1; gid < nGlyphs; gid++)
            {
                int sid = dataInput.readUnsignedShort();
                charset.addSID(gid, sid, readString(sid));
            }
        }
        return charset;
    }

    private Format1Charset readFormat1Charset(DataInput dataInput, int nGlyphs,
                                              boolean isCIDFont) throws IOException
    {
        Format1Charset charset = new Format1Charset(isCIDFont);
        if (isCIDFont)
        {
            charset.addCID(0, 0);
            int gid = 1;
            while (gid < nGlyphs)
            {
                int rangeFirst = dataInput.readUnsignedShort();
                int rangeLeft = dataInput.readUnsignedByte();
                charset.addRangeMapping(new RangeMapping(gid, rangeFirst, rangeLeft));
                gid += rangeLeft + 1;
            }
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
            int gid = 1;
            while (gid < nGlyphs)
            {
                int rangeFirst = dataInput.readUnsignedShort();
                int rangeLeft = dataInput.readUnsignedByte() + 1;
                for (int j = 0; j < rangeLeft; j++)
                {
                    int sid = rangeFirst + j;
                    charset.addSID(gid + j, sid, readString(sid));
                }
                gid += rangeLeft;
            }
        }
        return charset;
    }

    private Format2Charset readFormat2Charset(DataInput dataInput, int nGlyphs,
            boolean isCIDFont) throws IOException
    {
        Format2Charset charset = new Format2Charset(isCIDFont);
        if (isCIDFont)
        {
            charset.addCID(0, 0);
            int gid = 1;
            while (gid < nGlyphs)
            {
                int first = dataInput.readUnsignedShort();
                int nLeft = dataInput.readUnsignedShort();
                charset.addRangeMapping(new RangeMapping(gid, first, nLeft));
                gid += nLeft + 1;
            }
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
            int gid = 1;
            while (gid < nGlyphs)
            {
                int first = dataInput.readUnsignedShort();
                int nLeft = dataInput.readUnsignedShort() + 1;
                for (int j = 0; j < nLeft; j++)
                {
                    int sid = first + j;
                    charset.addSID(gid + j, sid, readString(sid));
                }
                gid += nLeft;
            }
        }
        return charset;
    }

    /**
     * Inner class holding the header of a CFF font. 
     */
    private static class Header
    {
        private final int major;
        private final int minor;
        private final int hdrSize;
        private final int offSize;

        private Header(int major, int minor, int hdrSize, int offSize)
        {
            this.major = major;
            this.minor = minor;
            this.hdrSize = hdrSize;
            this.offSize = offSize;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[major=" + major + ", minor=" + minor + ", hdrSize=" + hdrSize
                    + ", offSize=" + offSize + "]";
        }
    }

    /**
     * Inner class holding the DictData of a CFF font. 
     */
    private static class DictData
    {
        private final Map<String, Entry> entries = new HashMap<>();

        public void add(Entry entry)
        {
            if (entry.operatorName != null)
            {
                entries.put(entry.operatorName, entry);
            }
        }
        
        public Entry getEntry(String name)
        {
            return entries.get(name);
        }

        public Boolean getBoolean(String name, boolean defaultValue)
        {
            Entry entry = getEntry(name);
            return entry != null && entry.hasOperands() ? entry.getBoolean(0, defaultValue) : defaultValue;
        }

        public List<Number> getArray(String name, List<Number> defaultValue)
        {
            Entry entry = getEntry(name);
            return entry != null && entry.hasOperands() ? entry.getOperands() : defaultValue;
        }

        public Number getNumber(String name, Number defaultValue)
        {
            Entry entry = getEntry(name);
            return entry != null && entry.hasOperands() ? entry.getNumber(0) : defaultValue;
        }

        public List<Number> getDelta(String name, List<Number> defaultValue) 
        {
            Entry entry = getEntry(name);
            return entry != null && entry.hasOperands() ? entry.getDelta() : defaultValue;
        }
        
        /**
         * {@inheritDoc} 
         */
        @Override
        public String toString()
        {
            return getClass().getName() + "[entries=" + entries + "]";
        }

        /**
         * Inner class holding an operand of a CFF font. 
         */
        private static class Entry
        {
            private final List<Number> operands = new ArrayList<>();
            private String operatorName = null;

            public Number getNumber(int index)
            {
                return operands.get(index);
            }

            public int size()
            {
                return operands.size();
            }

            public Boolean getBoolean(int index, Boolean defaultValue)
            {
                Number operand = operands.get(index);
                if (operand instanceof Integer)
                {
                    switch (operand.intValue())
                    {
                        case 0:
                            return Boolean.FALSE;
                        case 1:
                            return Boolean.TRUE;
                        default:
                            break;
                    }
                }
                LOG.warn("Expected boolean, got {}, returning default {}", operand, defaultValue);
                return defaultValue;
            }

            public void addOperand(Number operand)
            {
                operands.add(operand);
            }

            public boolean hasOperands()
            {
                return !operands.isEmpty();
            }

            public List<Number> getOperands()
            {
                return operands;
            }

            public List<Number> getDelta()
            {
                List<Number> result = new ArrayList<>(operands);
                for (int i = 1; i < result.size(); i++)
                {
                    Number previous = result.get(i - 1);
                    Number current = result.get(i);
                    int sum = previous.intValue() + current.intValue();
                    result.set(i, sum);
                }
                return result;
            }

            @Override
            public String toString()
            {
                return getClass().getName() + "[operands=" + operands + ", operator=" + operatorName
                        + "]";
            }
        }
    }

    /**
     * Inner class representing a font's built-in CFF encoding. 
     */
    abstract static class CFFBuiltInEncoding extends CFFEncoding
    {
        private Supplement[] supplement;

        /**
         * Inner class representing a supplement for an encoding. 
         */
        private static class Supplement
        {
            private final int code;
            private final int sid;
            private final String name;

            private Supplement(int code, int sid, String name)
            {
                this.code = code;
                this.sid = sid;
                this.name = name;
            }

            @Override
            public String toString()
            {
                return getClass().getName() + "[code=" + code + ", sid=" + sid + "]";
            }
        }

        public void add(Supplement supplement)
        {
            add(supplement.code, supplement.sid, supplement.name);
        }
    }

    /**
     * Inner class representing a Format0 encoding. 
     */
    private static class Format0Encoding extends CFFBuiltInEncoding
    {
        private final int nCodes;

        private Format0Encoding(int nCodes)
        {
            this.nCodes = nCodes;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[nCodes=" + nCodes
                    + ", supplement=" + Arrays.toString(super.supplement) + "]";
        }
    }

    /**
     * Inner class representing a Format1 encoding. 
     */
    private static class Format1Encoding extends CFFBuiltInEncoding
    {
        private final int nRanges;

        private Format1Encoding(int nRanges)
        {
            this.nRanges = nRanges;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[nRanges=" + nRanges
                    + ", supplement=" + Arrays.toString(super.supplement) + "]";
        }
    }

    /**
     * An empty charset in a malformed CID font.
     */
    private static class EmptyCharsetCID extends CFFCharsetCID
    {
        private EmptyCharsetCID(int numCharStrings)
        {
            addCID(0, 0); // .notdef
            
            // Adobe Reader treats CID as GID, PDFBOX-2571 p11.
            for (int i = 1; i <= numCharStrings; i++)
            {
                addCID(i, i);
            }
        }

        @Override
        public String toString()
        {
            return getClass().getName();
        }
    }

    /**
     * An empty charset in a malformed Type1 font.
     */
    private static class EmptyCharsetType1 extends CFFCharsetType1
    {
        private EmptyCharsetType1()
        {
            addSID(0, 0, ".notdef");
        }

        @Override
        public String toString()
        {
            return getClass().getName();
        }
    }

    /**
     * Inner class representing a Format0 charset.
     */
    private static class Format0Charset extends EmbeddedCharset
    {
        private Format0Charset(boolean isCIDFont)
        {
            super(isCIDFont);
        }
    }

    /**
     * Inner class representing a Format1 charset. 
     */
    private static class Format1Charset extends EmbeddedCharset
    {
        private final List<RangeMapping> rangesCID2GID;

        private Format1Charset(boolean isCIDFont)
        {
            super(isCIDFont);
            rangesCID2GID = new ArrayList<>();
        }

        /**
         * Add the given range mapping.
         * 
         * @param rangeMapping the range mapping to be added.
         */
        public void addRangeMapping(RangeMapping rangeMapping)
        {
            rangesCID2GID.add(rangeMapping);
        }

        @Override
        public int getCIDForGID(int gid)
        {
            if (isCIDFont())
            {
                for (RangeMapping mapping : rangesCID2GID)
                {
                    if (mapping.isInRange(gid))
                    {
                        return mapping.mapValue(gid);
                    }
                }
            }
            return super.getCIDForGID(gid);
        }
        
        @Override
        public int getGIDForCID(int cid)
        {
            if (isCIDFont())
            {
                for (RangeMapping mapping : rangesCID2GID)
                {
                    if (mapping.isInReverseRange(cid))
                    {
                        return mapping.mapReverseValue(cid);
                    }
                }
            }
            return super.getGIDForCID(cid);
        }
    }

    /**
     * Inner class representing a Format2 charset. 
     */
    private static class Format2Charset extends EmbeddedCharset
    {
        private final List<RangeMapping> rangesCID2GID;
        
        private Format2Charset(boolean isCIDFont)
        {
            super(isCIDFont);
            rangesCID2GID = new ArrayList<>();
        }

        /**
         * Add the given range mapping.
         * 
         * @param rangeMapping the range mapping to be added.
         */
        public void addRangeMapping(RangeMapping rangeMapping)
        {
            rangesCID2GID.add(rangeMapping);
        }

        @Override
        public int getCIDForGID(int gid)
        {
            for (RangeMapping mapping : rangesCID2GID)
            {
                if (mapping.isInRange(gid))
                {
                    return mapping.mapValue(gid);
                }
            }
            return super.getCIDForGID(gid);
        }
        
        @Override
        public int getGIDForCID(int cid)
        {
            for (RangeMapping mapping : rangesCID2GID)
            {
                if (mapping.isInReverseRange(cid))
                {
                    return mapping.mapReverseValue(cid);
                }
            }
            return super.getGIDForCID(cid);
        }
    }

    /**
     * Inner class representing a rang mapping for a CID charset. 
     */
    private static final class RangeMapping
    {
        private final int startValue;
        private final int endValue;
        private final int startMappedValue;
        private final int endMappedValue;

        private RangeMapping(int startGID, int first, int nLeft)
        {
            this.startValue = startGID;
            endValue = startValue + nLeft;
            this.startMappedValue = first;
            endMappedValue = startMappedValue + nLeft;
        }
        
        boolean isInRange(int value)
        {
            return value >= startValue && value <= endValue;
        }
        
        boolean isInReverseRange(int value)
        {
            return value >= startMappedValue && value <= endMappedValue;
        }

        int mapValue(int value)
        {
            return isInRange(value) ? startMappedValue + (value - startValue) : 0;
        }

        int mapReverseValue(int value)
        {
            return isInReverseRange(value) ? startValue + (value - startMappedValue) : 0;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[start value=" + startValue + ", end value=" + endValue +  ", start mapped-value=" + startMappedValue +  ", end mapped-value=" + endMappedValue +"]";
        }
    }

    /**
     * Allows bytes to be re-read later by CFFParser.
     */
    private static class CFFBytesource implements CFFParser.ByteSource
    {
        private final byte[] bytes;

        CFFBytesource(byte[] bytes)
        {
            this.bytes = bytes;
        }

        @Override
        public byte[] getBytes() throws IOException
        {
            return bytes;
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + debugFontName + "]";
    }
}
