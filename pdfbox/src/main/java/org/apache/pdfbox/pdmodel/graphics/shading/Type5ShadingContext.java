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
 * AWT PaintContext for Gouraud Triangle Lattice (Type 5) shading.
 *
 * @author Tilman Hausherr
 * @author Shaola Ren
 */
class Type5ShadingContext extends GouraudShadingContext
{
    private static final Log LOG = LogFactory.getLog(Type5ShadingContext.class);

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param cm the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @throws IOException if something went wrong
     */
    Type5ShadingContext(PDShadingType5 shading, ColorModel cm, AffineTransform xform,
                               Matrix matrix, Rectangle deviceBounds) throws IOException
    {
        super(shading, cm, xform, matrix);

        LOG.debug("Type5ShadingContext");

        setTriangleList(collectTriangles(shading, xform, matrix));
        createPixelTable(deviceBounds);
    }

    private List<ShadedTriangle> collectTriangles(PDShadingType5 latticeTriangleShadingType,
            AffineTransform xform, Matrix matrix) throws IOException
    {
        COSDictionary cosDictionary = latticeTriangleShadingType.getCOSObject();
        PDRange rangeX = latticeTriangleShadingType.getDecodeForParameter(0);
        PDRange rangeY = latticeTriangleShadingType.getDecodeForParameter(1);
        int numPerRow = latticeTriangleShadingType.getVerticesPerRow();
        PDRange[] colRange = new PDRange[numberOfColorComponents];
        for (int i = 0; i < numberOfColorComponents; ++i)
        {
            colRange[i] = latticeTriangleShadingType.getDecodeForParameter(2 + i);
        }
        List<Vertex> vlist = new ArrayList<Vertex>();
        long maxSrcCoord = (long) Math.pow(2, bitsPerCoordinate) - 1;
        long maxSrcColor = (long) Math.pow(2, bitsPerColorComponent) - 1;
        COSStream cosStream = (COSStream) cosDictionary;

        ImageInputStream mciis = new MemoryCacheImageInputStream(cosStream.createInputStream());
        try
        {
            while (true)
            {
                Vertex p;
                try
                {
                    p = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY, colRange, matrix, xform);
                    vlist.add(p);
                }
                catch (EOFException ex)
                {
                    break;
                }
            }
        }
        finally
        {
            mciis.close();
        }
        int sz = vlist.size(), rowNum = sz / numPerRow;
        Vertex[][] latticeArray = new Vertex[rowNum][numPerRow];
        List<ShadedTriangle> list = new ArrayList<ShadedTriangle>();
        if (rowNum < 2)
        {
            // must have at least two rows; if not, return empty list
            return list;
        }
        for (int i = 0; i < rowNum; i++)
        {
            for (int j = 0; j < numPerRow; j++)
            {
                latticeArray[i][j] = vlist.get(i * numPerRow + j);
            }
        }

        for (int i = 0; i < rowNum - 1; i++)
        {
            for (int j = 0; j < numPerRow - 1; j++)
            {
                Point2D[] ps = new Point2D[] {
                    latticeArray[i][j].point,
                    latticeArray[i][j + 1].point,
                    latticeArray[i + 1][j].point  };

                float[][] cs = new float[][] {
                    latticeArray[i][j].color,
                    latticeArray[i][j + 1].color,
                    latticeArray[i + 1][j].color };

                list.add(new ShadedTriangle(ps, cs));

                ps = new Point2D[] {
                    latticeArray[i][j + 1].point,
                    latticeArray[i + 1][j].point,
                    latticeArray[i + 1][j + 1].point };

                cs = new float[][]{
                    latticeArray[i][j + 1].color,
                    latticeArray[i + 1][j].color,
                    latticeArray[i + 1][j + 1].color };

                list.add(new ShadedTriangle(ps, cs));
            }
        }
        return list;
    }

}
