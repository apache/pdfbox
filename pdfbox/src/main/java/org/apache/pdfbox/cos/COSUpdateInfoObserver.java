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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.observer.COSIncrementObserver;
import org.apache.pdfbox.cos.observer.COSObserver;
import org.apache.pdfbox.cos.observer.event.COSAddEvent;
import org.apache.pdfbox.cos.observer.event.COSDereferenceEvent;
import org.apache.pdfbox.cos.observer.event.COSDirectUpdateEvent;
import org.apache.pdfbox.cos.observer.event.COSEvent;
import org.apache.pdfbox.cos.observer.event.COSRemoveEvent;
import org.apache.pdfbox.cos.observer.event.COSReplaceEvent;


/**
 * <p>
 * This basic implementation of {@link COSIncrementObserver} shall be capable of reacting to {@link COSEvent}s reported
 * by monitored {@link COSBase} objects.<br>
 * It shall automatically add further monitored {@link COSBase} objects, as required and can be determined via
 * evaluating incoming {@link COSEvent}s.<br>
 * Such a {@link COSUpdateInfoObserver} shall deduce the {@link COSBase}s, that must be included in an incremental save
 * of the {@link COSDocument}, from the occurring {@link COSEvent}s.
 * </p>
 *
 * @author Christian Appl
 * @see COSEvent
 * @see COSIncrementObserver
 */
@SuppressWarnings("unused")
public class COSUpdateInfoObserver implements COSIncrementObserver, Iterable<COSUpdateInfo>
{

    private final COSDocument document;
    private boolean trackingChanges = false;
    /**
     * A list of all {@link COSObject}s, that were contained in the original document.
     */
    private final List<COSObject> preexistingObjects = new ArrayList<>();

    /**
     * A list of all {@link COSUpdateInfo} instances, that are currently monitored by this observer.
     */
    private final List<COSUpdateInfo> monitoredObjects = new ArrayList<>();
    /**
     * A list of all {@link COSUpdateInfo} instances, that this observer determined to be part of an increment.
     */
    private final List<COSUpdateInfo> updatedObjects = new ArrayList<>();

    /**
     * A map mapping monitored {@link COSUpdateInfo} to a list of structures, that hold a reference to said object.
     */
    private final Map<COSUpdateInfo, List<COSUpdateInfo>> referenceHolders = new HashMap<>();

    /**
     * Instantiates a new {@link COSUpdateInfoObserver} for the given {@link COSDocument}.
     *
     * @param document The {@link COSDocument} update states shall be monitored for.
     */
    public COSUpdateInfoObserver(COSDocument document)
    {
        this.document = document;
    }

    /**
     * Unless this method is called, a {@link COSObserver} shall not react to reported {@link COSEvent}s.
     */
    @Override
    public void startTrackingChanges()
    {
        trackingChanges = true;
    }

    /**
     * Returns {@code true}, when {@link #startTrackingChanges()} has been called and the {@link COSObserver} may react
     * to {@link COSEvent}s.
     *
     * @return {@code true}, when {@link #startTrackingChanges()} has been called and the {@link COSObserver} may react
     * to {@link COSEvent}s.
     */
    @Override
    public boolean isTrackingChanges()
    {
        return trackingChanges;
    }

    /**
     * Stops the observer from tracking the following changes.
     */
    @Override
    public void stopTrackingChanges() {
        trackingChanges = false;
    }

    /**
     * Returns the {@link COSDocument} that this {@link COSIncrementObserver} is observing update states for.
     *
     * @return The {@link COSDocument} that this {@link COSIncrementObserver} is observing update states for.
     */
    @Override
    public COSDocument getDocument()
    {
        return document;
    }

    /**
     * Asks this {@link COSUpdateInfoObserver} whether it determined the given {@link COSBase} to have changed and
     * requiring an increment of the document.<br>
     * If it chooses to return {@code true}, the object shall be marked as requiring inclusion in an
     * incremental save - according to this observer.
     *
     * @return the update state of the monitored {@link COSBase}.
     */
    @Override
    public boolean isNeedToBeUpdated(COSBase object)
    {
        return object instanceof COSUpdateInfo && !(object instanceof UnmodifiableCOSDictionary)
            && updatedObjects.contains((COSUpdateInfo) object);
    }

