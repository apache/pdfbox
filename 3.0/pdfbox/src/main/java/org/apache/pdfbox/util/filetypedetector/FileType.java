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
package org.apache.pdfbox.util.filetypedetector;

/**
 * @author Drew Noakes
 *
 * code taken from https://github.com/drewnoakes/metadata-extractor
 *
 * 2016-01-04
 *
 * latest commit number 73f1a48
 *
 * Enumeration of supported image file formats.
 */
public enum FileType
{
    UNKNOWN,
    JPEG,
    TIFF,
    PSD,
    PNG,
    BMP,
    GIF,
    ICO,
    PCX,
    RIFF,

    /** Sony camera raw. */
    ARW,
    /** Canon camera raw, version 1. */
    CRW,
    /** Canon camera raw, version 2. */
    CR2,
    /** Nikon camera raw. */
    NEF,
    /** Olympus camera raw. */
    ORF,
    /** FujiFilm camera raw. */
    RAF,
    /** Panasonic camera raw. */
    RW2
}