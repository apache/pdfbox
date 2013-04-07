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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.fontbox.encoding.Encoding;
import org.apache.fontbox.encoding.MacRomanEncoding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A font, which is comprised of a subset of characters of a TrueType font.
 * Based on code developed by Wolfgang Glas
 * http://svn.clazzes.org/svn/sketch/trunk/pdf/pdf-entities/src/main/java/org/clazzes/sketch/pdf/entities/impl/TTFSubFont.java
 */
public class TTFSubFont 
{

    private static final Log LOG = LogFactory.getLog(TTFSubFont.class);
    private static final byte[] PAD_BUF = new byte[] {0,0,0};
    
    private final TrueTypeFont baseTTF;
    private final String nameSuffix;
    private final CMAPEncodingEntry baseCmap;
    
    // A map of unicode char codes to glyph IDs of the original font.
    private final SortedMap<Integer,Integer> characters;
    // A sorted version of this set will comprise the generated glyph IDs
    // for the written truetype font.
    private final SortedSet<Integer> glyphIds;
    
    /**
     * Constructs a subfont based on the given font using the given suffix.
     * 
     * @param baseFont the base font of the subfont
     * @param suffix suffix used for the naming
     * 
     */
    public TTFSubFont(TrueTypeFont baseFont, String suffix) 
    {
        baseTTF = baseFont;
        nameSuffix = suffix;
        characters = new TreeMap<Integer, Integer>();
        glyphIds = new TreeSet<Integer>();
        
        CMAPEncodingEntry[] cmaps = this.baseTTF.getCMAP().getCmaps();
        CMAPEncodingEntry unicodeCmap = null;
        
        for (CMAPEncodingEntry cmap : cmaps) 
        {
            // take first unicode map.
            if (cmap.getPlatformId() == 0 || (cmap.getPlatformId() == 3 && cmap.getPlatformEncodingId() == 1)) 
            {
                unicodeCmap = cmap;
                break;
            }
        }
        baseCmap = unicodeCmap;
        // add notdef character.
        addCharCode(0);
    }
    
    /**
     * Add the given charcode to the subfpont.
     * 
     * @param charCode the charCode to be added
     * 
     */
    public void addCharCode(int charCode) 
    {
        Integer gid = Integer.valueOf(baseCmap.getGlyphId(charCode));
        if (charCode == 0 || gid != 0) 
        {
            characters.put(charCode,gid);
            glyphIds.add(gid);
        }
    }

    private static int log2i(int i) 
    {
        int ret = -1;
        if ((i & 0xffff0000) != 0) 
        {
            i >>>= 16;
            ret += 16;
        }
        if ((i & 0xff00) != 0) 
        {
            i >>>= 8;
            ret += 8;
        }
        if ((i & 0xf0) != 0) 
        {
            i >>>= 4;
            ret += 4;
        }
        if ((i & 0xc) != 0) 
        {
            i >>>= 2;
            ret += 2;
        }
        if ((i & 0x2) != 0) 
        {
            i >>>= 1;
            ++ret;
        }
        if (i != 0) 
        {
            ++ret;
        }
        return ret;
    }
    
    private static long buildUint32(int high, int low) 
    {
        return ((((long)high)&0xffffL) << 16) | (((long)low)&0xffffL);
    }
    
    private static long buildUint32(byte[] bytes) 
    {
        return ((((long)bytes[0])&0xffL) << 24) |
                ((((long)bytes[1])&0xffL) << 16) |
                ((((long)bytes[2])&0xffL) << 8) |
                (((long)bytes[3])&0xffL);
    }

    /**
     * @param dos The data output stream.
     * @param nTables The number of table.
     * @return The file offset of the first TTF table to write.
     * @throws IOException Upon errors.
     */
    private static long writeFileHeader(DataOutputStream dos, int nTables) throws IOException 
    {
        dos.writeInt(0x00010000);
        dos.writeShort(nTables);
        
        int mask = Integer.highestOneBit(nTables);
        int searchRange = mask * 16;
        dos.writeShort(searchRange);
        
        int entrySelector=log2i(mask);
    
        dos.writeShort(entrySelector);
        
        // numTables * 16 - searchRange
        int last = 16 * nTables - searchRange;
        dos.writeShort(last);
        
        return 0x00010000L + buildUint32(nTables,searchRange) + buildUint32(entrySelector,last);
    }
        
