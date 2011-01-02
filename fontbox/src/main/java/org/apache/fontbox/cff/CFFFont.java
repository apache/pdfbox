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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.fontbox.cff.IndexData;
import org.apache.fontbox.cff.charset.CFFCharset;
import org.apache.fontbox.cff.encoding.CFFEncoding;

/**
 * This class represents a CFF/Type2 Font.
 * 
 * @author Villu Ruusmann
 * @version $Revision$
 */
public class CFFFont
{

    private String fontname = null;
    private Map<String, Object> topDict = new LinkedHashMap<String, Object>();
    private Map<String, Object> privateDict = new LinkedHashMap<String, Object>();
    private CFFEncoding fontEncoding = null;
    private CFFCharset fontCharset = null;
    private Map<String, byte[]> charStringsDict = new LinkedHashMap<String, byte[]>();
    private IndexData globalSubrIndex = null;
    private IndexData localSubrIndex = null;

    /**
     * The name of the font.
     * @return the name of the font
     */
    public String getName()
    {
        return fontname;
    }

    /**
     * Sets the name of the font.
     * @param name the name of the font
     */
    public void setName(String name)
    {
        fontname = name;
    }

    /**
     * Returns the value for the given name from the dictionary.
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
     * @return the dictionary
     */
    public Map<String, Object> getTopDict()
    {
        return topDict;
    }

    /**
     * Adds the given key/value pair to the private dictionary. 
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
     * @return the dictionary
     */
    public Map<String, Object> getPrivateDict()
    {
        return privateDict;
    }

