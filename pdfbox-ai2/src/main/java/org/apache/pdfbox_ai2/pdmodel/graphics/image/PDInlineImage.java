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
package org.apache.pdfbox_ai2.pdmodel.graphics.image;

import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.pdfbox_ai2.cos.COSArray;
import org.apache.pdfbox_ai2.cos.COSBase;
import org.apache.pdfbox_ai2.cos.COSDictionary;
import org.apache.pdfbox_ai2.cos.COSName;
import org.apache.pdfbox_ai2.filter.DecodeResult;
import org.apache.pdfbox_ai2.filter.Filter;
import org.apache.pdfbox_ai2.filter.FilterFactory;
import org.apache.pdfbox_ai2.pdmodel.PDResources;
import org.apache.pdfbox_ai2.pdmodel.common.COSArrayList;
import org.apache.pdfbox_ai2.pdmodel.common.PDStream;
import org.apache.pdfbox_ai2.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox_ai2.pdmodel.graphics.color.PDDeviceGray;

/**
 * An inline image object which uses a special syntax to express the data for a
 * small image directly within the content stream.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class PDInlineImage implements PDImage
{
    // image parameters
    private final COSDictionary parameters;

    // the current resources, contains named color spaces
    private final PDResources resources;

    // image data
    private final byte[] rawData;
    private final byte[] decodedData;

    /**
     * Creates an inline image from the given parameters and data.
     *
     * @param parameters the image parameters
     * @param data the image data
     * @param resources the current resources
     */
    public PDInlineImage(COSDictionary parameters, byte[] data, PDResources resources)
            throws IOException
    {
        this.parameters = parameters;
        this.resources = resources;
        this.rawData = data;

        DecodeResult decodeResult = null;
        List<String> filters = getFilters();
        if (filters == null || filters.isEmpty())
        {
            this.decodedData = data;
        }
        else
        {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
            for (int i = 0; i < filters.size(); i++)
            {
                // TODO handling of abbreviated names belongs here, rather than in other classes
                out.reset();
                Filter filter = FilterFactory.INSTANCE.getFilter(filters.get(i));
                decodeResult = filter.decode(in, out, parameters, i);
                in = new ByteArrayInputStream(out.toByteArray());
            }
            this.decodedData = out.toByteArray();
        }

        // repair parameters
        if (decodeResult != null)
        {
            parameters.addAll(decodeResult.getParameters());
        }
    }

    @Override
    public COSBase getCOSObject()
    {
        return parameters;
    }

    @Override
    public int getBitsPerComponent()
    {
        if (isStencil())
        {
            return 1;
        }
        else
        {
            return parameters.getInt(COSName.BPC, COSName.BITS_PER_COMPONENT, -1);
        }
    }

    @Override
    public void setBitsPerComponent(int bitsPerComponent)
    {
        parameters.setInt(COSName.BPC, bitsPerComponent);
    }

    @Override
    public PDColorSpace getColorSpace() throws IOException
    {
        COSBase cs = parameters.getDictionaryObject(COSName.CS);
        if (cs == null)
        {
            cs = parameters.getDictionaryObject(COSName.COLORSPACE);
        }

        if (cs != null)
        {
            // TODO: handling of abbreviated color space names belongs here, not in the factory
            return PDColorSpace.create(cs, resources);
        }
        else if (isStencil())
        {
            // stencil mask color space must be gray, it is often missing
            return PDDeviceGray.INSTANCE;
        }
        else
        {
            // an image without a color space is always broken
            throw new IOException("could not determine color space");
        }
    }

    @Override
    public void setColorSpace(PDColorSpace colorSpace)
    {
        COSBase base = null;
        if (colorSpace != null)
        {
            base = colorSpace.getCOSObject();
        }
        parameters.setItem(COSName.CS, base);
    }

    @Override
    public int getHeight()
    {
        return parameters.getInt(COSName.H, COSName.HEIGHT, -1);
    }

    @Override
    public void setHeight(int height)
    {
        parameters.setInt(COSName.H, height);
    }

    @Override
    public int getWidth()
    {
        return parameters.getInt(COSName.W, COSName.WIDTH, -1);
    }

    @Override
    public void setWidth(int width)
    {
        parameters.setInt(COSName.W, width);
    }

    @Override
    public boolean getInterpolate()
    {
        return parameters.getBoolean(COSName.I, COSName.INTERPOLATE, false);
    }

    @Override
    public void setInterpolate(boolean value)
    {
        parameters.setBoolean(COSName.I, value);
    }

    /**
     * Returns a list of filters applied to this stream, or null if there are none.
     *
     * @return a list of filters applied to this stream
     */
    // TODO return an empty list if there are none?
    public List<String> getFilters()
    {
        List<String> names = null;
        COSBase filters = parameters.getDictionaryObject(COSName.F, COSName.FILTER);
        if (filters instanceof COSName)
        {
            COSName name = (COSName) filters;
            names = new COSArrayList<String>(name.getName(), name, parameters, COSName.FILTER);
        }
        else if (filters instanceof COSArray)
        {
            names = COSArrayList.convertCOSNameCOSArrayToList((COSArray) filters);
        }
        return names;
    }

    /**
     * Sets which filters are applied to this stream.
     *
     * @param filters the filters to apply to this stream.
     */
    public void setFilters(List<String> filters)
    {
        COSBase obj = COSArrayList.convertStringListToCOSNameCOSArray(filters);
        parameters.setItem(COSName.F, obj);
    }

    @Override
    public void setDecode(COSArray decode)
    {
        parameters.setItem(COSName.D, decode);
    }

    @Override
    public COSArray getDecode()
    {
        return (COSArray) parameters.getDictionaryObject(COSName.D, COSName.DECODE);
    }

    @Override
    public boolean isStencil()
    {
        return parameters.getBoolean(COSName.IM, COSName.IMAGE_MASK, false);
    }

    @Override
    public void setStencil(boolean isStencil)
    {
        parameters.setBoolean(COSName.IM, isStencil);
    }

    /**
     * Always null, use {@link #createInputStream()} instead.
     */
    @Override
    public PDStream getStream() throws IOException
    {
        return null;
    }

    @Override
    public InputStream createInputStream() throws IOException
    {
        return new ByteArrayInputStream(decodedData);
    }

    @Override
    public InputStream createInputStream(List<String> stopFilters) throws IOException
    {
        List<String> filters = getFilters();
        ByteArrayInputStream in = new ByteArrayInputStream(rawData);
        ByteArrayOutputStream out = new ByteArrayOutputStream(rawData.length);
        for (int i = 0; i < filters.size(); i++)
        {
            // TODO handling of abbreviated names belongs here, rather than in other classes
            out.reset();
            if (stopFilters.contains(filters.get(i)))
            {
                break;
            }
            else
            {
                Filter filter = FilterFactory.INSTANCE.getFilter(filters.get(i));
                filter.decode(in, out, parameters, i);
                in = new ByteArrayInputStream(out.toByteArray());
            }
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public boolean isEmpty()
    {
        return decodedData.length == 0;
    }

    /**
     * Returns the inline image data.
     */
    public byte[] getData()
    {
        return decodedData;
    }
    
    @Override
    public BufferedImage getImage() throws IOException
    {
        return SampledImageReader.getRGBImage(this, getColorKeyMask());
    }

    @Override
    public BufferedImage getStencilImage(Paint paint) throws IOException
    {
        if (!isStencil())
        {
            throw new IllegalStateException("Image is not a stencil");
        }
        return SampledImageReader.getStencilImage(this, paint);
    }

    /**
     * Returns the color key mask array associated with this image, or null if
     * there is none.
     *
     * @return Mask Image XObject
     */
    public COSArray getColorKeyMask()
    {
        COSBase mask = parameters.getDictionaryObject(COSName.IM, COSName.MASK);
        if (mask instanceof COSArray)
        {
            return (COSArray) mask;
        }
        return null;
    }

    /**
     * Returns the suffix for this image type, e.g. jpg/png.
     *
     * @return The image suffix.
     */
    public String getSuffix()
    {
        // TODO implement me
        return null;
    }
}
