package filetransfer;


import com.jcraft.jsch.UserInfo;


/**
 * This interface extends jcraft UserInfo interface. Adds necessary information
 * to set up connection with SFTP server.
 */
public interface ISftpConnectionInfo extends UserInfo
{
    String getUserName ();

    String getHost ();

    int getPort ();
    
    Object getConection();
    
    
}
