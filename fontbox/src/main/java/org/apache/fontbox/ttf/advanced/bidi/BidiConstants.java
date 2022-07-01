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

/* $Id$ */

package org.apache.fontbox.ttf.advanced.bidi;


/**
 * <p>Constants used for bidirectional processing.</p>
 *
 * <p>Adapted from the Apache FOP Project.</p>
 *
 * @author Glenn Adams
 */
public interface BidiConstants {

    // bidi character class

    /** first external (official) category */
    int FIRST       = 1;

    // strong category
    /** left-to-right class */
    int L           = 1;
    /** left-to-right embedding class */
    int LRE         = 2;
    /** left-to-right override class */
    int LRO         = 3;
    /** right-to-left  class */
    int R           = 4;
    /** right-to-left arabic class */
    int AL          = 5;
    /** right-to-left embedding class */
    int RLE         = 6;
    /** right-to-left override class */
    int RLO         = 7;

    // weak category
    /** pop directional formatting class */
    int PDF         = 8;
    /** european number class */
    int EN          = 9;
    /** european number separator class */
    int ES          = 10;
    /** european number terminator class */
    int ET          = 11;
    /** arabic number class */
    int AN          = 12;
    /** common number separator class */
    int CS          = 13;
    /** non-spacing mark class */
    int NSM         = 14;
    /** boundary neutral class */
    int BN          = 15;

    // neutral category
    /** paragraph separator class */
    int B           = 16;
    /** segment separator class */
    int S           = 17;
    /** whitespace class */
    int WS          = 18;
    /** other neutrals class */
    int ON          = 19;

    /** last external (official) category */
    int LAST        = 19;

    // implementation specific categories
    /** placeholder for low surrogate */
    int SURROGATE   = 20;

    // other constants
    /** last
    /** maximum bidirectional levels */
    int MAX_LEVELS  = 61;
    /** override flag */
    int OVERRIDE    = 128;
}
