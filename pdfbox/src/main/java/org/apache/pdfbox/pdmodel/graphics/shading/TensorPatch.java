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
 * This class is used to describe a patch for type 7 shading. This was done as
 * part of GSoC2014, Tilman Hausherr is the mentor.
 *
 * @author Shaola Ren
 */
class TensorPatch extends Patch
{
    /**
     * Constructor of a patch for type 7 shading.
     *
     * @param tcp 16 control points
     * @param color 4 corner colors
     */
    protected TensorPatch(Point2D[] tcp, float[][] color)
    {
        super(tcp, color);
        controlPoints = reshapeControlPoints(tcp);
        level = calcLevel();
        listOfTriangles = getTriangles();
    }

    /*
     order the 16 1d points to a square matrix which is as the one described 
     in p.199 of PDF3200_2008.pdf rotated 90 degrees clockwise
     */
    private Point2D[][] reshapeControlPoints(Point2D[] tcp)
    {
        Point2D[][] square = new Point2D[4][4];
        for (int i = 0; i <= 3; i++)
        {
            square[0][i] = tcp[i];
            square[3][i] = tcp[9 - i];
        }
        for (int i = 1; i <= 2; i++)
        {
            square[i][0] = tcp[12 - i];
            square[i][2] = tcp[12 + i];
            square[i][3] = tcp[3 + i];
        }
        square[1][1] = tcp[12];
        square[2][1] = tcp[15];
        return square;
    }

