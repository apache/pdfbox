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
import org.apache.pdfbox.cos.observer.COSObserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A {@link COSAddEvent} informs a {@link COSObserver}, that a {@link COSBase} has been added to a monitored COS
 * structure.
 *
 * @param <COS_TYPE> The runtime type of the monitored {@link COSBase}.
 * @author Christian Appl
 * @see COSObserver
 * @see COSEvent
 */
@SuppressWarnings("unused")
public class COSAddEvent<COS_TYPE extends COSBase> extends COSEvent<COS_TYPE>
{

    private final List<COSBase> addedEntries = new ArrayList<>();

    /**
     * <p>
     * Instantiates a new {@link COSAddEvent} to report to {@link COSObserver}s.<br>
     * This type of {@link COSEvent} informs observers, that the given {@link COSBase} entries have been added to the
     * monitored structure.
     * </p>
     *
     * @param monitoredCOSBase The monitored {@link COSBase}.
     * @param addedEntries     The {@link COSBase} entries added to the monitored structure.
     */
    public COSAddEvent(COS_TYPE monitoredCOSBase, COSBase... addedEntries)
    {
        super(monitoredCOSBase);
        if (addedEntries != null)
        {
            this.addedEntries.addAll(Arrays.asList(addedEntries));
        }
    }

    /**
     * <p>
     * Instantiates a new {@link COSAddEvent} to report to {@link COSObserver}s.<br>
     * This type of {@link COSEvent} informs observers, that the given {@link COSBase} entries have been added to the
     * monitored structure.
     * </p>
     *
     * @param monitoredCOSBase The monitored {@link COSBase}.
     * @param addedEntries     The {@link COSBase} entries added to the monitored structure.
     */
    public COSAddEvent(COS_TYPE monitoredCOSBase, Collection<COSBase> addedEntries)
    {
        super(monitoredCOSBase);
        if (addedEntries != null)
        {
            this.addedEntries.addAll(addedEntries);
        }
    }

    /**
     * Returns the {@link COSBase} entries added to the monitored structure.
     *
     * @return The {@link COSBase} entries added to the monitored structure.
     */
    public List<COSBase> getAddedEntries()
    {
        return addedEntries;
    }

}
