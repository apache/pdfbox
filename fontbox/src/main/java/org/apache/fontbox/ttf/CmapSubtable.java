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
package org.apache.fontbox.ttf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A "cmap" subtable.
 * 
 * @author Ben Litchfield
 */
public class CmapSubtable implements CmapLookup
{
    private static final Log LOG = LogFactory.getLog(CmapSubtable.class);

    private static final long LEAD_OFFSET = 0xD800l - (0x10000 >> 10);
    private static final long SURROGATE_OFFSET = 0x10000l - (0xD800 << 10) - 0xDC00;

    private int platformId;
    private int platformEncodingId;
    private long subTableOffset;
    private int[] glyphIdToCharacterCode;
    private final Map<Integer, List<Integer>> glyphIdToCharacterCodeMultiple = new HashMap<>();
    private Map<Integer, Integer> characterCodeToGlyphId = new HashMap<>();

    /**
     * This will read the required data from the stream.
     * 
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    void initData(TTFDataStream data) throws IOException
    {
        platformId = data.readUnsignedShort();
        platformEncodingId = data.readUnsignedShort();
        subTableOffset = data.readUnsignedInt();
    }

    /**
     * This will read the required data from the stream.
     * 
     * @param cmap the CMAP this encoding belongs to.
     * @param numGlyphs number of glyphs.
     * @param data The stream to read the data from.
     * @throws IOException If there is an error reading the data.
     */
    void initSubtable(CmapTable cmap, int numGlyphs, TTFDataStream data) throws IOException
    {
        data.seek(cmap.getOffset() + subTableOffset);
        int subtableFormat = data.readUnsignedShort();
        long length;
        long version;
        if (subtableFormat < 8)
        {
            length = data.readUnsignedShort();
            version = data.readUnsignedShort();
        }
        else
        {
            // read an other UnsignedShort to read a Fixed32
            data.readUnsignedShort();
            length = data.readUnsignedInt();
            version = data.readUnsignedInt();
        }

        switch (subtableFormat)
        {
        case 0:
            processSubtype0(data);
            break;
        case 2:
            processSubtype2(data, numGlyphs);
            break;
        case 4:
            processSubtype4(data, numGlyphs);
            break;
        case 6:
            processSubtype6(data, numGlyphs);
            break;
        case 8:
            processSubtype8(data, numGlyphs);
            break;
        case 10:
            processSubtype10(data, numGlyphs);
            break;
        case 12:
            processSubtype12(data, numGlyphs);
            break;
        case 13:
            processSubtype13(data, numGlyphs);
            break;
        case 14:
            processSubtype14(data, numGlyphs);
            break;
        default:
            throw new IOException("Unknown cmap format:" + subtableFormat);
        }
    }

