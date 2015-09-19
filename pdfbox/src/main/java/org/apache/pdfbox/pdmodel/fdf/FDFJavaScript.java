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
package org.apache.pdfbox.pdmodel.fdf;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionFactory;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionJavaScript;

/**
 * This represents an FDF JavaScript dictionary that is part of the FDF document.
 *
 * @author Ben Litchfield
 */
public class FDFJavaScript implements COSObjectable
{
    private final COSDictionary dictionary;

    /**
     * Default constructor.
     */
    public FDFJavaScript()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param javaScript The FDF java script.
     */
    public FDFJavaScript(COSDictionary javaScript)
    {
        dictionary = javaScript;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * This will get the javascript that is executed before the import.
     *
     * @return Some javascript code.
     */
    public String getBefore()
    {
        COSBase base = dictionary.getDictionaryObject(COSName.BEFORE);
        if (base instanceof COSString)
        {
            return ((COSString) base).getString();
        }
        else if (base instanceof COSStream)
        {
            return ((COSStream) base).toTextString();
        }
        else
        {
            return null;
        }
    }

    /**
     * This will set the javascript code the will get execute before the import.
     *
     * @param before A reference to some javascript code.
     */
    public void setBefore(String before)
    {
        dictionary.setItem(COSName.BEFORE, new COSString(before));
    }

    /**
     * This will get the javascript that is executed after the import.
     *
     * @return Some javascript code.
     */
    public String getAfter()
    {
        COSBase base = dictionary.getDictionaryObject(COSName.AFTER);
        if (base instanceof COSString)
        {
            return ((COSString) base).getString();
        }
        else if (base instanceof COSStream)
        {
            return ((COSStream) base).toTextString();
        }
        else
        {
            return null;
        }
    }

    /**
     * This will set the javascript code the will get execute after the import.
     *
     * @param after A reference to some javascript code.
     */
    public void setAfter(String after)
    {
        dictionary.setItem(COSName.AFTER, new COSString(after));
    }

    /**
     * Returns the dictionary's "Doc" entry, that is, a map of key value pairs to be added to the document's JavaScript
     * name tree.
     * 
     * @return Map of named "JavaScript" dictionaries.
     */
    public Map<String, PDActionJavaScript> getDoc()
    {
        Map<String, PDActionJavaScript> map = new LinkedHashMap<String, PDActionJavaScript>();
        COSArray array = (COSArray) dictionary.getDictionaryObject(COSName.DOC);
        if (array == null)
        {
            return null;
        }
        for (int i = 0; i < array.size(); i++)
        {
            PDActionFactory.createAction((COSDictionary) array.getObject(i));
        }
        return map;
    }

    /**
     * Sets the dictionary's "Doc" entry.
     * 
     * @param map Map of named "JavaScript" dictionaries.
     */
    public void setDoc(Map<String, PDActionJavaScript> map)
    {
        COSArray array = new COSArray();
        for (Map.Entry<String, PDActionJavaScript> entry : map.entrySet())
        {
            array.add(new COSString(entry.getKey()));
            array.add(entry.getValue());
        }
        dictionary.setItem(COSName.DOC, array);
    }
}
