package filetransfer;

import com.jcraft.jsch.SftpProgressMonitor;


public class ProgressMonitor implements SftpProgressMonitor {

	@Override
	public boolean count(long arg0) {		
		return true;
	}

	@Override
	public void end() {		
	}

	@Override
	public void init(int arg0, String arg1, String arg2, long arg3) {		
	}

}
