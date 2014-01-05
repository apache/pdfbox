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

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
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

    private CFFFont cffFont;
    private GeneralPath path = null;
    private int width = 0;
    private Point2D leftSideBearing = null;
    private Point2D referencePoint = null;
    protected List<Object> type1Sequence;

    /**
     * Constructs a new Type1CharString object.
     * @param font Parent CFF font
     * @param sequence Type 1 char string sequence
     */
    public Type1CharString(CFFFont font, List<Object> sequence)
    {
        this(font);
        type1Sequence = sequence;
    }

    /**
     * Constructor for use in subclasses.
     * @param font Parent CFF font
     */
    protected Type1CharString(CFFFont font)
    {
        cffFont = font;
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
        //TODO does not need to be lazy anymore?
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
        referencePoint = null;
        width = 0;
        CharStringHandler handler = new CharStringHandler() {
            public void handleCommand(List<Integer> numbers, CharStringCommand command)
            {
                Type1CharString.this.handleCommand(numbers, command);
            }
        };
        handler.handleSequence(type1Sequence);
    }

    private void handleCommand(List<Integer> numbers, CharStringCommand command)
    {
        String name = CharStringCommand.TYPE1_VOCABULARY.get(command.getKey());

        if ("vmoveto".equals(name))
        {
            rmoveTo(0, numbers.get(0));
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
            rrcurveTo(numbers.get(0), numbers.get(1), numbers.get(2), numbers
                .get(3), numbers.get(4), numbers.get(5));
        }
        else if ("closepath".equals(name))
        {
            closepath();
        }
        else if ("sbw".equals(name))
        {
            leftSideBearing = new Point2D.Float(numbers.get(0), numbers.get(1));
            width = numbers.get(2);
        }
        else if ("hsbw".equals(name))
        {
            leftSideBearing = new Point2D.Float(numbers.get(0), 0);
            width = numbers.get(1);
        }
        else if ("rmoveto".equals(name))
        {
            rmoveTo(numbers.get(0), numbers.get(1));
        }
        else if ("hmoveto".equals(name))
        {
            rmoveTo(numbers.get(0), 0);
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
    }

    /**
     * Relative moveto.
     */
    private void rmoveTo(Number dx, Number dy)
    {
        Point2D point = referencePoint;
        if (point == null)
        {
            point = path.getCurrentPoint();
            if (point == null)
            {
                point = leftSideBearing;
            }
        }
        referencePoint = null;
        path.moveTo((float)(point.getX() + dx.doubleValue()),
                    (float)(point.getY() + dy.doubleValue()));
    }

    /**
     * Relative lineto.
     */
    private void rlineTo(Number dx, Number dy)
    {
        Point2D point = path.getCurrentPoint();
        path.lineTo((float)(point.getX() + dx.doubleValue()),
                    (float)(point.getY() + dy.doubleValue()));
    }

    /**
     * Relative curveto.
     */
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

    /**
     * Close path.
     */
    private void closepath()
    {
        referencePoint = path.getCurrentPoint();
        path.closePath();
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
                Type1CharString base = cffFont.getType1CharString(baseName);
                path.append(base.getPath().getPathIterator(null), false);
            }
            catch (IOException e)
            {
                LOG.warn("invalid character in seac command");
            }
        }
        // accent character
        String accentName = StandardEncoding.INSTANCE.getName(achar.intValue());
        if (accentName != null)
        {
            try
            {
                Type1CharString accent = cffFont.getType1CharString(accentName);
                AffineTransform at = AffineTransform.getTranslateInstance(
                    leftSideBearing.getX() + adx.floatValue(),
                    leftSideBearing.getY() + ady.floatValue());
                path.append(accent.getPath().getPathIterator(at), false);
            }
            catch (IOException e)
            {
                LOG.warn("invalid character in seac command");
            }
        }
    }
}