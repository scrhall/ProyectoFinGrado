package isaFoundry.email;


import isaFoundry.processEngine.UserTaskRequest;
import isaFoundry.processEngine.UserTaskRequest.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailService {

	private Logger				Log	= LoggerFactory.getLogger(EmailService.class);
	private EmailReadService	eReadService;
	private EmailSenderService	eSenderService;

	public EmailService() {
		this.eReadService = new EmailReadService();
		this.eSenderService = new EmailSenderService();
	}

	public void SendEmail(String subject, String body, List<String> tos) {
		this.eSenderService.sendEmail(subject , body , tos);
	}
	public String html2text(String html) {
	    return Jsoup.parse(html).text();
	}
	public List<UserTaskRequest> taskReceived() {
		List<UserTaskRequest> ids = new ArrayList<UserTaskRequest>();
		this.eReadService.connect();
		this.Log.info("Mensajes no leidos: " + this.eReadService.getUnreadMessageCount());
		if (this.eReadService.getUnreadMessageCount() != 0) {
			List<Email> emails = this.eReadService.readEmails();
			this.Log.info(String.valueOf(emails.size()));
			for (Email email : emails) {
				UserTaskRequest uTaskRequest = new UserTaskRequest();
				String[] aux = email.Subject.split("\\-=\\[");
				this.Log.info(email.Subject);
				if (aux.length > 1) {
					String head = aux[1];
					this.Log.info(head);
					String[] data = head.split("\\|");
					uTaskRequest.action = Action.valueOf(data[2]);
					uTaskRequest.idTask = data[1];
					uTaskRequest.idProcces = data[0];
					uTaskRequest.options = new HashMap<String, Object>();
					uTaskRequest.options.put("From" , email.From);
					String body =email.Body.replaceAll("<br" ,"{NewLine}<br");
					body=html2text(body);
					aux = body.split("<\\-\\-|\\-\\->");
					if (aux.length > 1) {
						String moreoptions = aux[1];
						aux = moreoptions.split("\\{NewLine\\}");
						for (String element : aux) {
							String[] option = element.split(":");
							if (option.length == 2) {
								uTaskRequest.options.put(option[0].trim() , option[1].trim());
							}
						}
					}
					ids.add(uTaskRequest);
				}
			}
		}
		return ids;
	}
}


class Email {

	public String	From;
	public String	Subject;
	public String	Body;
};
