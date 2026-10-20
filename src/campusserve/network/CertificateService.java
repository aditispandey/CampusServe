package campusserve.network;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** RMI REMOTE INTERFACE: methods a client may call on the server. Each must declare RemoteException. */
public interface CertificateService extends Remote {
    String verify(String certificateCode) throws RemoteException;
}
