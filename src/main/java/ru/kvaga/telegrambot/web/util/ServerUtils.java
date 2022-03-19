package ru.kvaga.telegrambot.web.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.investments.stocks.StockItem;
import telegrambot.ConfigMap;

public class ServerUtils {
	 final static Logger log = LogManager.getLogger(ServerUtils.class);
	public static synchronized void saveXMLObjectToFile(File file, Object obj) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(obj.getClass());
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(obj, file);
	}
	public static Object readXMLObjectFromFile(File xmlFile, Object obj) throws JAXBException {
		System.out.println("xmlFile: " + xmlFile);
		if(!xmlFile.exists()) {
			log.warn("File ["+xmlFile+"] doesn't exist");
			return null;
		}
    	JAXBContext jaxbContext;
	    jaxbContext = JAXBContext.newInstance(obj.getClass());           
	    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	    return jaxbUnmarshaller.unmarshal(xmlFile);
	}
	public static synchronized File getStockFileByName(String name) {
		return new File(ConfigMap.stocksPath+File.separator+name+".xml");
	}
	public static synchronized File getBondFileByName(String name) {
		return new File(ConfigMap.bondsPath+File.separator+name+".xml");
	}public static synchronized File getEtfFileByName(String name) {
		return new File(ConfigMap.etfsPath+File.separator+name+".xml");
	}
	
	public static synchronized String getHTMLSuccessText(String text) {
		return "<font color=\"green\">"+text+"</font>";
	}
	public static synchronized String getHTMLFailText(String text) {
		return "<font color=\"red\">"+text+"</font>";
	}
	public static synchronized String getHTMLFailText(Exception e) {
		return "<font color=\"red\">Exception: "+e.getMessage()+", Cause: "+e.getCause()+"</font>";
	}
	
	public static synchronized boolean parameterExists(String paramName, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if(request.getParameter(paramName)==null) {
			log.debug("Parameter ["+paramName+"] can't be null");
			response.getWriter().write("Parameter ["+paramName+"] can't be null");
			return false;
		}
		return true;
	}
	
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
	
	public File[] getListFiles(String dir) throws IOException {
		File path = new File(dir);
	    return path.listFiles();
	}
}
