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

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.fontbox.type1.Type1CharStringReader;

/**
 * A Type 0 CIDFont represented in a CFF file. Thread safe.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class CFFCIDFont extends CFFFont
{
    private String registry;
    private String ordering;
    private int supplement;

    private List<Map<String, Object>> fontDictionaries = new LinkedList<Map<String,Object>>();
    private List<Map<String, Object>> privateDictionaries = new LinkedList<Map<String,Object>>();
    private FDSelect fdSelect;

    private final Map<Integer, CIDKeyedType2CharString> charStringCache =
            new ConcurrentHashMap<Integer, CIDKeyedType2CharString>();

    private final PrivateType1CharStringReader reader = new PrivateType1CharStringReader();

    /**
     * Returns the registry value.
     * * @return the registry
     */
    public String getRegistry() 
    {
        return registry;
    }

    /**
     * Sets the registry value.
     *
     * @param registry the registry to set
     */
    void setRegistry(String registry)
    {
        this.registry = registry;
    }

    /**
     * Returns the ordering value.
     *
     * @return the ordering
     */
    public String getOrdering() 
    {
        return ordering;
    }

    /**
     * Sets the ordering value.
     *
     * @param ordering the ordering to set
     */
    void setOrdering(String ordering)
    {
        this.ordering = ordering;
    }

    /**
     * Returns the supplement value.
     *
     * @return the supplement
     */
    public int getSupplement() 
    {
        return supplement;
    }

    /**
     * Sets the supplement value.
     *
     * @param supplement the supplement to set
     */
    void setSupplement(int supplement)
    {
        this.supplement = supplement;
    }

    /**
     * Returns the font dictionaries.
     *
     * @return the fontDict
     */
    public List<Map<String, Object>> getFontDicts()
    {
        return fontDictionaries;
    }

    /**
     * Sets the font dictionaries.
     *
     * @param fontDict the fontDict to set
     */
    void setFontDict(List<Map<String, Object>> fontDict)
    {
        this.fontDictionaries = fontDict;
    }

    /**
     * Returns the private dictionary.
     *
     * @return the privDict
     */
    public List<Map<String, Object>> getPrivDicts()
    {
        return privateDictionaries;
    }

    /**
     * Sets the private dictionary.
     *
     * @param privDict the privDict to set
     */
    void setPrivDict(List<Map<String, Object>> privDict)
    {
        this.privateDictionaries = privDict;
    }

    /**
     * Returns the fdSelect value.
     *
     * @return the fdSelect
     */
    public FDSelect getFdSelect()
    {
        return fdSelect;
    }

    /**
     * Sets the fdSelect value.
     *
     * @param fdSelect the fdSelect to set
     */
    void setFdSelect(FDSelect fdSelect)
    {
        this.fdSelect = fdSelect;
    }

    /**
     * Returns the defaultWidthX for the given GID.
     *
     * @param gid GID
     */
    private int getDefaultWidthX(int gid)
    {
        int fdArrayIndex = this.fdSelect.getFDIndex(gid);
        if (fdArrayIndex == -1)
        {
            return 1000;
        }
        Map<String, Object> privDict = this.privateDictionaries.get(fdArrayIndex);
        return privDict.containsKey("defaultWidthX") ? ((Number)privDict.get("defaultWidthX")).intValue() : 1000;
    }

    /**
     * Returns the nominalWidthX for the given GID.
     *
     * @param gid GID
     */
    private int getNominalWidthX(int gid)
    {
        int fdArrayIndex = this.fdSelect.getFDIndex(gid);
        if (fdArrayIndex == -1)
        {
            return 0;
        }
        Map<String, Object> privDict = this.privateDictionaries.get(fdArrayIndex);
        return privDict.containsKey("nominalWidthX") ? ((Number)privDict.get("nominalWidthX")).intValue() : 0;
    }

    /**
     * Returns the LocalSubrIndex for the given GID.
     *
     * @param gid GID
     */
    private IndexData getLocalSubrIndex(int gid)
    {
        int fdArrayIndex = this.fdSelect.getFDIndex(gid);
        if (fdArrayIndex == -1)
        {
            return new IndexData(0);
        }
        Map<String, Object> privDict = this.privateDictionaries.get(fdArrayIndex);
        return (IndexData)privDict.get("Subrs");
    }

    /**
     * Returns the Type 2 charstring for the given CID.
     *
     * @param cid CID
     * @throws IOException if the charstring could not be read
     */
    public CIDKeyedType2CharString getType2CharString(int cid) throws IOException
    {
        CIDKeyedType2CharString type2 = charStringCache.get(cid);
        if (type2 == null)
        {
            int gid = charset.getGIDForCID(cid);

            byte[] bytes = charStrings.get(gid);
            if (bytes == null)
            {
                bytes = charStrings.get(0); // .notdef
            }
            Type2CharStringParser parser = new Type2CharStringParser(fontName, cid);
            List<Object> type2seq = parser.parse(bytes, globalSubrIndex, getLocalSubrIndex(gid));
            type2 = new CIDKeyedType2CharString(reader, fontName, cid, gid, type2seq,
                                                getDefaultWidthX(gid), getNominalWidthX(gid));
            charStringCache.put(cid, type2);
        }
        return type2;
    }

    @Override
    public List<Number> getFontMatrix()
    {
        // our parser guarantees that FontMatrix will be present and correct in the Top DICT
        return (List<Number>)topDict.get("FontMatrix");
    }

    @Override
    public GeneralPath getPath(String selector) throws IOException
    {
        int cid = selectorToCID(selector);
        return getType2CharString(cid).getPath();
    }

    @Override
    public float getWidth(String selector) throws IOException
    {
        int cid = selectorToCID(selector);
        return getType2CharString(cid).getWidth();
    }

    @Override
    public boolean hasGlyph(String selector) throws IOException
    {
        int cid = selectorToCID(selector);
        return cid != 0;
    }

    /**
     * Parses a CID selector of the form \ddddd.
     */
    private int selectorToCID(String selector)
    {
        if (!selector.startsWith("\\"))
        {
            throw new IllegalArgumentException("Invalid selector");
        }
        return Integer.parseInt(selector.substring(1));
    }

    /**
     * Private implementation of Type1CharStringReader, because only CFFType1Font can
     * expose this publicly, as CIDFonts only support this for legacy 'seac' commands.
     */
    private class PrivateType1CharStringReader implements Type1CharStringReader
    {
        @Override
        public Type1CharString getType1CharString(String name) throws IOException
        {
            return CFFCIDFont.this.getType2CharString(0); // .notdef
        }
    }
}
