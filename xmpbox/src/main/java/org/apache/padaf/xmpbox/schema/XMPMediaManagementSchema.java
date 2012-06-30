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

import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.ComplexProperty;
import org.apache.padaf.xmpbox.type.ResourceRefType;
import org.apache.padaf.xmpbox.type.TextType;


/**
 * Representation of XMPMediaManagement Schema
 * 
 * @author gbailleul
 * 
 */
public class XMPMediaManagementSchema extends XMPSchema {

	public static final String PREFERRED_XMPMM_PREFIX = "xmpMM";

	public static final String XMPMMURI = "http://ns.adobe.com/xap/1.0/mm/";

	/**
	 * Constructor of XMPMediaManagement Schema with preferred prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 */
	public XMPMediaManagementSchema(XMPMetadata metadata) {
		super(metadata, PREFERRED_XMPMM_PREFIX, XMPMMURI);

	}

	/**
	 * Constructor of XMPMediaManagementSchema schema with specified prefix
	 * 
	 * @param metadata
	 *            The metadata to attach this schema
	 * @param ownPrefix
	 *            The prefix to assign
	 */
	public XMPMediaManagementSchema(XMPMetadata metadata, String ownPrefix) {
		super(metadata, ownPrefix, XMPMMURI);

	}

	// -------------------------------- ResourceRef --------------------

	@PropertyType(propertyType = "ResourceRef")
	public static final String DERIVED_FROM = "DerivedFrom";

	/**
	 * Set ResourceRef property
	 * 
	 * @param tt
	 *            ResourceRef property to set
	 */
	public void setDerivedFromProperty(ResourceRefType tt) {
		addProperty(tt);
	}

	/**
	 * Get ResourceRef property
	 * 
	 * @return ResourceRef property
	 */
	public ResourceRefType getResourceRefProperty() {
		return (ResourceRefType)getProperty(localPrefixSep + DERIVED_FROM);
	}

		
	
	// --------------------------------------- DocumentID
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String DOCUMENTID = "DocumentID";

	/**
	 * Set DocumentId value
	 * 
	 * @param url
	 *            DocumentId value to set
	 */
	public void setDocumentID(String url) {
		setDocumentIDProperty(new TextType(metadata, localPrefix, DOCUMENTID, url));
	}

	/**
	 * Set DocumentId Property
	 * 
	 * @param tt
	 *            DocumentId Property to set
	 */
	public void setDocumentIDProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get DocumentId property
	 * 
	 * @return DocumentId property
	 */
	public TextType getDocumentIDProperty() {
		return (TextType) getProperty(localPrefixSep + DOCUMENTID);
	}

