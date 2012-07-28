/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.padaf.xmpbox.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class TypeUtil {

	private TypeUtil() {
		// hide constructor
	}
	
	/**
	 * Generic String List Builder for arrays contents
	 * 
	 * @return String list which represents content of array property
	 */
    public static List<String> getArrayListToString(ArrayProperty array) {
        List<String> retval = null;
        if (array != null) {
            retval = new ArrayList<String>();
            Iterator<AbstractField> it = array.getContainer()
            .getAllProperties().iterator();
            AbstractSimpleProperty tmp;
            while (it.hasNext()) {
                tmp = (AbstractSimpleProperty) it.next();
                retval.add(tmp.getStringValue());
            }
            retval = Collections.unmodifiableList(retval);
        }
        return retval;
    }

	
}
