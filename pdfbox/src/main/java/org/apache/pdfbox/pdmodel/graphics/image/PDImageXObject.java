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

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.filter.DecodeResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An Image XObject.
 *
 * @author John Hewson
 * @author Ben Litchfield
 */
public final class PDImageXObject extends PDXObject implements PDImage
{
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDImageXObject.class);

    private BufferedImage cachedImage;
    private PDColorSpace colorSpace;
    private Map<String, PDColorSpace> colorSpaces;  // from current resource dictionary

    /**
     * Creates a thumbnail Image XObject from the given COSBase and name.
     * @param cosStream the COS stream
     * @return an XObject
     * @throws IOException if there is an error creating the XObject.
     */
    public static PDImageXObject createThumbnail(COSStream cosStream) throws IOException
    {
        // thumbnails are special, any non-null subtype is treated as being "Image"
        PDStream pdStream = new PDStream(cosStream);
        return new PDImageXObject(pdStream, null);
    }

    /**
     * Creates an Image XObject in the given document.
     * @param document the current document
     * @throws java.io.IOException if there is an error creating the XObject.
     */
    public PDImageXObject(PDDocument document) throws IOException
    {
        this(new PDStream(document), null);
    }

    /**
     * Creates an Image XObject in the given document using the given filtered stream.
     * @param document the current document
     * @param filteredStream a filtered stream of image data
     * @param cosFilter the filter or a COSArray of filters
     * @param width the image width
     * @param height the image height
     * @param bitsPerComponent the bits per component
     * @param initColorSpace the color space
     * @throws IOException if there is an error creating the XObject.
     */
    public PDImageXObject(PDDocument document, InputStream filteredStream, 
            COSBase cosFilter, int width, int height, int bitsPerComponent, 
            PDColorSpace initColorSpace) throws IOException
    {
        super(new PDStream(document, filteredStream, true), COSName.IMAGE);
        getCOSStream().setItem(COSName.FILTER, cosFilter);
        colorSpaces = null;
        colorSpace = null;
        setBitsPerComponent(bitsPerComponent);
        setWidth(width);
        setHeight(height);
        setColorSpace(initColorSpace);
    }

    /**
     * Creates an Image XObject with the given stream as its contents and current color spaces.
     * @param stream the XObject stream to read
     * @param colorSpaces the color spaces in the current resources dictionary, null for masks
     * @throws java.io.IOException if there is an error creating the XObject.
     */
    public PDImageXObject(PDStream stream, Map<String, PDColorSpace> colorSpaces) throws IOException
    {
        this(stream, colorSpaces, stream.getStream().getDecodeResult());
    }

    // repairs parameters using decode result
    private PDImageXObject(PDStream stream, Map<String, PDColorSpace> colorSpaces,
                           DecodeResult decodeResult)
    {
        super(repair(stream, decodeResult), COSName.IMAGE);
        this.colorSpaces = colorSpaces;
        this.colorSpace = decodeResult.getJPXColorSpace();
    }

    // repairs parameters using decode result
    private static PDStream repair(PDStream stream, DecodeResult decodeResult)
    {
        stream.getStream().addAll(decodeResult.getParameters());
        return stream;
    }

    /**
     * Returns the metadata associated with this XObject, or null if there is none.
     * @return the metadata associated with this object.
     */
    public PDMetadata getMetadata()
    {
        COSStream cosStream = (COSStream) getCOSStream().getDictionaryObject(COSName.METADATA);
        if (cosStream != null)
        {
            return new PDMetadata(cosStream);
        }
        return null;
    }

    /**
     * Sets the metadata associated with this XObject, or null if there is none.
     * @param meta the metadata associated with this object
     */
    public void setMetadata(PDMetadata meta)
    {
        getCOSStream().setItem(COSName.METADATA, meta);
    }

    /**
     * Returns the key of this XObject in the structural parent tree.
     * @return this object's key the structural parent tree
     */
    public int getStructParent()
    {
        return getCOSStream().getInt(COSName.STRUCT_PARENT, 0);
    }

    /**
     * Sets the key of this XObject in the structural parent tree.
     * @param key the new key for this XObject
     */
    public void setStructParent(int key)
    {
        getCOSStream().setInt(COSName.STRUCT_PARENT, key);
    }

    /**
     * {@inheritDoc}
     * The returned images are cached for the lifetime of this XObject.
     */
    @Override
    public BufferedImage getImage() throws IOException
    {
        if (cachedImage != null)
        {
            return cachedImage;
        }

        // get image as RGB
        BufferedImage image = SampledImageReader.getRGBImage(this, getColorKeyMask());

        // soft mask (overrides explicit mask)
        PDImageXObject softMask = getSoftMask();
        if (softMask != null)
        {
            image = applyMask(image, softMask.getOpaqueImage(), true);
        }
        else
        {
            // explicit mask
            PDImageXObject mask = getMask();
            if (mask != null)
            {
                image = applyMask(image, mask.getOpaqueImage(), false);
            }
        }

        cachedImage = image;
        return image;
    }

    /**
     * {@inheritDoc}
     * The returned images are not cached.
     */
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
     * Returns an RGB buffered image containing the opaque image stream without any masks applied.
     * If this Image XObject is a mask then the buffered image will contain the raw mask.
     * @return the image without any masks applied
     * @throws IOException if the image cannot be read
     */
    public BufferedImage getOpaqueImage() throws IOException
    {
        return SampledImageReader.getRGBImage(this, null);
    }

    // explicit mask: RGB + Binary -> ARGB
    // soft mask: RGB + Gray -> ARGB
    private BufferedImage applyMask(BufferedImage image, BufferedImage mask, boolean isSoft)
            throws IOException
    {
        if (mask == null)
        {
            return image;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // compose to ARGB
        BufferedImage masked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // scale mask to fit image
        if (mask.getWidth() != width || mask.getHeight() != height)
        {
            BufferedImage mask2 = new BufferedImage(width, height, mask.getType());
            Graphics2D g = mask2.createGraphics();
            g.drawImage(mask, 0, 0, width, height, 0, 0, mask.getWidth(), mask.getHeight(), null);
            g.dispose();
            mask = mask2;
        }

        WritableRaster src = image.getRaster();
        WritableRaster dest = masked.getRaster();
        WritableRaster alpha = mask.getRaster();

        float[] rgb = new float[3];
        float[] rgba = new float[4];
        float[] alphaPixel = null;
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                src.getPixel(x, y, rgb);

                rgba[0] = rgb[0];
                rgba[1] = rgb[1];
                rgba[2] = rgb[2];
                
                alphaPixel = alpha.getPixel(x, y, alphaPixel);
                if (isSoft)
                {
                    rgba[3] = alphaPixel[0];
                }
                else
                {
                    rgba[3] = 255 - alphaPixel[0];
                }

                dest.setPixel(x, y, rgba);
            }
        }

        return masked;
    }

    /**
     * Returns the Mask Image XObject associated with this image, or null if there is none.
     * @return Mask Image XObject
     */
    public PDImageXObject getMask() throws IOException
    {
        COSBase mask = getCOSStream().getDictionaryObject(COSName.MASK);
        if (mask instanceof COSArray)
        {
            // color key mask, no explicit mask to return
            return null;
        }
        else
        {
            COSStream cosStream = (COSStream)getCOSStream().getDictionaryObject(COSName.MASK);
            if (cosStream != null)
            {
                return new PDImageXObject(new PDStream(cosStream), null); // always DeviceGray
            }
            return null;
        }
    }

    /**
     * Returns the color key mask array associated with this image, or null if there is none.
     * @return Mask Image XObject
     */
    public COSArray getColorKeyMask()
    {
        COSBase mask = getCOSStream().getDictionaryObject(COSName.MASK);
        if (mask instanceof COSArray)
        {
            return (COSArray)mask;
        }
        return null;
    }

    /**
     * Returns the Soft Mask Image XObject associated with this image, or null if there is none.
     * @return the SMask Image XObject, or null.
     */
    public PDImageXObject getSoftMask() throws IOException
    {
        COSStream cosStream = (COSStream)getCOSStream().getDictionaryObject(COSName.SMASK);
        if (cosStream != null)
        {
            return new PDImageXObject(new PDStream(cosStream), null);  // always DeviceGray
        }
        return null;
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
            return getCOSStream().getInt(COSName.BITS_PER_COMPONENT, COSName.BPC);
        }
    }

    @Override
    public void setBitsPerComponent(int bpc)
    {
        getCOSStream().setInt(COSName.BITS_PER_COMPONENT, bpc);
    }

    @Override
    public PDColorSpace getColorSpace() throws IOException
    {
        if (colorSpace == null)
        {
            COSBase cosBase = getCOSStream().getDictionaryObject(COSName.COLORSPACE, COSName.CS);
            if (cosBase != null)
            {
                colorSpace = PDColorSpace.create(cosBase, colorSpaces, null);
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
        return colorSpace;
    }

    @Override
    public PDStream getStream() throws IOException
    {
        return getPDStream();
    }

    @Override
    public void setColorSpace(PDColorSpace cs)
    {
        getCOSStream().setItem(COSName.COLORSPACE, cs != null ? cs.getCOSObject() : null);
    }

    @Override
    public int getHeight()
    {
        return getCOSStream().getInt(COSName.HEIGHT);
    }

    @Override
    public void setHeight(int h)
    {
        getCOSStream().setInt(COSName.HEIGHT, h);
    }

    @Override
    public int getWidth()
    {
        return getCOSStream().getInt(COSName.WIDTH);
    }

    @Override
    public void setWidth(int w)
    {
        getCOSStream().setInt(COSName.WIDTH, w);
    }

    @Override
    public void setDecode(COSArray decode)
    {
        getCOSStream().setItem(COSName.DECODE, decode);
    }

    @Override
    public COSArray getDecode()
    {
        COSBase decode = getCOSStream().getDictionaryObject(COSName.DECODE);
        if (decode != null && decode instanceof COSArray)
        {
            return (COSArray) decode;
        }
        return null;
    }

    @Override
    public boolean isStencil()
    {
        return getCOSStream().getBoolean(COSName.IMAGE_MASK, false);
    }

    @Override
    public void setStencil(boolean isStencil)
    {
        getCOSStream().setBoolean(COSName.IMAGE_MASK, isStencil);
    }

    /**
     * This will get the suffix for this image type, e.g. jpg/png.
     * @return The image suffix or null if not available.
     */
    public String getSuffix()
    {
        List<COSName> filters = getPDStream().getFilters();

        if (filters == null)
        {
            return "png";
        }
        else if (filters.contains(COSName.DCT_DECODE))
        {
            return "jpg";
        }
        else if (filters.contains(COSName.JPX_DECODE))
        {
            return "jpx";
        }
        else if (filters.contains(COSName.CCITTFAX_DECODE))
        {
            return "tiff";
        }
        else if (filters.contains(COSName.FLATE_DECODE)
                || filters.contains(COSName.LZW_DECODE)
                || filters.contains(COSName.RUN_LENGTH_DECODE))
        {
            return "png";
        }
        else
        {
            LOG.warn("getSuffix() returns null, filters: " + filters);
            // TODO more...
            return null;
        }
    }
    
    @Override
    public void clear()
    {
        super.clear();
        cachedImage = null;
    }
}
