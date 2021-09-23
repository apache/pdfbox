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

/**
 * A {@link COSDirectUpdateEvent} informs a {@link COSObserver}, that a {@link COSBase} has been directly marked as
 * having been updated.
 *
 * @param <COS_TYPE> The runtime type of the monitored {@link COSBase}.
 * @author Christian Appl
 * @see COSObserver
 * @see COSEvent
 */
public class COSDirectUpdateEvent<COS_TYPE extends COSBase> extends COSEvent<COS_TYPE>
{

    private final boolean updateState;

    /**
     * <p>
     * Instantiates a new {@link COSDirectUpdateEvent} to report to {@link COSObserver}s.<br>
     * This type of {@link COSEvent} informs observers, that a given {@link COSBase} has been marked as being updated.
     * </p>
     *
     * @param updatedObject The monitored {@link COSBase}.
     * @param updateState   The update state, that shall be set.
     */
    public COSDirectUpdateEvent(COS_TYPE updatedObject, boolean updateState)
    {
        super(updatedObject);
        this.updateState = updateState;
    }

    /**
     * Returns the update state to be set.
     *
     * @return The update state to be set.
     */
    public boolean isUpdateState()
    {
        return updateState;
    }

}
