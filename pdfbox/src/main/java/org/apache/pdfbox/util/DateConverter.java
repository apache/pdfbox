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
package org.apache.pdfbox.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.io.IOException;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.pdfbox.cos.COSString;

/**
 * This class is used to convert dates to strings and back using the PDF
 * date standards.  Date are described in PDFReference1.4 section 3.8.2
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.14 $
 */
public class DateConverter
{
    //The Date format is supposed to be the PDF_DATE_FORMAT, but not all PDF documents
    //will use that date, so I have added a couple other potential formats
    //to try if the original one does not work.
    private static final SimpleDateFormat[] POTENTIAL_FORMATS = new SimpleDateFormat[] {
        new SimpleDateFormat("EEEE, dd MMM yyyy hh:mm:ss a", Locale.ENGLISH),
        new SimpleDateFormat("EEEE, MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH),
        new SimpleDateFormat("MM/dd/yyyy hh:mm:ss", Locale.ENGLISH),
        new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz", Locale.ENGLISH),
        new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.ENGLISH), // Acrobat Distiller 1.0.2 for Macintosh
        new SimpleDateFormat("EEEE MMM dd, yyyy HH:mm:ss", Locale.ENGLISH), // ECMP5
        new SimpleDateFormat("EEEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH), // GNU Ghostscript 7.0.7
        new SimpleDateFormat("EEEE, MMM dd, yyyy 'at' hh:mma", Locale.ENGLISH), // Acrobat Net Distiller 1.0 for Windows
        new SimpleDateFormat("d/MM/yyyy hh:mm:ss", Locale.ENGLISH), // PDFBOX-164
        new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.ENGLISH), // PDFBOX-170
        new SimpleDateFormat("EEEEEEEEEE, MMMMMMMMMMMM dd, yyyy", Locale.ENGLISH),  // PDFBOX-465
        new SimpleDateFormat("dd MMM yyyy hh:mm:ss", Locale.ENGLISH),  // for 26 May 2000 11:25:00
        new SimpleDateFormat("dd MMM yyyy hh:mm", Locale.ENGLISH),  // for 26 May 2000 11:25
        new SimpleDateFormat("M/dd/yyyy hh:mm:ss", Locale.ENGLISH),
        new SimpleDateFormat("MM/d/yyyy hh:mm:ss", Locale.ENGLISH),
        new SimpleDateFormat("M/dd/yyyy", Locale.ENGLISH),
        new SimpleDateFormat("MM/d/yyyy", Locale.ENGLISH),
        new SimpleDateFormat("M/d/yyyy hh:mm:ss", Locale.ENGLISH),
        new SimpleDateFormat("M/d/yyyy", Locale.ENGLISH),
        new SimpleDateFormat("M/d/yy hh:mm:ss", Locale.ENGLISH),
        new SimpleDateFormat("M/d/yy", Locale.ENGLISH),
        new SimpleDateFormat("yyyymmdd hh:mm:ss Z"), //
        new SimpleDateFormat("yyyymmdd hh:mm:ss"),   //
        new SimpleDateFormat("yyyymmdd'+00''00'''"), //
        new SimpleDateFormat("yyyymmdd'+01''00'''"), //
        new SimpleDateFormat("yyyymmdd'+02''00'''"), //
        new SimpleDateFormat("yyyymmdd'+03''00'''"), //
        new SimpleDateFormat("yyyymmdd'+04''00'''"), //
        new SimpleDateFormat("yyyymmdd'+05''00'''"), //
        new SimpleDateFormat("yyyymmdd'+06''00'''"), //
        new SimpleDateFormat("yyyymmdd'+07''00'''"), //
        new SimpleDateFormat("yyyymmdd'+08''00'''"), //
        new SimpleDateFormat("yyyymmdd'+09''00'''"), //
        new SimpleDateFormat("yyyymmdd'+10''00'''"), //
        new SimpleDateFormat("yyyymmdd'+11''00'''"), //
        new SimpleDateFormat("yyyymmdd'+12''00'''"), //
        new SimpleDateFormat("yyyymmdd'-01''00'''"), //
        new SimpleDateFormat("yyyymmdd'-02''00'''"), //
        new SimpleDateFormat("yyyymmdd'-03''00'''"), //
        new SimpleDateFormat("yyyymmdd'-04''00'''"), //
        new SimpleDateFormat("yyyymmdd'-05''00'''"), //
        new SimpleDateFormat("yyyymmdd'-06''00'''"), //
        new SimpleDateFormat("yyyymmdd'-07''00'''"), //
        new SimpleDateFormat("yyyymmdd'-08''00'''"), //
        new SimpleDateFormat("yyyymmdd'-09''00'''"), //
        new SimpleDateFormat("yyyymmdd'-10''00'''"), //
        new SimpleDateFormat("yyyymmdd'-11''00'''"), //
        new SimpleDateFormat("yyyymmdd'-12''00'''"), //
        new SimpleDateFormat("yyyymmdd"), // for 20090401+0200
    };

