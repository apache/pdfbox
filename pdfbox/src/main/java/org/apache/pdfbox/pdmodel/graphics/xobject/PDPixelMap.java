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

import java.awt.Transparency;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDStream;

import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;



/**
 * This class contains a PixelMap Image.
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author mathiak
 * @version $Revision: 1.10 $
 */
public class PDPixelMap extends PDXObjectImage
{
    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDPixelMap.class);

    private BufferedImage image = null;

    /**
     * Standard constructor. Basically does nothing.
     * @param pdStream The stream that holds the pixel map.
     */
    public PDPixelMap(PDStream pdStream)
    {
        super(pdStream, "png");
    }

    /**
     * Construct a pixel map image from an AWT image.
     *
     * @param doc The PDF document to embed the image in.
     * @param awtImage The image to read data from.
     *
     * @throws IOException If there is an error while embedding this image.
     */
    /*
     * This method is broken and needs to be implemented, any takers?
    public PDPixelMap(PDDocument doc, BufferedImage awtImage) throws IOException
    {
        super( doc, "png");
        image = awtImage;
        setWidth( image.getWidth() );
        setHeight( image.getHeight() );

        ColorModel cm = image.getColorModel();
        ColorSpace cs = cm.getColorSpace();
        PDColorSpace pdColorSpace = PDColorSpaceFactory.createColorSpace( doc, cs );
        setColorSpace( pdColorSpace );
        //setColorSpace( )

        PDStream stream = getPDStream();
        OutputStream output = null;
        try
        {
            output = stream.createOutputStream();
            DataBuffer buffer = awtImage.getRaster().getDataBuffer();
            if( buffer instanceof DataBufferByte )
            {
                DataBufferByte byteBuffer = (DataBufferByte)buffer;
                byte[] data = byteBuffer.getData();
                output.write( data );
            }
            setBitsPerComponent( cm.getPixelSize() );
        }
        finally
        {
            if( output != null )
            {
                output.close();
            }
        }
    }*/

    /**
     * Returns a {@link java.awt.image.BufferedImage} of the COSStream
     * set in the constructor or null if the COSStream could not be encoded.
     *
     * @return {@inheritDoc}
     *
     * @throws IOException {@inheritDoc}
     */
    public BufferedImage getRGBImage() throws IOException
    {
        if( image != null )
        {
            return image;
        }

        try
        {
            int width = getWidth();
            int height = getHeight();
            int bpc = getBitsPerComponent();

            byte[] array = getPDStream().getByteArray();
            if (array.length == 0)
            {
                log.error("Something went wrong ... the pixelmap doesn't contain any data.");
                return null;
            }
            // Get the ColorModel right
            PDColorSpace colorspace = getColorSpace();
            if (colorspace == null)
            {
                log.error("getColorSpace() returned NULL.  Predictor = " + getPredictor());
                return null;
            }

            ColorModel cm = null;
            if (colorspace instanceof PDIndexed)
            {
                PDIndexed csIndexed = (PDIndexed)colorspace;
                ColorModel baseColorModel = csIndexed.getBaseColorSpace().createColorModel(bpc);
                int size = Math.min(csIndexed.getHighValue(), 1 << (bpc-1));  //suggested in PDFBOX-1075
                byte[] index = csIndexed.getLookupData();
                boolean hasAlpha = baseColorModel.hasAlpha();
                COSArray maskArray = getMask();
                if( baseColorModel.getTransferType() != DataBuffer.TYPE_BYTE )
                {
                    throw new IOException( "Not implemented" );
                }
                byte[] r = new byte[size+1];
                byte[] g = new byte[size+1];
                byte[] b = new byte[size+1];
                byte[] a = hasAlpha ? new byte[size+1] : null;
                byte[] inData = new byte[baseColorModel.getNumComponents()];
                for( int i = 0; i <= size; i++ )
                {
                    System.arraycopy(index, i * inData.length, inData, 0, inData.length);
                    r[i] = (byte)baseColorModel.getRed(inData);
                    g[i] = (byte)baseColorModel.getGreen(inData);
                    b[i] = (byte)baseColorModel.getBlue(inData);
                    if( hasAlpha )
                    {
                        a[i] = (byte)baseColorModel.getAlpha(inData);
                    }
                }
                if (hasAlpha)
                {
                    cm = new IndexColorModel(bpc, size+1, r, g, b, a);
                }
                else
                {
                    if (maskArray != null)
                    {
                        cm = new IndexColorModel(bpc, size+1, r, g, b, maskArray.getInt(0));
                    }
                    else
                    {
                        cm = new IndexColorModel(bpc, size+1, r, g, b);
                    }
                }
            }
            else if (bpc == 1)
            {
                byte[] map = null;
                if (colorspace instanceof PDDeviceGray)
                {
                    COSArray decode = getDecode();
                    // we have to invert the b/w-values,
                    // if the Decode array exists and consists of (1,0)
                    if (decode != null && decode.getInt(0) == 1)
                    {
                        map = new byte[] {(byte)0xff};
                    }
                    else
                    {
                        map = new byte[] {(byte)0x00, (byte)0xff};
                    }
                }
                else if (colorspace instanceof PDICCBased)
                {
                    if ( ((PDICCBased)colorspace).getNumberOfComponents() == 1)
                    {
                        map = new byte[] {(byte)0xff};
                    }
                    else
                    {
                        map = new byte[] {(byte)0x00, (byte)0xff};
                    }
                }
                else
                {
                    map = new byte[] {(byte)0x00, (byte)0xff};
                }
                cm = new IndexColorModel(bpc, map.length, map, map, map, Transparency.OPAQUE);
            }
            else
            {
                if (colorspace instanceof PDICCBased)
                {
                    if (((PDICCBased)colorspace).getNumberOfComponents() == 1)
                    {
                        byte[] map = new byte[] {(byte)0xff};
                        cm = new IndexColorModel(bpc, 1, map, map, map, Transparency.OPAQUE);
                    }
                    else
                    {
                        cm = colorspace.createColorModel( bpc );
                    }
                }
                else
                {
                    cm = colorspace.createColorModel( bpc );
                }
            }

            log.debug("ColorModel: " + cm.toString());
            WritableRaster raster = cm.createCompatibleWritableRaster( width, height );
            DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
            byte[] bufferData = buffer.getData();

            System.arraycopy( array, 0,bufferData, 0,
                    (array.length<bufferData.length?array.length: bufferData.length) );
            image = new BufferedImage(cm, raster, false, null);

            // If there is a 'soft mask' image then we use that as a transparency mask.
            PDXObjectImage smask = getSMaskImage();
            if (smask != null)
            {
                BufferedImage smaskBI = smask.getRGBImage();

                COSArray decodeArray = smask.getDecode();

                CompositeImage compositeImage = new CompositeImage(image, smaskBI);
                BufferedImage rgbImage = compositeImage.createMaskedImage(decodeArray);

                return rgbImage;
            }
            else
            {
                // But if there is no soft mask, use the unaltered image.
                return image;
            }
        }
        catch (Exception exception)
        {
            log.error(exception, exception);
            //A NULL return is caught in pagedrawer.Invoke.process() so don't re-throw.
            //Returning the NULL falls through to Phillip Koch's TODO section.
            return null;
        }
    }

    /**
     * Writes the image as .png.
     *
     * {@inheritDoc}
     */
    public void write2OutputStream(OutputStream out) throws IOException
    {
        getRGBImage();
        if (image != null)
        {
            ImageIO.write(image, "png", out);
        }
    }

    /**
     * DecodeParms is an optional parameter for filters.
     *
     * It is provided if any of the filters has nondefault parameters. If there
     * is only one filter it is a dictionary, if there are multiple filters it
     * is an array with an entry for each filter. An array entry can hold a null
     * value if only the default values are used or a dictionary with
     * parameters.
     *
     * @return The decoding parameters.
     *
     */
    public COSDictionary getDecodeParams()
    {
        COSBase decodeParms = getCOSStream().getDictionaryObject(COSName.DECODE_PARMS);
        if (decodeParms != null)
        {
            if (decodeParms instanceof COSDictionary)
            {
                return (COSDictionary) decodeParms;
            }
            else if (decodeParms instanceof COSArray)
            {
                // not implemented yet, which index should we use?
                return null;//(COSDictionary)((COSArray)decodeParms).get(0);
            }
            else
            {
                return null;
            }
        }
        return null;
    }

    /**
     * A code that selects the predictor algorithm.
     *
     * <ul>
     * <li>1 No prediction (the default value)
     * <li>2 TIFF Predictor 2
     * <li>10 PNG prediction (on encoding, PNG None on all rows)
     * <li>11 PNG prediction (on encoding, PNG Sub on all rows)
     * <li>12 PNG prediction (on encoding, PNG Up on all rows)
     * <li>13 PNG prediction (on encoding, PNG Average on all rows)
     * <li>14 PNG prediction (on encoding, PNG Path on all rows)
     * <li>15 PNG prediction (on encoding, PNG optimum)
     * </ul>
     *
     * Default value: 1.
     *
     * @return predictor algorithm code
     */
    public int getPredictor()
    {
        COSDictionary decodeParms = getDecodeParams();
        if (decodeParms != null)
        {
            int i = decodeParms.getInt(COSName.PREDICTOR);
            if (i != -1)
            {
                return i;
            }
        }
        return 1;
    }
}
