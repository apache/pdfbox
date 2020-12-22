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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class represents a parser for a CFF font. 
 * @author Villu Ruusmann
 */
public class CFFParser
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(CFFParser.class);

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
         */
        byte[] getBytes() throws IOException;
    }

    /**
     * Parse CFF font using byte array, also passing in a byte source for future use.
     * 
     * @param bytes source bytes
     * @param source source to re-read bytes from in the future
     * @return the parsed CFF fonts
     * @throws IOException If there is an error reading from the stream
     */
    public List<CFFFont> parse(final byte[] bytes, final ByteSource source) throws IOException
    {
        this.source = source;
        return parse(bytes);
    }
    
    /**
     * Parse CFF font using a byte array as input.
     * 
     * @param bytes the given byte array
     * @return the parsed CFF fonts
     * @throws IOException If there is an error reading from the stream
     */
    public List<CFFFont> parse(final byte[] bytes) throws IOException
    {
        CFFDataInput input = new CFFDataInput(bytes);

        final String firstTag = readTagName(input);
        // try to determine which kind of font we have
        switch (firstTag)
        {
            case TAG_OTTO:
                input = createTaggedCFFDataInput(input, bytes);
                break;
            case TAG_TTCF:
                throw new IOException("True Type Collection fonts are not supported.");
            case TAG_TTFONLY:
                throw new IOException("OpenType fonts containing a true type font are not supported.");
            default:
                input.setPosition(0);
                break;
        }

        @SuppressWarnings("unused") final Header header = readHeader(input);
        final String[] nameIndex = readStringIndexData(input);
        if (nameIndex == null)
        {
            throw new IOException("Name index missing in CFF font");
        }
        final byte[][] topDictIndex = readIndexData(input);
        if (topDictIndex == null)
        {
            throw new IOException("Top DICT INDEX missing in CFF font");
        }
        
        stringIndex = readStringIndexData(input);
        final byte[][] globalSubrIndex = readIndexData(input);

        final List<CFFFont> fonts = new ArrayList<>();
        for (int i = 0; i < nameIndex.length; i++)
        {
            final CFFFont font = parseFont(input, nameIndex[i], topDictIndex[i]);
            font.setGlobalSubrIndex(globalSubrIndex);
            font.setData(source);
            fonts.add(font);
        }
        return fonts;
    }

    private CFFDataInput createTaggedCFFDataInput(final CFFDataInput input, final byte[] bytes) throws IOException
    {
        // this is OpenType font containing CFF data
        // so find CFF tag
        final short numTables = input.readShort();
        @SuppressWarnings({"unused", "squid:S1854"}) final short searchRange = input.readShort();
        @SuppressWarnings({"unused", "squid:S1854"}) final short entrySelector = input.readShort();
        @SuppressWarnings({"unused", "squid:S1854"}) final short rangeShift = input.readShort();
        for (int q = 0; q < numTables; q++)
        {
            final String tagName = readTagName(input);
            @SuppressWarnings("unused") final long checksum = readLong(input);
            final long offset = readLong(input);
            final long length = readLong(input);
            if ("CFF ".equals(tagName))
            {
                final byte[] bytes2 = Arrays.copyOfRange(bytes, (int) offset, (int) (offset + length));
                return new CFFDataInput(bytes2);
            }
        }
        throw new IOException("CFF tag not found in this OpenType font.");
    }

    private static String readTagName(final CFFDataInput input) throws IOException
    {
        final byte[] b = input.readBytes(4);
        return new String(b, StandardCharsets.ISO_8859_1);
    }

    private static long readLong(final CFFDataInput input) throws IOException
    {
        return (input.readCard16() << 16) | input.readCard16();
    }

    private static Header readHeader(final CFFDataInput input) throws IOException
    {
        final Header cffHeader = new Header();
        cffHeader.major = input.readCard8();
        cffHeader.minor = input.readCard8();
        cffHeader.hdrSize = input.readCard8();
        cffHeader.offSize = input.readOffSize();
        return cffHeader;
    }

    private static int[] readIndexDataOffsets(final CFFDataInput input) throws IOException
    {
        final int count = input.readCard16();
        if (count == 0)
        {
            return null;
        }
        final int offSize = input.readOffSize();
        final int[] offsets = new int[count+1];
        for (int i = 0; i <= count; i++)
        {
            final int offset = input.readOffset(offSize);
            if (offset > input.length())
            {
                throw new IOException("illegal offset value " + offset + " in CFF font");
            }
            offsets[i] = offset;
        }
        return offsets;
    }

    private static byte[][] readIndexData(final CFFDataInput input) throws IOException
    {
        final int[] offsets = readIndexDataOffsets(input);
        if (offsets == null)
        {
            return null;
        }
        final int count = offsets.length-1;
        final byte[][] indexDataValues = new byte[count][];
        for (int i = 0; i < count; i++)
        {
            final int length = offsets[i + 1] - offsets[i];
            indexDataValues[i] = input.readBytes(length);
        }
        return indexDataValues;
    }

    private static String[] readStringIndexData(final CFFDataInput input) throws IOException
    {
        final int[] offsets = readIndexDataOffsets(input);
        if (offsets == null)
        {
            return null;
        }
        final int count = offsets.length-1;
        final String[] indexDataValues = new String[count];
        for (int i = 0; i < count; i++)
        {
            final int length = offsets[i + 1] - offsets[i];
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

    private static DictData readDictData(final CFFDataInput input) throws IOException
    {
        final DictData dict = new DictData();
        while (input.hasRemaining())
        {
            dict.add(readEntry(input));
        }
        return dict;
    }

    private static DictData readDictData(final CFFDataInput input, final int dictSize) throws IOException
    {
        final DictData dict = new DictData();
        final int endPosition = input.getPosition() + dictSize;
        while (input.getPosition() < endPosition)
        {
            dict.add(readEntry(input));
        }
        return dict;
    }

    private static DictData.Entry readEntry(final CFFDataInput input) throws IOException
    {
        final DictData.Entry entry = new DictData.Entry();
        while (true)
        {
            final int b0 = input.readUnsignedByte();

            if (b0 >= 0 && b0 <= 21)
            {
                entry.operator = readOperator(input, b0);
                break;
            }
            else if (b0 == 28 || b0 == 29)
            {
                entry.operands.add(readIntegerNumber(input, b0));
            }
            else if (b0 == 30)
            {
                entry.operands.add(readRealNumber(input, b0));
            }
            else if (b0 >= 32 && b0 <= 254)
            {
                entry.operands.add(readIntegerNumber(input, b0));
            }
            else
            {
                throw new IOException("invalid DICT data b0 byte: " + b0);
            }
        }
        return entry;
    }

    private static CFFOperator readOperator(final CFFDataInput input, final int b0) throws IOException
    {
        final CFFOperator.Key key = readOperatorKey(input, b0);
        return CFFOperator.getOperator(key);
    }

    private static CFFOperator.Key readOperatorKey(final CFFDataInput input, final int b0) throws IOException
    {
        if (b0 == 12)
        {
            final int b1 = input.readUnsignedByte();
            return new CFFOperator.Key(b0, b1);
        }
        return new CFFOperator.Key(b0);
    }

    private static Integer readIntegerNumber(final CFFDataInput input, final int b0) throws IOException
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
            final int b1 = input.readUnsignedByte();
            return (b0 - 247) * 256 + b1 + 108;
        }
        else if (b0 >= 251 && b0 <= 254)
        {
            final int b1 = input.readUnsignedByte();
            return -(b0 - 251) * 256 - b1 - 108;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param b0  
     */
    private static Double readRealNumber(final CFFDataInput input, final int b0) throws IOException
    {
        final StringBuilder sb = new StringBuilder();
        boolean done = false;
        boolean exponentMissing = false;
        boolean hasExponent = false;
        while (!done)
        {
            final int b = input.readUnsignedByte();
            final int[] nibbles = { b / 16, b % 16 };
            for (final int nibble : nibbles)
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
                    sb.append(".");
                    break;
                case 0xb:
                    if (hasExponent)
                    {
                        LOG.warn("duplicate 'E' ignored after " + sb);
                        break;
                    }
                    sb.append("E");
                    exponentMissing = true;
                    hasExponent = true;
                    break;
                case 0xc:
                    if (hasExponent)
                    {
                        LOG.warn("duplicate 'E-' ignored after " + sb);
                        break;
                    }
                    sb.append("E-");
                    exponentMissing = true;
                    hasExponent = true;
                    break;
                case 0xd:
                    break;
                case 0xe:
                    sb.append("-");
                    break;
                case 0xf:
                    done = true;
                    break;
                default:
                    throw new IllegalArgumentException();
                }
            }
        }
        if (exponentMissing)
        {
            // the exponent is missing, just append "0" to avoid an exception
            // not sure if 0 is the correct value, but it seems to fit
            // see PDFBOX-1522
            sb.append("0");
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

    private CFFFont parseFont(final CFFDataInput input, final String name, final byte[] topDictIndex) throws IOException
    {
        // top dict
        final CFFDataInput topDictInput = new CFFDataInput(topDictIndex);
        final DictData topDict = readDictData(topDictInput);

        // we don't support synthetic fonts
        final DictData.Entry syntheticBaseEntry = topDict.getEntry("SyntheticBase");
        if (syntheticBaseEntry != null)
        {
            throw new IOException("Synthetic Fonts are not supported");
        }

        // determine if this is a Type 1-equivalent font or a CIDFont
        final CFFFont font;
        final boolean isCIDFont = topDict.getEntry("ROS") != null;
        if (isCIDFont)
        {
            font = new CFFCIDFont();
            final DictData.Entry rosEntry = topDict.getEntry("ROS");
            ((CFFCIDFont) font).setRegistry(readString(rosEntry.getNumber(0).intValue()));
            ((CFFCIDFont) font).setOrdering(readString(rosEntry.getNumber(1).intValue()));
            ((CFFCIDFont) font).setSupplement(rosEntry.getNumber(2).intValue());
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
        font.addValueToTopDict("FontMatrix", topDict.getArray("FontMatrix", Arrays.<Number>asList(
                                                      0.001, (double) 0, (double) 0, 0.001,
                                                      (double) 0, (double) 0)));
        font.addValueToTopDict("UniqueID", topDict.getNumber("UniqueID", null));
        font.addValueToTopDict("FontBBox", topDict.getArray("FontBBox",
                                                    Arrays.<Number> asList(0, 0, 0, 0)));
        font.addValueToTopDict("StrokeWidth", topDict.getNumber("StrokeWidth", 0));
        font.addValueToTopDict("XUID", topDict.getArray("XUID", null));

        // charstrings index
        final DictData.Entry charStringsEntry = topDict.getEntry("CharStrings");
        final int charStringsOffset = charStringsEntry.getNumber(0).intValue();
        input.setPosition(charStringsOffset);
        final byte[][] charStringsIndex = readIndexData(input);
        
        // charset
        final DictData.Entry charsetEntry = topDict.getEntry("charset");
        final CFFCharset charset;
        if (charsetEntry != null)
        {
            final int charsetId = charsetEntry.getNumber(0).intValue();
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
            else if (charStringsIndex != null)
            {
                input.setPosition(charsetId);
                charset = readCharset(input, charStringsIndex.length, isCIDFont);
            }
            // that should not happen
            else
            {
                LOG.debug("Couldn't read CharStrings index - returning empty charset instead");
                charset = new EmptyCharset(0);
            }
            
        }
        else
        {
            if (isCIDFont)
            {
                // CharStrings index could be null if the index data couldnÄt be read
                final int numEntries = charStringsIndex == null ? 0 :  charStringsIndex.length;
                // a CID font with no charset does not default to any predefined charset
                charset = new EmptyCharset(numEntries);
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
            if (charStringsIndex == null)
            {
                LOG.debug("Couldn't read CharStrings index - parsing CIDFontDicts with number of char strings set to 0");
            }
            else
            {
                numEntries = charStringsIndex.length;
            }

            parseCIDFontDicts(input, topDict, (CFFCIDFont) font, numEntries);

            List<Number> privMatrix = null;
            final List<Map<String, Object>> fontDicts = ((CFFCIDFont) font).getFontDicts();
            if (!fontDicts.isEmpty() && fontDicts.get(0).containsKey("FontMatrix"))
            {
                privMatrix = (List<Number>) fontDicts.get(0).get("FontMatrix");
            }
            // some malformed fonts have FontMatrix in their Font DICT, see PDFBOX-2495
            final List<Number> matrix = topDict.getArray("FontMatrix", null);
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
                            Arrays.<Number>asList(0.001, (double) 0, (double) 0, 0.001,
                                    (double) 0, (double) 0)));
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

    private void concatenateMatrix(final List<Number> matrixDest, final List<Number> matrixConcat)
    {
        // concatenate matrices
        // (a b 0)
        // (c d 0)
        // (x y 1)
        final double a1 = matrixDest.get(0).doubleValue();
        final double b1 = matrixDest.get(1).doubleValue();
        final double c1 = matrixDest.get(2).doubleValue();
        final double d1 = matrixDest.get(3).doubleValue();
        final double x1 = matrixDest.get(4).doubleValue();
        final double y1 = matrixDest.get(5).doubleValue();

        final double a2 = matrixConcat.get(0).doubleValue();
        final double b2 = matrixConcat.get(1).doubleValue();
        final double c2 = matrixConcat.get(2).doubleValue();
        final double d2 = matrixConcat.get(3).doubleValue();
        final double x2 = matrixConcat.get(4).doubleValue();
        final double y2 = matrixConcat.get(5).doubleValue();

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
    private void parseCIDFontDicts(final CFFDataInput input, final DictData topDict, final CFFCIDFont font, final int nrOfcharStrings)
            throws IOException
    {
        // In a CIDKeyed Font, the Private dictionary isn't in the Top Dict but in the Font dict
        // which can be accessed by a lookup using FDArray and FDSelect
        final DictData.Entry fdArrayEntry = topDict.getEntry("FDArray");
        if (fdArrayEntry == null)
        {
            throw new IOException("FDArray is missing for a CIDKeyed Font.");
        }

        // font dict index
        final int fontDictOffset = fdArrayEntry.getNumber(0).intValue();
        input.setPosition(fontDictOffset);
        final byte[][] fdIndex = readIndexData(input);
        if (fdIndex == null)
        {
            throw new IOException("Font dict index is missing for a CIDKeyed Font");
        }

        final List<Map<String, Object>> privateDictionaries = new LinkedList<>();
        final List<Map<String, Object>> fontDictionaries = new LinkedList<>();

        for (final byte[] bytes : fdIndex)
        {
            final CFFDataInput fontDictInput = new CFFDataInput(bytes);
            final DictData fontDict = readDictData(fontDictInput);

            // read private dict
            final DictData.Entry privateEntry = fontDict.getEntry("Private");
            if (privateEntry == null)
            {
                throw new IOException("Font DICT invalid without \"Private\" entry");
            }

            // font dict
            final Map<String, Object> fontDictMap = new LinkedHashMap<>(4);
            fontDictMap.put("FontName", getString(fontDict, "FontName"));
            fontDictMap.put("FontType", fontDict.getNumber("FontType", 0));
            fontDictMap.put("FontBBox", fontDict.getArray("FontBBox", null));
            fontDictMap.put("FontMatrix", fontDict.getArray("FontMatrix", null));
            // TODO OD-4 : Add here other keys
            fontDictionaries.add(fontDictMap);

            final int privateOffset = privateEntry.getNumber(1).intValue();
            input.setPosition(privateOffset);
            final int privateSize = privateEntry.getNumber(0).intValue();
            final DictData privateDict = readDictData(input, privateSize);

            // populate private dict
            final Map<String, Object> privDict = readPrivateDict(privateDict);
            privateDictionaries.add(privDict);

            // local subrs
            final int localSubrOffset = (Integer) privateDict.getNumber("Subrs", 0);
            if (localSubrOffset > 0)
            {
                input.setPosition(privateOffset + localSubrOffset);
                privDict.put("Subrs", readIndexData(input));
            }
        }

        // font-dict (FD) select
        final DictData.Entry fdSelectEntry = topDict.getEntry("FDSelect");
        final int fdSelectPos = fdSelectEntry.getNumber(0).intValue();
        input.setPosition(fdSelectPos);
        final FDSelect fdSelect = readFDSelect(input, nrOfcharStrings, font);

        // TODO almost certainly erroneous - CIDFonts do not have a top-level private dict
        // font.addValueToPrivateDict("defaultWidthX", 1000);
        // font.addValueToPrivateDict("nominalWidthX", 0);

        font.setFontDict(fontDictionaries);
        font.setPrivDict(privateDictionaries);
        font.setFdSelect(fdSelect);
    }

    private Map<String, Object> readPrivateDict(final DictData privateDict)
    {
        final Map<String, Object> privDict = new LinkedHashMap<>(17);
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
    private void parseType1Dicts(final CFFDataInput input, final DictData topDict, final CFFType1Font font, final CFFCharset charset)
            throws IOException
    {
        // encoding
        final DictData.Entry encodingEntry = topDict.getEntry("Encoding");
        final CFFEncoding encoding;
        final int encodingId = encodingEntry != null ? encodingEntry.getNumber(0).intValue() : 0;
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
        final DictData.Entry privateEntry = topDict.getEntry("Private");
        if (privateEntry == null)
        {
            throw new IOException("Private dictionary entry missing for font " + font.fontName);
        }
        final int privateOffset = privateEntry.getNumber(1).intValue();
        input.setPosition(privateOffset);
        final int privateSize = privateEntry.getNumber(0).intValue();
        final DictData privateDict = readDictData(input, privateSize);

        // populate private dict
        final Map<String, Object> privDict = readPrivateDict(privateDict);
        privDict.forEach(font::addToPrivateDict);

        // local subrs
        final int localSubrOffset = (Integer) privateDict.getNumber("Subrs", 0);
        if (localSubrOffset > 0)
        {
            input.setPosition(privateOffset + localSubrOffset);
            font.addToPrivateDict("Subrs", readIndexData(input));
        }
    }

    private String readString(final int index)
    {
        if (index >= 0 && index <= 390)
        {
            return CFFStandardString.getName(index);
        }
        if (index - 391 < stringIndex.length)
        {
            return stringIndex[index - 391];
        }
        else
        {
            // technically this maps to .notdef, but we need a unique sid name
            return "SID" + index;
        }
    }

    private String getString(final DictData dict, final String name)
    {
        final DictData.Entry entry = dict.getEntry(name);
        return entry != null ? readString(entry.getNumber(0).intValue()) : null;
    }

    private CFFEncoding readEncoding(final CFFDataInput dataInput, final CFFCharset charset) throws IOException
    {
        final int format = dataInput.readCard8();
        final int baseFormat = format & 0x7f;

        switch (baseFormat)
        {
            case 0:
                return readFormat0Encoding(dataInput, charset, format);
            case 1:
                return readFormat1Encoding(dataInput, charset, format);
            default:
                throw new IllegalArgumentException();
        }
    }

    private Format0Encoding readFormat0Encoding(final CFFDataInput dataInput, final CFFCharset charset,
                                                final int format) throws IOException
    {
        final Format0Encoding encoding = new Format0Encoding();
        encoding.format = format;
        encoding.nCodes = dataInput.readCard8();
        encoding.add(0, 0, ".notdef");
        for (int gid = 1; gid <= encoding.nCodes; gid++)
        {
            final int code = dataInput.readCard8();
            final int sid = charset.getSIDForGID(gid);
            encoding.add(code, sid, readString(sid));
        }
        if ((format & 0x80) != 0)
        {
            readSupplement(dataInput, encoding);
        }
        return encoding;
    }

    private Format1Encoding readFormat1Encoding(final CFFDataInput dataInput, final CFFCharset charset,
                                                final int format) throws IOException
    {
        final Format1Encoding encoding = new Format1Encoding();
        encoding.format = format;
        encoding.nRanges = dataInput.readCard8();
        encoding.add(0, 0, ".notdef");
        int gid = 1;
        for (int i = 0; i < encoding.nRanges; i++)
        {
            final int rangeFirst = dataInput.readCard8();
            final int rangeLeft = dataInput.readCard8();
            for (int j = 0; j < 1 + rangeLeft; j++)
            {
                final int sid = charset.getSIDForGID(gid);
                final int code = rangeFirst + j;
                encoding.add(code, sid, readString(sid));
                gid++;
            }
        }
        if ((format & 0x80) != 0)
        {
            readSupplement(dataInput, encoding);
        }
        return encoding;
    }

    private void readSupplement(final CFFDataInput dataInput, final CFFBuiltInEncoding encoding) throws IOException
    {
        encoding.nSups = dataInput.readCard8();
        encoding.supplement = new CFFBuiltInEncoding.Supplement[encoding.nSups];
        for (int i = 0; i < encoding.supplement.length; i++)
        {
            final CFFBuiltInEncoding.Supplement supplement = new CFFBuiltInEncoding.Supplement();
            supplement.code = dataInput.readCard8();
            supplement.sid = dataInput.readSID();
            supplement.name = readString(supplement.sid);
            encoding.supplement[i] = supplement;
            encoding.add(supplement.code, supplement.sid, readString(supplement.sid));
        }
    }

    /**
     * Read the FDSelect Data according to the format.
     * @param dataInput
     * @param nGlyphs
     * @param ros
     * @return the FDSelect data
     * @throws IOException
     */
    private static FDSelect readFDSelect(final CFFDataInput dataInput, final int nGlyphs, final CFFCIDFont ros) throws IOException
    {
        final int format = dataInput.readCard8();
        switch (format)
        {
            case 0:
                return readFormat0FDSelect(dataInput, format, nGlyphs, ros);
            case 3:
                return readFormat3FDSelect(dataInput, format, nGlyphs, ros);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Read the Format 0 of the FDSelect data structure.
     * @param dataInput
     * @param format
     * @param nGlyphs
     * @param ros
     * @return the Format 0 of the FDSelect data
     * @throws IOException
     */
    private static Format0FDSelect readFormat0FDSelect(final CFFDataInput dataInput, final int format, final int nGlyphs, final CFFCIDFont ros)
            throws IOException
    {
        final Format0FDSelect fdselect = new Format0FDSelect(ros);
        fdselect.format = format;
        fdselect.fds = new int[nGlyphs];
        for (int i = 0; i < fdselect.fds.length; i++)
        {
            fdselect.fds[i] = dataInput.readCard8();
        }
        return fdselect;
    }

    /**
     * Read the Format 3 of the FDSelect data structure.
     * 
     * @param dataInput
     * @param format
     * @param nGlyphs
     * @param ros
     * @return the Format 3 of the FDSelect data
     * @throws IOException
     */
    private static Format3FDSelect readFormat3FDSelect(final CFFDataInput dataInput, final int format, final int nGlyphs, final CFFCIDFont ros)
            throws IOException
    {
        final Format3FDSelect fdselect = new Format3FDSelect(ros);
        fdselect.format = format;
        fdselect.nbRanges = dataInput.readCard16();

        fdselect.range3 = new Range3[fdselect.nbRanges];
        for (int i = 0; i < fdselect.nbRanges; i++)
        {
            final Range3 r3 = new Range3();
            r3.first = dataInput.readCard16();
            r3.fd = dataInput.readCard8();
            fdselect.range3[i] = r3;

        }

        fdselect.sentinel = dataInput.readCard16();
        return fdselect;
    }

    /**
     *  Format 3 FDSelect data.
     */
    private static final class Format3FDSelect extends FDSelect
    {
        private int format;
        private int nbRanges;
        private Range3[] range3;
        private int sentinel;

        private Format3FDSelect(final CFFCIDFont owner)
        {
            super(owner);
        }

        @Override
        public int getFDIndex(final int gid)
        {
            for (int i = 0; i < nbRanges; ++i)
            {
                if (range3[i].first <= gid)
                {
                    if (i + 1 < nbRanges)
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
            return getClass().getName() + "[format=" + format + " nbRanges=" + nbRanges + ", range3="
                    + Arrays.toString(range3) + " sentinel=" + sentinel + "]";
        }
    }

    /**
     * Structure of a Range3 element.
     */
    private static final class Range3
    {
        private int first;
        private int fd;

        @Override
        public String toString()
        {
            return getClass().getName() + "[first=" + first + ", fd=" + fd + "]";
        }
    }

    /**
     *  Format 0 FDSelect.
     */
    private static class Format0FDSelect extends FDSelect
    {
        @SuppressWarnings("unused")
        private int format;
        private int[] fds;

        private Format0FDSelect(final CFFCIDFont owner)
        {
            super(owner);
        }

        @Override
        public int getFDIndex(final int gid)
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

    private CFFCharset readCharset(final CFFDataInput dataInput, final int nGlyphs, final boolean isCIDFont)
            throws IOException
    {
        final int format = dataInput.readCard8();
        switch (format)
        {
            case 0:
                return readFormat0Charset(dataInput, format, nGlyphs, isCIDFont);
            case 1:
                return readFormat1Charset(dataInput, format, nGlyphs, isCIDFont);
            case 2:
                return readFormat2Charset(dataInput, format, nGlyphs, isCIDFont);
            default:
                throw new IllegalArgumentException();
        }
    }

    private Format0Charset readFormat0Charset(final CFFDataInput dataInput, final int format, final int nGlyphs,
                                              final boolean isCIDFont) throws IOException
    {
        final Format0Charset charset = new Format0Charset(isCIDFont);
        charset.format = format;
        if (isCIDFont)
        {
            charset.addCID(0, 0);
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
        }

        for (int gid = 1; gid < nGlyphs; gid++)
        {
            final int sid = dataInput.readSID();
            if (isCIDFont)
            {
                charset.addCID(gid, sid);
            }
            else
            {
                charset.addSID(gid, sid, readString(sid));
            }
        }
        return charset;
    }

    private Format1Charset readFormat1Charset(final CFFDataInput dataInput, final int format, final int nGlyphs,
                                              final boolean isCIDFont) throws IOException
    {
        final Format1Charset charset = new Format1Charset(isCIDFont);
        charset.format = format;
        if (isCIDFont)
        {
            charset.addCID(0, 0);
            charset.rangesCID2GID = new ArrayList<>();
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
        }

        for (int gid = 1; gid < nGlyphs; gid++)
        {
            final int rangeFirst = dataInput.readSID();
            final int rangeLeft = dataInput.readCard8();
            if (!isCIDFont)
            {
                for (int j = 0; j < 1 + rangeLeft; j++)
                {
                    final int sid = rangeFirst + j;
                    charset.addSID(gid + j, sid, readString(sid));
                }
            }
            else
            {
                charset.rangesCID2GID.add(new RangeMapping(gid, rangeFirst, rangeLeft));
            }
            gid += rangeLeft;
        }
        return charset;
    }

    private Format2Charset readFormat2Charset(final CFFDataInput dataInput, final int format, final int nGlyphs,
                                              final boolean isCIDFont) throws IOException
    {
        final Format2Charset charset = new Format2Charset(isCIDFont);
        charset.format = format;
        if (isCIDFont)
        {
            charset.addCID(0, 0);
            charset.rangesCID2GID = new ArrayList<>();
        }
        else
        {
            charset.addSID(0, 0, ".notdef");
        }

        for (int gid = 1; gid < nGlyphs; gid++)
        {
            final int first = dataInput.readSID();
            final int nLeft = dataInput.readCard16();
            if (!isCIDFont)
            {
                for (int j = 0; j < 1 + nLeft; j++)
                {
                    final int sid = first + j;
                    charset.addSID(gid + j, sid, readString(sid));
                }
            }
            else
            {
                charset.rangesCID2GID.add(new RangeMapping(gid, first, nLeft));
            }
            gid += nLeft;
        }
        return charset;
    }

    /**
     * Inner class holding the header of a CFF font. 
     */
    private static class Header
    {
        private int major;
        private int minor;
        private int hdrSize;
        private int offSize;

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

        public void add(final Entry entry)
        {
            if (entry.operator != null)
            {
                entries.put(entry.operator.getName(), entry);
            }
        }
        
        public Entry getEntry(final String name)
        {
            return entries.get(name);
        }

        public Boolean getBoolean(final String name, final boolean defaultValue)
        {
            final Entry entry = getEntry(name);
            return entry != null && !entry.getArray().isEmpty() ? entry.getBoolean(0) : defaultValue;
        }

        public List<Number> getArray(final String name, final List<Number> defaultValue)
        {
            final Entry entry = getEntry(name);
            return entry != null && !entry.getArray().isEmpty() ? entry.getArray() : defaultValue;
        }

        public Number getNumber(final String name, final Number defaultValue)
        {
            final Entry entry = getEntry(name);
            return entry != null && !entry.getArray().isEmpty() ? entry.getNumber(0) : defaultValue;
        }

        public List<Number> getDelta(final String name, final List<Number> defaultValue)
        {
            final Entry entry = getEntry(name);
            return entry != null && !entry.getArray().isEmpty() ? entry.getDelta() : defaultValue;
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
            private CFFOperator operator = null;

            public Number getNumber(final int index)
            {
                return operands.get(index);
            }

            public Boolean getBoolean(final int index)
            {
                final Number operand = operands.get(index);
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
                throw new IllegalArgumentException();
            }

            public List<Number> getArray()
            {
                return operands;
            }

            public List<Number> getDelta()
            {
                final List<Number> result = new ArrayList<>(operands);
                for (int i = 1; i < result.size(); i++)
                {
                    final Number previous = result.get(i - 1);
                    final Number current = result.get(i);
                    final Integer sum = previous.intValue() + current.intValue();
                    result.set(i, sum);
                }
                return result;
            }

            @Override
            public String toString()
            {
                return getClass().getName() + "[operands=" + operands + ", operator=" + operator + "]";
            }
        }
    }

    /**
     * Inner class representing a font's built-in CFF encoding. 
     */
    abstract static class CFFBuiltInEncoding extends CFFEncoding
    {
        private int nSups;
        private Supplement[] supplement;

        /**
         * Inner class representing a supplement for an encoding. 
         */
        static class Supplement
        {
            private int code;
            private int sid;
            private String name;

            public int getCode()
            {
                return code;
            }

            public int getSID()
            {
                return sid;
            }

            public String getName()
            {
                return name;
            }

            @Override
            public String toString()
            {
                return getClass().getName() + "[code=" + code + ", sid=" + sid + "]";
            }
        }
    }

    /**
     * Inner class representing a Format0 encoding. 
     */
    private static class Format0Encoding extends CFFBuiltInEncoding
    {
        private int format;
        private int nCodes;

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", nCodes=" + nCodes
                    + ", supplement=" + Arrays.toString(super.supplement) + "]";
        }
    }

    /**
     * Inner class representing a Format1 encoding. 
     */
    private static class Format1Encoding extends CFFBuiltInEncoding
    {
        private int format;
        private int nRanges;

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", nRanges=" + nRanges
                    + ", supplement=" + Arrays.toString(super.supplement) + "]";
        }
    }

    /**
     * Inner class representing an embedded CFF charset.
     */
    abstract static class EmbeddedCharset extends CFFCharset
    {
        protected EmbeddedCharset(final boolean isCIDFont)
        {
            super(isCIDFont);
        }
    }

    /**
     * An empty charset in a malformed CID font.
     */
    private static class EmptyCharset extends EmbeddedCharset
    {
        protected EmptyCharset(final int numCharStrings)
        {
            super(true);
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
     * Inner class representing a Format0 charset. 
     */
    private static class Format0Charset extends EmbeddedCharset
    {
        private int format;

        protected Format0Charset(final boolean isCIDFont)
        {
            super(isCIDFont);
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + "]";
        }
    }

    /**
     * Inner class representing a Format1 charset. 
     */
    private static class Format1Charset extends EmbeddedCharset
    {
        private int format;
        private List<RangeMapping> rangesCID2GID;

        protected Format1Charset(final boolean isCIDFont)
        {
            super(isCIDFont);
        }

        @Override
        public int getCIDForGID(final int gid)
        {
            if (isCIDFont())
            {
                for (final RangeMapping mapping : rangesCID2GID)
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
        public int getGIDForCID(final int cid)
        {
            if (isCIDFont())
            {
                for (final RangeMapping mapping : rangesCID2GID)
                {
                    if (mapping.isInReverseRange(cid))
                    {
                        return mapping.mapReverseValue(cid);
                    }
                }
            }
            return super.getGIDForCID(cid);
        }
        
        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + "]";
        }
    }

    /**
     * Inner class representing a Format2 charset. 
     */
    private static class Format2Charset extends EmbeddedCharset
    {
        private int format;
        private List<RangeMapping> rangesCID2GID;
        
        protected Format2Charset(final boolean isCIDFont)
        {
            super(isCIDFont);
        }

        @Override
        public int getCIDForGID(final int gid)
        {
            for (final RangeMapping mapping : rangesCID2GID)
            {
                if (mapping.isInRange(gid))
                {
                    return mapping.mapValue(gid);
                }
            }
            return super.getCIDForGID(gid);
        }
        
        @Override
        public int getGIDForCID(final int cid)
        {
            for (final RangeMapping mapping : rangesCID2GID)
            {
                if (mapping.isInReverseRange(cid))
                {
                    return mapping.mapReverseValue(cid);
                }
            }
            return super.getGIDForCID(cid);
        }
        
        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + "]";
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

        private RangeMapping(final int startGID, final int first, final int nLeft)
        {
            this.startValue = startGID;
            endValue = startValue + nLeft;
            this.startMappedValue = first;
            endMappedValue = startMappedValue + nLeft;
        }
        
        boolean isInRange(final int value)
        {
            return value >= startValue && value <= endValue;
        }
        
        boolean isInReverseRange(final int value)
        {
            return value >= startMappedValue && value <= endMappedValue;
        }

        int mapValue(final int value)
        {
            if (isInRange(value))
            {
                return startMappedValue + (value - startValue);
            }
            else
            {
                return 0;
            }
        }

        int mapReverseValue(final int value)
        {
            if (isInReverseRange(value))
            {
                return startValue + (value - startMappedValue);
            }
            else
            {
                return 0;
            }
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[start value=" + startValue + ", end value=" + endValue +  ", start mapped-value=" + startMappedValue +  ", end mapped-value=" + endMappedValue +"]";
        }
    }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[" + debugFontName + "]";
    }
}
