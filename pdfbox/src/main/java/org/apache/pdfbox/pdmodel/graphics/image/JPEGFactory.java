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

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.filter.Filter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.w3c.dom.Element;

/**
 * Factory for creating a PDImageXObject containing a JPEG compressed image.
 * @author John Hewson
 */
public final class JPEGFactory
{
    private static final Log LOG = LogFactory.getLog(JPEGFactory.class);

    private JPEGFactory()
    {
    }

    /**
     * Creates a new JPEG Image XObject from an input stream containing JPEG data.
     * 
     * The input stream data will be preserved and embedded in the PDF file without modification.
     * @param document the document where the image will be created
     * @param stream a stream of JPEG data
     * @return a new Image XObject
     * 
     * @throws IOException if the input stream cannot be read
     */
    public static PDImageXObject createFromStream(PDDocument document, InputStream stream)
            throws IOException
    {
        return createFromByteArray(document, stream.readAllBytes());
    }

    /**
     * Creates a new JPEG Image XObject from a byte array containing JPEG data.
     *
     * @param document the document where the image will be created
     * @param byteArray bytes of JPEG image
     * @return a new Image XObject
     *
     * @throws IOException if the input stream cannot be read
     */
    public static PDImageXObject createFromByteArray(PDDocument document, byte[] byteArray)
            throws IOException
    {
        // copy stream
        ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);

        Dimensions meta = retrieveDimensions(byteStream);

        PDColorSpace colorSpace;
        switch (meta.numComponents)
        {
            case 1:
                colorSpace = PDDeviceGray.INSTANCE;
                break;
            case 3:
                colorSpace = PDDeviceRGB.INSTANCE;
                break;
            case 4:
                colorSpace = PDDeviceCMYK.INSTANCE;
                break;
            default:
                throw new UnsupportedOperationException("number of data elements not supported: " +
                        meta.numComponents);
        }

        // create PDImageXObject from stream
        PDImageXObject pdImage = new PDImageXObject(document, byteStream, 
                COSName.DCT_DECODE, meta.width, meta.height, 8, colorSpace);

        if (colorSpace instanceof PDDeviceCMYK)
        {
            COSArray decode = new COSArray();
            decode.add(COSInteger.ONE);
            decode.add(COSInteger.ZERO);
            decode.add(COSInteger.ONE);
            decode.add(COSInteger.ZERO);
            decode.add(COSInteger.ONE);
            decode.add(COSInteger.ZERO);
            decode.add(COSInteger.ONE);
            decode.add(COSInteger.ZERO);
            pdImage.setDecode(decode);
        }

