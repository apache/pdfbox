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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.ImagingOpException;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInputStream;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.filter.DecodeOptions;
import org.apache.pdfbox.filter.DecodeResult;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
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

    // initialize to MAX_VALUE as we prefer lower subsampling when keeping/replacing cache.
    private int cachedImageSubsampling = Integer.MAX_VALUE;
    // indicates whether this image has an JPX-based filter applied
    private boolean hasJPXFilter = false;
    // is set to true after reading some values from a JPX-based image
    private boolean jpxValuesInitialized = false;

    /**
     * current resource dictionary (has color spaces)
     */
    private final PDResources resources;

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
        super(stream, COSName.IMAGE);
        this.resources = resources;
        List<COSName> filters = stream.getFilters();
        if (!filters.isEmpty() && COSName.JPX_DECODE.equals(filters.get(filters.size() - 1)))
        {
            hasJPXFilter = true;
        }
    }

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
     * Creates a COS stream from raw (encoded) data.
     */
    private static COSStream createRawStream(PDDocument document, InputStream rawInput)
            throws IOException
    {
        COSStream stream = document.getDocument().createCOSStream();
        try (OutputStream output = stream.createRawOutputStream())
        {
            IOUtils.copy(rawInput, output);
        }
        return stream;
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
     * suffix. The following suffixes are supported: JPG, JPEG, TIF, TIFF, GIF, BMP and PNG. This is
     * a convenience method that calls {@link JPEGFactory#createFromStream},
     * {@link CCITTFactory#createFromFile} or {@link ImageIO#read} combined with
     * {@link LosslessFactory#createFromImage}. (The later can also be used to create a
     * PDImageXObject from a BufferedImage). Starting with 2.0.18, this call will create an image
     * directly from a PNG file without decoding it (when possible), which is faster. However the
     * result size depends on the compression skill of the software that created the PNG file. If
     * file size or bandwidth are important to you or to your clients, then create your PNG files
     * with a tool that has implemented the
     * <a href="https://blog.codinghorror.com/zopfli-optimization-literally-free-bandwidth/">Zopfli
     * algorithm</a>, or use the two-step process mentioned above.
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
        int dot = name.lastIndexOf('.');
        if (dot == -1)
        {
            throw new IllegalArgumentException("Image type not supported: " + name);
        }
        String ext = name.substring(dot + 1).toLowerCase();
        if ("jpg".equals(ext) || "jpeg".equals(ext))
        {
            try (FileInputStream fis = new FileInputStream(file))
            {
                return JPEGFactory.createFromStream(doc, fis);
            }
        }
        if ("tif".equals(ext) || "tiff".equals(ext))
        {
            try
            {
                return CCITTFactory.createFromFile(doc, file);
            }
            catch (IOException ex)
            {
                LOG.debug("Reading as TIFF failed, setting fileType to PNG", ex);
                // Plan B: try reading with ImageIO
                // common exception:
                // First image in tiff is not CCITT T4 or T6 compressed
                ext = "png";
            }
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
     * content. The following file types are supported: JPG, JPEG, TIF, TIFF, GIF, BMP and PNG. This
     * is a convenience method that calls {@link JPEGFactory#createFromStream},
     * {@link CCITTFactory#createFromFile} or {@link ImageIO#read} combined with
     * {@link LosslessFactory#createFromImage}. (The later can also be used to create a
     * PDImageXObject from a BufferedImage). Starting with 2.0.18, this call will create an image
     * directly from a PNG file without decoding it (when possible), which is faster. However the
     * result size depends on the compression skill of the software that created the PNG file. If
     * file size or bandwidth are important to you or to your clients, then create your PNG files
     * with a tool that has implemented the
     * <a href="https://blog.codinghorror.com/zopfli-optimization-literally-free-bandwidth/">Zopfli
     * algorithm</a>, or use the two-step process mentioned above.
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
        FileType fileType = null;
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file)))
        {
            fileType = FileTypeDetector.detectFileType(bufferedInputStream);
        }
        catch (IOException e)
        {
            throw new IOException("Could not determine file type: " + file.getName(), e);
        }
        if (fileType == null)
        {
            throw new IllegalArgumentException("Image type not supported: " + file.getName());
        }

        if (fileType == FileType.JPEG)
        {
            try (FileInputStream fis = new FileInputStream(file))
            {
                return JPEGFactory.createFromStream(doc, fis);
            }
        }
        if (fileType == FileType.TIFF)
        {
            try
            {
                return CCITTFactory.createFromFile(doc, file);
            }
            catch (IOException ex)
            {
                LOG.debug("Reading as TIFF failed, setting fileType to PNG", ex);
                // Plan B: try reading with ImageIO
                // common exception:
                // First image in tiff is not CCITT T4 or T6 compressed
                fileType = FileType.PNG;
            }
        }
        if (fileType == FileType.BMP || fileType == FileType.GIF || fileType == FileType.PNG)
        {
            BufferedImage bim = ImageIO.read(file);
            return LosslessFactory.createFromImage(doc, bim);
        }
        throw new IllegalArgumentException("Image type " + fileType + " not supported: " + file.getName());
    }

    /**
     * Create a PDImageXObject from bytes of an image file. The file format is determined by the
     * file content. The following file types are supported: JPG, JPEG, TIF, TIFF, GIF, BMP and PNG.
     * This is a convenience method that calls {@link JPEGFactory#createFromByteArray},
     * {@link CCITTFactory#createFromFile} or {@link ImageIO#read} combined with
     * {@link LosslessFactory#createFromImage}. (The later can also be used to create a
     * PDImageXObject from a BufferedImage). Starting with 2.0.18, this call will create an image
     * directly from a PNG file without decoding it (when possible), which is faster. However the
     * result size depends on the compression skill of the software that created the PNG file. If
     * file size or bandwidth are important to you or to your clients, then create your PNG files
     * with a tool that has implemented the
     * <a href="https://blog.codinghorror.com/zopfli-optimization-literally-free-bandwidth/">Zopfli
     * algorithm</a>, or use the two-step process mentioned above.
     *
     * @param byteArray bytes from an image file.
     * @param document the document that shall use this PDImageXObject.
     * @param name name of image file for exception messages, can be null.
     * @return a PDImageXObject.
     * @throws IOException if there is an error when reading the file or creating the
     * PDImageXObject.
     * @throws IllegalArgumentException if the image type is not supported.
     */
    public static PDImageXObject createFromByteArray(PDDocument document, byte[] byteArray, String name) throws IOException
    {
        FileType fileType = FileTypeDetector.detectFileType(byteArray);
        if (fileType == null)
        {
            throw new IllegalArgumentException("Image type not supported: " + name);
        }

        if (fileType == FileType.JPEG)
        {
            return JPEGFactory.createFromByteArray(document, byteArray);
        }
        if (fileType == FileType.PNG)
        {
            // Try to directly convert the image without recoding it.
            PDImageXObject image = PNGConverter.convertPNGImage(document, byteArray);
            if (image != null)
            {
                return image;
            }
        }
        if (fileType == FileType.TIFF)
        {
            try
            {
                return CCITTFactory.createFromByteArray(document, byteArray);
            }
            catch (IOException ex)
            {
                LOG.debug("Reading as TIFF failed, setting fileType to PNG", ex);
                // Plan B: try reading with ImageIO
                // common exception:
                // First image in tiff is not CCITT T4 or T6 compressed
                fileType = FileType.PNG;
            }
        }
        if (fileType == FileType.BMP || fileType == FileType.GIF || fileType == FileType.PNG)
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            BufferedImage bim = ImageIO.read(bais);
            return LosslessFactory.createFromImage(document, bim);
        }
        throw new IllegalArgumentException("Image type " + fileType + " not supported: " + name);
    }

    /**
     * Returns the metadata associated with this XObject, or null if there is none.
     * @return the metadata associated with this object.
     */
    public PDMetadata getMetadata()
    {
        COSStream cosStream = getCOSObject().getCOSStream(COSName.METADATA);
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
     *
     * @return this object's key the structural parent tree or -1 if there isn't any.
     */
    public int getStructParent()
    {
        return getCOSObject().getInt(COSName.STRUCT_PARENT);
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
        return getImage(null, 1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getImage(Rectangle region, int subsampling) throws IOException
    {
        if (region == null && subsampling == cachedImageSubsampling && cachedImage != null)
        {
            BufferedImage cached = cachedImage.get();
            if (cached != null)
            {
                return cached;
            }
        }

        initJPXValues();

        // get RGB image w/o reference because applyMask might modify it, take long time and a lot of memory. 
        final BufferedImage image;
        final PDImageXObject softMask = getSoftMask();
        final PDImageXObject mask = getMask();
        // soft mask (overrides explicit mask)
        if (softMask != null)
        {
            image = applyMask(SampledImageReader.getRGBImage(this, region, subsampling, getColorKeyMask()),
                    softMask.getOpaqueImage(region, subsampling), softMask.getInterpolate(), true,
                    extractMatte(softMask));
        }
        // explicit mask - to be applied only if /ImageMask true
        else if (mask != null && mask.isStencil())
        {
            image = applyMask(SampledImageReader.getRGBImage(this, region, subsampling, getColorKeyMask()),
                    mask.getOpaqueImage(region, subsampling), mask.getInterpolate(), false, null);
        }
        else
        {
            image = SampledImageReader.getRGBImage(this, region, subsampling, getColorKeyMask());
        }

        if (region == null && subsampling <= cachedImageSubsampling)
        {
            // only cache full-image renders, and prefer lower subsampling frequency, as lower
            // subsampling means higher quality and longer render times.
            cachedImageSubsampling = subsampling;
            cachedImage = new SoftReference<>(image);
        }

        return image;
    }

    @Override
    public BufferedImage getRawImage() throws IOException
    {
        return getColorSpace().toRawImage(getRawRaster());
    }

    @Override
    public WritableRaster getRawRaster() throws IOException
    {
        return SampledImageReader.getRawRaster(this);
    }

    /**
     * Extract the matte color from a softmask.
     * 
     * @param softMask
     * @return the matte color.
     * @throws IOException if the color conversion fails.
     */
    private float[] extractMatte(PDImageXObject softMask) throws IOException
    {
        COSBase base = softMask.getCOSObject().getItem(COSName.MATTE);
        float[] matte = null;
        if (base instanceof COSArray)
        {
            // PDFBOX-4267: process /Matte
            // see PDF specification 1.7, 11.6.5.3 Soft-Mask Images
            matte = ((COSArray) base).toFloatArray();
            // convert to RGB
            if (matte.length < getColorSpace().getNumberOfComponents())
            {
                LOG.error("Image /Matte entry not long enough for colorspace, skipped");
                return null;
            }
            matte = getColorSpace().toRGB(matte);
        }
        return matte;
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
     * Returns an RGB buffered image containing the opaque image stream without any masks applied. If this Image XObject
     * is a mask then the buffered image will contain the raw mask.
     * 
     * @return the image without any masks applied
     * @throws IOException if the image cannot be read
     */
    public BufferedImage getOpaqueImage() throws IOException
    {
        return getOpaqueImage(null, 1);
    }

    /**
     * Returns an RGB buffered image containing the opaque image stream without any masks applied. If this Image XObject
     * is a mask then the buffered image will contain the raw mask.
     * 
     * @param region The region of the source image to get, or null if the entire image is needed. The actual region
     * will be clipped to the dimensions of the source image.
     * 
     * @param subsampling The amount of rows and columns to advance for every output pixel, a value of 1 meaning every
     * pixel will be read. It must not be larger than the image width or height.
     * 
     * @return the image without any masks applied
     * @throws IOException if the image cannot be read
     */
    public BufferedImage getOpaqueImage(Rectangle region, int subsampling) throws IOException
    {
        return SampledImageReader.getRGBImage(this, region, subsampling, null);
    }

    /**
     * @param image The image to apply the mask to as alpha channel.
     * @param mask A mask image in 8 bit Gray. Even for a stencil mask image due to
     * {@link #getOpaqueImage()} and {@link SampledImageReader}'s {@code from1Bit()} special
     * handling of DeviceGray.
     * @param interpolateMask interpolation flag of the mask image.
     * @param isSoft {@code true} if a soft mask. If not stencil mask, then alpha will be inverted
     * by this method.
     * @param matte an optional RGB matte if a soft mask.
     * @return an ARGB image (can be the altered original image)
     */
    private BufferedImage applyMask(BufferedImage image, BufferedImage mask, boolean interpolateMask,
            boolean isSoft, float[] matte)
    {
        if (mask == null)
        {
            return image;
        }

        final int width = Math.max(image.getWidth(), mask.getWidth());
        final int height = Math.max(image.getHeight(), mask.getHeight());

        // scale mask to fit image, or image to fit mask, whichever is larger.
        // also make sure that mask is 8 bit gray and image is ARGB as this
        // is what needs to be returned.
        if (mask.getWidth() < width || mask.getHeight() < height)
        {
            mask = scaleImage(mask, width, height, BufferedImage.TYPE_BYTE_GRAY, interpolateMask);
        }
        else if (mask.getType() != BufferedImage.TYPE_BYTE_GRAY)
        {
            mask = scaleImage(mask, width, height, BufferedImage.TYPE_BYTE_GRAY, false);
        }

        if (image.getWidth() < width || image.getHeight() < height)
        {
            image = scaleImage(image, width, height, BufferedImage.TYPE_INT_ARGB, getInterpolate());
        }
        else if (image.getType() != BufferedImage.TYPE_INT_ARGB)
        {
            image = scaleImage(image, width, height, BufferedImage.TYPE_INT_ARGB, false);
        }

        // compose alpha into ARGB image, either:
        // - very fast by direct bit combination if not a soft mask and a 8 bit alpha source. 
        // - fast by letting the sample model do a bulk band operation if no matte is set.
        // - slow and complex by matte calculations on individual pixel components.
        final WritableRaster raster = image.getRaster();
        final WritableRaster alpha = mask.getRaster();
        if (!isSoft && raster.getDataBuffer().getSize() == alpha.getDataBuffer().getSize())
        {
            final DataBuffer dst = raster.getDataBuffer();
            final DataBuffer src = alpha.getDataBuffer();
            for (int i = 0, c = dst.getSize(); c > 0; i++, c--)
            {
                dst.setElem(i, dst.getElem(i) & 0xffffff | ~src.getElem(i) << 24);
            }
        }
        else if (matte == null)
        {
            final int[] samples = new int[width];
            for (int y = 0; y < height; y++)
            {
                alpha.getSamples(0, y, width, 1, 0, samples);
                if (!isSoft)
                {
                    for (int x = 0; x < width; x++)
                    {
                        samples[x] ^= -1;
                    }
                }
                raster.setSamples(0, y, width, 1, 3, samples);
            }
        }
        else
        {
            final int[] alphas = new int[width];
            final int[] pixels = new int[4 * width];
            // Original code is to clamp component and alpha to [0f, 1f] as matte is,
            // and later expand to [0; 255] again (with rounding).
            // component = 255f * ((component / 255f - matte) / (alpha / 255f) + matte)
            //           = (255 * component - 255 * 255f * matte) / alpha + 255f * matte
            // There is a clearly visible factor 255 for most components in above formula,
            // i.e. max value is 255 * 255: 16 bits + sign.
            // Let's use faster fixed point integer arithmetics with Q16.15,
            // introducing neglible errors (0.001%).
            // Note: For "correct" rounding we increase the final matte value (m0h, m1h, m2h) by
            // a half an integer.
            final int fraction = 15;
            final int factor = 255 << fraction;
            final int m0 = Math.round(factor * matte[0]) * 255;
            final int m1 = Math.round(factor * matte[1]) * 255;
            final int m2 = Math.round(factor * matte[2]) * 255;
            final int m0h = m0 / 255 + (1 << fraction - 1);
            final int m1h = m1 / 255 + (1 << fraction - 1);
            final int m2h = m2 / 255 + (1 << fraction - 1);
            for (int y = 0; y < height; y++)
            {
                raster.getPixels(0, y, width, 1, pixels);
                alpha.getSamples(0, y, width, 1, 0, alphas);
                int offset = 0;
                for (int x = 0; x < width; x++)
                {
                    int a = alphas[x];
                    if (a == 0)
                    {
                        offset += 3;
                    }
                    else
                    {
                        pixels[offset] = clampColor(((pixels[offset++] * factor - m0) / a + m0h) >> fraction);
                        pixels[offset] = clampColor(((pixels[offset++] * factor - m1) / a + m1h) >> fraction);
                        pixels[offset] = clampColor(((pixels[offset++] * factor - m2) / a + m2h) >> fraction);
                    }
                    pixels[offset++] = a;
                }
                raster.setPixels(0, y, width, 1, pixels);
            }
        }

        return image;
    }

    private static int clampColor(int color)
    {
        return color < 0 ? 0 : color > 255 ? 255 : color;
    }

    private void initJPXValues()
    {
        if (!hasJPXFilter || jpxValuesInitialized)
        {
            return;
        }
        // some of the dictionary values of the COSStream may be overwritten by values which are extracted from the
        // image itself, such as
        // width and height of the image
        // bits per component
        // the colorspace of the image is used if the dictionary doesn't provide any value
        PDStream stream = getStream();
        try (COSInputStream is = stream.createInputStream())
        {
            DecodeResult decodeResult = is.getDecodeResult();
            stream.getCOSObject().addAll(decodeResult.getParameters());
            if (colorSpace == null)
            {
                colorSpace = decodeResult.getJPXColorSpace();
            }
            jpxValuesInitialized = true;
        }
        catch (IOException exception)
        {
            LOG.debug("Can't initialize JPX based values", exception);
        }
    }

    /**
     * High-quality image scaling.
     */
    private static BufferedImage scaleImage(BufferedImage image, int width, int height, int type, boolean interpolate)
    {
        final int imgWidth = image.getWidth();
        final int imgHeight = image.getHeight();
        // largeScale switch is arbitrarily chosen as to where bicubic becomes very slow
        boolean largeScale = width * height > 3000 * 3000 * (type == BufferedImage.TYPE_BYTE_GRAY ? 3 : 1);
        interpolate &= imgWidth != width || imgHeight != height;

        BufferedImage image2 = new BufferedImage(width, height, type);
        if (interpolate)
        {
            AffineTransform af = AffineTransform.getScaleInstance((double) width / imgWidth, (double) height / imgHeight);
            AffineTransformOp afo = new AffineTransformOp(af, largeScale ? AffineTransformOp.TYPE_BILINEAR : AffineTransformOp.TYPE_BICUBIC);
            try
            {
                afo.filter(image, image2);
                return image2;
            }
            catch (ImagingOpException e)
            {
                LOG.warn(e.getMessage(), e);
            }
        }
        Graphics2D g = image2.createGraphics();
        if (interpolate)
        {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    largeScale ? RenderingHints.VALUE_INTERPOLATION_BILINEAR : RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,
                    largeScale ? RenderingHints.VALUE_RENDER_DEFAULT : RenderingHints.VALUE_RENDER_QUALITY);
        }
        g.drawImage(image, 0, 0, width, height, 0, 0, imgWidth, imgHeight, null);
        g.dispose();
        return image2;
    }

    /**
     * Returns the Mask Image XObject associated with this image, or null if there is none.
     * 
     * @return Mask Image XObject
     * @throws java.io.IOException if the mask data could not be read
     */
    public PDImageXObject getMask() throws IOException
    {
        COSArray mask = getCOSObject().getCOSArray(COSName.MASK);
        if (mask != null)
        {
            // color key mask, no explicit mask to return
            return null;
        }
        else
        {
            COSStream cosStream = getCOSObject().getCOSStream(COSName.MASK);
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
        return getCOSObject().getCOSArray(COSName.MASK);
    }

    /**
     * Returns the Soft Mask Image XObject associated with this image, or null if there is none.
     * 
     * @return the SMask Image XObject, or null.
     * @throws java.io.IOException if the soft mask data could not be read
     */
    public PDImageXObject getSoftMask() throws IOException
    {
        COSStream cosStream = getCOSObject().getCOSStream(COSName.SMASK);
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
            initJPXValues();
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
            COSBase cosBase = getCOSObject().getItem(COSName.COLORSPACE, COSName.CS);
            if (cosBase != null)
            {
                COSObject indirect = null;
                if (cosBase instanceof COSObject &&
                        resources != null && resources.getResourceCache() != null)
                {
                    // PDFBOX-4022: use the resource cache because several images
                    // might have the same colorspace indirect object.
                    indirect = (COSObject) cosBase;
                    colorSpace = resources.getResourceCache().getColorSpace(indirect);
                    if (colorSpace != null)
                    {
                        return colorSpace;
                    }
                }
                colorSpace = PDColorSpace.create(cosBase, resources);
                if (indirect != null)
                {
                    resources.getResourceCache().put(indirect, colorSpace);
                }
            }
            else if (isStencil())
            {
                // stencil mask color space must be gray, it is often missing
                colorSpace = PDDeviceGray.INSTANCE;
            }
            else
            {
                initJPXValues();
            }
            if (colorSpace == null)
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
    public InputStream createInputStream(DecodeOptions options) throws IOException
    {
        return getStream().createInputStream(options);
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
        colorSpace = null;
        cachedImage = null;
    }

    @Override
    public int getHeight()
    {
        initJPXValues();
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
        initJPXValues();
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
        return getCOSObject().getCOSArray(COSName.DECODE);
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

        if (filters.isEmpty())
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
        else if (filters.contains(COSName.JBIG2_DECODE))
        {
            return "jb2";
        }
        else
        {
            LOG.warn("getSuffix() returns null, filters: " + filters);
            return null;
        }
    }

    /**
     * This will get the optional content group or optional content membership dictionary.
     *
     * @return The optional content group or optional content membership dictionary or null if there
     * is none.
     */
    public PDPropertyList getOptionalContent()
    {
        COSDictionary optionalContent = getCOSObject().getCOSDictionary(COSName.OC);
        return optionalContent != null ? PDPropertyList.create(optionalContent) : null;
    }

    /**
     * Sets the optional content group or optional content membership dictionary.
     *
     * @param oc The optional content group or optional content membership dictionary.
     */
    public void setOptionalContent(PDPropertyList oc)
    {
        getCOSObject().setItem(COSName.OC, oc);
    }
}
