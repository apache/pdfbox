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
package org.apache.pdfbox.pdmodel.interactive.digitalsignature;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * If exist, it describe where the signature handler can request a RFC3161
 * timestamp and if it is a must have for the signature.
 *
 * @author Thomas Chojecki
 */
public class PDSeedValueTimeStamp
{
    private final COSDictionary dictionary;

    /**
     * Default constructor.
     */
    public PDSeedValueTimeStamp()
    {
        dictionary = new COSDictionary();
        dictionary.setDirect(true);
    }

    /**
     * Constructor.
     *
     * @param dict The signature dictionary.
     */
    public PDSeedValueTimeStamp(COSDictionary dict)
    {
        dictionary = dict;
        dictionary.setDirect(true);
    }

    /**
     * Convert this standard java object to a COS dictionary.
     *
     * @return The COS dictionary that matches this Java object.
     */
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * Returns the URL.
     * 
     * @return the URL
     */
    public String getURL()
    {
        return dictionary.getString(COSName.URL);
    }

    /**
     * Sets the URL.
     * @param url the URL to be set as URL
     */
    public void setURL(String url)
    {
        dictionary.setString(COSName.URL, url);
    }

    /**
     * Indicates if a timestamp is required.
     * 
     * @return true if a timestamp is required
     */
    public boolean isTimestampRequired()
    {
        return dictionary.getInt(COSName.FT, 0) != 0;
    }

    /**
     * Sets if a timestamp is reuqired or not.
     * 
     * @param flag true if a timestamp is required
     */
    public void setTimestampRequired(boolean flag)
    {
        dictionary.setInt(COSName.FT, flag ? 1 : 0);
    }
}
