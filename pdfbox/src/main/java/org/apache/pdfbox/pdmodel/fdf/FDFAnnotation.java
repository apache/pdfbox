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
package org.apache.pdfbox.pdmodel.fdf;

import java.awt.Color;
import java.io.IOException;
import java.util.Calendar;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;

import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.util.BitFlagHelper;
import org.apache.pdfbox.util.DateConverter;
import org.w3c.dom.Element;

/**
 * This represents an FDF annotation that is part of the FDF document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.4 $
 */
public abstract class FDFAnnotation implements COSObjectable
{
    /**
     * Annotation dictionary.
     */
    protected COSDictionary annot;

    /**
     * Default constructor.
     */
    public FDFAnnotation()
    {
        annot = new COSDictionary();
        annot.setName( "Type", "Annot" );
    }

    /**
     * Constructor.
     *
     * @param a The FDF annotation.
     */
    public FDFAnnotation( COSDictionary a )
    {
        annot = a;
    }

    /**
     * Constructor.
     *
     * @param element An XFDF element.
     *
     * @throws IOException If there is an error extracting data from the element.
     */
    public FDFAnnotation( Element element ) throws IOException
    {
        this();

        String page = element.getAttribute( "page" );
        if( page != null )
        {
            setPage( Integer.parseInt( page ) );
        }

        String color = element.getAttribute( "color" );
        if( color != null )
        {
            if( color.length() == 7 && color.charAt( 0 ) == '#' )
            {
                int colorValue = Integer.parseInt(color.substring(1,7), 16);
                setColor( new Color(colorValue) );
            }
        }

        setDate( element.getAttribute( "date" ) );

        String flags = element.getAttribute( "flags" );
        if( flags != null )
        {
            String[] flagTokens = flags.split( "," );
            for( int i=0; i< flagTokens.length; i++ )
            {
                if( flagTokens[i].equals( "invisible" ) )
                {
                    setInvisible( true );
                }
                else if( flagTokens[i].equals( "hidden" ) )
                {
                    setHidden( true );
                }
                else if( flagTokens[i].equals( "print" ) )
                {
                    setPrinted( true );
                }
                else if( flagTokens[i].equals( "nozoom" ) )
                {
                    setNoZoom( true );
                }
                else if( flagTokens[i].equals( "norotate" ) )
                {
                    setNoRotate( true );
                }
                else if( flagTokens[i].equals( "noview" ) )
                {
                    setNoView( true );
                }
                else if( flagTokens[i].equals( "readonly" ) )
                {
                    setReadOnly( true );
                }
                else if( flagTokens[i].equals( "locked" ) )
                {
                    setLocked( true );
                }
                else if( flagTokens[i].equals( "togglenoview" ) )
                {
                    setToggleNoView( true );
                }
            }

        }

        setName( element.getAttribute( "name" ) );

        String rect = element.getAttribute( "rect" );
        if( rect != null )
        {
            String[] rectValues = rect.split( "," );
            float[] values = new float[ rectValues.length ];
            for( int i=0; i<rectValues.length; i++ )
            {
                values[i] = Float.parseFloat( rectValues[i] );
            }
            COSArray array = new COSArray();
            array.setFloatArray( values );
            setRectangle( new PDRectangle( array ) );
        }

        setName( element.getAttribute( "title" ) );
        setCreationDate( DateConverter.toCalendar( element.getAttribute( "creationdate" ) ) );
        String opac = element.getAttribute( "opacity" );
        if( opac != null )
        {
            setOpacity( Float.parseFloat( opac ) );
        }
        setSubject( element.getAttribute( "subject" ) );
    }

