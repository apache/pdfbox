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

import java.lang.reflect.Constructor;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.PropertiesDescription;
import org.apache.xmpbox.type.PropertyType;

/**
 * A factory for each kind of schemas
 * 
 * @author a183132
 * 
 */
public class XMPSchemaFactory
{

    private String namespace;

    private Class<? extends XMPSchema> schemaClass;

    private PropertiesDescription propDef;

    private String nsName;

    /**
     * Factory Constructor for basic known schemas
     * 
     * @param namespace
     *            namespace URI to treat
     * @param schemaClass
     *            Class representation associated to this URI
     * @param propDef
     *            Properties Types list associated
     */
    public XMPSchemaFactory(String namespace, Class<? extends XMPSchema> schemaClass, PropertiesDescription propDef)
    {
        this.namespace = namespace;
        this.schemaClass = schemaClass;
        this.propDef = propDef;
    }

    /**
     * Get namespace URI treated by this factory
     * 
     * @return The namespace URI
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * Get type declared for the name property given
     * 
     * @param name
     *            The property name
     * @return null if propery name is unknown
     */
    public PropertyType getPropertyType(String name)
    {
        return propDef.getPropertyType(name);
    }

    /**
     * Create a schema that corresponding to this factory and add it to metadata
     * 
     * @param metadata
     *            Metadata to attach the Schema created
     * @param prefix
     *            The namespace prefix (optional)
     * @return the schema created and added to metadata
     * @throws XmpSchemaException
     *             When Instancing specified Object Schema failed
     */
    public XMPSchema createXMPSchema(XMPMetadata metadata, String prefix) throws XmpSchemaException
    {
        XMPSchema schema = null;
        Class<?>[] argsClass;
        Object[] schemaArgs;

        if (schemaClass == XMPSchema.class)
        {
            argsClass = new Class[] { XMPMetadata.class, String.class, String.class };
            schemaArgs = new Object[] { metadata, namespace, nsName };
        }
        else if (prefix != null && !"".equals(prefix))
        {
            argsClass = new Class[] { XMPMetadata.class, String.class };
            schemaArgs = new Object[] { metadata, prefix };
        }
        else
        {
            argsClass = new Class[] { XMPMetadata.class };
            schemaArgs = new Object[] { metadata };
        }

        Constructor<? extends XMPSchema> schemaConstructor;
        try
        {
            schemaConstructor = schemaClass.getConstructor(argsClass);
            schema = schemaConstructor.newInstance(schemaArgs);
            if (schema != null)
            {
                metadata.addSchema(schema);
            }
            return schema;
        }
        catch (Exception e)
        {
            throw new XmpSchemaException("Cannot Instanciate specified Object Schema", e);
        }
    }

    public PropertiesDescription getPropertyDefinition()
    {
        return this.propDef;
    }

}
