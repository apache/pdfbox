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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class handles some ImageIO operations.
 *
 * @version $Revision$
 * 
 */
public class ImageIOUtil
{   
    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(ImageIOUtil.class);

    /**
     * Default screen resolution: 72dpi.
     */
    public static final int DEFAULT_SCREEN_RESOLUTION = 72;
    /**
     * Default compression quality: 1.0f.
     */
    public static final float DEFAULT_COMPRESSION_QUALITY = 1.0f;
    
    private ImageIOUtil()
    {
        // Default constructor
    }

    /**
     * Writes a buffered image to a file using the given image format.
     * 
     * @param image the image to be written
     * @param imageFormat the target format (ex. "png")
     * @param filename used to construct the filename for the individual images
     * @param resolution the resolution in dpi (dots per inch)
     * 
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String imageFormat, String filename,
                                     int resolution) throws IOException
    {
        String fileName = filename + "." + imageFormat;
        File file = new File(fileName);
        return writeImage(image, imageFormat, file, resolution);
    }

    /**
     * Writes a buffered image to a file using the given image format.
     * 
     * @param image the image to be written
     * @param imageFormat the target format (ex. "png")
     * @param outputStream the output stream to be used for writing
     * 
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String imageFormat,
                                     OutputStream outputStream) throws IOException
    {
        return writeImage(image, imageFormat, outputStream, DEFAULT_SCREEN_RESOLUTION);
    }

    /**
     * Writes a buffered image to a file using the given image format.
     * 
     * @param image the image to be written
     * @param imageFormat the target format (ex. "png")
     * @param outputStream the output stream to be used for writing
     * @param resolution resolution to be used when writing the image
     * 
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String imageFormat, Object outputStream,
                                     int resolution) throws IOException
    {
        return writeImage(image, imageFormat, outputStream, resolution, DEFAULT_COMPRESSION_QUALITY);
    }
    
    /**
     * Writes a buffered image to a file using the given image format.
     * 
     * @param image the image to be written
     * @param imageFormat the target format (ex. "png")
     * @param outputStream the output stream to be used for writing
     * @param resolution resolution to be used when writing the image
     * @param quality quality to be used when compressing the image (0 &lt;
     * quality &lt; 1.0f)
     * 
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String imageFormat, Object outputStream,
                                     int resolution, float quality) throws IOException
    {
        boolean bSuccess = true;
        ImageOutputStream output = null;
        ImageWriter imageWriter = null;
        try
        {
            output = ImageIO.createImageOutputStream(outputStream);
    
            boolean foundWriter = false;
            Iterator<ImageWriter> writerIter = ImageIO.getImageWritersByFormatName(imageFormat);
            while (writerIter.hasNext() && !foundWriter)
            {
                try
                {
                    imageWriter = (ImageWriter) writerIter.next();
                    ImageWriteParam writerParams = imageWriter.getDefaultWriteParam();
                    if (writerParams.canWriteCompressed())
                    {
                        writerParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        // reset the compression type if overwritten by setCompressionMode

                        if (writerParams.getCompressionType() == null)
                        {
                            if (imageFormat.toLowerCase().startsWith("tif"))
                            {
                                // avoid error: first compression type is RLE, not optimal and incorrect for color images
                                //TODO? another writeImage() call with extra compression param so user can decide
                                if (image.getType() == BufferedImage.TYPE_BYTE_BINARY && image.getColorModel().getPixelSize() == 1)
                                {
                                    writerParams.setCompressionType("CCITT T.6");
                                }
                                else
                                {
                                    writerParams.setCompressionType("LZW");
                                }
                            }
                            else
                            {
                                writerParams.setCompressionType(writerParams.getCompressionTypes()[0]);
                            }
                        }
                        writerParams.setCompressionQuality(quality);
                    }
                    IIOMetadata meta = createMetadata(image, imageWriter, writerParams, resolution);
                    if (meta != null)
                    {
                        imageWriter.setOutput(output);
                        imageWriter.write(null, new IIOImage(image, null, meta), writerParams);
                    	foundWriter = true;
                    }
                }
                catch (IIOException io)
                {
                    LOG.error("IIOException in writeImage()", io);
                    throw new IOException(io.getMessage());
                }
                finally
                {
                    if (imageWriter != null)
                    {
                        imageWriter.dispose();
                    }
                }
            }
            if (!foundWriter)
            {
                LOG.error("No writer found for format '" + imageFormat + "'");
                bSuccess = false;
            }
        }
        finally
        {
            if (output != null)
            {
                output.flush();
                output.close();
            }
        }
        return bSuccess;
    }

    private static IIOMetadata createMetadata(BufferedImage image, ImageWriter imageWriter,
            ImageWriteParam writerParams, int resolution)
    {
        ImageTypeSpecifier type;
        if (writerParams.getDestinationType() != null)
        {
            type = writerParams.getDestinationType();
        }
        else
        {
            type = ImageTypeSpecifier.createFromRenderedImage( image );
        }
        IIOMetadata meta = imageWriter.getDefaultImageMetadata(type, writerParams);
        logMeta(meta, STANDARD_METADATA_FORMAT);
        if (imageWriter.getClass().getName().toUpperCase().contains("TIFF"))
        {
            updateMetadata(image, meta, resolution);
        }
        else
        {
            if (!addResolution(meta, resolution))
            {
                meta = null;
            }
        }
        logMeta(meta, STANDARD_METADATA_FORMAT);
        return meta;
    }

    /**
     * log the meta data as an XML tree if debug is enabled.
     * 
     * @param meta meta data.
     * @param format the XML format to be used.
     */
    private static void logMeta(IIOMetadata meta, String format)
    {
        if (!LOG.isDebugEnabled())
        {
            return;
        }
        if (meta == null)
        {
            LOG.debug("meta is null");
            return;
        }
        IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree(format);
        // http://download.java.net/jdk8/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
        // http://www.java-forum.org/java-basics-anfaenger-themen/96982-aufloesung-dpi-tiff-png-bildern-auslesen.html#post617178

        try
        {
            StringWriter xmlStringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(xmlStringWriter);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // http://stackoverflow.com/a/1264872/535646
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource domSource = new DOMSource(root);
            transformer.transform(domSource, streamResult);
            LOG.debug("\n" + xmlStringWriter);
        }
        catch (TransformerFactoryConfigurationError ex)
        {
            LOG.error(ex, ex);
        }
        catch (IllegalArgumentException ex)
        {
            LOG.error(ex, ex);
        }
        catch (TransformerException ex)
        {
            LOG.error(ex, ex);
        }
    }

