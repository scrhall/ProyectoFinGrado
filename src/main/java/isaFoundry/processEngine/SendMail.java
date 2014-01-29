package isaFoundry.processEngine;

import java.util.List;

import isaFoundry.core.Core;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

public class SendMail implements ExecutionListener {

	public void notify(DelegateExecution execution) throws Exception {
		List<String> tos = (List<String>) execution.getVariable("tos");
		String subject = (String) execution.getVariable("subject");
		String body = (String) execution.getVariable("body");
		Core.sendEmail(subject, body, tos);
		
	}
}