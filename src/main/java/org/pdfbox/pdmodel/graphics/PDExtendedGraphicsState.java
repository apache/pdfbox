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
package org.pdfbox.pdmodel.graphics;

import org.pdfbox.cos.COSArray;
import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSFloat;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSNumber;

import org.pdfbox.pdmodel.common.COSObjectable;

import java.io.IOException;

import java.util.Iterator;

/**
 * This class represents the graphics state dictionary that is stored in the PDF document.
 * The PDGraphicsStateValue holds the current runtime values as a stream is being executed.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PDExtendedGraphicsState implements COSObjectable
{
    private static final COSName LW = COSName.getPDFName( "LW" );
    private static final COSName LC = COSName.getPDFName( "LC" );
    private static final COSName LJ = COSName.getPDFName( "LJ" );
    private static final COSName ML = COSName.getPDFName( "ML" );
    private static final COSName D = COSName.getPDFName( "D" );
    private static final COSName RI = COSName.getPDFName( "RI" );
    private static final COSName OP = COSName.getPDFName( "OP" );
    private static final COSName OP_NS = COSName.getPDFName( "op" );
    private static final COSName OPM = COSName.getPDFName( "OPM" );
    private static final COSName FONT = COSName.getPDFName( "Font" );
    private static final COSName FL = COSName.getPDFName( "FL" );
    private static final COSName SM = COSName.getPDFName( "SM" );
    private static final COSName SA = COSName.getPDFName( "SA" );
    private static final COSName CA = COSName.getPDFName( "CA" );
    private static final COSName CA_NS = COSName.getPDFName( "ca" );
    private static final COSName AIS = COSName.getPDFName( "AIS" );
    private static final COSName TK = COSName.getPDFName( "TK" );

    /**
     * Rendering intent constants, see PDF Reference 1.5 Section 4.5.4 Rendering Intents.
     */
    public static final String RENDERING_INTENT_ABSOLUTE_COLORIMETRIC = "AbsoluteColorimetric";
    /**
     * Rendering intent constants, see PDF Reference 1.5 Section 4.5.4 Rendering Intents.
     */
    public static final String RENDERING_INTENT_RELATIVE_COLORIMETRIC = "RelativeColorimetric";
    /**
     * Rendering intent constants, see PDF Reference 1.5 Section 4.5.4 Rendering Intents.
     */
    public static final String RENDERING_INTENT_SATURATION = "Saturation";
    /**
     * Rendering intent constants, see PDF Reference 1.5 Section 4.5.4 Rendering Intents.
     */
    public static final String RENDERING_INTENT_PERCEPTUAL = "Perceptual";


    private COSDictionary graphicsState;

    /**
     * Default constructor, creates blank graphics state.
     */
    public PDExtendedGraphicsState()
    {
        graphicsState = new COSDictionary();
        graphicsState.setItem( COSName.TYPE, COSName.getPDFName( "ExtGState" ) );
    }

    /**
     * Create a graphics state from an existing dictionary.
     *
     * @param dictionary The existing graphics state.
     */
    public PDExtendedGraphicsState( COSDictionary dictionary )
    {
        graphicsState = dictionary;
    }

    /**
     * This will implement the gs operator.
     *
     * @param gs The state to copy this dictionaries values into.
     *
     * @throws IOException If there is an error copying font information.
     */
    public void copyIntoGraphicsState( PDGraphicsState gs ) throws IOException
    {
        Iterator keys = graphicsState.keyList().iterator();
        while( keys.hasNext() )
        {
            COSName key = (COSName)keys.next();
            if( key.equals( LW ) )
            {
                gs.setLineWidth( getLineWidth().doubleValue() );
            }
            else if( key.equals( LC ) )
            {
                gs.setLineCap( getLineCapStyle() );
            }
            else if( key.equals( LJ ) )
            {
                gs.setLineJoin( getLineJoinStyle() );
            }
            else if( key.equals( ML ) )
            {
                gs.setMiterLimit( getMiterLimit().doubleValue() );
            }
            else if( key.equals( D ) )
            {
                gs.setLineDashPattern( getLineDashPattern() );
            }
            else if( key.equals( RI ) )
            {
                gs.setRenderingIntent( getRenderingIntent() );
            }
            else if( key.equals( OPM ) )
            {
                gs.setOverprintMode( getOverprintMode().doubleValue() );
            }
            else if( key.equals( FONT ) )
            {
                PDFontSetting setting = getFontSetting();
                gs.getTextState().setFont( setting.getFont() );
                gs.getTextState().setFontSize( setting.getFontSize() );
            }
            else if( key.equals( FL ) )
            {
                gs.setFlatness( getFlatnessTolerance().floatValue() );
            }
            else if( key.equals( SM ) )
            {
                gs.setSmoothness( getSmoothnessTolerance().floatValue() );
            }
            else if( key.equals( SA ) )
            {
                gs.setStrokeAdjustment( getAutomaticStrokeAdjustment() );
            }
            else if( key.equals( CA ) )
            {
                gs.setAlphaConstants( getStrokingAlpaConstant().floatValue() );
            }/**
            else if( key.equals( CA_NS ) )
            {
            }**/
            else if( key.equals( AIS ) )
            {
                gs.setAlphaSource( getAlphaSourceFlag() );
            }
            else if( key.equals( TK ) )
            {
                gs.getTextState().setKnockoutFlag( getTextKnockoutFlag() );
            }
        }
    }

    /**
     * This will get the underlying dictionary that this class acts on.
     *
     * @return The underlying dictionary for this class.
     */
    public COSDictionary getCOSDictionary()
    {
        return graphicsState;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return graphicsState;
    }

    /**
     * This will get the line width.  This will return null if there is no line width
     *
     * @return null or the LW value of the dictionary.
     */
    public Float getLineWidth()
    {
        return getFloatItem( LW );
    }

    /**
     * This will set the line width.
     *
     * @param width The line width for the object.
     */
    public void setLineWidth( Float width )
    {
        setFloatItem( LW, width );
    }

    /**
     * This will get the line cap style.
     *
     * @return null or the LC value of the dictionary.
     */
    public int getLineCapStyle()
    {
        return graphicsState.getInt( LC );
    }

    /**
     * This will set the line cap style for the graphics state.
     *
     * @param style The new line cap style to set.
     */
    public void setLineCapStyle( int style )
    {
        graphicsState.setInt( LC, style );
    }

    /**
     * This will get the line join style.
     *
     * @return null or the LJ value in the dictionary.
     */
    public int getLineJoinStyle()
    {
        return graphicsState.getInt( LJ );
    }

    /**
     * This will set the line join style.
     *
     * @param style The new line join style.
     */
    public void setLineJoinStyle( int style )
    {
        graphicsState.setInt( LJ, style );
    }


    /**
     * This will get the miter limit.
     *
     * @return null or the ML value in the dictionary.
     */
    public Float getMiterLimit()
    {
        return getFloatItem( ML );
    }

    /**
     * This will set the miter limit for the graphics state.
     *
     * @param miterLimit The new miter limit value
     */
    public void setMiterLimit( Float miterLimit )
    {
        setFloatItem( ML, miterLimit );
    }

    /**
     * This will get the dash pattern.
     *
     * @return null or the D value in the dictionary.
     */
    public PDLineDashPattern getLineDashPattern()
    {
        PDLineDashPattern retval = null;
        COSArray dp = (COSArray)graphicsState.getDictionaryObject( D );
        if( dp != null )
        {
            retval = new PDLineDashPattern( dp );
        }
        return retval;
    }

    /**
     * This will set the dash pattern for the graphics state.
     *
     * @param dashPattern The dash pattern
     */
    public void setLineDashPattern( PDLineDashPattern dashPattern )
    {
        graphicsState.setItem( D, dashPattern.getCOSObject() );
    }

    /**
     * This will get the rendering intent.
     *
     * @return null or the RI value in the dictionary.
     */
    public String getRenderingIntent()
    {
        return graphicsState.getNameAsString( "RI" );
    }

    /**
     * This will set the rendering intent for the graphics state.
     *
     * @param ri The new rendering intent
     */
    public void setRenderingIntent( String ri )
    {
        graphicsState.setName( "RI", ri );
    }

    /**
     * This will get the overprint control.
     *
     * @return The overprint control or null if one has not been set.
     */
    public boolean getStrokingOverprintControl()
    {
        return graphicsState.getBoolean( OP, false );
    }

    /**
     * This will get the overprint control(OP).
     *
     * @param op The overprint control.
     */
    public void setStrokingOverprintControl( boolean op )
    {
        graphicsState.setBoolean( OP, op );
    }

    /**
     * This will get the overprint control for non stroking operations.  If this
     * value is null then the regular overprint control value will be returned.
     *
     * @return The overprint control or null if one has not been set.
     */
    public boolean getNonStrokingOverprintControl()
    {
        return graphicsState.getBoolean( OP_NS, getStrokingOverprintControl() );
    }

    /**
     * This will get the overprint control(OP).
     *
     * @param op The overprint control.
     */
    public void setNonStrokingOverprintControl( boolean op )
    {
        graphicsState.setBoolean( OP_NS, op );
    }

    /**
     * This will get the overprint control mode.
     *
     * @return The overprint control mode or null if one has not been set.
     */
    public Float getOverprintMode()
    {
        return getFloatItem( OPM );
    }

    /**
     * This will get the overprint mode(OPM).
     *
     * @param overprintMode The overprint mode
     */
    public void setOverprintMode( Float overprintMode )
    {
        setFloatItem( OPM, overprintMode );
    }

    /**
     * This will get the font setting of the graphics state.
     *
     * @return The font setting.
     */
    public PDFontSetting getFontSetting()
    {
        PDFontSetting setting = null;
        COSArray font = (COSArray)graphicsState.getDictionaryObject( FONT );
        if( font != null )
        {
            setting = new PDFontSetting( font );
        }
        return setting;
    }

    /**
     * This will set the font setting for this graphics state.
     *
     * @param fs The new font setting.
     */
    public void setFontSetting( PDFontSetting fs )
    {
        graphicsState.setItem( FONT, fs );
    }

    /**
     * This will get the flatness tolerance.
     *
     * @return The flatness tolerance or null if one has not been set.
     */
    public Float getFlatnessTolerance()
    {
        return getFloatItem( FL );
    }

    /**
     * This will get the flatness tolerance.
     *
     * @param flatness The new flatness tolerance
     */
    public void setFlatnessTolerance( Float flatness )
    {
        setFloatItem( FL, flatness );
    }

    /**
     * This will get the smothness tolerance.
     *
     * @return The smothness tolerance or null if one has not been set.
     */
    public Float getSmoothnessTolerance()
    {
        return getFloatItem( SM );
    }

    /**
     * This will get the smoothness tolerance.
     *
     * @param smoothness The new smoothness tolerance
     */
    public void setSmoothnessTolerance( Float smoothness )
    {
        setFloatItem( SM, smoothness );
    }

    /**
     * This will get the automatic stroke adjustment flag.
     *
     * @return The automatic stroke adjustment flag or null if one has not been set.
     */
    public boolean getAutomaticStrokeAdjustment()
    {
        return graphicsState.getBoolean( SA,false );
    }

    /**
     * This will get the automatic stroke adjustment flag.
     *
     * @param sa The new automatic stroke adjustment flag.
     */
    public void setAutomaticStrokeAdjustment( boolean sa )
    {
        graphicsState.setBoolean( SA, sa );
    }

    /**
     * This will get the stroking alpha constant.
     *
     * @return The stroking alpha constant or null if one has not been set.
     */
    public Float getStrokingAlpaConstant()
    {
        return getFloatItem( CA );
    }

    /**
     * This will get the stroking alpha constant.
     *
     * @param alpha The new stroking alpha constant.
     */
    public void setStrokingAlphaConstant( Float alpha )
    {
        setFloatItem( CA, alpha );
    }

    /**
     * This will get the non stroking alpha constant.
     *
     * @return The non stroking alpha constant or null if one has not been set.
     */
    public Float getNonStrokingAlpaConstant()
    {
        return getFloatItem( CA_NS );
    }

    /**
     * This will get the non stroking alpha constant.
     *
     * @param alpha The new non stroking alpha constant.
     */
    public void setNonStrokingAlphaConstant( Float alpha )
    {
        setFloatItem( CA_NS, alpha );
    }

    /**
     * This will get the alpha source flag.
     *
     * @return The alpha source flag.
     */
    public boolean getAlphaSourceFlag()
    {
        return graphicsState.getBoolean( AIS, false );
    }

    /**
     * This will get the alpha source flag.
     *
     * @param alpha The alpha source flag.
     */
    public void setAlphaSourceFlag( boolean alpha )
    {
        graphicsState.setBoolean( AIS, alpha );
    }

    /**
     * This will get the text knockout flag.
     *
     * @return The text knockout flag.
     */
    public boolean getTextKnockoutFlag()
    {
        return graphicsState.getBoolean( TK,true );
    }

    /**
     * This will get the text knockout flag.
     *
     * @param tk The text knockout flag.
     */
    public void setTextKnockoutFlag( boolean tk )
    {
        graphicsState.setBoolean( TK, tk );
    }

    /**
     * This will get a float item from the dictionary.
     *
     * @param key The key to the item.
     *
     * @return The value for that item.
     */
    private Float getFloatItem( COSName key )
    {
        Float retval = null;
        COSNumber value = (COSNumber)graphicsState.getDictionaryObject( key );
        if( value != null )
        {
            retval = new Float( value.floatValue() );
        }
        return retval;
    }

    /**
     * This will set a float object.
     *
     * @param key The key to the data that we are setting.
     * @param value The value that we are setting.
     */
    private void setFloatItem( COSName key, Float value )
    {
        if( value == null )
        {
            graphicsState.removeItem( key );
        }
        else
        {
            graphicsState.setItem( key, new COSFloat( value.floatValue() ) );
        }
    }
}
