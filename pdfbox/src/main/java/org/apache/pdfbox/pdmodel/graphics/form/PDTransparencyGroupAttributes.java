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
package org.apache.pdfbox.pdmodel.graphics.form;

import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;

/**
 * Transparency group attributes.
 * 
 * @author Kühn &amp; Weyh Software GmbH
 */
public final class PDTransparencyGroupAttributes implements COSObjectable
{
    private final COSDictionary dictionary;
    private PDColorSpace colorSpace;

    /**
     * Creates a group object with /Transparency subtype entry.
     */
    public PDTransparencyGroupAttributes()
    {
        dictionary = new COSDictionary();
        dictionary.setItem(COSName.S, COSName.TRANSPARENCY);
    }

    /**
     * Creates a group object from a given dictionary
     * @param dic {@link COSDictionary} object
     */
    public PDTransparencyGroupAttributes(COSDictionary dic)
    {
        dictionary = dic;
    }

    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * Returns the group color space or null if it isn't defined.
     *
     * @return the group color space.
     * @throws IOException
     */
    public PDColorSpace getColorSpace() throws IOException
    {
        if (colorSpace == null && getCOSObject().containsKey(COSName.CS))
        {
            colorSpace = PDColorSpace.create(getCOSObject().getDictionaryObject(COSName.CS));
        }
        return colorSpace;
    }

    /**
     * Returns true if this group is isolated. Isolated groups begin with the fully transparent
     * image, non-isolated begin with the current backdrop.
     */
    public boolean isIsolated()
    {
        return getCOSObject().getBoolean(COSName.I, false);
    }

    /**
     * Returns true if this group is a knockout. A knockout group blends with original backdrop,
     * a non-knockout group blends with the current backdrop.
     */
    public boolean isKnockout()
    {
        return getCOSObject().getBoolean(COSName.K, false);
    }
}
