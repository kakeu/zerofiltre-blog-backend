package tech.zerofiltre.blog.domain.article.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ReactionProvider;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.Reaction;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.PublishOrSaveArticleException;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@ExtendWith(SpringExtension.class)
class PublishOrSaveArticleTest {

    public static final String NEW_CONTENT = "New content";
    public static final String NEW_THUMBNAIL = "New thumbnail";
    public static final String NEW_TITLE = "New title";
    public static final String NEW_SUMMARY = "New summary";
    public static final String VIDEO = "video";
    Tag newTag = new Tag(12, "c++");


    private PublishOrSaveArticle publishOrSaveArticle;


    @MockBean
    private ArticleProvider articleProvider;
    @MockBean
    private TagProvider tagProvider;
    @MockBean
    private UserProvider userProvider;
    @MockBean
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

        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.getAuthor().setRoles(Collections.singleton("ROLE_ADMIN"));

        mockArticle.setId(45);
        when(articleProvider.articleOfId(45)).thenReturn(Optional.of(mockArticle));

        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(tagProvider.tagOfId(12)).thenReturn(Optional.of(newTag));

        LocalDateTime beforePublication = LocalDateTime.now();


        //ACT
        List<Tag> newTags = Collections.singletonList(newTag);
        Article publishedArticle = publishOrSaveArticle.execute(
                mockArticle.getAuthor(),
                45,
                NEW_TITLE,
                NEW_THUMBNAIL,
                NEW_SUMMARY,
                NEW_CONTENT,
                newTags,
                VIDEO, PUBLISHED,
                null);

