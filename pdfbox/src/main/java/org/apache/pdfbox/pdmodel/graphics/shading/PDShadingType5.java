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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * Resources for a shading type 5 (Lattice-Form Gouraud-Shade Triangle Mesh).
 */
public class PDShadingType5 extends PDTriangleBasedShadingType
{
    /**
     * Constructor using the given shading dictionary.
     *
     * @param shadingDictionary the dictionary for this shading
     */
    public PDShadingType5(COSDictionary shadingDictionary)
    {
        super(shadingDictionary);
    }

    @Override
    public int getShadingType()
    {
        return PDShading.SHADING_TYPE5;
    }

    /**
     * The vertices per row of this shading. This will return -1 if one has not
     * been set.
     *
     * @return the number of vertices per row
     */
    public int getVerticesPerRow()
    {
        return getCOSObject().getInt(COSName.VERTICES_PER_ROW, -1);
    }

    /**
     * Set the number of vertices per row.
     *
     * @param verticesPerRow the number of vertices per row
     */
    public void setVerticesPerRow(int verticesPerRow)
    {
        getCOSObject().setInt(COSName.VERTICES_PER_ROW, verticesPerRow);
    }

    @Override
    public Paint toPaint(Matrix matrix)
    {
        return new Type5ShadingPaint(this, matrix);
    }
    
    @SuppressWarnings("squid:S1166")
    @Override
    List<ShadedTriangle> collectTriangles(AffineTransform xform, Matrix matrix) throws IOException
    {
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
        int numPerRow = getVerticesPerRow();
        PDRange[] colRange = new PDRange[getNumberOfColorComponents()];
        for (int i = 0; i < colRange.length; ++i)
        {
            colRange[i] = getDecodeForParameter(2 + i);
            if (colRange[i] == null)
            {
                throw new IOException("Range missing in shading /Decode entry");
            }
        }
        List<Vertex> vlist = new ArrayList<>();
        long maxSrcCoord = (long) Math.pow(2, getBitsPerCoordinate()) - 1;
        long maxSrcColor = (long) Math.pow(2, getBitsPerComponent()) - 1;

        // MemoryCacheImageInputStream doesn't close the wrapped stream
        try (InputStream imageStream = ((COSStream) dict).createInputStream())
        {
            try (ImageInputStream mciis = new MemoryCacheImageInputStream(imageStream))
            {
                boolean eof = false;
                while (!eof)
                {
                    Vertex p;
                    try
                    {
                        p = readVertex(mciis, maxSrcCoord, maxSrcColor, rangeX, rangeY, colRange,
                                matrix, xform);
                        vlist.add(p);
                    }
                    catch (EOFException ex)
                    {
                        eof = true;
                    }
                }
            }
        }
        int rowNum = vlist.size() / numPerRow;
        if (rowNum < 2)
        {
            // must have at least two rows; if not, return empty list
            return Collections.emptyList();
        }
        Vertex[][] latticeArray = new Vertex[rowNum][numPerRow];
        for (int i = 0; i < rowNum; i++)
        {
            for (int j = 0; j < numPerRow; j++)
            {
                latticeArray[i][j] = vlist.get(i * numPerRow + j);
            }
        }

        return createShadedTriangleList(rowNum, numPerRow, latticeArray);
    }

    private List<ShadedTriangle> createShadedTriangleList(int rowNum, int numPerRow, Vertex[][] latticeArray)
    {
        Point2D[] ps = new Point2D[3]; // array will be shallow-cloned in ShadedTriangle constructor
        float[][] cs = new float[3][];
        List<ShadedTriangle> list = new ArrayList<>();
        for (int i = 0; i < rowNum - 1; i++)
        {
            for (int j = 0; j < numPerRow - 1; j++)
            {
                ps[0] = latticeArray[i][j].point;
                ps[1] = latticeArray[i][j + 1].point;
                ps[2] = latticeArray[i + 1][j].point;

                cs[0] = latticeArray[i][j].color;
                cs[1] = latticeArray[i][j + 1].color;
                cs[2] = latticeArray[i + 1][j].color;

                list.add(new ShadedTriangle(ps, cs));

                ps[0] = latticeArray[i][j + 1].point;
                ps[1] = latticeArray[i + 1][j].point;
                ps[2] = latticeArray[i + 1][j + 1].point;

                cs[0] = latticeArray[i][j + 1].color;
                cs[1] = latticeArray[i + 1][j].color;
                cs[2] = latticeArray[i + 1][j + 1].color;

                list.add(new ShadedTriangle(ps, cs));
            }
        }
        return list;
    }
}
