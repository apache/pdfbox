/**
 * Copyright (c) 2004-2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.graphics.xobject;

import java.awt.image.DataBufferByte;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.pdmodel.common.PDStream;

import org.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.pdfbox.pdmodel.graphics.predictor.PredictorAlgorithm;

/**
 * This class contains a PixelMap Image. 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author mathiak
 * @version $Revision: 1.10 $
 */
public class PDPixelMap extends PDXObjectImage 
{
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

        //byte[] index =
        //ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);  
        int width = getWidth();
        int height = getHeight();
        int bpc = getBitsPerComponent();
        //COSInteger length =
        //        (COSInteger) stream.getStream().getDictionary().getDictionaryObject(COSName.LENGTH);
        //byte[] array = new byte[stream.getFilteredStream().];
        byte[] array = getPDStream().getByteArray(); 

//      Get the ColorModel right
        PDColorSpace colorspace = getColorSpace();
        ColorModel cm = colorspace.createColorModel( bpc );
        WritableRaster raster = cm.createCompatibleWritableRaster( width, height );
        //DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
        DataBufferByte buffer = (DataBufferByte)raster.getDataBuffer();
        byte[] bufferData = buffer.getData();
        //System.arraycopy( array, 0, bufferData, 0, array.length );
        int predictor = getPredictor();
        List filters = getPDStream().getFilters();
         
        /**
         * PDF Spec 1.6 3.3.3 LZW and Flate predictor function
         * 
         * Basically if predictor > 10 and LZW or Flate is being used then the
         * predictor is not used.
         *  
         * "For LZWDecode and FlateDecode, a Predictor value greater than or equal to 10
         * merely indicates that a PNG predictor is in use; the specific predictor function
         * used is explicitly encoded in the incoming data. The value of Predictor supplied
         * by the decoding filter need not match the value used when the data was encoded
         * if they are both greater than or equal to 10."
         */
        if( predictor < 10 || 
            filters == null || !(filters.contains( COSName.LZW_DECODE.getName()) || 
                                 filters.contains( COSName.FLATE_DECODE.getName()) ) )
        {
            PredictorAlgorithm filter = PredictorAlgorithm.getFilter(predictor);
            filter.setWidth(width);
            filter.setHeight(height);
            filter.setBpp((bpc * 3) / 8);
            filter.decode(array, bufferData);
        }
        else
        {
            System.arraycopy( array, 0,bufferData, 0, bufferData.length );
        }
        image = new BufferedImage(cm, raster, false, null);
        return image;
    }

    /**
     * Writes the image as .png.
     * 
     * {@inheritDoc}
     */
    public void write2OutputStream(OutputStream out) throws IOException 
    {
        getRGBImage();
        if (image!=null)
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
        COSBase decodeParms = getCOSStream().getDictionaryObject("DecodeParms");
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
     * <li>14 PNG prediction (on encoding, PNG Paeth on all rows)
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
            int i = decodeParms.getInt("Predictor");
            if (i != -1)
            {
                return i;
            }
        }
        return 1;
    }
}