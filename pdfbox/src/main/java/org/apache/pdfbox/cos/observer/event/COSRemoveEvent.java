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
package org.apache.pdfbox.cos.observer.event;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.observer.COSObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A {@link COSRemoveEvent} informs a {@link COSObserver}, that a {@link COSBase} has been removed from a monitored COS
 * structure.
 *
 * @param <COS_TYPE> The runtime type of the monitored {@link COSBase}.
 * @author Christian Appl
 * @see COSObserver
 * @see COSEvent
 */
@SuppressWarnings("unused")
public class COSRemoveEvent<COS_TYPE extends COSBase> extends COSEvent<COS_TYPE>
{

    private final List<COSBase> removedEntries = new ArrayList<>();

    /**
     * <p>
     * Instantiates a new {@link COSRemoveEvent} to report to {@link COSObserver}s.<br>
     * This type of {@link COSEvent} informs observers, that a given {@link COSObject} has been removed from the
     * monitored {@link COSBase}.
     * </p>
     *
     * @param monitoredCOSBase The monitored {@link COSBase}.
     * @param removedEntries   The {@link COSBase} entries removed from the monitored structure.
     */
    public COSRemoveEvent(COS_TYPE monitoredCOSBase, COSBase... removedEntries)
    {
        super(monitoredCOSBase);
        if (removedEntries != null)
        {
            this.removedEntries.addAll(Arrays.asList(removedEntries));
        }
    }

    /**
     * <p>
     * Instantiates a new {@link COSRemoveEvent} to report to {@link COSObserver}s.<br>
     * This type of {@link COSEvent} informs observers, that a given {@link COSObject} has been removed from the
     * monitored {@link COSBase}.
     * </p>
     *
     * @param monitoredCOSBase The monitored {@link COSBase}.
     * @param removedEntries   The {@link COSBase} entries removed from the monitored structure.
     */
    public COSRemoveEvent(COS_TYPE monitoredCOSBase, Collection<COSBase> removedEntries)
    {
        super(monitoredCOSBase);
        if (removedEntries != null)
        {
            this.removedEntries.addAll(removedEntries);
        }
    }

    /**
     * Returns the {@link COSBase} entries removed from the monitored structure.
     *
     * @return The {@link COSBase} entries removed from the monitored structure.
     */
    public List<COSBase> getRemovedEntries()
    {
        return this.removedEntries;
    }

}
