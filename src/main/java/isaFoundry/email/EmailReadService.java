package isaFoundry.email;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

public class EmailReadService {
	//Configuration
	private final Properties properties = new Properties();

	private String pass, user, host;

	private Folder inbox;
	private Session session;
	//Constructor
	public EmailReadService() {
		this.user = "Pruebas@scrhall.com";
		this.pass = "uno2tres4";
		this.host = "imap.gmail.com";
		this.properties.put("mail.store.protocol", "imaps");

		this.session = Session.getDefaultInstance(this.properties);
	}

	public boolean connect() {
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");

			Store store = this.session.getStore("imaps");
			store.connect(this.host, this.user, this.pass);

			this.inbox = store.getFolder("Inbox");
			// Obtenemos la bandeja de entrada como carpeta a analizar

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private String getText(Part p) throws MessagingException, IOException {
		boolean textIsHtml = false;

		if (p.isMimeType("text/*")) {
			String s = (String) p.getContent();
			textIsHtml = p.isMimeType("text/html");
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

	public void readEmails() {

		try {

			this.inbox.open(Folder.READ_ONLY);
			Message[] messages = this.inbox.getMessages();
			System.out.println("No of Messages : "
					+ this.inbox.getMessageCount());
			System.out.println("No of Unread Messages : "
					+ this.inbox.getUnreadMessageCount());
			System.out.println(messages.length);
			for (int i = 0; i < messages.length; i++) {

				System.out
						.println("*****************************************************************************");
				System.out.println("MESSAGE " + (i + 1) + ":");
				Message msg = messages[i];

				System.out.println("Subject: " + msg.getSubject());
				System.out.println("From: " + msg.getFrom()[0]);
				System.out.println("To: " + msg.getAllRecipients()[0]);
				System.out.println("Date: " + msg.getReceivedDate());
				System.out.println("Size: " + msg.getSize());
				System.out.println(msg.getFlags());
				System.out.println("Body: \n" + getText( msg));
				System.out.println(msg.getContentType());

			}

			this.inbox.close(false);
		} catch (MessagingException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
