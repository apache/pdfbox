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

package org.apache.padaf.xmpbox.schema;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.BooleanType;
import org.apache.padaf.xmpbox.type.ComplexProperty;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.apache.padaf.xmpbox.type.DateType;
import org.apache.padaf.xmpbox.type.Elementable;
import org.apache.padaf.xmpbox.type.IntegerType;
import org.apache.padaf.xmpbox.type.TextType;
import org.w3c.dom.Element;

/**
 * This class represents a metadata schema that can be stored in an XMP
 * document. It handles all generic properties that are available. See
 * subclasses for access to specific properties. MODIFIED TO INCLUDE OBJECT
 * REPRESENTATION
 * 
 */
public class XMPSchema implements Elementable {
	/**
	 * The standard xmlns namespace.
	 */
	public static final String NS_NAMESPACE = "http://www.w3.org/2000/xmlns/";

	public static final String RDFABOUT = "rdf:about";

	protected String localPrefix, localNSUri;
	protected String localPrefixSep;
	protected XMPMetadata metadata;
	protected ComplexPropertyContainer content;

	/**
	 * Create a new blank schema that can be populated.
	 * 
	 * @param metadata
	 *            The parent XMP metadata that this schema will be part of.
	 * @param namespaceName
	 *            The name of the namespace, ie pdf,dc,...
	 * @param namespaceURI
	 *            The URI of the namespace, ie "http://ns.adobe.com/pdf/1.3/"
	 * 
	 */
	public XMPSchema(XMPMetadata metadata, String namespaceName, String namespaceURI) {
		this.metadata = metadata;
		content = new ComplexPropertyContainer(metadata,
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf",
				"Description");

		localPrefix = namespaceName;
		localPrefixSep = localPrefix + ":";
		localNSUri = namespaceURI;
		content.setAttribute(new Attribute(NS_NAMESPACE, "xmlns",
				namespaceName, namespaceURI));

	}

	/**
	 * Get the schema prefix
	 * 
	 * @return Prefix fixed for the schema
	 */
	public String getPrefix() {
		return localPrefix;

	}

	/**
	 * Get the namespace URI of this schema
	 * 
	 * @return the namespace URI of this schema
	 */
	public String getNamespaceValue() {
		return localNSUri;
	}

