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
 * The sub algorithm.
 * 
 * <code>Sub(i,j) = Raw(i,j) - Raw(i-1,j)</code>
 * 
 * <code>Raw(i,j) = Sub(i,j) + Raw(i-1,j)</code>
 * 
 * @author xylifyx@yahoo.co.uk
 * @version $Revision: 1.3 $
 */
public class Sub extends PredictorAlgorithm 
{
    /**
     * {@inheritDoc}
     */
    public void encodeLine(byte[] src, byte[] dest, int srcDy, int srcOffset,
            int destDy, int destOffset) 
    {
        int bpl = getWidth()*getBpp();
        int bpp = getBpp();
        // case: x < bpp
        for (int x = 0; x < bpl && x < bpp; x++) 
        {
            dest[x + destOffset] = src[x + srcOffset];
        }
        // otherwise
        for (int x = getBpp(); x < bpl; x++) 
        {
            dest[x + destOffset] = (byte) (src[x + srcOffset] - src[x
                    + srcOffset - bpp]);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void decodeLine(byte[] src, byte[] dest, int srcDy, int srcOffset,
            int destDy, int destOffset) 
    {
        int bpl = getWidth()*getBpp();
        int bpp = getBpp();
        // case: x < bpp
        for (int x = 0; x < bpl && x < bpp; x++) 
        {
            dest[x + destOffset] = src[x + srcOffset];
        }
        // otherwise
        for (int x = getBpp(); x < bpl; x++) 
        {
            dest[x + destOffset] = (byte) (src[x + srcOffset] + dest[x
                    + destOffset - bpp]);
        }
    }
}
