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
package org.apache.pdfbox.util;

import java.awt.image.BufferedImage;
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
import org.w3c.dom.Element;

/**
 * Handles some ImageIO operations.
 */
public class ImageIOUtil
{
    private ImageIOUtil()
    {
    }

    /**
     * Writes a buffered image to a file using the given image format.
     * @param image the image to be written
     * @param formatName the target format (ex. "png")
     * @param filename used to construct the filename for the individual images
     * @param dpi the resolution in dpi (dots per inch)
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String formatName, String filename,
                                     int dpi) throws IOException
    {
        OutputStream output = new FileOutputStream(filename + "." + formatName);
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
     * Writes a buffered image to a file using the given image format.
     * @param image the image to be written
     * @param formatName the target format (ex. "png")
     * @param output the output stream to be used for writing
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String formatName, OutputStream output)
            throws IOException
    {
        return writeImage(image, formatName, output, 72);
    }

    /**
     * Writes a buffered image to a file using the given image format.
     * @param image the image to be written
     * @param formatName the target format (ex. "png")
     * @param output the output stream to be used for writing
     * @param dpi resolution to be used when writing the image
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String formatName, OutputStream output,
                                     int dpi) throws IOException
    {
        return writeImage(image, formatName, output, dpi, 1.0f);
    }
    
    /**
     * Writes a buffered image to a file using the given image format.
     * @param image the image to be written
     * @param formatName the target format (ex. "png")
     * @param output the output stream to be used for writing
     * @param dpi resolution to be used when writing the image
     * @param quality quality to be used when compressing the image (0 &lt; quality &lt; 1.0f)
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String formatName, OutputStream output,
                                     int dpi, float quality) throws IOException
    {
        ImageOutputStream imageOutput = null;
        ImageWriter writer = null;
        try
        {
            // find suitable image writer
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
            ImageWriteParam param = null;
            IIOMetadata metadata = null;
            while (writers.hasNext())
            {
                writer = writers.next();
                param = writer.getDefaultWriteParam();
                metadata = writer.getDefaultImageMetadata(new ImageTypeSpecifier(image), param);
                if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported())
                {
                    writer = null;
                }
            }

            if (writer == null)
            {
                return false;
            }

            // compression
            if (param.canWriteCompressed())
            {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (formatName.toLowerCase().startsWith("tif"))
                {
                    // TIFF compression
                    TIFFUtil.setCompressionType(param, image);
                }
                else
                {
                    // JPEG, PNG compression
                    param.setCompressionType(param.getCompressionTypes()[0]);
                    param.setCompressionQuality(quality);
                }
            }

            // metadata
            setDPI(metadata, dpi);

            // TIFF metadata
            if (formatName.toLowerCase().startsWith("tif"))
            {
                TIFFUtil.updateMetadata(metadata, image, dpi);
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
    
    // sets the DPI metadata
    private static void setDPI(IIOMetadata metadata, int dpi)
    {
        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        Element dimension = new IIOMetadataNode("Dimension");


        Element h = new IIOMetadataNode("HorizontalPixelSize");
        h.setAttribute("value", Double.toString(dpi / 25.4));
        dimension.appendChild(h);

        Element v = new IIOMetadataNode("VerticalPixelSize");
        v.setAttribute("value", Double.toString(dpi / 25.4));
        dimension.appendChild(v);

        root.appendChild(dimension);

        try
        {
            metadata.mergeTree("javax_imageio_1.0", root);
        }
        catch (IIOInvalidTreeException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }
    }
}
