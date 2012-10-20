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
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
     * @param imageType the image type (see {@link BufferedImage}.TYPE_*)
     * @param resolution the resolution in dpi (dots per inch)
     * 
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String imageFormat, String filename, 
            int imageType, int resolution)
    throws IOException
    {
        String fileName = filename + "." + imageFormat;
        File file = new File( fileName );
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
    public static boolean writeImage(BufferedImage image, String imageFormat, OutputStream outputStream) 
    throws IOException
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
    public static boolean writeImage(BufferedImage image, String imageFormat, Object outputStream, int resolution)
    throws IOException
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
     * @param quality quality to be used when compressing the image (0 < quality < 1.0f)
     * 
     * @return true if the images were produced, false if there was an error
     * @throws IOException if an I/O error occurs
     */
    public static boolean writeImage(BufferedImage image, String imageFormat, Object outputStream, int resolution, 
            float quality)
    throws IOException
    {
        boolean bSuccess = true;
        ImageOutputStream output = null;
        ImageWriter imageWriter = null;
        try
        {
            output = ImageIO.createImageOutputStream( outputStream );
    
            boolean foundWriter = false;
            Iterator<ImageWriter> writerIter = ImageIO.getImageWritersByFormatName( imageFormat );
            while( writerIter.hasNext() && !foundWriter )
            {
                try
                {
                    imageWriter = (ImageWriter)writerIter.next();
                    ImageWriteParam writerParams = imageWriter.getDefaultWriteParam();
                    if( writerParams.canWriteCompressed() )
                    {
                        writerParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        // reset the compression type if overwritten by setCompressionMode
                        if (writerParams.getCompressionType() == null)
                        {
                            writerParams.setCompressionType(writerParams.getCompressionTypes()[0]);
                        }
                        writerParams.setCompressionQuality(quality);
                    }
                    IIOMetadata meta = createMetadata( image, imageWriter, writerParams, resolution);
                    imageWriter.setOutput( output );
                    imageWriter.write( null, new IIOImage( image, null, meta ), writerParams );
                    foundWriter = true;
                }
                catch( IIOException io )
                {
                    throw new IOException( io.getMessage() );
                }
                finally
                {
                    if( imageWriter != null )
                    {
                        imageWriter.dispose();
                    }
                }
            }
            if( !foundWriter )
            {
                bSuccess = false;
            }
        }
        finally
        {
            if( output != null )
            {
                output.flush();
                output.close();
            }
        }
        return bSuccess;
    }

    private static IIOMetadata createMetadata(RenderedImage image, ImageWriter imageWriter,
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
        IIOMetadata meta = imageWriter.getDefaultImageMetadata( type, writerParams );
        return (addResolution(meta, resolution) ? meta : null);
    }

    private static final String STANDARD_METADATA_FORMAT = "javax_imageio_1.0";

    private static boolean addResolution(IIOMetadata meta, int resolution)
    {
        if (!meta.isReadOnly() && meta.isStandardMetadataFormatSupported())
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
    
}
