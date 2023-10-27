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
package org.apache.pdfbox.tools.imageio;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles some ImageIO operations.
 */
public final class ImageIOUtil
{
    /**
     * Log instance
     */
    private static final Logger LOG = LogManager.getLogger(ImageIOUtil.class);

    private ImageIOUtil()
    {
    }

    /**
     * Writes a buffered image to a file using the given image format. The compression is set for
     * maximum compression for PNG and maximum quality for all other file formats. See
     * {@link #writeImage(BufferedImage image, String formatName, OutputStream output, int dpi, float compressionQuality)}
     * for more details.
     *
     * @param image the image to be written
     * @param filename used to construct the filename for the individual image.
     * Its suffix will be used as the image format.
     * @param dpi the resolution in dpi (dots per inch) to be used in metadata
     * @return true if the image file was produced, false if there was an error.
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String filename,
            int dpi) throws IOException
    {
        float compressionQuality = 1f;
        String formatName = filename.substring(filename.lastIndexOf('.') + 1);
        if ("png".equalsIgnoreCase(formatName))
        {
            // PDFBOX-4655: prevent huge PNG files on jdk11 / jdk12 / jdk13
            compressionQuality = 0f;
        }
        return writeImage(image, filename, dpi, compressionQuality);
    }

    /**
     * Writes a buffered image to a file using the given image format.
     * See {@link #writeImage(BufferedImage image, String formatName,
     * OutputStream output, int dpi, float compressionQuality)} for more details.
     *
     * @param image the image to be written
     * @param filename used to construct the filename for the individual image. Its suffix will be
     * used as the image format.
     * @param dpi the resolution in dpi (dots per inch) to be used in metadata
     * @param compressionQuality quality to be used when compressing the image (0 &lt;
     * compressionQuality &lt; 1.0f). See {@link ImageWriteParam#setCompressionQuality(float)} for
     * more details.
     * @return true if the image file was produced, false if there was an error.
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String filename,
            int dpi, float compressionQuality) throws IOException
    {
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(filename)))
        {
            String formatName = filename.substring(filename.lastIndexOf('.') + 1);
            return writeImage(image, formatName, output, dpi, compressionQuality);
        }
    }

    /**
     * Writes a buffered image to a file using the given image format. The compression is set for
     * maximum compression for PNG and maximum quality for all other file formats. See
     * {@link #writeImage(BufferedImage image, String formatName, OutputStream output, int dpi, float compressionQuality)}
     * for more details.
     *
     * @param image the image to be written
     * @param formatName the target format (ex. "png")
     * @param output the output stream to be used for writing
     * @return true if the image file was produced, false if there was an error.
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String formatName, OutputStream output)
            throws IOException
    {
        return writeImage(image, formatName, output, 72);
    }

    /**
     * Writes a buffered image to a file using the given image format. The compression is set for
     * maximum compression for PNG and maximum quality for all other file formats. See
     * {@link #writeImage(BufferedImage image, String formatName, OutputStream output, int dpi, float compressionQuality)}
     * for more details.
     *
     * @param image the image to be written
     * @param formatName the target format (ex. "png")
     * @param output the output stream to be used for writing
     * @param dpi the resolution in dpi (dots per inch) to be used in metadata
     * @return true if the image file was produced, false if there was an error.
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String formatName, OutputStream output,
            int dpi) throws IOException
    {
        float compressionQuality = 1f;
        if ("png".equalsIgnoreCase(formatName))
        {
            // PDFBOX-4655: prevent huge PNG files on jdk11 / jdk12 / jdk13
            compressionQuality = 0f;
        }
        return writeImage(image, formatName, output, dpi, compressionQuality);
    }

    /**
     * Writes a buffered image to a file using the given image format.
     * Compression is fixed for PNG, GIF, BMP and WBMP, dependent of the compressionQuality
     * parameter for JPG, and dependent of bit count for TIFF (a bitonal image
     * will be compressed with CCITT G4, a color image with LZW). Creating a
     * TIFF image is only supported if the jai_imageio library (or equivalent)
     * is in the class path.
     *
     * @param image the image to be written
     * @param formatName the target format (ex. "png")
     * @param output the output stream to be used for writing
     * @param dpi the resolution in dpi (dots per inch) to be used in metadata
     * @param compressionQuality quality to be used when compressing the image (0 &lt;
     * compressionQuality &lt; 1.0f). See {@link ImageWriteParam#setCompressionQuality(float)} for
     * more details.
     * @return true if the image file was produced, false if there was an error.
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String formatName, OutputStream output,
            int dpi, float compressionQuality) throws IOException
    {
        return writeImage(image, formatName, output, dpi, compressionQuality, "");
    }

    /**
     * Writes a buffered image to a file using the given image format.
     * Compression is fixed for PNG, GIF, BMP and WBMP, dependent of the compressionQuality
     * parameter for JPG, and dependent of bit count for TIFF (a bitonal image
     * will be compressed with CCITT G4, a color image with LZW). Creating a
     * TIFF image is only supported if the jai_imageio library is in the class
     * path.
     *
     * @param image the image to be written
     * @param formatName the target format (ex. "png")
     * @param output the output stream to be used for writing
     * @param dpi the resolution in dpi (dots per inch) to be used in metadata
     * @param compressionQuality quality to be used when compressing the image (0 &lt;
     * compressionQuality &lt; 1.0f). See {@link ImageWriteParam#setCompressionQuality(float)} for
     * more details.
     * @param compressionType Advanced users only, and only relevant for TIFF
     * files: If null, save uncompressed; if empty string, use logic explained
     * above; other valid values are found in the javadoc of
     * <a href="https://download.java.net/media/jai-imageio/javadoc/1.1/com/sun/media/imageio/plugins/tiff/TIFFImageWriteParam.html">TIFFImageWriteParam</a>.
     * @return true if the image file was produced, false if there was an error.
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String formatName, OutputStream output,
            int dpi, float compressionQuality, String compressionType) throws IOException
    {
        ImageOutputStream imageOutput = null;
        ImageWriter writer = null;
        try
        {
            // find suitable image writer
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
            ImageWriteParam param = null;
            IIOMetadata metadata = null;
            // Loop until we get the best driver, i.e. one that supports
            // setting dpi in the standard metadata format; however we'd also 
            // accept a driver that can't, if a better one can't be found
            while (writers.hasNext())
            {
                if (writer != null)
                {
                    writer.dispose();
                }
                writer = writers.next();
                if (writer != null)
                {
                    param = writer.getDefaultWriteParam();
                    metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                    if (metadata != null
                            && !metadata.isReadOnly()
                            && metadata.isStandardMetadataFormatSupported())
                    {
                        break;
                    }
                }
            }
            if (writer == null)
            {
                LOG.error("No ImageWriter found for '{}' format", formatName);
                LOG.error("Supported formats: {}", Arrays.toString(ImageIO.getWriterFormatNames()));
                return false;
            }
            
            boolean isTifFormat = formatName.toLowerCase().startsWith("tif");

            // compression
            if (param != null && param.canWriteCompressed())
            {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (isTifFormat)
                {
                    if ("".equals(compressionType))
                    {
                        // default logic
                        TIFFUtil.setCompressionType(param, image);
                    }
                    else
                    {
                        param.setCompressionType(compressionType);
                        if (compressionType != null)
                        {
                            param.setCompressionQuality(compressionQuality);
                        }
                    }
                }
                else
                {
                    param.setCompressionType(param.getCompressionTypes()[0]);
                    param.setCompressionQuality(compressionQuality);
                }
            }

            if (metadata != null)
            {
                if (isTifFormat)
                {
                    // TIFF metadata
                    TIFFUtil.updateMetadata(metadata, image, dpi);
                }
                else if ("jpeg".equalsIgnoreCase(formatName) || "jpg".equalsIgnoreCase(formatName))
                {
                    // This segment must be run before other meta operations,
                    // or else "IIOInvalidTreeException: Invalid node: app0JFIF"
                    // The other (general) "meta" methods may not be used, because
                    // this will break the reading of the meta data in tests
                    JPEGUtil.updateMetadata(metadata, dpi);
                }
                else
                {
                    // write metadata is possible
                    if (!metadata.isReadOnly() && metadata.isStandardMetadataFormatSupported())
                    {
                        setDPI(metadata, dpi, formatName);
                    }
                }
            }

            if (metadata != null && formatName.equalsIgnoreCase("png") && hasICCProfile(image))
            {
                // add ICC profile
                IIOMetadataNode iccp = new IIOMetadataNode("iCCP");
                ICC_Profile profile = ((ICC_ColorSpace) image.getColorModel().getColorSpace())
                        .getProfile();
                iccp.setUserObject(getAsDeflatedBytes(profile));
                iccp.setAttribute("profileName", "unknown");
                iccp.setAttribute("compressionMethod", "deflate");
                Node nativeTree = metadata.getAsTree(metadata.getNativeMetadataFormatName());
                nativeTree.appendChild(iccp);
                metadata.mergeTree(metadata.getNativeMetadataFormatName(), nativeTree);
            }

            // write
            imageOutput = ImageIO.createImageOutputStream(output);
            writer.setOutput(imageOutput);
            writer.write(null, new IIOImage(image, null, metadata), param);
        }
        finally
        {
            if (writer != null)
            {
                writer.dispose();
            }
            if (imageOutput != null)
            {
                imageOutput.close();
            }
        }
        return true;
    }

    /**
     * Determine if the given image has a ICC profile that should be embedded.
     * @param image the image to analyse
     * @return true if this image has an ICC profile, that is different from sRGB.
     */
    private static boolean hasICCProfile(BufferedImage image)
    {
        ColorSpace colorSpace = image.getColorModel().getColorSpace();
        // We can only export ICC color spaces
        if (!(colorSpace instanceof ICC_ColorSpace))
        {
            return false;
        }

        // The colorspace should not be sRGB and not be the builtin gray colorspace
        return !colorSpace.isCS_sRGB() && colorSpace != ColorSpace.getInstance(ColorSpace.CS_GRAY);
    }

