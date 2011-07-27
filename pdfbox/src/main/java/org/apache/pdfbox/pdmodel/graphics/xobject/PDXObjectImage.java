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

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpaceFactory;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;

/**
 * The prototype for all PDImages.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author mathiak
 * @version $Revision: 1.9 $
 */
public abstract class PDXObjectImage extends PDXObject
{

    /**
     * Log instance.
     */
    private static final Log log = LogFactory.getLog(PDXObjectImage.class);

    /**
     * The XObject subtype.
     */
    public static final String SUB_TYPE = "Image";

    /**
     * This contains the suffix used when writing to file.
     */
    private String suffix;

    private PDGraphicsState graphicsState;

    /**
     * Standard constuctor.
     *
     * @param imageStream The XObject is passed as a COSStream.
     * @param fileSuffix The file suffix, jpg/png.
     */
    public PDXObjectImage(PDStream imageStream, String fileSuffix)
    {
        super( imageStream );
        suffix = fileSuffix;
    }

    /**
     * Standard constuctor.
     *
     * @param doc The document to store the stream in.
     * @param fileSuffix The file suffix, jpg/png.
     */
    public PDXObjectImage(PDDocument doc, String fileSuffix)
    {
        super( doc );
        getCOSStream().setName( COSName.SUBTYPE, SUB_TYPE );
        suffix = fileSuffix;
    }

    /**
     * Returns an java.awt.Image, that can be used for display etc.
     *
     * @return This PDF object as an AWT image.
     *
     * @throws IOException If there is an error creating the image.
     */
    public abstract BufferedImage getRGBImage() throws IOException;

    /**
     * Returns a PDXObjectImage of the SMask image, if there is one.
     * See section 11.5 of the pdf specification for details on Soft Masks.
     *
     * @return the PDXObjectImage of the SMask if there is one, else <code>null</code>.
     * @throws IOException if an I/O error occurs creating an XObject
     */
    public PDXObjectImage getSMaskImage() throws IOException
    {
        COSStream cosStream = getPDStream().getStream();
        COSBase smask = cosStream.getDictionaryObject(COSName.SMASK);

        if (smask == null)
        {
            return null;
        }
        else
        {
            return (PDXObjectImage)PDXObject.createXObject(smask);
        }
    }

    /**
     * Writes the Image to out.
     * @param out the OutputStream that the Image is written to.
     * @throws IOException when somethings wrong with out
     */
    public abstract void write2OutputStream(OutputStream out) throws IOException;

