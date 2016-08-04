package com.rsicms.rsuite.translation.advisor;

import java.util.List;

import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.content.ContentAdvisorContext;
import com.reallysi.rsuite.api.content.ContentDisplayAdvisor;
import com.reallysi.rsuite.api.content.ContentDisplayObject;
import com.rsicms.rsuite.helpers.utils.RSuiteUtils;

import com.rsicms.rsuite.translation.TranslationConstants;
import com.rsicms.rsuite.translation.utils.TranslationSetUtils;

public class TranslationContentAdvisor implements ContentDisplayAdvisor {

    @Override
    public void adjustContentItem(ContentAdvisorContext context, ContentDisplayObject item) throws RSuiteException {

        User user = context.getUser();

        if (item == null)
        	return;
        
        try {
    		item.getManagedObject();
        } catch (RSuiteException e) {
        	return;
        }
        
        ManagedObject mo = item.getManagedObject();
        mo = RSuiteUtils.getRealMo(context, user, mo);

        String isBaseLanguage = mo.getLayeredMetadataValue(TranslationConstants.LMD_IS_BASE_LANGUAGE);
        String translationOfRevision = mo.getLayeredMetadataValue(TranslationConstants.LMD_TRANSLATION_OF_REVISION);
        String ancTranslationOf = RSuiteUtils.getLmdFromAncestorOrSelf(context, user, mo, TranslationConstants.LMD_TRANSLATION_OF);
        String lang = RSuiteUtils.getLmdFromAncestorOrSelf(context, user, mo, TranslationConstants.LMD_LANGUAGE);

        String transMessage = "";
        if (isBaseLanguage != null  && isBaseLanguage.equals("true")) {
        	transMessage = "";
        }
        String translationState = "";
        if (ancTranslationOf != null ) {
            translationState = TranslationSetUtils.getTranslationStateHtml(context, user, mo); 
            ManagedObject sourceMo = context.getManagedObjectService().getManagedObject(user, ancTranslationOf);
        	transMessage = "<span title=\"Translation: " + sourceMo.getDisplayName() + " [" + ancTranslationOf + "v"+ translationOfRevision + "]\">" +
        			ancTranslationOf + "</span>";
        }

        if (lang == null) lang = "";
        item.addCustomValue("language", lang);
        item.addCustomValue("translationOf", transMessage);
        item.addCustomValue("translationStatus", translationState);
    }

    @Override
    public void adjustNodeCollectionList(ContentAdvisorContext context, List<ContentDisplayObject> collection) throws RSuiteException {
    }

}
