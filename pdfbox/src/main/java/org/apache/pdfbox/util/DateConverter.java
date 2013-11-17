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

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.apache.pdfbox.cos.COSString;

/**
 * Date format is described in PDF Reference 1.7 section 3.8.2
 * (www.adobe.com/devnet/acrobat/pdfs/pdf_reference_1-7.pdf)
 * and also in PDF 32000-1:2008 
 * (http://www.adobe.com/devnet/acrobat/pdfs/PDF32000_2008.pdf))
 * although the latter inexplicably omits the trailing apostrophe.
 * 
 * The interpretation of dates without timezones is unclear. 
 * The code below assumes that such dates are in UTC+00 (aka GMT).
 * This is in keeping with the PDF Reference's assertion that:
 *      numerical fields default to zero values. 
 * However, the Reference does go on to make the cryptic remark:
 *      If no UT information is specified, the relationship of the specified  
 *      time to UT is considered to be unknown. Whether or not the time 
 *      zone is known, the rest of the date should be specified in local time.
 * I understand this to refer to _creating_ a pdf date value. That is, 
 * code that can get the wall clock time and cannot get the timezone 
 * should write the wall clock time with a time zone of zero.
 * When _parsing_ a PDF date, the statement talks about "the rest of the date"
 * being local time, thus explicitly excluding the use of the local time
 * for the time zone.
*/ 

/**
 * This class is used to convert dates to strings and back using the PDF
 * date standard in section 3.8.2 of PDF Reference 1.7.  
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @author <a href="mailto:zweibieren@ahoo.com">Fred Hansen</a>
 * 
 * TODO Move members of this class elsewhere for shared use in pdfbox, xmpbox, and jempbox.
 */
public class DateConverter
{
    // milliseconds/1000 = seconds; seconds / 60 = minutes; minutes/60 = hours
    private static final int MINUTES_PER_HOUR = 60;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MILLIS_PER_MINUTE = SECONDS_PER_MINUTE*1000;
    private static final int MILLIS_PER_HOUR = MINUTES_PER_HOUR * MILLIS_PER_MINUTE;
    private static final int 
            HALF_DAY = 12 * MINUTES_PER_HOUR * MILLIS_PER_MINUTE, 
            DAY = 2*HALF_DAY;
    
    /**
     * Error value if date is invalid. Parsing is done with 
     * GregorianCalendar.setLenient(false), so every date field value
     * must be within bounds. If an attempt is made to parse an invalid date 
     * field, toCalendar(String, String[]) returns Jan 1 in year INVALID_YEAR.
     */
    public static final int INVALID_YEAR = 999;
    
    
    /**
     * The Date format is supposed to be the PDF_DATE_FORMAT, but other
     * forms appear. These lists offer alternatives to be tried 
     * if parseBigEndianDate fails.  
     * 
     * The time zone offset generally trails the date string, so it is processed
     * separately with parseTZoffset. (This does not preclude having time
     * zones in the elements below; one does.)
     * 
     * Alas, SimpleDateFormat is badly non-reentrant -- it modifies its 
     * calendar field (PDFBox-402), so these lists are strings to create
     * SimpleDate format as needed.
     * 
     * Some past entries have been elided because they duplicate existing 
     * entries. See the API for SimpleDateFormat, which says 
     *      "For parsing, the number of pattern letters is ignored 
     *      unless it's needed to separate two adjacent fields."
     * 
     * toCalendar(String, String[]) tests to see that the entire input text
     * has been consumed. Therefore the ordering of formats is important. 
     * If one format begins with the entirety of another, the longer
     * must precede the other in the list.
     * 
     * HH is for 0-23 hours and hh for 1-12 hours; an "a" field must follow "hh"
     * Where year is yy, four digit years are accepted 
     * and two digit years are converted to four digits in the range
     *      [thisyear-79...thisyear+20]
     */
    private static final String[] ALPHA_START_FORMATS = 
    {
            "EEEE, dd MMM yy hh:mm:ss a",
            "EEEE, MMM dd, yy hh:mm:ss a",
            "EEEE, MMM dd, yy 'at' hh:mma", // Acrobat Net Distiller 1.0 for Windows
            "EEEE, MMM dd, yy", // Acrobat Distiller 1.0.2 for Macintosh  && PDFBOX-465
            "EEEE MMM dd, yy HH:mm:ss", // ECMP5
            "EEEE MMM dd HH:mm:ss z yy", // GNU Ghostscript 7.0.7
            "EEEE MMM dd HH:mm:ss yy", // GNU Ghostscript 7.0.7 variant
    };
    
