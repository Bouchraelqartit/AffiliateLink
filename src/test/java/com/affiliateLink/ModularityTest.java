
package com.affiliateLink;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTest {

    static ApplicationModules modules =
            ApplicationModules.of(AffiliatelinkApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.forEach(System.out::println);
        modules.verify();
    }
}
