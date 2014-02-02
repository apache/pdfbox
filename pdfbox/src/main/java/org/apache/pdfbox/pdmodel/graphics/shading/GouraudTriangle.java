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

import java.awt.Polygon;
import java.awt.geom.Point2D;

/**
 * Helper class to deal with Gouraud triangles for type 4 and 5 shading.
 * 
 * @author Tilman Hausherr
 */
public class GouraudTriangle
{
    /**
     * the polygon representing the triangle.
     */
    protected final Polygon polygon = new Polygon();
    /** 
     * point A of the triangle.
     */
    protected final Point2D pointA;
    /** 
     * point B of the triangle.
     */
    protected final Point2D pointB;
    /** 
     * point C of the triangle.
     */
    protected final Point2D pointC;
    /** 
     * the color of point A.
     */
    protected final float[] colorA;
    /** 
     * the color of point B.
     */
    protected final float[] colorB;
    /** 
     * the color of point C.
     */
    protected final float[] colorC;
    
    
    /**
     * Constructor for using 3 points and their colors.
     * @param a point A of the triangle
     * @param aColor color of point A
     * @param b point B of the triangle
     * @param bColor color of point B
     * @param c point C of the triangle
     * @param cColor color of point C
     */
    public GouraudTriangle(Point2D a, float[] aColor, Point2D b, float[] bColor, Point2D c, float[] cColor)
    {
        pointA = a;
        pointB = b;
        pointC = c;
        colorA = aColor;
        colorB = bColor;
        colorC = cColor;
        polygon.addPoint((int) Math.round(a.getX()), (int) Math.round(a.getY()));
        polygon.addPoint((int) Math.round(b.getX()), (int) Math.round(b.getY()));
        polygon.addPoint((int) Math.round(c.getX()), (int) Math.round(c.getY()));
    }

    /**
     * Check whether the point is within the triangle.
     * 
     * @param p Point
     * 
     * @return true if yes, false if no
     */
    public boolean contains(Point2D p)
    {
        // if there is ever a need to optimize, go here
        // http://stackoverflow.com/a/9755252/535646
        // http://math.stackexchange.com/q/51326
        return polygon.contains(p);
    }

    /**
     * Get the area of a triangle.
     *
     */
    private double getArea(Point2D a, Point2D b, Point2D c)
    {
        // inspiration: http://stackoverflow.com/a/2145584/535646
        // test: http://www.mathopenref.com/coordtrianglearea.html
        return Math.abs((a.getX() - c.getX()) * (b.getY() - a.getY()) - (a.getX() - b.getX()) 
                * (c.getY() - a.getY())) / 2;
    }

    /**
     * calculate color weights with barycentric interpolation.
     * 
     * @param p Point within triangle
     *
     * @return array of weights (between 0 and 1) for a b c
     */
    public double[] getWeights(Point2D p)
    {
        // http://classes.soe.ucsc.edu/cmps160/Fall10/resources/barycentricInterpolation.pdf
        double area = getArea(pointA, pointB, pointC);
        return new double[]{getArea(pointB, pointC, p) / area, getArea(pointA, pointC, p) 
                / area, getArea(pointA, pointB, p) / area};
    }

}
