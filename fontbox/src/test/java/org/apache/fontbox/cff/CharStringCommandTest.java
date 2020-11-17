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

package org.apache.fontbox.cff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;

import org.apache.fontbox.cff.CharStringCommand.Key;
import org.apache.fontbox.cff.CharStringCommand.Type1KeyWord;
import org.apache.fontbox.cff.CharStringCommand.Type2KeyWord;
import org.junit.jupiter.api.Test;

class CharStringCommandTest
{

    @Test
    void testKey()
    {
        Key key1 = new Key(1);
        int[] value1 = key1.getValue();
        assertEquals(1, value1.length);
        assertEquals(1, value1[0]);
        assertEquals(1, key1.hashCode());
        assertEquals(Arrays.toString(new int[] { 1 }), key1.toString());

        Key key12_0 = new Key(12, 0);
        int[] value12_0 = key12_0.getValue();
        assertEquals(2, value12_0.length);
        assertEquals(12, value12_0[0]);
        assertEquals(0, value12_0[1]);
        assertEquals(12 ^ 0, key12_0.hashCode());
        assertEquals(Arrays.toString(new int[] { 12, 0 }), key12_0.toString());

        int[] keyValues12_3 = new int[] { 12, 3 };
        Key key12_3 = new Key(keyValues12_3);
        int[] value12_3 = key12_3.getValue();
        assertEquals(2, value12_3.length);
        assertEquals(12, value12_3[0]);
        assertEquals(3, value12_3[1]);
        assertEquals(12 ^ 3, key12_3.hashCode());

        assertEquals(Type1KeyWord.HSTEM.key, key1);
        assertNotEquals(key1, key12_0);
        assertNotEquals(key12_0, key1);
        assertNotEquals(key12_0, key12_3);
        assertNotEquals(key12_0, new Key(new int[] { 12, 3, 0 }));
    }

    @Test
    void testCharStringCommand()
    {
        CharStringCommand charStringCommand1 = new CharStringCommand(1);
        assertEquals(1, charStringCommand1.getKey().getValue()[0]);
        assertEquals(Type1KeyWord.HSTEM, charStringCommand1.getType1KeyWord());
        assertEquals(Type2KeyWord.HSTEM, charStringCommand1.getType2KeyWord());
        assertEquals(1, charStringCommand1.hashCode());
        assertEquals("HSTEM|", charStringCommand1.toString());

        CharStringCommand charStringCommand12_0 = new CharStringCommand(12, 0);
        assertEquals(12, charStringCommand12_0.getKey().getValue()[0]);
        assertEquals(0, charStringCommand12_0.getKey().getValue()[1]);
        assertEquals(Type1KeyWord.DOTSECTION, charStringCommand12_0.getType1KeyWord());
        assertNull(charStringCommand12_0.getType2KeyWord());
        assertEquals(12 ^ 0, charStringCommand12_0.hashCode());
        assertEquals("DOTSECTION|", charStringCommand12_0.toString());

        int[] values12_3 = new int[] { 12, 3 };
        CharStringCommand charStringCommand12_3 = new CharStringCommand(values12_3);
        assertEquals(12, charStringCommand12_3.getKey().getValue()[0]);
        assertEquals(3, charStringCommand12_3.getKey().getValue()[1]);
        assertNull(charStringCommand12_3.getType1KeyWord());
        assertEquals(Type2KeyWord.AND, charStringCommand12_3.getType2KeyWord());
        assertEquals(12 ^ 3, charStringCommand12_3.hashCode());
        assertEquals("AND|", charStringCommand12_3.toString());

        assertNotEquals(charStringCommand1, charStringCommand12_0);
        assertNotEquals(charStringCommand12_0, charStringCommand12_3);
    }

    @Test
    void testUnknownCharStringCommand()
    {
        CharStringCommand charStringCommandUnknown = new CharStringCommand(99);
        assertEquals(99, charStringCommandUnknown.getKey().getValue()[0]);
        assertEquals("[99]|", charStringCommandUnknown.toString());
    }

}
