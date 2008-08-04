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
package org.pdfbox.pdmodel.interactive.action;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSObjectable;
import org.pdfbox.pdmodel.interactive.action.type.PDAction;

/**
 * This class represents a form field's dictionary of actions
 * that occur due to events.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Panagiotis Toumasis (ptoumasis@mail.gr)
 * @version $Revision: 1.2 $
 */
public class PDFormFieldAdditionalActions implements COSObjectable
{
    private COSDictionary actions;

    /**
     * Default constructor.
     */
    public PDFormFieldAdditionalActions()
    {
        actions = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDFormFieldAdditionalActions( COSDictionary a )
    {
        actions = a;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return actions;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return actions;
    }

    /**
     * This will get a JavaScript action to be performed when the user
     * types a keystroke into a text field or combo box or modifies the
     * selection in a scrollable list box. This allows the keystroke to
     * be checked for validity and rejected or modified.
     *
     * @return The K entry of form field's additional actions dictionary.
     */
    public PDAction getK()
    {
        COSDictionary k = (COSDictionary)actions.getDictionaryObject( "K" );
        PDAction retval = null;
        if( k != null )
        {
            retval = PDActionFactory.createAction( k );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed when the user
     * types a keystroke into a text field or combo box or modifies the
     * selection in a scrollable list box. This allows the keystroke to
     * be checked for validity and rejected or modified.
     *
     * @param k The action to be performed.
     */
    public void setK( PDAction k )
    {
        actions.setItem( "K", k );
    }

    /**
     * This will get a JavaScript action to be performed before
     * the field is formatted to display its current value. This
     * allows the field's value to be modified before formatting.
     *
     * @return The F entry of form field's additional actions dictionary.
     */
    public PDAction getF()
    {
        COSDictionary f = (COSDictionary)actions.getDictionaryObject( "F" );
        PDAction retval = null;
        if( f != null )
        {
            retval = PDActionFactory.createAction( f );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed before
     * the field is formatted to display its current value. This
     * allows the field's value to be modified before formatting.
     *
     * @param f The action to be performed.
     */
    public void setF( PDAction f )
    {
        actions.setItem( "F", f );
    }

    /**
     * This will get a JavaScript action to be performed
     * when the field's value is changed. This allows the
     * new value to be checked for validity.
     * The name V stands for "validate".
     *
     * @return The V entry of form field's additional actions dictionary.
     */
    public PDAction getV()
    {
        COSDictionary v = (COSDictionary)actions.getDictionaryObject( "V" );
        PDAction retval = null;
        if( v != null )
        {
            retval = PDActionFactory.createAction( v );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed
     * when the field's value is changed. This allows the
     * new value to be checked for validity.
     * The name V stands for "validate".
     *
     * @param v The action to be performed.
     */
    public void setV( PDAction v )
    {
        actions.setItem( "V", v );
    }

    /**
     * This will get a JavaScript action to be performed in order to recalculate
     * the value of this field when that of another field changes. The order in which
     * the document's fields are recalculated is defined by the CO entry in the
     * interactive form dictionary.
     * The name C stands for "calculate".
     *
     * @return The C entry of form field's additional actions dictionary.
     */
    public PDAction getC()
    {
        COSDictionary c = (COSDictionary)actions.getDictionaryObject( "C" );
        PDAction retval = null;
        if( c != null )
        {
            retval = PDActionFactory.createAction( c );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed in order to recalculate
     * the value of this field when that of another field changes. The order in which
     * the document's fields are recalculated is defined by the CO entry in the
     * interactive form dictionary.
     * The name C stands for "calculate".
     *
     * @param c The action to be performed.
     */
    public void setC( PDAction c )
    {
        actions.setItem( "C", c );
    }
}