    /**
     * Create the correct FDFAnnotation.
     *
     * @param fdfDic The FDF dictionary.
     *
     * @return A newly created FDFAnnotation
     *
     * @throws IOException If there is an error accessing the FDF information.
     */
    public static FDFAnnotation create( COSDictionary fdfDic ) throws IOException
    {
        FDFAnnotation retval = null;
        if( fdfDic == null )
        {
            //do nothing and return null
        }
        else if( FDFAnnotationText.SUBTYPE.equals( fdfDic.getNameAsString( COSName.SUBTYPE ) ) )
        {
            retval = new FDFAnnotationText( fdfDic );
        }
        else
        {
            throw new IOException( "Unknown annotation type '" + fdfDic.getNameAsString( COSName.SUBTYPE ) + "'" );
        }
        return retval;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return annot;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return annot;
    }

    /**
     * This will get the page number or null if it does not exist.
     *
     * @return The page number.
     */
    public Integer getPage()
    {
        Integer retval = null;
        COSNumber page = (COSNumber)annot.getDictionaryObject( "Page" );
        if( page != null )
        {
            retval = new Integer( page.intValue() );
        }
        return retval;
    }

    /**
     * This will set the page.
     *
     * @param page The page number.
     */
    public void setPage( int page )
    {
        annot.setInt( "Page", page );
    }

    /**
     * Get the annotation color.
     *
     * @return The annotation color, or null if there is none.
     */
    public Color getColor()
    {
        Color retval = null;
        COSArray array = (COSArray)annot.getDictionaryObject( "color" );
        if( array != null )
        {
            float[] rgb = array.toFloatArray();
            if( rgb.length >=3 )
            {
                retval = new Color( rgb[0], rgb[1], rgb[2] );
            }
        }
        return retval;
    }

    /**
     * Set the annotation color.
     *
     * @param c The annotation color.
     */
    public void setColor( Color c )
    {
        COSArray color = null;
        if( c != null )
        {
            float[] colors = c.getRGBColorComponents( null );
            color = new COSArray();
            color.setFloatArray( colors );
        }
        annot.setItem( "color", color );
    }

    /**
     * Modification date.
     *
     * @return The date as a string.
     */
    public String getDate()
    {
        return annot.getString( "date" );
    }

    /**
     * The annotation modification date.
     *
     * @param date The date to store in the FDF annotation.
     */
    public void setDate( String date )
    {
        annot.setString( "date", date );
    }

