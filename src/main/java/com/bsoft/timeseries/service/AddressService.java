package com.bsoft.timeseries.service;

import com.bsoft.timeseries.entity.AddressEntity;
import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.exception.ResourceNotFoundException;
import com.bsoft.timeseries.mapper.AddressMapper;

import com.bsoft.timeseries.repository.AddressRepository;

import com.bsoft.timeseries.timeseries.model.Address;
import com.bsoft.timeseries.timeseries.model.AddressPage;
import com.bsoft.timeseries.timeseries.model.AddressRequest;
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
 * Uses new + setters for all entity construction — see PersonService for
 * the explanation of why the Lombok builder is avoided here.
 */
@Service
@Slf4j
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    // Handmatige constructor ipv @RequiredArgsConstructor om @Qualifier toe te voegen
    public AddressService(AddressRepository addressRepository,
                          @Qualifier("addressMapperImpl") AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
    }

    public Address create(AddressRequest request) {
        AddressEntity entity = addressMapper.toNewEntity(request);
        if (entity.getValidFrom() == null) entity.setValidFrom(OffsetDateTime.now());
        if (entity.getValidTo() == null) entity.setValidTo(BitemporalEntity.INFINITY);
        AddressEntity saved = addressRepository.save(entity);
        log.info("Created address uid={}", saved.getUid());
        return addressMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Address findAtPoint(UUID uid, OffsetDateTime validAt, OffsetDateTime transactionAt) {
        return addressRepository.findAtPoint(uid, validAt, transactionAt)
                .map(addressMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Address", uid));
    }

    @Transactional(readOnly = true)
    public AddressPage listAtPoint(OffsetDateTime validAt, OffsetDateTime transactionAt,
                                   int page, int size) {
        Page<AddressEntity> p = addressRepository.findAllAtPoint(
                validAt, transactionAt, PageRequest.of(page, size));
        AddressPage result = new AddressPage();
        result.setContent(addressMapper.toDtoList(p.getContent()));
        result.setTotalElements(p.getTotalElements());
        result.setTotalPages(p.getTotalPages());
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

    public Address update(UUID uid, AddressRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        AddressEntity current = addressRepository.findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Address", uid));

        addressRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        AddressEntity next = new AddressEntity();
        next.setUid(uid);
        next.setStreet(request.getStreet() != null
                ? request.getStreet() : current.getStreet());
        next.setHouseNumber(request.getHouseNumber() != null
                ? request.getHouseNumber() : current.getHouseNumber());
        next.setPostalCode(request.getPostalCode() != null
                ? request.getPostalCode() : current.getPostalCode());
        next.setCity(request.getCity() != null
                ? request.getCity() : current.getCity());
        next.setValidFrom(request.getValidFrom() != null
                ? request.getValidFrom() : current.getValidFrom());
        next.setValidTo(request.getValidTo() != null
                ? request.getValidTo() : current.getValidTo());
        next.setTransactionFrom(now);
        next.setTransactionTo(BitemporalEntity.INFINITY);

        AddressEntity saved = addressRepository.save(next);
        log.info("Updated address uid={}", uid);
        return addressMapper.toDto(saved);
    }

    public void terminate(UUID uid, OffsetDateTime validTo) {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        AddressEntity current = addressRepository.findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Address", uid));

        addressRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        AddressEntity terminated = new AddressEntity();
        terminated.setUid(uid);
        terminated.setStreet(current.getStreet());
        terminated.setHouseNumber(current.getHouseNumber());
        terminated.setPostalCode(current.getPostalCode());
        terminated.setCity(current.getCity());
        terminated.setValidFrom(current.getValidFrom());
        terminated.setValidTo(effectiveTo);
        terminated.setTransactionFrom(now);
        terminated.setTransactionTo(BitemporalEntity.INFINITY);

        addressRepository.save(terminated);
        log.info("Terminated address uid={} validTo={}", uid, effectiveTo);
    }
}