package com.bsoft.timeseries.controller.timeseries;

import com.bsoft.timeseries.service.PersonRelationService;
import com.bsoft.timeseries.timeseries.api.PersonRelationsApi;
import com.bsoft.timeseries.timeseries.model.PersonAddress;
import com.bsoft.timeseries.timeseries.model.PersonAddressRequest;
import com.bsoft.timeseries.timeseries.model.PersonAgreement;
import com.bsoft.timeseries.timeseries.model.PersonAgreementRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.bsoft.timeseries.delegate.DelegateSupport.resolveTransactionAt;
import static com.bsoft.timeseries.delegate.DelegateSupport.resolveValidAt;

@RequestMapping("${openapi.timeseries.base-path}")
@RestController
@RequiredArgsConstructor
public class PersonRelationsController implements PersonRelationsApi {
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
