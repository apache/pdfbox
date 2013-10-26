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
public class GlyfSimpleDescript extends GlyfDescript
{

    private int[] endPtsOfContours;
    private byte[] flags;
    private short[] xCoordinates;
    private short[] yCoordinates;
    private int pointCount;

    /**
     * Constructor.
     * 
     * @param numberOfContours number of contours
     * @param bais the stream to be read
     * @throws IOException is thrown if something went wrong
     */
    public GlyfSimpleDescript(short numberOfContours, TTFDataStream bais) throws IOException
    {
        super(numberOfContours, bais);

        /*
         * https://developer.apple.com/fonts/TTRefMan/RM06/Chap6glyf.html
         * "If a glyph has zero contours, it need not have any glyph data." set the pointCount to zero to initialize
         * attributes and avoid nullpointer but maybe there shouldn't have GlyphDescript in the GlyphData?
         */
        if (numberOfContours == 0)
        {
            pointCount = 0;
            return;
        }

        // Simple glyph description
        endPtsOfContours = new int[numberOfContours];
        endPtsOfContours = bais.readUnsignedShortArray(numberOfContours);

        // The last end point index reveals the total number of points
        pointCount = endPtsOfContours[numberOfContours - 1] + 1;

        flags = new byte[pointCount];
        xCoordinates = new short[pointCount];
        yCoordinates = new short[pointCount];

        int instructionCount = bais.readUnsignedShort();
        readInstructions(bais, instructionCount);
        readFlags(pointCount, bais);
        readCoords(pointCount, bais);
    }

    /**
     * {@inheritDoc}
     */
    public int getEndPtOfContours(int i)
    {
        return endPtsOfContours[i];
    }

    /**
     * {@inheritDoc}
     */
    public byte getFlags(int i)
    {
        return flags[i];
    }

    /**
     * {@inheritDoc}
     */
    public short getXCoordinate(int i)
    {
        return xCoordinates[i];
    }

    /**
     * {@inheritDoc}
     */
    public short getYCoordinate(int i)
    {
        return yCoordinates[i];
    }

    /**
     * {@inheritDoc}
     */
    public boolean isComposite()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int getPointCount()
    {
        return pointCount;
    }

    /**
     * The table is stored as relative values, but we'll store them as absolutes.
     */
    private void readCoords(int count, TTFDataStream bais) throws IOException
    {
        short x = 0;
        short y = 0;
        for (int i = 0; i < count; i++)
        {
            if ((flags[i] & X_DUAL) != 0)
            {
                if ((flags[i] & X_SHORT_VECTOR) != 0)
                {
                    x += (short) bais.readUnsignedByte();
                }
            }
            else
            {
                if ((flags[i] & X_SHORT_VECTOR) != 0)
                {
                    x += (short) -((short) bais.readUnsignedByte());
                }
                else
                {
                    x += bais.readSignedShort();
                }
            }
            xCoordinates[i] = x;
        }

        for (int i = 0; i < count; i++)
        {
            if ((flags[i] & Y_DUAL) != 0)
            {
                if ((flags[i] & Y_SHORT_VECTOR) != 0)
                {
                    y += (short) bais.readUnsignedByte();
                }
            }
            else
            {
                if ((flags[i] & Y_SHORT_VECTOR) != 0)
                {
                    y += (short) -((short) bais.readUnsignedByte());
                }
                else
                {
                    y += bais.readSignedShort();
                }
            }
            yCoordinates[i] = y;
        }
    }

    /**
     * The flags are run-length encoded.
     */
    private void readFlags(int flagCount, TTFDataStream bais) throws IOException
    {
        try
        {
            for (int index = 0; index < flagCount; index++)
            {
                flags[index] = (byte) bais.readUnsignedByte();
                if ((flags[index] & REPEAT) != 0)
                {
                    int repeats = bais.readUnsignedByte();
                    for (int i = 1; i <= repeats; i++)
                    {
                        flags[index + i] = flags[index];
                    }
                    index += repeats;
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            System.out.println("error: array index out of bounds");
        }
    }

}
