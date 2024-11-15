package tech.zerofiltre.blog.domain.article.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.ArticleView;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.UnAuthenticatedActionException;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.DummyMetricsProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.features.FindArticle.DOTS;
import static tech.zerofiltre.blog.domain.article.model.Status.*;

@ExtendWith(SpringExtension.class)
class FindArticleTest {

    private final int numberOfElements = 1;
    private final int totalNumberOfPages = 4;
    FindArticle findArticle;
    @MockBean
    private ArticleProvider articleProvider;
    @Mock
    private ArticleViewProvider articleViewProvider;
    private MetricsProvider metricsProvider;

    @BeforeEach
    void setUp() {
        metricsProvider = new DummyMetricsProvider();
        findArticle = new FindArticle(articleProvider, metricsProvider, articleViewProvider);
    }


    @Test
    @DisplayName("Must return the article corresponding ot the id")
    void mustNotFilterOnAuthors() throws ResourceNotFoundException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(mockArticle));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        //ACT
        Article article = findArticle.byId(12, null);

        //ASSERT
        assertThat(article).isEqualTo(mockArticle);

    }

    @Test
    @DisplayName("Increment view count if article is found and published")
    void mustIncrementViewIfArticleIsFound() throws ResourceNotFoundException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        mockArticle.setStatus(PUBLISHED);
        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(mockArticle));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        assertThat(mockArticle.getViewsCount()).isZero();

        //ACT
        Article article = findArticle.byId(12, null);

        //ASSERT
        assertThat(article.getTitle()).isEqualTo(mockArticle.getTitle());
        assertThat(article.getViewsCount()).isOne();
    }

    @Test
    void mustSave_AnArticleView_IfArticleIsFound() throws ResourceNotFoundException {
        //ARRANGE
        LocalDateTime beforeViewing = LocalDateTime.now();
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        mockArticle.setStatus(PUBLISHED);
        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(mockArticle));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(24);

        //ACT
        Article article = findArticle.byId(12, user);
        LocalDateTime afterViewing = LocalDateTime.now();


        //ASSERT
        ArgumentCaptor<ArticleView> captor = ArgumentCaptor.forClass(ArticleView.class);
        verify(articleViewProvider, times(1)).save(captor.capture());
        ArticleView captured = captor.getValue();
        assertThat(captured.getViewer().getId()).isEqualTo(user.getId());
        assertThat(captured.getViewed().getId()).isEqualTo(article.getId());
        assertThat(captured.getViewedAt()).isAfterOrEqualTo(beforeViewing);
        assertThat(captured.getViewedAt()).isBeforeOrEqualTo(afterViewing);
    }

    @Test
    void doNotCreateArticleView_ifArticle_IsNot_Published() throws ResourceNotFoundException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        mockArticle.setStatus(DRAFT);
        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(mockArticle));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(24);

        //ACT
        findArticle.byId(12, user);


        //ASSERT
        verify(articleViewProvider, times(0)).save(any());

    }

    @Test
    @DisplayName("Do not Increment view count if article is not published")
    void mustNotIncrementView_IfArticle_IsNot_Published() throws ResourceNotFoundException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(mockArticle));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        assertThat(mockArticle.getViewsCount()).isZero();

        //ACT
        Article article = findArticle.byId(12, null);

        //ASSERT
        assertThat(article.getTitle()).isEqualTo(mockArticle.getTitle());
        assertThat(article.getViewsCount()).isZero();
    }

    @Test
    @DisplayName("Do not Increment view count if viewer is the author even if it is published")
    void mustNotIncrementView_IfViewer_IsTheAuthor() throws ResourceNotFoundException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        mockArticle.setStatus(PUBLISHED);
        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(mockArticle));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        assertThat(mockArticle.getViewsCount()).isZero();

        //ACT
        Article article = findArticle.byId(12, mockArticle.getAuthor());

        //ASSERT
        assertThat(article.getTitle()).isEqualTo(mockArticle.getTitle());
        assertThat(article.getViewsCount()).isZero();
    }

    @Test
    void mustNotSave_AnArticleView_IfViewer_IsTheAuthor() throws ResourceNotFoundException {
        //ARRANGE
        Article mockArticle = ZerofiltreUtils.createMockArticle(false);
        mockArticle.setStatus(PUBLISHED);
        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(mockArticle));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        User user = ZerofiltreUtils.createMockUser(true);
        user.setId(24);

        //ACT
        findArticle.byId(12, mockArticle.getAuthor());


        //ASSERT
        verify(articleViewProvider, times(0)).save(any());
    }

    @Test
    @DisplayName("Throw FindArticleException if no article is found")
    void mustThrowExceptionOnNoArticle() {
        //ARRANGE
        when(articleProvider.articleOfId(anyLong())).thenReturn(java.util.Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(ResourceNotFoundException.class).isThrownBy(() -> findArticle.byId(12, null));
    }

    @Test
    @DisplayName("All users can ask for PUBLISHED article")
    void mustAllow_Asking_PublishedArticles_forAll() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        Article published = new Article();
        published.setStatus(PUBLISHED);


        when(articleProvider.articlesOf(anyInt(), anyInt(), eq(PUBLISHED), anyLong(), any(), any())).thenReturn(
                new Page<>(1, 0, numberOfElements, 1, totalNumberOfPages, Collections.singletonList(published), true, false)
        );

        //ACT
        Page<Article> articles = findArticle.of(new FinderRequest(0, 3, PUBLISHED, new User()));

        //ASSERT
        verify(articleProvider, times(1)).articlesOf(0, 3, PUBLISHED, 0, null, null);
        assertThat(articles).isNotNull();
        articles.getContent().forEach(article -> assertThat(PUBLISHED).isEqualTo(article.getStatus()));
    }

    @Test
    @DisplayName("Not being an admin, ask for DRAFT throws exception")
    void mustThrowException_IfNotAdminButAskForDRAFT() {
        //ARRANGE

        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class).
                isThrownBy(() -> findArticle.of(new FinderRequest(0, 3, DRAFT, new User())));

    }

    @Test
    @DisplayName("Not being authenticated, ask for DRAFT throws unauthenticatedException")
    void mustThrowException_IfNotAuthenticatedButAskForDRAFT() {
        //ARRANGE

        //ACT & ASSERT
        assertThatExceptionOfType(UnAuthenticatedActionException.class).
                isThrownBy(() -> findArticle.of(new FinderRequest(0, 3, DRAFT, null)));

    }

    @Test
    @DisplayName("Must not filter on authors it the user is not requesting his own articles")
    void mustReturnAnArticle() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        Article published = new Article();
        when(articleProvider.articlesOf(anyInt(), anyInt(), eq(PUBLISHED), anyLong(), any(), any())).thenReturn(
                new Page<>(1, 0, numberOfElements, 1, totalNumberOfPages, Collections.singletonList(published), true, false)
        );

        User user = new User();
        user.setId(24);

        //ACT
        findArticle.of(new FinderRequest(0, 3, PUBLISHED, user));

        //ASSERT
        //call with authorId = 0
        verify(articleProvider, times(1)).articlesOf(0, 3, PUBLISHED, 0, null, null);


    }

    @Test
    @DisplayName("Not being an admin, a user can ask for his OTHER THAN PUBLISHED articles")
    void mustAllow_Asking_YourDraftedArticles() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        Article drafted = new Article();
        drafted.setStatus(DRAFT);

        when(articleProvider.articlesOf(anyInt(), anyInt(), eq(DRAFT), anyLong(), any(), any())).thenReturn(
                new Page<>(1, 0, numberOfElements, 1, totalNumberOfPages, Collections.singletonList(drafted), true, false)
        );

        //ACT
        FinderRequest request = new FinderRequest(0, 3, DRAFT, new User());
        request.setYours(true);
        Page<Article> articles = findArticle.of(request);

        //ASSERT
        verify(articleProvider, times(1)).articlesOf(0, 3, DRAFT, 0, null, null);
        assertThat(articles).isNotNull();
        articles.getContent().forEach(article -> assertThat(DRAFT).isEqualTo(article.getStatus()));
    }

    @Test
    @DisplayName("Not being an admin, ask for IN_REVIEW throws exception")
    void mustThrowException_IfNotAdminButAskForInReview() {
        //ARRANGE

        //ACT & ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class).
                isThrownBy(() -> findArticle.of(new FinderRequest(0, 3, IN_REVIEW, new User())));

    }

    @Test
    @DisplayName("Not being an admin, nor author, ask for premium article return a truncated one")
    void mustTruncate_IfNotAdminButAskForPremium() throws ResourceNotFoundException {
        //ARRANGE
        String a26WordContent = "This is a 26-char content.";
        String halfOfA26WordContent = "This is a 26-";

        User author = ZerofiltreUtils.createMockUser(false);
        author.setId(85);

        Article premium = new Article();
        premium.setStatus(PUBLISHED);
        premium.setContent(a26WordContent);
        premium.setPremium(true);
        premium.setAuthor(author);


        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(premium));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT
        Article result = findArticle.byId(12, new User());

        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(halfOfA26WordContent + DOTS);

    }

    @Test
    @DisplayName("Being an admin, asking for premium article returns the full content")
    void mustNotTruncate_IfAdminAndAskForPremium() throws ResourceNotFoundException {
        //ARRANGE
        String a26WordContent = "This is a 26-char content.";

        User author = ZerofiltreUtils.createMockUser(false);

        Article premium = new Article();
        premium.setStatus(PUBLISHED);
        premium.setContent(a26WordContent);
        premium.setPremium(true);
        premium.setAuthor(author);


        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(premium));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        User admin = ZerofiltreUtils.createMockUser(true);
        admin.setId(24);

        //ACT
        Article result = findArticle.byId(12, admin);

        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(a26WordContent);

    }

    @Test
    @DisplayName("Being the author  of a premium article, asking for that article returns the full content")
    void mustNotTruncate_IfAuthorAndAskForPremium() throws ResourceNotFoundException {
        //ARRANGE
        String a26WordContent = "This is a 26-char content.";

        User author = ZerofiltreUtils.createMockUser(false);
        author.setId(24);

        Article premium = new Article();
        premium.setStatus(PUBLISHED);
        premium.setContent(a26WordContent);
        premium.setPremium(true);
        premium.setAuthor(author);


        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(premium));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT
        Article result = findArticle.byId(12, author);

        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(a26WordContent);

    }

    @Test
    @DisplayName("Being a PRO member, asking for a premium article returns the full content")
    void mustNotTruncate_IfPROAndAskForPremium() throws ResourceNotFoundException {
        //ARRANGE
        String a26WordContent = "This is a 26-char content.";

        User author = ZerofiltreUtils.createMockUser(false);
        author.setId(24);

        Article premium = new Article();
        premium.setStatus(PUBLISHED);
        premium.setContent(a26WordContent);
        premium.setPremium(true);
        premium.setAuthor(author);


        when(articleProvider.articleOfId(12)).thenReturn(java.util.Optional.of(premium));
        when(articleProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        User pro = ZerofiltreUtils.createMockUser(false);
        pro.setId(102);
        pro.setPlan(User.Plan.PRO);

        //ACT
        Article result = findArticle.byId(12, pro);

        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo(a26WordContent);

    }

    @Test
    void mustCallArticleProvider_WithCorrectParams() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE


        FinderRequest request = new FinderRequest(0, 3, PUBLISHED, new User());
        request.setTag("tag");

        findArticle.of(request);

        //ASSERT
        verify(articleProvider, times(1))
                .articlesOf(0, 3, PUBLISHED, 0, null, "tag");

    }

    @Test
    void mustCallArticleProvider_WithMostViewed() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        FinderRequest request = new FinderRequest(0, 3, PUBLISHED, new User());
        request.setFilter(FinderRequest.Filter.MOST_VIEWED);

        findArticle.of(request);

        //ASSERT
        verify(articleProvider, times(1))
                .articlesOf(0, 3, PUBLISHED, 0, FinderRequest.Filter.MOST_VIEWED, null);

    }

    @Test
    void mustCallArticleProvider_WithCorrectParamsAndUser() throws ForbiddenActionException, UnAuthenticatedActionException {
        //ARRANGE
        User user = new User();
        user.setId(2);
        FinderRequest request = new FinderRequest(0, 3, PUBLISHED, user);
        request.setYours(true);

        findArticle.of(request);

        //ASSERT
        verify(articleProvider, times(1))
                .articlesOf(0, 3, PUBLISHED, 2, null, null);

    }
}