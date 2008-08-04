/**
 * Copyright (c) 2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.graphics.predictor;

/**
 * 
 * 
 * In an Uptimum encoded image, each line takes up width*bpp+1 bytes. The first
 * byte holds a number that signifies which algorithm encoded the line.
 * 
 * @author xylifyx@yahoo.co.uk
 * @version $Revision: 1.1 $
 */
public class Optimum extends PredictorAlgorithm 
{
    /**
     * {@inheritDoc}
     */
    public void checkBufsiz(byte[] filtered, byte[] raw) 
    {
        if (filtered.length != (getWidth() * getBpp() + 1) * getHeight()) 
        {

            throw new IllegalArgumentException(
                    "filtered.length != (width*bpp + 1) * height, "
                            + filtered.length + " "
                            + (getWidth() * getBpp() + 1) * getHeight()
                            + "w,h,bpp=" + getWidth() + "," + getHeight() + ","
                            + getBpp());
        }
        if (raw.length != getWidth() * getHeight() * getBpp()) 
        {
            throw new IllegalArgumentException(
                    "raw.length != width * height * bpp, raw.length="
                            + raw.length + " w,h,bpp=" + getWidth() + ","
                            + getHeight() + "," + getBpp());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void encodeLine(byte[] src, byte[] dest, int srcDy, int srcOffset,
            int destDy, int destOffset) 
    {
        throw new UnsupportedOperationException("encodeLine");
    }

    /**
     * {@inheritDoc}
     */
    public void decodeLine(byte[] src, byte[] dest, int srcDy, int srcOffset,
            int destDy, int destOffset) 
    {
        throw new UnsupportedOperationException("decodeLine");
    }

    /**
     * {@inheritDoc}
     */
    public void encode(byte[] src, byte[] dest) 
    {
        checkBufsiz(dest, src);
        throw new UnsupportedOperationException("encode");
    }

    /** 
     * Filter indexed by byte code.
     */
    PredictorAlgorithm[] filter = { new None(), new Sub(), new Up(), new Average(),
            new Paeth() };

    /**
     * {@inheritDoc}
     */
    public void setBpp(int bpp) 
    {
        super.setBpp(bpp);
        for (int i = 0; i < filter.length; i++)
        {
            filter[i].setBpp(bpp);
        }
    }
    /**
     * {@inheritDoc}
     */
    public void setHeight(int height) 
    {
        super.setHeight(height);
        for (int i = 0; i < filter.length; i++)
        {
            filter[i].setHeight(height);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setWidth(int width) 
    {
        super.setWidth(width);
        for (int i = 0; i < filter.length; i++)
        {
            filter[i].setWidth(width);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void decode(byte[] src, byte[] dest) 
    {
        checkBufsiz(src, dest);
        int bpl = getWidth() * getBpp();
        int srcDy = bpl + 1;
        for (int y = 0; y < getHeight(); y++) 
        {
            PredictorAlgorithm f = filter[src[y * srcDy]];
            int srcOffset = y * srcDy + 1;
            f.decodeLine(src, dest, srcDy, srcOffset, bpl, y * bpl);
        }
    }
}