/**
 * Copyright (c) 2004, www.pdfbox.org
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
package org.pdfbox.pdmodel.interactive.action.type;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.pdmodel.common.PDTextStream;

/**
 * This represents a JavaScript action.
 *
 * @author Michael Schwarzenberger (mi2kee@gmail.com)
 * @version $Revision: 1.1 $
 */
public class PDActionJavaScript extends PDAction
{
    /**
     * This type of action this object represents.
     */
    public static final String SUB_TYPE = "JavaScript";
    
    /**
     * Constructor #1.
     */
    public PDActionJavaScript()
    {
        super();
        setSubType( SUB_TYPE );
    }
    
    /**
     * Constructor.
     * 
     * @param js Some javascript code.
     */
    public PDActionJavaScript( String js )
    {
        this();
        setAction( js );
    }

    /**
     * Constructor #2.
     * 
     *  @param a The action dictionary.
     */
    public PDActionJavaScript(COSDictionary a)
    {
        super(a);
    }
    
    /**
     * @param sAction The JavaScript.
     */
    public void setAction(PDTextStream sAction)
    {
        action.setItem("JS", sAction);
    }
    
    /**
     * @param sAction The JavaScript.
     */
    public void setAction(String sAction)
    {
        action.setString("JS", sAction);
    }
  
    /**
     * @return The Javascript Code.
     */
    public PDTextStream getAction()
    {
        return PDTextStream.createTextStream( action.getDictionaryObject("JS") );
    }  
}