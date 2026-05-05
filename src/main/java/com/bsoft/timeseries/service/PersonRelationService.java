package com.bsoft.timeseries.service;

import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.entity.PersonAddressEntity;
import com.bsoft.timeseries.entity.PersonAgreementEntity;
import com.bsoft.timeseries.exception.ConflictException;
import com.bsoft.timeseries.exception.ResourceNotFoundException;
import com.bsoft.timeseries.mapper.PersonAddressMapper;
import com.bsoft.timeseries.mapper.PersonAgreementMapper;
import com.bsoft.timeseries.model.*;
import com.bsoft.timeseries.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Manages bitemporal N:M relations: Person↔Address and Person↔Agreement.
 * <p>
 * Entity construction uses new + setters throughout (not the Lombok builder)
 * because @Builder on a subclass does not expose inherited BitemporalEntity
 * fields. See PersonService for full explanation.
 * <p>
 * The generated model fields addressUid and agreementUid are typed UUID
 * (format: uuid + dateLibrary=java8), so UUID.fromString() must NOT be called.
 */
@Service
@Slf4j
@Transactional
public class PersonRelationService {

    private final PersonRepository personRepository;
    private final AddressRepository addressRepository;
    private final AgreementRepository agreementRepository;
    private final PersonAddressRepository personAddressRepository;
    private final PersonAgreementRepository personAgreementRepository;
    private final PersonAddressMapper personAddressMapper;
    private final PersonAgreementMapper personAgreementMapper;

    public PersonRelationService(
            PersonRepository personRepository,
            AddressRepository addressRepository,
            AgreementRepository agreementRepository,
            PersonAddressRepository personAddressRepository,
            PersonAgreementRepository personAgreementRepository,
            @Qualifier("personAddressMapperImpl") PersonAddressMapper personAddressMapper,
            @Qualifier("personAgreementMapperImpl") PersonAgreementMapper personAgreementMapper
    ) {
        this.personRepository = personRepository;
        this.addressRepository = addressRepository;
        this.agreementRepository = agreementRepository;
        this.personAddressRepository = personAddressRepository;
        this.personAgreementRepository = personAgreementRepository;
        this.personAddressMapper = personAddressMapper;
        this.personAgreementMapper = personAgreementMapper;
    }
    // ==================================================================
    // PERSON ↔ ADDRESS
    // ==================================================================

    @Transactional(readOnly = true)
    public List<PersonAddress> getPersonAddresses(UUID personUid,
                                                  OffsetDateTime validAt,
                                                  OffsetDateTime transactionAt) {
        assertPersonExists(personUid, validAt, transactionAt);
        return personAddressMapper.toDtoList(
                personAddressRepository.findByPersonAtPoint(personUid, validAt, transactionAt));
    }

    public PersonAddress addPersonAddress(UUID personUid, PersonAddressRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        // getAddressUid() returns UUID directly — no UUID.fromString() needed
        UUID addressUid = request.getAddressUid();
        OffsetDateTime vf = request.getValidFrom() != null ? request.getValidFrom() : now;
        OffsetDateTime vt = request.getValidTo() != null ? request.getValidTo() : BitemporalEntity.INFINITY;

        assertPersonExists(personUid, now, now);
        assertAddressExists(addressUid, now, now);

        // Guard: no duplicate link
        personAddressRepository.findLinkAtPoint(personUid, addressUid, vf, now)
                .ifPresent(e -> {
                    throw new ConflictException(
                            "Person %s already has address %s linked at %s"
                                    .formatted(personUid, addressUid, vf));
                });

        // Guard: at most one HOME address per person at a time
        if (request.getAddressType() == AddressType.HOME) {
            long currentHomes = personAddressRepository
                    .countCurrentHomeAddresses(personUid, vf, BitemporalEntity.INFINITY);
            if (currentHomes > 0) {
                throw new ConflictException(
                        "Person %s already has a HOME address valid at %s."
                                .formatted(personUid, vf));
            }
        }

        PersonAddressEntity link = new PersonAddressEntity();
        link.setPersonUid(personUid);
        link.setAddressUid(addressUid);
        link.setAddressType(PersonAddressEntity.AddressType.valueOf(
                request.getAddressType().getValue()));
        link.setValidFrom(vf);
        link.setValidTo(vt);
        link.setTransactionFrom(now);
        link.setTransactionTo(BitemporalEntity.INFINITY);

        PersonAddressEntity saved = personAddressRepository.save(link);
        log.info("Linked address {} ({}) → person {}", addressUid, request.getAddressType(), personUid);
        return personAddressMapper.toDto(saved);
    }

