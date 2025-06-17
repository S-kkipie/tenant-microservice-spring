package unsa.sistemas.tenantservice.Utils;

public class URLUtil {
    public static String generateUrl(String urlBase, int port){
        return urlBase + ":" + port;
    }
}
