package com.rsicms.rsuite.translation.datatype;

import java.util.List;

import com.reallysi.rsuite.api.DataTypeOptionValue;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.forms.DataTypeProviderOptionValuesContext;
import com.reallysi.rsuite.api.forms.DefaultDataTypeOptionValuesProviderHandler;

import com.rsicms.rsuite.translation.TranslationConstants;

/**
 * Provides a datatype handler for the list of translation status constants.
 * 
 */
public class TranslationStatusDataTypeProvider extends
		DefaultDataTypeOptionValuesProviderHandler {

	public void provideOptionValues(
			DataTypeProviderOptionValuesContext context,
			List<DataTypeOptionValue> optionValues) throws RSuiteException {

		optionValues.add(new DataTypeOptionValue(TranslationConstants.STATUS_TRANSLATION_NA_VALUE, TranslationConstants.STATUS_TRANSLATION_NA_LABEL));
		optionValues.add(new DataTypeOptionValue(TranslationConstants.STATUS_NOT_TRANSLATED_VALUE, TranslationConstants.STATUS_NOT_TRANSLATED_LABEL));
		optionValues.add(new DataTypeOptionValue(TranslationConstants.STATUS_TRANSLATION_IN_PROGRESS_VALUE, TranslationConstants.STATUS_TRANSLATION_IN_PROGRESS_LABEL));
		optionValues.add(new DataTypeOptionValue(TranslationConstants.STATUS_TRANSLATION_REQUEST_STALE_VALUE, TranslationConstants.STATUS_TRANSLATION_REQUEST_STALE_LABEL));
		optionValues.add(new DataTypeOptionValue(TranslationConstants.STATUS_TRANSLATION_STALE_VALUE, TranslationConstants.STATUS_TRANSLATION_STALE_LABEL));
		optionValues.add(new DataTypeOptionValue(TranslationConstants.STATUS_UP_TO_DATE_VALUE, TranslationConstants.STATUS_UP_TO_DATE_LABEL));

	}

}
