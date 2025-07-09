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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * A node in the structure tree.
 * 
 * @author Johannes Koch
 */
public abstract class PDStructureNode implements COSObjectable
{

    /**
     * Constructor.
     *
     * @param type the type
     */
    protected PDStructureNode(String type)
    {
        this.dictionary = new COSDictionary();
        this.dictionary.setName(COSName.TYPE, type);
    }

    /**
     * Constructor for an existing structure node.
     *
     * @param dictionary The existing dictionary.
     */
    protected PDStructureNode(COSDictionary dictionary)
    {
        this.dictionary = dictionary;
    }

    /**
     * Creates a node in the structure tree. Can be either a structure tree root,
     *  or a structure element.
     * 
     * @param node the node dictionary
     * @return the structure node
     */
    public static PDStructureNode create(COSDictionary node)
    {
        String type = node.getNameAsString(COSName.TYPE);
        if ("StructTreeRoot".equals(type))
        {
            return new PDStructureTreeRoot(node);
        }
        if (type == null || "StructElem".equals(type))
        {
            return new PDStructureElement(node);
        }
        throw new IllegalArgumentException("Dictionary must not include a Type entry with a value that is neither StructTreeRoot nor StructElem.");
    }


    private final COSDictionary dictionary;

    /**
     * {@inheritDoc}
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return dictionary;
    }

    /**
     * Returns the type.
     * 
     * @return the type
     */
    public String getType()
    {
        return this.getCOSObject().getNameAsString(COSName.TYPE);
    }

    /**
     * Returns a list of objects for the kids (K).
     * 
     * @return a list of objects for the kids, never null.
     */
    public List<Object> getKids()
    {
        List<Object> kidObjects = new ArrayList<>();
        COSBase k = this.getCOSObject().getDictionaryObject(COSName.K);
        if (k instanceof COSArray)
        {
            for (COSBase kid : (COSArray) k)
            {
                Object kidObject = this.createObject(kid);
                if (kidObject != null)
                {
                    kidObjects.add(kidObject);
                }
            }
        }
        else
        {
            Object kidObject = this.createObject(k);
            if (kidObject != null)
            {
                kidObjects.add(kidObject);
            }
        }
        return kidObjects;
    }

    /**
     * Sets the kids (K).
     * 
     * @param kids the kids
     */
    public void setKids(List<Object> kids)
    {
        this.getCOSObject().setItem(COSName.K,
            COSArrayList.converterToCOSArray(kids));
    }

    /**
     * Appends a structure element kid and sets the parent property (/P) to this.
     * 
     * @param structureElement the structure element
     */
    public void appendKid(PDStructureElement structureElement)
    {
        this.appendObjectableKid(structureElement);
        structureElement.setParent(this);
    }

    /**
     * Appends an objectable kid.
     * 
     * @param objectable the objectable
     */
    protected void appendObjectableKid(COSObjectable objectable)
    {
        if (objectable == null)
        {
            return;
        }
        this.appendKid(objectable.getCOSObject());
    }

    /**
     * Appends a COS base kid.
     * 
     * @param object the COS base
     */
    protected void appendKid(COSBase object)
    {
        if (object == null)
        {
            return;
        }
        COSBase k = this.getCOSObject().getDictionaryObject(COSName.K);
        if (k == null)
        {
            // currently no kid: set new kid as kids
            this.getCOSObject().setItem(COSName.K, object);
        }
        else if (k instanceof COSArray)
        {
            // currently more than one kid: add new kid to existing array
            COSArray array = (COSArray) k;
            array.add(object);
        }
        else
        {
            // currently one kid: put current and new kid into array and set array as kids
            COSArray array = new COSArray(Arrays.asList(k, object));
            this.getCOSObject().setItem(COSName.K, array);
        }
    }

    /**
     * Inserts a structure element kid before a reference kid.
     * 
     * @param newKid the structure element
     * @param refKid the reference kid
     */
    public void insertBefore(PDStructureElement newKid, Object refKid)
    {
        this.insertObjectableBefore(newKid, refKid);
    }

    /**
     * Inserts an objectable kid before a reference kid.
     * 
     * @param newKid the objectable
     * @param refKid the reference kid
     */
    protected void insertObjectableBefore(COSObjectable newKid, Object refKid)
    {
        if (newKid == null)
        {
            return;
        }
        this.insertBefore(newKid.getCOSObject(), refKid);
    }

