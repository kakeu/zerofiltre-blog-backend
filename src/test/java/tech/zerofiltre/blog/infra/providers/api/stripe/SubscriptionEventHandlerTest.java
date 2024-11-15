package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.PlanRetrieveParams;
import com.stripe.param.SubscriptionRetrieveParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.features.enrollment.Suspend;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionEventHandlerTest {

    @Mock
    Suspend suspend;

    @Mock
    Event event;

    @Mock
    PurchaseProvider purchaseProvider;

    @Mock
    ChapterProvider chapterProvider;

    @Mock
    EnrollmentProvider enrollmentProvider;

    @Mock
    Customer customer;

    @Mock
    Plan plan;

    @Mock
    Product product;

    @Mock
    Subscription subscription;

    @Mock
    UserProvider userProvider;

    @Mock
    StripeCommons stripeCommons;

    @Mock
    InfraProperties infraProperties;

    SubscriptionEventHandler eventHandler;

    @BeforeEach
    void init() {
        eventHandler = new SubscriptionEventHandler(enrollmentProvider, chapterProvider, purchaseProvider, userProvider, stripeCommons, infraProperties, null);
        ReflectionTestUtils.setField(eventHandler, "suspend", suspend);
    }

    @DisplayName("if the user is PRO and the subscription is canceled before the third payment then must transform enrollment to PRO")
    @Test
    void mustTransform_EnrollmentToPro_forUserPro() throws ZerofiltreException, StripeException {

        try (MockedStatic<Subscription> mockedStatic = Mockito.mockStatic(Subscription.class)) {
            //arrange
            mockedStatic.when(() -> Subscription.retrieve(anyString(), any(SubscriptionRetrieveParams.class), eq(null)))
                    .thenReturn(subscription);

            try (MockedStatic<Plan> mockedStaticPlan = Mockito.mockStatic(Plan.class)) {
                mockedStaticPlan.when(() -> Plan.retrieve(anyString(), any(PlanRetrieveParams.class), eq(null)))
                        .thenReturn(plan);

                when(subscription.getId()).thenReturn("subscriptionId");
                when(subscription.getCustomerObject()).thenReturn(customer);
                when(subscription.getMetadata()).thenReturn(Collections.emptyMap());

                when(plan.getId()).thenReturn("8");
                when(plan.getProductObject()).thenReturn(product);

                long userId15 = 15;
                long courseId3 = 3;

                Map<String, String> metadataProduct = new HashMap<>();
                metadataProduct.put(PRODUCT_ID, String.valueOf(courseId3));

                when(product.getMetadata()).thenReturn(metadataProduct);

                Map<String, String> metadataCustomer = new HashMap<>();
                metadataCustomer.put(USER_ID, String.valueOf(userId15));
                when(customer.getMetadata()).thenReturn(metadataCustomer);

                SubscriptionItem subscriptionItem = new SubscriptionItem();
                subscriptionItem.setPlan(plan);

                List<SubscriptionItem> data = new ArrayList<>();
                data.add(subscriptionItem);

                SubscriptionItemCollection subscriptionItemCollection = new SubscriptionItemCollection();
                subscriptionItemCollection.setData(data);

                when(subscription.getItems()).thenReturn(subscriptionItemCollection);

                User user = new User();
                user.setId(userId15);
                user.setPlan(User.Plan.PRO);
                when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

                Enrollment enrollment = new Enrollment();
                enrollment.setUser(user);
                enrollment.setCourse(new Course());
                enrollment.setActive(true);
                when(enrollmentProvider.enrollmentOf(anyLong(), anyLong(), eq(true))).thenReturn(Optional.of(enrollment));

                when(enrollmentProvider.save(any(Enrollment.class))).thenReturn(enrollment);

                doNothing().when(stripeCommons).notifyUser(any(Customer.class), anyString(), anyString());

                //act
                eventHandler.handleSubscriptionDeleted(event, subscription);

                //assert
                assertThat(enrollment.isForLife()).isEqualTo(false);

                verify(enrollmentProvider, times(1)).save(any(Enrollment.class));
                verify(suspend, times(0)).execute(anyLong(), anyLong());
                verify(suspend, times(0)).all(anyLong(), anyBoolean());
                verify(stripeCommons, times(1)).notifyUser(any(), anyString(), anyString());
            }
        }
    }

    @DisplayName("if the user is not PRO and the subscription is canceled before the third payment then must suspend enrollment on course")
    @Test
    void mustSuspend_Enrollment_forCoursePurchase() throws ZerofiltreException, StripeException {

        try (MockedStatic<Subscription> mockedStatic = Mockito.mockStatic(Subscription.class)) {
            //arrange
            when(subscription.getCustomerObject()).thenReturn(customer);
            mockedStatic.when(() -> Subscription.retrieve(anyString(), any(SubscriptionRetrieveParams.class), any()))
                    .thenReturn(subscription);

            try (MockedStatic<Plan> mockedStaticPlan = Mockito.mockStatic(Plan.class)) {
                mockedStaticPlan.when(() -> Plan.retrieve(anyString(), any(PlanRetrieveParams.class), eq(null)))
                        .thenReturn(plan);

                when(subscription.getId()).thenReturn("subscriptionId");
                when(subscription.getCustomerObject()).thenReturn(customer);
                when(subscription.getMetadata()).thenReturn(Collections.emptyMap());

                when(plan.getId()).thenReturn("8");
                when(plan.getProductObject()).thenReturn(product);

                long userId15 = 15;
                long courseId3 = 3;

                Map<String, String> metadataProduct = new HashMap<>();
                metadataProduct.put(PRODUCT_ID, String.valueOf(courseId3));

                when(product.getMetadata()).thenReturn(metadataProduct);

                Map<String, String> metadataCustomer = new HashMap<>();
                metadataCustomer.put(USER_ID, String.valueOf(userId15));
                when(customer.getMetadata()).thenReturn(metadataCustomer);

                SubscriptionItem subscriptionItem = new SubscriptionItem();
                subscriptionItem.setPlan(plan);

                List<SubscriptionItem> data = new ArrayList<>();
                data.add(subscriptionItem);

                SubscriptionItemCollection subscriptionItemCollection = new SubscriptionItemCollection();
                subscriptionItemCollection.setData(data);

                when(subscription.getItems()).thenReturn(subscriptionItemCollection);

                User user = new User();
                user.setId(userId15);
                user.setPlan(User.Plan.BASIC);

                when(userProvider.userOfId(userId15)).thenReturn(Optional.of(user));

                doNothing().when(stripeCommons).notifyUser(any(Customer.class), anyString(), anyString());

                //act
                eventHandler.handleSubscriptionDeleted(event, subscription);

                //assert
                verify(suspend, times(1)).execute(userId15, courseId3);
                verify(suspend, times(0)).all(anyLong(), anyBoolean());
                verify(stripeCommons, times(1)).notifyUser(any(), anyString(), anyString());
            }
        }
    }

    @DisplayName("if the subscription is canceled AFTER the third payment then must ignore the event")
    @Test
    void mustIgnore_DeletedSubscription_IfItIs_3TimesPaymentCancelled() throws ZerofiltreException, StripeException {

        //arrange
        Map<String, String> metadata = new HashMap<>();
        metadata.put(CANCELLED_3TIMES_PAID, Boolean.toString(true));
        when(subscription.getMetadata()).thenReturn(metadata);

        //act
        eventHandler.handleSubscriptionDeleted(event, subscription);

        //assert
        verify(suspend, times(0)).all(anyLong(), anyBoolean());
        verify(suspend, times(0)).execute(anyLong(), anyLong());

    }

    @DisplayName("if the user is not PRO and the subscription is not related to a course then must suspend all")
    @Test
    void mustSuspend_allEvents_onDeleted_BasicPlanSubscription() throws ZerofiltreException, StripeException {

        try (MockedStatic<Subscription> mockedStatic = Mockito.mockStatic(Subscription.class)) {
            //arrange
            mockedStatic.when(() -> Subscription.retrieve(anyString(), any(SubscriptionRetrieveParams.class), eq(null)))
                    .thenReturn(subscription);

            try (MockedStatic<Plan> mockedStaticPlan = Mockito.mockStatic(Plan.class)) {
                mockedStaticPlan.when(() -> Plan.retrieve(anyString(), any(PlanRetrieveParams.class), eq(null)))
                        .thenReturn(plan);

                when(subscription.getId()).thenReturn("subscriptionId");
                when(subscription.getCustomerObject()).thenReturn(customer);
                when(subscription.getMetadata()).thenReturn(Collections.emptyMap());

                when(plan.getId()).thenReturn("8");
                when(plan.getProductObject()).thenReturn(product);

                long userId15 = 15;

                Map<String, String> metadataCustomer = new HashMap<>();
                metadataCustomer.put(USER_ID, String.valueOf(userId15));
                when(customer.getMetadata()).thenReturn(metadataCustomer);

                SubscriptionItem subscriptionItem = new SubscriptionItem();
                subscriptionItem.setPlan(plan);

                List<SubscriptionItem> data = new ArrayList<>();
                data.add(subscriptionItem);

                SubscriptionItemCollection subscriptionItemCollection = new SubscriptionItemCollection();
                subscriptionItemCollection.setData(data);

                when(subscription.getItems()).thenReturn(subscriptionItemCollection);

                User user = new User();
                user.setId(userId15);
                user.setPlan(User.Plan.BASIC);
                when(userProvider.userOfId(userId15)).thenReturn(Optional.of(user));

                doNothing().when(stripeCommons).notifyUser(any(Customer.class), anyString(), anyString());

                //act
                eventHandler.handleSubscriptionDeleted(event, subscription);

                //assert
                verify(suspend, times(1)).all(userId15, false);
                verify(suspend, times(0)).execute(anyLong(), anyLong());
                verify(stripeCommons, times(1)).notifyUser(any(), anyString(), anyString());
            }
        }
    }

    @DisplayName("if the user is PRO and the subscription is not related to a course then must suspend all and bring the user back to basic")
    @Test
    void mustSuspend_allEnrollments_onDeleted_ProPlanSubscription() throws ZerofiltreException, StripeException {

        try (MockedStatic<Subscription> mockedStatic = Mockito.mockStatic(Subscription.class)) {
            //arrange
            mockedStatic.when(() -> Subscription.retrieve(anyString(), any(SubscriptionRetrieveParams.class), eq(null)))
                    .thenReturn(subscription);

            try (MockedStatic<Plan> mockedStaticPlan = Mockito.mockStatic(Plan.class)) {
                mockedStaticPlan.when(() -> Plan.retrieve(anyString(), any(PlanRetrieveParams.class), eq(null)))
                        .thenReturn(plan);

                when(subscription.getId()).thenReturn("subscriptionId");
                when(subscription.getCustomerObject()).thenReturn(customer);
                when(subscription.getMetadata()).thenReturn(Collections.emptyMap());

                when(plan.getId()).thenReturn("8");
                when(plan.getProductObject()).thenReturn(product);

                long userId15 = 15;

                Map<String, String> metadataCustomer = new HashMap<>();
                metadataCustomer.put(USER_ID, String.valueOf(userId15));
                when(customer.getMetadata()).thenReturn(metadataCustomer);

                SubscriptionItem subscriptionItem = new SubscriptionItem();
                subscriptionItem.setPlan(plan);

                List<SubscriptionItem> data = new ArrayList<>();
                data.add(subscriptionItem);

                SubscriptionItemCollection subscriptionItemCollection = new SubscriptionItemCollection();
                subscriptionItemCollection.setData(data);

                when(subscription.getItems()).thenReturn(subscriptionItemCollection);

                User user = new User();
                user.setId(userId15);
                user.setPlan(User.Plan.PRO);
                when(userProvider.userOfId(userId15)).thenReturn(Optional.of(user));

                when(infraProperties.getEnv()).thenReturn("dev");

                doNothing().when(stripeCommons).notifyUser(any(Customer.class), anyString(), anyString());

                //act
                eventHandler.handleSubscriptionDeleted(event, subscription);

                //assert
                assertThat(user.getPlan()).isEqualTo(User.Plan.BASIC);

                verify(suspend, times(1)).all(userId15, false);
                verify(suspend, times(0)).execute(anyLong(), anyLong());
                verify(userProvider, times(1)).save(any());
                verify(stripeCommons, times(1)).notifyUser(any(), anyString(), anyString());
            }
        }
    }

    @DisplayName("if the user is not found then must ignore the event")
    @Test
    void mustIgnore_DeletedSubscription_IfUserNotfound() throws ZerofiltreException, StripeException {
        try (MockedStatic<Subscription> mockedStatic = Mockito.mockStatic(Subscription.class)) {
            //arrange
            mockedStatic.when(() -> Subscription.retrieve(anyString(), any(SubscriptionRetrieveParams.class), eq(null)))
                    .thenReturn(subscription);

            when(subscription.getId()).thenReturn("subscriptionId");
            when(subscription.getCustomerObject()).thenReturn(customer);
            when(subscription.getMetadata()).thenReturn(Collections.emptyMap());

            long userId15 = 15;

            Map<String, String> metadataCustomer = new HashMap<>();
            metadataCustomer.put(USER_ID, String.valueOf(userId15));
            when(customer.getMetadata()).thenReturn(metadataCustomer);

            when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

            //act
            eventHandler.handleSubscriptionDeleted(event, subscription);

            //assert
            verify(suspend, times(0)).all(anyLong(), anyBoolean());
            verify(suspend, times(0)).execute(anyLong(), anyLong());
            verify(stripeCommons, times(0)).notifyUser(any(), anyString(), anyString());
        }
    }

}