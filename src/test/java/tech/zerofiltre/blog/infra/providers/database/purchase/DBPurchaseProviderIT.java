package tech.zerofiltre.blog.infra.providers.database.purchase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.CourseJPARepository;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@DataJpaTest
@ExtendWith(MockitoExtension.class)
class DBPurchaseProviderTest {

    @Autowired
    private PurchaseJPARepository purchaseRepository;

    @Autowired
    private CourseJPARepository courseRepository;

    @Autowired
    private UserJPARepository userRepository;

    private DBPurchaseProvider dbPurchaseProvider;
    private Purchase purchase;
    private User user;
    private Course course;

    @BeforeEach
    public void setup() {
        DBCourseProvider courseProvider = new DBCourseProvider(courseRepository, null);
        DBUserProvider userProvider = new DBUserProvider(userRepository);
        dbPurchaseProvider = new DBPurchaseProvider(purchaseRepository);

        user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        course = ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        purchase = new Purchase();
        purchase.setUser(user);
        purchase.setCourse(course);
    }

    @Test
    void save_Persists_Properly() throws ZerofiltreException {

        Purchase savedPurchase = dbPurchaseProvider.save(purchase);
        assertThat(savedPurchase).isNotNull();
        assertThat(savedPurchase.getId()).isNotZero();
        assertThat(savedPurchase.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedPurchase.getCourse().getId()).isEqualTo(course.getId());
    }


    @Test
    void purchaseOf_finds_Properly() throws ZerofiltreException {
        // set properties for purchase
        Purchase savedPurchase = dbPurchaseProvider.save(purchase);

        Optional<Purchase> foundPurchase = dbPurchaseProvider.purchaseOf(savedPurchase.getUser().getId(), savedPurchase.getCourse().getId());
        assertThat(foundPurchase).isPresent();
        assertThat(foundPurchase.get().getId()).isEqualTo(savedPurchase.getId());
    }

    @Test
    void delete_isDone_Properly() throws ZerofiltreException {
        Purchase savedPurchase = dbPurchaseProvider.save(purchase);

        dbPurchaseProvider.delete(savedPurchase.getUser().getId(), savedPurchase.getCourse().getId());
        Optional<Purchase> foundPurchase = dbPurchaseProvider.purchaseOf(savedPurchase.getUser().getId(), savedPurchase.getCourse().getId());

        assertThat(foundPurchase).isNotPresent();
    }
}