    private static long writeTableHeader(DataOutputStream dos, String tag, long offset, byte[] bytes) 
            throws IOException 
    {
        
        int n = bytes.length;
        int nup;
        long checksum = 0L;
        
        for (nup=0;nup<n;++nup) 
        {
            checksum += (((long)bytes[nup]) & 0xffL)<<(24-(nup%4)*8);
        }
        
        checksum &= 0xffffffffL;
        
        LOG.debug(String.format("Writing table header [%s,%08x,%08x,%08x]",tag,checksum,offset,bytes.length));
        
        byte[] tagbytes = tag.getBytes("US-ASCII");
        
        dos.write(tagbytes,0,4);
        dos.writeInt((int)checksum);
        dos.writeInt((int)offset);
        dos.writeInt(bytes.length);
        
        // account for the checksum twice, one time for the header field, on time for the content itself.
        return buildUint32(tagbytes) + checksum + checksum + offset + bytes.length;
    }
    
    private static void writeTableBody(OutputStream os, byte[] bytes) throws IOException 
    {
        int n = bytes.length;
        os.write(bytes);
        if ((n%4)!=0) 
        {
            os.write(PAD_BUF,0,4-n%4);
        }
    }

    private static void writeFixed(DataOutputStream dos, double f) throws IOException 
    {
        double ip = Math.floor(f);
        double fp = (f-ip) * 65536.0;
        dos.writeShort((int)ip);
        dos.writeShort((int)fp);
    }
    
    private static void writeUint32(DataOutputStream dos, long l) throws IOException 
    {
        dos.writeInt((int)l);
    }

    private static void writeUint16(DataOutputStream dos, int i) throws IOException 
    {
        dos.writeShort(i);
    }

    private static void writeSint16(DataOutputStream dos, short i) throws IOException 
    {
        dos.writeShort(i);
    }

    private static void writeUint8(DataOutputStream dos, int i) throws IOException 
    {
        dos.writeByte(i);
    }

    private static void writeLongDateTime(DataOutputStream dos, Calendar calendar) throws IOException 
    {
        // inverse operation of TTFDataStream.readInternationalDate()
        GregorianCalendar cal = new GregorianCalendar( 1904, 0, 1 );
        long millisFor1904 = cal.getTimeInMillis();
        long secondsSince1904 = (calendar.getTimeInMillis() - millisFor1904) / 1000L;
        dos.writeLong(secondsSince1904);
    }

    private byte[] buildHeadTable() throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        
        LOG.debug("Building table [head]...");
        
        HeaderTable h = this.baseTTF.getHeader();
        
        writeFixed(dos,h.getVersion());
        writeFixed(dos,h.getFontRevision());
        writeUint32(dos,0 /* h.getCheckSumAdjustment() */);
        writeUint32(dos,h.getMagicNumber());
        writeUint16(dos,h.getFlags());
        writeUint16(dos,h.getUnitsPerEm());
        writeLongDateTime(dos,h.getCreated());
        writeLongDateTime(dos,h.getModified());
        writeSint16(dos,h.getXMin());
        writeSint16(dos,h.getYMin());
        writeSint16(dos,h.getXMax());
        writeSint16(dos,h.getYMax());
        writeUint16(dos,h.getMacStyle());
        writeUint16(dos,h.getLowestRecPPEM());
        writeSint16(dos,h.getFontDirectionHint());
        // force long format of 'loca' table.
        writeSint16(dos,(short)1 /* h.getIndexToLocFormat() */);
        writeSint16(dos,h.getGlyphDataFormat());
        dos.flush();
        
        LOG.debug("Finished table [head].");

