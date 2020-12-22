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
package org.apache.pdfbox.pdmodel.graphics.color;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A DeviceN Process Dictionary
 *
 * @author John Hewson
 */
public class PDDeviceNProcess
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDDeviceNProcess.class);

    private final COSDictionary dictionary;

    /**
     * Creates a new DeviceN Process Dictionary.
     */
    public PDDeviceNProcess()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Creates a new  DeviceN Process Dictionary from the given attributes.
     * @param attributes a DeviceN attributes dictionary
     */
    public PDDeviceNProcess(final COSDictionary attributes)
    {
        dictionary = attributes;
    }

    /**
     * Returns the underlying COS dictionary.
     * @return the underlying COS dictionary.
     */
    public COSDictionary getCOSDictionary()
    {
        return dictionary;
    }

    /**
     * Returns the process color space
     * @return the process color space
     * @throws IOException if the color space cannot be read
     */
    public PDColorSpace getColorSpace() throws IOException
    {
        final COSBase cosColorSpace = dictionary.getDictionaryObject(COSName.COLORSPACE);
        if (cosColorSpace == null)
        {
            return null; // TODO: return a default?
        }
        return PDColorSpace.create(cosColorSpace);
    }

    /**
     * Returns the names of the color components.
     * @return the names of the color components
     */
    public List<String> getComponents()
    {
        final List<String> components = new ArrayList<>();
        final COSArray cosComponents = (COSArray)dictionary.getDictionaryObject(COSName.COMPONENTS);
        if (cosComponents == null)
        {
            return components;
        }
        for (final COSBase name : cosComponents)
        {
            components.add(((COSName)name).getName());
        }
        return components;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Process{");
        try
        {
            sb.append(getColorSpace());
            for (final String component : getComponents())
            {
                sb.append(" \"");
                sb.append(component);
                sb.append('\"');
            }
        }
        catch (final IOException e)
        {
            LOG.debug("Couldn't get the colorants information - returning 'ERROR' instead'", e);
            sb.append("ERROR");
        }
        sb.append('}');
        return sb.toString();
    }

}
