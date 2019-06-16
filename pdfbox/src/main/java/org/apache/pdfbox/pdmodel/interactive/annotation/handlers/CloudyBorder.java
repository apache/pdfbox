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
package org.apache.pdfbox.pdmodel.interactive.annotation.handlers;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDAppearanceContentStream;

/**
 * Generates annotation appearances with a cloudy border.
 * <p>
 * Dashed stroke styles are not recommended with cloudy borders. The result would
 * not look good because some parts of the arcs are traced twice by the stroked
 * path. Actually Acrobat Reader's line style dialog does not allow to choose a
 * dashed and a cloudy style at the same time.
 */

class CloudyBorder
{
    private static final double ANGLE_180_DEG = Math.PI;
    private static final double ANGLE_90_DEG = Math.PI / 2;
    private static final double ANGLE_34_DEG = Math.toRadians(34);
    private static final double ANGLE_30_DEG = Math.toRadians(30);
    private static final double ANGLE_12_DEG = Math.toRadians(12);

    private final PDAppearanceContentStream output;
    private final PDRectangle annotRect;
    private final double intensity;
    private final double lineWidth;
    private PDRectangle rectWithDiff;
    private boolean outputStarted = false;
    private double bboxMinX;
    private double bboxMinY;
    private double bboxMaxX;
    private double bboxMaxY;

    /**
     * Creates a new <code>CloudyBorder</code> that writes to the specified
     * content stream.
     *
     * @param stream content stream
     * @param intensity intensity of cloudy effect (entry <code>I</code>); typically 1.0 or 2.0
     * @param lineWidth line width for annotation border (entry <code>W</code>)
     * @param rect annotation rectangle (entry <code>Rect</code>)
     */
    CloudyBorder(PDAppearanceContentStream stream, double intensity,
    double lineWidth, PDRectangle rect)
    {
        this.output = stream;
        this.intensity = intensity;
        this.lineWidth = lineWidth;
        this.annotRect = rect;
    }

    /**
     * Creates a cloudy border for a rectangular annotation.
     * The rectangle is specified by the <code>RD</code> entry and the
     * <code>Rect</code> entry that was passed in to the constructor.
     * <p>
     * This can be used for Square and FreeText annotations. However, this does
     * not produce the text and the callout line for FreeTexts.
     *
     * @param rd entry <code>RD</code>, or null if the entry does not exist
     * @throws IOException If there is an error writing to the stream.
     */
    void createCloudyRectangle(PDRectangle rd) throws IOException
    {
        rectWithDiff = applyRectDiff(rd, lineWidth / 2);
        double left = rectWithDiff.getLowerLeftX();
        double bottom = rectWithDiff.getLowerLeftY();
        double right = rectWithDiff.getUpperRightX();
        double top = rectWithDiff.getUpperRightY();

        cloudyRectangleImpl(left, bottom, right, top, false);
        finish();
    }

    /**
     * Creates a cloudy border for a Polygon annotation.
     *
     * @param path polygon path
     * @throws IOException If there is an error writing to the stream.
     */
    void createCloudyPolygon(float[][] path) throws IOException
    {
        int n = path.length;
        Point2D.Double[] polygon = new Point2D.Double[n];

        for (int i = 0; i < n; i++)
        {
            float[] array = path[i];
            if (array.length == 2)
            {
                polygon[i] = new Point2D.Double(array[0], array[1]);
            }
            else if (array.length == 6)
            {
                // TODO Curve segments are not yet supported in cloudy border.
                polygon[i] = new Point2D.Double(array[4], array[5]);
            }
        }

        cloudyPolygonImpl(polygon, false);
        finish();
    }

    /**
     * Creates a cloudy border for a Circle annotation.
     * The ellipse is specified by the <code>RD</code> entry and the
     * <code>Rect</code> entry that was passed in to the constructor.
     *
     * @param rd entry <code>RD</code>, or null if the entry does not exist
     * @throws IOException If there is an error writing to the stream.
     */
    void createCloudyEllipse(PDRectangle rd) throws IOException
    {
        rectWithDiff = applyRectDiff(rd, 0);
        double left = rectWithDiff.getLowerLeftX();
        double bottom = rectWithDiff.getLowerLeftY();
        double right = rectWithDiff.getUpperRightX();
        double top = rectWithDiff.getUpperRightY();

        cloudyEllipseImpl(left, bottom, right, top);
        finish();
    }

