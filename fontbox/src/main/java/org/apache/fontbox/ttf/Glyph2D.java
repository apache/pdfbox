/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.fontbox.ttf;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;


/**
 * This class provides a glyph to GeneralPath conversion.
 * 
 * This class is based on code from Apache Batik a subproject of Apache XMLGraphics.
 * see http://xmlgraphics.apache.org/batik/ for further details.
 */
public class Glyph2D 
{

    private short leftSideBearing = 0;
    private int advanceWidth = 0;
    private Point[] points;
    private GeneralPath glyphPath;

    /**
     * Constructor.
     * 
     * @param gd the glyph description
     * @param lsb leftSideBearing
     * @param advance advanceWidth
     */
    public Glyph2D(GlyphDescription gd, short lsb, int advance) 
    {
        leftSideBearing = lsb;
        advanceWidth = advance;
        describe(gd);
    }

    /**
     * Returns the advanceWidth value.
     * 
     * @return the advanceWidth
     */
    public int getAdvanceWidth() 
    {
        return advanceWidth;
    }

    /**
     * Returns the leftSideBearing value.
     * 
     * @return the leftSideBearing
     */
    public short getLeftSideBearing() 
    {
        return leftSideBearing;
    }

    /**
     * Set the points of a glyph from the GlyphDescription.
     */
    private void describe(GlyphDescription gd) 
    {
        int endPtIndex = 0;
        points = new Point[gd.getPointCount()];
        for (int i = 0; i < gd.getPointCount(); i++) 
        {
            boolean endPt = gd.getEndPtOfContours(endPtIndex) == i;
            if (endPt) 
            {
                endPtIndex++;
            }
            points[i] = new Point(
                    gd.getXCoordinate(i),
                    gd.getYCoordinate(i),
                    (gd.getFlags(i) & GlyfDescript.ON_CURVE) != 0,
                    endPt);
        }
    }
    
    /**
     * Returns the path describing the glyph.
     * 
     * @return the GeneralPath of the glyph
     */
    public GeneralPath getPath() 
    {
        if (glyphPath == null)
        {
            glyphPath = calculatePath();
        }
        return glyphPath;
    }
    
    private GeneralPath calculatePath()
    {
        GeneralPath path = new GeneralPath();
        int numberOfPoints = points.length;
        int i=0;
        boolean endOfContour = true;
        Point startingPoint = null;
        Point lastCtrlPoint = null;
        while (i < numberOfPoints) 
        {
            Point point = points[i%numberOfPoints];
            Point nextPoint1 = points[(i+1)%numberOfPoints];
            Point nextPoint2 = points[(i+2)%numberOfPoints];
            // new contour
            if (endOfContour) 
            {
                // skip endOfContour points
                if (point.endOfContour)
                {
                    i++;
                    continue;
                }
                // move to the starting point
                path.moveTo(point.x, point.y);
                endOfContour = false;
                startingPoint = point;
            }
            // lineTo
            if (point.onCurve && nextPoint1.onCurve) 
            {
                path.lineTo(nextPoint1.x, nextPoint1.y);
                i++;
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    path.closePath();
                }
                continue;
            } 
            // quadratic bezier
            if (point.onCurve && !nextPoint1.onCurve && nextPoint2.onCurve) 
            {
                if (nextPoint1.endOfContour)
                {
                    // use the starting point as end point
                    path.quadTo(nextPoint1.x, nextPoint1.y, startingPoint.x, startingPoint.y);
                }
                else
                {
                    path.quadTo(nextPoint1.x, nextPoint1.y, nextPoint2.x, nextPoint2.y);
                }
                if (nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    endOfContour = true;
                    path.closePath();
                }
                i+=2;
                lastCtrlPoint = nextPoint1;
                continue;
            } 
            if (point.onCurve && !nextPoint1.onCurve && !nextPoint2.onCurve) 
            {
                // interpolate endPoint
                int endPointX = midValue(nextPoint1.x, nextPoint2.x);
                int endPointY = midValue(nextPoint1.y, nextPoint2.y);
                path.quadTo(nextPoint1.x, nextPoint1.y, endPointX, endPointY);
                if (point.endOfContour || nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    path.quadTo(nextPoint2.x, nextPoint2.y, startingPoint.x, startingPoint.y);
                    endOfContour = true;
                    path.closePath();
                }
                i+=2;
                lastCtrlPoint = nextPoint1;
                continue;
            } 
            if (!point.onCurve && !nextPoint1.onCurve) 
            {
                Point2D lastEndPoint = path.getCurrentPoint();
                // calculate new control point using the previous control point
                lastCtrlPoint = new Point(midValue(lastCtrlPoint.x, (int)lastEndPoint.getX()), 
                        midValue(lastCtrlPoint.y, (int)lastEndPoint.getY()));
                // interpolate endPoint
                int endPointX = midValue((int)lastEndPoint.getX(), nextPoint1.x);
                int endPointY = midValue((int)lastEndPoint.getY(), nextPoint1.y);
                path.quadTo(lastCtrlPoint.x, lastCtrlPoint.y, endPointX, endPointY);
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    path.closePath();
                }
                i++;
                continue;
            } 
            if (!point.onCurve && nextPoint1.onCurve) 
            {
                path.quadTo(point.x, point.y, nextPoint1.x, nextPoint1.y);
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    path.closePath();
                }
                i++;
                lastCtrlPoint = point;
                continue;
            } 
            System.err.println("Unknown glyph command!!");
            break;
        }
        return path;
    }

    private int midValue(int a, int b) 
    {
        return a + (b - a)/2;
    }

    /**
     * This class represents one point of a glyph.  
     *
     */
    private class Point 
    {

        public int x = 0;
        public int y = 0;
        public boolean onCurve = true;
        public boolean endOfContour = false;

        public Point(int xValue, int yValue, boolean onCurveValue, boolean endOfContourValue) 
        {
            x = xValue;
            y = yValue;
            onCurve = onCurveValue;
            endOfContour = endOfContourValue;
        }

        public Point(int xValue, int yValue) 
        {
            this(xValue, yValue, false, false);
        }
    }

}
