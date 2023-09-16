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
package org.apache.pdfbox.debugger.streampane;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * @author Khyrul Bashar
 *
 * A class that provides the COSStream in various views and related information.
 */
public class Stream
{
    private static final Log LOG = LogFactory.getLog(Stream.class);

    public static final String DECODED = "Decoded (Plain Text)";
    public static final String IMAGE = "Image";

    private final COSStream strm;
    private final boolean isThumb;
    private final boolean isImage;
    private final boolean isXmlMetadata;
    
    private final Map<String, List<String>> filters;

    /**
     * Constructor.
     *
     * @param cosStream COSStream instance.
     * @param isThumb boolean instance says if the stream is thumbnail image.
     */
    Stream(COSStream cosStream, boolean isThumb)
    {
        this.strm = cosStream;
        this.isThumb = isThumb;
        this.isImage = isImageStream(cosStream, isThumb);
        this.isXmlMetadata = isXmlMetadataStream(cosStream);

        filters = createFilterList(cosStream);
    }

    /**
     * Return if this is stream is an Image XObject.
     *
     * @return true if this an image and false otherwise.
     */
    public boolean isImage()
    {
        return isImage;
    }
    
    /**
     * Return if this is stream is an Metadata stream.
     *
     * @return true if this a metadata stream and false otherwise.
     */
    public boolean isXmlMetadata()
    {
        return isXmlMetadata;
    }

    /**
     * Return the available filter list. Only "Unfiltered" is returned if there is no filter and in
     * case of XObject image type stream "Image" is also included in the list.
     *
     * @return An array of String.
     */
    public List<String> getFilterList()
    {
        return new ArrayList<>(filters.keySet());
    }

    /**
     * Returns the label for the "Unfiltered" menu item.
     */
    private String getFilteredLabel()
    {
        StringBuilder sb = new StringBuilder();
        COSBase base = strm.getFilters();
        if (base instanceof COSName)
        {
            sb.append(((COSName) base).getName());
        }
        else if (base instanceof COSArray)
        {
            COSArray filterArray = (COSArray) base;
            for (int i = 0; i < filterArray.size(); i++)
            {
                if (i > 0)
                {
                    sb.append(", ");
                }
                sb.append(((COSName) filterArray.get(i)).getName());
            }
        }
        return "Encoded (" + sb + ")";
    }

    /**
     * Returns a InputStream of a partially filtered stream.
     *
     * @param key is an instance of String which tells which version of stream should be returned.
     * @return an InputStream.
     */
    public InputStream getStream(String key)
    {
        try
        {
            if (DECODED.equals(key))
            {
                return strm.createInputStream();
            }
            else if (getFilteredLabel().equals(key))
            {
                return strm.createRawInputStream();
            }
            else
            {
                return new PDStream(strm).createInputStream(filters.get(key));
            }
        }
        catch (IOException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Provide the image for stream. The stream must be image XObject.
     *
     * @param resources PDResources for the XObject.
     * @return A BufferedImage.
     */
    public BufferedImage getImage(PDResources resources)
    {
        try
        {
            PDImageXObject imageXObject;
            if (isThumb)
            {
                imageXObject = PDImageXObject.createThumbnail(strm);
            }
            else
            {
                imageXObject = new PDImageXObject(new PDStream(strm), resources);
            }
            return imageXObject.getImage();
        }
        catch (IOException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private Map<String, List<String>> createFilterList(COSStream stream)
    {
        Map<String, List<String>> filterList = new LinkedHashMap<>();

        if (isImage)
        {
            filterList.put(IMAGE, null);
        }

        filterList.put(DECODED, null);
        PDStream pdStream = new PDStream(stream);

        int filtersSize = pdStream.getFilters().size();

        for (int i = filtersSize - 1; i >= 1; i--)
        {
            filterList.put(getPartialStreamCommand(i), getStopFilterList(i));
        }
        filterList.put(getFilteredLabel(), null);

        return filterList;
    }

    private String getPartialStreamCommand(final int indexOfStopFilter)
    {
        List<COSName> availableFilters = new PDStream(strm).getFilters();

        StringBuilder nameListBuilder = new StringBuilder();
        for (int i = indexOfStopFilter; i < availableFilters.size(); i++)
        {
            nameListBuilder.append(availableFilters.get(i).getName()).append(" & ");
        }
        nameListBuilder.delete(nameListBuilder.lastIndexOf("&"), nameListBuilder.length());

        return "Keep " + nameListBuilder + "...";
    }

    private List<String> getStopFilterList(final int stopFilterIndex)
    {
        List<COSName> availableFilters = new PDStream(strm).getFilters();

        final List<String> stopFilters = new ArrayList<>(1);
        stopFilters.add(availableFilters.get(stopFilterIndex).getName());

        return stopFilters;
    }

    private boolean isImageStream(COSDictionary dic, boolean isThumb)
    {
        if (isThumb)
        {
            return true;
        }
        return dic.containsKey(COSName.SUBTYPE) && dic.getCOSName(COSName.SUBTYPE).equals(COSName.IMAGE);
    }
    
    private boolean isXmlMetadataStream(COSDictionary dic)
    {
        return dic.containsKey(COSName.SUBTYPE) && dic.getCOSName(COSName.SUBTYPE).equals(COSName.getPDFName("XML"));
    }
}
