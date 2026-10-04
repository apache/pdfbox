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

import java.math.BigInteger;

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
     * Builds a unit vector in F2Dot14 from a coordinate delta. Each component is the exact quotient
     * {@code d / |(dx, dy)|} in 16.16 fixed point, rounded to nearest, then reduced to F2Dot14 by
     * truncation towards zero - the reduction FreeType uses, so diagonal vectors agree with it. A
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
        if (dx == 0)
        {
            return new UnitVector(0, dy > 0 ? Fixed.ONE_F2DOT14 : -Fixed.ONE_F2DOT14);
        }
        if (dy == 0)
        {
            return new UnitVector(dx > 0 ? Fixed.ONE_F2DOT14 : -Fixed.ONE_F2DOT14, 0);
        }
        BigInteger lengthSquared = BigInteger.valueOf(dx).pow(2).add(BigInteger.valueOf(dy).pow(2));
        return new UnitVector(component(dx, lengthSquared) / 4, component(dy, lengthSquared) / 4);
    }

    /**
     * {@code round(d * 65536 / sqrt(lengthSquared))} computed exactly: the result r is the integer
     * whose interval [r - 1/2, r + 1/2) contains the quotient, found from the squared inequality
     * {@code (2|d| * 65536)^2 >= (2r - 1)^2 * lengthSquared}, so no floating point is involved.
     */
    private static int component(int d, BigInteger lengthSquared)
    {
        BigInteger numerator = BigInteger.valueOf(2L * Math.abs((long) d) * 0x10000L).pow(2);
        // the quotient is at most 65536; start from the floating-point estimate and correct it
        long r = Math.round(Math.abs((double) d) * 0x10000 / Math.sqrt(lengthSquared.doubleValue()));
        while (r > 0 && BigInteger.valueOf(2 * r - 1).pow(2).multiply(lengthSquared).compareTo(numerator) > 0)
        {
            r--;
        }
        while (BigInteger.valueOf(2 * r + 1).pow(2).multiply(lengthSquared).compareTo(numerator) <= 0)
        {
            r++;
        }
        return (int) (d < 0 ? -r : r);
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
