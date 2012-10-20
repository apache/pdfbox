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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.common.function.PDFunction;

import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDIndexed;
import org.apache.pdfbox.pdmodel.graphics.color.PDSeparation;
import org.apache.pdfbox.util.ImageIOUtil;



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
    private static final Log LOG = LogFactory.getLog(PDPixelMap.class);

    private BufferedImage image = null;

    private static final String PNG = "png";

    /**
     * Standard constructor. Basically does nothing.
     * @param pdStream The stream that holds the pixel map.
     */
    public PDPixelMap(PDStream pdStream)
    {
        super(pdStream, PNG);
    }

    /**
     * Construct a pixel map image from an AWT image.
     * 
     * 
     * @param doc The PDF document to embed the image in.
     * @param bi The image to read data from.
     *
     * @throws IOException If there is an error while embedding this image.
     */
    public PDPixelMap(PDDocument doc, BufferedImage bi) throws IOException
    {
        super( doc, PNG);
        createImageStream(doc, bi);
    }

    private void createImageStream(PDDocument doc, BufferedImage bi) throws IOException
    {
        BufferedImage alphaImage = null;
        BufferedImage rgbImage = null;
        int width = bi.getWidth();
        int height = bi.getHeight();
        if (bi.getColorModel().hasAlpha())
        {
            // extract the alpha information
            WritableRaster alphaRaster = bi.getAlphaRaster();
            ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), 
                    false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
            alphaImage = new BufferedImage(cm, alphaRaster, false, null);
            // create a RGB image without alpha
            rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = rgbImage.createGraphics();
            g.setComposite(AlphaComposite.Src);
            g.drawImage(bi, 0, 0, null);
        }
        else
        {
            rgbImage = bi;
        }
        java.io.OutputStream os = null;
        try
        {
            int numberOfComponents = rgbImage.getColorModel().getNumComponents();
            if (numberOfComponents == 3)
            {
                setColorSpace( PDDeviceRGB.INSTANCE );
            }
            else
            {
                if (numberOfComponents == 1)
                {
                    setColorSpace( new PDDeviceGray() );
                }
                else
                {
                    throw new IllegalStateException();
                }
            }
            byte[] outData = new byte[width * height * numberOfComponents];
            rgbImage.getData().getDataElements(0, 0, width, height, outData);
            // add FlateDecode compression
            getPDStream().addCompression();
            os = getCOSStream().createUnfilteredStream();
            os.write(outData);
            
            COSDictionary dic = getCOSStream();
            dic.setItem( COSName.FILTER, COSName.FLATE_DECODE );
            dic.setItem( COSName.SUBTYPE, COSName.IMAGE);
            dic.setItem( COSName.TYPE, COSName.XOBJECT );
            if(alphaImage != null)
            {
                PDPixelMap smask = new PDPixelMap(doc, alphaImage);
                dic.setItem(COSName.SMASK, smask);
            }
            setBitsPerComponent( 8 );
            setHeight( height );
            setWidth( width );
        }
        finally
        {
            if (os != null)
            {
                os.close();
            }
        }
    }
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
                LOG.error("Something went wrong ... the pixelmap doesn't contain any data.");
                return null;
            }
            // Get the ColorModel right
            PDColorSpace colorspace = getColorSpace();
            if (colorspace == null)
            {
                LOG.error("getColorSpace() returned NULL.  Predictor = " + getPredictor());
                return null;
            }

            ColorModel cm = null;
            if (colorspace instanceof PDIndexed)
            {
                PDIndexed csIndexed = (PDIndexed)colorspace;
                // the base color space uses 8 bit per component, as the indexed color values
                // of an indexed color space are always in a range from 0 to 255
                ColorModel baseColorModel = csIndexed.getBaseColorSpace().createColorModel(8);
                // number of possible color values in the target color space
                int numberOfColorValues = 1 << bpc;
                // number of indexed color values
                int highValue = csIndexed.getHighValue();
                // choose the correct size, sometimes there are more indexed values than needed
                // and sometimes there are fewer indexed value than possible
                int size = Math.min(numberOfColorValues-1, highValue);
                byte[] index = csIndexed.getLookupData();
                boolean hasAlpha = baseColorModel.hasAlpha();
                COSBase maskArray = getMask();
                if( baseColorModel.getTransferType() != DataBuffer.TYPE_BYTE )
                {
                    throw new IOException( "Not implemented" );
                }
                // the IndexColorModel uses RGB-based color values
                // which leads to 3 color components and a optional alpha channel
                int numberOfComponents = 3 + (hasAlpha ? 1 : 0);
                int buffersize = (size+1) * numberOfComponents;
                byte[] colorValues = new byte[buffersize];
                byte[] inData = new byte[baseColorModel.getNumComponents()];
                int bufferIndex = 0;
                for( int i = 0; i <= size; i++ )
                {
                    System.arraycopy(index, i * inData.length, inData, 0, inData.length);
                    // convert the indexed color values to RGB 
                    colorValues[bufferIndex] = (byte)baseColorModel.getRed(inData);
                    colorValues[bufferIndex+1] = (byte)baseColorModel.getGreen(inData);
                    colorValues[bufferIndex+2] = (byte)baseColorModel.getBlue(inData);
                    if( hasAlpha )
                    {
                        colorValues[bufferIndex+3] = (byte)baseColorModel.getAlpha(inData);
                    }
                    bufferIndex += numberOfComponents;
                }
                if (maskArray != null && maskArray instanceof COSArray)
                {
                    cm = new IndexColorModel(bpc, size+1, colorValues, 0, hasAlpha, ((COSArray)maskArray).getInt(0));
                }
                else
                {
                    cm = new IndexColorModel(bpc, size+1, colorValues, 0, hasAlpha);
                }
            }
            else if (colorspace instanceof PDSeparation)
            {
                PDSeparation csSeparation = (PDSeparation)colorspace;
                int numberOfComponents = csSeparation.getAlternateColorSpace().getNumberOfComponents();
                PDFunction tintTransformFunc = csSeparation.getTintTransform();
                COSArray decode = getDecode();
                // we have to invert the tint-values,
                // if the Decode array exists and consists of (1,0)
                boolean invert = decode != null && decode.getInt(0) == 1;
                // TODO add interpolation for other decode values then 1,0
                int maxValue = (int)Math.pow(2,bpc) - 1;
                // destination array
                byte[] mappedData = new byte[width*height*numberOfComponents];
                int rowLength = width*numberOfComponents;
                float[] input = new float[1];
                for ( int i = 0; i < height; i++ )
                {
                    int rowOffset = i * rowLength; 
                    for (int j = 0; j < width; j++)
                    {
                        // scale tint values to a range of 0...1
                        int value = (array[ i * width + j ] + 256) % 256;
                        if (invert)
                        {
                            input[0] = 1-(value / maxValue);
                        }
                        else
                        {
                            input[0] =  value / maxValue;
                        }
                        float[] mappedColor = tintTransformFunc.eval(input);
                        int columnOffset = j * numberOfComponents;
                        for ( int k = 0; k < numberOfComponents; k++ ) 
                        {
                            // redo scaling for every single color value 
                            float mappedValue = mappedColor[k];
                            mappedData[ rowOffset + columnOffset + k] = (byte)(mappedValue * maxValue);
                        }
                    }
                }
                array = mappedData;
                cm = colorspace.createColorModel( bpc );
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

            LOG.debug("ColorModel: " + cm.toString());
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
            else if (getImageMask())
            {
                BufferedImage stencilMask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = (Graphics2D)stencilMask.getGraphics();
                if (getStencilColor() != null)
                {
                    graphics.setColor(getStencilColor().getJavaColor());
                }
                else
                {
                    // this might happen when using ExractImages, see PDFBOX-1145
                    LOG.debug("no stencil color for PixelMap found, using Color.BLACK instead.");
                    graphics.setColor(Color.BLACK);
                }
                
                graphics.fillRect(0, 0, width, height);
                // assume default values ([0,1]) for the DecodeArray
                // TODO DecodeArray == [1,0]
                graphics.setComposite(AlphaComposite.DstIn);
                graphics.drawImage(image, null, 0, 0);
                return stencilMask;
            }
            else
            {
                // if there is no mask, use the unaltered image.
                return image;
            }
        }
        catch (Exception exception)
        {
            LOG.error(exception, exception);
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
            ImageIOUtil.writeImage(image, PNG, out);
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
