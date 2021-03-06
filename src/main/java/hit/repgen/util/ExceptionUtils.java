package hit.repgen.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

	public static String getStackTrace(Throwable t){
		if( t == null ){
			return null;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString();
	}
	
}
