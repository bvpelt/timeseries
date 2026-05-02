package com.bsoft.timeseries.service;

import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.entity.PersonEntity;
import com.bsoft.timeseries.exception.ResourceNotFoundException;
import com.bsoft.timeseries.mapper.PersonMapper;
import com.bsoft.timeseries.model.Person;
import com.bsoft.timeseries.model.PersonPage;
import com.bsoft.timeseries.model.PersonRequest;
import com.bsoft.timeseries.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for person management using bitemporal semantics.
 *
 * <h2>Bitemporal update protocol (Snodgrass §8)</h2>
 * <ol>
 *   <li>Close the current transaction-time version by setting
 *       {@code transactionTo = now}.</li>
 *   <li>Insert a new row with {@code transactionFrom = now},
 *       {@code transactionTo = INFINITY}, carrying the updated
 *       attributes and the caller-supplied valid time.</li>
 * </ol>
 * This preserves the full audit trail — no row is ever deleted or modified
 * after initial insert.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PersonService {

    private final PersonRepository  personRepository;
    private final PersonMapper      personMapper;

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    /**
     * Creates a new person with the given attributes.
     * Valid time defaults to {@code now → INFINITY} unless the caller
     * supplies explicit bounds in the request.
     */
    public Person create(PersonRequest request) {
        PersonEntity entity = personMapper.toNewEntity(request);

        // Apply valid-time defaults if not provided
        if (entity.getValidFrom() == null) entity.setValidFrom(OffsetDateTime.now());
        if (entity.getValidTo()   == null) entity.setValidTo(BitemporalEntity.INFINITY);

        // Transaction time is always set by @PrePersist
        PersonEntity saved = personRepository.save(entity);
        log.info("Created person uid={}", saved.getUid());
        return personMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // READ — point-in-time
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public Person findAtPoint(UUID uid,
                              OffsetDateTime validAt,
                              OffsetDateTime transactionAt) {
        return personRepository.findAtPoint(uid, validAt, transactionAt)
                .map(personMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Person", uid));
    }

    @Transactional(readOnly = true)
    public PersonPage listAtPoint(OffsetDateTime validAt,
                                  OffsetDateTime transactionAt,
                                  int page, int size) {
        Page<PersonEntity> entityPage = personRepository.findAllAtPoint(
                validAt, transactionAt, PageRequest.of(page, size));

        PersonPage result = new PersonPage();
        result.setContent(personMapper.toDtoList(entityPage.getContent()));
        result.setTotalElements(entityPage.getTotalElements());
        result.setTotalPages(entityPage.getTotalPages());
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    // ----------------------------------------------------------------
    // HISTORY
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Person> history(UUID uid) {
        List<PersonEntity> versions = personRepository.findAllVersions(uid);
        if (versions.isEmpty()) {
            throw new ResourceNotFoundException("Person", uid);
        }
        return personMapper.toDtoList(versions);
    }

    // ----------------------------------------------------------------
    // UPDATE — bitemporal correction
    // ----------------------------------------------------------------

    /**
     * Updates a person by closing the current transaction version and inserting
     * a new one. The caller may supply new valid-time bounds; if omitted,
     * the existing bounds are carried forward.
     */
    public Person update(UUID uid, PersonRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        // 1. Verify entity exists (current transaction-time version)
        PersonEntity current = personRepository
                .findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Person", uid));

        // 2. Close the current transaction version
        personRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        // 3. Build new version copying the current entity, then apply request delta
        PersonEntity next = PersonEntity.builder()
                .uid(uid)
                .firstName(request.getFirstName() != null
                        ? request.getFirstName() : current.getFirstName())
                .lastName(request.getLastName() != null
                        ? request.getLastName() : current.getLastName())
                .dateOfBirth(request.getDateOfBirth() != null
                        ? java.time.LocalDate.parse(request.getDateOfBirth())
                        : current.getDateOfBirth())
                .validFrom(request.getValidFrom() != null
                        ? request.getValidFrom() : current.getValidFrom())
                .validTo(request.getValidTo() != null
                        ? request.getValidTo() : current.getValidTo())
                .transactionFrom(now)
                .transactionTo(BitemporalEntity.INFINITY)
                .build();

        PersonEntity saved = personRepository.save(next);
        log.info("Updated person uid={} — new transaction version", uid);
        return personMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // TERMINATE (valid-time close)
    // ----------------------------------------------------------------

    /**
     * Ends the valid-time period of a person. The existing transaction-time
     * row is replaced with a new version that has {@code validTo} set.
     * This models the real-world fact ending, not an error correction.
     */
    public void terminate(UUID uid, OffsetDateTime validTo) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        PersonEntity current = personRepository
                .findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Person", uid));

        // Close old version
        personRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        // New version with restricted valid_to
        PersonEntity terminated = PersonEntity.builder()
                .uid(uid)
                .firstName(current.getFirstName())
                .lastName(current.getLastName())
                .dateOfBirth(current.getDateOfBirth())
                .validFrom(current.getValidFrom())
                .validTo(effectiveTo)
                .transactionFrom(now)
                .transactionTo(BitemporalEntity.INFINITY)
                .build();

        personRepository.save(terminated);
        log.info("Terminated person uid={} validTo={}", uid, effectiveTo);
    }
}