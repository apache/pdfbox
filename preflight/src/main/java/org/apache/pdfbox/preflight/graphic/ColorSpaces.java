/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.pdfbox.preflight.graphic;

/**
 * This enum makes ColorSpaces validation easier. Labels represent ColorSpace names as defined in the
 * "PDF Reference 1.4".
 */
public enum ColorSpaces
{

    Lab("Lab"), CalRGB("CalRGB"), CalGray("CalGray"), DeviceN("DeviceN"), Indexed("Indexed"), I("I"), 
    Pattern("Pattern"), ICCBased("ICCBased"), DeviceRGB("DeviceRGB"), RGB("RGB"), DeviceGray("DeviceGray"), 
    G("G"), DeviceCMYK("DeviceCMYK"), CMYK("CMYK"), Separation("Separation");

    /**
     * Name of the ColorSpace
     */
    private final String label;

    private ColorSpaces(final String _label)
    {
        label = _label;
    }

    /**
     * @return the label
     */
    public String getLabel()
    {
        return label;
    }
}
