package com.rsicms.rsuite.translation.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.DataType;
import com.reallysi.rsuite.api.ElementMatchingCriteria;
import com.reallysi.rsuite.api.FormControlType;
import com.reallysi.rsuite.api.LayeredMetadataDefinition;
import com.reallysi.rsuite.api.ManagedObjectDefinition;
import com.reallysi.rsuite.api.NamespaceDecl;
import com.reallysi.rsuite.api.Principal;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.SchemaInfo;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.extensions.Plugin;
import com.reallysi.rsuite.api.extensions.PluginLifecycleListener;
import com.reallysi.rsuite.api.process.ScheduledJobInfo;
import com.reallysi.rsuite.api.system.Job;
import com.reallysi.rsuite.service.SchemaService;
import com.rsicms.pluginUtilities.types.ElementTypeMatchingCriteria;

import com.rsicms.rsuite.translation.TranslationConstants;


public class ConfigurationLifecycle implements PluginLifecycleListener  {
	
    Log log = LogFactory.getLog(ConfigurationLifecycle.class);

    @Override
	public void start(ExecutionContext context, Plugin plugin) {
		try {
	        User user = context.getAuthorizationService().getSystemUser();
	            
	        configureLmd(context, user);
            
			Principal principal = (Principal) user;
			String jobName = "RetrieveFilesFromXTMBridge"; 
	        Map<String, String> jobVariables = new HashMap<String, String>();
	        try {
	        	context.getSchedulerService().createScheduledJob(principal, 
	        			"xtmTranslations", //handlerId as declared in rsuite-plugin.xml 
					jobName, //jobName
					"Scheduled job to retrieve translated files from XTM Bridge", //jobDescription 
					"1/30 * * * * ?", //cronExpression 
					jobVariables
				);
				//context.getSchedulerService().pauseJob(principal, jobName);
	        } catch (Exception e) {
	        	//job exists
	        	log.info("Job creation error: " + e);
	        }
	        
		} catch (RSuiteException e) {
			e.printStackTrace();
		}
		
	}

	private void configureLmd(ExecutionContext context, User user) throws RSuiteException {
        List<ElementMatchingCriteria> elements = new ArrayList<ElementMatchingCriteria>();
        
		SchemaService ss = context.getSchemaService();
		Map<String, Map<String, String>> normalizedElementList = new HashMap<String, Map<String, String>>();
		getElementList(ss, normalizedElementList);
		for (String elkey : normalizedElementList.keySet()) {
			elements.add(ElementTypeMatchingCriteria.createForElementType(normalizedElementList.get(elkey).get("namespaceUri"), normalizedElementList.get(elkey).get("localName")));
		}
        elements.add(ElementTypeMatchingCriteria.createForElementType(null, "rs_ca"));
        elements.add(ElementTypeMatchingCriteria.createForElementType(null, "rs_canode"));
        elements.add(ElementTypeMatchingCriteria.createForElementType(null, "nonxml"));

        Boolean isVersionable = false;
        Boolean allowsMultiple = false;
        Boolean allowsContextual = false;
        
        createLmdField(context, user, TranslationConstants.LMD_LANGUAGE, "languageDt", "Language", FormControlType.AUTOCOMPLETE, 
        		"Language", elements, isVersionable, allowsMultiple, allowsContextual);

        createLmdField(context, user, TranslationConstants.LMD_IS_BASE_LANGUAGE, "booleanDt", "Is Base Language", FormControlType.CHECKBOX, 
        		"Base language true/false", elements, isVersionable, allowsMultiple, allowsContextual);

        createLmdField(context, user, TranslationConstants.LMD_TRANSLATION_OF, "", "Translation of", FormControlType.INPUT, 
        		"Translation source id", elements, isVersionable, allowsMultiple, allowsContextual);

        createLmdField(context, user, TranslationConstants.LMD_TRANSLATION_OF_REVISION, "", "Translation of Revision", FormControlType.INPUT, 
        		"Translation source revision number", elements, isVersionable, allowsMultiple, allowsContextual);

        createLmdField(context, user, TranslationConstants.LMD_TRANSLATION_STATE, "translationStateDt", "Translation State", FormControlType.RADIOBUTTON, 
        		"Translation state relative to base language document", elements, isVersionable, allowsMultiple, allowsContextual);

        createLmdField(context, user, TranslationConstants.LMD_TRANSLATION_UPDATE_DATE, "", "Translation Update Date", FormControlType.INPUT, 
        		"Date on which translation was received from translation system", elements, isVersionable, allowsMultiple, allowsContextual);

        createLmdField(context, user, TranslationConstants.LMD_TRANSLATION_REQUEST_DATE, "", "Translation Request Date", FormControlType.INPUT, 
        		"Translation request date", elements, isVersionable, allowsMultiple, allowsContextual);

	}

	private void getElementList(SchemaService ss, Map<String, Map<String, String>> normalizedElementList)
			throws RSuiteException {
		for (SchemaInfo si : ss.getSchemaInfoValues()) {
			Map<String, String> nsLookup = new HashMap<String, String>();
			if (null != si.getNamespaceDecls()) {
				for (@SuppressWarnings("deprecation") NamespaceDecl nsd : si.getNamespaceDecls()) {					
					nsLookup.put(nsd.getUri(), nsd.getPrefix());
				}
			}
			Map<String, ManagedObjectDefinition> moDefs = ss.getManagedObjectDefinitionCatalog(si.getSchemaId()).getManagedObjectDefinitions();
			for (String moDefKey : moDefs.keySet()) {
				ManagedObjectDefinition moDef = moDefs.get(moDefKey);
				Map<String, String> ret = new HashMap<String, String>();
				ret.put("localName", moDef.getName());
				ret.put("namespaceUri", moDef.getNamespaceUri());
				ret.put("prefix", nsLookup.get(moDef.getNamespaceUri()));
				String key = "";
				if (moDef.getNamespaceUri() != null) {
					key += "{" + moDef.getNamespaceUri() + "}:";
				}
				key += moDef.getName();
				normalizedElementList.put(key, ret);
			}
		}
	}

	private void createLmdField(ExecutionContext context, User user, String defName, String dtName, String label, FormControlType controlType, String description, 
			List<ElementMatchingCriteria> elements, Boolean isVersionable, Boolean allowsMultiple,
			Boolean allowsContextual) throws RSuiteException {
		removeLmdDefIfExists(context, user, defName);
		DataType dt = null;
		if (dtName != null & !dtName.isEmpty())
			dt = context.getDomainManager().getCurrentDomainContext().getDataTypeManager().getDataType(user, dtName);
        try {
        	context.getMetaDataService().createLayeredMetaDataDefinition(user, defName, "string", isVersionable, allowsMultiple, allowsContextual, 
        		elements, dt, label, "", controlType, description);
        	log.info("Added LMD field " + defName);
        } catch (Exception e) {
        	log.error("Error creating LMD def, but it probably was created just fine. " + e.getMessage() + " " + e);
        }
	}

	private void removeLmdDefIfExists(ExecutionContext context, User user, String defName) throws RSuiteException {
		LayeredMetadataDefinition def = context.getMetaDataService().getLayeredMetaDataDefinition(user, defName);
		if (def != null) {
			try {
				context.getMetaDataService().removeLayeredMetaDataDefinition(user, defName);
			} catch (Exception e) {
				log.error("Unexpected error removing LMD def that we know exists: " + e.getMessage() +  " " + e);
			}
		}
	}
 
    @Override
	public void stop(ExecutionContext context, Plugin plugin) {}

}
