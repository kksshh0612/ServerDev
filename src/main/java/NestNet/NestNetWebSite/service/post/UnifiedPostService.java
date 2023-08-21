package NestNet.NestNetWebSite.service.post;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.domain.attachedfile.AttachedFile;
import NestNet.NestNetWebSite.domain.member.Member;
import NestNet.NestNetWebSite.domain.post.unified.UnifiedPost;
import NestNet.NestNetWebSite.domain.post.unified.UnifiedPostType;
import NestNet.NestNetWebSite.domain.token.dto.request.UnifiedPostRequest;
import NestNet.NestNetWebSite.domain.token.dto.response.UnifiedPostResponse;
import NestNet.NestNetWebSite.domain.token.dto.response.UnifiedPostListResponse;
import NestNet.NestNetWebSite.repository.attachedfile.AttachedFileRepository;
import NestNet.NestNetWebSite.repository.post.UnifiedPostRepository;
import NestNet.NestNetWebSite.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UnifiedPostService {

    private final UnifiedPostRepository unifiedPostRepository;
    private final MemberRepository memberRepository;
    private final AttachedFileRepository attachedFileRepository;

    /*
    통합 게시판 게시물 저장
     */
    @Transactional
    public void savePost(UnifiedPostRequest unifiedPostRequest, List<MultipartFile> files, String memberLoginId) {

        Member member = memberRepository.findByLoginId(memberLoginId);

        UnifiedPost post = unifiedPostRequest.toEntity(member);

        List<AttachedFile> attachedFileList = new ArrayList<>();
        for(MultipartFile file : files){
            AttachedFile attachedFile = new AttachedFile(post, file);
            attachedFileList.add(attachedFile);
            post.addAttachedFiles(attachedFile);
        }

        unifiedPostRepository.save(post);
        attachedFileRepository.saveAll(attachedFileList, files);
    }

    /*
    통합 게시판 목록 조회
     */
    public ApiResult<?> findPostList(UnifiedPostType unifiedPostType, int offset, int limit){

        List<UnifiedPost> postList = unifiedPostRepository.findUnifiedPostByType(offset, limit, unifiedPostType);

        List<UnifiedPostListResponse> resultList = new ArrayList<>();

        for(UnifiedPost post : postList){
            resultList.add(new UnifiedPostListResponse(post.getMember().getName(), post.getTitle(),
                    post.getCreatedTime(), post.getViewCount(), post.getLikeCount()));
        }

        return ApiResult.success(resultList);
    }

    /*
    통합 게시판 게시물 단건 조회
     */
    @Transactional
    public UnifiedPostResponse findPostById(Long id, String memberLoginId){

        UnifiedPost post = unifiedPostRepository.findById(id);
        unifiedPostRepository.addViewCount(post, memberLoginId);

        return UnifiedPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .bodyContent(post.getBodyContent())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .unifiedPostType(post.getUnifiedPostType())
                .userName(post.getMember().getName())
                .build();
    }

    /*
    좋아요
     */
    @Transactional
    public void like(Long id){

        UnifiedPost post = unifiedPostRepository.findById(id);
        unifiedPostRepository.like(post);
    }

    /*
    좋아요 취소
     */
    @Transactional
    public void cancelLike(Long id){

        UnifiedPost post = unifiedPostRepository.findById(id);
        unifiedPostRepository.cancelLike(post);
    }

    /*
    족보 게시물 삭제
     */
    public void deletePost(Long id){
        unifiedPostRepository.deletePost(id);
    }
}
