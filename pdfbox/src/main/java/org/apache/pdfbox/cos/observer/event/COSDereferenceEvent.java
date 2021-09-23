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
 * A {@link COSDereferenceEvent} informs a {@link COSObserver}, that a {@link COSObject} has been dereferenced and the
 * matching {@link COSBase} has been parsed and loaded.
 *
 * @param <COS_TYPE> The runtime type of the monitored {@link COSBase}.
 * @author Christian Appl
 * @see COSObserver
 * @see COSEvent
 */
@SuppressWarnings("unused")
public class COSDereferenceEvent<COS_TYPE extends COSBase> extends COSEvent<COS_TYPE>
{

    COSBase dereferencedObject;

    /**
     * <p>
     * Instantiates a new {@link COSDereferenceEvent} to report to {@link COSObserver}s.<br>
     * This type of {@link COSEvent} informs observers, that a given {@link COSObject} has been dereferenced, parsing
     * and loading the referenced {@link COSBase}.
     * </p>
     *
     * @param monitoredCOSBase   The monitored {@link COSBase}.
     * @param dereferencedObject The {@link COSBase} that has been parsed and loaded.
     */
    public COSDereferenceEvent(COS_TYPE monitoredCOSBase, COSBase dereferencedObject)
    {
        super(monitoredCOSBase);
        this.dereferencedObject = dereferencedObject;
    }

    /**
     * Returns the parsed and loaded {@link COSBase}.
     *
     * @return The parsed and loaded {@link COSBase}.
     */
    public COSBase getDereferencedObject()
    {
        return dereferencedObject;
    }

}
