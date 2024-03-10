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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.fontbox.cff.CharStringCommand.Type1KeyWord;
import org.apache.fontbox.cff.CharStringCommand.Type2KeyWord;
import org.junit.jupiter.api.Test;

class CharStringCommandTest
{

    @Test
    void testValue()
    {
        assertEquals(CharStringCommand.HSTEM.getValue(), 1);
        assertEquals(CharStringCommand.ESCAPE.getValue(), 12);
        assertEquals(CharStringCommand.DOTSECTION.getValue(), (12 << 4) + 0);
        assertEquals(CharStringCommand.AND.getValue(), (12 << 4) + 3);
        assertEquals(CharStringCommand.HSBW.getValue(), 13);
    }

    @Test
    void testCharStringCommand()
    {
        CharStringCommand charStringCommand1 = CharStringCommand.getInstance(1);
        assertEquals(Type1KeyWord.HSTEM, charStringCommand1.getType1KeyWord());
        assertEquals(Type2KeyWord.HSTEM, charStringCommand1.getType2KeyWord());
        assertEquals("HSTEM|", charStringCommand1.toString());

        CharStringCommand charStringCommand12_0 = CharStringCommand.getInstance(12, 0);
        assertEquals(Type1KeyWord.DOTSECTION, charStringCommand12_0.getType1KeyWord());
        assertNull(charStringCommand12_0.getType2KeyWord());
        assertEquals("DOTSECTION|", charStringCommand12_0.toString());

        int[] values12_3 = { 12, 3 };
        CharStringCommand charStringCommand12_3 = CharStringCommand.getInstance(values12_3);
        assertNull(charStringCommand12_3.getType1KeyWord());
        assertEquals(Type2KeyWord.AND, charStringCommand12_3.getType2KeyWord());
        assertEquals("AND|", charStringCommand12_3.toString());

    }

    @Test
    void testUnknownCharStringCommand()
    {
        CharStringCommand charStringCommandUnknown = CharStringCommand.getInstance(99);
        assertEquals("unknown command|", charStringCommandUnknown.toString());
    }

}
