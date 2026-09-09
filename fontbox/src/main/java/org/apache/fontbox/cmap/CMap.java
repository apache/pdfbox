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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a CMap file.
 *
 * @author Ben Litchfield
 */
public class CMap
{
    private static final Log LOG = LogFactory.getLog(CMap.class);

    private int wmode = 0;
    private String cmapName = null;
    private String cmapVersion = null;
    private int cmapType = -1;

    private String registry = null;
    private String ordering = null;
    private int supplement = 0;

    private int minCodeLength = 4;
    private int maxCodeLength;

    // code lengths
    private final List<CodespaceRange> codespaceRanges = new ArrayList<CodespaceRange>();

    // Unicode mappings
    private final Map<Integer,String> charToUnicode = new HashMap<Integer,String>();

    // inverted map
    private final Map <String, byte[]> unicodeToByteCodes = new HashMap<String, byte[]>();

    // CID mappings
    // map with all code to cid mappings organized by the origin byte length of the input value
    private final Map<Integer,Integer> codeToCid = new HashMap<Integer,Integer>();
    private final List<CIDRange> codeToCidRanges = new ArrayList<CIDRange>();

    // the CMaps this one inherits from through the usecmap operator, see useCmap
    private final List<CMap> parentCMaps = new ArrayList<CMap>();

    private static final String SPACE = " ";
    private int spaceMapping = -1;

    /**
     * Creates a new instance of CMap.
     */
    CMap()
    {
    }

    /**
     * This will tell if this cmap has any CID mappings.
     * 
     * @return true If there are any CID mappings, false otherwise.
     */
    public boolean hasCIDMappings()
    {
        return !codeToCid.isEmpty() || !codeToCidRanges.isEmpty() || hasCIDMappings(parentCMaps);
    }

    private boolean hasCIDMappings(List<CMap> parentCMaps)
    {
        for (CMap cmap : parentCMaps)
        {
            if (cmap.hasCIDMappings())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This will tell if this cmap has any Unicode mappings.
     *
     * @return true If there are any Unicode mappings, false otherwise.
     */
    public boolean hasUnicodeMappings()
    {
        return !charToUnicode.isEmpty();
    }

    /**
     * Returns the sequence of Unicode characters for the given character code.
     *
     * @param code character code
     * @return Unicode characters (may be more than one, e.g "fi" ligature)
     */
    public String toUnicode(int code)
    {
        return charToUnicode.get(code);
    }

    /**
     * Reads a character code from a string in the content stream.
     * <p>See "CMap Mapping" and "Handling Undefined Characters" in PDF32000 for more details.
     *
     * @param in string stream
     * @return character code
     * @throws IOException if there was an error reading the stream or CMap
     */
    public int readCode(InputStream in) throws IOException
    {
        byte[] bytes = new byte[maxCodeLength];
        in.read(bytes,0,minCodeLength);
        in.mark(maxCodeLength);
        for (int i = minCodeLength-1; i < maxCodeLength; i++)
        {
            final int byteCount = i+1;
            for (CodespaceRange range : codespaceRanges)
            {
                if (range.isFullMatch(bytes, byteCount))
                {
                    return toInt(bytes, byteCount);
                }
            }
            if (byteCount < maxCodeLength)
            {
                bytes[byteCount] = (byte)in.read();
            }
        }
        if (LOG.isWarnEnabled())
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < maxCodeLength; ++i)
            {
                sb.append(String.format("0x%02X (%04o) ", bytes[i], bytes[i]));
            }
            LOG.warn("Invalid character code sequence " + sb + "in CMap " + cmapName);
        }
        // PDFBOX-4811 reposition to where we were after initial read
        if (in.markSupported())
        {
            in.reset();
        }
        else
        {
            LOG.warn("mark() and reset() not supported, " + (maxCodeLength - 1) +
                     " bytes have been skipped");
        }
        return toInt(bytes, minCodeLength); // Adobe Reader behavior
    }

    /**
     * Returns an int for the given byte array
     */
    static int toInt(byte[] data, int dataLen)
    {
        int code = 0;
        for (int i = 0; i < dataLen; ++i)
        {
            code <<= 8;
            code |= (data[i] & 0xFF);
        }
        return code;
    }

