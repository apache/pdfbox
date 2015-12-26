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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.fontbox.EncodedFont;
import org.apache.fontbox.type1.Type1CharStringReader;

/**
 * A Type 1-equivalent font program represented in a CFF file. Thread safe.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class CFFType1Font extends CFFFont implements EncodedFont
{
    private final Map<String, Object> privateDict = new LinkedHashMap<String, Object>();
    private CFFEncoding encoding;

    private final Map<Integer, Type2CharString> charStringCache =
            new ConcurrentHashMap<Integer, Type2CharString>();

    private final PrivateType1CharStringReader reader = new PrivateType1CharStringReader();

    /**
     * Private implementation of Type1CharStringReader, because only CFFType1Font can
     * expose this publicly, as CIDFonts only support this for legacy 'seac' commands.
     */
    private class PrivateType1CharStringReader implements Type1CharStringReader
    {
        @Override
        public Type1CharString getType1CharString(String name) throws IOException
        {
            return CFFType1Font.this.getType1CharString(name);
        }
    }

    @Override
    public GeneralPath getPath(String name) throws IOException
    {
        return getType1CharString(name).getPath();
    }

    @Override
    public float getWidth(String name) throws IOException
    {
        return getType1CharString(name).getWidth();
    }

    @Override
    public boolean hasGlyph(String name)
    {
        int sid = charset.getSID(name);
        int gid = charset.getGIDForSID(sid);
        return gid != 0;
    }

    @Override
    public List<Number> getFontMatrix()
    {
        return (List<Number>)topDict.get("FontMatrix");
    }

    /**
     * Returns the Type 1 charstring for the given PostScript glyph name.
     *
     * @param name PostScript glyph name
     * @throws IOException if the charstring could not be read
     */
    public Type1CharString getType1CharString(String name) throws IOException
    {
        // lookup via charset
        int gid = nameToGID(name);

        // lookup in CharStrings INDEX
        return getType2CharString(gid, name);
    }

    /**
     * Returns the GID for the given PostScript glyph name.
     * 
     * @param name a PostScript glyph name.
     * @return GID
     */
    public int nameToGID(String name)
    {
        // some fonts have glyphs beyond their encoding, so we look up by charset SID
        int sid = charset.getSID(name);
        return charset.getGIDForSID(sid);
    }

    /**
     * Returns the Type 1 charstring for the given GID.
     *
     * @param gid GID
     * @throws IOException if the charstring could not be read
     */
    @Override
    public Type2CharString getType2CharString(int gid) throws IOException
    {
        String name = "GID+" + gid; // for debugging only
        return getType2CharString(gid, name);
    }

    // Returns the Type 2 charstring for the given GID, with name for debugging
    private Type2CharString getType2CharString(int gid, String name) throws IOException
    {
        Type2CharString type2 = charStringCache.get(gid);
        if (type2 == null)
        {
            byte[] bytes = null;
            if (gid < charStrings.length)
            {
                bytes = charStrings[gid];
            }
            if (bytes == null)
            {
                // .notdef
                bytes = charStrings[0];
            }
            Type2CharStringParser parser = new Type2CharStringParser(fontName, name);
            List<Object> type2seq = parser.parse(bytes, globalSubrIndex, getLocalSubrIndex());
            type2 = new Type2CharString(reader, fontName, name, gid, type2seq, getDefaultWidthX(),
                    getNominalWidthX());
            charStringCache.put(gid, type2);
        }
        return type2;
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
     * Adds the given key/value pair to the private dictionary.
     *
     * @param name the given key
     * @param value the given value
     */
    // todo: can't we just accept a Map?
    void addToPrivateDict(String name, Object value)
    {
        if (value != null)
        {
            privateDict.put(name, value);
        }
    }

    /**
     * Returns the CFFEncoding of the font.
     *
     * @return the encoding
     */
    @Override
    public CFFEncoding getEncoding()
    {
        return encoding;
    }

    /**
     * Sets the CFFEncoding of the font.
     *
     * @param encoding the given CFFEncoding
     */
    void setEncoding(CFFEncoding encoding)
    {
        this.encoding = encoding;
    }

    private byte[][] getLocalSubrIndex()
    {
        return (byte[][])privateDict.get("Subrs");
    }

    // helper for looking up keys/values
    private Object getProperty(String name)
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

    private int getDefaultWidthX()
    {
        Number num = (Number)getProperty("defaultWidthX");
        if (num == null)
        {
            return 1000;
        }
        return num.intValue();
    }

    private int getNominalWidthX()
    {
        Number num = (Number)getProperty("nominalWidthX");
        if (num == null)
        {
            return 0;
        }
        return num.intValue();
    }
}
