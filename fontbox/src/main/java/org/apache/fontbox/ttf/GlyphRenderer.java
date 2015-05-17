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
package org.apache.fontbox.ttf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a glyph to GeneralPath conversion for true type fonts.
 * Based on code from Apache Batik, a subproject of Apache XMLGraphics.
 *
 * @see
 * <a href="http://xmlgraphics.apache.org/batik">http://xmlgraphics.apache.org/batik</a>
 * 
 * Contour rendering ported from PDF.js, viewed on 14.2.2015, rev 2e97c0d
 *
 * @see
 * <a href="https://github.com/mozilla/pdf.js/blob/c0d17013a28ee7aa048831560b6494a26c52360c/src/core/font_renderer.js">pdf.js/src/core/font_renderer.js</a>
 *
 */
class GlyphRenderer
{
    private static final Log LOG = LogFactory.getLog(GlyphRenderer.class);

    private GlyphDescription glyphDescription;

    GlyphRenderer(GlyphDescription glyphDescription)
    {
        this.glyphDescription = glyphDescription;
    }

    /**
     * Returns the path of the glyph.
     * @return the path
     */
    public GeneralPath getPath()
    {
        Point[] points = describe(glyphDescription);
        return calculatePath(points);
    }

    /**
     * Set the points of a glyph from the GlyphDescription.
     */
    private Point[] describe(GlyphDescription gd)
    {
        int endPtIndex = 0;
        Point[] points = new Point[gd.getPointCount()];
        for (int i = 0; i < gd.getPointCount(); i++)
        {
            boolean endPt = gd.getEndPtOfContours(endPtIndex) == i;
            if (endPt)
            {
                endPtIndex++;
            }
            points[i] = new Point(gd.getXCoordinate(i), gd.getYCoordinate(i),
                    (gd.getFlags(i) & GlyfDescript.ON_CURVE) != 0, endPt);
        }
        return points;
    }

    /**
     * Use the given points to calculate a GeneralPath.
     *
     * @param points the points to be used to generate the GeneralPath
     *
     * @return the calculated GeneralPath
     */
    private GeneralPath calculatePath(Point[] points)
    {
        GeneralPath path = new GeneralPath();
        int start = 0;
        for (int p = 0, len = points.length; p < len; ++p)
        {
            if (points[p].endOfContour)
            {
                Point firstPoint = points[start];
                Point lastPoint = points[p];
                List<Point> contour = new ArrayList<Point>();
                for (int q = start; q <= p; ++q)
                {
                    contour.add(points[q]);
                }
                if (points[start].onCurve)
                {
                    // using start point at the contour end
                    contour.add(firstPoint);
                }
                else if (points[p].onCurve)
                {
                    // first is off-curve point, trying to use one from the end
                    contour.add(0, lastPoint);
                }
                else
                {
                    // start and end are off-curve points, creating implicit one
                    Point pmid = midValue(firstPoint, lastPoint);
                    contour.add(0, pmid);
                    contour.add(pmid);
                }
                moveTo(path, contour.get(0));
                for (int j = 1, clen = contour.size(); j < clen; j++)
                {
                    Point pnow = contour.get(j);
                    if (pnow.onCurve)
                    {
                        lineTo(path, pnow);
                    }
                    else if (contour.get(j + 1).onCurve)
                    {
                        quadTo(path, pnow, contour.get(j + 1));
                        ++j;
                    }
                    else
                    {
                        quadTo(path, pnow, midValue(pnow, contour.get(j + 1)));
                    }
                }
                start = p + 1;
            }
        }
        return path;
    }

    private void moveTo(GeneralPath path, Point point)
    {
        path.moveTo(point.x, point.y);
        if (LOG.isDebugEnabled())
        {
            LOG.trace("moveTo: " + String.format("%d,%d", point.x, point.y));
        }
    }

    private void lineTo(GeneralPath path, Point point)
    {
        path.lineTo(point.x, point.y);
        if (LOG.isDebugEnabled())
        {
            LOG.trace("lineTo: " + String.format("%d,%d", point.x, point.y));
        }
    }

    private void quadTo(GeneralPath path, Point ctrlPoint, Point point)
    {
        path.quadTo(ctrlPoint.x, ctrlPoint.y, point.x, point.y);
        if (LOG.isDebugEnabled())
        {
            LOG.trace("quadTo: " + String.format("%d,%d %d,%d", ctrlPoint.x, ctrlPoint.y,
                    point.x, point.y));
        }
    }

    private int midValue(int a, int b)
    {
        return a + (b - a) / 2;
    }

    // this creates an onCurve point that is between point1 and point2
    private Point midValue(Point point1, Point point2)
    {
        return new Point(midValue(point1.x, point2.x), midValue(point1.y, point2.y));
    }

    /**
     * This class represents one point of a glyph.
     */
    private static class Point
    {
        private int x = 0;
        private int y = 0;
        private boolean onCurve = true;
        private boolean endOfContour = false;

        Point(int xValue, int yValue, boolean onCurveValue, boolean endOfContourValue)
        {
            x = xValue;
            y = yValue;
            onCurve = onCurveValue;
            endOfContour = endOfContourValue;
        }

        // this constructs an on-curve, non-endofcountour point
        Point(int xValue, int yValue)
        {
            this(xValue, yValue, true, false);
        }

        @Override
        public String toString()
        {
            return String.format("Point(%d,%d,%s,%s)", x, y, onCurve ? "onCurve" : "",
                    endOfContour ? "endOfContour" : "");
        }
    }
    
}
