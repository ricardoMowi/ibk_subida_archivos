package com.example.demo.config;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;
 
 
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPUtils {
 
    // For FTP server
 
    private String hostName;
    private String hostPort;
    private String userName;
    private String passWord;
    private String destinationDir;
 
    // For sFTP server
    private ChannelSftp channelSftp = null;
    private Session session = null;
    private Channel channel = null;
 
    private int userGroupId = 0;
 
    public SFTPUtils() {
 
    }
 
    public String getHostName() {
        return hostName;
    }
 
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
 
    public String getHostPort() {
        return hostPort;
    }
 
    public void setHostPort(String hostPort) {
        this.hostPort = hostPort;
    }
 
    public String getUserName() {
        return userName;
    }
 
    public void setUserName(String userName) {
        this.userName = userName;
    }
 
    public String getPassWord() {
        return passWord;
    }
 
    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
 
    public String getDestinationDir() {
        return destinationDir;
    }
 
    public void setDestinationDir(String destinationDir) {
        this.destinationDir = destinationDir;
    }
 
    public int getUserGroupId() {
        return userGroupId;
    }
 
    public void setUserGroupId(int userGroupId) {
        this.userGroupId = userGroupId;
    }
 
    private void initChannelSftp() {
        channelSftp = null;
        session = null;
        try {
 
            JSch jsch = new JSch();
            //
            session = jsch.getSession(userName, hostName,
                    Integer.valueOf(hostPort));
            // logger.info("get Session end");
            session.setPassword(passWord);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
 
        } catch (Exception ex) {
            System.out.println(ex);
        }
        // }
    }
 
    /*
     * Upload file to ftp server that has configuration on sysConfig.properties
     * filename: name of file that will be stored on ftp fis: input stream of
     * file that will be stored on ftp enableLog: enable log return value: URN
     */
    public String uploadFileToFTP(String filename, InputStream fis,
            boolean enableLog) {
        String result = "";
 
        initChannelSftp();
        try {
            // logger.info("session connect begin");
            if (!session.isConnected())
                session.connect();
            // logger.info("session connect end");
            channel = session.openChannel("sftp");
            // logger.info("channel connect begin");
            channel.connect();
            // logger.info("channel connect end");
            channelSftp = (ChannelSftp) channel;
            try {
                channelSftp.cd(destinationDir);
                // logger.info("cd relative Dir");
            } catch (SftpException e) {
                channelSftp.mkdir(destinationDir);
                channelSftp.cd(destinationDir);
            }
 
            channelSftp.put(fis, filename);
            System.out.println("Upload successful portfolio file name:" + filename);
            result = String.format("sftp://%s/%s/%s", hostName, destinationDir, filename);
 
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
        } catch (Exception ex) {
            if (enableLog)
            System.out.println(ex);
 
        }
 
        return result;
    }
 
    public String uploadFileToFTP(String desFileName, String srcFilePath,
            boolean enableLog) {
        String result = "";
        try {
            InputStream fis = new FileInputStream(srcFilePath);
            result = uploadFileToFTP(desFileName, fis, enableLog);
        } catch (Exception ex) {
            if (enableLog)
            System.out.println("ex");
            System.out.println("ex");
            System.out.println("ex");
            System.out.println(ex);
        }
 
        return result;
    }
 
    public boolean checkExist(String fileName) {
        boolean existed = false;
 
        initChannelSftp();
        try {
            if (!session.isConnected())
                session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            try {
                channelSftp.cd(destinationDir);
            } catch (SftpException e) {
                channelSftp.mkdir(destinationDir);
                channelSftp.cd(destinationDir);
            }
 
            Vector ls = channelSftp.ls(destinationDir);
            if (ls != null) {
                // Iterate listing.
                System.out.println(fileName);
                for (int i = 0; i < ls.size(); i++) {
                    LsEntry entry = (LsEntry) ls.elementAt(i);
                    String file_name = entry.getFilename();
                    if (!entry.getAttrs().isDir()) {
                        if (fileName.toLowerCase().startsWith(file_name)) {
                            existed = true;
                        }
                    }
                }
            }
 
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
        } catch (Exception ex) {
            existed = false;
            if (session.isConnected()) {
                session.disconnect();
            }
        }
 
        return existed;
    }
 
    public void deleteFile(String fileName) {
 
        initChannelSftp();
        try {
            if (!session.isConnected())
                session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;
            try {
                channelSftp.cd(destinationDir);
            } catch (SftpException e) {
                channelSftp.mkdir(destinationDir);
                channelSftp.cd(destinationDir);
            }
            channelSftp.rm(fileName);
            channelSftp.exit();
            channel.disconnect();
            session.disconnect();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            if (session.isConnected()) {
                session.disconnect();
            }
        }
 
    }
}