    /**
     * Returns the CID for the given character code.
     *
     * This method exists for convenience. It may return false values as the origin byte length of the input value is
     * unknown and the mapping for some input values aren't unique. <br>
     * Example:<br>
     * The two byte value 0x00, 0x65 maps to 0x20 <br>
     * An input value of 0x65 always returns 0x20 even if the value has an origin byte length of 1.
     *
     * @param code character code
     * @return CID
     */
    public int toCID(int code)
    {
        int cid = findCID(code);
        if (cid != -1)
        {
            return cid;
        }
        return 0;
    }

    /**
     * Returns the CID this CMap, or one of the CMaps it inherits from, maps the given character code
     * to, or -1 if none of them maps it. CID 0 is the .notdef glyph and a CMap may map a code to it
     * deliberately, so "mapped to 0" has to be told apart from "not mapped" while the usecmap chain
     * is walked. The public toCID methods report both as 0.
     *
     * @param code   character code
     * @return CID, or -1 if neither this CMap nor any it inherits from maps the code
     */
    private int findCID(int code)
    {
        Integer cid = codeToCid.get(code);
        if (cid != null)
        {
            return cid;
        }
        int cidFromRange = toCIDFromRanges(code);
        if (cidFromRange != -1)
        {
            return cidFromRange;
        }
        // this CMap doesn't map the code itself, so ask the ones it inherits from
        for (CMap parentCMap : parentCMaps)
        {
            int parentCid = parentCMap.findCID(code);
            if (parentCid != -1)
            {
                return parentCid;
            }
        }
        return -1;
    }

    /**
     * Returns the CID, the CID ranges of this CMap map the given character code to.
     *
     * @param code   character code
     * @return CID, or -1 if no range covers the code
     */
    private int toCIDFromRanges(int code)
    {
        for (CIDRange range : codeToCidRanges)
        {
            int ch = range.map((char)code);
            if (ch != -1)
            {
                return ch;
            }
        }
        return -1;
    }

    /**
     * This will add a character code to Unicode character sequence mapping.
     *
     * @param codes The character codes to map from.
     * @param unicode The Unicode characters to map to.
     */
    void addCharMapping(byte[] codes, String unicode)
    {
        if (codes.length == 0)
        {
            return;
        }
        if (codes.length <= 2)
        {
            unicodeToByteCodes.put(unicode, CMapStrings.getByteValue(codes));
            charToUnicode.put(CMapStrings.getIndexValue(codes), unicode);
        }
        else
        {
            unicodeToByteCodes.put(unicode, codes.clone());
            charToUnicode.put(toInt(codes, codes.length), unicode);
        }
        // fixme: ugly little hack
        if (SPACE.equals(unicode))
        {
            spaceMapping = toInt(codes, codes.length);
        }
    }

    /**
     * Get the code bytes for an unicode string.
     *
     * @param unicode The unicode string.
     * @return the code bytes or null if there is none.
     */
    public byte[] getCodesFromUnicode(String unicode)
    {
        return unicodeToByteCodes.get(unicode);
    }

    /**
     * This will add a CID mapping.
     * <p>
     * <b>This method had wrong parameter names until 2.0.38</b>
     *
     * @param cid character code
     * @param code CID
     */
    void addCIDMapping(int cid, int code)
    {
        codeToCid.put(code, cid);
    }

    /**
     * This will add a CID Range.
     *
     * @param from starting character of the CID range.
     * @param to ending character of the CID range.
     * @param cid the cid to be started with.
     *
     */
    void addCIDRange(char from, char to, int cid)
    {
        addCIDRange(codeToCidRanges, from, to, cid);
    }

    private void addCIDRange(List<CIDRange> cidRanges, char from, char to, int cid)
    {
        CIDRange lastRange = null;
        if (!cidRanges.isEmpty())
        {
            lastRange = cidRanges.get(cidRanges.size() - 1);
        }
        if (lastRange == null || !lastRange.extend(from, to, cid))
        {
            cidRanges.add(new CIDRange(from, to, cid));
        }
    }

