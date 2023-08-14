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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This class represents a document catalog's dictionary of actions
 * that occur due to events.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
 */
public class PDDocumentCatalogAdditionalActions implements COSObjectable
{
    private final COSDictionary actions;

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
    @Override
    public COSDictionary getCOSObject()
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
        COSDictionary wc = actions.getCOSDictionary(COSName.WC);
        return wc != null ? PDActionFactory.createAction(wc) : null;
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
        actions.setItem(COSName.WC, wc);
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
        COSDictionary ws = actions.getCOSDictionary(COSName.WS);
        return ws != null ? PDActionFactory.createAction(ws) : null;
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
        actions.setItem(COSName.WS, ws);
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
        COSDictionary ds = actions.getCOSDictionary(COSName.DS);
        return ds != null ? PDActionFactory.createAction(ds) : null;
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
        actions.setItem(COSName.DS, ds);
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
        COSDictionary wp = actions.getCOSDictionary(COSName.WP);
        return wp != null ? PDActionFactory.createAction(wp) : null;
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
        actions.setItem(COSName.WP, wp);
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
        COSDictionary dp = actions.getCOSDictionary(COSName.DP);
        return dp != null ? PDActionFactory.createAction(dp) : null;
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
        actions.setItem(COSName.DP, dp);
    }
}
