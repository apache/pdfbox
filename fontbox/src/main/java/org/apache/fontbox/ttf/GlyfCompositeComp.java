/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.fontbox.ttf;

import java.io.IOException;

/**
 * This class is based on code from Apache Batik a subproject of Apache XMLGraphics. see
 * http://xmlgraphics.apache.org/batik/ for further details.
 */
public class GlyfCompositeComp
{

    // Flags for composite glyphs.

    /**
     * If set, the arguments are words; otherwise, they are bytes.
     */
    protected static final short ARG_1_AND_2_ARE_WORDS = 0x0001;
    /**
     * If set, the arguments are xy values; otherwise they are points.
     */
    protected static final short ARGS_ARE_XY_VALUES = 0x0002;
    /**
     * If set, xy values are rounded to those of the closest grid lines.
     */
    protected static final short ROUND_XY_TO_GRID = 0x0004;
    /**
     * If set, there is a simple scale; otherwise, scale = 1.0.
     */
    protected static final short WE_HAVE_A_SCALE = 0x0008;
    /**
     * Indicates at least one more glyph after this one.
     */
    protected static final short MORE_COMPONENTS = 0x0020;
    /**
     * The x direction will use a different scale from the y direction.
     */
    protected static final short WE_HAVE_AN_X_AND_Y_SCALE = 0x0040;
    /**
     * There is a 2 by2 transformation that will be used to scale the component.
     */
    protected static final short WE_HAVE_A_TWO_BY_TWO = 0x0080;
    /**
     * Following the last component are instructions for the composite character.
     */
    protected static final short WE_HAVE_INSTRUCTIONS = 0x0100;
    /**
     * If set, this forces the aw and lsb (and rsb) for the composite to be equal to those from this original glyph.
     */
    protected static final short USE_MY_METRICS = 0x0200;

    private int firstIndex;
    private int firstContour;
    private short argument1;
    private short argument2;
    private short flags;
    private int glyphIndex;
    private double xscale = 1.0;
    private double yscale = 1.0;
    private double scale01 = 0.0;
    private double scale10 = 0.0;
    private int xtranslate = 0;
    private int ytranslate = 0;
    private int point1 = 0;
    private int point2 = 0;

    /**
     * Constructor.
     * 
     * @param bais the stream to be read
     * @throws IOException is thrown if something went wrong
     */
    protected GlyfCompositeComp(TTFDataStream bais) throws IOException
    {
        flags = bais.readSignedShort();
        glyphIndex = bais.readUnsignedShort();// number of glyph in a font is uint16

        // Get the arguments as just their raw values
        if ((flags & ARG_1_AND_2_ARE_WORDS) != 0)
        {
            argument1 = bais.readSignedShort();
            argument2 = bais.readSignedShort();
        }
        else
        {
            argument1 = (short) bais.readUnsignedByte();
            argument2 = (short) bais.readUnsignedByte();
        }

        // Assign the arguments according to the flags
        if ((flags & ARGS_ARE_XY_VALUES) != 0)
        {
            xtranslate = argument1;
            ytranslate = argument2;
        }
        else
        {
            // TODO unused?
            point1 = argument1;
            point2 = argument2;
        }

        // Get the scale values (if any)
        if ((flags & WE_HAVE_A_SCALE) != 0)
        {
            int i = bais.readSignedShort();
            xscale = yscale = (double) i / (double) 0x4000;
        }
        else if ((flags & WE_HAVE_AN_X_AND_Y_SCALE) != 0)
        {
            short i = bais.readSignedShort();
            xscale = (double) i / (double) 0x4000;
            i = bais.readSignedShort();
            yscale = (double) i / (double) 0x4000;
        }
        else if ((flags & WE_HAVE_A_TWO_BY_TWO) != 0)
        {
            int i = bais.readSignedShort();
            xscale = (double) i / (double) 0x4000;
            i = bais.readSignedShort();
            scale01 = (double) i / (double) 0x4000;
            i = bais.readSignedShort();
            scale10 = (double) i / (double) 0x4000;
            i = bais.readSignedShort();
            yscale = (double) i / (double) 0x4000;
        }
    }

    /**
     * Sets the first index.
     * 
     * @param idx the first index
     */
    public void setFirstIndex(int idx)
    {
        firstIndex = idx;
    }

    /**
     * Returns the first index.
     * 
     * @return the first index.
     */
    public int getFirstIndex()
    {
        return firstIndex;
    }

    /**
     * Sets the index for the first contour.
     * 
     * @param idx the index of the first contour
     */
    public void setFirstContour(int idx)
    {
        firstContour = idx;
    }

    /**
     * Returns the index of the first contour.
     * 
     * @return the index of the first contour.
     */
    public int getFirstContour()
    {
        return firstContour;
    }

    /**
     * Returns argument 1.
     * 
     * @return argument 1.
     */
    public short getArgument1()
    {
        return argument1;
    }

    /**
     * Returns argument 2.
     * 
     * @return argument 2.
     */
    public short getArgument2()
    {
        return argument2;
    }

    /**
     * Returns the flags of the glyph.
     * 
     * @return the flags.
     */
    public short getFlags()
    {
        return flags;
    }

    /**
     * Returns the index of the first contour.
     * 
     * @return index of the first contour.
     */
    public int getGlyphIndex()
    {
        return glyphIndex;
    }

    /**
     * Returns the scale-01 value.
     * 
     * @return the scale-01 value.
     */
    public double getScale01()
    {
        return scale01;
    }

    /**
     * Returns the scale-10 value.
     * 
     * @return the scale-10 value.
     */
    public double getScale10()
    {
        return scale10;
    }

    /**
     * Returns the x-scaling value.
     * 
     * @return the x-scaling value.
     */
    public double getXScale()
    {
        return xscale;
    }

    /**
     * Returns the y-scaling value.
     * 
     * @return the y-scaling value.
     */
    public double getYScale()
    {
        return yscale;
    }

    /**
     * Returns the x-translation value.
     * 
     * @return the x-translation value.
     */
    public int getXTranslate()
    {
        return xtranslate;
    }

    /**
     * Returns the y-translation value.
     * 
     * @return the y-translation value.
     */
    public int getYTranslate()
    {
        return ytranslate;
    }

    /**
     * Transforms an x-coordinate of a point for this component.
     * 
     * @param x The x-coordinate of the point to transform
     * @param y The y-coordinate of the point to transform
     * @return The transformed x-coordinate
     */
    public int scaleX(int x, int y)
    {
        return Math.round((float) (x * xscale + y * scale10));
    }

    /**
     * Transforms a y-coordinate of a point for this component.
     * 
     * @param x The x-coordinate of the point to transform
     * @param y The y-coordinate of the point to transform
     * @return The transformed y-coordinate
     */
    public int scaleY(int x, int y)
    {
        return Math.round((float) (x * scale01 + y * yscale));
    }
}
