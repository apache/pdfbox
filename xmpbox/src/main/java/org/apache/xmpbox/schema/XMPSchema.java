/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.xmpbox.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.XmpConstants;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.AbstractSimpleProperty;
import org.apache.xmpbox.type.AbstractStructuredType;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Attribute;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.BooleanType;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.ComplexPropertyContainer;
import org.apache.xmpbox.type.DateType;
import org.apache.xmpbox.type.IntegerType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.TypeMapping;
import org.apache.xmpbox.type.Types;

/**
 * This class represents a metadata schema that can be stored in an XMP document. It handles all generic properties that
 * are available. See subclasses for access to specific properties. MODIFIED TO INCLUDE OBJECT REPRESENTATION
 * 
 */
public class XMPSchema extends AbstractStructuredType
{

    /**
     * Create a new blank schema that can be populated.
     * 
     * @param metadata
     *            The parent XMP metadata that this schema will be part of.
     * @param namespaceURI
     *            The URI of the namespace, ie "http://ns.adobe.com/pdf/1.3/"
     * 
     */
    public XMPSchema(XMPMetadata metadata, String namespaceURI, String prefix, String name)
    {
        super(metadata, namespaceURI, prefix, name);
        addNamespace(getNamespace(), getPrefix());
    }

    public XMPSchema(XMPMetadata metadata)
    {
        this(metadata, null, null, null);
    }

    public XMPSchema(XMPMetadata metadata, String prefix)
    {
        this(metadata, null, prefix, null);
    }

    public XMPSchema(XMPMetadata metadata, String namespaceURI, String prefix)
    {
        this(metadata, namespaceURI, prefix, null);
    }

    /**
     * Retrieve a generic simple type property
     * 
     * @param qualifiedName
     *            Full qualified name of proeprty wanted
     * @return The generic simple type property according to its qualified Name
     */
    public AbstractField getAbstractProperty(String qualifiedName)
    {
        Iterator<AbstractField> it = getContainer().getAllProperties().iterator();
        AbstractField tmp;
        while (it.hasNext())
        {
            tmp = it.next();
            if (tmp.getPropertyName().equals(qualifiedName))
            {
                return tmp;
            }
        }
        return null;

    }

    /**
     * Get the RDF about attribute
     * 
     * @return The RDF 'about' attribute.
     */
    public Attribute getAboutAttribute()
    {
        return getAttribute(XmpConstants.ABOUT_NAME);
    }

    /**
     * Get the RDF about value.
     * 
     * @return The RDF 'about' value. If there are not rdf:about attribute, an empty string is returned.
     */
    public String getAboutValue()
    {
        Attribute prop = getAttribute(XmpConstants.ABOUT_NAME);
        if (prop != null)
        {
            return prop.getValue();
        }
        return ""; // PDFBOX-1685 : if missing rdf:about should be considered as empty string
    }

    /**
     * Set the RDF 'about' attribute
     * 
     * @param about
     *            the well-formed attribute
     * @throws BadFieldValueException
     *             Bad Attribute name (not corresponding to about attribute)
     */
    public void setAbout(Attribute about) throws BadFieldValueException
    {
        if (XmpConstants.RDF_NAMESPACE.equals(about.getNamespace()))
        {
            if (XmpConstants.ABOUT_NAME.equals(about.getName()))
            {
                setAttribute(about);
                return;
            }
        }
        // else
        throw new BadFieldValueException("Attribute 'about' must be named 'rdf:about' or 'about'");
    }

    /**
     * Set the RDF 'about' attribute. Passing in null will clear this attribute.
     * 
     * @param about
     *            The new RFD about value.
     */
    public void setAboutAsSimple(String about)
    {
        if (about == null)
        {
            removeAttribute(XmpConstants.ABOUT_NAME);
        }
        else
        {
            setAttribute(new Attribute(XmpConstants.RDF_NAMESPACE, XmpConstants.ABOUT_NAME, about));

        }
    }

