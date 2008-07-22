/**
 * Copyright (c) 2005, www.fontbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of fontbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.fontbox.org
 *
 */
package org.fontbox.afm;

/**
 * This class represents a piece of track kerning data.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
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