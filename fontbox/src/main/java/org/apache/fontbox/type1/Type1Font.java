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

package org.apache.fontbox.type1;

import org.apache.fontbox.cff.Type1CharString;
import org.apache.fontbox.cff.Type1CharStringParser;
import org.apache.fontbox.encoding.Encoding;
import org.apache.fontbox.pfb.PfbParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an Adobe Type 1 (.pfb) font.
 *
 * @author John Hewson
 */
public final class Type1Font implements Type1CharStringReader
{
    /**
     * Constructs a new Type1Font object from a .pfb stream.
     *
     * @param pfbStream .pfb input stream, including headers
     * @return a type1 font
     * 
     * @throws IOException if something went wrong
     */
    public static Type1Font createWithPFB(InputStream pfbStream) throws IOException
    {
        PfbParser pfb = new PfbParser(pfbStream);
        Type1Parser parser = new Type1Parser();
        return parser.parse(pfb.getSegment1(), pfb.getSegment2());
    }

    /**
     * Constructs a new Type1Font object from two header-less .pfb segments.
     *
     * @param segment1 The first segment, without header
     * @param segment2 The second segment, without header
     * @return A new Type1Font instance
     * @throws IOException if something went wrong
     */
    public static Type1Font createWithSegments(byte[] segment1, byte[] segment2) throws IOException
    {
        Type1Parser parser = new Type1Parser();
        return parser.parse(segment1, segment2);
    }

    // font dictionary
    String fontName = "";
    Encoding encoding = null;
    int paintType;
    int fontType;
    List<Number> fontMatrix = new ArrayList<Number>();
    List<Number> fontBBox = new ArrayList<Number>();
    int uniqueID;
    float strokeWidth;
    String fontID = "";

    // FontInfo dictionary
    String version = "";
    String notice = "";
    String fullName = "";
    String familyName = "";
    String weight = "";
    float italicAngle;
    boolean isFixedPitch;
    float underlinePosition;
    float underlineThickness;

    // Private dictionary
    List<Number> blueValues = new ArrayList<Number>();
    List<Number> otherBlues = new ArrayList<Number>();
    List<Number> familyBlues = new ArrayList<Number>();
    List<Number> familyOtherBlues = new ArrayList<Number>();
    float blueScale;
    int blueShift, blueFuzz;
    List<Number> stdHW = new ArrayList<Number>();
    List<Number> stdVW = new ArrayList<Number>();
    List<Number> stemSnapH = new ArrayList<Number>();
    List<Number> stemSnapV = new ArrayList<Number>();
    boolean forceBold;
    int languageGroup;

    // Subrs array, and CharStrings dictionary
    final List<byte[]> subrs = new ArrayList<byte[]>();
    final Map<String, byte[]> charstrings = new LinkedHashMap<String, byte[]>();

    // private caches
    private final Map<String, Type1CharString> charStringCache = new HashMap<String, Type1CharString>();
    private Collection<Mapping> mappings;

    /**
     * Constructs a new Type1Font, called by Type1Parser.
     */
    Type1Font()
    {
    }

    /**
     * Returns the /Subrs array as raw bytes.
     *
     * @return Type 1 char string bytes
     */
    public List<byte[]> getSubrsArray()
    {
        return Collections.unmodifiableList(subrs);
    }

    /**
     * Returns the /CharStrings dictionary as raw bytes.
     *
     * @return Type 1 char string bytes
     */
    public Map<String, byte[]> getCharStringsDict()
    {
        return Collections.unmodifiableMap(charstrings);
    }

    /**
     * {@inheritDoc}
     */
    public Type1CharString getType1CharString(String name) throws IOException
    {
        Type1CharString type1 = charStringCache.get(name);
        if (type1 == null)
        {
            Type1CharStringParser parser = new Type1CharStringParser(fontName, name);
            List<Object> sequence = parser.parse(charstrings.get(name), subrs);
            type1 = new Type1CharString(this, fontName, name, sequence);
            charStringCache.put(name, type1);
        }
        return type1;
    }

    /**
     * Get the mappings for the font as Type1Mapping.
     *
     * @return the Type1Mapping
     */
    public Collection<? extends Type1Mapping> getType1Mappings()
    {
        return getMappings();
    }

    /**
     * Get the mapping (code/charname/bytes) for this font.
     *
     * @return mappings for character codes
     */
    public Collection<Type1Font.Mapping> getMappings()
    {
        if (mappings == null)
        {
            mappings = new ArrayList<Mapping>();
            for (String name : getCharStringsDict().keySet())
            {
                Integer code = encoding.getCode(name);
                if (code == null)
                {
                    code = 0; // .notdef
                }
                Mapping mapping = new Mapping();
                mapping.setCode(code);
                mapping.setName(name);
                mapping.setBytes(getCharStringsDict().get(name));
                mappings.add(mapping);
            }
            mappings = Collections.unmodifiableCollection(mappings);
        }
        return mappings;
    }

    /**
     * {@inheritDoc}
     */
    public class Mapping implements Type1Mapping
    {
        private int mappedCode;
        private String mappedName;
        private byte[] mappedBytes;

        /**
         * {@inheritDoc}
         */
        public Type1CharString getType1CharString() throws IOException
        {
            return Type1Font.this.getType1CharString(mappedName);
        }

