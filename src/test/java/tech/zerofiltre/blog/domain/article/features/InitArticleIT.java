package tech.zerofiltre.blog.domain.article.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBArticleProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBReactionProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;

@DataJpaTest
@Import({DBArticleProvider.class, DBTagProvider.class, DBUserProvider.class, DBReactionProvider.class})
class InitArticleIT {

    public static final String TITLE = "Title";
    private InitArticle initArticle;

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private TagProvider tagProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ReactionProvider reactionProvider;

    @BeforeEach
    void init() {
        initArticle = new InitArticle(articleProvider);
    }

    @Test
    @DisplayName("Must save article with all data")
    void mustSetStatusToPublished() {
        //ARRANGE
        LocalDateTime beforeCreation = LocalDateTime.now();
        User mockUser = userProvider.save(ZerofiltreUtils.createMockUser(false));

        //ACT
        Article initializedArticle = initArticle.execute(TITLE, mockUser);

        //ASSERT
        assertThat(initializedArticle).isNotNull();
        assertThat(initializedArticle.getId()).isNotZero();

        assertThat(initializedArticle.getCreatedAt()).isNotNull();
        assertThat(initializedArticle.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(initializedArticle.getLastSavedAt()).isNotNull();
        assertThat(initializedArticle.getCreatedAt()).isBeforeOrEqualTo(initializedArticle.getLastSavedAt());
        assertThat(initializedArticle.getLastSavedAt()).isAfterOrEqualTo(beforeCreation);


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