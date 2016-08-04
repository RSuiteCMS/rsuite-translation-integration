package com.rsicms.rsuite.translation.events;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.MetaDataItem;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.event.Event;
import com.reallysi.rsuite.api.event.EventHandler;
import com.reallysi.rsuite.api.event.EventTypes;
import com.reallysi.rsuite.api.event.events.ObjectCheckedInEventData;
import com.reallysi.rsuite.api.extensions.ExecutionContext;

import com.rsicms.rsuite.translation.TranslationConstants;
import com.rsicms.rsuite.translation.utils.TranslationSet;

public class TranslationEventListener implements EventHandler {

	private static final Log log = LogFactory.getLog(TranslationEventListener.class);

	@Override
	public void handleEvent(ExecutionContext context, Event event, Object handback) throws RSuiteException {

		if (event.getType().equals(EventTypes.OBJECT_CHECKEDIN)) {

			ObjectCheckedInEventData eventData = (ObjectCheckedInEventData) event.getUserData();
			User user = eventData.getUser();
			ManagedObject mo = eventData.getManagedObject();

			log.info("Checking whether this event qualifies as translation source update of: " + mo.getDisplayName());
			if (mo.getLayeredMetadataValue(TranslationConstants.LMD_IS_BASE_LANGUAGE) == null || 
					!mo.getLayeredMetadataValue(TranslationConstants.LMD_IS_BASE_LANGUAGE).equals("true"))
				return;
			log.info("   ... it does.");
			//TODO should also verify the doc has actually changed as opposed to some other reason for check in
			
			TranslationSet ts = new TranslationSet(context, user, mo.getId());
			List<ManagedObject> translationMos = ts.getTranslationsMos();
			
			if (translationMos != null) {
				for (ManagedObject translationMo : translationMos) {
					List<MetaDataItem> mdis = new ArrayList<MetaDataItem>();
					MetaDataItem newMdi = null;
					String transState = translationMo.getLayeredMetadataValue(TranslationConstants.LMD_TRANSLATION_STATE);
					if (transState == null)
						transState = TranslationConstants.STATUS_NOT_TRANSLATED_VALUE;
					log.info("   Translation status for " + translationMo.getDisplayName() + " [" + translationMo.getId() + "] is " + transState);
					
					if (transState.equals(TranslationConstants.STATUS_TRANSLATION_IN_PROGRESS_VALUE)) {
						newMdi = new MetaDataItem(TranslationConstants.LMD_TRANSLATION_STATE, TranslationConstants.STATUS_TRANSLATION_REQUEST_STALE_VALUE);
						log.info("   Translation status should be " + newMdi.getValue());
					} else if (transState.equals(TranslationConstants.STATUS_NOT_TRANSLATED_VALUE)) {
						log.info("   Translation status OK as is");
					} else {
						newMdi = new MetaDataItem(TranslationConstants.LMD_TRANSLATION_STATE, TranslationConstants.STATUS_TRANSLATION_STALE_VALUE);
						log.info("   Translation status should be " + newMdi.getValue());
					}
					
					if (newMdi != null) {
						log.info("   Updating translation status.");
						mdis.add(newMdi);
						try {
							context.getManagedObjectService().setMetaDataEntries(user, translationMo.getId(), mdis);
						} catch (Exception e) {
							log.error(" Unable to update translation status metadata for " + translationMo.getDisplayName() + "/" + translationMo.getId() + ": " + e.getMessage() + e);
						}
					}
				}
			}
		}
	}

}
