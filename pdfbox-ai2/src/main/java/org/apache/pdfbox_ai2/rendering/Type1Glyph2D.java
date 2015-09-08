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
package org.apache.pdfbox_ai2.rendering;

import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox_ai2.pdmodel.font.PDSimpleFont;

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
        if (cache.containsKey(code))
        {
            return cache.get(code);
        }

        // fetch
        try
        {
            String name = font.getEncoding().getName(code);
            if (!font.hasGlyph(name))
            {
                LOG.warn("No glyph for " + code + " (" + name + ") in font " + font.getName());
            }

            // todo: can this happen? should it be encapsulated?
            GeneralPath path = font.getPath(name);
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
            return new GeneralPath();
        }
    }

    @Override
    public void dispose()
    {
        cache.clear();
    }
}
