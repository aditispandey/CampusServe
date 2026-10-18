package campusserve.network;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Run this ONCE in its own terminal. It starts both servers:
 *   - the socket server (live slot checks)      on port 5000
 *   - the RMI server (certificate verification) on port 1099
 * RMI note: the client's STUB is created automatically at run time (Java 5+); skeletons are obsolete.
 */
public class CampusServer {
    public static void main(String[] args) throws Exception {
        new Thread(new SlotServer(), "SlotServer").start();

        Registry registry = LocateRegistry.createRegistry(1099);
        registry.rebind("CertificateService", new CertificateServiceImpl());
        System.out.println("[RMI] CertificateService ready on port 1099");
        System.out.println("CampusServer is running. Press Ctrl+C to stop.");
    }
}
