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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT PaintContext for Gouraud Triangle Mesh (Type 4) shading.
 *
 * @author Tilman Hausherr
 * @author Shaola Ren
 */
class Type4ShadingContext extends GouraudShadingContext
{
    private static final Logger LOG = LogManager.getLogger(Type4ShadingContext.class);
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
        LOG.debug("bitsPerFlag: {}", bitsPerFlag);
        setTriangleList(shading.collectTriangles(xform, matrix));
        createPixelTable(deviceBounds);
    }
}
