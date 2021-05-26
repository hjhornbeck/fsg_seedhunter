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
import java.util.Base64;

import javax.net.ssl.SSLContext;

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
     * Return the last retrieved JWT authorization token.
     * @return The token itself, or null if there was no token.
     */
    public JWT getAuth_token() {
        return auth_token;
    }

    /**
     * Keep a copy of the filter around, just in case.
     */
    private String filter = null;

    /**
     * Return the last retrieved filter.
     * @return The filter, or null if there was none.
     */
    public String getFilter() {
        return filter;
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
        String get = "GET /credentials/authenticate?src_api_key=" +
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

    /**
     * Get the current filter settings the seed bank is using.
     * @return A String containing the aforementioned settings, or null on error.
     */
    public String get_categories() {

        // see authorize() for a detailed breakdown
        String filter = null;

        String get = "GET /filtered/get_categories HTTP/1.0\n" +
                "Host: " + this.host + "\n\n";
        ByteBuffer get_bytes = ByteBuffer.wrap(get.getBytes(utf8));

        try (SocketChannel channel = SocketChannel.open()) {

            channel.connect( new InetSocketAddress(this.host, this.port) );

            ClientTlsChannel.Builder builder = ClientTlsChannel.newBuilder(channel, this.ssl_context);
            try (TlsChannel tlsChannel = builder.build()) {

                tlsChannel.write( get_bytes );

                // read in the remote side and tidy it up
                ByteBuffer result = ByteBuffer.allocate(4096);
                while (tlsChannel.read(result) != -1) {}
                result.flip();

                filter = utf8.decode(result).toString();
            }
        } catch (IOException e) {
            return null;
        }

        // prevent clobbering the existing filter
        if( filter != null ) {
            this.filter = filter;
            return filter;
        }
        else
            return null;
    }

    /**
     * Send off a seed to the bank, simultaneously retrieving the next type of seed to work on.
     * @return A String containing the aforementioned filter settings.
     */
    public String submit( long seed ) {

        // same basic idea as the prior ones
        String filter = null;

        // convert the seed to a string. Kudos: https://stackoverflow.com/a/4485196
        ByteBuffer seed_buf = ByteBuffer.allocate( Long.BYTES );
        seed_buf.putLong( seed );
        String seed_string = utf8.decode( Base64.getEncoder().encode( seed_buf ) ).toString();

        // this time we use POST instead of GET
        String post = "POST /filtered/submit HTTP/1.0\n" +
                "Host: " + this.host + "\n" +
                "Content-Type: multipart/form-data; boundary=\"-----------\"\n" +
                "\n" +
                "-------------\n" +
                "Content-type: application/json; charset=utf-8\n" +
                "\n" +
                "{user_token: \"" + this.auth_token.toString() + "\"," +
                " settings_hash: \"" + this.filter + "\"," +
                " seed: \"" + seed_string + "\"}\n" +
                "\n" +
                "---------------";

        ByteBuffer post_bytes = ByteBuffer.wrap(post.getBytes(utf8));

        try (SocketChannel channel = SocketChannel.open()) {

            channel.connect( new InetSocketAddress(this.host, this.port) );

            ClientTlsChannel.Builder builder = ClientTlsChannel.newBuilder(channel, this.ssl_context);
            try (TlsChannel tlsChannel = builder.build()) {

                tlsChannel.write( post_bytes );

                // read in the remote side and tidy it up
                ByteBuffer result = ByteBuffer.allocate(4096);
                while (tlsChannel.read(result) != -1) {}
                result.flip();

                filter = utf8.decode(result).toString();
            }
        } catch (IOException e) {
            return null;
        }

        if( filter != null ) {
            this.filter = filter;
            return filter;
        }
        else
            return null;
    }

}
