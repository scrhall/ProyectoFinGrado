package isaFoundry.email;


import isaFoundry.processEngine.UserTaskRequest;
import isaFoundry.processEngine.UserTaskRequest.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class Email {

	public String	From;
	public String	Subject;
	public String	Body;
}


public class EmailService {
	private Logger				Log			= LoggerFactory.getLogger(EmailService.class);
	private EmailReadService	eReadService;
	private EmailSenderService	eSenderService;

	public EmailService() {
		this.eReadService = new EmailReadService();
		this.eSenderService = new EmailSenderService();
	}

	public void SendEmail(String subject, String body, List<String> tos) {
		this.eSenderService.sendEmail(subject , body , tos);
	}

	public List<UserTaskRequest> taskReceived() {
		List<UserTaskRequest> ids = new ArrayList<UserTaskRequest>();
		this.eReadService.connect();
		Log.info("Mensajes no leidos: "+this.eReadService.getUnreadMessageCount());
		if (this.eReadService.getUnreadMessageCount() != 0) {
			List<Email> emails = this.eReadService.readEmails();
			Log.info(String.valueOf(emails.size()));

			for (Email email : emails) {
				UserTaskRequest uTaskRequest = new UserTaskRequest();
				String[] aux = email.Subject.split("\\-=\\[");
				
				Log.info(email.Subject);
				if (aux.length>1)
				{
					String head = aux[1];
					Log.info(head);
					String[] data = head.split("\\|");
					uTaskRequest.action = Action.valueOf(data[2]);
					uTaskRequest.idTask = data[1];
					uTaskRequest.idProcces = Integer.valueOf(data[0]);
					uTaskRequest.options = new HashMap<String, String>();
					uTaskRequest.options.put("From", email.From);
					aux = email.Body.split("<\\-\\-|\\-\\->");
					if (aux.length > 1) {
						String moreoptions = aux[1];
						aux = moreoptions.split("\r\n");
						for (String element : aux) {
							String[] option = element.split(":");
							if(option.length==2)
								uTaskRequest.options.put(option[0] , option[1]);
						}
				}
				ids.add(uTaskRequest);
				}
			}
		}
		return ids;
	}
};