	/**
	 * Retrieve a generic simple type property
	 * 
	 * @param qualifiedName
	 *            Full qualified name of proeprty wanted
	 * @return The generic simple type property according to its qualified Name
	 */
	public AbstractField getAbstractProperty(String qualifiedName) {
		Iterator<AbstractField> it = content.getAllProperties().iterator();
		AbstractField tmp;
		while (it.hasNext()) {
			tmp = it.next();
			if (tmp.getQualifiedName().equals(qualifiedName)) {
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
	public Attribute getAboutAttribute() {
		return content.getAttribute(RDFABOUT);
	}

	/**
	 * Get the RDF about value.
	 * 
	 * @return The RDF 'about' value.
	 */
	public String getAboutValue() {
		Attribute prop = content.getAttribute(RDFABOUT);
		if (prop != null) {
			return prop.getValue();
		}
		return null;
	}

	/**
	 * Set the RDF 'about' attribute
	 * 
	 * @param about
	 *            the well-formed attribute
	 * @throws BadFieldValueException
	 *             Bad Attribute name (not corresponding to about attribute)
	 */
	public void setAbout(Attribute about) throws BadFieldValueException {
		if (about.getQualifiedName().equals(RDFABOUT)
				|| about.getQualifiedName().equals("about")) {
			content.setAttribute(about);
		} else {
			throw new BadFieldValueException(
					"Attribute 'about' must be named 'rdf:about' or 'about'");
		}
	}

	/**
	 * Set the RDF 'about' attribute. Passing in null will clear this attribute.
	 * 
	 * @param about
	 *            The new RFD about value.
	 */
	public void setAboutAsSimple(String about) {
		if (about == null) {
			content.removeAttribute(RDFABOUT);
		} else {
			content.setAttribute(new Attribute(null, "rdf", "about", about));

		}
	}

	/**
	 * Set a simple specified type property on the schema.
	 * 
	 * @param type
	 *            the property type
	 * @param qualifiedName
	 *            the qualified name to specify for the new property
	 * @param propertyValue
	 *            The value (must be an object understandable by specified type)
	 */
	@SuppressWarnings("unchecked")
	private void setSpecifiedSimpleTypeProperty(
			Class<? extends AbstractSimpleProperty> type, String qualifiedName,
			Object propertyValue) {
		String[] splittedQualifiedName = qualifiedName.split(":");

		Class[] propertyArgsClass = new Class[] { XMPMetadata.class,
				String.class, String.class, Object.class };
		Object[] propertyArgs = new Object[] { metadata,
				splittedQualifiedName[0], splittedQualifiedName[1],
				propertyValue };
		Constructor<? extends AbstractSimpleProperty> propertyConstructor;

		AbstractSimpleProperty specifiedTypeProperty;
		if (propertyValue == null) {
			// Search in properties to erase
			Iterator<AbstractField> it = content.getAllProperties().iterator();
			AbstractField tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (tmp.getQualifiedName().equals(qualifiedName)) {
					content.removeProperty(tmp);
					return;
				}
			}
		} else {
			try {
				propertyConstructor = type.getConstructor(propertyArgsClass);
				specifiedTypeProperty = (AbstractSimpleProperty) propertyConstructor
						.newInstance(propertyArgs);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Failed to create property with the specified type given in parameters");
			}
			// attribute placement for simple property has been removed
			// Search in properties to erase
			Iterator<AbstractField> it = content.getAllProperties().iterator();
			AbstractField tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (tmp.getQualifiedName().equals(qualifiedName)) {
					content.removeProperty(tmp);
					content.addProperty(specifiedTypeProperty);
					return;
				}
			}
			content.addProperty(specifiedTypeProperty);
		}
	}

	/**
	 * Add a SimpleProperty to this schema
	 * 
	 * @param prop
	 *            The Property to add
	 */
	private void setSpecifiedSimpleTypeProperty(AbstractSimpleProperty prop) {
		// attribute placement for simple property has been removed
		// Search in properties to erase
		Iterator<AbstractField> it = content.getAllProperties().iterator();
		AbstractField tmp;
		while (it.hasNext()) {
			tmp = it.next();
			if (tmp.getQualifiedName().equals(prop.getQualifiedName())) {
				content.removeProperty(tmp);
				content.addProperty(prop);
				return;
			}
		}
		content.addProperty(prop);
	}

	/**
	 * Set TextType property
	 * 
	 * @param prop
	 *            The text property to add
	 */
	public void setTextProperty(TextType prop) {
		setSpecifiedSimpleTypeProperty(prop);
	}

	/**
	 * Set a simple text property on the schema.
	 * 
	 * @param qualifiedName
	 *            The name of the property, it must contain the namespace
	 *            prefix, ie "pdf:Keywords"
	 * @param propertyValue
	 *            The value for the property, can be any string. Passing null
	 *            will remove the property.
	 */
	public void setTextPropertyValue(String qualifiedName, String propertyValue) {
		setSpecifiedSimpleTypeProperty(TextType.class, qualifiedName,
				propertyValue);
	}

	/**
	 * Set a simple text property on the schema, using the current prefix.
	 * 
	 * @param simpleName
	 *            the name of the property without prefix
	 * @param propertyValue
	 *            The value for the property, can be any string. Passing null
	 *            will remove the property.
	 */
	public void setTextPropertyValueAsSimple(String simpleName,
			String propertyValue) {
		this.setTextPropertyValue(localPrefixSep + simpleName, propertyValue);
	}

	/**
	 * Get a TextProperty Type from its name
	 * 
	 * @param qualifiedName
	 *            The full qualified name of the property wanted
	 * @return The Text Type property wanted
	 */
	public TextType getTextProperty(String qualifiedName) {
		AbstractField prop = getAbstractProperty(qualifiedName);
		if (prop != null) {
			if (prop instanceof TextType) {
				return (TextType) prop;
			} else {
				throw new IllegalArgumentException(
						"Property asked is not a Text Property");
			}
		}
		return null;
	}

	/**
	 * Get a simple text property value on the schema, using the current prefix.
	 * 
	 * @param simpleName
	 *            The local name of the property wanted
	 * @return The value of the text property or the null if there is no value.
	 * 
	 */
	public String getTextPropertyValueAsSimple(String simpleName) {
		return this.getTextPropertyValue(localPrefixSep + simpleName);
	}

	/**
	 * Get the value of a simple text property.
	 * 
	 * @param qualifiedName
	 *            The name of the property to get, it must include the namespace
	 *            prefix. ie "pdf:Keywords".
	 * 
	 * @return The value of the text property or the null if there is no value.
	 * 
	 */
	public String getTextPropertyValue(String qualifiedName) {
		AbstractField prop = getAbstractProperty(qualifiedName);
		if (prop != null) {
			if (prop instanceof TextType) {
				return ((TextType) prop).getStringValue();
			} else {
				throw new IllegalArgumentException(
						"Property asked is not a Text Property");
			}
		}
		return null;
	}

	/**
	 * Get the Date property with its name
	 * 
	 * @param qualifiedName
	 *            The name of the property to get, it must include the namespace
	 *            prefix. ie "pdf:Keywords".
	 * @return Date Type property
	 * 
	 */
	public DateType getDateProperty(String qualifiedName) {
		AbstractField prop = getAbstractProperty(qualifiedName);
		if (prop != null) {
			if (prop instanceof DateType) {
				return (DateType) prop;
			} else {
				throw new IllegalArgumentException(
						"Property asked is not a Date Property");
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
	public Calendar getDatePropertyValueAsSimple(String simpleName) {
		return this.getDatePropertyValue(localPrefixSep + simpleName);
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
	public Calendar getDatePropertyValue(String qualifiedName) {
		AbstractField prop = getAbstractProperty(qualifiedName);
		if (prop != null) {
			if (prop instanceof DateType) {
				return ((DateType) prop).getValue();
			} else {
				throw new IllegalArgumentException(
						"Property asked is not a Date Property");
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
	public void setDateProperty(DateType date) {
		setSpecifiedSimpleTypeProperty(date);
	}

	/**
	 * Set a simple Date property on the schema, using the current prefix.
	 * 
	 * @param simpleName
	 *            the name of the property without prefix
	 * @param date
	 *            The calendar value for the property, can be any string.
	 *            Passing null will remove the property.
	 */
	public void setDatePropertyValueAsSimple(String simpleName, Calendar date) {
		this.setDatePropertyValue(localPrefixSep + simpleName, date);
	}

	/**
	 * Set the value of the property as a date.
	 * 
	 * @param qualifiedName
	 *            The fully qualified property name for the date.
	 * @param date
	 *            The date to set, or null to clear.
	 */
	public void setDatePropertyValue(String qualifiedName, Calendar date) {
		setSpecifiedSimpleTypeProperty(DateType.class, qualifiedName, date);

	}

	/**
	 * Get a BooleanType property with its name
	 * 
	 * @param qualifiedName
	 *            the full qualified name of property wanted
	 * @return boolean Type property
	 */
	public BooleanType getBooleanProperty(String qualifiedName) {
		AbstractField prop = getAbstractProperty(qualifiedName);
		if (prop != null) {
			if (prop instanceof BooleanType) {
				return (BooleanType) prop;
			} else {
				throw new IllegalArgumentException(
						"Property asked is not a Boolean Property");
			}
		}
		return null;
	}

	/**
	 * Get a simple boolean property value on the schema, using the current
	 * prefix.
	 * 
	 * @param simpleName
	 *            the local name of property wanted
	 * @return The value of the property as a boolean.
	 */
	public Boolean getBooleanPropertyValueAsSimple(String simpleName) {
		return this.getBooleanPropertyValue(localPrefixSep + simpleName);
	}

	/**
	 * Get the value of the property as a boolean.
	 * 
	 * @param qualifiedName
	 *            The fully qualified property name for the boolean.
	 * 
	 * @return The value of the property as a boolean. Return null if property
	 *         not exist
	 */
	public Boolean getBooleanPropertyValue(String qualifiedName) {
		AbstractField prop = getAbstractProperty(qualifiedName);
		if (prop != null) {
			if (prop instanceof BooleanType) {
				return ((BooleanType) prop).getValue();
			} else {
				throw new IllegalArgumentException(
						"Property asked is not a Boolean Property");
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
	public void setBooleanProperty(BooleanType bool) {
		setSpecifiedSimpleTypeProperty(bool);
	}

	/**
	 * Set a simple Boolean property on the schema, using the current prefix.
	 * 
	 * @param simpleName
	 *            the name of the property without prefix
	 * @param bool
	 *            The value for the property, can be any string. Passing null
	 *            will remove the property.
	 */
	public void setBooleanPropertyValueAsSimple(String simpleName, Boolean bool) {
		this.setBooleanPropertyValue(localPrefixSep + simpleName, bool);
	}

	/**
	 * Set the value of the property as a boolean.
	 * 
	 * @param qualifiedName
	 *            The fully qualified property name for the boolean.
	 * @param bool
	 *            The boolean to set, or null to clear.
	 */
	public void setBooleanPropertyValue(String qualifiedName, Boolean bool) {
		setSpecifiedSimpleTypeProperty(BooleanType.class, qualifiedName, bool);
	}

	/**
	 * Get the Integer property with its name
	 * 
	 * @param qualifiedName
	 *            the full qualified name of property wanted
	 * @return Integer Type property
	 */
	public IntegerType getIntegerProperty(String qualifiedName) {
		AbstractField prop = getAbstractProperty(qualifiedName);
		if (prop != null) {
			if (prop instanceof IntegerType) {
				return ((IntegerType) prop);
			} else {
				throw new IllegalArgumentException(
						"Property asked is not an Integer Property");
			}
		}
		return null;
	}

	/**
	 * Get a simple integer property value on the schema, using the current
	 * prefix.
	 * 
	 * @param simpleName
	 *            the local name of property wanted
	 * @return The value of the property as an integer.
	 */
	public Integer getIntegerPropertyValueAsSimple(String simpleName) {
		return this.getIntegerPropertyValue(localPrefixSep + simpleName);
	}

	/**
	 * Get the value of the property as an integer.
	 * 
	 * @param qualifiedName
	 *            The fully qualified property name for the integer.
	 * 
	 * @return The value of the property as an integer.
	 */
	public Integer getIntegerPropertyValue(String qualifiedName) {
		AbstractField prop = getAbstractProperty(qualifiedName);
		if (prop != null) {
			if (prop instanceof IntegerType) {
				return ((IntegerType) prop).getValue();
			} else {
				throw new IllegalArgumentException(
						"Property asked is not an Integer Property");
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
	public void setIntegerProperty(IntegerType prop) {
		setSpecifiedSimpleTypeProperty(prop);
	}

	/**
	 * Set a simple Integer property on the schema, using the current prefix.
	 * 
	 * @param simpleName
	 *            the name of the property without prefix
	 * @param intValue
	 *            The value for the property, can be any string. Passing null
	 *            will remove the property.
	 */
	public void setIntegerPropertyValueAsSimple(String simpleName,
			Integer intValue) {
		this.setIntegerPropertyValue(localPrefixSep + simpleName, intValue);
	}

	/**
	 * Set the value of the property as an integer.
	 * 
	 * @param qualifiedName
	 *            The fully qualified property name for the integer.
	 * @param intValue
	 *            The int to set, or null to clear.
	 */
	public void setIntegerPropertyValue(String qualifiedName, Integer intValue) {
		setSpecifiedSimpleTypeProperty(IntegerType.class, qualifiedName,
				intValue);
	}

	/**
	 * Generic array property removing
	 * 
	 * @param qualifiedArrayName
	 *            the full qualified name of property wanted
	 * @param fieldValue
	 *            the field value
	 */
	private void removeArrayValue(String qualifiedArrayName, String fieldValue) {
		ComplexProperty array = (ComplexProperty) getAbstractProperty(qualifiedArrayName);
		if (array != null) {
			ArrayList<AbstractField> toDelete = new ArrayList<AbstractField>();
			Iterator<AbstractField> it = array.getContainer()
					.getAllProperties().iterator();
			AbstractSimpleProperty tmp;
			while (it.hasNext()) {
				tmp = (AbstractSimpleProperty) it.next();
				if (tmp.getStringValue().equals(fieldValue)) {
					toDelete.add(tmp);
				}
			}
			Iterator<AbstractField> eraseProperties = toDelete.iterator();
			while (eraseProperties.hasNext()) {
				array.getContainer().removeProperty(eraseProperties.next());
			}
		}

	}

	/**
	 * Remove all matching entries with the given value from the bag.
	 * 
	 * @param qualifiedBagName
	 *            The name of the bag, it must include the namespace prefix. ie
	 *            "pdf:Keywords".
	 * @param bagValue
	 *            The value to remove from the bagList.
	 */
	public void removeBagValue(String qualifiedBagName, String bagValue) {
		removeArrayValue(qualifiedBagName, bagValue);
	}

	/**
	 * add a bag value property on the schema, using the current prefix.
	 * 
	 * @param simpleName
	 *            the local name of property
	 * @param bagValue
	 *            the string value to add
	 */
	public void addBagValueAsSimple(String simpleName, String bagValue) {
		this.addBagValue(localPrefixSep + simpleName, bagValue);
	}

	/**
	 * Add an entry to a bag property.
	 * 
	 * @param qualifiedBagName
	 *            The name of the bag, it must include the namespace prefix. ie
	 *            "pdf:Keywords".
	 * @param bagValue
	 *            The value to add to the bagList.
	 */
	public void addBagValue(String qualifiedBagName, String bagValue) {
		String[] splittedQualifiedName = qualifiedBagName.split(":");
		ComplexProperty bag = (ComplexProperty) getAbstractProperty(qualifiedBagName);
		TextType li = new TextType(metadata, "rdf", "li", bagValue);
		if (bag != null) {
			bag.getContainer().addProperty(li);
		} else {
			ComplexProperty newBag = new ComplexProperty(metadata,
					splittedQualifiedName[0], splittedQualifiedName[1],
					ComplexProperty.UNORDERED_ARRAY);
			newBag.getContainer().addProperty(li);
			content.addProperty(newBag);
		}
	}

	/**
	 * Generic String List Builder for arrays contents
	 * 
	 * @param qualifiedArrayName
	 *            the full qualified name of property concerned
	 * @return String list which represents content of array property
	 */
	private List<String> getArrayListToString(String qualifiedArrayName) {
		List<String> retval = null;
		ComplexProperty array = (ComplexProperty) getAbstractProperty(qualifiedArrayName);
		if (array != null) {
			retval = new ArrayList<String>();
			Iterator<AbstractField> it = array.getContainer()
					.getAllProperties().iterator();
			AbstractSimpleProperty tmp;
			while (it.hasNext()) {
				tmp = (AbstractSimpleProperty) it.next();
				retval.add(tmp.getStringValue());
			}
			retval = Collections.unmodifiableList(retval);
		}
		return retval;
	}

	/**
	 * Get all the values of the bag property, using the current prefix. This
	 * will return a list of java.lang.String objects, this is a read-only list.
	 * 
	 * @param simpleName
	 *            the local name of property concerned
	 * 
	 * 
	 * @return All values of the bag property in a list.
	 */
	public List<String> getBagValueListAsSimple(String simpleName) {
		return getBagValueList(localPrefixSep + simpleName);
	}

	/**
	 * Get all the values of the bag property. This will return a list of
	 * java.lang.String objects, this is a read-only list.
	 * 
	 * @param qualifiedBagName
	 *            The name of the bag property to get, it must include the
	 *            namespace prefix. ie "pdf:Keywords"
	 * 
	 * @return All values of the bag property in a list.
	 */
	public List<String> getBagValueList(String qualifiedBagName) {
		return getArrayListToString(qualifiedBagName);
	}

	/**
	 * Remove all matching values from a sequence property.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property. It must include the
	 *            namespace prefix. ie "pdf:Keywords".
	 * @param seqValue
	 *            The value to remove from the list.
	 */
	public void removeSequenceValue(String qualifiedSeqName, String seqValue) {
		removeArrayValue(qualifiedSeqName, seqValue);
	}

	/**
	 * Generic method to remove a field from an array with an Elementable Object
	 * 
	 * @param qualifiedArrayName
	 *            the full qualified name of the property concerned
	 * @param fieldValue
	 *            the elementable field value
	 */
	public void removeArrayValue(String qualifiedArrayName,
			Elementable fieldValue) {
		ComplexProperty array = (ComplexProperty) getAbstractProperty(qualifiedArrayName);
		if (array != null) {
			ArrayList<AbstractField> toDelete = new ArrayList<AbstractField>();
			Iterator<AbstractField> it = array.getContainer()
					.getAllProperties().iterator();
			AbstractSimpleProperty tmp;
			while (it.hasNext()) {
				tmp = (AbstractSimpleProperty) it.next();
				if (tmp.equals(fieldValue)) {
					toDelete.add(tmp);
				}
			}
			Iterator<AbstractField> eraseProperties = toDelete.iterator();
			while (eraseProperties.hasNext()) {
				array.getContainer().removeProperty(eraseProperties.next());
			}
		}
	}

	/**
	 * Remove a value from a sequence property. This will remove all entries
	 * from the list.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property. It must include the
	 *            namespace prefix. ie "pdf:Keywords".
	 * @param seqValue
	 *            The value to remove from the list.
	 */
	public void removeSequenceValue(String qualifiedSeqName,
			Elementable seqValue) {
		removeArrayValue(qualifiedSeqName, seqValue);
	}

	/**
	 * Add a new value to a sequence property.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property, it must include the
	 *            namespace prefix. ie "pdf:Keywords"
	 * @param seqValue
	 *            The value to add to the sequence.
	 */
	public void addSequenceValue(String qualifiedSeqName, String seqValue) {
		String[] splittedQualifiedName = qualifiedSeqName.split(":");
		ComplexProperty seq = (ComplexProperty) getAbstractProperty(qualifiedSeqName);
		TextType li = new TextType(metadata, "rdf", "li", seqValue);
		if (seq != null) {
			seq.getContainer().addProperty(li);
		} else {
			ComplexProperty newSeq = new ComplexProperty(metadata,
					splittedQualifiedName[0], splittedQualifiedName[1],
					ComplexProperty.ORDERED_ARRAY);
			newSeq.getContainer().addProperty(li);
			content.addProperty(newSeq);
		}

	}

	/**
	 * Add a new value to a bag property.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property, it must include the
	 *            namespace prefix. ie "pdf:Keywords"
	 * @param seqValue
	 *            The value to add to the bag.
	 */
	public void addBagValue(String qualifiedSeqName, AbstractField seqValue) {

		String[] splittedQualifiedName = qualifiedSeqName.split(":");
		ComplexProperty bag = (ComplexProperty) getAbstractProperty(qualifiedSeqName);
		if (bag != null) {
			bag.getContainer().addProperty(seqValue);
		} else {
			ComplexProperty newBag = new ComplexProperty(metadata,
					splittedQualifiedName[0], splittedQualifiedName[1],
					ComplexProperty.UNORDERED_ARRAY);
			newBag.getContainer().addProperty(seqValue);
			content.addProperty(newBag);
		}
	}

	/**
	 * add a new value to a sequence property using the current prefix.
	 * 
	 * @param simpleName
	 *            the local name of the property
	 * @param seqValue
	 *            the string value to add
	 */
	public void addSequenceValueAsSimple(String simpleName, String seqValue) {
		this.addSequenceValue(localPrefixSep + simpleName, seqValue);
	}

	/**
	 * Add a new value to a sequence property.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property, it must include the
	 *            namespace prefix. ie "pdf:Keywords"
	 * @param seqValue
	 *            The value to add to the sequence.
	 */
	public void addSequenceValue(String qualifiedSeqName, AbstractField seqValue) {

		String[] splittedQualifiedName = qualifiedSeqName.split(":");
		ComplexProperty seq = (ComplexProperty) getAbstractProperty(qualifiedSeqName);
		if (seq != null) {
			seq.getContainer().addProperty(seqValue);
		} else {
			ComplexProperty newSeq = new ComplexProperty(metadata,
					splittedQualifiedName[0], splittedQualifiedName[1],
					ComplexProperty.ORDERED_ARRAY);
			newSeq.getContainer().addProperty(seqValue);
			content.addProperty(newSeq);
		}
	}

	/**
	 * Get all the values in a sequence property, using the current prefix.
	 * 
	 * @param simpleName
	 *            the local name of the property
	 * @return A read-only list of java.lang.String objects or null if the
	 *         property does not exist.
	 */
	public List<String> getSequenceValueListAsSimple(String simpleName) {
		return this.getSequenceValueList(localPrefixSep + simpleName);
	}

	/**
	 * Get all the values in a sequence property.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property, it must include the
	 *            namespace prefix. ie "pdf:Keywords".
	 * 
	 * @return A read-only list of java.lang.String objects or null if the
	 *         property does not exist.
	 */
	public List<String> getSequenceValueList(String qualifiedSeqName) {
		return getArrayListToString(qualifiedSeqName);
	}

	/**
	 * Remove a date sequence value from the list.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property, it must include the
	 *            namespace prefix. ie "pdf:Keywords"
	 * @param date
	 *            The date to remove from the sequence property.
	 */
	public void removeSequenceDateValue(String qualifiedSeqName, Calendar date) {
		ComplexProperty seq = (ComplexProperty) getAbstractProperty(qualifiedSeqName);
		if (seq != null) {
			ArrayList<AbstractField> toDelete = new ArrayList<AbstractField>();
			Iterator<AbstractField> it = seq.getContainer().getAllProperties()
					.iterator();
			AbstractField tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (tmp instanceof DateType) {
					if (((DateType) tmp).getValue().equals(date)) {
						toDelete.add(tmp);

					}
				}
			}
			Iterator<AbstractField> eraseProperties = toDelete.iterator();
			while (eraseProperties.hasNext()) {
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
	public void addSequenceDateValueAsSimple(String simpleName, Calendar date) {
		addSequenceDateValue(localPrefixSep + simpleName, date);
	}

	/**
	 * Add a date sequence value to the list.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property, it must include the
	 *            namespace prefix. ie "pdf:Keywords"
	 * @param date
	 *            The date to add to the sequence property.
	 */
	public void addSequenceDateValue(String qualifiedSeqName, Calendar date) {
		addSequenceValue(qualifiedSeqName, new DateType(metadata, "rdf", "li",
				date));
	}

	/**
	 * Get all the date values in a sequence property, using the current prefix.
	 * 
	 * @param simpleName
	 *            the local name of property concerned
	 * @return A read-only list of java.util.Calendar objects or null if the
	 *         property does not exist.
	 */
	public List<Calendar> getSequenceDateValueListAsSimple(String simpleName) {
		return this.getSequenceDateValueList(localPrefixSep + simpleName);
	}

	/**
	 * Get all the date values in a sequence property.
	 * 
	 * @param qualifiedSeqName
	 *            The name of the sequence property, it must include the
	 *            namespace prefix. ie "pdf:Keywords".
	 * 
	 * @return A read-only list of java.util.Calendar objects or null if the
	 *         property does not exist.
	 * 
	 */
	public List<Calendar> getSequenceDateValueList(String qualifiedSeqName) {
		List<Calendar> retval = null;
		ComplexProperty seq = (ComplexProperty) getAbstractProperty(qualifiedSeqName);
		if (seq != null) {
			retval = new ArrayList<Calendar>();
			Iterator<AbstractField> it = seq.getContainer().getAllProperties()
					.iterator();
			AbstractField tmp;
			while (it.hasNext()) {
				tmp = it.next();
				if (tmp instanceof DateType) {
					retval.add(((DateType) tmp).getValue());
				}
			}
		}
		return retval;
	}

	/**
	 * Method used to place the 'x-default' value in first in Language
	 * alternatives as said in xmp spec
	 * 
	 * @param alt
	 *            The property to reorganize
	 */
	public void reorganizeAltOrder(ComplexPropertyContainer alt) {
		Iterator<AbstractField> it = alt.getAllProperties().iterator();
		AbstractField xdefault = null;
		boolean xdefaultFound = false;
		// If alternatives contains x-default in first value
		if (it.hasNext()) {
			if (it.next().getAttribute("xml:lang").getValue().equals(
					"x-default")) {
				return;
			}
		}
		// Find the xdefault definition
		while (it.hasNext() && !xdefaultFound) {
			xdefault = it.next();
			if (xdefault.getAttribute("xml:lang").getValue()
					.equals("x-default")) {
				alt.removeProperty(xdefault);
				xdefaultFound = true;
			}
		}
		if (xdefaultFound) {
			it = alt.getAllProperties().iterator();
			ArrayList<AbstractField> reordered = new ArrayList<AbstractField>();
			ArrayList<AbstractField> toDelete = new ArrayList<AbstractField>();
			reordered.add(xdefault);
			AbstractField tmp;
			while (it.hasNext()) {
				tmp = it.next();
				reordered.add(tmp);
				toDelete.add(tmp);
			}
			Iterator<AbstractField> eraseProperties = toDelete.iterator();
			while (eraseProperties.hasNext()) {
				alt.removeProperty(eraseProperties.next());
			}
			it = reordered.iterator();
			while (it.hasNext()) {
				alt.addProperty(it.next());
			}
		}

	}

	/**
	 * Set a multi-lingual property on the schema, using the current prefix.
	 * 
	 * @param simpleName
	 *            the local name of the property
	 * @param language
	 *            the language concerned
	 * @param value
	 *            the value to set for the language specified
	 */
	public void setLanguagePropertyValueAsSimple(String simpleName,
			String language, String value) {
		this.setLanguagePropertyValue(localPrefixSep + simpleName, language,
				value);
	}

	/**
	 * Set the value of a multi-lingual property.
	 * 
	 * @param qualifiedName
	 *            The name of the property, it must include the namespace
	 *            prefix. ie "pdf:Keywords"
	 * @param language
	 *            The language code of the value. If null then "x-default" is
	 *            assumed.
	 * @param value
	 *            The value of the property in the specified language.
	 */
	public void setLanguagePropertyValue(String qualifiedName, String language,
			String value) {
		AbstractField property = getAbstractProperty(qualifiedName);
		ComplexProperty prop;
		if (property != null) {
			// Analyzing content of property
			if (property instanceof ComplexProperty) {
				prop = (ComplexProperty) property;
				Iterator<AbstractField> itCplx = prop.getContainer()
						.getAllProperties().iterator();
				// try to find the same lang definition
				AbstractField tmp;
				// Try to find a definition
				while (itCplx.hasNext()) {
					tmp = itCplx.next();
					// System.err.println(tmp.getAttribute("xml:lang").getStringValue());
					if (tmp.getAttribute("xml:lang").getValue()
							.equals(language)) {
						// the same language has been found
						if (value == null) {
							// if value null, erase this definition
							prop.getContainer().removeProperty(tmp);
						} else {
							prop.getContainer().removeProperty(tmp);
							TextType langValue;
							langValue = new TextType(metadata, "rdf", "li",
									value);

							langValue.setAttribute(new Attribute(null, "xml",
									"lang", language));
							prop.getContainer().addProperty(langValue);
						}
						reorganizeAltOrder(prop.getContainer());
						return;
					}
				}
				// if no definition found, we add a new one
				TextType langValue;
				langValue = new TextType(metadata, "rdf", "li", value);
				langValue.setAttribute(new Attribute(null, "xml", "lang",
						language));
				prop.getContainer().addProperty(langValue);
				reorganizeAltOrder(prop.getContainer());
			}
		} else {
			String[] splittedQualifiedName = qualifiedName.split(":");
			prop = new ComplexProperty(metadata, splittedQualifiedName[0],
					splittedQualifiedName[1], ComplexProperty.ALTERNATIVE_ARRAY);
			TextType langValue;
			langValue = new TextType(metadata, "rdf", "li", value);
			langValue
					.setAttribute(new Attribute(null, "xml", "lang", language));
			prop.getContainer().addProperty(langValue);
			content.addProperty(prop);
		}
	}

	/**
	 * Get the value of a multi-lingual property, using the current prefix.
	 * 
	 * @param simpleName
	 *            the local name of the property
	 * @param language
	 *            The language code of the value. If null then "x-default" is
	 *            assumed.
	 * 
	 * @return The value of the language property.
	 */
	public String getLanguagePropertyValueAsSimple(String simpleName,
			String language) {
		return this.getLanguagePropertyValue(localPrefixSep + simpleName,
				language);
	}

	/**
	 * Get the value of a multi-lingual property.
	 * 
	 * @param qualifiedName
	 *            The name of the property, it must include the namespace
	 *            prefix. ie "pdf:Keywords"
	 * @param language
	 *            The language code of the value. If null then "x-default" is
	 *            assumed.
	 * 
	 * @return The value of the language property.
	 */
	public String getLanguagePropertyValue(String qualifiedName, String language) {
		if (language == null) {
			language = "x-default";
		}

		AbstractField property = getAbstractProperty(qualifiedName);
		if (property != null) {
			if (property instanceof ComplexProperty) {
				ComplexProperty prop = (ComplexProperty) property;
				Iterator<AbstractField> langsDef = prop.getContainer()
						.getAllProperties().iterator();
				AbstractField tmp;
				Attribute text;
				while (langsDef.hasNext()) {
					tmp = langsDef.next();
					text = tmp.getAttribute("xml:lang");
					if (text != null) {
						if (text.getValue().equals(language)) {
							return ((TextType) tmp).getStringValue();
						}
					}
				}
				return null;
			} else {
				throw new IllegalArgumentException("The property '"
						+ qualifiedName + "' is not of Lang Alt type");
			}
		}
		return null;
	}

	/**
	 * Get a list of all languages that are currently defined for a specific
	 * property, using the current prefix.
	 * 
	 * @param simpleName
	 *            the local name of the property
	 * @return A list of all languages, this will return an non-null empty list
	 *         if none have been defined.
	 */
	public List<String> getLanguagePropertyLanguagesValueAsSimple(
			String simpleName) {
		return this.getLanguagePropertyLanguagesValue(localPrefixSep
				+ simpleName);
	}

	/**
	 * Get a list of all languages that are currently defined for a specific
	 * property.
	 * 
	 * @param qualifiedName
	 *            The name of the property, it must include the namespace
	 *            prefix. ie "pdf:Keywords"
	 * 
	 * @return A list of all languages, this will return an non-null empty list
	 *         if none have been defined.
	 */
	public List<String> getLanguagePropertyLanguagesValue(String qualifiedName) {
		List<String> retval = new ArrayList<String>();

		AbstractField property = getAbstractProperty(qualifiedName);
		if (property != null) {
			if (property instanceof ComplexProperty) {
				ComplexProperty prop = (ComplexProperty) property;
				Iterator<AbstractField> langsDef = prop.getContainer()
						.getAllProperties().iterator();
				AbstractField tmp;
				Attribute text;
				while (langsDef.hasNext()) {
					tmp = langsDef.next();
					text = tmp.getAttribute("xml:lang");
					if (text != null) {
						retval.add(text.getValue());
					} else {
						retval.add("x-default");
					}
				}
				return retval;
			} else {
				throw new IllegalArgumentException("The property '"
						+ qualifiedName + "' is not of Lang Alt type");
			}
		}
		// no property with that name
		return null;
	}

	/**
	 * A basic schema merge, it merges bags and sequences and replace everything
	 * else.
	 * 
	 * @param xmpSchema
	 *            The schema to merge.
	 * @throws IOException
	 *             If there is an error during the merge.
	 */
	public void merge(XMPSchema xmpSchema) throws IOException {
		if (!xmpSchema.getClass().equals(this.getClass())) {
			throw new IOException("Can only merge schemas of the same type.");
		}

		Iterator<Attribute> itAtt = xmpSchema.content.getAllAttributes()
				.iterator();
		Attribute att;
		while (itAtt.hasNext()) {
			att = itAtt.next();
			if (att.getPrefix().equals(getPrefix())) {
				content.setAttribute(att);
			}
		}

		String analyzedPropQualifiedName;
		Iterator<AbstractField> itProp = xmpSchema.content.getAllProperties()
				.iterator();
		AbstractField prop;
		while (itProp.hasNext()) {
			prop = itProp.next();
			if (prop.getPrefix().equals(getPrefix())) {
				if (prop instanceof ComplexProperty) {
					analyzedPropQualifiedName = prop.getQualifiedName();
					Iterator<AbstractField> itActualEmbeddedProperties = content
							.getAllProperties().iterator();
					AbstractField tmpEmbeddedProperty;

					Iterator<AbstractField> itNewValues;
					TextType tmpNewValue;

					Iterator<AbstractField> itOldValues;
					TextType tmpOldValue;

					boolean alreadyPresent = false;

					while (itActualEmbeddedProperties.hasNext()) {
						tmpEmbeddedProperty = itActualEmbeddedProperties.next();
						if (tmpEmbeddedProperty instanceof ComplexProperty) {
							if (tmpEmbeddedProperty.getQualifiedName().equals(
									analyzedPropQualifiedName)) {
								itNewValues = ((ComplexProperty) prop)
										.getContainer().getAllProperties()
										.iterator();
								// Merge a complex property
								while (itNewValues.hasNext()) {
									tmpNewValue = (TextType) itNewValues.next();
									itOldValues = ((ComplexProperty) tmpEmbeddedProperty)
											.getContainer().getAllProperties()
											.iterator();
									while (itOldValues.hasNext()
											&& !alreadyPresent) {
										tmpOldValue = (TextType) itOldValues
												.next();
										if (tmpOldValue
												.getStringValue()
												.equals(
														tmpNewValue
																.getStringValue())) {
											alreadyPresent = true;
										}
									}
									if (!alreadyPresent) {
										((ComplexProperty) tmpEmbeddedProperty)
												.getContainer().addProperty(
														tmpNewValue);
									}
								}

							}
						}
					}
				} else {
					content.addProperty(prop);
				}
			}
		}
	}

	/**
	 * Get an AbstractField list corresponding to the content of an array Return
	 * null if the property is unknown
	 * 
	 * @param qualifiedName
	 *            the full qualified name of the property concerned
	 * @return List of property contained in the complex property
	 * @throws BadFieldValueException
	 *             Property not contains property (not complex property)
	 */
	public List<AbstractField> getArrayList(String qualifiedName)
			throws BadFieldValueException {
		ComplexProperty array = null;
		Iterator<AbstractField> itProp = content.getAllProperties().iterator();
		AbstractField tmp;
		while (itProp.hasNext()) {
			tmp = itProp.next();
			if (tmp.getQualifiedName().equals(qualifiedName)) {
				if (tmp instanceof ComplexProperty) {
					array = (ComplexProperty) tmp;
					break;
				} else {
					throw new BadFieldValueException(
							"Property asked not seems to be an array");
				}

			}
		}
		if (array != null) {
			Iterator<AbstractField> it = array.getContainer()
					.getAllProperties().iterator();
			List<AbstractField> list = new ArrayList<AbstractField>();
			while (it.hasNext()) {
				list.add(it.next());
			}
			return list;
		}
		return null;
	}

	/**
	 * Get PropertyContainer of this Schema
	 * 
	 * @return the ComplexProperty which represents the schema content
	 */
	public ComplexPropertyContainer getContent() {
		return content;
	}

	/**
	 * Get All attributes defined for this schema
	 * 
	 * @return Attributes list defined for this schema
	 */
	public List<Attribute> getAllAttributes() {
		return content.getAllAttributes();
	}

	/**
	 * Get All properties defined in this schema
	 * 
	 * @return Properties list defined in this schema
	 */
	public List<AbstractField> getAllProperties() {
		return content.getAllProperties();
	}

	/**
	 * Set a new attribute for this schema
	 * 
	 * @param attr
	 *            The new Attribute to set
	 */
	public void setAttribute(Attribute attr) {
		content.setAttribute(attr);
	}

	/**
	 * Add a new Property to this schema
	 * 
	 * @param obj
	 *            The new property to add
	 */
	public void addProperty(AbstractField obj) {
		content.addProperty(obj);
	}

	/**
	 * Get DOM Element for rdf/xml serialization
	 * 
	 * @return the DOM Element
	 */
	public Element getElement() {
		return content.getElement();
	}

	/**
	 * get a Property with its name, using the current prefix
	 * 
	 * @param simpleName
	 *            the local name of the property
	 * @return The property wanted
	 */
	protected AbstractField getPropertyAsSimple(String simpleName) {
		return getProperty(localPrefixSep + simpleName);
	}

	/**
	 * get a Property with its qualified Name (with its prefix)
	 * 
	 * @param qualifiedName
	 *            The full qualified name of the property wanted
	 * @return the property wanted
	 */
	protected AbstractField getProperty(String qualifiedName) {
		Iterator<AbstractField> it = getAllProperties().iterator();
		AbstractField tmp;
		while (it.hasNext()) {
			tmp = it.next();
			if (tmp.getQualifiedName().equals(qualifiedName)) {
				return tmp;
			}
		}
		return null;
	}

	/**
	 * Return local prefix
	 * 
	 * @return current prefix fixed for this schema
	 */
	public String getLocalPrefix() {
		return this.localPrefix;
	}

	/**
	 * Return local prefix with separator
	 * 
	 * @return current prefix fixed for this schema with ':' separator
	 */
	public String getLocalPrefixWithSeparator() {
		return this.localPrefixSep;
	}

}
