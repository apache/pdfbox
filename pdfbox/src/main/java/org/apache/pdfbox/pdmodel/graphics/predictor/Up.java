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
 * The up algorithm.
 *
 * <code>Up(i,j) = Raw(i,j) - Raw(i,j-1)</code>
 *
 * <code>Raw(i,j) = Up(i,j) + Raw(i,j-1)</code>
 *
 * @author xylifyx@yahoo.co.uk
 * @version $Revision: 1.3 $
 */
public class Up extends PredictorAlgorithm
{
    /**
     * {@inheritDoc}
     */
    public void encodeLine(byte[] src, byte[] dest, int srcDy, int srcOffset,
            int destDy, int destOffset)
    {
        int bpl = getWidth()*getBpp();
        // case: y = 0;
        if (srcOffset - srcDy < 0)
        {
            if (0 < getHeight())
            {
                for (int x = 0; x < bpl; x++)
                {
                    dest[destOffset + x] = src[srcOffset + x];
                }
            }
        }
        else
        {
            for (int x = 0; x < bpl; x++)
            {
                dest[destOffset + x] = (byte) (src[srcOffset + x] - src[srcOffset
                        + x - srcDy]);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void decodeLine(byte[] src, byte[] dest, int srcDy, int srcOffset,
            int destDy, int destOffset)
    {
        // case: y = 0;
        int bpl = getWidth()*getBpp();
        if (destOffset - destDy < 0)
        {
            if (0 < getHeight())
            {
                for (int x = 0; x < bpl; x++)
                {
                    dest[destOffset + x] = src[srcOffset + x];
                }
            }
        }
        else
        {
            for (int x = 0; x < bpl; x++)
            {
                dest[destOffset + x] = (byte) (src[srcOffset + x] + dest[destOffset
                        + x - destDy]);
            }
        }
    }
}
