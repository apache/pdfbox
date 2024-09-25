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
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.util.Matrix;

/**
 * Shades Gouraud triangles for Type4ShadingContext and Type5ShadingContext.
 *
 * @author Tilman Hausherr
 * @author Shaola Ren
 */
abstract class GouraudShadingContext extends TriangleBasedShadingContext
{
    /**
     * triangle list.
     */
    private List<ShadedTriangle> triangleList = new ArrayList<>();

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @throws IOException if something went wrong
     */
    protected GouraudShadingContext(PDShading shading, ColorModel colorModel, AffineTransform xform,
                                    Matrix matrix) throws IOException
    {
        super(shading, colorModel, xform, matrix);
    }

    final void setTriangleList(List<ShadedTriangle> triangleList)
    {
        this.triangleList = triangleList;
    }

    @Override
    protected int[][] calcPixelTableArray(Rectangle deviceBounds) throws IOException
    {
        int[][] array = new int[deviceBounds.width + 1][deviceBounds.height + 1];
        int initialValue = getBackground() != null ? getRgbBackground() : -1;
        for (int i = 0; i < deviceBounds.width + 1; i++)
        {
            Arrays.fill(array[i], initialValue);
        }
        calcPixelTable(triangleList, array, deviceBounds);
        return array;
    }

    @Override
    public void dispose()
    {
        triangleList = null;
        super.dispose();
    }

    @Override
    protected boolean isDataEmpty()
    {
        return triangleList.isEmpty();
    }
}
