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

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.PDDestinationOrAction;

/**
 * This represents an action that can be executed in a PDF document.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
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
    @Override
    public COSDictionary getCOSObject()
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
       return action.getNameAsString( COSName.TYPE );
    }

    /**
     * This will set the type of PDF object that the actions dictionary describes.
     * If present must be Action for an action dictionary.
     *
     * @param type The new Type for the PDF object.
     */
    public final void setType( String type )
    {
       action.setName(COSName.TYPE, type );
    }

    /**
     * This will get the type of action that the actions dictionary describes.
     *
     * @return The S entry of actions dictionary.
     */
    public String getSubType()
    {
        return action.getNameAsString(COSName.S);
    }

    /**
     * This will set the type of action that the actions dictionary describes.
     *
     * @param s The new type of action.
     */
    public void setSubType( String s )
    {
        action.setName(COSName.S, s);
    }

    /**
     * This will get the next action, or sequence of actions, to be performed after this one.
     * The value is either a single action dictionary or an array of action dictionaries
     * to be performed in order.
     *
     * @return The Next action or sequence of actions.
     */
    public List<PDAction> getNext()
    {
        List<PDAction> retval = null;
        COSBase next = action.getDictionaryObject(COSName.NEXT);
        if( next instanceof COSDictionary )
        {
            PDAction pdAction = PDActionFactory.createAction( (COSDictionary) next );
            retval = new COSArrayList<PDAction>(pdAction, next, action, COSName.NEXT);
        }
        else if( next instanceof COSArray )
        {
            COSArray array = (COSArray)next;
            List<PDAction> actions = new ArrayList<PDAction>();
            for( int i=0; i<array.size(); i++ )
            {
                actions.add( PDActionFactory.createAction( (COSDictionary) array.getObject( i )));
            }
            retval = new COSArrayList<PDAction>( actions, array );
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
    public void setNext( List<?> next )
    {
        action.setItem(COSName.NEXT, COSArrayList.converterToCOSArray(next));
    }
}
