package org.apache.xmpbox;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Test;

/**
 * Test the date conversion utility.
 *
 */
public class DateConverterTest
{

    /**
     * Test several ISO6801 date formats.
     * 
     * Test with additional time zone
     * information normally not supported by ISO8601
     *
     * @throws Exception when there is an exception
     */
    @Test
    public void testDateConversion() throws Exception
    {

        final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm.ss.SSSXXX");
        Calendar jaxbCal = null,
                convDate = null;
        // Test partial dates
        convDate = DateConverter.toCalendar("2015-02-02");
        assertEquals(2015, convDate.get(Calendar.YEAR));
        
        // Test some time zone offsets
        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192Z");
        convDate = DateConverter.toCalendar("2015-02-02T16:37:19.192Z");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192+00:00");
        convDate = DateConverter.toCalendar("2015-02-02T16:37:19.192Z");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192+02:00");
        convDate = DateConverter.toCalendar("2015-02-02T16:37:19.192+02:00");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192Z");
        convDate = DateConverter.toCalendar("2015-02-02T08:37:19.192PST");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

        jaxbCal = javax.xml.bind.DatatypeConverter.parseDateTime("2015-02-02T16:37:19.192+01:00");
        convDate = DateConverter.toCalendar("2015-02-02T16:37:19.192Europe/Berlin");
        assertEquals(dateFormat.format(jaxbCal.getTime()), dateFormat.format(convDate.getTime()));

    }
}
