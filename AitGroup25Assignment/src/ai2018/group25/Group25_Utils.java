package ai2018.group25;

import java.util.Map;

import genius.core.misc.Range;

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
	
	public static Range createRange(double startPoint, double range) {
		return new Range(startPoint - range / 2, startPoint + range / 2);
	}
}
