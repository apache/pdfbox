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
    private ICOSParser parser;
    private boolean isDereferenced = false;
    private final COSUpdateState updateState;
    
    private static final Log LOG = LogFactory.getLog(COSObject.class);

    /**
     * Constructor.
     *
     * @param object The object that this encapsulates.
     *
     */
    public COSObject(COSBase object)
    {
        updateState = new COSUpdateState(this);
        baseObject = object;
        isDereferenced = true;
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
        isDereferenced = true;
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
        updateState = new COSUpdateState(this);
        baseObject = object;
        isDereferenced = object != null;
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
        updateState = new COSUpdateState(this);
        this.parser = parser;
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
                getUpdateState().dereferenceChild(baseObject);
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
        if(baseObject != null)
        {
            getUpdateState().update();
        }
        baseObject = COSNull.NULL;
        parser = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "COSObject{" + getKey() + "}";
    }

    /**
     * Getter for property objectNumber.
     * 
     * @return Value of property objectNumber.
     * 
     * @deprecated will be removed in 4.0.0
     */
    @Deprecated
    public long getObjectNumber()
    {
        return getKey() != null ? getKey().getNumber() : 0;
    }

    /**
     * Getter for property generationNumber.
     * 
     * @return Value of property generationNumber.
     * 
     * @deprecated will be removed in 4.0.0
     */
    @Deprecated
    public int getGenerationNumber()
    {
        return getKey() != null ? getKey().getGeneration() : 0;
    }

    /**
     * visitor pattern double dispatch method.
     *
     * @param visitor The object to notify when visiting this object.
     * @throws IOException If an error occurs while visiting this object.
     */
    @Override
    public void accept( ICOSVisitor visitor ) throws IOException
    {
        COSBase object = getObject();
        if (object != null)
        {
            object.accept(visitor);
        }
        else
        {
            COSNull.NULL.accept(visitor);
        }
    }

    /**
     * Returns {@code true}, if the hereby referenced {@link COSBase} has already been parsed and loaded.
     *
     * @return {@code true}, if the hereby referenced {@link COSBase} has already been parsed and loaded.
     */
    public boolean isDereferenced()
    {
        return isDereferenced;
    }
    
    /**
     * Returns the current {@link COSUpdateState} of this {@link COSObject}.
     *
     * @return The current {@link COSUpdateState} of this {@link COSObject}.
     * @see COSUpdateState
     */
    @Override
    public COSUpdateState getUpdateState()
    {
        return updateState;
    }
    
}
