/**
 * Copyright (c) 2003-2006, www.pdfbox.org
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
 * 3. Neither the name of pdfbox; nor the names of its
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
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.persistence.util;

import org.pdfbox.cos.COSObject;

/**
 * Object representing the physical reference to an indirect pdf object.
 *
 * @author Michael Traut
 * @version $Revision: 1.5 $
 */
public class COSObjectKey
{
    private long number;
    private long generation;

    /**
     * PDFObjectKey constructor comment.
     *
     * @param object The object that this key will represent.
     */
    public COSObjectKey(COSObject object)
    {
        this( object.getObjectNumber().longValue(), object.getGenerationNumber().longValue() );
    }
    
    /**
     * PDFObjectKey constructor comment.
     *
     * @param num The object number.
     * @param gen The object generation number.
     */
    public COSObjectKey(long num, long gen)
    {
        setNumber(num);
        setGeneration(gen);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj)
    {
        return (obj instanceof COSObjectKey) &&
               ((COSObjectKey)obj).getNumber() == getNumber() &&
               ((COSObjectKey)obj).getGeneration() == getGeneration();
    }

    /**
     * This will get the generation number.
     *
     * @return The objects generation number.
     */
    public long getGeneration()
    {
        return generation;
    }
    /**
     * This will get the objects id.
     *
     * @return The object's id.
     */
    public long getNumber()
    {
        return number;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return (int)(number + generation);
    }
    /**
     * This will set the objects generation number.
     *
     * @param newGeneration The objects generation number.
     */
    public void setGeneration(long newGeneration)
    {
        generation = newGeneration;
    }
    /**
     * This will set the objects id.
     *
     * @param newNumber The objects number.
     */
    public void setNumber(long newNumber)
    {
        number = newNumber;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "" + getNumber() + " " + getGeneration() + " R";
    }
}