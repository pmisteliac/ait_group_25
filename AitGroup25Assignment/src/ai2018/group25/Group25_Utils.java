package ai2018.group25;

import java.util.Map;

public final class Group25_Utils {

	private Group25_Utils(){
		
	}
	
	public static Double getParams(String paramName, Double defaultValue, Map<String, Double> paramMap) {
		if(paramName == null || defaultValue == null || paramMap == null) {
			throw new NullPointerException("One of the arguments was null.");
		}
		Double paramValue = paramMap.get(paramName);
		if (paramValue != null) {
			return paramValue;
		}
		
		return defaultValue;
	}
}
