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


import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.PDDictionaryWrapper;

/**
 * Base class for a node in the outline of a PDF document.
 *
 * @author Ben Litchfield
 */
public abstract class PDOutlineNode extends PDDictionaryWrapper
{

    /**
     * Default Constructor.
     */
    public PDOutlineNode()
    {
    }

    /**
     * @param dict The dictionary storage.
     */
    public PDOutlineNode(final COSDictionary dict)
    {
        super(dict);
    }

    /**
     * @return The parent of this node or null if there is no parent.
     */
    PDOutlineNode getParent()
    {
        final COSBase base = getCOSObject().getDictionaryObject(COSName.PARENT);
        if (base instanceof COSDictionary)
        {
            final COSDictionary parent = (COSDictionary) base;
            if (COSName.OUTLINES.equals(parent.getCOSName(COSName.TYPE)))
            {
                return new PDDocumentOutline(parent);
            }
            return new PDOutlineItem(parent);
        }
        return null;
    }

    void setParent(final PDOutlineNode parent)
    {
        getCOSObject().setItem(COSName.PARENT, parent);
    }

    /**
     * Adds the given node to the bottom of the children list.
     *
     * @param newChild The node to add.
     * @throws IllegalArgumentException if the given node is part of a list (i.e. if it has a previous or a next
     * sibling)
     */
    public void addLast(final PDOutlineItem newChild)
    {
        requireSingleNode(newChild);
        append(newChild);
        updateParentOpenCountForAddedChild(newChild);
    }

    /**
     * Adds the given node to the top of the children list.
     * 
     * @param newChild The node to add.
     * @throws IllegalArgumentException if the given node is part of a list (i.e. if it has a previous or a next
     * sibling)
     */
    public void addFirst(final PDOutlineItem newChild)
    {
        requireSingleNode(newChild);
        prepend(newChild);
        updateParentOpenCountForAddedChild(newChild);
    }

    /**
     * @param node
     * @throws IllegalArgumentException if the given node is part of a list (i.e. if it has a previous or a next
     * sibling)
     */
    void requireSingleNode(final PDOutlineItem node)
    {
        if (node.getNextSibling() != null || node.getPreviousSibling() != null)
        {
            throw new IllegalArgumentException("A single node with no siblings is required");
        }
    }

    /**
     * Appends the child to the linked list of children. This method only adjust pointers but doesn't take care of the
     * Count key in the parent hierarchy.
     * 
     * @param newChild
     */
    private void append(final PDOutlineItem newChild)
    {
        newChild.setParent(this);
        if (!hasChildren())
        {
            setFirstChild(newChild);
        }
        else
        {
            final PDOutlineItem previousLastChild = getLastChild();
            previousLastChild.setNextSibling(newChild);
            newChild.setPreviousSibling(previousLastChild);
        }
        setLastChild(newChild);
    }

    /**
     * Prepends the child to the linked list of children. This method only adjust pointers but doesn't take care of the
     * Count key in the parent hierarchy.
     * 
     * @param newChild
     */
    private void prepend(final PDOutlineItem newChild)
    {
        newChild.setParent(this);
        if (!hasChildren())
        {
            setLastChild(newChild);
        }
        else
        {
            final PDOutlineItem previousFirstChild = getFirstChild();
            newChild.setNextSibling(previousFirstChild);
            previousFirstChild.setPreviousSibling(newChild);
        }
        setFirstChild(newChild);
    }

    void updateParentOpenCountForAddedChild(final PDOutlineItem newChild)
    {
        int delta = 1;
        if (newChild.isNodeOpen())
        {
            delta += newChild.getOpenCount();
        }
        newChild.updateParentOpenCount(delta);
    }

    /**
     * @return true if the node has at least one child
     */
    public boolean hasChildren()
    {
        return getFirstChild() != null;
    }

    PDOutlineItem getOutlineItem(final COSName name)
    {
        final COSBase base = getCOSObject().getDictionaryObject(name);
        if (base instanceof COSDictionary)
        {
            return new PDOutlineItem((COSDictionary) base);
        }
        return null;
    }

    /**
     * @return The first child or null if there is no child.
     */
    public PDOutlineItem getFirstChild()
    {
        return getOutlineItem(COSName.FIRST);
    }

    /**
     * Set the first child, this will be maintained by this class.
     *
     * @param outlineNode The new first child.
     */
    void setFirstChild(final PDOutlineNode outlineNode)
    {
        getCOSObject().setItem(COSName.FIRST, outlineNode);
    }

    /**
     * @return The last child or null if there is no child.
     */
    public PDOutlineItem getLastChild()
    {
        return getOutlineItem(COSName.LAST);
    }

    /**
     * Set the last child, this will be maintained by this class.
     *
     * @param outlineNode The new last child.
     */
    void setLastChild(final PDOutlineNode outlineNode)
    {
        getCOSObject().setItem(COSName.LAST, outlineNode);
    }

    /**
     * Get the number of open nodes or a negative number if this node is closed.
     * See PDF Reference 32000-1:2008 table 152 and 153 for more details. This
     * value is updated as you append children and siblings.
     *
     * @return The Count attribute of the outline dictionary.
     */
    public int getOpenCount()
    {
        return getCOSObject().getInt(COSName.COUNT, 0);
    }

    /**
     * Set the open count. This number is automatically managed for you when you add items to the outline.
     *
     * @param openCount The new open count.
     */
    void setOpenCount(final int openCount)
    {
        getCOSObject().setInt(COSName.COUNT, openCount);
    }

    /**
     * This will set this node to be open when it is shown in the viewer. By default, when a new node is created it will
     * be closed. This will do nothing if the node is already open.
     */
    public void openNode()
    {
        //if the node is already open then do nothing.
        if( !isNodeOpen() )
        {
            switchNodeCount();
        }
    }

    /**
     * Close this node.
     *
     */
    public void closeNode()
    {
        if (isNodeOpen())
        {
            switchNodeCount();
        }
    }

    private void switchNodeCount()
    {
        final int openCount = getOpenCount();
        setOpenCount(-openCount);
        updateParentOpenCount(-openCount);
    }

    /**
     * @return true if this node count is greater than zero, false otherwise.
     */
    public boolean isNodeOpen()
    {
        return getOpenCount() > 0;
    }

    /**
     * The count parameter needs to be updated when you add, remove, open or close outline items.
     *
     * @param delta The amount to update by.
     */
    void updateParentOpenCount(final int delta)
    {
        final PDOutlineNode parent = getParent();
        if (parent != null)
        {
            if (parent.isNodeOpen())
            {
                parent.setOpenCount(parent.getOpenCount() + delta);
                parent.updateParentOpenCount(delta);
            }
            else
            {
                parent.setOpenCount(parent.getOpenCount() - delta);
            }
        }
    }

    /**
     * @return An {@link Iterable} view of the items children
     */
    public Iterable<PDOutlineItem> children()
    {
        return () -> new PDOutlineItemIterator(getFirstChild());
    }
}