    private static final String[] DIGIT_START_FORMATS = 
    {
        "dd MMM yy HH:mm:ss",  // for 26 May 2000 11:25:00
        "dd MMM yy HH:mm",  // for 26 May 2000 11:25
        "yyyy MMM d",   // ambiguity resolved only by omitting time
        "yyyymmddhh:mm:ss", // test case "200712172:2:3"
        "H:m M/d/yy", // test case "9:47 5/12/2008"
        "M/d/yy HH:mm:ss",
        "M/d/yy HH:mm",
        "M/d/yy",

        // proposed rule that is unreachable due to "dd MMM yy HH:mm:ss" 
        //     "yyyy MMM d HH:mm:ss", 

        // rules made unreachable by "M/d/yy HH:mm:ss" "M/d/yy HH:mm"  "M/d/yy",
        // (incoming digit strings do not mark themselves as y, m, or d!)
            // "d/MM/yyyy HH:mm:ss", // PDFBOX-164 and PDFBOX-170 
            // "M/dd/yyyy hh:mm:ss",
            // "MM/d/yyyy hh:mm:ss",
            // "M/d/yyyy HH:mm:ss",
            // "M/dd/yyyy",
            // "MM/d/yyyy",
            // "M/d/yyyy",
            // "M/d/yyyy HH:mm:ss",
            // "M/d/yy HH:mm:ss",
        // subsumed by big-endian parse
            // "yyyy-MM-dd'T'HH:mm:ss",
            // "yyyy-MM-dd'T'HH:mm:ss",
            // "yyyymmdd hh:mm:ss", 
            // "yyyymmdd", 
            // "yyyymmddX''00''",  // covers 24 cases 
            //    (orignally the above ended with '+00''00'''; 
            //      the first apostrophe quoted the plus, 
            //      '' mapped to a single ', and the ''' was invalid)
    };


    private DateConverter()
    {
        //utility class should not be constructed.
    }

    ////////////////////////////////////////////
    // C o n v e r t   t o   S t r i n g   Methods
     
    /**
     * Get all know formats.
     * 
     * @return an array containig all known formats
     */
    public static String[] getFormats() 
    {
        String[] val = new String[ALPHA_START_FORMATS.length+DIGIT_START_FORMATS.length];
        System.arraycopy(ALPHA_START_FORMATS, 0, val, 0, ALPHA_START_FORMATS.length);
        System.arraycopy(DIGIT_START_FORMATS, 0, val,ALPHA_START_FORMATS.length, DIGIT_START_FORMATS.length);
        return val;
    }

    /**
     * Converts a Calendar to a string formatted as:
     *     D:yyyyMMddHHmmss#hh'mm'  where # is Z, +, or -.
     * 
     * @param cal The date to convert to a string. May be null.
     * The DST_OFFSET is included when computing the output time zone.
     *
     * @return The date as a String to be used in a PDF document, 
     *      or null if the cal value is null
     */
    public static String toString(Calendar cal)
    {
        if (cal == null) 
        {
            return null;
        }
        String offset = formatTZoffset(cal.get(Calendar.ZONE_OFFSET)
                + cal.get(Calendar.DST_OFFSET), "'");
        return String.format("D:"
                + "%1$4tY%1$2tm%1$2td"   // yyyyMMdd 
                + "%1$2tH%1$2tM%1$2tS"   // HHmmss 
                + "%2$s"                // time zone
                + "'",                  // trailing apostrophe
            cal, offset);      
    }

