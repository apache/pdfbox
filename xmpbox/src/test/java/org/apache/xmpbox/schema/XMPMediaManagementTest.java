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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.Types;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class XMPMediaManagementTest extends AbstractXMPSchemaTest
{

    @Before
    public void initTempMetaData() throws Exception
    {
        metadata = XMPMetadata.createXMPMetadata();
        schema = metadata.createAndAddXMPMediaManagementSchema();
        schemaClass = XMPMediaManagementSchema.class;
    }

    @Parameters(name = "{0} {1} '{2}'")
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        List<Object[]> data = new ArrayList<Object[]>();
        data.add(wrapProperty("DocumentID", Types.URI, "uuid:FB031973-5E75-11B2-8F06-E7F5C101C07A"));
        data.add(wrapProperty("Manager", Types.AgentName, "Raoul"));
        data.add(wrapProperty("ManageTo", Types.URI, "uuid:36"));
        data.add(wrapProperty("ManageUI", Types.URI, "uuid:3635"));
        // data.add(wrapProperty("ManageFrom", "ResourceRef", "uuid:36"));
        data.add(wrapProperty("InstanceID", Types.URI, "uuid:42"));
        data.add(wrapProperty("OriginalDocumentID", Types.Text, "uuid:142"));
        // data.add(wrapProperty("RenditionClass", "Text", "myclass"));
        data.add(wrapProperty("RenditionParams", Types.Text, "my params"));
        data.add(wrapProperty("VersionID", Types.Text, "14"));
        data.add(wrapProperty("Versions", Types.Version, Cardinality.Seq, new String[] { "1", "2", "3" }));
        data.add(wrapProperty("History", Types.Text, Cardinality.Seq,
                new String[] { "action 1", "action 2", "action 3" }));
        data.add(wrapProperty("Ingredients", Types.Text, Cardinality.Bag, new String[] { "resource1", "resource2" }));
        return data;
    }

    public XMPMediaManagementTest(String property, PropertyType type, Object value)
    {
        super(property, type, value);
    }

}
