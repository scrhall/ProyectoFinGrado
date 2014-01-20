package isaFoundry;


import java.util.List;

import isaFoundry.core.Core;
import isaFoundry.email.EmailService;
import isaFoundry.processEngine.UserTaskRequest;

import org.slf4j.LoggerFactory;


/**
 * Hello world!
 * 
 */
public class App {

	public static void main(String[] args) {
		LoggerFactory.getLogger(App.class);
		//Core core = new Core();
		//core.run();
		EmailService es=new EmailService();
		//es.SendEmail();
		List<UserTaskRequest> hola= es.IdTaskReceived();
	}
}
