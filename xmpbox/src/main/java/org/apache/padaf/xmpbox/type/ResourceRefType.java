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

package org.apache.padaf.xmpbox.type;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PropertyType;
import org.apache.padaf.xmpbox.schema.XMPSchema;

public class ResourceRefType extends ComplexPropertyContainer {

	public static final String ELEMENT_NS = "http://ns.adobe.com/xap/1.0/sType/ResourceRef#";

	public static final String PREFERRED_PREFIX = "stRef";
	
	protected XMPMetadata metadata;
	
	public static final String DOCUMENT_ID = "documentID";

	public static final String FILE_PATH = "filePath";

	public static final String INSTANCE_ID = "instanceID";
	
	public static final String LAS_MODIFY_DATE = "lastModifyDate";
	
	public static final String MANAGE_TO = "manageTo";

	public static final String MANAGE_UI = "manageUI";
	
	public static final String MANAGER = "manager";

	public static final String MANAGER_VARIANT = "managerVariant";

	public static final String PART_MAPPING = "partMapping";
	
	public static final String RENDITION_PARAMS = "renditionParams";
	
	public static final String VERSION_ID = "versionID";

	public static final String MASK_MARKERS = "maskMarkers";
	
	public static final String RENDITION_CLASS = "renditionClass";
	
	public static final String FROM_PART = "fromPart";
	
	public static final String TO_PART = "toPart";
	
	public static final String ALTERNATE_PATHS = "alternatePaths";

	/**
	 * 
	 * @param metadata
	 *            The metadata to attach to this property
	 * @param namespace
	 *            the namespace URI to associate to this property
	 * @param prefix
	 *            The prefix to set for this property
	 * @param propertyName
	 *            The local Name of this thumbnail type
	 */
	public ResourceRefType(XMPMetadata metadata, String namespace, String prefix,
			String propertyName) {
		super(metadata, namespace, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(XMPSchema.NS_NAMESPACE, "xmlns", PREFERRED_PREFIX, ELEMENT_NS));
	}
	
	public String getDocumentId () {
		TextType absProp = (TextType)getFirstEquivalentProperty(DOCUMENT_ID,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setDocumentId (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, DOCUMENT_ID, value));
	}
	
	public String getFilePath () {
		TextType absProp = (TextType)getFirstEquivalentProperty(FILE_PATH,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setFilePath (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, FILE_PATH, value));
	}

	public String getInstanceID () {
		TextType absProp = (TextType)getFirstEquivalentProperty(INSTANCE_ID,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}

	public void setInstanceID (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, INSTANCE_ID, value));
	}

	public Calendar getLastModifyDate () {
		DateType absProp = (DateType)getFirstEquivalentProperty(INSTANCE_ID,DateType.class);
		if (absProp != null) {
			return absProp.getValue();
		} else {
			return null;
		}
	}

	public void setLastModifyDate (Calendar value) {
		this.addProperty(new DateType(metadata, PREFERRED_PREFIX, INSTANCE_ID, value));
	}

	public String getManageUI () {
		TextType absProp = (TextType)getFirstEquivalentProperty(MANAGE_UI,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setManageUI (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, MANAGE_UI, value));
	}
	
	public String getManageTo () {
		TextType absProp = (TextType)getFirstEquivalentProperty(MANAGE_TO,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setManageTo (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, MANAGE_TO, value));
	}
	
	public String getManager () {
		TextType absProp = (TextType)getFirstEquivalentProperty(MANAGER,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setManager (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, MANAGER, value));
	}
	
	public String getManagerVariant () {
		TextType absProp = (TextType)getFirstEquivalentProperty(MANAGER_VARIANT,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setManagerVariant (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, MANAGER_VARIANT, value));
	}
	
	public String getPartMapping () {
		TextType absProp = (TextType)getFirstEquivalentProperty(PART_MAPPING,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setPartMapping (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, PART_MAPPING, value));
	}
	
	public String getRenditionParams () {
		TextType absProp = (TextType)getFirstEquivalentProperty(RENDITION_PARAMS,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setRenditionParams (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, RENDITION_PARAMS, value));
	}
	
	public String getVersionID () {
		TextType absProp = (TextType)getFirstEquivalentProperty(VERSION_ID,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setVersionID (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, VERSION_ID, value));
	}
	
	public String getMaskMarkers () {
		TextType absProp = (TextType)getFirstEquivalentProperty(MASK_MARKERS,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setMaskMarkers (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, MASK_MARKERS, value));
	}
	
	public String getRenditionClass () {
		TextType absProp = (TextType)getFirstEquivalentProperty(RENDITION_CLASS,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setRenditionClass (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, RENDITION_CLASS, value));
	}
	
	public String getFromPart () {
		TextType absProp = (TextType)getFirstEquivalentProperty(FROM_PART,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setFromPart (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, FROM_PART, value));
	}
	
	public String getToPart () {
		TextType absProp = (TextType)getFirstEquivalentProperty(TO_PART,TextType.class);
		if (absProp != null) {
			return absProp.getStringValue();
		} else {
			return null;
		}
	}
	
	public void setToPart (String value) {
		this.addProperty(new TextType(metadata, PREFERRED_PREFIX, TO_PART, value));
	}
	
	public void addAlternatePath(String value) {
        ComplexProperty seq = (ComplexProperty) getFirstEquivalentProperty(ALTERNATE_PATHS, ComplexProperty.class);
        if (seq==null) {
        	seq = new ComplexProperty(metadata,
                    PREFERRED_PREFIX, ALTERNATE_PATHS,
                    ComplexProperty.ORDERED_ARRAY);
        	addProperty(seq);
        }
        seq.getContainer().addProperty(new TextType(metadata, "rdf", "li", value) );
	}

	/**
	 * Get Versions property
	 * 
	 * @return version property to set
	 */
	public ComplexProperty getAlternatePathsProperty() {
        ComplexProperty seq = (ComplexProperty) getFirstEquivalentProperty(ALTERNATE_PATHS, ComplexProperty.class);
		return seq;
	}

	/**
	 * Get List of Versions values
	 * 
	 * @return List of Versions values
	 */
	public List<String> getAlternatePaths() {
        ComplexProperty seq = (ComplexProperty) getFirstEquivalentProperty(ALTERNATE_PATHS, ComplexProperty.class);
        if (seq!=null) {
        	return getArrayListToString(seq);
        } else {
        	return null;
        }
	}

	// TODO should factorize in helper (exists in XMPSchema)
    private List<String> getArrayListToString(ComplexProperty array) {
        List<String> retval = null;
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

	
}