    /**
     * Returns all {@link COSUpdateInfo} objects, currently monitored by this observer.
     *
     * @return All {@link COSUpdateInfo} objects, currently monitored by this observer.
     */
    public List<COSUpdateInfo> getMonitoredObjects()
    {
        return monitoredObjects;
    }

    /**
     * Returns all monitored {@link COSUpdateInfo} objects, that require an update - according to this observer.
     *
     * @return All monitored {@link COSUpdateInfo} objects, that require an update - according to this observer.
     */
    public List<COSUpdateInfo> getUpdatedObjects()
    {
        return updatedObjects;
    }

    /**
     * Returns an {@link Iterator} over all updated {@link COSUpdateInfo} objects, collected by this observer.
     *
     * @return An {@link Iterator} over all updated {@link COSUpdateInfo} objects, collected by this observer.
     */
    @Override
    public Iterator<COSUpdateInfo> iterator()
    {
        return getUpdatedObjects().iterator();
    }

    /**
     * A monitored structure reports, that the given {@link COSEvent} has occurred.
     *
     * @param updateEvent The {@link COSEvent}, that the observer might want to react to.
     */
    @Override
    public void reportUpdate(COSEvent<?> updateEvent)
    {
        // Prepare the actual updatable object:
        COSUpdateInfo updatedObject = updateEvent.getMonitoredCOSBase() instanceof COSUpdateInfo ?
            (COSUpdateInfo) updateEvent.getMonitoredCOSBase() : null;
        // COSBase objects, that are not updatable, are ignored:
        if (updatedObject == null)
        {
            return;
        }

        // TODO: This is enabling and preserving the "setNeedToBeUpdated" method to work.
        // If that method shall be removed one day, simply remove the following handling for the COSDirectUpdateEvent.
        // DIRECT-UPDATE-EVENT
        // Directly add the object to the increment.
        if (updateEvent instanceof COSDirectUpdateEvent)
        {
            if (((COSDirectUpdateEvent<?>) updateEvent).isUpdateState())
            {
                addUpdatedObject(updatedObject.getCOSObject());
                monitor(updatedObject.getCOSObject());
            }
            else
            {
                removeUpdatedObject(updatedObject.getCOSObject());
            }
        }

        // DEREFERENCE-EVENT
        // Add the dereferenced structure to monitoring:
        else if (updateEvent instanceof COSDereferenceEvent)
        {
            boolean tracking = isTrackingChanges();
            // Dereferencing a COSObject, that was initially contained in a COSDocument, shall never cause it's
            // contained structures to be updated. (but will cause them to be monitored for future changes.)
            if (updatedObject instanceof COSObject && preexistingObjects.contains(updatedObject))
            {
                stopTrackingChanges();
            }
            addReferenceHolder(((COSDereferenceEvent<?>) updateEvent).getDereferencedObject(), updatedObject);
            monitor(((COSDereferenceEvent<?>) updateEvent).getDereferencedObject());
            if (tracking)
            {
                startTrackingChanges();
            }
        }

        // ADD-EVENT
        // Add the entries to the monitored objects:
        else if (updateEvent instanceof COSAddEvent)
        {
            addUpdatedObject(updateEvent.getMonitoredCOSBase());
            for (COSBase addedObject : ((COSAddEvent<?>) updateEvent).getAddedEntries())
            {
                monitor(addedObject);
            }
        }

        // REMOVE-EVENT:
        // Remove the entries from the reference holder (possibly also exclude them from increment and monitoring):
        else if (updateEvent instanceof COSRemoveEvent)
        {
            addUpdatedObject(updateEvent.getMonitoredCOSBase());
            for (COSBase removedObject : ((COSRemoveEvent<?>) updateEvent).getRemovedEntries())
            {
                removeReferenceHolder(removedObject, updatedObject);
            }
        }

        // REPLACE-EVENT
        // Remove the previous entry (possibly also exclude it from increment and monitoring), monitor the new entry:
        else if (updateEvent instanceof COSReplaceEvent)
        {
            addUpdatedObject(updateEvent.getMonitoredCOSBase());
            removeReferenceHolder(((COSReplaceEvent<?>) updateEvent).getReplacedEntry(), updatedObject);
            monitor(((COSReplaceEvent<?>) updateEvent).getReplacingEntry());
        }
    }