    private void setSpecifiedSimpleTypeProperty(Types type, String qualifiedName, Object propertyValue)
    {
        if (propertyValue == null)
        {
            // Search in properties to erase
            Iterator<AbstractField> it = getContainer().getAllProperties().iterator();
            AbstractField tmp;
            while (it.hasNext())
            {
                tmp = it.next();
                if (tmp.getPropertyName().equals(qualifiedName))
                {
                    getContainer().removeProperty(tmp);
                    return;
                }
            }
        }
        else
        {
            AbstractSimpleProperty specifiedTypeProperty;
            try
            {
                TypeMapping tm = getMetadata().getTypeMapping();
                specifiedTypeProperty = tm.instanciateSimpleProperty(null, getPrefix(), qualifiedName, propertyValue,
                        type);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException(
                        "Failed to create property with the specified type given in parameters", e);
            }
            // attribute placement for simple property has been removed
            // Search in properties to erase
            Iterator<AbstractField> it = getAllProperties().iterator();
            AbstractField tmp;
            while (it.hasNext())
            {
                tmp = it.next();
                if (tmp.getPropertyName().equals(qualifiedName))
                {
                    removeProperty(tmp);
                    addProperty(specifiedTypeProperty);
                    return;
                }
            }
            addProperty(specifiedTypeProperty);
        }
    }

    /**
     * Add a SimpleProperty to this schema
     * 
     * @param prop
     *            The Property to add
     */
    private void setSpecifiedSimpleTypeProperty(AbstractSimpleProperty prop)
    {
        // attribute placement for simple property has been removed
        // Search in properties to erase
        Iterator<AbstractField> it = getAllProperties().iterator();
        AbstractField tmp;
        while (it.hasNext())
        {
            tmp = it.next();
            if (tmp.getPropertyName().equals(prop.getPropertyName()))
            {
                removeProperty(tmp);
                addProperty(prop);
                return;
            }
        }
        addProperty(prop);
    }

    /**
     * Set TextType property
     * 
     * @param prop
     *            The text property to add
     */
    public void setTextProperty(TextType prop)
    {
        setSpecifiedSimpleTypeProperty(prop);
    }

    /**
     * Set a simple text property on the schema.
     * 
     * @param qualifiedName
     *            The name of the property, it must contain the namespace prefix, ie "pdf:Keywords"
     * @param propertyValue
     *            The value for the property, can be any string. Passing null will remove the property.
     */
    public void setTextPropertyValue(String qualifiedName, String propertyValue)
    {
        setSpecifiedSimpleTypeProperty(Types.Text, qualifiedName, propertyValue);
    }

    /**
     * Set a simple text property on the schema, using the current prefix.
     * 
     * @param simpleName
     *            the name of the property without prefix
     * @param propertyValue
     *            The value for the property, can be any string. Passing null will remove the property.
     */
    public void setTextPropertyValueAsSimple(String simpleName, String propertyValue)
    {
        this.setTextPropertyValue(simpleName, propertyValue);
    }

    /**
     * Get a TextProperty Type from its name
     * 
     * @return The Text Type property wanted
     */
    public TextType getUnqualifiedTextProperty(String name)
    {
        AbstractField prop = getAbstractProperty(name);
        if (prop != null)
        {
            if (prop instanceof TextType)
            {
                return (TextType) prop;
            }
            else
            {
                throw new IllegalArgumentException("Property asked is not a Text Property");
            }
        }
        return null;
    }

