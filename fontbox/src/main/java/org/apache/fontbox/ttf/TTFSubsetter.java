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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Subsetter for TrueType (TTF) fonts.
 *
 * <p>Originally developed by Wolfgang Glas for
 * <a href="https://clazzes.org/display/SKETCH/Clazzes.org+Sketch+Home">Sketch</a>.
 *
 * @author Wolfgang Glas
 */
public final class TTFSubsetter
{
    private static final byte[] PAD_BUF = new byte[] { 0, 0, 0 };

    private final TrueTypeFont ttf;
    private final CmapSubtable unicodeCmap;
    private final SortedMap<Integer, Integer> uniToGID;

    private final List<String> keepTables;
    private final SortedSet<Integer> glyphIds; // new glyph ids
    private String prefix;
    private boolean hasAddedCompoundReferences;

    /**
     * Creates a subsetter for the given font.
     *
     * @param ttf the font to be subset
     */
    public TTFSubsetter(TrueTypeFont ttf) throws IOException
    {
        this(ttf, null);
    }

    /**
     * Creates a subsetter for the given font.
     * 
     * @param ttf the font to be subset
     * @param tables optional tables to keep if present
     */
    public TTFSubsetter(TrueTypeFont ttf, List<String> tables) throws IOException
    {
        this.ttf = ttf;
        this.keepTables = tables;

        uniToGID = new TreeMap<Integer, Integer>();
        glyphIds = new TreeSet<Integer>();

        // find the best Unicode cmap
        this.unicodeCmap = ttf.getUnicodeCmap();

        // always copy GID 0
        glyphIds.add(0);
    }

    /**
     * Sets the prefix to add to the font's PostScript name.
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }
    
    /**
     * Add the given character code to the subset.
     * 
     * @param unicode character code
     */
    public void add(int unicode)
    {
        int gid = unicodeCmap.getGlyphId(unicode);
        if (gid != 0)
        {
            uniToGID.put(unicode, gid);
            glyphIds.add(gid);
        }
    }

    /**
     * Add the given character codes to the subset.
     *
     * @param unicodeSet character code set
     */
    public void addAll(Set<Integer> unicodeSet)
    {
        for (int unicode : unicodeSet)
        {
            add(unicode);
        }
    }

    /**
     * Returns the map of new -> old GIDs.
     */
    public Map<Integer, Integer> getGIDMap() throws IOException
    {
        addCompoundReferences();

        Map<Integer, Integer> newToOld = new HashMap<Integer, Integer>();
        int newGID = 0;
        for (int oldGID : glyphIds)
        {
            newToOld.put(newGID, oldGID);
            newGID++;
        }
        return newToOld;
    }

    /**
     * @param out The data output stream.
     * @param nTables The number of table.
     * @return The file offset of the first TTF table to write.
     * @throws IOException Upon errors.
     */
    private long writeFileHeader(DataOutputStream out, int nTables) throws IOException
    {
        out.writeInt(0x00010000);
        out.writeShort(nTables);
        
        int mask = Integer.highestOneBit(nTables);
        int searchRange = mask * 16;
        out.writeShort(searchRange);
        
        int entrySelector = log2(mask);
    
        out.writeShort(entrySelector);
        
        // numTables * 16 - searchRange
        int last = 16 * nTables - searchRange;
        out.writeShort(last);
        
        return 0x00010000L + toUInt32(nTables, searchRange) + toUInt32(entrySelector, last);
    }
        
    private long writeTableHeader(DataOutputStream out, String tag, long offset, byte[] bytes)
            throws IOException 
    {
        long checksum = 0;
        for (int nup = 0, n = bytes.length; nup < n; nup++)
        {
            checksum += (bytes[nup] & 0xffL) << 24 - nup % 4 * 8;
        }
        checksum &= 0xffffffffL;

        byte[] tagbytes = tag.getBytes("US-ASCII");

        out.write(tagbytes, 0, 4);
        out.writeInt((int)checksum);
        out.writeInt((int)offset);
        out.writeInt(bytes.length);

        // account for the checksum twice, once for the header field, once for the content itself
        return toUInt32(tagbytes) + checksum + checksum + offset + bytes.length;
    }

    private void writeTableBody(OutputStream os, byte[] bytes) throws IOException
    {
        int n = bytes.length;
        os.write(bytes);
        if (n % 4 != 0)
        {
            os.write(PAD_BUF, 0, 4 - n % 4);
        }
    }

