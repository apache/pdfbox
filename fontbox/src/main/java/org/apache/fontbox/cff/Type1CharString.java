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

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fontbox.encoding.StandardEncoding;
import org.apache.fontbox.type1.Type1CharStringReader;

/**
 * This class represents and renders a Type 1 CharString.
 *
 * @author Villu Ruusmann
 * @author John Hewson
 */
public class Type1CharString
{
    private static final Log LOG = LogFactory.getLog(Type1CharString.class);

    private Type1CharStringReader font;
    private final String fontName, glyphName;
    private GeneralPath path = null;
    private int width = 0;
    private Point2D.Float leftSideBearing = null;
    private Point2D.Float current = null;
    private boolean isFlex = false;
    private final List<Point.Float> flexPoints = new ArrayList<Point2D.Float>();
    protected List<Object> type1Sequence;
    protected int commandCount;

    /**
     * Constructs a new Type1CharString object.
     *
     * @param font Parent Type 1 CharString font.
     * @param fontName Name of the font.
     * @param glyphName Name of the glyph.
     * @param sequence Type 1 char string sequence
     */
    public Type1CharString(Type1CharStringReader font, String fontName, String glyphName,
                           List<Object> sequence)
    {
        this(font, fontName, glyphName);
        type1Sequence = sequence;
    }

    /**
     * Constructor for use in subclasses.
     *
     * @param font Parent Type 1 CharString font.
     * @param fontName Name of the font.
     * @param glyphName Name of the glyph.
     */
    protected Type1CharString(Type1CharStringReader font, String fontName, String glyphName)
    {
        this.font = font;
        this.fontName = fontName;
        this.glyphName = glyphName;
        this.current = new Point2D.Float(0, 0);
    }

    // todo: NEW name (or CID as hex)
    public String getName()
    {
        return glyphName;
    }

    /**
     * Returns the bounds of the renderer path.
     * @return the bounds as Rectangle2D
     */
    public Rectangle2D getBounds()
    {
        synchronized(LOG)
        {
            if (path == null)
            {
                render();
            }
        }
        return path.getBounds2D();
    }

    /**
     * Returns the advance width of the glyph.
     * @return the width
     */
    public int getWidth()
    {
        synchronized(LOG)
        {
            if (path == null)
            {
                render();
            }
        }
        return width;
    }

    /**
     * Returns the path of the character.
     * @return the path
     */
    public GeneralPath getPath()
    {
        synchronized(LOG)
        {
            if (path == null)
            {
                render();
            }
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
            @Override
            public List<Number> handleCommand(List<Number> numbers, CharStringCommand command)
            {
                return Type1CharString.this.handleCommand(numbers, command);
            }
        };
        handler.handleSequence(type1Sequence);
    }

