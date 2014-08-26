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
package org.apache.pdfbox.rendering.font;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.encoding.Encoding;
import org.apache.pdfbox.encoding.GlyphList;
import org.apache.pdfbox.pdmodel.font.PDType1Equivalent;

/**
 * Glyph to GeneralPath conversion for Type 1 PFB and CFF, and TrueType fonts with a 'post' table.
 */
public class Type1Glyph2D implements Glyph2D
{
    private static final Log LOG = LogFactory.getLog(Type1Glyph2D.class);

    // alternative names for glyphs which are commonly encountered
    private static final Map<String, String> ALT_NAMES = new HashMap<String, String>();
    static
    {
        ALT_NAMES.put("ff", "f_f");
        ALT_NAMES.put("ffi", "f_f_i");
        ALT_NAMES.put("ffl", "f_f_l");
        ALT_NAMES.put("fi", "f_i");
        ALT_NAMES.put("fl", "f_l");
        ALT_NAMES.put("st", "s_t");
        ALT_NAMES.put("IJ", "I_J");
        ALT_NAMES.put("ij", "i_j");
    }

    // unicode names for ligatures, needed to undo mapping in org.apache.pdfbox.Encoding
    private static final Map<String, String> LIGATURE_UNI_NAMES = new HashMap<String, String>();
    static
    {
        LIGATURE_UNI_NAMES.put("ff", "uniFB00");
        LIGATURE_UNI_NAMES.put("fi", "uniFB01");
        LIGATURE_UNI_NAMES.put("fl", "uniFB02");
        LIGATURE_UNI_NAMES.put("ffi", "uniFB03");
        LIGATURE_UNI_NAMES.put("ffl", "uniFB04");
        LIGATURE_UNI_NAMES.put("pi", "uni03C0");
    }

    private final HashMap<Integer, GeneralPath> cache = new HashMap<Integer, GeneralPath>();
    private final PDType1Equivalent font;

    /**
     * Constructor.
     *
     * @param font PDF Type1 font.
     */
    public Type1Glyph2D(PDType1Equivalent font)
    {
        this.font = font;
    }

    @Override
    public GeneralPath getPathForCharacterCode(int code)
    {
        // cache
        if (cache.containsKey(code))
        {
            return cache.get(code);
        }

        // fetch
        try
        {
            String name = font.codeToName(code);
            GeneralPath path = null;
            if (font.hasGlyph(name))
            {
                path = font.getPath(name);
            }
            else
            {
                // try alternative name
                String altName = ALT_NAMES.get(name);
                if (altName != null && font.hasGlyph(altName))
                {
                    path = font.getPath(altName);
                }
                else
                {
                    // try unicode name
                    String unicodes = GlyphList.toUnicode(name);
                    if (unicodes != null)
                    {
                        if (unicodes.length() == 1)
                        {
                            String uniName = String.format("uni%04X", unicodes.codePointAt(0));
                            path = font.getPath(uniName);
                        }
                        else if (unicodes.length() > 1)
                        {
                            if (LIGATURE_UNI_NAMES.containsKey(name))
                            {
                                path = font.getPath(LIGATURE_UNI_NAMES.get(name));
                            }
                        }
                    }
                }
            }

            if (path == null)
            {
                LOG.warn("No glyph for " + code + " (" + name + ") in font " + font.getName());
                path = font.getPath(".notdef");
            }

            cache.put(code, path);
            return path;
        }
        catch (IOException e)
        {
            LOG.error("Glyph rendering failed", e); // todo: escalate this error?
            return new GeneralPath();
        }
    }

    @Override
    public void dispose()
    {
        cache.clear();
    }
}
