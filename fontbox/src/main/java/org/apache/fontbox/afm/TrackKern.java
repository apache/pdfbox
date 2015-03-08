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
    private int degree;
    private float minPointSize;
    private float minKern;
    private float maxPointSize;
    private float maxKern;

    /** Getter for property degree.
     * @return Value of property degree.
     */
    public int getDegree()
    {
        return degree;
    }

    /** Setter for property degree.
     * @param degreeValue New value of property degree.
     */
    public void setDegree(int degreeValue)
    {
        degree = degreeValue;
    }

    /** Getter for property maxKern.
     * @return Value of property maxKern.
     */
    public float getMaxKern()
    {
        return maxKern;
    }

    /** Setter for property maxKern.
     * @param maxKernValue New value of property maxKern.
     */
    public void setMaxKern(float maxKernValue)
    {
        maxKern = maxKernValue;
    }

    /** Getter for property maxPointSize.
     * @return Value of property maxPointSize.
     */
    public float getMaxPointSize()
    {
        return maxPointSize;
    }

    /** Setter for property maxPointSize.
     * @param maxPointSizeValue New value of property maxPointSize.
     */
    public void setMaxPointSize(float maxPointSizeValue)
    {
        maxPointSize = maxPointSizeValue;
    }

    /** Getter for property minKern.
     * @return Value of property minKern.
     */
    public float getMinKern()
    {
        return minKern;
    }

    /** Setter for property minKern.
     * @param minKernValue New value of property minKern.
     */
    public void setMinKern(float minKernValue)
    {
        minKern = minKernValue;
    }

    /** Getter for property minPointSize.
     * @return Value of property minPointSize.
     */
    public float getMinPointSize()
    {
        return minPointSize;
    }

    /** Setter for property minPointSize.
     * @param minPointSizeValue New value of property minPointSize.
     */
    public void setMinPointSize(float minPointSizeValue)
    {
        minPointSize = minPointSizeValue;
    }

}