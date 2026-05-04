package com.bsoft.timeseries.delegate;

import com.bsoft.timeseries.api.PersonRelationsApiDelegate;
import com.bsoft.timeseries.model.*;
import com.bsoft.timeseries.service.PersonRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.bsoft.timeseries.delegate.DelegateSupport.*;

/**
 * Implements PersonRelationsApiDelegate — the person-address and
 * person-agreement relation endpoints.
 *
 * The OpenAPI generator placed these here (not in PersonsApiDelegate)
 * because those endpoints are tagged "person-relations" in the spec.
 */
@Component
@RequiredArgsConstructor
public class PersonRelationsApiDelegateImpl implements PersonRelationsApiDelegate {

    private final PersonRelationService personRelationService;

    // ------------------------------------------------------------------
    // Person ↔ Address
    // ------------------------------------------------------------------

    @Override
    public ResponseEntity<List<PersonAddress>> getPersonAddresses(UUID personId,
                                                                  OffsetDateTime validAt,
                                                                  OffsetDateTime transactionAt) {
        return ResponseEntity.ok(personRelationService.getPersonAddresses(
                personId, resolveValidAt(validAt), resolveTransactionAt(transactionAt)));
    }

    @Override
    public ResponseEntity<PersonAddress> addPersonAddress(UUID personId,
                                                          PersonAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(personRelationService.addPersonAddress(personId, request));
    }

    @Override
    public ResponseEntity<Void> removePersonAddress(UUID personId,
                                                    UUID addressId,
                                                    OffsetDateTime validTo) {
        personRelationService.removePersonAddress(personId, addressId, validTo);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------
    // Person ↔ Agreement
    // ------------------------------------------------------------------

    @Override
    public ResponseEntity<List<PersonAgreement>> getPersonAgreements(UUID personId,
                                                                     OffsetDateTime validAt,
                                                                     OffsetDateTime transactionAt) {
        return ResponseEntity.ok(personRelationService.getPersonAgreements(
                personId, resolveValidAt(validAt), resolveTransactionAt(transactionAt)));
    }

    @Override
    public ResponseEntity<PersonAgreement> addPersonAgreement(UUID personId,
                                                              PersonAgreementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(personRelationService.addPersonAgreement(personId, request));
    }

    @Override
    public ResponseEntity<Void> removePersonAgreement(UUID personId,
                                                      UUID agreementId,
                                                      OffsetDateTime validTo) {
        personRelationService.removePersonAgreement(personId, agreementId, validTo);
        return ResponseEntity.noContent().build();
    }
}