    private static byte[] getAsDeflatedBytes(ICC_Profile profile) throws IOException
    {
        byte[] data = profile.getData();

        ByteArrayOutputStream deflated = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(deflated))
        {
            deflater.write(data);
        }

        return deflated.toByteArray();
    }

    /**
     * Gets the named child node, or creates and attaches it.
     *
     * @param parentNode the parent node
     * @param name name of the child node
     *
     * @return the existing or just created child node
     */
    private static IIOMetadataNode getOrCreateChildNode(IIOMetadataNode parentNode, String name)
    {
        NodeList nodeList = parentNode.getElementsByTagName(name);
        if (nodeList.getLength() > 0)
        {
            return (IIOMetadataNode) nodeList.item(0);
        }
        IIOMetadataNode childNode = new IIOMetadataNode(name);
        parentNode.appendChild(childNode);
        return childNode;
    }

    // sets the DPI metadata
    private static void setDPI(IIOMetadata metadata, int dpi, String formatName)
            throws IIOInvalidTreeException
    {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(MetaUtil.STANDARD_METADATA_FORMAT);

        IIOMetadataNode dimension = getOrCreateChildNode(root, "Dimension");

        // PNG writer doesn't conform to the spec which is
        // "The width of a pixel, in millimeters"
        // but instead counts the pixels per millimeter
        float res = "PNG".equalsIgnoreCase(formatName)
                    ? dpi / 25.4f
                    : 25.4f / dpi;

        IIOMetadataNode child;

        child = getOrCreateChildNode(dimension, "HorizontalPixelSize");
        child.setAttribute("value", Double.toString(res));

        child = getOrCreateChildNode(dimension, "VerticalPixelSize");
        child.setAttribute("value", Double.toString(res));

        metadata.mergeTree(MetaUtil.STANDARD_METADATA_FORMAT, root);
    }
}
