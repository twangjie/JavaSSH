package javassh;

import org.apache.commons.cli.*;

public class Main {

    private static String server = "127.0.0.1";
    private static int port = 22;
    private static String username = "root";
    private static String password = "rootroot";
    private static String command = "ls -lh";

    public static void parseOptions(String[] args) throws Exception {

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption("s", "server", true, "192.168.36.101");
        options.addOption("P", "port", true, "22");
        options.addOption("u", "username", true, "root");
        options.addOption("p", "password", true, "rootroot");

        // Parse the program arguments
        CommandLine cmd = parser.parse(options, args);

        if (args.length == 0 || cmd.hasOption('h')
                || (args[0].compareToIgnoreCase("exec") != 0 && args[0].compareToIgnoreCase("scp") != 0)) {
//
//            String strSpliter = "==================";
//            HelpFormatter hf = new HelpFormatter();
//            hf.printHelp("java -jar JavaSSH.jar exec", strSpliter, options, strSpliter);

            System.out.println("Usage:\n\tjava -jar JavaSSH.jar [ssh/scp] ..." +
                    "\n\te.g." +
                    "\n\t java -jar JavaSSH.jar exec -u root -p testtest -s 192.168.35.11  \"ls -l\"" +
                    "\n\t java -jar JavaSSH.jar scp /var/log/test sftp://root:testtest@192.168.35.11:22//var/log/");

            System.exit(-1);
        }

        if (cmd.hasOption('s')) {
            server = cmd.getOptionValue('s');
        }
        if (cmd.hasOption('P')) {
            port = Integer.parseInt(cmd.getOptionValue('P'));
        }
        if (cmd.hasOption('u')) {
            username = cmd.getOptionValue('u');
        }
        if (cmd.hasOption('p')) {
            password = cmd.getOptionValue('p');
        }

        command = args[args.length - 1];

    }

    public static void main(String[] args) throws Exception {

        Main m = new Main();
        m.parseOptions(args);

        if (args[0].compareToIgnoreCase("exec") == 0) {
            String res = RemoteExec.exeCommand(server, port, username, password, command);
            System.out.println(res);
        } else if (args[0].compareToIgnoreCase("scp") == 0) {
            Scp.ScpTransfer(args[1], args[2]);
        }else {
            throw new Exception("Wrong parameter: " + args[0]);
        }
    }
}
