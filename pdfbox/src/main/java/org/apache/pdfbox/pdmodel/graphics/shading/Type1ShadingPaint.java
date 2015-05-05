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

import java.awt.Color;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.util.Matrix;

/**
 * AWT PaintContext for function-based (Type 1) shading.
 */
class Type1ShadingPaint implements Paint
{
    private static final Log LOG = LogFactory.getLog(Type1ShadingPaint.class);

    private final PDShadingType1 shading;
    private final Matrix matrix;

    /**
     * Constructor.
     *
     * @param shading the shading resources
     * @param matrix the pattern matrix concatenated with that of the parent content stream
     */
    Type1ShadingPaint(PDShadingType1 shading, Matrix matrix)
    {
        this.shading = shading;
        this.matrix = matrix;
    }

    @Override
    public int getTransparency()
    {
        return 0;
    }

    @Override
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds,
                                      AffineTransform xform, RenderingHints hints)
    {
        try
        {
            return new Type1ShadingContext(shading, cm, xform, matrix);
        }
        catch (IOException e)
        {
            LOG.error("An error occurred while painting", e);
            return new Color(0, 0, 0, 0).createContext(cm, deviceBounds, userBounds, xform, hints);
        }
    }
}
