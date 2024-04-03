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
package org.apache.pdfbox.pdmodel.graphics.image;

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.filter.DecodeOptions;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;

/**
 * An image in a PDF document.
 *
 * @author John Hewson
 */
public interface PDImage extends COSObjectable
{
    /**
     * Returns the content of this image as an AWT buffered image with an (A)RGB color space. The size of the returned
     * image is the larger of the size of the image itself or its mask.
     * 
     * @return content of this image as a buffered image.
     * @throws IOException if the buffered image could not be created
     */
    BufferedImage getImage() throws IOException;

    /**
     * Return the image data as WritableRaster. You should consult the PDColorSpace returned by {@link #getColorSpace()}
     * to know how to interpret the data in this WritableRaster.
     *
     * Use this if e.g. want access to the raw color information of a
     * {@link org.apache.pdfbox.pdmodel.graphics.color.PDDeviceN} image.
     *
     * @return the raw writable raster for this image
     * @throws IOException if the buffered raw writable raster could not be created
     */
    WritableRaster getRawRaster() throws IOException;

    /**
     * Try to get the raw image as AWT buffered image with it's original colorspace. No color conversion is performed.
     *
     * You could use the returned BufferedImage for draw operations. But this would be very slow as the color conversion
     * would happen on demand. You rather should use {@link #getImage()} for that.
     *
     * This method returns null if it is not possible to map the underlying colorspace into a java.awt.ColorSpace.
     *
     * Use this method if you want to extract the image without loosing any color information, as no color conversion
     * will be performed.
     *
     * You can alwoys use {@link #getRawRaster()}, if you want to access the raw data even if no matching
     * java.awt.ColorSpace exists
     *
     * @return the raw image with a java.awt.ColorSpace or null
     * @throws IOException if the raw image could not be created
     */
    BufferedImage getRawImage() throws IOException;

    /**
     * Returns the content of this image as an AWT buffered image with an (A)RGB colored space. Only the subregion
     * specified is rendered, and is subsampled by advancing the specified amount of rows and columns in the source
     * image for every resulting pixel.
     *
     * Note that unlike {@link PDImage#getImage() the unparameterized version}, this method does not cache the resulting
     * image.
     * 
     * @param region The region of the source image to get, or null if the entire image is needed. The actual region
     * will be clipped to the dimensions of the source image.
     * @param subsampling The amount of rows and columns to advance for every output pixel, a value of 1 meaning every
     * pixel will be read
     * @return subsampled content of the requested subregion as a buffered image.
     * @throws IOException if the buffered image could not be created
     */
    BufferedImage getImage(Rectangle region, int subsampling) throws IOException;

    /**
     * Returns an ARGB image filled with the given paint and using this image as a mask.
     * @param paint the paint to fill the visible portions of the image with
     * @return a masked image filled with the given paint
     * @throws IOException if the image cannot be read
     * @throws IllegalStateException if the image is not a stencil.
     */
    BufferedImage getStencilImage(Paint paint) throws IOException;
    
    /**
     * Returns an InputStream containing the image data, irrespective of whether this is an
     * inline image or an image XObject.
     * @return Decoded stream
     * @throws IOException if the data could not be read.
     */
    InputStream createInputStream() throws IOException;

    /**
     * Returns an InputStream containing the image data, irrespective of whether this is an
     * inline image or an image XObject. The given filters will not be decoded.
     * @param stopFilters A list of filters to stop decoding at.
     * @return Decoded stream
     * @throws IOException if the data could not be read.
     */
    InputStream createInputStream(List<String> stopFilters) throws IOException;

    /**
     * Returns an InputStream, passing additional options to each filter. As a side effect, the
     * filterSubsampled flag is set in {@link DecodeOptions}.
     *
     * @param options Additional decoding options passed to the filters used
     * @return Decoded stream
     * @throws IOException if the data could not be read
     */
    InputStream createInputStream(DecodeOptions options) throws IOException;

    /**
     * Returns true if the image has no data.
     * 
     * @return true if the image has no data
     */
    boolean isEmpty();

    /**
     * Returns true if the image is a stencil mask.
     * 
     * @return true if the image is a stencil mask
     */
    boolean isStencil();

    /**
     * Sets whether or not the image is a stencil.
     * This corresponds to the {@code ImageMask} entry in the image stream's dictionary.
     * @param isStencil True to make the image a stencil.
     */
    void setStencil(boolean isStencil);

    /**
     * Returns bits per component of this image, or -1 if one has not been set.
     * 
     * @return bits per component of this image or -1
     */
    int getBitsPerComponent();

    /**
     * Set the number of bits per component.
     * @param bitsPerComponent The number of bits per component.
     */
    void setBitsPerComponent(int bitsPerComponent);

    /**
     * Returns the image's color space.
     * 
     * @return the image's color space
     * @throws IOException If there is an error getting the color space.
     */
    PDColorSpace getColorSpace() throws IOException;

    /**
     * Sets the color space for this image.
     * @param colorSpace The color space for this image.
     */
    void setColorSpace(PDColorSpace colorSpace);

    /**
     * Returns height of this image, or -1 if one has not been set.
     * 
     * @return height of this image or -1
     */
    int getHeight();

    /**
     * Sets the height of the image. This is for internal PDFBox usage and not to set the size of
     * the image on the page.
     *
     * @param height The height of the image.
     */
    void setHeight(int height);

    /**
     * Returns the width of this image, or -1 if one has not been set.
     * 
     * @return width of this image or -1
     */
    int getWidth();

    /**
     * Sets the width of the image. This is for internal PDFBox usage and not to set the size of
     * the image on the page.
     *
     * @param width The width of the image.
     */
    void setWidth(int width);

    /**
     * Sets the decode array.
     * @param decode  the new decode array.
     */
    void setDecode(COSArray decode);

    /**
     * Returns the decode array.
     * 
     * @return the decode array
     */
    COSArray getDecode();

    /**
     * Returns true if the image should be interpolated when rendered.
     * 
     * @return true if the image should be interpolated when rendered
     */
    boolean getInterpolate();


    /**
     * Sets the Interpolate flag, true for high-quality image scaling.
     * 
     * @param value true for high-quality image scaling
     */
    void setInterpolate(boolean value);

    /**
     * Returns the suffix for this image type, e.g. "jpg"
     * 
     * @return the suffix for this image type
     */
    String getSuffix();
    
    /**
     * Convert this image to a COS object.
     *
     * @return The cos object that matches this image object.
     */
    @Override
    COSDictionary getCOSObject();
}
