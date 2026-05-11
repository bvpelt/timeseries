package com.bsoft.timeseries.controller;

import com.bsoft.timeseries.service.AgreementService;
import com.bsoft.timeseries.timeseries.api.AgreementsApi;
import com.bsoft.timeseries.timeseries.model.Agreement;
import com.bsoft.timeseries.timeseries.model.AgreementPage;
import com.bsoft.timeseries.timeseries.model.AgreementRequest;
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
public class AgreementsController implements AgreementsApi {
    private final AgreementService agreementService;

    @Override
    public ResponseEntity<AgreementPage> listAgreements(OffsetDateTime validAt,
                                                        OffsetDateTime transactionAt,
                                                        Integer page,
                                                        Integer size) {
        return ResponseEntity.ok(agreementService.listAtPoint(
                resolveValidAt(validAt),
                resolveTransactionAt(transactionAt),
                page != null ? page : 0,
                size != null ? size : 20));
    }

    @Override
    public ResponseEntity<Agreement> createAgreement(AgreementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(agreementService.create(request));
    }

    @Override
    public ResponseEntity<Agreement> getAgreement(UUID agreementId,
                                                  OffsetDateTime validAt,
                                                  OffsetDateTime transactionAt) {
        return ResponseEntity.ok(agreementService.findAtPoint(
                agreementId,
                resolveValidAt(validAt),
                resolveTransactionAt(transactionAt)));
    }

    @Override
    public ResponseEntity<Agreement> updateAgreement(UUID agreementId,
                                                     AgreementRequest request) {
        return ResponseEntity.ok(agreementService.update(agreementId, request));
    }

    @Override
    public ResponseEntity<Void> terminateAgreement(UUID agreementId,
                                                   OffsetDateTime validTo) {
        agreementService.terminate(agreementId, validTo);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<Agreement>> getAgreementHistory(UUID agreementId) {
        return ResponseEntity.ok(agreementService.history(agreementId));
    }
}

