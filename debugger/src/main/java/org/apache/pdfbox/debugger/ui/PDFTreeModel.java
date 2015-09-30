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
package org.apache.pdfbox.debugger.ui;

/**
 * A tree model that uses a cos document.
 *
 *
 * @author wurtz
 * @author Ben Litchfield
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A class to model a PDF document as a tree structure.
 *
 * @author Ben Litchfield
 */
public class PDFTreeModel implements TreeModel
{
    private Object root;

    /**
     * constructor.
     */
    public PDFTreeModel()
    {
        //default constructor
    }

    /**
     * Constructor to take a document.
     *
     * @param doc The document to display in the tree.
     */
    public PDFTreeModel(PDDocument doc)
    {
        root = doc.getDocument().getTrailer();
    }

    /**
     * Constructor to take a document.
     *
     * @param docEntry The document to display in the tree.
     */
    public PDFTreeModel(DocumentEntry docEntry)
    {
        root = docEntry;
    }

    /**
     * Adds a listener for the <code>TreeModelEvent</code>
     * posted after the tree changes.
     *
     * @param   l       the listener to add
     * @see     #removeTreeModelListener
     *
     */
    @Override
    public void addTreeModelListener(TreeModelListener l)
    {
        //required for interface
    }

    /**
     * Returns the child of <code>parent</code> at index <code>index</code> in the parent's child
     * array. <code>parent</code> must be a node previously obtained from this data source. This
     * should not return <code>null</code> if <code>index</code> is a valid index for
     * <code>parent</code> (that is <code>index &gt;= 0 &&
     * index &lt; getChildCount(parent</code>)).
     *
     * @param parent a node in the tree, obtained from this data source
     * @param index The index into the parent object to location the child object.
     * @return the child of <code>parent</code> at index <code>index</code>
     * @throws IllegalArgumentException if an unknown unknown COS type is passed as parent
     * parameter.
     */
    @Override
    public Object getChild(Object parent, int index)
    {
        Object retval = null;
        if( parent instanceof COSArray )
        {
            ArrayEntry entry = new ArrayEntry();
            entry.setIndex( index );
            entry.setValue( ((COSArray)parent).getObject(index) );
            entry.setItem(((COSArray) parent).get(index));
            retval = entry;
        }
        else if( parent instanceof COSDictionary )
        {
            COSDictionary dict = (COSDictionary)parent;
            List<COSName> keys = new ArrayList<COSName>(dict.keySet());
            Collections.sort( keys );
            COSName key = keys.get( index );
            COSBase value = dict.getDictionaryObject(key);
            MapEntry entry = new MapEntry();
            entry.setKey( key );
            entry.setValue( value );
            entry.setItem(dict.getItem(key));
            retval = entry;
        }
        else if( parent instanceof MapEntry )
        {
            retval = getChild( ((MapEntry)parent).getValue(), index );
        }
        else if( parent instanceof ArrayEntry )
        {
            retval = getChild( ((ArrayEntry)parent).getValue(), index );
        }
        else if( parent instanceof COSDocument )
        {
            retval = ((COSDocument)parent).getObjects().get( index );
        }
        else if( parent instanceof DocumentEntry)
        {
            retval = ((DocumentEntry)parent).getPage(index);
        }
        else if( parent instanceof PageEntry)
        {
            retval = getChild(((PageEntry)parent).getDict(), index);
        }
        else if( parent instanceof COSObject )
        {
            retval = ((COSObject)parent).getObject();
        }
        else
        {
            throw new IllegalArgumentException("Unknown COS type " + parent.getClass().getName());
        }
        return retval;
    }

    /** Returns the number of children of <code>parent</code>.
     * Returns 0 if the node
     * is a leaf or if it has no children.  <code>parent</code> must be a node
     * previously obtained from this data source.
     *
     * @param   parent  a node in the tree, obtained from this data source
     * @return  the number of children of the node <code>parent</code>
     *
     */
    @Override
    public int getChildCount(Object parent)
    {
        int retval = 0;
        if( parent instanceof COSArray )
        {
            retval = ((COSArray)parent).size();
        }
        else if( parent instanceof COSDictionary )
        {
            retval = ((COSDictionary)parent).size();
        }
        else if( parent instanceof MapEntry )
        {
            retval = getChildCount(((MapEntry) parent).getValue());
        }
        else if( parent instanceof ArrayEntry )
        {
            retval = getChildCount(((ArrayEntry) parent).getValue());
        }
        else if( parent instanceof COSDocument )
        {
            retval = ((COSDocument)parent).getObjects().size();
        }
        else if( parent instanceof DocumentEntry )
        {
            retval = ((DocumentEntry)parent).getPageCount();
        }
        else if( parent instanceof PageEntry)
        {
            retval = getChildCount(((PageEntry) parent).getDict());
        }
        else if( parent instanceof COSObject )
        {
            retval = 1;
        }
        return retval;
    }