    /**
     * Converts the date to ISO 8601 string format:
     *     yyyy-mm-ddThh:MM:ss#hh:mm    (where '#" is '+' or '-').
     *
     * @param cal The date to convert.  Must not be null.
     * The DST_OFFSET is included in the output value.
     * 
     * @return The date represented as an ISO 8601 string.
     */
    public static String toISO8601(Calendar cal)
    {
        String offset = formatTZoffset(cal.get(Calendar.ZONE_OFFSET)
                + cal.get(Calendar.DST_OFFSET), ":");
        return String.format(
                "%1$4tY"   // yyyy
                + "-%1$2tm"   // -mm  (%tm adds one to cal month value)
                + "-%1$2td"  // -dd  (%tm adds one to cal month value)
                + "T"                             // T
                + "%1$2tH:%1$2tM:%1$2tS"   // HHmmss  
                + "%2$s",              // time zone
            cal, offset);      
    }
    
    /**
     * Constrain a timezone offset to the range  [-11:59 thru +11:59].
     * @param proposedOffset A value intended to be a timezone offset.
     * @return The corresponding value reduced to the above noted range 
     * by adding or subtracting multiples of a full day.
     */
    public static int restrainTZoffset(long proposedOffset) 
    {
        proposedOffset = ((proposedOffset+HALF_DAY)%DAY+DAY)%DAY; 
        // 0 <= proposedOffset < DAY
        proposedOffset = (proposedOffset-HALF_DAY)%HALF_DAY;   
        // -HALF_DAY < proposedOffset < HALF_DAY
        return (int)proposedOffset;
    }
    
    /** 
     * Formats a time zone offset as #hh^mm
     * where # is + or -, hh is hours, ^ is a separator, and mm is minutes.
     * Any separator may be specified by the second argument;
     * the usual values are ":" (ISO 8601), "" (RFC 822), and "'" (PDF).
     * The returned value is constrained to the range -11:59 ... 11:59.
     * For offset of 0 millis, the String returned is "+00^00", never "Z".
     * To get a "general" offset in form GMT#hh:mm, write
     *      "GMT"+DateConverter.formatTZoffset(offset, ":");
     * <p>
     * Take thought in choosing the source for the millis value. 
     * It can come from calendarValue.getTimeZone() or from 
     * calendarValue.get(Calendar.ZONE_OFFSET).  If a TimeZone was created
     * from a valid time zone ID, then it may have a daylight savings rule.
     * (As of July 4, 2013, the data base at http://www.iana.org/time-zones 
     * recognized 629 time zone regions. But a TimeZone created as 
     *      new SimpleTimeZone(millisOffset, "ID"), 
     * will not have a daylight savings rule. (Not even if there is a
     * known time zone with the given ID. To get the TimeZone named "xDT"
     * with its DST rule, use an ID of EST5EDT, CST6CDT, MST7MDT, or PST8PDT.
     * <p>
     * When parsing PDF dates, the incoming values DOES NOT have a TIMEZONE value.
     * At most it has an OFFSET value like -04'00'. It is generally impossible to 
     * determine what TIMEZONE corresponds to a given OFFSET. If the date is
     * in the summer when daylight savings is in effect, an offset of -0400
     * might correspond to any one of the 38 regions (of 53) with standard time 
     * offset -0400 and no daylight saving. Or it might correspond to 
     * any one of the 31 regions (out of 43) that observe daylight savings 
     * and have standard time offset of -0500.
     * <p>
     * If a Calendar has not been assigned a TimeZone with setTimeZone(), 
     * it will have by default the local TIMEZONE, not just the OFFSET.  In the
     * USA, this TimeZone will have a daylight savings rule.
     * <p>
     * The offset assigned with calVal.set(Calendar.ZONE_OFFSET) differs
     * from the offset in the TimeZone set by Calendar.setTimeZone(). Example:
     * Suppose my local TimeZone is America/New_York. It has an offset of -05'00'.
     * And suppose I set a GregorianCalendar's ZONE_OFFSET to -07'00'
     *     calVal = new GregorianCalendar();   // TimeZone is the local default
     *     calVal.set(Calendar.ZONE_OFFSET, -7* MILLIS_PER_HOUR);
     * Four different offsets can be computed from calVal:
     *     calVal.get(Calendar.ZONE_OFFSET)  =>  -07:00
     *     calVal.get(Calendar.ZONE_OFFSET) + calVal.get(Calendar.DST_OFFSET) => -06:00
     *     calVal.getTimeZone().getRawOffset()  =>  -05:00
     *     calVal.getTimeZone().getOffset(calVal.getTimeInMillis())  =>  -04:00
     * <p>
     * Which is correct??? I dunno, though setTimeZone() does seem to affect
     * ZONE_OFFSET, and not vice versa.  One cannot even test whether TimeZone 
     * or ZONE_OFFSET has been set; both have been set by initialization code.
     * TimeZone is initialized to the local default time zone 
     * and ZONE_OFFSET is set from it.
     * 
     * My choice in this DateConverter class has been to set the 
     * initial TimeZone of a GregorianCalendar to GMT. Thereafter
     * the TimeZone is modified with {@link #adjustTimeZoneNicely}. 
     * 
     * @param millis a time zone offset expressed in milliseconds
     *      Any value is accepted; it is normalized to [-11:59 ... +11:59]
     * @param sep a String to insert between hh and mm. May be empty.
     * @return the formatted String for the offset
     */
    public static String formatTZoffset(long millis, String sep) 
    {
        SimpleDateFormat sdf = new SimpleDateFormat("Z"); // #hhmm
        sdf.setTimeZone(new SimpleTimeZone(restrainTZoffset(millis),"unknown"));
        String tz = sdf.format(new Date());
        return tz.substring(0,3)+sep+tz.substring(3);
    }

