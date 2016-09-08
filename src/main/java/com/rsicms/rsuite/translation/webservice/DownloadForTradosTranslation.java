package com.rsicms.rsuite.translation.webservice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.MetaDataItem;
import com.reallysi.rsuite.api.ObjectType;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.control.NonXmlObjectSource;
import com.reallysi.rsuite.api.control.ObjectInsertOptions;
import com.reallysi.rsuite.api.control.ObjectSource;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.remoteapi.CallArgument;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;
import com.reallysi.rsuite.api.remoteapi.ContextPath;
import com.reallysi.rsuite.api.remoteapi.DefaultRemoteApiHandler;
import com.reallysi.rsuite.api.remoteapi.RemoteApiExecutionContext;
import com.reallysi.rsuite.api.remoteapi.RemoteApiResult;
import com.reallysi.rsuite.api.remoteapi.result.MessageAction;
import com.reallysi.rsuite.api.remoteapi.result.MessageDialogResult;
import com.reallysi.rsuite.api.remoteapi.result.MessageType;
import com.reallysi.rsuite.api.remoteapi.result.NotificationAction;
import com.reallysi.rsuite.api.remoteapi.result.RestResult;
import com.reallysi.rsuite.api.remoteapi.result.UserInterfaceAction;
import com.reallysi.rsuite.api.rule.Action;
import com.rsicms.rsuite.helpers.utils.RSuiteUtils;
import com.rsicms.rsuite.helpers.utils.ZipUtil;
import com.rsicms.rsuite.translation.TranslationConstants;
import com.rsicms.rsuite.translation.utils.TranslationSet;
import com.rsicms.rsuite.translation.utils.XTMUtils;

public class DownloadForTradosTranslation extends DefaultRemoteApiHandler {

	private static Log log = LogFactory.getLog(DownloadForTradosTranslation.class);
	
	String errMsgTitle = "Trados Project Error";
	String errMsgBase = "Error during Trados project creation";
	String errSeeAdmin = "If this persists, see your system administrator.";

