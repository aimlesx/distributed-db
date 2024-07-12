import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class DatabaseNode {

    public static void main(String[] args) throws UnknownHostException {
        String argsLine = String.join(" ", args);
        Scanner scanner = new Scanner(argsLine).useDelimiter(" +|:");

        // java DatabaseNode -tcpport 9991 -record 17:256 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989
        int bindPort = -1;
        Node.Record record = null;
        List<Node> neighbours = new ArrayList<>();

        while (scanner.hasNext()) {
            String option = scanner.next();
            switch (option) {
                case "-tcpport": {
                    if (bindPort != -1) throw new RuntimeException("Args error: -tcpport option can occur only once.");

                    bindPort = scanner.nextInt();
                } break;
                case "-record": {
                    if (record != null) throw new RuntimeException("Args error: -record option can occur only once.");

                    record = new Node.Record(scanner.nextInt(), scanner.nextInt());
                } break;
                case "-connect": {
                    String address = scanner.next();
                    if (address.equals("localhost")) address = InetAddress.getLocalHost().getHostAddress();
                    neighbours.add(new RemoteNode(new InetSocketAddress(address, scanner.nextInt())));
                } break;
            }
        }

        if (record == null || bindPort == -1) {
            System.exit(1337);
        }

        try {

            LocalNode node = new LocalNode(bindPort, record, neighbours);
            node.run();

        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }

}
