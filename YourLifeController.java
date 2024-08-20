package com.example.geungeunhanjan.controller.lifes;


import com.example.geungeunhanjan.domain.dto.board.BoardDTO;
import com.example.geungeunhanjan.domain.dto.board.LifeUserInfoDTO;
import com.example.geungeunhanjan.domain.dto.file.FollowDTO;
import com.example.geungeunhanjan.domain.dto.file.FollowHeartDTO;
import com.example.geungeunhanjan.domain.dto.lifePage.Criteria;
import com.example.geungeunhanjan.domain.dto.lifePage.Page;
import com.example.geungeunhanjan.domain.vo.board.BoardVO;
import com.example.geungeunhanjan.domain.vo.file.UserFileVO;
import com.example.geungeunhanjan.domain.vo.lifes.FollowVO;
import com.example.geungeunhanjan.domain.vo.user.UniVO;
import com.example.geungeunhanjan.service.MyPageService;
import com.example.geungeunhanjan.service.board.BoardService;
import com.example.geungeunhanjan.service.lifes.FollowService;
import com.example.geungeunhanjan.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/yourLife")
@AllArgsConstructor
public class YourLifeController {

    private final FollowService followService;
    private final BoardService boardService;
    private final MyPageService myPageService;
    private final UserService userService;


    //너의 일대기 클릭시
    @GetMapping()
    public String yourLife(Model model, HttpSession session) {
        // 로그인 여부 확인
        Long uniId = (Long) session.getAttribute("uniId");
        if (uniId == null) {
            return "redirect:/user/login";
        }
        // 유저 정보 (프로필쪽)
        LifeUserInfoDTO userInfo = myPageService.selectAllInfo(uniId);
        model.addAttribute("userInfo", userInfo);

        //팔로워 리스트 조회
        List<FollowDTO> followers = followService.selectFollower(uniId);
        model.addAttribute("followers", followers);
        System.out.println("팔로워 목록 " + followers);
        //팔로잉 리스트 조회
        List<FollowDTO> followings = followService.selectFollowing(uniId);
        model.addAttribute("followings", followings);
        System.out.println(followings);
        System.out.println(model);
        //팔로우의 일기수 조회 - 삭제 할수도있음
        List<FollowDTO> boards = followService.selectBoardCount(uniId);
        model.addAttribute("boards", boards);

        //페이지 처리
//        List<FollowDTO> followLists = followService.selectAllPageFollow((followCriteria));
//        int total = followService.selectTotalFollow();
//        FollowPage followPage = new FollowPage(followCriteria,total);
//
//        //페이징 정보 가져오기
//        model.addAttribute("followLists", followLists);
//        model.addAttribute("page", followPage);


        return "yourLife/yourLife";
    }





    //★☆★☆★☆★☆★☆★☆★☆★☆★☆ myLife의 userpage ★☆★☆★☆★☆★☆★☆★☆★☆
    @GetMapping("/userpage/{uniId}")

