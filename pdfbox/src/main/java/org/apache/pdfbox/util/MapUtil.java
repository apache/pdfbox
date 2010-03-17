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
package org.apache.pdfbox.util;

import java.util.Map;


/**
 * This class with handle some simple Map operations.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.2 $
 */
public class MapUtil
{
    private MapUtil()
    {
        //utility class
    }

    /**
     * Generate a unique key for the map based on a prefix.
     *
     * @param map The map to look for existing keys.
     * @param prefix The prefix to use when generating the key.
     * @return The new unique key that does not currently exist in the map.
     */
    public static final String getNextUniqueKey( Map map, String prefix )
    {
        int counter = 0;
        while( map.get( prefix+counter ) != null )
        {
            counter++;
        }
        return prefix+counter;
    }
}