    /**
     * Returns the <code>BBox</code> entry (bounding box) for the
     * appearance stream form XObject.
     *
     * @return Bounding box for appearance stream form XObject.
     */
    PDRectangle getBBox()
    {
        return getRectangle();
    }

    /**
     * Returns the updated <code>Rect</code> entry for the annotation.
     * The rectangle completely contains the cloudy border.
     *
     * @return Annotation <code>Rect</code>.
     */
    PDRectangle getRectangle()
    {
        return new PDRectangle((float)bboxMinX, (float)bboxMinY,
            (float)(bboxMaxX - bboxMinX), (float)(bboxMaxY - bboxMinY));
    }

    /**
     * Returns the <code>Matrix</code> entry for the appearance stream form XObject.
     *
     * @return Matrix for appearance stream form XObject.
     */
    AffineTransform getMatrix()
    {
        return AffineTransform.getTranslateInstance(-bboxMinX, -bboxMinY);
    }

    /**
     * Returns the updated <code>RD</code> entry for Square and Circle annotations.
     *
     * @return Annotation <code>RD</code> value.
     */
    PDRectangle getRectDifference()
    {
        if (annotRect == null)
        {
            float d = (float)lineWidth / 2;
            return new PDRectangle(d, d, (float)lineWidth, (float)lineWidth);
        }

        PDRectangle re = (rectWithDiff != null) ? rectWithDiff : annotRect;

        float left = re.getLowerLeftX() - (float)bboxMinX;
        float bottom = re.getLowerLeftY() - (float)bboxMinY;
        float right = (float)bboxMaxX - re.getUpperRightX();
        float top = (float)bboxMaxY - re.getUpperRightY();

        return new PDRectangle(left, bottom, right - left, top - bottom);
    }

    private static double cosine(double dx, double hypot)
    {
        if (Double.compare(hypot, 0.0) == 0)
        {
            return 0;
        }
        return dx / hypot;
    }

    private static double sine(double dy, double hypot)
    {
        if (Double.compare(hypot, 0.0) == 0)
        {
            return 0;
        }
        return dy / hypot;
    }

    /**
     * Cloudy rectangle implementation is based on converting the rectangle
     * to a polygon.
     */
    private void cloudyRectangleImpl(double left, double bottom,
    double right, double top, boolean isEllipse) throws IOException
    {
        double w = right - left;
        double h = top - bottom;

        if (intensity <= 0.0)
        {
            output.addRect((float)left, (float)bottom, (float)w, (float)h);
            bboxMinX = left;
            bboxMinY = bottom;
            bboxMaxX = right;
            bboxMaxY = top;
            return;
        }

        // Make a polygon with direction equal to the positive angle direction.
        Point2D.Double[] polygon;

        if (w < 1.0)
        {
            polygon = new Point2D.Double[]
            {
                new Point2D.Double(left, bottom), new Point2D.Double(left, top),
                new Point2D.Double(left, bottom)
            };
        }
        else if (h < 1.0)
        {
            polygon = new Point2D.Double[]
            {
                new Point2D.Double(left, bottom), new Point2D.Double(right, bottom),
                new Point2D.Double(left, bottom)
            };
        }
        else
        {
            polygon = new Point2D.Double[]
            {
                new Point2D.Double(left, bottom), new Point2D.Double(right, bottom),
                new Point2D.Double(right, top), new Point2D.Double(left, top),
                new Point2D.Double(left, bottom)
            };
        }

        cloudyPolygonImpl(polygon, isEllipse);
    }