    private List<Number> handleCommand(List<Number> numbers, CharStringCommand command)
    {
        commandCount++;
        String name = CharStringCommand.TYPE1_VOCABULARY.get(command.getKey());

        if ("rmoveto".equals(name))
        {
            if (numbers.size() >= 2)
            {
                if (isFlex)
                {
                    flexPoints.add(new Point2D.Float(numbers.get(0).floatValue(), numbers.get(1).floatValue()));
                }
                else
                {
                    rmoveTo(numbers.get(0), numbers.get(1));
                }
            }
        }
        else if ("vmoveto".equals(name))
        {
            if (numbers.size() >= 1)
            {
                if (isFlex)
                {
                    // not in the Type 1 spec, but exists in some fonts
                    flexPoints.add(new Point2D.Float(0f, numbers.get(0).floatValue()));
                }
                else
                {
                    rmoveTo(0, numbers.get(0));
                }
            }
        }
        else if ("hmoveto".equals(name))
        {
            if (numbers.size() >= 1)
            {
                if (isFlex)
                {
                    // not in the Type 1 spec, but exists in some fonts
                    flexPoints.add(new Point2D.Float(numbers.get(0).floatValue(), 0f));
                }
                else
                {
                    rmoveTo(numbers.get(0), 0);
                }
            }
        }
        else if ("rlineto".equals(name))
        {
            if (numbers.size() >= 2)
            {
                rlineTo(numbers.get(0), numbers.get(1));
            }
        }
        else if ("hlineto".equals(name))
        {
            if (numbers.size() >= 1)
            {
                rlineTo(numbers.get(0), 0);
            }
        }
        else if ("vlineto".equals(name))
        {
            if (numbers.size() >= 1)
            {
                rlineTo(0, numbers.get(0));
            }
        }
        else if ("rrcurveto".equals(name))
        {
            if (numbers.size() >= 6)
            {
                rrcurveTo(numbers.get(0), numbers.get(1), numbers.get(2),
                        numbers.get(3), numbers.get(4), numbers.get(5));
            }
        }
        else if ("closepath".equals(name))
        {
            closepath();
        }
        else if ("sbw".equals(name))
        {
            if (numbers.size() >= 3)
            {
                leftSideBearing = new Point2D.Float(numbers.get(0).floatValue(), numbers.get(1).floatValue());
                width = numbers.get(2).intValue();
                current.setLocation(leftSideBearing);
            }
        }
        else if ("hsbw".equals(name))
        {
            if (numbers.size() >= 2)
            {
                leftSideBearing = new Point2D.Float(numbers.get(0).floatValue(), 0);
                width = numbers.get(1).intValue();
                current.setLocation(leftSideBearing);
            }
        }
        else if ("vhcurveto".equals(name))
        {
            if (numbers.size() >= 4)
            {
                rrcurveTo(0, numbers.get(0), numbers.get(1),
                        numbers.get(2), numbers.get(3), 0);
            }
        }
        else if ("hvcurveto".equals(name))
        {
            if (numbers.size() >= 4)
            {
                rrcurveTo(numbers.get(0), 0, numbers.get(1),
                        numbers.get(2), 0, numbers.get(3));
            }
        }
        else if ("seac".equals(name))
        {
            if (numbers.size() >= 5)
            {
                seac(numbers.get(0), numbers.get(1), numbers.get(2), numbers.get(3), numbers.get(4));
            }
        }
        else if ("setcurrentpoint".equals(name))
        {
            if (numbers.size() >= 2)
            {
                setcurrentpoint(numbers.get(0), numbers.get(1));
            }
        }
        else if ("callothersubr".equals(name))
        {
            if (numbers.size() >= 1)
            {
                callothersubr(numbers.get(0).intValue());
            }
        }
        else if ("div".equals(name))
        {
            float b = numbers.get(numbers.size() -1).floatValue();
            float a = numbers.get(numbers.size() -2).floatValue();

            float result = a / b;

            List<Number> list = new ArrayList<Number>(numbers);
            list.remove(list.size() - 1);
            list.remove(list.size() - 1);
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
        else if ("return".equals(name))
        {
            // indicates an invalid charstring
            LOG.warn("Unexpected charstring command: " + command.getKey() + " in glyph " +
                    glyphName + " of font " + fontName);
        }
        else if (name != null)
        {
            // indicates a PDFBox bug
            throw new IllegalArgumentException("Unhandled command: " + name);
        }
        else
        {
            // indicates an invalid charstring
            LOG.warn("Unknown charstring command: " + command.getKey() + " in glyph " + glyphName +
                     " of font " + fontName);
        }
        return null;
    }

    /**
     * Sets the current absolute point without performing a moveto.
     * Used only with results from callothersubr
     */
    private void setcurrentpoint(Number x, Number y)
    {
        current.setLocation(x.floatValue(), y.floatValue());
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

            if (flexPoints.size() < 7)
            {
                LOG.warn("flex without moveTo in font " + fontName + ", glyph " + glyphName +
                         ", command " + commandCount);
                return;
            }

            // reference point is relative to start point
            Point.Float reference = flexPoints.get(0);
            reference.setLocation(current.getX() + reference.getX(),
                                  current.getY() + reference.getY());

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
        if (path.getCurrentPoint() == null)
        {
            LOG.warn("rlineTo without initial moveTo in font " + fontName + ", glyph " + glyphName);
            path.moveTo(x, y);
        }
        else
        {
            path.lineTo(x, y);
        }
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
        if (path.getCurrentPoint() == null)
        {
            LOG.warn("rrcurveTo without initial moveTo in font " + fontName + ", glyph " + glyphName);
            path.moveTo(x3, y3);
        }
        else
        {
            path.curveTo(x1, y1, x2, y2, x3, y3);
        }
        current.setLocation(x3, y3);
    }

    /**
     * Close path.
     */
    private void closepath()
    {
        if (path.getCurrentPoint() == null)
        {
            LOG.warn("closepath without initial moveTo in font " + fontName + ", glyph " + glyphName);
        }
        else
        {
            path.closePath();
        }
        path.moveTo(current.getX(), current.getY());
    }

    /**
     * Standard Encoding Accented Character
     *
     * Makes an accented character from two other characters.
     * @param asb 
     */
    private void seac(Number asb, Number adx, Number ady, Number bchar, Number achar)
    {
        // base character
        String baseName = StandardEncoding.INSTANCE.getName(bchar.intValue());
        try
        {
            Type1CharString base = font.getType1CharString(baseName);
            path.append(base.getPath().getPathIterator(null), false);
        }
        catch (IOException e)
        {
            LOG.warn("invalid seac character in glyph " + glyphName + " of font " + fontName);
        }
        // accent character
        String accentName = StandardEncoding.INSTANCE.getName(achar.intValue());
        try
        {
            Type1CharString accent = font.getType1CharString(accentName);
            AffineTransform at = AffineTransform.getTranslateInstance(
                    leftSideBearing.getX() + adx.floatValue() - asb.floatValue(),
                    leftSideBearing.getY() + ady.floatValue());
            path.append(accent.getPath().getPathIterator(at), false);
        }
        catch (IOException e)
        {
            LOG.warn("invalid seac character in glyph " + glyphName + " of font " + fontName);
        }
    }

    @Override
    public String toString()
    {
        return type1Sequence.toString().replace("|","\n").replace(",", " ");
    }
}