    private DateConverter()
    {
        //utility class should not be constructed.
    }

    /**
     * This will convert the calendar to a string.
     *
     * @param date The date to convert to a string.
     *
     * @return The date as a String to be used in a PDF document.
     */
    public static String toString( Calendar date )
    {
        String retval = null;
        if( date != null )
        {
            StringBuffer buffer = new StringBuffer();
            TimeZone zone = date.getTimeZone();
            long offsetInMinutes = zone.getOffset( date.getTimeInMillis() )/1000/60;
            long hours = Math.abs( offsetInMinutes/60 );
            long minutes = Math.abs( offsetInMinutes%60 );
            buffer.append( "D:" );
            // PDFBOX-402 , SimpleDateFormat is not thread safe, created it when you use it.
            buffer.append( new SimpleDateFormat( "yyyyMMddHHmmss" , Locale.ENGLISH).format( date.getTime() ) );
            if( offsetInMinutes == 0 )
            {
                buffer.append( "Z" );
            }
            else if( offsetInMinutes < 0 )
            {
                buffer.append( "-" );
            }
            else
            {
                buffer.append( "+" );
            }
            if( hours < 10 )
            {
                buffer.append( "0" );
            }
            buffer.append( hours );
            buffer.append( "'" );
            if( minutes < 10 )
            {
                buffer.append( "0" );
            }
            buffer.append( minutes );
            buffer.append( "'" );
            retval = buffer.toString();

        }
        return retval;
    }

    /**
     * This will convert a string to a calendar.
     *
     * @param date The string representation of the calendar.
     *
     * @return The calendar that this string represents.
     *
     * @throws IOException If the date string is not in the correct format.
     */
    public static Calendar toCalendar( COSString date ) throws IOException
    {
        Calendar retval = null;
        if( date != null )
        {
            retval = toCalendar( date.getString() );
        }

        return retval;
    }