    /**
     * Cloudy polygon implementation.
     *
     * @param vertices polygon vertices; first and last point must be equal
     * @param isEllipse specifies if the polygon represents an ellipse
     */
    private void cloudyPolygonImpl(Point2D.Double[] vertices, boolean isEllipse)
    throws IOException
    {
        Point2D.Double[] polygon = removeZeroLengthSegments(vertices);
        getPositivePolygon(polygon);
        int numPoints = polygon.length;

        if (numPoints < 2)
        {
            return;
        }
        if (intensity <= 0.0)
        {
            moveTo(polygon[0]);
            for (int i = 1; i < numPoints; i++)
            {
                lineTo(polygon[i]);
            }
            return;
        }

        double cloudRadius = isEllipse ? getEllipseCloudRadius() : getPolygonCloudRadius();

        if (cloudRadius < 0.5)
        {
            cloudRadius = 0.5;
        }

        final double k = Math.cos(ANGLE_34_DEG);
        final double advIntermDefault = 2 * k * cloudRadius;
        final double advCornerDefault = k * cloudRadius;
        double[] array = new double[2];
        double anglePrev = 0;

        // The number of curls per polygon segment is hardly ever an integer,
        // so the length of some curls must be adjustable. We adjust the angle
        // of the trailing arc of corner curls and the leading arc of the first
        // intermediate curl.
        // In each polygon segment, we have n intermediate curls plus one half of a
        // corner curl at each end. One of the n intermediate curls is adjustable.
        // Thus the number of fixed (or unadjusted) intermediate curls is n - 1.

        // Find the adjusted angle `alpha` for the first corner curl.
        int n0 = computeParamsPolygon(advIntermDefault, advCornerDefault, k, cloudRadius,
            polygon[numPoints - 2].distance(polygon[0]), array);
        double alphaPrev = (n0 == 0) ? array[0] : ANGLE_34_DEG;

        for (int j = 0; j + 1 < numPoints; j++)
        {
            Point2D.Double pt = polygon[j];
            Point2D.Double ptNext = polygon[j + 1];
            double length = pt.distance(ptNext);
            if (Double.compare(length, 0.0) == 0)
            {
                alphaPrev = ANGLE_34_DEG;
                continue;
            }

            // n is the number of intermediate curls in the current polygon segment.
            int n = computeParamsPolygon(advIntermDefault, advCornerDefault, k,
                cloudRadius, length, array);
            if (n < 0)
            {
                if (!outputStarted)
                {
                    moveTo(pt);
                }
                continue;
            }

            double alpha = array[0];
            double dx = array[1];

            double angleCur = Math.atan2(ptNext.y - pt.y, ptNext.x - pt.x);
            if (j == 0)
            {
                Point2D.Double ptPrev = polygon[numPoints - 2];
                anglePrev = Math.atan2(pt.y - ptPrev.y, pt.x - ptPrev.x);
            }

            double cos = cosine(ptNext.x - pt.x, length);
            double sin = sine(ptNext.y - pt.y, length);
            double x = pt.x;
            double y = pt.y;

            addCornerCurl(anglePrev, angleCur, cloudRadius, pt.x, pt.y, alpha,
                alphaPrev, !outputStarted);
            // Proceed to the center point of the first intermediate curl.
            double adv = 2 * k * cloudRadius + 2 * dx;
            x += adv * cos;
            y += adv * sin;

            // Create the first intermediate curl.
            int numInterm = n;
            if (n >= 1)
            {
                addFirstIntermediateCurl(angleCur, cloudRadius, alpha, x, y);
                x += advIntermDefault * cos;
                y += advIntermDefault * sin;
                numInterm = n - 1;
            }

            // Create one intermediate curl and replicate it along the polygon segment.
            Point2D.Double[] template = getIntermediateCurlTemplate(angleCur, cloudRadius);
            for (int i = 0; i < numInterm; i++)
            {
                outputCurlTemplate(template, x, y);
                x += advIntermDefault * cos;
                y += advIntermDefault * sin;
            }

            anglePrev = angleCur;
            alphaPrev = (n == 0) ? alpha : ANGLE_34_DEG;
        }
    }

    /**
     * Computes parameters for a cloudy polygon: n, alpha, and dx.
     */
    private int computeParamsPolygon(double advInterm, double advCorner, double k,
    double r, double length, double[] array)
    {
        if (Double.compare(length, 0.0) == 0)
        {
            array[0] = ANGLE_34_DEG;
            array[1] = 0;
            return -1;
        }

        // n is the number of intermediate curls in the current polygon segment
        int n = (int) Math.ceil((length - 2 * advCorner) / advInterm);

        // Fitting error along polygon segment
        double e = length - (2 * advCorner + n * advInterm);
        // Fitting error per each adjustable half curl
        double dx = e / 2;

        // Convert fitting error to an angle that can be used to control arcs.
        double arg = (k * r + dx) / r;
        double alpha = (arg < -1.0 || arg > 1.0) ? 0.0 : Math.acos(arg);

        array[0] = alpha;
        array[1] = dx;
        return n;
    }