    /**
     * This will add a codespace range.
     *
     * @param range A single codespace range.
     */
    void addCodespaceRange( CodespaceRange range )
    {
        codespaceRanges.add(range);
        maxCodeLength = Math.max(maxCodeLength, range.getCodeLength());
        minCodeLength = Math.min(minCodeLength, range.getCodeLength());
    }
    
    /**
     * Implementation of the usecmap operator.  This will
     * copy all of the mappings from one cmap to another.
     * 
     * @param cmap The cmap to load mappings from.
     */
    void useCmap( CMap cmap )
    {
        for (CodespaceRange codespaceRange : cmap.codespaceRanges)
        {
            addCodespaceRange(codespaceRange);
        }
        charToUnicode.putAll(cmap.charToUnicode);
        // unicodeToByteCodes should be filled too, but this isn't possible in 2.0.*
        // because we don't know the code length

        // The parent is kept, not merged: it is asked only for codes this CMap doesn't map itself,
        // so this CMap's own mappings win and a usecmap chain resolves nearest-first. See toCID(int, int).
        parentCMaps.add(cmap);
        maxCodeLength = Math.max(maxCodeLength, cmap.maxCodeLength);
        minCodeLength = Math.min(minCodeLength, cmap.minCodeLength);
    }

    /**
     * Returns the WMode of a CMap.
     *
     * 0 represents a horizontal and 1 represents a vertical orientation.
     * 
     * @return the wmode
     */
    public int getWMode() 
    {
        return wmode;
    }

    /**
     * Sets the WMode of a CMap.
     * 
     * @param newWMode the new WMode.
     */
    public void setWMode(int newWMode) 
    {
        wmode = newWMode;
    }

    /**
     * Returns the name of the CMap.
     * 
     * @return the CMap name.
     */
    public String getName() 
    {
        return cmapName;
    }

    /**
     * Sets the name of the CMap.
     * 
     * @param name the CMap name.
     */
    public void setName(String name) 
    {
        cmapName = name;
    }

    /**
     * Returns the version of the CMap.
     * 
     * @return the CMap version.
     */
    public String getVersion() 
    {
        return cmapVersion;
    }

    /**
     * Sets the version of the CMap.
     * 
     * @param version the CMap version.
     */
    public void setVersion(String version) 
    {
        cmapVersion = version;
    }

    /**
     * Returns the type of the CMap.
     * 
     * @return the CMap type.
     */
    public int getType() 
    {
        return cmapType;
    }

    /**
     * Sets the type of the CMap.
     * 
     * @param type the CMap type.
     */
    public void setType(int type) 
    {
        cmapType = type;
    }

    /**
     * Returns the registry of the CIDSystemInfo.
     * 
     * @return the registry.
     */
    public String getRegistry() 
    {
        return registry;
    }

    /**
     * Sets the registry of the CIDSystemInfo.
     * 
     * @param newRegistry the registry.
     */
    public void setRegistry(String newRegistry) 
    {
        registry = newRegistry;
    }

    /**
     * Returns the ordering of the CIDSystemInfo.
     * 
     * @return the ordering.
     */
    public String getOrdering() 
    {
        return ordering;
    }

    /**
     * Sets the ordering of the CIDSystemInfo.
     * 
     * @param newOrdering the ordering.
     */
    public void setOrdering(String newOrdering) 
    {
        ordering = newOrdering;
    }

    /**
     * Returns the supplement of the CIDSystemInfo.
     * 
     * @return the supplement.
     */
    public int getSupplement() 
    {
        return supplement;
    }

    /**
     * Sets the supplement of the CIDSystemInfo.
     * 
     * @param newSupplement the supplement.
     */
    public void setSupplement(int newSupplement) 
    {
        supplement = newSupplement;
    }
    
    /** 
     * Returns the mapping for the space character.
     * 
     * @return the mapped code for the space character
     */
    public int getSpaceMapping()
    {
        return spaceMapping;
    }

    @Override
    public String toString()
    {
        return cmapName;
    }
}
