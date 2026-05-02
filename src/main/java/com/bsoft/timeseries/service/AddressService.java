package com.bsoft.timeseries.service;

import com.bsoft.timeseries.entity.AddressEntity;
import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.exception.ResourceNotFoundException;
import com.bsoft.timeseries.mapper.AddressMapper;
import com.bsoft.timeseries.model.Address;
import com.bsoft.timeseries.model.AddressPage;
import com.bsoft.timeseries.model.AddressRequest;
import com.bsoft.timeseries.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper     addressMapper;

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    public Address create(AddressRequest request) {
        AddressEntity entity = addressMapper.toNewEntity(request);
        if (entity.getValidFrom() == null) entity.setValidFrom(OffsetDateTime.now());
        if (entity.getValidTo()   == null) entity.setValidTo(BitemporalEntity.INFINITY);

        AddressEntity saved = addressRepository.save(entity);
        log.info("Created address uid={}", saved.getUid());
        return addressMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public Address findAtPoint(UUID uid,
                               OffsetDateTime validAt,
                               OffsetDateTime transactionAt) {
        return addressRepository.findAtPoint(uid, validAt, transactionAt)
                .map(addressMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Address", uid));
    }

    @Transactional(readOnly = true)
    public AddressPage listAtPoint(OffsetDateTime validAt,
                                   OffsetDateTime transactionAt,
                                   int page, int size) {
        Page<AddressEntity> entityPage = addressRepository.findAllAtPoint(
                validAt, transactionAt, PageRequest.of(page, size));

        AddressPage result = new AddressPage();
        result.setContent(addressMapper.toDtoList(entityPage.getContent()));
        result.setTotalElements(entityPage.getTotalElements());
        result.setTotalPages(entityPage.getTotalPages());
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Address> history(UUID uid) {
        List<AddressEntity> versions = addressRepository.findAllVersions(uid);
        if (versions.isEmpty()) throw new ResourceNotFoundException("Address", uid);
        return addressMapper.toDtoList(versions);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    public Address update(UUID uid, AddressRequest request) {
        OffsetDateTime now = OffsetDateTime.now();

        AddressEntity current = addressRepository.findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Address", uid));

        addressRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        AddressEntity next = AddressEntity.builder()
                .uid(uid)
                .street(request.getStreet() != null
                        ? request.getStreet() : current.getStreet())
                .houseNumber(request.getHouseNumber() != null
                        ? request.getHouseNumber() : current.getHouseNumber())
                .postalCode(request.getPostalCode() != null
                        ? request.getPostalCode() : current.getPostalCode())
                .city(request.getCity() != null
                        ? request.getCity() : current.getCity())
                .validFrom(request.getValidFrom() != null
                        ? request.getValidFrom() : current.getValidFrom())
                .validTo(request.getValidTo() != null
                        ? request.getValidTo() : current.getValidTo())
                .transactionFrom(now)
                .transactionTo(BitemporalEntity.INFINITY)
                .build();

        AddressEntity saved = addressRepository.save(next);
        log.info("Updated address uid={}", uid);
        return addressMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // TERMINATE
    // ----------------------------------------------------------------

    public void terminate(UUID uid, OffsetDateTime validTo) {
        OffsetDateTime now         = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        AddressEntity current = addressRepository.findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Address", uid));

        addressRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        AddressEntity terminated = AddressEntity.builder()
                .uid(uid)
                .street(current.getStreet())
                .houseNumber(current.getHouseNumber())
                .postalCode(current.getPostalCode())
                .city(current.getCity())
                .validFrom(current.getValidFrom())
                .validTo(effectiveTo)
                .transactionFrom(now)
                .transactionTo(BitemporalEntity.INFINITY)
                .build();

        addressRepository.save(terminated);
        log.info("Terminated address uid={} validTo={}", uid, effectiveTo);
    }
}