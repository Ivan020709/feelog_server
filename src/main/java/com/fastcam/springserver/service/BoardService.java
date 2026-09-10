package com.fastcam.springserver.service;

import com.fastcam.springserver.dto.Paging;
import com.fastcam.springserver.entity.Board;
import com.fastcam.springserver.entity.BoardLike;
import com.fastcam.springserver.entity.BoardReport;
import com.fastcam.springserver.entity.Member;
import com.fastcam.springserver.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class BoardService {
    @Autowired
    BoardRepository br;
    @Autowired
    BoardLikeRepository blr;
    @Autowired
    BoardReportRepository brr;

    private final BoardRepository boards;
    private final BoardLikeRepository likes;
    private final BoardReportRepository reports;
    private final BoardCommentRepository comments;
    private final MemberRepository members;

    public BoardService(BoardRepository boards, BoardLikeRepository likes, BoardReportRepository reports,
                        BoardCommentRepository comments, MemberRepository members) {
        this.boards = boards;
        this.likes = likes;
        this.reports = reports;
        this.comments = comments;
        this.members = members;
    }

    public void plusCount(int boardnum) {
        Board board = requireBoard(boardnum);
        board.setViewcount(board.getViewcount() + 1);
    }

    @Transactional(readOnly = true)
    public HashMap<String, Object> getBoardList(int requestedPage, String requestedSort, Integer userId) {
        int page = Math.max(1, requestedPage);
        String sort = "likes".equalsIgnoreCase(requestedSort) ? "likes" : "latest";
        Paging paging = new Paging();
        paging.setPage(page);
        paging.setDisplayPage(5);
        paging.setDisplayRow(8);
        paging.setTotalCount((int) boards.count());
        paging.calPaing();
        List<Board> pageBoards;
        if (sort.equals("likes")) {
            List<Board> sortedBoards = boards.findAll();
            sortedBoards.sort(Comparator
                    .comparingLong((Board board) -> likes.countByBoardId(board.getBoardnum())).reversed()
                    .thenComparing(Comparator.comparingInt(Board::getBoardnum).reversed()));
            int fromIndex = Math.min((page - 1) * paging.getDisplayRow(), sortedBoards.size());
            int toIndex = Math.min(fromIndex + paging.getDisplayRow(), sortedBoards.size());
            pageBoards = sortedBoards.subList(fromIndex, toIndex);
        } else {
            Pageable pageable = PageRequest.of(page - 1, paging.getDisplayRow(),
                    Sort.by(Sort.Direction.DESC, "boardnum"));
            Page<Board> boardPage = boards.findAll(pageable);
            pageBoards = boardPage.getContent();
        }
        List<HashMap<String, Object>> enriched = pageBoards.stream()
                .map(board -> toListItem(board, userId)).toList();
        HashMap<String, Object> result = new HashMap<>();
        result.put("boardList", enriched);
        result.put("paging", paging);
        result.put("sort", sort);
        return result;
    }

    @Transactional(readOnly = true)
    public HashMap<String, Object> getBoardDetail(int boardnum, Integer userId) {
        Board board = requireBoard(boardnum);
        if (board.isIsprivate() && (userId == null || board.getUserid() != userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비공개 게시글입니다.");
        }
        HashMap<String, Object> result = new HashMap<>();
        // 게시글 원본만 보내면 프론트에는 userid만 표시됩니다.
        // 회원 이름과 댓글 수 등이 들어 있는 형태로 바꿔서 전달합니다.
        result.put("board", toListItem(board, userId));
        result.put("likeCount", likes.countByBoardId(boardnum));
        result.put("commentCount", comments.countByBoardId(boardnum));
        result.put("likedByMe", userId != null && likes.findByBoardIdAndUserId(boardnum, userId).isPresent());
        return result;
    }

    public HashMap<String, Object> toggleLike(int boardId, int userId) {
        requireBoard(boardId);
        var existing = likes.findByBoardIdAndUserId(boardId, userId);
        boolean liked;
        if (existing.isPresent()) {
            likes.delete(existing.get());
            liked = false;
        } else {
            BoardLike like = new BoardLike();
            like.setBoardId(boardId);
            like.setUserId(userId);
            try {
                likes.saveAndFlush(like);
                liked = true;
            } catch (DataIntegrityViolationException exception) {
                liked = true;
            }
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", likes.countByBoardId(boardId));
        return result;
    }

    public void reportBoard(int boardId, int reporterId, String reason, String detail) {
        Board board = requireBoard(boardId);
        if (board.getUserid() == reporterId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "본인의 게시글은 신고할 수 없습니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "신고 사유를 선택해주세요.");
        }
        if (reports.findByBoardIdAndReporterId(boardId, reporterId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 신고한 게시글입니다.");
        }
        BoardReport report = new BoardReport();
        report.setBoardId(boardId);
        report.setReporterId(reporterId);
        report.setReason(reason.trim());
        report.setDetail(detail == null ? "" : detail.trim());
        reports.save(report);
    }

    public void deleteBoard(int boardnum, int userId) {
        Board board = requireOwner(boardnum, userId);
        comments.deleteAllByBoardId(boardnum);
        likes.deleteAllByBoardId(boardnum);
        reports.deleteAllByBoardId(boardnum);
        boards.delete(board);
    }

    public Board updateBoard(Board incoming, int userId) {
        Board board = requireOwner(incoming.getBoardnum(), userId);
        board.setTitle(requireText(incoming.getTitle(), "제목"));
        board.setContent(requireText(incoming.getContent(), "내용"));
        if (incoming.getCategory() != null && !incoming.getCategory().isBlank()) {
            board.setCategory(incoming.getCategory().trim());
        }
        board.setIsprivate(incoming.isIsprivate());
        return board;
    }

    public Board insertBoard(Board board, int userId) {
        Member member = members.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 회원을 찾을 수 없습니다."));
        board.setBoardnum(0);
        board.setUserid(userId);
        board.setEmail(member.getEmail());
        board.setTitle(requireText(board.getTitle(), "제목"));
        board.setContent(requireText(board.getContent(), "내용"));
        board.setCategory(requireText(board.getCategory(), "카테고리"));
        board.setViewcount(0);
        return boards.save(board);
    }

    private HashMap<String, Object> toListItem(Board board, Integer userId) {
        HashMap<String, Object> item = new HashMap<>();
        boolean hidden = board.isIsprivate() && (userId == null || board.getUserid() != userId);
        item.put("boardnum", board.getBoardnum());
        item.put("userid", board.getUserid());

        // 게시글 작성자의 회원 정보를 찾아 실제 이름을 함께 보냅니다.
        Member writer = members.findByUserid(board.getUserid());
        item.put(
                "writerName",
                writer != null ? writer.getNickname() : "알 수 없음"
        );
        item.put("email", hidden ? "" : board.getEmail());
        item.put("title", hidden ? "비공개 게시글입니다." : board.getTitle());
        item.put("content", hidden ? "" : board.getContent());
        item.put("viewcount", board.getViewcount());
        item.put("indate", board.getIndate());
        item.put("category", board.getCategory());
        item.put("isprivate", board.isIsprivate());
        item.put("likeCount", likes.countByBoardId(board.getBoardnum()));
        item.put("commentCount", comments.countByBoardId(board.getBoardnum()));
        item.put("likedByMe", userId != null && likes.findByBoardIdAndUserId(board.getBoardnum(), userId).isPresent());
        return item;
    }

    private Board requireBoard(int boardnum) {
        Board board = boards.findByBoardnum(boardnum);
        if (board == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        return board;
    }

    private Board requireOwner(int boardnum, int userId) {
        Board board = requireBoard(boardnum);
        if (board.getUserid() != userId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 게시글만 변경할 수 있습니다.");
        }
        return board;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "을(를) 입력해주세요.");
        }
        return value.trim();
    }

    public Board getBoard(int boardnum) {
        Board board = br.findByBoardnum(boardnum);
        return board;
    }
}
