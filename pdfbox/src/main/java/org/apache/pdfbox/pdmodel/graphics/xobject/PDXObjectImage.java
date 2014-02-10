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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
import org.apache.pdfbox.pdmodel.graphics.color.PDColorState;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;

/**
 * The prototype for all PDImages.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author mathiak
 */
public abstract class PDXObjectImage extends PDXObject
{

    /**
     * Log instance.
     */
    private static final Log LOG = LogFactory.getLog(PDXObjectImage.class);

    /**
     * The XObject subtype.
     */
    public static final String SUB_TYPE = "Image";

    /**
     * This contains the suffix used when writing to file.
     */
    private String suffix;

    private PDColorState stencilColor;

    /**
     * Standard constructor.
     *
     * @param imageStream The XObject is passed as a COSStream.
     * @param fileSuffix The file suffix, jpg/png.
     */
    public PDXObjectImage(PDStream imageStream, String fileSuffix)
    {
        super(imageStream);
        suffix = fileSuffix;
    }

    /**
     * Standard constructor.
     *
     * @param doc The document to store the stream in.
     * @param fileSuffix The file suffix, jpg/png.
     */
    public PDXObjectImage(PDDocument doc, String fileSuffix)
    {
        super(doc);
        getCOSStream().setName(COSName.SUBTYPE, SUB_TYPE);
        suffix = fileSuffix;
    }