    //////////////////////////////////////////////
    // P A R S E   Methods

     /**
     * Parses an integer from a string, starting at and advancing a ParsePosition.
     * 
     * @param text The string being parsed. If null, the remedy value is returned.
     * @param where The ParsePosition to start the search. This value 
     *      will be incremented by the number of digits found, but no 
     *      more than maxlen.  That is, the ParsePosition will 
     *      advance across at most maxlen initial digits in text.
     *      The error index is ignored and unchanged.
     * @param maxlen The maximum length of the integer to parse. 
     *      Usually 2, but 4 for year fields.
     *      If the field of length maxlen begins with a digit, 
     *      but contains a non-digit, no error is signaled 
     *      and the integer value is returned.
     * @param remedy Value to be assigned if no digit is found at the
     *      initial parse position; that is, if the field is empty.
     * @return The integer that was at the given parse position. Or
     *      the remedy value if no digits were found.
     */
    public static int parseTimeField(String text, ParsePosition where, 
            int maxlen, int remedy) 
    {
        if (text == null) 
        {
                    return remedy; 
        }
        // (it would seem that DecimalFormat.parse() would be simpler;
        //     but that class blithely ignores setMaximumIntegerDigits)
        int retval = 0;
        int index = where.getIndex();
        int limit = index + Math.min(maxlen, text.length()-index);
        for (; index < limit; index++)
        {
            int cval = text.charAt(index) - '0';  // convert digit to integer
            if (cval <0 || cval > 9)   // test to see if we got a digit
            {
                break;   // no digit at index
            }
            retval = retval*10 + cval;   // append the digit to the return value
        }   
        if (index == where.getIndex())
        {
            return remedy;
        }
        where.setIndex(index);
        return retval;
    }
 
