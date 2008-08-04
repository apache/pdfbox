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

import java.util.ArrayList;
import java.util.List;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSArrayList;
import org.pdfbox.pdmodel.common.PDDestinationOrAction;
import org.pdfbox.pdmodel.interactive.action.PDActionFactory;

/**
 * This represents an action that can be executed in a PDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Panagiotis Toumasis (ptoumasis@mail.gr)
 * @version $Revision: 1.3 $
 */
public abstract class PDAction implements PDDestinationOrAction
{
    /**
     * The type of PDF object.
     */
    public static final String TYPE = "Action";
    
    /**
     * The action dictionary.
     */
    protected COSDictionary action;

    /**
     * Default constructor.
     */
    public PDAction()
    {
        action = new COSDictionary();
        setType( TYPE );
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDAction( COSDictionary a )
    {
        action = a;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return action;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return action;
    }

    /**
     * This will get the type of PDF object that the actions dictionary describes.
     * If present must be Action for an action dictionary.
     *
     * @return The Type of PDF object.
     */
    public String getType()
    {
       return action.getNameAsString( "Type" );
    }

    /**
     * This will set the type of PDF object that the actions dictionary describes.
     * If present must be Action for an action dictionary.
     *
     * @param type The new Type for the PDF object.
     */
    public void setType( String type )
    {
       action.setName( "Type", type );
    }

    /**
     * This will get the type of action that the actions dictionary describes.
     * If present, must be Action for an action dictionary.
     *
     * @return The S entry of actions dictionary.
     */
    public String getSubType()
    {
       return action.getNameAsString( "S" );
    }

    /**
     * This will set the type of action that the actions dictionary describes.
     * If present, must be Action for an action dictionary.
     *
     * @param s The new type of action.
     */
    public void setSubType( String s )
    {
       action.setName( "S", s );
    }

    /**
     * This will get the next action, or sequence of actions, to be performed after this one.
     * The value is either a single action dictionary or an array of action dictionaries
     * to be performed in order.
     *
     * @return The Next action or sequence of actions.
     */
    public List getNext()
    {
        List retval = null;
        COSBase next = action.getDictionaryObject( "Next" );
        if( next instanceof COSDictionary )
        {
            PDAction pdAction = PDActionFactory.createAction( (COSDictionary) next );
            retval = new COSArrayList(pdAction, next, action, "Next" );
        }
        else if( next instanceof COSArray )
        {
            COSArray array = (COSArray)next;
            List actions = new ArrayList();
            for( int i=0; i<array.size(); i++ )
            {
                actions.add( PDActionFactory.createAction( (COSDictionary) array.getObject( i )));
            }
            retval = new COSArrayList( actions, array );
        }

        return retval;
    }

    /**
     * This will set the next action, or sequence of actions, to be performed after this one.
     * The value is either a single action dictionary or an array of action dictionaries
     * to be performed in order.
     *
     * @param next The Next action or sequence of actions.
     */
    public void setNext( List next )
    {
        action.setItem( "Next", COSArrayList.converterToCOSArray( next ) );
    }
}