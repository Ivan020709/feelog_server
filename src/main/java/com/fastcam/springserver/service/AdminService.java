package com.fastcam.springserver.service;

import com.fastcam.springserver.dto.Paging;
import com.fastcam.springserver.entity.*;
import com.fastcam.springserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Service
@Transactional
public class AdminService {

    @Autowired
    AdminRepository ar;

    @Autowired
    BoardRepository br;

    @Autowired
    MemberRepository mr;

    @Autowired
    AdminActivityLogRepository aalr;

    @Autowired
    AdminErrorRepository aer;

    @Autowired
    MemberRoleRepository mrr;

    @Autowired
    InquiryRepository ir;

    @Autowired
    InquiryCommentRepository icr;

    /** 관리자만 회원 목록을 조회합니다. 비밀번호와 SNS 식별값은 응답에서 제외합니다. */
    public List<HashMap<String, Object>> getMemberList(String adminEmail) {
        MemberRole adminRole = mrr.findByEmail(adminEmail);
        if (adminRole == null || adminRole.getRole() == null
                || !adminRole.getRole().toUpperCase().contains("ADMIN")) {
            throw new IllegalArgumentException("관리자만 회원 목록을 조회할 수 있습니다.");
        }

        List<HashMap<String, Object>> result = new ArrayList<>();
        for (Member member : mr.findAll(Sort.by(Sort.Direction.DESC, "userid"))) {
            HashMap<String, Object> item = new LinkedHashMap<>();
            item.put("userid", member.getUserid());
            item.put("name", member.getName());
            item.put("nickname", member.getNickname());
            item.put("email", member.getEmail());
            item.put("phone", member.getPhone());
            item.put("provider", member.getProvider());
            item.put("indate", member.getIndate());
            MemberRole role = mrr.findByEmail(member.getEmail());
            item.put("role", role == null ? "USER" : role.getRole());
            result.add(item);
        }
        return result;
    }

    public void getReport(AdminReport areport) {
        Board board = br.findByBoardnum(areport.getBoardnum());
        Member member = mr.findByUserid(board.getUserid());
        if(member==null){
            areport.setCriminal("탈퇴한 회원");
        }
        areport.setCriminal(member.getNickname());
        ar.save(areport);
    }


    public HashMap<String, Object> getReportList(int page) {
        HashMap<String, Object> result = new HashMap<String, Object>();

        Paging paging = new Paging();
        paging.setPage(page);
        List<AdminReport> list = ar.findAll();
        int count = list.size();

        paging.setTotalCount(count);
        paging.calPaing();
        paging.setDisplayPage(10);
        paging.setDisplayRow(10);

        Pageable pageable = PageRequest.of(page-1, paging.getDisplayRow(),
                Sort.by(Sort.Direction.DESC, "reportnum"));

        Page<AdminReport> pageList = ar.findAll(pageable);
        // Page<Qna> 에서 필요한 목록을 꺼냅니다.
        List<AdminReport> list2 = pageList.getContent();
        result.put("reportList", list2);
        result.put("paging", paging);

        return  result;
    }

    public void deleteReport(int reportnum, String adminid, String adminname, int boardnum) {
        AdminReport areport = ar.findByReportnum(reportnum);
        Board board = br.findByBoardnum(boardnum);
        br.delete(board);
        ar.delete(areport);

        AdminActivityLog log = new AdminActivityLog();

        log.setAdminid(adminid);
        log.setAdminname(adminname);
        log.setActivity("게시글 삭제");
        log.setTarget("게시글 #" + reportnum);
        log.setMethod("DELETE");
        log.setApi("/api/board/deleteBoard");
        log.setResult("SUCCESS");

        aalr.save(log);
    }

    public List<AdminActivityLog> getAdminActivityLog() {

        System.out.println("===== 활동 로그 조회 시작 =====");

        List<AdminActivityLog> list =
                aalr.findAll(Sort.by(Sort.Direction.DESC, "indate"));

        System.out.println("조회된 로그 개수 : " + list.size());

        return list;
    }

    public AdminError checkerror(int errornum) {
        AdminError aerror = aer.findByErrornum(errornum);
        aerror.setState("Y");
        return aerror;

    }
    public boolean checkAdminCode(int userid, String code) {

        System.out.println("===== checkAdminCode 시작 =====");
        System.out.println("userid : " + userid);
        System.out.println("입력 code : [" + code + "]");

        // 관리자 코드 확인
        if (!"GJ8S-WTJ3-MRW6-FKSL".equals(code)) {

            System.out.println("❌ 관리자 코드 불일치");

            return false;
        }

        System.out.println("✅ 관리자 코드 일치");


        // 회원 조회
        Member member = mr.findByUserid(userid);

        System.out.println("회원 조회 결과 : " + member);

        if (member == null) {

            System.out.println("❌ 회원을 찾을 수 없음");

            return false;
        }

        System.out.println("회원 이메일 : " + member.getEmail());


        // 회원의 역할 조회
        MemberRole memberRole = mrr.findByEmail(member.getEmail());

        System.out.println("MemberRole 조회 결과 : " + memberRole);


        // 역할 정보가 없으면 새로 생성
        if (memberRole == null) {

            memberRole = new MemberRole();

            memberRole.setEmail(member.getEmail());

            System.out.println("MemberRole 신규 생성");

        }


        // 관리자 권한 부여
        memberRole.setRole("ADMIN");

        mrr.save(memberRole);

        System.out.println("✅ 관리자 권한 부여 완료");

        return true;
    }


    public HashMap<String, Object> getAdmin(String email) {

        HashMap<String, Object> map = new HashMap<>();
        MemberRole memberRole = mrr.findByEmail(email);
        if (memberRole != null) {
            map.put("role", memberRole.getRole());
        } else {
            map.put("role", "USER");
        }

        return map;
    }

    public void insertAnswer(int inquirynum, String nickname, String content) {
        Inquiry inquiry  = ir.findByInquirynum(inquirynum);

        InquiryComment ic = new InquiryComment();

        ic.setInquiryId(inquiry.getInquirynum());
        ic.setNickname(nickname);
        ic.setContent(content);
        icr.save(ic);
        inquiry.setStatus("답변완료");
        ir.save(inquiry);
    }

    public InquiryComment getAdminAnswer(int inquiryId) {
        return icr.findFirstByInquiryIdOrderByCreatedAtDesc(inquiryId);
    }

}
