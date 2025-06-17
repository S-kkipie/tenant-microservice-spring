package unsa.sistemas.tenantservice.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import unsa.sistemas.tenantservice.Models.Role;

import java.io.IOException;

@Component
public class HeaderContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws IOException {
        String username = request.getHeader("X-User-Name");
        String role = request.getHeader("X-User-Role");
        Role roleEnum = Role.valueOf(role);

        if (username == null || role == null || roleEnum == Role.ROLE_ADMIN || roleEnum == Role.ROLE_USER) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: Missing authentication headers");
            response.getWriter().flush();
            return false;
        }

        UserContext context = new UserContext(username, role);
        UserContextHolder.set(context);

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull  Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
