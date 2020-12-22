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

package org.apache.xmpbox.type;

import java.util.Calendar;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;

@StructuredType(preferedPrefix = "stRef", namespace = "http://ns.adobe.com/xap/1.0/sType/ResourceRef#")
public class ResourceRefType extends AbstractStructuredType
{

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String DOCUMENT_ID = "documentID";

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String FILE_PATH = "filePath";

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String INSTANCE_ID = "instanceID";

    @PropertyType(type = Types.Date, card = Cardinality.Simple)
    public static final String LAST_MODIFY_DATE = "lastModifyDate";

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String MANAGE_TO = "manageTo";

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String MANAGE_UI = "manageUI";

    @PropertyType(type = Types.AgentName, card = Cardinality.Simple)
    public static final String MANAGER = "manager";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String MANAGER_VARIANT = "managerVariant";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String PART_MAPPING = "partMapping";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String RENDITION_PARAMS = "renditionParams";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String VERSION_ID = "versionID";

    @PropertyType(type = Types.Choice, card = Cardinality.Simple)
    public static final String MASK_MARKERS = "maskMarkers";

    @PropertyType(type = Types.RenditionClass, card = Cardinality.Simple)
    public static final String RENDITION_CLASS = "renditionClass";

    @PropertyType(type = Types.Part, card = Cardinality.Simple)
    public static final String FROM_PART = "fromPart";

    @PropertyType(type = Types.Part, card = Cardinality.Simple)
    public static final String TO_PART = "toPart";

    public static final String ALTERNATE_PATHS = "alternatePaths";

    /**
     * 
     * @param metadata
     *            The metadata to attach to this property
     */
    public ResourceRefType(final XMPMetadata metadata)
    {
        super(metadata);
        addNamespace(getNamespace(), getPreferedPrefix());

    }

    public String getDocumentID()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(DOCUMENT_ID, URIType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setDocumentID(final String value)
    {
        addSimpleProperty(DOCUMENT_ID, value);
    }

    public String getFilePath()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(FILE_PATH, URIType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setFilePath(final String value)
    {
        addSimpleProperty(FILE_PATH, value);
    }

    public String getInstanceID()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(INSTANCE_ID, URIType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setInstanceID(final String value)
    {
        addSimpleProperty(INSTANCE_ID, value);
    }

    public Calendar getLastModifyDate()
    {
        final DateType absProp = (DateType) getFirstEquivalentProperty(LAST_MODIFY_DATE, DateType.class);
        if (absProp != null)
        {
            return absProp.getValue();
        }
        else
        {
            return null;
        }
    }

    public void setLastModifyDate(final Calendar value)
    {
        addSimpleProperty(LAST_MODIFY_DATE, value);
    }

    public String getManageUI()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(MANAGE_UI, URIType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setManageUI(final String value)
    {
        addSimpleProperty(MANAGE_UI, value);
    }

    public String getManageTo()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(MANAGE_TO, URIType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setManageTo(final String value)
    {
        addSimpleProperty(MANAGE_TO, value);
    }

    public String getManager()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(MANAGER, AgentNameType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setManager(final String value)
    {
        addSimpleProperty(MANAGER, value);
    }

    public String getManagerVariant()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(MANAGER_VARIANT, TextType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setManagerVariant(final String value)
    {
        addSimpleProperty(MANAGER_VARIANT, value);
    }

    public String getPartMapping()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(PART_MAPPING, TextType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setPartMapping(final String value)
    {
        addSimpleProperty(PART_MAPPING, value);
    }

    public String getRenditionParams()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(RENDITION_PARAMS, TextType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setRenditionParams(final String value)
    {
        addSimpleProperty(RENDITION_PARAMS, value);
    }

    public String getVersionID()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(VERSION_ID, TextType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setVersionID(final String value)
    {
        addSimpleProperty(VERSION_ID, value);
    }

    public String getMaskMarkers()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(MASK_MARKERS, ChoiceType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setMaskMarkers(final String value)
    {
        addSimpleProperty(MASK_MARKERS, value);
    }

    public String getRenditionClass()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(RENDITION_CLASS, RenditionClassType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setRenditionClass(final String value)
    {
        addSimpleProperty(RENDITION_CLASS, value);
    }

    public String getFromPart()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(FROM_PART, PartType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setFromPart(final String value)
    {
        addSimpleProperty(FROM_PART, value);
    }

    public String getToPart()
    {
        final TextType absProp = (TextType) getFirstEquivalentProperty(TO_PART, PartType.class);
        if (absProp != null)
        {
            return absProp.getStringValue();
        }
        else
        {
            return null;
        }
    }

    public void setToPart(final String value)
    {
        addSimpleProperty(TO_PART, value);
    }

    public void addAlternatePath(final String value)
    {
        ArrayProperty seq = (ArrayProperty) getFirstEquivalentProperty(ALTERNATE_PATHS, ArrayProperty.class);
        if (seq == null)
        {
            seq = getMetadata().getTypeMapping().createArrayProperty(null, getPreferedPrefix(), ALTERNATE_PATHS,
                    Cardinality.Seq);
            addProperty(seq);
        }
        final TypeMapping tm = getMetadata().getTypeMapping();
        final TextType tt = (TextType) tm.instanciateSimpleProperty(null, "rdf", "li", value, Types.Text);
        seq.addProperty(tt);
    }

    /**
     * Get Versions property
     * 
     * @return version property to set
     */
    public ArrayProperty getAlternatePathsProperty()
    {
        return (ArrayProperty) getFirstEquivalentProperty(ALTERNATE_PATHS, ArrayProperty.class);
    }

    /**
     * Get List of Versions values
     * 
     * @return List of Versions values
     */
    public List<String> getAlternatePaths()
    {
        final ArrayProperty seq = (ArrayProperty) getFirstEquivalentProperty(ALTERNATE_PATHS, ArrayProperty.class);
        if (seq != null)
        {
            return seq.getElementsAsString();
        }
        else
        {
            return null;
        }
    }

}