    /**
     * This will convert a string to a calendar.
     *
     * @param date The string representation of the calendar.
     *
     * @return The calendar that this string represents.
     *
     * @throws IOException If the date string is not in the correct format.
     */
    public static Calendar toCalendar( String date ) throws IOException
    {
        Calendar retval = null;
        if( date != null && date.trim().length() > 0 )
        {
            //these are the default values
            int year = 0;
            int month = 1;
            int day = 1;
            int hour = 0;
            int minute = 0;
            int second = 0;
            //first string off the prefix if it exists
            try
            {
                SimpleTimeZone zone = null;
                if( date.startsWith( "D:" ) )
                {
                    date = date.substring( 2, date.length() );
                }
                if( date.length() < 4 )
                {
                    throw new IOException( "Error: Invalid date format '" + date + "'" );
                }
                year = Integer.parseInt( date.substring( 0, 4 ) );
                if( date.length() >= 6 )
                {
                    month = Integer.parseInt( date.substring( 4, 6 ) );
                }
                if( date.length() >= 8 )
                {
                    day = Integer.parseInt( date.substring( 6, 8 ) );
                }
                if( date.length() >= 10 )
                {
                    hour = Integer.parseInt( date.substring( 8, 10 ) );
                }
                if( date.length() >= 12 )
                {
                    minute = Integer.parseInt( date.substring( 10, 12 ) );
                }
                if( date.length() >= 14 )
                {
                    second = Integer.parseInt( date.substring( 12, 14 ) );
                }

                if( date.length() >= 15 )
                {
                    char sign = date.charAt( 14 );
                    if( sign == 'Z' )
                    {
                        zone = new SimpleTimeZone(0,"Unknown");
                    }
                    else
                    {
                        int hours = 0;
                        int minutes = 0;
                        if( date.length() >= 17 )
                        {
                            if( sign == '+' )
                            {
                                //parseInt cannot handle the + sign
                                hours = Integer.parseInt( date.substring( 15, 17 ) );
                            }
                            else if (sign == '-')
                            {
                                hours = -Integer.parseInt(date.substring(15,17));
                            }
                            else
                            {
                                hours = -Integer.parseInt( date.substring( 14, 16 ) );
                            }
                        }
                        if( date.length() > 20 )
                        {
                            minutes = Integer.parseInt( date.substring( 18, 20 ) );
                        }
                        zone = new SimpleTimeZone( hours*60*60*1000 + minutes*60*1000, "Unknown" );
                    }
                }
                if( zone != null )
                {
                    retval = new GregorianCalendar( zone );
                }
                else
                {
                    retval = new GregorianCalendar();
                }

                retval.set(year, month-1, day, hour, minute, second );
                // PDFBOX-598: PDF dates are only accurate up to a second
                retval.set(Calendar.MILLISECOND, 0);
            }
            catch( NumberFormatException e )
            {
                for( int i=0; retval == null && i<POTENTIAL_FORMATS.length; i++ )
                {
                    try
                    {
                        Date utilDate = POTENTIAL_FORMATS[i].parse( date );
                        retval = new GregorianCalendar();
                        retval.setTime( utilDate );
                    }
                    catch( ParseException pe )
                    {
                        //ignore and move to next potential format
                    }
                }
                if( retval == null )
                {
                    //we didn't find a valid date format so throw an exception
                    throw new IOException( "Error converting date:" + date );
                }
            }
        }
        return retval;
    }

    private static final void zeroAppend( StringBuffer out, int number )
    {
        if( number < 10 )
        {
            out.append( "0" );
        }
        out.append( number );
    }

    /**
     * Convert the date to iso 8601 string format.
     *
     * @param cal The date to convert.
     * @return The date represented as an ISO 8601 string.
     */
    public static String toISO8601( Calendar cal )
    {
        StringBuffer retval = new StringBuffer();

        retval.append( cal.get( Calendar.YEAR ) );
        retval.append( "-" );
        zeroAppend( retval, cal.get( Calendar.MONTH )+1 );
        retval.append( "-" );
        zeroAppend( retval, cal.get( Calendar.DAY_OF_MONTH ) );
        retval.append( "T" );
        zeroAppend( retval, cal.get( Calendar.HOUR_OF_DAY ));
        retval.append( ":" );
        zeroAppend( retval, cal.get( Calendar.MINUTE ));
        retval.append( ":" );
        zeroAppend( retval, cal.get( Calendar.SECOND ));

        int timeZone = cal.get( Calendar.ZONE_OFFSET ) + cal.get(Calendar.DST_OFFSET );
        if( timeZone < 0 )
        {
            retval.append( "-" );
        }
        else
        {
            retval.append( "+" );
        }
        timeZone = Math.abs( timeZone );
        //milliseconds/1000 = seconds = seconds / 60 = minutes = minutes/60 = hours
        int hours = timeZone/1000/60/60;
        int minutes = (timeZone - (hours*1000*60*60))/1000/1000;
        if( hours < 10 )
        {
            retval.append( "0" );
        }
        retval.append( Integer.toString( hours ) );
        retval.append( ":" );
        if( minutes < 10 )
        {
            retval.append( "0" );
        }
        retval.append( Integer.toString( minutes ) );

        return retval.toString();
    }
}
