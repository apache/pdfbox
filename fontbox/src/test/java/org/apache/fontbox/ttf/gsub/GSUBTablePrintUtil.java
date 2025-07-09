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

package org.apache.fontbox.ttf.gsub;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.GSUBTableDebugger;
import org.apache.fontbox.ttf.model.GsubData;
import org.apache.fontbox.ttf.model.ScriptFeature;

/**
 * This class is to be used mainly for debugging purposes. It is used in {@link GSUBTableDebugger}. It is used to print
 * all the GSUB Glyphs to characters. The format is: <br>
 * 
 * &lt;Serial no.&gt;.) &lt;Space separated characters to be replaced&gt; : RawUnicode: [&lt;Space separated unicode
 * representation of each character to be replaced in hexadecimal&gt;] : &lt;The compound character&gt; : &lt;The
 * GlyphId with which these characters are replaced&gt;
 * 
 * @author Palash Ray
 * 
 */
public class GSUBTablePrintUtil
{

    public void printCharacterToGlyph(GsubData gsubData, CmapLookup cmap)
    {
        System.err.println(
                "Format:\n<Serial no.>.) <Space separated characters to be replaced> : "
                        + "RawUnicode: [<Space separated unicode representation of each character "
                        + "to be replaced in hexadecimal>] : <The compound character> : "
                        + "<The GlyphId with which these characters are replaced>");
        Map<List<Integer>, List<Integer>> rawGSubTableData = new HashMap<>();

        for (String featureName : gsubData.getSupportedFeatures())
        {
            ScriptFeature scriptFeature = gsubData.getFeature(featureName);
            for (List<Integer> glyphsToBeReplaced : scriptFeature.getAllGlyphIdsForSubstitution())
            {
                rawGSubTableData.put(scriptFeature.getReplacementForGlyphs(glyphsToBeReplaced),
                        glyphsToBeReplaced);
            }

        }

        for (String featureName : gsubData.getSupportedFeatures())
        {
            System.out
                    .println("******************      " + featureName + "      ******************");
            ScriptFeature scriptFeature = gsubData.getFeature(featureName);
            int index = 0;
            for (List<Integer> glyphsToBeReplaced : scriptFeature.getAllGlyphIdsForSubstitution())
            {
                String unicodeText = getUnicodeString(rawGSubTableData, cmap, glyphsToBeReplaced);
                System.out.println(++index + ".) " + getExplainedUnicodeText(unicodeText) + " : "
                        + scriptFeature.getReplacementForGlyphs(glyphsToBeReplaced));
            }

        }

    }

    private String getUnicodeChar(Map<List<Integer>, List<Integer>> rawGSubTableData, CmapLookup cmap,
            Integer glyphId)
    {
        List<Integer> keyChars = cmap.getCharCodes(glyphId);

        // its a compound glyph
        if (keyChars == null)
        {
            List<Integer> constituentGlyphs = rawGSubTableData.get(Collections.singletonList(glyphId));

            if (constituentGlyphs == null || constituentGlyphs.isEmpty())
            {
                String message = "lookup for the glyphId: " + glyphId
                        + " failed, as no corresponding Unicode char found mapped to it";
                throw new IllegalStateException(message);
            }
            else
            {
                return getUnicodeString(rawGSubTableData, cmap, constituentGlyphs);
            }
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            for (int unicodeChar : keyChars)
            {
                sb.append((char) unicodeChar);
            }
            return sb.toString();
        }

    }

    private String getUnicodeString(Map<List<Integer>, List<Integer>> rawGSubTableData, CmapLookup cmap,
            List<Integer> glyphIDs)
    {
        StringBuilder sb = new StringBuilder();
        for (Integer glyphId : glyphIDs)
        {
            sb.append(getUnicodeChar(rawGSubTableData, cmap, glyphId));
        }
        return sb.toString();
    }

    private String getExplainedUnicodeText(String unicodeText)
    {
        StringBuilder sb = new StringBuilder();

        for (char unicode : unicodeText.toCharArray())
        {
            sb.append(unicode).append(" ");
        }
        sb.append(":");

        sb.append(" RawUnicode: [");
        for (char unicode : unicodeText.toCharArray())
        {
            sb.append("\\u0").append(Integer.toHexString(unicode).toUpperCase()).append(" ");
        }
        sb.append("] : ");

        sb.append(unicodeText);

        return sb.toString();
    }

}
