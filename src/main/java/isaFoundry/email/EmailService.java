package isaFoundry.email;


import isaFoundry.processEngine.UserTaskRequest;
import isaFoundry.processEngine.UserTaskRequest.Action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailService {

	private Logger				Log	= LoggerFactory.getLogger(EmailService.class);
	private EmailReadService	eReadService;
	private EmailSenderService	eSenderService;

	public EmailService() {
		this.eReadService = new EmailReadService(this);
		this.eSenderService = new EmailSenderService();
	}

	public String html2text(String html) {
		return Jsoup.parse(html).text();
	}

	public void reply(Message msg, String string) {
		this.eSenderService.reply(msg , string);
	}

	public void sendEmail(String subject, String body, List<String> tos) {
		this.eSenderService.sendEmail(subject , body , tos);
	}

	public List<UserTaskRequest> taskReceived() {
		 boolean	error=false;
		List<UserTaskRequest> ids = new ArrayList<UserTaskRequest>();
		Message[] emails = this.eReadService.readEmails();
		this.Log.info("Emails Leidos");
		for (Message email : emails) {
			String body;
			try {
				body = this.eReadService.getText(email).replaceAll("<br" , "{NewLine}<br").replaceAll("</div>" , "</div>{NewLine}");
				body = this.html2text(body);
				String[] splitbody = body.split("<\\-\\-|\\-\\->");
				if (splitbody.length > 1) {
					UserTaskRequest uTaskRequest = new UserTaskRequest();
					uTaskRequest.msg = email;
					this.Log.info(email.getSubject());
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
									uTaskRequest.options.put(option[0].trim() , option[1].trim());
								}
							}
						}
						ids.add(uTaskRequest);
					}else {error=true;}
				} else {error=true;}
				if(error){
					UserTaskRequest uTaskRequest = new UserTaskRequest();
					uTaskRequest.msg = email;
					uTaskRequest.action = Action.ERROR;
					uTaskRequest.hash = "";
					uTaskRequest.options = new HashMap<String, Object>();
					uTaskRequest.options.put("From" , email.getFrom()[0].toString());
					ids.add(uTaskRequest);
				}
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ids;
	}

	public void connect() {
		this.eReadService.connect();
	}
}
