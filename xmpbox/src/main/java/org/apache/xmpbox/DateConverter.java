/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.xmpbox;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to convert dates to strings and back using the PDF date standards. Date are described in
 * PDFReference1.4 section 3.8.2
 * 
 * <p>
 * <strong>This is (and will not be) a Java date parsing library and will likely still have limited
 * support for various strings as it’s main use case it to parse from PDF date strings.</strong>
 * </p>
 * 
 * @author Ben Litchfield
 * @author Christopher Oezbek
 * 
 */
public final class DateConverter
{

    // The Date format is supposed to be the PDF_DATE_FORMAT, but not all PDF
    // documents
    // will use that date, so I have added a couple other potential formats
    // to try if the original one does not work.
    private static final SimpleDateFormat[] POTENTIAL_FORMATS = new SimpleDateFormat[] {
            new SimpleDateFormat("EEEE, dd MMM yyyy hh:mm:ss a"),
            new SimpleDateFormat("EEEE, MMM dd, yyyy hh:mm:ss a"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S")
        };

    /**
     * According to check-style, Utility classes should not have a public or default constructor.
     */
    private DateConverter()
    {
    }

    /**
     * This will convert a string to a calendar.
     * 
     * @param date
     *            The string representation of the calendar.
     * 
     * @return The calendar that this string represents.
     * 
     * @throws IOException
     *             If the date string is not in the correct format.
     */
    public static Calendar toCalendar(String date) throws IOException
    {
        Calendar retval = null;
        if ((date != null) && (date.trim().length() > 0))
        {
            // these are the default values
            int month = 1;
            int day = 1;
            int hour = 0;
            int minute = 0;
            int second = 0;
            // first string off the prefix if it exists
            try
            {
                SimpleTimeZone zone = null;
                
                if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2}T.*", date))
                {
                    // Assuming ISO860 date string
                    return fromISO8601(date);
                }
                else if (date.startsWith("D:"))
                {
                    date = date.substring(2, date.length());
                }

                date = date.replaceAll("[-:T]", "");

                if (date.length() < 4)
                {
                    throw new IOException("Error: Invalid date format '" + date + "'");
                }
                int year = Integer.parseInt(date.substring(0, 4));
                if (date.length() >= 6)
                {
                    month = Integer.parseInt(date.substring(4, 6));
                }
                if (date.length() >= 8)
                {
                    day = Integer.parseInt(date.substring(6, 8));
                }
                if (date.length() >= 10)
                {
                    hour = Integer.parseInt(date.substring(8, 10));
                }
                if (date.length() >= 12)
                {
                    minute = Integer.parseInt(date.substring(10, 12));
                }

                int timeZonePos = 12;
                if (date.length() - 12 > 5 || (date.length() - 12 == 3 && date.endsWith("Z")))
                {
                    if (date.length() >= 14)
                    {
                        second = Integer.parseInt(date.substring(12, 14));
                    }
                    timeZonePos = 14;
                }
                else
                {
                    second = 0;
                }

                if (date.length() >= (timeZonePos + 1))
                {
                    char sign = date.charAt(timeZonePos);
                    if (sign == 'Z')
                    {
                        zone = new SimpleTimeZone(0, "Unknown");
                    }
                    else
                    {
                        int hours = 0;
                        int minutes = 0;
                        if (date.length() >= (timeZonePos + 3))
                        {
                            if (sign == '+')
                            {
                                // parseInt cannot handle the + sign
                                hours = Integer.parseInt(date.substring((timeZonePos + 1), (timeZonePos + 3)));
                            }
                            else
                            {
                                hours = -Integer.parseInt(date.substring(timeZonePos, (timeZonePos + 2)));
                            }
                        }
                        if (sign == '+')
                        {
                            if (date.length() >= (timeZonePos + 5))
                            {
                                minutes = Integer.parseInt(date.substring((timeZonePos + 3), (timeZonePos + 5)));
                            }
                        }
                        else
                        {
                            if (date.length() >= (timeZonePos + 4))
                            {
                                minutes = Integer.parseInt(date.substring((timeZonePos + 2), (timeZonePos + 4)));
                            }
                        }
                        zone = new SimpleTimeZone(hours * 60 * 60 * 1000 + minutes * 60 * 1000, "Unknown");
                    }
                }

                if (zone == null)
                {
                    retval = new GregorianCalendar();
                }
                else
                {
                    updateZoneId(zone);
                    retval = new GregorianCalendar(zone);
                }
                retval.clear();
                retval.set(year, month - 1, day, hour, minute, second);
            }
            catch (NumberFormatException e)
            {

                // remove the arbitrary : in the timezone. SimpleDateFormat
                // can't handle it
                if (date.substring(date.length() - 3, date.length() - 2).equals(":")
                        && (date.substring(date.length() - 6, date.length() - 5).equals("+") || date.substring(
                                date.length() - 6, date.length() - 5).equals("-")))
                {
                    // thats a timezone string, remove the :
                    date = date.substring(0, date.length() - 3) + date.substring(date.length() - 2);
                }
                for (int i = 0; (retval == null) && (i < POTENTIAL_FORMATS.length); i++)
                {
                    try
                    {
                        Date utilDate = POTENTIAL_FORMATS[i].parse(date);
                        retval = new GregorianCalendar();
                        retval.setTime(utilDate);
                    }
                    catch (ParseException pe)
                    {
                        // ignore and move to next potential format
                    }
                }
                if (retval == null)
                {
                    // we didn't find a valid date format so throw an exception
                    throw new IOException("Error converting date:" + date, e);
                }
            }
        }
        return retval;
    }