    /**
     * Get the mapping (code/SID/charname/bytes) for this font.
     * @return mappings for codes < 256 and for codes > = 256
     */
    public Collection<Mapping> getMappings()
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
            Mapping mapping = new Mapping();
            mapping.setCode(entry.getCode());
            mapping.setSID(entry.getSID());
            mapping.setName(charName);
            mapping.setBytes(bytes);
            mappings.add(mapping);
            mappedNames.add(charName);
        }
        if (fontEncoding instanceof CFFParser.EmbeddedEncoding)
        {
            CFFParser.EmbeddedEncoding embeddedEncoding = (CFFParser.EmbeddedEncoding)fontEncoding;

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
                Mapping mapping = new Mapping();
                mapping.setCode(supplement.getCode());
                mapping.setSID(supplement.getGlyph());
                mapping.setName(charName);
                mapping.setBytes(bytes);
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
            Mapping mapping = new Mapping();
            mapping.setCode(code++);
            mapping.setSID(entry.getSID());
            mapping.setName(name);
            mapping.setBytes(bytes);

            mappings.add(mapping);

            mappedNames.add(name);
        }
        return mappings;
    }

	/**
	 * Return the Width value of the given Glyph identifier
	 * 
	 * @param SID
	 * @return -1 if the SID is missing from the Font.
	 * @throws IOException
	 */
	public int getWidth(int SID) throws IOException {
		int nominalWidth = privateDict.containsKey("nominalWidthX") ? ((Number)privateDict.get("nominalWidthX")).intValue() : 0;
		int defaultWidth = privateDict.containsKey("defaultWidthX") ? ((Number)privateDict.get("defaultWidthX")).intValue() : 1000 ;
		for (Mapping m : getMappings() ){
			if (m.getSID() == SID) {

				CharStringRenderer csr = null;
				if (((Number)getProperty("CharstringType")).intValue() == 2 ) {
					List<Object> lSeq = m.toType2Sequence();
					csr = new CharStringRenderer(false);
					csr.render(lSeq);
				} else {
					List<Object> lSeq = m.toType1Sequence();
					csr = new CharStringRenderer();
					csr.render(lSeq);
				}

				// ---- If the CharString has a Width nominalWidthX must be added, 
				//	    otherwise it is the default width.
				return csr.getWidth() != 0 ? csr.getWidth() + nominalWidth : defaultWidth;
			}
		}

		// ---- Width not found, return the default width
		return defaultWidth;
	}
    
    /**
     * Returns the CFFEncoding of the font.
     * @return the encoding
     */
    public CFFEncoding getEncoding()
    {
        return fontEncoding;
    }

    /**
     * Sets the CFFEncoding of the font.
     * @param encoding the given CFFEncoding
     */
    public void setEncoding(CFFEncoding encoding)
    {
        fontEncoding = encoding;
    }

    /**
     * Returns the CFFCharset of the font.
     * @return the charset
     */
    public CFFCharset getCharset()
    {
        return fontCharset;
    }

    /**
     * Sets the CFFCharset of the font.
     * @param charset the given CFFCharset
     */
    public void setCharset(CFFCharset charset)
    {
        fontCharset = charset;
    }

    /** 
     * Returns the character strings dictionary.
     * @return the dictionary
     */
    public Map<String, byte[]> getCharStringsDict()
    {
        return charStringsDict;
    }

    /**
     * Creates a CharStringConverter for this font.
     * @return the new CharStringConverter
     */
    public CharStringConverter createConverter()
    {
        Number defaultWidthX = (Number) getProperty("defaultWidthX");
        Number nominalWidthX = (Number) getProperty("nominalWidthX");
        return new CharStringConverter(defaultWidthX.intValue(), nominalWidthX
                .intValue(), getGlobalSubrIndex(), getLocalSubrIndex());
    }

    /**
     * Creates a CharStringRenderer for this font.
     * @return the new CharStringRenderer
     */
    public CharStringRenderer createRenderer()
    {
        return new CharStringRenderer();
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getClass().getName() + "[name=" + fontname + ", topDict=" + topDict
                + ", privateDict=" + privateDict + ", encoding=" + fontEncoding
                + ", charset=" + fontCharset + ", charStringsDict="
                + charStringsDict + "]";
    }

    
    /**
     * Sets the global subroutine index data.
     * @param globalSubrIndex the IndexData object containing the global subroutines 
     */
    public void setGlobalSubrIndex(IndexData globalSubrIndex) {
		this.globalSubrIndex = globalSubrIndex;
	}

    /** 
     * Returns the global subroutine index data.
     * @return the dictionary
     */
	public IndexData getGlobalSubrIndex() {
		return globalSubrIndex;
	}
	
    /** 
     * Returns the local subroutine index data.
     * @return the dictionary
     */
	public IndexData getLocalSubrIndex() {
		return localSubrIndex;
	}
	
    /**
     * Sets the local subroutine index data.
     * @param localSubrIndex the IndexData object containing the local subroutines 
     */
	public void setLocalSubrIndex(IndexData localSubrIndex) {
		this.localSubrIndex = localSubrIndex;	
	}

	/**
     * This class is used for the font mapping.
     *
     */
    public class Mapping
    {
        private int mappedCode;
        private int mappedSID;
        private String mappedName;
        private byte[] mappedBytes;

        /**
         * Converts the mapping into a Type1-sequence.
         * @return the Type1-sequence
         * @throws IOException if an error occurs during reading
         */
        public List<Object> toType1Sequence() throws IOException
        {
            CharStringConverter converter = createConverter();
            return converter.convert(toType2Sequence());
        }

        /**
         * Converts the mapping into a Type2-sequence.
         * @return the Type2-sequence
         * @throws IOException if an error occurs during reading
         */
        public List<Object> toType2Sequence() throws IOException
        {
            Type2CharStringParser parser = new Type2CharStringParser();
            return parser.parse(getBytes());
        }

        /**
         * Gets the value for the code.
         * @return the code
         */
        public int getCode()
        {
            return mappedCode;
        }

        private void setCode(int code)
        {
            mappedCode = code;
        }

        /**
         * Gets the value for the SID.
         * @return the SID
         */
        public int getSID()
        {
            return mappedSID;
        }

        private void setSID(int sid)
        {
            this.mappedSID = sid;
        }

        /**
         * Gets the value for the name.
         * @return the name
         */
        public String getName()
        {
            return mappedName;
        }

        private void setName(String name)
        {
            this.mappedName = name;
        }

        /**
         * Gets the value for the bytes.
         * @return the bytes
         */
        public byte[] getBytes()
        {
            return mappedBytes;
        }

        private void setBytes(byte[] bytes)
        {
            this.mappedBytes = bytes;
        }
    }
}