    /**
     * Reads a format 8 subtable.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype8(TTFDataStream data, int numGlyphs) throws IOException
    {
        // --- is32 is a 65536 BITS array ( = 8192 BYTES)
        int[] is32 = data.readUnsignedByteArray(8192);
        long nbGroups = data.readUnsignedInt();

        // --- nbGroups shouldn't be greater than 65536
        if (nbGroups > 65536)
        {
            throw new IOException("CMap ( Subtype8 ) is invalid");
        }

        glyphIdToCharacterCode = newGlyphIdToCharacterCode(numGlyphs);
        characterCodeToGlyphId = new HashMap<>(numGlyphs);
        if (numGlyphs == 0)
        {
            LOG.warn("subtable has no glyphs");
            return;
        }
        // -- Read all sub header
        for (long i = 0; i < nbGroups; ++i)
        {
            long firstCode = data.readUnsignedInt();
            long endCode = data.readUnsignedInt();
            long startGlyph = data.readUnsignedInt();

            // -- process simple validation
            if (firstCode > endCode || 0 > firstCode)
            {
                throw new IOException("Range invalid");
            }

            for (long j = firstCode; j <= endCode; ++j)
            {
                // -- Convert the Character code in decimal
                if (j > Integer.MAX_VALUE)
                {
                    throw new IOException("[Sub Format 8] Invalid character code " + j);
                }
                if ((int) j / 8 >= is32.length)
                {
                    throw new IOException("[Sub Format 8] Invalid character code " + j);
                }

                int currentCharCode;
                if ((is32[(int) j / 8] & (1 << ((int) j % 8))) == 0)
                {
                    currentCharCode = (int) j;
                }
                else
                {
                    // the character code uses a 32bits format
                    // convert it in decimal : see http://www.unicode.org/faq//utf_bom.html#utf16-4
                    long lead = LEAD_OFFSET + (j >> 10);
                    long trail = 0xDC00 + (j & 0x3FF);

                    long codepoint = (lead << 10) + trail + SURROGATE_OFFSET;
                    if (codepoint > Integer.MAX_VALUE)
                    {
                        throw new IOException("[Sub Format 8] Invalid character code " + codepoint);
                    }
                    currentCharCode = (int) codepoint;
                }

                long glyphIndex = startGlyph + (j - firstCode);
                if (glyphIndex > numGlyphs || glyphIndex > Integer.MAX_VALUE)
                {
                    throw new IOException("CMap contains an invalid glyph index");
                }

                glyphIdToCharacterCode[(int) glyphIndex] = currentCharCode;
                characterCodeToGlyphId.put(currentCharCode, (int) glyphIndex);
            }
        }
    }

    /**
     * Reads a format 10 subtable.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype10(TTFDataStream data, int numGlyphs) throws IOException
    {
        long startCode = data.readUnsignedInt();
        long numChars = data.readUnsignedInt();
        if (numChars > Integer.MAX_VALUE)
        {
            throw new IOException("Invalid number of Characters");
        }

        if (startCode < 0 || startCode > 0x0010FFFF || (startCode + numChars) > 0x0010FFFF
                || ((startCode + numChars) >= 0x0000D800 && (startCode + numChars) <= 0x0000DFFF))
        {
            throw new IOException("Invalid character codes, " + 
                    String.format("startCode: 0x%X, numChars: %d", startCode, numChars));

        }
    }

    /**
     * Reads a format 12 subtable.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype12(TTFDataStream data, int numGlyphs) throws IOException
    {
        int maxGlyphId = 0;
        long nbGroups = data.readUnsignedInt();
        glyphIdToCharacterCode = newGlyphIdToCharacterCode(numGlyphs);
        characterCodeToGlyphId = new HashMap<>(numGlyphs);
        if (numGlyphs == 0)
        {
            LOG.warn("subtable has no glyphs");
            return;
        }
        for (long i = 0; i < nbGroups; ++i)
        {
            long firstCode = data.readUnsignedInt();
            long endCode = data.readUnsignedInt();
            long startGlyph = data.readUnsignedInt();

            if (firstCode < 0 || firstCode > 0x0010FFFF ||
                firstCode >= 0x0000D800 && firstCode <= 0x0000DFFF)
            {
                throw new IOException("Invalid character code " + String.format("0x%X", firstCode));
            }

            if (endCode > 0 && endCode < firstCode ||
                endCode > 0x0010FFFF ||
                endCode >= 0x0000D800 && endCode <= 0x0000DFFF)
            {
                throw new IOException("Invalid character code " + String.format("0x%X", endCode));
            }

            for (long j = 0; j <= endCode - firstCode; ++j)
            {
                long glyphIndex = startGlyph + j;
                if (glyphIndex >= numGlyphs)
                {
                    LOG.warn("Format 12 cmap contains an invalid glyph index");
                    break;
                }

                if (firstCode + j > 0x10FFFF)
                {
                    LOG.warn("Format 12 cmap contains character beyond UCS-4");
                }

                maxGlyphId = Math.max(maxGlyphId, (int) glyphIndex);
                characterCodeToGlyphId.put((int) (firstCode + j), (int) glyphIndex);
            }
        }
        buildGlyphIdToCharacterCodeLookup(maxGlyphId);
    }

    /**
     * Reads a format 13 subtable.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype13(TTFDataStream data, int numGlyphs) throws IOException
    {
        long nbGroups = data.readUnsignedInt();
        glyphIdToCharacterCode = newGlyphIdToCharacterCode(numGlyphs);
        characterCodeToGlyphId = new HashMap<>(numGlyphs);
        if (numGlyphs == 0)
        {
            LOG.warn("subtable has no glyphs");
            return;
        }
        for (long i = 0; i < nbGroups; ++i)
        {
            long firstCode = data.readUnsignedInt();
            long endCode = data.readUnsignedInt();
            long glyphId = data.readUnsignedInt();

            if (glyphId > numGlyphs)
            {
                LOG.warn("Format 13 cmap contains an invalid glyph index");
                break;
            }

            if (firstCode < 0 || firstCode > 0x0010FFFF || (firstCode >= 0x0000D800 && firstCode <= 0x0000DFFF))
            {
                throw new IOException("Invalid character code " + String.format("0x%X", firstCode));
            }

            if ((endCode > 0 && endCode < firstCode) || endCode > 0x0010FFFF
                    || (endCode >= 0x0000D800 && endCode <= 0x0000DFFF))
            {
                throw new IOException("Invalid character code " + String.format("0x%X", endCode));
            }

            for (long j = 0; j <= endCode - firstCode; ++j)
            {
                if (firstCode + j > Integer.MAX_VALUE)
                {
                    throw new IOException("Character Code greater than Integer.MAX_VALUE");
                }

                if (firstCode + j > 0x10FFFF)
                {
                    LOG.warn("Format 13 cmap contains character beyond UCS-4");
                }

                glyphIdToCharacterCode[(int) glyphId] = (int) (firstCode + j);
                characterCodeToGlyphId.put((int) (firstCode + j), (int) glyphId);
            }
        }
    }

    /**
     * Reads a format 14 subtable.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype14(TTFDataStream data, int numGlyphs) throws IOException
    {
        // Unicode Variation Sequences (UVS)
        // see http://blogs.adobe.com/CCJKType/2013/05/opentype-cmap-table-ramblings.html
        LOG.warn("Format 14 cmap table is not supported and will be ignored");
    }

    /**
     * Reads a format 6 subtable.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype6(TTFDataStream data, int numGlyphs) throws IOException
    {
        int firstCode = data.readUnsignedShort();
        int entryCount = data.readUnsignedShort();
        // skip empty tables
        if (entryCount == 0)
        {
            return;
        }
        characterCodeToGlyphId = new HashMap<>(numGlyphs);
        int[] glyphIdArray = data.readUnsignedShortArray(entryCount);
        int maxGlyphId = 0;
        for (int i = 0; i < entryCount; i++)
        {
            maxGlyphId = Math.max(maxGlyphId, glyphIdArray[i]);
            characterCodeToGlyphId.put(firstCode + i, glyphIdArray[i]);
        }
        buildGlyphIdToCharacterCodeLookup(maxGlyphId);
    }

    /**
     * Reads a format 4 subtable.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype4(TTFDataStream data, int numGlyphs) throws IOException
    {
        int segCountX2 = data.readUnsignedShort();
        int segCount = segCountX2 / 2;
        int searchRange = data.readUnsignedShort();
        int entrySelector = data.readUnsignedShort();
        int rangeShift = data.readUnsignedShort();
        int[] endCount = data.readUnsignedShortArray(segCount);
        int reservedPad = data.readUnsignedShort();
        int[] startCount = data.readUnsignedShortArray(segCount);
        int[] idDelta = data.readUnsignedShortArray(segCount);
        long idRangeOffsetPosition = data.getCurrentPosition();
        int[] idRangeOffset = data.readUnsignedShortArray(segCount);

        characterCodeToGlyphId = new HashMap<>(numGlyphs);
        int maxGlyphId = 0;

        for (int i = 0; i < segCount; i++)
        {
            int start = startCount[i];
            int end = endCount[i];
            int delta = idDelta[i];
            int rangeOffset = idRangeOffset[i];
            long segmentRangeOffset = idRangeOffsetPosition + (i * 2L) + rangeOffset;
            if (start != 65535 && end != 65535)
            {
                for (int j = start; j <= end; j++)
                {
                    if (rangeOffset == 0)
                    {
                        int glyphid = (j + delta) & 0xFFFF;
                        maxGlyphId = Math.max(glyphid, maxGlyphId);
                        characterCodeToGlyphId.put(j, glyphid);
                    }
                    else
                    {
                        long glyphOffset = segmentRangeOffset + ((j - start) * 2L);
                        data.seek(glyphOffset);
                        int glyphIndex = data.readUnsignedShort();
                        if (glyphIndex != 0)
                        {
                            glyphIndex = (glyphIndex + delta) & 0xFFFF;
                            maxGlyphId = Math.max(glyphIndex, maxGlyphId);
                            characterCodeToGlyphId.put(j, glyphIndex);
                        }
                    }
                }
            }
        }

        /*
         * this is the final result key=glyphId, value is character codes Create an array that contains MAX(GlyphIds)
         * element, or -1
         */
        if (characterCodeToGlyphId.isEmpty())
        {
            LOG.warn("cmap format 4 subtable is empty");
            return;
        }
        buildGlyphIdToCharacterCodeLookup(maxGlyphId);
    }

    private void buildGlyphIdToCharacterCodeLookup(int maxGlyphId)
    {
        glyphIdToCharacterCode = newGlyphIdToCharacterCode(maxGlyphId + 1);
        characterCodeToGlyphId.forEach((key, value) ->
        {
            if (glyphIdToCharacterCode[value] == -1)
            {
                // add new value to the array
                glyphIdToCharacterCode[value] = key;
            }
            else
            {
                // there is already a mapping for the given glyphId
                List<Integer> mappedValues = glyphIdToCharacterCodeMultiple.get(value);
                if (mappedValues == null)
                {
                    mappedValues = new ArrayList<>();
                    glyphIdToCharacterCodeMultiple.put(value, mappedValues);
                    mappedValues.add(glyphIdToCharacterCode[value]);
                    // mark value as multiple mapping
                    glyphIdToCharacterCode[value] = Integer.MIN_VALUE;
                }
                mappedValues.add(key);
            }
        });
    }

    /**
     * Read a format 2 subtable.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @param numGlyphs number of glyphs to be read
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype2(TTFDataStream data, int numGlyphs) throws IOException
    {
        int[] subHeaderKeys = new int[256];
        // ---- keep the Max Index of the SubHeader array to know its length
        int maxSubHeaderIndex = 0;
        for (int i = 0; i < 256; i++)
        {
            subHeaderKeys[i] = data.readUnsignedShort();
            maxSubHeaderIndex = Math.max(maxSubHeaderIndex, subHeaderKeys[i] / 8);
        }

        // ---- Read all SubHeaders to avoid useless seek on DataSource
        SubHeader[] subHeaders = new SubHeader[maxSubHeaderIndex + 1];
        for (int i = 0; i <= maxSubHeaderIndex; ++i)
        {
            int firstCode = data.readUnsignedShort();
            int entryCount = data.readUnsignedShort();
            short idDelta = data.readSignedShort();
            int idRangeOffset = data.readUnsignedShort() - (maxSubHeaderIndex + 1 - i - 1) * 8 - 2;
            subHeaders[i] = new SubHeader(firstCode, entryCount, idDelta, idRangeOffset);
        }
        long startGlyphIndexOffset = data.getCurrentPosition();
        glyphIdToCharacterCode = newGlyphIdToCharacterCode(numGlyphs);
        characterCodeToGlyphId = new HashMap<>(numGlyphs);
        if (numGlyphs == 0)
        {
            LOG.warn("subtable has no glyphs");
            return;
        }
        for (int i = 0; i <= maxSubHeaderIndex; ++i)
        {
            SubHeader sh = subHeaders[i];
            int firstCode = sh.getFirstCode();
            int idRangeOffset = sh.getIdRangeOffset();
            int idDelta = sh.getIdDelta();
            int entryCount = sh.getEntryCount();
            data.seek(startGlyphIndexOffset + idRangeOffset);
            for (int j = 0; j < entryCount; ++j)
            {
                // ---- compute the Character Code
                int charCode = i;
                charCode = (charCode << 8) + (firstCode + j);

                // ---- Go to the CharacterCOde position in the Sub Array
                // of the glyphIndexArray
                // glyphIndexArray contains Unsigned Short so add (j * 2) bytes
                // at the index position
                int p = data.readUnsignedShort();
                // ---- compute the glyphIndex
                if (p > 0)
                {
                    p = (p + idDelta) % 65536;
                    if (p < 0)
                    {
                        p += 65536;
                    }
                }
                
                if (p >= numGlyphs)
                {
                    LOG.warn("glyphId " + p + " for charcode " + charCode + " ignored, numGlyphs is " + numGlyphs);
                    continue;
                }
                
                glyphIdToCharacterCode[p] = charCode;
                characterCodeToGlyphId.put(charCode, p);
            }
        }
    }

    /**
     * Initialize the CMapEntry when it is a subtype 0.
     * 
     * @param data the data stream of the to be parsed ttf font
     * @throws IOException If there is an error parsing the true type font.
     */
    void processSubtype0(TTFDataStream data) throws IOException
    {
        byte[] glyphMapping = data.read(256);
        glyphIdToCharacterCode = newGlyphIdToCharacterCode(256);
        characterCodeToGlyphId = new HashMap<>(glyphMapping.length);
        for (int i = 0; i < glyphMapping.length; i++)
        {
            int glyphIndex = glyphMapping[i] & 0xFF;
            glyphIdToCharacterCode[glyphIndex] = i;
            characterCodeToGlyphId.put(i, glyphIndex);
        }
    }

    /**
     * Workaround for the fact that glyphIdToCharacterCode doesn't distinguish between
     * missing character codes and code 0.
     */
    private int[] newGlyphIdToCharacterCode(int size)
    {
        int[] gidToCode = new int[size];
        Arrays.fill(gidToCode, -1);
        return gidToCode;
    }

    /**
     * @return Returns the platformEncodingId.
     */
    public int getPlatformEncodingId()
    {
        return platformEncodingId;
    }

    /**
     * @param platformEncodingIdValue The platformEncodingId to set.
     */
    public void setPlatformEncodingId(int platformEncodingIdValue)
    {
        platformEncodingId = platformEncodingIdValue;
    }

    /**
     * @return Returns the platformId.
     */
    public int getPlatformId()
    {
        return platformId;
    }

    /**
     * @param platformIdValue The platformId to set.
     */
    public void setPlatformId(int platformIdValue)
    {
        platformId = platformIdValue;
    }

    /**
     * Returns the GlyphId linked with the given character code.
     *
     * @param characterCode the given character code to be mapped
     * @return glyphId the corresponding glyph id for the given character code
     */
    @Override
    public int getGlyphId(int characterCode)
    {
        Integer glyphId = characterCodeToGlyphId.get(characterCode);
        return glyphId == null ? 0 : glyphId;
    }

    private int getCharCode(int gid)
    {
        if (gid < 0 || gid >= glyphIdToCharacterCode.length)
        {
            return -1;
        }
        return glyphIdToCharacterCode[gid];
    }

    /**
     * Returns all possible character codes for the given gid, or null if there is none.
     *
     * @param gid glyph id
     * @return a list with all character codes the given gid maps to
     * 
     */
    @Override
    public List<Integer> getCharCodes(int gid)
    {
        int code = getCharCode(gid);
        if (code == -1)
        {
            return null;
        }
        List<Integer> codes = null;
        if (code == Integer.MIN_VALUE)
        {
            List<Integer> mappedValues = glyphIdToCharacterCodeMultiple.get(gid);
            if (mappedValues != null)
            {
                codes = new ArrayList<>(mappedValues);
                // sort the list to provide a reliable order
                Collections.sort(codes);
            }
        }
        else
        {
            codes = new ArrayList<>(1);
            codes.add(code);
        }
        return codes;
    }

    @Override
    public String toString()
    {
        return "{" + getPlatformId() + " " + getPlatformEncodingId() + "}";
    }

    /**
     * 
     * Class used to manage CMap - Format 2.
     * 
     */
    private static class SubHeader
    {
        private final int firstCode;
        private final int entryCount;
        /**
         * used to compute the GlyphIndex : P = glyphIndexArray.SubArray[pos] GlyphIndex = P + idDelta % 65536.
         */
        private final short idDelta;
        /**
         * Number of bytes to skip to reach the firstCode in the glyphIndexArray.
         */
        private final int idRangeOffset;

        private SubHeader(int firstCodeValue, int entryCountValue, short idDeltaValue, int idRangeOffsetValue)
        {
            firstCode = firstCodeValue;
            entryCount = entryCountValue;
            idDelta = idDeltaValue;
            idRangeOffset = idRangeOffsetValue;
        }

        /**
         * @return the firstCode
         */
        private int getFirstCode()
        {
            return firstCode;
        }

        /**
         * @return the entryCount
         */
        private int getEntryCount()
        {
            return entryCount;
        }

        /**
         * @return the idDelta
         */
        private short getIdDelta()
        {
            return idDelta;
        }

        /**
         * @return the idRangeOffset
         */
        private int getIdRangeOffset()
        {
            return idRangeOffset;
        }
    }
}
