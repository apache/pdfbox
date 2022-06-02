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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A {@link COSIncrement} starts at a given {@link COSUpdateInfo} to collect updates, that have been made to a
 * {@link COSDocument} and therefore should be added to it´s next increment.
 *
 * @author Christian Appl
 * @see COSUpdateState
 * @see COSUpdateInfo
 */
public class COSIncrement implements Iterable<COSBase>
{
    
    /**
     * Contains the {@link COSBase}s, that shall be added to the increment at top level.
     */
    private final Set<COSBase> objects = new LinkedHashSet<>();
    /**
     * Contains the direct {@link COSBase}s, that are either contained written directly by structures contained in
     * {@link #objects} or that must be excluded from being written as indirect {@link COSObject}s for other reasons.
     */
    private final Set<COSBase> excluded = new HashSet<>();
    /**
     * Contains all {@link COSObject}s, that have already been processed by this {@link COSIncrement} and shall not be
     * processed again.
     */
    private final Set<COSObject> processedObjects = new HashSet<>();
    /**
     * Contains the {@link COSUpdateInfo} that this {@link COSIncrement} creates an increment for.
     */
    private final COSUpdateInfo incrementOrigin;
    /**
     * Whether this {@link COSIncrement} has already been determined, or must still be evaluated.
     */
    private boolean initialized = false;
    
    /**
     * Creates a new {@link COSIncrement} for the given {@link COSUpdateInfo}, the increment will use it´s
     * {@link COSDocumentState} as it´s own origin and shall collect all updates contained in the given
     * {@link COSUpdateInfo}.<br>
     * Should the given object be {@code null}, the resulting increment shall be empty.
     *
     * @param incrementOrigin The {@link COSUpdateInfo} serving as an update source for this {@link COSIncrement}.
     */
    public COSIncrement(COSUpdateInfo incrementOrigin)
    {
        this.incrementOrigin = incrementOrigin;
    }
    
    /**
     * Collect all updates made to the given {@link COSBase} and it's contained structures.<br>
     * This shall forward all {@link COSUpdateInfo} objects to the proper specialized collection methods.
     *
     * @param base The {@link COSBase} updates shall be collected for.
     * @return Returns {@code true}, if the {@link COSBase} represents a direct child structure, that would require it´s
     * parent to be updated instead.
     * @see #collect(COSDictionary)
     * @see #collect(COSArray)
     * @see #collect(COSObject)
     */
    private boolean collect(COSBase base)
    {
        if(contains(base))
        {
            return false;
        }
        // handle updatable objects:
        if(base instanceof COSDictionary)
        {
            return collect((COSDictionary) base);
        }
        else if(base instanceof COSObject)
        {
            return collect((COSObject) base);
        }
        else if(base instanceof COSArray)
        {
            return collect((COSArray) base);
        }
        return false;
    }
    
    /**
     * Collect all updates made to the given {@link COSDictionary} and it's contained structures.
     *
     * @param dictionary The {@link COSDictionary} updates shall be collected for.
     * @return Returns {@code true}, if the {@link COSDictionary} represents a direct child structure, that would
     * require it´s parent to be updated instead.
     */
    private boolean collect(COSDictionary dictionary)
    {
        COSUpdateState updateState = dictionary.getUpdateState();
        // Is definitely part of the increment?
        if(!isExcluded(dictionary) && !contains(dictionary) && updateState.isUpdated())
        {
            add(dictionary);
        }
        boolean childDemandsParentUpdate = false;
        // Collect children:
        for(COSBase entry : dictionary.getValues())
        {
            // Primitives can not be part of an increment. (on top level)
            if(!(entry instanceof COSUpdateInfo) || contains(entry))
            {
                continue;
            }
            COSUpdateInfo updatableEntry = (COSUpdateInfo) entry;
            COSUpdateState entryUpdateState = updatableEntry.getUpdateState();
            // Entries with different document origin must be part of the increment!
            updateDifferentOrigin(entryUpdateState);
            // Always attempt to write COSArrays as direct objects.
            if(updatableEntry.isNeedToBeUpdated() &&
                ((!(entry instanceof COSObject) && entry.isDirect()) || entry instanceof COSArray))
            {
                // Exclude direct entries from the increment!
                exclude(entry);
                childDemandsParentUpdate = true;
            }
            // Collect descendants:
            childDemandsParentUpdate = collect(entry) || childDemandsParentUpdate;
        }
        
        if(isExcluded(dictionary))
        {
            return childDemandsParentUpdate;
        }
        else
        {
            if(childDemandsParentUpdate && !contains(dictionary))
            {
                add(dictionary);
            }
            return false;
        }
    }
    
    /**
     * Collect all updates made to the given {@link COSArray} and it's contained structures.
     *
     * @param array The {@link COSDictionary} updates shall be collected for.
     * @return Returns {@code true}, if the {@link COSArray}´s elements changed. A {@link COSArray} shall always be
     * treated as a direct structure, that would require it´s parent to be updated instead.
     */
    private boolean collect(COSArray array)
    {
        COSUpdateState updateState = array.getUpdateState();
        boolean childDemandsParentUpdate = updateState.isUpdated();
        for(COSBase entry : array)
        {
            // Primitives can not be part of an increment. (on top level)
            if(!(entry instanceof COSUpdateInfo) || contains(entry))
            {
                continue;
            }
            COSUpdateState entryUpdateState = ((COSUpdateInfo) entry).getUpdateState();
            // Entries with different document origin must be part of the increment!
            updateDifferentOrigin(entryUpdateState);
            // Collect descendants:
            childDemandsParentUpdate = collect(entry) || childDemandsParentUpdate;
        }
        return childDemandsParentUpdate;
    }
    
