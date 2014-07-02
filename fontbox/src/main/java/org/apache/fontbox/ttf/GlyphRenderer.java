package org.apache.fontbox.ttf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.geom.GeneralPath;

/**
 * This class provides a glyph to GeneralPath conversion for true type fonts.
 * Based on code from Apache Batik a subproject of Apache XMLGraphics.
 *
 * @see <a href="http://xmlgraphics.apache.org/batik">http://xmlgraphics.apache.org/batik</a>
 */
class GlyphRenderer
{
    private static final Log LOG = LogFactory.getLog(GlyphRenderer.class);

    private GlyphDescription glyphDescription;

    public GlyphRenderer(GlyphDescription glyphDescription) {
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
        int numberOfPoints = points.length;
        int i = 0;
        boolean endOfContour = true;
        Point startingPoint = null;
        Point offCurveStartPoint = null;
        while (i < numberOfPoints)
        {
            Point point = points[i % numberOfPoints];
            Point nextPoint1 = points[(i + 1) % numberOfPoints];
            Point nextPoint2 = points[(i + 2) % numberOfPoints];
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
                moveTo(path, point);
                endOfContour = false;
                startingPoint = point;

                offCurveStartPoint = null;
                if (!point.onCurve && !nextPoint1.onCurve)
                {
                    // off curve start
                    offCurveStartPoint = point;
                    startingPoint = midValue(point, nextPoint1);
                    moveTo(path, startingPoint);
                }
            }

            if (point.onCurve)
            {
                offCurveStartPoint = null;
            }
            // lineTo
            if (point.onCurve && nextPoint1.onCurve)
            {
                lineTo(path, nextPoint1);
                i++;
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    closePath(path);
                }
                continue;
            }
            // quadratic bezier
            if (point.onCurve && !nextPoint1.onCurve && nextPoint2.onCurve)
            {
                if (nextPoint1.endOfContour)
                {
                    // use the starting point as end point
                    quadTo(path, nextPoint1, startingPoint);
                }
                else
                {
                    quadTo(path, nextPoint1, nextPoint2);
                }
                if (nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    endOfContour = true;
                    closePath(path);
                }
                i += 2;
                continue;
            }

            // TH segment for curves that start with an off-curve point
            if (offCurveStartPoint != null && !nextPoint1.onCurve && !nextPoint2.onCurve)
            {
                // interpolate endPoint
                quadTo(path, nextPoint1, midValue(nextPoint1, nextPoint2));
                if (point.endOfContour || nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    quadTo(path, nextPoint2, midValue(nextPoint2, offCurveStartPoint));
                    quadTo(path, offCurveStartPoint, startingPoint);
                    endOfContour = true;
                    i += 2;
                    continue;
                }
                ++i;
                continue;
            }

            if (point.onCurve && !nextPoint1.onCurve && !nextPoint2.onCurve)
            {
                // interpolate endPoint
                quadTo(path, nextPoint1, midValue(nextPoint1, nextPoint2));
                if (point.endOfContour || nextPoint1.endOfContour || nextPoint2.endOfContour)
                {
                    quadTo(path, nextPoint2, startingPoint);
                    endOfContour = true;
                    closePath(path);
                }
                i += 2;
                continue;
            }

            // TH the control point is never interpolated
            if (!point.onCurve && !nextPoint1.onCurve)
            {
                quadTo(path, point, midValue(point, nextPoint1));
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    quadTo(path, nextPoint1, startingPoint);
                }
                i++;
                continue;
            }

            if (!point.onCurve && nextPoint1.onCurve)
            {
                quadTo(path, point, nextPoint1);
                if (point.endOfContour || nextPoint1.endOfContour)
                {
                    endOfContour = true;
                    closePath(path);
                }
                i++;
                continue;
            }
            LOG.error("Unknown glyph command!!");
            break;
        }
        return path;
    }

    private void closePath(GeneralPath path)
    {
        path.closePath();
        if (LOG.isDebugEnabled())
        {
            LOG.debug("closePath");
        }
    }

    private void moveTo(GeneralPath path, Point point)
    {
        path.moveTo(point.x, point.y);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("moveTo: " + String.format("%d,%d", point.x, point.y));
        }
    }

    private void lineTo(GeneralPath path, Point point)
    {
        path.lineTo(point.x, point.y);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("lineTo: " + String.format("%d,%d", point.x, point.y));
        }
    }

    private void quadTo(GeneralPath path, Point ctrlPoint, Point point)
    {
        path.quadTo(ctrlPoint.x, ctrlPoint.y, point.x, point.y);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("quadTo: " + String.format("%d,%d %d,%d", ctrlPoint.x, ctrlPoint.y,
                    point.x, point.y));
        }
    }

    private int midValue(int a, int b)
    {
        return a + (b - a) / 2;
    }

    private Point midValue(Point point1, Point point2)
    {
        return new Point(midValue(point1.x, point2.x), midValue(point1.y, point2.y));
    }

    /**
     * This class represents one point of a glyph.
     */
    private class Point
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

        Point(int xValue, int yValue)
        {
            this(xValue, yValue, false, false);
        }

        @Override
        public String toString()
        {
            return String.format("Point(%d,%d,%s,%s)", x, y, onCurve ? "onCurve" : "",
                    endOfContour ? "endOfContour" : "");
        }
    }
}
