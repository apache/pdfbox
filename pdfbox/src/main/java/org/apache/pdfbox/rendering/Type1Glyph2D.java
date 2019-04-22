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
package org.apache.pdfbox.rendering;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;

/**
 * Glyph to GeneralPath conversion for Type 1 PFB and CFF, and TrueType fonts with a 'post' table.
 */
final class Type1Glyph2D implements Glyph2D
{
    private static final Log LOG = LogFactory.getLog(Type1Glyph2D.class);

    private final Map<Integer, GeneralPath> cache = new HashMap<Integer, GeneralPath>();
    private final PDSimpleFont font;

    /**
     * Constructor.
     *
     * @param font PDF Type1 font.
     */
    Type1Glyph2D(PDSimpleFont font)
    {
        this.font = font;
    }

    @Override
    public GeneralPath getPathForCharacterCode(int code)
    {
        // cache
        GeneralPath path = cache.get(code);
        if (path == null)
        {
            // fetch
            try
            {
                String name = font.getEncoding().getName(code);
                if (!font.hasGlyph(name))
                {
                    LOG.warn("No glyph for code " + code + " (" + name + ") in font " + font.getName());
                    if (code == 10 && font.isStandard14())
                    {
                        // PDFBOX-4001 return empty path for line feed on std14
                        path = new GeneralPath();
                        cache.put(code, path);
                        return path;
                    }

                    // try unicode name
                    String unicodes = font.getGlyphList().toUnicode(name);
                    if (unicodes != null && unicodes.length() == 1)
                    {
                        String uniName = getUniNameOfCodePoint(unicodes.codePointAt(0));
                        if (font.hasGlyph(uniName))
                        {
                            name = uniName;
                        }
                    }
                }
    
                // todo: can this happen? should it be encapsulated?
                path = font.getPath(name);
                if (path == null)
                {
                    path = font.getPath(".notdef");
                }
    
                cache.put(code, path);
                return path;
            }
            catch (IOException e)
            {
                // todo: escalate this error?
                LOG.error("Glyph rendering failed", e); 
                path = new GeneralPath();
            }
        }
        return path;
    }

    @Override
    public void dispose()
    {
        cache.clear();
    }

    // copied from UniUtil
    private static String getUniNameOfCodePoint(int codePoint)
    {
        String hex = Integer.toString(codePoint, 16).toUpperCase(Locale.US);
        switch (hex.length())
        {
            case 1:
                return "uni000" + hex;
            case 2:
                return "uni00" + hex;
            case 3:
                return "uni0" + hex;
            default:
                return "uni" + hex;
        }
    }
}
