package campusserve.network;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/** RMI CLIENT used by the admin dashboard. */
public final class CertificateClient {
    private CertificateClient() { }

    public static String verify(String code) throws Exception {
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        CertificateService service = (CertificateService) registry.lookup("CertificateService");   // this object is the STUB
        return service.verify(code);                                                                // runs on the server
    }
}
