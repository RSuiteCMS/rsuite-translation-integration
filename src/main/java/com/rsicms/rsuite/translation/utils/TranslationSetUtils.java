package com.rsicms.rsuite.translation.utils;

import java.util.HashMap;
import java.util.Map;

import com.reallysi.rsuite.api.DataTypeOptionValue;
import com.reallysi.rsuite.api.DataTypeOptionValuesProvider;
import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.rsicms.rsuite.helpers.utils.RSuiteUtils;

import com.rsicms.rsuite.translation.TranslationConstants;

public class TranslationSetUtils {

    private static String imagePath = "";

    public static String getTranslationStateHtml(ExecutionContext context, User user, ManagedObject mo) throws RSuiteException {
        	
        imagePath = "http://" + context.getRSuiteServerConfiguration().getHostName() + ":" + context.getRSuiteServerConfiguration().getPort() + 
        		"/rsuite-cms/plugin/rsuite-translation-integration/images/";

        String translationState = RSuiteUtils.getLmdFromAncestorOrSelf(context, user, mo, TranslationConstants.LMD_TRANSLATION_STATE);
        
        if (translationState == null || translationState.isEmpty() || translationState.equals(TranslationConstants.STATUS_NOT_TRANSLATED_VALUE)) {
        	translationState = buildLabel(
        			"translation-none.png", 
        			TranslationConstants.STATUS_NOT_TRANSLATED_LABEL
        			);
        } else if (translationState.equals(TranslationConstants.STATUS_TRANSLATION_STALE_VALUE)) {
        	translationState = buildLabel(
        			"translation-stale.png", 
        			TranslationConstants.STATUS_TRANSLATION_STALE_LABEL
        			);
        } else if (translationState.equals(TranslationConstants.STATUS_TRANSLATION_REQUEST_STALE_VALUE)) {
        	translationState = buildLabel(
        			"translation-inprogress-stale.png", 
        			TranslationConstants.STATUS_TRANSLATION_REQUEST_STALE_LABEL
        			);
        } else if (translationState.equals(TranslationConstants.STATUS_TRANSLATION_IN_PROGRESS_VALUE)) {
        	translationState = buildLabel(
        			"translation-inprogress.png", 
        			TranslationConstants.STATUS_TRANSLATION_IN_PROGRESS_LABEL
        			);
        } else {
        	translationState = buildLabel(
        			"translation-ok.png", 
        			TranslationConstants.STATUS_UP_TO_DATE_LABEL
        			);
        }
        return translationState;
	}

	private static String buildLabel(String image, String label) {
		return "<img src=\"" + imagePath + image + "\"/ title=\"" + label + "\" style=\"width:24px; vertical-align: middle;\">";
	}

	public static Map<String, String> getLanguageDataTypeAsMap(ExecutionContext context, User user)
			throws RSuiteException {
		Map<String, String> langMap = new HashMap<String, String>();
		DataTypeOptionValuesProvider langDt = context.getDomainManager().getCurrentDomainContext().getDataTypeManager()
				.getDataType(user, "languageDt").getOptionValuesProvider();
		for (DataTypeOptionValue langDtVal : langDt.getOptionValues()) {
			langMap.put(langDtVal.getValue(), langDtVal.getLabel());
		}
		return langMap;
	}

}
