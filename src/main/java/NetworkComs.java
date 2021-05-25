// based on the example code for TLS-channel.

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import tlschannel.*;


/**
 * Handles network communication for SeedPublisher.
 */
public class NetworkComs {

    /**
     * The foreign host to connect to.
     */
    private String host;

    /**
     * The foreign port to connect to.
     */
    private int port;

    /**
     * The API key used to identify the current user.
     */
    private String api_key;

    /**
     * Initialize this class. No connections will be made until the member methods are called.
     * @param host The hostname to connect to.
     * @param port The port to connect to at that hostname.
     * @param api_key The API key to pass, uniquely identifying the current user.
     */
    public NetworkComs( String host, int port, String api_key ) {

        this.host = host;
        this.port = port;
        this.api_key = api_key;

    }
}
