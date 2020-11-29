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
package org.apache.pdfbox.pdfwriter.compress;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class represents a traversed element of a COS tree. It allows to determine the position of a
 * {@link COSBase} in a hierarchical COS structure and provides the means to further traverse and evaluate it's
 * descendants.
 *
 * @author Christian Appl
 */
public class TraversedCOSElement
{

    private final TraversedCOSElement parent;
    private final COSBase currentObject;
    private final List<TraversedCOSElement> traversedChildren = new ArrayList<>();
    private boolean partOfStreamDictionary = false;
    private final List<COSBase> allObjects;

    /**
     * Construct a fresh entrypoint for the traversal of a hierarchical COS structure, beginning with the given
     * {@link COSBase}.
     *
     * @param currentObject The initial {@link COSBase}, with which the structure traversal shall begin.
     */
    public TraversedCOSElement(COSBase currentObject)
    {
        this(new ArrayList<>(), null, currentObject);
    }

    /**
     * Construct a traversal node for the traversal of a hierarchical COS structure, located at the given
     * {@link COSBase}, preceded by this given list of ancestors and contained in the given parent structure.
     *
     * @param allObjects The list of nodes, that have been traversed to reach the current object.
     * @param parent The parent node, that does contain this node.
     * @param currentObject The initial {@link COSBase}, with which the structure traversal shall begin.
     */
    private TraversedCOSElement(List<COSBase> allObjects, TraversedCOSElement parent,
            COSBase currentObject)
    {
        this.parent = parent;
        this.currentObject = currentObject;
        this.allObjects = allObjects;
    }

    /**
     * Construct a new traversal node for the given element and append it as a child to the current node.
     *
     * @param element The element, that shall be traversed.
     * @return The resulting traversal node, that has been created.
     */
    public TraversedCOSElement appendTraversedElement(COSBase element)
    {
        if (element == null)
        {
            return this;
        }
        allObjects.add(element);
        TraversedCOSElement traversedElement = new TraversedCOSElement(allObjects, this, element);
        traversedElement.setPartOfStreamDictionary(
                isPartOfStreamDictionary() || getCurrentBaseObject() instanceof COSStream);
        this.traversedChildren.add(traversedElement);
        return traversedElement;
    }

    /**
     * Returns the current {@link COSBase} of this traversal node.
     *
     * @return The current {@link COSBase} of this traversal node.
     */
    public COSBase getCurrentObject()
    {
        return currentObject;
    }

    /**
     * Returns the actual current {@link COSBase} of this traversal node. Meaning: If the current traversal node
     * contains a reference to a {@link COSObject}, it's actual base object will be returned instead.
     *
     * @return The actual current {@link COSBase} of this traversal node.
     */
    public COSBase getCurrentBaseObject()
    {
        return currentObject instanceof COSObject ? ((COSObject) currentObject).getObject()
                : currentObject;
    }

    /**
     * Returns the parent node of the current traversal node.
     *
     * @return The parent node of the current traversal node.
     */
    public TraversedCOSElement getParent()
    {
        return this.parent;
    }

    /**
     * Returns all known traversable/traversed children contained by the current traversal node.
     *
     * @return All known traversable/traversed children contained by the current traversal node.
     */
    public List<TraversedCOSElement> getTraversedChildren()
    {
        return traversedChildren;
    }

    public List<TraversedCOSElement> getTraversedElements()
    {
        List<TraversedCOSElement> ancestry = parent == null ? new ArrayList<>()
                : parent.getTraversedElements();
        ancestry.add(this);
        return ancestry;
    }

    /**
     * Searches all known traversed child nodes of the current traversal node for the given {@link COSBase}.
     *
     * @param object The {@link COSBase}, that shall be found.
     * @return The traversal node representing the searched {@link COSBase} or null, if such a node can not be found.
     */
    public TraversedCOSElement findAtCurrentPosition(COSBase object)
    {
        for (TraversedCOSElement child : traversedChildren)
        {
            if (child.getCurrentObject() == object)
            {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns a list of all objects, that have been traversed in the created traversal tree.
     *
     * @return A list of all objects, that have been traversed in the created traversal tree.
     */
    public List<COSBase> getAllTraversedObjects()
    {
        return allObjects;
    }

    /**
     * Returns true, if the given traversal node has been marked as a part of a {@link COSStream}.
     *
     * @return True, if the given traversal node has been marked as a part of a {@link COSStream}
     */
    public boolean isPartOfStreamDictionary()
    {
        return partOfStreamDictionary;
    }

    /**
     * Set to true, if the given traversal node shall be marked as a part of a {@link COSStream}.
     *
     * @param partOfStreamDictionary True, if the given traversal node shall be marked as a part of a {@link COSStream}
     */
    public void setPartOfStreamDictionary(boolean partOfStreamDictionary)
    {
        this.partOfStreamDictionary = partOfStreamDictionary;
    }

}
