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

import java.util.ArrayList;
import java.util.List;

import org.fontbox.util.BoundingBox;

/**
 * This class represents a single character metric.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class CharMetric
{
    private int characterCode;

    private float wx;
    private float w0x;
    private float w1x;

    private float wy;
    private float w0y;
    private float w1y;

    private float[] w;
    private float[] w0;
    private float[] w1;
    private float[] vv;

    private String name;
    private BoundingBox boundingBox;
    private List ligatures = new ArrayList();

    /** Getter for property boundingBox.
     * @return Value of property boundingBox.
     */
    public BoundingBox getBoundingBox()
    {
        return boundingBox;
    }

    /** Setter for property boundingBox.
     * @param bBox New value of property boundingBox.
     */
    public void setBoundingBox(BoundingBox bBox)
    {
        boundingBox = bBox;
    }

    /** Getter for property characterCode.
     * @return Value of property characterCode.
     */
    public int getCharacterCode()
    {
        return characterCode;
    }

    /** Setter for property characterCode.
     * @param cCode New value of property characterCode.
     */
    public void setCharacterCode(int cCode)
    {
        characterCode = cCode;
    }

    /**
     * This will add an entry to the list of ligatures.
     *
     * @param ligature The ligature to add.
     */
    public void addLigature( Ligature ligature )
    {
        ligatures.add( ligature );
    }

    /** Getter for property ligatures.
     * @return Value of property ligatures.
     */
    public List getLigatures()
    {
        return ligatures;
    }

    /** Setter for property ligatures.
     * @param lig New value of property ligatures.
     */
    public void setLigatures(List lig)
    {
        this.ligatures = lig;
    }

    /** Getter for property name.
     * @return Value of property name.
     */
    public String getName()
    {
        return name;
    }

    /** Setter for property name.
     * @param n New value of property name.
     */
    public void setName(String n)
    {
        this.name = n;
    }

    /** Getter for property vv.
     * @return Value of property vv.
     */
    public float[] getVv()
    {
        return this.vv;
    }

    /** Setter for property vv.
     * @param vvValue New value of property vv.
     */
    public void setVv(float[] vvValue)
    {
        this.vv = vvValue;
    }

    /** Getter for property w.
     * @return Value of property w.
     */
    public float[] getW()
    {
        return this.w;
    }

    /** Setter for property w.
     * @param wValue New value of property w.
     */
    public void setW(float[] wValue)
    {
        this.w = wValue;
    }

    /** Getter for property w0.
     * @return Value of property w0.
     */
    public float[] getW0()
    {
        return this.w0;
    }

    /** Setter for property w0.
     * @param w0Value New value of property w0.
     */
    public void setW0(float[] w0Value)
    {
        w0 = w0Value;
    }

    /** Getter for property w0x.
     * @return Value of property w0x.
     */
    public float getW0x()
    {
        return w0x;
    }

    /** Setter for property w0x.
     * @param w0xValue New value of property w0x.
     */
    public void setW0x(float w0xValue)
    {
        w0x = w0xValue;
    }

    /** Getter for property w0y.
     * @return Value of property w0y.
     */
    public float getW0y()
    {
        return w0y;
    }

    /** Setter for property w0y.
     * @param w0yValue New value of property w0y.
     */
    public void setW0y(float w0yValue)
    {
        w0y = w0yValue;
    }

    /** Getter for property w1.
     * @return Value of property w1.
     */
    public float[] getW1()
    {
        return this.w1;
    }

    /** Setter for property w1.
     * @param w1Value New value of property w1.
     */
    public void setW1(float[] w1Value)
    {
        w1 = w1Value;
    }

    /** Getter for property w1x.
     * @return Value of property w1x.
     */
    public float getW1x()
    {
        return w1x;
    }

    /** Setter for property w1x.
     * @param w1xValue New value of property w1x.
     */
    public void setW1x(float w1xValue)
    {
        w1x = w1xValue;
    }

    /** Getter for property w1y.
     * @return Value of property w1y.
     */
    public float getW1y()
    {
        return w1y;
    }

    /** Setter for property w1y.
     * @param w1yValue New value of property w1y.
     */
    public void setW1y(float w1yValue)
    {
        w1y = w1yValue;
    }

    /** Getter for property wx.
     * @return Value of property wx.
     */
    public float getWx()
    {
        return wx;
    }

    /** Setter for property wx.
     * @param wxValue New value of property wx.
     */
    public void setWx(float wxValue)
    {
        wx = wxValue;
    }

    /** Getter for property wy.
     * @return Value of property wy.
     */
    public float getWy()
    {
        return wy;
    }

    /** Setter for property wy.
     * @param wyValue New value of property wy.
     */
    public void setWy(float wyValue)
    {
        this.wy = wyValue;
    }

}