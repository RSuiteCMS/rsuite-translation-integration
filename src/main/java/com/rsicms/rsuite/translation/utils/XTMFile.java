package com.rsicms.rsuite.translation.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.extensions.ExecutionContext;

public class XTMFile {
	
	private String xtmClient = "";
	private String xtmProject = "";
	private String translationLanguage = "";
	private String translationRSuiteId = "";
	private File xtmFile = null;

	private static Log log = LogFactory.getLog(XTMFile.class);

	public XTMFile(ExecutionContext context, User user, File file) throws RSuiteException {

		//File pattern is:  rsuiteInstanceId + "_" + client + "_ " + project + "_" + rsuiteId + "_"  + targetLanguage + "."  + fileExtension;
		log.info("Reading translated file " + file.getName());

		String basename = FilenameUtils.getBaseName(file.getName());
		String[] filenameParts = basename.split("_");
		
		if (filenameParts.length < 5) {
			throw new RSuiteException("Unexpected filename pattern returned from XTM Cloud. File cannot be loaded: " + basename);
		}
		
		xtmFile = file;
		setClient(filenameParts[1]);
		String[] projectParts = filenameParts[2].split("-");
		setProject(projectParts[0]);
		setTranslationId(filenameParts[3]);
		setLanguage(filenameParts[4]);
	}

	public void setLanguage(String language) {
		log.info("  Language = " + language);
		translationLanguage = language;
	}
	
	public void setClient(String client) {
		log.info("  Client= " + client);
		xtmClient = client;
	}
	
	public void setProject(String project) {
		log.info("  Project = " + project);
		xtmProject = project;
	}

	public void setTranslationId(String id) {
		log.info("  RSuite ID = " + id);
		translationRSuiteId = id;
	}

	public String getTranslationLanguage() {
		return translationLanguage;
	}

	public String getClient() {
		return xtmClient;
	}

	public String getProject() {
		return xtmProject;
	}

	public String getTranslationRSuiteId() {
		return translationRSuiteId;
	}

	public File getFile() {
		return xtmFile;
	}
}
