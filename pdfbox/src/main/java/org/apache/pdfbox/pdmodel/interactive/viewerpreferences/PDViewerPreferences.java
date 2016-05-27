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
package org.apache.pdfbox.pdmodel.interactive.viewerpreferences;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This is the document viewing preferences.
 *
 * @author Ben Litchfield
 */
public class PDViewerPreferences implements COSObjectable
{
    
    /**
     * From PDF Reference: "Neither document outline nor thumbnail images visible".
     * 
     * @deprecated use {@link NON_FULL_SCREEN_PAGE_MODE} instead
     */
    public static final String NON_FULL_SCREEN_PAGE_MODE_USE_NONE = "UseNone";
    /**
     * From PDF Reference: "Document outline visible".
     * 
     * @deprecated use {@link NON_FULL_SCREEN_PAGE_MODE} instead
     */
    public static final String NON_FULL_SCREEN_PAGE_MODE_USE_OUTLINES = "UseOutlines";
    /**
     * From PDF Reference: "Thumbnail images visible".
     * 
     * @deprecated use {@link NON_FULL_SCREEN_PAGE_MODE} instead
     */
    public static final String NON_FULL_SCREEN_PAGE_MODE_USE_THUMBS = "UseThumbs";
    /**
     * From PDF Reference: "Optional content group panel visible".
     * 
     * @deprecated use {@link NON_FULL_SCREEN_PAGE_MODE} instead
     */
    public static final String NON_FULL_SCREEN_PAGE_MODE_USE_OPTIONAL_CONTENT = "UseOC";

    /**
     * Enumeration containing all valid values for NonFullScreenPageMode.
     */
    public enum NON_FULL_SCREEN_PAGE_MODE
    {
        /**
         *  From PDF Reference: "Neither document outline nor thumbnail images visible".
         */
        UseNone,
        /**
         * From PDF Reference: "Document outline visible".
         */
        UseOutlines, 
        /**
         * From PDF Reference: "Thumbnail images visible".
         */
        UseThumbs, 
        /**
         * From PDF Reference: "Optional content group panel visible".
         */
        UseOC
    }

    /**
     * Reading direction.
     * 
     * @deprecated use {@link READING_DIRECTION} instead
     */
    public static final String READING_DIRECTION_L2R = "L2R";
    /**
     * Reading direction.
     * 
     * @deprecated use {@link READING_DIRECTION} instead
     */
    public static final String READING_DIRECTION_R2L = "R2L";
    /**
     * Enumeration containing all valid values for ReadingDirection.
     */
    public enum READING_DIRECTION
    {
        /**
         * left to right.
         */
        L2R,
        /**
         * right to left.
         */
        R2L
    }

    /**
     * Boundary constant.
     * 
     * @deprecated use {@link BOUNDARY} instead
     */
    public static final String BOUNDARY_MEDIA_BOX = "MediaBox";
    /**
     * Boundary constant.
     * 
     * @deprecated use {@link BOUNDARY} instead
     */
    public static final String BOUNDARY_CROP_BOX = "CropBox";
    /**
     * Boundary constant.
     * 
     * @deprecated use {@link BOUNDARY} instead
     */
    public static final String BOUNDARY_BLEED_BOX = "BleedBox";
    /**
     * Boundary constant.
     * 
     * @deprecated use {@link BOUNDARY} instead
     */
    public static final String BOUNDARY_TRIM_BOX = "TrimBox";
    /**
     * Boundary constant.
     * 
     * @deprecated use {@link BOUNDARY} instead
     */
    public static final String BOUNDARY_ART_BOX = "ArtBox";
    /**
     * Enumeration containing all valid values for boundaries.
     */
    public enum BOUNDARY
    {
        /**
         * use media box as boundary.
         */
        MediaBox, 
        /**
         * use crop box as boundary.
         */
        CropBox, 
        /**
         * use bleed box as boundary.
         */
        BleedBox, 
        /**
         * use trim box as boundary.
         */
        TrimBox, 
        /**
         * use art box as boundary.
         */
        ArtBox
    }

    /**
     * Enumeration containing all valid values for duplex.
     */
    public enum DUPLEX
    {
        /**
         * simplex printing.
         */
        Simplex,
        /**
         * duplex printing, flip at short edge.
         */
        DuplexFlipShortEdge, 
        /**
         * duplex printing, flip at long edge.
         */
        DuplexFlipLongEdge
    }

    /**
     * Enumeration containing all valid values for printscaling.
     */
    public enum PRINT_SCALING
    {
        /**
         * no scaling.
         */
        None, 
        /**
         * use app default.
         */
        AppDefault
    }

