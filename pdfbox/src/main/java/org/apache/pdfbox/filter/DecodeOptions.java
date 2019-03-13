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
package org.apache.pdfbox.filter;

import java.awt.Rectangle;

/**
 * Options that may be passed to a Filter to request special handling when decoding the stream.
 * Filters may not honor some or all of the specified options, and so callers should check the
 * honored flag if further processing relies on the options being used.
 */
public class DecodeOptions
{
    /**
     * Default decode options. The honored flag for this instance is always true, as it represents
     * the default behavior.
     */
    public static final DecodeOptions DEFAULT = new FinalDecodeOptions(true);

    private Rectangle sourceRegion = null;
    private int subsamplingX = 1, subsamplingY = 1, subsamplingOffsetX = 0, subsamplingOffsetY = 0;
    private boolean filterSubsampled = false;

    /**
     * Constructs an empty DecodeOptions instance
     */
    public DecodeOptions()
    {
        // this constructor is intentionally left empty
    }

    /**
     * Constructs an instance specifying the region of the image that should be decoded. The actual
     * region will be clipped to the dimensions of the image.
     *
     * @param sourceRegion Region of the source image that should be decoded
     */
    public DecodeOptions(Rectangle sourceRegion)
    {
        this.sourceRegion = sourceRegion;
    }

    /**
     * Constructs an instance specifying the region of the image that should be decoded. The actual
     * region will be clipped to the dimensions of the image.
     *
     * @param x x-coordinate of the top-left corner of the region to be decoded
     * @param y y-coordinate of the top-left corner of the region to be decoded
     * @param width Width of the region to be decoded
     * @param height Height of the region to be decoded
     */
    public DecodeOptions(int x, int y, int width, int height)
    {
        this(new Rectangle(x, y, width, height));
    }

    /**
     * Constructs an instance specifying the image should be decoded using subsampling. The
     * subsampling will be the same for the X and Y axes.
     *
     * @param subsampling The number of rows and columns to advance in the source for each pixel in
     * the decoded image.
     */
    public DecodeOptions(int subsampling)
    {
        subsamplingX = subsampling;
        subsamplingY = subsampling;
    }

    /**
     * When decoding an image, the part of the image that should be decoded, or null if the entire
     * image is needed.
     *
     * @return The source region to decode, or null if the entire image should be decoded
     */
    public Rectangle getSourceRegion()
    {
        return sourceRegion;
    }

    /**
     * Sets the region of the source image that should be decoded. The region will be clipped to the
     * dimensions of the source image. Setting this value to null will result in the entire image
     * being decoded.
     *
     * @param sourceRegion The source region to decode, or null if the entire image should be
     * decoded.
     */
    public void setSourceRegion(Rectangle sourceRegion)
    {
        this.sourceRegion = sourceRegion;
    }

    /**
     * When decoding an image, the number of columns to advance in the source for every pixel
     * decoded.
     *
     * @return The x-axis subsampling value
     */
    public int getSubsamplingX()
    {
        return subsamplingX;
    }

    /**
     * Sets the number of columns to advance in the source for every pixel decoded
     *
     * @param ssX The x-axis subsampling value
     */
    public void setSubsamplingX(int ssX)
    {
        this.subsamplingX = ssX;
    }

    /**
     * When decoding an image, the number of rows to advance in the source for every pixel decoded.
     *
     * @return The y-axis subsampling value
     */
    public int getSubsamplingY()
    {
        return subsamplingY;
    }

    /**
     * Sets the number of rows to advance in the source for every pixel decoded
     *
     * @param ssY The y-axis subsampling value
     */
    public void setSubsamplingY(int ssY)
    {
        this.subsamplingY = ssY;
    }

    /**
     * When decoding an image, the horizontal offset for subsampling
     *
     * @return The x-axis subsampling offset
     */
    public int getSubsamplingOffsetX()
    {
        return subsamplingOffsetX;
    }

    /**
     * Sets the horizontal subsampling offset for decoding images
     *
     * @param ssOffsetX The x-axis subsampling offset
     */
    public void setSubsamplingOffsetX(int ssOffsetX)
    {
        this.subsamplingOffsetX = ssOffsetX;
    }

    /**
     * When decoding an image, the vertical offset for subsampling
     *
     * @return The y-axis subsampling offset
     */
    public int getSubsamplingOffsetY()
    {
        return subsamplingOffsetY;
    }

    /**
     * Sets the vertical subsampling offset for decoding images
     *
     * @param ssOffsetY The y-axis subsampling offset
     */
    public void setSubsamplingOffsetY(int ssOffsetY)
    {
        this.subsamplingOffsetY = ssOffsetY;
    }

    /**
     * Flag used by the filter to specify if it performed subsampling.
     *
     * Some filters may be unable or unwilling to apply subsampling, and so the caller must check
     * this flag <b>after</b> decoding.
     *
     * @return True if the filter applied the options specified by this instance, false otherwise.
     */
    public boolean isFilterSubsampled()
    {
        return filterSubsampled;
    }

    /**
     * Used internally by filters to signal they have applied subsampling as requested by this
     * options instance.
     *
     * @param filterSubsampled Value specifying if the filter could meet the requested options.
     * Usually a filter will only call this with the value <code>true</code>, as the default value
     * for the flag is <code>false</code>.
     */
    void setFilterSubsampled(boolean filterSubsampled)
    {
        this.filterSubsampled = filterSubsampled;
    }

    /**
     * Helper class for reusable instances which may not be modified.
     */
    private static class FinalDecodeOptions extends DecodeOptions
    {
        FinalDecodeOptions(boolean filterSubsampled)
        {
            super.setFilterSubsampled(filterSubsampled);
        }

        @Override
        public void setSourceRegion(Rectangle sourceRegion)
        {
            throw new UnsupportedOperationException("This instance may not be modified.");
        }

        @Override
        public void setSubsamplingX(int ssX)
        {
            throw new UnsupportedOperationException("This instance may not be modified.");
        }

        @Override
        public void setSubsamplingY(int ssY)
        {
            throw new UnsupportedOperationException("This instance may not be modified.");
        }

        @Override
        public void setSubsamplingOffsetX(int ssOffsetX)
        {
            throw new UnsupportedOperationException("This instance may not be modified.");
        }

        @Override
        public void setSubsamplingOffsetY(int ssOffsetY)
        {
            throw new UnsupportedOperationException("This instance may not be modified.");
        }

        @Override
        void setFilterSubsampled(boolean filterSubsampled)
        {
            // Silently ignore the request.
        }
    }
}
