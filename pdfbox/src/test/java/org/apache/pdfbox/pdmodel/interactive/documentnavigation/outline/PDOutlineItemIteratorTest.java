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
package org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class PDOutlineItemIteratorTest
{

    @Test
    public void singleItem()
    {
        PDOutlineItem first = new PDOutlineItem();
        PDOutlineItemIterator iterator = new PDOutlineItemIterator(first);
        assertTrue(iterator.hasNext());
        assertEquals(first, iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void multipleItem()
    {
        PDOutlineItem first = new PDOutlineItem();
        PDOutlineItem second = new PDOutlineItem();
        first.setNextSibling(second);
        PDOutlineItemIterator iterator = new PDOutlineItemIterator(first);
        assertTrue(iterator.hasNext());
        assertEquals(first, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(second, iterator.next());
        assertFalse(iterator.hasNext());
    }

    public void removeUnsupported()
    {
        assertThrows(UnsupportedOperationException.class,
                () -> 
                new PDOutlineItemIterator(new PDOutlineItem()).remove());
    }

    @Test
    public void noChildren()
    {
        PDOutlineItemIterator iterator = new PDOutlineItemIterator(null);
        assertFalse(iterator.hasNext());
    }
}