    /**
     * Returns the index of child in parent. If <code>parent</code> is <code>null</code> or
     * <code>child</code> is <code>null</code>, returns -1.
     *
     * @param parent a node in the tree, obtained from this data source
     * @param child the node we are interested in
     * @return the index of the child in the parent, or -1 if either <code>child</code> or
     * <code>parent</code> are <code>null</code>
     * @throws IllegalArgumentException if an unknown unknown COS type is passed as parent parameter.
     */
    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        int retval = -1;
        if( parent != null && child != null )
        {
            if( parent instanceof COSArray )
            {
                COSArray array = (COSArray)parent;
                if( child instanceof ArrayEntry )
                {
                    ArrayEntry arrayEntry = (ArrayEntry)child;
                    retval = arrayEntry.getIndex();
                }
                else
                {
                    retval = array.indexOf( (COSBase)child );
                }
            }
            else if( parent instanceof COSDictionary )
            {
                MapEntry entry = (MapEntry)child;
                COSDictionary dict = (COSDictionary)parent;
                List<COSName> keys = new ArrayList<COSName>(dict.keySet());
                Collections.sort( keys );
                for( int i=0; retval == -1 && i<keys.size(); i++ )
                {
                    if( keys.get( i ).equals( entry.getKey() ) )
                    {
                        retval = i;
                    }
                }
            }
            else if( parent instanceof MapEntry )
            {
                retval = getIndexOfChild( ((MapEntry)parent).getValue(), child );
            }
            else if( parent instanceof ArrayEntry )
            {
                retval = getIndexOfChild( ((ArrayEntry)parent).getValue(), child );
            }
            else if( parent instanceof COSDocument )
            {
                retval = ((COSDocument)parent).getObjects().indexOf( child );
            }
            else if( parent instanceof DocumentEntry )
            {
                retval = ((DocumentEntry)parent).indexOf( (PageEntry)child );
            }
            else if( parent instanceof PageEntry)
            {
                retval = getIndexOfChild(((PageEntry)parent).getDict(), child);
            }
            else if( parent instanceof COSObject )
            {
                retval = 0;
            }
            else
            {
                throw new IllegalArgumentException("Unknown COS type " + parent.getClass().getName());
            }
        }
        return retval;
    }

    /** Returns the root of the tree.  Returns <code>null</code>
     * only if the tree has no nodes.
     *
     * @return  the root of the tree
     *
     */
    @Override
    public Object getRoot()
    {
        return root;
    }

    /** Returns <code>true</code> if <code>node</code> is a leaf.
     * It is possible for this method to return <code>false</code>
     * even if <code>node</code> has no children.
     * A directory in a filesystem, for example,
     * may contain no files; the node representing
     * the directory is not a leaf, but it also has no children.
     *
     * @param   node  a node in the tree, obtained from this data source
     * @return  true if <code>node</code> is a leaf
     *
     */
    @Override
    public boolean isLeaf(Object node)
    {
        boolean isLeaf = !(node instanceof COSDictionary ||
                 node instanceof COSArray ||
                 node instanceof COSDocument ||
                 node instanceof DocumentEntry ||
                 node instanceof PageEntry ||
                 node instanceof COSObject ||
                 (node instanceof MapEntry && !isLeaf(((MapEntry)node).getValue()) ) ||
                 (node instanceof ArrayEntry && !isLeaf(((ArrayEntry)node).getValue()) ));
        return isLeaf;
    }

    /** Removes a listener previously added with
     * <code>addTreeModelListener</code>.
     *
     * @see     #addTreeModelListener
     * @param   l       the listener to remove
     *
     */

    @Override
    public void removeTreeModelListener(TreeModelListener l)
    {
        //required for interface
    }

    /** Messaged when the user has altered the value for the item identified
     * by <code>path</code> to <code>newValue</code>.
     * If <code>newValue</code> signifies a truly new value
     * the model should post a <code>treeNodesChanged</code> event.
     *
     * @param path path to the node that the user has altered
     * @param newValue the new value from the TreeCellEditor
     *
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        //required for interface
    }
}
