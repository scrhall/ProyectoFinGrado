package isaFoundry.email;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailReadService {

	private Logger				Log			= LoggerFactory.getLogger(EmailReadService.class);
	private final Properties	properties	= new Properties();
	private String				pass , user , host;
	private Folder				inbox;
	private Session				session;

	// Constructor
	public EmailReadService() {
		try {
			Properties config = new Properties();
			config.load(this.getClass().getResourceAsStream("/config/emailRead.properties"));
			this.user = config.getProperty("USER");
			this.pass = config.getProperty("PASSWORD");
			this.host = config.getProperty("HOST");
			this.properties.put("mail.store.protocol" , "imaps");
			this.session = Session.getDefaultInstance(this.properties);
		} catch (FileNotFoundException e) {
			this.Log.error("Error: Archivo no encontrado | emailRead.properties " + e);
		} catch (IOException e) {
			this.Log.info("Error: Entrada/Salida |  emailRead.properties " + e);
		} catch (NullPointerException e) {
			this.Log.info("Error: NullPointerException | emailRead.properties " + e);
		}
	}

	public boolean connect() {
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol" , "imaps");
			Store store = this.session.getStore("imaps");
			store.connect(this.host , this.user , this.pass);
			this.inbox = store.getFolder("Inbox");
			// Obtenemos la bandeja de entrada como carpeta a analizar
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public int getUnreadMessageCount() {
		try {
			this.inbox.open(Folder.READ_ONLY);
			int count = this.inbox.getUnreadMessageCount();
			this.inbox.close(false);
			return count;
		} catch (Exception e) {
			System.out.println(e);
			return -1;
			// En caso de una excepcion retornamos -1
		}
	}

	public List<Email> readEmails() {
		List<Email> emails = new ArrayList<Email>();
		try {
			this.inbox.open(Folder.READ_ONLY);//(Folder.READ_WRITE);
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN) , false);
			Message messages[] = this.inbox.search(ft);
			for (Message msg : messages) {
				Email email = new Email();
				email.Subject = msg.getSubject();
				email.From = msg.getFrom()[0].toString();
				email.Body = this.getText(msg);
				emails.add(email);
				//msg.setFlag(Flags.Flag.SEEN, true);  //Marca Los mensajes como leidos
			}
			this.inbox.close(false);
		} catch (MessagingException e) {
			this.Log.error("Error: MessagingException al leer los mensajes | " + e);
		} catch (IOException e) {
			this.Log.error("Error: Entrada/Salida al leer los mensajes | " + e);
		}
		return emails;
	}

	private String getText(Part p) throws MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			p.isMimeType("text/html");
			return s;
		}
		if (p.isMimeType("multipart/alternative")) {
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null) {
						text = this.getText(bp);
					}
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = this.getText(bp);
					if (s != null) {
						return s;
					}
				} else {
					return this.getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = this.getText(mp.getBodyPart(i));
				if (s != null) {
					return s;
				}
			}
		}
		return null;
	}
}
