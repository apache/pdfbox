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
package org.apache.pdfbox.pdmodel.font.encoding;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * A PostScript encoding vector, maps character codes to glyph names.
 * 
 * @author Ben Litchfield
 */
public abstract class Encoding implements COSObjectable
{
    /**
     * This will get an encoding by name. May return null.
     *
     * @param name The name of the encoding to get.
     * @return The encoding that matches the name.
     */
    public static Encoding getInstance(COSName name)
    {
        if (COSName.STANDARD_ENCODING.equals(name))
        {
            return StandardEncoding.INSTANCE;
        }
        else if (COSName.WIN_ANSI_ENCODING.equals(name))
        {
            return WinAnsiEncoding.INSTANCE;
        }
        else if (COSName.MAC_ROMAN_ENCODING.equals(name))
        {
            return MacRomanEncoding.INSTANCE;
        }
        else if (COSName.MAC_EXPERT_ENCODING.equals(name))
        {
            return MacExpertEncoding.INSTANCE;
        }
        else
        {
            return null;
        }
    }

    protected final Map<Integer, String> codeToName = new HashMap<Integer, String>(250);
    protected final Map<String, Integer> inverted = new HashMap<String, Integer>(250);
    private Set<String> names;

    /**
     * Returns an unmodifiable view of the code -&gt; name mapping.
     * 
     * @return the code -&gt; name map
     */
    public Map<Integer, String> getCodeToNameMap()
    {
        return Collections.unmodifiableMap(codeToName);
    }

    /**
     * Returns an unmodifiable view of the name -&gt; code mapping. More than one name may map to
     * the same code.
     *
     * @return the name -&gt; code map
     */
    public Map<String, Integer> getNameToCodeMap()
    {
        return Collections.unmodifiableMap(inverted);
    }

    /**
     * This will add a character encoding. An already existing mapping is preservered when creating the reverse mapping.
     * 
     * @see #overwrite(int, String)
     * 
     * @param code character code
     * @param name PostScript glyph name
     */
    protected void add(int code, String name)
    {
        codeToName.put(code, name);
        if (!inverted.containsKey(name))
        {
            inverted.put(name, code);
        }
    }

    /**
     * This will add a character encoding. An already existing mapping is overwritten when creating the reverse mapping.
     * 
     * @see Encoding#add(int, String)
     *
     * @param code character code
     * @param name PostScript glyph name
     */
    protected void overwrite(int code, String name)
    {
        // remove existing reverse mapping first
        String oldName = codeToName.get(code);
        if (oldName != null)
        {
            Integer oldCode = inverted.get(oldName);
            if (oldCode != null && oldCode == code)
            {
                inverted.remove(oldName);
            }
        }
        inverted.put(name, code);
        codeToName.put(code, name);
    }

    /**
     * Determines if the encoding has a mapping for the given name value.
     * 
     * @param name PostScript glyph name
     */
    public boolean contains(String name)
    {
        // we have to wait until all add() calls are done before building the name cache
        // otherwise /Differences won't be accounted for
        if (names == null)
        {
            synchronized(this)
            {
                // PDFBOX-3404: avoid possibility that one thread ends up with newly created empty map from other thread
                Set<String> tmpSet = new HashSet<String>(codeToName.values());
                // make sure that assignment is done after initialisation is complete
                names = tmpSet;
                // note that it might still happen that 'names' is initialized twice, but this is harmless
            }
            // at this point, names will never be null.
        }
        return names.contains(name);
    }

    /**
     * Determines if the encoding has a mapping for the given code value.
     * 
     * @param code character code
     */
    public boolean contains(int code)
    {
        return codeToName.containsKey(code);
    }

    /**
     * This will take a character code and get the name from the code.
     * 
     * @param code character code
     * @return PostScript glyph name
     */
    public String getName(int code)
    {
       String name = codeToName.get(code);
       if (name != null)
       {
          return name;
       }
       return ".notdef";
    }

    /**
     * Returns the name of this encoding.
     */
    public abstract String getEncodingName();
}
