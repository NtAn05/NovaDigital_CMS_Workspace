package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ============================================================
 * JwtAuthenticationFilter - Bộ lọc xác thực Token JWT
 * ============================================================
 * Kế thừa OncePerRequestFilter để đảm bảo bộ lọc này CHỈ CHẠY 1 LẦN DUY NHẤT
 * cho mỗi HTTP Request gửi đến hệ thống.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider; // Tiêm Bean xử lý mã hóa & giải mã JWT

    @Autowired
    private CustomUserDetailsService customUserDetailsService; // Tiêm Bean lấy thông tin User từ Database

    /**
     * Phương thức đánh chặn và xử lý kiểm tra Token JWT cho mọi HTTP Request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Bước 1: Trích xuất chuỗi JWT Token từ Header 'Authorization' của Request
            String jwt = getJwtFromRequest(request);

            // Bước 2: Kiểm tra Token hợp lệ (không rỗng và đúng chữ ký HMAC-SHA256)
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // Bước 3: Giải mã lấy Username từ thông tin Token
                String username = tokenProvider.getUsernameFromJWT(jwt);

                // Bước 4: Lấy thông tin chi tiết người dùng và danh sách quyền từ Database
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                
                // Bước 5: Tạo đối tượng xác thực Spring Security
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Bước 6: Lưu đối tượng xác thực vào SecurityContextHolder (Ghi nhận request đã ĐĂNG NHẬP)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Không thể thiết lập xác thực người dùng trong SecurityContext", ex);
        }

        // Bước 7: Chuyển giao Request sang Filter tiếp theo trong chuỗi xử lý
        filterChain.doFilter(request, response);
    }

    /**
     * Hàm phụ trợ trích xuất JWT Token từ Header 'Authorization'
     * Định dạng chuẩn: Authorization: Bearer <token_string>
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Kiểm tra nếu Header chứa chuỗi bắt đầu bằng 'Bearer ' thì cắt lấy phần token phía sau
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

