package com.bsoft.timeseries.delegate;

import com.bsoft.timeseries.api.PersonsApiDelegate;
import com.bsoft.timeseries.model.*;
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
 * Implements PersonsApiDelegate — person CRUD + history only.
 *
 * The person-relations endpoints (addresses, agreements on a person) are
 * tagged "person-relations" in the OpenAPI spec, so the generator placed
 * them in PersonRelationsApiDelegate. They are implemented in
 * PersonRelationsApiDelegateImpl.
 */
@Component
@RequiredArgsConstructor
public class PersonsApiDelegateImpl implements PersonsApiDelegate {

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