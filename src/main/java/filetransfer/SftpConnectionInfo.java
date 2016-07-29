package filetransfer;

/**
 * The connection info object
 */
public class SftpConnectionInfo implements ISftpConnectionInfo {

	private int maxRetries = 3; 
	
	private int attempt = 0;
	
	private String password;
	
	private int port;
	
	private String userName;
	
	private String host;
	
	public SftpConnectionInfo(
			String host, 
			int port, 
			String userName, 
			String password) {
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
	}

	public String getPassphrase() {
		return null;
	}

	public String getPassword() {
		attempt++;
		
		if (attempt > maxRetries ){
			throw new IllegalStateException("Maximum login attempts exceeded");
		}
		return password;
	}

	public boolean promptPassphrase(String arg0) {
		return false;
	}

	public boolean promptPassword(String arg0) {
		return true;
	}

	public boolean promptYesNo(String arg0) {
		return false;
	}

	public void showMessage(String arg0) {
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public Object getConection() {		
		return this;
	}
}
