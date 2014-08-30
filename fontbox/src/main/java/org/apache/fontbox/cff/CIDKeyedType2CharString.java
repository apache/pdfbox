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

import org.apache.fontbox.type1.Type1CharStringReader;

import java.util.List;

/**
 * A CID-Keyed Type 2 CharString.
 *
 * @author John Hewson
 */
public class CIDKeyedType2CharString extends Type2CharString
{
    private final int cid;

    /**
     * Constructor.
     *
     * @param font Parent CFF font
     * @param fontName font name
     * @param cid CID
     * @param gid GID
     * @param sequence Type 2 char string sequence
     * @param defaultWidthX default width
     * @param nomWidthX nominal width
     */
    public CIDKeyedType2CharString(Type1CharStringReader font, String fontName, int cid, int gid, List<Object> sequence, int defaultWidthX, int nomWidthX)
    {
        // glyph name is for debugging only
        super(font, fontName, String.format("%04x", cid), gid, sequence, defaultWidthX, nomWidthX);
        this.cid = cid;
    }

    /**
     * Returns the CID (character id) of this charstring.
     */
    public int getCID()
    {
        return cid;
    }
}
