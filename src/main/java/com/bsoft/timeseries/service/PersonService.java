package com.bsoft.timeseries.service;

import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.entity.PersonEntity;
import com.bsoft.timeseries.exception.ResourceNotFoundException;
import com.bsoft.timeseries.mapper.PersonMapper;
import com.bsoft.timeseries.repository.PersonRepository;
import com.bsoft.timeseries.timeseries.model.Person;
import com.bsoft.timeseries.timeseries.model.PersonPage;
import com.bsoft.timeseries.timeseries.model.PersonRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Bitemporal person service following the Snodgrass update protocol:
 * 1. Close current transaction version (transactionTo = now)
 * 2. Insert new version (transactionFrom = now, transactionTo = INFINITY)
 * <p>
 * NOTE: entity construction uses new + setters, NOT the Lombok builder,
 * because @Builder on a subclass only exposes fields declared in that
 * subclass. Inherited BitemporalEntity fields (uid, validFrom, …) are
 * invisible to the builder and cause "cannot find symbol" compile errors.
 */
@Service
@Slf4j
@Transactional
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonService(PersonRepository personRepository,
                         @Qualifier("personMapperImpl") PersonMapper personMapper) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }


    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    public Person create(PersonRequest request) {
        PersonEntity entity = personMapper.toNewEntity(request);
        // @PrePersist sets uid / transactionFrom / transactionTo / createdAt
        if (entity.getValidFrom() == null) entity.setValidFrom(OffsetDateTime.now());
        if (entity.getValidTo() == null) entity.setValidTo(BitemporalEntity.INFINITY);

        PersonEntity saved = personRepository.save(entity);
        log.info("Created person uid={}", saved.getUid());
        return personMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public Person findAtPoint(UUID uid, OffsetDateTime validAt, OffsetDateTime transactionAt) {
        return personRepository.findAtPoint(uid, validAt, transactionAt)
                .map(personMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Person", uid));
    }

    @Transactional(readOnly = true)
    public PersonPage listAtPoint(OffsetDateTime validAt, OffsetDateTime transactionAt,
                                  int page, int size) {
        Page<PersonEntity> p = personRepository.findAllAtPoint(
                validAt, transactionAt, PageRequest.of(page, size));
        PersonPage result = new PersonPage();
        result.setContent(personMapper.toDtoList(p.getContent()));
        result.setTotalElements(p.getTotalElements());
        result.setTotalPages(p.getTotalPages());
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Person> history(UUID uid) {
        List<PersonEntity> versions = personRepository.findAllVersions(uid);
        if (versions.isEmpty()) throw new ResourceNotFoundException("Person", uid);
        return personMapper.toDtoList(versions);
    }

    // ----------------------------------------------------------------
    // UPDATE — new transaction-time version
    // ----------------------------------------------------------------

    public Person update(UUID uid, PersonRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        PersonEntity current = personRepository.findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Person", uid));

        // 1. Close current transaction version
        personRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        // 2. Insert new version — use setters so inherited fields are reachable
        PersonEntity next = new PersonEntity();
        next.setUid(uid);
        next.setFirstName(request.getFirstName() != null
                ? request.getFirstName() : current.getFirstName());
        next.setLastName(request.getLastName() != null
                ? request.getLastName() : current.getLastName());
        // getDateOfBirth() returns LocalDate directly (format: date + java8 dateLibrary)
        next.setDateOfBirth(request.getDateOfBirth() != null
                ? request.getDateOfBirth() : current.getDateOfBirth());
        next.setValidFrom(request.getValidFrom() != null
                ? request.getValidFrom() : current.getValidFrom());
        next.setValidTo(request.getValidTo() != null
                ? request.getValidTo() : current.getValidTo());
        next.setTransactionFrom(now);
        next.setTransactionTo(BitemporalEntity.INFINITY);

        PersonEntity saved = personRepository.save(next);
        log.info("Updated person uid={}", uid);
        return personMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // TERMINATE
    // ----------------------------------------------------------------

    public void terminate(UUID uid, OffsetDateTime validTo) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        PersonEntity current = personRepository.findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Person", uid));

        personRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        PersonEntity terminated = new PersonEntity();
        terminated.setUid(uid);
        terminated.setFirstName(current.getFirstName());
        terminated.setLastName(current.getLastName());
        terminated.setDateOfBirth(current.getDateOfBirth());
        terminated.setValidFrom(current.getValidFrom());
        terminated.setValidTo(effectiveTo);
        terminated.setTransactionFrom(now);
        terminated.setTransactionTo(BitemporalEntity.INFINITY);

        personRepository.save(terminated);
        log.info("Terminated person uid={} validTo={}", uid, effectiveTo);
    }
}