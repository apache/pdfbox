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

import org.apache.pdfbox.cos.observer.COSIncrementObserver;
import org.apache.pdfbox.cos.observer.event.COSDirectUpdateEvent;
import org.apache.pdfbox.cos.observer.COSObserver;

public interface COSUpdateInfo
{

    /**
     * Get the update state for the COSWriter. This indicates whether an object is to be written
     * when there is an incremental save.
     *
     * @return the update state.
     */
    default boolean isNeedToBeUpdated()
    {
        COSBase instance = getCOSObject();
        if (instance != null)
        {
            for (COSObserver observer : instance.getRegisteredObservers())
            {
                if (observer instanceof COSIncrementObserver)
                {
                    if (((COSIncrementObserver) observer).isNeedToBeUpdated(instance))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Set the update state of the dictionary for the COSWriter. This indicates whether an object is
     * to be written when there is an incremental save.
     *
     * @param flag the update state.
     */
    default void setNeedToBeUpdated(boolean flag)
    {
        COSBase instance = getCOSObject();
        if (instance != null)
        {
            instance.reportUpdate(new COSDirectUpdateEvent<>(instance, flag));
        }
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     * @see COSBase#getCOSObject()
     */
    COSBase getCOSObject();

}