    /**
     * Creates a corner curl for polygons and ellipses.
     */
    private void addCornerCurl(double anglePrev, double angleCur, double radius,
    double cx, double cy, double alpha, double alphaPrev, boolean addMoveTo)
    throws IOException
    {
        double a = anglePrev + ANGLE_180_DEG + alphaPrev;
        double b = anglePrev + ANGLE_180_DEG + alphaPrev - Math.toRadians(22);
        getArcSegment(a, b, cx, cy, radius, radius, null, addMoveTo);

        a = b;
        b = angleCur - alpha;
        getArc(a, b, radius, radius, cx, cy, null, false);
    }

    /**
     * Generates the first intermediate curl for a cloudy polygon.
     */
    private void addFirstIntermediateCurl(double angleCur, double r, double alpha,
    double cx, double cy) throws IOException
    {
        double a = angleCur + ANGLE_180_DEG;

        getArcSegment(a + alpha, a + alpha - ANGLE_30_DEG, cx, cy, r, r, null, false);
        getArcSegment(a + alpha - ANGLE_30_DEG, a + ANGLE_90_DEG, cx, cy, r, r, null, false);
        getArcSegment(a + ANGLE_90_DEG, a + ANGLE_180_DEG - ANGLE_34_DEG,
            cx, cy, r, r, null, false);
    }

    /**
     * Returns a template for intermediate curls in a cloudy polygon.
     */
    private Point2D.Double[] getIntermediateCurlTemplate(double angleCur, double r)
    throws IOException
    {
        ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
        double a = angleCur + ANGLE_180_DEG;

        getArcSegment(a + ANGLE_34_DEG, a + ANGLE_12_DEG, 0, 0, r, r, points, false);
        getArcSegment(a + ANGLE_12_DEG, a + ANGLE_90_DEG,  0, 0, r, r, points, false);
        getArcSegment(a + ANGLE_90_DEG, a + ANGLE_180_DEG - ANGLE_34_DEG,
            0, 0, r, r, points, false);

        return points.toArray(new Point2D.Double[points.size()]);
    }

    /**
     * Writes the curl template points to the output and applies translation (x, y).
     */
    private void outputCurlTemplate(Point2D.Double[] template, double x, double y)
    throws IOException
    {
        int n = template.length;
        int i = 0;

        if ((n % 3) == 1)
        {
            Point2D.Double a = template[0];
            moveTo(a.x + x, a.y + y);
            i++;
        }
        for (; i + 2 < n; i += 3)
        {
            Point2D.Double a = template[i];
            Point2D.Double b = template[i + 1];
            Point2D.Double c = template[i + 2];
            curveTo(a.x + x, a.y + y, b.x + x, b.y + y, c.x + x, c.y + y);
        }
    }

    private PDRectangle applyRectDiff(PDRectangle rd, double min)
    {
        float rectLeft = annotRect.getLowerLeftX();
        float rectBottom = annotRect.getLowerLeftY();
        float rectRight = annotRect.getUpperRightX();
        float rectTop = annotRect.getUpperRightY();

        // Normalize
        rectLeft = Math.min(rectLeft, rectRight);
        rectBottom = Math.min(rectBottom, rectTop);
        rectRight = Math.max(rectLeft, rectRight);
        rectTop = Math.max(rectBottom, rectTop);

        double rdLeft;
        double rdBottom;
        double rdRight;
        double rdTop;

        if (rd != null)
        {
            rdLeft = Math.max(rd.getLowerLeftX(), min);
            rdBottom = Math.max(rd.getLowerLeftY(), min);
            rdRight = Math.max(rd.getUpperRightX(), min);
            rdTop = Math.max(rd.getUpperRightY(), min);
        }
        else
        {
            rdLeft = min;
            rdBottom = min;
            rdRight = min;
            rdTop = min;
        }

        rectLeft += rdLeft;
        rectBottom += rdBottom;
        rectRight -= rdRight;
        rectTop -= rdTop;

        return new PDRectangle(rectLeft, rectBottom, rectRight - rectLeft, rectTop - rectBottom);
    }

