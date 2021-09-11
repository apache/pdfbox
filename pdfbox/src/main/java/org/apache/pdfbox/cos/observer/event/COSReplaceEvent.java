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
 * A {@link COSReplaceEvent} informs a {@link COSObserver}, that a {@link COSBase} has been replaced by another in a
 * monitored COS structure.
 *
 * @param <COS_TYPE> The runtime type of the monitored {@link COSBase}.
 * @author Christian Appl
 * @see COSObserver
 * @see COSEvent
 */
@SuppressWarnings("unused")
public class COSReplaceEvent<COS_TYPE extends COSBase> extends COSEvent<COS_TYPE>
{

    private final COSBase replacedEntry;
    private final COSBase replacingEntry;

    /**
     * <p>
     * Instantiates a new {@link COSReplaceEvent} to report to {@link COSObserver}s.<br>
     * This type of {@link COSEvent} informs observers, that the given {@link COSBase} has been replaced by the given
     * replacing {@link COSBase}.
     * </p>
     *
     * @param monitoredCOSBase The monitored {@link COSBase}.
     * @param replacedEntry    The {@link COSBase} entry, that has been replaced.
     * @param replacingEntry   The {@link COSBase}, that is replacing the entry.
     */
    public COSReplaceEvent(COS_TYPE monitoredCOSBase, COSBase replacedEntry, COSBase replacingEntry)
    {
        super(monitoredCOSBase);
        this.replacedEntry = replacedEntry;
        this.replacingEntry = replacingEntry;
    }

    /**
     * Returns the {@link COSBase} entry, that has been replaced.
     *
     * @return The {@link COSBase} entry, that has been replaced.
     */
    public COSBase getReplacedEntry()
    {
        return replacedEntry;
    }

    /**
     * Returns the {@link COSBase}, that is replacing the entry.
     *
     * @return The {@link COSBase}, that is replacing the entry.
     */
    public COSBase getReplacingEntry()
    {
        return replacingEntry;
    }

}
