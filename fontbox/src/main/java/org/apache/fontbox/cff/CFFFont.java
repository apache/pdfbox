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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fontbox.cff.charset.CFFCharset;
import org.apache.fontbox.cff.encoding.CFFEncoding;
import org.apache.fontbox.type1.Type1CharStringReader;
import org.apache.fontbox.type1.Type1Mapping;

/**
 * This class represents a CFF/Type2 Font.
 * 
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class CFFFont implements Type1CharStringReader
{
    private String fontname = null;
    private Map<String, Object> topDict = new LinkedHashMap<String, Object>();
    private Map<String, Object> privateDict = new LinkedHashMap<String, Object>();
    private CFFEncoding fontEncoding = null;
    private CFFCharset fontCharset = null;
    private Map<String, byte[]> charStringsDict = new LinkedHashMap<String, byte[]>();
    private IndexData globalSubrIndex = null;
    private IndexData localSubrIndex = null;
    private Map<String, Type2CharString> charStringCache = new HashMap<String, Type2CharString>();

    /**
     * The name of the font.
     * 
     * @return the name of the font
     */
    public String getName()
    {
        return fontname;
    }

    /**
     * Sets the name of the font.
     * 
     * @param name the name of the font
     */
    public void setName(String name)
    {
        fontname = name;
    }

    /**
     * Returns the value for the given name from the dictionary.
     * 
     * @param name the name of the value
     * @return the value of the name if available
     */
    public Object getProperty(String name)
    {
        Object topDictValue = topDict.get(name);
        if (topDictValue != null)
        {
            return topDictValue;
        }
        Object privateDictValue = privateDict.get(name);
        if (privateDictValue != null)
        {
            return privateDictValue;
        }
        return null;
    }

    /**
     * Adds the given key/value pair to the top dictionary.
     * 
     * @param name the given key
     * @param value the given value
     */
    public void addValueToTopDict(String name, Object value)
    {
        if (value != null)
        {
            topDict.put(name, value);
        }
    }

    /**
     * Returns the top dictionary.
     * 
     * @return the dictionary
     */
    public Map<String, Object> getTopDict()
    {
        return topDict;
    }

    /**
     * Adds the given key/value pair to the private dictionary.
     * 
     * @param name the given key
     * @param value the given value
     */
    public void addValueToPrivateDict(String name, Object value)
    {
        if (value != null)
        {
            privateDict.put(name, value);
        }
    }

    /**
     * Returns the private dictionary.
     * 
     * @return the dictionary
     */
    public Map<String, Object> getPrivateDict()
    {
        return privateDict;
    }

    /**
     * Get the mappings for the font as Type1Mapping
     *
     * @return the Type1Mapping
     */
    public Collection<? extends Type1Mapping> getType1Mappings()
    {
        return getMappings();
    }

    /**
     * Get the mapping (code/SID/charname/bytes) for this font.
     * 
     * @return mappings for codes &lt; 256 and for codes &gt;= 256
     */
    public Collection<CFFFont.Mapping> getMappings()
    {
        List<Mapping> mappings = new ArrayList<Mapping>();
        Set<String> mappedNames = new HashSet<String>();
        for (CFFEncoding.Entry entry : fontEncoding.getEntries())
        {
            String charName = fontCharset.getName(entry.getSID());
            // Predefined encoding
            if (charName == null)
            {
                continue;
            }
            byte[] bytes = charStringsDict.get(charName);
            if (bytes == null)
            {
                continue;
            }
            Mapping mapping = createMapping(entry.getCode(), entry.getSID(), charName, bytes);
            mappings.add(mapping);
            mappedNames.add(charName);
        }
        if (fontEncoding instanceof CFFParser.EmbeddedEncoding)
        {
            CFFParser.EmbeddedEncoding embeddedEncoding = (CFFParser.EmbeddedEncoding) fontEncoding;

            for (CFFParser.EmbeddedEncoding.Supplement supplement : embeddedEncoding.getSupplements())
            {
                String charName = fontCharset.getName(supplement.getGlyph());
                if (charName == null)
                {
                    continue;
                }
                byte[] bytes = charStringsDict.get(charName);
                if (bytes == null)
                {
                    continue;
                }
                Mapping mapping = createMapping(supplement.getCode(), supplement.getGlyph(), charName, bytes);
                mappings.add(mapping);
                mappedNames.add(charName);
            }
        }
        // XXX
        int code = 256;
        for (CFFCharset.Entry entry : fontCharset.getEntries())
        {
            String name = entry.getName();
            if (mappedNames.contains(name))
            {
                continue;
            }
            byte[] bytes = this.charStringsDict.get(name);
            if (bytes == null)
            {
                continue;
            }
            Mapping mapping = createMapping(code++, entry.getSID(), name, bytes);
            mappings.add(mapping);
            mappedNames.add(name);
        }
        return mappings;
    }

    /**
     * Returns a new mapping. Overridden in subclasses.
     *
     * @param code chatacter code
     * @param sid SID
     * @param name glyph name
     * @param bytes charstring bytes
     * @return a new mapping object
     */
    protected Mapping createMapping(int code, int sid, String name, byte[] bytes)
    {
        Mapping mapping = new Mapping();
        mapping.setCode(code);
        mapping.setSID(sid);
        mapping.setName(name);
        mapping.setBytes(bytes);
        return  mapping;
    }

    /**
     * Return the Width value of the given Glyph identifier.
     * 
     * @param sid SID
     * @return -1 if the SID is missing from the Font.
     * @throws IOException if something went wrong
     * 
     */
    public int getWidth(int sid) throws IOException
    {
        for (Mapping m : getMappings())
        {
            if (m.getSID() == sid)
            {
                Type1CharString charstring = m.getType1CharString();
                return charstring.getWidth();
            }
        }

        // SID not found, return the nodef width
        int nominalWidth = getNominalWidthX(sid);
        int defaultWidth = getDefaultWidthX(sid);
        return getNotDefWidth(defaultWidth, nominalWidth);
    }

    /**
     * Returns the witdth of the .notdef character.
     * 
     * @param defaultWidth default width
     * @param nominalWidth nominal width
     * @return the calculated width for the .notdef character
     * @throws IOException if something went wrong
     */
    protected int getNotDefWidth(int defaultWidth, int nominalWidth) throws IOException
    {
        Type1CharString charstring = getType1CharString(".notdef");
        return charstring.getWidth() != 0 ? charstring.getWidth() + nominalWidth : defaultWidth;
    }

    /**
     * Returns the CFFEncoding of the font.
     * 
     * @return the encoding
     */
    public CFFEncoding getEncoding()
    {
        return fontEncoding;
    }

    /**
     * Sets the CFFEncoding of the font.
     * 
     * @param encoding the given CFFEncoding
     */
    public void setEncoding(CFFEncoding encoding)
    {
        fontEncoding = encoding;
    }

    /**
     * Returns the CFFCharset of the font.
     * 
     * @return the charset
     */
    public CFFCharset getCharset()
    {
        return fontCharset;
    }

    /**
     * Sets the CFFCharset of the font.
     * 
     * @param charset the given CFFCharset
     */
    public void setCharset(CFFCharset charset)
    {
        fontCharset = charset;
    }

    /**
     * Returns the SID for a given glyph name.
     * @param name glyph name
     * @return SID
     */
    private int getSIDForName(String name)
    {
        int sid = 0; // .notdef
        for (Mapping m : getMappings())
        {
            if (m.getName().equals(name))
            {
                sid = m.getSID();
                break;
            }
        }
      return sid;
    }

    /**
     * Returns the character strings dictionary.
     * 
     * @return the dictionary
     */
    public Map<String, byte[]> getCharStringsDict()
    {
        return charStringsDict;
    }

    /**
     * {@inheritDoc}
     */
    public Type1CharString getType1CharString(String name) throws IOException
    {
        return getType1CharString(name, getSIDForName(name));
    }

    /**
     * Returns the Type 1 CharString for the character with the given name and SID.
     *
     * @return Type 1 CharString
     */
    private Type1CharString getType1CharString(String name, int sid) throws IOException
    {
        Type2CharString type2 = charStringCache.get(name);
        if (type2 == null)
        {
            Type2CharStringParser parser = new Type2CharStringParser();
            List<Object> type2seq = parser.parse(charStringsDict.get(name), globalSubrIndex, localSubrIndex);
            type2 = new Type2CharString(this, fontname, name, type2seq, getDefaultWidthX(sid), getNominalWidthX(sid));
            charStringCache.put(name, type2);
        }
        return type2;
    }

    /**
     * Returns the defaultWidthX for the given SID.
     *
     * @param sid SID
     * @return defaultWidthX
     */
    protected int getDefaultWidthX(int sid)
    {
        Number num = (Number)getProperty("defaultWidthX");
        if (num == null)
        {
            return 1000;
        }
        return num.intValue();
    }

    /**
     * Returns the nominalWidthX for the given SID.
     *
     * @param sid SID
     * @return defaultWidthX
     */
    protected int getNominalWidthX(int sid)
    {
        Number num = (Number)getProperty("nominalWidthX");
        if (num == null)
        {
            return 0;
        }
        return num.intValue();
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getClass().getName() + "[name=" + fontname + ", topDict=" + topDict + ", privateDict=" + privateDict
                + ", encoding=" + fontEncoding + ", charset=" + fontCharset + ", charStringsDict=" + charStringsDict
                + "]";
    }

    /**
     * Sets the global subroutine index data.
     * 
     * @param globalSubrIndexValue the IndexData object containing the global subroutines
     */
    public void setGlobalSubrIndex(IndexData globalSubrIndexValue)
    {
        globalSubrIndex = globalSubrIndexValue;
    }

    /**
     * Returns the global subroutine index data.
     * 
     * @return the dictionary
     */
    public IndexData getGlobalSubrIndex()
    {
        return globalSubrIndex;
    }

    /**
     * Returns the local subroutine index data.
     * 
     * @return the dictionary
     */
    public IndexData getLocalSubrIndex()
    {
        return localSubrIndex;
    }

    /**
     * Sets the local subroutine index data.
     * 
     * @param localSubrIndexValue the IndexData object containing the local subroutines
     */
    public void setLocalSubrIndex(IndexData localSubrIndexValue)
    {
        localSubrIndex = localSubrIndexValue;
    }

    /**
     * {@inheritDoc}
     */
    public class Mapping implements Type1Mapping
    {
        private int mappedCode;
        private int mappedSID;
        private String mappedName;
        private byte[] mappedBytes;

        /**
         * {@inheritDoc}
         */
        public Type1CharString getType1CharString() throws IOException
        {
            return CFFFont.this.getType1CharString(mappedName, mappedSID);
        }

        /**
         * {@inheritDoc}
         */
        public int getCode()
        {
            return mappedCode;
        }

        protected void setCode(int code)
        {
            mappedCode = code;
        }

        /**
         * Gets the value for the SID.
         *
         * @return the SID
         */
        public int getSID()
        {
            return mappedSID;
        }

        protected void setSID(int sid)
        {
            this.mappedSID = sid;
        }

        /**
         * {@inheritDoc}
         */
        public String getName()
        {
            return mappedName;
        }

        protected void setName(String name)
        {
            this.mappedName = name;
        }

        /**
         * {@inheritDoc}
         */
        public byte[] getBytes()
        {
            return mappedBytes;
        }

        protected void setBytes(byte[] bytes)
        {
            this.mappedBytes = bytes;
        }
    }
}