        return bos.toByteArray();
    }
    
    private byte[] buildHheaTable() throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        LOG.debug("Building table [hhea]...");

        HorizontalHeaderTable h = this.baseTTF.getHorizontalHeader();
        
        writeFixed(dos,h.getVersion());
        writeSint16(dos,h.getAscender());
        writeSint16(dos,h.getDescender());
        writeSint16(dos,h.getLineGap());
        writeUint16(dos,h.getAdvanceWidthMax());
        writeSint16(dos,h.getMinLeftSideBearing());
        writeSint16(dos,h.getMinRightSideBearing());
        writeSint16(dos,h.getXMaxExtent());
        writeSint16(dos,h.getCaretSlopeRise());
        writeSint16(dos,h.getCaretSlopeRun());
        writeSint16(dos,h.getReserved1()); // caretOffset
        writeSint16(dos,h.getReserved2());
        writeSint16(dos,h.getReserved3());
        writeSint16(dos,h.getReserved4());
        writeSint16(dos,h.getReserved5());
        writeSint16(dos,h.getMetricDataFormat());
        writeUint16(dos,glyphIds.subSet(0,h.getNumberOfHMetrics()).size());
                
        dos.flush();
        LOG.debug("Finished table [hhea].");
        return bos.toByteArray();
    }

    private static boolean replicateNameRecord(NameRecord nr) 
    {
        return nr.getPlatformId() == NameRecord.PLATFORM_WINDOWS 
                && nr.getPlatformEncodingId() == NameRecord.PLATFORM_ENCODING_WINDOWS_UNICODE 
                && nr.getLanguageId() == 0 
                && nr.getNameId() >= 0 && nr.getNameId() < 7;
    }
    
    private byte[] buildNameTable() throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        
        LOG.debug("Building table [name]...");

        NamingTable n = this.baseTTF.getNaming();
        List<NameRecord> nameRecords = null;
        if ( n != null)
        {
            nameRecords = n.getNameRecords();
        }
        else
        {
            // sometimes there is no naming table in an embedded subfonts
            // create some dummies
            nameRecords = new ArrayList<NameRecord>();
            NameRecord nr = new NameRecord();
            nr.setPlatformId(NameRecord.PLATFORM_WINDOWS);
            nr.setPlatformEncodingId(NameRecord.PLATFORM_ENCODING_WINDOWS_UNICODE);
            nr.setLanguageId(0);
            nr.setNameId(NameRecord.NAME_FONT_FAMILY_NAME);
            nr.setString("PDFBox-Dummy-Familyname");
            nameRecords.add(nr);
            nr = new NameRecord();
            nr.setPlatformId(NameRecord.PLATFORM_WINDOWS);
            nr.setPlatformEncodingId(NameRecord.PLATFORM_ENCODING_WINDOWS_UNICODE);
            nr.setLanguageId(0);
            nr.setNameId(NameRecord.NAME_FULL_FONT_NAME);
            nr.setString("PDFBox-Dummy-Fullname");
            nameRecords.add(nr);
        }
        int numberOfRecords = nameRecords.size();
        int nrep = 0;
        for (int i=0;i<numberOfRecords;++i) 
        {
            NameRecord nr = nameRecords.get(i);
            if (replicateNameRecord(nr)) 
            {
                LOG.debug("Writing name record ["+nr.getNameId()+"], ["+nr.getString()+"],");
                ++nrep;
            }
        }
        writeUint16(dos,0);
        writeUint16(dos,nrep);
        writeUint16(dos,2*3 + (2*6) * nrep);

        byte[][] names = new byte[nrep][];
        int j=0;
        for (int i=0;i<numberOfRecords;++i) 
        {
            NameRecord nr = nameRecords.get(i);
            if (replicateNameRecord(nr)) 
            {
                int platform = nr.getPlatformId();
                int encoding = nr.getPlatformEncodingId();
                String charset = "ISO-8859-1";
                if( platform == 3 && encoding == 1 )
                {
                    charset = "UTF-16BE";
                }
                else if( platform == 2 )
                {
                    if( encoding == 0 )
                    {
                        charset = "US-ASCII";
                    }
                    else if( encoding == 1 )
                    {
                        //not sure is this is correct??
                        charset = "UTF16-BE";
                    }
                    else if( encoding == 2 )
                    {
                        charset = "ISO-8859-1";
                    }
                }
                String value = nr.getString();
                if (nr.getNameId() == 6 && this.nameSuffix != null) 
                {
                    value += this.nameSuffix;
                }
                names[j] = value.getBytes(charset);
                ++j;
            }
        }
        
        int offset = 0;
        j = 0;
        for (int i=0;i<numberOfRecords;++i) 
        {
            NameRecord nr = nameRecords.get(i);
            if (replicateNameRecord(nr)) 
            {
                writeUint16(dos,nr.getPlatformId());
                writeUint16(dos,nr.getPlatformEncodingId());
                writeUint16(dos,nr.getLanguageId());
                writeUint16(dos,nr.getNameId());
                writeUint16(dos,names[j].length);
                writeUint16(dos,offset);
                offset += names[j].length;
                ++j;
            }
        }
        
        for (int i=0;i<nrep;++i) 
        {
            dos.write(names[i]);
        }
        dos.flush();
        LOG.debug("Finished table [name].");
        return bos.toByteArray();
    }
    
    private byte[] buildMaxpTable() throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        LOG.debug("Building table [maxp]...");

        MaximumProfileTable p = this.baseTTF.getMaximumProfile();

        writeFixed(dos,1.0);
        writeUint16(dos,glyphIds.size());
        writeUint16(dos,p.getMaxPoints());
        writeUint16(dos,p.getMaxContours());
        writeUint16(dos,p.getMaxCompositePoints());
        writeUint16(dos,p.getMaxCompositeContours());
        writeUint16(dos,p.getMaxZones());
        writeUint16(dos,p.getMaxTwilightPoints());
        writeUint16(dos,p.getMaxStorage());
        writeUint16(dos,p.getMaxFunctionDefs());
        writeUint16(dos,0);
        writeUint16(dos,p.getMaxStackElements());
        writeUint16(dos,0);
        writeUint16(dos,p.getMaxComponentElements());
        writeUint16(dos,p.getMaxComponentDepth());

        dos.flush();
        LOG.debug("Finished table [maxp].");
        return bos.toByteArray();
    }
    
    private byte[] buildOS2Table() throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        OS2WindowsMetricsTable os2 = this.baseTTF.getOS2Windows();
        if (os2 == null)
        {
            // sometimes there is no OS2 table in an embedded subfonts
            // create a dummy
            os2 = new OS2WindowsMetricsTable();
        }
        
        LOG.debug("Building table [OS/2]...");

        writeUint16(dos,0);
        writeSint16(dos,os2.getAverageCharWidth());
        writeUint16(dos,os2.getWeightClass());
        writeUint16(dos,os2.getWidthClass());
            
        writeSint16(dos,os2.getFsType());
        
        writeSint16(dos,os2.getSubscriptXSize());
        writeSint16(dos,os2.getSubscriptYSize());
        writeSint16(dos,os2.getSubscriptXOffset());
        writeSint16(dos,os2.getSubscriptYOffset());
        
        writeSint16(dos,os2.getSuperscriptXSize());
        writeSint16(dos,os2.getSuperscriptYSize());
        writeSint16(dos,os2.getSuperscriptXOffset());
        writeSint16(dos,os2.getSuperscriptYOffset());
        
        writeSint16(dos,os2.getStrikeoutSize());
        writeSint16(dos,os2.getStrikeoutPosition());
        writeUint8(dos,os2.getFamilyClass());
        writeUint8(dos,os2.getFamilySubClass());
        dos.write(os2.getPanose());
        
        writeUint32(dos,0);
        writeUint32(dos,0);
        writeUint32(dos,0);
        writeUint32(dos,0);
        
        dos.write(os2.getAchVendId().getBytes("ISO-8859-1"));

        Iterator<Entry<Integer, Integer>> it = characters.entrySet().iterator();
        it.next();
        Entry<Integer, Integer> first = it.next();
        
        writeUint16(dos,os2.getFsSelection());
        writeUint16(dos,first.getKey());
        writeUint16(dos,characters.lastKey());
        /*
         * The mysterious Microsoft additions.
         *
         * SHORT    sTypoAscender    
         * SHORT    sTypoDescender    
         * SHORT    sTypoLineGap
         * USHORT    usWinAscent
         * USHORT    usWinDescent
         */
        writeUint16(dos,os2.getTypoAscender());
        writeUint16(dos,os2.getTypoDescender());
        writeUint16(dos,os2.getTypeLineGap());
        writeUint16(dos,os2.getWinAscent());
        writeUint16(dos,os2.getWinDescent());
        
        dos.flush();
        LOG.debug("Finished table [OS/2].");
        return bos.toByteArray();
    }
    
    private byte[] buildLocaTable(long[] newOffsets) throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);

        LOG.debug("Building table [loca]...");

        for (long newOff : newOffsets)
        {                
            writeUint32(dos,newOff);
        }
        dos.flush();
        LOG.debug("Finished table [loca].");
        return bos.toByteArray();
    }

    private boolean addCompoundReferences() throws IOException 
    {
        GlyphTable g = this.baseTTF.getGlyph();
        long[] offsets = this.baseTTF.getIndexToLocation().getOffsets();
        InputStream is = this.baseTTF.getOriginalData();
        Set<Integer> glyphIdsToAdd = null;
        try 
        {
            is.skip(g.getOffset());
            long lastOff = 0L;
            for (Integer glyphId : this.glyphIds) 
            {
                long offset = offsets[glyphId.intValue()];
                long len = offsets[glyphId.intValue()+1] - offset;
                is.skip(offset-lastOff);
                byte[] buf= new byte[(int)len];
                is.read(buf);
                // rewrite glyphIds for compound glyphs
                if (buf.length >= 2 && buf[0] == -1 && buf[1] == -1) 
                {
                    int off = 2*5;
                    int flags = 0;
                    do 
                    {
                        flags = ((((int)buf[off]) & 0xff) << 8) | (buf[off+1] & 0xff); 
                        off +=2;
                        int ogid = ((((int)buf[off]) & 0xff) << 8) | (buf[off+1] & 0xff);
                        if (!this.glyphIds.contains(ogid)) 
                        {
                            LOG.debug("Adding referenced glyph "+ogid+" of compound glyph "+glyphId);
                            if (glyphIdsToAdd == null) 
                            {
                                glyphIdsToAdd = new TreeSet<Integer>();
                            }
                            glyphIdsToAdd.add(ogid);
                        }
                        off += 2;
                        // ARG_1_AND_2_ARE_WORDS
                        if ((flags & (1 << 0)) != 0) 
                        {
                            off += 2 * 2;
                        }
                        else 
                        {
                            off += 2;
                        }
                        // WE_HAVE_A_TWO_BY_TWO
                        if ((flags & (1 << 7)) != 0) 
                        {
                            off += 2 * 4;
                        }
                        // WE_HAVE_AN_X_AND_Y_SCALE
                        else if ((flags & (1 << 6)) != 0) 
                        {
                            off += 2 * 2;
                        }
                        // WE_HAVE_A_SCALE
                        else if ((flags & (1 << 3)) != 0) 
                        {
                            off += 2;
                        }
                        
                        // MORE_COMPONENTS
                    } 
                    while ((flags & (1 << 5)) != 0);
                    
                }
                lastOff = offsets[glyphId.intValue()+1];
            }
        }
        finally 
        {
            is.close();
        }
        if (glyphIdsToAdd != null) 
        {
            this.glyphIds.addAll(glyphIdsToAdd);
        }
        return glyphIdsToAdd == null;
    }
    
    private byte[] buildGlyfTable(long[] newOffsets) throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LOG.debug("Building table [glyf]...");
        GlyphTable g = this.baseTTF.getGlyph();
        long[] offsets = this.baseTTF.getIndexToLocation().getOffsets();
        InputStream is = this.baseTTF.getOriginalData();
        try 
        {
            is.skip(g.getOffset());
            long lastOff = 0L;
            long newOff = 0L;
            int ioff = 0;
            for (Integer glyphId : this.glyphIds) 
            {
                long offset = offsets[glyphId.intValue()];
                long len = offsets[glyphId.intValue()+1] - offset;
                newOffsets[ioff++] = newOff;
                is.skip(offset-lastOff);
                byte[] buf= new byte[(int)len];
                is.read(buf);
                // rewrite glyphIds for compound glyphs
                if (buf.length >= 2 && buf[0] == -1 && buf[1] == -1) 
                {
                    LOG.debug("Compound glyph "+glyphId);
                    int off = 2*5;
                    int flags = 0;
                    do 
                    {
                        // clear the WE_HAVE_INSTRUCTIONS bit. (bit 8 is lsb of the first byte)
                        buf[off] &= 0xfe;
                        flags = ((((int)buf[off]) & 0xff) << 8) | ((int)buf[off+1] & 0xff);                         
                        off +=2;
                        int ogid = ((((int)buf[off]) & 0xff) << 8) | ((int)buf[off+1] & 0xff);
                        if (!this.glyphIds.contains(ogid)) 
                        {
                            this.glyphIds.add(ogid);
                        }
                        int ngid = this.getNewGlyphId(ogid);
                        if (LOG.isDebugEnabled()) 
                        {
                            LOG.debug(String.format("mapped glyph  %d to %d in compound reference (flags=%04x)",
                                    ogid,ngid,flags));
                        }
                        buf[off]   = (byte)(ngid >>> 8);
                        buf[off+1] = (byte)ngid;
                        off += 2;
                        // ARG_1_AND_2_ARE_WORDS
                        if ((flags & (1 << 0)) != 0) 
                        {
                            off += 2 * 2;
                        }
                        else 
                        {
                            off += 2;
                        }
                        // WE_HAVE_A_TWO_BY_TWO
                        if ((flags & (1 << 7)) != 0) 
                        {
                            off += 2 * 4;
                        }
                        // WE_HAVE_AN_X_AND_Y_SCALE
                        else if ((flags & (1 << 6)) != 0) 
                        {
                            off += 2 * 2;
                        }
                        // WE_HAVE_A_SCALE
                        else if ((flags & (1 << 3)) != 0) 
                        {
                            off += 2;
                        }
                        // MORE_COMPONENTS
                    } 
                    while ((flags & (1 << 5)) != 0);
                    // write the compound glyph
                    bos.write(buf,0,off);
                    newOff += off;
                }
                else if (buf.length > 0)
                {
                    /*
                     * bail out instructions for simple glyphs, an excerpt from the specs is given below:
                     *                         
                     * int16    numberOfContours    If the number of contours is positive or zero, it is a single glyph;
                     * If the number of contours is -1, the glyph is compound
                     *  FWord    xMin    Minimum x for coordinate data
                     *  FWord    yMin    Minimum y for coordinate data
                     *  FWord    xMax    Maximum x for coordinate data
                     *  FWord    yMax    Maximum y for coordinate data
                     *  (here follow the data for the simple or compound glyph)
                     *
                     * Table 15: Simple glyph definition
                     *  Type    Name    Description
                     *  uint16  endPtsOfContours[n] Array of last points of each contour; n is the number of contours;
                     *          array entries are point indices
                     *  uint16  instructionLength Total number of bytes needed for instructions
                     *  uint8   instructions[instructionLength] Array of instructions for this glyph
                     *  uint8   flags[variable] Array of flags
                     *  uint8 or int16  xCoordinates[] Array of x-coordinates; the first is relative to (0,0),
                     *                                 others are relative to previous point
                     *  uint8 or int16  yCoordinates[] Array of y-coordinates; the first is relative to (0,0), 
                     *                                 others are relative to previous point
                     */
                                        
                    int    numberOfContours = (((int)buf[0]& 0xff) << 8) | ((int)buf[1] & 0xff); 
                    
                    // offset of instructionLength
                    int off = 2*5 + numberOfContours * 2;
                    
                    // write numberOfContours, xMin, yMin, xMax, yMax, endPtsOfContours[n]
                    bos.write(buf,0,off);
                    newOff += off;
                    
                    int instructionLength = ((((int)buf[off]) & 0xff) << 8) | ((int)buf[off+1] & 0xff);
                        
                    // zarro instructions.
                    bos.write(0);
                    bos.write(0);
                    newOff += 2;
                    
                    off += 2 + instructionLength;
                    
                    // flags and coordinates
                    bos.write(buf,off,buf.length - off);
                    newOff += buf.length - off;
                }
                
                
                if ((newOff % 4L) != 0L) 
                {
                    int np = (int)(4-newOff%4L);
                    bos.write(PAD_BUF,0,np);
                    newOff += np;
                }
                
                lastOff = offsets[glyphId.intValue()+1];
            }
            newOffsets[ioff++] = newOff;
        }
        finally 
        {
            is.close();
        }
        LOG.debug("Finished table [glyf].");
        return bos.toByteArray();
    }

    private int getNewGlyphId(Integer oldGid) 
    {
        return this.glyphIds.headSet(oldGid).size();
    }
    
    private byte[] buildCmapTable() throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        LOG.debug("Building table [cmap]...");
        /*
         * UInt16    version    Version number (Set to zero)
         * UInt16    numberSubtables    Number of encoding subtables
         */
        writeUint16(dos,0);
        writeUint16(dos,1);
        /*
         * UInt16    platformID    Platform identifier
         * UInt16    platformSpecificID    Platform-specific encoding identifier
         * UInt32    offset    Offset of the mapping table
         */
        writeUint16(dos,3); // unicode
        writeUint16(dos,1); // Default Semantics
        writeUint32(dos, 4 * 2 + 4);
        // mapping of type 4.
        Iterator<Entry<Integer, Integer>> it = this.characters.entrySet().iterator();
        it.next();
        Entry<Integer, Integer> lastChar = it.next();
        Entry<Integer, Integer> prevChar = lastChar;
        int lastGid = this.getNewGlyphId(lastChar.getValue());

        int[] startCode = new int[this.characters.size()];
        int[] endCode = new int[this.characters.size()];
        int[] idDelta = new int[this.characters.size()];
        int nseg = 0;
        while(it.hasNext()) 
        {
            Entry<Integer, Integer> curChar = it.next();
            int curGid = this.getNewGlyphId(curChar.getValue());
            
            if (curChar.getKey() != prevChar.getKey()+1 ||
                    curGid - lastGid != curChar.getKey() - lastChar.getKey()) 
            {
                // Don't emit ranges, which map to the undef glyph, the
                // undef glyph is emitted a the very last segment.
                if (lastGid != 0) 
                {
                    startCode[nseg] = lastChar.getKey();
                    endCode[nseg] = prevChar.getKey();
                    idDelta[nseg] = lastGid - lastChar.getKey();
                    ++nseg;
                }
                // shorten ranges which start with undef by one.
                else if (!lastChar.getKey().equals(prevChar.getKey())) 
                {
                    startCode[nseg] = lastChar.getKey()+1;
                    endCode[nseg] = prevChar.getKey();
                    idDelta[nseg] = lastGid - lastChar.getKey();
                    ++nseg;
                }
                lastGid = curGid;
                lastChar = curChar;
            }
            prevChar = curChar;
        }
        // trailing segment
        startCode[nseg] = lastChar.getKey();
        endCode[nseg] = prevChar.getKey();
        idDelta[nseg] = lastGid -lastChar.getKey();
        ++nseg;
        // notdef character.
        startCode[nseg] = 0xffff;
        endCode[nseg] = 0xffff;
        idDelta[nseg] = 1;
        ++nseg;
        
        /*
         * UInt16    format    Format number is set to 4     
         * UInt16    length    Length of subtable in bytes     
         * UInt16    language    Language code for this encoding subtable, or zero if language-independent     
         * UInt16    segCountX2    2 * segCount     
         * UInt16    searchRange    2 * (2**FLOOR(log2(segCount)))     
         * UInt16    entrySelector    log2(searchRange/2)     
         * UInt16    rangeShift    (2 * segCount) - searchRange     
         * UInt16    endCode[segCount]    Ending character code for each segment, last = 0xFFFF.    
         * UInt16    reservedPad    This value should be zero    
         * UInt16    startCode[segCount]    Starting character code for each segment    
         * UInt16    idDelta[segCount]    Delta for all character codes in segment     
         * UInt16    idRangeOffset[segCount]    Offset in bytes to glyph indexArray, or 0     
         * UInt16    glyphIndexArray[variable]    Glyph index array
         */
        
        writeUint16(dos,4);
        writeUint16(dos, 8*2 + nseg * (4*2));
        writeUint16(dos,0);
        writeUint16(dos,nseg*2);
        int nsegHigh = Integer.highestOneBit(nseg);
        writeUint16(dos,nsegHigh*2);
        writeUint16(dos,log2i(nsegHigh));
        writeUint16(dos,2*(nseg-nsegHigh));
        
        for (int i=0;i<nseg;++i) 
        {    
            writeUint16(dos,endCode[i]);
        }
        writeUint16(dos,0);
        for (int i=0;i<nseg;++i) 
        {    
            writeUint16(dos,startCode[i]);
        }
        for (int i=0;i<nseg;++i) 
        {    
            writeUint16(dos,idDelta[i]);
        }
        for (int i=0;i<nseg;++i) 
        {    
            writeUint16(dos,0);
        }
        LOG.debug("Finished table [cmap].");
        return bos.toByteArray();
    }

    private byte[] buildPostTable() throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        LOG.debug("Building table [post]...");
        PostScriptTable p = this.baseTTF.getPostScript();
        if (p == null)
        {
            // sometimes there is no post table in an embedded subfonts
            // create a dummy
            p = new PostScriptTable();
        }
        String[] glyphNames = p.getGlyphNames();
        /*
            Fixed    format    Format of this table
            Fixed    italicAngle    Italic angle in degrees
            FWord    underlinePosition    Underline position
            FWord    underlineThickness    Underline thickness
            uint32    isFixedPitch    Font is monospaced; set to 1 if the font is monospaced and 0 otherwise 
            (N.B., to maintain compatibility with older versions of the TrueType spec, accept any non-zero value
             as meaning that the font is monospaced)
            uint32    minMemType42    Minimum memory usage when a TrueType font is downloaded as a Type 42 font
            uint32    maxMemType42    Maximum memory usage when a TrueType font is downloaded as a Type 42 font
            uint32    minMemType1    Minimum memory usage when a TrueType font is downloaded as a Type 1 font
            uint32    maxMemType1    Maximum memory usage when a TrueType font is downloaded as a Type 1 font
            uint16    numberOfGlyphs    number of glyphs
            uint16    glyphNameIndex[numberOfGlyphs]    Ordinal number of this glyph in 'post' string tables. 
            This is not an offset.
            Pascal string    names[numberNewGlyphs]  glyph names with length bytes [variable] (a Pascal string)
         */
        writeFixed(dos,2.0);
        writeFixed(dos,p.getItalicAngle());
        writeSint16(dos,p.getUnderlinePosition());
        writeSint16(dos,p.getUnderlineThickness());
        writeUint32(dos,p.getIsFixedPitch());
        writeUint32(dos,p.getMinMemType42());
        writeUint32(dos,p.getMaxMemType42());
        writeUint32(dos,p.getMimMemType1());
        writeUint32(dos,p.getMaxMemType1());
        writeUint16(dos,baseTTF.getHorizontalHeader().getNumberOfHMetrics());
            
        List<String> additionalNames = new ArrayList<String>();
        Map<String,Integer> additionalNamesIndices = new HashMap<String,Integer>();
        
        if (glyphNames == null) 
        {
            Encoding enc = MacRomanEncoding.INSTANCE;
            int[] gidToUC = this.baseCmap.getGlyphIdToCharacterCode();
            for (Integer glyphId : this.glyphIds) 
            {
                int uc = gidToUC[glyphId.intValue()];
                String name = null;
                if (uc < 0x8000) 
                {
                    try 
                    {
                        name = enc.getNameFromCharacter((char)uc);
                    }
                    catch (IOException e) 
                    {
                        // TODO
                    }
                }
                if (name == null) 
                {
                    name = String.format(Locale.ENGLISH,"uni%04X",uc);
                }
                Integer macId = Encoding.MAC_GLYPH_NAMES_INDICES.get(name);
                if (macId == null) 
                {
                    Integer idx = additionalNamesIndices.get(name);
                    if (idx == null) 
                    {
                        idx = additionalNames.size();
                        additionalNames.add(name);
                        additionalNamesIndices.put(name,idx);
                    }
                    writeUint16(dos,idx.intValue()+258);
                }
                else 
                {
                    writeUint16(dos,macId.intValue());
                }
            }
        }
        else 
        { 
            for (Integer glyphId : this.glyphIds) 
            {
                String name = glyphNames[glyphId.intValue()];
                Integer macId = Encoding.MAC_GLYPH_NAMES_INDICES.get(name);
                if (macId == null) 
                {
                    Integer idx = additionalNamesIndices.get(name);
                    if (idx == null) 
                    {
                        idx = additionalNames.size();
                        additionalNames.add(name);
                        additionalNamesIndices.put(name,idx);
                    }
                    writeUint16(dos,idx.intValue()+258);
                }
                else 
                {
                    writeUint16(dos,macId.intValue());
                }
            }
        }
        
        for (String name : additionalNames) 
        {
            LOG.debug("additionalName=["+name+"].");
            byte[] buf = name.getBytes("US-ASCII");
            writeUint8(dos,buf.length);
            dos.write(buf);
        }
        dos.flush();
        LOG.debug("Finished table [post].");
        return bos.toByteArray();
    }

    private byte[] buildHmtxTable() throws IOException 
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LOG.debug("Building table [hmtx]...");
        HorizontalHeaderTable h = this.baseTTF.getHorizontalHeader();
        HorizontalMetricsTable hm = this.baseTTF.getHorizontalMetrics();
        byte [] buf = new byte[4];
        InputStream is = this.baseTTF.getOriginalData();
        try 
        {
            is.skip(hm.getOffset());
            long lastOff = 0;
            for (Integer glyphId : this.glyphIds) 
            {
                // offset in original file.
                long off;
                if (glyphId < h.getNumberOfHMetrics()) 
                {
                    off = glyphId * 4;
                }
                else 
                {
                    off = h.getNumberOfHMetrics() * 4 + (glyphId - h.getNumberOfHMetrics()) * 2;
                }
                // skip over from last original offset.
                if (off != lastOff) 
                {
                    long nskip = off-lastOff;
                    if (nskip != is.skip(nskip)) 
                    {
                        throw new EOFException("Unexpected EOF exception parsing glyphId of hmtx table.");
                    }
                }
                // read left side bearings only, if we are beyond numOfHMetrics.
                int n = glyphId < h.getNumberOfHMetrics() ? 4 : 2;
                if (n != is.read(buf,0,n)) 
                {
                    throw new EOFException("Unexpected EOF exception parsing glyphId of hmtx table.");
                }
                bos.write(buf,0,n);
                lastOff = off + n;
            }
            LOG.debug("Finished table [hmtx].");
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
     */
    public void writeToStream(OutputStream os) throws IOException 
    {
        LOG.debug("glyphIds=[" + glyphIds + "]");
        LOG.debug("numGlyphs=[" + glyphIds.size() + "]");
        while (!addCompoundReferences()) 
        {
        }
        DataOutputStream dos = new DataOutputStream(os);
        try 
        {
            /*
                'cmap'    character to glyph mapping
                'glyf'    glyph data
                'head'    font header
                'hhea'    horizontal header
                'OS/2'  OS/2 compatibility table.
                'hmtx'    horizontal metrics
                'loca'    index to location
                'maxp'    maximum profile
                'name'    naming
                'post'    PostScript
             */
            String[] tableNames = {"OS/2","cmap","glyf","head","hhea","hmtx","loca","maxp","name","post"};
            byte [][] tables = new byte[tableNames.length][];
            long[] newOffsets = new long[this.glyphIds.size()+1];
            tables[3] = this.buildHeadTable();
            tables[4] = this.buildHheaTable();
            tables[7] = this.buildMaxpTable();
            tables[8] = this.buildNameTable();
            tables[0] = this.buildOS2Table();
            tables[2] = this.buildGlyfTable(newOffsets);
            tables[6] = this.buildLocaTable(newOffsets);
            tables[1] = this.buildCmapTable();
            tables[5] = this.buildHmtxTable();
            tables[9] = this.buildPostTable();
            long checksum = writeFileHeader(dos,tableNames.length);
            long offset = 12L + 16L * tableNames.length;
            for (int i=0;i<tableNames.length;++i) 
            {
                checksum += writeTableHeader(dos,tableNames[i],offset,tables[i]);
                offset += ((tables[i].length + 3) / 4) * 4;
            }
            checksum = 0xB1B0AFBAL - (checksum & 0xffffffffL);
            // correct checksumAdjustment of 'head' table.
            tables[3][8] = (byte)(checksum >>> 24);
            tables[3][9] = (byte)(checksum >>> 16);
            tables[3][10] = (byte)(checksum >>> 8);
            tables[3][11] = (byte)(checksum);
            for (int i=0;i<tableNames.length;++i) 
            {
                 writeTableBody(dos,tables[i]);
            }
        }
        finally 
        {
            dos.close();
        }
    }
}