	/**
	 * Get DocumentId value
	 * 
	 * @return DocumentId value
	 */
	public String getDocumentID() {
		TextType tt = getDocumentIDProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- Manager
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String MANAGER = "Manager";

	/**
	 * Set Manager value
	 * 
	 * @param url
	 *            Manager value to set
	 */
	public void setManager(String url) {
		setManagerProperty(new TextType(metadata, localPrefix, MANAGER, url));
	}

	/**
	 * Set Manager property
	 * 
	 * @param tt
	 *            Manager property to set
	 */
	public void setManagerProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get Manager property
	 * 
	 * @return Manager property
	 */
	public TextType getManagerProperty() {
		return (TextType) getProperty(localPrefixSep + MANAGER);
	}

	/**
	 * Get Manager value
	 * 
	 * @return Manager value
	 */
	public String getManager() {
		TextType tt = getManagerProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- ManageTo
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String MANAGETO = "ManageTo";

	/**
	 * Set ManageTo Value
	 * 
	 * @param url
	 *            ManageTo Value to set
	 */
	public void setManageTo(String url) {
		setManageToProperty(new TextType(metadata, localPrefix, MANAGETO, url));
	}

	/**
	 * Set ManageTo property
	 * 
	 * @param tt
	 *            ManageTo property to set
	 */
	public void setManageToProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * get ManageTo property
	 * 
	 * @return ManageTo property
	 */
	public TextType getManageToProperty() {
		return (TextType) getProperty(localPrefixSep + MANAGETO);
	}

	/**
	 * get ManageTo value
	 * 
	 * @return ManageTo value
	 */
	public String getManageTo() {
		TextType tt = getManageToProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- ManageUI
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String MANAGEUI = "ManageUI";

	/**
	 * Set ManageUI value
	 * 
	 * @param url
	 *            ManageUI value to set
	 */
	public void setManageUI(String url) {
		setManageUIProperty(new TextType(metadata, localPrefix, MANAGEUI, url));
	}

	/**
	 * Set ManageUI property
	 * 
	 * @param tt
	 *            ManageUI property to set
	 */
	public void setManageUIProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get ManageUI property
	 * 
	 * @return ManageUI property
	 */
	public TextType getManageUIProperty() {
		return (TextType) getProperty(localPrefixSep + MANAGEUI);
	}

	/**
	 * Get ManageUI Value
	 * 
	 * @return ManageUI Value
	 */
	public String getManageUI() {
		TextType tt = getManageUIProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- ManagerVariant
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String MANAGERVARIANT = "ManagerVariant";

	/**
	 * Set ManagerVariant value
	 * 
	 * @param url
	 *            ManagerVariant value to set
	 */
	public void setManagerVariant(String url) {
		setManagerVariantProperty(new TextType(metadata, localPrefix, MANAGERVARIANT,
				url));
	}

	/**
	 * Set ManagerVariant Property
	 * 
	 * @param tt
	 *            ManagerVariant Property to set
	 */
	public void setManagerVariantProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get ManagerVariant property
	 * 
	 * @return ManagerVariant property
	 */
	public TextType getManagerVariantProperty() {
		return (TextType) getProperty(localPrefixSep + MANAGERVARIANT);
	}

	/**
	 * Get ManagerVariant value
	 * 
	 * @return ManagerVariant value
	 */
	public String getManagerVariant() {
		TextType tt = getManagerVariantProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- InstanceID
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String INSTANCEID = "InstanceID";

	/**
	 * Set InstanceId value
	 * 
	 * @param url
	 *            InstanceId value to set
	 */
	public void setInstanceID(String url) {
		setInstanceIDProperty(new TextType(metadata, localPrefix, INSTANCEID, url));
	}

	/**
	 * Set InstanceId property
	 * 
	 * @param tt
	 *            InstanceId property to set
	 */
	public void setInstanceIDProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get InstanceId property
	 * 
	 * @return InstanceId property
	 */
	public TextType getInstanceIDProperty() {
		return (TextType) getProperty(localPrefixSep + INSTANCEID);
	}

	/**
	 * Get InstanceId value
	 * 
	 * @return InstanceId value
	 */
	public String getInstanceID() {
		TextType tt = getInstanceIDProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- ManageFrom
	// ----------------------------

	@PropertyType(propertyType = "ResourceRef")
	public static final String MANAGED_FROM = "ManagedFrom";
	
//	/**
//	 * set ManageFrom Value
//	 * 
//	 * @param url
//	 *            ManageFrom Value to set
//	 */
//	public void setManagedFrom(ResourceRefType resourceRef) {
//		
//		setManagedFromProperty(new TextType(metadata, localPrefix, MANAGED_FROM, url));
//	}

	/**
	 * set ManageFrom Property
	 * 
	 * @param tt
	 *            ManageFrom Property to set
	 */
	public void setManagedFromProperty(ResourceRefType resourceRef) {
		addProperty(resourceRef);
	}

	/**
	 * get ManageFrom Property
	 * 
	 * @return ManageFrom Property
	 */
	public ResourceRefType getManagedFromProperty() {
		return (ResourceRefType) getProperty(localPrefixSep + MANAGED_FROM);
	}

//	/**
//	 * Get ManageFrom value
//	 * 
//	 * @return ManageFrom value
//	 */
//	public String getManagedFrom() {
//		TextType tt = getManagedFromProperty();
//		return tt != null ? tt.getStringValue() : null;
//	}

	// --------------------------------------- OriginalDocumentID
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String ORIGINALDOCUMENTID = "OriginalDocumentID";

	/**
	 * Set OriginalDocumentId value
	 * 
	 * @param url
	 *            OriginalDocumentId value to set
	 */
	public void setOriginalDocumentID(String url) {
		setOriginalDocumentIDProperty(new TextType(metadata, localPrefix,
				ORIGINALDOCUMENTID, url));
	}

	/**
	 * Set OriginalDocumentId property
	 * 
	 * @param tt
	 *            OriginalDocumentId property to set
	 */
	public void setOriginalDocumentIDProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get OriginalDocumentId property
	 * 
	 * @return OriginalDocumentId property
	 */
	public TextType getOriginalDocumentIDProperty() {
		return (TextType) getProperty(localPrefixSep + ORIGINALDOCUMENTID);
	}

	/**
	 * Get OriginalDocumentId value
	 * 
	 * @return OriginalDocumentId value
	 */
	public String getOriginalDocumentID() {
		TextType tt = getOriginalDocumentIDProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- RenditionClass
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String RENDITIONCLASS = "RenditionClass";

	/**
	 * Set renditionClass Value
	 * 
	 * @param url
	 *            renditionClass Value to set
	 */
	public void setRenditionClass(String url) {
		setRenditionClassProperty(new TextType(metadata, localPrefix, RENDITIONCLASS,
				url));
	}

	/**
	 * Set RenditionClass Property
	 * 
	 * @param tt
	 *            renditionClass Property to set
	 */
	public void setRenditionClassProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get RenditionClass property
	 * 
	 * @return RenditionClass property
	 */
	public TextType getRenditionClassProperty() {
		return (TextType) getProperty(localPrefixSep + RENDITIONCLASS);
	}

	/**
	 * Get RenditionClass value
	 * 
	 * @return RenditionClass value
	 */
	public String getRenditionClass() {
		TextType tt = getRenditionClassProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- RenditionParams
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String RENDITIONPARAMS = "RenditionParams";

	/**
	 * Set RenditionParams Value
	 * 
	 * @param url
	 *            RenditionParams Value to set
	 */
	public void setRenditionParams(String url) {
		setRenditionParamsProperty(new TextType(metadata, localPrefix, RENDITIONPARAMS,
				url));
	}

	/**
	 * Set RenditionParams property
	 * 
	 * @param tt
	 *            RenditionParams property to set
	 */
	public void setRenditionParamsProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get RenditionParams property
	 * 
	 * @return RenditionParams property
	 */
	public TextType getRenditionParamsProperty() {
		return (TextType) getProperty(localPrefixSep + RENDITIONPARAMS);
	}

	/**
	 * Get RenditionParams value
	 * 
	 * @return RenditionParams value
	 */
	public String getRenditionParams() {
		TextType tt = getRenditionParamsProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- VersionID
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String VERSIONID = "VersionID";

	/**
	 * Set VersionId value
	 * 
	 * @param url
	 *            VersionId value to set
	 */
	public void setVersionID(String url) {
		setVersionIDProperty(new TextType(metadata, localPrefix, VERSIONID, url));
	}

	/**
	 * Set VersionId property
	 * 
	 * @param tt
	 *            VersionId property to set
	 */
	public void setVersionIDProperty(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get VersionId property
	 * 
	 * @return VersionId property
	 */
	public TextType getVersionIDProperty() {
		return (TextType) getProperty(localPrefixSep + VERSIONID);
	}

	/**
	 * Get VersionId value
	 * 
	 * @return VersionId value
	 */
	public String getVersionID() {
		TextType tt = getVersionIDProperty();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- Versions
	// ----------------------------

	@PropertyType(propertyType = "seq Text")
	public static final String VERSIONS = "Versions";

	/**
	 * Add a version value
	 * 
	 * @param version
	 *            version value to set
	 */
	public void addVersions(String version) {
		addSequenceValue(localPrefixSep + VERSIONS, version);
	}

	/**
	 * Get Versions property
	 * 
	 * @return version property to set
	 */
	public ComplexProperty getVersionsProperty() {
		return (ComplexProperty) getProperty(localPrefixSep + VERSIONS);
	}

	/**
	 * Get List of Versions values
	 * 
	 * @return List of Versions values
	 */
	public List<String> getVersions() {
		return getSequenceValueList(localPrefixSep + VERSIONS);
	}

	// --------------------------------------- History
	// ----------------------------

//    @PropertyType(propertyType = "seq Text")
	@PropertyType(propertyType = "Unmanaged")
	public static final String HISTORY = "History";

	/**
	 * Add a History Value
	 * 
	 * @param history
	 *            History Value to add
	 */
	public void addHistory(String history) {
		addSequenceValue(localPrefixSep + HISTORY, history);
	}

	/**
	 * Get History Property
	 * 
	 * @return History Property
	 */
	public ComplexProperty getHistoryProperty() {
		return (ComplexProperty) getProperty(localPrefixSep + HISTORY);
	}

	/**
	 * Get List of History values
	 * 
	 * @return List of History values
	 */
	public List<String> getHistory() {
		return getSequenceValueList(localPrefixSep + HISTORY);
	}

	// --------------------------------------- Ingredients
	// ----------------------------

	@PropertyType(propertyType = "bag Text")
	public static final String INGREDIENTS = "Ingredients";

	/**
	 * Add an Ingredients value
	 * 
	 * @param ingredients
	 *            Ingredients value to add
	 */
	public void addIngredients(String ingredients) {
		addBagValue(localPrefixSep + INGREDIENTS, ingredients);
	}

	/**
	 * . Get Ingredients Property
	 * 
	 * @return Ingredients property
	 */
	public ComplexProperty getIngredientsProperty() {
		return (ComplexProperty) getProperty(localPrefixSep + INGREDIENTS);
	}

	/**
	 * Get List of Ingredients values
	 * 
	 * @return List of Ingredients values
	 */
	public List<String> getIngredients() {
		return getBagValueList(localPrefixSep + INGREDIENTS);
	}

}
