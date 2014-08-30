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

import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.fontbox.type1.Type1Font;

/**
 * External font service provider interface. Implementations are expected to be thread safe.
 *
 * @author John Hewson
 */
public interface FontProvider
{
    /**
     * Returns a TrueType which corresponds to the given PostScript name. If there is no
     * suitable font, then this method will return null.
     *
     * @param postScriptName PostScript font name
     */
    public TrueTypeFont getTrueTypeFont(String postScriptName);

    /**
     * Returns a CFF font which corresponds to the given PostScript name. If there is no
     * suitable font, then this method will return null.
     *
     * @param postScriptName PostScript font name
     */
    public CFFFont getCFFFont(String postScriptName);

    /**
     * Returns a Type 1 which corresponds to the given PostScript name. If there is no
     * suitable font, then this method will return null.
     *
     * @param postScriptName PostScript font name
     */
    public Type1Font getType1Font(String postScriptName);

    /**
     * Returns a string containing debugging information. This will be written to the log if no
     * suitable fonts are found and no fallback fonts are available. May be null.
     */
    public String toDebugString();
}
