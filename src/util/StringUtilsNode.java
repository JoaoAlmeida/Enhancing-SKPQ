package util;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Clob;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;

public class StringUtilsNode {

	public final static String DATE_FORMAT_SLASH = "dd/mm/yyyy";

	public final static String DATE_FORMAT_SCORE = "dd-mm-yyyy";

	public static void main(String[] args) {
		
		//System.out.println(isNegative(-1));
		//System.out.println(isNegative(1));
		
		//System.out.println(StringUtilsNode.getDifferenceAndReturnNormalized01(-45d,-1d));
		System.out.println(StringUtilsNode.getMachineName());
/*		StringUtilsNode.getDifferenceAndReturnNormalized012(-344444d,16556d);
		StringUtilsNode.getDifferenceAndReturnNormalized012(-45d,-1d);
		StringUtilsNode.getDifferenceAndReturnNormalized012(-678945d,-14545d);*/
		//System.out.println(StringUtilsNode.getDifferenceAndReturnNormalized01(1454d,-7444448d));
	//	System.out.println(StringUtilsNode.getDifferenceAndReturnNormalized01(1d,345451d));
		
		
	}
	
	
	public static String elapsed( long duration ) {
	    final TimeUnit scale = NANOSECONDS;

	    long days = scale.toDays( duration );
	    duration -= DAYS.toMillis( days );
	    long hours = scale.toHours( duration );
	    duration -= HOURS.toMillis( hours );
	    long minutes = scale.toMinutes( duration );
	    duration -= MINUTES.toMillis( minutes );
	    long seconds = scale.toSeconds( duration );
	    duration -= SECONDS.toMillis( seconds );
	    long millis = scale.toMillis( duration );
	    duration -= MILLISECONDS.toMillis( seconds );
	    long nanos = scale.toNanos( duration );

	    return String.format(
	      "%d days, %d hours, %d minutes, %d seconds, %d millis, %d nanos",
	      days, hours, minutes, seconds, millis, nanos );
	  }	
	
