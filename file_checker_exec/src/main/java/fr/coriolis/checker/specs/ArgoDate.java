package fr.coriolis.checker.specs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.regex.Pattern;


/**
 * @version  $HeadURL: https://inversion.nrlmry.navy.mil/svn/godae/trunk/argo/bin/java/usgdac/ArgoDate.java $
 * @version  $Id: ArgoDate.java 963 2018-08-09 16:34:21Z ignaszewski $
 */

public class ArgoDate extends Date
{
   //******************************************************
   //                  VARIABLES
   //******************************************************

   //............class variables.................

   private static final long refTime = Instant.parse("1950-01-01T00:00:00.000Z").toEpochMilli();

   // SimpleDateFormat is not thread safe.  Use factory functions.
   private static final Map<String, Supplier<SimpleDateFormat>> validFormat;

   private static final Pattern nonDigit = Pattern.compile(".*\\D.*");

   private static SimpleDateFormat createSimpleDateFormat(String pattern) {
     SimpleDateFormat df = new SimpleDateFormat(pattern);
     df.setTimeZone(TimeZone.getTimeZone("GMT"));
     df.setLenient(false);
     return df;
   }

   static {
     Map<String, Supplier<SimpleDateFormat>> validFormatTemp = new HashMap<>();
     validFormatTemp.put("DDMMYYYY", () -> createSimpleDateFormat("ddMMyyyy"));
     validFormatTemp.put("YYYYMMDDHHMMSS", () -> createSimpleDateFormat("yyyyMMddHHmmss"));
     validFormatTemp.put("YYYYMMDD", () -> createSimpleDateFormat("yyyyMMdd"));
     validFormatTemp.put("YYYY", () -> createSimpleDateFormat("yyyy"));
     validFormatTemp.put("MM", () -> createSimpleDateFormat("MM"));
     validFormatTemp.put("DD", () -> createSimpleDateFormat("dd"));
     validFormatTemp.put("HHMMSS", () -> createSimpleDateFormat("HHmmss"));
     validFormatTemp.put("HHMM", () -> createSimpleDateFormat("HHmm"));
     validFormatTemp.put("MMSS", () -> createSimpleDateFormat("mmss"));
     validFormatTemp.put("HH", () -> createSimpleDateFormat("HH"));
     validFormat = Collections.unmodifiableMap(validFormatTemp);
   }

   //static PrintStream stdout = new PrintStream(System.out);

   //******************************************************
   //                METHODS
   //******************************************************


   public static String format(Date date) {
     return validFormat.get("YYYYMMDDHHMMSS").get().format(date);
   }
   
   public static Date get(long long_juld)
   {
      return (new Date(refTime + long_juld));
   }

   public static Date get(double juld)
   {
      long long_juld = Math.round(juld * 24.D * 3600.D * 1000.D);
      return (new Date(refTime + long_juld));
   }

   /**
    * Checks the input date/time "pattern" against the known patterns typically used as 
    * Technical Parameter Units and checks the "value" to see if it conforms to the pattern.
    *
    * @param pattern  Date/time pattern to be checked against known Argo data/time patterns
    *                 (typically used as units in Argo technical files)
    * @param value  Date/time value to be compared to the pattern
    * @return Boolean null if pattern is not a known Argo pattern; true if the pattern is
    * known and the value is valid; false if the pattern is known but the value is invalid
    */
   public static Boolean checkArgoDatePattern (String pattern, String value)
   {
      SimpleDateFormat format = validFormat.get(pattern).get();

      if (format == null) {
         return null;
      }

      if (value.length() != pattern.length() || nonDigit.matcher(value).matches()) {
         return false;
      }


      try {
         Date date = format.parse(value);
      } catch (ParseException e) {
         return false;
      }
      return true;
   }

   /**
    * Returns a Date object for an Argo string date value, checking for validity
    *
    * @param dtg  String (14-char) date/time setting
    * @return Date object or null if input dtg is illegal
    */
   public static Date get(String dtg)
   {
      Date date;

      try {
         //..
        SimpleDateFormat dateFormat = validFormat.get("YYYYMMDDHHMMSS").get();
         date = dateFormat.parse(dtg);

         String tst = dateFormat.format(date);
         if (! dtg.equals(tst)) {
            date = null;
         }
         
      } catch (ParseException e) {
         date = null;
      }   

      return (date);
   }

}
