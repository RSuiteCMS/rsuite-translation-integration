rsuite-file-transfer-tools
-----

This plugin provides methods to send and retrieve files using FTP and SFTP, leveraging JCraft JSch (Java secure channel library, http://www.jcraft.com/jsch/). 

This plugin doesn't directly provide RSuite user features. It provides classes for developers to reference from other RSuite plugins.

To use:

1) Add this jar as a dependency for your plugin project.

2) Adjust your build process to copy the jar into your plugin build.

3) Call the appropriate methods to FTP/SFTP. For example:


Send by FTP
	InputStream translationSourceStream =  sourceMo.getInputStream(); 
	String filename = "myfile.xml";
	String ftpFolderPath = "export";
	ISftpConnectionInfo connectionInfo = FTPConnectionInfoFactory.createConnectionInfoObject(context);
	try {
		FtpUtils.uploadInputStream(connectionInfo, translationSourceStream, filename, ftpFolderPath);
		log.info("Success submitting " + filename + " to XTM Cloud via FTP.");
	} catch (SftpUtilsException e) {
		log.error("Error sending: " + filename + "to: " + connectionInfo.getHost() + ":" + connectionInfo.getPort());
		throw new RSuiteException(e.getMessage());
	}


Add RSuite properties for FTP or SFTP connection. 
	rsuite.ftp.host - eg us5.rsuitecms.com
	rsuite.ftp.user - eg myusername
	rsuite.ftp.password - eg mypassword
	rsuite.ftp.port - eg 21 
    rsuite.ftp.sftp - true or false; defaults to false

	(We should make it possible to set these via API instead...)