    /**
     * Collect all updates made to the given {@link COSObject} and it's contained structures.
     *
     * @param object The {@link COSObject} updates shall be collected for.
     * @return Always returns {@code false}. {@link COSObject}s by definition are indirect and shall never cause a
     * parent structure to be updated.
     */
    private boolean collect(COSObject object)
    {
        if(contains(object))
        {
            return false;
        }
        addProcessedObject(object);
        COSUpdateState updateState = object.getUpdateState();
        // Objects with different document origin must be part of the increment!
        updateDifferentOrigin(updateState);
        // determine actual, if necessary or possible without dereferencing:
        COSUpdateInfo actual = null;
        if(updateState.isUpdated() || object.isDereferenced())
        {
            COSBase base = object.getObject();
            if(base instanceof COSUpdateInfo)
            {
                actual = (COSUpdateInfo) base;
            }
        }
        // Skip?
        if(actual == null || contains(actual.getCOSObject()))
        {
            return false;
        }
        boolean childDemandsParentUpdate = false;
        COSUpdateState actualUpdateState = actual.getUpdateState();
        if(actualUpdateState.isUpdated())
        {
            childDemandsParentUpdate = true;
        }
        exclude(actual.getCOSObject());
        childDemandsParentUpdate = collect(actual.getCOSObject()) || childDemandsParentUpdate;
        if(updateState.isUpdated() || childDemandsParentUpdate)
        {
            add(actual.getCOSObject());
        }
        return false;
    }
    
    /**
     * Returns {@code true}, if the given {@link COSBase} is already known to and has been processed by this
     * {@link COSIncrement}.
     *
     * @param base The {@link COSBase} to check.
     * @return {@code true}, if the given {@link COSBase} is already known to and has been processed by this
     * {@link COSIncrement}.
     * @see #objects
     * @see #processedObjects
     */
    public boolean contains(COSBase base)
    {
        return objects.contains(base) || (base instanceof COSObject && processedObjects.contains((COSObject) base));
    }
    
    /**
     * Check whether the given {@link COSUpdateState}´s {@link COSDocumentState} differs from the {@link COSIncrement}´s
     * known {@link #incrementOrigin}.<br>
     * Should that be the case, the {@link COSUpdateState} originates from another {@link COSDocument} and must be added
     * to the {@link COSIncrement}, hence call {@link COSUpdateState#update()}.
     *
     * @param updateState The {@link COSUpdateState} that shall be updated, if it's originating from another
     *                    {@link COSDocument}.
     * @see #incrementOrigin
     */
    private void updateDifferentOrigin(COSUpdateState updateState)
    {
        if(incrementOrigin != null && updateState != null &&
            incrementOrigin.getUpdateState().getOriginDocumentState() != updateState.getOriginDocumentState())
        {
            updateState.update();
        }
    }
    
    /**
     * The given object and actual {COSBase}s shall be part of the increment and must be added to {@link #objects},
     * if possible.<br>
     * {@code null} values shall be skipped.
     *
     * @param object The {@link COSBase} to add to {@link #objects}.
     * @see #objects
     */
    private void add(COSBase object)
    {
        if(object != null)
        {
            objects.add(object);
        }
    }
    
    /**
     * The given {@link COSObject} has been processed, or is being processed. It shall be added to
     * {@link #processedObjects} to skip it, should it be encountered again.<br>
     * {@code null} values shall be ignored.
     *
     * @param base The {@link COSObject} to add to {@link #processedObjects}.
     * @see #processedObjects
     */
    private void addProcessedObject(COSObject base)
    {
        if(base != null)
        {
            processedObjects.add(base);
        }
    }
    
    /**
     * The given {@link COSBase}s are not fit for inclusion in an increment and shall be added to {@link #excluded}.<br>
     * {@code null} values shall be ignored.
     *
     * @param base The {@link COSBase}s to add to {@link #excluded}.
     * @return The {@link COSIncrement} itself, to allow method chaining.
     * @see #excluded
     */
    public COSIncrement exclude(COSBase... base)
    {
        if(base != null)
        {
            excluded.addAll(Arrays.asList(base));
        }
        return this;
    }
    
    /**
     * Returns {@code true}, if the given {@link COSBase} has been excluded from the increment, and hence is contained
     * in {@link #excluded}.
     *
     * @param base The {@link COSBase} to check for exclusion.
     * @return {@code true}, if the given {@link COSBase} has been excluded from the increment, and hence is contained
     * in {@link #excluded}.
     * @see #excluded
     */
    private boolean isExcluded(COSBase base)
    {
        return excluded.contains(base);
    }
    
    /**
     * Returns all indirect {@link COSBase}s, that shall be written to an increment as top level {@link COSObject}s.<br>
     * Calling this method will cause the increment to be initialized.
     *
     * @return All indirect {@link COSBase}s, that shall be written to an increment as top level {@link COSObject}s.
     * @see #objects
     */
    public Set<COSBase> getObjects()
    {
        if(!initialized && incrementOrigin != null)
        {
            collect(incrementOrigin.getCOSObject());
            initialized = true;
        }
        return objects;
    }
    
    /**
     * Return an iterator for the determined {@link #objects} contained in this {@link COSIncrement}.
     *
     * @return An iterator for the determined {@link #objects} contained in this {@link COSIncrement}.
     */
    @Override
    public Iterator<COSBase> iterator()
    {
        return getObjects().iterator();
    }
    
}
