package NestNet.NestNetWebSite.domain.post.unified;

import NestNet.NestNetWebSite.domain.member.Member;
import NestNet.NestNetWebSite.domain.post.Post;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 통합 게시판 엔티티
 */
@Entity
@Getter
@DiscriminatorValue("Unified")
@NoArgsConstructor
public class UnifiedPost extends Post {

    @Enumerated(value = EnumType.STRING)
    private UnifiedPostType unifiedPostType;                    // 게시판 소분류 (자유, 개발, 진로)

    /*
    생성자
     */
    @Builder
    public UnifiedPost(String title, String bodyContent, Member member, int viewCount, int recommendationCount, NestNet.NestNetWebSite.domain.post.PostCategory postCategory,
                        LocalDateTime createdTime, UnifiedPostType unifiedPostType){

        super(title, bodyContent, member, viewCount, recommendationCount, postCategory, createdTime);
        this.unifiedPostType = unifiedPostType;
    }

    //== 비지니스 로직 ==//
    /*
    게시글 수정
     */
    @Override
    public void modifyPost(String title, String bodyContent) {
        super.setTitle(title);
        super.setBodyContent(bodyContent);
        super.setModifiedTime(LocalDateTime.now());
    }

    //== setter ==//
    public void setUnifiedPostType(UnifiedPostType unifiedPostType) {
        this.unifiedPostType = unifiedPostType;
    }
}