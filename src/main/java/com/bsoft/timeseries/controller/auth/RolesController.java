package com.bsoft.timeseries.controller.auth;

import com.bsoft.timeseries.authentication.api.RolesApi;
import com.bsoft.timeseries.authentication.model.PagedRoles;
import com.bsoft.timeseries.authentication.model.Role;
import com.bsoft.timeseries.authentication.model.RoleBody;
import com.bsoft.timeseries.controller.ControllerSortUtil;
import com.bsoft.timeseries.exception.InvalidParameterException;
import com.bsoft.timeseries.exception.RoleNotDeletedException;
import com.bsoft.timeseries.service.RolesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("${openapi.auth.base-path}")
@Controller
public class RolesController implements RolesApi {

    private final RolesService rolesService;

    @Value("${info.project.version}")
    private String version;

    private HttpHeaders getVersionHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Version", version);
        return headers;
    }

    @Override
    public ResponseEntity<Void> _deleteRole(Long id) {
        log.debug("_deleteRole id: {}", id);
        boolean deleted = rolesService.deleteRole(id);
        if (!deleted) {
            throw new RoleNotDeletedException("Role not deleted");
        }

        HttpHeaders headers = getVersionHeaders();

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> _deleteAllRoles() {
        log.debug("_deleteAllRoles");
        rolesService.deleteAll();

        HttpHeaders headers = getVersionHeaders();

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Role> _getRole(Long roleId) {
        log.debug("_getRole id: {}", roleId);
        Role Role = rolesService.getRole(roleId);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(Role); // Return 201 Created with the created entity
    }

    @Override
    public ResponseEntity<PagedRoles> _getRoles(Integer page, Integer size, List<String> sort) {

        log.debug("_getRoles");
        List<Sort.Order> sortParameter;
        PageRequest pageRequest;
        log.trace("Get adresses for pagenumber: {} pagesize: {}, sort: {}", page, size, sort);
        // Validate input parameters
        if (page == null) {
            page = 1;
        }
        if (page < 1) {
            throw new InvalidParameterException("Page number must be greater than 0");
        }
        if (size == null) { // set to default
            size = 25;
        }
        if (size < 1) {
            throw new InvalidParameterException("Page size must be greater than 0");
        }
        //
        if (sort != null && !sort.isEmpty()) {
            List<Sort.Order> orders = ControllerSortUtil.getSortOrder(sort);
            pageRequest = PageRequest.of(page - 1, size, Sort.by(orders));
        } else {
            pageRequest = PageRequest.of(page - 1, size);
        }

        PagedRoles pageResponse = new PagedRoles();
        Page<Role> rolePage = rolesService.getRolesPage(pageRequest);
        pageResponse.setContent(rolePage.getContent());
        pageResponse.setPageNumber(rolePage.getNumber() + 1);
        pageResponse.setPageSize(rolePage.getSize());
        pageResponse.setTotalElements(BigDecimal.valueOf(rolePage.getTotalElements()));
        pageResponse.setTotalPages(rolePage.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(pageResponse);
    }

    @Override
    public ResponseEntity<Role> _patchRole(Long id, RoleBody roleBody) {
        log.debug("_patchRole");
        Role Role = rolesService.patch(id, roleBody);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(Role); // Return 201 Created with the created entity
    }

    @Override
    public ResponseEntity<Role> _postRole(RoleBody roleBody) {
        log.debug("_postRole");

        Role Role = rolesService.postRole(false, roleBody); // Call the service method

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(getVersionHeaders())
                .body(Role); // Return 201 Created with the created entity
    }

}