package telegrambot;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.telegrambot.web.server.servlets.SettingsServlet;
import ru.kvaga.telegrambot.web.server.servlets.WorkingDay;
import ru.kvaga.telegrambot.web.server.servlets.WorkingHour;
import ru.kvaga.telegrambot.web.util.ServerUtils;

@XmlRootElement
public class Settings {
	private static Settings settings=null;
	final private static Logger log = LogManager.getLogger(Settings.class);
	private static File file=new File(ConfigMap.configPath+File.separator+"Settings.xml");
	private int telegramNotificationsTopCountForSending;
	private HashSet<WorkingDay> workingDays; 
	private WorkingHour workingHours; 

	public WorkingHour getWorkingHours() {
		return workingHours;
	}
	public void setWorkingHours(WorkingHour workingHours) {
		this.workingHours = workingHours;
	}
	public HashSet<WorkingDay> getWorkingDays() {
		return workingDays;
	}
	public void setWorkingDays(HashSet<WorkingDay> workingDays) {
		this.workingDays = workingDays;
	}


	public static Settings getInstance() throws JAXBException, IOException {
		if(settings==null) {
			settings = Settings.load();
		}
		return settings;
	}
	private Settings() {
		this.telegramNotificationsTopCountForSending=5;
	}
	public int getTelegramNotificationsTopCountForSending() {
		return telegramNotificationsTopCountForSending;
	}

	public void setTelegramNotificationsTopCountForSending(int telegramNotificationsTopCountForSending) {
		this.telegramNotificationsTopCountForSending = telegramNotificationsTopCountForSending;
	}
	
	public synchronized void save() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(this.getClass());
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(this, file);
        log.debug("File ["+file+"] saved");
	}
	
	public synchronized static Settings load() throws JAXBException, IOException {
		Settings settings=null;	
		if(!file.exists()) {
				settings = new Settings();
				file.createNewFile();
				settings.save();
			}
			
	    	JAXBContext jaxbContext;
		    jaxbContext = JAXBContext.newInstance((new Settings()).getClass());           
		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    settings = (Settings)jaxbUnmarshaller.unmarshal(file);
		    if(settings.workingHours==null) {
		    	settings.workingHours = new WorkingHour();
		    }
		    
		    if(settings.workingDays==null) {
		    	settings.workingDays = new HashSet<WorkingDay>(
//		    	Map.ofEntries(
		    			new HashSet<WorkingDay>(Arrays.asList(
		    					new WorkingDay(Calendar.MONDAY),
		    					new WorkingDay(Calendar.TUESDAY),
		    					new WorkingDay(Calendar.WEDNESDAY),
		    					new WorkingDay(Calendar.THURSDAY),
		    					new WorkingDay(Calendar.FRIDAY),
		    					new WorkingDay(Calendar.SATURDAY),
		    					new WorkingDay(Calendar.SUNDAY)
		    					)
//		  					    new AbstractMap.SimpleEntry<WorkingDay>(new WorkingDay()),
//		  					    new AbstractMap.SimpleEntry<String,Boolean>("Tuesday", Boolean.TRUE),
//		  					    new AbstractMap.SimpleEntry<String,Boolean>("Wednesday", Boolean.TRUE),
//		  					    new AbstractMap.SimpleEntry<String,Boolean>("Thursday", Boolean.TRUE),
//		  					    new AbstractMap.SimpleEntry<String,Boolean>("Friday", Boolean.TRUE),
//		  					    new AbstractMap.SimpleEntry<String,Boolean>("Saturday", Boolean.TRUE),
//		  					    new AbstractMap.SimpleEntry<String,Boolean>("Sunday", Boolean.TRUE)	    
		  					  )
		    			
		    			);
		    }
		    return settings;
	}
	public void updateWorkingDay(WorkingDay day) {
		for(WorkingDay item : workingDays) {
			if(item.getId() == day.getId()) {
				workingDays.remove(item);
				workingDays.add(day);
				break;
			}
		}
	}
}
