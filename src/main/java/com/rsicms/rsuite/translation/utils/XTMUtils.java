package com.rsicms.rsuite.translation.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.ManagedObject;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.xml.XmlSerializer;

import filetransfer.FTPConnectionInfoFactory;
import filetransfer.FtpUtils;
import filetransfer.ISftpConnectionInfo;
import filetransfer.SftpUtilsException;
import com.rsicms.rsuite.translation.TranslationConstants;

public class XTMUtils {

	private static Log log = LogFactory.getLog(XTMUtils.class);

	/**
	 * Request to send a file to XTM Cloud for translation, via XTM Bridge.
	 * 
	 * @param context
	 * @param sourceMo
	 *            ManagedObject to be translated.
	 * @param fileExtension
	 *            File extension of the file to be translated.
	 * @param client
	 *            The client for whom translation has been configured. Included
	 *            in the filename pattern so that the same RSuite instance can
	 *            be used to configure actions for more than one customer or
	 *            prospect.
	 * @param project
	 *            Whatever the developer wants it to be, but typically the
	 *            RSuite ID for the source language MO.
	 * @param rsuiteId
	 *            RSuite ID of the placeholder MO for the translation.
	 * @param targetLanguage
	 *            Language of the translation.
	 * @param lang
	 *            Language of the source document.
	 * @throws RSuiteException
	 */
	public static void submitProjectToXtmCloud(ExecutionContext context,
			ManagedObject sourceMo, String fileExtension, String client,
			String project, String rsuiteId, String targetLanguage, String lang)
			throws RSuiteException {

		String rsuiteInstanceId = context.getConfigurationProperties()
				.getProperty(TranslationConstants.RSUITE_INSTANCE_ID, "");
		
		String baseImportPath = context.getConfigurationProperties().getProperty(TranslationConstants.FTP_EXPORT_PATH, "export");

		String filename = rsuiteInstanceId + "_" + client + "_" + project
				+ "_" + rsuiteId + "_" + targetLanguage + "." + fileExtension;
		String sftpFolderPath = baseImportPath + "/" + project + "/" + targetLanguage;

		log.info("Requesting XTM translation for: " + filename + " " + lang
				+ ">>" + targetLanguage);

		ISftpConnectionInfo connectionInfo = FTPConnectionInfoFactory.createConnectionInfoObject(context);
		try {
			InputStream translationSourceStream = removeIdsFromSource(context, sourceMo); 
			FtpUtils.uploadInputStream(connectionInfo, translationSourceStream, filename, sftpFolderPath);
			log.info("Success submitting " + filename + " to XTM Cloud via FTP.");
		} catch (Exception e) {
			log.error("Error sending: " + filename + "to: " + connectionInfo.getHost() + ":" + connectionInfo.getPort());
			throw new RSuiteException(e.getMessage());
		}
	}

	/**
	 * Use SFTP to check for translation output from XTM via XTM Bridge. Each
	 * RSuite instance has its own id in rsuite.properties to distinguish its
	 * translation files.
	 * 
	 * @param context
	 * @param user
	 * @param client
	 *            The client for whom translation has been configured. Included
	 *            in the filename pattern so that the same RSuite instance can
	 *            be used to configure actions for more than one customer or
	 *            prospect. NOT YET IMPLEMENTED - NOT USED AS FILTER WHEN 
	 *            RETRIEVING FILES.
	 * @return
	 * @throws RSuiteException
	 * @throws IOException
	 * @throws SftpUtilsException
	 */
	public static List<XTMFile> retrieveFilesFromXtmCloud(
			ExecutionContext context, User user, String client)
			throws RSuiteException {
		log.info("Requesting XTM translation files");
		List<XTMFile> xtmFiles = new ArrayList<XTMFile>();
		List<File> files = new ArrayList<File>();

		String rsuiteInstanceId = context.getConfigurationProperties()
				.getProperty(TranslationConstants.RSUITE_INSTANCE_ID, "");

		ISftpConnectionInfo connectionInfo = FTPConnectionInfoFactory
				.createConnectionInfoObject(context);

		String outputfolder = context.getConfigurationProperties().getProperty(
				TranslationConstants.RSUITE_TEMP_DIR, "");
		String sftpFolderPath = context.getConfigurationProperties()
				.getProperty(TranslationConstants.FTP_IMPORT_PATH, "import");

		try {
			FtpUtils.downloadAndRemoveFiles(connectionInfo, sftpFolderPath,
					rsuiteInstanceId + "*.*", outputfolder, files);
		} catch (IOException e) {
			log.error("Error retrieving file from the server. " + e.getMessage());
			throw new RSuiteException(e.getMessage());
		}

		for (File file : files) {
			// XTMFile() parses the filename to get the client etc
			XTMFile xtmFile = new XTMFile(context, user, file);
			xtmFiles.add(xtmFile);
			log.info("Adding file: " + file.getName() + " to the XTMFile list");
		}
		return xtmFiles;
	}

	public static InputStream removeIdsFromSource(ExecutionContext context, ManagedObject sourceMo)
			throws RSuiteException, IOException {
		return removeIdsFromSource(context, sourceMo, false);
	}
	public static InputStream removeIdsFromSource(ExecutionContext context, ManagedObject sourceMo, Boolean includeDocType)
			throws RSuiteException, IOException {
		String docString = "";
		try {
			//cannot include doctype when submit to XTM
			XmlSerializer serializer = context.getXmlApiManager().getXmlSerializer();
			byte[] bytes = serializer.convertToBytes(sourceMo.getElement(), "UTF-8", false, false);
			docString = IOUtils.toString(bytes, "UTF-8");
		} catch (IOException e) {
			log.error("Couldn't get string for mo document. Can't request translation. " + e);
			return null;
		}
		if (includeDocType) {
			docString = prependDoctypeDeclaration(sourceMo, docString);
		}
		docString = docString.replaceAll("r:rsuiteId=\\\"\\d+\\\"", "").trim();
		InputStream stream = new ByteArrayInputStream(docString.getBytes(StandardCharsets.UTF_8));
		return stream;
	}
	
	//TODO this is reusable code 
	private static String prependDoctypeDeclaration(ManagedObject mo, String docString) throws RSuiteException, IOException {
		String doctype = "<!DOCTYPE " + mo.getElement().getNodeName() + " PUBLIC \"" + mo.getPublicIdProperty() + "\" \"" + mo.getSystemIdProperty() + "\">";
		String docWithDocType = docString.replaceAll("\\<\\?xml(.)*?>", "");
		docWithDocType = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + doctype + docWithDocType;
		return docWithDocType;
	}

}
