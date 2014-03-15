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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import static org.apache.pdfbox.util.MetaUtil.SUN_TIFF_FORMAT;
import static org.apache.pdfbox.util.MetaUtil.debugLogMetadata;

/**
 * Used by ImageIOUtil to write TIFF files.
 * @author Tilman Hausherr
 */
class TIFFUtil
{
    private static final Log LOG = LogFactory.getLog(TIFFUtil.class);

    /**
     * Sets the ImageIO parameter compression type based on the given image.
     * @param image buffered image used to decide compression type
     * @param param ImageIO write parameter to update
     */
    public static void setCompressionType(ImageWriteParam param, BufferedImage image)
    {
        // avoid error: first compression type is RLE, not optimal and incorrect for color images
        // TODO expose this choice to the user?
        if (image.getType() == BufferedImage.TYPE_BYTE_BINARY &&
            image.getColorModel().getPixelSize() == 1)
        {
            param.setCompressionType("CCITT T.6");
        }
        else
        {
            param.setCompressionType("LZW");
        }
    }

    /**
     * Updates the given ImageIO metadata with Sun's custom TIFF tags.
     * {@see https://svn.apache.org/repos/asf/xmlgraphics/commons/tags/commons-1_3_1/src/java/org/
     *       apache/xmlgraphics/image/writer/imageio/ImageIOTIFFImageWriter.java}
     * {@see http://download.java.net/media/jai-imageio/javadoc/1.0_01/com/sun/media/imageio/
     *       plugins/tiff/package-summary.html}
     * {@see http://partners.adobe.com/public/developer/tiff/index.html}
     * @param image buffered image which will be written
     * @param metadata ImageIO metadata
     * @param dpi image dots per inch
     */
    public static void updateMetadata(IIOMetadata metadata, BufferedImage image, int dpi)
    {
        debugLogMetadata(metadata, SUN_TIFF_FORMAT);

        if (!SUN_TIFF_FORMAT.equals(metadata.getNativeMetadataFormatName()))
        {
            LOG.debug("Using unknown TIFF image writer: " + metadata.getNativeMetadataFormatName());
            return;
        }

        IIOMetadataNode root = new IIOMetadataNode(SUN_TIFF_FORMAT);
        IIOMetadataNode ifd;
        if (root.getElementsByTagName("TIFFIFD").getLength() == 0)
        {
            ifd = new IIOMetadataNode("TIFFIFD");
            ifd.setAttribute("tagSets",
                             "com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet");
            root.appendChild(ifd);
        }
        else
        {
            ifd = (IIOMetadataNode)root.getElementsByTagName("TIFFIFD").item(0);
        }

        // standard metadata does not work, so we set the DPI manually
        ifd.appendChild(createRationalField(282, "XResolution", dpi, 1));
        ifd.appendChild(createRationalField(283, "YResolution", dpi, 1));
        ifd.appendChild(createShortField(296, "ResolutionUnit", 2)); // Inch

        ifd.appendChild(createLongField(278, "RowsPerStrip", image.getHeight()));
        ifd.appendChild(createAsciiField(305, "Software", "PDFBOX"));

        if (image.getType() == BufferedImage.TYPE_BYTE_BINARY && 
                image.getColorModel().getPixelSize() == 1)
        {
            // set PhotometricInterpretation WhiteIsZero
            // because of bug in Windows XP preview
            ifd.appendChild(createShortField(262, "PhotometricInterpretation", 0));
        }
        
        try
        {
            metadata.mergeTree(SUN_TIFF_FORMAT, root);
        }
        catch (IIOInvalidTreeException e)
        {
            // should never happen
            throw new RuntimeException(e);
        }

        debugLogMetadata(metadata, SUN_TIFF_FORMAT);
    }

    private static IIOMetadataNode createShortField(int tiffTagNumber, String name, int val)
    {
        IIOMetadataNode field, arrayNode, valueNode;
        field = new IIOMetadataNode("TIFFField");
        field.setAttribute("number", Integer.toString(tiffTagNumber));
        field.setAttribute("name", name);
        arrayNode = new IIOMetadataNode("TIFFShorts");
        field.appendChild(arrayNode);
        valueNode = new IIOMetadataNode("TIFFShort");
        arrayNode.appendChild(valueNode);
        valueNode.setAttribute("value", Integer.toString(val));
        return field;
    }

    private static IIOMetadataNode createAsciiField(int number, String name, String val)
    {
        IIOMetadataNode field, arrayNode, valueNode;
        field = new IIOMetadataNode("TIFFField");
        field.setAttribute("number", Integer.toString(number));
        field.setAttribute("name", name);
        arrayNode = new IIOMetadataNode("TIFFAsciis");
        field.appendChild(arrayNode);
        valueNode = new IIOMetadataNode("TIFFAscii");
        arrayNode.appendChild(valueNode);
        valueNode.setAttribute("value", val);
        return field;
    }

    private static IIOMetadataNode createLongField(int number, String name, long val)
    {
        IIOMetadataNode field, arrayNode, valueNode;
        field = new IIOMetadataNode("TIFFField");
        field.setAttribute("number", Integer.toString(number));
        field.setAttribute("name", name);
        arrayNode = new IIOMetadataNode("TIFFLongs");
        field.appendChild(arrayNode);
        valueNode = new IIOMetadataNode("TIFFLong");
        arrayNode.appendChild(valueNode);
        valueNode.setAttribute("value", Long.toString(val));
        return field;
    }

    private static IIOMetadataNode createRationalField(int number, String name, int numerator,
                                                       int denominator)
    {
        IIOMetadataNode field, arrayNode, valueNode;
        field = new IIOMetadataNode("TIFFField");
        field.setAttribute("number", Integer.toString(number));
        field.setAttribute("name", name);
        arrayNode = new IIOMetadataNode("TIFFRationals");
        field.appendChild(arrayNode);
        valueNode = new IIOMetadataNode("TIFFRational");
        arrayNode.appendChild(valueNode);
        valueNode.setAttribute("value", numerator + "/" + denominator);
        return field;
    }

}
