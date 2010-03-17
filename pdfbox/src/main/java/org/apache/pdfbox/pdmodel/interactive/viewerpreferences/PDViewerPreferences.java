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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;

import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This is the document viewing preferences.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDViewerPreferences implements COSObjectable
{
    /**
     * From PDF Reference: "Neither document outline nor thumbnail images visible".
     */
    public static final String NON_FULL_SCREEN_PAGE_MODE_USE_NONE = "UseNone";
    /**
     * From PDF Reference: "Document outline visible".
     */
    public static final String NON_FULL_SCREEN_PAGE_MODE_USE_OUTLINES = "UseOutlines";
    /**
     * From PDF Reference: "Thumbnail images visible".
     */
    public static final String NON_FULL_SCREEN_PAGE_MODE_USE_THUMBS = "UseThumbs";
    /**
     * From PDF Reference: "Optional content group panel visible".
     */
    public static final String NON_FULL_SCREEN_PAGE_MODE_USE_OPTIONAL_CONTENT = "UseOC";

    /**
     * Reading direction.
     */
    public static final String READING_DIRECTION_L2R = "L2R";
    /**
     * Reading direction.
     */
    public static final String READING_DIRECTION_R2L = "R2L";

    /**
     * Boundary constant.
     */
    public static final String BOUNDARY_MEDIA_BOX = "MediaBox";
    /**
     * Boundary constant.
     */
    public static final String BOUNDARY_CROP_BOX = "CropBox";
    /**
     * Boundary constant.
     */
    public static final String BOUNDARY_BLEED_BOX = "BleedBox";
    /**
     * Boundary constant.
     */
    public static final String BOUNDARY_TRIM_BOX = "TrimBox";
    /**
     * Boundary constant.
     */
    public static final String BOUNDARY_ART_BOX = "ArtBox";


    private COSDictionary prefs;

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
    public COSDictionary getDictionary()
    {
        return prefs;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
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
        return prefs.getBoolean( "HideToolbar", false );
    }

    /**
     * Set the toolbar preference.
     *
     * @param value Set the toolbar preference.
     */
    public void setHideToolbar( boolean value )
    {
        prefs.setBoolean( "HideToolbar", value );
    }

    /**
     * Get the menubar preference.
     *
     * @return the menubar preference.
     */
    public boolean hideMenubar()
    {
        return prefs.getBoolean( "HideMenubar", false );
    }

    /**
     * Set the menubar preference.
     *
     * @param value Set the menubar preference.
     */
    public void setHideMenubar( boolean value )
    {
        prefs.setBoolean( "HideMenubar", value );
    }

    /**
     * Get the window UI preference.
     *
     * @return the window UI preference.
     */
    public boolean hideWindowUI()
    {
        return prefs.getBoolean( "HideWindowUI", false );
    }

    /**
     * Set the window UI preference.
     *
     * @param value Set the window UI preference.
     */
    public void setHideWindowUI( boolean value )
    {
        prefs.setBoolean( "HideWindowUI", value );
    }

    /**
     * Get the fit window preference.
     *
     * @return the fit window preference.
     */
    public boolean fitWindow()
    {
        return prefs.getBoolean( "FitWindow", false );
    }

    /**
     * Set the fit window preference.
     *
     * @param value Set the fit window preference.
     */
    public void setFitWindow( boolean value )
    {
        prefs.setBoolean( "FitWindow", value );
    }

    /**
     * Get the center window preference.
     *
     * @return the center window preference.
     */
    public boolean centerWindow()
    {
        return prefs.getBoolean( "CenterWindow", false );
    }

    /**
     * Set the center window preference.
     *
     * @param value Set the center window preference.
     */
    public void setCenterWindow( boolean value )
    {
        prefs.setBoolean( "CenterWindow", value );
    }

    /**
     * Get the display doc title preference.
     *
     * @return the display doc title preference.
     */
    public boolean displayDocTitle()
    {
        return prefs.getBoolean( "DisplayDocTitle", false );
    }

    /**
     * Set the display doc title preference.
     *
     * @param value Set the display doc title preference.
     */
    public void setDisplayDocTitle( boolean value )
    {
        prefs.setBoolean( "DisplayDocTitle", value );
    }

    /**
     * Get the non full screen page mode preference.
     *
     * @return the non full screen page mode preference.
     */
    public String getNonFullScreenPageMode()
    {
        return prefs.getNameAsString( "NonFullScreenPageMode", NON_FULL_SCREEN_PAGE_MODE_USE_NONE);
    }

    /**
     * Set the non full screen page mode preference.
     *
     * @param value Set the non full screen page mode preference.
     */
    public void setNonFullScreenPageMode( String value )
    {
        prefs.setName( "NonFullScreenPageMode", value );
    }

    /**
     * Get the reading direction preference.
     *
     * @return the reading direction preference.
     */
    public String getReadingDirection()
    {
        return prefs.getNameAsString( "Direction", READING_DIRECTION_L2R);
    }

    /**
     * Set the reading direction preference.
     *
     * @param value Set the reading direction preference.
     */
    public void setReadingDirection( String value )
    {
        prefs.setName( "Direction", value );
    }

    /**
     * Get the ViewArea preference.  See BOUNDARY_XXX constants.
     *
     * @return the ViewArea preference.
     */
    public String getViewArea()
    {
        return prefs.getNameAsString( "ViewArea", BOUNDARY_CROP_BOX);
    }

    /**
     * Set the ViewArea preference.  See BOUNDARY_XXX constants.
     *
     * @param value Set the ViewArea preference.
     */
    public void setViewArea( String value )
    {
        prefs.setName( "ViewArea", value );
    }

    /**
     * Get the ViewClip preference.  See BOUNDARY_XXX constants.
     *
     * @return the ViewClip preference.
     */
    public String getViewClip()
    {
        return prefs.getNameAsString( "ViewClip", BOUNDARY_CROP_BOX);
    }

    /**
     * Set the ViewClip preference.  See BOUNDARY_XXX constants.
     *
     * @param value Set the ViewClip preference.
     */
    public void setViewClip( String value )
    {
        prefs.setName( "ViewClip", value );
    }

    /**
     * Get the PrintArea preference.  See BOUNDARY_XXX constants.
     *
     * @return the PrintArea preference.
     */
    public String getPrintArea()
    {
        return prefs.getNameAsString( "PrintArea", BOUNDARY_CROP_BOX);
    }

    /**
     * Set the PrintArea preference.  See BOUNDARY_XXX constants.
     *
     * @param value Set the PrintArea preference.
     */
    public void setPrintArea( String value )
    {
        prefs.setName( "PrintArea", value );
    }

    /**
     * Get the PrintClip preference.  See BOUNDARY_XXX constants.
     *
     * @return the PrintClip preference.
     */
    public String getPrintClip()
    {
        return prefs.getNameAsString( "PrintClip", BOUNDARY_CROP_BOX);
    }

    /**
     * Set the PrintClip preference.  See BOUNDARY_XXX constants.
     *
     * @param value Set the PrintClip preference.
     */
    public void setPrintClip( String value )
    {
        prefs.setName( "PrintClip", value );
    }
}
