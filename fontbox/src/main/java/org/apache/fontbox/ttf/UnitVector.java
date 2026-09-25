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
package org.apache.fontbox.ttf;

/**
 * A 2D unit vector in F2Dot14 fixed point, used for the projection, freedom and dual-projection
 * vectors of the TrueType graphics state. Kept as a mutable class (not {@code Point2D.Float}) so the
 * interpreter can stay in integer math. Because they are mutable, {@link GraphicsState#copy()}
 * deep-copies them, so a per-glyph clone cannot write through to the saved post-{@code prep} template.
 * <p>
 * Named after FreeType's {@code FT_UnitVector}, and like it the unit length is a convention rather than
 * an enforced invariant: {@code SPVFS} and {@code SFVFS} write whatever the font pushed on the stack,
 * which a malformed font need not have normalized.
 *
 * @author Apache PDFBox
 */
class UnitVector
{
    private int x;
    private int y;

    /**
     * @param x the x component in F2Dot14
     * @param y the y component in F2Dot14
     */
    public UnitVector(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x axis unit vector (1, 0)
     */
    public static UnitVector xAxis()
    {
        return new UnitVector(Fixed.ONE_F2DOT14, 0);
    }

    /**
     * @return the y axis unit vector (0, 1)
     */
    public static UnitVector yAxis()
    {
        return new UnitVector(0, Fixed.ONE_F2DOT14);
    }

    /**
     * Builds a unit vector in F2Dot14 from a coordinate delta, bit-for-bit as FreeType does
     * (ttinterp.c Normalize: FT_Vector_NormLen to 16.16, then divided by 4 with truncation). A
     * zero-length delta falls back to the x axis.
     *
     * @param dx the x delta
     * @param dy the y delta
     * @return the normalized unit vector
     */
    public static UnitVector normalize(int dx, int dy)
    {
        if (dx == 0 && dy == 0)
        {
            return xAxis();
        }
        int[] v = normLen(dx, dy);
        return new UnitVector(v[0] / 4, v[1] / 4);
    }

    /**
     * FreeType's FT_Vector_NormLen: scales a non-zero vector to unit length in 16.16 fixed point
     * using integer Newton iterations. Java's int arithmetic wraps like the C code's 32-bit
     * unsigned/signed arithmetic, which the algorithm relies on.
     */
    private static int[] normLen(int vx, int vy)
    {
        int sx = vx < 0 ? -1 : 1;
        int sy = vy < 0 ? -1 : 1;
        int x = Math.abs(vx);
        int y = Math.abs(vy);
        if (x == 0)
        {
            return new int[] { 0, sy * 0x10000 };
        }
        if (y == 0)
        {
            return new int[] { sx * 0x10000, 0 };
        }
        // estimate the length and prenormalize so it lies between 2/3 and 4/3 in 16.16
        long l = x > y ? x + (y >>> 1) : y + (x >>> 1);
        int msb = 63 - Long.numberOfLeadingZeros(l);
        int shift = 31 - msb;
        shift -= 15 + (l >= (0xAAAAAAAAL >>> shift) ? 1 : 0);
        if (shift > 0)
        {
            x <<= shift;
            y <<= shift;
            l = Integer.toUnsignedLong(x) > Integer.toUnsignedLong(y)
                    ? Integer.toUnsignedLong(x) + (Integer.toUnsignedLong(y) >>> 1)
                    : Integer.toUnsignedLong(y) + (Integer.toUnsignedLong(x) >>> 1);
        }
        else
        {
            x >>>= -shift;
            y >>>= -shift;
            l >>>= -shift;
        }
        // lower linear approximation for reciprocal length minus one, then Newton's iterations
        int b = 0x10000 - (int) l;
        int u;
        int w;
        int z;
        do
        {
            u = x + (x * b >> 16);
            w = y + (y * b >> 16);
            z = -(u * u + w * w) / 0x200;
            z = z * ((0x10000 + b) >> 8) / 0x10000;
            b += z;
        }
        while (z > 0);
        return new int[] { sx * u, sy * w };
    }

    /**
     * @return this vector rotated 90 degrees counter-clockwise, i.e. {@code (-y, x)}
     */
    public UnitVector perpendicular()
    {
        return new UnitVector(-y, x);
    }

    /**
     * @return the x component in F2Dot14
     */
    public int getX()
    {
        return x;
    }

    /**
     * @return the y component in F2Dot14
     */
    public int getY()
    {
        return y;
    }

    /**
     * @param x the x component in F2Dot14
     * @param y the y component in F2Dot14
     */
    public void set(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * @return an independent copy of this vector
     */
    public UnitVector copy()
    {
        return new UnitVector(x, y);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof UnitVector))
        {
            return false;
        }
        UnitVector other = (UnitVector) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode()
    {
        return 31 * x + y;
    }

    @Override
    public String toString()
    {
        return "UnitVector(" + x + ", " + y + ")";
    }
}
