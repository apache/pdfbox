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

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDDictionaryWrapper;

/**
 * An attribute object.
 *
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 *
 */
public abstract class PDAttributeObject extends PDDictionaryWrapper
{

    /**
     * Creates an attribute object.
     * 
     * @param dictionary the dictionary
     * @return the attribute object
     */
    public static PDAttributeObject create(COSDictionary dictionary)
    {
        String owner = dictionary.getNameAsString(COSName.O);
        if (PDUserAttributeObject.USER_PROPERTIES.equals(owner))
        {
            return new PDUserAttributeObject(dictionary);
        }
        return new PDDefaultAttributeObject(dictionary);
    }

    private PDStructureElement structureElement;

    /**
     * Gets the structure element.
     * 
     * @return the structure element
     */
    private PDStructureElement getStructureElement()
    {
        return this.structureElement;
    }

    /**
     * Sets the structure element.
     * 
     * @param structureElement the structure element
     */
    protected void setStructureElement(PDStructureElement structureElement)
    {
        this.structureElement = structureElement;
    }


    /**
     * Default constructor.
     */
    public PDAttributeObject()
    {
    }

    /**
     * Creates a new attribute object with a given dictionary.
     * 
     * @param dictionary the dictionary
     */
    public PDAttributeObject(COSDictionary dictionary)
    {
        super(dictionary);
    }


    /**
     * Returns the owner of the attributes.
     * 
     * @return the owner of the attributes
     */
    public String getOwner()
    {
        return this.getCOSDictionary().getNameAsString(COSName.O);
    }

    /**
     * Sets the owner of the attributes.
     * 
     * @param owner the owner of the attributes
     */
    protected void setOwner(String owner)
    {
        this.getCOSDictionary().setName(COSName.O, owner);
    }

    /**
     * Detects whether there are no properties in the attribute object.
     * 
     * @return <code>true</code> if the attribute object is empty,
     *  <code>false</code> otherwise
     */
    public boolean isEmpty()
    {
        // only entry is the owner?
        return (this.getCOSDictionary().size() == 1) && (this.getOwner() != null);
    }


    /**
     * Notifies the attribute object change listeners if the attribute is changed.
     * 
     * @param oldValue old value
     * @param newValue new value
     */
    protected void potentiallyNotifyChanged(Object oldValue, Object newValue)
    {
        if (this.isValueChanged(oldValue, newValue))
        {
            this.notifyChanged();
        }
    }

    /**
     * Is the value changed?
     * 
     * @param oldValue old value
     * @param newValue new value
     * @return <code>true</code> if the value is changed, <code>false</code>
     * otherwise
     */
    private boolean isValueChanged(Object oldValue, Object newValue)
    {
        if (oldValue == null)
        {
            if (newValue == null)
            {
                return false;
            }
            return true;
        }
        return !oldValue.equals(newValue);
    }

    /**
     * Notifies the attribute object change listeners about a change in this
     * attribute object.
     */
    protected void notifyChanged()
    {
        if (this.getStructureElement() != null)
        {
            this.getStructureElement().attributeChanged(this);
        }
    }

    @Override
    public String toString()
    {
        return new StringBuilder("O=").append(this.getOwner()).toString();
    }

    protected static String arrayToString(Object[] array)
    {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++)
        {
            if (i > 0)
            {
                sb.append(", ");
            }
            sb.append(array[i]);
        }
        return sb.append(']').toString();
    }

    protected static String arrayToString(float[] array)
    {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++)
        {
            if (i > 0)
            {
                sb.append(", ");
            }
            sb.append(array[i]);
        }
        return sb.append(']').toString();
    }

}
