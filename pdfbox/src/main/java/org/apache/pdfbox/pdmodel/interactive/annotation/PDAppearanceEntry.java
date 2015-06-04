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

package org.apache.pdfbox.pdmodel.interactive.annotation;

import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.COSDictionaryMap;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * An entry in an appearance dictionary. May contain either a single appearance stream or an appearance subdictionary.
 *
 * @author John Hewson
 */
public class PDAppearanceEntry implements COSObjectable
{
    private COSBase entry;

    private PDAppearanceEntry()
    {
    }

    /**
     * Constructor for reading.
     * 
     * @param entry
     */
    public PDAppearanceEntry(COSBase entry)
    {
        this.entry = entry;
    }

    @Override
    public COSBase getCOSObject()
    {
        return entry;
    }

    /**
     * Returns true if this entry is an appearance subdictionary.
     */
    public boolean isSubDictionary()
    {
        return !(this.entry instanceof COSStream);
    }

    /**
     * Returns true if this entry is an appearance stream.
     */
    public boolean isStream()
    {
        return this.entry instanceof COSStream;
    }

    /**
     * Returns the entry as an appearance stream.
     *
     * @throws IllegalStateException if this entry is not an appearance stream
     */
    public PDAppearanceStream getAppearanceStream()
    {
        if (!isStream())
        {
            throw new IllegalStateException();
        }
        return new PDAppearanceStream((COSStream) entry);
    }

    /**
     * Returns the entry as an appearance subdictionary.
     *
     * @throws IllegalStateException if this entry is not an appearance subdictionary
     */
    public Map<COSName, PDAppearanceStream> getSubDictionary()
    {
        if (!isSubDictionary())
        {
            throw new IllegalStateException();
        }

        COSDictionary dict = (COSDictionary) entry;
        Map<COSName, PDAppearanceStream> map = new HashMap<COSName, PDAppearanceStream>();

        for (COSName name : dict.keySet())
        {
            COSBase value = dict.getDictionaryObject(name);

            // the file from PDFBOX-1599 contains /null as its entry, so we skip non-stream entries
            if (value instanceof COSStream)
            {
                map.put(name, new PDAppearanceStream((COSStream) value));
            }
        }

        return new COSDictionaryMap<COSName, PDAppearanceStream>(map, dict);
    }
}
