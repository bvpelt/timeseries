package com.bsoft.timeseries.service;

import com.bsoft.timeseries.entity.AgreementEntity;
import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.exception.ResourceNotFoundException;
import com.bsoft.timeseries.mapper.AgreementMapper;
import com.bsoft.timeseries.model.Agreement;
import com.bsoft.timeseries.model.AgreementPage;
import com.bsoft.timeseries.model.AgreementRequest;
import com.bsoft.timeseries.repository.AgreementRepository;
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
public class AgreementService {

    private final AgreementRepository agreementRepository;
    private final AgreementMapper     agreementMapper;

    public Agreement create(AgreementRequest request) {
        AgreementEntity entity = agreementMapper.toNewEntity(request);
        if (entity.getValidFrom() == null) entity.setValidFrom(OffsetDateTime.now());
        if (entity.getValidTo()   == null) entity.setValidTo(BitemporalEntity.INFINITY);
        AgreementEntity saved = agreementRepository.save(entity);
        log.info("Created agreement uid={}", saved.getUid());
        return agreementMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public Agreement findAtPoint(UUID uid, OffsetDateTime validAt, OffsetDateTime transactionAt) {
        return agreementRepository.findAtPoint(uid, validAt, transactionAt)
                .map(agreementMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement", uid));
    }

    @Transactional(readOnly = true)
    public AgreementPage listAtPoint(OffsetDateTime validAt, OffsetDateTime transactionAt,
                                     int page, int size) {
        Page<AgreementEntity> p = agreementRepository.findAllAtPoint(
                validAt, transactionAt, PageRequest.of(page, size));
        AgreementPage result = new AgreementPage();
        result.setContent(agreementMapper.toDtoList(p.getContent()));
        result.setTotalElements(p.getTotalElements());
        result.setTotalPages(p.getTotalPages());
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Agreement> history(UUID uid) {
        List<AgreementEntity> versions = agreementRepository.findAllVersions(uid);
        if (versions.isEmpty()) throw new ResourceNotFoundException("Agreement", uid);
        return agreementMapper.toDtoList(versions);
    }

    public Agreement update(UUID uid, AgreementRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        AgreementEntity current = agreementRepository.findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement", uid));

        agreementRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        AgreementEntity.State nextState = request.getState() != null
                ? AgreementEntity.State.valueOf(request.getState().getValue())
                : current.getState();

        AgreementEntity next = new AgreementEntity();
        next.setUid(uid);
        next.setTitle(request.getTitle() != null ? request.getTitle() : current.getTitle());
        next.setDescription(request.getDescription() != null
                ? request.getDescription() : current.getDescription());
        next.setState(nextState);
        next.setValidFrom(request.getValidFrom() != null
                ? request.getValidFrom() : current.getValidFrom());
        next.setValidTo(request.getValidTo() != null
                ? request.getValidTo() : current.getValidTo());
        next.setTransactionFrom(now);
        next.setTransactionTo(BitemporalEntity.INFINITY);

        AgreementEntity saved = agreementRepository.save(next);
        log.info("Updated agreement uid={} state={}", uid, nextState);
        return agreementMapper.toDto(saved);
    }

    public void terminate(UUID uid, OffsetDateTime validTo) {
        OffsetDateTime now         = OffsetDateTime.now();
        OffsetDateTime effectiveTo = validTo != null ? validTo : now;

        AgreementEntity current = agreementRepository.findAtPoint(uid, now, now)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement", uid));

        agreementRepository.closeCurrentTransactionVersion(uid, now, BitemporalEntity.INFINITY);

        AgreementEntity terminated = new AgreementEntity();
        terminated.setUid(uid);
        terminated.setTitle(current.getTitle());
        terminated.setDescription(current.getDescription());
        terminated.setState(current.getState());
        terminated.setValidFrom(current.getValidFrom());
        terminated.setValidTo(effectiveTo);
        terminated.setTransactionFrom(now);
        terminated.setTransactionTo(BitemporalEntity.INFINITY);

        agreementRepository.save(terminated);
        log.info("Terminated agreement uid={} validTo={}", uid, effectiveTo);
    }
}