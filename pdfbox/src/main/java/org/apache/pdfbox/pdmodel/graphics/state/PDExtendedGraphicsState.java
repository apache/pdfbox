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
package org.apache.pdfbox.pdmodel.graphics.state;

import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.graphics.PDFontSetting;
import org.apache.pdfbox.pdmodel.graphics.PDLineDashPattern;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;

/**
 * An extended graphics state dictionary.
 *
 * @author Ben Litchfield
 */
public class PDExtendedGraphicsState implements COSObjectable
{
    private final COSDictionary dict;

    /**
     * Default constructor, creates blank graphics state.
     */
    public PDExtendedGraphicsState()
    {
        dict = new COSDictionary();
        dict.setItem(COSName.TYPE, COSName.EXT_G_STATE);
    }

    /**
     * Create a graphics state from an existing dictionary.
     *
     * @param dictionary The existing graphics state.
     */
    public PDExtendedGraphicsState(COSDictionary dictionary)
    {
        dict = dictionary;
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
        for( COSName key : dict.keySet() )
        {
            if( key.equals( COSName.LW ) )
            {
                gs.setLineWidth( getLineWidth() );
            }
            else if( key.equals( COSName.LC ) )
            {
                gs.setLineCap( getLineCapStyle() );
            }
            else if( key.equals( COSName.LJ ) )
            {
                gs.setLineJoin( getLineJoinStyle() );
            }
            else if( key.equals( COSName.ML ) )
            {
                gs.setMiterLimit( getMiterLimit() );
            }
            else if( key.equals( COSName.D ) )
            {
                gs.setLineDashPattern( getLineDashPattern() );
            }
            else if( key.equals( COSName.RI ) )
            {
                gs.setRenderingIntent( getRenderingIntent() );
            }
            else if( key.equals( COSName.OPM ) )
            {
                gs.setOverprintMode( getOverprintMode().doubleValue() );
            }
            else if( key.equals( COSName.OP ) )
            {
                gs.setOverprint( getStrokingOverprintControl());
            }
            else if( key.equals( COSName.OP_NS ) )
            {
                gs.setNonStrokingOverprint(getNonStrokingOverprintControl());
            }
            else if( key.equals( COSName.FONT ) )
            {
                PDFontSetting setting = getFontSetting();
                if (setting != null)
                {
                    gs.getTextState().setFont( setting.getFont() );
                    gs.getTextState().setFontSize( setting.getFontSize() );
                }
            }
            else if( key.equals( COSName.FL ) )
            {
                gs.setFlatness( getFlatnessTolerance() );
            }
            else if( key.equals( COSName.SM ) )
            {
                gs.setSmoothness( getSmoothnessTolerance() );
            }
            else if( key.equals( COSName.SA ) )
            {
                gs.setStrokeAdjustment( getAutomaticStrokeAdjustment() );
            }
            else if( key.equals( COSName.CA ) )
            {
                gs.setAlphaConstant(getStrokingAlphaConstant());
            }
            else if( key.equals( COSName.CA_NS ) )
            {
                gs.setNonStrokeAlphaConstant(getNonStrokingAlphaConstant() );
            }
            else if( key.equals( COSName.AIS ) )
            {
                gs.setAlphaSource( getAlphaSourceFlag() );
            }
            else if( key.equals( COSName.TK ) )
            {
                gs.getTextState().setKnockoutFlag( getTextKnockoutFlag() );
            }
            else if( key.equals( COSName.SMASK ) ) 
            {
                PDSoftMask softmask = getSoftMask();
                if (softmask != null)
                {
                    // Softmask must know the CTM at the time the ExtGState is activated. Read
                    // https://bugs.ghostscript.com/show_bug.cgi?id=691157#c7 for a good explanation.
                    softmask.setInitialTransformationMatrix(gs.getCurrentTransformationMatrix().clone());
                }
                gs.setSoftMask(softmask);
            }
            else if( key.equals( COSName.BM ) ) 
            {
                gs.setBlendMode( getBlendMode() );
            }
            else if (key.equals(COSName.TR))
            {
                if (dict.containsKey(COSName.TR2))
                {
                    // "If both TR and TR2 are present in the same graphics state parameter dictionary, 
                    // TR2 shall take precedence."
                    continue;
                }
                gs.setTransfer(getTransfer());
            }
            else if (key.equals(COSName.TR2))
            {
                gs.setTransfer(getTransfer2());
            }
        }
    }

