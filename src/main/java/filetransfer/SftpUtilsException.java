package filetransfer;

public class SftpUtilsException extends Exception {

	
	private static final long serialVersionUID = -7627554826316322409L;

	public SftpUtilsException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SftpUtilsException(String msg) {
		super(msg);
	}

	public SftpUtilsException(Throwable cause) {
		super(cause);
	}
}