    /**
     * Adds the given {@link COSBase} and it´s descendants to the objects this {@link COSObserver} does observe.<br>
     * If this observer {@link #isTrackingChanges()}, the object shall also be added to the list of updated objects
     * (as it must be assumed, that those are new objects).
     *
     * @param object The {@link COSBase} to monitor.
     */
    @Override
    public void monitor(COSBase object)
    {
        if (!(object instanceof COSUpdateInfo) || monitoredObjects.contains((COSUpdateInfo) object))
        {
            return;
        }
        monitoredObjects.add((COSUpdateInfo) object);
        object.registerObserver(this);

        // Attempt to add the object to the increment (possibly new object):
        addUpdatedObject(object);

        // Recursively resolve arrays and dictionaries:
        if (object instanceof COSArray)
        {
            for (COSBase entry : (COSArray) object)
            {
                addReferenceHolder(entry, (COSUpdateInfo) object);
                monitor(entry);
            }
        }
        else if (object instanceof COSDictionary)
        {
            for (COSBase entry : ((COSDictionary) object).getValues())
            {
                addReferenceHolder(entry, (COSUpdateInfo) object);
                monitor(entry);
            }
        }

        // Handle an indirect reference:
        else if (object instanceof COSObject)
        {
            COSObject cosObject = (COSObject) object;
            // Preexisting COSObjects must be stored for later use and shall only be dereferenced if necessary.
            if (!isTrackingChanges())
            {
                preexistingObjects.add(cosObject);
            }
            // Recursively resolve dereferenced COSObjects:
            if (cosObject.isDereferenced())
            {
                addReferenceHolder(cosObject, (COSUpdateInfo) object);
                monitor(cosObject.getObject());
            }
            // An indirect object added to an active observer must be dereferenced! (causing an event to react to.)
            else if ((isTrackingChanges() && !preexistingObjects.contains(cosObject)))
            {
                cosObject.getObject();
            }
        }
    }

    /**
     * Attempts to add the given {@link COSBase} as a referenced {@link COSUpdateInfo} and adds the given
     * {@link COSUpdateInfo} as a reference holder of that object.
     *
     * @param referencedObject The {@link COSBase}, that might require collection as a referencable resource.
     * @param referenceHolder  The {@link COSUpdateInfo} reference holder to add for that object.
     */
    private void addReferenceHolder(COSBase referencedObject, COSUpdateInfo referenceHolder)
    {
        if (!(referencedObject instanceof COSUpdateInfo))
        {
            return;
        }
        List<COSUpdateInfo> referenceHolders;
        if (!this.referenceHolders.containsKey((COSUpdateInfo) referencedObject))
        {
            referenceHolders = new ArrayList<>();
            this.referenceHolders.put((COSUpdateInfo) referencedObject, referenceHolders);
        }
        else
        {
            referenceHolders = this.referenceHolders.get((COSUpdateInfo) referencedObject);
        }
        if (!referenceHolders.contains(referenceHolder))
        {
            referenceHolders.add(referenceHolder);
        }
    }

    /**
     * Attempts to add the given {@link COSBase} as an updated {@link COSUpdateInfo}, if the object may currently be
     * collected as such.
     *
     * @param object The {@link COSBase} to check for inclusion in the increment.
     */
    private void addUpdatedObject(COSBase object)
    {
        // Either updates are currently not collected, or the object is not fit for inclusion in an increment:
        if (!isTrackingChanges() || !(object instanceof COSUpdateInfo) ||
            updatedObjects.contains((COSUpdateInfo) object))
        {
            return;
        }

        // The object has to be written directly. Update the top level reference holders instead:
        // Also: Ignore indirect COSObjects, that claim being direct objects.
        if ((!(object instanceof COSObject) && object.isDirect()) || object instanceof COSArray)
        {
            if (!referenceHolders.containsKey((COSUpdateInfo) object))
            {
                return;
            }
            updateReferenceHolders((COSUpdateInfo) object, new ArrayList<>());
            return;
        }

        // The object is updatable and shall be added to the increment:
        updatedObjects.add((COSUpdateInfo) object);
    }

