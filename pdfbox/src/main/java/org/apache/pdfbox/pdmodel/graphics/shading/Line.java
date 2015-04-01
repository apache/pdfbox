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

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

/**
 * This class describes a rasterized line. This was done as part of GSoC2014,
 * Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
class Line
{
    private final Point point0;
    private final Point point1;
    private final float[] color0;
    private final float[] color1;

    protected final Set<Point> linePoints; // all the points in this rasterized line

    /**
     * Constructor of class Line.
     *
     * @param p0 one end of a line
     * @param p1 the other end of the line
     * @param c0 color of point p0
     * @param c1 color of point p1
     */
    Line(Point p0, Point p1, float[] c0, float[] c1)
    {
        point0 = p0;
        point1 = p1;
        color0 = c0.clone();
        color1 = c1.clone();
        linePoints = calcLine(point0.x, point0.y, point1.x, point1.y);
    }

    /**
     * Calculate the points of a line with Bresenham's line algorithm
     * <a
     * href="http://en.wikipedia.org/wiki/Bresenham's_line_algorithm">Bresenham's
     * line algorithm</a>
     *
     * @param x0 coordinate
     * @param y0 coordinate
     * @param x1 coordinate
     * @param y1 coordinate
     * @return all the points on the rasterized line from (x0, y0) to (x1, y1)
     */
    private Set<Point> calcLine(int x0, int y0, int x1, int y1)
    {
        Set<Point> points = new HashSet<Point>(3);
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true)
        {
            points.add(new Point(x0, y0));
            if (x0 == x1 && y0 == y1)
            {
                break;
            }
            int e2 = 2 * err;
            if (e2 > -dy)
            {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx)
            {
                err += dx;
                y0 += sy;
            }
        }
        return points;
    }

    /**
     * Calculate the color of a point on a rasterized line by linear
     * interpolation.
     *
     * @param p target point, p should always be contained in linePoints
     * @return color
     */
    protected float[] calcColor(Point p)
    {
        int numberOfColorComponents = color0.length;
        float[] pc = new float[numberOfColorComponents];
        if (point0.x == point1.x && point0.y == point1.y)
        {
            return color0;
        }
        else if (point0.x == point1.x)
        {
            float l = point1.y - point0.y;
            for (int i = 0; i < numberOfColorComponents; i++)
            {
                pc[i] = (color0[i] * (point1.y - p.y) / l
                        + color1[i] * (p.y - point0.y) / l);
            }
        }
        else
        {
            float l = point1.x - point0.x;
            for (int i = 0; i < numberOfColorComponents; i++)
            {
                pc[i] = (color0[i] * (point1.x - p.x) / l
                        + color1[i] * (p.x - point0.x) / l);
            }
        }
        return pc;
    }
}
