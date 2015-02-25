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
import java.util.List;

/**
 * This class is used to describe a patch for type 6 shading. This was done as
 * part of GSoC2014, Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
class CoonsPatch extends Patch
{
    /**
     * Constructor of a patch for type 6 shading.
     *
     * @param points 12 control points
     * @param color 4 corner colors
     */
    protected CoonsPatch(Point2D[] points, float[][] color)
    {
        super(points, color);
        controlPoints = reshapeControlPoints(points);
        level = calcLevel();
        listOfTriangles = getTriangles();
    }

    // adjust the 12 control points to 4 groups, each group defines one edge of a patch
    private Point2D[][] reshapeControlPoints(Point2D[] points)
    {
        Point2D[][] fourRows = new Point2D[4][4];
        fourRows[2] = new Point2D[]
        {
            points[0], points[1], points[2], points[3]
        }; // d1
        fourRows[1] = new Point2D[]
        {
            points[3], points[4], points[5], points[6]
        }; // c2
        fourRows[3] = new Point2D[]
        {
            points[9], points[8], points[7], points[6]
        }; // d2
        fourRows[0] = new Point2D[]
        {
            points[0], points[11], points[10], points[9]
        }; // c1
        return fourRows;
    }

    // calculate the dividing level from control points
    private int[] calcLevel()
    {
        int[] l =
        {
            4, 4
        };
        // if two opposite edges are both lines, there is a possibility to reduce the dividing level
        if (isEdgeALine(controlPoints[0]) && isEdgeALine(controlPoints[1]))
        {
            double lc1 = getLen(controlPoints[0][0], controlPoints[0][3]),
                    lc2 = getLen(controlPoints[1][0], controlPoints[1][3]);
            // determine the dividing level by the lengths of edges
            if (lc1 > 800 || lc2 > 800)
            {
                // keeps init value 4
            }
            else if (lc1 > 400 || lc2 > 400)
            {
                l[0] = 3;
            }
            else if (lc1 > 200 || lc2 > 200)
            {
                l[0] = 2;
            }
            else
            {
                l[0] = 1;
            }
        }

        // the other two opposite edges
        if (isEdgeALine(controlPoints[2]) && isEdgeALine(controlPoints[3]))
        {
            double ld1 = getLen(controlPoints[2][0], controlPoints[2][3]),
                    ld2 = getLen(controlPoints[3][0], controlPoints[3][3]);
            if (ld1 > 800 || ld2 > 800)
            {
                // keeps init value 4
            }
            else if (ld1 > 400 || ld2 > 400)
            {
                l[1] = 3;
            }
            else if (ld1 > 200 || ld2 > 200)
            {
                l[1] = 2;
            }
            else
            {
                l[1] = 1;
            }
        }
        return l;
    }

    // get a list of triangles which compose this coons patch
    private List<ShadedTriangle> getTriangles()
    {
        // 4 edges are 4 cubic Bezier curves
        CubicBezierCurve eC1 = new CubicBezierCurve(controlPoints[0], level[0]);
        CubicBezierCurve eC2 = new CubicBezierCurve(controlPoints[1], level[0]);
        CubicBezierCurve eD1 = new CubicBezierCurve(controlPoints[2], level[1]);
        CubicBezierCurve eD2 = new CubicBezierCurve(controlPoints[3], level[1]);
        CoordinateColorPair[][] patchCC = getPatchCoordinatesColor(eC1, eC2, eD1, eD2);
        return getShadedTriangles(patchCC);
    }

    @Override
    protected Point2D[] getFlag1Edge()
    {
        return controlPoints[1].clone();
    }

    @Override
    protected Point2D[] getFlag2Edge()
    {
        Point2D[] implicitEdge = new Point2D[4];
        implicitEdge[0] = controlPoints[3][3];
        implicitEdge[1] = controlPoints[3][2];
        implicitEdge[2] = controlPoints[3][1];
        implicitEdge[3] = controlPoints[3][0];
        return implicitEdge;
    }

    @Override
    protected Point2D[] getFlag3Edge()
    {
        Point2D[] implicitEdge = new Point2D[4];
        implicitEdge[0] = controlPoints[0][3];
        implicitEdge[1] = controlPoints[0][2];
        implicitEdge[2] = controlPoints[0][1];
        implicitEdge[3] = controlPoints[0][0];
        return implicitEdge;
    }

    /*
     dividing a patch into a grid, return a matrix of the coordinate and color at the crossing points of the grid, 
     the rule to calculate the coordinate is defined in page 195 of PDF32000_2008.pdf, the rule to calculate the 
     cooresponding color is bilinear interpolation
     */
    private CoordinateColorPair[][] getPatchCoordinatesColor(CubicBezierCurve c1, CubicBezierCurve c2, CubicBezierCurve d1, CubicBezierCurve d2)
    {
        Point2D[] curveC1 = c1.getCubicBezierCurve();
        Point2D[] curveC2 = c2.getCubicBezierCurve();
        Point2D[] curveD1 = d1.getCubicBezierCurve();
        Point2D[] curveD2 = d2.getCubicBezierCurve();

        int numberOfColorComponents = cornerColor[0].length;
        int szV = curveD1.length;
        int szU = curveC1.length;

        CoordinateColorPair[][] patchCC = new CoordinateColorPair[szV][szU];

        double stepV = (double) 1 / (szV - 1);
        double stepU = (double) 1 / (szU - 1);
        double v = -stepV;
        for (int i = 0; i < szV; i++)
        {
            // v and u are the assistant parameters
            v += stepV;
            double u = -stepU;
            for (int j = 0; j < szU; j++)
            {
                u += stepU;
                double scx = (1 - v) * curveC1[j].getX() + v * curveC2[j].getX();
                double scy = (1 - v) * curveC1[j].getY() + v * curveC2[j].getY();
                double sdx = (1 - u) * curveD1[i].getX() + u * curveD2[i].getX();
                double sdy = (1 - u) * curveD1[i].getY() + u * curveD2[i].getY();
                double sbx = (1 - v) * ((1 - u) * controlPoints[0][0].getX() + u * controlPoints[0][3].getX())
                        + v * ((1 - u) * controlPoints[1][0].getX() + u * controlPoints[1][3].getX());
                double sby = (1 - v) * ((1 - u) * controlPoints[0][0].getY() + u * controlPoints[0][3].getY())
                        + v * ((1 - u) * controlPoints[1][0].getY() + u * controlPoints[1][3].getY());

                double sx = scx + sdx - sbx;
                double sy = scy + sdy - sby;
                // the above code in this for loop defines the patch surface (coordinates)

                Point2D tmpC = new Point2D.Double(sx, sy);

                float[] paramSC = new float[numberOfColorComponents];
                for (int ci = 0; ci < numberOfColorComponents; ci++)
                {
                    paramSC[ci] = (float) ((1 - v) * ((1 - u) * cornerColor[0][ci] + u * cornerColor[3][ci])
                            + v * ((1 - u) * cornerColor[1][ci] + u * cornerColor[2][ci])); // bilinear interpolation
                }
                patchCC[i][j] = new CoordinateColorPair(tmpC, paramSC);
            }
        }
        return patchCC;
    }
}
