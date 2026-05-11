package com.bsoft.timeseries.service;

import com.bsoft.timeseries.authentication.model.Role;
import com.bsoft.timeseries.authentication.model.RoleBody;
import com.bsoft.timeseries.entity.RolesEntity;
import com.bsoft.timeseries.exception.RoleExistsException;
import com.bsoft.timeseries.exception.RoleNotExistsException;
import com.bsoft.timeseries.mapper.RoleMapper;
import com.bsoft.timeseries.repository.RoleRepository;
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
public class RolesService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    public void deleteAll() {
        try {
            roleRepository.deleteAll();
        } catch (Exception e) {
            log.error("Deleting all roles failed: {}", e.toString());
        }
    }

    public boolean deleteRole(Long roleId) {
        boolean deleted = false;
        try {
            Optional<RolesEntity> optionalRole = roleRepository.findByRoleId(roleId);
            if (optionalRole.isEmpty()) {
                throw new RoleNotExistsException("Role with id " + roleId + " not found and not deleted");
            }
            roleRepository.deleteById(roleId);
            deleted = true;
        } catch (Exception e) {
            log.error("Delete role failed: {}", e.toString());
            throw e;
        }
        return deleted;
    }

    public Role getRole(Long roleId) {
        Optional<RolesEntity> optionalRoleDAO = roleRepository.findByRoleId(roleId);
        if (optionalRoleDAO.isEmpty()) {
            throw new RoleNotExistsException("Role with id " + roleId + " not found");
        }

        RolesEntity rolesEntity = optionalRoleDAO.get();

        return roleMapper.map(rolesEntity);
    }

    public List<Role> getRoles() {
        List<Role> roleList = new ArrayList<Role>();
        Iterable<RolesEntity> iterableRole = roleRepository.findAll();

        iterableRole.forEach(roleInstance -> {
            Role Role = roleMapper.map(roleInstance);
            roleList.add(Role);
        });

        return roleList;
    }

    public List<Role> getRoles(final PageRequest pageRequest) {
        List<Role> roleList = new ArrayList<Role>();
        Iterable<RolesEntity> iterableRole = roleRepository.findAllByPaged(pageRequest);

        iterableRole.forEach(roleInstance -> {
            Role Role = roleMapper.map(roleInstance);
            roleList.add(Role);
        });

        return roleList;
    }

    public Role postRole(Boolean override, final RoleBody roleBody) {
        RolesEntity rolesEntity = new RolesEntity(roleBody);

        try {
            if (!override) {
                Optional<RolesEntity> optionalRole = roleRepository.findByHash(rolesEntity.getHash());
                if (optionalRole.isPresent()) {
                    throw new RoleExistsException("Role " + rolesEntity + " already exists cannot insert again");
                }
            }

            roleRepository.save(rolesEntity);

            return roleMapper.map(rolesEntity); // Return 201 Created with the created entity
        } catch (Error e) {
            log.error("Error inserting adres: {}", e.toString());
            throw e;
        }
    }

    public Role patch(Long roleId, final RoleBody roleBody) {
        Role role = new Role();

        Optional<RolesEntity> optionalRole = roleRepository.findByRoleId(roleId);
        if (optionalRole.isEmpty()) {
            throw new RoleNotExistsException("Role with id " + roleId + " not found");
        }
        RolesEntity foundRole = optionalRole.get();
        if (roleBody.getRolename() != null) {
            foundRole.setRolename(roleBody.getRolename());
        }
        if (roleBody.getDescription() != null) {
            foundRole.setDescription(roleBody.getDescription());
        }
        foundRole.setHash(foundRole.genHash());

        roleRepository.save(foundRole);

        return roleMapper.map(foundRole);

    }

    public Page<Role> getRolesPage(PageRequest pageRequest) {
        Page<RolesEntity> foundRolesPage = roleRepository.findAllByPage(pageRequest);

        List<Role> rolesList = new ArrayList<>();
        rolesList = foundRolesPage.getContent().stream()
                .map(roleMapper::map) // Apply mapper to each AdresDAO
                .toList();

        return new PageImpl<>(rolesList, foundRolesPage.getPageable(), foundRolesPage.getTotalElements());
    }
}