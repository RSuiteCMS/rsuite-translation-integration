package com.rsicms.rsuite.translation.utils;

import java.util.List;

import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.service.ManagedObjectService;
import com.rsicms.rsuite.helpers.utils.RSuiteUtils;

import com.rsicms.rsuite.translation.TranslationConstants;

public class TranslationSet {

	private String baseMoid = null;
	private String baseLang = null;
	private Boolean addedAncestor = false;
	private ManagedObject baseMo = null;
	private List<ManagedObject> transMos = null;
	private String[] moids = null;

	public TranslationSet(ExecutionContext context, User user, String moid) throws RSuiteException {

		ManagedObjectService mosvc = context.getManagedObjectService();
		ManagedObject mo = null;

		mo = RSuiteUtils.getRealMo(context, user, mosvc.getManagedObject(user, moid));
		String rootId = mosvc.getRootManagedObjectId(user, mo.getId());
		if (!rootId.equals(moid)) {
			mo = mosvc.getManagedObject(user, rootId);
			addedAncestor = true;
		}
		moid = mo.getId();

		if (mo.getLayeredMetadataValue(TranslationConstants.LMD_TRANSLATION_OF) != null) {
			baseMoid = mo.getLayeredMetadataValue(TranslationConstants.LMD_TRANSLATION_OF);
			baseMo = mosvc.getManagedObject(user, baseMoid);
		} else {
			baseMoid = moid;
			baseMo = mo;
		}
		
		baseLang = baseMo.getLayeredMetadataValue(TranslationConstants.LMD_LANGUAGE);
		
		String query = "/*[rmd:get-lmd-value(., '" + TranslationConstants.LMD_TRANSLATION_OF + "')='" + baseMoid + "']";
		transMos = context.getSearchService().executeXPathSearch(user, query, 1, 100);
		moids = new String[transMos.size() + 1];
		moids[0] = baseMoid;
		for (int i = 0; i < transMos.size(); i++) {
			moids[i + 1] = transMos.get(i).getId();
		}

	}

	public String getBaseMoid() {
		return baseMoid;
	}
	
	public ManagedObject getBaseMo() {
		return baseMo;
	}

	public String getBaseLang() {
		return baseLang;
	}
	
	public List<ManagedObject> getTranslationsMos() {
		return transMos;
	}
	
	public String[] getTranslationSetIds() {
		return moids;
	}

	public Boolean wasDescendant() {
		return addedAncestor;
	}
}
