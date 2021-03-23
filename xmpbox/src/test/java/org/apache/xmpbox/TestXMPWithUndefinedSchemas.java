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

package org.apache.xmpbox;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.io.InputStream;
import java.util.stream.Stream;


import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestXMPWithUndefinedSchemas
{
    static Stream<Arguments> initializeParameters() throws Exception
    {
        return Stream.of(
            Arguments.of("/undefinedxmp/prism.xmp", "http://prismstandard.org/namespaces/basic/2.0/", "aggregationType", "journal")
        );
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void main(String path, String namespace, String propertyName, String propertyValue) throws Exception
    {
        InputStream is = this.getClass().getResourceAsStream(path);

        DomXmpParser builder = new DomXmpParser();
        builder.setStrictParsing(false);
        XMPMetadata rxmp = builder.parse(is);
        // ensure basic parsing was OK
        assertTrue(rxmp.getAllSchemas().size()>0, "There should be a least one schema");
        assertNotNull(rxmp.getSchema(namespace), "The schema for {" + namespace + "} should be available");
        assertNotNull(rxmp.getSchema(namespace).getProperty(propertyName), "The schema for {" + namespace + "} should have a property {" + propertyName + "} ");
        assertEquals(rxmp.getSchema(namespace).getProperty(propertyName).getPropertyName(), propertyName,  "The schema for {" + namespace + "} should have a property {" + propertyName + "} ");
        assertEquals(rxmp.getSchema(namespace).getUnqualifiedTextPropertyValue(propertyName), propertyValue,  "The property {" + propertyName + "} should have a value of {" + propertyValue + "}");
    }
}
