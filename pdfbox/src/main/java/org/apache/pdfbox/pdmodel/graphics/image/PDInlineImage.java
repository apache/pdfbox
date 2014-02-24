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
package org.apache.pdfbox.pdmodel.graphics.image;

import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.filter.DecodeResult;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.RandomAccess;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDMemoryStream;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;

/**
 * An inline image object which uses a special syntax to express the data for
 * a small image directly within the content stream.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public final class PDInlineImage implements PDImage
{
    // image parameters
    private COSDictionary parameters;

    // color spaces in current resource dictionary
    private Map<String, PDColorSpace> colorSpaces;

    // image data
    private PDStream stream;

    /**
     * Creates an inline image from the given parameters and data.
     * @param parameters the image parameters
     * @param data the image data
     * @param colorSpaces the color spaces in the current resources parameters
     */
    public PDInlineImage(COSDictionary parameters, byte[] data,
                         Map<String, PDColorSpace> colorSpaces) throws IOException
    {
        this.parameters = parameters;
        this.colorSpaces = colorSpaces;

        DecodeResult decodeResult = null;
        if (getFilters() == null)
        {
            this.stream = new PDMemoryStream(data);
        }
        else
        {
            List<String> filters = getFilters();
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
            byte[] finalData = out.toByteArray();
            this.stream = new PDMemoryStream(finalData);
        }

        // repair parameters
        if (decodeResult != null)
        {
            parameters.addAll(decodeResult.getParameters());
        }
    }

    public COSBase getCOSObject()
    {
        return parameters;
    }

    public int getBitsPerComponent()
    {
        return parameters.getInt(COSName.BPC, COSName.BITS_PER_COMPONENT, -1);
    }

    public void setBitsPerComponent(int bitsPerComponent)
    {
        parameters.setInt(COSName.BPC, bitsPerComponent);
    }

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
            return PDColorSpace.create(cs, colorSpaces, null);
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

    public void setColorSpace(PDColorSpace colorSpace)
    {
        COSBase base = null;
        if (colorSpace != null)
        {
            base = colorSpace.getCOSObject();
        }
        parameters.setItem(COSName.CS, base);
    }

    public int getHeight()
    {
        return parameters.getInt(COSName.H, COSName.HEIGHT, -1);
    }

    public void setHeight(int height)
    {
        parameters.setInt(COSName.H, height);
    }

    public int getWidth()
    {
        return parameters.getInt(COSName.W, COSName.WIDTH, -1);
    }

    public void setWidth(int width)
    {
        parameters.setInt(COSName.W, width);
    }

    /**
     * Returns a list of filters applied to this stream, or null if there are none.
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
     * @param filters the filters to apply to this stream.
     */
    public void setFilters(List<String> filters)
    {
        COSBase obj = COSArrayList.convertStringListToCOSNameCOSArray(filters);
        parameters.setItem(COSName.F, obj);
    }

    public void setDecode(COSArray decode)
    {
        parameters.setItem(COSName.D, decode);
    }

    public COSArray getDecode()
    {
        return (COSArray) parameters.getDictionaryObject(COSName.D, COSName.DECODE);
    }

    public boolean isStencil()
    {
        return parameters.getBoolean(COSName.IM, COSName.IMAGE_MASK, false);
    }

    public void setStencil(boolean isStencil)
    {
        parameters.setBoolean(COSName.IM, isStencil);
    }

    public PDStream getStream() throws IOException
    {
        return stream;
    }

    public BufferedImage getImage() throws IOException
    {
        return SampledImageReader.getRGBImage(this, getColorKeyMask());
    }

    public BufferedImage getStencilImage(Paint paint) throws IOException
    {
        if (!isStencil())
        {
            throw new IllegalStateException("Image is not a stencil");
        }
        return SampledImageReader.getStencilImage(this, paint);
    }

    /**
     * Returns the color key mask array associated with this image, or null if there is none.
     * @return Mask Image XObject
     */
    public COSArray getColorKeyMask()
    {
        COSBase mask = parameters.getDictionaryObject(COSName.IM, COSName.MASK);
        if (mask instanceof COSArray)
        {
            return (COSArray)mask;
        }
        return null;
    }

    /**
     * Returns the suffix for this image type, e.g. jpg/png.
     * @return The image suffix.
     */
    public String getSuffix()
    {
        // TODO implement me
        return null;
    }
}
