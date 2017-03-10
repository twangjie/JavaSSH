package test;

import com.jcraft.jsch.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class Main {

    private static String mode = "SSH"; // SSH, SFTP
    private static String server = "127.0.0.1";
    private static int port = 22;
    private static String username = "root";
    private static String password = "rootroot";
    private static String command = "ll -h";
    private static Boolean isDownlaod = false;
    private static String inputFile = null;
    private static String outputFile = null;

    public static void parseOptions(String[] args) throws Exception {

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("m", "mode", true, "SSH, SFTP");
        options.addOption("s", "server", true, "192.168.36.101");
        options.addOption("p", "port", true, "22");
        options.addOption("u", "username", true, "root");
        options.addOption("P", "password", true, "rootroot");
        options.addOption("c", "command", true, "shell command in SSH mode");
        options.addOption("D", "download", true, "Download file in SFTP mode");
        options.addOption("U", "upload", true, "upload file in SFTP mode");

        // Parse the program arguments
        CommandLine cmd = parser.parse(options, args);

        if (args.length == 0 || cmd.hasOption('h')) {

            String strSpliter = "==================";
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("java -cp JavaSSH.jar test.Main", strSpliter, options, strSpliter);
            System.exit(-1);
        }

        if (cmd.hasOption('m')) {
            mode = cmd.getOptionValue('m');
        }
        if (cmd.hasOption('s')) {
            server = cmd.getOptionValue('s');
        }
        if (cmd.hasOption('p')) {
            port = Integer.parseInt(cmd.getOptionValue('p'));
        }
        if (cmd.hasOption('u')) {
            username = cmd.getOptionValue('u');
        }
        if (cmd.hasOption('P')) {
            password = cmd.getOptionValue('P');
        }
        if (cmd.hasOption('c')) {
            command = cmd.getOptionValue('c');
        }
        if (cmd.hasOption("D")) {
            isDownlaod = true;
            inputFile = cmd.getOptionValue('D');
            outputFile = args[args.length - 1];
        }
        if (cmd.hasOption('U')) {
            isDownlaod = false;
            inputFile = args[args.length - 1];
            outputFile = cmd.getOptionValue('U');
        }
    }

    public static void main(String[] args) throws Exception {

        Main m = new Main();
        m.parseOptions(args);

        if (mode.compareToIgnoreCase("ssh") == 0) {
            String res = exeCommand(server, port, username, password, command);
            System.out.println(res);
        } else if (mode.compareToIgnoreCase("sftp") == 0) {
            if (isDownlaod) {
                downloadFile(server, port, username, password, inputFile, outputFile);
            } else {
                uploadFile(server, port, username, password, inputFile, outputFile);
            }
        }
    }


    public static String exeCommand(String host, int port, String user, String password, String command) throws JSchException, IOException {

        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");

        session.setPassword(password);
        session.connect();

        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        InputStream in = channelExec.getInputStream();
        channelExec.setCommand(command);
        channelExec.setErrStream(System.err);
        channelExec.connect();
        String out = IOUtils.toString(in, "UTF-8");

        channelExec.disconnect();
        session.disconnect();

        return out;
    }

    static class FileTransferMonitor implements SftpProgressMonitor{
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

            if(((int)ret) - lastProgress >= 10) {
                System.out.println(String.format("Processed %.2f(%d/%d)", ret, sent, total));

                lastProgress = (int)ret;
            }

            return true;
        }

        public void end() {
            System.out.println("finished.");
        }
    }

    public static void uploadFile(String host, int port, String user, String password, String input, String output) throws JSchException, IOException {
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
        }finally {
            try {
                if (channel != null && channel.isClosed()) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public static void downloadFile(String host, int port, String user, String password, String input, String output) throws JSchException, IOException {
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

            channelSftp.get(input, output, new FileTransferMonitor());

        } catch (Exception ex) {
            ex.printStackTrace();
        }finally {
            try {
                if (channel != null && channel.isClosed()) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
