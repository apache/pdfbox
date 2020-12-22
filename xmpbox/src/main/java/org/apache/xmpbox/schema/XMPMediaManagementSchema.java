/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.xmpbox.schema;

import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.AgentNameType;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.IntegerType;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.RenditionClassType;
import org.apache.xmpbox.type.ResourceRefType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.Types;
import org.apache.xmpbox.type.URIType;
import org.apache.xmpbox.type.URLType;

/**
 * Representation of XMPMediaManagement Schema
 * 
 * @author gbailleul
 * 
 */
@StructuredType(preferedPrefix = "xmpMM", namespace = "http://ns.adobe.com/xap/1.0/mm/")
public class XMPMediaManagementSchema extends XMPSchema
{

    @PropertyType(type = Types.URL, card = Cardinality.Simple)
    public static final String LAST_URL = "LastURL";


    @PropertyType(type = Types.ResourceRef, card = Cardinality.Simple)
    public static final String RENDITION_OF = "RenditionOf";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String SAVE_ID = "SaveID";

    /**
     * Constructor of XMPMediaManagement Schema with preferred prefix
     * 
     * @param metadata
     *            The metadata to attach this schema
     */
    public XMPMediaManagementSchema(final XMPMetadata metadata)
    {
        super(metadata);

    }

    /**
     * Constructor of XMPMediaManagementSchema schema with specified prefix
     * 
     * @param metadata
     *            The metadata to attach this schema
     * @param ownPrefix
     *            The prefix to assign
     */
    public XMPMediaManagementSchema(final XMPMetadata metadata, final String ownPrefix)
    {
        super(metadata, ownPrefix);

    }

    // -------------------------------- ResourceRef --------------------

    @PropertyType(type = Types.ResourceRef, card = Cardinality.Simple)
    public static final String DERIVED_FROM = "DerivedFrom";

    /**
     * Set ResourceRef property
     * 
     * @param tt
     *            ResourceRef property to set
     */
    public void setDerivedFromProperty(final ResourceRefType tt)
    {
        addProperty(tt);
    }

    /**
     * Get ResourceRef property
     * 
     * @return ResourceRef property
     */
    public ResourceRefType getResourceRefProperty()
    {
        return (ResourceRefType) getProperty(DERIVED_FROM);
    }

    // --------------------------------------- DocumentID
    // ----------------------------

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String DOCUMENTID = "DocumentID";

    /**
     * Set DocumentId value
     * 
     * @param url
     *            DocumentId value to set
     */
    public void setDocumentID(final String url)
    {
        final URIType tt = (URIType) instanciateSimple(DOCUMENTID, url);
        setDocumentIDProperty(tt);
    }

    /**
     * Set DocumentId Property
     * 
     * @param tt
     *            DocumentId Property to set
     */
    public void setDocumentIDProperty(final URIType tt)
    {
        addProperty(tt);
    }



    /**
     * Get DocumentId property
     * 
     * @return DocumentId property
     */
    public TextType getDocumentIDProperty()
    {
        return (TextType) getProperty(DOCUMENTID);
    }

