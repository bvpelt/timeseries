package com.bsoft.timeseries.service;

import com.bsoft.timeseries.authentication.model.Privilege;
import com.bsoft.timeseries.authentication.model.PrivilegeBody;
import com.bsoft.timeseries.entity.PrivilegeEntity;
import com.bsoft.timeseries.exception.PrivilegeExistsException;
import com.bsoft.timeseries.exception.PrivilegeNotExistsException;
import com.bsoft.timeseries.mapper.PrivilegeMapper;
import com.bsoft.timeseries.repository.PrivilegeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PrivilegesService {

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PrivilegeMapper privilegeMapper;

    public void deleteAll() {
        try {
            privilegeRepository.deleteAll();
        } catch (Exception e) {
            log.error("Deleting all Privileges failed: {}", e.toString());
        }
    }

    public boolean deletePrivilege(Long privilegeId) {
        boolean deleted = false;
        try {
            Optional<PrivilegeEntity> optionalPrivilegeDAO = privilegeRepository.findByPrivilegeId(privilegeId);
            if (optionalPrivilegeDAO.isEmpty()) {
                throw new PrivilegeNotExistsException("Privilege with id " + privilegeId + " not found and not deleted");
            }
            privilegeRepository.deleteById(privilegeId);
            deleted = true;
        } catch (Exception e) {
            log.error("Delete Privilege failed: {}", e.toString());
            throw e;
        }
        return deleted;
    }

    public Privilege getPrivilege(Long privilegeId) {
        Optional<PrivilegeEntity> optionalPrivilegeDAO = privilegeRepository.findByPrivilegeId(privilegeId);
        if (optionalPrivilegeDAO.isEmpty()) {
            throw new PrivilegeNotExistsException("Privilege with id " + privilegeId + " not found");
        }

        return privilegeMapper.map(optionalPrivilegeDAO.get());
    }

    public List<Privilege> getPrivileges() {
        List<Privilege> PrivilegeList = new ArrayList<Privilege>();
        Iterable<PrivilegeEntity> privilegeDAOIterable = privilegeRepository.findAll();

        privilegeDAOIterable.forEach(privilegeDAO -> {
            PrivilegeList.add(privilegeMapper.map(privilegeDAO));
        });

        return PrivilegeList;
    }

    public List<Privilege> getPrivileges(final PageRequest pageRequest) {
        List<Privilege> privilegeList = new ArrayList<Privilege>();
        Iterable<PrivilegeEntity> privilegeDAOIterable = privilegeRepository.findAllByPaged(pageRequest);

        privilegeDAOIterable.forEach(privilegeDAO -> {
            privilegeList.add(privilegeMapper.map(privilegeDAO));
        });

        return privilegeList;
    }

    public Privilege postPrivilege(Boolean override, final PrivilegeBody privilegeBody) {
        PrivilegeEntity privilegeDTO = new PrivilegeEntity(privilegeBody);

        try {
            if (!override) {
                Optional<PrivilegeEntity> optionalPrivilegeDAO = privilegeRepository.findByHash(privilegeDTO.getHash());
                if (optionalPrivilegeDAO.isPresent()) {
                    throw new PrivilegeExistsException("Privilege " + privilegeDTO + " already exists cannot insert again");
                }
            }

            privilegeRepository.save(privilegeDTO);

            return privilegeMapper.map(privilegeDTO); // Return 201 Created with the created entity
        } catch (Error e) {
            log.error("Error inserting adres: {}", e.toString());
            throw e;
        }
    }

    public Privilege patch(Long privilegeId, final PrivilegeBody privilegeBody) {
        Privilege Privilege = new Privilege();

        Optional<PrivilegeEntity> optionalPrivilegeDAO = privilegeRepository.findByPrivilegeId(privilegeId);
        if (optionalPrivilegeDAO.isEmpty()) {
            throw new PrivilegeNotExistsException("Privilege with id " + privilegeId + " not found");
        }
        PrivilegeEntity foundPrivilege = optionalPrivilegeDAO.get();
        if (privilegeBody.getName() != null) {
            foundPrivilege.setName(privilegeBody.getName());
        }
        foundPrivilege.setHash(foundPrivilege.genHash());

        privilegeRepository.save(foundPrivilege);

        return privilegeMapper.map(foundPrivilege);

    }


    public Page<Privilege> getPrivilegesPage(PageRequest pageRequest) {
        Page<PrivilegeEntity> foundPrivilegesPage = privilegeRepository.findAllByPage(pageRequest);

        List<Privilege> privilegesList = new ArrayList<>();
        privilegesList = foundPrivilegesPage.getContent().stream()
                .map(privilegeMapper::map) // Apply mapper to each AdresDAO
                .toList();

        return new PageImpl<>(privilegesList, foundPrivilegesPage.getPageable(), foundPrivilegesPage.getTotalElements());
    }
}