	public static String getDuration(long millis) {
	    if(millis < 0) {
	      throw new IllegalArgumentException("Duration must be greater than zero!");
	    }

	    long days = TimeUnit.MILLISECONDS.toDays(millis);
	    millis -= TimeUnit.DAYS.toMillis(days);
	    long hours = TimeUnit.MILLISECONDS.toHours(millis);
	    millis -= TimeUnit.HOURS.toMillis(hours);
	    long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
	    millis -= TimeUnit.MINUTES.toMillis(minutes);
	    long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

	    StringBuilder sb = new StringBuilder(64);
	   if (days>0) {
		    sb.append(days);
		    sb.append(" Days (");		
	   }else if (hours>0) {
		    sb.append(hours);
		    sb.append(" Hours (");		   
	   }else if (minutes>0) {
		    sb.append(minutes);
		    sb.append(" Minutes (");		   
	   }else if (seconds>0) {
		    sb.append(seconds);
		    sb.append(" Seconds (");		   
	   }
	    sb.append(millis);
	    sb.append(") ms");

	    return sb.toString();
	}	
	
	
	public static String getMachineName(){
		String hostname = "Unknown";

		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
		}
		catch (UnknownHostException ex)
		{
		    System.out.println("Hostname can not be resolved");
		}
		return hostname;
	}
	
	 public static String greeting(){
		    
	    	String greeting = "Good Bye";
	    	
	    	int timeOfDay = Calendar.getInstance(TimeZone.getTimeZone("Europe/Dublin")).get(Calendar.HOUR_OF_DAY);
		
	    	
		    if(timeOfDay >= 0 && timeOfDay < 12){
		    	greeting = "Good Morning";
			}else if(timeOfDay >= 12 && timeOfDay < 16){
				greeting = "Good Afternoon";
			}else if(timeOfDay >= 16 && timeOfDay < 21){
				greeting = "Good Evening";
			}else if(timeOfDay >= 21 && timeOfDay < 24){
			        greeting = "Good Morning";
			}
		
		    return greeting;
	    }	
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}	
	
	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAscending(
			Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(
				map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}	
	
	public static boolean isNegative(Number number) {
	    return (Double.doubleToLongBits(number.doubleValue()) & Long.MIN_VALUE) == Long.MIN_VALUE;
	}
	
	
	public static double getDifferenceAndReturnNormalized01NotWorking(double value1, double value2) {
		
		double delta = Math.abs(value1-value2);
		if (value1 >= value2) {
			return StringUtilsNode.normalize(delta, value1, value2);
		}else{
			return StringUtilsNode.normalize(delta, value2, value1);
		}
		
		
		
		
	}
	
	public static double getDifferenceAndReturnNormalized01(double value1, double value2) {
		if (value1==value2) {
			return 0d;
		}
		
		double delta = Math.abs(value1-value2);
		
		double[] v = {value1,value2,delta};
		
		List<Double> b = Arrays.asList(ArrayUtils.toObject(v));
		double minV = Collections.min(b);
		
		for(int i=0; i<v.length; i++) {v[i] -= minV;}
		
		double maxV = Collections.max(b);
		
		for(int i=0; i<v.length; i++) {
			v[i] /= ( maxV - minV );
			//System.out.println(v[i]);
		}
		return v[1];
		
	}
	
	
	public static double getNormalized01(double min, double currentValue ,double max) {
		
		//System.out.println(min);
		//System.out.println(currentValue);
		//System.out.println(max);
		
		if (min==max) {
			return 0d;
		}
		
		double[] v = {min,max,currentValue};
		
		List<Double> b = Arrays.asList(ArrayUtils.toObject(v));
		double minV = Collections.min(b);
		
		for(int i=0; i<v.length; i++) {v[i] -= minV;}
		
		double maxV = Collections.max(b);
		
		for(int i=0; i<v.length; i++) {
			v[i] /= ( maxV - minV );
			//System.out.println(v[i]);
		}
		return v[1];
		
	}	
	
	
	
	/**
	 * @param rawValue
	 * @param max
	 * @param min
	 * @return
	 */
	public static double normalize(double rawValue, double max, double min){
		if (max==min) {
			return 0d;
		}
		double scaledValue = (rawValue - min) / (max - min);
		
		
		return scaledValue;
	}
	

	public static String convertClobToString(Clob clob) {
		String data = new String();
		try {
			if (clob != null) {
				Reader is = clob.getCharacterStream();
				StringBuffer sb = new StringBuffer();
				int length = (int) clob.length();
				if (length > 0) {
					char[] buffer = new char[length];
					int count = 0;
					while ((count = is.read(buffer)) != -1)
						sb.append(buffer);
					data = new String(sb);
				}
				return data;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static boolean isScientificNotation(String numberString) {

	    // Validate number
	    try {
	        new BigDecimal(numberString);
	    } catch (NumberFormatException e) {
	        return false;
	    }

	    // Check for scientific notation
	    return numberString.toUpperCase().contains("E");   
	}

	/**
	 * @param tags
	 * @return
	 */
	public static String getStringList(List<String> tags) {
		StringBuilder tagString = new StringBuilder();
		Set<String> singleTag = new HashSet<String>();
		if (tags != null) {
			for (String stringItem : tags) {
				if (singleTag != null && !singleTag.contains(stringItem)) {
					singleTag.add(stringItem);
					tagString.append(stringItem.toLowerCase());
					tagString.append(" ");
				}
			}
		}

		try {
			if (tags.size() > 0 && tagString.length() > 1 && (tagString.lastIndexOf(",") == tagString.length() - 1)) {
				tagString = new StringBuilder(tagString.substring(0, tagString.lastIndexOf(",")));
				// System.out.println("*******   "+tagString);
			}
		} catch (StringIndexOutOfBoundsException e) {
			tagString = new StringBuilder(tagString.substring(0, 5));
		}

		return tagString.toString();
	}

	/**
	 * @param pNumber
	 * @param scale
	 * @return
	 */
	public static double getNumberFormated(double pNumber, int scale) {
		double currency = new BigDecimal(pNumber).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
		return currency;
	}

	/**
	 * Verifies if the source string contains the value string
	 * 
	 * @param source
	 * @param value
	 * 
	 * @return
	 */
	public static boolean contains(String source, String value) {
		boolean contains;

		if (source == null || value == null) {
			contains = false;
		} else {
			contains = (source.indexOf(value) != -1);
		}
		return contains;
	}

	/**
	 * Replaces all ocurrences of oldValue for newValue in the source parameter
	 * 
	 * @param source
	 * @param oldValue
	 * @param newValue
	 * 
	 * @return
	 */
	public static String replace(String source, String oldValue, String newValue) {
		if (source == null || oldValue == null || newValue == null) {
			throw new IllegalArgumentException("source == null || oldValue == null || newValue == null");
		}

		StringBuffer newString = new StringBuffer(source.length());

		int beginIndex = 0;
		int oldValueIndex = source.indexOf(oldValue);

		while (oldValueIndex != -1) {
			newString.append(source.substring(beginIndex, oldValueIndex));
			newString.append(newValue);

			beginIndex = oldValueIndex + oldValue.length();
			oldValueIndex = source.indexOf(oldValue, beginIndex);
		}

		newString.append(source.substring(beginIndex));

		return newString.toString();
	}

	/**
	 * Verifies if two given strings are equals
	 * 
	 * @param arg1
	 * @param arg2
	 * 
	 * @return
	 */
	public static boolean equals(String arg1, String arg2) {
		boolean equals = false;

		if (arg1 == null || arg2 == null) {
			equals = (arg1 == null && arg2 == null);
		} else {
			equals = arg1.trim().equals(arg2.trim());
		}
		return equals;

	}

	/**
	 * @param date
	 * @return
	 */
	public static String getFormatedDate(Date date) {
		return getFormatedDate(date, Locale.getDefault());
	}

	public static String getFormatedDate(Date date, Locale locale) {
		// SimpleDateFormat formatter = new SimpleDateFormat(Resources.getString(Resources.DATE_FORMAT, locale));
		SimpleDateFormat formatter = new SimpleDateFormat("dd-nn-aaaa");
		return formatter.format(date);
	}

	/**
	 * @param date
	 * @return
	 */
	public static String getFormatedTime(Date date) {
		return getFormatedTime(date, Locale.getDefault());
	}

	/**
	 * @param date
	 * @return
	 */
	public static String getFormatedTime(Date date, Locale locale) {
		// SimpleDateFormat formatter = new SimpleDateFormat(Resources.getString("Resources.TIME_FORMAT", locale));
		SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss");
		return formatter.format(date);
	}
	/**
	 * Verifies if a given string is empty (null or equals to empty string)
	 * 
	 * @param value
	 * 
	 * @return
	 */
	public static boolean isEmpty(String value) {
		return (value == null || value.trim().equals(""));
	}

	/**
	 * Caso todos os valores passados estejam vazios, retorna true
	 */
	public static boolean isEmpty(String... values) {
		boolean isEmpty = true;

		if (values != null && values.length > 0) {
			int countNotEmptyValue = 0;
			for (String value : values) {
				if (!value.trim().equals(""))
					countNotEmptyValue++;
			}
			if (countNotEmptyValue > 0)
				isEmpty = false;
		}

		return isEmpty;
	}

	/**
	 * Caso exista algum valor vazio, retorna true
	 */
	public static boolean isSomeValueEmpty(String... values) {
		boolean isEmpty = true;

		if (values != null && values.length > 0) {
			for (String value : values) {
				if (value.trim().equals(""))
					return isEmpty;
			}
			isEmpty = false;
		}
		return isEmpty;
	}

	public static boolean isDateValid(String date, String format) {
		try {
			DateFormat df = new SimpleDateFormat(format);
			df.setLenient(false);
			df.parse(date);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * Replaces the "\n" break lines with "<br/>
	 * " break lines.
	 * 
	 * @param text
	 *            Original text
	 * 
	 * @return A new text containing HTML break lines insted normal break lines.
	 */
	public static String replaceBreakLines(String text) {
		return text != null ? replace(text, "\n", "<br/>") : null;
	}

	/**
	 * Replaces the "<br/>
	 * " break lines with "\n" break lines.
	 * 
	 * @param text
	 *            Original text
	 * 
	 * @return A new text containing HTML break lines insted normal break lines.
	 */
	public static String replaceInverseBreakLine(String text) {
		return text != null ? replace(text, "<br/>", "\n") : null;
	}

	public static String replaceURLEscapeChars(String value) {
		// value = replace(value, " ", "%20");
		value = replace(value, "#", "%23");
		value = replace(value, "$", "%24");
		value = replace(value, "%", "%25");
		value = replace(value, "&", "%26");
		value = replace(value, "@", "%40");
		value = replace(value, "'", "%60");
		value = replace(value, "/", "%2F");
		value = replace(value, ":", "%3A");
		value = replace(value, ";", "%3B");
		value = replace(value, "<", "%3C");
		value = replace(value, "=", "%3D");
		value = replace(value, ">", "%3E");
		value = replace(value, "?", "%3F");
		value = replace(value, "[", "%5B");
		value = replace(value, "\\", "%5C");
		value = replace(value, "]", "%5D");
		value = replace(value, "^", "%5E");
		value = replace(value, "{", "%7B");
		value = replace(value, "|", "%7C");
		value = replace(value, "}", "%7D");
		value = replace(value, "~", "%7E");
		return value;
	}

	public static String replaceInvalidChars(String value) {
		//
		value = replace(value, "#", "");
		value = replace(value, "$", "");
		value = replace(value, "%", "");
		value = replace(value, "&", "%26");
		value = replace(value, "@", "%40");
		value = replace(value, "'", "%60");
		value = replace(value, "/", "%2F");
		value = replace(value, ":", "%3A");
		value = replace(value, ";", "%3B");
		value = replace(value, "<", "%3C");
		value = replace(value, "=", "%3D");
		value = replace(value, ">", "%3E");
		value = replace(value, "?", "%3F");
		value = replace(value, "[", "%5B");
		value = replace(value, "\\", "%5C");
		value = replace(value, "]", "%5D");
		value = replace(value, "^", "%5E");
		value = replace(value, "{", "%7B");
		value = replace(value, "|", "%7C");
		value = replace(value, "}", "%7D");
		value = replace(value, "~", "%7E");
		return value;
	}

	/**
	 * It validates the email format
	 * 
	 * @param email
	 * @return
	 */
	public static boolean verifyEmailFormat(String email) {
		boolean isValid = false;

		if (email != null) {
			Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
			Matcher m = p.matcher(email);
			isValid = m.matches();
		}

		return isValid;
	}

	/**
	 * @param message
	 * @return
	 */
	public static boolean validadeMessageLenth(String message) {
		// return (message != null && message.trim().length()>=Configuration.getMaxMessageLength());
		return (message != null && message.trim().length() >= 10);
	}

	/**
	 * verify if the login contains some invalid character
	 * 
	 * @param string
	 * @return
	 */
	public static boolean hasInvalidCharacter(String string) {
		String regex = "[|\"&*=+'@#$\\%\\/?{}?:;<>,\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D8\u0152\u00DE\u00D9\u00DA\u00DB\u00DC\u00DD\u0178\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F8\u0153\u00DF\u00FE\u00F9\u00FA\u00FB\u00FC\u00FD\u00FF]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(string);
		return m.find();
	}

	public static boolean containsMoreMissingInvalidCharacterInLogin(String login) {
		boolean done = false;
		if (login.contains("?") || login.contains("-") || login.contains("`") || login.contains("�")) {
			done = true;
		}
		return done;
	}

	/**
	 * verify if there are and remove some invalid character
	 * 
	 * @param login
	 * @return
	 */
	public static String removeInvalidCharacteres(String string) {
		String corretString = string;
		if (hasInvalidCharacter(string)) {
			String regex = "[|\"&*=+'@#$%\\/?{}?:;_~<>,\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D8\u0152\u00DE\u00D9\u00DA\u00DB\u00DC\u00DD\u0178\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F8\u0153\u00DF\u00FE\u00F9\u00FA\u00FB\u00FC\u00FD\u00FF]";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(string);
			corretString = m.replaceAll("");
			// System.out.println(corretString);
		}

		corretString = corretString.replace("\\", "");

		return corretString;
	}

	/**
	 * It compares null date values before comparing dates
	 * 
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int validateNullDateValues(Date date1, Date date2) {
		int returnValue = 0;
		if ((date1 == null) & (date2 == null)) {
			returnValue = 0;
		} else if ((date1 == null) & (date2 != null)) {
			returnValue = -1;
		} else if ((date1 != null) & (date2 == null)) {
			returnValue = 1;
		}
		return returnValue;
	}

	/**
	 * @param tagString
	 * @param delimiter
	 * @return
	 */
	public static Set<String> getToken(String tagString, String delimiter) {
		Set<String> tagStrings = new HashSet<String>();
		StringTokenizer tempStringTokenizer = new StringTokenizer(tagString, delimiter);
		String term = null;
		while (tempStringTokenizer.hasMoreTokens()) {
			term = tempStringTokenizer.nextElement().toString();
			tagStrings.add(term);
		}
		return tagStrings;
	}

	/**
	 * @param tagString
	 * @param delimiter
	 * @return
	 */
	public static List<String> getListToken(String tagString, String delimiter) {
		List<String> tagStrings = new ArrayList<String>();
		StringTokenizer tempStringTokenizer = new StringTokenizer(tagString, delimiter);
		String term = null;
		while (tempStringTokenizer.hasMoreTokens()) {
			term = tempStringTokenizer.nextElement().toString();
			tagStrings.add(term);
		}
		return tagStrings;
	}

	/**
	 * @param tagString
	 * @param delimiter
	 *            example: "userId=fred&linkId=99","=","&"
	 * @return
	 */
	public static Map<String, String> getValuePairFromString(String valuePairString, String valuePairDelimiterString,
			String delimiter) {
		Map<String, String> valuePair = new HashMap<String, String>();
		StringTokenizer tempStringTokenizer = new StringTokenizer(valuePairString, delimiter);
		String term = null;
		while (tempStringTokenizer.hasMoreTokens()) {
			term = tempStringTokenizer.nextElement().toString();
			String propery = term.substring(0, term.indexOf(valuePairDelimiterString));
			String value = term.substring(term.indexOf(valuePairDelimiterString) + 1, term.length());
			valuePair.put(propery, value);
		}
		// System.out.println(valuePair.toString());
		return valuePair;
	}

	/**
	 * @param url
	 * @return
	 */
	public static String getURLName(String url) {
		try {
			url = new URL(url).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		int index = 0;
		if (url.contains("www")) {
			index = 1;
		}
		StringTokenizer tempStringTokenizer = new StringTokenizer(url, ".");
		int i = 0;
		String term = null;
		while (tempStringTokenizer.hasMoreTokens()) {
			term = tempStringTokenizer.nextElement().toString();
			if (i == index) {
				break;
			}
			i++;
		}

		return term;
	}

	public static String getDecimalFormat(Object obj) {
		DecimalFormat twoDigits = new DecimalFormat("##.##");
		return twoDigits.format(obj);

	}

	public static void saveFile(String fileName, String content) {
		try {
			File file = new File("C:\\M-Eco Software\\medworm by jan\\medwo\\" + fileName + ".sql");
			if (file.exists()) {
				FileWriter fstream = new FileWriter(file, true);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(content);
				out.close();
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}

	public static String readFileAsString(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		StringBuilder sb = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		reader.close();
		return sb.toString();
	}

	/**
	 * @param text
	 * @return String
	 */
	public static String escape(String text) {
		return text.replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"");
	}

	/**
	 * @param size
	 * @param text
	 * @return String
	 */
	public static String fillChar(int size, String text) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size; i++) {
			sb.append(text);
		}
		return sb.toString();
	}

	/**
	 * @param s
	 * @return String
	 */
	public static String removeDuplicatedEmptySpaces(String s) {
		StringBuffer toReturn = new StringBuffer();
		String[] arr = s.trim().split(" ");
		boolean emptySpaceAdded = false;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals("")) {
				if (!emptySpaceAdded) {
					toReturn.append(" ");
					emptySpaceAdded = true;
				}
			} else {
				if (!emptySpaceAdded && i > 0) {
					toReturn.append(" ");
				}
				emptySpaceAdded = false;
				toReturn.append(arr[i]);
			}
		}
		return toReturn.toString();
	}

	public static String[] getAllDirectoriesFromAPath(String fullpath) {
		String[] directories = fullpath.split("/");
		return directories;
	}

	/**
	 * Receive a string path and nomalize it.
	 * 
	 * @param path
	 */

	public static final String normalizePath(String path) {
		// Normalize the slashes and add leading slash if necessary
		String normalized = path;
		if (normalized.indexOf('\\') >= 0) {
			normalized = normalized.replace('\\', '/');
		}

		/*
		 * if (!normalized.startsWith("/")) { normalized = "/" + normalized; }
		 */

		// Resolve occurrences of "//" in the normalized path
		while (true) {
			int index = normalized.indexOf("//");
			if (index < 0) {
				break;
			}
			normalized = normalized.substring(0, index) + normalized.substring(index + 1);
		}

		// Resolve occurrences of "%20" in the normalized path
		while (true) {
			int index = normalized.indexOf("%20");
			if (index < 0) {
				break;
			}
			normalized = normalized.substring(0, index) + " " + normalized.substring(index + 3);
		}

		// Resolve occurrences of "/./" in the normalized path
		while (true) {
			int index = normalized.indexOf("/./");
			if (index < 0) {
				break;
			}
			normalized = normalized.substring(0, index) + normalized.substring(index + 2);
		}

		while (true) {
			int index = normalized.indexOf("/../");
			if (index < 0)
				break;
			if (index == 0) {
				return (null); // Trying to go outside our context
			}
			int index2 = normalized.lastIndexOf('/', index - 1);
			normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
		}

		// Return the normalized path that we have completed
		return (normalized);
	}

	public static String getSimpleFormat(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		return format.format(date);

	}

	public static String convertTweeterDateFormat(String tweetDate, String formatDate) {
		Date d = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
			d = sdf.parse(tweetDate);
		} catch (ParseException e) {// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getFormat(d, formatDate);
	}

	public static String getFormat(Date date, String formatDate) {
		SimpleDateFormat format = new SimpleDateFormat(formatDate);
		return format.format(date);
	}

	public static String removeFirstBarFromPath(String path) {
		if (path.startsWith("/"))
			return path.replaceFirst("/", "");
		return path;
	}

	public static String removeLastBarFromPath(String path) {
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1) + " ";
		}
		return path;
	}

	public static String removeFileFromPath(String path) {
		int lastIndexBar = path.lastIndexOf("/");
		if (lastIndexBar > 0) {
			path = path.substring(0, lastIndexBar);
		}
		return path;
	}

	public static boolean isNumeric(String... str) {
		for (String s : str) {
			for (char c : s.toCharArray()) {
				if (!Character.isDigit(c))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * @param str
	 * @return
	 */
	public static boolean hasDigit(String str) {
		return str.matches(".*\\d+.*");
	}	
	
	/**
	 * @param str
	 * @param index
	 * @param replace
	 * @return
	 */
	public static String replaceByIndex(String str, int index, char replace){     
	    if(str==null){
	        return str;
	    }else if(index<0 || index>=str.length()){
	        return str;
	    }
	    char[] chars = str.toCharArray();
	    chars[index] = replace;
	    return String.valueOf(chars);       
	}

	/**
	 * @param originalPath
	 * @return
	 */
	public static String codifyPath(String originalPath) {

		if (originalPath.contains("//")) {
			originalPath = removeFirstBarFromPath(originalPath);
		}

		if (originalPath.contains("/")) {
			return originalPath.replaceAll("/", "_PATH_SEPARATOR_");
		}

		return originalPath;
	}

	/**
	 * Decode paths from REST url since paths value cannot contain slash otherwise they can break the rest call.
	 * 
	 * @param codedPath
	 * @return
	 */

	public static String addBarAtTheEndOfThePath(String path) {
		if (!path.equals("") && !path.substring(path.length() - 1).equals("/")) {
			path = path + "/";
		}
		return path;
	}

	public static String shortLabel(String label) {
		if (label.length() > 37) {
			String labelTemp = label.substring(0, 19) + "...";
			labelTemp += label.substring(label.length() - 19);
			return labelTemp;
		}
		return label;
	}

	/**
	 * Decode paths from REST url since paths value cannot contain slash otherwise they can break the rest call.
	 * 
	 * @param codedPath
	 * @return
	 */
	public static String decodePath(String codedPath) {
		if (codedPath.contains("_PATH_SEPARATOR_")) {
			return codedPath.replaceAll("_PATH_SEPARATOR_", "/");
		}
		return codedPath;
	}

	public static String getSubdirectoryFromPath(String path) {
		String pathSplited[] = path.split("/");
		String subDirectory = "";

		for (int i = 0; i < pathSplited.length - 1; i++) {
			subDirectory += "/" + pathSplited[i];
		}
		return subDirectory.replaceFirst("/", "");
	}

	public static String getEncodedBase64StringToUTF8(String stringToEncode) {
		try {
			stringToEncode = new String(Base64.encodeBase64(stringToEncode.getBytes("UTF-8")), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return stringToEncode;
	}

	public static String getDecodedBase64StringToUTF8(String stringToDecode) {
		try {
			stringToDecode = new String(Base64.decodeBase64(stringToDecode.getBytes("UTF-8")), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return stringToDecode;
	}

	public static String fileNameWithoutFolders(String fileName) {
		String fileWithDir[] = fileName.split("/");
		fileName = fileWithDir[fileWithDir.length - 1];
		return fileName;
	}

}