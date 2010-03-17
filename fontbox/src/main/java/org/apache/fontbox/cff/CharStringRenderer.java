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
package org.apache.fontbox.cff;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * This class represents a renderer for a charstring.
 * @author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class CharStringRenderer extends CharStringHandler
{

    private GeneralPath path = null;
    private Point2D sidebearingPoint = null;
    private Point2D referencePoint = null;
    private int width = 0;

    /**
     * Renders the given sequence and returns the result as a GeneralPath.
     * @param sequence the given charstring sequence
     * @return the rendered GeneralPath 
     */
    public GeneralPath render(List<Object> sequence)
    {
        path = new GeneralPath();
        sidebearingPoint = new Point2D.Float(0, 0);
        referencePoint = null;
        setWidth(0);
        handleSequence(sequence);
        return path;
    }

    /**
     * {@inheritDoc}
     */
    public void handleCommand(List<Integer> numbers, CharStringCommand command)
    {
        String name = CharStringCommand.TYPE1_VOCABULARY.get(command.getKey());

        if ("vmoveto".equals(name))
        {
            rmoveTo(Integer.valueOf(0), numbers.get(0));
        } 
        else if ("rlineto".equals(name))
        {
            rlineTo(numbers.get(0), numbers.get(1));
        } 
        else if ("hlineto".equals(name))
        {
            rlineTo(numbers.get(0), Integer.valueOf(0));
        } 
        else if ("vlineto".equals(name))
        {
            rlineTo(Integer.valueOf(0), numbers.get(0));
        } 
        else if ("rrcurveto".equals(name))
        {
            rrcurveTo(numbers.get(0), numbers.get(1), numbers.get(2), numbers
                    .get(3), numbers.get(4), numbers.get(5));
        } 
        else if ("closepath".equals(name))
        {
            closePath();
        } 
        else if ("sbw".equals(name))
        {
            pointSb(numbers.get(0), numbers.get(1));
            setWidth(numbers.get(2).intValue());
        } 
        else if ("hsbw".equals(name))
        {
            pointSb(numbers.get(0), Integer.valueOf(0));
            setWidth(numbers.get(1).intValue());
        } 
        else if ("rmoveto".equals(name))
        {
            rmoveTo(numbers.get(0), numbers.get(1));
        } 
        else if ("hmoveto".equals(name))
        {
            rmoveTo(numbers.get(0), Integer.valueOf(0));
        } 
        else if ("vhcurveto".equals(name))
        {
            rrcurveTo(Integer.valueOf(0), numbers.get(0), numbers.get(1),
                    numbers.get(2), numbers.get(3), Integer.valueOf(0));
        } 
        else if ("hvcurveto".equals(name))
        {
            rrcurveTo(numbers.get(0), Integer.valueOf(0), numbers.get(1),
                    numbers.get(2), Integer.valueOf(0), numbers.get(3));
        }
    }

    private void rmoveTo(Number dx, Number dy)
    {
        Point2D point = referencePoint;
        if (point == null)
        {
            point = sidebearingPoint;
        }
        referencePoint = null;
        path.moveTo((float)(point.getX() + dx.doubleValue()),
                    (float)(point.getY() + dy.doubleValue()));
    }

    private void rlineTo(Number dx, Number dy)
    {
        Point2D point = path.getCurrentPoint();
        path.lineTo((float)(point.getX() + dx.doubleValue()),
                    (float)(point.getY() + dy.doubleValue()));
    }

    private void rrcurveTo(Number dx1, Number dy1, Number dx2, Number dy2,
            Number dx3, Number dy3)
    {
        Point2D point = path.getCurrentPoint();
        float x1 = (float) point.getX() + dx1.floatValue();
        float y1 = (float) point.getY() + dy1.floatValue();
        float x2 = x1 + dx2.floatValue();
        float y2 = y1 + dy2.floatValue();
        float x3 = x2 + dx3.floatValue();
        float y3 = y2 + dy3.floatValue();
        path.curveTo(x1, y1, x2, y2, x3, y3);
    }

    private void closePath()
    {
        referencePoint = path.getCurrentPoint();
        path.closePath();
    }

    private void pointSb(Number x, Number y)
    {
        sidebearingPoint = new Point2D.Float(x.floatValue(), y.floatValue());
    }

    /**
     * Returns the bounds of the renderer path.
     * @return the bounds as Rectangle2D
     */
    public Rectangle2D getBounds()
    {
        return path.getBounds2D();
    }

    /**
     * Returns the width of the current command.
     * @return the width
     */
    public int getWidth()
    {
        return width;
    }

    private void setWidth(int width)
    {
        this.width = width;
    }
}