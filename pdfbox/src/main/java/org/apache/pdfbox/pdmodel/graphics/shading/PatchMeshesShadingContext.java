/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.util.Matrix;

/**
 * This class is extended in Type6ShadingContext and Type7ShadingContext. This
 * was done as part of GSoC2014, Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
abstract class PatchMeshesShadingContext extends TriangleBasedShadingContext
{
    /**
     * patch list
     */
    private List<Patch> patchList;
    
    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     * @param deviceBounds device bounds
     * @param controlPoints number of control points, 12 for type 6 shading and 16 for type 7 shading
     * @throws IOException if something went wrong
     */
    protected PatchMeshesShadingContext(PDMeshBasedShadingType shading, ColorModel colorModel,
            AffineTransform xform, Matrix matrix, Rectangle deviceBounds,
            int controlPoints) throws IOException
    {
        super(shading, colorModel, xform, matrix);
        patchList = shading.collectPatches(xform, matrix, controlPoints);
        createPixelTable(deviceBounds);
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
        for (Patch it : patchList)
        {
            calcPixelTable(it.listOfTriangles, array, deviceBounds);
        }
        return array;
    }

    @Override
    public void dispose()
    {
        patchList = null;
        super.dispose();
    }

    @Override
    protected boolean isDataEmpty()
    {
        return patchList.isEmpty();
    }    
}
