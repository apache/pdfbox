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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TestXMPWithDefinedSchemas
{
    static Stream<String> initializeParameters() throws Exception
    {
        return Stream.of(
            "/validxmp/override_ns.rdf",
            "/validxmp/ghost2.xmp",
            "/validxmp/history2.rdf",
            "/validxmp/Notepad++_A1b.xmp",
            "/validxmp/metadata.rdf"
        );
    }

    @ParameterizedTest
    @MethodSource("initializeParameters")
    void main(String path) throws Exception
    {
        InputStream is = this.getClass().getResourceAsStream(path);

        DomXmpParser builder = new DomXmpParser();
        XMPMetadata rxmp = builder.parse(is);
        // ensure basic parsing was OK
        assertTrue(rxmp.getAllSchemas().size()>0);
    }
}