    /**
     * Writes the image to a file with the filename + an appropriate suffix, like "Image.jpg".
     * The suffix is automatically set by the
     * @param filename the filename
     * @throws IOException When somethings wrong with the corresponding file.
     */
    public void write2file(String filename) throws IOException
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(filename + "." + suffix);
            write2OutputStream(out);
            out.flush();
        }
        finally
        {
            if( out != null )
            {
                out.close();
            }
        }
    }

        /**
     * Writes the image to a file with the filename + an appropriate
suffix, like "Image.jpg".
     * The suffix is automatically set by the
     * @param file the file
     * @throws IOException When somethings wrong with the corresponding
file.
     */
    public void write2file(File file) throws IOException
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            write2OutputStream(out);
            out.flush();
        }
        finally
        {
            if( out != null )
            {
                out.close();
            }
        }
    }

    /**
     * Get the height of the image.
     *
     * @return The height of the image.
     */
    public int getHeight()
    {
        return getCOSStream().getInt( COSName.HEIGHT, -1 );
    }

    /**
     * Set the height of the image.
     *
     * @param height The height of the image.
     */
    public void setHeight( int height )
    {
        getCOSStream().setInt( COSName.HEIGHT, height );
    }

    /**
     * Get the width of the image.
     *
     * @return The width of the image.
     */
    public int getWidth()
    {
        return getCOSStream().getInt( COSName.WIDTH, -1 );
    }

    /**
     * Set the width of the image.
     *
     * @param width The width of the image.
     */
    public void setWidth( int width )
    {
        getCOSStream().setInt( COSName.WIDTH, width );
    }

    /**
     * The bits per component of this image.  This will return -1 if one has not
     * been set.
     *
     * @return The number of bits per component.
     */
    public int getBitsPerComponent()
    {
        return getCOSStream().getInt( new String[] { "BPC", "BitsPerComponent"}, -1 );
    }

    /**
     * Set the number of bits per component.
     *
     * @param bpc The number of bits per component.
     */
    public void setBitsPerComponent( int bpc )
    {
        getCOSStream().setInt( "BitsPerComponent", bpc );
    }

    /**
     * This will get the color space or null if none exists.
     *
     * @return The color space for this image.
     *
     * @throws IOException If there is an error getting the colorspace.
     */
    public PDColorSpace getColorSpace() throws IOException
    {
        COSBase cs = getCOSStream().getDictionaryObject( new String[]{ "CS", "ColorSpace" } );
        PDColorSpace retval = null;
        if( cs != null )
        {
            retval = PDColorSpaceFactory.createColorSpace( cs );
            if (retval == null)
                {
                    log.info("About to return NULL from createColorSpace branch");
                }
        }
        else
        {
            //there are some cases where the 'required' CS value is not present
            //but we know that it will be grayscale for a CCITT filter.
            COSBase filter = getCOSStream().getDictionaryObject( "Filter" );
            if( COSName.CCITTFAX_DECODE.equals( filter ) ||
                COSName.CCITTFAX_DECODE_ABBREVIATION.equals( filter ) )
            {
                retval = new PDDeviceGray();
                if (retval == null)
                    {
                        log.info("About to return NULL from CCITT branch");
                    }
            }
            else if( COSName.JBIG2_DECODE.equals( filter ) )
            {
                retval = new PDDeviceGray();
                if (retval == null)
                {
                    log.info("About to return NULL from JBIG2 branch");
                }
            }
            else if (getImageMask())
            {
                //Stencil Mask branch.  Section 4.8.5 of the reference, page 350 in version 1.7.
                retval = graphicsState.getNonStrokingColor().getColorSpace();
                log.info("Stencil Mask branch returning " + retval.toString());
                //throw new IOException("Trace the Stencil Mask!!!!");

            }
            else
            {
                log.info("About to return NULL from unhandled branch."
                        + " filter = " + filter);
            }
        }
        return retval;
    }

    /**
     * This will set the color space for this image.
     *
     * @param cs The color space for this image.
     */
    public void setColorSpace( PDColorSpace cs )
    {
        COSBase base = null;
        if( cs != null )
        {
            base = cs.getCOSObject();
        }
        getCOSStream().setItem( COSName.COLORSPACE, base );
    }

    /**
     * This will get the suffix for this image type, jpg/png.
     *
     * @return The image suffix.
     */
    public String getSuffix()
    {
        return suffix;
    }

    /**
     * Get the ImageMask flag. Used in Stencil Masking.  Section 4.8.5 of the spec.
     *
     * @return The ImageMask flag.  This is optional and defaults to False, so if it does not exist, we return False
     */
    public boolean getImageMask()
    {
        return getCOSStream().getBoolean( COSName.IMAGE_MASK, false );
    }

    /**
     * Allow the Invoke operator to set the graphics state so that,
     * in the case of an Image Mask, we can get to the current nonstroking colorspace.
     * @param newGS The new graphicstate
     */
    public void setGraphicsState(PDGraphicsState newGS)
    {
        graphicsState = newGS;
    }

    /**
     * Returns the Decode Array of an XObjectImage.
     * @return the decode array
     */
    public COSArray getDecode()
    {
        COSBase decode = getCOSStream().getDictionaryObject( COSName.DECODE );
        if (decode != null && decode instanceof COSArray)
        {
            return (COSArray)decode;
        }
        return null;
    }

    /**
     * Returns the optional mask of a XObjectImage if there is one.
     *
     * @return The mask as COSArray otherwise null.
     */
    public COSArray getMask()
    {
        COSBase mask = getCOSStream().getDictionaryObject(COSName.MASK);
        if (mask != null)
        {
            return (COSArray)mask;
        }
        return null;
    }
}
