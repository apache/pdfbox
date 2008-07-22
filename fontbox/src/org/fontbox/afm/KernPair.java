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
 * This represents some kern pair data.
 *
 * @author Ben Litchfield (ben@benlitchfield.com)
 * @version $Revision: 1.1 $
 */
public class KernPair
{
    private String firstKernCharacter;
    private String secondKernCharacter;
    private float x;
    private float y;

    /** Getter for property firstKernCharacter.
     * @return Value of property firstKernCharacter.
     */
    public java.lang.String getFirstKernCharacter()
    {
        return firstKernCharacter;
    }

    /** Setter for property firstKernCharacter.
     * @param firstKernCharacterValue New value of property firstKernCharacter.
     */
    public void setFirstKernCharacter(String firstKernCharacterValue)
    {
        firstKernCharacter = firstKernCharacterValue;
    }

    /** Getter for property secondKernCharacter.
     * @return Value of property secondKernCharacter.
     */
    public java.lang.String getSecondKernCharacter()
    {
        return secondKernCharacter;
    }

    /** Setter for property secondKernCharacter.
     * @param secondKernCharacterValue New value of property secondKernCharacter.
     */
    public void setSecondKernCharacter(String secondKernCharacterValue)
    {
        secondKernCharacter = secondKernCharacterValue;
    }

    /** Getter for property x.
     * @return Value of property x.
     */
    public float getX()
    {
        return x;
    }

    /** Setter for property x.
     * @param xValue New value of property x.
     */
    public void setX(float xValue)
    {
        x = xValue;
    }

    /** Getter for property y.
     * @return Value of property y.
     */
    public float getY()
    {
        return y;
    }

    /** Setter for property y.
     * @param yValue New value of property y.
     */
    public void setY(float yValue)
    {
        y = yValue;
    }

}