    /**
     * This will get the underlying dictionary that this class acts on.
     *
     * @return The underlying dictionary for this class.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return dict;
    }

    /**
     * This will get the line width.  This will return null if there is no line width
     *
     * @return null or the LW value of the dictionary.
     */
    public Float getLineWidth()
    {
        return getFloatItem( COSName.LW );
    }

    /**
     * This will set the line width.
     *
     * @param width The line width for the object.
     */
    public void setLineWidth( Float width )
    {
        setFloatItem( COSName.LW, width );
    }

    /**
     * This will get the line cap style.
     *
     * @return null or the LC value of the dictionary.
     */
    public int getLineCapStyle()
    {
        return dict.getInt( COSName.LC );
    }

    /**
     * This will set the line cap style for the graphics state.
     *
     * @param style The new line cap style to set.
     */
    public void setLineCapStyle( int style )
    {
        dict.setInt(COSName.LC, style);
    }

    /**
     * This will get the line join style.
     *
     * @return null or the LJ value in the dictionary.
     */
    public int getLineJoinStyle()
    {
        return dict.getInt( COSName.LJ );
    }

    /**
     * This will set the line join style.
     *
     * @param style The new line join style.
     */
    public void setLineJoinStyle( int style )
    {
        dict.setInt(COSName.LJ, style);
    }


    /**
     * This will get the miter limit.
     *
     * @return null or the ML value in the dictionary.
     */
    public Float getMiterLimit()
    {
        return getFloatItem( COSName.ML );
    }

    /**
     * This will set the miter limit for the graphics state.
     *
     * @param miterLimit The new miter limit value
     */
    public void setMiterLimit( Float miterLimit )
    {
        setFloatItem( COSName.ML, miterLimit );
    }

