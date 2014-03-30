package isaFoundry.processEngine;


import isaFoundry.core.Core;

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SendMail implements ExecutionListener {

	private static Logger	Log	= LoggerFactory.getLogger(ProccesEngine.class);

	public void notify(DelegateExecution execution) throws Exception {
		List<String> tos = (List<String>) execution.getVariable("tos");
		//List<String> tos =(List<String>)Arrays.asList((String[])execution.getVariable("tos"));
		String emailAction = (String) execution.getVariable("emailAction");
		Log.info("Variable tos =" + tos);
		String subject = (String) execution.getVariable("subject");
		Log.info("Variable subject =" + subject);
		String body = (String) execution.getVariable("body");
		body += "<br/><--"+emailAction+":" + ProccesEngine.calculeHash(execution.getCurrentActivityId() , execution.getProcessInstanceId()) + "-->";
		Log.info("Variable body =" + body);
		Core.sendEmail(subject , body , tos);
	}
}