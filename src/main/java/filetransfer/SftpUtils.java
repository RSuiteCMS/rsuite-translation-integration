package filetransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.reallysi.rsuite.api.extensions.ExecutionContext;

public class SftpUtils {

	private static Log log = LogFactory.getLog(SftpUtils.class);

	/**
	 * Uploads a file to SFTP server into a specific folder.
	 * 
	 * @param context
	 * @param connectionInfo
	 * @param sftpFolderPath
	 *            Remote path to upload file within.
	 * @param file
	 *            Local file to transfer the bytes of.
	 * @param filename
	 *            Remote file name to use.
	 */
	public static void uploadFile(ExecutionContext context,
			ISftpConnectionInfo connectionInfo, String sftpFolderPath,
			File file, String filename) throws SftpUtilsException {
		log.info("Transferring \"" + file.getAbsolutePath() + "\" as \""
				+ sftpFolderPath + "/" + filename + "\"...");
		try {
			FileInputStream inStream = new FileInputStream(file);
			uploadInputStream(context, connectionInfo, sftpFolderPath,
					inStream, filename);
		} catch (FileNotFoundException e) {
			handleExceptions("uploading file", sftpFolderPath, "", e);
		}
	}

	/**
	 * Uploads an inputStream to SFTP server into a specific folder.
	 * 
	 * @param context
	 * @param connectionInfo
	 * @param sftpFolderPath
	 *            Remote path to upload file within.
	 * @param file
	 *            Local file to transfer the bytes of.
	 * @param filename
	 *            Remote file name to use.
	 */
	public static void uploadInputStream(ExecutionContext context,
			ISftpConnectionInfo connectionInfo, String sftpFolderPath,
			InputStream inputStream, String filename) throws SftpUtilsException {

		Session session = null;
		ChannelSftp c = null;

		try {

			session = createSession(connectionInfo, context);
			c = createChannel(session);

			if (!stringIsEmpty(sftpFolderPath)) {
				changeRemoteDirectory(c, sftpFolderPath);
			}

			log.info("Transferring input stream as \"" + sftpFolderPath + "/"
					+ filename + "\" to " + connectionInfo.getHost() + " with user " + connectionInfo.getUserName() + "...");
			c.put(inputStream, filename, new ProgressMonitor(),
					ChannelSftp.OVERWRITE);
			log.info("...transfer complete.");
		} catch (Exception e) {
			handleExceptions("uploading input stream", sftpFolderPath, "", e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			if (c != null)
				c.exit();
			if (session != null)
				session.disconnect();
		}
	}

	/**
	 * Utility to change the remote directory, creating them when necessary.
	 * 
	 * @param c
	 * @param sftpFolderPath
	 * @throws SftpUtilsException
	 */
	private static void changeRemoteDirectory(ChannelSftp c,
			String sftpFolderPath) throws SftpUtilsException {

		String[] folders = sftpFolderPath.split("[/|\\\\]");
		log.info(folders.length + " dirs");
		for (String folder : folders) {

			// Move on to next folder name if this one is blank.
			if (stringIsBlank(folder))
				continue;

			try {

				// Try to cd to next level.
				log.info("changing remote dir: " + folder);
				c.cd(folder);

			} catch (Exception e) {

				try {

					// Try to create the remote dir, then cd to it.
					log.info("creating remote dir: " + folder);
					c.mkdir(folder);
					c.cd(folder);

				} catch (Exception ex) {

					throw new SftpUtilsException(
							"Error trying to change directory on SFTP site. Make sure you have sufficient permissions or that the remote directory exists.",
							e);

				}
			}
		}
	}

	/**
	 * Downloads a file from SFTP server.
	 * 
	 * @param connectionInfo
	 *            The connection info object.
	 * @param sftpFolderPath
	 *            The path to folder on the SFTP site.
	 * @param sftpFileName
	 *            The file name to download
	 * @param resultStream
	 *            The result stream
	 * @throws SftpUtilsException
	 *             if something goes wrong
	 */
	public static void downloadFile(ExecutionContext context,
			ISftpConnectionInfo connectionInfo, String sftpFolderPath,
			String sftpFileName, OutputStream resultStream)
			throws SftpUtilsException {

		try {

			Session session = createSession(connectionInfo, context);

			ChannelSftp c = createChannel(session);

			if (!stringIsBlank(sftpFolderPath)) {
				c.cd(sftpFolderPath);
			}

			c.get(sftpFileName, resultStream);

			c.exit();
			session.disconnect();

		} catch (JSchException e) {
			handleExceptions("creating channel (JSchException)",
					sftpFolderPath, sftpFileName, e);
		} catch (SftpException e) {
			handleExceptions("creating channel (SftpException)",
					sftpFolderPath, sftpFileName, e);
		} catch (IllegalStateException e) {
			handleExceptions("creating channel (IllegalStateException)",
					sftpFolderPath, sftpFileName, e);
		}
	}

	/**
	 * Consumes a file from SFTP server - download and remove file from the
	 * server.
	 * 
	 * @param connectionInfo
	 *            The connection info object.
	 * @param sftpFolderPath
	 *            The path to folder on the SFTP site.
	 * @param sftpFileName
	 *            The file name to download
	 * @param resultStream
	 *            The result stream
	 * @throws SftpUtilsException
	 *             if something goes wrong
	 */
	public static void downloadAndRemoveFile(ExecutionContext context,
			ISftpConnectionInfo connectionInfo, String sftpFolderPath,
			String sftpFileName, OutputStream resultStream)
			throws SftpUtilsException {

		try {

			Session session = createSession(connectionInfo, context);

			ChannelSftp c = createChannel(session);

			if (!stringIsBlank(sftpFolderPath)) {
				c.cd(sftpFolderPath);
			}

			c.get(sftpFileName, resultStream);
			c.rm(sftpFileName);
			c.exit();
			session.disconnect();

		} catch (JSchException e) {
			handleExceptions("creating channel (JSchException)",
					sftpFolderPath, sftpFileName, e);
		} catch (SftpException e) {
			handleExceptions("creating channel (SftpException)",
					sftpFolderPath, sftpFileName, e);
		} catch (IllegalStateException e) {
			handleExceptions("creating channel (IllegalStateException)",
					sftpFolderPath, sftpFileName, e);
		}
	}

	/**
	 * 
	 * @param context
	 * @param connectionInfo
	 * @param sftpFolderPath
	 * @param prefix
	 * @param OutputFolder
	 * @param files
	 * @throws SftpUtilsException
	 * @throws IOException
	 */
	public static void downloadAndRemoveFiles(ExecutionContext context,
			ISftpConnectionInfo connectionInfo, String sftpFolderPath,
			String prefix, String OutputFolder, List<File> files)
			throws SftpUtilsException, IOException {

		log.info("Retrieving files in: " + sftpFolderPath);
		try {

			Session session = createSession(connectionInfo, context);

			ChannelSftp c = createChannel(session);
			Vector<?> entryfiles = null;

			if (!stringIsBlank(sftpFolderPath)) {
				entryfiles = c.ls(sftpFolderPath);
				c.cd(sftpFolderPath);
			} else {
				entryfiles = c.ls(c.pwd());
			}

			String filename = "";

			for (Object entry : entryfiles) {

				ChannelSftp.LsEntry file = (ChannelSftp.LsEntry) entry;
				filename = file.getFilename();
				if (filename.startsWith(prefix)) {
					OutputStream output;
					output = new FileOutputStream(OutputFolder + "/" + filename);
					log.info("Dowloading file: " + filename + " to: "
							+ OutputFolder + "/" + filename);
					c.get(filename, output);
					c.rm(filename);
					output.close();

					File resultfile = new File(OutputFolder, filename);

					files.add(resultfile);

				}
			}

			c.exit();
			session.disconnect();

		} catch (JSchException e) {
			handleExceptions("creating channel (JSchException)",
					sftpFolderPath, prefix, e);
		} catch (SftpException e) {
			handleExceptions("creating channel (SftpException)",
					sftpFolderPath, prefix, e);
		} catch (IllegalStateException e) {
			handleExceptions("creating channel (IllegalStateException)",
					sftpFolderPath, prefix, e);
		}
	}

	private static ChannelSftp createChannel(Session session)
			throws JSchException {
		Channel channel = session.openChannel("sftp");
		channel.connect();
		ChannelSftp c = (ChannelSftp) channel;
		return c;
	}

	private static Session createSession(ISftpConnectionInfo connectionInfo,
			ExecutionContext context) throws SftpUtilsException {

		String host = connectionInfo.getHost();
		int port = connectionInfo.getPort();
		String userName = connectionInfo.getUserName();
		log.info("Creating SFTP session with " + userName + " " + host + " "
				+ String.valueOf(port));

		Properties properties = new Properties();
		properties.put("StrictHostKeyChecking", "no");

		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession(userName, host, port);
		} catch (JSchException e) {
			Exception e1 = new Exception(e.getMessage());
			e1.setStackTrace(e.getStackTrace());
			handleExceptions("get session", "", "", e1);
		}
		session.setConfig(properties);
		// TODO This line was present in the shared code received as input, but
		// causes an "auth error" when attempt to use with the SFTP site we're
		// using
		// session.setUserInfo((UserInfo) connectionInfo);
		session.setPassword(connectionInfo.getPassword());
		try {
			session.connect();
		} catch (JSchException e) {
			Exception e1 = new Exception(e.getMessage());
			e1.setStackTrace(e.getStackTrace());
			handleExceptions("session connection", "", "", e1);
		}

		return session;
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