    private static final String STANDARD_METADATA_FORMAT = "javax_imageio_1.0";
    private static final String SUN_TIFF_NATIVE_FORMAT
            = "com_sun_media_imageio_plugins_tiff_image_1.0";

    private static boolean addResolution(IIOMetadata meta, int resolution)
    {
        if (meta != null && !meta.isReadOnly() && meta.isStandardMetadataFormatSupported())
        {
            IIOMetadataNode root = (IIOMetadataNode)meta.getAsTree(STANDARD_METADATA_FORMAT);
            IIOMetadataNode dim = getChildNode(root, "Dimension");
            if (dim == null)
            {
                dim = new IIOMetadataNode("Dimension");
                root.appendChild(dim);
            }
            IIOMetadataNode child;
            child = getChildNode(dim, "HorizontalPixelSize");
            if (child == null)
            {
                child = new IIOMetadataNode("HorizontalPixelSize");
                dim.appendChild(child);
            }
            child.setAttribute("value",
                    Double.toString(resolution / 25.4));
            child = getChildNode(dim, "VerticalPixelSize");
            if (child == null)
            {
                child = new IIOMetadataNode("VerticalPixelSize");
                dim.appendChild(child);
            }
            child.setAttribute("value",
                    Double.toString(resolution / 25.4));
            try
            {
                meta.mergeTree(STANDARD_METADATA_FORMAT, root);
            }
            catch (IIOInvalidTreeException e)
            {
                throw new RuntimeException("Cannot update image metadata: "
                        + e.getMessage());
            }
            return true;
        }
        return false;
    }

    private static IIOMetadataNode getChildNode(Node n, String name)
    {
        NodeList nodes = n.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node child = nodes.item(i);
            if (name.equals(child.getNodeName()))
            {
                return (IIOMetadataNode)child;
            }
        }
        return null;
    }
    
    /**
     * Sets the resolution in a TIFF image, the resolution unit (Inches), rows
     * per strip to the height to get smaller files, and the name of the
     * software to "PDFBOX".
     *
     * @param tiffImage the TIFF Image
     * @param meta the meta data that is to be set
     * @param resolution in dots per inch
     */
    protected static void updateMetadata(BufferedImage tiffImage, IIOMetadata meta, int resolution)
    {
        // Code inspired by Apache XML graphics
        // https://svn.apache.org/repos/asf/xmlgraphics/commons/tags/commons-1_3_1/src/java/org/apache/xmlgraphics/image/writer/imageio/ImageIOTIFFImageWriter.java
        // DTD:
        // http://download.java.net/media/jai-imageio/javadoc/1.0_01/com/sun/media/imageio/plugins/tiff/package-summary.html
        // TIFF6 Spec:
        // http://partners.adobe.com/public/developer/tiff/index.html
        // We set the resolution manually using the native format since it appears that
        // it doesn't work properly through the standard metadata. Haven't figured out why
        // that happens.
        if (SUN_TIFF_NATIVE_FORMAT.equals(meta.getNativeMetadataFormatName()))
        {
            IIOMetadataNode root = new IIOMetadataNode(SUN_TIFF_NATIVE_FORMAT);
            IIOMetadataNode ifd = getChildNode(root, "TIFFIFD");
            if (ifd == null)
            {
                ifd = new IIOMetadataNode("TIFFIFD");
                ifd.setAttribute("tagSets",
                        "com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet");
                root.appendChild(ifd);
            }

            ifd.appendChild(createRationalField(282, "XResolution", resolution, 1));
            ifd.appendChild(createRationalField(283, "YResolution", resolution, 1));
            ifd.appendChild(createShortField(296, "ResolutionUnit", 2)); // Inch

            ifd.appendChild(createLongField(278, "RowsPerStrip", tiffImage.getHeight()));

            ifd.appendChild(createAsciiField(305, "Software", "PDFBOX"));

            try
            {
                meta.mergeTree(SUN_TIFF_NATIVE_FORMAT, root);
            }
            catch (IIOInvalidTreeException e)
            {
                throw new RuntimeException("Cannot update image metadata: "
                        + e.getMessage(), e);
            }
        }
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

    private static IIOMetadataNode createRationalField(int number, String name, int numerator, int denominator)
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
