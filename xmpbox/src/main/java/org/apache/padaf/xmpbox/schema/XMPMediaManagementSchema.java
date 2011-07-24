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

	@PropertyType(propertyType = "Text")
	public static final String RESOURCEREF = "ResourceRef";

	/**
	 * Set ResourceRef value
	 * 
	 * @param url
	 *            resourceRef value to set
	 */
	public void setResourceRefValue(String url) {
		setResourceRef(new TextType(metadata, localPrefix, RESOURCEREF, url));
	}

	/**
	 * Set ResourceRef property
	 * 
	 * @param tt
	 *            ResourceRef property to set
	 */
	public void setResourceRef(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get ResourceRef Value
	 * 
	 * @return ResourceRef value
	 */
	public String getResourceRefValue() {
		TextType tt = getResourceRef();
		return tt != null ? tt.getStringValue() : null;
	}

	/**
	 * Get ResourceRef property
	 * 
	 * @return ResourceRef property
	 */
	public TextType getResourceRef() {
		return (TextType) getProperty(localPrefixSep + RESOURCEREF);
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
	public void setDocumentIDValue(String url) {
		setDocumentID(new TextType(metadata, localPrefix, DOCUMENTID, url));
	}

	/**
	 * Set DocumentId Property
	 * 
	 * @param tt
	 *            DocumentId Property to set
	 */
	public void setDocumentID(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get DocumentId property
	 * 
	 * @return DocumentId property
	 */
	public TextType getDocumentID() {
		return (TextType) getProperty(localPrefixSep + DOCUMENTID);
	}

	/**
	 * Get DocumentId value
	 * 
	 * @return DocumentId value
	 */
	public String getDocumentIDValue() {
		TextType tt = getDocumentID();
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
	public void setManagerValue(String url) {
		setManager(new TextType(metadata, localPrefix, MANAGER, url));
	}

	/**
	 * Set Manager property
	 * 
	 * @param tt
	 *            Manager property to set
	 */
	public void setManager(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get Manager property
	 * 
	 * @return Manager property
	 */
	public TextType getManager() {
		return (TextType) getProperty(localPrefixSep + MANAGER);
	}

	/**
	 * Get Manager value
	 * 
	 * @return Manager value
	 */
	public String getManagerValue() {
		TextType tt = getManager();
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
	public void setManageToValue(String url) {
		setManageTo(new TextType(metadata, localPrefix, MANAGETO, url));
	}

	/**
	 * Set ManageTo property
	 * 
	 * @param tt
	 *            ManageTo property to set
	 */
	public void setManageTo(TextType tt) {
		addProperty(tt);
	}

	/**
	 * get ManageTo property
	 * 
	 * @return ManageTo property
	 */
	public TextType getManageTo() {
		return (TextType) getProperty(localPrefixSep + MANAGETO);
	}

	/**
	 * get ManageTo value
	 * 
	 * @return ManageTo value
	 */
	public String getManageToValue() {
		TextType tt = getManageTo();
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
	public void setManageUIValue(String url) {
		setManageUI(new TextType(metadata, localPrefix, MANAGEUI, url));
	}

	/**
	 * Set ManageUI property
	 * 
	 * @param tt
	 *            ManageUI property to set
	 */
	public void setManageUI(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get ManageUI property
	 * 
	 * @return ManageUI property
	 */
	public TextType getManageUI() {
		return (TextType) getProperty(localPrefixSep + MANAGEUI);
	}

	/**
	 * Get ManageUI Value
	 * 
	 * @return ManageUI Value
	 */
	public String getManageUIValue() {
		TextType tt = getManageUI();
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
	public void setManagerVariantValue(String url) {
		setManagerVariant(new TextType(metadata, localPrefix, MANAGERVARIANT,
				url));
	}

	/**
	 * Set ManagerVariant Property
	 * 
	 * @param tt
	 *            ManagerVariant Property to set
	 */
	public void setManagerVariant(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get ManagerVariant property
	 * 
	 * @return ManagerVariant property
	 */
	public TextType getManagerVariant() {
		return (TextType) getProperty(localPrefixSep + MANAGERVARIANT);
	}

	/**
	 * Get ManagerVariant value
	 * 
	 * @return ManagerVariant value
	 */
	public String getManagerVariantValue() {
		TextType tt = getManagerVariant();
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
	public void setInstanceIDValue(String url) {
		setInstanceID(new TextType(metadata, localPrefix, INSTANCEID, url));
	}

	/**
	 * Set InstanceId property
	 * 
	 * @param tt
	 *            InstanceId property to set
	 */
	public void setInstanceID(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get InstanceId property
	 * 
	 * @return InstanceId property
	 */
	public TextType getInstanceID() {
		return (TextType) getProperty(localPrefixSep + INSTANCEID);
	}

	/**
	 * Get InstanceId value
	 * 
	 * @return InstanceId value
	 */
	public String getInstanceIDValue() {
		TextType tt = getInstanceID();
		return tt != null ? tt.getStringValue() : null;
	}

	// --------------------------------------- ManageFrom
	// ----------------------------

	@PropertyType(propertyType = "Text")
	public static final String MANAGEFROM = "ManageFrom";

	/**
	 * set ManageFrom Value
	 * 
	 * @param url
	 *            ManageFrom Value to set
	 */
	public void setManageFromValue(String url) {
		setManageFrom(new TextType(metadata, localPrefix, MANAGEFROM, url));
	}

	/**
	 * set ManageFrom Property
	 * 
	 * @param tt
	 *            ManageFrom Property to set
	 */
	public void setManageFrom(TextType tt) {
		addProperty(tt);
	}

	/**
	 * get ManageFrom Property
	 * 
	 * @return ManageFrom Property
	 */
	public TextType getManageFrom() {
		return (TextType) getProperty(localPrefixSep + MANAGEFROM);
	}

	/**
	 * Get ManageFrom value
	 * 
	 * @return ManageFrom value
	 */
	public String getManageFromValue() {
		TextType tt = getManageFrom();
		return tt != null ? tt.getStringValue() : null;
	}

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
	public void setOriginalDocumentIDValue(String url) {
		setOriginalDocumentID(new TextType(metadata, localPrefix,
				ORIGINALDOCUMENTID, url));
	}

	/**
	 * Set OriginalDocumentId property
	 * 
	 * @param tt
	 *            OriginalDocumentId property to set
	 */
	public void setOriginalDocumentID(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get OriginalDocumentId property
	 * 
	 * @return OriginalDocumentId property
	 */
	public TextType getOriginalDocumentID() {
		return (TextType) getProperty(localPrefixSep + ORIGINALDOCUMENTID);
	}

	/**
	 * Get OriginalDocumentId value
	 * 
	 * @return OriginalDocumentId value
	 */
	public String getOriginalDocumentIDValue() {
		TextType tt = getOriginalDocumentID();
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
	public void setRenditionClassValue(String url) {
		setRenditionClass(new TextType(metadata, localPrefix, RENDITIONCLASS,
				url));
	}

	/**
	 * Set RenditionClass Property
	 * 
	 * @param tt
	 *            renditionClass Property to set
	 */
	public void setRenditionClass(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get RenditionClass property
	 * 
	 * @return RenditionClass property
	 */
	public TextType getRenditionClass() {
		return (TextType) getProperty(localPrefixSep + RENDITIONCLASS);
	}

	/**
	 * Get RenditionClass value
	 * 
	 * @return RenditionClass value
	 */
	public String getRenditionClassValue() {
		TextType tt = getRenditionClass();
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
	public void setRenditionParamsValue(String url) {
		setRenditionParams(new TextType(metadata, localPrefix, RENDITIONPARAMS,
				url));
	}

	/**
	 * Set RenditionParams property
	 * 
	 * @param tt
	 *            RenditionParams property to set
	 */
	public void setRenditionParams(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get RenditionParams property
	 * 
	 * @return RenditionParams property
	 */
	public TextType getRenditionParams() {
		return (TextType) getProperty(localPrefixSep + RENDITIONPARAMS);
	}

	/**
	 * Get RenditionParams value
	 * 
	 * @return RenditionParams value
	 */
	public String getRenditionParamsValue() {
		TextType tt = getRenditionParams();
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
	public void setVersionIDValue(String url) {
		setVersionID(new TextType(metadata, localPrefix, VERSIONID, url));
	}

	/**
	 * Set VersionId property
	 * 
	 * @param tt
	 *            VersionId property to set
	 */
	public void setVersionID(TextType tt) {
		addProperty(tt);
	}

	/**
	 * Get VersionId property
	 * 
	 * @return VersionId property
	 */
	public TextType getVersionID() {
		return (TextType) getProperty(localPrefixSep + VERSIONID);
	}

	/**
	 * Get VersionId value
	 * 
	 * @return VersionId value
	 */
	public String getVersionIDValue() {
		TextType tt = getVersionID();
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
	public void addToVersionsValue(String version) {
		addSequenceValue(localPrefixSep + VERSIONS, version);
	}

	/**
	 * Get Versions property
	 * 
	 * @return version property to set
	 */
	public ComplexProperty getVersions() {
		return (ComplexProperty) getProperty(localPrefixSep + VERSIONS);
	}

	/**
	 * Get List of Versions values
	 * 
	 * @return List of Versions values
	 */
	public List<String> getVersionsValue() {
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
	public void addToHistoryValue(String history) {
		addSequenceValue(localPrefixSep + HISTORY, history);
	}

	/**
	 * Get History Property
	 * 
	 * @return History Property
	 */
	public ComplexProperty getHistory() {
		return (ComplexProperty) getProperty(localPrefixSep + HISTORY);
	}

	/**
	 * Get List of History values
	 * 
	 * @return List of History values
	 */
	public List<String> getHistoryValue() {
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
	public void addToIngredientsValue(String ingredients) {
		addBagValue(localPrefixSep + INGREDIENTS, ingredients);
	}

	/**
	 * . Get Ingredients Property
	 * 
	 * @return Ingredients property
	 */
	public ComplexProperty getIngredients() {
		return (ComplexProperty) getProperty(localPrefixSep + INGREDIENTS);
	}

	/**
	 * Get List of Ingredients values
	 * 
	 * @return List of Ingredients values
	 */
	public List<String> getIngredientsValue() {
		return getBagValueList(localPrefixSep + INGREDIENTS);
	}

}