    /**
     * This will get the dash pattern.
     *
     * @return null or the D value in the dictionary.
     */
    public PDLineDashPattern getLineDashPattern()
    {
        PDLineDashPattern retval = null;
        COSArray dp = (COSArray) dict.getDictionaryObject( COSName.D );
        if( dp != null )
        {
            COSArray array = new COSArray();
            dp.addAll(dp);
            dp.remove(dp.size() - 1);
            int phase = dp.getInt(dp.size() - 1);

            retval = new PDLineDashPattern( array, phase );
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
        dict.setItem(COSName.D, dashPattern.getCOSObject());
    }

    /**
     * This will get the rendering intent.
     *
     * @return null or the RI value in the dictionary.
     */
    public RenderingIntent getRenderingIntent()
    {
        String ri = dict.getNameAsString( "RI" );
        if (ri != null)
        {
            return RenderingIntent.fromString(ri);
        }
        else
        {
            return null;
        }
    }

    /**
     * This will set the rendering intent for the graphics state.
     *
     * @param ri The new rendering intent
     */
    public void setRenderingIntent( String ri )
    {
        dict.setName("RI", ri);
    }

    /**
     * This will get the overprint control.
     *
     * @return The overprint control or null if one has not been set.
     */
    public boolean getStrokingOverprintControl()
    {
        return dict.getBoolean(COSName.OP, false);
    }

    /**
     * This will get the overprint control(OP).
     *
     * @param op The overprint control.
     */
    public void setStrokingOverprintControl( boolean op )
    {
        dict.setBoolean(COSName.OP, op);
    }

    /**
     * This will get the overprint control for non stroking operations.  If this
     * value is null then the regular overprint control value will be returned.
     *
     * @return The overprint control or null if one has not been set.
     */
    public boolean getNonStrokingOverprintControl()
    {
        return dict.getBoolean( COSName.OP_NS, getStrokingOverprintControl() );
    }

    /**
     * This will get the overprint control(OP).
     *
     * @param op The overprint control.
     */
    public void setNonStrokingOverprintControl( boolean op )
    {
        dict.setBoolean(COSName.OP_NS, op);
    }

    /**
     * This will get the overprint control mode.
     *
     * @return The overprint control mode or null if one has not been set.
     */
    public Float getOverprintMode()
    {
        return getFloatItem(COSName.OPM);
    }

    /**
     * This will get the overprint mode(OPM).
     *
     * @param overprintMode The overprint mode
     */
    public void setOverprintMode( Float overprintMode )
    {
        setFloatItem(COSName.OPM, overprintMode);
    }

    /**
     * This will get the font setting of the graphics state.
     *
     * @return The font setting.
     */
    public PDFontSetting getFontSetting()
    {
        PDFontSetting setting = null;
        COSBase base = dict.getDictionaryObject(COSName.FONT);
        if (base instanceof COSArray)
        {
            COSArray font = (COSArray) base;
            setting = new PDFontSetting(font);
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
        dict.setItem(COSName.FONT, fs);
    }

    /**
     * This will get the flatness tolerance.
     *
     * @return The flatness tolerance or null if one has not been set.
     */
    public Float getFlatnessTolerance()
    {
        return getFloatItem( COSName.FL );
    }

    /**
     * This will get the flatness tolerance.
     *
     * @param flatness The new flatness tolerance
     */
    public void setFlatnessTolerance( Float flatness )
    {
        setFloatItem(COSName.FL, flatness);
    }

    /**
     * This will get the smothness tolerance.
     *
     * @return The smothness tolerance or null if one has not been set.
     */
    public Float getSmoothnessTolerance()
    {
        return getFloatItem( COSName.SM );
    }

    /**
     * This will get the smoothness tolerance.
     *
     * @param smoothness The new smoothness tolerance
     */
    public void setSmoothnessTolerance( Float smoothness )
    {
        setFloatItem( COSName.SM, smoothness );
    }

    /**
     * This will get the automatic stroke adjustment flag.
     *
     * @return The automatic stroke adjustment flag or null if one has not been set.
     */
    public boolean getAutomaticStrokeAdjustment()
    {
        return dict.getBoolean(COSName.SA, false);
    }

    /**
     * This will get the automatic stroke adjustment flag.
     *
     * @param sa The new automatic stroke adjustment flag.
     */
    public void setAutomaticStrokeAdjustment( boolean sa )
    {
        dict.setBoolean(COSName.SA, sa);
    }

    /**
     * This will get the stroking alpha constant.
     *
     * @return The stroking alpha constant or null if one has not been set.
     */
    public Float getStrokingAlphaConstant()
    {
        return getFloatItem(COSName.CA);
    }

    /**
     * This will get the stroking alpha constant.
     *
     * @param alpha The new stroking alpha constant.
     */
    public void setStrokingAlphaConstant( Float alpha )
    {
        setFloatItem(COSName.CA, alpha);
    }

    /**
     * This will get the non stroking alpha constant.
     *
     * @return The non stroking alpha constant or null if one has not been set.
     */
    public Float getNonStrokingAlphaConstant()
    {
        return getFloatItem( COSName.CA_NS );
    }

    /**
     * This will get the non stroking alpha constant.
     *
     * @param alpha The new non stroking alpha constant.
     */
    public void setNonStrokingAlphaConstant( Float alpha )
    {
        setFloatItem( COSName.CA_NS, alpha );
    }

    /**
     * This will get the alpha source flag (“alpha is shape”), that specifies whether the current
     * soft mask and alpha constant shall be interpreted as shape values (true) or opacity values
     * (false).
     *
     * @return The alpha source flag.
     */
    public boolean getAlphaSourceFlag()
    {
        return dict.getBoolean(COSName.AIS, false);
    }

    /**
     * This will get the alpha source flag (“alpha is shape”), that specifies whether the current
     * soft mask and alpha constant shall be interpreted as shape values (true) or opacity values
     * (false).
     *
     * @param alpha The alpha source flag.
     */
    public void setAlphaSourceFlag( boolean alpha )
    {
        dict.setBoolean(COSName.AIS, alpha);
    }

    /**
     * Returns the blending mode stored in the COS dictionary
     *
     * @return the blending mode
     */
    public BlendMode getBlendMode()
    {
        return BlendMode.getInstance(dict.getDictionaryObject(COSName.BM));
    }

    /**
     * Returns the soft mask stored in the COS dictionary
     *
     * @return the soft mask
     */
    public PDSoftMask getSoftMask()
    {
        return PDSoftMask.create(dict.getDictionaryObject(COSName.SMASK));
    }

    /**

    /**
     * This will get the text knockout flag.
     *
     * @return The text knockout flag.
     */
    public boolean getTextKnockoutFlag()
    {
        return dict.getBoolean( COSName.TK,true );
    }

    /**
     * This will get the text knockout flag.
     *
     * @param tk The text knockout flag.
     */
    public void setTextKnockoutFlag( boolean tk )
    {
        dict.setBoolean(COSName.TK, tk);
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
        COSNumber value = (COSNumber) dict.getDictionaryObject( key );
        if( value != null )
        {
            retval = value.floatValue();
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
            dict.removeItem(key);
        }
        else
        {
            dict.setItem(key, new COSFloat(value));
        }
    }

    /**
     * This will get the transfer function of the /TR dictionary.
     *
     * @return The transfer function. According to the PDF specification, this is either a single
     * function (which applies to all process colorants) or an array of four functions (which apply
     * to the process colorants individually). The name Identity may be used to represent the
     * identity function.
     */
    public COSBase getTransfer()
    {
        COSBase base = dict.getDictionaryObject(COSName.TR);
        if (base instanceof COSArray && ((COSArray) base).size() != 4)
        {
            return null;
        }
        return base;
    }

    /**
     * This will set the transfer function of the /TR dictionary.
     *
     * @param transfer The transfer function. According to the PDF specification, this is either a
     * single function (which applies to all process colorants) or an array of four functions (which
     * apply to the process colorants individually). The name Identity may be used to represent the
     * identity function.
     */
    public void setTransfer(COSBase transfer)
    {
        dict.setItem(COSName.TR, transfer);
    }

    /**
     * This will get the transfer function of the /TR2 dictionary.
     *
     * @return The transfer function. According to the PDF specification, this is either a single
     * function (which applies to all process colorants) or an array of four functions (which apply
     * to the process colorants individually). The name Identity may be used to represent the
     * identity function, and the name Default denotes the transfer function that was in effect at
     * the start of the page.
     */
    public COSBase getTransfer2()
    {
        COSBase base = dict.getDictionaryObject(COSName.TR2);
        if (base instanceof COSArray && ((COSArray) base).size() != 4)
        {
            return null;
        }
        return base;
    }

    /**
     * This will set the transfer function of the /TR2 dictionary.
     *
     * @param transfer2 The transfer function. According to the PDF specification, this is either a
     * single function (which applies to all process colorants) or an array of four functions (which
     * apply to the process colorants individually). The name Identity may be used to represent the
     * identity function, and the name Default denotes the transfer function that was in effect at
     * the start of the page.
     */
    public void setTransfer2(COSBase transfer2)
    {
        dict.setItem(COSName.TR2, transfer2);
    }
}