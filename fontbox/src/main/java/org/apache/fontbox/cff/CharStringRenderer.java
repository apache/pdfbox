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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a renderer for a charstring.
 * @author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class CharStringRenderer extends CharStringHandler
{
    // TODO CharStringRenderer as abstract Class with two inherited classes according to the Charsstring type....
	private static final Log LOG = LogFactory.getLog(CharStringRenderer.class);
	
	private boolean isCharstringType1 = true;
    private boolean isFirstCommand = true;

    private GeneralPath path = null;
    private Point2D sidebearingPoint = null;
    private Point2D referencePoint = null;
    private int width = 0;
    private boolean hasNonEndCharOp = false;

    /**
     * Constructor for the char string renderer.
     */
    public CharStringRenderer()
    {
        isCharstringType1 = true;
    }

    /**
     * Constructor for the char string renderer with a parameter
     * to determine whether the rendered CharString is type 1.
     * @param isType1 Determines wheher the charstring is type 1
     */
    public CharStringRenderer(boolean isType1)
    {
        isCharstringType1 = isType1;
    }

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
    public List<Integer> handleCommand(List<Integer> numbers, CharStringCommand command)
    {
        if (isCharstringType1)
        {
            handleCommandType1(numbers, command);
        }
        else
        {
            handleCommandType2(numbers, command);
        }
        return null;
    }

    /**
     *
     * @param numbers
     * @param command
     */
    private void handleCommandType2(List<Integer> numbers, CharStringCommand command)
    {
        String name = CharStringCommand.TYPE2_VOCABULARY.get(command.getKey());

        if (!hasNonEndCharOp)
        {
            hasNonEndCharOp = !"endchar".equals(name);
        }
        if ("vmoveto".equals(name)) //
        {
            if (path.getCurrentPoint() != null)
            {
                closePath();
            }
            if (isFirstCommand && numbers.size() == 2)
            {
                setWidth(numbers.get(0));
                rmoveTo(Integer.valueOf(0), numbers.get(1));
            }
            else
            {
                rmoveTo(Integer.valueOf(0), numbers.get(0));
            }
        }
        else if ("rlineto".equals(name)) //
        {
            if (isFirstCommand && numbers.size() == 3)
            {
                setWidth(numbers.get(0));
            }
            rrlineTo(numbers);
        }
        else if ("hlineto".equals(name))//
        {
            if (isFirstCommand && numbers.size() == 2)
            {
                setWidth(numbers.get(0));
            }
            hlineTo(numbers);
        }
        else if ("vlineto".equals(name))//
        {
            if (isFirstCommand && numbers.size() == 2)
            {
                setWidth(numbers.get(0));
            }
            vlineTo(numbers);
        }
        else if ("rrcurveto".equals(name))//
        {
            if (isFirstCommand && numbers.size() == 7)
            {
                setWidth(numbers.get(0));
            }
            rrCurveTo(numbers);
        }
        else if ("rlinecurve".equals(name))
        {
            rlineCurve(numbers);
        }
        else if ("rcurveline".equals(name))
        {
            rcurveLine(numbers);
        }
        else if ("closepath".equals(name))
        {
            closePath();
        }
        else if ("rmoveto".equals(name))//
        {
            if (path.getCurrentPoint() != null)
            {
                closePath();
            }
            if (isFirstCommand && numbers.size() == 3)
            {
                setWidth(numbers.get(0));
                rmoveTo(numbers.get(1), numbers.get(2));
            }
            else
            {
                rmoveTo(numbers.get(0), numbers.get(1));
            }
        }
        else if ("hmoveto".equals(name)) //
        {
            if (path.getCurrentPoint() != null)
            {
                closePath();
            }
            if (isFirstCommand && numbers.size() == 2)
            {
                setWidth(numbers.get(0));
                rmoveTo(numbers.get(1), Integer.valueOf(0));
            }
            else
            {
                rmoveTo(numbers.get(0), Integer.valueOf(0));
            }
        }
        else if ("vhcurveto".equals(name))
        {
            if (isFirstCommand && numbers.size() == 5)
            {
                setWidth(numbers.get(0));
            }
            rvhCurveTo(numbers);
        }
        else if ("hvcurveto".equals(name))
        {
            if (isFirstCommand && numbers.size() == 5)
            {
                setWidth(numbers.get(0));
            }
            rhvCurveTo(numbers);
        }
        else if ("hhcurveto".equals(name))
        {
            rhhCurveTo(numbers);
        }
        else if ("vvcurveto".equals(name))
        {
            rvvCurveTo(numbers);
        }
        else if ("hstem".equals(name))
        {
            if (numbers.size() % 2 == 1 )
            {
                setWidth(numbers.get(0));
            }
        }
        else if ("vstem".equals(name))
        {
            if (numbers.size() % 2 == 1 )
            {
                setWidth(numbers.get(0));
            }
        }
        else if ("hstemhm".equals(name))
        {
            if (numbers.size() % 2 == 1 )
            {
                setWidth(numbers.get(0));
            }
        }
        else if ("vstemhm".equals(name))
        {
            if (numbers.size() % 2 == 1)
            {
                setWidth(numbers.get(0));
            }
        }
        else if ("cntrmask".equals(name))
        {
            if (numbers.size() == 1 )
            {
                setWidth(numbers.get(0));
            }
        }
        else if ("hintmask".equals(name))
        {
            if (numbers.size() == 1 )
            {
                setWidth(numbers.get(0));
            }
        }
        else if ("endchar".equals(name))
        {
            if (hasNonEndCharOp)
            {
                closePath();
            }
            if (numbers.size() % 2 == 1 )
            {
                setWidth(numbers.get(0));
                if (numbers.size() > 1)
                {
                	LOG.debug("endChar: too many numbers left, using the first one, see PDFBOX-1501 for details");
                }
            }
        }
        if (isFirstCommand)
        {
            isFirstCommand = false;
        }
    }

    /**
     *
     * @param numbers
     * @param command
     */
    private void handleCommandType1(List<Integer> numbers, CharStringCommand command)
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
            point = path.getCurrentPoint();
            if (point == null)
            {
                point = sidebearingPoint;
            }
        }
        referencePoint = null;
        path.moveTo((float)(point.getX() + dx.doubleValue()),
                (float)(point.getY() + dy.doubleValue()));
    }

    private void hlineTo(List<Integer> numbers)
    {
        for (int i = 0;i < numbers.size();i++)
        {
            if (i % 2 == 0)
            {
                rlineTo(numbers.get(i), Integer.valueOf(0));
            }
            else
            {
                rlineTo(Integer.valueOf(0), numbers.get(i));
            }
        }
    }

    private void vlineTo(List<Integer> numbers)
    {
        for (int i = 0;i < numbers.size();i++)
        {
            if (i % 2 == 0)
            {
                rlineTo(Integer.valueOf(0), numbers.get(i));
            }
            else
            {
                rlineTo(numbers.get(i), Integer.valueOf(0));
            }
        }
    }

    private void rlineTo(Number dx, Number dy)
    {
        Point2D point = path.getCurrentPoint();
        path.lineTo((float)(point.getX() + dx.doubleValue()),
                (float)(point.getY() + dy.doubleValue()));
    }

    private void rrlineTo(List<Integer> numbers)
    {
        for (int i = 0;i < numbers.size();i += 2)
        {
            rlineTo(numbers.get(i), numbers.get(i + 1));
        }
    }

    private void rrCurveTo(List<Integer> numbers)
    {
        if (numbers.size() >= 6)
        {
            for (int i = 0;i < numbers.size();i += 6)
            {
                float x1 = numbers.get(i);
                float y1 = numbers.get(i + 1);
                float x2 = numbers.get(i + 2);
                float y2 = numbers.get(i + 3);
                float x3 = numbers.get(i + 4);
                float y3 = numbers.get(i + 5);
                rrcurveTo(x1, y1, x2, y2, x3, y3);
            }
        }
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


    private void rlineCurve(List<Integer> numbers)
    {
        if (numbers.size() >= 6)
        {
            if (numbers.size() - 6 > 0)
            {
                for (int i = 0;i < numbers.size() - 6;i += 2)
                {
                    if (i + 1 >= numbers.size())
                    {
                        break;
                    }
                    rlineTo(numbers.get(i), numbers.get(i + 1));
                }
            }
            float x1 = numbers.get(numbers.size() - 6);
            float y1 = numbers.get(numbers.size() - 5);
            float x2 = numbers.get(numbers.size() - 4);
            float y2 = numbers.get(numbers.size() - 3);
            float x3 = numbers.get(numbers.size() - 2);
            float y3 = numbers.get(numbers.size() - 1);
            rrcurveTo(x1, y1, x2, y2, x3, y3);
        }
    }

    private void rcurveLine(List<Integer> numbers)
    {
        for (int i = 0;i < numbers.size();i += 6)
        {
            if (numbers.size() - i < 6)
            {
                break;
            }
            float x1 = numbers.get(i);
            float y1 = numbers.get(i + 1);
            float x2 = numbers.get(i + 2);
            float y2 = numbers.get(i + 3);
            float x3 = numbers.get(i + 4);
            float y3 = numbers.get(i + 5);
            rrcurveTo(x1, y1, x2, y2, x3, y3);
            if (numbers.size() - (i + 6) == 2)
            {
                rlineTo(numbers.get(i + 6), numbers.get(i + 7));
            }
        }
    }

    private void rvhCurveTo(List<Integer> numbers)
    {
        boolean smallCase = numbers.size() <= 5;
        boolean odd = numbers.size() % 2 != 0;
        if ((!odd) ? numbers.size() % 4 == 0 : (numbers.size() -1) % 4 == 0)
        {
            float lastY = -1;
            for (int i = 0;i < numbers.size();i += 4)
            {
                if ((numbers.size() - i) < 4)
                {
                    break;
                }
                float x1 = (lastY != -1) ? numbers.get(i) : 0;
                float y1 = (lastY != -1) ? 0 : numbers.get(i);
                float x2 = numbers.get(i + 1);
                float y2 = numbers.get(i + 2);
                float x3 = (lastY != -1) ? 0 : numbers.get(i + 3);
                float y3 = (lastY != -1) ? numbers.get(i + 3) : 0;
                if (odd && (numbers.size() - i) == 5)
                {
                    if (smallCase)
                    {
                        y3 = numbers.get(i + 4);
                    }
                    else
                    {
                        x3 = numbers.get(i + 4);
                    }
                }
                rrcurveTo(x1, y1, x2, y2, x3, y3);
                if (lastY == -1)
                {
                    lastY = 0;
                }
                else
                {
                    if (numbers.size() - (i + 4) > 0)
                    {
                        rvhCurveTo(numbers.subList(i + 4, numbers.size()));
                    }
                    break;
                }
            }
        }
    }

    private void rhvCurveTo(List<Integer> numbers)
    {
        boolean smallCase = numbers.size() <= 5;
        boolean odd = numbers.size() % 2 != 0;
        if ((!odd) ? numbers.size() % 4 == 0 : (numbers.size() -1) % 4 == 0)
        {
            float lastX = -1;
            for (int i = 0;i < numbers.size();i += 4)
            {
                if ((numbers.size() - i) < 4)
                {
                    break;
                }
                float x1 = (lastX != -1) ? 0 : numbers.get(i);
                float y1 = (lastX != -1) ? numbers.get(i) : 0;
                float x2 = numbers.get(i + 1);
                float y2 = numbers.get(i + 2);
                float x3 = (lastX != -1) ? numbers.get(i + 3) : 0;
                float y3 = (lastX != -1) ? 0 : numbers.get(i + 3);
                if (odd && (numbers.size() - i) == 5)
                {
                    if (smallCase)
                    {
                        x3 = numbers.get(i + 4);
                    }
                    else
                    {
                        y3 = numbers.get(i + 4);
                    }
                }
                rrcurveTo(x1, y1, x2, y2, x3, y3);
                if (lastX == -1)
                {
                    lastX = 0;
                }
                else
                {
                    if (numbers.size() - (i + 4) > 0)
                    {
                        rhvCurveTo(numbers.subList(i + 4, numbers.size()));
                    }
                    break;
                }
            }
        }
    }

    private void rhhCurveTo(List<Integer> numbers)
    {
        boolean odd = numbers.size() % 2 != 0;
        if ((!odd) ? numbers.size() % 4 == 0 : (numbers.size() -1) % 4 == 0)
        {
            float lastY = -1;
            boolean bHandled = false;
            int increment = (odd) ? 1 : 0;
            for (int i = 0;i < numbers.size();i += 4)
            {
                if ((numbers.size() - i) < 4)
                {
                    break;
                }
                float x1 = (odd && !bHandled) ? numbers.get(i + increment) : numbers.get(i);
                float y1 = (lastY != -1) ? lastY : (odd && !bHandled) ? numbers.get(i) : 0;
                float x2 = numbers.get(i + 1 + increment);
                float y2 = numbers.get(i + 2 + increment);
                float x3 = numbers.get(i + 3 + increment);
                float y3 = 0;
                rrcurveTo(x1, y1, x2, y2, x3, y3);
                lastY = 0;
                if (odd && !bHandled)
                {
                    i++;
                    bHandled = true;
                }
                increment = 0;
            }
        }
    }

    private void rvvCurveTo(List<Integer> numbers)
    {
        boolean odd = numbers.size() % 2 != 0;
        if ((!odd) ? numbers.size() % 4 == 0 : (numbers.size() -1) % 4 == 0)
        {
            boolean bHandled = false;
            int increment = (odd) ? 1 : 0;
            for (int i = 0;i < numbers.size();i += 4)
            {
                if ((numbers.size() - i) < 4)
                {
                    break;
                }
                float x1 = (odd && !bHandled) ? numbers.get(i) : 0;
                float y1 = numbers.get(i + increment);
                float x2 = numbers.get(i + 1 + increment);
                float y2 = numbers.get(i + 2 + increment);
                float x3 = 0;
                float y3 = numbers.get(i + 3 + increment);
                rrcurveTo(x1, y1, x2, y2, x3, y3);
                if (odd && !bHandled)
                {
                    i++;
                    bHandled = true;
                }
                increment = 0;
            }
        }
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

     private void setWidth(int aWidth)
     {
         this.width = aWidth;
     }
}