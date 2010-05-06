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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.fontbox.cff.CFFOperator;
import org.apache.fontbox.cff.charset.CFFCharset;
import org.apache.fontbox.cff.charset.CFFExpertCharset;
import org.apache.fontbox.cff.charset.CFFExpertSubsetCharset;
import org.apache.fontbox.cff.charset.CFFISOAdobeCharset;
import org.apache.fontbox.cff.encoding.CFFEncoding;
import org.apache.fontbox.cff.encoding.CFFExpertEncoding;
import org.apache.fontbox.cff.encoding.CFFStandardEncoding;

/**
 * This class represents a parser for a CFF font. 
 * @author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class CFFParser
{

    private CFFDataInput input = null;
    private Header header = null;
    private IndexData nameIndex = null;
    private IndexData topDictIndex = null;
    private IndexData stringIndex = null;
    private IndexData globalSubrIndex = null;

    /**
     * Parsing CFF Font using a byte array as input.
     * @param bytes the given byte array
     * @return the parsed CFF fonts
     * @throws IOException If there is an error reading from the stream
     */
    public List<CFFFont> parse(byte[] bytes) throws IOException
    {
        input = new CFFDataInput(bytes);
        header = readHeader(input);
        nameIndex = readIndexData(input);
        topDictIndex = readIndexData(input);
        stringIndex = readIndexData(input);
        globalSubrIndex = readIndexData(input);

        List<CFFFont> fonts = new ArrayList<CFFFont>();
        for (int i = 0; i < nameIndex.count; i++)
        {
            CFFFont font = parseFont(i);
            fonts.add(font);
        }
        return fonts;
    }

    private static Header readHeader(CFFDataInput input) throws IOException
    {
        Header header = new Header();
        header.major = input.readCard8();
        header.minor = input.readCard8();
        header.hdrSize = input.readCard8();
        header.offSize = input.readOffSize();
        return header;
    }

    private static IndexData readIndexData(CFFDataInput input)
            throws IOException
    {
        IndexData index = new IndexData();
        index.count = input.readCard16();
        if (index.count == 0)
        {
            return index;
        }
        index.offSize = input.readOffSize();
        index.offset = new int[index.count + 1];
        for (int i = 0; i < index.offset.length; i++)
        {
            index.offset[i] = input.readOffset(index.offSize);
        }
        index.data = new int[index.offset[index.offset.length - 1]
                - index.offset[0]];
        for (int i = 0; i < index.data.length; i++)
        {
            index.data[i] = input.readCard8();
        }
        return index;
    }

    private static DictData readDictData(CFFDataInput input) throws IOException
    {
        DictData dict = new DictData();
        dict.entries = new ArrayList<DictData.Entry>();
        while (input.hasRemaining())
        {
            DictData.Entry entry = readEntry(input);
            dict.entries.add(entry);
        }
        return dict;
    }

    private static DictData.Entry readEntry(CFFDataInput input)
            throws IOException
    {
        DictData.Entry entry = new DictData.Entry();
        while (true)
        {
            int b0 = input.readUnsignedByte();

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
                throw new IllegalArgumentException();
            }
        }
        return entry;
    }

    private static CFFOperator readOperator(CFFDataInput input, int b0)
            throws IOException
    {
        CFFOperator.Key key = readOperatorKey(input, b0);
        return CFFOperator.getOperator(key);
    }

    private static CFFOperator.Key readOperatorKey(CFFDataInput input, int b0)
            throws IOException
    {
        if (b0 == 12)
        {
            int b1 = input.readUnsignedByte();
            return new CFFOperator.Key(b0, b1);
        }
        return new CFFOperator.Key(b0);
    }

    private static Integer readIntegerNumber(CFFDataInput input, int b0)
            throws IOException
    {
        if (b0 == 28)
        {
            int b1 = input.readUnsignedByte();
            int b2 = input.readUnsignedByte();
            return Integer.valueOf((short) (b1 << 8 | b2));
        } 
        else if (b0 == 29)
        {
            int b1 = input.readUnsignedByte();
            int b2 = input.readUnsignedByte();
            int b3 = input.readUnsignedByte();
            int b4 = input.readUnsignedByte();
            return Integer.valueOf(b1 << 24 | b2 << 16 | b3 << 8 | b4);
        } 
        else if (b0 >= 32 && b0 <= 246)
        {
            return Integer.valueOf(b0 - 139);
        } 
        else if (b0 >= 247 && b0 <= 250)
        {
            int b1 = input.readUnsignedByte();
            return Integer.valueOf((b0 - 247) * 256 + b1 + 108);
        } 
        else if (b0 >= 251 && b0 <= 254)
        {
            int b1 = input.readUnsignedByte();
            return Integer.valueOf(-(b0 - 251) * 256 - b1 - 108);
        } 
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private static Double readRealNumber(CFFDataInput input, int b0)
            throws IOException
    {
        StringBuffer sb = new StringBuffer();
        boolean done = false;
        while (!done)
        {
            int b = input.readUnsignedByte();
            int[] nibbles = { b / 16, b % 16 };
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
                    break;
                case 0xa:
                    sb.append(".");
                    break;
                case 0xb:
                    sb.append("E");
                    break;
                case 0xc:
                    sb.append("E-");
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
        return Double.valueOf(sb.toString());
    }

    private CFFFont parseFont(int index) throws IOException
    {
        CFFFont font = new CFFFont();
        DataInput nameInput = new DataInput(nameIndex.getBytes(index));
        String name = nameInput.getString();
        font.setName(name);
        CFFDataInput topDictInput = new CFFDataInput(topDictIndex
                .getBytes(index));
        DictData topDict = readDictData(topDictInput);
        DictData.Entry syntheticBaseEntry = topDict.getEntry("SyntheticBase");
        if (syntheticBaseEntry != null)
        {
            throw new IOException("Synthetic Fonts are not supported");
        }
        DictData.Entry rosEntry = topDict.getEntry("ROS");
        if (rosEntry != null)
        {
            throw new IOException("CID-keyed Fonts are not supported");
        }
        font.addValueToTopDict("version", getString(topDict,"version"));
        font.addValueToTopDict("Notice", getString(topDict,"Notice"));
        font.addValueToTopDict("Copyright", getString(topDict,"Copyright"));
        font.addValueToTopDict("FullName", getString(topDict,"FullName"));
        font.addValueToTopDict("FamilyName", getString(topDict,"FamilyName"));
        font.addValueToTopDict("Weight", getString(topDict,"Weight"));
        font.addValueToTopDict("isFixedPitch", getBoolean(topDict, "isFixedPitch", false));
        font.addValueToTopDict("ItalicAngle", getNumber(topDict, "ItalicAngle", 0));
        font.addValueToTopDict("UnderlinePosition", getNumber(topDict, "UnderlinePosition", -100));
        font.addValueToTopDict("UnderlineThickness", getNumber(topDict, "UnderlineThickness", 50));
        font.addValueToTopDict("PaintType", getNumber(topDict, "PaintType", 0));
        font.addValueToTopDict("CharstringType", getNumber(topDict, "CharstringType", 2));
        font.addValueToTopDict("FontMatrix", getArray(topDict, "FontMatrix", Arrays
                .<Number> asList(Double.valueOf(0.001), Double.valueOf(0),
                        Double.valueOf(0), Double.valueOf(0.001), Double
                                .valueOf(0), Double.valueOf(0))));
        font.addValueToTopDict("UniqueID", getNumber(topDict, "UniqueID", null));
        font.addValueToTopDict("FontBBox", getArray(topDict, "FontBBox", Arrays
                .<Number> asList(Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0), Integer.valueOf(0))));
        font.addValueToTopDict("StrokeWidth", getNumber(topDict, "StrokeWidth", 0));
        font.addValueToTopDict("XUID", getArray(topDict, "XUID", null));
        DictData.Entry charStringsEntry = topDict.getEntry("CharStrings");
        int charStringsOffset = charStringsEntry.getNumber(0).intValue();
        input.setPosition(charStringsOffset);
        IndexData charStringsIndex = readIndexData(input);
        DictData.Entry charsetEntry = topDict.getEntry("charset");
        CFFCharset charset;
        int charsetId = charsetEntry != null ? charsetEntry.getNumber(0)
                .intValue() : 0;
        if (charsetId == 0)
        {
            charset = CFFISOAdobeCharset.getInstance();
        } 
        else if (charsetId == 1)
        {
            charset = CFFExpertCharset.getInstance();
        } 
        else if (charsetId == 2)
        {
            charset = CFFExpertSubsetCharset.getInstance();
        } 
        else
        {
            input.setPosition(charsetId);
            charset = readCharset(input, charStringsIndex.count);
        }
        font.setCharset(charset);
        font.getCharStringsDict().put(".notdef", charStringsIndex.getBytes(0));
        int[] gids = new int[charStringsIndex.count];
        List<CFFCharset.Entry> glyphEntries = charset.getEntries();
        for (int i = 1; i < charStringsIndex.count; i++)
        {
            CFFCharset.Entry glyphEntry = glyphEntries.get(i - 1);
            gids[i - 1] = glyphEntry.getSID();
            font.getCharStringsDict().put(glyphEntry.getName(), charStringsIndex.getBytes(i));
        }
        DictData.Entry encodingEntry = topDict.getEntry("Encoding");
        CFFEncoding encoding;
        int encodingId = encodingEntry != null ? encodingEntry.getNumber(0)
                .intValue() : 0;
        if (encodingId == 0)
        {
            encoding = CFFStandardEncoding.getInstance();
        } 
        else if (encodingId == 1)
        {
            encoding = CFFExpertEncoding.getInstance();
        } 
        else
        {
            input.setPosition(encodingId);
            encoding = readEncoding(input, gids);
        }
        font.setEncoding(encoding);
        DictData.Entry privateEntry = topDict.getEntry("Private");
        int privateOffset = privateEntry.getNumber(1).intValue();
        input.setPosition(privateOffset);
        int privateSize = privateEntry.getNumber(0).intValue();
        CFFDataInput privateDictData = new CFFDataInput(input.readBytes(privateSize));
        DictData privateDict = readDictData(privateDictData);
        font.addValueToPrivateDict("BlueValues", getDelta(privateDict, "BlueValues", null));
        font.addValueToPrivateDict("OtherBlues", getDelta(privateDict, "OtherBlues", null));
        font.addValueToPrivateDict("FamilyBlues", getDelta(privateDict, "FamilyBlues", null));
        font.addValueToPrivateDict("FamilyOtherBlues", getDelta(privateDict, "FamilyOtherBlues", null));
        font.addValueToPrivateDict("BlueScale", getNumber(privateDict, "BlueScale", Double.valueOf(0.039625)));
        font.addValueToPrivateDict("BlueShift", getNumber(privateDict, "BlueShift", Integer.valueOf(7)));
        font.addValueToPrivateDict("BlueFuzz", getNumber(privateDict, "BlueFuzz", Integer.valueOf(1)));
        font.addValueToPrivateDict("StdHW", getNumber(privateDict, "StdHW", null));
        font.addValueToPrivateDict("StdVW", getNumber(privateDict, "StdVW", null));
        font.addValueToPrivateDict("StemSnapH", getDelta(privateDict, "StemSnapH", null));
        font.addValueToPrivateDict("StemSnapV", getDelta(privateDict, "StemSnapV", null));
        font.addValueToPrivateDict("ForceBold", getBoolean(privateDict, "ForceBold", false));
        font.addValueToPrivateDict("LanguageGroup", getNumber(privateDict, "LanguageGroup", Integer.valueOf(0)));
        font.addValueToPrivateDict("ExpansionFactor", getNumber(privateDict, "ExpansionFactor", Double.valueOf(0.06)));
        font.addValueToPrivateDict("initialRandomSeed", getNumber(privateDict, "initialRandomSeed", Integer.valueOf(0)));
        font.addValueToPrivateDict("defaultWidthX", getNumber(privateDict, "defaultWidthX", Integer.valueOf(0)));
        font.addValueToPrivateDict("nominalWidthX", getNumber(privateDict, "nominalWidthX", Integer.valueOf(0)));
        return font;
    }

    private String readString(int index) throws IOException
    {
        if (index >= 0 && index <= 390)
        {
            return CFFStandardString.getName(index);
        }
        DataInput dataInput = new DataInput(stringIndex.getBytes(index - 391));
        return dataInput.getString();
    }

    private String getString(DictData dict, String name) throws IOException
    {
        DictData.Entry entry = dict.getEntry(name);
        return (entry != null ? readString(entry.getNumber(0).intValue()) : null);
    }

    private Boolean getBoolean(DictData dict, String name, boolean defaultValue) throws IOException
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? entry.getBoolean(0) : defaultValue;
    }

    private Number getNumber(DictData dict, String name, Number defaultValue) throws IOException
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? entry.getNumber(0) : defaultValue;
    }

    // TODO Where is the difference to getDelta??
    private List<Number> getArray(DictData dict, String name, List<Number> defaultValue) throws IOException
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? entry.getArray() : defaultValue;
    }

    // TODO Where is the difference to getArray??
    private List<Number> getDelta(DictData dict, String name, List<Number> defaultValue) throws IOException
    {
        DictData.Entry entry = dict.getEntry(name);
        return entry != null ? entry.getArray() : defaultValue;
    }

    private CFFEncoding readEncoding(CFFDataInput dataInput, int[] gids)
            throws IOException
    {
        int format = dataInput.readCard8();
        int baseFormat = format & 0x7f;

        if (baseFormat == 0)
        {
            return readFormat0Encoding(dataInput, format, gids);
        } 
        else if (baseFormat == 1)
        {
            return readFormat1Encoding(dataInput, format, gids);
        } 
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private Format0Encoding readFormat0Encoding(CFFDataInput dataInput, int format,
            int[] gids) throws IOException
    {
        Format0Encoding encoding = new Format0Encoding();
        encoding.format = format;
        encoding.nCodes = dataInput.readCard8();
        encoding.code = new int[encoding.nCodes];
        for (int i = 0; i < encoding.code.length; i++)
        {
            encoding.code[i] = dataInput.readCard8();
            encoding.register(encoding.code[i], gids[i]);
        }
        if ((format & 0x80) != 0)
        {
            readSupplement(dataInput, encoding);
        }
        return encoding;
    }

    private Format1Encoding readFormat1Encoding(CFFDataInput dataInput, int format,
            int[] gids) throws IOException
    {
        Format1Encoding encoding = new Format1Encoding();
        encoding.format = format;
        encoding.nRanges = dataInput.readCard8();
        int count = 0;
        encoding.range = new Format1Encoding.Range1[encoding.nRanges];
        for (int i = 0; i < encoding.range.length; i++)
        {
            Format1Encoding.Range1 range = new Format1Encoding.Range1();
            range.first = dataInput.readCard8();
            range.nLeft = dataInput.readCard8();
            encoding.range[i] = range;
            for (int j = 0; j < 1 + range.nLeft; j++)
            {
                encoding.register(range.first + j, gids[count + j]);
            }
            count += 1 + range.nLeft;
        }
        if ((format & 0x80) != 0)
        {
            readSupplement(dataInput, encoding);
        }
        return encoding;
    }

    private void readSupplement(CFFDataInput dataInput, EmbeddedEncoding encoding)
            throws IOException
    {
        encoding.nSups = dataInput.readCard8();
        encoding.supplement = new EmbeddedEncoding.Supplement[encoding.nSups];
        for (int i = 0; i < encoding.supplement.length; i++)
        {
            EmbeddedEncoding.Supplement supplement = new EmbeddedEncoding.Supplement();
            supplement.code = dataInput.readCard8();
            supplement.glyph = dataInput.readSID();
            encoding.supplement[i] = supplement;
        }
    }

    private CFFCharset readCharset(CFFDataInput dataInput, int nGlyphs)
            throws IOException
    {
        int format = dataInput.readCard8();
        if (format == 0)
        {
            return readFormat0Charset(dataInput, format, nGlyphs);
        } 
        else if (format == 1)
        {
            return readFormat1Charset(dataInput, format, nGlyphs);
        } 
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private Format0Charset readFormat0Charset(CFFDataInput dataInput, int format,
            int nGlyphs) throws IOException
    {
        Format0Charset charset = new Format0Charset();
        charset.format = format;
        charset.glyph = new int[nGlyphs - 1];
        for (int i = 0; i < charset.glyph.length; i++)
        {
            charset.glyph[i] = dataInput.readSID();
            charset.register(charset.glyph[i], readString(charset.glyph[i]));
        }
        return charset;
    }

    private Format1Charset readFormat1Charset(CFFDataInput dataInput, int format,
            int nGlyphs) throws IOException
    {
        Format1Charset charset = new Format1Charset();
        charset.format = format;
        charset.range = new Format1Charset.Range1[0];
        for (int i = 0; i < nGlyphs - 1;)
        {
            Format1Charset.Range1[] newRange = new Format1Charset.Range1[charset.range.length + 1];
            System.arraycopy(charset.range, 0, newRange, 0,
                    charset.range.length);
            charset.range = newRange;
            Format1Charset.Range1 range = new Format1Charset.Range1();
            range.first = dataInput.readSID();
            range.nLeft = dataInput.readCard8();
            charset.range[charset.range.length - 1] = range;
            for (int j = 0; j < 1 + range.nLeft; j++)
            {
                charset.register(range.first + j, readString(range.first + j));
            }
            i += 1 + range.nLeft;
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
            return getClass().getName() + "[major=" + major + ", minor="
                    + minor + ", hdrSize=" + hdrSize + ", offSize=" + offSize
                    + "]";
        }
    }

    /**
     * Inner class holding the IndexData of a CFF font. 
     */
    private static class IndexData
    {
        private int count;
        private int offSize;
        private int[] offset;
        private int[] data;

        public byte[] getBytes(int index)
        {
            int length = offset[index + 1] - offset[index];
            byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++)
            {
                bytes[i] = (byte) data[offset[index] - 1 + i];
            }
            return bytes;
        }

        @Override
        public String toString()
        {
            return getClass().getName() + "[count=" + count + ", offSize="
                    + offSize + ", offset=" + Arrays.toString(offset)
                    + ", data=" + Arrays.toString(data) + "]";
        }
    }

    /**
     * Inner class holding the DictData of a CFF font. 
     */
    private static class DictData
    {

        private List<Entry> entries = null;

        public Entry getEntry(CFFOperator.Key key)
        {
            return getEntry(CFFOperator.getOperator(key));
        }

        public Entry getEntry(String name)
        {
            return getEntry(CFFOperator.getOperator(name));
        }

        private Entry getEntry(CFFOperator operator)
        {
            for (Entry entry : entries)
            {
            	// Check for null entry before comparing the Font
                if (entry != null && entry.operator != null && 
                    entry.operator.equals(operator))
                {
                    return entry;
                }
            }
            return null;
        }

        /**
         * {@inheritDoc} 
         */
        public String toString()
        {
            return getClass().getName() + "[entries=" + entries + "]";
        }

        /**
         * Inner class holding an operand of a CFF font. 
         */
        private static class Entry
        {
            private List<Number> operands = new ArrayList<Number>();
            private CFFOperator operator = null;

            public Number getNumber(int index)
            {
                return operands.get(index);
            }

            public Boolean getBoolean(int index)
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
                throw new IllegalArgumentException();
            }

            // TODO unused??
            public Integer getSID(int index)
            {
                Number operand = operands.get(index);
                if (operand instanceof Integer)
                {
                    return (Integer) operand;
                }
                throw new IllegalArgumentException();
            }

            // TODO Where is the difference to getDelta??
            public List<Number> getArray()
            {
                return operands;
            }

            // TODO Where is the difference to getArray??
            public List<Number> getDelta()
            {
                return operands;
            }

            @Override
            public String toString()
            {
                return getClass().getName() + "[operands=" + operands
                        + ", operator=" + operator + "]";
            }
        }
    }

    /**
     * Inner class representing an embedded CFF encoding. 
     */
    abstract static class EmbeddedEncoding extends CFFEncoding
    {

        private int nSups;
        private Supplement[] supplement;

        @Override
        public boolean isFontSpecific()
        {
            return true;
        }

        List<Supplement> getSupplements()
        {
            if(supplement == null){
                return Collections.<Supplement>emptyList();
            }
            return Arrays.asList(supplement);
        }

        /**
         * Inner class representing a supplement for an encoding. 
         */
        static class Supplement
        {
            private int code;
            private int glyph;

            int getCode(){
                return code;
            }

            int getGlyph(){
                return glyph;
            }

            @Override
            public String toString()
            {
                return getClass().getName() + "[code=" + code + ", glyph="
                        + glyph + "]";
            }
        }
    }

    /**
     * Inner class representing a Format0 encoding. 
     */
    private static class Format0Encoding extends EmbeddedEncoding
    {
        private int format;
        private int nCodes;
        private int[] code;

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", nCodes="
                    + nCodes + ", code=" + Arrays.toString(code)
                    + ", supplement=" + Arrays.toString(super.supplement) + "]";
        }
    }

    /**
     * Inner class representing a Format1 encoding. 
     */
    private static class Format1Encoding extends EmbeddedEncoding
    {
        private int format;
        private int nRanges;
        private Range1[] range;

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", nRanges="
                    + nRanges + ", range=" + Arrays.toString(range)
                    + ", supplement=" + Arrays.toString(super.supplement) + "]";
        }

        /**
         * Inner class representing a range of an encoding. 
         */
        private static class Range1
        {
            private int first;
            private int nLeft;

            @Override
            public String toString()
            {
                return getClass().getName() + "[first=" + first + ", nLeft="
                        + nLeft + "]";
            }
        }
    }

    /**
     * Inner class representing an embedded CFF charset. 
     */
    abstract static class EmbeddedCharset extends CFFCharset
    {
        @Override
        public boolean isFontSpecific()
        {
            return true;
        }
    }

    /**
     * Inner class representing a Format0 charset. 
     */
    private static class Format0Charset extends EmbeddedCharset
    {
        private int format;
        private int[] glyph;

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", glyph="
                    + Arrays.toString(glyph) + "]";
        }
    }

    /**
     * Inner class representing a Format1 charset. 
     */
    private static class Format1Charset extends EmbeddedCharset
    {
        private int format;
        private Range1[] range;

        @Override
        public String toString()
        {
            return getClass().getName() + "[format=" + format + ", range="
                    + Arrays.toString(range) + "]";
        }

        /**
         * Inner class representing a range of a charset. 
         */
        private static class Range1
        {
            private int first;
            private int nLeft;

            @Override
            public String toString()
            {
                return getClass().getName() + "[first=" + first + ", nLeft="
                        + nLeft + "]";
            }
        }
    }
}