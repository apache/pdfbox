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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageWriteParam;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import static org.apache.pdfbox.tools.imageio.MetaUtil.debugLogMetadata;
import org.w3c.dom.NodeList;

/**
 * Used by ImageIOUtil to write TIFF files.
 * @author Tilman Hausherr
 */
final class TIFFUtil
{
    private static final Log LOG = LogFactory.getLog(TIFFUtil.class);

    private TIFFUtil()
    {
    }    

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
     * Updates the given ImageIO metadata with Sun's custom TIFF tags, as described in
     * the <a href="https://svn.apache.org/repos/asf/xmlgraphics/commons/tags/commons-1_3_1/src/java/org/apache/xmlgraphics/image/writer/imageio/ImageIOTIFFImageWriter.java">org.apache.xmlgraphics.image.writer.imageio.ImageIOTIFFImageWriter
     * sources</a>, 
     * the <a href="http://download.java.net/media/jai-imageio/javadoc/1.0_01/com/sun/media/imageio/plugins/tiff/package-summary.html">com.sun.media.imageio.plugins.tiff
     * package javadoc</a>
     * and the <a href="http://partners.adobe.com/public/developer/tiff/index.html">TIFF
     * specification</a>.
     *
     * @param image buffered image which will be written
     * @param metadata ImageIO metadata
     * @param dpi image dots per inch
     * @throws IIOInvalidTreeException if something goes wrong
     */
    static void updateMetadata(IIOMetadata metadata, BufferedImage image, int dpi)
            throws IIOInvalidTreeException
    {
        String metaDataFormat = metadata.getNativeMetadataFormatName();
        if (metaDataFormat == null)
        {
            LOG.debug("TIFF image writer doesn't support any data format");
            return;
        }

        debugLogMetadata(metadata, metaDataFormat);

        IIOMetadataNode root = new IIOMetadataNode(metaDataFormat);
        IIOMetadataNode ifd;
        NodeList nodeListTIFFIFD = root.getElementsByTagName("TIFFIFD");
        if (nodeListTIFFIFD.getLength() == 0)
        {
            ifd = new IIOMetadataNode("TIFFIFD");
            root.appendChild(ifd);
        }
        else
        {
            ifd = (IIOMetadataNode) nodeListTIFFIFD.item(0);
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
        
        metadata.mergeTree(metaDataFormat, root);
        
        debugLogMetadata(metadata, metaDataFormat);
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
