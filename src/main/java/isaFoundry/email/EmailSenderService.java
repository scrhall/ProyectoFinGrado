package isaFoundry.email;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
<<<<<<< HEAD
=======

>>>>>>> branch 'master' of https://github.com/scrhall/ProyectoFinGrado.git
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailSenderService {

	private static Logger		Log			= LoggerFactory.getLogger(EmailSenderService.class);
	private final Properties	properties	= new Properties();
	private Session				session;

	/**
	 * Constructor de la clase, inicializa los paramentros necesarios.
	 */
	public EmailSenderService() {
		try {
			Log.info("Incializando el  servicio de envio de correos electronicos.");
			EmailSenderService.Log.info("Leyendo configuracion.");
			this.properties.load(this.getClass().getResourceAsStream("/config/emailSender.properties"));
			this.session = Session.getInstance(this.properties , new GMailAuthenticator((String) this.properties.get("mail.smtp.user") ,
					(String) this.properties.get("mail.smtp.password")));
		} catch (FileNotFoundException e) {
			Log.error("Error: Archivo no encontrado | emailRead.properties");
			e.printStackTrace();
		} catch (IOException e) {
			Log.error("Error: Entrada/Salida | emailRead.properties ");
			e.printStackTrace();
		}
	}

	/**
	 * Crea un mensaje de respuesta y lo manda.
	 * 
	 * @param msg
	 *            Mensaje que al que se debe responder.
	 * @param string
	 *            Texto de la respuesta.
	 */
	public void reply(Message msg, String string) {
		try {
			Log.info("Preparando Respuesta");
			Message replyMessage = new MimeMessage(this.session);
			replyMessage = msg.reply(false);
			replyMessage.setFrom(new InternetAddress((String) this.properties.get("mail.smtp.mail.sender")));
			replyMessage.setText(string);
			replyMessage.setReplyTo(msg.getReplyTo());
			this.connectAndSend(replyMessage);
		} catch (MessagingException e) {
			Log.error("Error: No se pudo crear un mensaje de respuesta");
			e.printStackTrace();
		}
	}

	/**
	 * Envia un correo electronico.
	 * 
	 * @param subject
	 *            Asunto
	 * @param body
	 *            Texto
	 * @param tos
	 *            Destinatarios
	 */
	public void sendEmail(String subject, String body, List<String> tos) {
		this.sendEmail(subject , body , tos , "" , "");
	}

	/**
	 * Envia un correo electronico con adjunto.
	 * 
	 * @param subject
	 *            Asunto
	 * @param body
	 *            Texto
	 * @param tos
	 *            Destinatario
	 * @param attachedPatch
	 *            Ruta del archivo adajunto.
	 * @param attachedName
	 *            Nombre del archivo adjunto.
	 */
	public void sendEmail(String subject, String body, List<String> tos, String attachedPatch, String attachedName) {
		try {
			Log.info("Enviando correo electronico...");
			MimeMultipart multipart = new MimeMultipart();
			BodyPart text = new MimeBodyPart();
			text.setContent(body , "text/html");
			multipart.addBodyPart(text);
			if (attachedPatch != "") {
				BodyPart attached = new MimeBodyPart();
				attached.setDataHandler(new DataHandler(new FileDataSource(attachedPatch)));
				attached.setFileName(attachedName);
				multipart.addBodyPart(attached);
			}
			MimeMessage message = new MimeMessage(this.session);
			message.setFrom(new InternetAddress((String) this.properties.get("mail.smtp.mail.sender")));
			for (String to : tos) {
				to=to.replace(String.valueOf((char) 160), " ").trim();
				message.addRecipient(Message.RecipientType.BCC , new InternetAddress(to));
				Log.info("Destinatario: " + to);
			}
			message.setSubject(subject);
			Log.info("Titulo del mensaje: " + subject);
			message.setContent(multipart);
			this.connectAndSend(message);
		} catch (MessagingException e) {
			Log.error("Error: No se pudo enviar el mensaje");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Conecta con el servidor de correo para enviar un mensaje.
	 * 
	 * @param message
	 *            Correo electronco a enviar.
	 */
	private void connectAndSend(Message message) {
		try {
			Log.info("Conectando con el servidor de correo...");
			Transport t = this.session.getTransport("smtp");
			t.connect((String) this.properties.get("mail.smtp.user") , (String) this.properties.get("mail.smtp.password"));
			t.sendMessage(message , message.getAllRecipients());
			t.close();
			Log.info("Correo electronico enviado con exito.");
		} catch (NoSuchProviderException e) {
			Log.error("Error: No se pudo enviar el mensaje");
			e.printStackTrace();
		} catch (MessagingException e) {
			Log.error("Error: No se pudo enviar el mensaje");
			e.printStackTrace();
		}
	}
}


/**
 * Clase usada para la autentificacion con servidores de GMAIL.
 * 
 */
class GMailAuthenticator extends Authenticator {

	String	user;
	String	pw;

	public GMailAuthenticator(String username, String password) {
		super();
		this.user = username;
		this.pw = password;
	}

	@Override
	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.user , this.pw);
	}
}
