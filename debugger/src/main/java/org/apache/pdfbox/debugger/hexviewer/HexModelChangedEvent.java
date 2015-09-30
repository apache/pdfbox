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

package org.apache.pdfbox.debugger.hexviewer;

/**
 * @author Khyrul Bashar
 *
 * HexModelChangedEvent describes the change in the HexModel.
 */
class HexModelChangedEvent
{
    static final int BULK_CHANGE = 1;
    static final int SINGLE_CHANGE = 2;

    private final int startIndex;
    private final int changeType;

    /**
     * Constructor.
     *
     * @param startIndex int. From where changes start.
     * @param changeType int. Change type if it is only a single change or a bulk change by pasting
     * or deleting. Though later features are not yet implemented.
     */
    HexModelChangedEvent(int startIndex, int changeType)
    {
        this.startIndex = startIndex;
        this.changeType = changeType;
    }

    int getStartIndex()
    {
        return startIndex;
    }

    int getChangeType()
    {
        return changeType;
    }
}