    /**
     * Get the invisible flag.
     *
     * @return The invisible flag.
     */
    public boolean isInvisible()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_INVISIBLE );
    }

    /**
     * Set the invisible flag.
     *
     * @param invisible The new invisible flag.
     */
    public void setInvisible( boolean invisible )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_INVISIBLE, invisible );
    }

    /**
     * Get the hidden flag.
     *
     * @return The hidden flag.
     */
    public boolean isHidden()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_HIDDEN );
    }

    /**
     * Set the hidden flag.
     *
     * @param hidden The new hidden flag.
     */
    public void setHidden( boolean hidden )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_HIDDEN, hidden );
    }

    /**
     * Get the printed flag.
     *
     * @return The printed flag.
     */
    public boolean isPrinted()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_PRINTED );
    }

    /**
     * Set the printed flag.
     *
     * @param printed The new printed flag.
     */
    public void setPrinted( boolean printed )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_PRINTED, printed );
    }

    /**
     * Get the noZoom flag.
     *
     * @return The noZoom flag.
     */
    public boolean isNoZoom()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_NO_ZOOM );
    }

    /**
     * Set the noZoom flag.
     *
     * @param noZoom The new noZoom flag.
     */
    public void setNoZoom( boolean noZoom )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_NO_ZOOM, noZoom );
    }

    /**
     * Get the noRotate flag.
     *
     * @return The noRotate flag.
     */
    public boolean isNoRotate()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_NO_ROTATE );
    }

    /**
     * Set the noRotate flag.
     *
     * @param noRotate The new noRotate flag.
     */
    public void setNoRotate( boolean noRotate )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_NO_ROTATE, noRotate );
    }

    /**
     * Get the noView flag.
     *
     * @return The noView flag.
     */
    public boolean isNoView()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_NO_VIEW );
    }

    /**
     * Set the noView flag.
     *
     * @param noView The new noView flag.
     */
    public void setNoView( boolean noView )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_NO_VIEW, noView );
    }

    /**
     * Get the readOnly flag.
     *
     * @return The readOnly flag.
     */
    public boolean isReadOnly()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_READ_ONLY );
    }

    /**
     * Set the readOnly flag.
     *
     * @param readOnly The new readOnly flag.
     */
    public void setReadOnly( boolean readOnly )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_READ_ONLY, readOnly );
    }

    /**
     * Get the locked flag.
     *
     * @return The locked flag.
     */
    public boolean isLocked()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_LOCKED );
    }

    /**
     * Set the locked flag.
     *
     * @param locked The new locked flag.
     */
    public void setLocked( boolean locked )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_LOCKED, locked );
    }

    /**
     * Get the toggleNoView flag.
     *
     * @return The toggleNoView flag.
     */
    public boolean isToggleNoView()
    {
        return BitFlagHelper.getFlag( annot, COSName.F, PDAnnotation.FLAG_TOGGLE_NO_VIEW );
    }

    /**
     * Set the toggleNoView flag.
     *
     * @param toggleNoView The new toggleNoView flag.
     */
    public void setToggleNoView( boolean toggleNoView )
    {
        BitFlagHelper.setFlag( annot, COSName.F, PDAnnotation.FLAG_TOGGLE_NO_VIEW, toggleNoView );
    }

    /**
     * Set a unique name for an annotation.
     *
     * @param name The unique annotation name.
     */
    public void setName( String name )
    {
        annot.setString( COSName.NM, name );
    }

    /**
     * Get the annotation name.
     *
     * @return The unique name of the annotation.
     */
    public String getName()
    {
        return annot.getString( COSName.NM );
    }

    /**
     * Set the rectangle associated with this annotation.
     *
     * @param rectangle The annotation rectangle.
     */
    public void setRectangle( PDRectangle rectangle )
    {
        annot.setItem( COSName.RECT, rectangle );
    }

    /**
     * The rectangle associated with this annotation.
     *
     * @return The annotation rectangle.
     */
    public PDRectangle getRectangle()
    {
        PDRectangle retval = null;
        COSArray rectArray = (COSArray)annot.getDictionaryObject( COSName.RECT );
        if( rectArray != null )
        {
            retval = new PDRectangle( rectArray );
        }

        return retval;
    }

    /**
     * Set a unique title for an annotation.
     *
     * @param title The annotation title.
     */
    public void setTitle( String title )
    {
        annot.setString( COSName.T, title );
    }

    /**
     * Get the annotation title.
     *
     * @return The title of the annotation.
     */
    public String getTitle()
    {
        return annot.getString( COSName.T );
    }

    /**
     * The annotation create date.
     *
     * @return The date of the creation of the annotation date
     *
     * @throws IOException If there is an error converting the string to a Calendar object.
     */
    public Calendar getCreationDate() throws IOException
    {
        return annot.getDate( COSName.CREATION_DATE );
    }

    /**
     * Set the creation date.
     *
     * @param date The date the annotation was created.
     */
    public void setCreationDate( Calendar date )
    {
        annot.setDate( COSName.CREATION_DATE, date );
    }

    /**
     * Set the annotation opacity.
     *
     * @param opacity The new opacity value.
     */
    public void setOpacity( float opacity )
    {
        annot.setFloat( COSName.CA, opacity );
    }

    /**
     * Get the opacity value.
     *
     * @return The opacity of the annotation.
     */
    public float getOpacity()
    {
        return annot.getFloat( COSName.CA, 1f );

    }

    /**
     * A short description of the annotation.
     *
     * @param subject The annotation subject.
     */
    public void setSubject( String subject )
    {
        annot.setString( COSName.SUBJ, subject );
    }

    /**
     * Get the description of the annotation.
     *
     * @return The subject of the annotation.
     */
    public String getSubject()
    {
        return annot.getString( COSName.SUBJ );
    }
}