    /**
     * Attempts to remove the given {@link COSBase} as an updated {@link COSUpdateInfo}.
     *
     * @param object The {@link COSBase} to remove.
     */
    private void removeUpdatedObject(COSBase object)
    {
        if (!isTrackingChanges() || !(object instanceof COSUpdateInfo) ||
            !updatedObjects.contains((COSUpdateInfo) object))
        {
            return;
        }
        updatedObjects.remove((COSUpdateInfo) object);
    }

    /**
     * Recursively update containing reference holders for direct objects.
     *
     * @param object                    The causing direct object.
     * @param processedReferenceHolders The reference holders, that have already been processed.
     */
    private void updateReferenceHolders(COSUpdateInfo object, List<COSUpdateInfo> processedReferenceHolders)
    {
        if (!referenceHolders.containsKey(object))
        {
            return;
        }
        for (COSUpdateInfo referenceHolder : referenceHolders.get(object))
        {
            if (processedReferenceHolders.contains(referenceHolder))
            {
                continue;
            }
            processedReferenceHolders.add(referenceHolder);
            if (updatedObjects.contains(referenceHolder))
            {
                continue;
            }
            if ((!(referenceHolder instanceof COSObject) && ((COSBase) referenceHolder).isDirect()) ||
                referenceHolder instanceof COSArray)
            {
                updateReferenceHolders(referenceHolder, processedReferenceHolders);
                continue;
            }
            updatedObjects.add(referenceHolder);
        }
    }

    /**
     * <p>
     * Remove the given {@link COSUpdateInfo} as a referenceHolder for the given {@link COSBase}.<br>
     * If the reference holder had been the last referenceHolder of that object, remove the object from the increment
     * and monitored objects all together.
     * </p>
     *
     * @param referencedObject        The {@link COSBase}, that shall no longer be included in the given reference holder.
     * @param previousReferenceHolder The {@link COSUpdateInfo} reference holder, that shall no longer include the object.
     */
    private void removeReferenceHolder(COSBase referencedObject, COSUpdateInfo previousReferenceHolder)
    {
        // The object is not updatable anyway, or it is the document´s trailer and must never be excluded.
        if (!(referencedObject instanceof COSUpdateInfo) || referencedObject == getDocument().getTrailer())
        {
            return;
        }

        boolean eliminateReferenceObject = false;

        // The object had not been referenced anyway - stop monitoring it!
        if (!referenceHolders.containsKey((COSUpdateInfo) referencedObject))
        {
            eliminateReferenceObject = true;
        }
        // The object´s reference holder shall be removed.
        else
        {
            List<COSUpdateInfo> actualReferenceHolders = referenceHolders.get((COSUpdateInfo) referencedObject);
            actualReferenceHolders.remove((COSUpdateInfo) referencedObject);

            // The object is no longer referenced and must not be part of the increment,
            // it shall no longer be monitored.
            if (actualReferenceHolders.isEmpty())
            {
                eliminateReferenceObject = true;
            }
        }

        // The object must be removed from increment and monitoring, it may no longer serve as a reference holder.
        if (eliminateReferenceObject)
        {
            referencedObject.unregisterObserver(this);
            updatedObjects.remove((COSUpdateInfo) referencedObject);
            monitoredObjects.remove((COSUpdateInfo) referencedObject);
            referenceHolders.remove((COSUpdateInfo) referencedObject);
            for (COSUpdateInfo object : referenceHolders.keySet())
            {
                removeReferenceHolder(object.getCOSObject(), (COSUpdateInfo) referencedObject);
            }
        }
    }

}