    private byte[] buildHeadTable() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        HeaderTable h = ttf.getHeader();
        writeFixed(out, h.getVersion());
        writeFixed(out, h.getFontRevision());
        writeUint32(out, 0); // h.getCheckSumAdjustment()
        writeUint32(out, h.getMagicNumber());
        writeUint16(out, h.getFlags());
        writeUint16(out, h.getUnitsPerEm());
        writeLongDateTime(out, h.getCreated());
        writeLongDateTime(out, h.getModified());
        writeSInt16(out, h.getXMin());
        writeSInt16(out, h.getYMin());
        writeSInt16(out, h.getXMax());
        writeSInt16(out, h.getYMax());
        writeUint16(out, h.getMacStyle());
        writeUint16(out, h.getLowestRecPPEM());
        writeSInt16(out, h.getFontDirectionHint());
        // force long format of 'loca' table
        writeSInt16(out, (short)1); // h.getIndexToLocFormat()
        writeSInt16(out, h.getGlyphDataFormat());
        out.flush();

        return bos.toByteArray();
    }

    private byte[] buildHheaTable() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        HorizontalHeaderTable h = ttf.getHorizontalHeader();
        writeFixed(out, h.getVersion());
        writeSInt16(out, h.getAscender());
        writeSInt16(out, h.getDescender());
        writeSInt16(out, h.getLineGap());
        writeUint16(out, h.getAdvanceWidthMax());
        writeSInt16(out, h.getMinLeftSideBearing());
        writeSInt16(out, h.getMinRightSideBearing());
        writeSInt16(out, h.getXMaxExtent());
        writeSInt16(out, h.getCaretSlopeRise());
        writeSInt16(out, h.getCaretSlopeRun());
        writeSInt16(out, h.getReserved1()); // caretOffset
        writeSInt16(out, h.getReserved2());
        writeSInt16(out, h.getReserved3());
        writeSInt16(out, h.getReserved4());
        writeSInt16(out, h.getReserved5());
        writeSInt16(out, h.getMetricDataFormat());
        writeUint16(out, glyphIds.subSet(0, h.getNumberOfHMetrics()).size());

        out.flush();
        return bos.toByteArray();
    }

    private boolean shouldCopyNameRecord(NameRecord nr)
    {
        return nr.getPlatformId() == NameRecord.PLATFORM_WINDOWS
                && nr.getPlatformEncodingId() == NameRecord.ENCODING_WINDOWS_UNICODE_BMP
                && nr.getLanguageId() == NameRecord.LANGUGAE_WINDOWS_EN_US
                && nr.getNameId() >= 0 && nr.getNameId() < 7;
    }

    private byte[] buildNameTable() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        NamingTable name = ttf.getNaming();
        if (name == null || keepTables != null && !keepTables.contains("name"))
        {
            return null;
        }

        List<NameRecord> nameRecords = name.getNameRecords();
        int numRecords = 0;
        for (NameRecord record : nameRecords)
        {
            if (shouldCopyNameRecord(record))
            {
                numRecords++;
            }
        }
        writeUint16(out, 0);
        writeUint16(out, numRecords);
        writeUint16(out, 2*3 + 2*6 * numRecords);

        if (numRecords == 0)
        {
            return null;
        }

        byte[][] names = new byte[numRecords][];
        int j = 0;
        for (NameRecord record : nameRecords)
        {
            if (shouldCopyNameRecord(record))
            {
                int platform = record.getPlatformId();
                int encoding = record.getPlatformEncodingId();
                String charset = "ISO-8859-1";

                if (platform == CmapTable.PLATFORM_WINDOWS &&
                    encoding == CmapTable.ENCODING_WIN_UNICODE_BMP)
                {
                    charset = "UTF-16BE";
                }
                else if (platform == 2) // ISO [deprecated]=
                {
                    if (encoding == 0) // 7-bit ASCII
                    {
                        charset = "US-ASCII";
                    }
                    else if (encoding == 1) // ISO 10646=
                    {
                        //not sure is this is correct??
                        charset = "UTF16-BE";
                    }
                    else if (encoding == 2) // ISO 8859-1
                    {
                        charset = "ISO-8859-1";
                    }
                }
                String value = record.getString();
                if (record.getNameId() == 6 && prefix != null)
                {
                    value = prefix + value;
                }
                names[j] = value.getBytes(charset);
                j++;
            }
        }

        int offset = 0;
        j = 0;
        for (NameRecord nr : nameRecords)
        {
            if (shouldCopyNameRecord(nr))
            {
                writeUint16(out, nr.getPlatformId());
                writeUint16(out, nr.getPlatformEncodingId());
                writeUint16(out, nr.getLanguageId());
                writeUint16(out, nr.getNameId());
                writeUint16(out, names[j].length);
                writeUint16(out, offset);
                offset += names[j].length;
                j++;
            }
        }

        for (int i = 0; i < numRecords; i++)
        {
            out.write(names[i]);
        }

        out.flush();
        return bos.toByteArray();
    }

    private byte[] buildMaxpTable() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        MaximumProfileTable p = ttf.getMaximumProfile();
        writeFixed(out, 1.0);
        writeUint16(out, glyphIds.size());
        writeUint16(out, p.getMaxPoints());
        writeUint16(out, p.getMaxContours());
        writeUint16(out, p.getMaxCompositePoints());
        writeUint16(out, p.getMaxCompositeContours());
        writeUint16(out, p.getMaxZones());
        writeUint16(out, p.getMaxTwilightPoints());
        writeUint16(out, p.getMaxStorage());
        writeUint16(out, p.getMaxFunctionDefs());
        writeUint16(out, p.getMaxInstructionDefs());
        writeUint16(out, p.getMaxStackElements());
        writeUint16(out, p.getMaxSizeOfInstructions());
        writeUint16(out, p.getMaxComponentElements());
        writeUint16(out, p.getMaxComponentDepth());

        out.flush();
        return bos.toByteArray();
    }

    private byte[] buildOS2Table() throws IOException
    {
        OS2WindowsMetricsTable os2 = ttf.getOS2Windows();
        if (os2 == null || keepTables != null && !keepTables.contains("OS/2"))
        {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        writeUint16(out, os2.getVersion());
        writeSInt16(out, os2.getAverageCharWidth());
        writeUint16(out, os2.getWeightClass());
        writeUint16(out, os2.getWidthClass());

        writeSInt16(out, os2.getFsType());

        writeSInt16(out, os2.getSubscriptXSize());
        writeSInt16(out, os2.getSubscriptYSize());
        writeSInt16(out, os2.getSubscriptXOffset());
        writeSInt16(out, os2.getSubscriptYOffset());

        writeSInt16(out, os2.getSuperscriptXSize());
        writeSInt16(out, os2.getSuperscriptYSize());
        writeSInt16(out, os2.getSuperscriptXOffset());
        writeSInt16(out, os2.getSuperscriptYOffset());

        writeSInt16(out, os2.getStrikeoutSize());
        writeSInt16(out, os2.getStrikeoutPosition());
        writeSInt16(out, (short)os2.getFamilyClass());
        out.write(os2.getPanose());

        writeUint32(out, 0);
        writeUint32(out, 0);
        writeUint32(out, 0);
        writeUint32(out, 0);

        out.write(os2.getAchVendId().getBytes("US-ASCII"));

        Iterator<Entry<Integer, Integer>> it = uniToGID.entrySet().iterator();
        it.next();
        Entry<Integer, Integer> first = it.next();

        writeUint16(out, os2.getFsSelection());
        writeUint16(out, first.getKey());
        writeUint16(out, uniToGID.lastKey());
        writeUint16(out, os2.getTypoAscender());
        writeUint16(out, os2.getTypoDescender());
        writeUint16(out, os2.getTypoLineGap());
        writeUint16(out, os2.getWinAscent());
        writeUint16(out, os2.getWinDescent());

        out.flush();
        return bos.toByteArray();
    }

    private byte[] buildLocaTable(long[] newOffsets) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        for (long offset : newOffsets)
        {
            writeUint32(out, offset);
        }

        out.flush();
        return bos.toByteArray();
    }

    /**
     * Resolve compound glyph references.
     */
    private void addCompoundReferences() throws IOException
    {
        if (hasAddedCompoundReferences)
        {
            return;
        }
        hasAddedCompoundReferences = true;

        boolean hasNested;
        do
        {
            GlyphTable g = ttf.getGlyph();
            long[] offsets = ttf.getIndexToLocation().getOffsets();
            InputStream is = ttf.getOriginalData();
            Set<Integer> glyphIdsToAdd = null;
            try
            {
                is.skip(g.getOffset());
                long lastOff = 0L;
                for (Integer glyphId : glyphIds)
                {
                    long offset = offsets[glyphId];
                    long len = offsets[glyphId + 1] - offset;
                    is.skip(offset - lastOff);
                    byte[] buf = new byte[(int)len];
                    is.read(buf);
                    // rewrite glyphIds for compound glyphs
                    if (buf.length >= 2 && buf[0] == -1 && buf[1] == -1)
                    {
                        int off = 2*5;
                        int flags;
                        do
                        {
                            flags = (buf[off] & 0xff) << 8 | buf[off + 1] & 0xff;
                            off +=2;
                            int ogid = (buf[off] & 0xff) << 8 | buf[off + 1] & 0xff;
                            if (!glyphIds.contains(ogid))
                            {
                                if (glyphIdsToAdd == null)
                                {
                                    glyphIdsToAdd = new TreeSet<Integer>();
                                }
                                glyphIdsToAdd.add(ogid);
                            }
                            off += 2;
                            // ARG_1_AND_2_ARE_WORDS
                            if ((flags & 1 << 0) != 0)
                            {
                                off += 2 * 2;
                            }
                            else
                            {
                                off += 2;
                            }
                            // WE_HAVE_A_TWO_BY_TWO
                            if ((flags & 1 << 7) != 0)
                            {
                                off += 2 * 4;
                            }
                            // WE_HAVE_AN_X_AND_Y_SCALE
                            else if ((flags & 1 << 6) != 0)
                            {
                                off += 2 * 2;
                            }
                            // WE_HAVE_A_SCALE
                            else if ((flags & 1 << 3) != 0)
                            {
                                off += 2;
                            }
                        }
                        while ((flags & 1 << 5) != 0); // MORE_COMPONENTS

                    }
                    lastOff = offsets[glyphId + 1];
                }
            }
            finally
            {
                is.close();
            }
            if (glyphIdsToAdd != null)
            {
                glyphIds.addAll(glyphIdsToAdd);
            }
            hasNested = glyphIdsToAdd != null;
        } while (hasNested);
    }

    private byte[] buildGlyfTable(long[] newOffsets) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        GlyphTable g = ttf.getGlyph();
        long[] offsets = ttf.getIndexToLocation().getOffsets();
        InputStream is = ttf.getOriginalData();
        try
        {
            is.skip(g.getOffset());

            long prevEnd = 0;    // previously read glyph offset
            long newOffset = 0;  // new offset for the glyph in the subset font
            int newGid = 0;      // new GID in subset font

            // for each glyph in the subset
            for (Integer gid : glyphIds)
            {
                long offset = offsets[gid];
                long length = offsets[gid + 1] - offset;

                newOffsets[newGid++] = newOffset;
                is.skip(offset - prevEnd);

                byte[] buf = new byte[(int)length];
                is.read(buf);

                // detect glyph type
                if (buf.length >= 2 && buf[0] == -1 && buf[1] == -1)
                {
                    // compound glyph
                    int off = 2*5;
                    int flags;
                    do
                    {
                        // flags
                        flags = (buf[off] & 0xff) << 8 | buf[off + 1] & 0xff;
                        off += 2;

                        // glyphIndex
                        int componentGid = (buf[off] & 0xff) << 8 | buf[off + 1] & 0xff;
                        if (!glyphIds.contains(componentGid))
                        {
                            glyphIds.add(componentGid);
                        }

                        int newComponentGid = getNewGlyphId(componentGid);
                        buf[off]   = (byte)(newComponentGid >>> 8);
                        buf[off + 1] = (byte)newComponentGid;
                        off += 2;

                        // ARG_1_AND_2_ARE_WORDS
                        if ((flags & 1 << 0) != 0)
                        {
                            off += 2 * 2;
                        }
                        else
                        {
                            off += 2;
                        }
                        // WE_HAVE_A_TWO_BY_TWO
                        if ((flags & 1 << 7) != 0)
                        {
                            off += 2 * 4;
                        }
                        // WE_HAVE_AN_X_AND_Y_SCALE
                        else if ((flags & 1 << 6) != 0)
                        {
                            off += 2 * 2;
                        }
                        // WE_HAVE_A_SCALE
                        else if ((flags & 1 << 3) != 0)
                        {
                            off += 2;
                        }
                    }
                    while ((flags & 1 << 5) != 0); // MORE_COMPONENTS

                    // WE_HAVE_INSTRUCTIONS
                    if ((flags & 0x0100) == 0x0100)
                    {
                        // USHORT numInstr
                        int numInstr = (buf[off] & 0xff) << 8 | buf[off + 1] & 0xff;
                        off += 2;

                        // BYTE instr[numInstr]
                        off += numInstr;
                    }

                    // write the compound glyph
                    bos.write(buf, 0, off);

                    // offset to start next glyph
                    newOffset += off;
                }
                else if (buf.length > 0)
                {
                    // copy the entire glyph
                    bos.write(buf, 0, buf.length);

                    // offset to start next glyph
                    newOffset += buf.length;
                }

                // 4-byte alignment
                if (newOffset % 4 != 0)
                {
                    int len = 4 - (int)(newOffset % 4);
                    bos.write(PAD_BUF, 0, len);
                    newOffset += len;
                }

                prevEnd = offset + length;
            }
            newOffsets[newGid++] = newOffset;
        }
        finally
        {
            is.close();
        }

        return bos.toByteArray();
    }

    private int getNewGlyphId(Integer oldGid)
    {
        return glyphIds.headSet(oldGid).size();
    }

    private byte[] buildCmapTable() throws IOException
    {
        if (ttf.getCmap() == null || keepTables != null && !keepTables.contains("cmap"))
        {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        // cmap header
        writeUint16(out, 0); // version
        writeUint16(out, 1); // numberSubtables

        // encoding record
        writeUint16(out, CmapTable.PLATFORM_WINDOWS); // platformID
        writeUint16(out, CmapTable.ENCODING_WIN_UNICODE_BMP); // platformSpecificID
        writeUint32(out, 4 * 2 + 4); // offset

        // build Format 4 subtable (Unicode BMP)
        Iterator<Entry<Integer, Integer>> it = uniToGID.entrySet().iterator();
        it.next();
        Entry<Integer, Integer> lastChar = it.next();
        Entry<Integer, Integer> prevChar = lastChar;
        int lastGid = getNewGlyphId(lastChar.getValue());

        int[] startCode = new int[uniToGID.size()];
        int[] endCode = new int[uniToGID.size()];
        int[] idDelta = new int[uniToGID.size()];
        int segCount = 0;
        while(it.hasNext())
        {
            Entry<Integer, Integer> curChar2Gid = it.next();
            int curGid = getNewGlyphId(curChar2Gid.getValue());

            // todo: need format Format 12 for non-BMP
            if (curChar2Gid.getKey() > 0xFFFF)
            {
                throw new UnsupportedOperationException("non-BMP Unicode character");
            }

            if (curChar2Gid.getKey() != prevChar.getKey()+1 ||
                curGid - lastGid != curChar2Gid.getKey() - lastChar.getKey())
            {
                if (lastGid != 0)
                {
                    // don't emit ranges, which map to GID 0, the
                    // undef glyph is emitted a the very last segment
                    startCode[segCount] = lastChar.getKey();
                    endCode[segCount] = prevChar.getKey();
                    idDelta[segCount] = lastGid - lastChar.getKey();
                    segCount++;
                }
                else if (!lastChar.getKey().equals(prevChar.getKey()))
                {
                    // shorten ranges which start with GID 0 by one
                    startCode[segCount] = lastChar.getKey() + 1;
                    endCode[segCount] = prevChar.getKey();
                    idDelta[segCount] = lastGid - lastChar.getKey();
                    segCount++;
                }
                lastGid = curGid;
                lastChar = curChar2Gid;
            }
            prevChar = curChar2Gid;
        }

        // trailing segment
        startCode[segCount] = lastChar.getKey();
        endCode[segCount] = prevChar.getKey();
        idDelta[segCount] = lastGid -lastChar.getKey();
        segCount++;

        // GID 0
        startCode[segCount] = 0xffff;
        endCode[segCount] = 0xffff;
        idDelta[segCount] = 1;
        segCount++;

        // write format 4 subtable
        int searchRange = 2 * (int)Math.pow(2, Math.floor(log2(segCount)));
        writeUint16(out, 4); // format
        writeUint16(out, 8 * 2 + segCount * 4*2); // length
        writeUint16(out, 0); // language
        writeUint16(out, segCount * 2); // segCountX2
        writeUint16(out, searchRange); // searchRange
        writeUint16(out, log2(searchRange / 2)); // entrySelector
        writeUint16(out, 2 * segCount - searchRange); // rangeShift

        // endCode[segCount]
        for (int i = 0; i < segCount; i++)
        {
            writeUint16(out, endCode[i]);
        }

        // reservedPad
        writeUint16(out, 0);

        // startCode[segCount]
        for (int i = 0; i < segCount; i++)
        {
            writeUint16(out, startCode[i]);
        }

        // idDelta[segCount]
        for (int i = 0; i < segCount; i++)
        {
            writeUint16(out, idDelta[i]);
        }

        for (int i = 0; i < segCount; i++)
        {
            writeUint16(out, 0);
        }

        return bos.toByteArray();
    }

    private byte[] buildPostTable() throws IOException
    {
        PostScriptTable post = ttf.getPostScript();
        if (post == null || keepTables != null && !keepTables.contains("post"))
        {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);

        writeFixed(out, 2.0); // version
        writeFixed(out, post.getItalicAngle());
        writeSInt16(out, post.getUnderlinePosition());
        writeSInt16(out, post.getUnderlineThickness());
        writeUint32(out, post.getIsFixedPitch());
        writeUint32(out, post.getMinMemType42());
        writeUint32(out, post.getMaxMemType42());
        writeUint32(out, post.getMinMemType1());
        writeUint32(out, post.getMaxMemType1());

        // version 2.0

        // numberOfGlyphs
        writeUint16(out, glyphIds.size());

        // glyphNameIndex[numGlyphs]
        Map<String, Integer> names = new TreeMap<String, Integer>();
        for (int gid : glyphIds)
        {
            String name = post.getName(gid);
            Integer macId = WGL4Names.MAC_GLYPH_NAMES_INDICES.get(name);
            if (macId != null)
            {
                // the name is implicit, as it's from MacRoman
                writeUint16(out, macId);
            }
            else
            {
                // the name will be written explicitly
                Integer ordinal = names.get(name);
                if (ordinal == null)
                {
                    ordinal = names.size();
                    names.put(name, ordinal);
                }
                writeUint16(out, 258 + ordinal);
            }
        }

        // names[numberNewGlyphs]
        for (String name : names.keySet())
        {
            byte[] buf = name.getBytes(Charset.forName("US-ASCII"));
            writeUint8(out, buf.length);
            out.write(buf);
        }

        out.flush();
        return bos.toByteArray();
    }

    private byte[] buildHmtxTable() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        HorizontalHeaderTable h = ttf.getHorizontalHeader();
        HorizontalMetricsTable hm = ttf.getHorizontalMetrics();
        byte [] buf = new byte[4];
        InputStream is = ttf.getOriginalData();
        try
        {
            is.skip(hm.getOffset());
            long lastOff = 0;
            for (Integer glyphId : glyphIds)
            {
                // offset in original file
                long off;
                if (glyphId < h.getNumberOfHMetrics())
                {
                    off = glyphId * 4;
                }
                else
                {
                    off = h.getNumberOfHMetrics() * 4 + (glyphId - h.getNumberOfHMetrics()) * 2;
                }
                // skip over from last original offset
                if (off != lastOff)
                {
                    long nskip = off-lastOff;
                    if (nskip != is.skip(nskip))
                    {
                        throw new EOFException("Unexpected EOF exception parsing glyphId of hmtx table.");
                    }
                }
                // read left side bearings only, if we are beyond numOfHMetrics
                int n = glyphId < h.getNumberOfHMetrics() ? 4 : 2;
                if (n != is.read(buf, 0, n))
                {
                    throw new EOFException("Unexpected EOF exception parsing glyphId of hmtx table.");
                }
                bos.write(buf, 0, n);
                lastOff = off + n;
            }

            return bos.toByteArray();
        }
        finally
        {
            is.close();
        }
    }

    /**
     * Write the subfont to the given output stream.
     *
     * @param os the stream used for writing
     * @throws IOException if something went wrong.
     * @throws IllegalStateException if the subset is empty.
     */
    public void writeToStream(OutputStream os) throws IOException
    {
        if (glyphIds.isEmpty() || uniToGID.isEmpty())
        {
            throw new IllegalStateException("subset is empty");
        }
        
        addCompoundReferences();

        DataOutputStream out = new DataOutputStream(os);
        try 
        {
            long[] newLoca = new long[glyphIds.size() + 1];

            // generate tables in dependency order
            byte[] head = buildHeadTable();
            byte[] hhea = buildHheaTable();
            byte[] maxp = buildMaxpTable();
            byte[] name = buildNameTable();
            byte[] os2  = buildOS2Table();
            byte[] glyf = buildGlyfTable(newLoca);
            byte[] loca = buildLocaTable(newLoca);
            byte[] cmap = buildCmapTable();
            byte[] hmtx = buildHmtxTable();
            byte[] post = buildPostTable();

            // save to TTF in optimized order
            Map<String, byte[]> tables = new TreeMap<String, byte[]>();
            if (os2 != null)
            {
                tables.put("OS/2", os2);
            }
            if (cmap != null)
            {
                tables.put("cmap", cmap);
            }
            if (glyf != null)
            {
                tables.put("glyf", glyf); 
            }
            tables.put("head", head);
            tables.put("hhea", hhea);
            tables.put("hmtx", hmtx);
            if (loca != null)
            {
                tables.put("loca", loca);
            }
            tables.put("maxp", maxp);
            if (name != null)
            {
                tables.put("name", name);
            }
            if (post != null)
            {
                tables.put("post", post);
            }

            // copy all other tables
            for (Map.Entry<String, TTFTable> entry : ttf.getTableMap().entrySet())
            {
                String tag = entry.getKey();
                TTFTable table = entry.getValue();

                if (!tables.containsKey(tag) && (keepTables == null || keepTables.contains(tag)))
                {
                    tables.put(tag, ttf.getTableBytes(table));
                }
            }

            // calculate checksum
            long checksum = writeFileHeader(out, tables.size());
            long offset = 12L + 16L * tables.size();
            for (Map.Entry<String, byte[]> entry : tables.entrySet())
            {
                checksum += writeTableHeader(out, entry.getKey(), offset, entry.getValue());
                offset += (entry.getValue().length + 3) / 4 * 4;
            }
            checksum = 0xB1B0AFBAL - (checksum & 0xffffffffL);

            // update checksumAdjustment in 'head' table
            head[8] = (byte)(checksum >>> 24);
            head[9] = (byte)(checksum >>> 16);
            head[10] = (byte)(checksum >>> 8);
            head[11] = (byte)checksum;
            for (byte[] bytes : tables.values())
            {
                writeTableBody(out, bytes);
            }
        }
        finally 
        {
            out.close();
        }
    }

    private void writeFixed(DataOutputStream out, double f) throws IOException
    {
        double ip = Math.floor(f);
        double fp = (f-ip) * 65536.0;
        out.writeShort((int)ip);
        out.writeShort((int)fp);
    }

    private void writeUint32(DataOutputStream out, long l) throws IOException
    {
        out.writeInt((int)l);
    }

    private void writeUint16(DataOutputStream out, int i) throws IOException
    {
        out.writeShort(i);
    }

    private void writeSInt16(DataOutputStream out, short i) throws IOException
    {
        out.writeShort(i);
    }

    private void writeUint8(DataOutputStream out, int i) throws IOException
    {
        out.writeByte(i);
    }

    private void writeLongDateTime(DataOutputStream out, Calendar calendar) throws IOException
    {
        // inverse operation of TTFDataStream.readInternationalDate()
        GregorianCalendar cal = new GregorianCalendar( 1904, 0, 1 );
        long millisFor1904 = cal.getTimeInMillis();
        long secondsSince1904 = (calendar.getTimeInMillis() - millisFor1904) / 1000L;
        out.writeLong(secondsSince1904);
    }

    private long toUInt32(int high, int low)
    {
        return (high & 0xffffL) << 16 | low & 0xffffL;
    }

    private long toUInt32(byte[] bytes)
    {
        return (bytes[0] & 0xffL) << 24
                | (bytes[1] & 0xffL) << 16
                | (bytes[2] & 0xffL) << 8
                | bytes[3] & 0xffL;
    }

    private int log2(int num)
    {
        return (int)Math.round(Math.log(num) / Math.log(2));
    }
}
