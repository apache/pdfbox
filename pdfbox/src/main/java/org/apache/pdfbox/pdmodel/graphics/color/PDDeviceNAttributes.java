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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSDictionaryMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains additional information about the components of colour space.
 * Instead of using the alternate color space and tint transform, conforming readers may use custom
 * blending algorithms, along with other information provided in the attributes dictionary.
 *
 * @author Ben Litchfield
 */
public final class PDDeviceNAttributes
{
    private final COSDictionary dictionary;

    /**
     * Creates a new DeviceN colour space attributes dictionary.
     */
    public PDDeviceNAttributes()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Creates a new DeviceN colour space attributes dictionary from the given dictionary.
     * @param attributes a dictionary that has all of the attributes
     */
    public PDDeviceNAttributes(COSDictionary attributes)
    {
        dictionary = attributes;
    }

    /**
     * Returns the underlying COS dictionary.
     * @return the dictionary that this object wraps
     */
    public COSDictionary getCOSDictionary()
    {
        return dictionary;
    }

    /**
     * Returns a map of colorants and their associated Separation color space.
     * @return map of colorants to color spaces
     * @throws IOException If there is an error reading a color space
     */
    public Map<String, PDSeparation> getColorants() throws IOException
    {
        Map<String,PDSeparation> actuals = new HashMap<String, PDSeparation>();
        COSDictionary colorants = (COSDictionary)dictionary.getDictionaryObject(COSName.COLORANTS);
        if(colorants == null)
        {
            colorants = new COSDictionary();
            dictionary.setItem(COSName.COLORANTS, colorants);
        }
        for(COSName name : colorants.keySet())
        {
            COSBase value = colorants.getDictionaryObject(name);
            actuals.put(name.getName(), (PDSeparation)PDColorSpace.create(value));
        }
        return new COSDictionaryMap<String, PDSeparation>(actuals, colorants);
    }

    /**
     * Returns the DeviceN Process Dictionary, or null if it is missing.
     * @return the DeviceN Process Dictionary, or null if it is missing.
     */
    public PDDeviceNProcess getProcess()
    {
        COSDictionary process = (COSDictionary)dictionary.getDictionaryObject(COSName.PROCESS);
        if (process == null)
        {
            return null;
        }
        return new PDDeviceNProcess(process);
    }

    /**
     * Returns true if this is an NChannel (PDF 1.6) color space.
     * @return true if this is an NChannel color space.
     */
    public boolean isNChannel()
    {
        return "NChannel".equals(dictionary.getNameAsString(COSName.SUBTYPE));
    }

    /**
     * Sets the colorant map.
     * @param colorants the map of colorants
     */
    public void setColorants(Map<String, PDColorSpace> colorants)
    {
        COSDictionary colorantDict = null;
        if(colorants != null)
        {
            colorantDict = COSDictionaryMap.convert(colorants);
        }
        dictionary.setItem(COSName.COLORANTS, colorantDict);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(dictionary.getNameAsString(COSName.SUBTYPE));
        sb.append('{');
        PDDeviceNProcess process = getProcess();
        if (process != null)
        {
            sb.append(getProcess());
            sb.append(' ');
        }

        Map<String, PDSeparation> colorants;
        try
        {
            colorants = getColorants();
            sb.append("Colorants{");
            for (Map.Entry<String, PDSeparation> col : colorants.entrySet())
            {
                sb.append('\"');
                sb.append(col.getKey());
                sb.append("\": ");
                sb.append(col.getValue());
                sb.append(' ');
            }
            sb.append('}');
        }
        catch (IOException e)
        {
            sb.append("ERROR");
        }
        sb.append('}');
        return sb.toString();
    }

}
