package isaFoundry.email;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

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

	private Logger				Log			= LoggerFactory.getLogger(EmailSenderService.class);
	private final Properties	properties	= new Properties();
	private Session				session;

	public EmailSenderService() {
		try {
			this.properties.load(this.getClass().getResourceAsStream("/config/emailSender.properties"));
			this.session = Session.getInstance(this.properties , new GMailAuthenticator((String) this.properties.get("mail.smtp.user") ,
					(String) this.properties.get("mail.smtp.password")));
		} catch (FileNotFoundException e) {
			this.Log.error("Error: Archivo no encontrado | emailRead.properties" + e);
		} catch (IOException e) {
			this.Log.info("Error: Entrada/Salida | emailRead.properties " + e);
		}
	}

	public void reply(Message msg, String string) {
		try {
			this.Log.info("Preparando Respuesta");
			Message replyMessage = new MimeMessage(this.session);
			replyMessage = msg.reply(false);
			replyMessage.setFrom(new InternetAddress((String) this.properties.get("mail.smtp.mail.sender")));
			replyMessage.setText(string);
			replyMessage.setReplyTo(msg.getReplyTo());
			this.connectAndSend(replyMessage);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendEmail(String subject, String body, List<String> tos) {
		this.sendEmail(subject , body , tos , "" , "");
	}

	public void sendEmail(String subject, String body, List<String> tos, String attachedPatch, String attachedName) {
		try {
			MimeMultipart multipart = new MimeMultipart();
			// Texto del Email
			BodyPart text = new MimeBodyPart();
			text.setContent(body , "text/html");
			multipart.addBodyPart(text);
			if (attachedPatch != "") {
				// Adjunto
				BodyPart attached = new MimeBodyPart();
				attached.setDataHandler(new DataHandler(new FileDataSource(attachedPatch)));
				attached.setFileName(attachedName);
				multipart.addBodyPart(attached);
			}
			// Mensaje
			MimeMessage message = new MimeMessage(this.session);
			// Remitente
			message.setFrom(new InternetAddress((String) this.properties.get("mail.smtp.mail.sender")));
			// Insertamos los destinatarios en el correo
			for (String to : tos) {
				message.addRecipient(Message.RecipientType.BCC , new InternetAddress(to));
				this.Log.info("Destinatario: " + to);
			}
			// Indicamos el titulo del mensage
			message.setSubject(subject);
			this.Log.info("Titulo del mensaje: " + subject);
			// A�adimos el Cuerpo
			message.setContent(multipart);
			this.connectAndSend(message);
		} catch (MessagingException me) {
			// TODO
			System.out.println(me);
			// Aqui se deberia o mostrar un mensaje de error o en lugar
			// de no hacer nada con la excepcion, lanzarla para que el modulo
			// superior la capture y avise al usuario con un popup, por ejemplo.
			this.Log.info("Error al enviar el mensaje");
			return;
		}
	}

	private void connectAndSend(Message message) {
		try {
			Transport t = this.session.getTransport("smtp");
			t.connect((String) this.properties.get("mail.smtp.user") , (String) this.properties.get("mail.smtp.password"));
			t.sendMessage(message , message.getAllRecipients());
			t.close();
			this.Log.info("Email enviado con exito");
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


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
