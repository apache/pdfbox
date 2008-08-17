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
package org.pdfbox.pdmodel.graphics.xobject;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.IIOException;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.common.PDStream;
import org.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

/**
 * An image class for JPegs.
 *
 * @author mathiak
 * @version $Revision: 1.5 $
 */
public class PDJpeg extends PDXObjectImage
{

    private static final List DCT_FILTERS = new ArrayList();

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
        super(jpeg, "jpg");
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
        super( new PDStream( doc, is, true ), "jpg" );
        COSDictionary dic = getCOSStream();
        dic.setItem( COSName.FILTER, COSName.DCT_DECODE );
        dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
        dic.setItem( COSName.TYPE, COSName.getPDFName( "XObject" ) );

        BufferedImage image = getRGBImage();
        if (image != null) {
        setBitsPerComponent( 8 );
        setColorSpace( PDDeviceRGB.INSTANCE );
        setHeight( image.getHeight() );
        setWidth( image.getWidth() );
        }

    }

    /**
     * Construct from a buffered image.
     *
     * @param doc The document to create the image as part of.
     * @param bi The image to convert to a jpeg
     * @throws IOException If there is an error processing the jpeg data.
     */
    public PDJpeg( PDDocument doc, BufferedImage bi ) throws IOException
    {
        super( new PDStream( doc ), "jpg" );

        java.io.OutputStream os = getCOSStream().createFilteredStream();
        try
        {

            ImageIO.write(bi,"jpeg",os);

            COSDictionary dic = getCOSStream();
            dic.setItem( COSName.FILTER, COSName.DCT_DECODE );
            dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
            dic.setItem( COSName.TYPE, COSName.getPDFName( "XObject" ) );

            setBitsPerComponent( 8 );
            setColorSpace( PDDeviceRGB.INSTANCE );
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
        File imgFile = null;
        BufferedImage bi = null;
        boolean readError = false;
        try {
            imgFile = File.createTempFile("pdjpeg", ".jpeg");
            write2file(imgFile);

            // 1. try to read jpeg image
            try {
                bi = ImageIO.read(imgFile);
            } catch (IIOException iioe) {
                // cannot read jpeg
                readError = true;
            } catch (Exception ignore) {}

            // 2. try to read jpeg again. some jpegs have some strange header containing
            //    "Adobe " at some place. so just replace the header with a valid jpeg header.
            //    TODO: not sure if it works for all cases
            if (bi == null && readError) {
                byte newImage[] = replaceHeader(imgFile);

                ByteArrayInputStream bai = new ByteArrayInputStream(newImage);

                // persist file temporarely, because i was not able to manage
                // to call the ImageIO.read(InputStream) successfully.
                FileOutputStream o = new FileOutputStream(imgFile);
                byte[] buffer = new byte[512];
                int read;
                while ((read=bai.read(buffer)) >0) {
                   o.write(buffer, 0, read);
                }

                bai.close();
                o.close();

                bi = ImageIO.read(imgFile);
            }
        } finally {
            if (imgFile != null) {
                imgFile.delete();
            }
        }
        return bi;
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

    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();

        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is tooo large");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;

        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();

        return bytes;
    }

    private int getHeaderEndPos(byte[] image) {
        for (int i = 0; i < image.length; i++) {
            byte b = image[i];
            if (b == (byte) 0xDB) {         // to do: check for ff db
                return i -2;
            }
        }
        return 0;
    }

    private byte[] replaceHeader(File jpegFile) throws IOException {
        // read image into memory
        byte image[] = getBytesFromFile(jpegFile);

        // get end position of wrong header respectively startposition of "real jpeg data"
        int pos = getHeaderEndPos(image);

        // simple correct header
        byte header[] = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, (byte) 0x00, (byte) 0x10, (byte) 0x4A, (byte) 0x46, (byte) 0x49, (byte) 0x46, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x00};

        // concat
        byte newImage[] = new byte[image.length - pos + header.length - 1];
        System.arraycopy(header, 0, newImage, 0, header.length);
        System.arraycopy(image, pos + 1, newImage, header.length, image.length - pos - 1);

        return newImage;
    }
}
