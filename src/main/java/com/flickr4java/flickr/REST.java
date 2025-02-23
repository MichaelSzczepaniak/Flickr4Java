
package com.flickr4java.flickr;

import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.uploader.Payload;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.flickr4java.flickr.util.DebugInputStream;
import com.flickr4java.flickr.util.IOUtilities;
import com.flickr4java.flickr.util.OAuthUtilities;
import com.flickr4java.flickr.util.UrlUtilities;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Parameter;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Transport implementation using the REST interface.
 *
 * @author Anthony Eden
 * @version $Id: REST.java,v 1.26 2009/07/01 22:07:08 x-mago Exp $
 */
public class REST extends Transport {

    private static final Logger logger = LoggerFactory.getLogger(REST.class);

    private static final String PATH = "/services/rest/";

    /**
     * Error code from Flickr API when the service is unavailable.
     */
    private static final String FLICKR_SERVICE_UNAVAILABLE = "105";

    private boolean proxyAuth = false;

    private String proxyUser = "";

    private String proxyPassword = "";

    private Integer connectTimeoutMs;

    private Integer readTimeoutMs;

    /**
     * Construct a new REST transport instance.
     */
    public REST() {
        setTransportType(REST);
        setHost(API_HOST);
        setPath(PATH);
        setScheme(DEFAULT_SCHEME);
        setResponseClass(RESTResponse.class);
    }

    /**
     * Construct a new REST transport instance using the specified host endpoint.
     *
     * @param host The host endpoint
     */
    public REST(String host) {
        this();
        setHost(host);
    }

    /**
     * Construct a new REST transport instance using the specified host and port endpoint.
     *
     * @param host The host endpoint
     * @param port The port
     */
    public REST(String host, int port) {
        this();
        setHost(host);
        setPort(port);
    }

    /**
     * Set a proxy for REST-requests.
     *
     * @param proxyHost
     * @param proxyPort
     */
    public void setProxy(String proxyHost, int proxyPort) {
        System.setProperty("http.proxySet", "true");
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", "" + proxyPort);
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", "" + proxyPort);
    }

    /**
     * Set a proxy with authentication for REST-requests.
     *
     * @param proxyHost
     * @param proxyPort
     * @param username
     * @param password
     */
    public void setProxy(String proxyHost, int proxyPort, String username, String password) {
        setProxy(proxyHost, proxyPort);
        proxyAuth = true;
        proxyUser = username;
        proxyPassword = password;
    }

    /**
     * Invoke an HTTP GET request on a remote host. You must close the InputStream after you are done with.
     *
     * @param path       The request path
     * @param parameters The parameters (collection of Parameter objects)
     * @return The Response
     */
    @Override
    public com.flickr4java.flickr.Response get(String path, Map<String, Object> parameters, String apiKey, String sharedSecret) throws FlickrException {

        OAuthRequest request = new OAuthRequest(Verb.GET, buildUrl(path));
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            request.addQuerystringParameter(entry.getKey(), String.valueOf(entry.getValue()));
        }

        if (proxyAuth) {
            request.addHeader("Proxy-Authorization", "Basic " + getProxyCredentials());
        }

        RequestContext requestContext = RequestContext.getRequestContext();
        Auth auth = requestContext.getAuth();
        OAuth10aService service = OAuthUtilities.createOAuthService(apiKey, sharedSecret, connectTimeoutMs, readTimeoutMs);
        if (auth != null) {
            OAuth1AccessToken requestToken = new OAuth1AccessToken(auth.getToken(), auth.getTokenSecret());
            service.signRequest(requestToken, request);
        } else {
            // For calls that do not require authorization e.g. flickr.people.findByUsername which could be the
            // first call if the user did not supply the user-id (i.e. nsid).
            if (!parameters.containsKey(Flickr.API_KEY)) {
                request.addQuerystringParameter(Flickr.API_KEY, apiKey);
            }
        }

        if (Flickr.debugRequest) {
            logger.debug("GET: " + request.getCompleteUrl());
        }