    /**
     * Get DocumentId value
     * 
     * @return DocumentId value
     */
    public String getDocumentID()
    {
        final TextType tt = getDocumentIDProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    /**
     * Set DocumentId value
     *
     * @param url
     *            DocumentId value to set
     */
    public void setLastURL(final String url)
    {
        final URLType tt = (URLType) instanciateSimple(LAST_URL, url);
        setLastURLProperty(tt);
    }

    /**
     * Set DocumentId Property
     *
     * @param tt
     *            DocumentId Property to set
     */
    public void setLastURLProperty(final URLType tt)
    {
        addProperty(tt);
    }



    /**
     * Get DocumentId property
     *
     * @return DocumentId property
     */
    public URLType getLastURLProperty()
    {
        return (URLType) getProperty(LAST_URL);
    }

    /**
     * Get DocumentId value
     *
     * @return DocumentId value
     */
    public String getLastURL()
    {
        final URLType tt = getLastURLProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    /**
     * Set DocumentId value
     *
     * @param url
     *            DocumentId value to set
     */
    public void setSaveId(final Integer url)
    {
        final IntegerType tt = (IntegerType) instanciateSimple(SAVE_ID, url);
        setSaveIDProperty(tt);
    }

    /**
     * Set DocumentId Property
     *
     * @param tt
     *            DocumentId Property to set
     */
    public void setSaveIDProperty(final IntegerType tt)
    {
        addProperty(tt);
    }



    /**
     * Get DocumentId property
     *
     * @return DocumentId property
     */
    public IntegerType getSaveIDProperty()
    {
        return (IntegerType) getProperty(SAVE_ID);
    }

    /**
     * Get DocumentId value
     *
     * @return DocumentId value
     */
    public Integer getSaveID()
    {
        final IntegerType tt = getSaveIDProperty();
        return tt != null ? tt.getValue() : null;
    }

    // --------------------------------------- Manager
    // ----------------------------

    @PropertyType(type = Types.AgentName, card = Cardinality.Simple)
    public static final String MANAGER = "Manager";

    /**
     * Set Manager value
     * 
     * @param value
     *            Manager value to set
     */
    public void setManager(final String value)
    {
        final AgentNameType tt = (AgentNameType) instanciateSimple(MANAGER, value);
        setManagerProperty(tt);
    }

    /**
     * Set Manager property
     * 
     * @param tt
     *            Manager property to set
     */
    public void setManagerProperty(final AgentNameType tt)
    {
        addProperty(tt);
    }

    /**
     * Get Manager property
     * 
     * @return Manager property
     */
    public TextType getManagerProperty()
    {
        return (TextType) getProperty(MANAGER);
    }

    /**
     * Get Manager value
     * 
     * @return Manager value
     */
    public String getManager()
    {
        final TextType tt = getManagerProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- ManageTo
    // ----------------------------

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String MANAGETO = "ManageTo";

    /**
     * Set ManageTo Value
     * 
     * @param value
     *            ManageTo Value to set
     */
    public void setManageTo(final String value)
    {
        final URIType tt = (URIType) instanciateSimple(MANAGETO, value);
        setManageToProperty(tt);
    }

    /**
     * Set ManageTo property
     * 
     * @param tt
     *            ManageTo property to set
     */
    public void setManageToProperty(final URIType tt)
    {
        addProperty(tt);
    }

    /**
     * get ManageTo property
     * 
     * @return ManageTo property
     */
    public TextType getManageToProperty()
    {
        return (TextType) getProperty(MANAGETO);
    }

    /**
     * get ManageTo value
     * 
     * @return ManageTo value
     */
    public String getManageTo()
    {
        final TextType tt = getManageToProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- ManageUI
    // ----------------------------

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String MANAGEUI = "ManageUI";

    /**
     * Set ManageUI value
     * 
     * @param value
     *            ManageUI value to set
     */
    public void setManageUI(final String value)
    {
        final URIType tt = (URIType) instanciateSimple(MANAGEUI, value);
        setManageUIProperty(tt);
    }

    /**
     * Set ManageUI property
     * 
     * @param tt
     *            ManageUI property to set
     */
    public void setManageUIProperty(final URIType tt)
    {
        addProperty(tt);
    }

    /**
     * Get ManageUI property
     * 
     * @return ManageUI property
     */
    public TextType getManageUIProperty()
    {
        return (TextType) getProperty(MANAGEUI);
    }

    /**
     * Get ManageUI Value
     * 
     * @return ManageUI Value
     */
    public String getManageUI()
    {
        final TextType tt = getManageUIProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- ManagerVariant
    // ----------------------------

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String MANAGERVARIANT = "ManagerVariant";

    /**
     * Set ManagerVariant value
     * 
     * @param value
     *            ManagerVariant value to set
     */
    public void setManagerVariant(final String value)
    {
        final TextType tt = (TextType) instanciateSimple(MANAGERVARIANT, value);
        setManagerVariantProperty(tt);
    }

    /**
     * Set ManagerVariant Property
     * 
     * @param tt
     *            ManagerVariant Property to set
     */
    public void setManagerVariantProperty(final TextType tt)
    {
        addProperty(tt);
    }

    /**
     * Get ManagerVariant property
     * 
     * @return ManagerVariant property
     */
    public TextType getManagerVariantProperty()
    {
        return (TextType) getProperty(MANAGERVARIANT);
    }

    /**
     * Get ManagerVariant value
     * 
     * @return ManagerVariant value
     */
    public String getManagerVariant()
    {
        final TextType tt = getManagerVariantProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- InstanceID
    // ----------------------------

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String INSTANCEID = "InstanceID";

    /**
     * Set InstanceId value
     * 
     * @param value
     *            InstanceId value to set
     */
    public void setInstanceID(final String value)
    {
        final URIType tt = (URIType) instanciateSimple(INSTANCEID, value);
        setInstanceIDProperty(tt);
    }

    /**
     * Set InstanceId property
     * 
     * @param tt
     *            InstanceId property to set
     */
    public void setInstanceIDProperty(final URIType tt)
    {
        addProperty(tt);
    }

    /**
     * Get InstanceId property
     * 
     * @return InstanceId property
     */
    public TextType getInstanceIDProperty()
    {
        return (TextType) getProperty(INSTANCEID);
    }

    /**
     * Get InstanceId value
     * 
     * @return InstanceId value
     */
    public String getInstanceID()
    {
        final TextType tt = getInstanceIDProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- ManageFrom
    // ----------------------------

    @PropertyType(type = Types.ResourceRef, card = Cardinality.Simple)
    public static final String MANAGED_FROM = "ManagedFrom";

    /**
     * set ManageFrom Property
     * 
     * @param resourceRef
     *            ManageFrom Property to set
     */
    public void setManagedFromProperty(final ResourceRefType resourceRef)
    {
        addProperty(resourceRef);
    }

    /**
     * get ManageFrom Property
     * 
     * @return ManageFrom Property
     */
    public ResourceRefType getManagedFromProperty()
    {
        return (ResourceRefType) getProperty(MANAGED_FROM);
    }

    // --------------------------------------- OriginalDocumentID
    // ----------------------------

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String ORIGINALDOCUMENTID = "OriginalDocumentID";

    /**
     * Set OriginalDocumentId value
     * 
     * @param url
     *            OriginalDocumentId value to set
     */
    public void setOriginalDocumentID(final String url)
    {
        final TextType tt = (TextType) instanciateSimple(ORIGINALDOCUMENTID, url);
        setOriginalDocumentIDProperty(tt);
    }

    /**
     * Set OriginalDocumentId property
     * 
     * @param tt
     *            OriginalDocumentId property to set
     */
    public void setOriginalDocumentIDProperty(final TextType tt)
    {
        addProperty(tt);
    }

    /**
     * Get OriginalDocumentId property
     * 
     * @return OriginalDocumentId property
     */
    public TextType getOriginalDocumentIDProperty()
    {
        return (TextType) getProperty(ORIGINALDOCUMENTID);
    }

    /**
     * Get OriginalDocumentId value
     * 
     * @return OriginalDocumentId value
     */
    public String getOriginalDocumentID()
    {
        final TextType tt = getOriginalDocumentIDProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- RenditionClass
    // ----------------------------

    @PropertyType(type = Types.RenditionClass, card = Cardinality.Simple)
    public static final String RENDITIONCLASS = "RenditionClass";

    /**
     * Set renditionClass Value
     * 
     * @param value
     *            renditionClass Value to set
     */
    public void setRenditionClass(final String value)
    {
        final RenditionClassType tt = (RenditionClassType) instanciateSimple(RENDITIONCLASS, value);
        setRenditionClassProperty(tt);
    }

    /**
     * Set RenditionClass Property
     * 
     * @param tt
     *            renditionClass Property to set
     */
    public void setRenditionClassProperty(final RenditionClassType tt)
    {
        addProperty(tt);
    }

    /**
     * Get RenditionClass property
     * 
     * @return RenditionClass property
     */
    public TextType getRenditionClassProperty()
    {
        return (TextType) getProperty(RENDITIONCLASS);
    }

    /**
     * Get RenditionClass value
     * 
     * @return RenditionClass value
     */
    public String getRenditionClass()
    {
        final TextType tt = getRenditionClassProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- RenditionParams
    // ----------------------------

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String RENDITIONPARAMS = "RenditionParams";

    /**
     * Set RenditionParams Value
     * 
     * @param url
     *            RenditionParams Value to set
     */
    public void setRenditionParams(final String url)
    {
        final TextType tt = (TextType) instanciateSimple(RENDITIONPARAMS, url);
        setRenditionParamsProperty(tt);
    }

    /**
     * Set RenditionParams property
     * 
     * @param tt
     *            RenditionParams property to set
     */
    public void setRenditionParamsProperty(final TextType tt)
    {
        addProperty(tt);
    }

    /**
     * Get RenditionParams property
     * 
     * @return RenditionParams property
     */
    public TextType getRenditionParamsProperty()
    {
        return (TextType) getProperty(RENDITIONPARAMS);
    }

    /**
     * Get RenditionParams value
     * 
     * @return RenditionParams value
     */
    public String getRenditionParams()
    {
        final TextType tt = getRenditionParamsProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- VersionID
    // ----------------------------

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String VERSIONID = "VersionID";

    /**
     * Set VersionId value
     * 
     * @param value
     *            VersionId value to set
     */
    public void setVersionID(final String value)
    {
        final TextType tt = (TextType) instanciateSimple(VERSIONID, value);
        setVersionIDProperty(tt);
    }

    /**
     * Set VersionId property
     * 
     * @param tt
     *            VersionId property to set
     */
    public void setVersionIDProperty(final TextType tt)
    {
        addProperty(tt);
    }

    /**
     * Get VersionId property
     * 
     * @return VersionId property
     */
    public TextType getVersionIDProperty()
    {
        return (TextType) getProperty(VERSIONID);
    }

    /**
     * Get VersionId value
     * 
     * @return VersionId value
     */
    public String getVersionID()
    {
        final TextType tt = getVersionIDProperty();
        return tt != null ? tt.getStringValue() : null;
    }

    // --------------------------------------- Versions
    // ----------------------------

    @PropertyType(type = Types.Version, card = Cardinality.Seq)
    public static final String VERSIONS = "Versions";

    public void addVersions(final String value)
    {
        addQualifiedBagValue(VERSIONS, value);
    }

    /**
     * Get Versions property
     * 
     * @return version property to set
     */
    public ArrayProperty getVersionsProperty()
    {
        return (ArrayProperty) getProperty(VERSIONS);
    }

    public List<String> getVersions()
    {
        return getUnqualifiedBagValueList(VERSIONS);
    }

    // --------------------------------------- History
    // ----------------------------

    @PropertyType(type = Types.ResourceEvent, card = Cardinality.Seq)
    public static final String HISTORY = "History";

    /**
     * Add a History Value
     * 
     * @param history
     *            History Value to add
     */
    public void addHistory(final String history)
    {
        addUnqualifiedSequenceValue(HISTORY, history);
    }

    /**
     * Get History Property
     * 
     * @return History Property
     */
    public ArrayProperty getHistoryProperty()
    {
        return (ArrayProperty) getProperty(HISTORY);
    }

    /**
     * Get List of History values
     * 
     * @return List of History values
     */
    public List<String> getHistory()
    {
        return getUnqualifiedSequenceValueList(HISTORY);
    }

    // --------------------------------------- Ingredients
    // ----------------------------

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String INGREDIENTS = "Ingredients";

    /**
     * Add an Ingredients value
     * 
     * @param ingredients
     *            Ingredients value to add
     */
    public void addIngredients(final String ingredients)
    {
        addQualifiedBagValue(INGREDIENTS, ingredients);
    }

    /**
     * . Get Ingredients Property
     * 
     * @return Ingredients property
     */
    public ArrayProperty getIngredientsProperty()
    {
        return (ArrayProperty) getProperty(INGREDIENTS);
    }

    /**
     * Get List of Ingredients values
     * 
     * @return List of Ingredients values
     */
    public List<String> getIngredients()
    {
        return getUnqualifiedBagValueList(INGREDIENTS);
    }

}