	@Override
	public RemoteApiResult execute(RemoteApiExecutionContext context, CallArgumentList args) throws RSuiteException  {

		for (CallArgument arg : args.getAll()) {
			log.info("Arg: " + arg.getName() + ":" + arg.getValue());
		}

		User user = context.getSession().getUser();
		String baseMoid = args.getFirstString("baseMoid");
		TranslationSet ts = new TranslationSet(context, user, baseMoid);
		List<String> refreshIds = new ArrayList<String>();
		ManagedObject sourceMo = context.getManagedObjectService().getManagedObject(user, baseMoid);

    	ContextPath cp = ContextPath.fromString(args.getFirstString("rsuiteBrowseUri"), user, context);
    	if (cp == null || cp.isEmpty()) {
    		return new MessageDialogResult(MessageType.ERROR, errMsgTitle,
				"This feature is only available when browsing in a folder (not from search results or other contexts).");
    	}

    	List<ManagedObject> translationMos = new ArrayList<ManagedObject>();
		List<String> langs = args.getStrings("lang");
		for (String lang : langs) {
			String query = "/*[rmd:get-lmd-value(., '" + TranslationConstants.LMD_TRANSLATION_OF + "')='" + baseMoid + 
					"' and rmd:get-lmd-value(., '" + TranslationConstants.LMD_LANGUAGE + "')='" + lang + "']";
			ManagedObject transMo = null;
			try {
				List<ManagedObject> transMos = context.getSearchService().executeXPathSearch(user, query, 1, 1);
				transMo = transMos.get(0);
				refreshIds.add(transMo.getId());
				translationMos.add(transMo);
			} catch (Exception e) {
				return returnAnError(e);
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
		Date now = new Date();
		String outputfolder = context.getConfigurationProperties().getProperty(
				TranslationConstants.RSUITE_TEMP_DIR, "");
		String projectFolderName = sourceMo.getId() + "_" + langs.toString() + "_" + sdf.format(now);
		File projectFolder = new File(outputfolder, projectFolderName);
		log.info("Writing Trados project temporary folder to " + projectFolder.getAbsolutePath());
		if (projectFolder.exists()) {
			projectFolder.delete();
		}
		projectFolder.mkdirs();

		File baseLangFolder = new File(projectFolder, ts.getBaseLang());
		baseLangFolder.mkdirs();
		try {
			writeMoToFile(context, sourceMo, baseLangFolder.getAbsolutePath(), sourceMo.getId() + ".xml");
		} catch (Exception e) {
			return returnAnError(e);
		}
		
		try {
			writeProjectFile(context, ts.getBaseLang(), langs, projectFolder.getAbsolutePath(), projectFolderName);
		} catch (Exception e) {
			return returnAnError(e);
		}

		File zipFile = new File(outputfolder, projectFolderName + ".zip");
		log.info("Writing Trados project zip file to " + zipFile.getAbsolutePath());
    	try {
			ZipUtil.zipFolder(projectFolder.getAbsolutePath(), zipFile.getAbsolutePath());
		} catch (Exception e) {
			return returnAnError(e);
		}
    	
    	ManagedObject caMo = null;
    	for (int i = 0; i < cp.size(); i++) {
    		caMo = RSuiteUtils.getRealMo(context, user, context.getManagedObjectService().getManagedObject(user, cp.get(cp.size() - (i+1)).getId()));
    		if (caMo.getObjectType().equals(ObjectType.CONTENT_ASSEMBLY) || caMo.getObjectType().equals(ObjectType.CONTENT_ASSEMBLY_NODE)) {
    			ObjectSource src;
				try {
					src = new NonXmlObjectSource(zipFile);
				} catch (IOException e) {
					return returnAnError(e);
				}
    			ObjectInsertOptions options = new ObjectInsertOptions(zipFile.getName(), null, null, false);
    			options.setDisplayName("Trados_" + zipFile.getName());
				ManagedObject zipMo = context.getManagedObjectService().load(user, src, options);
				context.getContentAssemblyService().attach(user, caMo.getId(), zipMo.getId(), null);
				refreshIds.add(caMo.getId());
				refreshIds.add(zipMo.getId());
				break;
    		}
    	}
		
		for (ManagedObject tMo : translationMos) {
			MetaDataItem transStateLmd =  new MetaDataItem(TranslationConstants.LMD_TRANSLATION_STATE, TranslationConstants.STATUS_TRANSLATION_IN_PROGRESS_VALUE);
			context.getManagedObjectService().setMetaDataEntry(user, tMo.getId(), transStateLmd);
		}
		
		MessageAction messageAction = new MessageAction("Trados project has been created. You can now download it, unzip it, and open it in Trados." );
        RestResult webServiceResult = new RestResult();
        webServiceResult.addAction(messageAction);
        UserInterfaceAction uiAction = new UserInterfaceAction("rsuite:refreshManagedObjects");
        StringBuffer sb = new StringBuffer();
        for (String id : refreshIds) {
        	sb.append(id + ",");
        }
        uiAction.addProperty("objects", sb.toString());
        uiAction.addProperty("children", true);
        webServiceResult.addAction(uiAction);
        return webServiceResult;
	}

	private RemoteApiResult returnAnError(Exception e) {
		log.error(errMsgBase + ": " + e.getMessage() + e);
		return new MessageDialogResult(MessageType.ERROR, errMsgTitle,
				errMsgBase + ". " + errSeeAdmin);
	}

	private File writeProjectFile(ExecutionContext context, String baseLang, List<String> transLangs, String path, String baseFn) throws IOException,
		RSuiteException {
		if (!path.endsWith(File.separator))
			path += File.separator;
		File projectFile = new File(path, baseFn + ".sdlproj");
		
		StringBuffer projectSb = new StringBuffer();
		projectSb.append("<Project xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" Version=\"4.0.0.0\"" + 
				" ProjectTemplateGuid=\"00000000-0000-0000-0000-000000000000\">\n");
		projectSb.append("<LanguageDirections>\n");
		for (String l : transLangs) {
			projectSb.append("<LanguageDirection TargetLanguageCode=\"").append(l).append("\" SourceLanguageCode=\"").append(baseLang)
				.append("\">").append("<AutoSuggestDictionaries/>").append("<CascadeItem OverrideParent=\"false\" StopSearchingWhenResultsFound=\"false\"/>")
				.append("</LanguageDirection>\n");
		}
		projectSb.append("</LanguageDirections>\n");
		projectSb.append("<GeneralProjectInfo IsInPlace=\"false\" IsImported=\"false\" Description=\"\" Name=\"").append(baseFn).append("\"/>\n");
		projectSb.append("<SourceLanguageCode>").append(baseLang).append("</SourceLanguageCode>\n");
		projectSb.append("</Project>");
		
		InputStream is = IOUtils.toInputStream(projectSb.toString(), "UTF-8");
		FileUtils.writeByteArrayToFile(projectFile, IOUtils.toByteArray(is));
		return projectFile;
	}

	private File writeMoToFile(ExecutionContext context, ManagedObject mo, String path, String fn) throws IOException,
			RSuiteException {
		if (!path.endsWith(File.separator))
			path += File.separator;
		InputStream is = XTMUtils.removeIdsFromSource(context, mo, true);
		File outFile = new File(path, fn);
		FileUtils.writeByteArrayToFile(outFile, IOUtils.toByteArray(is));
		return outFile;
	}
	
}