    /**
     * Advances the ParsePosition past any and all the characters 
     *      that match those in the optionals list.
     *      In particular, a space will skip all spaces.
     * @param text The text to examine
     * @param where index to start looking. 
     *      The value is incremented by the number of optionals found.
     *      The error index is ignored and unchanged.
     * @param optionals A String listing all the optional characters 
     *      to be skipped.
     * @return The last non-space character passed over. 
     *      Returns a space if no non-space character was found 
     *      (even if space is not in the optionals list.)
     */
    public static char skipOptionals(String text, ParsePosition where, 
            String optionals) 
    {
        char retval = ' ', currch;
        while (text != null && where.getIndex() < text.length() 
                && optionals.indexOf(
                        (currch=text.charAt(where.getIndex()))
                ) >= 0) 
        {
            retval = (currch != ' ') ? currch : retval;
            where.setIndex(where.getIndex() + 1);
        }
        return retval;
    }
    
    /**
     * If the victim string is at the given position in the text,
     * this method advances the position past that string. 
     * 
     * @param text The text to examine
     * @param victim The string to look for
     * @param where The initial position to look at. After return, this will
     *      have been incremented by the length of the victim if it was found.
     *      The error index is ignored and unchanged.
     * @return true if victim was found; otherwise false.
     */
    public static boolean skipString(String text, String victim, ParsePosition where) 
    {
        if (text.startsWith(victim, where.getIndex()))
        {
            where.setIndex(where.getIndex()+victim.length());
            return true;
        }
        return false;
    }

    /** 
     * Construct a new GregorianCalendar and set defaults.
     * Locale is ENGLISH.
     * TimeZone is "UTC" (zero offset and no DST).
     * Parsing is NOT lenient. Milliseconds are zero.
     * 
     * @return a new gregorian calendar
     */
    public static GregorianCalendar newGreg()  
    {
        GregorianCalendar retCal = new GregorianCalendar(Locale.ENGLISH);
        retCal.setTimeZone(new SimpleTimeZone(0, "UTC"));
        retCal.setLenient(false);
        retCal.set(Calendar.MILLISECOND, 0);
        return retCal;
    }
    
