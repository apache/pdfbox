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
package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.util.Charsets;

/**
 * This represents a URI action that can be executed in a PDF document.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
 */
public class PDActionURI extends PDAction
{
    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "URI";

    /**
     * Default constructor.
     */
    public PDActionURI()
    {
        action = new COSDictionary();
        setSubType(SUB_TYPE);
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDActionURI(COSDictionary a)
    {
        super(a);
    }

    /**
     * This will get the uniform resource identifier to resolve. It should be encoded in 7-bit
     * ASCII, but UTF-8 and UTF-16 are supported too.
     *
     * @return The URI entry of the specific URI action dictionary or null if there isn't any.
     */
    public String getURI()
    {
        COSBase base = action.getDictionaryObject(COSName.URI);
        if (base instanceof COSString)
        {
            byte[] bytes = ((COSString) base).getBytes();
            if (bytes.length >= 2)
            {
                // UTF-16 (BE)
                if ((bytes[0] & 0xFF) == 0xFE && (bytes[1] & 0xFF) == 0xFF)
                {
                    return action.getString(COSName.URI);
                }
                // UTF-16 (LE)
                if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE)
                {
                    return action.getString(COSName.URI);
                }
            }
            return new String(bytes, Charsets.UTF_8);
        }
        return null;
    }

    /**
     * This will set the uniform resource identifier to resolve, encoded in
     * 7-bit ASCII.
     *
     * @param uri The uniform resource identifier.
     */
    public void setURI(String uri)
    {
        action.setString(COSName.URI, uri);
    }

    /**
     * This will specify whether to track the mouse position when the URI is
     * resolved. Default value: false. This entry applies only to actions
     * triggered by the user's clicking an annotation; it is ignored for actions
     * associated with outline items or with a document's OpenAction entry.
     *
     * @return A flag specifying whether to track the mouse position when the
     * URI is resolved.
     */
    public boolean shouldTrackMousePosition()
    {
        return this.action.getBoolean("IsMap", false);
    }

    /**
     * This will specify whether to track the mouse position when the URI is
     * resolved.
     *
     * @param value The flag value.
     */
    public void setTrackMousePosition(boolean value)
    {
        this.action.setBoolean("IsMap", value);
    }
}
