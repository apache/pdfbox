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
package org.apache.pdfbox.pdfviewer.font;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.type1.Type1Font;
import org.apache.fontbox.type1.Type1Mapping;
import org.apache.pdfbox.encoding.Encoding;

/**
 * This class provides a glyph to GeneralPath conversion for Type 1 PFB and CFF fonts.
 */
public class Type1Glyph2D implements Glyph2D
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(Type1Glyph2D.class);

    private HashMap<String, GeneralPath> glyphs = new HashMap<String, GeneralPath>();
    private Map<Integer, String> codeToName = new HashMap<Integer, String>();
    private String fontName = null;

    /**
     * Constructs a new Type1Glyph2D object for a CFF/Type2 font.
     *
     * @param font CFF/Type2 font
     * @param encoding PDF Encoding or null
     */
    public Type1Glyph2D(CFFFont font, Encoding encoding)
    {
        this(font.getName(), font.getType1Mappings(), encoding);
    }

    /**
     * Constructs a new Type1Glyph2D object for a Type 1 (PFB) font.
     *
     * @param font Type 1 (PFB) font
     * @param encoding PDF Encoding or null
     */
    public Type1Glyph2D(Type1Font font, Encoding encoding)
    {
        this(font.getFontName(), font.getType1Mappings(), encoding);
    }

    /**
     * Private constructor.
     */
    private Type1Glyph2D(String fontName, Collection<? extends Type1Mapping> mappings, Encoding encoding)
    {
        this.fontName = fontName;
        // start with built-in encoding
        for (Type1Mapping mapping : mappings)
        {
            codeToName.put(mapping.getCode(), mapping.getName());
        }
        // override existing entries with an optional PDF Encoding
        if (encoding != null) 
        {
            Map<Integer, String> encodingCodeToName = encoding.getCodeToNameMap();
            for (Integer key : encodingCodeToName.keySet())
            {
                codeToName.put(key, encodingCodeToName.get(key));
            }
        }
        for (Type1Mapping mapping : mappings)
        {
            GeneralPath path;
            try
            {
                path = mapping.getType1CharString().getPath();
                glyphs.put(mapping.getName(), path);
            }
            catch (IOException exception)
            {
                LOG.error("Type 1 glyph rendering failed", exception);
            }
        }
    }

    /**
     * Returns the path describing the glyph for the given name.
     *
     * @param name the name of the glyph
     * @return the GeneralPath for the given glyph
     */
    public GeneralPath getPathForGlyphName(String name)
    {
        return glyphs.get(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GeneralPath getPathForCharacterCode(int code)
    {
        if (codeToName.containsKey(code))
        {
            String name = codeToName.get(code);
            return glyphs.get(name);
        }
        else
        {
            LOG.debug(fontName + ": glyph mapping for " + code + " not found");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfGlyphs()
    {
        if (glyphs != null)
        {
            return glyphs.size();
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose()
    {
        if (glyphs != null)
        {
            glyphs.clear();
        }
        if (codeToName != null)
        {
            codeToName.clear();
        }
    }
}
