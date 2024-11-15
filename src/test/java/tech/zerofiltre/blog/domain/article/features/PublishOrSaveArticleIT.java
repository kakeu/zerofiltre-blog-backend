package tech.zerofiltre.blog.domain.article.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.PublishOrSaveArticleException;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBArticleProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBReactionProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.notification.user.AppPublisherNotificationProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;
import static tech.zerofiltre.blog.domain.article.model.Status.PUBLISHED;

@DataJpaTest
@Import({DBArticleProvider.class, DBTagProvider.class, DBUserProvider.class, DBReactionProvider.class, AppPublisherNotificationProvider.class})
class PublishOrSaveArticleIT {

    public static final String NEW_CONTENT = "New content";
    public static final String NEW_THUMBNAIL = "New thumbnail";
    public static final String NEW_TITLE = "New title";
    public static final String NEW_SUMMARY = "New summary";
    public static final String VIDEO = "video";
    private PublishOrSaveArticle publishOrSaveArticle;

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private TagProvider tagProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ReactionProvider reactionProvider;

    @MockBean
    private UserNotificationProvider userNotificationProvider;

    @BeforeEach
    void init() {
        publishOrSaveArticle = new PublishOrSaveArticle(articleProvider, tagProvider, userNotificationProvider);
    }

    @Test
    @DisplayName("Must properly partially save the data on publish")
    void mustPublishProperly() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE
        User user = userProvider.save(ZerofiltreUtils.createMockUser(true));

        List<Tag> newTags = ZerofiltreUtils.createMockTags(false).stream()
                .map(tagProvider::save)
                .collect(Collectors.toList());


        Article article = ZerofiltreUtils.createMockArticle(user, new ArrayList<>(), new ArrayList<>());
        article = articleProvider.save(article);

        ZerofiltreUtils.createMockReactions(true, article.getId(), 0,user)
                .forEach(reactionProvider::save);


        LocalDateTime beforePublication = LocalDateTime.now();


        //ACT
        Article publishedArticle = publishOrSaveArticle.execute(user, article.getId(), NEW_TITLE, NEW_THUMBNAIL, NEW_SUMMARY, NEW_CONTENT, newTags, VIDEO, PUBLISHED, null);

        //ASSERT
        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getSummary()).isNotNull();
        assertThat(publishedArticle.getSummary()).isEqualTo(NEW_SUMMARY);

        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(publishedArticle.getPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
        assertThat(publishedArticle.getVideo()).isNotNull();
        assertThat(publishedArticle.getVideo()).isEqualTo(VIDEO);
        assertThat(publishedArticle.getPublishedAt()).isBeforeOrEqualTo(publishedArticle.getLastPublishedAt());
        assertThat(publishedArticle.getLastPublishedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);


        User publisher = publishedArticle.getAuthor();
        assertThat(publisher).isNotNull();
        assertThat(publisher.getRegisteredOn()).isEqualTo(user.getRegisteredOn());
        assertThat(publisher.getId()).isEqualTo(user.getId());
        assertThat(publisher.getFullName()).isEqualTo(user.getFullName());
        assertThat(publisher.getProfilePicture()).isEqualTo(user.getProfilePicture());
        assertThat(publisher.getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(publisher.getBio()).isEqualTo(user.getBio());
        assertThat(publisher.getProfession()).isEqualTo(user.getProfession());
        assertThat(publisher.getWebsite()).isEqualTo(user.getWebsite());

        Set<SocialLink> publishedSocialLinks = publisher.getSocialLinks();
        Set<SocialLink> userSocialLinks = user.getSocialLinks();
        assertThat(publishedSocialLinks).hasSameSizeAs(userSocialLinks);
        assertThat(publishedSocialLinks.stream().anyMatch(socialLink ->
                userSocialLinks.stream().anyMatch(userSocialLink ->
                        socialLink.getLink().equals(userSocialLink.getLink()) &&
                                socialLink.getPlatform().equals(userSocialLink.getPlatform())
                )
        )).isTrue();

        assertThat(publishedArticle.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(publishedArticle.getThumbnail()).isEqualTo(NEW_THUMBNAIL);
        assertThat(publishedArticle.getTitle()).isEqualTo(NEW_TITLE);

        List<Tag> publishedArticleTags = publishedArticle.getTags();

        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());

        assertThat(publishedArticleTags.stream().anyMatch(tag ->
                newTags.stream().anyMatch(mockTag ->
                        tag.getId() == mockTag.getId() &&
                                tag.getName().equals(mockTag.getName())
                )
        )).isTrue();

        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    @DisplayName("Must properly partially save the data on save")
    void mustSaveProperly() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE
        User user = userProvider.save(ZerofiltreUtils.createMockUser(false));

        List<Tag> newTags = ZerofiltreUtils.createMockTags(false).stream()
                .map(tagProvider::save)
                .collect(Collectors.toList());

        Article article = ZerofiltreUtils.createMockArticle(user, new ArrayList<>(), new ArrayList<>());
        article = articleProvider.save(article);

        ZerofiltreUtils.createMockReactions(true, article.getId(),0, user)
                .forEach(reactionProvider::save);


        LocalDateTime beforePublication = LocalDateTime.now();


        //ACT
        Article savedArticle = publishOrSaveArticle.execute(user, article.getId(), NEW_TITLE, NEW_THUMBNAIL, NEW_SUMMARY, NEW_CONTENT, newTags, VIDEO, DRAFT, null);

        //ASSERT
        assertThat(savedArticle).isNotNull();
        assertThat(savedArticle.getId()).isNotZero();

        assertThat(savedArticle.getSummary()).isEqualTo(NEW_SUMMARY);


        assertThat(savedArticle.getVideo()).isNotNull();
        assertThat(savedArticle.getVideo()).isEqualTo(VIDEO);
        assertThat(savedArticle.getCreatedAt()).isNotNull();
        assertThat(savedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(savedArticle.getLastSavedAt()).isNotNull();
        assertThat(savedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);


        User publisher = savedArticle.getAuthor();
        assertThat(publisher).isNotNull();
        assertThat(publisher.getRegisteredOn()).isEqualTo(user.getRegisteredOn());
        assertThat(publisher.getId()).isEqualTo(user.getId());
        assertThat(publisher.getFullName()).isEqualTo(user.getFullName());
        assertThat(publisher.getProfilePicture()).isEqualTo(user.getProfilePicture());
        assertThat(publisher.getPseudoName()).isEqualTo(user.getPseudoName());

        assertThat(savedArticle.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(savedArticle.getThumbnail()).isEqualTo(NEW_THUMBNAIL);
        assertThat(savedArticle.getTitle()).isEqualTo(NEW_TITLE);

        List<Tag> publishedArticleTags = savedArticle.getTags();

        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());

        assertThat(publishedArticleTags.stream().anyMatch(tag ->
                newTags.stream().anyMatch(mockTag ->
                        tag.getId() == mockTag.getId() &&
                                tag.getName().equals(mockTag.getName())
                )
        )).isTrue();

        assertThat(savedArticle.getStatus()).isEqualTo(DRAFT);
    }


}