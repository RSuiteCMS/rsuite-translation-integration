package filetransfer;

import com.reallysi.rsuite.api.ConfigurationProperties;
import com.reallysi.rsuite.api.extensions.ExecutionContext;

public class FTPConnectionInfoFactory {

	public static ISftpConnectionInfo createConnectionInfoObject(
			ExecutionContext context) {

		ConfigurationProperties properties = context
				.getConfigurationProperties();

		String host = properties.getProperty(FileTransferConstants.FTP_HOST, null);
		int port = Integer.parseInt(properties.getProperty(FileTransferConstants.FTP_PORT,
				"0"));
		String user = properties.getProperty(FileTransferConstants.FTP_USER, null);
		String password = properties.getProperty(FileTransferConstants.FTP_PASSWORD, null);

		String isSftp = properties.getProperty(FileTransferConstants.FTP_SSL, "false");

		if (host != null && user != null && password != null && port != 0) {
			if ("true".equals(isSftp)) {
				return new SftpConnectionInfo(host, port, user, password);
			} else {
				return new SimpleFtpConnectionInfo(host, port, user, password);
			}
		}

		return null;

	}

}