    private final COSDictionary prefs;

    /**
     * Constructor that is used for a preexisting dictionary.
     *
     * @param dic The underlying dictionary.
     */
    public PDViewerPreferences( COSDictionary dic )
    {
        prefs = dic;
    }

    /**
     * This will get the underlying dictionary that this object wraps.
     *
     * @return The underlying info dictionary.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return prefs;
    }

    /**
     * Get the toolbar preference.
     *
     * @return the toolbar preference.
     */
    public boolean hideToolbar()
    {
        return prefs.getBoolean( COSName.HIDE_TOOLBAR, false );
    }

    /**
     * Set the toolbar preference.
     *
     * @param value Set the toolbar preference.
     */
    public void setHideToolbar( boolean value )
    {
        prefs.setBoolean( COSName.HIDE_TOOLBAR, value );
    }

    /**
     * Get the menubar preference.
     *
     * @return the menubar preference.
     */
    public boolean hideMenubar()
    {
        return prefs.getBoolean( COSName.HIDE_MENUBAR, false );
    }

    /**
     * Set the menubar preference.
     *
     * @param value Set the menubar preference.
     */
    public void setHideMenubar( boolean value )
    {
        prefs.setBoolean( COSName.HIDE_MENUBAR, value );
    }

    /**
     * Get the window UI preference.
     *
     * @return the window UI preference.
     */
    public boolean hideWindowUI()
    {
        return prefs.getBoolean( COSName.HIDE_WINDOWUI, false );
    }

    /**
     * Set the window UI preference.
     *
     * @param value Set the window UI preference.
     */
    public void setHideWindowUI( boolean value )
    {
        prefs.setBoolean( COSName.HIDE_WINDOWUI, value );
    }

    /**
     * Get the fit window preference.
     *
     * @return the fit window preference.
     */
    public boolean fitWindow()
    {
        return prefs.getBoolean( COSName.FIT_WINDOW, false );
    }

    /**
     * Set the fit window preference.
     *
     * @param value Set the fit window preference.
     */
    public void setFitWindow( boolean value )
    {
        prefs.setBoolean( COSName.FIT_WINDOW, value );
    }

    /**
     * Get the center window preference.
     *
     * @return the center window preference.
     */
    public boolean centerWindow()
    {
        return prefs.getBoolean( COSName.CENTER_WINDOW, false );
    }

    /**
     * Set the center window preference.
     *
     * @param value Set the center window preference.
     */
    public void setCenterWindow( boolean value )
    {
        prefs.setBoolean( COSName.CENTER_WINDOW, value );
    }

    /**
     * Get the display doc title preference.
     *
     * @return the display doc title preference.
     */
    public boolean displayDocTitle()
    {
        return prefs.getBoolean( COSName.DISPLAY_DOC_TITLE, false );
    }

    /**
     * Set the display doc title preference.
     *
     * @param value Set the display doc title preference.
     */
    public void setDisplayDocTitle( boolean value )
    {
        prefs.setBoolean( COSName.DISPLAY_DOC_TITLE, value );
    }

    /**
     * Get the non full screen page mode preference.
     *
     * @return the non full screen page mode preference.
     */
    public String getNonFullScreenPageMode()
    {
        return prefs.getNameAsString( COSName.NON_FULL_SCREEN_PAGE_MODE, 
                NON_FULL_SCREEN_PAGE_MODE.UseNone.toString());
    }

    /**
     * Set the non full screen page mode preference.
     *
     * @param value Set the non full screen page mode preference.
     */
    public void setNonFullScreenPageMode( NON_FULL_SCREEN_PAGE_MODE value )
    {
        prefs.setName( COSName.NON_FULL_SCREEN_PAGE_MODE, value.toString() );
    }

    /**
     * Set the non full screen page mode preference.
     *
     * @param value Set the non full screen page mode preference.
     * 
     * @deprecated
     */
    public void setNonFullScreenPageMode( String value )
    {
        prefs.setName( COSName.NON_FULL_SCREEN_PAGE_MODE, value );
    }

    /**
     * Get the reading direction preference.
     *
     * @return the reading direction preference.
     */
    public String getReadingDirection()
    {
        return prefs.getNameAsString( COSName.DIRECTION, READING_DIRECTION.L2R.toString());
    }

    /**
     * Set the reading direction preference.
     *
     * @param value Set the reading direction preference.
     */
    public void setReadingDirection( READING_DIRECTION value )
    {
        prefs.setName( COSName.DIRECTION, value.toString() );
    }

