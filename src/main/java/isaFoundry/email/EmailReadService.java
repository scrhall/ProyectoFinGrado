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

	/**
	 * Clase que se encarga de ejecutar un hilo paralelo para mantener la
	 * conexion con el servidor de correo IMAP
	 * 
	 */
	private static class KeepAliveRunnable implements Runnable {

		private static final long	KEEP_ALIVE_FREQ	= 300000;	// 5 minutos
		private IMAPFolder			folder;

		public KeepAliveRunnable(IMAPFolder folder) {
			this.folder = folder;
		}

		public void run() {
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(KEEP_ALIVE_FREQ);
					// Realiza un comando vacio para mantener la conexion
					// abierta.
					Log.info("Realizando cocmando NOOP al servidor de correo IMAP.");
					this.folder.doCommand(new IMAPFolder.ProtocolCommand() {

						public Object doCommand(IMAPProtocol p) throws ProtocolException {
							p.simpleCommand("NOOP" , null);
							return null;
						}
					});
				} catch (InterruptedException e) {} catch (MessagingException e) {
					Log.warn("Error: Se produjo un error mientras se intentaba mantener la conexion abierta con el servidor IMAP" , e);
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

	/**
	 * Constructor de la clase, inicializa los paramentros necesarios.
	 * 
	 * @param emailService
	 *            recibe la clase que lo contiene para poder ejecutar algunos
	 *            metodos necesarios.
	 */
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

	/**
	 * Conecta con el servidor IMAP.
	 * 
	 * @return Devuelve Verdadero si la conexion se realizo sin Errores.
	 */
	public boolean connect() {
		try {
			Log.info("Conectando con el servidor de correo IMAP.");
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
			Log.info("Error: No se pudo conectar con el servidor de correo IMAP.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Obtiene el texto de un mensaje.
	 * 
	 * @param p
	 * @return Devuelve el mensaje.
	 * @throws MessagingException
	 * @throws IOException
	 */
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

	/**
	 * Obtiene todos los mensajes no leidos del servidor de correo.
	 * 
	 * @return Devuelve un array con todos los correos.
	 */
	public Message[] readEmails() {
		try {
			Log.info("Obteniendo correos electronicos no leidos");
			FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN) , false);
			Message messages[] = this.inbox.search(ft);
			for (Message msg : messages) {
				// Marca Los mensajes como leidos
				msg.setFlag(Flags.Flag.SEEN , true);
			}
			return messages;
		} catch (MessagingException e) {
			Log.error("Error: No se pudieron leer los correos.");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Inicia la escucha en una carpeta de IMAP, para recibir eventos instantaneos.
	 * @param imapFolder 
	 */
	public void startListening(IMAPFolder imapFolder) {
		try {
			Log.info("Inicio de la escucha");
			imapFolder.open(Folder.READ_WRITE);
			Thread t = new Thread(new KeepAliveRunnable(imapFolder) , "IdleConnectionKeepAlive");
			t.start();
			Log.info("Iniciado hilo para mantener la conexion activa");
			while (!Thread.interrupted()) {
				Log.debug("Iniciando IDLE");
				try {
					imapFolder.idle();
				} catch (MessagingException e) {
					Log.warn("Error: No se pudo iniciar la conexion IDLE.");
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			// Cierra el hilo que mantenia la conexion activa.
			if (t.isAlive()) {
				t.interrupt();
			}
		} catch (MessagingException e1) {
			Log.warn("Error: No se pudo iniciar la escucha.");
			e1.printStackTrace();
		}
	}
}
