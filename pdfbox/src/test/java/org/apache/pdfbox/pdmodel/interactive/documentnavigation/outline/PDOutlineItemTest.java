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

import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrea Vacondio
 *
 */
public class PDOutlineItemTest
{
    private PDOutlineItem root;
    private PDOutlineItem first;
    private PDOutlineItem second;
    private PDOutlineItem newSibling;

    @Before
    public void setUp()
    {
        root = new PDOutlineItem();
        first = new PDOutlineItem();
        second = new PDOutlineItem();
        root.addLast(first);
        root.addLast(second);
        newSibling = new PDOutlineItem();
        newSibling.addLast(new PDOutlineItem());
        newSibling.addLast(new PDOutlineItem());
    }

    @Test
    public void insertSiblingAfter_OpenChildToOpenParent()
    {
        newSibling.openNode();
        root.openNode();
        assertEquals(2, root.getOpenCount());
        first.insertSiblingAfter(newSibling);
        assertEquals(first.getNextSibling(), newSibling);
        assertEquals(second.getPreviousSibling(), newSibling);
        assertEquals(5, root.getOpenCount());
    }

    @Test
    public void insertSiblingBefore_OpenChildToOpenParent()
    {
        newSibling.openNode();
        root.openNode();
        assertEquals(2, root.getOpenCount());
        second.insertSiblingBefore(newSibling);
        assertEquals(first.getNextSibling(), newSibling);
        assertEquals(second.getPreviousSibling(), newSibling);
        assertEquals(5, root.getOpenCount());
    }

    @Test
    public void insertSiblingAfter_OpenChildToClosedParent()
    {
        newSibling.openNode();
        assertEquals(-2, root.getOpenCount());
        first.insertSiblingAfter(newSibling);
        assertEquals(first.getNextSibling(), newSibling);
        assertEquals(second.getPreviousSibling(), newSibling);
        assertEquals(-5, root.getOpenCount());
    }

    @Test
    public void insertSiblingBefore_OpenChildToClosedParent()
    {
        newSibling.openNode();
        assertEquals(-2, root.getOpenCount());
        second.insertSiblingBefore(newSibling);
        assertEquals(first.getNextSibling(), newSibling);
        assertEquals(second.getPreviousSibling(), newSibling);
        assertEquals(-5, root.getOpenCount());
    }

    @Test
    public void insertSiblingAfter_ClosedChildToOpenParent()
    {
        root.openNode();
        assertEquals(2, root.getOpenCount());
        first.insertSiblingAfter(newSibling);
        assertEquals(first.getNextSibling(), newSibling);
        assertEquals(second.getPreviousSibling(), newSibling);
        assertEquals(3, root.getOpenCount());
    }

    @Test
    public void insertSiblingBefore_ClosedChildToOpenParent()
    {
        root.openNode();
        assertEquals(2, root.getOpenCount());
        second.insertSiblingBefore(newSibling);
        assertEquals(first.getNextSibling(), newSibling);
        assertEquals(second.getPreviousSibling(), newSibling);
        assertEquals(3, root.getOpenCount());
    }

    @Test
    public void insertSiblingAfter_ClosedChildToClosedParent()
    {
        assertEquals(-2, root.getOpenCount());
        first.insertSiblingAfter(newSibling);
        assertEquals(first.getNextSibling(), newSibling);
        assertEquals(second.getPreviousSibling(), newSibling);
        assertEquals(-3, root.getOpenCount());
    }

    @Test
    public void insertSiblingBefore_ClosedChildToClosedParent()
    {
        assertEquals(-2, root.getOpenCount());
        second.insertSiblingBefore(newSibling);
        assertEquals(first.getNextSibling(), newSibling);
        assertEquals(second.getPreviousSibling(), newSibling);
        assertEquals(-3, root.getOpenCount());
    }

    @Test
    public void insertSiblingTop()
    {
        assertEquals(root.getFirstChild(), first);
        PDOutlineItem newSibling = new PDOutlineItem();
        first.insertSiblingBefore(newSibling);
        assertEquals(first.getPreviousSibling(), newSibling);
        assertEquals(root.getFirstChild(), newSibling);
    }

    @Test
    public void insertSiblingTopNoParent()
    {
        assertEquals(root.getFirstChild(), first);
        PDOutlineItem newSibling = new PDOutlineItem();
        root.insertSiblingBefore(newSibling);
        assertEquals(root.getPreviousSibling(), newSibling);
    }

    @Test
    public void insertSiblingBottom()
    {
        assertEquals(root.getLastChild(), second);
        PDOutlineItem newSibling = new PDOutlineItem();
        second.insertSiblingAfter(newSibling);
        assertEquals(second.getNextSibling(), newSibling);
        assertEquals(root.getLastChild(), newSibling);
    }

    @Test
    public void insertSiblingBottomNoParent()
    {
        assertEquals(root.getLastChild(), second);
        PDOutlineItem newSibling = new PDOutlineItem();
        root.insertSiblingAfter(newSibling);
        assertEquals(root.getNextSibling(), newSibling);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotInsertSiblingBeforeAList()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.insertSiblingAfter(new PDOutlineItem());
        child.insertSiblingAfter(new PDOutlineItem());
        root.insertSiblingBefore(child);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotInsertSiblingAfterAList()
    {
        PDOutlineItem child = new PDOutlineItem();
        child.insertSiblingAfter(new PDOutlineItem());
        child.insertSiblingAfter(new PDOutlineItem());
        root.insertSiblingAfter(child);
    }
}
