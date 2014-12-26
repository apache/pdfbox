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

import org.apache.fontbox.encoding.Encoding;

import java.util.HashMap;
import java.util.Map;

/**
 * A CFF Type 1-equivalent Encoding. An encoding is an array of codes associated with some or all
 * glyphs in a font
 *
 * @author John Hewson
 */
public abstract class CFFEncoding extends Encoding
{
    private final Map<Integer, String> codeToName = new HashMap<Integer, String>();

    /**
     * Package-private constructor for subclasses.
     */
    CFFEncoding()
    {
    }

    /**
     * Returns the name of the glyph for the given character code.
     *
     * @param code character code
     * @return PostScript glyph name
     */
    @Override
    public String getName(int code)
    {
        String name = codeToName.get(code);
        if (name == null)
        {
            return ".notdef";
        }
        return name;
    }

    /**
     * Adds a new code/SID combination to the encoding.
     * @param code the given code
     * @param sid the given SID
     */
    public void add(int code, int sid, String name)
    {
        codeToName.put(code, name);
        addCharacterEncoding(code, name);
    }

    /**
     * For use by subclasses only.
     */
    protected void add(int code, int sid)
    {
        String name = CFFStandardString.getName(sid);
        codeToName.put(code, name);
        addCharacterEncoding(code, name);
    }
}
