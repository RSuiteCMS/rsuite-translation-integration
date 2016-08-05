package com.rsicms.rsuite.translation.formhandler;

import static com.rsicms.pluginUtilities.FormsUtils.addFormHiddenParameter;
import static com.rsicms.pluginUtilities.FormsUtils.addFormLabelParameter;
import static com.rsicms.pluginUtilities.FormsUtils.addFormSelectTypeParameter;
import static com.rsicms.pluginUtilities.FormsUtils.allowMultiple;
import static com.rsicms.pluginUtilities.FormsUtils.notReadOnly;
import static com.rsicms.pluginUtilities.FormsUtils.notRequired;
import static com.rsicms.pluginUtilities.FormsUtils.nullDataType;
import static com.rsicms.pluginUtilities.FormsUtils.nullValues;
import static com.rsicms.pluginUtilities.FormsUtils.readOnly;
import static com.rsicms.pluginUtilities.FormsUtils.sortNoSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.DataTypeOptionValue;
import com.reallysi.rsuite.api.FormControlType;
import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.forms.DefaultFormHandler;
import com.reallysi.rsuite.api.forms.FormColumnInstance;
import com.reallysi.rsuite.api.forms.FormDefinition;
import com.reallysi.rsuite.api.forms.FormInstance;
import com.reallysi.rsuite.api.forms.FormInstanceCreationContext;
import com.reallysi.rsuite.api.forms.FormParameterInstance;
import com.reallysi.rsuite.api.remoteapi.CallArgument;

import com.rsicms.rsuite.translation.TranslationConstants;
import com.rsicms.rsuite.translation.utils.TranslationSet;
import com.rsicms.rsuite.translation.utils.TranslationSetUtils;

public class TranslationAddForm extends DefaultFormHandler {
	private static Log log = LogFactory.getLog(TranslationAddForm.class);

	public void initialize(FormDefinition formDefinition) {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void adjustFormInstance(FormInstanceCreationContext context, FormInstance formInstance)
			throws RSuiteException {
		log.info("Returned arguments are: ");
		for (CallArgument arg : context.getArgs().getAll()) {
			log.info("  " + arg.getName() + " = " + arg.getValue());
		}

		List<FormColumnInstance> cols = new ArrayList<FormColumnInstance>();
		FormColumnInstance fci = new FormColumnInstance();
		List<FormParameterInstance> params = new ArrayList<FormParameterInstance>();

		String rsuiteId = context.getArgs().getFirstString("rsuiteId");
		User user = context.getSession().getUser();

		TranslationSet ts = new TranslationSet(context, user, rsuiteId);
		String baseLang = ts.getBaseLang();

		Map<String, String> langMap = TranslationSetUtils.getLanguageDataTypeAsMap(context, user);
		langMap.remove(baseLang);
		Map<String, String> missingLangMap = new HashMap<String,String>(langMap);

		addFormLabelParameter(params, "baseMo",
				"Add translations for \"" + ts.getBaseMo().getDisplayName() + "\" (" + ts.getBaseLang() + "):");
		int trans = 0;
		for (ManagedObject mo : ts.getTranslationsMos()) {
			String lang = mo.getLayeredMetadataValue(TranslationConstants.LMD_LANGUAGE);
			missingLangMap.remove(lang);
		}
		List<String> alphaKeys = new ArrayList<String>();
		for (String a : missingLangMap.keySet()) {
			alphaKeys.add(a);
		}
		Collections.sort(alphaKeys);
		for (String l : alphaKeys) {
			List<DataTypeOptionValue> options = new ArrayList<DataTypeOptionValue>();
			options.add(new DataTypeOptionValue(l, l + "/" + langMap.get(l)));
			addFormSelectTypeParameter(FormControlType.CHECKBOX, params, "lang", "", nullDataType, options, nullValues,
					sortNoSort, allowMultiple, notRequired, notReadOnly);
			trans++;
		}
		if (trans == 0) {
			List<DataTypeOptionValue> options = new ArrayList<DataTypeOptionValue>();
			options.add(new DataTypeOptionValue("none", "None available"));
			String[] val = new String[1];
			val[0] = "none";
			addFormSelectTypeParameter(FormControlType.CHECKBOX, params, "none", "", nullDataType, options, val,
					sortNoSort, allowMultiple, notRequired, readOnly);
		}

		addFormLabelParameter(params, "existing", "Existing translations:");
		int rc = 0;
		for (ManagedObject mo : ts.getTranslationsMos()) {
			String lang = mo.getLayeredMetadataValue(TranslationConstants.LMD_LANGUAGE);
			List<DataTypeOptionValue> options = new ArrayList<DataTypeOptionValue>();
			options.add(new DataTypeOptionValue(lang, langMap.get(lang) + " (" + lang + ") "
					+ TranslationSetUtils.getTranslationStateHtml(context, user, mo) ));
			String[] val = new String[1];
			val[0] = lang;
			addFormSelectTypeParameter(FormControlType.CHECKBOX, params, "langExists", "", nullDataType,
					options, val, sortNoSort, allowMultiple, notRequired, readOnly);
			rc++;
		}
		if (rc == 0) {
			List<DataTypeOptionValue> options = new ArrayList<DataTypeOptionValue>();
			options.add(new DataTypeOptionValue("none", "None"));
			String[] val = new String[1];
			val[0] = "none";
			addFormSelectTypeParameter(FormControlType.CHECKBOX, params, "none", "", nullDataType, options, val,
					sortNoSort, allowMultiple, notRequired, readOnly);
		}

		addFormHiddenParameter(params, "baseMoid", ts.getBaseMoid());

		fci.addParams(params);
		fci.setName("reportColumnsCol1");
		cols.add(fci);

		formInstance.setColumns(cols);

	}

}
