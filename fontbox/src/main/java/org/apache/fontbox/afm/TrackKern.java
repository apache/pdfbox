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
package org.apache.fontbox.afm;

/**
 * This class represents a piece of track kerning data.
 *
 * @author Ben Litchfield
 */
public class TrackKern
{
    private final int degree;
    private final float minPointSize;
    private final float minKern;
    private final float maxPointSize;
    private final float maxKern;

    public TrackKern(int degree, float minPointSize, float minKern, float maxPointSize,
            float maxKern)
    {
        this.degree = degree;
        this.minPointSize = minPointSize;
        this.minKern = minKern;
        this.maxPointSize = maxPointSize;
        this.maxKern = maxKern;
    }

    /** Getter for property degree.
     * @return Value of property degree.
     */
    public int getDegree()
    {
        return degree;
    }

    /** Getter for property maxKern.
     * @return Value of property maxKern.
     */
    public float getMaxKern()
    {
        return maxKern;
    }

    /** Getter for property maxPointSize.
     * @return Value of property maxPointSize.
     */
    public float getMaxPointSize()
    {
        return maxPointSize;
    }

    /** Getter for property minKern.
     * @return Value of property minKern.
     */
    public float getMinKern()
    {
        return minKern;
    }

    /** Getter for property minPointSize.
     * @return Value of property minPointSize.
     */
    public float getMinPointSize()
    {
        return minPointSize;
    }
}