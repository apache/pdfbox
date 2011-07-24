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
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.persistence.util.COSObjectKey;

public class ExtractStream {
	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("usage : ExtractStream file objNum objGen");
		}
		PDDocument document = PDDocument.load(new FileInputStream(args[0]));
		COSObject obj = document.getDocument().getObjectFromPool(new COSObjectKey(Integer.parseInt(args[1]),Integer.parseInt(args[2])));
		if (obj.getObject() instanceof COSStream) {
			COSStream stream = (COSStream)obj.getObject();
			InputStream is = stream.getUnfilteredStream();
			FileOutputStream out = new FileOutputStream("stream.out");
			IOUtils.copyLarge(is, out);
			IOUtils.closeQuietly(out);
		}
	}
}
