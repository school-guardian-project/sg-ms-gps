package gps_backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    public static final String USER_ROLE_NAME_HEADER = "X-User-Role-Name";
    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_PERMISSIONS_HEADER = "X-User-Permissions";
    public static final String CAMPUS_ID_HEADER = "X-Campus-Id";
    public static final String SCHOOL_ID_HEADER = "X-School-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        String roleId = request.getHeader(USER_ROLE_HEADER);
        if (roleId != null && !roleId.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleId));
        }
        String roleName = request.getHeader(USER_ROLE_NAME_HEADER);
        if (roleName != null && !roleName.isBlank()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        }

        String permissions = request.getHeader(USER_PERMISSIONS_HEADER);
        if (permissions != null && !permissions.isBlank()) {
            String[] perms = permissions.split(",");
            for (String perm : perms) {
                String p = perm.trim();
                if (!p.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(p));
                }
            }
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
