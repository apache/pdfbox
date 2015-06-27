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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.FontBoxFont;
import org.apache.fontbox.util.BoundingBox;

/**
 * An Adobe Compact Font Format (CFF) font. Thread safe.
 * 
 * @author Villu Ruusmann
 * @author John Hewson
 */
public abstract class CFFFont implements FontBoxFont
{
    protected String fontName;
    protected final Map<String, Object> topDict = new LinkedHashMap<String, Object>();
    protected CFFCharset charset;
    protected final List<byte[]> charStrings = new ArrayList<byte[]>();
    protected IndexData globalSubrIndex;
    private byte[] data;

    /**
     * The name of the font.
     *
     * @return the name of the font
     */
    public String getName()
    {
        return fontName;
    }

    /**
     * Sets the name of the font.
     *
     * @param name the name of the font
     */
    void setName(String name)
    {
        fontName = name;
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
     * Returns the FontMatrix.
     */
    public abstract List<Number> getFontMatrix();

    /**
     * Returns the FontBBox.
     */
    public BoundingBox getFontBBox()
    {
        List<Number> numbers = (List<Number>)topDict.get("FontBBox");
        return new BoundingBox(numbers);
    }

    /**
     * Returns the CFFCharset of the font.
     * 
     * @return the charset
     */
    public CFFCharset getCharset()
    {
        return charset;
    }

    /**
     * Sets the CFFCharset of the font.
     * 
     * @param charset the given CFFCharset
     */
    void setCharset(CFFCharset charset)
    {
        this.charset = charset;
    }

    /**
     * Returns the character strings dictionary.
     *
     * @return the dictionary
     */
    List<byte[]> getCharStringBytes()
    {
        return charStrings;
    }

    /**
     * Sets the original data.
     *
     * @param data the original data.
     */
    void setData(byte[] data)
    {
        this.data = data;
    }

    /**
     * Returns the the original data.
     *
     * @return the dictionary
     */
    public byte[] getData()
    {
        return data;
    }
    
    /**
     * Returns the number of charstrings in the font.
     */
    public int getNumCharStrings()
    {
        return charStrings.size();
    }

    /**
     * Sets the global subroutine index data.
     * 
     * @param globalSubrIndexValue the IndexData object containing the global subroutines
     */
    void setGlobalSubrIndex(IndexData globalSubrIndexValue)
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
     * Returns the Type 2 charstring for the given CID.
     *
     * @param cidOrGid CID for CIFFont, or GID for Type 1 font
     * @throws IOException if the charstring could not be read
     */
    public abstract Type2CharString getType2CharString(int cidOrGid) throws IOException;

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "[name=" + fontName + ", topDict=" + topDict
                + ", charset=" + charset + ", charStrings=" + charStrings
                + "]";
    }
}
