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

/**
 * A {@link COSEvent} informs a {@link COSObserver}, that some change occurred for a monitored {@link COSBase}, that
 * the {@link COSObserver} might choose to react to.
 *
 * @param <COS_TYPE> The runtime type of the monitored {@link COSBase}.
 * @author Christian Appl
 * @see COSObserver
 */
@SuppressWarnings("unused")
public abstract class COSEvent<COS_TYPE extends COSBase>
{

    private final COS_TYPE monitoredCOSBase;

    /**
     * <p>
     * Instantiates a new {@link COSEvent} to report to {@link COSObserver}s.<br>
     * This type of {@link COSEvent} informs observers, that a given {@link COSObject} has been altered, possibly
     * requiring a reaction by the observer.
     * </p>
     *
     * @param monitoredCOSBase The monitored {@link COSBase}.
     */
    public COSEvent(COS_TYPE monitoredCOSBase)
    {
        this.monitoredCOSBase = monitoredCOSBase;
    }

    /**
     * Returns the monitored {@link COSBase}.
     *
     * @return The monitored {@link COSBase}.
     */
    public COS_TYPE getMonitoredCOSBase()
    {
        return this.monitoredCOSBase;
    }

}
