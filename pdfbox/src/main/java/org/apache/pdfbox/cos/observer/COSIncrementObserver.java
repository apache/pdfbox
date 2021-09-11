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
package org.apache.pdfbox.cos.observer;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.observer.event.COSEvent;

/**
 * <p>
 * A class implementing {@link COSIncrementObserver} shall be capable of reacting to {@link COSEvent}s reported by
 * monitored {@link COSBase} objects.<br>
 * Such a {@link COSObserver} shall deduce the {@link COSBase}s, that must be included in an incremental save of the
 * {@link COSDocument}, from the occurring {@link COSEvent}s.
 * </p>
 *
 * @author Christian Appl
 * @see COSEvent
 * @see COSObserver
 */
@SuppressWarnings("unused")
public interface COSIncrementObserver extends COSObserver
{

    /**
     * Returns the {@link COSDocument} that this {@link COSIncrementObserver} is observing update states for.
     *
     * @return The {@link COSDocument} that this {@link COSIncrementObserver} is observing update states for.
     */
    COSDocument getDocument();

    /**
     * Asks this {@link COSIncrementObserver} whether it determined the given {@link COSBase} to have changed and
     * requiring an increment of the document.<br>
     * If it chooses to return {@code true}, the object shall be marked as requiring inclusion in an
     * incremental save - according to this observer.
     *
     * @param monitoredObject The monitored {@link COSBase}.
     * @return the update state of the monitored {@link COSBase}.
     */
    boolean isNeedToBeUpdated(COSBase monitoredObject);

}