    private void reversePolygon(Point2D.Double[] points)
    {
        int len = points.length;
        int n = len / 2;
        for (int i = 0; i < n; i++)
        {
            int j = len - i - 1;
            Point2D.Double pi = points[i];
            Point2D.Double pj = points[j];
            points[i] = pj;
            points[j] = pi;
        }
    }

    /**
     * Makes a polygon whose direction is the same as the positive angle
     * direction in the coordinate system.
     * The polygon must not intersect itself.
     */
    private void getPositivePolygon(Point2D.Double[] points)
    {
        if (getPolygonDirection(points) < 0)
        {
            reversePolygon(points);
        }
    }

    /**
     * Returns the direction of the specified polygon.
     * A positive value indicates that the polygon's direction is the same as the
     * direction of positive angles in the coordinate system.
     * A negative value indicates the opposite direction.
     *
     * The polygon must not intersect itself. A 2-point polygon is not acceptable.
     * This is based on the "shoelace formula".
     */
    private double getPolygonDirection(Point2D.Double[] points)
    {
        double a = 0;
        int len = points.length;
        for (int i = 0; i < len; i++)
        {
            int j = (i + 1) % len;
            a += points[i].x * points[j].y - points[i].y * points[j].x;
        }
        return a;
    }

    /**
     * Creates one or more Bézier curves that represent an elliptical arc.
     * Angles are in radians.
     * The arc will always proceed in the positive angle direction.
     * If the argument `out` is null, this writes the results to the instance
     * variable `output`.
     */
    private void getArc(double startAng, double endAng, double rx, double ry,
    double cx, double cy, ArrayList<Point2D.Double> out, boolean addMoveTo) throws IOException
    {
        final double angleIncr = Math.PI / 2;
        double startx = rx * Math.cos(startAng) + cx;
        double starty = ry * Math.sin(startAng) + cy;

        double angleTodo = endAng - startAng;
        while (angleTodo < 0)
        {
            angleTodo += 2 * Math.PI;
        }
        double sweep = angleTodo;
        double angleDone = 0;

        if (addMoveTo)
        {
            if (out != null)
            {
                out.add(new Point2D.Double(startx, starty));
            }
            else
            {
                moveTo(startx, starty);
            }
        }

        while (angleTodo > angleIncr)
        {
            getArcSegment(startAng + angleDone,
                startAng + angleDone + angleIncr, cx, cy, rx, ry, out, false);
            angleDone += angleIncr;
            angleTodo -= angleIncr;
        }

        if (angleTodo > 0)
        {
            getArcSegment(startAng + angleDone, startAng + sweep, cx, cy, rx, ry, out, false);
        }
    }

    /**
     * Creates a single Bézier curve that represents a section of an elliptical
     * arc. The sweep angle of the section must not be larger than 90 degrees.
     * If argument `out` is null, this writes the results to the instance
     * variable `output`.
     */
    private void getArcSegment(double startAng, double endAng, double cx, double cy,
    double rx, double ry, ArrayList<Point2D.Double> out, boolean addMoveTo) throws IOException
    {
        // Algorithm is from the FAQ of the news group comp.text.pdf

        double cosA = Math.cos(startAng);
        double sinA = Math.sin(startAng);
        double cosB = Math.cos(endAng);
        double sinB = Math.sin(endAng);
        double denom = Math.sin((endAng - startAng) / 2.0);
        if (Double.compare(denom, 0.0) == 0)
        {
            // This can happen only if endAng == startAng.
            // The arc sweep angle is zero, so we create no arc at all.
            if (addMoveTo)
            {
                double xs = cx + rx * cosA;
                double ys = cy + ry * sinA;
                if (out != null)
                {
                    out.add(new Point2D.Double(xs, ys));
                }
                else
                {
                    moveTo(xs, ys);
                }
            }
            return;
        }
        double bcp = 1.333333333 * (1 - Math.cos((endAng - startAng) / 2.0)) / denom;
        double p1x = cx + rx * (cosA - bcp * sinA);
        double p1y = cy + ry * (sinA + bcp * cosA);
        double p2x = cx + rx * (cosB + bcp * sinB);
        double p2y = cy + ry * (sinB - bcp * cosB);
        double p3x = cx + rx * cosB;
        double p3y = cy + ry * sinB;

        if (addMoveTo)
        {
            double xs = cx + rx * cosA;
            double ys = cy + ry * sinA;
            if (out != null)
            {
                out.add(new Point2D.Double(xs, ys));
            }
            else
            {
                moveTo(xs, ys);
            }
        }

        if (out != null)
        {
            out.add(new Point2D.Double(p1x, p1y));
            out.add(new Point2D.Double(p2x, p2y));
            out.add(new Point2D.Double(p3x, p3y));
        }
        else
        {
            curveTo(p1x, p1y, p2x, p2y, p3x, p3y);
        }
    }

