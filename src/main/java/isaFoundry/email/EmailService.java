package isaFoundry.email;


import isaFoundry.processEngine.UserTaskRequest;
import isaFoundry.processEngine.UserTaskRequest.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


class Email {

	public String	From;
	public String	Subject;
	public String	Body;
}


public class EmailService {

	private EmailReadService	eReadService;
	private EmailSenderService	eSenderService;

	public EmailService() {
		this.eReadService = new EmailReadService();
		this.eSenderService = new EmailSenderService();
	}

	public List<UserTaskRequest> IdTaskReceived() {
		List<UserTaskRequest> ids = new ArrayList<UserTaskRequest>();
		this.eReadService.connect();
		if (this.eReadService.getUnreadMessageCount() != 0) {
			List<Email> emails = this.eReadService.readEmails();
			for (Email email : emails) {
				UserTaskRequest uTaskRequest = new UserTaskRequest();
				Document doc = Jsoup.parse(email.Body);
				uTaskRequest.action = Action.valueOf(doc.getElementById("action").val());
				uTaskRequest.idTask = Integer.valueOf(doc.getElementById("idTask").val());
				uTaskRequest.options = new HashMap<String, String>();
				for (Element option : doc.getElementsByClass("options")) {
					uTaskRequest.options.put(option.id() , option.val());
				}
				ids.add(uTaskRequest);
			}
		}
		return ids;
	}

	public void SendEmail() {
		// TODO: Eliminar pruebas
		this.eSenderService.sendEmail("Prueba" , "<input type='hidden' id='idTask' value='3'/>" + "<input type='hidden' id='action' value='DONE'/>"
				+ "<input type='hidden' class='options' id='User' value='Scrhall@scrhall.com'/>"
				+ "<input type='hidden' class='options' id='Response' value='No'/>" , Arrays.asList("pruebas@scrhall.com"));
	}
};
