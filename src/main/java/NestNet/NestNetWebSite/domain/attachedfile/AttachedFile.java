package NestNet.NestNetWebSite.domain.attachedfile;

import NestNet.NestNetWebSite.domain.post.Post;
import NestNet.NestNetWebSite.domain.post.PostCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.Normalizer;
import java.util.UUID;

/**
 * 첨부파일 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
public class AttachedFile {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attached_file_id")
    private Long id;                                            // PK

    @ManyToOne(fetch = FetchType.LAZY)  //양방향
    @JoinColumn(name = "Post_id")
    private Post post;                                          // 첨부파일이 포함된 게시물

    private String originalFileName;                            // 사용자가 입력한 파일 이름
    private String saveFileName;                                // 실제 저장된 파일 이름
    private String saveFilePath;                                // 파일 경로 (서버)

    @Transient      //영속성 컨텍스트에 관리되지 않음
    private static String basePath = "C:" + File.separator + "nestnetFile" + File.separator;

    /*
    생성자
     */
    public AttachedFile(Post post, MultipartFile file){
        this.post = post;
        createFileName(file);
        createSavePath();
    }

    //== setter ==//
    public void setPost(Post post) {
        this.post = post;
    }

    //== 비지니스 로직 ==//
    /*
    파일 이름 중복 방지를 위한 파일명 생성
     */
    public void createFileName(MultipartFile file){
        String fileName = Normalizer.normalize(file.getOriginalFilename(), Normalizer.Form.NFC);    //Mac, Window 한글 처리 다른 이슈 처리
        this.originalFileName = fileName;
        this.saveFileName = UUID.randomUUID().toString() + "_" + fileName;

    }

    /*
    파일 저장 경로 생성
     */
    public void createSavePath(){
        String path = basePath + this.post.getPostCategory();      //파일 저장 경로 ( ex) C:/nestnetFile/dev)
        File folder = new File(path);               //해당 경로에 폴더 생성

        if(!folder.exists()){       //해당 폴더가 존재하지 않을 경우 생성
            try {
                folder.mkdir();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        this.saveFilePath = path;
    }
}