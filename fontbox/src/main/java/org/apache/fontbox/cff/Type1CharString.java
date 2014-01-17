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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.encoding.StandardEncoding;
import org.apache.fontbox.type1.Type1CharStringReader;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents and renders a Type 1 CharString.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class Type1CharString
{
    private static final Log LOG = LogFactory.getLog(Type1CharString.class);

    private Type1CharStringReader reader;
    private String fontName, glyphName;
    private GeneralPath path = null;
    private int width = 0;
    private Point2D.Float leftSideBearing = null;
    private Point2D.Float current = null;
    private boolean isFlex = false;
    private List<Point.Float> flexPoints = new ArrayList<Point2D.Float>();
    protected List<Object> type1Sequence;

    /**
     * Constructs a new Type1CharString object.
     * @param reader Parent Type 1 CharString reader
     * @param sequence Type 1 char string sequence
     */
    public Type1CharString(Type1CharStringReader reader, String fontName, String glyphName, List<Object> sequence)
    {
        this(reader, fontName, glyphName);
        type1Sequence = sequence;
    }

    /**
     * Constructor for use in subclasses.
     * @param reader Parent Type 1 CharString reader
     */
    protected Type1CharString(Type1CharStringReader reader, String fontName, String glyphName)
    {
        this.reader = reader;
        this.fontName = fontName;
        this.glyphName = glyphName;
        this.current = new Point2D.Float(0, 0);
    }

    /**
     * Returns the bounds of the renderer path.
     * @return the bounds as Rectangle2D
     */
    public Rectangle2D getBounds()
    {
        if (path == null)
        {
            render();
        }
        return path.getBounds2D();
    }

    /**
     * Returns the advance width of the glyph.
     * @return the width
     */
    public int getWidth()
    {
        if (path == null)
        {
          render();
        }
        return width;
    }

    /**
     * Returns the path of the character.
     * @return the path
     */
    public GeneralPath getPath()
    {
        if (path == null)
        {
          render();
        }
        return path;
    }

    /**
     * Returns the Type 1 char string sequence.
     * @return the Type 1 sequence
     */
    public List<Object> getType1Sequence()
    {
        return type1Sequence;
    }

    /**
     * Renders the Type 1 char string sequence to a GeneralPath.
     */
    private void render() 
    {
        path = new GeneralPath();
        leftSideBearing = new Point2D.Float(0, 0);
        width = 0;
        CharStringHandler handler = new CharStringHandler() {
            public List<Integer> handleCommand(List<Integer> numbers, CharStringCommand command)
            {
                return Type1CharString.this.handleCommand(numbers, command);
            }
        };
        handler.handleSequence(type1Sequence);
    }

    private List<Integer> handleCommand(List<Integer> numbers, CharStringCommand command)
    {
        String name = CharStringCommand.TYPE1_VOCABULARY.get(command.getKey());

        if ("rmoveto".equals(name))
        {
            if (isFlex)
            {
                flexPoints.add(new Point2D.Float(numbers.get(0), numbers.get(1)));
            }
            else
            {
                rmoveTo(numbers.get(0), numbers.get(1));
            }
        }
        else if ("vmoveto".equals(name))
        {
            if (isFlex)
            {
                // not in the Type 1 spec, but exists in some fonts
                flexPoints.add(new Point2D.Float(0, numbers.get(0)));
            }
            else
            {
                rmoveTo(0, numbers.get(0));
            }
        }
        else if ("hmoveto".equals(name))
        {
            if (isFlex)
            {
                // not in the Type 1 spec, but exists in some fonts
                flexPoints.add(new Point2D.Float(numbers.get(0), 0));
            }
            else
            {
                rmoveTo(numbers.get(0), 0);
            }
        }
        else if ("rlineto".equals(name))
        {
            rlineTo(numbers.get(0), numbers.get(1));
        }
        else if ("hlineto".equals(name))
        {
            rlineTo(numbers.get(0), 0);
        }
        else if ("vlineto".equals(name))
        {
            rlineTo(0, numbers.get(0));
        }
        else if ("rrcurveto".equals(name))
        {
            rrcurveTo(numbers.get(0), numbers.get(1), numbers.get(2),
                    numbers.get(3), numbers.get(4), numbers.get(5));
        }
        else if ("closepath".equals(name))
        {
            closepath();
        }
        else if ("sbw".equals(name))
        {
            leftSideBearing = new Point2D.Float(numbers.get(0), numbers.get(1));
            width = numbers.get(2);
            current.setLocation(leftSideBearing);
        }
        else if ("hsbw".equals(name))
        {
            leftSideBearing = new Point2D.Float(numbers.get(0), 0);
            width = numbers.get(1);
            current.setLocation(leftSideBearing);
        }
        else if ("vhcurveto".equals(name))
        {
            rrcurveTo(0, numbers.get(0), numbers.get(1),
                numbers.get(2), numbers.get(3), 0);
        }
        else if ("hvcurveto".equals(name))
        {
            rrcurveTo(numbers.get(0), 0, numbers.get(1),
                    numbers.get(2), 0, numbers.get(3));
        }
        else if ("seac".equals(name))
        {
            seac(numbers.get(0), numbers.get(1), numbers.get(2), numbers.get(3), numbers.get(4));
        }
        else if ("setcurrentpoint".equals(name))
        {
            setcurrentpoint(numbers.get(0), numbers.get(1));
        }
        else if ("callothersubr".equals(name))
        {
            callothersubr(numbers.get(0));
        }
        else if ("div".equals(name))
        {
            int b = numbers.get(numbers.size() -1);
            int a = numbers.get(numbers.size() -2);

            int result = a / b; // TODO loss of precision, should be float

            List<Integer> list = new ArrayList<Integer>(numbers);
            numbers.remove(numbers.size() - 1);
            numbers.remove(numbers.size() - 1);
            list.add(result);
            return list;
        }
        else if ("hstem".equals(name) || "vstem".equals(name) ||
                 "hstem3".equals(name) || "vstem3".equals(name) || "dotsection".equals(name))
        {
            // ignore hints
        }
        else if ("endchar".equals(name))
        {
            // end
        }
        else
        {
            // indicates a PDFBox bug
            throw new IllegalArgumentException("Unknown command: " + name);
        }
        return null;
    }

    /**
     * Sets the current absolute point without performing a moveto.
     * Used only with results from callothersubr
     */
    private void setcurrentpoint(int x, int y)
    {
        current.setLocation(x, y);
    }

    /**
     * Flex (via OtherSubrs)
     * @param num OtherSubrs entry number
     */
    private void callothersubr(int num)
    {
        if (num == 0)
        {
            // end flex
            isFlex = false;

            // reference point is relative to start point
            Point.Float reference = flexPoints.get(0);
            reference.setLocation(current.getX() + reference.getX(), current.getY() + reference.getY());

            // first point is relative to reference point
            Point.Float first = flexPoints.get(1);
            first.setLocation(reference.getX() + first.getX(), reference.getY() + first.getY());

            // make the first point relative to the start point
            first.setLocation(first.getX() - current.getX(), first.getY() - current.getY());

            rrcurveTo(flexPoints.get(1).getX(), flexPoints.get(1).getY(),
                    flexPoints.get(2).getX(), flexPoints.get(2).getY(),
                    flexPoints.get(3).getX(), flexPoints.get(3).getY());

            rrcurveTo(flexPoints.get(4).getX(), flexPoints.get(4).getY(),
                    flexPoints.get(5).getX(), flexPoints.get(5).getY(),
                    flexPoints.get(6).getX(), flexPoints.get(6).getY());

            flexPoints.clear();
        }
        else if (num == 1)
        {
            // begin flex
            isFlex = true;
        }
        else
        {
            // indicates a PDFBox bug
            throw new IllegalArgumentException("Unexpected other subroutine: " + num);
        }
    }

    /**
     * Relative moveto.
     */
    private void rmoveTo(Number dx, Number dy)
    {
        float x = (float)current.getX() + dx.floatValue();
        float y = (float)current.getY() + dy.floatValue();
        path.moveTo(x, y);
        current.setLocation(x, y);
    }

    /**
     * Relative lineto.
     */
    private void rlineTo(Number dx, Number dy)
    {
        float x = (float)current.getX() + dx.floatValue();
        float y = (float)current.getY() + dy.floatValue();
        path.lineTo(x, y);
        current.setLocation(x, y);
    }

    /**
     * Relative curveto.
     */
    private void rrcurveTo(Number dx1, Number dy1, Number dx2, Number dy2,
            Number dx3, Number dy3)
    {
        float x1 = (float) current.getX() + dx1.floatValue();
        float y1 = (float) current.getY() + dy1.floatValue();
        float x2 = x1 + dx2.floatValue();
        float y2 = y1 + dy2.floatValue();
        float x3 = x2 + dx3.floatValue();
        float y3 = y2 + dy3.floatValue();
        path.curveTo(x1, y1, x2, y2, x3, y3);
        current.setLocation(x3, y3);
    }

    /**
     * Close path.
     */
    private void closepath()
    {
        path.closePath();
        path.moveTo(current.getX(), current.getY());
    }

    /**
     * Standard Encoding Accented Character
     *
     * Makes an accented character from two other characters.
     */
    private void seac(Number asb, Number adx, Number ady, Number bchar, Number achar)
    {
        // base character
        String baseName = StandardEncoding.INSTANCE.getName(bchar.intValue());
        if (baseName != null)
        {
            try
            {
                Type1CharString base = reader.getType1CharString(baseName);
                path.append(base.getPath().getPathIterator(null), false);
            }
            catch (IOException e)
            {
                LOG.warn("invalid seac character in glyph " + glyphName + " of font " + fontName);
            }
        }
        // accent character
        String accentName = StandardEncoding.INSTANCE.getName(achar.intValue());
        if (accentName != null)
        {
            try
            {
                Type1CharString accent = reader.getType1CharString(accentName);
                AffineTransform at = AffineTransform.getTranslateInstance(
                    leftSideBearing.getX() + adx.floatValue(),
                    leftSideBearing.getY() + ady.floatValue());
                path.append(accent.getPath().getPathIterator(at), false);
            }
            catch (IOException e)
            {
                LOG.warn("invalid seac character in glyph " + glyphName + " of font " + fontName);
            }
        }
    }
}