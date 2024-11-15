package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.PlanRetrieveParams;
import com.stripe.param.SubscriptionRetrieveParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.features.enrollment.Suspend;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static tech.zerofiltre.blog.infra.providers.api.stripe.StripeCommons.*;

@Slf4j
@Component
public class SubscriptionEventHandler {

    private final Suspend suspend;
    private final UserProvider userProvider;
    private final EnrollmentProvider enrollmentProvider;
    private final StripeCommons stripeCommons;
    private final InfraProperties infraProperties;

    public SubscriptionEventHandler(EnrollmentProvider enrollmentProvider, ChapterProvider chapterProvider, PurchaseProvider purchaseProvider, UserProvider userProvider, StripeCommons stripeCommons, InfraProperties infraProperties, SandboxProvider sandboxProvider) {
        this.userProvider = userProvider;
        this.enrollmentProvider = enrollmentProvider;
        this.stripeCommons = stripeCommons;
        this.infraProperties = infraProperties;
        this.suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider);
    }

    public void handleSubscriptionDeleted(Event event, Subscription subscription) throws ZerofiltreException, StripeException {
        Map<String, String> metadata = subscription.getMetadata();
        if (metadata.containsKey(CANCELLED_3TIMES_PAID) && Boolean.toString(true).equals(metadata.get(CANCELLED_3TIMES_PAID)))
            return;

        SubscriptionRetrieveParams subscriptionRetrieveParams = SubscriptionRetrieveParams.builder()
                .addExpand("customer")
                .build();
        subscription = Subscription.retrieve(subscription.getId(), subscriptionRetrieveParams, null);

        Customer customer = subscription.getCustomerObject();
        log.debug("EventId= {}, EventType={}, Customer: {}", event.getId(), event.getType(), customer != null ? customer.toString().replace("\n", " ") : "no customer provided");

        String userId = customer != null && customer.getMetadata() != null ? customer.getMetadata().get(USER_ID) : "";
        log.debug("EventId= {}, EventType={}, User id: {}", event.getId(), event.getType(), userId);

        long userIdLong = Long.parseLong(userId);
        Optional<User> foundUser = userProvider.userOfId(userIdLong);

        if (foundUser.isPresent()) {
            User user = foundUser.get();
            boolean isPro = user.isPro();
            String courseId = getCourseIdFromSubscription(subscription);

            if (isPro) {
                handleProUser(userIdLong, courseId, user);
            } else {
                handleNonProUser(userIdLong, courseId);
            }

            String message = generateMessage(isPro, courseId.isEmpty());
            stripeCommons.notifyUser(customer, "Subscription deleted", message);
        }
    }

    private static String getCourseIdFromSubscription(Subscription subscription) throws StripeException {

        SubscriptionItemCollection items = subscription.getItems();
        List<SubscriptionItem> data = items.getData();
        SubscriptionItem subscriptionItem = data.get(0);
        Plan plan = subscriptionItem.getPlan();

        PlanRetrieveParams retrieveParams = PlanRetrieveParams.builder()
                .addExpand("product")
                .build();
        plan = Plan.retrieve(plan.getId(), retrieveParams, null);

        Product productObject = plan.getProductObject();

        String productId = "";
        if(productObject != null && productObject.getMetadata().containsKey(PRODUCT_ID)){
            productId = productObject.getMetadata().get(PRODUCT_ID);
        }

        log.debug("Product id: {}", productId);

        return productId;
    }

    private void handleProUser(Long userIdLong, String courseId, User user) throws ZerofiltreException {
        if (courseId.isEmpty()) {
            suspend.all(userIdLong, false);
            user.setPlan(User.Plan.BASIC);
            userProvider.save(user);
        } else {
            Optional<Enrollment> enrollment = enrollmentProvider.enrollmentOf(userIdLong, Long.parseLong(courseId), true);
            if (enrollment.isPresent()) {
                enrollment.get().setForLife(false);
                enrollmentProvider.save(enrollment.get());
            }
        }
    }

    private void handleNonProUser(Long userIdLong, String courseId) throws ZerofiltreException {
        if (courseId.isEmpty()) {
            suspend.all(userIdLong, false);
        } else {
            suspend.execute(userIdLong, Long.parseLong(courseId));
        }
    }

    private String generateMessage(boolean isPro, boolean isCourseIdEmpty) {
        if (isPro) {
            if (isCourseIdEmpty) {
                return "Vous avez annulé votre abonnement Pro par conséquent vous n'avez plus accès au contenu de la plateforme." +
                        "\n Vous pouvez vous réabonner en utilisant le lien suivant: " + ZerofiltreUtils.getOriginUrl(infraProperties.getEnv()) + "/pro";
            } else {
                return "Vous avez annulé l'achat d'un cours avant le paiement complet." +
                        "\n Cependant vous avez souscrit à l'abonnement Pro qui vous permet d'accéder à ce cours tant que vous restez Pro.";
            }
        } else {
            if (isCourseIdEmpty) {
                return "Vous avez annulé votre abonnement à tous les cours en tant que Pro." +
                        "\n Vous avez cependant accès à tous les cours achetés individuellement";
            } else {
                return "Vous avez annulé l'achat d'un cours avant le paiement complet." +
                        "\n Vous ne pouvez plus suivre ce cours." +
                        "\n Si vous souhaitez réactiver le paiement contactez-nous par email: " + infraProperties.getContactEmail();
            }
        }
    }
}
