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
package org.apache.pdfbox.pdmodel.graphics.xobject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.IIOException;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

/**
 * An image class for JPegs.
 *
 * @author mathiak
 * @version $Revision: 1.5 $
 */
public class PDJpeg extends PDXObjectImage
{

    private static final String JPG = "jpg";

    private static final List<String> DCT_FILTERS = new ArrayList<String>();

    private static final float DEFAULT_COMPRESSION_LEVEL = 0.75f;

    static
    {
        DCT_FILTERS.add( COSName.DCT_DECODE.getName() );
        DCT_FILTERS.add( COSName.DCT_DECODE_ABBREVIATION.getName() );
    }

    /**
     * Standard constructor.
     *
     * @param jpeg The COSStream from which to extract the JPeg
     */
    public PDJpeg(PDStream jpeg)
    {
        super(jpeg, JPG);
    }

    /**
     * Construct from a stream.
     *
     * @param doc The document to create the image as part of.
     * @param is The stream that contains the jpeg data.
     * @throws IOException If there is an error reading the jpeg data.
     */
    public PDJpeg( PDDocument doc, InputStream is ) throws IOException
    {
        super( new PDStream( doc, is, true ), JPG);
        COSDictionary dic = getCOSStream();
        dic.setItem( COSName.FILTER, COSName.DCT_DECODE );
        dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
        dic.setItem( COSName.TYPE, COSName.XOBJECT );

        BufferedImage image = getRGBImage();
        if (image != null)
        {
            setBitsPerComponent( 8 );
            setColorSpace( PDDeviceRGB.INSTANCE );
            setHeight( image.getHeight() );
            setWidth( image.getWidth() );
        }

    }

    /**
     * Construct from a buffered image.
     * The default compression level of 0.75 will be used.
     *
     * @param doc The document to create the image as part of.
     * @param bi The image to convert to a jpeg
     * @throws IOException If there is an error processing the jpeg data.
     */
    public PDJpeg( PDDocument doc, BufferedImage bi ) throws IOException
    {
        super( new PDStream( doc ) , JPG);
        createImageStream(doc, bi, DEFAULT_COMPRESSION_LEVEL);
    }

    /**
     * Construct from a buffered image.
     *
     * @param doc The document to create the image as part of.
     * @param bi The image to convert to a jpeg
     * @param compressionQuality The quality level which is used to compress the image
     * @throws IOException If there is an error processing the jpeg data.
     */
    public PDJpeg( PDDocument doc, BufferedImage bi, float compressionQuality ) throws IOException
    {
        super( new PDStream( doc ), JPG);
        createImageStream(doc, bi, compressionQuality);
    }

    private void createImageStream(PDDocument doc, BufferedImage bi, float compressionQuality) throws IOException
    {
        BufferedImage alpha = null;
        if (bi.getColorModel().hasAlpha())
        {
            alpha = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = alpha.createGraphics();
            g.setColor(Color.BLACK);
            g.drawRect(0, 0, bi.getWidth(), bi.getHeight());
            g.setColor(Color.WHITE);
            g.drawImage(bi, 0, 0, null);
            int alphaHeight = alpha.getHeight();
            int alphaWidth = alpha.getWidth();
            int whiteRGB = (Color.WHITE).getRGB();
            for(int y = 0; y < alphaHeight; y++)
            {
                for(int x = 0; x < alphaWidth; x++)
                {
                    Color color = new Color(alpha.getRGB(x, y));
                    if(color.getRed() != 0 && color.getGreen() != 0 && color.getBlue() != 0)
                    {
                        alpha.setRGB(x, y, whiteRGB);
                    }
                }
            }
            BufferedImage image = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_USHORT_565_RGB);
            g = image.createGraphics();
            g.drawImage(bi, 0, 0, null);
            bi = image;
        }

        java.io.OutputStream os = getCOSStream().createFilteredStream();
        try
        {
            ImageWriter writer = null;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(JPG);
            if (iter.hasNext())
            {
                writer = iter.next();
            }
            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);

            // Set the compression quality
            JPEGImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
            iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwparam.setCompressionQuality(compressionQuality);

            // Write the image
            writer.write(null, new IIOImage(bi, null, null), iwparam);

            writer.dispose();

