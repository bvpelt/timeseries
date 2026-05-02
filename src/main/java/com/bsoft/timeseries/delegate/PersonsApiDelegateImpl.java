package com.bsoft.timeseries.delegate;

import com.bsoft.timeseries.api.PersonsApiDelegate;
import com.bsoft.timeseries.model.*;
import com.bsoft.timeseries.service.PersonRelationService;
import com.bsoft.timeseries.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.bsoft.timeseries.delegate.DelegateSupport.*;

/**
 * Implements the OpenAPI-generated {@link PersonsApiDelegate}.
 *
 * <p>Each method delegates to the appropriate service, handles parameter
 * defaulting (bitemporal point defaults to "now"), and wraps results in
 * the correct HTTP status code.</p>
 */
@Component
@RequiredArgsConstructor
public class PersonsApiDelegateImpl implements PersonsApiDelegate {

    private final PersonService        personService;
    private final PersonRelationService personRelationService;

    // ------------------------------------------------------------------
    // Person CRUD
    // ------------------------------------------------------------------

    @Override
    public ResponseEntity<PersonPage> listPersons(OffsetDateTime validAt,
                                                  OffsetDateTime transactionAt,
                                                  Integer page,
                                                  Integer size) {
        PersonPage result = personService.listAtPoint(
                resolveValidAt(validAt),
                resolveTransactionAt(transactionAt),
                page  != null ? page  : 0,
                size  != null ? size  : 20);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<Person> createPerson(PersonRequest request) {
        Person created = personService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    public ResponseEntity<Person> getPerson(UUID personId,
                                            OffsetDateTime validAt,
                                            OffsetDateTime transactionAt) {
        Person person = personService.findAtPoint(personId,
                resolveValidAt(validAt),
                resolveTransactionAt(transactionAt));
        return ResponseEntity.ok(person);
    }

    @Override
    public ResponseEntity<Person> updatePerson(UUID personId,
                                               PersonRequest request) {
        Person updated = personService.update(personId, request);
        return ResponseEntity.ok(updated);
    }

    @Override
    public ResponseEntity<Void> terminatePerson(UUID personId,
                                                OffsetDateTime validTo) {
        personService.terminate(personId, validTo);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<Person>> getPersonHistory(UUID personId) {
        return ResponseEntity.ok(personService.history(personId));
    }

    // ------------------------------------------------------------------
    // Person ↔ Address relations
    // ------------------------------------------------------------------

    @Override
    public ResponseEntity<List<PersonAddress>> getPersonAddresses(UUID personId,
                                                                  OffsetDateTime validAt,
                                                                  OffsetDateTime transactionAt) {
        List<PersonAddress> links = personRelationService.getPersonAddresses(
                personId,
                resolveValidAt(validAt),
                resolveTransactionAt(transactionAt));
        return ResponseEntity.ok(links);
    }

    @Override
    public ResponseEntity<PersonAddress> addPersonAddress(UUID personId,
                                                          PersonAddressRequest request) {
        PersonAddress link = personRelationService.addPersonAddress(personId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(link);
    }

    @Override
    public ResponseEntity<Void> removePersonAddress(UUID personId,
                                                    UUID addressId,
                                                    OffsetDateTime validTo) {
        personRelationService.removePersonAddress(personId, addressId, validTo);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Person ↔ Agreement relations
    // ------------------------------------------------------------------

    @Override
    public ResponseEntity<List<PersonAgreement>> getPersonAgreements(UUID personId,
                                                                     OffsetDateTime validAt,
                                                                     OffsetDateTime transactionAt) {
        List<PersonAgreement> links = personRelationService.getPersonAgreements(
                personId,
                resolveValidAt(validAt),
                resolveTransactionAt(transactionAt));
        return ResponseEntity.ok(links);
    }

    @Override
    public ResponseEntity<PersonAgreement> addPersonAgreement(UUID personId,
                                                              PersonAgreementRequest request) {
        PersonAgreement link = personRelationService.addPersonAgreement(personId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(link);
    }

    @Override
    public ResponseEntity<Void> removePersonAgreement(UUID personId,
                                                      UUID agreementId,
                                                      OffsetDateTime validTo) {
        personRelationService.removePersonAgreement(personId, agreementId, validTo);
        return ResponseEntity.noContent().build();
    }
}