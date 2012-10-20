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

/**
 * We can use raw on the right hand side of
 * the decoding formula because it is already decoded.
 *
 * <code>average(i,j) = raw(i,j) + (raw(i-1,j)+raw(i,j-1)/2</code>
 *
 * decoding
 *
 * <code>raw(i,j) = avarage(i,j) - (raw(i-1,j)+raw(i,j-1)/2</code>
 *
 * @author xylifyx@yahoo.co.uk
 * @version $Revision: 1.3 $
 */
public class Average extends PredictorAlgorithm
{
    /**
     * Not an optimal version, but close to the def.
     *
     * {@inheritDoc}
     */
    public void encodeLine(byte[] src, byte[] dest, int srcDy, int srcOffset,
            int destDy, int destOffset)
    {
        int bpl = getWidth() * getBpp();
        for (int x = 0; x < bpl; x++)
        {
            dest[x + destOffset] = (byte) (src[x + srcOffset] - ((leftPixel(
                    src, srcOffset, srcDy, x) + abovePixel(src, srcOffset,
                    srcDy, x)) >>> 2));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void decodeLine(byte[] src, byte[] dest, int srcDy, int srcOffset,
            int destDy, int destOffset)
    {
        int bpl = getWidth() * getBpp();
        for (int x = 0; x < bpl; x++)
        {
            dest[x + destOffset] = (byte) (src[x + srcOffset] + ((leftPixel(
                    dest, destOffset, destDy, x) + abovePixel(dest,
                    destOffset, destDy, x)) >>> 2));
        }
    }
}
