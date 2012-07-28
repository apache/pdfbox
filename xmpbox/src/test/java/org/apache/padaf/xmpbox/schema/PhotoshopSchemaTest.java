 /*****************************************************************************
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

package org.apache.padaf.xmpbox.schema;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PhotoshopSchemaTest extends AbstractSchemaTester {

	protected PhotoshopSchema schema = null;
	
	public PhotoshopSchema getSchema () {
		return schema;
	}
	
	@Before
	public void before() throws Exception {
		super.before();
		schema = xmp.createAndAddPhotoshopSchema();
	}

	public PhotoshopSchemaTest(String fieldName, String type, Cardinality card) {
		super(fieldName, type, card);
	}

	@Parameters
	public static Collection<Object[]> initializeParameters() throws Exception {
    	Collection<Object[]> result = new ArrayList<Object[]>();

    	result.add(new Object [] {"AncestorID","URI",Cardinality.Simple});
//    	result.add(new Object [] {"LayerName","Text",Cardinality.Simple}); TODO TEST missing in schema
//    	result.add(new Object [] {"LayerText","Text",Cardinality.Simple}); TODO TEST missing in schema
    	result.add(new Object [] {"AuthorsPosition","Text",Cardinality.Simple});
    	result.add(new Object [] {"CaptionWriter","ProperName",Cardinality.Simple});
    	result.add(new Object [] {"Category","Text",Cardinality.Simple});
    	result.add(new Object [] {"City","Text",Cardinality.Simple});
    	result.add(new Object [] {"ColorMode","Integer",Cardinality.Simple});
    	result.add(new Object [] {"Country","Text",Cardinality.Simple});
    	result.add(new Object [] {"Credit","Text",Cardinality.Simple});
    	result.add(new Object [] {"DateCreated","Date",Cardinality.Simple});
    	// DocumentAncestors TODO TEST bag Ancestor
    	result.add(new Object [] {"Headline","Text",Cardinality.Simple});
    	result.add(new Object [] {"History","Text",Cardinality.Simple});
    	result.add(new Object [] {"ICCProfile","Text",Cardinality.Simple});
    	result.add(new Object [] {"Instructions","Text",Cardinality.Simple});
    	result.add(new Object [] {"Source","Text",Cardinality.Simple});
    	result.add(new Object [] {"State","Text",Cardinality.Simple});
    	result.add(new Object [] {"SupplementalCategories","Text",Cardinality.Bag});
    	// Layer TODO TEST structured type
    	result.add(new Object [] {"TransmissionReference","Text",Cardinality.Simple});
    	result.add(new Object [] {"Urgency","Integer",Cardinality.Simple});

    	// TODO TEST camera raw
    	
    	return result;
	}


}
