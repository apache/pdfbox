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
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDExportFormatAttributeObject;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDLayoutAttributeObject;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDListAttributeObject;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDPrintFieldAttributeObject;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.PDTableAttributeObject;

/**
 * An attribute object.
 *
 * @author Johannes Koch
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
        if (PDUserAttributeObject.OWNER_USER_PROPERTIES.equals(owner))
        {
            return new PDUserAttributeObject(dictionary);
        }
        else if (PDListAttributeObject.OWNER_LIST.equals(owner))
        {
            return new PDListAttributeObject(dictionary);
        }
        else if (PDPrintFieldAttributeObject.OWNER_PRINT_FIELD.equals(owner))
        {
            return new PDPrintFieldAttributeObject(dictionary);
        }
        else if (PDTableAttributeObject.OWNER_TABLE.equals(owner))
        {
            return new PDTableAttributeObject(dictionary);
        }
        else if (PDLayoutAttributeObject.OWNER_LAYOUT.equals(owner))
        {
            return new PDLayoutAttributeObject(dictionary);
        }
        else if (PDExportFormatAttributeObject.OWNER_XML_1_00.equals(owner)
            || PDExportFormatAttributeObject.OWNER_HTML_3_20.equals(owner)
            || PDExportFormatAttributeObject.OWNER_HTML_4_01.equals(owner)
            || PDExportFormatAttributeObject.OWNER_OEB_1_00.equals(owner)
            || PDExportFormatAttributeObject.OWNER_RTF_1_05.equals(owner)
            || PDExportFormatAttributeObject.OWNER_CSS_1_00.equals(owner)
            || PDExportFormatAttributeObject.OWNER_CSS_2_00.equals(owner))
        {
            return new PDExportFormatAttributeObject(dictionary);
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
        return this.getCOSObject().getNameAsString(COSName.O);
    }

    /**
     * Sets the owner of the attributes.
     * 
     * @param owner the owner of the attributes
     */
    protected void setOwner(String owner)
    {
        this.getCOSObject().setName(COSName.O, owner);
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
        return (this.getCOSObject().size() == 1) && (this.getOwner() != null);
    }


    /**
     * Notifies the attribute object change listeners if the attribute is changed.
     * 
     * @param oldBase old value
     * @param newBase new value
     */
    protected void potentiallyNotifyChanged(COSBase oldBase, COSBase newBase)
    {
        if (this.isValueChanged(oldBase, newBase))
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
    private boolean isValueChanged(COSBase oldValue, COSBase newValue)
    {
        if (oldValue == null)
        {
            return newValue != null;
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
        return "O=" + this.getOwner();
    }

    /**
     * Creates a String representation of an Object array.
     * 
     * @param array the Object array
     * @return the String representation
     */
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

    /**
     * Creates a String representation of a float array.
     * 
     * @param array the float array
     * @return the String representation
     */
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