    /**
     * Update the zone ID based on the raw offset. This is either GMT, GMT+hh:mm or GMT-hh:mm, where
     * n is between 1 and 14. The highest negative hour is -14, the highest positive hour is 12.
     * Zones that don't fit in this schema are set to zone ID "unknown".
     *
     * @param tz the time zone to update.
     */
    private static void updateZoneId(TimeZone tz)
    {
        int offset = tz.getRawOffset();
        char pm = '+';
        if (offset < 0)
        {
            pm = '-';
            offset = -offset;
        }
        int hh = offset / 3600000;
        int mm = offset % 3600000 / 60000;
        if (offset == 0)
        {
            tz.setID("GMT");
        }
        else if (pm == '+' && hh <= 12)
        {
            tz.setID(String.format(Locale.US, "GMT+%02d:%02d", hh, mm));
        }
        else if (pm == '-' && hh <= 14)
        {
            tz.setID(String.format(Locale.US, "GMT-%02d:%02d", hh, mm));
        }
        else
        {
            tz.setID("unknown");
        }
    }

    /**
     * Convert the date to iso 8601 string format.
     * 
     * @param cal
     *            The date to convert.
     * @return The date represented as an ISO 8601 string.
     */
    public static String toISO8601(Calendar cal)
    {
        return toISO8601(cal, false);
    }
    
    /**
     * Convert the date to iso 8601 string format.
     * 
     * @param cal The date to convert.
     * @param printMillis Print Milliseconds.
     * @return The date represented as an ISO 8601 string.
     */
    public static String toISO8601(Calendar cal, boolean printMillis)
    {
        StringBuilder retval = new StringBuilder();

        retval.append(cal.get(Calendar.YEAR));
        retval.append("-");
        retval.append(String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1));
        retval.append("-");
        retval.append(String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)));
        retval.append("T");
        retval.append(String.format(Locale.US, "%02d", cal.get(Calendar.HOUR_OF_DAY)));
        retval.append(":");
        retval.append(String.format(Locale.US, "%02d", cal.get(Calendar.MINUTE)));
        retval.append(":");
        retval.append(String.format(Locale.US, "%02d", cal.get(Calendar.SECOND)));
        
        if (printMillis)
        {
            retval.append(".");
            retval.append(String.format(Locale.US, "%03d", cal.get(Calendar.MILLISECOND)));
        }

        int timeZone = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
        if (timeZone < 0)
        {
            retval.append("-");
        }
        else
        {
            retval.append("+");
        }
        timeZone = Math.abs(timeZone);
        // milliseconds/1000 = seconds = seconds / 60 = minutes = minutes/60 =
        // hours
        int hours = timeZone / 1000 / 60 / 60;
        int minutes = (timeZone - (hours * 1000 * 60 * 60)) / 1000 / 1000;
        if (hours < 10)
        {
            retval.append("0");
        }
        retval.append(Integer.toString(hours));
        retval.append(":");
        if (minutes < 10)
        {
            retval.append("0");
        }
        retval.append(Integer.toString(minutes));
        return retval.toString();
    }
    
    /**
     * Get a Calendar from an ISO8601 date string.
     * 
     * @param dateString
     * @return the Calendar instance.
     */
    private static Calendar fromISO8601(String dateString)
    {
        // Pattern to test for a time zone string
        Pattern timeZonePattern = Pattern.compile(
                    "[\\d-]*T?[\\d-\\.]([A-Z]{1,4})$|(.*\\d*)([A-Z][a-z]+\\/[A-Z][a-z]+)$"
                );
        Matcher timeZoneMatcher = timeZonePattern.matcher(dateString);
        
        String timeZoneString = null;
        
        while (timeZoneMatcher.find())
        {
            for (int i = 1; i <= timeZoneMatcher.groupCount(); i++)
            {
                if (timeZoneMatcher.group(i) != null)
                {
                    timeZoneString = timeZoneMatcher.group(i);
                }
            }
        }

        if (timeZoneString != null)
        {
            // can't use parseDateTime immediately, first do handling for time that has no seconds
            int teeIndex = dateString.indexOf('T');
            int tzIndex = dateString.indexOf(timeZoneString);
            String toParse = dateString.substring(0, tzIndex);
            if (tzIndex - teeIndex == 6)
            {
                toParse = dateString.substring(0, tzIndex) + ":00";
            }
            Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(toParse);

            TimeZone z = TimeZone.getTimeZone(timeZoneString);
            cal.setTimeZone(z);            
            return cal;
        }
        else
        {
            // can't use parseDateTime immediately, first do handling for time that has no seconds
            int teeIndex = dateString.indexOf('T');
            if (teeIndex == -1)
            {
                return javax.xml.bind.DatatypeConverter.parseDateTime(dateString);
            }
            int plusIndex = dateString.indexOf('+', teeIndex + 1);
            int minusIndex = dateString.indexOf('-', teeIndex + 1);
            if (plusIndex == -1 && minusIndex == -1)
            {
                return javax.xml.bind.DatatypeConverter.parseDateTime(dateString);
            }
            plusIndex = Math.max(plusIndex, minusIndex);
            if (plusIndex - teeIndex == 6)
            {
                String toParse = dateString.substring(0, plusIndex) + ":00" + dateString.substring(plusIndex);
                return javax.xml.bind.DatatypeConverter.parseDateTime(toParse);
            }
            return javax.xml.bind.DatatypeConverter.parseDateTime(dateString);
        }
    }
}
