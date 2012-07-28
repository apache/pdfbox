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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.AgentNameType;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.ArrayProperty;
import org.apache.padaf.xmpbox.type.DateType;
import org.apache.padaf.xmpbox.type.IntegerType;
import org.apache.padaf.xmpbox.type.PropertyType;
import org.apache.padaf.xmpbox.type.TextType;
import org.apache.padaf.xmpbox.type.ThumbnailType;
import org.apache.padaf.xmpbox.type.URLType;


/**
 * Representation of XMPBasic Schema
 * 
 * @author a183132
 * 
 */
public class XMPBasicSchema extends XMPSchema {

	public static final String PREFERRED_XMP_PREFIX = "xmp";

	public static final String XMPBASICURI = "http://ns.adobe.com/xap/1.0/";

	@PropertyType(propertyType = "bag Xpath")
	public static final String ADVISORY = "Advisory";

	@PropertyType(propertyType = "URL")
	public static final String BASEURL = "BaseURL";

	@PropertyType(propertyType = "Date")
	public static final String CREATEDATE = "CreateDate";

	@PropertyType(propertyType = "AgentName")
	public static final String CREATORTOOL = "CreatorTool";

	@PropertyType(propertyType = "bag Text")
	public static final String IDENTIFIER = "Identifier";

	@PropertyType(propertyType = "Text")
	public static final String LABEL = "Label";

	@PropertyType(propertyType = "Date")
	public static final String METADATADATE = "MetadataDate";

	@PropertyType(propertyType = "Date")
	public static final String MODIFYDATE = "ModifyDate";

	@PropertyType(propertyType = "Text")
	public static final String NICKNAME = "Nickname";

	@PropertyType(propertyType = "Integer")
	public static final String RATING = "Rating";

	@PropertyType(propertyType = "Alt Thumbnail")
	public static final String THUMBNAILS = "Thumbnails";

	private ArrayProperty altThumbs;

	/**
	 * Constructor of XMPBasic schema with preferred prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 */
	public XMPBasicSchema(XMPMetadata metadata) {
		super(metadata, PREFERRED_XMP_PREFIX, XMPBASICURI);

	}

	/**
	 * Constructor of XMPBasic schema with specified prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 * @param ownPrefix
	 *            The prefix to assign
	 */
	public XMPBasicSchema(XMPMetadata metadata, String ownPrefix) {
		super(metadata, ownPrefix, XMPBASICURI);

	}

	/**
	 * Add thumbnail to thumbnails list
	 * 
	 * @param height
	 *            height format
	 * @param width
	 *            width format
	 * @param format
	 *            thumbnail format
	 * @param img
	 *            Image data
	 */
	public void addThumbnails(Integer height, Integer width, String format,
			String img) {
		if (altThumbs == null) {
			altThumbs = new ArrayProperty(getMetadata(), null, getLocalPrefix(), THUMBNAILS,
					ArrayProperty.ALTERNATIVE_ARRAY);
			addProperty(altThumbs);
		}
		ThumbnailType thumb = new ThumbnailType(getMetadata());
		thumb.setHeight( height);
		thumb.setWidth( width);
		thumb.setFormat( format);
		thumb.setImage( img);
		altThumbs.getContainer().addProperty(thumb);
	}

	/**
	 * Add a property specification that were edited outside the authoring
	 * application
	 * 
	 * @param xpath
	 *            the value to add
	 */
	public void addAdvisory(String xpath) {
		addQualifiedBagValue(ADVISORY, xpath);
	}

	public void removeAdvisory (String xpath) {
		removeUnqualifiedBagValue(ADVISORY, xpath);
	}
	
	/**
	 * Set the base URL for relative URLs in the document content
	 * 
	 * @param url
	 *            the Base url value to set
	 */
	public void setBaseURL(String url) {
		URLType tt = (URLType)instanciateSimple(BASEURL, url);
		setBaseURLProperty(tt);
	}

	/**
	 * Set the base URL property
	 * 
	 * @param url
	 *            the Base url property to set
	 */
	public void setBaseURLProperty(URLType url) {
		addProperty(url);
	}

	/**
	 * Set the date and time the resource was originally created
	 * 
	 * @param date
	 *            the value to set
	 */
	public void setCreateDate(Calendar date) {
		DateType tt = (DateType)instanciateSimple(CREATEDATE, date);
		setCreateDateProperty(tt);
	}

	/**
	 * Set the create date property
	 * 
	 * @param date
	 *            the create date property to set
	 */
	public void setCreateDateProperty(DateType date) {
		addProperty(date);
	}

