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
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.DecodeOptions;
import org.apache.pdfbox.filter.DecodeResult;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;

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
     * @throws IOException if the stream cannot be decoded
     */
    public PDInlineImage(COSDictionary parameters, byte[] data, PDResources resources)
            throws IOException
    {
        this.parameters = parameters;
        this.resources = resources;
        this.rawData = data;

        DecodeResult decodeResult = null;
        List<String> filters = getFilters();
        if (filters.isEmpty())
        {
            this.decodedData = data;
        }
        else
        {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
            byte[] ba = null;
            for (int i = 0; i < filters.size(); i++)
            {
                // TODO handling of abbreviated names belongs here, rather than in other classes
                out.reset();
                Filter filter = FilterFactory.INSTANCE.getFilter(filters.get(i));
                decodeResult = filter.decode(in, out, parameters, i);
                ba = out.toByteArray();
                in = new ByteArrayInputStream(ba);
            }
            this.decodedData = ba;
        }

        // repair parameters
        if (decodeResult != null)
        {
            parameters.addAll(decodeResult.getParameters());
        }
    }

    @Override
    public COSDictionary getCOSObject()
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
        COSBase cs = parameters.getDictionaryObject(COSName.CS, COSName.COLORSPACE);
        if (cs != null)
        {
            return createColorSpace(cs);
        }
        else if (isStencil())
        {
            // stencil mask color space must be gray, it is often missing
            return PDDeviceGray.INSTANCE;
        }
        else
        {
            // an image without a color space is always broken
            throw new IOException("could not determine inline image color space");
        }
    }
    
    // deliver the long name of a device colorspace, or the parameter
    private COSBase toLongName(COSBase cs)
    {
        if (COSName.RGB.equals(cs))
        {
            return COSName.DEVICERGB;
        }
        if (COSName.CMYK.equals(cs))
        {
            return COSName.DEVICECMYK;
        }
        if (COSName.G.equals(cs))
        {
            return COSName.DEVICEGRAY;
        }
        return cs;
    }
    
    private PDColorSpace createColorSpace(COSBase cs) throws IOException
    {
        if (cs instanceof COSName)
        {
            return PDColorSpace.create(toLongName(cs), resources);
        }

        if (cs instanceof COSArray && ((COSArray) cs).size() > 1)
        {
            COSArray srcArray = (COSArray) cs;
            COSBase csType = srcArray.get(0);
            if (COSName.I.equals(csType) || COSName.INDEXED.equals(csType))
            {
                COSArray dstArray = new COSArray();
                dstArray.addAll(srcArray);
                dstArray.set(0, COSName.INDEXED);
                dstArray.set(1, toLongName(srcArray.get(1)));
                return PDColorSpace.create(dstArray, resources);
            }

            throw new IOException("Illegal type of inline image color space: " + csType);
        }

        throw new IOException("Illegal type of object for inline image color space: " + cs);
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
     * Returns A list of filters applied to this stream.
     *
     * @return A (possibly empty) list of filters applied to this stream, never null.
     */
    public List<String> getFilters()
    {
        COSBase filters = parameters.getDictionaryObject(COSName.F, COSName.FILTER);
        if (filters instanceof COSName)
        {
            COSName name = (COSName) filters;
            return Collections.singletonList(name.getName());
        }
        else if (filters instanceof COSArray)
        {
            return ((COSArray) filters).toCOSNameStringList();
        }
        return Collections.emptyList();
    }

    /**
     * Sets which filters are applied to this stream.
     *
     * @param filters the filters to apply to this stream.
     */
    public void setFilters(List<String> filters)
    {
        COSBase obj = COSArray.ofCOSNames(filters);
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

    @Override
    public InputStream createInputStream() throws IOException
    {
        return new ByteArrayInputStream(decodedData);
    }

    @Override
    public InputStream createInputStream(DecodeOptions options) throws IOException
    {
        // Decode options are irrelevant for inline image, as the data is always buffered.
        return createInputStream();
    }

    @Override
    public InputStream createInputStream(List<String> stopFilters) throws IOException
    {
        List<String> filters = getFilters();
        ByteArrayInputStream in = new ByteArrayInputStream(rawData);
        ByteArrayOutputStream out = new ByteArrayOutputStream(rawData.length);
        for (int i = 0; i < filters.size(); i++)
        {
            if (stopFilters.contains(filters.get(i)))
            {
                break;
            }

            // TODO handling of abbreviated names belongs here, rather than in other classes
            Filter filter = FilterFactory.INSTANCE.getFilter(filters.get(i));
            out.reset();
            filter.decode(in, out, parameters, i);
            in = new ByteArrayInputStream(out.toByteArray());
        }
        return in;
    }

    @Override
    public boolean isEmpty()
    {
        return decodedData.length == 0;
    }

    /**
     * Returns the inline image data.
     * 
     * @return the inline image data
     */
    public byte[] getData()
    {
        return decodedData;
    }
    
    @Override
    public BufferedImage getImage() throws IOException
    {
        return SampledImageReader.getRGBImage(this, null);
    }

    @Override
    public BufferedImage getImage(Rectangle region, int subsampling) throws IOException
    {
        return SampledImageReader.getRGBImage(this, region, subsampling, null);
    }

    @Override
    public WritableRaster getRawRaster() throws IOException
    {
        return SampledImageReader.getRawRaster(this);
    }

    @Override
    public BufferedImage getRawImage() throws IOException
    {
        return getColorSpace().toRawImage(getRawRaster());
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
     * Returns the suffix for this image type, e.g. jpg/png.
     *
     * @return The image suffix.
     */
    @Override
    public String getSuffix()
    {
        List<String> filters = getFilters();

        if (filters == null || filters.isEmpty())
        {
            return "png";
        }
        if (filters.contains(COSName.DCT_DECODE.getName()) || 
            filters.contains(COSName.DCT_DECODE_ABBREVIATION.getName()))
        {
            return "jpg";
        }
        if (filters.contains(COSName.CCITTFAX_DECODE.getName()) ||
            filters.contains(COSName.CCITTFAX_DECODE_ABBREVIATION.getName()))
        {
            return "tiff";
        }
        // JPX and JBIG2 don't exist for inline images
        return "png";        
    }
}
