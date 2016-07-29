package filetransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.rsicms.rsuite.helpers.utils.net.ftp.FTPConnectionInfo;
import com.rsicms.rsuite.helpers.utils.net.ftp.FTPUtils;

public class FtpUtils extends FTPUtils {

	private static Log log = LogFactory.getLog(FtpUtils.class);

	/**
	 * Uploads an inputStream to SFTP server into a specific folder.
	 * 
	 * @param context
	 * @param connectionInfo
	 * @param ftpFolderPath
	 *            Remote path to upload file within.
	 * @param file
	 *            Local file to transfer the bytes of.
	 * @param filename
	 *            Remote file name to use.
	 */
	public static void uploadInputStream(ISftpConnectionInfo connectionInfo,
			InputStream inputStream, String filename, String ftpFolderPath)
			throws SftpUtilsException {

		FTPConnectionInfo connection = (FTPConnectionInfo) (connectionInfo
				.getConection());

		try {

			if (!stringIsEmpty(ftpFolderPath)) {
				ftpCreateDirectoryTree(connection, ftpFolderPath);
			}

			log.info("Transferring input stream as \"" + ftpFolderPath + "/"
					+ filename + "\" to "+ connectionInfo.getHost() + " with user " + connectionInfo.getUserName() + "...");
			FTPUtils.uploadStream(connection, inputStream, filename,
					ftpFolderPath);
			log.info("...transfer complete.");

		} catch (Exception e) {
			handleExceptions("uploading input stream", ftpFolderPath, "", e);
		} finally {
			IOUtils.closeQuietly(inputStream);

		}
	}

	/**
	 * Utility to create the remote directory, creating them when necessary.
	 * 
	 * @param c
	 * @param sftpFolderPath
	 * @throws SftpUtilsException
	 * @throws IOException
	 */
	private static void ftpCreateDirectoryTree(FTPConnectionInfo connection,
			String dirTree) throws SftpUtilsException, IOException {

		boolean dirExists = true;

		FTPClient ftpClient = createFTPClient(connection);

		// tokenize the string and attempt to change into each directory level.
		// If you cannot, then start creating.
		String[] directories = dirTree.split("/");
		for (String dir : directories) {
			if (!dir.isEmpty()) {
				if (dirExists) {
					dirExists = ftpClient.changeWorkingDirectory(dir);
				}
				if (!dirExists) {
					if (!ftpClient.makeDirectory(dir)) {
						throw new SftpUtilsException(
								"Unable to create remote directory '" + dir
										+ "'.  error='"
										+ ftpClient.getReplyString() + "'");
					}
					if (!ftpClient.changeWorkingDirectory(dir)) {
						throw new SftpUtilsException(
								"Unable to change into newly created remote directory '"
										+ dir + "'.  error='"
										+ ftpClient.getReplyString() + "'");
					}
				}
			}
		}

		ftpClient.logout();
		ftpClient.disconnect();
	}

	/**
	 * 
	 * @param context
	 * @param connectionInfo
	 * @param sftpFolderPath
	 * @param prefix
	 * @param OutputFolder
	 * @param files
	 * @throws IOException
	 * @throws SocketException
	 */
	public static void downloadAndRemoveFiles(
			ISftpConnectionInfo connectionInfo, String sftpFolderPath,
			String prefix, String OutputFolder, List<File> files)
			throws SocketException, IOException {

		FTPConnectionInfo connection = (FTPConnectionInfo) (connectionInfo
				.getConection());

		FTPClient ftpClient = createFTPClient(connection);

		FTPFile[] FTPfiles = ftpClient.listFiles(sftpFolderPath);
		
		ftpClient.changeWorkingDirectory(sftpFolderPath);

		for (FTPFile file : FTPfiles) {

			String filename = file.getName();
			if (filename.startsWith(prefix)) {
				OutputStream output;
				output = new FileOutputStream(OutputFolder + "/" + filename);
				ftpClient.retrieveFile(filename, output);
				// close output stream
				output.close();				
				File resultfile = new File(OutputFolder, filename);
				files.add(resultfile);
				ftpClient.deleteFile(filename);
			}

		}
		
		ftpClient.logout();
		ftpClient.disconnect();

	}

	/**
	 * 
	 * @param connection
	 * @return
	 * @throws SocketException
	 * @throws IOException
	 */
	private static FTPClient createFTPClient(FTPConnectionInfo connection)
			throws SocketException, IOException {
		FTPClient ftpClient = new FTPClient();

		ftpClient.connect(connection.getHost(), connection.getPort());
		int replyCode = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(replyCode)) {
			log.error("Connection to FTP failed. Server reply code: "
					+ replyCode);
			return null;
		}
		boolean success = ftpClient.login(connection.getUser(),
				connection.getPassword());

		if (!success) {
			log.error("Login failed for user: " + connection.getUser());
			return null;
		}
		return ftpClient;
	}

	private static Boolean stringIsEmpty(String s) {
		if (s.isEmpty()) {
			return true;
		}
		return false;
	}

	private static Boolean stringIsBlank(String s) {
		if (s == null || s.isEmpty()) {
			return true;
		}
		return false;
	}

	private static void handleExceptions(String classContext,
			String sftpFolder, String sftpFileName, Exception e)
			throws SftpUtilsException {
		StringBuilder errMsg = new StringBuilder("Problem with " + classContext);
		if (!stringIsEmpty(sftpFileName))
			errMsg.append(" ").append(sftpFileName);
		if (!stringIsEmpty(sftpFolder))
			errMsg.append(" from folder ").append(sftpFolder);
		log.error("Throwing error: " + errMsg.toString());
		throw new SftpUtilsException(errMsg.toString(), e);
	}
}
