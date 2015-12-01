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

import org.apache.fontbox.FontBoxFont;

/**
 * A font mapping from a PDF font to a FontBox font.
 *
 * @author John Hewson
 */
public class FontMapping<T extends FontBoxFont>
{
    private final T font;
    private final boolean isFallback;

    public FontMapping(T font, boolean isFallback)
    {
        this.font = font;
        this.isFallback = isFallback;
    }

    /**
     * Returns the mapped, FontBox font. This is never null.
     */
    public T getFont()
    {
        return font;
    }

    /**
     * Returns true if the mapped font is a fallback, i.e. a substitute based on basic font style,
     * such as bold/italic, rather than font name.
     */
    public boolean isFallback()
    {
        return isFallback;
    }
}