    /**
     * Flattens an ellipse into a polygon.
     */
    private static Point2D.Double[] flattenEllipse(double left, double bottom,
    double right, double top)
    {
        Ellipse2D.Double ellipse = new Ellipse2D.Double(left, bottom, right - left, top - bottom);
        final double flatness = 0.50;
        PathIterator iterator = ellipse.getPathIterator(null, flatness);
        double[] coords = new double[6];
        ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();

        while (!iterator.isDone())
        {
            switch (iterator.currentSegment(coords))
            {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    points.add(new Point2D.Double(coords[0], coords[1]));
                    break;
                // Curve segments are not expected because the path iterator is
                // flattened. SEG_CLOSE can be ignored.
                default:
                    break;
            }
            iterator.next();
        }

        int size = points.size();
        final double closeTestLimit = 0.05;

        if (size >= 2 && points.get(size - 1).distance(points.get(0)) > closeTestLimit)
        {
            points.add(points.get(points.size() - 1));
        }
        return points.toArray(new Point2D.Double[points.size()]);
    }

    /**
     * Cloudy ellipse implementation.
     */
    private void cloudyEllipseImpl(final double leftOrig, final double bottomOrig,
    final double rightOrig, final double topOrig) throws IOException
    {
        if (intensity <= 0.0)
        {
            drawBasicEllipse(leftOrig, bottomOrig, rightOrig, topOrig);
            return;
        }

        double left = leftOrig;
        double bottom = bottomOrig;
        double right = rightOrig;
        double top = topOrig;
        double width = right - left;
        double height = top - bottom;
        double cloudRadius = getEllipseCloudRadius();

        // Omit cloudy border if the ellipse is very small.
        final double threshold1 = 0.50 * cloudRadius;
        if (width < threshold1 && height < threshold1)
        {
            drawBasicEllipse(left, bottom, right, top);
            return;
        }

        // Draw a cloudy rectangle instead of an ellipse when the
        // width or height is very small.
        final double threshold2 = 5;
        if ((width < threshold2 && height > 20) || (width > 20 && height < threshold2))
        {
            cloudyRectangleImpl(left, bottom, right, top, true);
            return;
        }

        // Decrease radii (while center point does not move). This makes the
        // "tails" of the curls almost touch the ellipse outline.
        double radiusAdj = Math.sin(ANGLE_12_DEG) * cloudRadius - 1.50;
        if (width > 2 * radiusAdj)
        {
            left += radiusAdj;
            right -= radiusAdj;
        }
        else
        {
            double mid = (left + right) / 2;
            left = mid - 0.10;
            right = mid + 0.10;
        }
        if (height > 2 * radiusAdj)
        {
            top -= radiusAdj;
            bottom += radiusAdj;
        }
        else
        {
            double mid = (top + bottom) / 2;
            top = mid + 0.10;
            bottom = mid - 0.10;
        }

        // Flatten the ellipse into a polygon. The segment lengths of the flattened
        // result don't need to be extremely short because the loop below is able to
        // interpolate between polygon points when it computes the center points
        // at which each curl is placed.

        Point2D.Double[] flatPolygon = flattenEllipse(left, bottom, right, top);
        int numPoints = flatPolygon.length;
        if (numPoints < 2)
        {
            return;
        }

        double totLen = 0;
        for(int i = 1; i < numPoints; i++){
            totLen += flatPolygon[i - 1].distance(flatPolygon[i]);
        }

        final double k = Math.cos(ANGLE_34_DEG);
        double curlAdvance = 2 * k * cloudRadius;
        int n = (int) Math.ceil(totLen / curlAdvance);
        if (n < 2)
        {
            drawBasicEllipse(leftOrig, bottomOrig, rightOrig, topOrig);
            return;
        }

        curlAdvance = totLen / n;
        cloudRadius = curlAdvance / (2 * k);

        if (cloudRadius < 0.5)
        {
            cloudRadius = 0.5;
            curlAdvance = 2 * k * cloudRadius;
        }
        else if (cloudRadius < 3.0)
        {
            // Draw a small circle when the scaled radius becomes very small.
            // This happens also if intensity is much smaller than 1.
            drawBasicEllipse(leftOrig, bottomOrig, rightOrig, topOrig);
            return;
        }

        // Construct centerPoints array, in which each point is the center point of a curl.
        // The length of each centerPoints segment ideally equals curlAdv but that
        // is not true in regions where the ellipse curvature is high.

        int centerPointsLength = n;
        Point2D.Double[] centerPoints = new Point2D.Double[centerPointsLength];
        int centerPointsIndex = 0;
        double lengthRemain = 0;
        final double comparisonToler = lineWidth * 0.10;

        for (int i = 0; i + 1 < numPoints; i++)
        {
            Point2D.Double p1 = flatPolygon[i];
            Point2D.Double p2 = flatPolygon[i + 1];
            double dx = p2.x - p1.x;
            double dy = p2.y - p1.y;
            double length = p1.distance(p2);
            if (Double.compare(length, 0.0) == 0)
            {
                continue;
            }
            double lengthTodo = length + lengthRemain;
            if (lengthTodo >= curlAdvance - comparisonToler || i == numPoints - 2)
            {
                double cos = cosine(dx, length);
                double sin = sine(dy, length);
                double d = curlAdvance - lengthRemain;
                do
                {
                    double x = p1.x + d * cos;
                    double y = p1.y + d * sin;
                    if (centerPointsIndex < centerPointsLength)
                    {
                        centerPoints[centerPointsIndex++] = new Point2D.Double(x, y);
                    }
                    lengthTodo -= curlAdvance;
                    d += curlAdvance;
                }
                while (lengthTodo >= curlAdvance - comparisonToler);

                lengthRemain = lengthTodo;
                if (lengthRemain < 0)
                {
                    lengthRemain = 0;
                }
            }
            else
            {
                lengthRemain += length;
            }
        }

        // Note: centerPoints does not repeat the first point as the last point
        // to create a "closing" segment.

        // Place a curl at each point of the centerPoints array.
        // In regions where the ellipse curvature is high, the centerPoints segments
        // are shorter than the actual distance along the ellipse. Thus we must
        // again compute arc adjustments like in cloudy polygons.

        numPoints = centerPointsIndex;
        double anglePrev = 0;
        double alphaPrev = 0;

        for (int i = 0; i < numPoints; i++)
        {
            int idxNext = i + 1;
            if (i + 1 >= numPoints)
            {
                idxNext = 0;
            }
            Point2D.Double pt = centerPoints[i];
            Point2D.Double ptNext = centerPoints[idxNext];

            if (i == 0)
            {
                Point2D.Double ptPrev = centerPoints[numPoints - 1];
                anglePrev = Math.atan2(pt.y - ptPrev.y, pt.x - ptPrev.x);
                alphaPrev = computeParamsEllipse(ptPrev, pt, cloudRadius, curlAdvance);
            }

            double angleCur = Math.atan2(ptNext.y - pt.y, ptNext.x - pt.x);
            double alpha = computeParamsEllipse(pt, ptNext, cloudRadius, curlAdvance);

            addCornerCurl(anglePrev, angleCur, cloudRadius, pt.x, pt.y, alpha,
                alphaPrev, !outputStarted);

            anglePrev = angleCur;
            alphaPrev = alpha;
        }
    }