	/**
	 * set the name of the first known tool used to create this resource
	 * 
	 * @param creatorTool
	 *            the creator tool value to set
	 */
	public void setCreatorTool(String creatorTool) {
		AgentNameType tt = (AgentNameType)instanciateSimple(CREATORTOOL, creatorTool);
		setCreatorToolProperty(tt);
	}

	/**
	 * set the creatorTool property
	 * 
	 * @param creatorTool
	 *            the creator tool property to set
	 */
	public void setCreatorToolProperty(AgentNameType creatorTool) {
		addProperty(creatorTool);
	}

	/**
	 * Add a text string which unambiguously identify the resource within a
	 * given context
	 * 
	 * @param text
	 *            the identifier value to add
	 */
	public void addIdentifier(String text) {
		addQualifiedBagValue(IDENTIFIER, text);
	}
	
	public void removeIdentifier(String text) {
		removeUnqualifiedBagValue(IDENTIFIER, text);
	}

	/**
	 * set a word or a short phrase which identifies a document as a member of a
	 * user-defined collection
	 * 
	 * @param text
	 *            the label value to set
	 */
	public void setLabel(String text) {
		TextType tt = (TextType)instanciateSimple(LABEL, text);
		setLabelProperty(tt);
	}

	/**
	 * set the label property
	 * 
	 * @param text
	 *            the label property to set
	 */
	public void setLabelProperty(TextType text) {
		addProperty(text);
	}

	/**
	 * Set the date and time that any metadata for this resource was last
	 * changed. (should be equals or more recent than the createDate)
	 * 
	 * @param date
	 *            the Metadata Date value to set
	 */
	public void setMetadataDate(Calendar date) {
		DateType tt =(DateType)instanciateSimple(METADATADATE, date);
		setMetadataDateProperty(tt);
}

	/**
	 * Set the MetadataDate property
	 * 
	 * @param date
	 *            the Metadata Date property to set
	 */
	public void setMetadataDateProperty(DateType date) {
		addProperty(date);
	}

	/**
	 * Set the date and time the resource was last modified
	 * 
	 * @param date
	 *            the Modify Date value to set
	 */
	public void setModifyDate(Calendar date) {
		DateType tt = (DateType)instanciateSimple(MODIFYDATE, date);
		setModifyDateProperty(tt);
	}

	/**
	 * Set the ModifyDate property
	 * 
	 * @param date
	 *            the Modify Date property to set
	 */
	public void setModifyDateProperty(DateType date) {
		addProperty(date);
	}

	/**
	 * Set a short informal name for the resource
	 * 
	 * @param text
	 *            the Nickname value to set
	 */
	public void setNickname(String text) {
		TextType tt = (TextType)instanciateSimple(NICKNAME, text);
		setNicknameProperty(tt);
	}

	/**
	 * Set the NickName property
	 * 
	 * @param text
	 *            the Nickname property to set
	 */
	public void setNicknameProperty(TextType text) {
		addProperty(text);
	}

	/**
	 * Set a number that indicates a document's status relative to other
	 * documents, used to organize documents in a file browser (values are
	 * user-defined within an application-defined range)
	 * 
	 * @param rate
	 *            the rate value to set
	 */
	public void setRating(Integer rate) {
//		addProperty(new IntegerType(metadata, localPrefix, RATING, rate));
		IntegerType tt = (IntegerType)instanciateSimple(RATING, rate);
		setRatingProperty(tt);
		
	}

	/**
	 * Set Rating Property
	 * 
	 * @param rate
	 *            the rate property to set
	 */
	public void setRatingProperty(IntegerType rate) {
		addProperty(rate);
	}

	/**
	 * Get the Advisory property
	 * 
	 * @return the advisory property
	 */
	public ArrayProperty getAdvisoryProperty() {
		return (ArrayProperty) getUnqualifiedProperty(ADVISORY);
	}

	/**
	 * Get the Advisory property values
	 * 
	 * @return list of adivory values
	 */
	public List<String> getAdvisory() {
		return getUnqualifiedBagValueList(ADVISORY);
	}

    /**
     * Convenience method for jempbox signature compatibility
     *
     * @see XMPBasicSchema#getAdvisory()
     */
	@Deprecated
    public List<String> getAdvisories()
    {
        return getAdvisory();
    }
    

	/**
	 * Get the BaseURL property
	 * 
	 * @return the base url property
	 */
	public TextType getBaseURLProperty() {
		return (TextType) getUnqualifiedProperty(BASEURL);
	}

