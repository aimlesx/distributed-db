import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalNode implements Node, Runnable {
    private final List<Node> neighbours;
    private Record data;
    private final ServerSocket listenerSocket;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    private Map<Integer, Long> sessions = new HashMap<>();

    private List<String> broadcast(String cmd) {
        List<String> results = new ArrayList<>();
        for (Node n : this.neighbours) {
            InetSocketAddress address = n.getAddress();
            try(
                    Socket socket = new Socket(address.getAddress().getHostAddress(), address.getPort());
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                out.println(cmd);
                results.add(in.readLine());
            } catch (IOException ignore) {}
        }
        return results;
    }

    public LocalNode(int bindPort, Record data, List<Node> neighbours) throws IOException {
        this.neighbours = neighbours;
        this.data = data;
        listenerSocket = new ServerSocket(bindPort);

        broadcast(String.format("handshake %s:%d",
                        InetAddress.getLocalHost().getHostAddress(),
                        listenerSocket.getLocalPort()));
    }

    private static final int ttl = 60000; // Time in ms for which the session id is blacklisted from processing
    public void beginTask(int cookie) { sessions.put(cookie, System.currentTimeMillis() + ttl); }

    @Override
    public void run() {
        System.out.printf("Listening on port %d\n", listenerSocket.getLocalPort());
        System.out.print("Neighbours: "); for (Node n : neighbours) System.out.printf("%s:%d ", n.getAddress().getAddress().getHostAddress(), n.getAddress().getPort());
        System.out.println();

        while(!this.listenerSocket.isClosed()) {
            try {
                Socket clientSocket = this.listenerSocket.accept();

                Scanner scanner = new Scanner(clientSocket.getInputStream());
                scanner.useDelimiter("\\r\\n| +|:");

                {
                    boolean temp = scanner.hasNext(); // Refresh buffer
                }

                if (!scanner.hasNextInt()) {
                    // Create session with random cookie
                    // And run task with it
                    threadPool.submit(new Task(this, clientSocket, scanner));
                } else {
                    int cookie = scanner.nextInt();

                    // Cycle avoidance
                    boolean sessionPresent = sessions.containsKey(cookie);
                    boolean sessionExpired = !sessionPresent || sessions.get(cookie) < System.currentTimeMillis();
                    if (sessionPresent && !sessionExpired) {
                        scanner.close();
                        clientSocket.close();
                    } else {
                        threadPool.submit(new Task(this, clientSocket, scanner, cookie));
                    }
                }
            } catch (IOException err) { System.err.println(err.getMessage()); }

            sessions = sessions.entrySet()
                                .stream()
                                .filter(s -> s.getValue() > System.currentTimeMillis())
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        threadPool.shutdown();
    }

    @Override
    public InetSocketAddress getAddress() {
        return new InetSocketAddress(listenerSocket.getInetAddress(), listenerSocket.getLocalPort());
    }

    public void addNeighbour(Node node) {
        this.neighbours.add(node);
        InetSocketAddress address = node.getAddress();
        System.out.printf("Added neighbour %s:%d\n", address.getAddress().getHostAddress(), address.getPort());
    }
    public void removeNeighbour(Node node) {
        this.neighbours.remove(node);
        InetSocketAddress address = node.getAddress();
        System.out.printf("Removed neighbour %s:%d, left: %d\n", address.getAddress().getHostAddress(), address.getPort(), this.neighbours.size());
    }

    @Override
    public boolean set(int cookie, int key, int value) {
        boolean successful = false;

        if (data.key() == key) {
            data = new Record(key, value);
            successful = true;
        }

        for (Node node : neighbours) {
            successful |= node.set(cookie, key, value);
        }

        return successful;
    }

    @Override
    public Record get(int cookie, int key) {
        if (this.data.key() == key) {
            return this.data;
        }

        for (Node node : this.neighbours) {
            Node.Record result = node.get(cookie, key);
            if (result != null) return result;
        }

        return null;
    }

    @Override
    public InetSocketAddress findKey(int cookie, int key) {
        if (this.data.key() == key) {
            try {
                return new InetSocketAddress(
                        InetAddress.getLocalHost().getHostAddress(),
                        this.listenerSocket.getLocalPort());
            } catch (Exception e) {
                return new InetSocketAddress(
                        "localhost",
                        this.listenerSocket.getLocalPort());
            }
        }

        for (Node node : this.neighbours) {
            InetSocketAddress result = node.findKey(cookie, key);
            if (result != null) return result;
        }

        return null;
    }

    private Record getMinMax(int cookie, boolean min) {
        Record result = this.data;

        for (Node node : this.neighbours) {
            Node.Record extracted = min ?
                    node.getMin(cookie) :
                    node.getMax(cookie);

            if (extracted == null) {
                continue;
            }

            if (min) {
                result = result.value() < extracted.value() ? result : extracted;
            } else {
                result = result.value() > extracted.value() ? result : extracted;
            }
        }

        return result;
    }

    @Override
    public Record getMax(int cookie) {
        return getMinMax(cookie, false);
    }

    @Override
    public Record getMin(int cookie) {
        return getMinMax(cookie, true);
    }

    @Override
    public void newRecord(int cookie, int key, int value) {
        this.data = new Record(key, value);
    }

    @Override
    public void terminate(int cookie) {
        try {
            broadcast(String.format("bye %s:%d",
                    InetAddress.getLocalHost().getHostAddress(),
                    listenerSocket.getLocalPort()));

            this.listenerSocket.close();
        } catch (Exception ignored) {}
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Node)) return false;
        return Node.equals(this, (Node)other);
    }
}
