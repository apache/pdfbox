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
import java.util.Collection;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestResourceRefType extends AbstractStructuredTypeTester{

	protected ResourceRefType structured = null;
	
	@Before
	public void before () throws Exception {
		super.before();
		structured = new ResourceRefType(xmp);
	}
	
	public TestResourceRefType (Class<? extends AbstractStructuredType> clz, String field,String type) {
		super(clz, field, type);
	}

	@Override
	protected AbstractStructuredType getStructured() {
		return structured;
	}

	
    @Parameters
    public static Collection<Object[]> initializeParameters() throws Exception {
    	Collection<Object[]> result = new ArrayList<Object[]>();

    	// TODO TEST test on arrays
//    	result.add(new Object [] {ResourceRefType.class,"alternatePaths","seq URI"});
    	result.add(new Object [] {ResourceRefType.class,"documentID","URI"});
    	result.add(new Object [] {ResourceRefType.class,"filePath","URI"});
    	result.add(new Object [] {ResourceRefType.class,"fromPart","Part"});
    	result.add(new Object [] {ResourceRefType.class,"instanceID","URI"});
    	result.add(new Object [] {ResourceRefType.class,"lastModifyDate","Date"});
    	result.add(new Object [] {ResourceRefType.class,"manager","AgentName"});
    	result.add(new Object [] {ResourceRefType.class,"managerVariant","Text"});
    	result.add(new Object [] {ResourceRefType.class,"manageTo","URI"});
    	result.add(new Object [] {ResourceRefType.class,"manageUI","URI"});
    	result.add(new Object [] {ResourceRefType.class,"maskMarkers","Choice"});
    	result.add(new Object [] {ResourceRefType.class,"partMapping","Text"});
    	result.add(new Object [] {ResourceRefType.class,"renditionClass","RenditionClass"});
    	result.add(new Object [] {ResourceRefType.class,"renditionParams","Text"});
    	result.add(new Object [] {ResourceRefType.class,"toPart","Part"});
    	result.add(new Object [] {ResourceRefType.class,"versionID","Text"});
    	
    	return result;
    	
    }


    
	
	
    
	
}
