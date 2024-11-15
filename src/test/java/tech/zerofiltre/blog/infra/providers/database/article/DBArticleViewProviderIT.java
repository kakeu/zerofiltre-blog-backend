package tech.zerofiltre.blog.infra.providers.database.article;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.article.ArticleProvider;
import tech.zerofiltre.blog.domain.article.ArticleViewProvider;
import tech.zerofiltre.blog.domain.article.model.Article;
import tech.zerofiltre.blog.domain.article.model.ArticleView;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({DBArticleViewProvider.class, DBArticleProvider.class, DBUserProvider.class})
class DBArticleViewProviderIT {

    @Autowired
    private ArticleProvider articleProvider;

    @Autowired
    private ArticleViewProvider articleViewProvider;

    @Autowired
    private UserProvider userProvider;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Count articles read by dates and user works properly")
    void countArticlesReadByDatesAndUser_works_properly() {
        /*
        SCENARIO
        --------
        un user A qui est auteur de 2 articles publiés A1 et A2
        Le user A qui a consulté 1 article B1 dont l'auteur B : consulté => une instance ArticleView(B1 et B)
        un autre user C qui a publé 0 article mais a consulté 3 articles A1,A2,B1 => 3 instances de ArticleView
        */

        //ARRANGE
        // -- dates
        LocalDateTime lastMonth = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().minusMonths(1).getMonthValue(), 10, 0, 0);
        LocalDateTime threeMonthsBack = lastMonth.minusMonths(2);
        List<LocalDate> listDates = ZerofiltreUtils.defineStartDateAndEndDate();

        // -- Users
        User userA = new User();
        userA = userProvider.save(userA);

        User userB = new User();
        userB = userProvider.save(userB);

        User userC = new User();
        userC = userProvider.save(userC);

        // -- Articles
        Article articleA1 = new Article();
        articleA1.setCreatedAt(threeMonthsBack);
        articleA1.setAuthor(userA);
        articleA1 = articleProvider.save(articleA1);

        Article articleA2 = new Article();
        articleA2.setCreatedAt(threeMonthsBack);
        articleA2.setAuthor(userA);
        articleA2 = articleProvider.save(articleA2);

        Article articleB1 = new Article();
        articleB1.setCreatedAt(threeMonthsBack);
        articleB1.setAuthor(userB);
        articleB1 = articleProvider.save(articleB1);

        // -- ArticleView
        ArticleView articleViewA1ByUserC = new ArticleView(userC, articleA1);
        articleViewA1ByUserC.setViewedAt(lastMonth);
        articleViewProvider.save(articleViewA1ByUserC);

        ArticleView articleViewA2ByUserC = new ArticleView(userC, articleA2);
        articleViewA2ByUserC.setViewedAt(lastMonth);
        articleViewProvider.save(articleViewA2ByUserC);

        ArticleView articleViewB1ByUserC = new ArticleView(userC, articleB1);
        articleViewB1ByUserC.setViewedAt(lastMonth);
        articleViewProvider.save(articleViewB1ByUserC);

        ArticleView articleViewB1ByUserA = new ArticleView(userA, articleB1);
        articleViewB1ByUserA.setViewedAt(lastMonth);
        articleViewProvider.save(articleViewB1ByUserA);

        //ACT
        int articlesReadByUserA = articleViewProvider.countArticlesReadByDatesAndUser(listDates.get(0), listDates.get(1), userA.getId());
        int articlesReadByUserC = articleViewProvider.countArticlesReadByDatesAndUser(listDates.get(0), listDates.get(1), userC.getId());

        //ASSERT
        assertThat(articlesReadByUserA).isNotNull();
        assertThat(articlesReadByUserA).isEqualTo(1);
        assertThat(articlesReadByUserC).isNotNull();
        assertThat(articlesReadByUserC).isEqualTo(3);
    }
}
