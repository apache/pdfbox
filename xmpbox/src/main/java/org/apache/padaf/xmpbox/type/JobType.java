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

import org.apache.padaf.xmpbox.XMPMetadata;

public class JobType extends ComplexPropertyContainer {

    public static final String ELEMENT_NS = "http://ns.adobe.com/xap/1.0/sType/Job#";

    public static final String PREFERED_PREFIX = "stJob";
    
    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String URL = "url";

    protected XMPMetadata metadata;


    public JobType(XMPMetadata metadata, String namespace, String prefix,
            String propertyName) {
        super(metadata, namespace, prefix, propertyName);
        this.metadata = metadata;
        setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
    }

    
    
    public JobType(XMPMetadata metadata, String prefix, String propertyName) {
        super(metadata, prefix, propertyName);
        this.metadata = metadata;
        setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
    }

    public void setId(String prefix, String id) {
        this.addProperty(new TextType(metadata, prefix, ID, id));
    }

    public void setName(String prefix, String name) {
        this.addProperty(new TextType(metadata, prefix, NAME, name));
    }

    public void setUrl(String prefix, String name) {
        this.addProperty(new TextType(metadata, prefix, URL, name));
    }

    public String getId() {
        AbstractField absProp = getFirstEquivalentProperty(ID,TextType.class);
        if (absProp != null) {
            return ((TextType) absProp).getStringValue();
        }
        return null;
    }

    public String getName() {
        AbstractField absProp = getFirstEquivalentProperty(NAME,TextType.class);
        if (absProp != null) {
            return ((TextType) absProp).getStringValue();
        }
        return null;
    }

    public String getUrl() {
        AbstractField absProp = getFirstEquivalentProperty(URL,TextType.class);
        if (absProp != null) {
            return ((TextType) absProp).getStringValue();
        }
        return null;
    }

}
