package tech.zerofiltre.blog.domain.article.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;

@ExtendWith(SpringExtension.class)
class InitArticleTest {

    public static final String TITLE = "title";
    private InitArticle initArticle;

    @MockBean
    private ArticleProvider articleProvider;
    @MockBean
    private TagProvider tagProvider;
    @MockBean
    private UserProvider userProvider;
    @MockBean
    private ReactionProvider reactionProvider;

    @BeforeEach
    void init() {
        initArticle = new InitArticle(articleProvider);
    }

    @Test
    @DisplayName("Must set the status to draft then save the article")
    void mustSetStatusToPublished() {
        //ARRANGE
        LocalDateTime beforeInit = LocalDateTime.now();
        User mockUser = ZerofiltreUtils.createMockUser(false);
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> {
            Article article = invocationOnMock.getArgument(0);
            article.setId(45);
            return article;
        });


        //ACT
        Article initializedArticle = initArticle.execute(TITLE, mockUser);

        //ASSERT
        verify(articleProvider, times(1)).save(any());
        assertThat(initializedArticle).isNotNull();
        assertThat(initializedArticle.getId()).isNotZero();

        assertThat(initializedArticle.getCreatedAt()).isNotNull();
        assertThat(initializedArticle.getCreatedAt()).isAfterOrEqualTo(beforeInit);
        assertThat(initializedArticle.getLastSavedAt()).isNotNull();
        assertThat(initializedArticle.getCreatedAt()).isBeforeOrEqualTo(initializedArticle.getLastSavedAt());
        assertThat(initializedArticle.getLastSavedAt()).isAfterOrEqualTo(beforeInit);


        User publisher = initializedArticle.getAuthor();
        assertThat(publisher).isNotNull();
        assertThat(publisher.getRegisteredOn()).isEqualTo(mockUser.getRegisteredOn());
        assertThat(publisher.getId()).isEqualTo(mockUser.getId());
        assertThat(publisher.getFullName()).isEqualTo(mockUser.getFullName());
        assertThat(publisher.getProfilePicture()).isEqualTo(mockUser.getProfilePicture());
        assertThat(publisher.getPseudoName()).isEqualTo(mockUser.getPseudoName());

        Set<SocialLink> publishedSocialLinks = publisher.getSocialLinks();
        Set<SocialLink> userSocialLinks = mockUser.getSocialLinks();
        assertThat(publishedSocialLinks).hasSameSizeAs(userSocialLinks);
        assertThat(publishedSocialLinks.stream().anyMatch(socialLink ->
                userSocialLinks.stream().anyMatch(userSocialLink ->
                        socialLink.getLink().equals(userSocialLink.getLink()) &&
                                socialLink.getPlatform().equals(userSocialLink.getPlatform())
                )
        )).isTrue();

        assertThat(initializedArticle.getTitle()).isEqualTo(TITLE);
        assertThat(initializedArticle.getStatus()).isEqualTo(DRAFT);
    }


}