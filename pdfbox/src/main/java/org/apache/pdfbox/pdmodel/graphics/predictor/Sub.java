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
