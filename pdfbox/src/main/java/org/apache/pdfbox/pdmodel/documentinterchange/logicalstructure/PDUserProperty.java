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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDDictionaryWrapper;

/**
 * A user property.
 * 
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 */
public class PDUserProperty extends PDDictionaryWrapper
{

    private final PDUserAttributeObject userAttributeObject;

    /**
     * Creates a new user property.
     * 
     * @param the user attribute object
     */
    public PDUserProperty(PDUserAttributeObject userAttributeObject)
    {
        this.userAttributeObject = userAttributeObject;
    }

    /**
     * Creates a user property with a given dictionary.
     * 
     * @param dictionary the dictionary
     * @param the user attribute object
     */
    public PDUserProperty(COSDictionary dictionary,
        PDUserAttributeObject userAttributeObject)
    {
        super(dictionary);
        this.userAttributeObject = userAttributeObject;
    }


    /**
     * Returns the property name.
     * 
     * @return the property name
     */
    public String getName()
    {
        return this.getCOSDictionary().getNameAsString(COSName.N);
    }

    /**
     * Sets the property name.
     * 
     * @param name the property name
     */
    public void setName(String name)
    {
        this.potentiallyNotifyChanged(this.getName(), name);
        this.getCOSDictionary().setName(COSName.N, name);
    }

    /**
     * Returns the property value.
     * 
     * @return the property value
     */
    public COSBase getValue()
    {
        return this.getCOSDictionary().getDictionaryObject(COSName.V);
    }

    /**
     * Sets the property value.
     * 
     * @param value the property value
     */
    public void setValue(COSBase value)
    {
        this.potentiallyNotifyChanged(this.getValue(), value);
        this.getCOSDictionary().setItem(COSName.V, value);
    }

    /**
     * Returns the string for the property value.
     * 
     * @return the string for the property value
     */
    public String getFormattedValue()
    {
        return this.getCOSDictionary().getString(COSName.F);
    }

    /**
     * Sets the string for the property value.
     * 
     * @param formattedValue the string for the property value
     */
    public void setFormattedValue(String formattedValue)
    {
        this.potentiallyNotifyChanged(this.getFormattedValue(), formattedValue);
        this.getCOSDictionary().setString(COSName.F, formattedValue);
    }

    /**
     * Shall the property be hidden?
     * 
     * @return <code>true</code> if the property shall be hidden,
     * <code>false</code> otherwise
     */
    public boolean isHidden()
    {
        return this.getCOSDictionary().getBoolean(COSName.H, false);
    }

    /**
     * Specifies whether the property shall be hidden.
     * 
     * @param hidden <code>true</code> if the property shall be hidden,
     * <code>false</code> otherwise
     */
    public void setHidden(boolean hidden)
    {
        this.potentiallyNotifyChanged(this.isHidden(), hidden);
        this.getCOSDictionary().setBoolean(COSName.H, hidden);
    }


    @Override
    public String toString()
    {
        return new StringBuilder("Name=").append(this.getName())
            .append(", Value=").append(this.getValue())
            .append(", FormattedValue=").append(this.getFormattedValue())
            .append(", Hidden=").append(this.isHidden()).toString();
    }


    /**
     * Notifies the user attribute object if the user property is changed.
     * 
     * @param oldEntry old entry
     * @param newEntry new entry
     */
    private void potentiallyNotifyChanged(Object oldEntry, Object newEntry)
    {
        if (this.isEntryChanged(oldEntry, newEntry))
        {
            this.userAttributeObject.userPropertyChanged(this);
        }
    }

    /**
     * Is the value changed?
     * 
     * @param oldEntry old entry
     * @param newEntry new entry
     * @return <code>true</code> if the entry is changed, <code>false</code>
     * otherwise
     */
    private boolean isEntryChanged(Object oldEntry, Object newEntry)
    {
        if (oldEntry == null)
        {
            if (newEntry == null)
            {
                return false;
            }
            return true;
        }
        return !oldEntry.equals(newEntry);
    }

}
