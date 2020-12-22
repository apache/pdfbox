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

package org.apache.pdfbox.preflight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestPreflightPath
{

    @Test
    void test()
    {
        final PreflightPath path = new PreflightPath();

        assertTrue(path.isEmpty());
        assertEquals(0, path.size());

        path.pushObject("a");
        assertEquals(1, path.size());
        assertFalse(path.isEmpty());

        int position = path.getClosestTypePosition(String.class);
        assertEquals(0, position);

        path.pushObject(6);
        assertEquals(2, path.size());

        position = path.getClosestTypePosition(String.class);
        assertEquals(0, position);

        position = path.getClosestTypePosition(Integer.class);
        assertEquals(1, position);

        path.pushObject("b");
        assertEquals(3, path.size());

        position = path.getClosestTypePosition(String.class);
        assertEquals(2, position);
        position = path.getClosestTypePosition(Integer.class);
        assertEquals(1, position);

        final Integer i = path.getPathElement(position, Integer.class);
        assertEquals(Integer.valueOf(6), i);

        Object str = path.peek();
        assertEquals(3, path.size());
        assertEquals(String.class, str.getClass());
        assertEquals("b", str);

        str = path.pop();
        assertEquals(2, path.size());
        assertEquals(String.class, str.getClass());
        assertEquals("b", str);

        path.clear();
        assertTrue(path.isEmpty());
        assertEquals(0, path.size());
    }

    @Test
    void testPush()
    {
        final PreflightPath path = new PreflightPath();
        assertTrue(path.pushObject("a"));
        assertFalse(path.pushObject(null));
    }

}
