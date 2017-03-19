package javassh;

import com.jcraft.jsch.*;
import com.jcraft.jsch.Session;
import org.junit.Test;


/**
 * Created by 王杰 on 2017/03/18.
 */
public class JschScp {

    static class FileTransferMonitor implements SftpProgressMonitor {
        long total = 0;
        long sent = 0;
        int lastProgress = 0;

        public void init(int op, String src, String dst, long max) {
            total = max;
//                    System.out.println(String.format("SftpProgressMonitor init called. : (op, src, dst, max)=(%d, %s, %s, %d)",
//                            op, src, dst, max));
        }

        public boolean count(long count) {
            sent += count;

            float ret = (float) sent * 100 / total;

            if (((int) ret) - lastProgress >= 10) {
                System.out.println(String.format("Processed %.2f(%d/%d)", ret, sent, total));

                lastProgress = (int) ret;
            }

            return true;
        }

        public void end() {
            System.out.println("finished.");
        }
    }

    private static ChannelSftp getSftpChannel(String host, int port, String user, String password) throws Exception {
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        channel = session.openChannel("sftp");
        channel.connect();
        channelSftp = (ChannelSftp) channel;

        return channelSftp;
    }

    private static void close(Channel channel) throws Exception {
        if(channel != null)
        {
            if( !channel.isClosed()) {
                channel.disconnect();
            }

            if(channel.getSession() != null){
                channel.getSession().disconnect();
            }
        }

    }

    public static void uploadFile(String host, int port, String user, String password, String input, String output) throws Exception {
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

            //channelSftp.cd(SFTPWORKINGDIR);

            channelSftp.put(input, output, new FileTransferMonitor());

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (channel != null && channel.isClosed()) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void downloadFile(String host, int port, String user, String password, String input, String output) throws Exception {

        ChannelSftp channelSftp = getSftpChannel(host, port, user, password);
        channelSftp.get(input, output, new FileTransferMonitor());
        close(channelSftp);
    }
    public static void scp(String host, int port, String username, String password, String input, String output) throws Exception {

//        // scp sftp://root:test@192.168.37.101//opt/dccs/install /opt/local/dir/
        ChannelSftp channelSftp = getSftpChannel(host, port, username, password);
        channelSftp.put(input, output, new FileTransferMonitor());
        close(channelSftp);
    }

    @Test
    public void testUpload() throws Exception{
        scp("202.5.16.25", 22, "mahjong","mahjong123","D:/spark-1.5.1/examples/src/main/resources", "/home/mahjong/wangjie/");
    }



}
