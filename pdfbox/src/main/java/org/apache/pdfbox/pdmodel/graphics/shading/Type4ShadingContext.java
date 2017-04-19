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

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT PaintContext for Gouraud Triangle Mesh (Type 4) shading.
 *
 * @author Tilman Hausherr
 * @author Shaola Ren
 */
class Type4ShadingContext extends GouraudShadingContext
{
    private static final Log LOG = LogFactory.getLog(Type4ShadingContext.class);
    private final int bitsPerFlag;

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param cm the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     */
    Type4ShadingContext(PDShadingType4 shading, ColorModel cm, AffineTransform xform,
                               Matrix matrix, Rectangle deviceBounds) throws IOException
    {
        super(shading, cm, xform, matrix);
        LOG.debug("Type4ShadingContext");

        bitsPerFlag = shading.getBitsPerFlag();
        //TODO handle cases where bitperflag isn't 8
        LOG.debug("bitsPerFlag: " + bitsPerFlag);
        setTriangleList(collectTriangles(shading, xform, matrix));
        createPixelTable(deviceBounds);
    }

    private List<ShadedTriangle> collectTriangles(PDShadingType4 freeTriangleShadingType, AffineTransform xform, Matrix matrix)
            throws IOException
    {
        COSDictionary dict = freeTriangleShadingType.getCOSObject();
        PDRange rangeX = freeTriangleShadingType.getDecodeForParameter(0);
        PDRange rangeY = freeTriangleShadingType.getDecodeForParameter(1);
        PDRange[] colRange = new PDRange[numberOfColorComponents];
        for (int i = 0; i < numberOfColorComponents; ++i)
        {
            colRange[i] = freeTriangleShadingType.getDecodeForParameter(2 + i);
        }
        List<ShadedTriangle> list = new ArrayList<>();
        long maxSrcCoord = (long) Math.pow(2, bitsPerCoordinate) - 1;
        long maxSrcColor = (long) Math.pow(2, bitsPerColorComponent) - 1;
        COSStream stream = (COSStream) dict;

        try (ImageInputStream mciis = new MemoryCacheImageInputStream(stream.createInputStream()))
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
                Vertex p0, p1, p2;
                Point2D[] ps;
                float[][] cs;
                int lastIndex;
                try
                {
                    switch (flag)
                    {
                        case 0:
                            p0 = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY, colRange,
                                            matrix, xform);
                            flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
                            if (flag != 0)
                            {
                                LOG.error("bad triangle: " + flag);
                            }
                            p1 = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY, colRange,
                                            matrix, xform);
                            mciis.readBits(bitsPerFlag);
                            if (flag != 0)
                            {
                                LOG.error("bad triangle: " + flag);
                            }
                            p2 = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY, colRange,
                                            matrix, xform);
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
                                LOG.error("broken data stream: " + list.size());
                            }
                            else
                            {
                                ShadedTriangle preTri = list.get(lastIndex);
                                p2 = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY,
                                                colRange, matrix, xform);
                                ps = new Point2D[] { flag == 1 ? preTri.corner[1] : preTri.corner[0],
                                                     preTri.corner[2],
                                                     p2.point };
                                cs = new float[][] { flag == 1 ? preTri.color[1] : preTri.color[0],
                                                     preTri.color[2],
                                                     p2.color };
                                list.add(new ShadedTriangle(ps, cs));
                                flag = (byte) (mciis.readBits(bitsPerFlag) & 3);
                            }
                            break;
                        default:
                            LOG.warn("bad flag: " + flag);
                            break;
                    }
                }
                catch (EOFException ex)
                {
                    eof = true;
                }
            }
        }
        return list;
    }
}
