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

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.util.filetypedetector.FileType;
import org.apache.pdfbox.util.filetypedetector.FileTypeDetector;

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

    private SoftReference<BufferedImage> cachedImage;
    private PDColorSpace colorSpace;

    /**
     * current resource dictionary (has color spaces)
     */
    private final PDResources resources;

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
     * Creates an Image XObject in the given document. This constructor is for internal PDFBox use
     * and is not for PDF generation. Users who want to create images should look at {@link #createFromFileByExtension(File, PDDocument)
     * }.
     *
     * @param document the current document
     * @throws java.io.IOException if there is an error creating the XObject.
     */
    public PDImageXObject(PDDocument document) throws IOException
    {
        this(new PDStream(document), null);
    }

    /**
     * Creates an Image XObject in the given document using the given filtered stream. This
     * constructor is for internal PDFBox use and is not for PDF generation. Users who want to
     * create images should look at {@link #createFromFileByExtension(File, PDDocument) }.
     *
     * @param document the current document
     * @param encodedStream an encoded stream of image data
     * @param cosFilter the filter or a COSArray of filters
     * @param width the image width
     * @param height the image height
     * @param bitsPerComponent the bits per component
     * @param initColorSpace the color space
     * @throws IOException if there is an error creating the XObject.
     */
    public PDImageXObject(PDDocument document, InputStream encodedStream, 
            COSBase cosFilter, int width, int height, int bitsPerComponent, 
            PDColorSpace initColorSpace) throws IOException
    {
        super(createRawStream(document, encodedStream), COSName.IMAGE);
        getCOSObject().setItem(COSName.FILTER, cosFilter);
        resources = null;
        colorSpace = null;
        setBitsPerComponent(bitsPerComponent);
        setWidth(width);
        setHeight(height);
        setColorSpace(initColorSpace);
    }

    /**
     * Creates a COS stream from raw (encoded) data.
     */
    private static COSStream createRawStream(PDDocument document, InputStream rawInput)
            throws IOException
    {
        COSStream stream = document.getDocument().createCOSStream();
        OutputStream output = null;
        try
        {
            output = stream.createRawOutputStream();
            IOUtils.copy(rawInput, output);
        }
        finally
        {
            if (output != null)
            {
                output.close();
            }
        }
        return stream;
    }

    /**
     * Creates an Image XObject with the given stream as its contents and current color spaces. This
     * constructor is for internal PDFBox use and is not for PDF generation. Users who want to
     * create images should look at {@link #createFromFileByExtension(File, PDDocument) }.
     *
     * @param stream the XObject stream to read
     * @param resources the current resources
     * @throws java.io.IOException if there is an error creating the XObject.
     */
    public PDImageXObject(PDStream stream, PDResources resources) throws IOException
    {
        this(stream, resources, stream.createInputStream());
    }
    
    /**
     * Create a PDImageXObject from an image file, see {@link #createFromFileByExtension(File, PDDocument)} for
     * more details.
     *
     * @param imagePath the image file path.
     * @param doc the document that shall use this PDImageXObject.
     * @return a PDImageXObject.
     * @throws IOException if there is an error when reading the file or creating the
     * PDImageXObject, or if the image type is not supported.
     */
    public static PDImageXObject createFromFile(String imagePath, PDDocument doc) throws IOException
    {
        return createFromFileByExtension(new File(imagePath), doc);
    }

    /**
     * Create a PDImageXObject from an image file. The file format is determined by the file name
     * suffix. The following suffixes are supported: jpg, jpeg, tif, tiff, gif, bmp and png. This is
     * a convenience method that calls {@link JPEGFactory#createFromStream},
     * {@link CCITTFactory#createFromFile} or {@link ImageIO#read} combined with
     * {@link LosslessFactory#createFromImage}. (The later can also be used to create a
     * PDImageXObject from a BufferedImage).
     *
     * @param file the image file.
     * @param doc the document that shall use this PDImageXObject.
     * @return a PDImageXObject.
     * @throws IOException if there is an error when reading the file or creating the
     * PDImageXObject.
     * @throws IllegalArgumentException if the image type is not supported.
     */
    public static PDImageXObject createFromFileByExtension(File file, PDDocument doc) throws IOException
    {
        String name = file.getName();
        int dot = file.getName().lastIndexOf('.');
        if (dot == -1)
        {
            throw new IllegalArgumentException("Image type not supported: " + name);
        }
        String ext = name.substring(dot + 1).toLowerCase();
        if ("jpg".equals(ext) || "jpeg".equals(ext))
        {
            FileInputStream fis = new FileInputStream(file);
            PDImageXObject imageXObject = JPEGFactory.createFromStream(doc, fis);
            fis.close();
            return imageXObject;
        }
        if ("tif".equals(ext) || "tiff".equals(ext))
        {
            return CCITTFactory.createFromFile(doc, file);
        }
        if ("gif".equals(ext) || "bmp".equals(ext) || "png".equals(ext))
        {
            BufferedImage bim = ImageIO.read(file);
            return LosslessFactory.createFromImage(doc, bim);
        }
        throw new IllegalArgumentException("Image type not supported: " + name);
    }

    /**
     * Create a PDImageXObject from an image file. The file format is determined by the file
     * content. The following file types are supported: jpg, jpeg, tif, tiff, gif, bmp and png. This
     * is a convenience method that calls {@link JPEGFactory#createFromStream},
     * {@link CCITTFactory#createFromFile} or {@link ImageIO#read} combined with
     * {@link LosslessFactory#createFromImage}. (The later can also be used to create a
     * PDImageXObject from a BufferedImage).
     *
     * @param file the image file.
     * @param doc the document that shall use this PDImageXObject.
     * @return a PDImageXObject.
     * @throws IOException if there is an error when reading the file or creating the
     * PDImageXObject.
     * @throws IllegalArgumentException if the image type is not supported.
     */
    public static PDImageXObject createFromFileByContent(File file, PDDocument doc) throws IOException
    {
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        FileType fileType = null;
        try
        {
            fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            fileType = FileTypeDetector.detectFileType(bufferedInputStream);
        }
        catch (IOException e)
        {
            throw new IOException("Could not determine file type: " + file.getName(), e);
        }
        finally
        {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(bufferedInputStream);
        }
        if (fileType == null)
        {
            throw new IllegalArgumentException("Image type not supported: " + file.getName());
        }

        if (fileType.equals(FileType.JPEG))
        {
            FileInputStream fis = new FileInputStream(file);
            PDImageXObject imageXObject = JPEGFactory.createFromStream(doc, fis);
            fis.close();
            return imageXObject;
        }
        if (fileType.equals(FileType.TIFF))
        {
            return CCITTFactory.createFromFile(doc, file);
        }
        if (fileType.equals(FileType.BMP) || fileType.equals(FileType.GIF) || fileType.equals(FileType.PNG))
        {
            BufferedImage bim = ImageIO.read(file);
            return LosslessFactory.createFromImage(doc, bim);
        }
        throw new IllegalArgumentException("Image type not supported: " + file.getName());
    }

    // repairs parameters using decode result
    private PDImageXObject(PDStream stream, PDResources resources, COSInputStream input)
    {
        super(repair(stream, input), COSName.IMAGE);
        this.resources = resources;
        this.colorSpace = input.getDecodeResult().getJPXColorSpace();
    }

    // repairs parameters using decode result
    private static PDStream repair(PDStream stream, COSInputStream input)
    {
        stream.getCOSObject().addAll(input.getDecodeResult().getParameters());
        return stream;
    }

    /**
     * Returns the metadata associated with this XObject, or null if there is none.
     * @return the metadata associated with this object.
     */
    public PDMetadata getMetadata()
    {
        COSStream cosStream = (COSStream) getCOSObject().getDictionaryObject(COSName.METADATA);
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
        getCOSObject().setItem(COSName.METADATA, meta);
    }

    /**
     * Returns the key of this XObject in the structural parent tree.
     * @return this object's key the structural parent tree
     */
    public int getStructParent()
    {
        return getCOSObject().getInt(COSName.STRUCT_PARENT, 0);
    }

    /**
     * Sets the key of this XObject in the structural parent tree.
     * @param key the new key for this XObject
     */
    public void setStructParent(int key)
    {
        getCOSObject().setInt(COSName.STRUCT_PARENT, key);
    }

    /**
     * {@inheritDoc}
     * The returned images are cached via a SoftReference.
     */
    @Override
    public BufferedImage getImage() throws IOException
    {
        if (cachedImage != null)
        {
            BufferedImage cached = cachedImage.get();
            if (cached != null)
            {
                return cached;
            }
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
            // explicit mask - to be applied only if /ImageMask true
            PDImageXObject mask = getMask();
            if (mask != null && mask.isStencil())
            {
                image = applyMask(image, mask.getOpaqueImage(), false);
            }
        }

        cachedImage = new SoftReference<BufferedImage>(image);
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

        // scale mask to fit image, or image to fit mask, whichever is larger
        if (mask.getWidth() < width || mask.getHeight() < height)
        {
            mask = scaleImage(mask, width, height);
        }
        else if (mask.getWidth() > width || mask.getHeight() > height)
        {
            width = mask.getWidth();
            height = mask.getHeight();
            image = scaleImage(image, width, height);
        }

        // compose to ARGB
        BufferedImage masked = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster src = image.getRaster();
        WritableRaster dest = masked.getRaster();
        WritableRaster alpha = mask.getRaster();

        float[] rgb = new float[4];
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
     * High-quality image scaling.
     */
    private BufferedImage scaleImage(BufferedImage image, int width, int height)
    {
        BufferedImage image2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image2.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return image2;
    }

    /**
     * Returns the Mask Image XObject associated with this image, or null if there is none.
     * @return Mask Image XObject
     * @throws java.io.IOException
     */
    public PDImageXObject getMask() throws IOException
    {
        COSBase mask = getCOSObject().getDictionaryObject(COSName.MASK);
        if (mask instanceof COSArray)
        {
            // color key mask, no explicit mask to return
            return null;
        }
        else
        {
            COSStream cosStream = (COSStream) getCOSObject().getDictionaryObject(COSName.MASK);
            if (cosStream != null)
            {
                // always DeviceGray
                return new PDImageXObject(new PDStream(cosStream), null);
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
        COSBase mask = getCOSObject().getDictionaryObject(COSName.MASK);
        if (mask instanceof COSArray)
        {
            return (COSArray)mask;
        }
        return null;
    }

    /**
     * Returns the Soft Mask Image XObject associated with this image, or null if there is none.
     * @return the SMask Image XObject, or null.
     * @throws java.io.IOException
     */
    public PDImageXObject getSoftMask() throws IOException
    {
        COSStream cosStream = (COSStream) getCOSObject().getDictionaryObject(COSName.SMASK);
        if (cosStream != null)
        {
            // always DeviceGray
            return new PDImageXObject(new PDStream(cosStream), null);
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
            return getCOSObject().getInt(COSName.BITS_PER_COMPONENT, COSName.BPC);
        }
    }

    @Override
    public void setBitsPerComponent(int bpc)
    {
        getCOSObject().setInt(COSName.BITS_PER_COMPONENT, bpc);
    }

    @Override
    public PDColorSpace getColorSpace() throws IOException
    {
        if (colorSpace == null)
        {
            COSBase cosBase = getCOSObject().getDictionaryObject(COSName.COLORSPACE, COSName.CS);
            if (cosBase != null)
            {
                colorSpace = PDColorSpace.create(cosBase, resources);
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
    public InputStream createInputStream() throws IOException
    {
        return getStream().createInputStream();
    }

    @Override
    public InputStream createInputStream(List<String> stopFilters) throws IOException
    {
        return getStream().createInputStream(stopFilters);
    }

    @Override
    public boolean isEmpty()
    {
        return getStream().getCOSObject().getLength() == 0;
    }

    @Override
    public void setColorSpace(PDColorSpace cs)
    {
        getCOSObject().setItem(COSName.COLORSPACE, cs != null ? cs.getCOSObject() : null);
    }

    @Override
    public int getHeight()
    {
        return getCOSObject().getInt(COSName.HEIGHT);
    }

    @Override
    public void setHeight(int h)
    {
        getCOSObject().setInt(COSName.HEIGHT, h);
    }

    @Override
    public int getWidth()
    {
        return getCOSObject().getInt(COSName.WIDTH);
    }

    @Override
    public void setWidth(int w)
    {
        getCOSObject().setInt(COSName.WIDTH, w);
    }

    @Override
    public boolean getInterpolate()
    {
        return getCOSObject().getBoolean(COSName.INTERPOLATE, false);
    }

    @Override
    public void setInterpolate(boolean value)
    {
        getCOSObject().setBoolean(COSName.INTERPOLATE, value);
    }

    @Override
    public void setDecode(COSArray decode)
    {
        getCOSObject().setItem(COSName.DECODE, decode);
    }

    @Override
    public COSArray getDecode()
    {
        COSBase decode = getCOSObject().getDictionaryObject(COSName.DECODE);
        if (decode instanceof COSArray)
        {
            return (COSArray) decode;
        }
        return null;
    }

    @Override
    public boolean isStencil()
    {
        return getCOSObject().getBoolean(COSName.IMAGE_MASK, false);
    }

    @Override
    public void setStencil(boolean isStencil)
    {
        getCOSObject().setBoolean(COSName.IMAGE_MASK, isStencil);
    }

    /**
     * This will get the suffix for this image type, e.g. jpg/png.
     * @return The image suffix or null if not available.
     */
    @Override
    public String getSuffix()
    {
        List<COSName> filters = getStream().getFilters();

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
}
