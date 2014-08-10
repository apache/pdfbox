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
 * AWT Paint for Gouraud Triangle Lattice (Type 5) shading.
 */
public class Type5ShadingPaint implements Paint
{
    private static final Log LOG = LogFactory.getLog(Type5ShadingPaint.class);

    private PDShadingType5 shading;
    private Matrix ctm;
    private int pageHeight;

    /**
     * Constructor.
     *
     * @param shading the shading resources
     * @param ctm current transformation matrix
     * @param pageHeight the height of the page
     */
    public Type5ShadingPaint(PDShadingType5 shading, Matrix ctm, int pageHeight)
    {
        this.shading = shading;
        this.ctm = ctm;
        this.pageHeight = pageHeight;
    }

    /**
     * {@inheritDoc}
     */
    public int getTransparency()
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
            Rectangle2D userBounds, AffineTransform xform, RenderingHints hints)
    {
        try
        {
            return new Type5ShadingContext(shading, cm, xform, ctm, pageHeight, deviceBounds);
        }
        catch (IOException ex)
        {
            LOG.error(ex);
            return null;
        }
    }
}
