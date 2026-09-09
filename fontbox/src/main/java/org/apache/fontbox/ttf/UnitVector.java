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
     * Builds a unit vector in F2Dot14 from a coordinate delta. A zero-length delta falls back to the
     * x axis. (The square-root normalization is the one place the interpreter steps outside integer
     * math; it only affects a direction vector, and is verified by the golden tests.)
     *
     * @param dx the x delta
     * @param dy the y delta
     * @return the normalized unit vector
     */
    public static UnitVector normalize(int dx, int dy)
    {
        double length = Math.hypot(dx, dy);
        if (length == 0)
        {
            return xAxis();
        }
        int ux = (int) Math.round(dx / length * Fixed.ONE_F2DOT14);
        int uy = (int) Math.round(dy / length * Fixed.ONE_F2DOT14);
        return new UnitVector(ux, uy);
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
