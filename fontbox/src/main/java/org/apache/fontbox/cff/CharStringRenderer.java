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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a renderer for a charstring.
 * @author Villu Ruusmann
 * @version $Revision: 1.0 $
 */
public class CharStringRenderer extends CharStringHandler
{
        // TODO CharStringRenderer as abstract Class with two inherited classes according to the Charsstring type....
    private boolean isCharstringType1 = true;
    private boolean isFirstCommand = true;
     
    private GeneralPath path = null;
    private Point2D sidebearingPoint = null;
    private Point2D referencePoint = null;
    private int width = 0;
     
    public CharStringRenderer() {
        isCharstringType1 = true;
    }
 
    public CharStringRenderer(boolean isType1) {
        isCharstringType1 = isType1;
    }
 
    /**
     * Renders the given sequence and returns the result as a GeneralPath.
     * @param sequence the given charstring sequence
     * @return the rendered GeneralPath 
     */
    public GeneralPath render(List<Object> sequence) throws IOException
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
        if (isCharstringType1) {
            handleCommandType1(numbers, command);
        } else {
            handleCommandType2(numbers, command);
        }
        return null;
    }
 
    /**
     * 
     * @param numbers
     * @param command
     */
    private void handleCommandType2(List<Integer> numbers, CharStringCommand command) {
        String name = CharStringCommand.TYPE2_VOCABULARY.get(command.getKey());
 
        if ("vmoveto".equals(name)) //
        {
            if (isFirstCommand && numbers.size() == 2) {
                setWidth(numbers.get(0));
                rmoveTo(Integer.valueOf(0), numbers.get(1));     
            } else {
                rmoveTo(Integer.valueOf(0), numbers.get(0));
            }
        } 
        else if ("rlineto".equals(name)) //
        {
            if (isFirstCommand && numbers.size() == 3) {
                setWidth(numbers.get(0));
                rlineTo(numbers.get(1), numbers.get(2));
            } else {
                rlineTo(numbers.get(0), numbers.get(1));
            }
        } 
        else if ("hlineto".equals(name))//
        {
            if (isFirstCommand && numbers.size() == 2) {
                setWidth(numbers.get(0));   
                rlineTo(numbers.get(1), Integer.valueOf(0));    
            } else {
                rlineTo(numbers.get(0), Integer.valueOf(0));
            }
        } 
        else if ("vlineto".equals(name))//
        {
            if (isFirstCommand && numbers.size() == 2) {
                setWidth(numbers.get(0));
                rlineTo(Integer.valueOf(0), numbers.get(1));
            } else {
                rlineTo(Integer.valueOf(0), numbers.get(0));
            }
        } 
        else if ("rrcurveto".equals(name))//
        {
            if (isFirstCommand && numbers.size() == 7) {
                setWidth(numbers.get(0));
                rrcurveTo(numbers.get(1), numbers.get(2), numbers.get(3), numbers
                        .get(4), numbers.get(5), numbers.get(6));
            } else {
                rrcurveTo(numbers.get(0), numbers.get(1), numbers.get(2), numbers
                        .get(3), numbers.get(4), numbers.get(5));
            }
        }
        else if ("closepath".equals(name))
        {
            closePath();
        } 
        else if ("rmoveto".equals(name))//
        {
            if (isFirstCommand && numbers.size() == 3) {
                setWidth(numbers.get(0));
                rmoveTo(numbers.get(1), numbers.get(2));
            } else {
                rmoveTo(numbers.get(0), numbers.get(1));
            }
        } 
        else if ("hmoveto".equals(name)) //
        {
            if (isFirstCommand && numbers.size() == 2) {
                setWidth(numbers.get(0));
                rmoveTo(numbers.get(1), Integer.valueOf(0));
            } else { 
                rmoveTo(numbers.get(0), Integer.valueOf(0));
            }           
        } 
        else if ("vhcurveto".equals(name))
        {
            if (isFirstCommand && numbers.size() == 5) {
                setWidth(numbers.get(0));
                rrcurveTo(Integer.valueOf(0), numbers.get(1), numbers.get(2),
                        numbers.get(3), numbers.get(4), Integer.valueOf(0));
            } else {
                rrcurveTo(Integer.valueOf(0), numbers.get(0), numbers.get(1),
                        numbers.get(2), numbers.get(3), Integer.valueOf(0));
            }
 
        } 
        else if ("hvcurveto".equals(name))
        {
            if (isFirstCommand && numbers.size() == 5) {
                setWidth(numbers.get(0));            
                rrcurveTo(numbers.get(1), Integer.valueOf(0), numbers.get(2),
                        numbers.get(3), Integer.valueOf(0), numbers.get(4));
            } else {
                rrcurveTo(numbers.get(0), Integer.valueOf(0), numbers.get(1),
                        numbers.get(2), Integer.valueOf(0), numbers.get(3));
            }
        }
        else if ("hstem".equals(name)) {
            if (numbers.size() % 2 == 1 ) {
                setWidth(numbers.get(0));
            }
        }
        else if ("vstem".equals(name)) {
            if (numbers.size() % 2 == 1 ) {
                setWidth(numbers.get(0));
            }   
        }
        else if ("hstemhm".equals(name)) {
            if (numbers.size() % 2 == 1 ) {
                setWidth(numbers.get(0));
            }
        }
        else if ("hstemhm".equals(name)) {
            if (numbers.size() % 2 == 1) {
                setWidth(numbers.get(0));
            }
        }
        else if ("cntrmask".equals(name)) {
            if (numbers.size() == 1 ) {
                setWidth(numbers.get(0));
            }
        }
        else if ("hintmask".equals(name)) {
            if (numbers.size() == 1 ) {
                setWidth(numbers.get(0));
            }
        }else if ("endchar".equals(name)) {
            if (numbers.size() == 1 ) {
                setWidth(numbers.get(0));
            }
        }
 
        if (isFirstCommand) {  isFirstCommand = false; }
    }
 
    /**
     * 
     * @param numbers
     * @param command
     */
    private void handleCommandType1(List<Integer> numbers, CharStringCommand command) {
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