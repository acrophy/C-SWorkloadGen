package cn.edu.sjtu.omnilab;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;

/**
 * Created by acrophy on 2016/11/30.
 */
public class TcpServer{

    public static final Logger LOG = Logger.getLogger(TcpServer.class);
    private InetAddress localIp = null;
    private int port = 3333;

    public void load() throws IOException {
        ServerSocket server;
        Socket socket;

        if (localIp == null) {
            server = new ServerSocket(port);
            LOG.info("Tcp server is listening on port: " + port);
            System.out.println("Tcp server is listening on port: " + port);
        } else {
            server = new ServerSocket(port, 5, localIp);
            LOG.info("Tcp server is listening on ip: " + localIp + " port: " + port);
            System.out.println("Tcp server is listening on ip: " + localIp + " port: " + port);
        }


        while(true) {
            socket = server.accept();
            new Thread(new Connection(socket)).start();
        }
    }

    public void readCmd(String[] args) throws ParseException, UnknownHostException {
        Options ops = new Options();
        ops.addOption("b", "binding", true, "bind to the specified IP address");
        ops.addOption("p", "port", true, "listen on the specified port");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(ops, args);

        if (cmd.hasOption("b")) {
            localIp = InetAddress.getByName(cmd.getOptionValue("b"));
        }
        if (cmd.hasOption("p")) {
            port = Integer.parseInt(cmd.getOptionValue("p"));
        }
    }

    public static void main(String[] args) throws IOException, ParseException {

        TcpServer server = new TcpServer();
        server.readCmd(args);
        server.load();
    }

    class Connection implements Runnable {

        public final  Logger LOG = Logger.getLogger(Connection.class);

        private Socket socket;

        public Connection(Socket socket) {
            this.socket = socket;
            LOG.info("A new connection established from: " + socket.getInetAddress().getHostAddress());
            System.out.println("A new connection established from: " + socket.getInetAddress().getHostAddress());
        }

        @Override
        public void run() {
            try {
                String tmp;
                String file = null;
                BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if ((tmp = rd.readLine()) != null) {
                    switch (tmp) {
                        case "small": file = "src/main/resources/small";break;
                        case "medium": file = "src/main/resources/medium";break;
                        case "large": file = "src/main/resources/large";break;
                        default: break;
                    }
                    if (file != null) {
                        FileInputStream fis = new FileInputStream(file);
                        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

                        byte[] bytes = new byte[1024];
                        int length;

                        while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                            dos.write(bytes, 0, length);
                            dos.flush();
                        }
                        LOG.info("File transfer complete!!!\n");
                        System.out.println("File transfer complete!!!\n");

                        rd.close();
                        fis.close();
                        dos.close();
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
