package tech.zerofiltre.blog.domain.article.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.ArticleView;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.UnAuthenticatedActionException;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.DummyMetricsProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBArticleProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBArticleViewProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBReactionProvider;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import({DBArticleProvider.class, DBTagProvider.class, DBUserProvider.class,
        DBReactionProvider.class, DBArticleViewProvider.class
})
class FindArticleIT {

    public static final String DDD = "DDD";
    public static final String TDD = "TDD";
    public static final String CLEAN_CODE = "CLEAN CODE";
    public static final String HEXAGONAL_ARCHITECTURE = "HEXAGONAL ARCHITECTURE";
    public static final String UI_DESIGN = "UI DESIGN";
    public static final String UX_DESIGN = "UX Design";
    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ArticleViewProvider articleViewProvider;

    private MetricsProvider metricsProvider;

    private FindArticle findArticle;

    @BeforeEach
    void init() {
        metricsProvider = new DummyMetricsProvider();
        findArticle = new FindArticle(articleProvider, metricsProvider, articleViewProvider);
    }

    @Test
    void mustProperlySave_AnArticleView() throws ResourceNotFoundException {
        //arrange
        LocalDateTime beforeViewing = LocalDateTime.now();

        User user = new User();
        user.setRoles(Collections.singleton("ROLE_ADMIN"));
        user = userProvider.save(user);

        User viewer = new User();
        viewer = userProvider.save(viewer);


        Article ddd = new Article();
        ddd.setTitle(DDD);
        ddd.setStatus(Status.PUBLISHED);
        ddd.setAuthor(user);

        ddd = articleProvider.save(ddd);

        long articleId = ddd.getId();
        long userId = viewer.getId();


        //act
        findArticle.byId(articleId, viewer);

        //assert
        LocalDateTime afterViewing = LocalDateTime.now();
        List<ArticleView> results = articleViewProvider.viewsOfArticle(articleId);
        assertThat(results).isNotEmpty();
        ArticleView only = results.get(0);
        LocalDateTime viewedAt = only.getViewedAt();
        assertThat(viewedAt).isAfterOrEqualTo(beforeViewing);
        assertThat(viewedAt).isBeforeOrEqualTo(afterViewing);
        assertThat(only.getViewed().getId()).isEqualTo(articleId);
        assertThat(only.getViewer().getId()).isEqualTo(userId);
        assertThat(only.getId()).isNotZero();
    }

    @Test
    @DisplayName("Must properly return articles from the requested page")
    void mustReturnArticlesFromTheRequestedPage() throws ForbiddenActionException, UnAuthenticatedActionException {


        //ARRANGE
        User user = new User();
        user.setRoles(Collections.singleton("ROLE_ADMIN"));
        user = userProvider.save(user);

        Article ddd = new Article();
        ddd.setTitle(DDD);
        ddd.setStatus(Status.PUBLISHED);

        Article tdd = new Article();
        tdd.setTitle(TDD);
        tdd.setStatus(Status.PUBLISHED);


        Article cleanCode = new Article();
        cleanCode.setTitle(CLEAN_CODE);
        cleanCode.setStatus(Status.DRAFT);
        cleanCode.setAuthor(user);

        Article hexagonalArchitecture = new Article();
        hexagonalArchitecture.setTitle(HEXAGONAL_ARCHITECTURE);
        hexagonalArchitecture.setStatus(Status.DRAFT);
        hexagonalArchitecture.setAuthor(user);

        Article uiDesign = new Article();
        uiDesign.setTitle(UI_DESIGN);
        uiDesign.setStatus(Status.IN_REVIEW);

        Article uxDesign = new Article();
        uxDesign.setTitle(UX_DESIGN);
        uxDesign.setStatus(Status.IN_REVIEW);

        articleProvider.save(ddd);
        articleProvider.save(tdd);
        articleProvider.save(cleanCode);
        articleProvider.save(hexagonalArchitecture);
        articleProvider.save(uiDesign);
        articleProvider.save(uxDesign);


        //ACT
        Page<Article> firstPageWithTwoInReview = findArticle.of(new FinderRequest(0, 2, Status.IN_REVIEW, user));
        Page<Article> firstPageWith2Published = findArticle.of(new FinderRequest(0, 2, Status.PUBLISHED, user));
        FinderRequest draftRequest = new FinderRequest(0, 2, Status.DRAFT, user);
        Page<Article> firstPageWith2Drafts = findArticle.of(draftRequest);
        draftRequest.setYours(true);
        Page<Article> firstPageWithYour2Drafts = findArticle.of(draftRequest);
        Page<Article> thirdPageWithThreePublished = findArticle.of(new FinderRequest(2, 3, Status.PUBLISHED, user));

        //ASSERT
        assertThat(firstPageWithTwoInReview.getContent()).hasSize(2);
        assertThat(firstPageWithTwoInReview.getContent().get(0).getTitle()).isEqualTo(UX_DESIGN);
        assertThat(firstPageWithTwoInReview.getContent().get(1).getTitle()).isEqualTo(UI_DESIGN);

        assertThat(firstPageWith2Published.getContent()).hasSize(2);
        assertThat(firstPageWith2Published.getContent().get(0).getTitle()).isEqualTo(TDD);
        assertThat(firstPageWith2Published.getContent().get(1).getTitle()).isEqualTo(DDD);

        assertThat(firstPageWith2Drafts.getContent()).hasSize(2);
        assertThat(firstPageWith2Drafts.getContent().get(0).getTitle()).isEqualTo(HEXAGONAL_ARCHITECTURE);
        assertThat(firstPageWith2Drafts.getContent().get(1).getTitle()).isEqualTo(CLEAN_CODE);

        assertThat(firstPageWithYour2Drafts.getContent()).hasSize(2);
        assertThat(firstPageWithYour2Drafts.getContent().get(0).getTitle()).isEqualTo(HEXAGONAL_ARCHITECTURE);
        assertThat(firstPageWithYour2Drafts.getContent().get(1).getTitle()).isEqualTo(CLEAN_CODE);

        assertThat(thirdPageWithThreePublished.getContent()).isEmpty();


    }
}
