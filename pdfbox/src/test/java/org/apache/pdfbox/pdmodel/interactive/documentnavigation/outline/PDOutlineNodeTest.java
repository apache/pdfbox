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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Andrea Vacondio
 *
 */
class PDOutlineNodeTest
{
    private PDOutlineItem root;

    @BeforeEach
    public void setUp()
    {
        root = new PDOutlineItem();
    }

    @Test
    void getParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addLast(child);
        PDDocumentOutline outline = new PDDocumentOutline();
        outline.addLast(root);
        assertNull(outline.getParent());
        assertEquals(outline, root.getParent());
        assertEquals(root, child.getParent());
    }

    @Test
    void nullLastChild()
    {
        assertNull(root.getLastChild());
    }

    @Test
    void nullFirstChild()
    {
        assertNull(root.getFirstChild());
    }

    @Test
    void openAlreadyOpenedRootNode()
    {
        PDOutlineItem child = new PDOutlineItem();
        assertEquals(0, root.getOpenCount());
        root.addLast(child);
        root.openNode();
        assertTrue(root.isNodeOpen());
        assertEquals(1, root.getOpenCount());
        root.openNode();
        assertTrue(root.isNodeOpen());
        assertEquals(1, root.getOpenCount());
    }

    @Test
    void closeAlreadyClosedRootNode()
    {
        PDOutlineItem child = new PDOutlineItem();
        assertEquals(0, root.getOpenCount());
        root.addLast(child);
        root.openNode();
        root.closeNode();
        assertFalse(root.isNodeOpen());
        assertEquals(-1, root.getOpenCount());
        root.closeNode();
        assertFalse(root.isNodeOpen());
        assertEquals(-1, root.getOpenCount());
    }

    @Test
    void openLeaf()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addLast(child);
        child.openNode();
        assertFalse(child.isNodeOpen());
    }

    @Test
    void nodeClosedByDefault()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addLast(child);
        assertFalse(root.isNodeOpen());
        assertEquals(-1, root.getOpenCount());
    }

    @Test
    void closeNodeWithOpendParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addLast(new PDOutlineItem());
        child.addLast(new PDOutlineItem());
        child.openNode();
        root.addLast(child);
        root.openNode();
        assertEquals(3, root.getOpenCount());
        assertEquals(2, child.getOpenCount());
        child.closeNode();
        assertEquals(1, root.getOpenCount());
        assertEquals(-2, child.getOpenCount());
    }

    @Test
    void closeNodeWithClosedParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addLast(new PDOutlineItem());
        child.addLast(new PDOutlineItem());
        child.openNode();
        root.addLast(child);
        assertEquals(-3, root.getOpenCount());
        assertEquals(2, child.getOpenCount());
        child.closeNode();
        assertEquals(-1, root.getOpenCount());
        assertEquals(-2, child.getOpenCount());
    }

    @Test
    void openNodeWithOpendParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addLast(new PDOutlineItem());
        child.addLast(new PDOutlineItem());
        root.addLast(child);
        root.openNode();
        assertEquals(1, root.getOpenCount());
        assertEquals(-2, child.getOpenCount());
        child.openNode();
        assertEquals(3, root.getOpenCount());
        assertEquals(2, child.getOpenCount());
    }

    @Test
    void openNodeWithClosedParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addLast(new PDOutlineItem());
        child.addLast(new PDOutlineItem());
        root.addLast(child);
        assertEquals(-1, root.getOpenCount());
        assertEquals(-2, child.getOpenCount());
        child.openNode();
        assertEquals(-3, root.getOpenCount());
        assertEquals(2, child.getOpenCount());
    }

    @Test
    void addLastSingleChild()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addLast(child);
        assertEquals(child, root.getFirstChild());
        assertEquals(child, root.getLastChild());
    }

    @Test
    void addFirstSingleChild()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addFirst(child);
        assertEquals(child, root.getFirstChild());
        assertEquals(child, root.getLastChild());
    }

    @Test
    void addLastOpenChildToOpenParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addLast(new PDOutlineItem());
        child.addLast(new PDOutlineItem());
        child.openNode();
        root.addLast(new PDOutlineItem());
        root.openNode();
        assertEquals(1, root.getOpenCount());
        assertEquals(2, child.getOpenCount());
        root.addLast(child);
        assertNotEquals(child, root.getFirstChild());
        assertEquals(child, root.getLastChild());
        assertEquals(4, root.getOpenCount());
    }

    @Test
    void addFirstOpenChildToOpenParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addFirst(new PDOutlineItem());
        child.addFirst(new PDOutlineItem());
        child.openNode();
        root.addFirst(new PDOutlineItem());
        root.openNode();
        assertEquals(1, root.getOpenCount());
        assertEquals(2, child.getOpenCount());
        root.addFirst(child);
        assertNotEquals(child, root.getLastChild());
        assertEquals(child, root.getFirstChild());
        assertEquals(4, root.getOpenCount());
    }

    @Test
    void addLastOpenChildToClosedParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addLast(new PDOutlineItem());
        child.addLast(new PDOutlineItem());
        child.openNode();
        root.addLast(new PDOutlineItem());
        assertEquals(-1, root.getOpenCount());
        assertEquals(2, child.getOpenCount());
        root.addLast(child);
        assertNotEquals(child, root.getFirstChild());
        assertEquals(child, root.getLastChild());
        assertEquals(-4, root.getOpenCount());
    }

    @Test
    void addFirstOpenChildToClosedParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addFirst(new PDOutlineItem());
        child.addFirst(new PDOutlineItem());
        child.openNode();
        root.addFirst(new PDOutlineItem());
        assertEquals(-1, root.getOpenCount());
        assertEquals(2, child.getOpenCount());
        root.addFirst(child);
        assertNotEquals(child, root.getLastChild());
        assertEquals(child, root.getFirstChild());
        assertEquals(-4, root.getOpenCount());
    }

    @Test
    void addLastClosedChildToOpenParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addLast(new PDOutlineItem());
        child.addLast(new PDOutlineItem());
        root.addLast(new PDOutlineItem());
        root.openNode();
        assertEquals(1, root.getOpenCount());
        assertEquals(-2, child.getOpenCount());
        root.addLast(child);
        assertNotEquals(child, root.getFirstChild());
        assertEquals(child, root.getLastChild());
        assertEquals(2, root.getOpenCount());
    }

    @Test
    void addFirstClosedChildToOpenParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addFirst(new PDOutlineItem());
        child.addFirst(new PDOutlineItem());
        root.addFirst(new PDOutlineItem());
        root.openNode();
        assertEquals(1, root.getOpenCount());
        assertEquals(-2, child.getOpenCount());
        root.addFirst(child);
        assertNotEquals(child, root.getLastChild());
        assertEquals(child, root.getFirstChild());
        assertEquals(2, root.getOpenCount());
    }

    @Test
    void addLastClosedChildToClosedParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addLast(new PDOutlineItem());
        child.addLast(new PDOutlineItem());
        root.addLast(new PDOutlineItem());
        assertEquals(-1, root.getOpenCount());
        assertEquals(-2, child.getOpenCount());
        root.addLast(child);
        assertNotEquals(child, root.getFirstChild());
        assertEquals(child, root.getLastChild());
        assertEquals(-2, root.getOpenCount());
    }

    @Test
    void addFirstClosedChildToClosedParent()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.addFirst(new PDOutlineItem());
        child.addFirst(new PDOutlineItem());
        root.addFirst(new PDOutlineItem());
        assertEquals(-1, root.getOpenCount());
        assertEquals(-2, child.getOpenCount());
        root.addFirst(child);
        assertNotEquals(child, root.getLastChild());
        assertEquals(child, root.getFirstChild());
        assertEquals(-2, root.getOpenCount());
    }

    @Test
    void cannotAddLastAList()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.insertSiblingAfter(new PDOutlineItem());
        child.insertSiblingAfter(new PDOutlineItem());
        assertThrows(IllegalArgumentException.class, () -> root.addLast(child));
    }

    @Test
    void cannotAddFirstAList()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.insertSiblingAfter(new PDOutlineItem());
        child.insertSiblingAfter(new PDOutlineItem());
        assertThrows(IllegalArgumentException.class, () -> root.addFirst(child));
    }

    @Test
    void equalsNode()
    {
        root.addFirst(new PDOutlineItem());
        assertEquals(root.getFirstChild(), root.getLastChild());
    }

    @Test
    void iterator()
    {
        PDOutlineItem first = new PDOutlineItem();
        root.addFirst(first);
        root.addLast(new PDOutlineItem());
        PDOutlineItem second = new PDOutlineItem();
        first.insertSiblingAfter(second);
        int counter = 0;
        for (PDOutlineItem current : root.children())
        {
            counter++;
        }
        assertEquals(3, counter);
    }

    @Test
    void iteratorNoChildre()
    {
        int counter = 0;
        for (PDOutlineItem current : new PDOutlineItem().children())
        {
            counter++;
        }
        assertEquals(0, counter);
    }

    @Test
    void openNodeAndAppend()
    {
        // TODO
    }

}