    public void removePersonAddress(UUID personUid, UUID addressUid, OffsetDateTime validTo) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        PersonAddressEntity link =
                personAddressRepository.findLinkAtPoint(personUid, addressUid, now, now)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No active link between person %s and address %s"
                                        .formatted(personUid, addressUid)));

        personAddressRepository.closeLinkTransaction(
                personUid, addressUid, now, BitemporalEntity.INFINITY);

        PersonAddressEntity terminated = new PersonAddressEntity();
        terminated.setPersonUid(personUid);
        terminated.setAddressUid(addressUid);
        terminated.setAddressType(link.getAddressType());
        terminated.setValidFrom(link.getValidFrom());
        terminated.setValidTo(effectiveTo);
        terminated.setTransactionFrom(now);
        terminated.setTransactionTo(BitemporalEntity.INFINITY);

        personAddressRepository.save(terminated);
        log.info("Removed address link {} → person {} validTo={}", addressUid, personUid, effectiveTo);
    }

    // ==================================================================
    // PERSON ↔ AGREEMENT
    // ==================================================================

    @Transactional(readOnly = true)
    public List<PersonAgreement> getPersonAgreements(UUID personUid,
                                                     OffsetDateTime validAt,
                                                     OffsetDateTime transactionAt) {
        assertPersonExists(personUid, validAt, transactionAt);
        return personAgreementMapper.toDtoList(
                personAgreementRepository.findByPersonAtPoint(personUid, validAt, transactionAt));
    }

    public PersonAgreement addPersonAgreement(UUID personUid, PersonAgreementRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        // getAgreementUid() returns UUID directly
        UUID agreementUid = request.getAgreementUid();
        OffsetDateTime vf = request.getValidFrom() != null ? request.getValidFrom() : now;
        OffsetDateTime vt = request.getValidTo() != null ? request.getValidTo() : BitemporalEntity.INFINITY;

        assertPersonExists(personUid, now, now);
        assertAgreementExists(agreementUid, now, now);

        personAgreementRepository.findLinkAtPoint(personUid, agreementUid, vf, now)
                .ifPresent(e -> {
                    throw new ConflictException(
                            "Person %s is already linked to agreement %s at %s"
                                    .formatted(personUid, agreementUid, vf));
                });

        PersonAgreementEntity link = new PersonAgreementEntity();
        link.setPersonUid(personUid);
        link.setAgreementUid(agreementUid);
        link.setValidFrom(vf);
        link.setValidTo(vt);
        link.setTransactionFrom(now);
        link.setTransactionTo(BitemporalEntity.INFINITY);

        PersonAgreementEntity saved = personAgreementRepository.save(link);
        log.info("Linked agreement {} → person {}", agreementUid, personUid);
        return personAgreementMapper.toDto(saved);
    }

    public void removePersonAgreement(UUID personUid, UUID agreementUid, OffsetDateTime validTo) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        PersonAgreementEntity link =
                personAgreementRepository.findLinkAtPoint(personUid, agreementUid, now, now)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No active link between person %s and agreement %s"
                                        .formatted(personUid, agreementUid)));

        personAgreementRepository.closeLinkTransaction(
                personUid, agreementUid, now, BitemporalEntity.INFINITY);

        PersonAgreementEntity terminated = new PersonAgreementEntity();
        terminated.setPersonUid(personUid);
        terminated.setAgreementUid(agreementUid);
        terminated.setValidFrom(link.getValidFrom());
        terminated.setValidTo(effectiveTo);
        terminated.setTransactionFrom(now);
        terminated.setTransactionTo(BitemporalEntity.INFINITY);

        personAgreementRepository.save(terminated);
        log.info("Removed agreement link {} → person {} validTo={}", agreementUid, personUid, effectiveTo);
    }

    // ==================================================================
    // Guards
    // ==================================================================

    private void assertPersonExists(UUID uid, OffsetDateTime validAt, OffsetDateTime transactionAt) {
        personRepository.findAtPoint(uid, validAt, transactionAt)
                .orElseThrow(() -> new ResourceNotFoundException("Person", uid));
    }

    private void assertAddressExists(UUID uid, OffsetDateTime validAt, OffsetDateTime transactionAt) {
        addressRepository.findAtPoint(uid, validAt, transactionAt)
                .orElseThrow(() -> new ResourceNotFoundException("Address", uid));
    }

    private void assertAgreementExists(UUID uid, OffsetDateTime validAt, OffsetDateTime transactionAt) {
        agreementRepository.findAtPoint(uid, validAt, transactionAt)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement", uid));
    }
}