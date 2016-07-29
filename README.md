rsuite-file-transfer-tools
-----

This plugin provides methods to send and retrieve files using FTP and SFTP, leveraging JCraft JSch (Java secure channel library, http://www.jcraft.com/jsch/). 

This plugin doesn't directly provide RSuite user features. It provides classes for developers to reference from other RSuite plugins.

To use:

1) Add this jar as a dependency for your plugin project.

2) Add jcraft:jsch:0.1.45 as a dependency.

3) Adjust your build process to copy both jars into your plugin build.

4) Call the appropriate methods to FTP/SFTP in your code. For example:
	
	private void ftpSend(ExecutionContext context, ManagedObject sourceMo) throws RSuiteException {
		InputStream translationSourceStream =  sourceMo.getInputStream(); 
		String filename = "myfile.xml";
		String ftpFolderPath = "export";
		ISftpConnectionInfo connectionInfo = FTPConnectionInfoFactory.createConnectionInfoObject(context);
		try {
			FtpUtils.uploadInputStream(connectionInfo, translationSourceStream, filename, ftpFolderPath);
			log.info("Success submitting " + filename + " via FTP.");
		} catch (SftpUtilsException e) {
			log.error("Error sending: " + filename + "to: " + connectionInfo.getHost() + ":" + connectionInfo.getPort());
			throw new RSuiteException(e.getMessage());
		}
	}

	private void sftpSend(ExecutionContext context, ManagedObject sourceMo) throws RSuiteException {
		InputStream translationSourceStream =  sourceMo.getInputStream(); 
		String filename = "myfile.xml";
		String sftpFolderPath = "export";
		ISftpConnectionInfo connectionInfo = FTPConnectionInfoFactory.createConnectionInfoObject(context);
		try {
			SftpUtils.uploadInputStream(context, connectionInfo, translationSourceStream, filename, sftpFolderPath);
			log.info("Success submitting " + filename + " via FTP.");
		} catch (SftpUtilsException e) {
			log.error("Error sending: " + filename + "to: " + connectionInfo.getHost() + ":" + connectionInfo.getPort());
			throw new RSuiteException(e.getMessage());
		}
	}

	private List<File> ftpDownload(ExecutionContext context) throws RSuiteException {
		String wildcardMatcher = "*.*";
		String ftpFolderPath = "import";
		String outputfolder = context.getConfigurationProperties().getProperty(
				Constants.RSUITE_TEMP_DIR, "");
		ISftpConnectionInfo connectionInfo = FTPConnectionInfoFactory.createConnectionInfoObject(context);
		List<File> files = new ArrayList<File>();
		try {
			FtpUtils.downloadAndRemoveFiles(connectionInfo, ftpFolderPath,
					wildcardMatcher, outputfolder, files);
		} catch (IOException e) {
			log.error("Error retrieving file from the server. " + e.getMessage());
			throw new RSuiteException(e.getMessage());
		}
		return files;
	}

	private List<File> sftpDownload(ExecutionContext context) throws RSuiteException {
		String wildcardMatcher = "*.*";
		String ftpFolderPath = "import";
		String outputfolder = context.getConfigurationProperties().getProperty(
				Constants.RSUITE_TEMP_DIR, "");
		ISftpConnectionInfo connectionInfo = FTPConnectionInfoFactory.createConnectionInfoObject(context);
		List<File> files = new ArrayList<File>();
		try {
			SftpUtils.downloadAndRemoveFiles(context, connectionInfo, ftpFolderPath,
					wildcardMatcher, outputfolder, files);
		} catch (IOException | SftpUtilsException e) {
			log.error("Error retrieving file from the server. " + e.getMessage());
			throw new RSuiteException(e.getMessage());
		}
		return files;
	}
