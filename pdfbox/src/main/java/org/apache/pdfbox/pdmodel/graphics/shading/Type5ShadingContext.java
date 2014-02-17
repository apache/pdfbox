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

import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 *
 * This represents the Paint of a type 5 (Gouraud triangle lattice) shading
 * context.
 *
 * @author Tilman Hausherr
 */
public class Type5ShadingContext extends GouraudShadingContext
{
    private static final Log LOG = LogFactory.getLog(Type5ShadingContext.class);

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shadingType5 the shading type to be used
     * @param colorModelValue the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     *
     * @throws IOException if something went wrong
     */
    public Type5ShadingContext(PDShadingType5 shadingType5, ColorModel colorModelValue,
            AffineTransform xform, Matrix ctm, int pageHeight) throws IOException
    {
        super(shadingType5, colorModelValue, xform, ctm, pageHeight);

        LOG.debug("Type5ShadingContext");

        bitsPerColorComponent = shadingType5.getBitsPerComponent();
        LOG.debug("bitsPerColorComponent: " + bitsPerColorComponent);
        bitsPerCoordinate = shadingType5.getBitsPerCoordinate();
        LOG.debug(Math.pow(2, bitsPerCoordinate) - 1);
        long maxSrcCoord = (int) Math.pow(2, bitsPerCoordinate) - 1;
        long maxSrcColor = (int) Math.pow(2, bitsPerColorComponent) - 1;
        LOG.debug("maxSrcCoord: " + maxSrcCoord);
        LOG.debug("maxSrcColor: " + maxSrcColor);

        COSDictionary cosDictionary = shadingType5.getCOSDictionary();
        COSStream cosStream = (COSStream) cosDictionary;

        //The Decode key specifies how
        //to decode coordinate and color component data into the ranges of values
        //appropriate for each. The ranges are specified as [xmin xmax ymin ymax c1,min,
        //c1,max,..., cn, min, cn,max].
        //
        // see p344
        COSArray decode = (COSArray) cosDictionary.getDictionaryObject(COSName.DECODE);
        LOG.debug("decode: " + decode);
        PDRange rangeX = shadingType5.getDecodeForParameter(0);
        PDRange rangeY = shadingType5.getDecodeForParameter(1);
        LOG.debug("rangeX: " + rangeX.getMin() + ", " + rangeX.getMax());
        LOG.debug("rangeY: " + rangeY.getMin() + ", " + rangeY.getMax());

        PDRange[] colRangeTab = new PDRange[numberOfColorComponents];
        for (int i = 0; i < numberOfColorComponents; ++i)
        {
            colRangeTab[i] = shadingType5.getDecodeForParameter(2 + i);
        }

        LOG.debug("bitsPerCoordinate: " + bitsPerCoordinate);

        // get background values if available
        COSArray bg = shadingType5.getBackground();
        if (bg != null)
        {
            background = bg.toFloatArray();
        }

        //TODO missing: BBox, AntiAlias (p. 305 in 1.7 spec)
        
        // p318:
        //  reading in sequence from higher-order to lower-order bit positions
        ImageInputStream mciis = new MemoryCacheImageInputStream(cosStream.getFilteredStream());

        int verticesPerRow = shadingType5.getVerticesPerRow(); //TODO check >=2
        LOG.debug("verticesPerRow" + verticesPerRow);

        try
        {
            ArrayList<Vertex> prevVertexRow = new ArrayList<Vertex>();
            while (true)
            {
                // read a vertex row
                ArrayList<Vertex> vertexList = new ArrayList<Vertex>();
                for (int row = 0; row < verticesPerRow; ++row)
                {
                    vertexList.add(readVertex(mciis, (byte) 0, maxSrcCoord, maxSrcColor, rangeX, rangeY, colRangeTab));
                }
                transformVertices(vertexList, ctm, xform, pageHeight);

                // create the triangles from two rows
                if (!prevVertexRow.isEmpty())
                {
                    for (int vj = 0; vj < vertexList.size() - 1; ++vj)
                    {
                        // p.192,194 pdf spec 1.7
                        Vertex vij = prevVertexRow.get(vj); // v i,j
                        Vertex vijplus1 = prevVertexRow.get(vj + 1); // v i,j+1
                        Vertex viplus1j = vertexList.get(vj); // v i+1,j
                        Vertex viplus1jplus1 = vertexList.get(vj + 1); // v i+1,j+1
                        GouraudTriangle g = new GouraudTriangle(vij.point, vij.color,
                                vijplus1.point, vijplus1.color,
                                viplus1j.point, viplus1j.color);
                        if (!g.isEmpty())
                        {
                            triangleList.add(g);
                        }
                        else
                        {
                            LOG.debug("triangle is empty!");
                        }
                        g = new GouraudTriangle(vijplus1.point, vijplus1.color,
                                viplus1j.point, viplus1j.color,
                                viplus1jplus1.point, viplus1jplus1.color);
                        if (!g.isEmpty())
                        {
                            triangleList.add(g);
                        }
                        else
                        {
                            LOG.debug("triangle is empty!");
                        }
                    }
                }
                prevVertexRow = vertexList;
            }
        }
        catch (EOFException ex)
        {
            LOG.debug("EOF");
        }

        mciis.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose()
    {
        super.dispose();
    }

}
