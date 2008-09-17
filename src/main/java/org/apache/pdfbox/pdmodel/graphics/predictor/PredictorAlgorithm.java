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
package org.apache.pdfbox.pdmodel.graphics.predictor;

import java.util.Random;

/**
 * Implements different PNG predictor algorithms that is used in PDF files.
 *
 * @author xylifyx@yahoo.co.uk
 * @version $Revision: 1.4 $
 * @see <a href="http://www.w3.org/TR/PNG-Filters.html">PNG Filters</a>
 */
public abstract class PredictorAlgorithm
{
    private int width;

    private int height;

    private int bpp;

    /**
     * check that buffer sizes matches width,height,bpp. This implementation is
     * used by most of the filters, but not Uptimum.
     *
     * @param src The source buffer.
     * @param dest The destination buffer.
     */
    public void checkBufsiz(byte[] src, byte[] dest)
    {
        if (src.length != dest.length)
        {
            throw new IllegalArgumentException("src.length != dest.length");
        }
        if (src.length != getWidth() * getHeight() * getBpp())
        {
            throw new IllegalArgumentException(
                    "src.length != width * height * bpp");
        }
    }

    /**
     * encode line of pixel data in src from srcOffset and width*bpp bytes
     * forward, put the decoded bytes into dest.
     *
     * @param src
     *            raw image data
     * @param dest
     *            encoded data
     * @param srcDy
     *            byte offset between lines
     * @param srcOffset
     *            beginning of line data
     * @param destDy
     *            byte offset between lines
     * @param destOffset
     *            beginning of line data
     */
    public abstract void encodeLine(byte[] src, byte[] dest, int srcDy,
            int srcOffset, int destDy, int destOffset);

    /**
     * decode line of pixel data in src from src_offset and width*bpp bytes
     * forward, put the decoded bytes into dest.
     *
     * @param src
     *            encoded image data
     * @param dest
     *            raw data
     * @param srcDy
     *            byte offset between lines
     * @param srcOffset
     *            beginning of line data
     * @param destDy
     *            byte offset between lines
     * @param destOffset
     *            beginning of line data
     */
    public abstract void decodeLine(byte[] src, byte[] dest, int srcDy,
            int srcOffset, int destDy, int destOffset);

    /**
     * Simple command line program to test the algorithm.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args)
    {
        Random rnd = new Random();
        int width = 5;
        int height = 5;
        int bpp = 3;
        byte[] raw = new byte[width * height * bpp];
        rnd.nextBytes(raw);
        System.out.println("raw:   ");
        dump(raw);
        for (int i = 10; i < 15; i++)
        {
            byte[] decoded = new byte[width * height * bpp];
            byte[] encoded = new byte[width * height * bpp];

            PredictorAlgorithm filter = PredictorAlgorithm.getFilter(i);
            filter.setWidth(width);
            filter.setHeight(height);
            filter.setBpp(bpp);
            filter.encode(raw, encoded);
            filter.decode(encoded, decoded);
            System.out.println(filter.getClass().getName());
            dump(decoded);
        }
    }

    /**
     * Get the left pixel from the buffer.
     *
     * @param buf The buffer.
     * @param offset The offset into the buffer.
     * @param dy The dy value.
     * @param x The x value.
     *
     * @return The left pixel.
     */
    public int leftPixel(byte[] buf, int offset, int dy, int x)
    {
        return x >= getBpp() ? buf[offset + x - getBpp()] : 0;
    }

    /**
     * Get the above pixel from the buffer.
     *
     * @param buf The buffer.
     * @param offset The offset into the buffer.
     * @param dy The dy value.
     * @param x The x value.
     *
     * @return The above pixel.
     */
    public int abovePixel(byte[] buf, int offset, int dy, int x)
    {
        return offset >= dy ? buf[offset + x - dy] : 0;
    }

    /**
     * Get the above-left pixel from the buffer.
     *
     * @param buf The buffer.
     * @param offset The offset into the buffer.
     * @param dy The dy value.
     * @param x The x value.
     *
     * @return The above-left pixel.
     */
    public int aboveLeftPixel(byte[] buf, int offset, int dy, int x)
    {
        return offset >= dy && x >= getBpp() ? buf[offset + x - dy - getBpp()]
                : 0;
    }

    /**
     * Simple helper to print out a buffer.
     *
     * @param raw The bytes to print out.
     */
    private static void dump(byte[] raw)
    {
        for (int i = 0; i < raw.length; i++)
        {
            System.out.print(raw[i] + " ");
        }
        System.out.println();
    }

    /**
     * @return Returns the bpp.
     */
    public int getBpp()
    {
        return bpp;
    }

    /**
     * @param newBpp
     *            The bpp to set.
     */
    public void setBpp(int newBpp)
    {
        bpp = newBpp;
    }

    /**
     * @return Returns the height.
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param newHeight
     *            The height to set.
     */
    public void setHeight(int newHeight)
    {
        height = newHeight;
    }

    /**
     * @return Returns the width.
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param newWidth
     *            The width to set.
     */
    public void setWidth(int newWidth)
    {
        this.width = newWidth;
    }


    /**
     * encode a byte array full of image data using the filter that this object
     * implements.
     *
     * @param src
     *            buffer
     * @param dest
     *            buffer
     */
    public void encode(byte[] src, byte[] dest)
    {
        checkBufsiz(dest, src);
        int dy = getWidth()*getBpp();
        for (int y = 0; y < height; y++)
        {
            int yoffset = y * dy;
            encodeLine(src, dest, dy, yoffset, dy, yoffset);
        }
    }

    /**
     * decode a byte array full of image data using the filter that this object
     * implements.
     *
     * @param src
     *            buffer
     * @param dest
     *            buffer
     */
    public void decode(byte[] src, byte[] dest)
    {
        checkBufsiz(src, dest);
        int dy = width * bpp;
        for (int y = 0; y < height; y++)
        {
            int yoffset = y * dy;
            decodeLine(src, dest, dy, yoffset, dy, yoffset);
        }
    }

    /**
     * @param predictor
     *            <ul>
     *            <li>1 No prediction (the default value)
     *            <li>2 TIFF Predictor 2
     *            <li>10 PNG prediction (on encoding, PNG None on all rows)
     *            <li>11 PNG prediction (on encoding, PNG Sub on all rows)
     *            <li>12 PNG prediction (on encoding, PNG Up on all rows)
     *            <li>13 PNG prediction (on encoding, PNG Average on all rows)
     *            <li>14 PNG prediction (on encoding, PNG Paeth on all rows)
     *            <li>15 PNG prediction (on encoding, PNG optimum)
     *            </ul>
     *
     * @return The predictor class based on the predictor code.
     */
    public static PredictorAlgorithm getFilter(int predictor)
    {
        PredictorAlgorithm filter;
        switch (predictor)
        {
            case 10:
                filter = new None();
                break;
            case 11:
                filter = new Sub();
                break;
            case 12:
                filter = new Up();
                break;
            case 13:
                filter = new Average();
                break;
            case 14:
                filter = new Paeth();
                break;
            case 15:
                filter = new Optimum();
                break;
            default:
                filter = new None();
        }
        return filter;
    }
}
