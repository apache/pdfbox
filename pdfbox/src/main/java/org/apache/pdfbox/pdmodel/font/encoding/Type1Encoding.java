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
package org.apache.pdfbox.pdmodel.font.encoding;

import org.apache.fontbox.afm.CharMetric;
import org.apache.fontbox.afm.FontMetrics;
import org.apache.pdfbox.cos.COSBase;

import java.util.Map;

/**
 * An encoding for a Type 1 font.
 */
public class Type1Encoding extends Encoding
{
    /**
     * Creates an encoding from the given FontBox encoding.
     *
     * @param encoding FontBox encoding
     */
    public static Type1Encoding fromFontBox(org.apache.fontbox.encoding.Encoding encoding)
    {
        // todo: could optimise this by looking for specific subclasses
        Map<Integer,String> codeToName = encoding.getCodeToNameMap();
        Type1Encoding enc = new Type1Encoding();

        for (Map.Entry<Integer, String> entry : codeToName.entrySet())
        {
            enc.add(entry.getKey(), entry.getValue());
        }

        return enc;
    }

    /**
     * Creates an empty encoding.
     */
    public Type1Encoding()
    {
    }

    /**
     * Creates an encoding from the given AFM font metrics.
     *
     * @param fontMetrics AFM font metrics.
     */
    public Type1Encoding(FontMetrics fontMetrics)
    {
        for (CharMetric nextMetric : fontMetrics.getCharMetrics())
        {
            add(nextMetric.getCharacterCode(), nextMetric.getName());
        }
    }

    @Override
    public COSBase getCOSObject()
    {
        return null;
    }

    @Override
    public String getEncodingName()
    {
        return "built-in (Type 1)";
    }
}
