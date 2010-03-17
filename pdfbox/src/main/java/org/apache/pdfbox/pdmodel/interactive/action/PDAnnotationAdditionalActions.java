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
 * This class represents an annotation's dictionary of actions
 * that occur due to events.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author Panagiotis Toumasis (ptoumasis@mail.gr)
 * @version $Revision: 1.2 $
 */
public class PDAnnotationAdditionalActions implements COSObjectable
{
    private COSDictionary actions;

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
     * This will get an action to be performed when the cursor
     * enters the annotation's active area.
     *
     * @return The E entry of annotation's additional actions dictionary.
     */
    public PDAction getE()
    {
        COSDictionary e = (COSDictionary)actions.getDictionaryObject( "E" );
        PDAction retval = null;
        if( e != null )
        {
            retval = PDActionFactory.createAction( e );
        }
        return retval;
    }

    /**
     * This will set an action to be performed when the cursor
     * enters the annotation's active area.
     *
     * @param e The action to be performed.
     */
    public void setE( PDAction e )
    {
        actions.setItem( "E", e );
    }

    /**
     * This will get an action to be performed when the cursor
     * exits the annotation's active area.
     *
     * @return The X entry of annotation's additional actions dictionary.
     */
    public PDAction getX()
    {
        COSDictionary x = (COSDictionary)actions.getDictionaryObject( "X" );
        PDAction retval = null;
        if( x != null )
        {
            retval = PDActionFactory.createAction( x );
        }
        return retval;
    }

    /**
     * This will set an action to be performed when the cursor
     * exits the annotation's active area.
     *
     * @param x The action to be performed.
     */
    public void setX( PDAction x )
    {
        actions.setItem( "X", x );
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
        COSDictionary d = (COSDictionary)actions.getDictionaryObject( "D" );
        PDAction retval = null;
        if( d != null )
        {
            retval = PDActionFactory.createAction( d );
        }
        return retval;
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
        actions.setItem( "D", d );
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
        COSDictionary u = (COSDictionary)actions.getDictionaryObject( "U" );
        PDAction retval = null;
        if( u != null )
        {
            retval = PDActionFactory.createAction( u );
        }
        return retval;
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
        actions.setItem( "U", u );
    }

    /**
     * This will get an action to be performed when the annotation
     * receives the input focus.
     *
     * @return The Fo entry of annotation's additional actions dictionary.
     */
    public PDAction getFo()
    {
        COSDictionary fo = (COSDictionary)actions.getDictionaryObject( "Fo" );
        PDAction retval = null;
        if( fo != null )
        {
            retval = PDActionFactory.createAction( fo );
        }
        return retval;
    }

    /**
     * This will set an action to be performed when the annotation
     * receives the input focus.
     *
     * @param fo The action to be performed.
     */
    public void setFo( PDAction fo )
    {
        actions.setItem( "Fo", fo );
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
        COSDictionary bl = (COSDictionary)actions.getDictionaryObject( "Bl" );
        PDAction retval = null;
        if( bl != null )
        {
            retval = PDActionFactory.createAction( bl );
        }
        return retval;
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
        actions.setItem( "Bl", bl );
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
        COSDictionary po = (COSDictionary)actions.getDictionaryObject( "PO" );
        PDAction retval = null;
        if( po != null )
        {
            retval = PDActionFactory.createAction( po );
        }
        return retval;
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
        actions.setItem( "PO", po );
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
        COSDictionary pc = (COSDictionary)actions.getDictionaryObject( "PC" );
        PDAction retval = null;
        if( pc != null )
        {
            retval = PDActionFactory.createAction( pc );
        }
        return retval;
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
        actions.setItem( "PC", pc );
    }

    /**
     * This will get an action to be performed when the page containing
     * the annotation becomes visible in the viewer application's user interface.
     *
     * @return The PV entry of annotation's additional actions dictionary.
     */
    public PDAction getPV()
    {
        COSDictionary pv = (COSDictionary)actions.getDictionaryObject( "PV" );
        PDAction retval = null;
        if( pv != null )
        {
            retval = PDActionFactory.createAction( pv );
        }
        return retval;
    }

    /**
     * This will set an action to be performed when the page containing
     * the annotation becomes visible in the viewer application's user interface.
     *
     * @param pv The action to be performed.
     */
    public void setPV( PDAction pv )
    {
        actions.setItem( "PV", pv );
    }

    /**
     * This will get an action to be performed when the page containing the annotation
     * is no longer visible in the viewer application's user interface.
     *
     * @return The PI entry of annotation's additional actions dictionary.
     */
    public PDAction getPI()
    {
        COSDictionary pi = (COSDictionary)actions.getDictionaryObject( "PI" );
        PDAction retval = null;
        if( pi != null )
        {
            retval = PDActionFactory.createAction( pi );
        }
        return retval;
    }

    /**
     * This will set an action to be performed when the page containing the annotation
     * is no longer visible in the viewer application's user interface.
     *
     * @param pi The action to be performed.
     */
    public void setPI( PDAction pi )
    {
        actions.setItem( "PI", pi );
    }
}
