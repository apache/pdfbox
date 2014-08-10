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
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDRange;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT PaintContext for coons patch meshes (type 6) shading. This was done as
 * part of GSoC2014, Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
class Type6ShadingContext extends PatchMeshesShadingContext
{

    /**
     * Constructor creates an instance to be used for fill operations.
     *
     * @param shading the shading type to be used
     * @param colorModel the color model to be used
     * @param xform transformation for user to device space
     * @param ctm current transformation matrix
     * @param pageHeight height of the current page
     * @param dBounds device bounds
     * @throws IOException if something went wrong
     */
    public Type6ShadingContext(PDShadingType6 shading, ColorModel colorModel, AffineTransform xform,
            Matrix ctm, int pageHeight, Rectangle dBounds) throws IOException
    {
        super(shading, colorModel, xform, ctm, pageHeight, dBounds);

        // PDFBOX-1966 flip the AffineTransform in 1.8 branch
        xform.scale(1, -1);
        xform.translate(0, -pageHeight);

        patchList = getCoonsPatchList(xform, ctm);
        pixelTable = calcPixelTable();
    }

    // get the patch list which forms the type 6 shading image from data stream
    private ArrayList<Patch> getCoonsPatchList(AffineTransform xform, Matrix ctm) throws IOException
    {
        PDShadingType6 coonsShadingType = (PDShadingType6) patchMeshesShadingType;
        COSDictionary cosDictionary = coonsShadingType.getCOSDictionary();
        PDRange rangeX = coonsShadingType.getDecodeForParameter(0);
        PDRange rangeY = coonsShadingType.getDecodeForParameter(1);

        PDRange[] colRange = new PDRange[numberOfColorComponents];
        for (int i = 0; i < numberOfColorComponents; ++i)
        {
            colRange[i] = coonsShadingType.getDecodeForParameter(2 + i);
        }
        return getPatchList(xform, ctm, cosDictionary, rangeX, rangeY, colRange, 12);
    }

    @Override
    protected Patch generatePatch(Point2D[] points, float[][] color)
    {
        return new CoonsPatch(points, color);
    }

}