        /**
         * {@inheritDoc}
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
         * {@inheritDoc}
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
         * {@inheritDoc}
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

    // font dictionary

    /**
     * Returns the font name.
     * 
     * @return the font name
     */
    public String getFontName()
    {
        return fontName;
    }

    /**
     * Returns the Encoding, if present.
     * @return the encoding or null
     */
    public Encoding getEncoding()
    {
        return encoding;
    }

    /**
     * Returns the paint type.
     * 
     * @return the paint type
     */
    public int getPaintType()
    {
        return paintType;
    }

    /**
     * Returns the font type.
     * 
     * @return the font type
     */
    public int getFontType()
    {
        return fontType;
    }

    /**
     * Returns the font matrix.
     * 
     * @return the font matrix
     */
    public List<Number> getFontMatrix()
    {
        return Collections.unmodifiableList(fontMatrix);
    }

    /**
     * Returns the font bounding box.
     * 
     * @return the font bounding box
     */
    public List<Number> getFontBBox()
    {
        return Collections.unmodifiableList(fontBBox);
    }

    /**
     * Returns unique ID.
     * 
     * @return the unique ID
     */
    public int getUniqueID()
    {
        return uniqueID;
    }

    /**
     * Returns the stroke width.
     * 
     * @return the stroke width
     */
    public float getStrokeWidth()
    {
        return strokeWidth;
    }

    /**
     * Returns the font ID.
     * 
     * @return the font ID
     */
    public String getFontID()
    {
        return fontID;
    }

    // FontInfo dictionary

    /**
     * Returns the version.
     * 
     * @return the version
     */
    public String getVersion()
    {
        return version;
    }

    /**
     * Returns the notice.
     * 
     * @return the notice
     */
    public String getNotice()
    {
        return notice;
    }

    /**
     * Returns the full name.
     * 
     * @return the full name
     */
    public String getFullName()
    {
        return fullName;
    }

    /**
     * Returns the family name.
     * 
     * @return the family name
     */
    public String getFamilyName()
    {
        return familyName;
    }

    /**
     * Returns the weight.
     * 
     * @return the weight
     */
    public String getWeight()
    {
        return weight;
    }

    /**
     * Returns the italic angle.
     * 
     * @return the italic angle
     */
    public float getItalicAngle()
    {
        return italicAngle;
    }

    /**
     * Determines if the font has a fixed pitch.
     * 
     * @return true if the font has a fixed pitch
     */
    public boolean isFixedPitch()
    {
        return isFixedPitch;
    }

    /**
     * Returns the underline position
     * 
     * @return the underline position
     */
    public float getUnderlinePosition()
    {
        return underlinePosition;
    }

    /**
     * Returns the underline thickness.
     * 
     * @return the underline thickness
     */
    public float getUnderlineThickness()
    {
        return underlineThickness;
    }

    // Private dictionary

    /**
     * Returns the blues values.
     * 
     * @return the blues values
     */
    public List<Number> getBlueValues()
    {
        return Collections.unmodifiableList(blueValues);
    }

    /**
     * Returns the other blues values.
     * 
     * @return the other blues values
     */
    public List<Number> getOtherBlues()
    {
        return Collections.unmodifiableList(otherBlues);
    }

    /**
     * Returns the family blues values.
     * 
     * @return the family blues values
     */
    public List<Number> getFamilyBlues()
    {
        return Collections.unmodifiableList(familyBlues);
    }

    /**
     * Returns the other family blues values.
     * 
     * @return the other family blues values
     */
    public List<Number> getFamilyOtherBlues()
    {
        return Collections.unmodifiableList(familyOtherBlues);
    }

    /**
     * Returns the blue scale.
     * 
     * @return the blue scale
     */
    public float getBlueScale()
    {
        return blueScale;
    }

    /**
     * Returns the blue shift.
     * 
     * @return the blue shift
     */
    public int getBlueShift()
    {
        return blueShift;
    }

    /**
     * Returns the blue fuzz.
     * 
     * @return the blue fuzz
     */
    public int getBlueFuzz()
    {
        return blueFuzz;
    }

    /**
     * Returns the StdHW value.
     * 
     * @return the StdHW value
     */
    public List<Number> getStdHW()
    {
        return Collections.unmodifiableList(stdHW);
    }

    /**
     * Returns the StdVW value.
     * 
     * @return the StdVW value
     */
    public List<Number> getStdVW()
    {
        return Collections.unmodifiableList(stdVW);
    }

    /**
     * Returns the StemSnapH value.
     * 
     * @return the StemSnapH value
     */
    public List<Number> getStemSnapH()
    {
        return Collections.unmodifiableList(stemSnapH);
    }

    /**
     * Returns the StemSnapV value.
     * 
     * @return the StemSnapV value
     */
    public List<Number> getStemSnapV()
    {
        return Collections.unmodifiableList(stemSnapV);
    }

    /**
     * Determines if the font is bold.
     * 
     * @return true if the font is bold
     */
    public boolean isForceBold()
    {
        return forceBold;
    }

    /**
     * Returns the language group.
     * 
     * @return the language group
     */
    public int getLanguageGroup()
    {
        return languageGroup;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return getClass().getName() + "[fontName=" + fontName + ", fullName=" + fullName
                + ", encoding=" + encoding + ", charStringsDict=" + charstrings
                + "]";
    }
}
