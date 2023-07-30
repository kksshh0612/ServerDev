package NestNet.NestNetWebSite.repository.comment;

import NestNet.NestNetWebSite.domain.comment.Comment;
import NestNet.NestNetWebSite.domain.post.Post;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final EntityManager entityManager;

    // 저장
    public void save(Comment comment){
        entityManager.persist(comment);
    }

    //=========================================조회=========================================//
    // id(PK)로 단건 조회
    public Comment findById(Long id){
        return entityManager.find(Comment.class, id);
    }

    // 게시물에 해당하는 댓글 모두 조회
    public List<Comment> findCommentsByPost(Post post){

        List<Comment> comments = entityManager.createQuery("select c from Comment c where c.post =: post")
                .setParameter("post", post)
                .getResultList();

        return comments;
    }
}
