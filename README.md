
## 📁 프로젝트 소개
<Strong>**근근한잔💪 (타임노션)** </Strong>은 개인의 생애주기별 경험과 추억을 기록하고, 이를 다른 사람들과 공유함으로써 공감을 형성하고 팔로잉/팔로우를 통한 채팅도 할 수 있는 플랫폼입니다. 

## 📖목차
- FollowMapper
- FollowMapper.xml 
- FollowService
- FollowServiceImpe
- YourLifeController


## 💻 기능 및 화면 소개

**<FollowMapper.xml>**<br>

- 웹에서 데이터를 전송하기 위해 미리 약속해둔 방식으로 만들어진 문서입니다.
- 팔로우 xml 파일 소스중 일부인 팔로우한 사람의 정보를 조회하는 코드입니다 .
- 
 <!-- 나를 팔로우한 사람의 정보 조회 ; 팔로워 -->
    <select id="selectFollower" resultType="FollowDTO" parameterType="long">
        SELECT u.UNI_ID                                                                 AS followerId,
               NVL(gu.USER_NICKNAME, gk.name)                                           AS userNickname,
               (SELECT COUNT(*) FROM GGHJ_FOLLOW f WHERE f.FOLLOW_FROM_USER = u.UNI_ID) AS followingCount,
               (SELECT COUNT(*) FROM GGHJ_FOLLOW f WHERE f.FOLLOW_TO_USER = u.UNI_ID)   AS followerCount,
               uf.USER_FILE_PROFILE_SOURCE, uf.USER_FILE_PROFILE_NAME , uf.USER_FILE_PROFILE_UUID,
               uf.USER_FILE_BACK_SOURCE, uf.USER_FILE_BACK_NAME , uf.USER_FILE_BACK_UUID
        FROM GGHJ_UNI u
                 LEFT JOIN GGHJ_USER gu ON u.USER_ID = gu.USER_ID
                 LEFT JOIN GGHJ_KAKAO gk ON u.KAKAO_ID = gk.KAKAO_ID
                 LEFT JOIN GGHJ_USER_FILE uf ON u.UNI_ID = uf.USER_ID
        WHERE u.UNI_ID IN (SELECT FOLLOW_TO_USER FROM GGHJ_FOLLOW WHERE FOLLOW_FROM_USER = #{uniId})
    </select>

- select 태그 내에는 sql문을 입력하여 데이터베이스와 함께 작업이 가능하도록 하였습니다
- 테이블은 유저와 카카오유저, 유저파일을 조인하여 팔로워의 정보를 가져오는 코드를 작성했습니다 

**<FollowMapper>**<br>

- 팔로우 mapper의 인터페이스 .java 파일입니다

    //    팔로워 리스트 조회하기
    List<FollowDTO> selectFollower(Long uniId);

- 위 xml 파일에 작성된 xml 파일의 코드를 자바파일에서 사용하기 위해 인터페이스에 정리해둔 코드 일부입니다 .
- 개발 기간을 단축시키며 서로 관계없는 클래스들에게 관계를 맺어주기위해 사용되었습니다 .
  


**<FollowService>**<br>

- 팔로우 Service 인터페이스 파일입니다

    //    팔로워 리스트 조회하기
    List<FollowDTO> selectFollower(Long uniId);

- 인터페이스에서 작성해둔 코드를 이 파일에서 이용합니다 .
- 이름을 변경해도 되나 되도록 팔로우매퍼 인터페이스와 동일한 이름으로 작성했습니다 .
- - 개발 기간을 단축시키며 서로 관계없는 클래스들에게 관계를 맺어주기위해 사용되었습니다 .


**<FollowServiceImpe>**<br>

- 팔로우 Service= 자바 파일입니다

    //팔로워 리스트 조회하기
    @Override
    public List<FollowDTO> selectFollower(Long userId) {
        return followMapper.selectFollower(userId);
    }

- 인터페이스에서 작성해둔 코드를 오버라이딩하여 이 파일에서 이용합니다 .
- 이름을 변경해도 되나 되도록 팔로우매퍼 인터페이스와 동일한 이름으로 작성했습니다 .
- FollowDTO는 xml에서 필요로하는 정보들의 내용을 가지고 와야하기에 DTO파일을 불러왔습니다 .


 **<YourLifeController>**<br>

- 팔로우 Service를 만든 후 컨트롤러에서 작업한 일부 소스코드입니다 

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

- private final FollowService followService; 그전에 컨트롤러 안에 해당 서비스를 선언하고 서비스 내 기능을 사용합니다.
- 해당 기능은 너의페이지 버튼을 클릭하면 필요로하는 기능들을 작성한 코드들입니다.

