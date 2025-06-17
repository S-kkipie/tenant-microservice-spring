package unsa.sistemas.tenantservice.Config;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    public static void set(UserContext context) {
        userContext.set(context);
    }

    public static UserContext get() {
        return userContext.get();
    }

    public static void clear() {
        userContext.remove();
    }
}
