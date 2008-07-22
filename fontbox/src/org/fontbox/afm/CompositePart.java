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
 * This class represents a part of composite character data.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class CompositePart
{
    private String name;
    private int xDisplacement;
    private int yDisplacement;

    /** Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName()
    {
        return name;
    }

    /** Setter for property name.
     * @param nameValue New value of property name.
     */
    public void setName(String nameValue)
    {
        name = nameValue;
    }

    /** Getter for property xDisplacement.
     * @return Value of property xDisplacement.
     */
    public int getXDisplacement()
    {
        return xDisplacement;
    }

    /** Setter for property xDisplacement.
     * @param xDisp New value of property xDisplacement.
     */
    public void setXDisplacement(int xDisp)
    {
        xDisplacement = xDisp;
    }

    /** Getter for property yDisplacement.
     * @return Value of property yDisplacement.
     */
    public int getYDisplacement()
    {
        return yDisplacement;
    }

    /** Setter for property yDisplacement.
     * @param yDisp New value of property yDisplacement.
     */
    public void setYDisplacement(int yDisp)
    {
        yDisplacement = yDisp;
    }

}