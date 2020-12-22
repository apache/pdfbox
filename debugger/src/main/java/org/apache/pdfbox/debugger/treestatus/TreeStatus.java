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

package org.apache.pdfbox.debugger.treestatus;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.debugger.ui.ArrayEntry;
import org.apache.pdfbox.debugger.ui.MapEntry;
import org.apache.pdfbox.debugger.ui.PageEntry;

/**
 * @author Khyrul Bashar
 */
public final class TreeStatus
{
    private Object rootNode;
   
    private TreeStatus()
    {
    }
    
    /**
     * Constructor.
     *
     * @param rootNode the root node of the tree which will be used to construct a treepath from a
     * tree status string.
     */
    public TreeStatus(final Object rootNode)
    {
        this.rootNode = rootNode;
    }

    /**
     * Provides status string for a TreePath instance.
     * @param path TreePath instance.
     * @return pathString.
     */
    public String getStringForPath(final TreePath path)
    {
        return generatePathString(path);
    }

    /**
     * Provides TreePath for a given status string. In case of invalid string returns null.
     * @param statusString
     * @return path.
     */
    public TreePath getPathForString(final String statusString)
    {
        return generatePath(statusString);
    }

    /**
     * Constructs a status string from the path.
     * @param path
     * @return the status string.
     */
    private String generatePathString(TreePath path)
    {
        final StringBuilder pathStringBuilder = new StringBuilder();
        while (path.getParentPath() != null)
        {
            final Object object = path.getLastPathComponent();
            pathStringBuilder.insert(0, "/" + getObjectName(object));
            path = path.getParentPath();
        }
        pathStringBuilder.delete(0, 1);
        return pathStringBuilder.toString();
    }

    /**
     * Constructs TreePath from Status String.
     * @param pathString
     * @return a TreePath, or null if there is an error.
     */
    private TreePath generatePath(final String pathString)
    {
        final List<String> nodes = parsePathString(pathString);
        if (nodes == null)
        {
            return null;
        }
        Object obj = rootNode;
        TreePath treePath = new TreePath(obj);
        for (final String node : nodes)
        {
            obj = searchNode(obj, node);
            if (obj == null)
            {
                return null;
            }
            treePath = treePath.pathByAddingChild(obj);
        }
        return treePath;
    }

    /**
     * Get the object name of a tree node. If the given node of the tree is a MapEntry, its key is
     * used as node identifier; if it is an ArrayEntry, then its index is used as identifier.
     *
     * @param treeNode node of a tree.
     * @return the name of the node.
     * @throws IllegalArgumentException if there is an unknown treeNode type.
     */
    private String getObjectName(final Object treeNode)
    {
        if (treeNode instanceof MapEntry)
        {
            final MapEntry entry = (MapEntry) treeNode;
            final COSName key = entry.getKey();
            return key.getName();
        }
        else if (treeNode instanceof ArrayEntry)
        {
            final ArrayEntry entry = (ArrayEntry) treeNode;
            return "[" + entry.getIndex() + "]";
        }
        else if (treeNode instanceof PageEntry)
        {
            final PageEntry entry = (PageEntry) treeNode;
            return entry.getPath();
        }
        throw new IllegalArgumentException("Unknown treeNode type: " + treeNode.getClass().getName());
    }

    /**
     * Parses a string and lists all the nodes.
     *
     * @param path a tree path.
     * @return a list of nodes, or null if there is an empty node.
     */
    private List<String> parsePathString(final String path)
    {
        final List<String> nodes = new ArrayList<>();
        for (String node : path.split("/"))
        {
            node = node.trim();
            if (node.startsWith("["))
            {
                node = node.replace("]", "").replace("[", "");
            }
            node = node.trim();
            if (node.isEmpty())
            {
                return null;
            }
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * An object is searched in the tree structure using the identifiers parsed earlier step.
     * @param obj
     * @param searchStr
     * @return
     */
    private Object searchNode(Object obj, final String searchStr)
    {
        if (obj instanceof MapEntry)
        {
            obj = ((MapEntry) obj).getValue();
        }
        else if (obj instanceof ArrayEntry)
        {
            obj = ((ArrayEntry) obj).getValue();
        }
        if (obj instanceof COSObject)
        {
            obj = ((COSObject) obj).getObject();
        }
        if (obj instanceof COSDictionary)
        {
            final COSDictionary dic = (COSDictionary) obj;
            if (dic.containsKey(searchStr))
            {
                final MapEntry entry = new MapEntry();
                entry.setKey(COSName.getPDFName(searchStr));
                entry.setValue(dic.getDictionaryObject(searchStr));
                entry.setValue(dic.getItem(searchStr));
                return entry;
            }
        }
        else if (obj instanceof COSArray)
        {
            final int index = Integer.parseInt(searchStr);
            final COSArray array = (COSArray) obj;
            if (index <= array.size() - 1)
            {
                final ArrayEntry entry = new ArrayEntry();
                entry.setIndex(index);
                entry.setValue(array.getObject(index));
                entry.setItem(array.get(index));
                return entry;
            }
        }
        return null;
    }
}
