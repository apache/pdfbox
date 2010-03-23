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
import java.util.Hashtable;
import java.util.Map;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSDictionaryMap;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;

/**
 * A root of a structure tree.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>,
 *  <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: 1.2 $
 */
public class PDStructureTreeRoot extends PDStructureNode
{

    public static final String TYPE = "StructTreeRoot";


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
    public PDStructureTreeRoot( COSDictionary dic )
    {
        super(dic);
    }


    /**
     * Returns the ID tree.
     * 
     * @return the ID tree
     */
    public PDNameTreeNode getIDTree()
    {
        COSDictionary idTreeDic = (COSDictionary) this.getCOSDictionary()
            .getDictionaryObject(COSName.ID_TREE);
        if (idTreeDic != null)
        {
            return new PDNameTreeNode(idTreeDic, PDStructureElement.class);
        }
        return null;
    }

    /**
     * Sets the ID tree.
     * 
     * @param idTree the ID tree
     */
    public void setIDTree(PDNameTreeNode idTree)
    {
        this.getCOSDictionary().setItem(COSName.ID_TREE, idTree);
    }

    /**
     * Returns the next key in the parent tree.
     * 
     * @return the next key in the parent tree
     */
    public int getParentTreeNextKey()
    {
        return this.getCOSDictionary().getInt(COSName.PARENT_TREE_NEXT_KEY);
    }

    /**
     * Returns the role map.
     * 
     * @return the role map
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getRoleMap()
    {
        COSBase rm = this.getCOSDictionary().getDictionaryObject(COSName.ROLE_MAP);
        if (rm instanceof COSDictionary)
        {
            try
            {
                return COSDictionaryMap.convertBasicTypesToMap((COSDictionary) rm);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return new Hashtable<String, String>();
    }

    /**
     * Sets the role map.
     * 
     * @param roleMap the role map
     */
    public void setRoleMap(Map<String, String> roleMap)
    {
        COSDictionary rmDic = new COSDictionary();
        for (String key : roleMap.keySet())
        {
            rmDic.setName(key, roleMap.get(key));
        }
        this.getCOSDictionary().setItem(COSName.ROLE_MAP, rmDic);
    }

}
