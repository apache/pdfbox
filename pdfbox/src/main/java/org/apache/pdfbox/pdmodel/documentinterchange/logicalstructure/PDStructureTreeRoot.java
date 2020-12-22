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
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
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
    private static final Log LOG = LogFactory.getLog(PDStructureTreeRoot.class);

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
    public PDStructureTreeRoot(final COSDictionary dic)
    {
        super(dic);
    }

    /**
     * Returns the K entry. This can be a dictionary representing a structure element, or an array
     * of them.
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
    public void setK(final COSBase k)
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
        final COSBase base = this.getCOSObject().getDictionaryObject(COSName.ID_TREE);
        if (base instanceof COSDictionary)
        {
            return new PDStructureElementNameTreeNode((COSDictionary) base);
        }
        return null;
    }

    /**
     * Sets the ID tree.
     * 
     * @param idTree the ID tree
     */
    public void setIDTree(final PDNameTreeNode<PDStructureElement> idTree)
    {
        this.getCOSObject().setItem(COSName.ID_TREE, idTree);
    }

    /**
     * Returns the parent tree.
     * 
     * @return the parent tree
     */
    public PDNumberTreeNode getParentTree()
    {
        final COSBase base = getCOSObject().getDictionaryObject(COSName.PARENT_TREE);
        if (base instanceof COSDictionary)
        {
            return new PDNumberTreeNode((COSDictionary) base, PDParentTreeValue.class);
        }
        return null;
    }

    /**
     * Sets the parent tree.
     * 
     * @param parentTree the parent tree
     */
    public void setParentTree(final PDNumberTreeNode parentTree)
    {
        this.getCOSObject().setItem(COSName.PARENT_TREE, parentTree);
    }

    /**
     * Returns the next key in the parent tree.
     * 
     * @return the next key in the parent tree
     */
    public int getParentTreeNextKey()
    {
        return this.getCOSObject().getInt(COSName.PARENT_TREE_NEXT_KEY);
    }

    /**
     * Sets the next key in the parent tree.
     * 
     * @param parentTreeNextkey the next key in the parent tree.
     */
    public void setParentTreeNextKey(final int parentTreeNextkey)
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
        final COSBase rm = this.getCOSObject().getDictionaryObject(COSName.ROLE_MAP);
        if (rm instanceof COSDictionary)
        {
            try
            {
                return COSDictionaryMap.convertBasicTypesToMap((COSDictionary) rm);
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
    public void setRoleMap(final Map<String, String> roleMap)
    {
        final COSDictionary rmDic = new COSDictionary();
        roleMap.forEach(rmDic::setName);
        this.getCOSObject().setItem(COSName.ROLE_MAP, rmDic);
    }

}
