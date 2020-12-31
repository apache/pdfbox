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
package org.apache.pdfbox.cos;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a PDF object.
 *
 * @author Ben Litchfield
 * 
 */
public class COSObject extends COSBase implements COSUpdateInfo
{
    private COSBase baseObject;
    private long objectNumber;
    private int generationNumber;
    private boolean needToBeUpdated;
    private ICOSParser parser;
    private boolean isDereferenced = false;

    private static final Log LOG = LogFactory.getLog(COSObject.class);

    /**
     * Constructor.
     *
     * @param object The object that this encapsulates.
     *
     */
    public COSObject(COSBase object)
    {
        baseObject = object;
    }

    /**
     * Constructor.
     *
     * @param object The object that this encapsulates.
     * @param objectKey The COSObjectKey of the encapsulated object
     */
    public COSObject(COSBase object, COSObjectKey objectKey)
    {
        this(objectKey, null);
        baseObject = object;
    }

    /**
     * Constructor.
     *
     * @param object The object that this encapsulates.
     * @param parser The parser to be used to load the object on demand
     *
     */
    public COSObject(COSBase object, ICOSParser parser)
    {
        baseObject = object;
        this.parser = parser;
    }

    /**
     * Constructor.
     *
     * @param key The object number of the encapsulated object.
     * @param parser The parser to be used to load the object on demand
     *
     */
    public COSObject(COSObjectKey key, ICOSParser parser)
    {
        this.parser = parser;
        objectNumber = key.getNumber();
        generationNumber = key.getGeneration();
        setKey(key);
    }

    /**
     * Indicates if the referenced object is present or not.
     * 
     * @return true if the indirect object is dereferenced
     */
    public boolean isObjectNull()
    {
        return baseObject == null;
    }

    /**
     * This will get the object that this object encapsulates.
     *
     * @return The encapsulated object.
     */
    public COSBase getObject()
    {
        if (!isDereferenced && parser != null)
        {
            try
            {
                // mark as dereferenced to avoid endless recursions
                isDereferenced = true;
                baseObject = parser.dereferenceCOSObject(this);
            }
            catch (IOException e)
            {
                LOG.error("Can't dereference " + this, e);
            }
            finally
            {
                parser = null;
            }
        }
        return baseObject;
    }

    /**
     * Sets the referenced object to COSNull and removes the initially assigned parser.
     */
    public final void setToNull()
    {
        baseObject = COSNull.NULL;
        parser = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "COSObject{" + Long.toString(objectNumber) + ", " + Integer.toString(generationNumber) + "}";
    }

    /** 
     * Getter for property objectNumber.
     * @return Value of property objectNumber.
     */
    public long getObjectNumber()
    {
        return objectNumber;
    }

    /** 
     * Getter for property generationNumber.
     * @return Value of property generationNumber.
     */
    public int getGenerationNumber()
    {
        return generationNumber;
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @return any object, depending on the visitor implementation, or null
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public Object accept( ICOSVisitor visitor ) throws IOException
    {
        return getObject() != null ? getObject().accept( visitor ) : COSNull.NULL.accept( visitor );
    }
    
    /**
     * Get the update state for the COSWriter.
     * 
     * @return the update state.
     */
    @Override
    public boolean isNeedToBeUpdated() 
    {
        return needToBeUpdated;
    }
    
    /**
     * Set the update state of the dictionary for the COSWriter.
     * 
     * @param flag the update state.
     */
    @Override
    public void setNeedToBeUpdated(boolean flag) 
    {
        needToBeUpdated = flag;
    }

}