        try {
            return handleResponse(request, service);
        } catch (ReflectiveOperationException | SAXException | IOException | InterruptedException | ExecutionException | ParserConfigurationException e) {
            throw new FlickrRuntimeException(e);
        }
    }

    /**
     * Invoke an HTTP POST request on a remote host.
     *
     * @param path       The request path
     * @param parameters The parameters (collection of Parameter objects)
     * @return The Response object
     */
    @Override
    public com.flickr4java.flickr.Response post(String path, Map<String, Object> parameters, String apiKey, String sharedSecret) throws FlickrException {

        OAuthRequest request = OAuthUtilities.buildNormalPostRequest(parameters, buildUrl(path));

        OAuth10aService service = createAndSignRequest(apiKey, sharedSecret, request);

        try {
            return handleResponse(request, service);
        } catch (ReflectiveOperationException | InterruptedException | ExecutionException | IOException | SAXException | ParserConfigurationException e) {
            throw new FlickrRuntimeException(e);
        }
    }

    /**
     * Invoke an HTTP POST request on a remote host.
     *
     * @param path     The request path
     * @param metaData The parameters (collection of Parameter objects)
     * @param payload
     * @return The Response object
     */
    @Override
    public com.flickr4java.flickr.Response postMultiPart(String path, UploadMetaData metaData, Payload payload, String apiKey, String sharedSecret) throws FlickrException {

        Map<String, String> uploadParameters = new HashMap<>(metaData.getUploadParameters());
        OAuthRequest request = OAuthUtilities.buildMultipartRequest(uploadParameters, buildUrl(path));

        OAuth10aService service = createAndSignRequest(apiKey, sharedSecret, request);

        // Ensure all parameters (including oauth) are added to payload so signature matches
        uploadParameters.putAll(request.getOauthParameters());

        request.addFileByteArrayBodyPartPayloadInMultipartPayload(payload.getPayload(), "photo", metaData.getFilename());
        uploadParameters.entrySet().forEach(e ->
                request.addFileByteArrayBodyPartPayloadInMultipartPayload(null, e.getValue().getBytes(), e.getKey()));

        try {
            return handleResponse(request, service);
        } catch (ReflectiveOperationException | InterruptedException | ExecutionException | IOException | SAXException | ParserConfigurationException e) {
            throw new FlickrRuntimeException(e);
        }
    }

    private OAuth10aService createAndSignRequest(String apiKey, String sharedSecret, OAuthRequest request) {
        OAuth10aService service = OAuthUtilities.createOAuthService(apiKey, sharedSecret, connectTimeoutMs, readTimeoutMs);
        OAuthUtilities.signRequest(service, request, proxyAuth ? getProxyCredentials() : null);
        return service;
    }

    private String buildUrl(String path) {
        return String.format("%s://%s%s", getScheme(), getHost(), path);
    }

    private Response handleResponse(OAuthRequest request, OAuth10aService service) throws InterruptedException, ExecutionException, IOException, SAXException, ParserConfigurationException, FlickrException, ReflectiveOperationException {
        com.github.scribejava.core.model.Response scribeResponse = service.execute(request);

        if (!scribeResponse.isSuccessful()) {
            throw new FlickrException(FLICKR_SERVICE_UNAVAILABLE, String.format("Received '%s' error from Flickr with status %d", scribeResponse.getMessage(), scribeResponse.getCode()));
        }
        String strXml = scribeResponse.getBody().trim();
        if (Flickr.debugStream) {
            logger.debug(strXml);
        }
        if (strXml.startsWith("oauth_problem=")) {
            throw new FlickrRuntimeException(strXml);
        }

        DocumentBuilder builder = getDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(strXml)));
        Response f4jResponse = (Response) responseClass.getConstructor().newInstance();
        f4jResponse.parse(document);

        // Enable this method to update the test payloads
        // dumpResponseToFile(request, strXml);

        return f4jResponse;
    }

    /**
     * Invoke a non OAuth HTTP GET request on a remote host.
     * <p>
     * This is only used for the Flickr OAuth methods checkToken and getAccessToken.
     *
     * @param path       The request path
     * @param parameters The parameters
     * @return The Response
     */
    @Override
    public Response getNonOAuth(String path, Map<String, String> parameters) {
        InputStream in = null;
        try {
            URL url = UrlUtilities.buildUrl(getScheme(), getHost(), getPort(), path, parameters);
            if (Flickr.debugRequest) {
                logger.debug("GET: " + url);
            }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            if (proxyAuth) {
                conn.setRequestProperty("Proxy-Authorization", "Basic " + getProxyCredentials());
            }
            setTimeouts(conn);
            conn.connect();

            if (Flickr.debugStream) {
                in = new DebugInputStream(conn.getInputStream(), System.out);
            } else {
                in = conn.getInputStream();
            }

            Response response;
            DocumentBuilder builder = getDocumentBuilder();
            Document document = builder.parse(in);
            response = (Response) responseClass.newInstance();
            response.parse(document);

            return response;
        } catch (IllegalAccessException | SAXException | IOException | InstantiationException | ParserConfigurationException e) {
            throw new FlickrRuntimeException(e);
        } finally {
            IOUtilities.close(in);
        }
    }

    public boolean isProxyAuth() {
        return proxyAuth;
    }

    /**
     * Generates Base64-encoded credentials from locally stored username and password.
     *
     * @return credentials
     */
    public String getProxyCredentials() {
        return new String(Base64.getEncoder().encode((proxyUser + ":" + proxyPassword).getBytes()));
    }

    private void setTimeouts(HttpURLConnection conn) {
        if (connectTimeoutMs != null) {
            conn.setConnectTimeout(connectTimeoutMs);
        }
        if (readTimeoutMs != null) {
            conn.setReadTimeout(readTimeoutMs);
        }
    }

    public void setConnectTimeoutMs(Integer connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public void setReadTimeoutMs(Integer readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        return builderFactory.newDocumentBuilder();
    }

    // Generate responses for offline tests

    private void dumpResponseToFile(OAuthRequest request, String strXml) throws IOException {
        Verb verb = request.getVerb();
        Optional<String> flickrMethod = Optional.empty();
        switch (verb) {
            case GET:
                flickrMethod = request.getQueryStringParams().getParams().stream().filter(param -> param.getKey().equals("method")).findFirst().map(Parameter::getValue);
                break;
            case POST:
                 flickrMethod = request.getBodyParams().getParams().stream().filter(param -> param.getKey().equals("method")).findFirst().map(Parameter::getValue);
                break;
            default:  // SpotBugs Issue 6
                throw new IllegalStateException("Unexpected value: " + verb);
        }
        if (flickrMethod.isPresent()) {
            String filename = String.format("%s.xml", flickrMethod.get());
            Path filePath = Paths.get("src/test/resources/payloads/" + verb, filename);
            Files.write(filePath, strXml.getBytes("UTF-16"));
            logger.info(String.format("Writing payload to file '%s'", filePath));
        } else {
            logger.warn("Not dumping response to file as method not found in request for URL {}", request.getUrl());
        }
    }
}
