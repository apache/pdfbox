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

package org.apache.padaf.preflight;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.persistence.util.COSObjectKey;

public class RetrieveMissingStream {
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("usage : RetrieveMissingStream file");
			System.exit(233);
		}

		HashSet<COSObjectKey> listOfKeys = new HashSet<COSObjectKey>();
		
		PDDocument document = PDDocument.load(new FileInputStream(args[0]));
		List<COSObject> lCosObj = document.getDocument().getObjects();
		for (COSObject cosObject : lCosObj) {

			if (cosObject.getObject() instanceof COSStream) {
				listOfKeys.add(new COSObjectKey(cosObject.getObjectNumber().intValue(),
																				cosObject.getGenerationNumber().intValue()));
			}

		}
		
    PDDocumentCatalog catalog = document.getDocumentCatalog();
    List<?> pages = catalog.getAllPages();
    for (int i = 0; i < pages.size(); ++i) {
    	PDPage pdp = (PDPage) pages.get(i);
    	PDStream pdStream = pdp.getContents();
 
    	COSBase b = pdp.getCOSDictionary().getItem(COSName.getPDFName("Contents"));
    	System.out.println();
    }
	}
}
