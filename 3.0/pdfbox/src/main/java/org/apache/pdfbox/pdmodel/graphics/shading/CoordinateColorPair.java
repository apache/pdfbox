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

import java.awt.geom.Point2D;

/**
 * This class is used to store a point's coordinate and its corresponding color.
 * This was done as part of GSoC2014, Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
class CoordinateColorPair
{
    final Point2D coordinate;
    final float[] color;

    /**
     * Constructor.
     *
     * @param p point
     * @param c color
     */
    CoordinateColorPair(Point2D p, float[] c)
    {
        coordinate = p;
        color = c.clone();
    }
}
