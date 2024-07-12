import java.net.InetSocketAddress;

public interface Node {
    class Record {
        public Integer key;
        public Integer value;

        public Record(int key, int value) {
            this.key = key;
            this.value = value;
        }

        Integer key() {
            return this.key;
        }

        Integer value() {
            return this.value;
        }

        static public Record fromString(String value) {
            String[] tokens = value.split(":");
            if (tokens.length != 2) return null;
            return new Record(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
        }

        @Override
        public String toString() {
            return String.format("%d:%d", this.key, this.value);
        }
    }

    InetSocketAddress getAddress();

    boolean set(int cookie, int key, int value);
    Record get(int cookie, int key);
    InetSocketAddress findKey(int cookie, int key);
    Record getMax(int cookie);
    Record getMin(int cookie);
    void newRecord(int cookie, int key, int value);
    void terminate(int cookie);

    static boolean equals(Node a, Node b) {
        String thisAddress = a.getAddress().getAddress().getHostAddress();
        String otherAddress = b.getAddress().getAddress().getHostAddress();

        int thisPort = a.getAddress().getPort();
        int otherPort = b.getAddress().getPort();

        return thisAddress.equals(otherAddress) && thisPort == otherPort;
    }
}