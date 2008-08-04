/**
 * Copyright (c) 2003-2005, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdmodel.interactive.viewerpreferences;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSObjectable;

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