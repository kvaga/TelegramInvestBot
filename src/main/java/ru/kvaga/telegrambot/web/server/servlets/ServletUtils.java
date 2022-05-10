package ru.kvaga.telegrambot.web.server.servlets;

public class ServletUtils {
	public synchronized static String listOfParametersToString(String...parameters) {
		if(parameters!=null) {
			int i=1;
			StringBuilder sb = new StringBuilder();
			boolean first=true;
			for(String parameter : parameters) {
				if(first) {
					first=false;
					sb.append(parameter);
					i++;
				}else {
					if(i%2==0) {
						sb.append("[");
					}else {
						sb.append(", ");
					}
					sb.append(parameter);
					sb.append(" ");
					if(i%2==0) {
						sb.append("]");
					}
					i++;
				}
			}
			return sb.toString();
		}
		return null;
	}
}