    /**
     * Inserts an COS base kid before a reference kid.
     * 
     * @param newKid the COS base
     * @param refKid the reference kid
     */
    protected void insertBefore(COSBase newKid, Object refKid)
    {
        if (newKid == null || refKid == null)
        {
            return;
        }
        COSBase k = this.getCOSObject().getDictionaryObject(COSName.K);
        if (k == null)
        {
            return;
        }
        COSBase refKidBase = null;
        if (refKid instanceof COSObjectable)
        {
            refKidBase = ((COSObjectable) refKid).getCOSObject();
        }
        if (k instanceof COSArray)
        {
            COSArray array = (COSArray) k;
            int refIndex = array.indexOfObject(refKidBase);
            array.add(refIndex, newKid.getCOSObject());
        }
        else
        {
            boolean onlyKid = k.equals(refKidBase);
            if (!onlyKid && (k instanceof COSObject))
            {
                COSBase kObj = ((COSObject) k).getObject();
                onlyKid = kObj.equals(refKidBase);
            }
            if (onlyKid)
            {
                COSArray array = new COSArray(Arrays.asList(newKid, refKidBase));
                this.getCOSObject().setItem(COSName.K, array);
            }
        }
    }

    /**
     * Removes a structure element kid and if successful also removes the parent property (/P).
     *
     * @param structureElement the structure element
     * @return <code>true</code> if the kid was removed, <code>false</code> otherwise.
     */
    public boolean removeKid(PDStructureElement structureElement)
    {
        boolean removed = this.removeObjectableKid(structureElement);
        if (removed)
        {
            structureElement.setParent(null);
        }
        return removed;
    }

    /**
     * Removes an objectable kid.
     * 
     * @param objectable the objectable
     * @return <code>true</code> if the kid was removed, <code>false</code> otherwise
     */
    protected boolean removeObjectableKid(COSObjectable objectable)
    {
        if (objectable == null)
        {
            return false;
        }
        return this.removeKid(objectable.getCOSObject());
    }

    /**
     * Removes a COS base kid.
     * 
     * @param object the COS base
     * @return <code>true</code> if the kid was removed, <code>false</code> otherwise
     */
    protected boolean removeKid(COSBase object)
    {
        if (object == null)
        {
            return false;
        }
        COSBase k = this.getCOSObject().getDictionaryObject(COSName.K);
        if (k == null)
        {
            // no kids: objectable is not a kid
            return false;
        }
        else if (k instanceof COSArray)
        {
            // currently more than one kid: remove kid from existing array
            COSArray array = (COSArray) k;
            boolean removed = array.removeObject(object);
            // if now only one kid: set remaining kid as kids
            if (array.size() == 1)
            {
                this.getCOSObject().setItem(COSName.K, array.getObject(0));
            }
            return removed;
        }
        else
        {
            // currently one kid: if current kid equals given object, remove kids entry
            boolean onlyKid = k.equals(object);
            if (!onlyKid && (k instanceof COSObject))
            {
                COSBase kObj = ((COSObject) k).getObject();
                onlyKid = kObj.equals(object);
            }
            if (onlyKid)
            {
                this.getCOSObject().setItem(COSName.K, null);
                return true;
            }
            return false;
        }
    }

    /**
     * Creates an object for a kid of this structure node.
     * The type of object depends on the type of the kid. It can be
     * <ul>
     * <li>a {@link PDStructureElement},</li>
     * <li>a {@link PDObjectReference},</li>
     * <li>a {@link PDMarkedContentReference},</li>
     * <li>an {@link Integer}</li>
     * </ul>
     * 
     * @param kid the kid
     * @return the object
     */
    protected Object createObject(COSBase kid)
    {
        COSDictionary kidDic = null;
        if (kid instanceof COSDictionary)
        {
            kidDic = (COSDictionary) kid;
        }
        else if (kid instanceof COSObject)
        {
            COSBase base = ((COSObject) kid).getObject();
            if (base instanceof COSDictionary)
            {
                kidDic = (COSDictionary) base;
            }
        }
        if (kidDic != null)
        {
            return createObjectFromDic(kidDic);
        }
        else if (kid instanceof COSInteger)
        {
            // An integer marked-content identifier denoting a marked-content sequence
            COSInteger mcid = (COSInteger) kid;
            return mcid.intValue();
        }
        return null;
    }

    private COSObjectable createObjectFromDic(COSDictionary kidDic)
    {
        String type = kidDic.getNameAsString(COSName.TYPE);
        if (type == null)
        {
            // A structure element dictionary denoting another structure element
            return new PDStructureElement(kidDic);
        }
        switch (type)
        {
            case PDStructureElement.TYPE:
                // A structure element dictionary denoting another structure element
                return new PDStructureElement(kidDic);
            case PDObjectReference.TYPE:
                // An object reference dictionary denoting a PDF object
                return new PDObjectReference(kidDic);
            case PDMarkedContentReference.TYPE:
                // A marked-content reference dictionary denoting a marked-content sequence
                return new PDMarkedContentReference(kidDic);
            default:
                return null;
        }
    }
}
