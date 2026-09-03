package com.fastcam.springserver.controller;

import com.fastcam.springserver.dto.KakaoProfile;
import com.fastcam.springserver.dto.OAuthToken;
import com.fastcam.springserver.entity.Member;
import com.fastcam.springserver.security.util.JWTException;
import com.fastcam.springserver.security.util.JWTUtil;
import com.fastcam.springserver.service.MemberService;
import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    MemberService ms;

//    @PostMapping("/login")
//    public HashMap<String, Object>login(
//            @RequestParam("email") String email,
//            @RequestParam("pwd") String pwd
//    ){
//        HashMap<String, Object> map = new HashMap<String, Object>();
//        Member mdto = ms.getEmail(email);
//
//        if(mdto ==null){
//            map.put("msg","notOK" );
//            return map;
//        } else if(!mdto.getPwd().equals(pwd)){
//            map.put("msg","notOK");
//            return map;
//        }else{
//            map.put("msg", "OK");
//            map.put("loginUser",mdto);
//        }
//        return map;
//    }

//    @GetMapping("/getMember")
//    public HashMap<String, Object>getMember(@RequestParam("snsid") String snsid){
//        HashMap<String, Object> map = new HashMap<>();
//        ms.getSnsid(snsid);
//        map.put("msg","OK");
//        return map;
//    }

    @PostMapping("/insertMember")
    public HashMap<String, Object>join(@RequestBody Member member){
        HashMap<String, Object> map = new HashMap<>();
        ms.insertMember(member);
        map.put("msg", "OK");
        return map;

    }

    // 이메일마다 서로 다른 인증번호를 저장합니다.
    // 여러 사용자가 동시에 인증번호를 요청해도 서로의 번호가 덮어써지지 않습니다.
    private final Map<String, Integer> emailCodeMap = new ConcurrentHashMap<>();

    @PostMapping("/emailCheck")
    public HashMap<String, Object>emailCheck(@RequestParam ("email") String email){
        HashMap<String, Object>map = new HashMap<>();
        Member mdto  = ms.getEmail(email);
        if(mdto==null){
            int number = ms.sendMail(email);
            emailCodeMap.put(email.trim().toLowerCase(), number);
            map.put("msg", "OK");
        }else{
            map.put("msg","notOK");
        }
        return map;

    }

    @PostMapping("/nicknameCheck")
    public HashMap<String, Object> nicknamecheck( @RequestParam("nickname") String nickname){
        HashMap<String, Object> map = new HashMap<String, Object>();
        Member mdto = ms.getNickname(nickname);
        if( mdto == null )
            map.put("msg", "OK");
        else
            map.put("msg", "notOK");
        return map;
    }


    @Autowired
    ServletContext sc;

    @PostMapping("/fileupload")
    public HashMap<String, Object> fileupload(@RequestParam("image") MultipartFile file){
        HashMap<String, Object> map = new HashMap<String, Object>();
        String path = sc.getRealPath("/images");
        Calendar today = Calendar.getInstance();
        long dt = today.getTimeInMillis();
        String filename = file.getOriginalFilename();
        String f1 = filename.substring(0, filename.lastIndexOf("."));
        String f2 = filename.substring(filename.lastIndexOf("."));
        String uploadPath = path + "/" + f1 + dt + f2;
        try {
            file.transferTo( new File(uploadPath) );
            map.put("savefilename", f1 + dt + f2);
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
        return map;
    }


    @Value("${kakao.client_id}")
    private String client_id;
    @Value("${kakao.redirect_uri}")
    private String redirect_uri;

    @GetMapping("/kakaostart")
    private @ResponseBody String kakaostart(){
        String a = "<script type='text/javascript'>" +
                "location.href='https://kauth.kakao.com/oauth/authorize?" +
                "client_id=" + client_id +
                "&redirect_uri=" + redirect_uri +
                "&response_type=code'" + "</script>";
        return a;
    }

    @GetMapping("/kakaoLogin")
    public void kakaoLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String endpoint = "https://kauth.kakao.com/oauth/token";
        URL url = new URL(endpoint);
        String bodyData = "grant_type=authorization_code&";
        bodyData += "client_id=" + client_id + "&";
        bodyData += "redirect_uri=" + redirect_uri + "&";
        bodyData += "code=" + code;

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        conn.setDoOutput(true);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
        bw.write(bodyData);
        bw.flush();
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        String input = "";
        StringBuilder sb = new StringBuilder();
        while ((input = br.readLine()) != null) {
            sb.append(input);
        }
        Gson gson = new Gson();
        OAuthToken oAuthToken = gson.fromJson(sb.toString(), OAuthToken.class);
        String endpoint2 = "https://kapi.kakao.com/v2/user/me";
        URL url2 = new URL(endpoint2);

        HttpsURLConnection conn2 = (HttpsURLConnection) url2.openConnection();
        conn2.setRequestProperty("Authorization", "Bearer " + oAuthToken.getAccess_token());
        conn2.setDoOutput(true);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(conn2.getInputStream(), "UTF-8"));
        String input2 = "";
        StringBuilder sb2 = new StringBuilder();
        while ((input2 = br2.readLine()) != null) {
            sb2.append(input2);
            //System.out.println(input2);
        }

        Gson gson2 = new Gson();
        KakaoProfile kakaoProfile = gson2.fromJson(sb2.toString(), KakaoProfile.class);
        KakaoProfile.KakaoAccount ac = kakaoProfile.getAccount();
        KakaoProfile.KakaoAccount.Profile pf = ac.getProfile();

        System.out.println("id : " + kakaoProfile.getId());
        System.out.println("Profile-Nickname : " + ac.getProfile().getNickname());
        System.out.println("Profile-pfimg : " + pf.getProfile_image_url());

        Member mdto = ms.getMemberBySnsid( kakaoProfile.getId() );
        if( mdto == null){
            mdto = new Member();

            mdto.setEmail( kakaoProfile.getId() );
            mdto.setSnsid( kakaoProfile.getId() );
            mdto.setName(ac.getProfile().getNickname());
            mdto.setNickname( ac.getProfile().getNickname() );
            mdto.setProvider("KAKAO");

            ms.insertKakaoMember(mdto);
            mdto = ms.getMemberBySnsid( kakaoProfile.getId() );
        }
        response.sendRedirect("http://localhost:3000/savekakaoinfo/" + mdto.getUserid());

    }

    @PostMapping("/updateMember")
    public HashMap<String, Object> updateMember(@RequestBody Member member ){
        HashMap<String, Object> map = new HashMap<>();
        ms.updateMember(member);
        map.put("updateMember", member);
        return map;

    }

    @GetMapping("/getEmail")
    public HashMap<String, Object> getEmail(@RequestParam("email") String email){

        HashMap<String, Object> map = new HashMap<>();

        Member member = ms.getEmail(email);

        if (member != null) {
            map.put("msg", "OK");
            map.put("member", member);
        } else {
            map.put("msg", "notOK");
        }

        return map;
    }

    // 카카오 로그인 회원에게도 일반 로그인과 같은 JWT를 발급한다.
    private HashMap<String, Object> makeKakaoLoginUser(Member member) {
        HashMap<String, Object> loginUser = new HashMap<>();

        // 프론트 Redux와 쿠키에서 사용하는 회원정보
        loginUser.put("userid", member.getUserid());
        loginUser.put("name", member.getName());
        loginUser.put("nickname", member.getNickname());
        loginUser.put("email", member.getEmail());
        loginUser.put("phone", member.getPhone());
        loginUser.put("savefilename", member.getSavefilename());
        loginUser.put("zip_num", member.getZip_num());
        loginUser.put("address1", member.getAddress1());
        loginUser.put("address2", member.getAddress2());
        loginUser.put("address3", member.getAddress3());
        loginUser.put("provider", member.getProvider());
        loginUser.put("snsid", member.getSnsid());
        loginUser.put("editcom", member.getEditcom());
        loginUser.put("role", "USER");

        // accessToken은 60분, refreshToken은 1일 동안 사용한다.
        String accessToken = JWTUtil.generateToken(loginUser, 60);
        String refreshToken = JWTUtil.generateToken(loginUser, 60 * 24);

        loginUser.put("accessToken", accessToken);
        loginUser.put("refreshToken", refreshToken);

        return loginUser;
    }

    @PostMapping("/updateKakaoMember")
    public HashMap<String,Object> updateKakaoMember(@RequestBody Member member){
        HashMap<String, Object> map = new HashMap<>();
        int userId = member.getUserid();
        ms.updateKakaoMember(member, userId);
        map.put("msg", "OK");
        Member loginMember = ms.getMemberByUserid(userId);
        map.put("loginUser", makeKakaoLoginUser(loginMember));
        return map;
    }

    @GetMapping("/getLoginUser")
    public HashMap<String, Object> getLoginUser(@RequestParam("userid") int userid){
        HashMap<String, Object> map = new HashMap<String, Object>();
        Member loginMember = ms.getMemberByUserid(userid);
        map.put("loginUser", makeKakaoLoginUser(loginMember));
        return map;
    }

    @DeleteMapping("/deleteMember")
    public HashMap<String, Object> deleteMember(@RequestParam("email") String email){
        HashMap<String, Object> map = new HashMap<>();
        ms.deleteMember(email);
        map.put("msg","OK");
        return map;
    }

    @PostMapping("/findId")
    public HashMap<String, Object> findId(
            @RequestBody HashMap<String, String> data
    ) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        String name = data.get("name");
        String phone = data.get("phone");

        Member member = ms.findId(name, phone);

        if (member == null) {
            map.put("msg", "notOK");
        } else {
            map.put("msg", "ok");

            // 프론트가 result.data.userid를 사용하고 있어서
            // userid라는 이름으로 이메일을 보내준다.
            map.put("userid", member.getEmail());
        }

        return map;
    }

    @PostMapping("/findPwd")
    public HashMap<String, Object> findPwd(
            @RequestBody HashMap<String, String> data
    ) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        // 프론트에서는 이메일을 userid라는 이름으로 보낸다.
        String email = data.get("userid");
        String name = data.get("name");
        String phone = data.get("phone");

        Member member = ms.findPwd(email, name, phone);

        if (member == null) {
            map.put("msg", "notOK");
        } else {
            map.put("msg", "OK");
            map.put("userid", member.getEmail());
        }

        return map;
    }

    @PostMapping("/updatePwd")
    public HashMap<String, Object> updatePwd(
            @RequestBody HashMap<String, String> data
    ) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        String email = data.get("userid");
        String password = data.get("password");

        Member member = ms.updatePwd(email, password);

        if (member == null) {
            map.put("msg", "notOK");
        } else {
            map.put("msg", "OK");
        }

        return map;
    }

    @GetMapping("/refresh/{refreshToken}")
    public HashMap<String, Object> refresh(
            @PathVariable("refreshToken") String refreshToken ,
            @RequestHeader("Authorization") String authHeader   ) throws JWTException {
        HashMap<String, Object> result = new HashMap<>();

        if( refreshToken == null || refreshToken.equals("") )
            throw  new JWTException("NULL_REFRESH");
        if( authHeader == null || authHeader.length() < 7 )
            throw new JWTException("INVALID_HEADER");

        String accessToken = authHeader.substring(7);

        // 기한 만료 체크
        boolean expiredResult = true;
        try {
            JWTUtil.validateToken( accessToken );
        } catch (JWTException e) {
            if( e.getMessage().equals("Expired") ) expiredResult=false;
        }

        if( expiredResult ){  // 유효기한 만료전
            System.out.println("토큰 유료기간 만료전... 계속 사용");
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
        }else{ // 유효기한 만료 후
            System.out.println("토큰 유료기간 만료후... 토큰 교체");
            // 리프레시 토큰에서 claims 를 추출
            Map<String, Object> claims = JWTUtil.validateToken(refreshToken);
            // 추출한 claims 로 accessToken 재발급
            String newAccessToken = JWTUtil.generateToken(claims, 1);

            String newRefreshToken = "";
            int exp = (Integer)claims.get("exp");
            java.util.Date expDate = new java.util.Date( (long)exp * (1000 ));//밀리초로 변환
            long gap = expDate.getTime() - System.currentTimeMillis();//현재 시간과의 차이 계산
            long leftMin = gap / (1000 * 60); //분단위 변환
            if(  leftMin < 60  )  // 한시간 미만으로 남았으면 토큰 교체
                newRefreshToken = JWTUtil.generateToken(claims, 60*24);
            else
                newRefreshToken = refreshToken;

            result.put("accessToken", newAccessToken);
            result.put("refreshToken", newRefreshToken);
        }
        return result;
    }





}
