package isaFoundry.email;


import isaFoundry.core.UserTaskRequest;
import isaFoundry.core.UserTaskRequest.Action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailService {

	private static Logger		Log	= LoggerFactory.getLogger(EmailService.class);
	private EmailReadService	eReadService;
	private EmailSenderService	eSenderService;

	/**
	 * Constructor de la clase, inicializa los paramentros necesarios.
	 */
	public EmailService() {
		Log.info("Incializando el  servicio de correos electronicos.");
		this.eReadService = new EmailReadService(this);
		this.eSenderService = new EmailSenderService();
	}

	/**
	 * Conecta el servicio de notificaciones de correos electronicos.
	 */
	public void connect() {
		this.eReadService.connect();
	}

	/**
	 * Extrae de un texto en html el texto sin las etiquetas html.
	 * 
	 * @param html
	 * @return
	 */
	public String html2text(String html) {
		return Jsoup.parse(html).text();
	}

	/**
	 * Responde a un correo electronico.
	 * 
	 * @param msg
	 * @param string
	 */
	public void reply(Message msg, String string) {
		this.eSenderService.reply(msg , string);
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
		this.eSenderService.sendEmail(subject , body , tos);
	}

	/**
	 * Lee los correos electronicos sin leer, los analiza y crea una lista de
	 * tareas para realizar.
	 * 
	 * @return Lista de tareas a realizar.
	 */
	public List<UserTaskRequest> taskReceived() {
		Log.info("Comprobando si existen tareas pendientes en el correo electronico.");
		boolean error = false;
		List<UserTaskRequest> ids = new ArrayList<UserTaskRequest>();
		Message[] emails = this.eReadService.readEmails();
		Log.info("Iniciando analisis de los correos electronicos.");
		for (Message email : emails) {
			String body;
			try {
				body = this.eReadService.getText(email).replaceAll("<br" , "{NewLine}<br").replaceAll("</div>" , "</div>{NewLine}");
				body = this.html2text(body);
				String[] splitbody = body.split("<\\-\\-|\\-\\->");
				if (splitbody.length > 1) {
					UserTaskRequest uTaskRequest = new UserTaskRequest();
					uTaskRequest.msg = email;
					EmailService.Log.info(email.getSubject());
					String[] hashAction = splitbody[splitbody.length - 2].split(":");
					if (hashAction.length == 2) {
						uTaskRequest.msg = email;
						uTaskRequest.action = Action.valueOf(hashAction[0]);
						uTaskRequest.hash = hashAction[1];
						uTaskRequest.options = new HashMap<String, Object>();
						uTaskRequest.options.put("From" , email.getFrom()[0].toString());
						if (splitbody.length > 1) {
							String moreoptions = splitbody[1];
							String[] options = moreoptions.split("\\{NewLine\\}");
							for (String element : options) {
								String[] option = element.split(":");
								if (option.length == 2) {
									//uTaskRequest.options.put(option[0].trim() , option[1].trim());
									//String[] aux = option[1].trim().split(",");
									List<String> aux =Arrays.asList(option[1].trim().split(","));
									if (aux.size() > 1) {
								 	 uTaskRequest.options.put(option[0].trim() , aux);
								 	} else {
								 	 uTaskRequest.options.put(option[0].trim() , option[1].trim());
								 	 }
								}
							}
						}
						ids.add(uTaskRequest);
					} else {
						error = true;
					}
				} else {
					error = true;
				}
				if (error) {
					UserTaskRequest uTaskRequest = new UserTaskRequest();
					uTaskRequest.msg = email;
					uTaskRequest.action = Action.ERROR;
					uTaskRequest.hash = "";
					uTaskRequest.options = new HashMap<String, Object>();
					uTaskRequest.options.put("From" , email.getFrom()[0].toString());
					ids.add(uTaskRequest);
				}
			} catch (MessagingException e) {
				Log.error("Error: No se pudieron analizar los correos electronicos.");
				e.printStackTrace();
			} catch (IOException e) {
				Log.error("Error: No se pudieron analizar los correos electronicos.");
				e.printStackTrace();
			}
		}
		return ids;
	}
}
