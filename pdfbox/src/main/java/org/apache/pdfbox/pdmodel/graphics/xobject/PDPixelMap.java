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
import java.awt.Transparency;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

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
     * @param doc The PDF document to embed the image in.
     * @param bi The image to read data from.
     *
     * @throws IOException If there is an error while embedding this image.
     */
    public PDPixelMap(PDDocument doc, BufferedImage bi) throws IOException
    {
        super( doc, PNG);
        createImageStream(doc, bi, false);
    }

    /**
     * Construct a pixel map image from an AWT image.
     * 
     * @param doc The PDF document to embed the image in.
     * @param bi The image to read data from.
     * @param isMask true if this is a mask, false if not.
     *
     * @throws IOException If there is an error while embedding this image.
     */
    private PDPixelMap(PDDocument doc, BufferedImage bi, boolean isMask) throws IOException
    {
        super(doc, PNG);
        createImageStream(doc, bi, isMask);
    }

    /**
     * Create an image stream from an AWT image.
     * 
     * @param doc The PDF document to embed the image in.
     * @param bi The image to read data from.
     * @param isMask true if this is a mask, false if not. If true, the image
     * stream will be forced to be DeviceGray.
     * 
     * @throws IOException If there is an error while embedding this image.
     */
    private void createImageStream(PDDocument doc, BufferedImage bi, boolean isMask) throws IOException
    {
        BufferedImage alphaImage = null;
        BufferedImage rgbImage;
        int width = bi.getWidth();
        int height = bi.getHeight();
        if (bi.getColorModel().hasAlpha())
        {
            alphaImage = extractAlphaImage(bi);

            // create RGB image without alpha
            //BEWARE: the previous solution in the history 
            // g.setComposite(AlphaComposite.Src) and g.drawImage()
            // didn't work properly for TYPE_4BYTE_ABGR.
            // alpha values of 0 result in a black dest pixel!!!
            rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            for (int x = 0; x < width; ++x)
            {
                for (int y = 0; y < height; ++y)
                {
                    rgbImage.setRGB(x, y, bi.getRGB(x, y) & 0xFFFFFF);
                }
            }
        }
        else
        {
            rgbImage = bi;
        }
        OutputStream os = null;
        try
        {
            int numberOfComponents = rgbImage.getColorModel().getNumComponents();
            if (numberOfComponents != 3 && numberOfComponents != 1)
            {
                throw new IllegalStateException();
            }

            int bpc;

            // use FlateDecode compression
            getPDStream().addCompression();
            os = getCOSStream().createUnfilteredStream();

            if (isMask
                    || (bi.getType() == BufferedImage.TYPE_BYTE_GRAY && bi.getColorModel().getPixelSize() <= 8)
                    || (bi.getType() == BufferedImage.TYPE_BYTE_BINARY && bi.getColorModel().getPixelSize() == 1)
                    )
            {
                // optimized (1 byte per pixel handling) for 1bit and gray
                //BEWARE: doesn't work with TYPE_BYTE_BINARY images with more than 1 bit (colored)!
                setColorSpace(new PDDeviceGray());
                bpc = bi.getColorModel().getPixelSize();
                if (bpc < 8)
                {
                    MemoryCacheImageOutputStream mcios = new MemoryCacheImageOutputStream(os);
                    for (int y = 0; y < height; ++y)
                    {
                        for (int x = 0; x < width; ++x)
                        {
                            // grayscale images need one color per sample
                            mcios.writeBits(bi.getRGB(x, y) & 0xFF, bpc);
                        }
                        // padding
                        while (mcios.getBitOffset() != 0)
                        {
                            mcios.writeBit(0);
                        }
                    }
                    mcios.flush();
                    mcios.close();
                }
                else
                {
                    //BEWARE: the gray values must be extracted from raster
                    // and not from getRGB or the TYPE_BYTE_GRAY tests will fail
                    DataBuffer dataBuffer = rgbImage.getData().getDataBuffer();
                    for (int y = 0; y < height; ++y)
                    {
                        for (int x = 0; x < width; ++x)
                        {
                            // grayscale images need one color per sample
                            os.write(dataBuffer.getElem(y * width + x));
                        }
                    }
                }
            }
            else
            {
                // RGB
                setColorSpace(PDDeviceRGB.INSTANCE);
                bpc = 8;
                for (int y = 0; y < height; ++y)
                {
                    for (int x = 0; x < width; ++x)
                    {
                        // rgb images need three colors per sample
                        Color color = new Color(rgbImage.getRGB(x, y));
                        os.write(color.getRed());
                        os.write(color.getGreen());
                        os.write(color.getBlue());
                    }
                }
            }
            COSDictionary dic = getCOSStream();
            dic.setItem(COSName.FILTER, COSName.FLATE_DECODE);
            dic.setItem(COSName.SUBTYPE, COSName.IMAGE);
            dic.setItem(COSName.TYPE, COSName.XOBJECT);
            if (alphaImage != null)
            {
                PDPixelMap smask = new PDPixelMap(doc, alphaImage, true);
                dic.setItem(COSName.SMASK, smask);
            }
            setBitsPerComponent(bpc);
            setHeight(height);
            setWidth(width);
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
            byte[] array = getPDStream().getByteArray();
            if (array.length == 0)
            {
                LOG.error("Something went wrong ... the pixelmap doesn't contain any data.");
                return null;
            }
            int width = getWidth();
            int height = getHeight();
            int bpc = getBitsPerComponent();

            PDColorSpace colorspace = getColorSpace();
            if (colorspace == null)
            {
                LOG.error("getColorSpace() returned NULL.");
                return null;
            }
            // Get the ColorModel right
            ColorModel cm;
            if (colorspace instanceof PDIndexed)
            {
                PDIndexed csIndexed = (PDIndexed)colorspace;
                COSBase maskArray = getMask();
                if (maskArray != null && maskArray instanceof COSArray)
                {
                    cm = csIndexed.createColorModel(bpc, ((COSArray)maskArray).getInt(0));
                }
                else
                {
                    cm = csIndexed.createColorModel(bpc);
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
                byte[] map;
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
                else
                {
                    map = new byte[] {(byte)0x00, (byte)0xff};
                }
                cm = new IndexColorModel(bpc, map.length, map, map, map, Transparency.OPAQUE);
            }
            else
            {
                cm = colorspace.createColorModel( bpc );
            }

            LOG.debug("ColorModel: " + cm.toString());
            WritableRaster raster = cm.createCompatibleWritableRaster( width, height );
            DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
            byte[] bufferData = buffer.getData();

            System.arraycopy( array, 0,bufferData, 0,
                    (array.length<bufferData.length?array.length: bufferData.length) );
            image = new BufferedImage(cm, raster, false, null);
           
            return applyMasks(image);  
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
     * @deprecated Use {@link org.apache.pdfbox.pdmodel.common.PDStream#getDecodeParms() } instead
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
     * 
     * @deprecated see {@link org.apache.pdfbox.filter.FlateFilter}
     * 
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
    
    @Override
    public void clear()
    {
        super.clear();
        image = null;
    }
}