    /**
     * Install a TimeZone on a GregorianCalendar without changing the 
     * hours value. A plain GregorianCalendat.setTimeZone() 
     * adjusts the Calendar.HOUR value to compensate. This is *BAD*
     * (not to say *EVIL*) when we have already set the time.
     * @param cal The GregorianCalendar whose TimeZone to change.
     * @param tz The new TimeZone.
     */
    public static void adjustTimeZoneNicely(GregorianCalendar cal, TimeZone tz) 
    {
        cal.setTimeZone(tz);
        int offset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET))
                / MILLIS_PER_HOUR;
        cal.add(Calendar.HOUR, -offset);
    }
    
    /**
     * Parses the end of a date string for a time zone and, if one is found,
     * sets the time zone of the GregorianCalendar. Otherwise the calendar 
     * time zone is unchanged.
     * 
     * The text is parsed as
     *      (Z|GMT|UTC)? [+- ]* h [': ]? m '?
     * where the leading String is optional, h is two digits by default, 
     * but may be a single digit if followed by one of space, apostrophe, 
     * colon, or the end of string. Similarly, m is one or two digits. 
     * This scheme accepts the format of PDF, RFC 822, and ISO8601. 
     * If none of these applies (as for a time zone name), we try
     * TimeZone.getTimeZone().
     * 
     * @param text The text expected to begin with a time zone value,
     * possibly with leading or trailing spaces.
     * @param cal The Calendar whose TimeZone to set. 
     * @param initialWhere where Scanning begins at where.index. After success, the returned
     *      index is that of the next character after the recognized string.
     *      The error index is ignored and unchanged.
     * @return true if parsed a time zone value; otherwise the 
     *      time zone is unchanged and the return value is false.
     */
    public static boolean parseTZoffset(String text, GregorianCalendar cal, 
            ParsePosition initialWhere) 
    {
        ParsePosition where = new ParsePosition(initialWhere.getIndex());
        TimeZone tz = new SimpleTimeZone(0, "GMT");
        int tzHours, tzMin;
        char sign = skipOptionals(text, where, "Z+- ");
        boolean hadGMT = (sign == 'Z' || skipString(text, "GMT", where) 
                || skipString(text, "UTC", where));
        sign = ( ! hadGMT) ? sign : skipOptionals(text, where, "+- "); 
        
        tzHours = parseTimeField(text, where, 2, -999);
        skipOptionals(text, where, "\': ");
        tzMin = parseTimeField(text, where, 2, 0);
        skipOptionals(text, where, "\' "); 
        
        if (tzHours != -999) 
        {                    // we parsed a time zone in default format
            int hrSign = (sign == '-' ? -1 :+1);
            tz.setRawOffset(restrainTZoffset(hrSign*(tzHours*MILLIS_PER_HOUR + tzMin*MILLIS_PER_MINUTE))); 
            tz.setID("unknown");
        }
        else if ( ! hadGMT)
        {            // try to process as a name; "GMT" or "UTC" has already been processed
            String tzText = text.substring(initialWhere.getIndex()).trim();
            tz = TimeZone.getTimeZone(tzText);
            // getTimeZone returns "GMT" for unknown ids
            if ("GMT".equals(tz.getID()))  
            {                // no timezone in text
                // cal amd initialWhere are unchanged
                return false;
            }
            else
            {                // we got a tz by name; use it
                where.setIndex(text.length());
            }
        }
        adjustTimeZoneNicely(cal, tz);
        initialWhere.setIndex(where.getIndex());
        return true;
    }
    
    /**
     * Parses a big-endian date: year month day hour min sec.
     * The year must be four digits. Other fields may be adjacent 
     * and delimited by length or they may follow appropriate delimiters.
     *     year [ -/]* month [ -/]* dayofmonth [ T]* hour [:] min [:] sec [.secFraction]
     * If any numeric field is omitted, all following fields must also be omitted.
     * No time zone is processed.
     * 
     * Ambiguous dates can produce unexpected results. For example:
     *      1970 12 23:08 will parse as 1970 December 23 00:08:00 
     * 
     * @param text The string to parse.
     * 
     * @param initialWhere Where to begin the parse. On return the index
     *      is advanced to just beyond the last character processed.
     *      The error index is ignored and unchanged.
     * 
     * @return a GregorianCalendar representing the parsed date. 
     *      Or null if the text did not begin with at least four digits.
     */
    public static GregorianCalendar parseBigEndianDate(String text, 
            ParsePosition initialWhere) 
    {
        ParsePosition where = new ParsePosition(initialWhere.getIndex());
        int year = parseTimeField(text, where, 4, 0);
        if (where.getIndex() != 4 + initialWhere.getIndex()) 
        {
            return null;
        }
        skipOptionals(text, where, "/- ");
        int month = parseTimeField(text, where, 2, 1) - 1; // Calendar months are 0...11
        skipOptionals(text, where, "/- ");
        int day = parseTimeField(text, where, 2, 1);
        skipOptionals(text, where, " T");
        int hour = parseTimeField(text, where, 2, 0);
        skipOptionals(text, where, ": ");
        int minute = parseTimeField(text, where, 2, 0);
        skipOptionals(text, where, ": ");
        int second = parseTimeField(text, where, 2, 0);
        char nextC = skipOptionals(text, where, ".");
        if (nextC == '.')
        {
            // fractions of a second: skip upto 19 digits
            parseTimeField(text, where, 19, 0);
        }

        GregorianCalendar dest = newGreg();
        try 
        {
            dest.set(year, month, day, hour, minute, second);
            dest.getTimeInMillis();    // trigger limit tests
        }
        catch (IllegalArgumentException ill) 
        {
            return  null;
        }
        initialWhere.setIndex(where.getIndex());
        skipOptionals(text, initialWhere, " ");
        return dest;    // dest has at least a year value
    }

    /**
     * See if text can be parsed as a date according to any of a list of 
     * formats. The time zone may be included as part of the format, or
     * omitted in favor of later testing for a trailing time zone.
     * 
     * @param text The text to be parsed.
     * 
     * @param fmts A list of formats to be tried. The syntax is that for 
     *      {@link #java.text.SimpleDateFormat}
     * 
     * @param initialWhere At start this is the position to begin
     *      examining the text. Upon return it will have been
     *      incremented to refer to the next non-space character after the date.
     *      If no date was found, the value is unchanged.
     *      The error index is ignored and unchanged.
     * 
     * @return null for failure to find a date, or the GregorianCalendar
     *      for the date that was found. Unless a time zone was 
     *      part of the format, the time zone will be GMT+0
     */
    public static GregorianCalendar parseSimpleDate(String text, String[] fmts, 
            ParsePosition initialWhere) 
    {
        for(String fmt : fmts)
        {
            ParsePosition where = new ParsePosition(initialWhere.getIndex());
            SimpleDateFormat sdf = new SimpleDateFormat(fmt, Locale.ENGLISH);
            GregorianCalendar retCal = newGreg();
            sdf.setCalendar(retCal);
            if (sdf.parse(text, where) != null)
            {
                initialWhere.setIndex(where.getIndex());
                skipOptionals(text, initialWhere, " ");
                return retCal;
            }
        }
        return null;
    }

    
    /**
     * Parses a String to see if it begins with a date, and if so, 
     * returns that date. The date must be strictly correct--no 
     * field may exceed the appropriate limit.
     * (That is, the Calendar has setLenient(false).) 
     * Skips initial spaces, but does NOT check for "D:"
     * 
     * The scan first tries parseBigEndianDate and parseTZoffset
     * and then tries parseSimpleDate with appropriate formats, 
     * again followed by parseTZoffset. If at any stage the entire 
     * text is consumed, that date value is returned immediately. 
     * Otherwise the date that consumes the longest initial part
     * of the text is returned.
     * 
     * - PDF format dates are among those recognized by parseBigEndianDate.
     * - The formats tried are alphaStartFormats or digitStartFormat and
     * any listed in the value of moreFmts.
     * 
     * @param text The String that may begin with a date. Must not be null.
     *      Initial spaces and "D:" are skipped over.
     * @param moreFmts Additional formats to be tried after trying the
     *      built-in formats.
     * @param initialWhere where Parsing begins at the given position in text. If the
     *      parse succeeds, the index of where is advanced to point 
     *      to the first unrecognized character.
     *      The error index is ignored and unchanged.
     * @return A GregorianCalendar for the date. If no date is found, 
     *      returns null. The time zone will be GMT+0 unless parsing 
     *      succeeded with a format containing a time zone. (Only one
     *      builtin format contains a time zone.)
     * 
     */
    public static Calendar parseDate(String text, String[] moreFmts, 
            ParsePosition initialWhere) 
    {
        // place to remember longestr date string
        int longestLen = -999999;  // theorem: this value will never be used
                // proof: longestLen is only used if longestDate is not null
        GregorianCalendar longestDate = null; // null says no date found yet
        int whereLen;   // tempcopy of where.getIndex()
        
        ParsePosition where = new ParsePosition(initialWhere.getIndex());
        // check for null (throws exception) and trim off surrounding spaces
        skipOptionals(text, where, " ");
        int startPosition = where.getIndex();

        // try big-endian parse
        GregorianCalendar retCal = parseBigEndianDate(text, where);
        // check for success and a timezone
        if (retCal != null &&
                (where.getIndex() == text.length() 
                || parseTZoffset(text, retCal, where))) 
        {
            // if text is fully consumed, return the date
            // else remember it and its length
            whereLen = where.getIndex();
            if (whereLen == text.length()) 
            {
                initialWhere.setIndex(whereLen);
                return retCal;
            }
            longestLen = whereLen;
            longestDate = retCal;
        }

        // try one of the sets of standard formats
        where.setIndex(startPosition);
        String [] formats 
                = Character.isDigit(text.charAt(startPosition))
                ? DIGIT_START_FORMATS
                : ALPHA_START_FORMATS;
        retCal = parseSimpleDate(text, formats, where);
        // check for success and a timezone
        if (retCal != null && 
                (where.getIndex() == text.length() 
                || parseTZoffset(text, retCal, where)))
        {
            // if text is fully consumed, return the date
            // else remember it and its length
            whereLen = where.getIndex();
            if (whereLen == text.length()) 
            {
                initialWhere.setIndex(whereLen);
                return retCal;
            }
            if (whereLen > longestLen) 
            {
                longestLen = whereLen;
                longestDate = retCal;
            }
        }
        
        // try the supplied formats
        if (moreFmts != null)
        {
            where.setIndex(startPosition);
            retCal = parseSimpleDate(text, moreFmts, where);
            if (retCal != null && 
                (where.getIndex() == text.length() 
                || parseTZoffset(text, retCal, where)))
            {
                whereLen = where.getIndex();
                // if text is fully consumed, return the date
                // else remember it and its length
                if (whereLen == text.length() || 
                        (longestDate != null && whereLen > longestLen)) 
                {
                    initialWhere.setIndex(whereLen);
                    return retCal;
                }
            }
        }
        if (longestDate != null) 
        {
            initialWhere.setIndex(longestLen);
            return longestDate;
        }
        return retCal;
    }
       
    /**
     * Converts a string to a Calendar by parsing the String for a date.
     * @see toCalendar(String).
     *
     * The returned value will have 0 for DST_OFFSET.
     * 
     * @param text The COSString representation of a date.
     * @return The Calendar that the text string represents. 
     *      Or null if text was null.
     * @throws IOException If the date string is not in the correct format.
     * @deprecated This method throws an IOException for failure. Replace
     *      calls to it with {@link #toCalendar(text.getString(), null)} 
     *      and test for failure with
     *          (value == null || value.get(Calendar.YEAR) == INVALID_YEAR)
     */
    public static Calendar toCalendar(COSString text) throws IOException
    {
        if (text == null)
        {
            return null;    
        }
        return toCalendar(text.getString());
    }
    
    /**
     * Converts a string date to a Calendar date value; equivalent to 
     * {@link #toCalendar(String, null)}, 
     * but throws an IOException for failure.
     * 
     * The returned value will have 0 for DST_OFFSET.
     * 
     * @param text The string representation of the calendar.
     * @return The Calendar that this string represents 
     *      or null if the incoming text is null.
     * @throws IOException If the date string is non-null 
     *      and not a parseable date.
     * @deprecated This method throws an IOException for failure. Replace
     *      calls to it with {@link #toCalendar(text, null)} 
     *      and test for failure with
     *          (value == null || value.get(Calendar.YEAR) == INVALID_YEAR)
     */
    public static Calendar toCalendar(String text) throws IOException
    {
        if (text == null)
        {
            return null;    
        }
        Calendar val = toCalendar(text, null);
        if (val != null && val.get(Calendar.YEAR) == INVALID_YEAR)  
        {
            throw new IOException("Error converting date: " + text);
        }
        return val;
    }
    
    /**
     * Converts a string to a calendar. The entire string must be consumed.
     * The date must be strictly correct; that is, no field may exceed
     * the appropriate limit. Uses {@link #parseDate} to do the actual parsing.
     * 
     * The returned value will have 0 for DST_OFFSET.
     * 
     * @param text The text to parse. Initial spaces and "D:" are skipped over.
     * @param moreFmts An Array of formats (as Strings) to try 
     *      in addition to the standard list.
     * @return the Calendar value corresponding to the date text. 
     *      If text does not represent a valid date, 
     *      the value is January 1 on year INVALID_YEAR at 0:0:0 GMT.
     * 
     */
    public static Calendar toCalendar(String text, String[] moreFmts)
    {
        ParsePosition where = new ParsePosition(0);
        skipOptionals(text, where, " ");
        skipString(text, "D:", where);
        Calendar retCal = parseDate(text, moreFmts, where);   // PARSE THE TEXT
        if (retCal == null || where.getIndex() != text.length()) 
        {
            // the date string is invalid for all formats we tried,
            retCal = newGreg();
            retCal.set(INVALID_YEAR, 0, 1, 0, 0, 0);
        }
        return retCal;
    }
}
