package filetransfer;

import com.rsicms.rsuite.helpers.utils.net.ftp.FTPConnectionInfo;

/**
 * The connection info object
 */
public class SimpleFtpConnectionInfo extends FTPConnectionInfo implements ISftpConnectionInfo {

	private int maxRetries = 3; 
	
	private int attempt = 0;
	
	public SimpleFtpConnectionInfo(
			String host, 
			int port, 
			String userName, 
			String password) {
		super(host, port, userName, password);
	}

	public String getPassphrase() {
		return null;
	}

	public String getPassword() {
		attempt++;
		
		if (attempt > maxRetries ){
			throw new IllegalStateException("Maximum login attempts exceeded");
		}
		return super.getPassword();
	}

	public boolean promptYesNo(String arg0) {
		return false;
	}

	public void showMessage(String arg0) {
	}

	public String getHost() {
		return super.getHost();
	}

	public int getPort() {
		return super.getPort();
	}

	public String getUserName() {
		return super.getUser();
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	@Override
	public boolean promptPassphrase(String arg0) {
		return false;
	}

	@Override
	public boolean promptPassword(String arg0) {
		return false;
	}
	
	// Awful! Just a quick workaround until the code gets a real plugin.
	@Override
	public Object getConection() {
		// TODO Auto-generated method stub
		return new FTPConnectionInfo(super.getHost(), super.getPort(), super.getUser(), super.getPassword());
	}
}