        //ASSERT
        verify(articleProvider, times(1)).save(any());
        verify(articleProvider, times(1)).articleOfId(45);
        verify(tagProvider, times(1)).tagOfId(12);

        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getVideo()).isNotNull();
        assertThat(publishedArticle.getVideo()).isEqualTo(VIDEO);
        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(publishedArticle.getPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastPublishedAt()).isNotNull();
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
        assertThat(publishedArticle.getPublishedAt()).isBeforeOrEqualTo(publishedArticle.getLastPublishedAt());
        assertThat(publishedArticle.getLastPublishedAt()).isAfterOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);

        User publisher = publishedArticle.getAuthor();
        User mockUser = mockArticle.getAuthor();
        assertThat(publisher).isNotNull();
        assertThat(publisher.getRegisteredOn()).isEqualTo(mockUser.getRegisteredOn());
        assertThat(publisher.getId()).isEqualTo(mockUser.getId());
        assertThat(publisher.getFullName()).isEqualTo(mockUser.getFullName());
        assertThat(publisher.getProfilePicture()).isEqualTo(mockUser.getProfilePicture());
        assertThat(publisher.getPseudoName()).isEqualTo(mockUser.getPseudoName());


        assertThat(publishedArticle.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(publishedArticle.getThumbnail()).isEqualTo(NEW_THUMBNAIL);
        assertThat(publishedArticle.getTitle()).isEqualTo(NEW_TITLE);


        List<Tag> publishedArticleTags = publishedArticle.getTags();
        List<Reaction> publishedArticleReactions = publishedArticle.getReactions();
        List<Reaction> articleReactions = mockArticle.getReactions();

        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());
        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());

        assertThat(publishedArticleTags.stream().anyMatch(tag ->
                newTags.stream().anyMatch(mockTag ->
                        tag.getId() == mockTag.getId() &&
                                tag.getName().equals(mockTag.getName())
                )
        )).isTrue();

        assertThat(publishedArticleReactions.stream().anyMatch(reaction ->
                articleReactions.stream().anyMatch(mockReaction ->
                        reaction.getId() == mockReaction.getId()
                                && reaction.getAction().equals(mockReaction.getAction())
                                && reaction.getAuthorId() == mockReaction.getAuthorId())
        )).isTrue();

        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    @DisplayName("Must not draft an already published article")
    void save_MustNotDraft_AnAlreadyPublishArticle() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.setId(45);
        mockArticle.setStatus(PUBLISHED);
        when(articleProvider.articleOfId(45)).thenReturn(Optional.of(mockArticle));

        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(tagProvider.tagOfId(12)).thenReturn(Optional.of(newTag));

        //ACT
        List<Tag> newTags = Collections.singletonList(newTag);
        Article publishedArticle = publishOrSaveArticle.execute(
                mockArticle.getAuthor(),
                45,
                NEW_TITLE,
                NEW_THUMBNAIL,
                NEW_SUMMARY,
                NEW_CONTENT,
                newTags,
                "", DRAFT,
                null);

        assertThat(publishedArticle.getStatus()).isEqualTo(PUBLISHED);

    }

    @Test
    @DisplayName("Must properly partially save the data on save")
    void mustSaveProperly() throws PublishOrSaveArticleException, ForbiddenActionException {
        //ARRANGE

        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        mockArticle.setId(45);
        when(articleProvider.articleOfId(45)).thenReturn(Optional.of(mockArticle));

        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(tagProvider.tagOfId(12)).thenReturn(Optional.of(newTag));

        LocalDateTime beforePublication = LocalDateTime.now();


        //ACT
        List<Tag> newTags = Collections.singletonList(newTag);
        Article publishedArticle = publishOrSaveArticle.execute(
                mockArticle.getAuthor(),
                45,
                NEW_TITLE,
                NEW_THUMBNAIL,
                NEW_SUMMARY,
                NEW_CONTENT,
                newTags,
                VIDEO, DRAFT,
                null);

        //ASSERT
        verify(articleProvider, times(1)).save(any());
        verify(articleProvider, times(1)).articleOfId(45);
        verify(tagProvider, times(1)).tagOfId(12);

        assertThat(publishedArticle).isNotNull();
        assertThat(publishedArticle.getId()).isNotZero();

        assertThat(publishedArticle.getVideo()).isNotNull();
        assertThat(publishedArticle.getVideo()).isEqualTo(VIDEO);
        assertThat(publishedArticle.getCreatedAt()).isNotNull();
        assertThat(publishedArticle.getCreatedAt()).isBeforeOrEqualTo(beforePublication);
        assertThat(publishedArticle.getLastSavedAt()).isNotNull();
        assertThat(publishedArticle.getLastSavedAt()).isAfterOrEqualTo(beforePublication);

        User publisher = publishedArticle.getAuthor();
        User mockUser = mockArticle.getAuthor();
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


        assertThat(publishedArticle.getContent()).isEqualTo(NEW_CONTENT);
        assertThat(publishedArticle.getThumbnail()).isEqualTo(NEW_THUMBNAIL);
        assertThat(publishedArticle.getTitle()).isEqualTo(NEW_TITLE);


        List<Tag> publishedArticleTags = publishedArticle.getTags();
        List<Reaction> publishedArticleReactions = publishedArticle.getReactions();
        List<Reaction> articleReactions = mockArticle.getReactions();

        assertThat(publishedArticleTags.size()).isEqualTo(newTags.size());

        assertThat(publishedArticleTags.stream().anyMatch(tag ->
                newTags.stream().anyMatch(mockTag ->
                        tag.getId() == mockTag.getId() &&
                                tag.getName().equals(mockTag.getName())
                )
        )).isTrue();

        assertThat(publishedArticleReactions.stream().anyMatch(reaction ->
                articleReactions.stream().anyMatch(mockReaction ->
                        reaction.getId() == mockReaction.getId()
                                && reaction.getAction().equals(mockReaction.getAction())
                                && reaction.getAuthorId() == mockReaction.getAuthorId())
        )).isTrue();

        assertThat(publishedArticle.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    @DisplayName("Must throw PublishArticleException if article is not yet registered")
    void mustThrowExceptionOnUnsavedArticle() {
        //ARRANGE
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.empty());


        //ACT & ASSERT
        assertThatExceptionOfType(PublishOrSaveArticleException.class)
                .isThrownBy(() -> publishOrSaveArticle.execute(new User(), 1, "", "", "", "", new ArrayList<>(), "", PUBLISHED, null));

    }

    @Test
    @DisplayName("Must throw ForbiddenActionException if editor is neither the author nor an admin ")
    void mustThrowForbiddenActionExceptionOnNotAuthor() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));
        User editor = new User();
        editor.setEmail("email");
        editor.setRoles(Collections.singleton("ROLE_USER"));


        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> publishOrSaveArticle.execute(editor, 1, "", "", "", "", new ArrayList<>(), "", PUBLISHED, null));

    }

    @Test
    void mustNotThrowExceptionIfNotOwnerButAdmin() {
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));
        User editor = new User();
        editor.setEmail("email");
        editor.setRoles(Collections.singleton("ROLE_ADMIN"));

        assertThatNoException().isThrownBy(() -> publishOrSaveArticle.execute(editor, 1, "", "", "", "", new ArrayList<>(), "", PUBLISHED, null));


    }

    @Test
    @DisplayName("Must throw PublishArticleException if tags are not saved")
    void mustThrowExceptionOnNoTags() {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.empty());
        User editor = new User();
        editor.setEmail(mockArticle.getAuthor().getEmail());
        editor.setRoles(Collections.singleton("ROLE_USER"));

        //ACT & ASSERT
        assertThatExceptionOfType(PublishOrSaveArticleException.class)
                .isThrownBy(() -> publishOrSaveArticle.execute(editor, 1, "", "", "", "", Collections.singletonList(newTag), "", PUBLISHED, null));
    }

    @Test
    @DisplayName("Author but non admin can only submit to validation")
    void execute_PutInReview_OnAuthorButNotAdmin() throws ForbiddenActionException, PublishOrSaveArticleException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(newTag));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT
        Article submittedArticle = publishOrSaveArticle.execute(
                mockArticle.getAuthor(), mockArticle.getId(), "", "", "", "", Collections.singletonList(newTag), "", PUBLISHED, null);

        assertThat(submittedArticle).isNotNull();
        assertThat(submittedArticle.getStatus()).isEqualTo(IN_REVIEW);
    }

    @Test
    @DisplayName("Publishing an article as non admin sends an email to the author")
    void execute_PutInReview_andSendNotification_OnAuthorButNotAdmin() throws ForbiddenActionException, PublishOrSaveArticleException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(true);
        when(articleProvider.articleOfId(anyLong())).thenReturn(Optional.of(mockArticle));
        when(tagProvider.tagOfId(anyLong())).thenReturn(Optional.of(newTag));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT
        publishOrSaveArticle.execute(
                mockArticle.getAuthor(), mockArticle.getId(), "", "", "", "", Collections.singletonList(newTag), "", PUBLISHED, "https://zerofiltre.tech");

        //ASSERT
        ArgumentCaptor<UserActionEvent> captor = ArgumentCaptor.forClass(UserActionEvent.class);
        verify(userNotificationProvider, times(1)).notify(captor.capture());
        UserActionEvent value = captor.getValue();
        assertThat(value.getAction()).isEqualTo(Action.ARTICLE_SUBMITTED);
        assertThat(value.getArticle()).isNotNull();
        assertThat(value.getAppUrl()).isEqualTo("https://zerofiltre.tech");
    }

}