    /**
     * Set the reading direction preference.
     *
     * @param value Set the reading direction preference.
     * 
     * @deprecated
     */
    public void setReadingDirection( String value )
    {
        prefs.setName( COSName.DIRECTION, value);
    }

    /**
     * Get the ViewArea preference.  See BOUNDARY enumeration.
     *
     * @return the ViewArea preference.
     */
    public String getViewArea()
    {
        return prefs.getNameAsString( COSName.VIEW_AREA, BOUNDARY.CropBox.toString());
    }

    /**
     * Set the ViewArea preference.  See BOUNDARY_XXX constants.
     *
     * @param value Set the ViewArea preference.
     * 
     * @deprecated
     */
    public void setViewArea( String value )
    {
        prefs.setName( COSName.VIEW_AREA, value );
    }

    /**
     * Set the ViewArea preference.  See BOUNDARY enumeration.
     *
     * @param value Set the ViewArea preference.
     */
    public void setViewArea( BOUNDARY value )
    {
        prefs.setName( COSName.VIEW_AREA, value.toString() );
    }

    /**
     * Get the ViewClip preference.  See BOUNDARY enumeration.
     *
     * @return the ViewClip preference.
     */
    public String getViewClip()
    {
        return prefs.getNameAsString( COSName.VIEW_CLIP, BOUNDARY.CropBox.toString());
    }

    /**
     * Set the ViewClip preference.  See BOUNDARY enumeration.
     *
     * @param value Set the ViewClip preference.
     */
    public void setViewClip( BOUNDARY value )
    {
        prefs.setName( COSName.VIEW_CLIP, value.toString() );
    }

    /**
     * Set the ViewClip preference.  See BOUNDARY_XXX constants.
     *
     * @param value Set the ViewClip preference.
     * 
     * @deprecated
     */
    public void setViewClip( String value )
    {
        prefs.setName( COSName.VIEW_CLIP, value );
    }

    /**
     * Get the PrintArea preference.  See BOUNDARY enumeration.
     *
     * @return the PrintArea preference.
     */
    public String getPrintArea()
    {
        return prefs.getNameAsString( COSName.PRINT_AREA, BOUNDARY.CropBox.toString());
    }

    /**
     * Set the PrintArea preference.  See BOUNDARY_XXX constants.
     *
     * @param value Set the PrintArea preference.
     * 
     * @deprecated
     */
    public void setPrintArea( String value )
    {
        prefs.setName( COSName.PRINT_AREA, value );
    }

    /**
     * Set the PrintArea preference.  See BOUNDARY enumeration.
     *
     * @param value Set the PrintArea preference.
     */
    public void setPrintArea( BOUNDARY value )
    {
        prefs.setName( COSName.PRINT_AREA, value.toString() );
    }

    /**
     * Get the PrintClip preference.  See BOUNDARY enumeration.
     *
     * @return the PrintClip preference.
     */
    public String getPrintClip()
    {
        return prefs.getNameAsString( COSName.PRINT_CLIP, BOUNDARY.CropBox.toString());
    }

    /**
     * Set the PrintClip preference.  See BOUNDARY_XXX constants.
     *
     * @param value Set the PrintClip preference.
     * 
     * @deprecated
     */
    public void setPrintClip( String value )
    {
        prefs.setName( COSName.PRINT_CLIP, value );
    }
    
    /**
     * Set the PrintClip preference.  See BOUNDARY enumeration.
     *
     * @param value Set the PrintClip preference.
     */
    public void setPrintClip( BOUNDARY value )
    {
        prefs.setName( COSName.PRINT_CLIP, value.toString() );
    }
    
    /**
     * Get the Duplex preference.  See DUPLEX enumeration.
     *
     * @return the Duplex preference.
     */
    public String getDuplex()
    {
        return prefs.getNameAsString( COSName.DUPLEX );
    }

    /**
     * Set the Duplex preference.  See DUPLEX enumeration.
     *
     * @param value Set the Duplex preference.
     */
    public void setDuplex( DUPLEX value )
    {
        prefs.setName( COSName.DUPLEX, value.toString() );
    }
    
    /**
     * Get the PrintScaling preference.  See PRINT_SCALING enumeration.
     *
     * @return the PrintScaling preference.
     */
    public String getPrintScaling()
    {
        return prefs.getNameAsString( COSName.PRINT_SCALING , PRINT_SCALING.AppDefault.toString());
    }

    /**
     * Set the PrintScaling preference.  See PRINT_SCALING enumeration.
     *
     * @param value Set the PrintScaling preference.
     */
    public void setPrintScaling( PRINT_SCALING value )
    {
        prefs.setName( COSName.PRINT_SCALING, value.toString() );
    }
}
