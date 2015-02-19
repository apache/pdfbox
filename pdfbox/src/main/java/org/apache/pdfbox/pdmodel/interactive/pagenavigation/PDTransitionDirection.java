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
package org.apache.pdfbox.pdmodel.interactive.pagenavigation;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;

/**
 * The direction in which the specified transition effect shall moves, expressed in degrees counterclockwise starting
 * from a left-to-right direction. Only for {@link PDTransitionStyle#Wipe}, {@link PDTransitionStyle#Glitter},
 * {@link PDTransitionStyle#Fly}, {@link PDTransitionStyle#Cover}, {@link PDTransitionStyle#Uncover} and
 * {@link PDTransitionStyle#Push}.
 * 
 * @author Andrea Vacondio
 *
 */
public enum PDTransitionDirection
{
    LEFT_TO_RIGHT(0),
    /**
     * Relevant only for the Wipe transition
     */
    BOTTOM_TO_TOP(90),
    /**
     * Relevant only for the Wipe transition
     */
    RIGHT_TO_LEFT(180), TOP_TO_BOTTOM(270),
    /**
     * Relevant only for the Glitter transition
     */
    TOP_LEFT_TO_BOTTOM_RIGHT(315),
    /**
     * Relevant only for the Fly transition when the value of SS is not 1.0
     */
    NONE(0)
    {
        @Override
        public COSBase getCOSBase()
        {
            return COSName.NONE;
        }
    };

    private int degrees;

    private PDTransitionDirection(int degrees)
    {
        this.degrees = degrees;
    }

    /**
     * @return the value for this direction
     */
    public COSBase getCOSBase()
    {
        return COSInteger.get(degrees);
    }
}