    // calculate the dividing level from the control points
    private int[] calcLevel()
    {
        int[] l =
        {
            4, 4
        };

        Point2D[] ctlC1 = new Point2D[4];
        Point2D[] ctlC2 = new Point2D[4];
        for (int j = 0; j < 4; j++)
        {
            ctlC1[j] = controlPoints[j][0];
            ctlC2[j] = controlPoints[j][3];
        }
        // if two opposite edges are both lines, there is a possibility to reduce the dividing level
        if (isEdgeALine(ctlC1) && isEdgeALine(ctlC2))
        {
            /*
             if any of the 4 inner control points is out of the patch formed by the 4 edges, 
             keep the high dividing level, 
             otherwise, determine the dividing level by the lengths of edges
             */
            if (isOnSameSideCC(controlPoints[1][1]) || isOnSameSideCC(controlPoints[1][2])
                    || isOnSameSideCC(controlPoints[2][1]) || isOnSameSideCC(controlPoints[2][2]))
            {
                // keep the high dividing level
            }
            else
            {
                // length's unit is one pixel in device space
                double lc1 = getLen(ctlC1[0], ctlC1[3]), lc2 = getLen(ctlC2[0], ctlC2[3]);
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
        }

        // the other two opposite edges
        if (isEdgeALine(controlPoints[0]) && isEdgeALine(controlPoints[3]))
        {
            if (isOnSameSideDD(controlPoints[1][1]) || isOnSameSideDD(controlPoints[1][2])
                    || isOnSameSideDD(controlPoints[2][1]) || isOnSameSideDD(controlPoints[2][2]))
            {
                // keep the high dividing level
            }
            else
            {
                double ld1 = getLen(controlPoints[0][0], controlPoints[0][3]);
                double ld2 = getLen(controlPoints[3][0], controlPoints[3][3]);
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
        }
        return l;
    }

    // whether a point is on the same side of edge C1 and edge C2
    private boolean isOnSameSideCC(Point2D p)
    {
        double cc = edgeEquationValue(p, controlPoints[0][0], controlPoints[3][0])
                * edgeEquationValue(p, controlPoints[0][3], controlPoints[3][3]);
        return cc > 0;
    }

    // whether a point is on the same side of edge D1 and edge D2
    private boolean isOnSameSideDD(Point2D p)
    {
        double dd = edgeEquationValue(p, controlPoints[0][0], controlPoints[0][3])
                * edgeEquationValue(p, controlPoints[3][0], controlPoints[3][3]);
        return dd > 0;
    }

    // get a list of triangles which compose this tensor patch
    private List<ShadedTriangle> getTriangles()
    {
        CoordinateColorPair[][] patchCC = getPatchCoordinatesColor();
        return getShadedTriangles(patchCC);
    }

    @Override
    protected Point2D[] getFlag1Edge()
    {
        Point2D[] implicitEdge = new Point2D[4];
        for (int i = 0; i < 4; i++)
        {
            implicitEdge[i] = controlPoints[i][3];
        }
        return implicitEdge;
    }

    @Override
    protected Point2D[] getFlag2Edge()
    {
        Point2D[] implicitEdge = new Point2D[4];
        for (int i = 0; i < 4; i++)
        {
            implicitEdge[i] = controlPoints[3][3 - i];
        }
        return implicitEdge;
    }

    @Override
    protected Point2D[] getFlag3Edge()
    {
        Point2D[] implicitEdge = new Point2D[4];
        for (int i = 0; i < 4; i++)
        {
            implicitEdge[i] = controlPoints[3 - i][0];
        }
        return implicitEdge;
    }

    /*
     dividing a patch into a grid according to level, then calculate the coordinate and color of 
     each crossing point in the grid, the rule to calculate the coordinate is tensor-product which 
     is defined in page 119 of PDF32000_2008.pdf, the method to calculate the cooresponding color is 
     bilinear interpolation
     */
    private CoordinateColorPair[][] getPatchCoordinatesColor()
    {
        int numberOfColorComponents = cornerColor[0].length;
        double[][] bernsteinPolyU = getBernsteinPolynomials(level[0]);
        int szU = bernsteinPolyU[0].length;
        double[][] bernsteinPolyV = getBernsteinPolynomials(level[1]);
        int szV = bernsteinPolyV[0].length;
        CoordinateColorPair[][] patchCC = new CoordinateColorPair[szV][szU];

        double stepU = 1.0 / (szU - 1);
        double stepV = 1.0 / (szV - 1);
        double v = -stepV;
        for (int k = 0; k < szV; k++)
        {
            // v and u are the assistant parameters
            v += stepV;
            double u = -stepU;
            for (int l = 0; l < szU; l++)
            {
                double tmpx = 0.0;
                double tmpy = 0.0;
                // these two "for" loops are for the equation to define the patch surface (coordinates)
                for (int i = 0; i < 4; i++)
                {
                    for (int j = 0; j < 4; j++)
                    {
                        tmpx += controlPoints[i][j].getX() * bernsteinPolyU[i][l] * bernsteinPolyV[j][k];
                        tmpy += controlPoints[i][j].getY() * bernsteinPolyU[i][l] * bernsteinPolyV[j][k];
                    }
                }
                Point2D tmpC = new Point2D.Double(tmpx, tmpy);

                u += stepU;
                float[] paramSC = new float[numberOfColorComponents];
                for (int ci = 0; ci < numberOfColorComponents; ci++)
                {
                    paramSC[ci] = (float) ((1 - v) * ((1 - u) * cornerColor[0][ci] + u * cornerColor[3][ci])
                            + v * ((1 - u) * cornerColor[1][ci] + u * cornerColor[2][ci])); // bilinear interpolation
                }
                patchCC[k][l] = new CoordinateColorPair(tmpC, paramSC);
            }
        }
        return patchCC;
    }

    // Bernstein polynomials which are defined in page 119 of PDF32000_2008.pdf
    private double[][] getBernsteinPolynomials(int lvl)
    {
        int sz = (1 << lvl) + 1;
        double[][] poly = new double[4][sz];
        double step = 1.0 / (sz - 1);
        double t = -step;
        for (int i = 0; i < sz; i++)
        {
            t += step;
            poly[0][i] = (1 - t) * (1 - t) * (1 - t);
            poly[1][i] = 3 * t * (1 - t) * (1 - t);
            poly[2][i] = 3 * t * t * (1 - t);
            poly[3][i] = t * t * t;
        }
        return poly;
    }
}
