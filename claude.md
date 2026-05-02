# Prompt

```text
You are an experienced java software developer with extended knowledge of:
- spring-boot version 4
- spring-security
- postgresql version 18
- software development best practices for instance from https://www.netguru.com/blog/best-software-development-practices
- software design patterns for instance from https://refactoring.guru/design-patterns/catalog
- enterprise design patterns for instance from https://www.enterpriseintegrationpatterns.com/
- maintenance of software applications
- bitemporal database usage as described by Richard SnodGrass
```

```text
I want to build a spring-boot java service which is secure, maintainable and has a well defined API interface to

add person information
add adres information and create a n-m relation between person and adres
add an arrangement and create a n-m relation between person and adres Each entity and relation of person,adres and arrangement and all relations should have the possibility to use timetraveling in a bi-temporal database.
The api should be defined using an openapi specification.
The api should have api keys which restrict acces to read, read+write, noacces
Services shoud be build using the openapi specification file.

A person has:

first name
last name
date of birth
unique identification
An adres has:

street
housenumber
postal code
city
unique identification
An agreement has:

unique identification
title
description
state (new, changed, ready)
A person can have a home and 0..n work adresses
Several persons can share the same adress
A person can have several (0..n) agreements
An agreement can be made by (0..n) persons

I have setup an empty spring-boot project with the pom.xml consisting of:
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>4.0.6</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.bsoft</groupId>
	<artifactId>timeseries</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name/>
	<description/>
	<url/>
	<licenses>
		<license/>
	</licenses>
	<developers>
		<developer/>
	</developers>
	<scm>
		<connection/>
		<developerConnection/>
		<tag/>
		<url/>
	</scm>
	<properties>
		<java.version>21</java.version>
	</properties>
	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc</artifactId>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<configuration>
					<excludes>
						<exclude>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</exclude>
					</excludes>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<executions>
					<execution>
						<id>default-compile</id>
						<phase>compile</phase>
						<goals>
							<goal>compile</goal>
						</goals>
						<configuration>
							<annotationProcessorPaths>
								<path>
									<groupId>org.projectlombok</groupId>
									<artifactId>lombok</artifactId>
								</path>
							</annotationProcessorPaths>
						</configuration>
					</execution>
					<execution>
						<id>default-testCompile</id>
						<phase>test-compile</phase>
						<goals>
							<goal>testCompile</goal>
						</goals>
						<configuration>
							<annotationProcessorPaths>
								<path>
									<groupId>org.projectlombok</groupId>
									<artifactId>lombok</artifactId>
								</path>
							</annotationProcessorPaths>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

</project>
```

```text
Generate the code for:
- the openapi specification of the spring-boot service
- additions to pom.xml to generate the services
- the spring-boot services
- the database definition with bi-temporal additions
- the code to insert, query and update data from the service to the database
- the security configuration and code needed to use api key's
```

# Response

This is a comprehensive request for a Spring Boot application with bitemporal database support. Let me plan out all the components needed:

- OpenAPI Specification - defining all endpoints
- Updated pom.xml - adding necessary dependencies and OpenAPI generator plugin
- Database Schema - PostgreSQL with bitemporal tables (valid_time + transaction_time)
- Spring Boot Services - implementing the generated interfaces
- Security Configuration - API key based security
- Repository/Data Access Layer - for bitemporal queries

## Let me think through the architecture:
Bitemporal Design (Snodgrass approach)

- Valid Time (VT): When the fact was true in reality (valid_from, valid_to)
- Transaction Time (TT): When the fact was recorded in the database (transaction_from, transaction_to)
- Use TIMESTAMP WITH TIME ZONE for all temporal columns
- Use infinity for open-ended periods

## Entities

- Person - first_name, last_name, date_of_birth, unique_id
- Address - street, house_number, postal_code, city, unique_id
- Agreement - title, description, state (enum), unique_id

## Relations

- PersonAddress - N:M with type (HOME, WORK)
- PersonAgreement - N:M

## API Design

- CRUD for each entity
- Time travel queries (at a specific valid_time and/or transaction_time)
- Relation management endpoints

## Security

- API Keys with roles: READ, READ_WRITE, NO_ACCESS
- Store API keys in database

## Tech Stack additions needed

- Spring Data JPA or JDBC
- Flyway for migrations
- OpenAPI Generator Maven Plugin
- MapStruct for mapping
- SpringDoc for OpenAPI

## Let me now write out all the files:

- openapi.yaml - comprehensive OpenAPI spec
- pom.xml additions
- Database migration scripts (Flyway)
- Security configuration
- Service implementations
- Repository layer

This is quite extensive. Let me structure it carefully.
For the OpenAPI spec, I need endpoints:

POST /persons - create person
…

## Create directories
mkdir -p src/main/resources/openapi
mkdir -p src/main/resources/db/migration
mkdir -p src/main/java/com/bsoft/timeseries/config
mkdir -p src/main/java/com/bsoft/timeseries/security
mkdir -p src/main/java/com/bsoft/timeseries/entity
mkdir -p src/main/java/com/bsoft/timeseries/repository
mkdir -p src/main/java/com/bsoft/timeseries/service
mkdir -p src/main/java/com/bsoft/timeseries/delegate
mkdir -p src/main/java/com/bsoft/timeseries/mapper
mkdir -p src/main/java/com/bsoft/timeseries/exception


## Summary

What was generated

### Security (completed)

| File                | Role                                                                               |
|---------------------|------------------------------------------------------------------------------------|
| ApiKeyPermission    | Enum: READ, READ_WRITE, NO_ACCESS                                                  |
| ApiKeyServiceHashes | the raw key (SHA-256) and looks it up in the DB                                    |
| ApiKeyAuthFilter    | OncePerRequestFilter — reads X-API-Key header, populates SecurityContext           | 
| SecurityConfig      | Stateless Spring Security 6 chain: GETs require READ, mutations require READ_WRITE |

### Entities (7 files)
- BitemporalEntity is the shared @MappedSuperclass carrying validFrom/To + transactionFrom/To. All domain entities extend it: 
- PersonEntity
- AddressEntity
- AgreementEntity
- PersonAddressEntity
- PersonAgreementEntity
- ApiKeyEntity.

### Repositories (7 files)
- BitemporalRepository<T> is a @NoRepositoryBean interface with JPQL for the four core bitemporal operations — point-in-time lookup, paginated listing, transaction-close, and full history. Each domain repository extends it; relation repositories add link-specific queries.

### Services (4 files)
Each service follows the Snodgrass update protocol: close the current transaction-time row (transactionTo = now), then insert a new row (transactionFrom = now). This guarantees no row is ever mutated after creation, preserving the full audit trail.

### Mappers + Delegates (9 files)
MapStruct mappers convert entities ↔ generated OpenAPI DTOs (mapping the infinity sentinel to null in responses). The four *ApiDelegateImpl classes plug into the OpenAPI-generated controller stubs, keeping hand-written code fully separated from generated code.

### Key design decisions to be aware of

- @MappingTarget on updateEntity — MapStruct in-place update is defined but the services use the builder pattern for new versions to keep transaction-time immutability explicit.
- PersonAddressEntity JPA join — the address field uses insertable=false, updatable=false to allow lazy loading of the full address in relation responses without creating write conflicts.
- Before running, make sure your PostgreSQL database exists and the env vars SPRING_DATASOURCE_URL/USERNAME/PASSWORD are set — Flyway will apply V1 and V2 automatically on startup.