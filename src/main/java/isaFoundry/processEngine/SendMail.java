package isaFoundry.processEngine;

import java.util.List;

import isaFoundry.core.Core;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendMail implements ExecutionListener {

	private static Logger			Log	= LoggerFactory.getLogger(ProccesEngine.class);
	public void notify(DelegateExecution execution) throws Exception {
		List<String> tos = (List<String>) execution.getVariable("tos");
		Log.info("Variable tos ="+tos);
		String subject = (String) execution.getVariable("subject");
		Log.info("Variable subject ="+subject);
		String body = (String) execution.getVariable("body");
		Log.info("Variable body ="+body);
		Core.sendEmail(subject, body, tos);
		
	}
}