            COSDictionary dic = getCOSStream();
            dic.setItem( COSName.FILTER, COSName.DCT_DECODE );
            dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
            dic.setItem( COSName.TYPE, COSName.XOBJECT );
            PDXObjectImage alphaPdImage = null;
            if(alpha != null)
            {
                alphaPdImage = new PDJpeg(doc, alpha, compressionQuality);
                dic.setItem(COSName.SMASK, alphaPdImage);
            }
            setBitsPerComponent( 8 );
            if (bi.getColorModel().getNumComponents() == 3)
            {
                setColorSpace( PDDeviceRGB.INSTANCE );
            }
            else
            {
                if (bi.getColorModel().getNumComponents() == 1)
                {
                    setColorSpace( new PDDeviceGray() );
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
            setHeight( bi.getHeight() );
            setWidth( bi.getWidth() );
        }
        finally
        {
            os.close();
        }
    }

    /**
     * Returns an image of the JPeg, or null if JPegs are not supported. (They should be. )
     * {@inheritDoc}
     */
    public BufferedImage getRGBImage() throws IOException
    {   //TODO PKOCH
        BufferedImage bi = null;
        boolean readError = false;
        try
        {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            write2OutputStream(os);
            os.close();
            byte[] img = os.toByteArray();

            // 1. try to read jpeg image
            try
            {
                bi = ImageIO.read(new ByteArrayInputStream(img));
            }
            catch (IIOException iioe)
            {
                // cannot read jpeg
                readError = true;
            }
            catch (Exception ignore)
            {
            }

            // 2. try to read jpeg again. some jpegs have some strange header containing
            //    "Adobe " at some place. so just replace the header with a valid jpeg header.
            // TODO : not sure if it works for all cases
            if (bi == null && readError)
            {
                byte[] newImage = replaceHeader(img);

                ByteArrayInputStream bai = new ByteArrayInputStream(newImage);

                bi = ImageIO.read(bai);
            }
        }
        finally
        {
        }

        // If there is a 'soft mask' image then we use that as a transparency mask.
        PDXObjectImage smask = getSMaskImage();
        if (smask != null)
        {
            BufferedImage smaskBI = smask.getRGBImage();

            COSArray decodeArray = smask.getDecode();
            CompositeImage compositeImage = new CompositeImage(bi, smaskBI);
            BufferedImage rgbImage = compositeImage.createMaskedImage(decodeArray);

            return rgbImage;
        }
        else
        {
            // But if there is no soft mask, use the unaltered image.
            return bi;
        }
    }

    /**
     * This writes the JPeg to out.
     * {@inheritDoc}
     */
    public void write2OutputStream(OutputStream out) throws IOException
    {
        InputStream data = getPDStream().getPartiallyFilteredStream( DCT_FILTERS );
        byte[] buf = new byte[1024];
        int amountRead = -1;
        while( (amountRead = data.read( buf )) != -1 )
        {
            out.write( buf, 0, amountRead );
        }
    }

    /**
     * Returns the given file as byte array.
     * @param file File to be read
     * @return given file as byte array
     * @throws IOException if somethin went wrong during reading the file
     */
    public static byte[] getBytesFromFile(File file) throws IOException
    {
        InputStream is = new FileInputStream(file);
        long length = file.length();

        if (length > Integer.MAX_VALUE)
        {
            // File is too large
            throw new IOException("File is tooo large");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;

        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0)
        {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length)
        {
            throw new IOException("Could not completely read file "+file.getName());
        }
        is.close();
        return bytes;
    }

    private int getHeaderEndPos(byte[] image)
    {
        for (int i = 0; i < image.length; i++)
        {
            byte b = image[i];
            if (b == (byte) 0xDB)
            {
                // TODO : check for ff db
                return i -2;
            }
        }
        return 0;
    }

    private byte[] replaceHeader(byte[] image)
    {
        // get end position of wrong header respectively startposition of "real jpeg data"
        int pos = getHeaderEndPos(image);

        // simple correct header
        byte[] header = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, (byte) 0x00,
                (byte) 0x10, (byte) 0x4A, (byte) 0x46, (byte) 0x49, (byte) 0x46, (byte) 0x00, (byte) 0x01,
                (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x00};

        // concat
        byte[] newImage = new byte[image.length - pos + header.length - 1];
        System.arraycopy(header, 0, newImage, 0, header.length);
        System.arraycopy(image, pos + 1, newImage, header.length, image.length - pos - 1);

        return newImage;
    }
}

