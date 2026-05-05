package com.bsoft.timeseries.controller;

import com.bsoft.timeseries.api.PersonsApi;
import com.bsoft.timeseries.model.Person;
import com.bsoft.timeseries.model.PersonPage;
import com.bsoft.timeseries.model.PersonRequest;
import com.bsoft.timeseries.service.PersonService;
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

@RequestMapping("${openapi.bitemporalPersonAddressAgreementService.base-path}")
@RestController
@RequiredArgsConstructor
public class PersonsController implements PersonsApi {

    private final PersonService personService;

    @Override
    public ResponseEntity<PersonPage> listPersons(OffsetDateTime validAt,
                                                  OffsetDateTime transactionAt,
                                                  Integer page, Integer size) {
        return ResponseEntity.ok(personService.listAtPoint(
                resolveValidAt(validAt), resolveTransactionAt(transactionAt),
                page != null ? page : 0,
                size != null ? size : 20));
    }

    @Override
    public ResponseEntity<Person> createPerson(PersonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(personService.create(request));
    }

    @Override
    public ResponseEntity<Person> getPerson(UUID personId,
                                            OffsetDateTime validAt,
                                            OffsetDateTime transactionAt) {
        return ResponseEntity.ok(personService.findAtPoint(
                personId, resolveValidAt(validAt), resolveTransactionAt(transactionAt)));
    }

    @Override
    public ResponseEntity<Person> updatePerson(UUID personId, PersonRequest request) {
        return ResponseEntity.ok(personService.update(personId, request));
    }

    @Override
    public ResponseEntity<Void> terminatePerson(UUID personId, OffsetDateTime validTo) {
        personService.terminate(personId, validTo);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<Person>> getPersonHistory(UUID personId) {
        return ResponseEntity.ok(personService.history(personId));
    }
}
