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
import java.util.Arrays;
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
    protected byte[][] charStrings;
    protected byte[][] globalSubrIndex;
    private CFFParser.ByteSource source;

    /**
     * The name of the font.
     *
     * @return the name of the font
     */
    @Override
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
    @Override
    public abstract List<Number> getFontMatrix();

    /**
     * Returns the FontBBox.
     */
    @Override
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
     * Returns the character strings dictionary. For expert users only.
     *
     * @return the dictionary
     */
    public final List<byte[]> getCharStringBytes()
    {
        return Arrays.asList(charStrings);
    }

    /**
     * Sets a byte source to re-read the CFF data in the future.
     */
    final void setData(CFFParser.ByteSource source)
    {
        this.source = source;
    }
    
    /**
     * Returns the CFF data.
     */
    public byte[] getData() throws IOException
    {
        return source.getBytes();
    }
    
    /**
     * Returns the number of charstrings in the font.
     */
    public int getNumCharStrings()
    {
        return charStrings.length;
    }

    /**
     * Sets the global subroutine index data.
     * 
     * @param globalSubrIndexValue an list containing the global subroutines
     */
    void setGlobalSubrIndex(byte[][] globalSubrIndexValue)
    {
        globalSubrIndex = globalSubrIndexValue;
    }

    /**
     * Returns the list containing the global subroutine .
     * 
     * @return the dictionary
     */
    public List<byte[]> getGlobalSubrIndex()
    {
        return Arrays.asList(globalSubrIndex);
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
                + ", charset=" + charset + ", charStrings=" + Arrays.deepToString(charStrings)
                + "]";
    }
}
