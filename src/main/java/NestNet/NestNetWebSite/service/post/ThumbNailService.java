package NestNet.NestNetWebSite.service.post;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.domain.post.Post;
import NestNet.NestNetWebSite.domain.post.photo.ThumbNail;
import NestNet.NestNetWebSite.domain.token.dto.response.ThumbNailResponse;
import NestNet.NestNetWebSite.repository.post.ThumbNailRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ThumbNailService {

    private final ThumbNailRepository thumbNailRepository;
    private final EntityManager entityManager;

    // Post 자식 객체를 가져오기 위한 간단한 로직 수행
    public Post findPost(Long postId){
        return entityManager.find(Post.class, postId);
    }

    /*
    첨부파일 저장 -> 저장 로직은 게시물에 종속적. 게시물 저장하는 서비스 로직에서 수행
     */

    /*
    게시판 화면에서 썸네일 모두 조회
     */
    public ApiResult<?> findAllThumbNail(int offset, int limit){

        List<ThumbNail> thumbNailList = thumbNailRepository.findAllPhotoThumbNailByPaging(offset, limit);
        List<ThumbNailResponse> thumbNailResponseList = new ArrayList<>();
        for(ThumbNail thumbNail : thumbNailList){
            thumbNailResponseList.add(new ThumbNailResponse(thumbNail.getPost().getId(), thumbNail.getTitle(), thumbNail.getSaveFileName(), thumbNail.getSaveFilePath()));
        }

        return ApiResult.success(thumbNailResponseList);
    }
}
