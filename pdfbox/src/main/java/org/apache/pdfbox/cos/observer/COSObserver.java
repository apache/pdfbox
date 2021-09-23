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
import org.apache.pdfbox.cos.observer.event.COSEvent;

/**
 * A class implementing {@link COSObserver} shall be capable of reacting to {@link COSEvent}s reported by monitored
 * {@link COSBase} objects.
 * What events are reacted to and which purpose this serves is up to the implementing class to define.
 *
 * @author Christian Appl
 * @see COSEvent
 */
@SuppressWarnings("unused")
public interface COSObserver
{

    /**
     * A monitored structure reports, that the given {@link COSEvent} has occurred.
     *
     * @param updateEvent The {@link COSEvent}, that the observer might want to react to.
     */
    void reportUpdate(COSEvent<?> updateEvent);

    /**
     * Unless this method is called, a {@link COSObserver} shall not react to reported {@link COSEvent}s.
     */
    void startTrackingChanges();

    /**
     * Returns {@code true}, when {@link #startTrackingChanges()} has been called and the {@link COSObserver} may react
     * to {@link COSEvent}s.
     *
     * @return {@code true}, when {@link #startTrackingChanges()} has been called and the {@link COSObserver} may react
     * to {@link COSEvent}s.
     */
    boolean isTrackingChanges();

    /**
     * Stops the observer from tracking the following changes.
     */
    void stopTrackingChanges();

    /**
     * Adds the given {@link COSBase} to the objects this {@link COSObserver} does observe.
     *
     * @param object The {@link COSBase} to monitor.
     */
    void monitor(COSBase object);

}
