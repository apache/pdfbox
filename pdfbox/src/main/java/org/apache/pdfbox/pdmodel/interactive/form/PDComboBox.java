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
package org.apache.pdfbox.pdmodel.interactive.form;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * A combo box consisting of a drop-down list.
 * May be accompanied by an editable text box in which non-predefined values may be entered.
 * @author John Hewson
 */
public final class PDComboBox extends PDChoice
{
    /**
     *  Ff-flag.
     */
    private static final int FLAG_EDIT = 1 << 18;
    
    /**
     * @see PDFieldTreeNode#PDFieldTreeNode(PDAcroForm)
     *
     * @param theAcroForm The acroform.
     */
    public PDComboBox(PDAcroForm theAcroForm)
    {
        super( theAcroForm );
        setCombo(true);
    }    

    /**
     * Constructor.
     * 
     * @param acroForm The form that this field is part of.
     * @param field the PDF object to represent as a field.
     * @param parentNode the parent node of the node to be created
     */
    public PDComboBox(PDAcroForm acroForm, COSDictionary field, PDFieldTreeNode parentNode)
    {
        super(acroForm, field, parentNode);
    }

    /**
     * Determines if Edit is set.
     * 
     * @return true if the combo box shall include an editable text box as well as a drop-down list.
     */
    public boolean isEdit()
    {
        return getCOSObject().getFlag( COSName.FF, FLAG_EDIT );
    }

    /**
     * Set the Edit bit.
     *
     * @param edit The value for Edit.
     */
    public void setEdit( boolean edit )
    {
        getCOSObject().setFlag( COSName.FF, FLAG_EDIT, edit );
    }

    /**
     * Sets the field value - the 'V' key.
     * 
     * @param value the value
     */
    @Override
    public void setValue(String value)
    {
        if (value != null)
        {
            // check if the options contain the value to be set is
            // only necessary if the edit flag has not been set.
            // If the edit flag has been set the field allows a custom value.
            if (!isEdit() && getOptions().indexOf((String) value) == -1)
            {
                throw new IllegalArgumentException("The list box does not contain the given value.");
            }
            else
            {
                getCOSObject().setString(COSName.V, (String)value);
                // remove I key for single valued choice field
                setSelectedOptionsIndex(null);
            }
        }
        else
        {
            getCOSObject().removeItem(COSName.V);
        }
        // TODO create/update appearance
    }
}