        return pdImage;
    }

    private static class Dimensions
    {
        private int width;
        private int height;
        private int numComponents;
    }

    private static Dimensions retrieveDimensions(ByteArrayInputStream stream) throws IOException
    {
        ImageReader reader =
                Filter.findImageReader("JPEG", "a suitable JAI I/O image filter is not installed");
        try (ImageInputStream iis = ImageIO.createImageInputStream(stream))
        {
            reader.setInput(iis);

            Dimensions meta = new Dimensions();
            meta.width = reader.getWidth(0);
            meta.height = reader.getHeight(0);
            // PDFBOX-4691: get from image metadata (faster because no decoding)
            try
            {
                meta.numComponents = getNumComponentsFromImageMetadata(reader);
                if (meta.numComponents != 0)
                {
                    return meta;
                }
                LOG.warn("No image metadata, will decode image and use raster size");
            }
            catch (IOException ex)
            {
                LOG.warn("Error reading image metadata, will decode image and use raster size", ex);
            }            

            // Old method: get from raster (slower)
            ImageIO.setUseCache(false);
            Raster raster = reader.readRaster(0, null);
            meta.numComponents = raster.getNumDataElements();
            return meta;
        }
        finally
        {
            stream.reset();
            reader.dispose();
        }
    }

    private static int getNumComponentsFromImageMetadata(ImageReader reader) throws IOException
    {
        IIOMetadata imageMetadata = reader.getImageMetadata(0);
        if (imageMetadata == null)
        {
            return 0;
        }
        Element root = (Element) imageMetadata.getAsTree("javax_imageio_jpeg_image_1.0");
        if (root == null)
        {
            return 0;
        }

        try
        {
            XPath xpath = XPathFactory.newInstance().newXPath();
            String numFrameComponents = xpath.evaluate("markerSequence/sof/@numFrameComponents", root);
            if (numFrameComponents.isEmpty())
            {
                return 0;
            }
            return Integer.parseInt(numFrameComponents);
        }
        catch (NumberFormatException | XPathExpressionException ex)
        {
            LOG.warn(ex.getMessage(), ex);
            return 0;
        }
    }

    /**
     * Creates a new JPEG PDImageXObject from a BufferedImage.
     * <p>
     * Do not read a JPEG image from a stream/file and call this method; you'll get more speed and
     * quality by calling {@link #createFromStream(org.apache.pdfbox.pdmodel.PDDocument,
     * java.io.InputStream) createFromStream()} instead.
     *
     * @param document the document where the image will be created
     * @param image the BufferedImage to embed
     * @return a new Image XObject
     * @throws IOException if the JPEG data cannot be written
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image)
        throws IOException
    {
        return createFromImage(document, image, 0.75f);
    }

    /**
     * Creates a new JPEG PDImageXObject from a BufferedImage and a given quality.
     * <p>
     * Do not read a JPEG image from a stream/file and call this method; you'll get more speed and
     * quality by calling {@link #createFromStream(org.apache.pdfbox.pdmodel.PDDocument,
     * java.io.InputStream) createFromStream()} instead.
     * 
     * The image will be created with a dpi value of 72 to be stored in metadata.
     * @param document the document where the image will be created
     * @param image the BufferedImage to embed
     * @param quality The desired JPEG compression quality; between 0 (best
     * compression) and 1 (best image quality). See
     * {@link ImageWriteParam#setCompressionQuality(float)} for more details.
     * @return a new Image XObject
     * @throws IOException if the JPEG data cannot be written
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image,
                                                 float quality) throws IOException
    {
        return createFromImage(document, image, quality, 72);
    }

    /**
     * Creates a new JPEG Image XObject from a BufferedImage, a given quality and dpi metadata.
     * <p>
     * Do not read a JPEG image from a stream/file and call this method; you'll get more speed and
     * quality by calling {@link #createFromStream(org.apache.pdfbox.pdmodel.PDDocument,
     * java.io.InputStream) createFromStream()} instead.
     * 
     * @param document the document where the image will be created
     * @param image the BufferedImage to embed
     * @param quality The desired JPEG compression quality; between 0 (best
     * compression) and 1 (best image quality). See
     * {@link ImageWriteParam#setCompressionQuality(float)} for more details.
     * @param dpi the desired dpi (resolution) value of the JPEG to be stored in metadata. This
     * value has no influence on image content or size.
     * @return a new Image XObject
     * @throws IOException if the JPEG data cannot be written
     */
    public static PDImageXObject createFromImage(PDDocument document, BufferedImage image,
                                                 float quality, int dpi) throws IOException
    {
        return createJPEG(document, image, quality, dpi);
    }
    
    // returns the alpha channel of an image
    private static BufferedImage getAlphaImage(BufferedImage image)
    {
        if (!image.getColorModel().hasAlpha())
        {
            return null;
        }
        if (image.getTransparency() == Transparency.BITMASK)
        {
            throw new UnsupportedOperationException("BITMASK Transparency JPEG compression is not" +
                    " useful, use LosslessImageFactory instead");
        }
        WritableRaster alphaRaster = image.getAlphaRaster();
        if (alphaRaster == null)
        {
            // happens sometimes (PDFBOX-2654) despite colormodel claiming to have alpha
            return null;
        }
        BufferedImage alphaImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        alphaImage.setData(alphaRaster);
        return alphaImage;
    }
    
    // Creates an Image XObject from a BufferedImage using JAI Image I/O
    private static PDImageXObject createJPEG(PDDocument document, BufferedImage image,
                                             float quality, int dpi) throws IOException
    {
        BufferedImage awtColorImage = getColorImage(image);

        // create XObject
        byte[] encoded = encodeImageToJPEGStream(awtColorImage, quality, dpi);
        ByteArrayInputStream encodedByteStream = new ByteArrayInputStream(encoded);

        PDImageXObject pdImage = new PDImageXObject(document, encodedByteStream, COSName.DCT_DECODE,
                awtColorImage.getWidth(), awtColorImage.getHeight(), 8,
                getColorSpaceFromAWT(awtColorImage));

        // extract alpha channel (if any)
        BufferedImage awtAlphaImage = getAlphaImage(image);
        if (awtAlphaImage != null)
        {
            // alpha -> soft mask
            PDImage xAlpha = JPEGFactory.createFromImage(document, awtAlphaImage, quality);
            pdImage.getCOSObject().setItem(COSName.SMASK, xAlpha);
        }

        return pdImage;
    }

    // never returns null
    private static ImageWriter getJPEGImageWriter() throws IOException
    {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
        while (writers.hasNext())
        {
            ImageWriter writer = writers.next();
            if (writer == null)
            {
                continue;
            }
            // PDFBOX-3566: avoid CLibJPEGImageWriter, which is not a JPEGImageWriteParam
            if (writer.getDefaultWriteParam() instanceof JPEGImageWriteParam)
            {
                return writer;
            }
            writer.dispose();
        }
        throw new IOException("No ImageWriter found for JPEG format");
    }

    private static byte[] encodeImageToJPEGStream(BufferedImage image, float quality, int dpi)
            throws IOException
    {
        ImageWriter imageWriter = getJPEGImageWriter(); // find JAI writer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos))
        {
            imageWriter.setOutput(ios);

            // add compression
            ImageWriteParam jpegParam = imageWriter.getDefaultWriteParam();
            jpegParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParam.setCompressionQuality(quality);

            // add metadata
            ImageTypeSpecifier imageTypeSpecifier = new ImageTypeSpecifier(image);
            IIOMetadata data = imageWriter.getDefaultImageMetadata(imageTypeSpecifier, jpegParam);
            Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            String dpiString = Integer.toString(dpi);
            jfif.setAttribute("Xdensity", dpiString);
            jfif.setAttribute("Ydensity", dpiString);
            jfif.setAttribute("resUnits", "1"); // 1 = dots/inch

            // write
            imageWriter.write(data, new IIOImage(image, null, null), jpegParam);

            return baos.toByteArray();
        }
        finally
        {
            imageWriter.dispose();
        }
    }
    
    // returns a PDColorSpace for a given BufferedImage
    private static PDColorSpace getColorSpaceFromAWT(BufferedImage awtImage)
    {
        if (awtImage.getColorModel().getNumComponents() == 1)
        {
            // 256 color (gray) JPEG
            return PDDeviceGray.INSTANCE;
        }
        
        ColorSpace awtColorSpace = awtImage.getColorModel().getColorSpace();
        if (awtColorSpace instanceof ICC_ColorSpace && !awtColorSpace.isCS_sRGB())
        {
            throw new UnsupportedOperationException("ICC color spaces not implemented");
        }
        
        switch (awtColorSpace.getType())
        {
            case ColorSpace.TYPE_RGB:
                return PDDeviceRGB.INSTANCE;
            case ColorSpace.TYPE_GRAY:
                return PDDeviceGray.INSTANCE;
            case ColorSpace.TYPE_CMYK:
                return PDDeviceCMYK.INSTANCE;
            default:
                throw new UnsupportedOperationException("color space not implemented: "
                        + awtColorSpace.getType());
        }
    }

    // returns the color channels of an image
    private static BufferedImage getColorImage(BufferedImage image)
    {
        if (!image.getColorModel().hasAlpha())
        {
            return image;
        }

        if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
        {
            throw new UnsupportedOperationException("only RGB color spaces are implemented");
        }

        // create an RGB image without alpha
        //BEWARE: the previous solution in the history 
        // g.setComposite(AlphaComposite.Src) and g.drawImage()
        // didn't work properly for TYPE_4BYTE_ABGR.
        // alpha values of 0 result in a black dest pixel!!!
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        return new ColorConvertOp(null).filter(image, rgbImage);
    }
}