    /**
     * Create the correct thumbnail from the cos base.
     *
     * @param xobject The cos level xobject to create.
     *
     * @return a pdmodel xobject
     * @throws IOException If there is an error creating the xobject.
     */
    public static PDXObject createThumbnailXObject(COSBase xobject) throws IOException
    {
        return commonXObjectCreation(xobject, true);
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
            return (PDXObjectImage) PDXObject.createXObject(smask);
        }
    }

    /**
     * Add masked image to the given image.
     * 
     * @param baseImage the base image.
     * @return the masked image.
     * @throws IOException if something went wrong
     */
    protected BufferedImage applyMasks(BufferedImage baseImage) throws IOException
    {
        if (getImageMask())
        {
            return imageMask(baseImage);
        }
        if (getMask() != null)
        {
            return mask(baseImage);
        }
        PDXObjectImage smask = getSMaskImage();
        if (smask != null)
        {
            BufferedImage smaskBI = smask.getRGBImage();
            if (smaskBI != null)
            {
	            COSArray decodeArray = smask.getDecode();
	            CompositeImage compositeImage = new CompositeImage(baseImage, smaskBI);
	            BufferedImage rgbImage = compositeImage.createMaskedImage(decodeArray);
	            return rgbImage;
            }
            else
            {
            	// this may happen if the smask is somehow broken, e.g. unsupported filter
                LOG.warn("masking getRGBImage returned NULL");
            }
        }
        return baseImage;
    }

    /**
     * Determines is the XObject has any mask.
     * 
     * @return true if the XObkject has any mask
     * @throws IOException if something went wrong
     */
    protected boolean hasMask() throws IOException
    {
        return getImageMask() || getMask() != null || getSMaskImage() != null;
    }

    private BufferedImage imageMask(BufferedImage baseImage) throws IOException
    {
        BufferedImage stencilMask = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = (Graphics2D) stencilMask.getGraphics();
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

        graphics.fillRect(0, 0, baseImage.getWidth(), baseImage.getHeight());
        // assume default values ([0,1]) for the DecodeArray
        // TODO DecodeArray == [1,0]
        graphics.setComposite(AlphaComposite.DstIn);
        graphics.drawImage(baseImage, null, 0, 0);
        graphics.dispose();
        return stencilMask;
    }

    private BufferedImage mask(BufferedImage baseImage) throws IOException
    {
        COSBase mask = getMask();
        if (mask instanceof COSStream)
        {
            PDXObjectImage maskImageRef = (PDXObjectImage) PDXObject.createXObject((COSStream) mask);
            BufferedImage maskImage = maskImageRef.getRGBImage();
            if (maskImage == null)
            {
                LOG.warn("masking getRGBImage returned NULL");
                return baseImage;
            }

            BufferedImage newImage = new BufferedImage(maskImage.getWidth(), maskImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = (Graphics2D) newImage.getGraphics();
            graphics.drawImage(baseImage, 0, 0, maskImage.getWidth(), maskImage.getHeight(), 0, 0,
                    baseImage.getWidth(), baseImage.getHeight(), null);
            graphics.setComposite(AlphaComposite.DstIn);
            graphics.drawImage(maskImage, null, 0, 0);
            graphics.dispose();
            return newImage;
        }
        else
        {
            // TODO Colour key masking
            LOG.warn("Colour key masking isn't supported");
            return baseImage;
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
            if (out != null)
            {
                out.close();
            }
        }
    }

    /**
     * Writes the image to a file with the filename + an appropriate
     * suffix, like "Image.jpg".
     * The suffix is automatically set by the
     * @param file the file
     * @throws IOException When somethings wrong with the corresponding file.
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
            if (out != null)
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
        return getCOSStream().getInt(COSName.HEIGHT, -1);
    }

    /**
     * Set the height of the image.
     *
     * @param height The height of the image.
     */
    public void setHeight(int height)
    {
        getCOSStream().setInt(COSName.HEIGHT, height);
    }

    /**
     * Get the width of the image.
     *
     * @return The width of the image.
     */
    public int getWidth()
    {
        return getCOSStream().getInt(COSName.WIDTH, -1);
    }

    /**
     * Set the width of the image.
     *
     * @param width The width of the image.
     */
    public void setWidth(int width)
    {
        getCOSStream().setInt(COSName.WIDTH, width);
    }

    /**
     * The bits per component of this image.  This will return -1 if one has not
     * been set.
     *
     * @return The number of bits per component.
     */
    public int getBitsPerComponent()
    {
        return getCOSStream().getInt(COSName.BITS_PER_COMPONENT, COSName.BPC, -1);
    }

    /**
     * Set the number of bits per component.
     *
     * @param bpc The number of bits per component.
     */
    public void setBitsPerComponent(int bpc)
    {
        getCOSStream().setInt(COSName.BITS_PER_COMPONENT, bpc);
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
        COSBase cs = getCOSStream().getDictionaryObject(COSName.COLORSPACE, COSName.CS);
        PDColorSpace retval = null;
        if (cs != null)
        {
            retval = PDColorSpaceFactory.createColorSpace(cs);
            if (retval == null)
            {
                LOG.info("About to return NULL from createColorSpace branch");
            }
        }
        else
        {
            // there are some cases where the 'required' CS value is not present
            // but we know that it will be grayscale for a CCITT filter.
            COSBase filter = getCOSStream().getDictionaryObject(COSName.FILTER);
            if (COSName.CCITTFAX_DECODE.equals(filter) || COSName.CCITTFAX_DECODE_ABBREVIATION.equals(filter))
            {
                retval = new PDDeviceGray();
            }
            else if (COSName.JBIG2_DECODE.equals(filter))
            {
                retval = new PDDeviceGray();
            }
            else if (getImageMask())
            {
                // image is a stencil mask -> use DeviceGray
                retval = new PDDeviceGray();
            }
            else
            {
                LOG.info("About to return NULL from unhandled branch." + " filter = " + filter);
            }
        }
        return retval;
    }

    /**
     * This will set the color space for this image.
     *
     * @param cs The color space for this image.
     */
    public void setColorSpace(PDColorSpace cs)
    {
        COSBase base = null;
        if (cs != null)
        {
            base = cs.getCOSObject();
        }
        getCOSStream().setItem(COSName.COLORSPACE, base);
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
        return getCOSStream().getBoolean(COSName.IMAGE_MASK, false);
    }

    /**
     * Set the current non stroking colorstate. It'll be used to create stencil masked images.
     * 
     * @param stencilColorValue The non stroking colorstate
     */
    public void setStencilColor(PDColorState stencilColorValue)
    {
        stencilColor = stencilColorValue;
    }

    /**
     * Returns the non stroking colorstate to be used to create stencil makes images.
     * 
     * @return The current non stroking colorstate.
     */
    public PDColorState getStencilColor()
    {
        return stencilColor;
    }

    /**
     * Returns the Decode Array of an XObjectImage.
     * @return the decode array
     */
    public COSArray getDecode()
    {
        COSBase decode = getCOSStream().getDictionaryObject(COSName.DECODE);
        if (decode != null && decode instanceof COSArray)
        {
            return (COSArray) decode;
        }
        return null;
    }

    /**
     * Returns the optional mask of a XObjectImage if there is one.
     *
     * @return The mask otherwise null.
     */
    public COSBase getMask()
    {
        COSBase mask = getCOSStream().getDictionaryObject(COSName.MASK);
        if (mask != null)
        {
            return mask;
        }
        return null;
    }

}
