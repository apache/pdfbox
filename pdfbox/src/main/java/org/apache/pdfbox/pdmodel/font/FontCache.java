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

package org.apache.pdfbox.pdmodel.font;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.fontbox.FontBoxFont;

/**
 * An in-memory cache for system fonts. This allows PDFBox to manage caching for a {@link FontProvider}.
 * PDFBox is free to purge this cache at will.
 *
 * @author John Hewson
 */
public final class FontCache
{
    private final Map<FontInfo, SoftReference<FontBoxFont>> cache =
            new ConcurrentHashMap<FontInfo, SoftReference<FontBoxFont>>();

    /**
     * Adds the given FontBox font to the cache.
     */
    public void addFont(FontInfo info, FontBoxFont font)
    {
        cache.put(info, new SoftReference<FontBoxFont>(font));
    }

    /**
     * Returns the FontBox font associated with the given FontInfo.
     */
    public FontBoxFont getFont(FontInfo info)
    {
        SoftReference<FontBoxFont> reference = cache.get(info);
        return reference != null ? reference.get() : null;
    }
}
