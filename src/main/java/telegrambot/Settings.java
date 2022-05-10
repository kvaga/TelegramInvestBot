package telegrambot;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.kvaga.telegrambot.web.server.servlets.SettingsServlet;
import ru.kvaga.telegrambot.web.util.ServerUtils;

@XmlRootElement
public class Settings {
	final private static Logger log = LogManager.getLogger(Settings.class);
	private static File file=new File(ConfigMap.configPath+File.separator+"Settings.xml");
	private int telegramNotificationsTopCountForSending;

	public Settings() {
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
			if(!file.exists()) {
				Settings settings = new Settings();
				file.createNewFile();
				settings.save();
			}
	    	JAXBContext jaxbContext;
		    jaxbContext = JAXBContext.newInstance((new Settings()).getClass());           
		    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		    return (Settings)jaxbUnmarshaller.unmarshal(file);	
	}
}
