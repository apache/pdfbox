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
 * This class represents a document catalog's dictionary of actions
 * that occur due to events.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Panagiotis Toumasis (ptoumasis@mail.gr)
 * @version $Revision: 1.2 $
 */
public class PDDocumentCatalogAdditionalActions implements COSObjectable
{
    private COSDictionary actions;

    /**
     * Default constructor.
     */
    public PDDocumentCatalogAdditionalActions()
    {
        actions = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDDocumentCatalogAdditionalActions( COSDictionary a )
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
     * This will get a JavaScript action to be performed
     * before closing a document.
     * The name WC stands for "will close".
     *
     * @return The WC entry of document catalog's additional actions dictionary.
     */
    public PDAction getWC()
    {
        COSDictionary wc = (COSDictionary)actions.getDictionaryObject( "WC" );
        PDAction retval = null;
        if( wc != null )
        {
            retval = PDActionFactory.createAction( wc );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed
     * before closing a document.
     * The name WC stands for "will close".
     *
     * @param wc The action to be performed.
     */
    public void setWC( PDAction wc )
    {
        actions.setItem( "WC", wc );
    }

    /**
     * This will get a JavaScript action to be performed
     * before saving a document.
     * The name WS stands for "will save".
     *
     * @return The WS entry of document catalog's additional actions dictionary.
     */
    public PDAction getWS()
    {
        COSDictionary ws = (COSDictionary)actions.getDictionaryObject( "WS" );
        PDAction retval = null;
        if( ws != null )
        {
            retval = PDActionFactory.createAction( ws );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed
     * before saving a document.
     * The name WS stands for "will save".
     *
     * @param ws The action to be performed.
     */
    public void setWS( PDAction ws )
    {
        actions.setItem( "WS", ws );
    }

    /**
     * This will get a JavaScript action to be performed
     * after saving a document.
     * The name DS stands for "did save".
     *
     * @return The DS entry of document catalog's additional actions dictionary.
     */
    public PDAction getDS()
    {
        COSDictionary ds = (COSDictionary)actions.getDictionaryObject( "DS" );
        PDAction retval = null;
        if( ds != null )
        {
            retval = PDActionFactory.createAction( ds );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed
     * after saving a document.
     * The name DS stands for "did save".
     *
     * @param ds The action to be performed.
     */
    public void setDS( PDAction ds )
    {
        actions.setItem( "DS", ds );
    }

    /**
     * This will get a JavaScript action to be performed
     * before printing a document.
     * The name WP stands for "will print".
     *
     * @return The WP entry of document catalog's additional actions dictionary.
     */
    public PDAction getWP()
    {
        COSDictionary wp = (COSDictionary)actions.getDictionaryObject( "WP" );
        PDAction retval = null;
        if( wp != null )
        {
            retval = PDActionFactory.createAction( wp );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed
     * before printing a document.
     * The name WP stands for "will print".
     *
     * @param wp The action to be performed.
     */
    public void setWP( PDAction wp )
    {
        actions.setItem( "WP", wp );
    }

    /**
     * This will get a JavaScript action to be performed
     * after printing a document.
     * The name DP stands for "did print".
     *
     * @return The DP entry of document catalog's additional actions dictionary.
     */
    public PDAction getDP()
    {
        COSDictionary dp = (COSDictionary)actions.getDictionaryObject( "DP" );
        PDAction retval = null;
        if( dp != null )
        {
            retval = PDActionFactory.createAction( dp );
        }
        return retval;
    }

    /**
     * This will set a JavaScript action to be performed
     * after printing a document.
     * The name DP stands for "did print".
     *
     * @param dp The action to be performed.
     */
    public void setDP( PDAction dp )
    {
        actions.setItem( "DP", dp );
    }
}