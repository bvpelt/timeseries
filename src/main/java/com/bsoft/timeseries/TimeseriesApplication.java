package com.bsoft.timeseries;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * @EnableJpaAuditing is declared in JpaConfig (with dateTimeProviderRef = "utcDateTimeProvider").
 * Declaring it here a second time causes a duplicate 'jpaAuditingHandler' bean and a
 * BeanDefinitionOverrideException at startup.
 *
 * @EnableJpaRepositories and @EnableTransactionManagement are auto-configured by
 * Spring Boot's JPA auto-configuration, so they are not needed here either.
 */
@SpringBootApplication
public class TimeseriesApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeseriesApplication.class, args);
	}
}