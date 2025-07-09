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
package org.apache.fontbox.ttf;

/**
 * To improve performance of {@code FileSystemFontProvider.scanFonts(...)},
 * this class is used both as a marker (to skip unused data) and as a storage for collected data.
 * <p>
 * Tables it needs:<ul>
 * <li>NamingTable.TAG
 * <li>HeaderTable.TAG
 * <li>OS2WindowsMetricsTable.TAG
 * <li>CFFTable.TAG (for OTF)
 * <li>"gcid" (for non-OTF)
 * </ul>
 *
 * @author Mykola Bohdiuk
 */
public final class FontHeaders
{
    static final int BYTES_GCID = 142;

    private String error;
    private String name;
    private Integer headerMacStyle;
    private OS2WindowsMetricsTable os2Windows;
    private String fontFamily;
    private String fontSubFamily;
    private byte[] nonOtfGcid142;
    //
    private boolean isOTFAndPostScript;
    private String otfRegistry;
    private String otfOrdering;
    private int otfSupplement;

    public String getError()
    {
        return error;
    }

    public String getName()
    {
        return name;
    }

    /**
     * null == no HeaderTable, {@code ttf.getHeader().getMacStyle()}
     */
    public Integer getHeaderMacStyle()
    {
        return headerMacStyle;
    }

    public OS2WindowsMetricsTable getOS2Windows()
    {
        return os2Windows;
    }

    // only when LOGGER(FileSystemFontProvider).isTraceEnabled() tracing: FontFamily, FontSubfamily
    public String getFontFamily()
    {
        return fontFamily;
    }

    public String getFontSubFamily()
    {
        return fontSubFamily;
    }

    public boolean isOpenTypePostScript()
    {
        return isOTFAndPostScript;
    }

    public byte[] getNonOtfTableGCID142()
    {
        return nonOtfGcid142;
    }

    public String getOtfRegistry()
    {
        return otfRegistry;
    }

    public String getOtfOrdering()
    {
        return otfOrdering;
    }

    public int getOtfSupplement()
    {
        return otfSupplement;
    }

    public void setError(String exception)
    {
        this.error = exception;
    }

    void setName(String name)
    {
        this.name = name;
    }

    void setHeaderMacStyle(Integer headerMacStyle)
    {
        this.headerMacStyle = headerMacStyle;
    }

    void setOs2Windows(OS2WindowsMetricsTable os2Windows)
    {
        this.os2Windows = os2Windows;
    }

    void setFontFamily(String fontFamily, String fontSubFamily)
    {
        this.fontFamily = fontFamily;
        this.fontSubFamily = fontSubFamily;
    }

    void setNonOtfGcid142(byte[] nonOtfGcid142)
    {
        this.nonOtfGcid142 = nonOtfGcid142;
    }

    void setIsOTFAndPostScript(boolean isOTFAndPostScript)
    {
        this.isOTFAndPostScript = isOTFAndPostScript;
    }

    // public because CFFParser is in a different package
    public void setOtfROS(String otfRegistry, String otfOrdering, int otfSupplement)
    {
        this.otfRegistry = otfRegistry;
        this.otfOrdering = otfOrdering;
        this.otfSupplement = otfSupplement;
    }
}
