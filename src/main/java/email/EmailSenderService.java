package email;

import java.io.FileInputStream;
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
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
class GMailAuthenticator extends Authenticator {
    String user;
    String pw;
    public GMailAuthenticator (String username, String password)
    {
       super();
       this.user = username;
       this.pw = password;
    }
   public PasswordAuthentication getPasswordAuthentication()
   {
      return new PasswordAuthentication(user, pw);
   }
}
public class EmailSenderService {
	private final Properties properties = new Properties();

	private Session session;

	private void init() {

		try {
			properties.load(new FileInputStream("mail.properties"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: "+e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: "+e);
			e.printStackTrace();
		}
		this.session = Session.getInstance(this.properties, new GMailAuthenticator((String) this.properties.get("mail.smtp.user"),(String) this.properties.get("mail.smtp.password")));
	}
	public void sendEmail(String subject,String body,List<String> tos) {
		sendEmail(subject,body,tos,"","");
		}
	public void sendEmail(String subject,String body,List<String> tos,String attachedPatch,String attachedName) {

		this.init();
		try {
			MimeMultipart multipart = new MimeMultipart();
			//Texto del Email
			BodyPart text = new MimeBodyPart();
			text.setText(body);			
			multipart.addBodyPart(text);
			if(attachedPatch!=""){
				//Adjunto
				BodyPart attached = new MimeBodyPart();
				attached.setDataHandler(new DataHandler(new FileDataSource(attachedPatch)));
				attached.setFileName(attachedName);


				multipart.addBodyPart(attached);
			}
			//Mensaje
			MimeMessage message = new MimeMessage(this.session);
			//Remitente
			message.setFrom(new InternetAddress((String) this.properties.get("mail.smtp.mail.sender")));
			// Insertamos los destinatarios en el correo
			for (String to : tos) {
				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(to));
			}
			// Indicamos el titulo del mensage
			message.setSubject(subject);
			// Añadimos el Cuerpo
			message.setContent(multipart);
			System.out.println((String) this.properties.get("mail.smtp.user")+"/"+(String) this.properties.get("mail.smtp.password"));

			
			
			// Cuerpo del mensage
			
			Transport t = this.session.getTransport("smtp");
			t.connect((String) this.properties.get("mail.smtp.user"),(String) this.properties.get("mail.smtp.password"));
			t.sendMessage(message, message.getAllRecipients());
			t.close();
		} catch (MessagingException me) {
			//TODO		
			System.out.println(me);
			// Aqui se deberia o mostrar un mensaje de error o en lugar
			// de no hacer nada con la excepcion, lanzarla para que el modulo
			// superior la capture y avise al usuario con un popup, por ejemplo.
			return;
		}

	}

}
