package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.course.features.enrollment.Enroll;
import tech.zerofiltre.blog.domain.course.features.enrollment.Suspend;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.PRODUCT_ID;

class StripeCommonsTest {
    @Mock
    private UserProvider userProvider;

    @Mock
    private Enroll enroll;

    @Mock
    private Suspend suspend;

    @Mock
    private InfraProperties infraProperties;

    @InjectMocks
    private StripeCommons stripeCommons;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(stripeCommons, "suspend", suspend);
        ReflectionTestUtils.setField(stripeCommons, "enroll", enroll);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void fulfillOrderWithNullProduct_doesNothing() throws ZerofiltreException {
        // Arrange
        String userId = "123";
        com.stripe.model.Product product = null;
        Event event = mock(Event.class);
        Customer customer = mock(Customer.class);

        // Act
        stripeCommons.fulfillOrder(userId, product, true, event, customer);

        // Assert
        verify(userProvider, times(0)).userOfId(anyLong());
    }

    @Test
    void proPlan_WithSuccessfulPayment_setUserToPRO() throws ZerofiltreException {
        // Arrange
        String userId = "123";
        com.stripe.model.Product product = mock(Product.class);
        Event event = mock(Event.class);
        Customer customer = mock(Customer.class);
        User user = mock(User.class);


        // Mock responses from userProvider, courseProvider, etc.
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.ofNullable(user));
        when(infraProperties.getProPlanProductId()).thenReturn("pro_plan_id");
        when(product.getId()).thenReturn("pro_plan_id");
        when(customer.getEmail()).thenReturn("payment_email");
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        // Act
        stripeCommons.fulfillOrder(userId, product, true, event, customer);

        // Assert
        // Verify user plan is set to PRO, user is enrolled, etc.
        verify(user, times(1)).setPlan(User.Plan.PRO);
        verify(user, times(1)).setPaymentEmail("payment_email");
        verify(userProvider, times(1)).save(user);
    }

    @Test
    void proPlan_WithUnSuccessfulPayment_setUserToBasic_andSuspendPROEnrollments() throws ZerofiltreException {
        // Arrange
        String userId = "123";
        com.stripe.model.Product product = mock(Product.class);
        Event event = mock(Event.class);
        Customer customer = mock(Customer.class);
        User user = mock(User.class);


        // Mock responses from userProvider, courseProvider, etc.
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.ofNullable(user));
        when(infraProperties.getProPlanProductId()).thenReturn("pro_plan_id");
        when(product.getId()).thenReturn("pro_plan_id");
        when(customer.getEmail()).thenReturn("payment_email");
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        // Act
        stripeCommons.fulfillOrder(userId, product, false, event, customer);

        // Assert
        // Verify user plan is set to PRO, user is enrolled, etc.
        verify(user, times(1)).setPlan(User.Plan.BASIC);
        verify(suspend, times(1)).all(Long.parseLong(userId), false);
        verify(user, times(1)).setPaymentEmail("payment_email");
        verify(userProvider, times(1)).save(user);
    }

    @Test
    void unSuccessfulPayment_suspendsEnrollment() throws ZerofiltreException {
        // Arrange
        String userId = "123";
        com.stripe.model.Product product = mock(Product.class);
        Event event = mock(Event.class);
        Customer customer = mock(Customer.class);
        User user = mock(User.class);


        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        when(infraProperties.getProPlanProductId()).thenReturn("pro_plan_id");

        when(product.getId()).thenReturn("not_pro_plan_id");
        when(product.getMetadata()).thenReturn(Collections.singletonMap(PRODUCT_ID, "25"));

        when(customer.getEmail()).thenReturn("payment_email");


        // Act
        stripeCommons.fulfillOrder(userId, product, false, event, customer);

        // Assert
        verify(suspend, times(1)).execute(Long.parseLong(userId), 25);
    }

}