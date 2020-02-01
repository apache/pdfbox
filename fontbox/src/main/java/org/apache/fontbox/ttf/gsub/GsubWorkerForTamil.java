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

package org.apache.fontbox.ttf.gsub;

import java.util.Arrays;
import java.util.List;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.model.GsubData;

/**
 * 
 * Tamil-specific implementation of GSUB system
 * 
 * @author Raja Ksv
 *
 */
public class GsubWorkerForTamil extends GsubWorkerBase {
	private static final List<String> FEATURES_IN_ORDER = Arrays.asList("locl", "nukt", "akhn", "rphf", "blwf", "pstf",
			"half", "vatu", "cjct", INIT_FEATURE, "pres", "abvs", "blws", "psts", "haln", "calt");

	private static final char[] BEFORE_HALF_CHARS = new char[] { '\u0BC6', '\u0BC7', '\u0BC8' };

	private static final BeforeAndAfterSpanComponent[] BEFORE_AND_AFTER_SPAN_CHARS = new BeforeAndAfterSpanComponent[] {
			new BeforeAndAfterSpanComponent('\u0BC7', '\u09C7', '\u09BE') };

	public GsubWorkerForTamil(CmapLookup cmapLookup, GsubData gsubData) {
		super(cmapLookup, gsubData);
	}

	@Override
	public char[] getBeforeHalfChars() {
		return BEFORE_HALF_CHARS;
	}

	@Override
	public List<String> getFeaturesInOrder() {
		return FEATURES_IN_ORDER;
	}

	@Override
	public BeforeAndAfterSpanComponent[] getBeforeAfterSpanChars() {
		return BEFORE_AND_AFTER_SPAN_CHARS;
	}

}
