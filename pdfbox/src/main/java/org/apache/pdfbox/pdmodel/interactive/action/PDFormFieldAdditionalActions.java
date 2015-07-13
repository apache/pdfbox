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
 * This class represents a form field's dictionary of actions
 * that occur due to events.
 *
 * @author Ben Litchfield
 * @author Panagiotis Toumasis
 */
public class PDFormFieldAdditionalActions implements COSObjectable
{
    private final COSDictionary actions;

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
    @Override
    public COSDictionary getCOSObject()
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
        COSDictionary k = (COSDictionary) actions.getDictionaryObject(COSName.K);
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
        actions.setItem(COSName.K, k);
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
        COSDictionary f = (COSDictionary) actions.getDictionaryObject(COSName.F);
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
        actions.setItem(COSName.F, f);
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
        COSDictionary v = (COSDictionary) actions.getDictionaryObject(COSName.V);
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
        actions.setItem(COSName.V, v);
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
        COSDictionary c = (COSDictionary) actions.getDictionaryObject(COSName.C);
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
        actions.setItem(COSName.C, c);
    }
}
