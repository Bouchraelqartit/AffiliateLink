package com.affiliateLink;

import com.affiliateLink.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "spring.docker.compose.enabled=false")
@Import(TestcontainersConfiguration.class)
class AffiliatelinkApplicationIT {

	@Test
	void contextLoads() {
	}
}