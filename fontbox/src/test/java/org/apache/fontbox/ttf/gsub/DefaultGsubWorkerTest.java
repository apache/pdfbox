package org.apache.fontbox.ttf.gsub;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * @author Vladimir Plizga
 */
class DefaultGsubWorkerTest
{

    @Test
    @DisplayName("Transformation result is actually a read-only version of the argument")
    void applyTransforms()
    {
        // given
        DefaultGsubWorker sut = new DefaultGsubWorker();
        List<Integer> originalGlyphIds = Arrays.asList(1, 2, 3, 4, 5);

        // when
        List<Integer> pseudoTransformedIds = sut.applyTransforms(originalGlyphIds);
        Executable modification = pseudoTransformedIds::clear;

        // then
        assertEquals(originalGlyphIds, pseudoTransformedIds);
        assertThrows(UnsupportedOperationException.class, modification);
    }
}