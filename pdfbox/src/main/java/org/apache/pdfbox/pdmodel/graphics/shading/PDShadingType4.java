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
package org.apache.pdfbox.pdmodel.graphics.shading;

import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * Resources for a shading type 4 (Free-Form Gouraud-Shaded Triangle Mesh).
 */
public class PDShadingType4 extends PDTriangleBasedShadingType
{
    private static final Logger LOG = LogManager.getLogger(PDShadingType4.class);

    /**
     * Constructor using the given shading dictionary.
     *
     * @param shadingDictionary the dictionary for this shading
     */
    public PDShadingType4(COSDictionary shadingDictionary)
    {
        super(shadingDictionary);
    }

    @Override
    public int getShadingType()
    {
        return PDShading.SHADING_TYPE4;
    }

    /**
     * The bits per flag of this shading. This will return -1 if one has not
     * been set.
     *
     * @return The number of bits per flag.
     */
    public int getBitsPerFlag()
    {
        return getCOSObject().getInt(COSName.BITS_PER_FLAG, -1);
    }

    /**
     * Set the number of bits per flag.
     *
     * @param bitsPerFlag the number of bits per flag
     */
    public void setBitsPerFlag(int bitsPerFlag)
    {
        getCOSObject().setInt(COSName.BITS_PER_FLAG, bitsPerFlag);
    }

    @Override
    public Paint toPaint(Matrix matrix)
    {
        return new Type4ShadingPaint(this, matrix);
    }
    
    @SuppressWarnings("squid:S1166")
    @Override
    List<ShadedTriangle> collectTriangles(AffineTransform xform, Matrix matrix)
            throws IOException
    {
        int bitsPerFlag = getBitsPerFlag();
        COSDictionary dict = getCOSObject();
        if (!(dict instanceof COSStream))
        {
            return Collections.emptyList();
        }
        PDRange rangeX = getDecodeForParameter(0);
        PDRange rangeY = getDecodeForParameter(1);
        if (rangeX == null || rangeY == null ||
            Float.compare(rangeX.getMin(), rangeX.getMax()) == 0 ||
            Float.compare(rangeY.getMin(), rangeY.getMax()) == 0)
        {
            return Collections.emptyList();
        }
        PDRange[] colRange = new PDRange[getNumberOfColorComponents()];
        for (int i = 0; i < colRange.length; ++i)
        {
            colRange[i] = getDecodeForParameter(2 + i);
            if (colRange[i] == null)
            {
                throw new IOException("Range missing in shading /Decode entry");
            }
        }
        List<ShadedTriangle> list = new ArrayList<>();
        long maxSrcCoord = (long) Math.pow(2, getBitsPerCoordinate()) - 1;
        long maxSrcColor = (long) Math.pow(2, getBitsPerComponent()) - 1;

        // MemoryCacheImageInputStream doesn't close the wrapped stream
        try (InputStream imageStream = ((COSStream) dict).createInputStream())
        {
            try (ImageInputStream mciis = new MemoryCacheImageInputStream(imageStream))
            {
                byte flag = (byte) 0;
                try
                {
                    flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
                }
                catch (EOFException ex)
                {
                    LOG.error(ex);
                }

                boolean eof = false;
                while (!eof)
                {
                    Vertex p0;
                    Vertex p1;
                    Vertex p2;
                    Point2D[] ps;
                    float[][] cs;
                    int lastIndex;
                    try
                    {
                        switch (flag)
                        {
                        case 0:
                            p0 = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY,
                                    colRange, matrix, xform);
                            flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
                            if (flag != 0)
                            {
                                LOG.error("bad triangle: {}", flag);
                            }
                            p1 = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY,
                                    colRange, matrix, xform);
                            mciis.readBits(bitsPerFlag);
                            if (flag != 0)
                            {
                                LOG.error("bad triangle: {}", flag);
                            }
                            p2 = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY,
                                    colRange, matrix, xform);
                            ps = new Point2D[] { p0.point, p1.point, p2.point };
                            cs = new float[][] { p0.color, p1.color, p2.color };
                                list.add(new ShadedTriangle(ps, cs));
                                flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
                                break;
                            case 1:
                            case 2:
                                lastIndex = list.size() - 1;
                                if (lastIndex < 0)
                                {
                                    LOG.error("broken data stream: {}", list.size());
                                }
                                else
                                {
                                    ShadedTriangle preTri = list.get(lastIndex);
                                    p2 = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY,
                                            colRange, matrix, xform);
                                    ps = new Point2D[] {
                                            flag == 1 ? preTri.corner[1] : preTri.corner[0],
                                            preTri.corner[2], p2.point };
                                    cs = new float[][] {
                                            flag == 1 ? preTri.color[1] : preTri.color[0],
                                            preTri.color[2], p2.color };
                                    list.add(new ShadedTriangle(ps, cs));
                                    flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
                                }
                                break;
                            default:
                                LOG.warn("bad flag: {}", flag);
                                break;
                            }
                        }
                        catch (EOFException ex)
                        {
                            eof = true;
                    }
                }
            }
        }
        return list;
    }
}
