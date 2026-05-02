package com.bsoft.timeseries.service;

import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.entity.PersonAddressEntity;
import com.bsoft.timeseries.entity.PersonAgreementEntity;
import com.bsoft.timeseries.exception.ConflictException;
import com.bsoft.timeseries.exception.ResourceNotFoundException;
import com.bsoft.timeseries.mapper.PersonAddressMapper;
import com.bsoft.timeseries.mapper.PersonAgreementMapper;
import com.bsoft.timeseries.model.*;
import com.bsoft.timeseries.repository.AddressRepository;
import com.bsoft.timeseries.repository.AgreementRepository;
import com.bsoft.timeseries.repository.PersonAddressRepository;
import com.bsoft.timeseries.repository.PersonAgreementRepository;
import com.bsoft.timeseries.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Manages the bitemporal N:M relations:
 * <ul>
 *   <li>Person ↔ Address (HOME / WORK)</li>
 *   <li>Person ↔ Agreement</li>
 * </ul>
 *
 * <h2>Business rules enforced here</h2>
 * <ul>
 *   <li>A person may have at most <b>one</b> current HOME address at any
 *       point in valid time.</li>
 *   <li>A link that already exists (same person + address + overlapping
 *       valid period) cannot be created again — returns {@code 409 Conflict}.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PersonRelationService {

    private final PersonRepository         personRepository;
    private final AddressRepository        addressRepository;
    private final AgreementRepository      agreementRepository;
    private final PersonAddressRepository  personAddressRepository;
    private final PersonAgreementRepository personAgreementRepository;
    private final PersonAddressMapper      personAddressMapper;
    private final PersonAgreementMapper    personAgreementMapper;

    // ==================================================================
    // PERSON ↔ ADDRESS
    // ==================================================================

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PersonAddress> getPersonAddresses(UUID personUid,
                                                  OffsetDateTime validAt,
                                                  OffsetDateTime transactionAt) {
        assertPersonExists(personUid, validAt, transactionAt);
        List<PersonAddressEntity> links =
                personAddressRepository.findByPersonAtPoint(personUid, validAt, transactionAt);
        return personAddressMapper.toDtoList(links);
    }

    // ----------------------------------------------------------------
    // LINK  (create relation)
    // ----------------------------------------------------------------

    public PersonAddress addPersonAddress(UUID personUid,
                                          PersonAddressRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        UUID addressUid   = UUID.fromString(request.getAddressUid());
        OffsetDateTime vf = request.getValidFrom()  != null ? request.getValidFrom() : now;
        OffsetDateTime vt = request.getValidTo()    != null ? request.getValidTo()   : BitemporalEntity.INFINITY;

        // Guard: person must exist in current time
        assertPersonExists(personUid, now, now);

        // Guard: address must exist in current time
        assertAddressExists(addressUid, now, now);

        // Guard: no duplicate link at the requested valid-from point
        personAddressRepository.findLinkAtPoint(personUid, addressUid, vf, now)
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Person %s already has address %s linked at %s"
                                    .formatted(personUid, addressUid, vf));
                });

        // Guard: at most one HOME address per person in current transaction time
        if (request.getAddressType() == AddressType.HOME) {
            long currentHomes = personAddressRepository
                    .countCurrentHomeAddresses(personUid, vf, BitemporalEntity.INFINITY);
            if (currentHomes > 0) {
                throw new ConflictException(
                        "Person %s already has a HOME address valid at %s. "
                                + "Terminate the existing HOME link before adding a new one."
                                .formatted(personUid, vf));
            }
        }

        PersonAddressEntity link = PersonAddressEntity.builder()
                .personUid(personUid)
                .addressUid(addressUid)
                .addressType(PersonAddressEntity.AddressType.valueOf(
                        request.getAddressType().getValue()))
                .validFrom(vf)
                .validTo(vt)
                .transactionFrom(now)
                .transactionTo(BitemporalEntity.INFINITY)
                .build();

        PersonAddressEntity saved = personAddressRepository.save(link);
        log.info("Linked address {} ({}) → person {} validFrom={}",
                addressUid, request.getAddressType(), personUid, vf);
        return personAddressMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // UNLINK  (terminate relation)
    // ----------------------------------------------------------------

    public void removePersonAddress(UUID personUid,
                                    UUID addressUid,
                                    OffsetDateTime validTo) {
        OffsetDateTime now         = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        PersonAddressEntity link =
                personAddressRepository.findLinkAtPoint(personUid, addressUid, now, now)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No active link between person %s and address %s"
                                        .formatted(personUid, addressUid)));

        // Close current transaction version
        personAddressRepository.closeLinkTransaction(
                personUid, addressUid, now, BitemporalEntity.INFINITY);

        // Re-insert with restricted validTo
        PersonAddressEntity terminated = PersonAddressEntity.builder()
                .personUid(personUid)
                .addressUid(addressUid)
                .addressType(link.getAddressType())
                .validFrom(link.getValidFrom())
                .validTo(effectiveTo)
                .transactionFrom(now)
                .transactionTo(BitemporalEntity.INFINITY)
                .build();

        personAddressRepository.save(terminated);
        log.info("Removed address link {} → person {} validTo={}",
                addressUid, personUid, effectiveTo);
    }

    // ==================================================================
    // PERSON ↔ AGREEMENT
    // ==================================================================

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PersonAgreement> getPersonAgreements(UUID personUid,
                                                     OffsetDateTime validAt,
                                                     OffsetDateTime transactionAt) {
        assertPersonExists(personUid, validAt, transactionAt);
        List<PersonAgreementEntity> links =
                personAgreementRepository.findByPersonAtPoint(personUid, validAt, transactionAt);
        return personAgreementMapper.toDtoList(links);
    }

    // ----------------------------------------------------------------
    // LINK
    // ----------------------------------------------------------------

    public PersonAgreement addPersonAgreement(UUID personUid,
                                              PersonAgreementRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        UUID agreementUid = UUID.fromString(request.getAgreementUid());
        OffsetDateTime vf = request.getValidFrom() != null ? request.getValidFrom() : now;
        OffsetDateTime vt = request.getValidTo()   != null ? request.getValidTo()   : BitemporalEntity.INFINITY;

        assertPersonExists(personUid, now, now);
        assertAgreementExists(agreementUid, now, now);

        // Guard: no duplicate
        personAgreementRepository.findLinkAtPoint(personUid, agreementUid, vf, now)
                .ifPresent(e -> {
                    throw new ConflictException(
                            "Person %s is already linked to agreement %s at %s"
                                    .formatted(personUid, agreementUid, vf));
                });

        PersonAgreementEntity link = PersonAgreementEntity.builder()
                .personUid(personUid)
                .agreementUid(agreementUid)
                .validFrom(vf)
                .validTo(vt)
                .transactionFrom(now)
                .transactionTo(BitemporalEntity.INFINITY)
                .build();

        PersonAgreementEntity saved = personAgreementRepository.save(link);
        log.info("Linked agreement {} → person {} validFrom={}", agreementUid, personUid, vf);
        return personAgreementMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // UNLINK
    // ----------------------------------------------------------------

    public void removePersonAgreement(UUID personUid,
                                      UUID agreementUid,
                                      OffsetDateTime validTo) {
        OffsetDateTime now         = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        PersonAgreementEntity link =
                personAgreementRepository.findLinkAtPoint(personUid, agreementUid, now, now)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "No active link between person %s and agreement %s"
                                        .formatted(personUid, agreementUid)));

        personAgreementRepository.closeLinkTransaction(
                personUid, agreementUid, now, BitemporalEntity.INFINITY);

        PersonAgreementEntity terminated = PersonAgreementEntity.builder()
                .personUid(personUid)
                .agreementUid(agreementUid)
                .validFrom(link.getValidFrom())
                .validTo(effectiveTo)
                .transactionFrom(now)
                .transactionTo(BitemporalEntity.INFINITY)
                .build();

        personAgreementRepository.save(terminated);
        log.info("Removed agreement link {} → person {} validTo={}",
                agreementUid, personUid, effectiveTo);
    }

    // ==================================================================
    // Private guards
    // ==================================================================

    private void assertPersonExists(UUID uid,
                                    OffsetDateTime validAt,
                                    OffsetDateTime transactionAt) {
        personRepository.findAtPoint(uid, validAt, transactionAt)
                .orElseThrow(() -> new ResourceNotFoundException("Person", uid));
    }

    private void assertAddressExists(UUID uid,
                                     OffsetDateTime validAt,
                                     OffsetDateTime transactionAt) {
        addressRepository.findAtPoint(uid, validAt, transactionAt)
                .orElseThrow(() -> new ResourceNotFoundException("Address", uid));
    }

    private void assertAgreementExists(UUID uid,
                                       OffsetDateTime validAt,
                                       OffsetDateTime transactionAt) {
        agreementRepository.findAtPoint(uid, validAt, transactionAt)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement", uid));
    }
}