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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestXMPWithDefinedSchemas
{

    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception
    {
        List<Object[]> data = new ArrayList<Object[]>();
        data.add(new Object[] { "/validxmp/override_ns.rdf" });
        data.add(new Object[] { "/validxmp/ghost2.xmp" });
        data.add(new Object[] { "/validxmp/history2.rdf" });
        data.add(new Object[] { "/validxmp/Notepad++_A1b.xmp" });
        data.add(new Object[] { "/validxmp/metadata.rdf" });
        return data;
    }

    private String path;

    public TestXMPWithDefinedSchemas(String path)
    {
        this.path = path;
    }

    @Test
    public void main() throws Exception
    {

        InputStream is = this.getClass().getResourceAsStream(path);

        DomXmpParser builder = new DomXmpParser();
        XMPMetadata rxmp = builder.parse(is);

    }

}
