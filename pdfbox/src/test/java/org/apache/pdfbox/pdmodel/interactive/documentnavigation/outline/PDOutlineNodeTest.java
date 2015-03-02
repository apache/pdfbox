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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class PDOutlineNodeTest
{
    private PDOutlineItem root;

    @Before
    public void setUp()
    {
        root = new PDOutlineItem();
    }

    @Test
    public void getParent()
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
    public void nullLastChild()
    {
        assertNull(root.getLastChild());
    }

    @Test
    public void nullFirstChild()
    {
        assertNull(root.getFirstChild());
    }

    @Test
    public void openAlreadyOpenedRootNode()
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
    public void closeAlreadyClosedRootNode()
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
    public void openLeaf()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addLast(child);
        child.openNode();
        assertFalse(child.isNodeOpen());
    }

    @Test
    public void nodeClosedByDefault()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addLast(child);
        assertFalse(root.isNodeOpen());
        assertEquals(-1, root.getOpenCount());
    }

    @Test
    public void closeNodeWithOpendParent()
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
    public void closeNodeWithClosedParent()
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
    public void openNodeWithOpendParent()
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
    public void openNodeWithClosedParent()
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
    public void addLastSingleChild()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addLast(child);
        assertEquals(child, root.getFirstChild());
        assertEquals(child, root.getLastChild());
    }

    @Test
    public void addFirstSingleChild()
    {
        PDOutlineItem child = new PDOutlineItem();
        root.addFirst(child);
        assertEquals(child, root.getFirstChild());
        assertEquals(child, root.getLastChild());
    }

    @Test
    public void addLastOpenChildToOpenParent()
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
    public void addFirstOpenChildToOpenParent()
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
    public void addLastOpenChildToClosedParent()
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
    public void addFirstOpenChildToClosedParent()
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
    public void addLastClosedChildToOpenParent()
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
    public void addFirstClosedChildToOpenParent()
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
    public void addLastClosedChildToClosedParent()
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
    public void addFirstClosedChildToClosedParent()
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

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddLastAList()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.insertSiblingAfter(new PDOutlineItem());
        child.insertSiblingAfter(new PDOutlineItem());
        root.addLast(child);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddFirstAList()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.insertSiblingAfter(new PDOutlineItem());
        child.insertSiblingAfter(new PDOutlineItem());
        root.addFirst(child);
    }

    @Test
    public void equalsNode()
    {
        root.addFirst(new PDOutlineItem());
        assertEquals(root.getFirstChild(), root.getLastChild());
    }

    @Test
    public void iterator()
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
    public void iteratorNoChildre()
    {
        int counter = 0;
        for (PDOutlineItem current : new PDOutlineItem().children())
        {
            counter++;
        }
        assertEquals(0, counter);
    }
    @Test
    public void openNodeAndAppend()
    {
        // TODO
    }

}