    public String userPage(Model model, @PathVariable("uniId") long userId, HttpServletRequest request, Criteria criteria) {




        LifeUserInfoDTO follow = myPageService.selectAllInfo(userId);
        int follwingCNT = myPageService.countFollowing(userId);
        int followerCNT = myPageService.countFollower(userId);
        model.addAttribute("follwingCNT", follwingCNT);
        model.addAttribute("followerCNT", followerCNT);

        UniVO about = followService.selectFollowAbout(userId);
        List<BoardVO> boards = boardService.selectBoard(userId);
        if(about != null) {
            model.addAttribute("about",about);
        }

        /* 페이징 윤근님꺼  */
        List<BoardVO> boardPaging = myPageService.selectMypagePaging(criteria, userId);
        int total = myPageService.myPageTotal(userId);
        Page page = new Page(criteria, total);
        model.addAttribute("boardPage", boardPaging);
        model.addAttribute("page", page);


        // 유저 정보 (프로필쪽)
        LifeUserInfoDTO userInfo = myPageService.selectAllInfo(userId);
        model.addAttribute("userInfo", userInfo);

        FollowHeartDTO followHeartDTO = new FollowHeartDTO();
        // 현재 사용자의 userId를 세션에서 가져오기
        Long loginUserId = (Long) request.getSession().getAttribute("uniId");
        followHeartDTO.setFollowToUser(loginUserId);
        followHeartDTO.setFollowFromUser(userId);
        int followStatus = followService.selectFollowStatus(followHeartDTO);

        // 세션에서 현재 사용자의 userId를 가져오기 추가 0709
        model.addAttribute("sessionUser", loginUserId); // sessionUser 값을 모델에 추가

        model.addAttribute("followStatus", followStatus);

        model.addAttribute("boards", boards);
        model.addAttribute("follow", follow);
        System.out.println("dddddddddd");

        return "yourLife/userpage";
    }
    // 유저 페이지 팔로우 기능 구현 컨트롤러 -하트클릭시
    @PostMapping("/userpage/{uniId}")
    public String userPage(
            HttpServletRequest request,
            @RequestBody Map<String, Object> requestBody,
            @PathVariable("uniId") long userId) {

        // 현재 사용자의 userId를 세션에서 가져오기
        Long loginUserId = (Long) request.getSession().getAttribute("uniId");
        System.out.println("loginUserId = " + loginUserId);
//        if (loginUserId == null) {
//            // userId가 없으면 에러 처리 또는 로그인 페이지로 리다이렉트
//            return "redirect:/user/login";
//        }

        //팔로우 로직 FollowVO
        FollowVO followVO = new FollowVO();
        //언팔로우 로직 FollowHeartDTO
        FollowHeartDTO followHeartDTO = new FollowHeartDTO();

        // followVO에 userId(followToUser) 설정
        followVO.setFollowToUser(loginUserId);
        followHeartDTO.setFollowToUser(loginUserId);
        System.out.println("followToUser 확인용1 : " + followVO.getFollowToUser());


        // followId 설정
        followVO.setFollowId(followService.getFollowSeqNext());
        System.out.println("FollowId 확인용 : " + followVO.getFollowId());

        // followFromUser 설정
        followVO.setFollowFromUser(userId);
        followHeartDTO.setFollowFromUser(userId);
        System.out.println("followFromUser 확인용 : " + followVO.getFollowFromUser());
        // checkFollow 상태 가져오기
        Boolean checkFollow = (Boolean) requestBody.get("checkFollow");
        System.out.println("checkFollow 상태: " + checkFollow);

        // checkFollow 상태에 따라 필요한 로직을 추가합니다
        if (checkFollow) {
            // 팔로우 로직
            followService.insertFollow(followVO);
        } else {
            // 언팔로우 로직
            followService.deleteFollow(followHeartDTO);
        }

        System.out.println("followVO 하트클릭테스트: " + followVO);

        return "redirect:/yourLife/userpage/{uniId}";
    }

    @GetMapping("/userpage/{uniId}/{boardLifeCycle}")
    public String everyLifeCycle(Model model , HttpSession session, Criteria criteria, HttpServletRequest request,
                                 @PathVariable String boardLifeCycle, @PathVariable("uniId") long userId){

        LifeUserInfoDTO follow = myPageService.selectAllInfo(userId);
        follow.setUniId(userId);
        model.addAttribute("follow", follow);

        int follwingCNT = myPageService.countFollowing(userId);
        int followerCNT = myPageService.countFollower(userId);
        model.addAttribute("follwingCNT", follwingCNT);
        model.addAttribute("followerCNT", followerCNT);

        UniVO about = followService.selectFollowAbout(userId);
        List<BoardVO> boards = boardService.selectBoard(userId);
        if(about != null) {
            model.addAttribute("about",about);
        }

        criteria.setAmount(9);
        List<BoardDTO> boardPaging = boardService.userLifeCyclePaging(boardLifeCycle,userId,criteria);
        model.addAttribute("boardPage", boardPaging);

        // 유저 정보 (프로필쪽)
        LifeUserInfoDTO userInfo = myPageService.selectAllInfo(userId);
        model.addAttribute("userInfo", userInfo);


        FollowHeartDTO followHeartDTO = new FollowHeartDTO();
        // 현재 사용자의 userId를 세션에서 가져오기
        Long loginUserId = (Long) request.getSession().getAttribute("uniId");
        followHeartDTO.setFollowToUser(loginUserId);
        followHeartDTO.setFollowFromUser(userId);
        int followStatus = followService.selectFollowStatus(followHeartDTO);
        model.addAttribute("followStatus", followStatus);

        model.addAttribute("boards", boards);
        System.out.println("dddddddddd");


        int total = boardService.MyAgePageMove(boardLifeCycle, userId);
        Page page = new Page(criteria, total);
        model.addAttribute("page", page);
        return "yourLife/userpage";
    }


}






