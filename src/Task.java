import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Task implements Runnable {
    private final Socket connection;
    public Scanner in;
    public PrintWriter out;

    private final int cookie;

    private final LocalNode owner;

    public Task(LocalNode owner, Socket socket, Scanner scanner) throws IOException {
        this(owner, socket, scanner, ThreadLocalRandom.current().nextInt());
    }

    public Task(LocalNode ownerNode, Socket socket, Scanner scanner, int cookie) throws IOException {
        owner = ownerNode;
        connection = socket;
        in = scanner;
        this.cookie = cookie;

        out = new PrintWriter(connection.getOutputStream(), true);

        owner.beginTask(cookie);
    }

    @Override
    public void run() {
        LocalNode node = owner;

        assert in.hasNext();
        String operation = in.next();

        switch (operation) {
            case "handshake": {
                node.addNeighbour(new RemoteNode(new InetSocketAddress(in.next(), in.nextInt())));
            } break;
            case "bye": {
                node.removeNeighbour(new RemoteNode(new InetSocketAddress(in.next(), in.nextInt())));
            } break;
            case "set-value": {
                boolean success = node.set(cookie, in.nextInt(), in.nextInt());

                String result = success ? "OK" : "ERROR";

                out.println(result);
            } break;
            case "get-value": {
                Node.Record record = node.get(cookie, in.nextInt());
                boolean found = record != null;

                String result = found ? String.valueOf(record) : "ERROR";

                out.println(result);
            } break;
            case "find-key": {
                InetSocketAddress address = node.findKey(cookie, in.nextInt());
                boolean found = address != null;

                if (!found) {
                    out.println("ERROR");
                    break;
                }

                String res = String.format("%s:%d",
                        address.getAddress().getHostAddress(),
                        address.getPort());

                out.println(res);
            } break;
            case "get-max": {
                Node.Record max = node.getMax(cookie);

                out.println(max);
            } break;
            case "get-min": {
                Node.Record min = node.getMin(cookie);

                out.println(min);
            } break;
            case "new-record": {
                node.newRecord(cookie, in.nextInt(), in.nextInt());

                out.println("OK");
            } break;
            case "terminate": {
                node.terminate(cookie);

                out.println("OK");
            } break;
        }

        // End session
        try {
            connection.close();
        } catch (IOException ignore) {}

        System.out.printf("Finishing Task[%s]\n", operation);
    }
}
