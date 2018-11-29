package ai2018.group25;

import java.util.Iterator;
import java.util.Map;

public class Group25_Map {
	private Map<String, Double> map;

	public Group25_Map(Map<String, Double> map) {
		this.map = map;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Map.Entry<String, Double>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Double> entry = iter.next();
			sb.append(entry.getKey());
			sb.append('=').append('"');
			sb.append(entry.getValue());
			sb.append('"');
			if (iter.hasNext()) {
				sb.append(',').append(' ');
			}
		}
		return sb.toString();

	}
}
