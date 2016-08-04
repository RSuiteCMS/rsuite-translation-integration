package com.rsicms.rsuite.translation.job;

import java.util.Map;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.scheduler.DefaultScheduledJobHandler;
import com.reallysi.rsuite.api.scheduler.ScheduledJobExecutionContext;
import com.rsicms.rsuite.translation.utils.RetrieveXtmTranslations;

public class RetrieveXtmTranslationsScheduledJob extends DefaultScheduledJobHandler {

	@Override
	public void execute(ScheduledJobExecutionContext context) throws RSuiteException  {

		User user = context.getAuthorizationService().getSystemUser();
		Map<String, String> map = context.getSchedulerService().getJobParameters(user, context.getJobName());
		String client = map.get("client");

		RetrieveXtmTranslations.retrieveTranslations(context, user, client);
		
	}

}
