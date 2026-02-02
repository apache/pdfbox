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
 * This class represents an annotation's dictionary of actions
 * that occur due to events.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
 */
public class PDAnnotationAdditionalActions implements COSObjectable
{
    private final COSDictionary actions;

    /**
     * Default constructor.
     */
    public PDAnnotationAdditionalActions()
    {
        actions = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param a The action dictionary.
     */
    public PDAnnotationAdditionalActions( COSDictionary a )
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
     * This will get an action to be performed when the cursor
     * enters the annotation's active area.
     *
     * @return The E entry of annotation's additional actions dictionary.
     */
    public PDAction getE()
    {
        COSDictionary e = actions.getCOSDictionary(COSName.E);
        return e != null ? PDActionFactory.createAction(e) : null;
    }

    /**
     * This will set an action to be performed when the cursor
     * enters the annotation's active area.
     *
     * @param e The action to be performed.
     */
    public void setE( PDAction e )
    {
        actions.setItem(COSName.E, e);
    }

    /**
     * This will get an action to be performed when the cursor
     * exits the annotation's active area.
     *
     * @return The X entry of annotation's additional actions dictionary.
     */
    public PDAction getX()
    {
        COSDictionary x = actions.getCOSDictionary(COSName.X);
        return x != null ? PDActionFactory.createAction(x) : null;
    }

    /**
     * This will set an action to be performed when the cursor
     * exits the annotation's active area.
     *
     * @param x The action to be performed.
     */
    public void setX( PDAction x )
    {
        actions.setItem(COSName.X, x);
    }

    /**
     * This will get an action to be performed when the mouse button
     * is pressed inside the annotation's active area.
     * The name D stands for "down".
     *
     * @return The d entry of annotation's additional actions dictionary.
     */
    public PDAction getD()
    {
        COSDictionary d = actions.getCOSDictionary(COSName.D);
        return d != null ? PDActionFactory.createAction(d) : null;
    }

    /**
     * This will set an action to be performed when the mouse button
     * is pressed inside the annotation's active area.
     * The name D stands for "down".
     *
     * @param d The action to be performed.
     */
    public void setD( PDAction d )
    {
        actions.setItem( COSName.D, d );
    }

    /**
     * This will get an action to be performed when the mouse button
     * is released inside the annotation's active area.
     * The name U stands for "up".
     *
     * @return The U entry of annotation's additional actions dictionary.
     */
    public PDAction getU()
    {
        COSDictionary u = actions.getCOSDictionary(COSName.U);
        return u != null ? PDActionFactory.createAction(u) : null;
    }

    /**
     * This will set an action to be performed when the mouse button
     * is released inside the annotation's active area.
     * The name U stands for "up".
     *
     * @param u The action to be performed.
     */
    public void setU( PDAction u )
    {
        actions.setItem(COSName.U, u);
    }

    /**
     * This will get an action to be performed when the annotation
     * receives the input focus.
     *
     * @return The Fo entry of annotation's additional actions dictionary.
     */
    public PDAction getFo()
    {
        COSDictionary fo = actions.getCOSDictionary(COSName.FO);
        return fo != null ? PDActionFactory.createAction( fo ) : null;
    }

    /**
     * This will set an action to be performed when the annotation
     * receives the input focus.
     *
     * @param fo The action to be performed.
     */
    public void setFo( PDAction fo )
    {
        actions.setItem(COSName.FO, fo);
    }

    /**
     * This will get an action to be performed when the annotation
     * loses the input focus.
     * The name Bl stands for "blurred".
     *
     * @return The Bl entry of annotation's additional actions dictionary.
     */
    public PDAction getBl()
    {
        COSDictionary bl = actions.getCOSDictionary(COSName.BL);
        return bl != null ? PDActionFactory.createAction(bl) : null;
    }

    /**
     * This will set an action to be performed when the annotation
     * loses the input focus.
     * The name Bl stands for "blurred".
     *
     * @param bl The action to be performed.
     */
    public void setBl( PDAction bl )
    {
        actions.setItem(COSName.BL, bl);
    }

    /**
     * This will get an action to be performed when the page containing
     * the annotation is opened. The action is executed after the O action
     * in the page's additional actions dictionary and the OpenAction entry
     * in the document catalog, if such actions are present.
     *
     * @return The PO entry of annotation's additional actions dictionary.
     */
    public PDAction getPO()
    {
        COSDictionary po = actions.getCOSDictionary(COSName.PO);
        return po != null ? PDActionFactory.createAction(po) : null;
    }

    /**
     * This will set an action to be performed when the page containing
     * the annotation is opened. The action is executed after the O action
     * in the page's additional actions dictionary and the OpenAction entry
     * in the document catalog, if such actions are present.
     *
     * @param po The action to be performed.
     */
    public void setPO( PDAction po )
    {
        actions.setItem(COSName.PO, po);
    }

    /**
     * This will get an action to be performed when the page containing
     * the annotation is closed. The action is executed before the C action
     * in the page's additional actions dictionary, if present.
     *
     * @return The PC entry of annotation's additional actions dictionary.
     */
    public PDAction getPC()
    {
        COSDictionary pc = actions.getCOSDictionary(COSName.PC);
        return pc != null ? PDActionFactory.createAction(pc) : null;
    }

    /**
     * This will set an action to be performed when the page containing
     * the annotation is closed. The action is executed before the C action
     * in the page's additional actions dictionary, if present.
     *
     * @param pc The action to be performed.
     */
    public void setPC( PDAction pc )
    {
        actions.setItem(COSName.PC, pc);
    }

    /**
     * This will get an action to be performed when the page containing
     * the annotation becomes visible in the viewer application's user interface.
     *
     * @return The PV entry of annotation's additional actions dictionary.
     */
    public PDAction getPV()
    {
        COSDictionary pv = actions.getCOSDictionary(COSName.PV);
        return pv != null ? PDActionFactory.createAction(pv) : null;
    }

    /**
     * This will set an action to be performed when the page containing
     * the annotation becomes visible in the viewer application's user interface.
     *
     * @param pv The action to be performed.
     */
    public void setPV( PDAction pv )
    {
        actions.setItem(COSName.PV, pv);
    }

    /**
     * This will get an action to be performed when the page containing the annotation
     * is no longer visible in the viewer application's user interface.
     *
     * @return The PI entry of annotation's additional actions dictionary.
     */
    public PDAction getPI()
    {
        COSDictionary pi = actions.getCOSDictionary(COSName.PI);
        return pi != null ? PDActionFactory.createAction(pi) : null;
    }

    /**
     * This will set an action to be performed when the page containing the annotation
     * is no longer visible in the viewer application's user interface.
     *
     * @param pi The action to be performed.
     */
    public void setPI( PDAction pi )
    {
        actions.setItem(COSName.PI, pi);
    }
}
