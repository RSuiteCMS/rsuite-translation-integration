package com.rsicms.rsuite.translation.webservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.Basket;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.remoteapi.CallArgument;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;
import com.reallysi.rsuite.api.remoteapi.DefaultRemoteApiHandler;
import com.reallysi.rsuite.api.remoteapi.RemoteApiExecutionContext;
import com.reallysi.rsuite.api.remoteapi.RemoteApiResult;
import com.reallysi.rsuite.api.remoteapi.result.MessageDialogResult;
import com.reallysi.rsuite.api.remoteapi.result.MessageType;
import com.reallysi.rsuite.api.remoteapi.result.NotificationResult;

import com.rsicms.rsuite.translation.utils.TranslationSet;

public class AddTranslationsToBriefcase extends DefaultRemoteApiHandler {

	private static Log log = LogFactory.getLog(AddTranslationsToBriefcase.class);

	@Override
	public RemoteApiResult execute(RemoteApiExecutionContext context, CallArgumentList args) {

		for (CallArgument arg : args.getAll()) {
			log.info("Arg: " + arg.getName() + ":" + arg.getValue());
		}
		
		String rsuiteId = args.getFirstString("rsuiteId");
		User user = context.getSession().getUser();
		TranslationSet ts;
		try {
			ts = new TranslationSet(context, user, rsuiteId);
		} catch (RSuiteException e) {
			log.error("Error getting set of related translation MOs."  + e.getMessage() + e);
			return new MessageDialogResult(MessageType.ERROR, "Translation Error", "Couldn't get set of related translation files. See your system administrator.");
		}

		Basket briefcase;
		try {
			briefcase = context.getBasketService().getOrCreateClipboard(user);
			for (String bid : ts.getTranslationSetIds()) {
				context.getBasketService().addObjectToBasket(user, briefcase.getId(), bid);
			}
		} catch (RSuiteException e) {
			log.error("Error during move to briefcase - problem adding: " + e.getMessage() + e);
			return new MessageDialogResult(MessageType.ERROR, "Briefcase Error",
					"Error while adding items to the Briefcase. If this persists, see your system administrator.");
		}
		String ancestorMessage = "";
		if (ts.wasDescendant())
			try {
				ancestorMessage = "<br/>Note: Because your selected item was part of a larger XML document, translations of the ancestor \"" + 
						ts.getBaseMo().getDisplayName() + "\" were added.";
			} catch (RSuiteException e) {
				log.error("Error during move to briefcase - problem getting displayname for MO: " + e.getMessage() + e);
				return new MessageDialogResult(MessageType.ERROR, "Briefcase Error",
						"Error while adding items to the Briefcase. If this persists, see your system administrator.");
			}

		return new NotificationResult("This item and its translations have been added to the Briefcase." + ancestorMessage);
	}

}
