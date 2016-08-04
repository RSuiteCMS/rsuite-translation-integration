package com.rsicms.rsuite.translation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.MetaDataItem;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.VersionType;
import com.reallysi.rsuite.api.control.ObjectCheckInOptions;
import com.reallysi.rsuite.api.control.ObjectUpdateOptions;
import com.reallysi.rsuite.api.control.XmlObjectSource;
import com.reallysi.rsuite.api.extensions.ExecutionContext;

import com.rsicms.rsuite.translation.TranslationConstants;

public class RetrieveXtmTranslations {

	private static Log log = LogFactory.getLog(RetrieveXtmTranslations.class);

	public static void retrieveTranslations (ExecutionContext context, User user, String client) throws RSuiteException {

		Map<String, String> langMap = TranslationSetUtils.getLanguageDataTypeAsMap(context, user);

		List<XTMFile> xtmFiles = XTMUtils.retrieveFilesFromXtmCloud(context, user, client);
		for (XTMFile xtmFile : xtmFiles) {
			String translationMoid = xtmFile.getTranslationRSuiteId();
			File translatedFile = xtmFile.getFile();

			ManagedObject mo = context.getManagedObjectService().getManagedObject(user, translationMoid);
			if (mo == null) {
				log.error("Returned translation file has unknown RSuite ID" + translationMoid + ". Cannot be loaded.");
				continue;
			}
			String sourceMoid = mo.getLayeredMetadataValue(TranslationConstants.LMD_TRANSLATION_OF);
			if (sourceMoid == null) {
				sourceMoid = "";
			}
			InputStream is = null;
			XmlObjectSource moSrc = null;
			try {
				is = new FileInputStream(translatedFile);
				moSrc = new XmlObjectSource(prependDoctypeDeclaration(mo, is));
			} catch (Exception e) {
				log.error("Unable to load translation file. " + e);
				String notification = "Translation load FAILED: " + mo.getDisplayName() + ", " + langMap.get(xtmFile.getTranslationLanguage()) + " translation of " + sourceMoid;
				Map<String, Object> wfVariables = new HashMap<String, Object>();
				wfVariables.put("rsuite:contents", translationMoid + "," + sourceMoid);
				wfVariables.put("moTitle", mo.getDisplayName());
				wfVariables.put("moDisplayName", mo.getDisplayName());
				wfVariables.put("notification", notification);
				context.getWorkflowInstanceService().startWorkflow("TranslationNotificationWorkflow", wfVariables);
				continue;
			}

			if (mo.isCheckedoutButNotByUser(user)) {
				context.getManagedObjectService().undoCheckout(context.getAuthorizationService().getSystemUser(), translationMoid);
			}
			if (!mo.isCheckedout()) {
				context.getManagedObjectService().checkOut(user, translationMoid);
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 

			ObjectUpdateOptions options = new ObjectUpdateOptions();
			List<MetaDataItem> mdis = new ArrayList<MetaDataItem>();
			String status = TranslationConstants.STATUS_UP_TO_DATE_VALUE;
			if (mo.getLayeredMetadataValue(TranslationConstants.LMD_TRANSLATION_STATE) != null && 
					mo.getLayeredMetadataValue(TranslationConstants.LMD_TRANSLATION_STATE).equals(TranslationConstants.STATUS_TRANSLATION_REQUEST_STALE_VALUE)) {
				status = TranslationConstants.STATUS_TRANSLATION_STALE_VALUE; 
			}
			mdis.add(new MetaDataItem(TranslationConstants.LMD_TRANSLATION_STATE, status));
			mdis.add(new MetaDataItem(TranslationConstants.LMD_TRANSLATION_UPDATE_DATE, sdf.format(new Date())));

			log.info("Updating translation to id " + translationMoid + " with status=" + status + " and update date=" + sdf.format(new Date()));
			try {
				context.getManagedObjectService().update(user, translationMoid, moSrc, options);
				ObjectCheckInOptions checkinOptions = new ObjectCheckInOptions();
				checkinOptions.setVersionType(VersionType.MAJOR);
				checkinOptions.setVersionNote("Updated translation from XTM Cloud."); 
				context.getManagedObjectService().setMetaDataEntries(user, translationMoid, mdis);
				context.getManagedObjectService().checkIn(user, translationMoid, checkinOptions);
			} catch (Exception e) {
				log.error("Unable to load translation file. " + e);
				String notification = "Translation load FAILED: " + mo.getDisplayName() + ", " + langMap.get(xtmFile.getTranslationLanguage()) + " translation of " + sourceMoid;
				Map<String, Object> wfVariables = new HashMap<String, Object>();
				wfVariables.put("rsuite:contents", translationMoid + "," + sourceMoid);
				wfVariables.put("moTitle", mo.getDisplayName());
				wfVariables.put("moDisplayName", mo.getDisplayName());
				wfVariables.put("notification", notification);
				context.getWorkflowInstanceService().startWorkflow("TranslationNotificationWorkflow", wfVariables);
				continue;
			}

			String notification = "Translation received: " + mo.getDisplayName() + ", " + langMap.get(xtmFile.getTranslationLanguage()) + " translation of " + sourceMoid;

			Map<String, Object> wfVariables = new HashMap<String, Object>();
			wfVariables.put("rsuite:contents", translationMoid + "," + sourceMoid);
			wfVariables.put("moTitle", mo.getDisplayName());
			wfVariables.put("moDisplayName", mo.getDisplayName());
			wfVariables.put("notification", notification);
			context.getWorkflowInstanceService().startWorkflow("TranslationNotificationWorkflow", wfVariables);

			log.info("Updated " + xtmFile.getTranslationLanguage() + " translation file from XTM: " + translatedFile.getName() + ">>" + translationMoid);
		}

	}


	private static byte[] prependDoctypeDeclaration(ManagedObject mo, InputStream is) throws RSuiteException, IOException {
		String doctype = "<!DOCTYPE " + mo.getElement().getNodeName() + " PUBLIC \"" + mo.getPublicIdProperty() + "\" \"" + mo.getSystemIdProperty() + "\">";
		String docWithDocType = IOUtils.toString(is, "UTF-8");
		docWithDocType = docWithDocType.replaceAll("\\<\\?xml(.)*?>", "");
		docWithDocType = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + doctype + docWithDocType;
		return docWithDocType.getBytes(StandardCharsets.UTF_8);
	}
}
