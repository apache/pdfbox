/**
 * Copyright (c) 2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.documentinterchange.logicalstructure;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.pdmodel.common.COSObjectable;

/**
 * The MarkInfo provides additional information relevant to specialized
 * uses of structured documents.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public class PDMarkInfo implements COSObjectable
{
    private COSDictionary dictionary;
    
    /**
     * Default Constructor.
     *
     */
    public PDMarkInfo()
    {
        dictionary = new COSDictionary();
    }
    
    /**
     * Constructor for an existing MarkInfo element.
     * 
     * @param dic The existing dictionary.
     */
    public PDMarkInfo( COSDictionary dic )
    {
        dictionary = dic;
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return dictionary;
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getDictionary()
    {
        return dictionary;
    }
    
    /**
     * Tells if this is a tagged PDF.
     * 
     * @return true If this is a tagged PDF.
     */
    public boolean isMarked()
    {
        return dictionary.getBoolean( "Marked", false );
    }
    
    /**
     * Set if this is a tagged PDF.
     * 
     * @param value The new marked value.
     */
    public void setMarked( boolean value )
    {
        dictionary.setBoolean( "Marked", value );
    }
    
    /**
     * Tells if structure elements use user properties.
     * 
     * @return A boolean telling if the structure elements use user properties.
     */
    public boolean usesUserProperties()
    {
        return dictionary.getBoolean( "UserProperties", false );
    }
    
    /**
     * Set if the structure elements contain user properties.
     * 
     * @param userProps The new value for this property.
     */
    public void setUserProperties( boolean userProps )
    {
        dictionary.setBoolean( "UserProperties", userProps );
    }
    
    /**
     * Tells if this PDF contain 'suspect' tags.  See PDF Reference 1.6 
     * section 10.6 "Logical Structure" for more information about this property.
     *  
     * @return true if the suspect flag has been set.
     */
    public boolean isSuspect()
    {
        return dictionary.getBoolean( "Suspects", false );
    }
    
    /**
     * Set the value of the suspects property.  See PDF Reference 1.6 
     * section 10.6 "Logical Structure" for more information about this 
     * property.
     * 
     * @param suspect The new "Suspects" value.
     */
    public void setSuspect( boolean suspect )
    {
        dictionary.setBoolean( "Suspects", false );
    }
}
