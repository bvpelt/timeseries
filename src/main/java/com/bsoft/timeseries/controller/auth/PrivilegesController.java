package com.bsoft.timeseries.controller.auth;

import com.bsoft.timeseries.authentication.api.PrivilegesApi;
import com.bsoft.timeseries.authentication.model.PagedPrivileges;
import com.bsoft.timeseries.authentication.model.Privilege;
import com.bsoft.timeseries.authentication.model.PrivilegeBody;
import com.bsoft.timeseries.controller.ControllerSortUtil;
import com.bsoft.timeseries.entity.PrivilegeNotDeletedException;
import com.bsoft.timeseries.exception.InvalidParameterException;
import com.bsoft.timeseries.service.PrivilegesService;
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
public class PrivilegesController implements PrivilegesApi {

    private final PrivilegesService privilegesService;

    @Value("${info.project.version}")
    private String version;

    private HttpHeaders getVersionHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Version", version);
        return headers;
    }

    @Override
    public ResponseEntity<Void> _deletePrivilege(Long id) {
        log.debug("_deletePrivilege id: {}", id);
        boolean deleted = privilegesService.deletePrivilege(id);
        if (!deleted) {
            throw new PrivilegeNotDeletedException("Privilege not deleted");
        }

        HttpHeaders headers = getVersionHeaders();

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> _deleteAllPrivileges() {
        log.debug("_deleteAllPrivileges");
        privilegesService.deleteAll();

        HttpHeaders headers = getVersionHeaders();

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Privilege> _getPrivilege(Long privilegeId) {
        log.debug("_getPrivilege id: {}", privilegeId);
        Privilege Privilege = privilegesService.getPrivilege(privilegeId);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(Privilege); // Return 201 Created with the created entity
    }

    @Override
    public ResponseEntity<PagedPrivileges> _getPrivileges(Integer page, Integer size, List<String> sort) {

        log.debug("_getPrivileges page {}, size: {}, sort: {}", page, size, sort.toString());
        List<Sort.Order> sortParameter;
        PageRequest pageRequest;
        log.trace("Get privileges for pagenumber: {} pagesize: {}, sort: {}", page, size, sort);
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

        PagedPrivileges pageResponse = new PagedPrivileges();
        Page<Privilege> PrivilegePage = privilegesService.getPrivilegesPage(pageRequest);
        pageResponse.setContent(PrivilegePage.getContent());
        pageResponse.setPageNumber(PrivilegePage.getNumber() + 1);
        pageResponse.setPageSize(PrivilegePage.getSize());
        pageResponse.setTotalElements(BigDecimal.valueOf(PrivilegePage.getTotalElements()));
        pageResponse.setTotalPages(PrivilegePage.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(pageResponse);
    }

    @Override
    public ResponseEntity<Privilege> _patchPrivilege(Long id, PrivilegeBody privilegeBody) {
        log.debug("_patchPrivilege id: {}", id);
        Privilege privilege = privilegesService.patch(id, privilegeBody);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(privilege); // Return 201 Created with the created entity
    }

    @Override
    public ResponseEntity<Privilege> _postPrivileges(PrivilegeBody privilegeBody) {
        log.debug("_postPrivilege");

        Privilege privilege = privilegesService.postPrivilege(false, privilegeBody); // Call the service method

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(getVersionHeaders())
                .body(privilege); // Return 201 Created with the created entity
    }

}