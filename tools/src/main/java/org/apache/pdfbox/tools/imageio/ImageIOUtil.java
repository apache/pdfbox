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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

/**
 * Handles some ImageIO operations.
 */
public final class ImageIOUtil
{
    /**
     * Log instance
     */
    private static final Log LOG = LogFactory.getLog(ImageIOUtil.class);

    private ImageIOUtil()
    {
    }

    /**
     * Writes a buffered image to a file using the given image format. See     
     * {@link #writeImage(BufferedImage image, String formatName, 
     * OutputStream output, int dpi, float compressionQuality)} for more details.
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
        return writeImage(image, filename, dpi, 1.0f);
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
     * compressionQuality &lt; 1.0f)
     * @return true if the image file was produced, false if there was an error.
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String filename,
            int dpi, float compressionQuality) throws IOException
    {
        File file = new File(filename);
        FileOutputStream output = new FileOutputStream(file);
        try
        {
            String formatName = filename.substring(filename.lastIndexOf('.') + 1);
            return writeImage(image, formatName, output, dpi, compressionQuality);
        }
        finally
        {
            output.close();
        }
    }

    /**
     * Writes a buffered image to a file using the given image format. See      
     * {@link #writeImage(BufferedImage image, String formatName, 
     * OutputStream output, int dpi, float compressionQuality)} for more details.
     *
     * @param image the image to be written
     * @param formatName the target format (ex. "png") which is also the suffix
     * for the filename
     * @param filename used to construct the filename for the individual image.
     * The formatName parameter will be used as the suffix.
     * @param dpi the resolution in dpi (dots per inch) to be used in metadata
     * @return true if the image file was produced, false if there was an error.
     * @throws IOException if an I/O error occurs
     * @deprecated use
     * {@link #writeImage(BufferedImage image, String filename, int dpi)}, which
     * uses the full filename instead of just the prefix.
     */
    @Deprecated
    public static boolean writeImage(BufferedImage image, String formatName, String filename,
            int dpi) throws IOException
    {
        File file = new File(filename + "." + formatName);
        FileOutputStream output = new FileOutputStream(file);
        try
        {
            return writeImage(image, formatName, output, dpi);
        }
        finally
        {
            output.close();
        }
    }

    /**
     * Writes a buffered image to a file using the given image format. See      
     * {@link #writeImage(BufferedImage image, String formatName, 
     * OutputStream output, int dpi, float compressionQuality)} for more details.
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
     * Writes a buffered image to a file using the given image format. See      
     * {@link #writeImage(BufferedImage image, String formatName, 
     * OutputStream output, int dpi, float compressionQuality)} for more details.
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
        return writeImage(image, formatName, output, dpi, 1.0f);
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
     * compressionQuality &lt; 1.0f)
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
     * @param compressionQuality quality to be used when compressing the image
     * (0 &lt; compressionQuality &lt; 1.0f)
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
                if (writer == null)
                {
                    continue;
                }
                param = writer.getDefaultWriteParam();
                metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                if (metadata != null
                        && !metadata.isReadOnly()
                        && metadata.isStandardMetadataFormatSupported())
                {
                    break;
                }
            }
            if (writer == null)
            {
                LOG.error("No ImageWriter found for '" + formatName + "' format");
                StringBuilder sb = new StringBuilder();
                String[] writerFormatNames = ImageIO.getWriterFormatNames();
                for (String fmt : writerFormatNames)
                {
                    sb.append(fmt);
                    sb.append(' ');
                }
                LOG.error("Supported formats: " + sb);
                return false;
            }

            // compression
            if (param != null && param.canWriteCompressed())
            {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (formatName.toLowerCase().startsWith("tif"))
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

            if (formatName.toLowerCase().startsWith("tif"))
            {
                // TIFF metadata
                TIFFUtil.updateMetadata(metadata, image, dpi);
            }
            else if ("jpeg".equalsIgnoreCase(formatName)
                    || "jpg".equalsIgnoreCase(formatName))
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
                if (metadata != null
                        && !metadata.isReadOnly()
                        && metadata.isStandardMetadataFormatSupported())
                {
                    setDPI(metadata, dpi, formatName);
                }
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
