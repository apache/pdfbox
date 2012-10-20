/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.interactive.action;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDAction;

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
