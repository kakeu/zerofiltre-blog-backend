package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.PRODUCT_ID;


@ExtendWith(MockitoExtension.class)
class InvoiceEventHandlerTest {

    @Mock
    private StripeCommons stripeCommons;
    @Mock
    private InfraProperties infraProperties;
    @Mock
    private CourseProvider courseProvider;

    private InvoiceEventHandler invoiceEventHandler;

    private Subscription subscription;
    private Product product;
    private Course course;

    @BeforeEach
    void init() {
        invoiceEventHandler = new InvoiceEventHandler(stripeCommons, infraProperties, courseProvider);
        subscription = mock(Subscription.class);
        product = mock(Product.class);
        course = new Course();
        when(product.getMetadata()).thenReturn(Collections.singletonMap(PRODUCT_ID, "1"));
        doReturn(Optional.of(course)).when(courseProvider).courseOfId(1);
    }

    @Test
    void shouldNotCancel_After3Payments_ifMentored() throws StripeException, ZerofiltreException {
        //arrange
        course.setMentored(true);

        //act
        invoiceEventHandler.cancelFor3TimesPayment(null, null, false, subscription, 3, product);

        //assert
        verify(subscription, times(0)).cancel();

    }

    @Test
    void shouldCancel_After3Payments_ifMentored() throws StripeException, ZerofiltreException {
        //arrange

        //act
        invoiceEventHandler.cancelFor3TimesPayment(new Event(), "userId", false, subscription, 3, product);

        //assert
        verify(subscription, times(1)).cancel();

    }

}