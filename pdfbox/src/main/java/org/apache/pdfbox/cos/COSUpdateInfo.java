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

import org.apache.pdfbox.pdmodel.common.COSObjectable;

public interface COSUpdateInfo extends COSObjectable
{

    /**
     * Get the update state for the COSWriter. This indicates whether an object is to be written
     * when there is an incremental save.
     *
     * @return the update state.
     */
    default boolean isNeedToBeUpdated()
    {
        return getUpdateState().isUpdated();
    }

    /**
     * Set the update state of the dictionary for the COSWriter. This indicates whether an object is
     * to be written when there is an incremental save.
     *
     * @param flag the update state.
     */
    default void setNeedToBeUpdated(boolean flag)
    {
        getUpdateState().update(flag);
    }
    
    /**
     * Uses this {@link COSUpdateInfo} as the base object of a new {@link COSIncrement}.
     *
     * @return A {@link COSIncrement} based on this {@link COSUpdateInfo}.
     * @see COSIncrement
     */
    default COSIncrement toIncrement()
    {
        return getUpdateState().toIncrement();
    }
    
    /**
     * Returns the current {@link COSUpdateState} of this {@link COSUpdateInfo}.
     *
     * @return The current {@link COSUpdateState} of this {@link COSUpdateInfo}.
     * @see COSUpdateState
     */
    COSUpdateState getUpdateState();
    
}