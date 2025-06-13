package unsa.sistemas.tenantservice.Utils;

public class URLUtil {
    public static String generateUrl(String urlBase, int port, String database){
        return urlBase + ":" + port + "/" + database;
    }
}