	/**
	 * Get the BaseURL property value
	 * 
	 * @return the base url value
	 */
	public String getBaseURL() {
		TextType tt = ((TextType) getUnqualifiedProperty(BASEURL));
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Get the CreateDate property
	 * 
	 * @return the CreateDate property
	 */
	public DateType getCreateDateProperty() {
		return (DateType) getUnqualifiedProperty(CREATEDATE);
	}

	/**
	 * Get the CreateDate property value
	 * 
	 * @return the CreateDate value
	 */
	public Calendar getCreateDate() {
		DateType createDate = (DateType) getUnqualifiedProperty(CREATEDATE);
		if (createDate != null) {
			return createDate.getValue();
		}
		return null;
	}

	/**
	 * Get the CreationTool property
	 * 
	 * @return the CreationTool property
	 */
	public TextType getCreatorToolProperty() {
		return (TextType) getUnqualifiedProperty(CREATORTOOL);
	}

	/**
	 * Get the CreationTool property value
	 * 
	 * @return the CreationTool value
	 */
	public String getCreatorTool() {
		TextType tt = ((TextType) getUnqualifiedProperty(CREATORTOOL));
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Get the Identifier property
	 * 
	 * @return the Identifier property
	 */
	public ArrayProperty getIdentifiersProperty() {
		return (ArrayProperty) getUnqualifiedProperty(IDENTIFIER);
	}

	/**
	 * Get the Identifier property values
	 * 
	 * @return list of all identifier values
	 */
	public List<String> getIdentifiers() {
		return getUnqualifiedBagValueList(IDENTIFIER);
	}

	/**
	 * Get the label property
	 * 
	 * @return the label property
	 */
	public TextType getLabelProperty() {
		return (TextType) getUnqualifiedProperty(LABEL);
	}

	/**
	 * Get the label property value
	 * 
	 * @return the label value
	 */
	public String getLabel() {
		TextType tt = ((TextType) getUnqualifiedProperty(LABEL));
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Get the MetadataDate property
	 * 
	 * @return the MetadataDate property
	 */
	public DateType getMetadataDateProperty() {
		return (DateType) getUnqualifiedProperty(METADATADATE);
	}

	/**
	 * Get the MetadataDate property value
	 * 
	 * @return the MetadataDate value
	 */
	public Calendar getMetadataDate() {
		DateType dt = ((DateType) getUnqualifiedProperty(METADATADATE));
		return dt == null ? null : dt.getValue();
	}

	/**
	 * Get the ModifyDate property
	 * 
	 * @return the ModifyDate property
	 */
	public DateType getModifyDateProperty() {
		return (DateType) getUnqualifiedProperty(MODIFYDATE);
	}

	/**
	 * Get the ModifyDate property value
	 * 
	 * @return the ModifyDate value
	 */
	public Calendar getModifyDate() {
		DateType modifyDate = (DateType) getUnqualifiedProperty(MODIFYDATE);
		if (modifyDate != null) {
			return modifyDate.getValue();
		}
		return null;

	}

	/**
	 * Get the Nickname property
	 * 
	 * @return the Nickname property
	 */
	public TextType getNicknameProperty() {
		return (TextType) getUnqualifiedProperty(NICKNAME);
	}

	/**
	 * Get the Nickname property value
	 * 
	 * @return the Nickname value
	 */
	public String getNickname() {
		TextType tt = ((TextType) getUnqualifiedProperty(NICKNAME));
		return tt == null ? null : tt.getStringValue();
	}

	/**
	 * Get the Rating property
	 * 
	 * @return the Rating property
	 */
	public IntegerType getRatingProperty() {
		return ((IntegerType) getUnqualifiedProperty(RATING));
	}

	/**
	 * Get the Rating property value
	 * 
	 * @return the Rating value
	 */
	public Integer getRating() {
		IntegerType it = ((IntegerType) getUnqualifiedProperty(RATING));
		return it == null ? null : it.getValue();
	}

	/**
	 * Get list of Thumbnails
	 * 
	 * @return List of all thumbnails properties defined
	 * @throws BadFieldValueException
	 *             if one thumbnail is not thumbnail type
	 */
	public List<ThumbnailType> getThumbnailsProperty() throws BadFieldValueException {
		List<AbstractField> tmp = getUnqualifiedArrayList(THUMBNAILS);
		if (tmp != null) {
			List<ThumbnailType> thumbs = new ArrayList<ThumbnailType>();
			for (AbstractField abstractField : tmp) {
				if (abstractField instanceof ThumbnailType) {
					thumbs.add((ThumbnailType) abstractField);
				} else {
					throw new BadFieldValueException("Thumbnail expected and "
							+ abstractField.getClass().getName() + " found.");
				}
			}
			return thumbs;
		}
		return null;

	}

}
