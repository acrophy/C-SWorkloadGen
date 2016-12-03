package cn.edu.sjtu.omnilab;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * Created by acrophy on 2016/11/30.
 */
public class TcpClient {

    public static final Logger LOG = Logger.getLogger(TcpClient.class);
    private static final double LAMBDA = 0.0008;
    private String serverIp = "127.0.0.1";
    private int serverPort = 3333;

    public int RandomExp(double lambda) {
        Random random = new Random(System.currentTimeMillis());
        double th = random.nextDouble();

        double t = 1/lambda * Math.log(1/th);
        return (int) t;
    }

    public void load() throws InterruptedException, IOException {
        int timeInterval;

        while (true) {
            timeInterval = this.RandomExp(LAMBDA);
            LOG.info("Next connection after " + timeInterval + " ms");
            System.out.println("Next connection after " + timeInterval + " ms");
            Thread.currentThread().sleep(timeInterval);
            new Thread(new Connection(serverIp, serverPort)).start();
        }
    }

    public void readCmd(String[] args) throws ParseException {
        Options ops = new Options();
        ops.addOption("s", "server IP", true, "server IP address");
        ops.addOption("p", "server port", true, "server port to connect");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(ops, args);

        if (cmd.hasOption("s")) {
            serverIp = cmd.getOptionValue("s");
        }
        if (cmd.hasOption("p")) {
            serverPort = Integer.parseInt(cmd.getOptionValue("p"));
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        TcpClient client = new TcpClient();
        client.readCmd(args);
        client.load();
    }

    class Connection implements Runnable {

        public final Logger LOG = Logger.getLogger(Connection.class);

        private Socket socket;

        public Connection(String serverIp, int serverPort) throws IOException {
            socket = new Socket(serverIp, serverPort);
            LOG.info("A new connection start!!!");
            System.out.println("A new connection start!!!");
        }

        @Override
        public void run() {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
                String fileType = "medium\n";
                osw.write(fileType, 0, fileType.length());
                osw.flush();


                DataInputStream dis = new DataInputStream(socket.getInputStream());
                File file = new File("tmp/" + System.currentTimeMillis());
                if (!file.exists()) file.createNewFile();

                FileOutputStream fos = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;

                long start = System.nanoTime();
                while((length = dis.read(bytes, 0, bytes.length)) != -1) {
                    fos.write(bytes, 0, length);
                    fos.flush();
                }
                long end = System.nanoTime();
                LOG.info("Flow completion time: " + (end - start)/1000 + " us");
                System.out.println("Flow completion time: " + (end - start)/1000 + " us\n");
                osw.close();
                dis.close();
                fos.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