    /**
     * Computes the alpha parameter for an ellipse curl.
     */
    private double computeParamsEllipse(Point2D.Double pt, Point2D.Double ptNext,
    double r, double curlAdv)
    {
        double length = pt.distance(ptNext);
        if (Double.compare(length, 0.0) == 0)
        {
            return ANGLE_34_DEG;
        }

        double e = length - curlAdv;
        double arg = (curlAdv / 2 + e / 2) / r;
        return (arg < -1.0 || arg > 1.0) ? 0.0 : Math.acos(arg);
    }

    private Point2D.Double[] removeZeroLengthSegments(Point2D.Double[] polygon)
    {
        int np = polygon.length;
        if (np <= 2)
        {
            return polygon;
        }

        final double toler = 0.50;
        int npNew = np;
        Point2D.Double ptPrev = polygon[0];

        // Don't remove the last point if it equals the first point.
        for (int i = 1; i < np; i++)
        {
            Point2D.Double pt = polygon[i];
            if (Math.abs(pt.x - ptPrev.x) < toler && Math.abs(pt.y - ptPrev.y) < toler)
            {
                polygon[i] = null;
                npNew--;
            }
            ptPrev = pt;
        }

        if (npNew == np)
        {
            return polygon;
        }

        Point2D.Double[] polygonNew = new Point2D.Double[npNew];
        int j = 0;
        for (int i = 0; i < np; i++)
        {
            Point2D.Double pt = polygon[i];
            if (pt != null)
            {
                polygonNew[j++] = pt;
            }
        }

        return polygonNew;
    }

