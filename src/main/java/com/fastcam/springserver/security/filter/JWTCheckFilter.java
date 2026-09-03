package com.fastcam.springserver.security.filter;

import com.fastcam.springserver.security.util.JWTException;
import com.fastcam.springserver.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class JWTCheckFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        System.out.println("=================================");
        System.out.println("JWT FILTER");
        System.out.println("URI : " + request.getRequestURI());
        System.out.println("Authorization : " + authHeader);
        System.out.println("=================================");

        System.out.println("JWT CHECK FILTER");

        System.out.println("Authorization : " + authHeader);

        // Authorization 헤더가 없으면 인증 실패
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"message\":\"Access Token이 없습니다.\"}");
            return;
        }

        String accessToken = authHeader.substring(7);

        try {

            // JWT 검증
            Map<String, Object> claims =
                    JWTUtil.validateToken(accessToken);

            System.out.println("JWT 검증 성공");
            System.out.println("claims = " + claims);

            // 검증 성공했으므로 다음 필터/Controller로 이동
            filterChain.doFilter(request, response);

        } catch (JWTException e) {

            System.out.println("JWT 검증 실패 : " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"message\":\"유효하지 않은 Access Token입니다.\"}"
            );
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        System.out.println("check uri................" + path);

        // 카카오 로그인 및 회원가입
        if(path.startsWith("/member/kakaostart"))
            return true;
        if(path.startsWith("/member/kakaoLogin"))
            return true;
        if(path.startsWith("/member/getLoginUser"))
            return true;
        if(path.startsWith("/member/updateKakaoMember"))
            return true;
        if(path.startsWith("/favicon.ico"))
            return true;

        // 아이디/비밀번호 찾기
        if(path.startsWith("/member/findId"))
            return true;
        if(path.startsWith("/member/findPwd"))
            return true;
        if(path.startsWith("/member/updatePwd"))
            return true;
        if(path.startsWith("/sms/sendSMS")) {
            return true;
        }
        if(path.startsWith("/sms/confirmNumber")) {
            return true;
        }

        // 회원가입 및 로그인
        if(path.startsWith("/member/login"))
            return true;
        if(path.startsWith("/member/emailCheck"))
            return true;
        if(path.startsWith("/member/nicknameCheck"))
            return true;
        if(path.startsWith("/member/fileupload"))
            return true;
        if(path.startsWith("/member/insertMember"))
            return true;
        if(path.startsWith("/images"))
            return true;

        // 토큰 재발급
        if(path.startsWith("/member/refresh"))
            return true;

        // 게시글 조회수 증가
        if(path.startsWith("/board/plusCount"))
            return true;

        // 브라우저 CORS 사전 요청
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()))
            return true;

        // 자유게시판 공개 조회
        if (path.startsWith("/board/getBoardList"))
            return true;

        if (path.startsWith("/board/getBoard/"))
            return true;

        // 자유게시판 댓글 공개 조회
        if ("GET".equalsIgnoreCase(request.getMethod())
                && path.matches("/board/\\d+/comments"))
            return true;

        // 공지사항 공개 조회
        if (path.startsWith("/notice/getNoticeList"))
            return true;

        if (path.startsWith("/notice/getNotice/"))
            return true;

        if (path.startsWith("/notice/plusCount"))
            return true;

        // 로그인 전에도 발생할 수 있는 프론트 오류 기록
        if (path.startsWith("/admin/errorLog"))
            return true;

        // 문의사항 목록은 비회원도 조회 가능
        if (path.startsWith("/inquiry/getInquiryList"))
            return true;

        // 문의사항 상세 내용은 비회원도 조회 가능
        if (path.startsWith("/inquiry/getInquiry/"))
            return true;

        // 문의사항에 작성된 관리자 답변은 비회원도 조회 가능
        if (path.startsWith("/admin/getAdminAnswer"))
            return true;

        if (path.startsWith("/member/conFirmCode"))
            return true;

        // 상점 상품과 친밀도 랭킹은 비회원도 조회할 수 있습니다.
        if ("GET".equalsIgnoreCase(request.getMethod())
                && path.startsWith("/item/"))
            return true;
        if ("GET".equalsIgnoreCase(request.getMethod())
                && path.startsWith("/affinity/ranking"))
            return true;

        return false;
    }
}