    /**
     * Get the value of a simple text property.
     * 
     * @return The value of the text property or the null if there is no value.
     * 
     */
    public String getUnqualifiedTextPropertyValue(String name)
    {
        TextType tt = getUnqualifiedTextProperty(name);
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Get the Date property with its name
     * 
     * @param qualifiedName
     *            The name of the property to get, it must include the namespace prefix. ie "pdf:Keywords".
     * @return Date Type property
     * 
     */
    public DateType getDateProperty(String qualifiedName)
    {
        AbstractField prop = getAbstractProperty(qualifiedName);
        if (prop != null)
        {
            if (prop instanceof DateType)
            {
                return (DateType) prop;
            }
            else
            {
                throw new IllegalArgumentException("Property asked is not a Date Property");
            }

        }
        return null;
    }

    /**
     * Get a simple date property value on the schema, using the current prefix.
     * 
     * @param simpleName
     *            the local name of the property to get
     * @return The value of the property as a calendar.
     * 
     */
    public Calendar getDatePropertyValueAsSimple(String simpleName)
    {
        return this.getDatePropertyValue(simpleName);
    }

    /**
     * Get the value of the property as a date.
     * 
     * @param qualifiedName
     *            The fully qualified property name for the date.
     * 
     * @return The value of the property as a date.
     * 
     */
    public Calendar getDatePropertyValue(String qualifiedName)
    {
        AbstractField prop = getAbstractProperty(qualifiedName);
        if (prop != null)
        {
            if (prop instanceof DateType)
            {
                return ((DateType) prop).getValue();
            }
            else
            {
                throw new IllegalArgumentException("Property asked is not a Date Property");
            }

        }
        return null;
    }

    /**
     * Set a new DateProperty
     * 
     * @param date
     *            The DateType Property
     */
    public void setDateProperty(DateType date)
    {
        setSpecifiedSimpleTypeProperty(date);
    }

    /**
     * Set a simple Date property on the schema, using the current prefix.
     * 
     * @param simpleName
     *            the name of the property without prefix
     * @param date
     *            The calendar value for the property, can be any string. Passing null will remove the property.
     */
    public void setDatePropertyValueAsSimple(String simpleName, Calendar date)
    {
        this.setDatePropertyValue(simpleName, date);
    }

    /**
     * Set the value of the property as a date.
     * 
     * @param qualifiedName
     *            The fully qualified property name for the date.
     * @param date
     *            The date to set, or null to clear.
     */
    public void setDatePropertyValue(String qualifiedName, Calendar date)
    {
        setSpecifiedSimpleTypeProperty(Types.Date, qualifiedName, date);

    }

    /**
     * Get a BooleanType property with its name
     * 
     * @param qualifiedName
     *            the full qualified name of property wanted
     * @return boolean Type property
     */
    public BooleanType getBooleanProperty(String qualifiedName)
    {
        AbstractField prop = getAbstractProperty(qualifiedName);
        if (prop != null)
        {
            if (prop instanceof BooleanType)
            {
                return (BooleanType) prop;
            }
            else
            {
                throw new IllegalArgumentException("Property asked is not a Boolean Property");
            }
        }
        return null;
    }

    /**
     * Get a simple boolean property value on the schema, using the current prefix.
     * 
     * @param simpleName
     *            the local name of property wanted
     * @return The value of the property as a boolean.
     */
    public Boolean getBooleanPropertyValueAsSimple(String simpleName)
    {
        return this.getBooleanPropertyValue(simpleName);
    }

    /**
     * Get the value of the property as a boolean.
     * 
     * @param qualifiedName
     *            The fully qualified property name for the boolean.
     * 
     * @return The value of the property as a boolean. Return null if property not exist
     */
    public Boolean getBooleanPropertyValue(String qualifiedName)
    {
        AbstractField prop = getAbstractProperty(qualifiedName);
        if (prop != null)
        {
            if (prop instanceof BooleanType)
            {
                return ((BooleanType) prop).getValue();
            }
            else
            {
                throw new IllegalArgumentException("Property asked is not a Boolean Property");
            }
        }
        // Return null if property not exist. This method give the property
        // value so treat this return in this way.
        // If you want to use this value like a condition, you must check this
        // return before
        return null;
    }

    /**
     * Set a BooleanType property
     * 
     * @param bool
     *            the booleanType property
     */
    public void setBooleanProperty(BooleanType bool)
    {
        setSpecifiedSimpleTypeProperty(bool);
    }

    /**
     * Set a simple Boolean property on the schema, using the current prefix.
     * 
     * @param simpleName
     *            the name of the property without prefix
     * @param bool
     *            The value for the property, can be any string. Passing null will remove the property.
     */
    public void setBooleanPropertyValueAsSimple(String simpleName, Boolean bool)
    {
        this.setBooleanPropertyValue(simpleName, bool);
    }

    /**
     * Set the value of the property as a boolean.
     * 
     * @param qualifiedName
     *            The fully qualified property name for the boolean.
     * @param bool
     *            The boolean to set, or null to clear.
     */
    public void setBooleanPropertyValue(String qualifiedName, Boolean bool)
    {
        setSpecifiedSimpleTypeProperty(Types.Boolean, qualifiedName, bool);
    }

    /**
     * Get the Integer property with its name
     * 
     * @param qualifiedName
     *            the full qualified name of property wanted
     * @return Integer Type property
     */
    public IntegerType getIntegerProperty(String qualifiedName)
    {
        AbstractField prop = getAbstractProperty(qualifiedName);
        if (prop != null)
        {
            if (prop instanceof IntegerType)
            {
                return ((IntegerType) prop);
            }
            else
            {
                throw new IllegalArgumentException("Property asked is not an Integer Property");
            }
        }
        return null;
    }

    /**
     * Get a simple integer property value on the schema, using the current prefix.
     * 
     * @param simpleName
     *            the local name of property wanted
     * @return The value of the property as an integer.
     */
    public Integer getIntegerPropertyValueAsSimple(String simpleName)
    {
        return this.getIntegerPropertyValue(simpleName);
    }

    /**
     * Get the value of the property as an integer.
     * 
     * @param qualifiedName
     *            The fully qualified property name for the integer.
     * 
     * @return The value of the property as an integer.
     */
    public Integer getIntegerPropertyValue(String qualifiedName)
    {
        AbstractField prop = getAbstractProperty(qualifiedName);
        if (prop != null)
        {
            if (prop instanceof IntegerType)
            {
                return ((IntegerType) prop).getValue();
            }
            else
            {
                throw new IllegalArgumentException("Property asked is not an Integer Property");
            }
        }
        return null;
    }

    /**
     * Add an integerProperty
     * 
     * @param prop
     *            The Integer Type property
     */
    public void setIntegerProperty(IntegerType prop)
    {
        setSpecifiedSimpleTypeProperty(prop);
    }

    /**
     * Set a simple Integer property on the schema, using the current prefix.
     * 
     * @param simpleName
     *            the name of the property without prefix
     * @param intValue
     *            The value for the property, can be any string. Passing null will remove the property.
     */
    public void setIntegerPropertyValueAsSimple(String simpleName, Integer intValue)
    {
        this.setIntegerPropertyValue(simpleName, intValue);
    }

    /**
     * Set the value of the property as an integer.
     * 
     * @param qualifiedName
     *            The fully qualified property name for the integer.
     * @param intValue
     *            The int to set, or null to clear.
     */
    public void setIntegerPropertyValue(String qualifiedName, Integer intValue)
    {
        setSpecifiedSimpleTypeProperty(Types.Integer, qualifiedName, intValue);
    }

    /**
     * Generic array property removing
     * 
     * @param fieldValue
     *            the field value
     */
    private void removeUnqualifiedArrayValue(String arrayName, String fieldValue)
    {
        ArrayProperty array = (ArrayProperty) getAbstractProperty(arrayName);
        if (array != null)
        {
            ArrayList<AbstractField> toDelete = new ArrayList<AbstractField>();
            Iterator<AbstractField> it = array.getContainer().getAllProperties().iterator();
            AbstractSimpleProperty tmp;
            while (it.hasNext())
            {
                tmp = (AbstractSimpleProperty) it.next();
                if (tmp.getStringValue().equals(fieldValue))
                {
                    toDelete.add(tmp);
                }
            }
            Iterator<AbstractField> eraseProperties = toDelete.iterator();
            while (eraseProperties.hasNext())
            {
                array.getContainer().removeProperty(eraseProperties.next());
            }
        }

    }

    /**
     * Remove all matching entries with the given value from the bag.
     * 
     * @param bagValue
     *            The value to remove from the bagList.
     */
    public void removeUnqualifiedBagValue(String bagName, String bagValue)
    {
        removeUnqualifiedArrayValue(bagName, bagValue);
    }

    /**
     * add a bag value property on the schema, using the current prefix.
     * 
     * @param simpleName
     *            the local name of property
     * @param bagValue
     *            the string value to add
     */
    public void addBagValueAsSimple(String simpleName, String bagValue)
    {
        this.internalAddBagValue(simpleName, bagValue);
    }

    private void internalAddBagValue(String qualifiedBagName, String bagValue)
    {
        ArrayProperty bag = (ArrayProperty) getAbstractProperty(qualifiedBagName);
        TextType li = createTextType(XmpConstants.LIST_NAME, bagValue);
        if (bag != null)
        {
            bag.getContainer().addProperty(li);
        }
        else
        {
            ArrayProperty newBag = createArrayProperty(qualifiedBagName, Cardinality.Bag);
            newBag.getContainer().addProperty(li);
            addProperty(newBag);
        }
    }

    /**
     * Add an entry to a bag property.
     * 
     * @param simpleName
     *            The name of the bag without the namespace prefix
     * @param bagValue
     *            The value to add to the bagList.
     */
    public void addQualifiedBagValue(String simpleName, String bagValue)
    {
        internalAddBagValue(simpleName, bagValue);
    }

    /**
     * Get all the values of the bag property. This will return a list of java.lang.String objects, this is a read-only
     * list.
     * 
     * @return All values of the bag property in a list.
     */
    public List<String> getUnqualifiedBagValueList(String bagName)
    {
        ArrayProperty array = (ArrayProperty) getAbstractProperty(bagName);
        if (array != null)
        {
            return array.getElementsAsString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Remove all matching values from a sequence property.
     * 
     * @param qualifiedSeqName
     *            The name of the sequence property. It must include the namespace prefix. ie "pdf:Keywords".
     * @param seqValue
     *            The value to remove from the list.
     */
    public void removeUnqualifiedSequenceValue(String qualifiedSeqName, String seqValue)
    {
        removeUnqualifiedArrayValue(qualifiedSeqName, seqValue);
    }

    /**
     * Generic method to remove a field from an array with an Elementable Object
     * 
     * @param arrayName
     *            the name of the property concerned
     * @param fieldValue
     *            the elementable field value
     */
    public void removeUnqualifiedArrayValue(String arrayName, AbstractField fieldValue)
    {
        String qualifiedArrayName = arrayName;
        ArrayProperty array = (ArrayProperty) getAbstractProperty(qualifiedArrayName);
        if (array != null)
        {
            ArrayList<AbstractField> toDelete = new ArrayList<AbstractField>();
            Iterator<AbstractField> it = array.getContainer().getAllProperties().iterator();
            AbstractSimpleProperty tmp;
            while (it.hasNext())
            {
                tmp = (AbstractSimpleProperty) it.next();
                if (tmp.equals(fieldValue))
                {
                    toDelete.add(tmp);
                }
            }
            Iterator<AbstractField> eraseProperties = toDelete.iterator();
            while (eraseProperties.hasNext())
            {
                array.getContainer().removeProperty(eraseProperties.next());
            }
        }
    }

    /**
     * Remove a value from a sequence property. This will remove all entries from the list.
     * 
     * @param qualifiedSeqName
     *            The name of the sequence property. It must include the namespace prefix. ie "pdf:Keywords".
     * @param seqValue
     *            The value to remove from the list.
     */
    public void removeUnqualifiedSequenceValue(String qualifiedSeqName, AbstractField seqValue)
    {
        removeUnqualifiedArrayValue(qualifiedSeqName, seqValue);
    }

    /**
     * Add a new value to a sequence property.
     * 
     * @param simpleSeqName
     *            The name of the sequence property without the namespace prefix
     * @param seqValue
     *            The value to add to the sequence.
     */
    public void addUnqualifiedSequenceValue(String simpleSeqName, String seqValue)
    {
        String qualifiedSeqName = simpleSeqName;
        ArrayProperty seq = (ArrayProperty) getAbstractProperty(qualifiedSeqName);
        TextType li = createTextType(XmpConstants.LIST_NAME, seqValue);
        if (seq != null)
        {
            seq.getContainer().addProperty(li);
        }
        else
        {
            ArrayProperty newSeq = createArrayProperty(simpleSeqName, Cardinality.Seq);
            newSeq.getContainer().addProperty(li);
            addProperty(newSeq);
        }
    }

    /**
     * Add a new value to a bag property.
     * 
     * @param qualifiedSeqName
     *            The name of the sequence property, it must include the namespace prefix. ie "pdf:Keywords"
     * @param seqValue
     *            The value to add to the bag.
     */
    public void addBagValue(String qualifiedSeqName, AbstractField seqValue)
    {
        ArrayProperty bag = (ArrayProperty) getAbstractProperty(qualifiedSeqName);
        if (bag != null)
        {
            bag.getContainer().addProperty(seqValue);
        }
        else
        {
            ArrayProperty newBag = createArrayProperty(qualifiedSeqName, Cardinality.Bag);
            newBag.getContainer().addProperty(seqValue);
            addProperty(newBag);
        }
    }

    /**
     * Add a new value to a sequence property.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the namespace prefix. ie "pdf:Keywords"
     * @param seqValue
     *            The value to add to the sequence.
     */
    public void addUnqualifiedSequenceValue(String seqName, AbstractField seqValue)
    {
        String qualifiedSeqName = seqName;
        ArrayProperty seq = (ArrayProperty) getAbstractProperty(qualifiedSeqName);
        if (seq != null)
        {
            seq.getContainer().addProperty(seqValue);
        }
        else
        {
            ArrayProperty newSeq = createArrayProperty(seqName, Cardinality.Seq);
            newSeq.getContainer().addProperty(seqValue);
            addProperty(newSeq);
        }
    }

    /**
     * Get all the values in a sequence property.
     * 
     * @param seqName
     *            The name of the sequence property without namespace prefix.
     * 
     * @return A read-only list of java.lang.String objects or null if the property does not exist.
     */
    public List<String> getUnqualifiedSequenceValueList(String seqName)
    {
        ArrayProperty array = (ArrayProperty) getAbstractProperty(seqName);
        if (array != null)
        {
            return array.getElementsAsString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Remove a date sequence value from the list.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the namespace prefix. ie "pdf:Keywords"
     * @param date
     *            The date to remove from the sequence property.
     */
    public void removeUnqualifiedSequenceDateValue(String seqName, Calendar date)
    {
        String qualifiedSeqName = seqName;
        ArrayProperty seq = (ArrayProperty) getAbstractProperty(qualifiedSeqName);
        if (seq != null)
        {
            ArrayList<AbstractField> toDelete = new ArrayList<AbstractField>();
            Iterator<AbstractField> it = seq.getContainer().getAllProperties().iterator();
            AbstractField tmp;
            while (it.hasNext())
            {
                tmp = it.next();
                if (tmp instanceof DateType)
                {
                    if (((DateType) tmp).getValue().equals(date))
                    {
                        toDelete.add(tmp);

                    }
                }
            }
            Iterator<AbstractField> eraseProperties = toDelete.iterator();
            while (eraseProperties.hasNext())
            {
                seq.getContainer().removeProperty(eraseProperties.next());
            }
        }
    }

    /**
     * Add a date sequence value to the list using the current prefix
     * 
     * @param simpleName
     *            the local name of the property
     * @param date
     *            the value to add
     */
    public void addSequenceDateValueAsSimple(String simpleName, Calendar date)
    {
        addUnqualifiedSequenceDateValue(simpleName, date);
    }

    /**
     * Add a date sequence value to the list.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the namespace prefix. ie "pdf:Keywords"
     * @param date
     *            The date to add to the sequence property.
     */
    public void addUnqualifiedSequenceDateValue(String seqName, Calendar date)
    {
        addUnqualifiedSequenceValue(
                seqName,
                getMetadata().getTypeMapping().createDate(null, XmpConstants.DEFAULT_RDF_LOCAL_NAME,
                        XmpConstants.LIST_NAME, date));
    }

    /**
     * Get all the date values in a sequence property.
     * 
     * @param seqName
     *            The name of the sequence property, it must include the namespace prefix. ie "pdf:Keywords".
     * 
     * @return A read-only list of java.util.Calendar objects or null if the property does not exist.
     * 
     */
    public List<Calendar> getUnqualifiedSequenceDateValueList(String seqName)
    {
        String qualifiedSeqName = seqName;
        List<Calendar> retval = null;
        ArrayProperty seq = (ArrayProperty) getAbstractProperty(qualifiedSeqName);
        if (seq != null)
        {
            retval = new ArrayList<Calendar>();
            Iterator<AbstractField> it = seq.getContainer().getAllProperties().iterator();
            AbstractField tmp;
            while (it.hasNext())
            {
                tmp = it.next();
                if (tmp instanceof DateType)
                {
                    retval.add(((DateType) tmp).getValue());
                }
            }
        }
        return retval;
    }

    /**
     * Method used to place the 'x-default' value in first in Language alternatives as said in xmp spec
     * 
     * @param alt
     *            The property to reorganize
     */
    public void reorganizeAltOrder(ComplexPropertyContainer alt)
    {
        Iterator<AbstractField> it = alt.getAllProperties().iterator();
        AbstractField xdefault = null;
        boolean xdefaultFound = false;
        // If alternatives contains x-default in first value
        if (it.hasNext())
        {
            if (it.next().getAttribute(XmpConstants.LANG_NAME).getValue().equals(XmpConstants.X_DEFAULT))
            {
                return;
            }
        }
        // Find the xdefault definition
        while (it.hasNext() && !xdefaultFound)
        {
            xdefault = it.next();
            if (xdefault.getAttribute(XmpConstants.LANG_NAME).getValue().equals(XmpConstants.X_DEFAULT))
            {
                alt.removeProperty(xdefault);
                xdefaultFound = true;
            }
        }
        if (xdefaultFound)
        {
            it = alt.getAllProperties().iterator();
            ArrayList<AbstractField> reordered = new ArrayList<AbstractField>();
            ArrayList<AbstractField> toDelete = new ArrayList<AbstractField>();
            reordered.add(xdefault);
            AbstractField tmp;
            while (it.hasNext())
            {
                tmp = it.next();
                reordered.add(tmp);
                toDelete.add(tmp);
            }
            Iterator<AbstractField> eraseProperties = toDelete.iterator();
            while (eraseProperties.hasNext())
            {
                alt.removeProperty(eraseProperties.next());
            }
            it = reordered.iterator();
            while (it.hasNext())
            {
                alt.addProperty(it.next());
            }
        }

    }

    /**
     * Set the value of a multi-lingual property.
     * 
     * @param name
     *            The name of the property, it must include the namespace prefix. ie "pdf:Keywords"
     * @param language
     *            The language code of the value. If null then "x-default" is assumed.
     * @param value
     *            The value of the property in the specified language.
     */
    public void setUnqualifiedLanguagePropertyValue(String name, String language, String value)
    {
        String qualifiedName = name;
        AbstractField property = getAbstractProperty(qualifiedName);
        ArrayProperty prop;
        if (property != null)
        {
            // Analyzing content of property
            if (property instanceof ArrayProperty)
            {
                prop = (ArrayProperty) property;
                Iterator<AbstractField> itCplx = prop.getContainer().getAllProperties().iterator();
                // try to find the same lang definition
                AbstractField tmp;
                // Try to find a definition
                while (itCplx.hasNext())
                {
                    tmp = itCplx.next();
                    // System.err.println(tmp.getAttribute("xml:lang").getStringValue());
                    if (tmp.getAttribute(XmpConstants.LANG_NAME).getValue().equals(language))
                    {
                        // the same language has been found
                        if (value == null)
                        {
                            // if value null, erase this definition
                            prop.getContainer().removeProperty(tmp);
                        }
                        else
                        {
                            prop.getContainer().removeProperty(tmp);
                            TextType langValue;
                            langValue = createTextType(XmpConstants.LIST_NAME, value);

                            langValue.setAttribute(new Attribute(XMLConstants.XML_NS_URI, XmpConstants.LANG_NAME,
                                    language));
                            prop.getContainer().addProperty(langValue);
                        }
                        reorganizeAltOrder(prop.getContainer());
                        return;
                    }
                }
                // if no definition found, we add a new one
                TextType langValue;
                langValue = createTextType(XmpConstants.LIST_NAME, value);
                langValue.setAttribute(new Attribute(XMLConstants.XML_NS_URI, XmpConstants.LANG_NAME, language));
                prop.getContainer().addProperty(langValue);
                reorganizeAltOrder(prop.getContainer());
            }
        }
        else
        {
            prop = createArrayProperty(name, Cardinality.Alt);
            TextType langValue;
            langValue = createTextType(XmpConstants.LIST_NAME, value);
            langValue.setAttribute(new Attribute(XMLConstants.XML_NS_URI, XmpConstants.LANG_NAME, language));
            prop.getContainer().addProperty(langValue);
            addProperty(prop);
        }
    }

    /**
     * Get the value of a multi-lingual property.
     * 
     * @param name
     *            The name of the property, without the namespace prefix.
     * @param expectedLanguage
     *            The language code of the value. If null then "x-default" is assumed.
     * 
     * @return The value of the language property.
     */
    public String getUnqualifiedLanguagePropertyValue(String name, String expectedLanguage)
    {
        String language = (expectedLanguage != null) ? expectedLanguage : XmpConstants.X_DEFAULT;
        AbstractField property = getAbstractProperty(name);
        if (property != null)
        {
            if (property instanceof ArrayProperty)
            {
                ArrayProperty prop = (ArrayProperty) property;
                Iterator<AbstractField> langsDef = prop.getContainer().getAllProperties().iterator();
                AbstractField tmp;
                Attribute text;
                while (langsDef.hasNext())
                {
                    tmp = langsDef.next();
                    text = tmp.getAttribute(XmpConstants.LANG_NAME);
                    if (text != null)
                    {
                        if (text.getValue().equals(language))
                        {
                            return ((TextType) tmp).getStringValue();
                        }
                    }
                }
                return null;
            }
            else
            {
                throw new IllegalArgumentException("The property '" + name + "' is not of Lang Alt type");
            }
        }
        return null;
    }

    /**
     * Get a list of all languages that are currently defined for a specific property.
     * 
     * @param name
     *            The name of the property, it must include the namespace prefix. ie "pdf:Keywords"
     * 
     * @return A list of all languages, this will return an non-null empty list if none have been defined.
     */
    public List<String> getUnqualifiedLanguagePropertyLanguagesValue(String name)
    {
        List<String> retval = new ArrayList<String>();
        AbstractField property = getAbstractProperty(name);
        if (property != null)
        {
            if (property instanceof ArrayProperty)
            {
                ArrayProperty prop = (ArrayProperty) property;
                Iterator<AbstractField> langsDef = prop.getContainer().getAllProperties().iterator();
                AbstractField tmp;
                Attribute text;
                while (langsDef.hasNext())
                {
                    tmp = langsDef.next();
                    text = tmp.getAttribute(XmpConstants.LANG_NAME);
                    if (text != null)
                    {
                        retval.add(text.getValue());
                    }
                    else
                    {
                        retval.add(XmpConstants.X_DEFAULT);
                    }
                }
                return retval;
            }
            else
            {
                throw new IllegalArgumentException("The property '" + name + "' is not of Lang Alt type");
            }
        }
        // no property with that name
        return null;
    }

    /**
     * A basic schema merge, it merges bags and sequences and replace everything else.
     * 
     * @param xmpSchema
     *            The schema to merge.
     * @throws IOException
     *             If there is an error during the merge.
     */
    public void merge(XMPSchema xmpSchema) throws IOException
    {
        if (!xmpSchema.getClass().equals(this.getClass()))
        {
            throw new IOException("Can only merge schemas of the same type.");
        }

        Iterator<Attribute> itAtt = xmpSchema.getAllAttributes().iterator();
        Attribute att;
        while (itAtt.hasNext())
        {
            att = itAtt.next();
            if (att.getNamespace().equals(getNamespace()))
            {
                setAttribute(att);
            }
        }

        String analyzedPropQualifiedName;
        Iterator<AbstractField> itProp = xmpSchema.getContainer().getAllProperties().iterator();
        AbstractField prop;
        while (itProp.hasNext())
        {
            prop = itProp.next();
            if (prop.getPrefix().equals(getPrefix()))
            {
                if (prop instanceof ArrayProperty)
                {
                    analyzedPropQualifiedName = prop.getPropertyName();
                    Iterator<AbstractField> itActualEmbeddedProperties = getAllProperties().iterator();
                    AbstractField tmpEmbeddedProperty;

                    Iterator<AbstractField> itNewValues;
                    TextType tmpNewValue;

                    Iterator<AbstractField> itOldValues;
                    TextType tmpOldValue;

                    boolean alreadyPresent = false;

                    while (itActualEmbeddedProperties.hasNext())
                    {
                        tmpEmbeddedProperty = itActualEmbeddedProperties.next();
                        if (tmpEmbeddedProperty instanceof ArrayProperty)
                        {
                            if (tmpEmbeddedProperty.getPropertyName().equals(analyzedPropQualifiedName))
                            {
                                itNewValues = ((ArrayProperty) prop).getContainer().getAllProperties().iterator();
                                // Merge a complex property
                                while (itNewValues.hasNext())
                                {
                                    tmpNewValue = (TextType) itNewValues.next();
                                    itOldValues = ((ArrayProperty) tmpEmbeddedProperty).getContainer()
                                            .getAllProperties().iterator();
                                    while (itOldValues.hasNext() && !alreadyPresent)
                                    {
                                        tmpOldValue = (TextType) itOldValues.next();
                                        if (tmpOldValue.getStringValue().equals(tmpNewValue.getStringValue()))
                                        {
                                            return;
                                        }
                                    }
                                    if (!alreadyPresent)
                                    {
                                        ((ArrayProperty) tmpEmbeddedProperty).getContainer().addProperty(tmpNewValue);
                                    }
                                }

                            }
                        }
                    }
                }
                else
                {
                    addProperty(prop);
                }
            }
        }
    }

    /**
     * Get an AbstractField list corresponding to the content of an array Return null if the property is unknown
     * 
     * @param name
     *            the property name whitout namespace;
     * @return List of property contained in the complex property
     * @throws BadFieldValueException
     *             Property not contains property (not complex property)
     */
    public List<AbstractField> getUnqualifiedArrayList(String name) throws BadFieldValueException
    {
        ArrayProperty array = null;
        Iterator<AbstractField> itProp = getAllProperties().iterator();
        AbstractField tmp;
        while (itProp.hasNext())
        {
            tmp = itProp.next();
            if (tmp.getPropertyName().equals(name))
            {
                if (tmp instanceof ArrayProperty)
                {
                    array = (ArrayProperty) tmp;
                    break;
                }
                else
                {
                    throw new BadFieldValueException("Property asked not seems to be an array");
                }

            }
        }
        if (array != null)
        {
            Iterator<AbstractField> it = array.getContainer().getAllProperties().iterator();
            List<AbstractField> list = new ArrayList<AbstractField>();
            while (it.hasNext())
            {
                list.add(it.next());
            }
            return list;
        }
        return null;
    }

    protected AbstractSimpleProperty instanciateSimple(String param, Object value)
    {
        TypeMapping tm = getMetadata().getTypeMapping();
        return tm.instanciateSimpleField(getClass(), null, getPrefix(), param, value);
    }

}
