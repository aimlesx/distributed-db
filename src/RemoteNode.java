import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RemoteNode implements Node {
    private final InetSocketAddress address;

    public RemoteNode(InetSocketAddress address) {
        this.address = address;
    }

    private String query(int cookie, String cmd) {
        try(
                Socket socket = new Socket(address.getAddress(), address.getPort());
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            out.println(cookie + " " + cmd);
            return in.readLine();
        } catch (IOException err) {
            System.err.println(err.getMessage());
        }

        return null;
    }

    @Override
    public InetSocketAddress getAddress() {
        return address;
    }

    @Override
    public boolean set(int cookie, int key, int value) {
        String cmd = String.format("set-value %d:%d", key, value);

        String response = query(cookie, cmd);
        return response != null && response.equals("OK");
    }

    @Override
    public Record get(int cookie, int key) {
        String cmd = String.format("get-value %d", key);

        String response = query(cookie, cmd);
        if (response == null || response.equals("ERROR")) return null;

        return Record.fromString(response);
    }

    @Override
    public InetSocketAddress findKey(int cookie, int key) {
        String cmd = String.format("find-key %d", key);

        String response = query(cookie, cmd);
        if (response == null || response.equals("ERROR")) return null;

        String[] keyValue = response.split(":");
        assert keyValue.length >= 2;

        return new InetSocketAddress(keyValue[0], Integer.parseInt(keyValue[1]));
    }

    private Record getMinMax(int cookie, boolean min) {
        String cmd = min ? "get-min" : "get-max";

        String response = query(cookie, cmd);
        if (response == null) return null;

        return Record.fromString(response);
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
        String cmd = String.format("new-record %d:%d", key, value);
        query(cookie, cmd);
    }

    @Override
    public void terminate(int cookie) {
        query(cookie, "terminate");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Node)) return false;
        return Node.equals(this, (Node)other);
    }
}
