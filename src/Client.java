import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(String.join(" ", args)).useDelimiter("\\r\\n| +");

        // java Client -gateway localhost:9991 -operation get-value 17
        InetSocketAddress address = null;
        String op = null;
        boolean uniformOutput = false;

        while (scanner.hasNext()) {
            String option = scanner.next();
            switch (option) {
                case "-gateway": {
                    if (address != null) throw new RuntimeException("Args error: -gateway option can occur only once.");

                    scanner.useDelimiter("\\r\\n| +|:");
                    address = new InetSocketAddress(scanner.next(), scanner.nextInt());
                    scanner.useDelimiter("\\r\\n| +");
                } break;
                case "-operation": {
                    if (op != null) throw new RuntimeException("Args error: -operation option can occur only once.");

                    op = scanner.next() + (scanner.hasNext() ? " " + scanner.next() : "");
                } break;
                case "-uniform-output": {
                    uniformOutput = true;
                } break;
            }
        }

        assert address != null && op != null;

        if (!uniformOutput)
            System.out.printf("[%s:%d] \"%s\" -> ", address.getAddress().getHostAddress(), address.getPort(), op);

        try(Socket sock = new Socket(address.getHostName(), address.getPort())) {
            PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

            out.println(op);

            if (!uniformOutput) {
                System.out.printf("%s\n", in.readLine());
            } else {
                System.out.printf("[%s:%d] \"%s\" -> %s\n",
                        address.getAddress().getHostAddress(),
                        address.getPort(),
                        op,
                        in.readLine());
            }

        } catch (IOException err) {
            System.out.println(err.getMessage());
        }
    }
}
