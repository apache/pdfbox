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
package org.apache.pdfbox.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;

/**
 * This contains all of the image parameters for in inlined image.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * 
 */
public class ImageParameters
{
    private COSDictionary dictionary;

    /**
     * Constructor.
     */
    public ImageParameters()
    {
        dictionary = new COSDictionary();
    }

    /**
     * Constructor.
     * 
     * @param params
     *            The image parameters.
     */
    public ImageParameters(COSDictionary params)
    {
        dictionary = params;
    }

    /**
     * This will get the dictionary that stores the image parameters.
     * 
     * @return The COS dictionary that stores the image parameters.
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }

    private COSBase getCOSObject(COSName abbreviatedName, COSName name)
    {
        COSBase retval = dictionary.getDictionaryObject(abbreviatedName);
        if (retval == null)
        {
            retval = dictionary.getDictionaryObject(name);
        }
        return retval;
    }

    private int getNumberOrNegativeOne(COSName abbreviatedName, COSName name)
    {
        int retval = -1;
        COSNumber number = (COSNumber) getCOSObject(abbreviatedName, name);
        if (number != null)
        {
            retval = number.intValue();
        }
        return retval;
    }

    /**
     * The bits per component of this image. This will return -1 if one has not been set.
     * 
     * @return The number of bits per component.
     */
    public int getBitsPerComponent()
    {
        return getNumberOrNegativeOne(COSName.BPC, COSName.BITS_PER_COMPONENT);
    }

    /**
     * Set the number of bits per component.
     * 
     * @param bpc
     *            The number of bits per component.
     */
    public void setBitsPerComponent(int bpc)
    {
        dictionary.setInt(COSName.BPC, bpc);
    }

    /**
     * This will get the color space or null if none exists.
     * 
     * @return The color space for this image.
     * 
     * @throws IOException
     *             If there is an error getting the colorspace.
     */
    public PDColorSpace getColorSpace() throws IOException
    {
        return getColorSpace(null);
    }

    /**
     * This will get the color space or null if none exists.
     * 
     * @param colorSpaces
     *            The ColorSpace dictionary from the current resources, if any.
     * 
     * @return The color space for this image.
     * 
     * @throws IOException
     *             If there is an error getting the colorspace.
     */
    public PDColorSpace getColorSpace(Map<String, PDColorSpace> colorSpaces) throws IOException
    {
        COSBase cs = getCOSObject(COSName.CS, COSName.COLORSPACE);
        PDColorSpace retval = null;
        if (cs != null)
        {
            retval = PDColorSpaceFactory.createColorSpace(cs, colorSpaces);
        }
        return retval;
    }

    /**
     * This will set the color space for this image.
     * 
     * @param cs
     *            The color space for this image.
     */
    public void setColorSpace(PDColorSpace cs)
    {
        COSBase base = null;
        if (cs != null)
        {
            base = cs.getCOSObject();
        }
        dictionary.setItem(COSName.CS, base);
    }

    /**
     * The height of this image. This will return -1 if one has not been set.
     * 
     * @return The height.
     */
    public int getHeight()
    {
        return getNumberOrNegativeOne(COSName.H, COSName.HEIGHT);
    }

    /**
     * Set the height of the image.
     * 
     * @param h
     *            The height of the image.
     */
    public void setHeight(int h)
    {
        dictionary.setInt(COSName.H, h);
    }

    /**
     * The width of this image. This will return -1 if one has not been set.
     * 
     * @return The width.
     */
    public int getWidth()
    {
        return getNumberOrNegativeOne(COSName.W, COSName.WIDTH);
    }

    /**
     * Set the width of the image.
     * 
     * @param w
     *            The width of the image.
     */
    public void setWidth(int w)
    {
        dictionary.setInt(COSName.W, w);
    }

    /**
     * This will get the list of filters that are associated with this stream. Or null if there are none.
     * 
     * @return A list of all encoding filters to apply to this stream.
     */
    public List<String> getFilters()
    {
        List<String> retval = null;
        COSBase filters = dictionary.getDictionaryObject(COSName.F, COSName.FILTER);
        if (filters instanceof COSName)
        {
            COSName name = (COSName) filters;
            retval = new COSArrayList<String>(name.getName(), name, dictionary, COSName.FILTER);
        }
        else if (filters instanceof COSArray)
        {
            retval = COSArrayList.convertCOSNameCOSArrayToList((COSArray) filters);
        }
        return retval;
    }

    /**
     * This will set the filters that are part of this stream.
     * 
     * @param filters
     *            The filters that are part of this stream.
     */
    public void setFilters(List<String> filters)
    {
        COSBase obj = COSArrayList.convertStringListToCOSNameCOSArray(filters);
        dictionary.setItem(COSName.F, obj);
    }

    /**
     * This will set the decode values.
     * 
     * @param decodeValues
     *            the new decode values.
     */
    public void setDecodeValues(COSArray decodeValues)
    {
        getDictionary().setItem(COSName.D, decodeValues);
    }

    /**
     * Returns all decode values as COSArray.
     * 
     * @return the decode array.
     */
    public COSArray getDecodeValues()
    {
        COSArray decode = (COSArray) getDictionary().getDictionaryObject(COSName.D, COSName.DECODE);
        return decode;
    }

}
