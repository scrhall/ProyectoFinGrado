package isaFoundry.email;


import isaFoundry.core.Core;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.search.FlagTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;


public class EmailReadService {

	private static class KeepAliveRunnable implements Runnable {

		private static final long	KEEP_ALIVE_FREQ	= 300000;	// 5 minutes
		private IMAPFolder			folder;

		public KeepAliveRunnable(IMAPFolder folder) {
			this.folder = folder;
		}

		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(KEEP_ALIVE_FREQ);
					// Perform a NOOP just to keep alive the connection
					Log.info("Performing a NOOP to keep alvie the connection");
					this.folder.doCommand(new IMAPFolder.ProtocolCommand() {

						public Object doCommand(IMAPProtocol p) throws ProtocolException {
							p.simpleCommand("NOOP" , null);
							return null;
						}
					});
				} catch (InterruptedException e) {
					// Ignore, just aborting the thread...
				} catch (MessagingException e) {
					// Shouldn't really happen...
					Log.warn("Unexpected exception while keeping alive the IDLE connection" , e);
				}
			}
		}
	}

	private static Logger		Log			= LoggerFactory.getLogger(EmailReadService.class);
	private final Properties	properties	= new Properties();
	private String				pass , user , host;
	private IMAPFolder			inbox;
	private Session				session;
	private EmailService		emailService;

	// Constructor
	public EmailReadService(EmailService emailService) {
		try {
			this.emailService = emailService;
			Properties config = new Properties();
			config.load(this.getClass().getResourceAsStream("/config/emailRead.properties"));
			this.user = config.getProperty("USER");
			this.pass = config.getProperty("PASSWORD");
			this.host = config.getProperty("HOST");
			this.properties.put("mail.store.protocol" , "imaps");
			this.session = Session.getDefaultInstance(this.properties);
		} catch (FileNotFoundException e) {
			Log.error("Error: Archivo no encontrado | emailRead.properties " + e);
		} catch (IOException e) {
			Log.info("Error: Entrada/Salida |  emailRead.properties " + e);
		} catch (NullPointerException e) {
			Log.info("Error: NullPointerException | emailRead.properties " + e);
		}
	}

	public boolean connect() {
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol" , "imaps");
			Store store = this.session.getStore("imaps");
			store.connect(this.host , this.user , this.pass);
			this.inbox = (IMAPFolder) store.getFolder("Inbox");
			this.inbox.addMessageCountListener(new MessageCountAdapter() {

				@Override
				public void messagesAdded(MessageCountEvent ev) {
					try {
						Core.doTasks(EmailReadService.this.emailService.taskReceived());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			this.startListening(this.inbox);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getText(Part p) throws MessagingException, IOException {
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

	public Message[] readEmails() {
		try {
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN) , false);
			Message messages[] = this.inbox.search(ft);
			for (Message msg : messages) {
				msg.setFlag(Flags.Flag.SEEN , true); // Marca Los mensajes como
				// leidos
			}
			return messages;
		} catch (MessagingException e) {
			EmailReadService.Log.error("Error: MessagingException al leer los mensajes | " + e);
		}
		return null;
	}

	public void startListening(IMAPFolder imapFolder) {
		// We need to create a new thread to keep alive the connection
		try {
			EmailReadService.Log.info("Inicio de la escucha");
			imapFolder.open(Folder.READ_WRITE);
			Thread t = new Thread(new KeepAliveRunnable(imapFolder) , "IdleConnectionKeepAlive");
			t.start();
			EmailReadService.Log.info("Iniciado hilo para mantener la conexion activa");
			while (!Thread.interrupted()) {
				Log.debug("Starting IDLE");
				try {
					imapFolder.idle();
				} catch (MessagingException e) {
					Log.warn("Messaging exception during IDLE" , e);
					throw new RuntimeException(e);
				}
			}
			// Shutdown keep alive thread
			if (t.isAlive()) {
				t.interrupt();
			}
		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
