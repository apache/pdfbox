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
package org.apache.pdfbox.util;

/**
 * A 2D vector.
 *
 * @author John Hewson
 */
public final class Vector
{
    private final float x, y;

    public Vector(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x magnitude.
     */
    public float getX()
    {
        return x;
    }

    /**
     * Returns the y magnitude.
     */
    public float getY()
    {
        return y;
    }

    /**
     * Returns a new vector scaled by both x and y.
     *
     * @param sxy x and y scale
     */
    public Vector scale(float sxy)
    {
        return new Vector(x * sxy, y * sxy);
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }
}
