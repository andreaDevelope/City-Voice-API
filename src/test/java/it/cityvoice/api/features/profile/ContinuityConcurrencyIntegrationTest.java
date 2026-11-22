package it.cityvoice.api.features.profile;

import it.cityvoice.api.IntegrationTestBase;
import it.cityvoice.api.features.profile.badges.entity.Badge;
import it.cityvoice.api.features.profile.badges.services.BadgeServ;
import it.cityvoice.api.features.profile.categories.entity.Category;
import it.cityvoice.api.features.profile.categories.services.CategoryServ;
import it.cityvoice.api.features.profile.user_badge.repositories.UserBadgeRepo;
import it.cityvoice.api.features.profile.user_rome.entity.UserRome;
import it.cityvoice.api.features.profile.user_rome.repositories.UserRomeRepo;
import it.cityvoice.api.features.profile.user_rome.services.UserRomeServ;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

// No @Transactional: the two threads need real, separate transactions that commit
// independently, not two participants sharing the test's own open transaction.
class ContinuityConcurrencyIntegrationTest extends IntegrationTestBase {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Autowired
    private UserRomeServ userRomeServ;

    @Autowired
    private UserRomeRepo userRomeRepo;

    @Autowired
    private CategoryServ categoryServ;

    @Autowired
    private BadgeServ badgeServ;

    @Autowired
    private UserBadgeRepo userBadgeRepo;

    @Test
    @DisplayName("due accessi paralleli al primo accesso del giorno fanno salire la serie una volta sola")
    void parallelFirstAccessesOfTheDayCountOnce() throws Exception {
        TestUser testUser = registerUser();

        Category continuity = categoryServ.getCategoryByName("continuity");
        Badge dayTwo = badgeServ.getAllBadgesForCategory(continuity.getId()).stream()
                .filter(badge -> badge.getMissionThreshold() == 2)
                .findFirst()
                .orElseThrow();

        // one day short of the "Giorno 2" badge: the next access should both
        // increment the streak to 2 and unlock it, exactly once
        UserRome userRome = userRomeRepo.findByAppUserId(testUser.appUserId());
        userRome.setLastActiveDate(LocalDate.now(ROME).minusDays(1));
        userRome.setContinuityCounter(1);
        userRomeRepo.save(userRome);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);
        Callable<UserRome> accessTask = () -> {
            startSignal.await();
            return userRomeServ.findByUsername(testUser.username());
        };

        Future<UserRome> first = executor.submit(accessTask);
        Future<UserRome> second = executor.submit(accessTask);
        startSignal.countDown();

        // get() rethrows anything thrown inside the thread, failing the test on any exception
        first.get(10, TimeUnit.SECONDS);
        second.get(10, TimeUnit.SECONDS);
        executor.shutdown();

        UserRome reloaded = userRomeRepo.findByAppUserId(testUser.appUserId());
        assertEquals(2, reloaded.getContinuityCounter());
        assertEquals(LocalDate.now(ROME), reloaded.getLastActiveDate());

        List<Long> unlockedCount = userBadgeRepo.findByUserRomeAndBadgeIdIn(reloaded, List.of(dayTwo.getId()))
                .stream().map(userBadge -> userBadge.getId()).toList();
        assertEquals(1, unlockedCount.size());
    }
}