    /**
     * Draws an ellipse without a cloudy border effect.
     */
    private void drawBasicEllipse(double left, double bottom, double right, double top)
    throws IOException
    {
        double rx = Math.abs(right - left) / 2;
        double ry = Math.abs(top - bottom) / 2;
        double cx = (left + right) / 2;
        double cy = (bottom + top) / 2;
        getArc(0, 2 * Math.PI, rx, ry, cx, cy, null, true);
    }

    private void beginOutput(double x, double y) throws IOException
    {
        bboxMinX = x;
        bboxMinY = y;
        bboxMaxX = x;
        bboxMaxY = y;
        outputStarted = true;
        // Set line join to bevel to avoid spikes
        output.setLineJoinStyle(2);
    }

    private void updateBBox(double x, double y)
    {
        bboxMinX = Math.min(bboxMinX, x);
        bboxMinY = Math.min(bboxMinY, y);
        bboxMaxX = Math.max(bboxMaxX, x);
        bboxMaxY = Math.max(bboxMaxY, y);
    }

    private void moveTo(Point2D.Double p) throws IOException
    {
        moveTo(p.x, p.y);
    }

    private void moveTo(double x, double y) throws IOException
    {
        if (outputStarted)
        {
            updateBBox(x, y);
        }
        else
        {
            beginOutput(x, y);
        }

        output.moveTo((float)x, (float)y);
    }

    private void lineTo(Point2D.Double p) throws IOException
    {
        lineTo(p.x, p.y);
    }

    private void lineTo(double x, double y) throws IOException
    {
        if (outputStarted)
        {
            updateBBox(x, y);
        }
        else
        {
            beginOutput(x, y);
        }

        output.lineTo((float)x, (float)y);
    }

    private void curveTo(double ax, double ay, double bx, double by, double cx, double cy)
    throws IOException
    {
        updateBBox(ax, ay);
        updateBBox(bx, by);
        updateBBox(cx, cy);
        output.curveTo((float)ax, (float)ay, (float)bx, (float)by, (float)cx, (float)cy);
    }

    private void finish() throws IOException
    {
        if (outputStarted)
        {
            output.closePath();
        }

        if (lineWidth > 0)
        {
            double d = lineWidth / 2;
            bboxMinX -= d;
            bboxMinY -= d;
            bboxMaxX += d;
            bboxMaxY += d;
        }
    }

    private double getEllipseCloudRadius()
    {
        // Equation deduced from Acrobat Reader's appearance streams. Circle
        // annotations have a slightly larger radius than Polygons and Squares.
        return 4.75 * intensity + 0.5 * lineWidth;
    }

    private double getPolygonCloudRadius()
    {
        // Equation deduced from Acrobat Reader's appearance streams.
        return 4 * intensity + 0.5 * lineWidth;
    }
}
