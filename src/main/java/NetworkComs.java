// based on the example code for TLS-channel.

import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.ec.ECVerifier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

import javax.net.ssl.SSLContext;

import io.fusionauth.jwt.json.ZonedDateTimeDeserializer;
import tlschannel.ClientTlsChannel;
import tlschannel.TlsChannel;


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
     * A helper to verify the provided JWTs.
     */
    private ECVerifier verifier;

    /**
     * A helper to create TLS connections.
     */
    private SSLContext ssl_context;

    /**
     * Keep a copy of the authorization JWT around, just in case.
     */
    private JWT auth_token = null;

    /**
     * Retrieve the last retrieved JWT authorization token.
     * @return The token itself, or null if there was no token.
     */
    public JWT getAuth_token() {
        return auth_token;
    }

    /**
     * Make accessing the UTF-8 charset a bit simpler.
     */
    private static final Charset utf8 = StandardCharsets.UTF_8;

    /**
     * Initialize this class. No connections will be made until the member methods are called.
     * @param host The hostname to connect to.
     * @param port The port to connect to at that hostname.
     * @param api_key The API key to pass, uniquely identifying the current user.
     * @param verify_key A Path object to the verification key.
     * @throws NoSuchAlgorithmException if the default TLS algorithm is unavailable. This should never happen.
     */
    public NetworkComs(String host, int port, String api_key, Path verify_key )
            throws NoSuchAlgorithmException {

        this.host = host;
        this.port = port;
        this.api_key = api_key;

        this.verifier = ECVerifier.newVerifier( verify_key );
        this.ssl_context = SSLContext.getDefault();
    }

    /**
     * Get an authorization token from the remote server. By convention it only returns
     *  the expiry time of the JWT. If you need access to the raw token, call getAuth_token().
     * @return The time the authorization token expires. If no token was retrieved, returns null.
     */
    public ZonedDateTime authorize() {

        // storage space for the retrieved string
        String jwt = null;

        // build the GET request
        String get = "GET https://" + this.host + "/credentials/authenticate?src_api_key=" +
                this.api_key + " HTTP/1.0\nHost: " + this.host + "\n\n";
        ByteBuffer get_bytes = ByteBuffer.wrap(get.getBytes(utf8));

        // try to open a connection
        try (SocketChannel channel = SocketChannel.open()) {

            // connect to the remote socket
            channel.connect( new InetSocketAddress(this.host, this.port) );

            // build a TLS channel with the default options
            ClientTlsChannel.Builder builder = ClientTlsChannel.newBuilder(channel, this.ssl_context);
            try (TlsChannel tlsChannel = builder.build()) {

                // send the get request
                tlsChannel.write( get_bytes );

                // read in the remote side and tidy it up
                ByteBuffer result = ByteBuffer.allocate(4096);
                while (tlsChannel.read(result) != -1) {}
                result.flip();

                jwt = utf8.decode(result).toString();
            }
        } catch (IOException e) {
            return null;
        }

        // no return at all? That's a failed token
        if( jwt == null )
            return null;

        // prevent clobbering the existing token
        JWT token = JWT.getDecoder().withClockSkew(60).decode(jwt, this.verifier);
        if( token != null ) {
            this.auth_token = token;
            return this.auth_token.expiration;
        }
        else
            return null;
    }
}
