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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * An appearance dictionary specifying how the annotation shall be presented visually on the page.
 *
 * @author Ben Litchfield
 */
public class PDAppearanceDictionary implements COSObjectable
{
    private final COSDictionary dictionary;

    /**
     * Constructor for embedding.
     */
    public PDAppearanceDictionary()
    {
        dictionary = new COSDictionary();
        // the N entry is required.
        dictionary.setItem(COSName.N, new COSDictionary());
    }

    /**
     * Constructor for reading.
     *
     * @param dictionary The annotations dictionary.
     */
    public PDAppearanceDictionary(COSDictionary dictionary)
    {
        this.dictionary = dictionary;
    }

    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * This will return a list of appearances. In the case where there is only one appearance the map will contain one
     * entry whose key is the string "default".
     *
     * @return A list of key(java.lang.String) value(PDAppearanceStream) pairs
     */
    public PDAppearanceEntry getNormalAppearance()
    {
        COSBase entry = dictionary.getDictionaryObject(COSName.N);
        if (entry == null)
        {
            return null;
        }
        else
        {
            return new PDAppearanceEntry(entry);
        }
    }

    /**
     * This will set a list of appearances. If you would like to set the single appearance then you should use the key
     * "default", and when the PDF is written back to the filesystem then there will only be one stream.
     *
     * @param entry appearance stream or subdictionary
     */
    public void setNormalAppearance(PDAppearanceEntry entry)
    {
        dictionary.setItem(COSName.N, entry);
    }

    /**
     * This will set the normal appearance when there is only one appearance to be shown.
     *
     * @param ap The appearance stream to show.
     */
    public void setNormalAppearance(PDAppearanceStream ap)
    {
        dictionary.setItem(COSName.N, ap);
    }

    /**
     * This will return a list of appearances. In the case where there is only one appearance the map will contain one
     * entry whose key is the string "default". If there is no rollover appearance then the normal appearance will be
     * returned. Which means that this method will never return null.
     *
     * @return A list of key(java.lang.String) value(PDAppearanceStream) pairs
     */
    public PDAppearanceEntry getRolloverAppearance()
    {
        COSBase entry = dictionary.getDictionaryObject(COSName.R);
        if (entry == null)
        {
            return getNormalAppearance();
        }
        else
        {
            return new PDAppearanceEntry(entry);
        }
    }

    /**
     * This will set a list of appearances. If you would like to set the single appearance then you should use the key
     * "default", and when the PDF is written back to the filesystem then there will only be one stream.
     *
     * @param entry appearance stream or subdictionary
     */
    public void setRolloverAppearance(PDAppearanceEntry entry)
    {
        dictionary.setItem(COSName.R, entry);
    }

    /**
     * This will set the rollover appearance when there is rollover appearance to be shown.
     *
     * @param ap The appearance stream to show.
     */
    public void setRolloverAppearance(PDAppearanceStream ap)
    {
        dictionary.setItem(COSName.R, ap);
    }

    /**
     * This will return a list of appearances. In the case where there is only one appearance the map will contain one
     * entry whose key is the string "default". If there is no rollover appearance then the normal appearance will be
     * returned. Which means that this method will never return null.
     *
     * @return A list of key(java.lang.String) value(PDAppearanceStream) pairs
     */
    public PDAppearanceEntry getDownAppearance()
    {
        COSBase entry = dictionary.getDictionaryObject(COSName.D);
        if (entry == null)
        {
            return getNormalAppearance();
        }
        else
        {
            return new PDAppearanceEntry(entry);
        }
    }

    /**
     * This will set a list of appearances. If you would like to set the single appearance then you should use the key
     * "default", and when the PDF is written back to the filesystem then there will only be one stream.
     *
     * @param entry appearance stream or subdictionary
     */
    public void setDownAppearance(PDAppearanceEntry entry)
    {
        dictionary.setItem(COSName.D, entry);
    }

    /**
     * This will set the down appearance when there is down appearance to be shown.
     *
     * @param ap The appearance stream to show.
     */
    public void setDownAppearance(PDAppearanceStream ap)
    {
        dictionary.setItem(COSName.D, ap);
    }
}
