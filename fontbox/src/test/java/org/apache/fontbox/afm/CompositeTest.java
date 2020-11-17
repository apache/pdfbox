/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.fontbox.afm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.Test;

class CompositeTest
{
    @Test
    void testComposite()
    {
        Composite composite = new Composite("name");
        assertEquals("name", composite.getName());
        assertEquals(0, composite.getParts().size());
        CompositePart compositePart = new CompositePart("name", 10, 20);
        composite.addPart(compositePart);
        List<CompositePart> parts = composite.getParts();
        assertEquals(1, parts.size());
        assertEquals("name", parts.get(0).getName());
        try
        {
            parts.add(compositePart);
            fail("An UnsupportedOperationException should have been thrown");
        }
        catch (UnsupportedOperationException exception)
        {
            // do nothing
        }
    }
}
