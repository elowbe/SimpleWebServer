package web;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.io.*;

public class SSLManager {

    private SSLContext sslContext;

    public SSLManager(String keystorePath, String keystorePassword) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException, KeyManagementException {
        // Load the keystore containing the SSL certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        FileInputStream keyStoreInputStream = new FileInputStream(keystorePath);
        keyStore.load(keyStoreInputStream, keystorePassword.toCharArray());

        // Initialize KeyManagerFactory with the keystore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

        // Initialize SSLContext
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
    }

    public SSLSocket createSSLSocket(String host, int port) throws IOException {
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        return (SSLSocket) socketFactory.createSocket(host, port);
    }

    public SSLServerSocket createSSLServerSocket(int port) throws IOException {
        SSLServerSocketFactory serverSocketFactory = sslContext.getServerSocketFactory();
        return (SSLServerSocket) serverSocketFactory.createServerSocket(port);
    }

    // Additional methods for certificate verification and other utilities can be added here
}
