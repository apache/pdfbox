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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class PDDocumentOutlineTest
{
    /**
     * see PDF 32000-1:2008 table 152
     */
    @Test
    public void outlinesCountShouldNotBeNegative()
    {
        PDDocumentOutline outline = new PDDocumentOutline();
        PDOutlineItem firstLevelChild = new PDOutlineItem();
        outline.addLast(firstLevelChild);
        PDOutlineItem secondLevelChild = new PDOutlineItem();
        firstLevelChild.addLast(secondLevelChild);
        assertEquals(0, secondLevelChild.getOpenCount());
        assertEquals(-1, firstLevelChild.getOpenCount());
        assertFalse("Outlines count cannot be " + outline.getOpenCount(),
                outline.getOpenCount() < 0);
    }

    @Test
    public void outlinesCount()
    {
        PDDocumentOutline outline = new PDDocumentOutline();
        PDOutlineItem root = new PDOutlineItem();
        outline.addLast(root);
        assertEquals(1, outline.getOpenCount());
        root.addLast(new PDOutlineItem());
        assertEquals(-1, root.getOpenCount());
        assertEquals(1, outline.getOpenCount());
        root.addLast(new PDOutlineItem());
        assertEquals(-2, root.getOpenCount());
        assertEquals(1, outline.getOpenCount());
        root.openNode();
        assertEquals(2, root.getOpenCount());
        assertEquals(3, outline.getOpenCount());
    }
}
