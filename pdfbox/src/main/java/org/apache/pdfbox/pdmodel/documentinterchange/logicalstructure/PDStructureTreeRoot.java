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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.PDStructureElementNameTreeNode;
import org.apache.pdfbox.pdmodel.common.COSDictionaryMap;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;

/**
 * A root of a structure tree.
 * 
 * @author Ben Litchfield
 * @author Johannes Koch
 * 
 */
public class PDStructureTreeRoot extends PDStructureNode
{

    /**
     * Log instance.
     */
    private static final Logger LOG = LogManager.getLogger(PDStructureTreeRoot.class);

    private static final String TYPE = "StructTreeRoot";

    /**
     * Default Constructor.
     * 
     */
    public PDStructureTreeRoot()
    {
        super(TYPE);
    }

    /**
     * Constructor for an existing structure element.
     * 
     * @param dic The existing dictionary.
     */
    public PDStructureTreeRoot(COSDictionary dic)
    {
        super(dic);
    }

    /**
     * Returns the K entry. This can be a dictionary representing a structure element, or an array
     * of them. To get it as a list of PDStructureElement objects, use {@link #getKids()} instead.
     *
     * @return the K entry.
     */
    public COSBase getK()
    {
        return this.getCOSObject().getDictionaryObject(COSName.K);
    }

    /**
     * Sets the K entry.
     * 
     * @param k the K value
     */
    public void setK(COSBase k)
    {
        this.getCOSObject().setItem(COSName.K, k);
    }

    /**
     * Returns the ID tree.
     * 
     * @return the ID tree
     */
    public PDNameTreeNode<PDStructureElement> getIDTree()
    {
        COSDictionary idTree = getCOSObject().getCOSDictionary(COSName.ID_TREE);
        return idTree != null ? new PDStructureElementNameTreeNode(idTree) : null;
    }

    /**
     * Sets the ID tree.
     * 
     * @param idTree the ID tree
     */
    public void setIDTree(PDNameTreeNode<PDStructureElement> idTree)
    {
        this.getCOSObject().setItem(COSName.ID_TREE, idTree);
    }

    /**
     * Returns the parent tree.<p>
     * The keys correspond to a single page of the document or to an individual object, e.g. an
     * annotation or an XObject, which have a <b>/StructParent</b> or <b>/StructParents</b>
     * entry.<p>
     * The values of type {@link PDParentTreeValue} are either a dictionary or an array. It's a
     * dictionary for individual objects like an annotation or an XObject, and an array for a page
     * object or a content stream containing marked-content sequences identified by an MCID.
     *
     * @return the parent tree.
     */
    public PDNumberTreeNode getParentTree()
    {
        COSDictionary parentTree = getCOSObject().getCOSDictionary(COSName.PARENT_TREE);
        return parentTree != null ? new PDNumberTreeNode(parentTree, PDParentTreeValue.class) : null;
    }

    /**
     * Sets the parent tree.<p>
     * The keys correspond to a single page of the document or to an individual object, e.g. an
     * annotation or an XObject, which have a <b>/StructParent</b> or <b>/StructParents</b>
     * entry.<p>
     * The values of type {@link PDParentTreeValue} are either a dictionary or an array. It's a
     * dictionary for individual objects like an annotation or an XObject, and an array for a page
     * object or a content stream containing marked-content sequences identified by an MCID.
     * <p>
     * To create an empty parent tree, call {@code new PDNumberTreeNode(PDParentTreeValue.class)}.
     *
     * @param parentTree the parent tree
     */
    public void setParentTree(PDNumberTreeNode parentTree)
    {
        this.getCOSObject().setItem(COSName.PARENT_TREE, parentTree);
    }

    /**
     * Returns The next key for the parent tree. This is a number greater than any existing key, and
     * which shall be used for the next entry to be added to the tree.
     *
     * @return The next key for the parent tree
     */
    public int getParentTreeNextKey()
    {
        return this.getCOSObject().getInt(COSName.PARENT_TREE_NEXT_KEY);
    }

    /**
     * Sets the next key in the parent tree. This is a number greater than any existing key, and
     * which shall be used for the next entry to be added to the tree.
     * 
     * @param parentTreeNextkey The next key in the parent tree.
     */
    public void setParentTreeNextKey(int parentTreeNextkey)
    {
        this.getCOSObject().setInt(COSName.PARENT_TREE_NEXT_KEY, parentTreeNextkey);
    }

    /**
     * Returns the role map.
     * 
     * @return the role map
     */
    public Map<String, Object> getRoleMap()
    {
        COSDictionary rm = getCOSObject().getCOSDictionary(COSName.ROLE_MAP);
        if (rm != null)
        {
            try
            {
                return COSDictionaryMap.convertBasicTypesToMap(rm);
            }
            catch (IOException e)
            {
                LOG.error(e,e);
            }
        }
        return new HashMap<>();
    }

    /**
     * Sets the role map.
     * 
     * @param roleMap the role map
     */
    public void setRoleMap(Map<String, String> roleMap)
    {
        COSDictionary rmDic = new COSDictionary();
        roleMap.forEach(rmDic::setName);
        this.getCOSObject().setItem(COSName.ROLE_MAP, rmDic);
    }

    /**
     * Sets the ClassMap.
     * 
     * @return the ClassMap, never null. The elements are either {@link PDAttributeObject} or lists
     * of it.
     */
    public Map<String, Object> getClassMap()
    {
        Map<String, Object> classMap = new HashMap<>();
        COSDictionary classMapDictionary = this.getCOSObject().getCOSDictionary(COSName.CLASS_MAP);
        if (classMapDictionary == null)
        {
            return classMap;
        }
        classMapDictionary.forEach((name, base) ->
        {
            if (base instanceof COSObject)
            {
                base = ((COSObject) base).getObject();
            }
            if (base instanceof COSDictionary)
            {
                classMap.put(name.getName(), PDAttributeObject.create((COSDictionary) base));
            }
            else if (base instanceof COSArray)
            {
                COSArray array = (COSArray) base;
                List<PDAttributeObject> list = new ArrayList<>();
                for (int i = 0; i < array.size(); ++i)
                {
                    COSBase base2 = array.getObject(i);
                    if (base2 instanceof COSDictionary)
                    {
                        list.add(PDAttributeObject.create((COSDictionary) base2));
                    }
                }
                classMap.put(name.getName(), list);
            }
        });
        return classMap;
    }

    /**
     * Sets the ClassMap.
     * 
     * @param classMap null, or a map whose elements are either {@link PDAttributeObject} or lists
     * of it.
     */
    public void setClassMap(Map<String, Object> classMap)
    {
        if (classMap == null || classMap.isEmpty())
        {
            this.getCOSObject().removeItem(COSName.CLASS_MAP);
            return;
        }
        COSDictionary classMapDictionary = new COSDictionary();
        classMap.forEach((name, object) ->
        {
            if (object instanceof PDAttributeObject)
            {
                classMapDictionary.setItem(name, ((PDAttributeObject) object).getCOSObject());
            }
            else if (object instanceof List)
            {
                classMapDictionary.setItem(name, new COSArray((List<PDAttributeObject>) object));
            }
        });
        this.getCOSObject().setItem(COSName.CLASS_MAP, classMapDictionary);        
    }
}
