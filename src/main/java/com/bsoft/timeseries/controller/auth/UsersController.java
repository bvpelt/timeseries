package com.bsoft.timeseries.controller.auth;


import com.bsoft.timeseries.authentication.api.UsersApi;
import com.bsoft.timeseries.authentication.model.PagedUsers;
import com.bsoft.timeseries.authentication.model.User;
import com.bsoft.timeseries.authentication.model.UserBody;
import com.bsoft.timeseries.controller.ControllerSortUtil;
import com.bsoft.timeseries.exception.InvalidParameterException;
import com.bsoft.timeseries.exception.UserNotDeletedException;
import com.bsoft.timeseries.service.UsersService;
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
public class UsersController implements UsersApi {

    private final UsersService usersService;

    @Value("${info.project.version}")
    private String version;

    private HttpHeaders getVersionHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Version", version);
        return headers;
    }

    @Override
    public ResponseEntity<Void> _deleteUser(Long id) {
        log.debug("_deleteUser id: {}", id);
        boolean deleted = usersService.deleteUser(id);
        if (!deleted) {
            throw new UserNotDeletedException("User not deleted");
        }

        HttpHeaders headers = getVersionHeaders();

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> _deleteAllUsers() {
        log.debug("_deleteAllUsers");
        usersService.deleteAll();

        HttpHeaders headers = getVersionHeaders();

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<User> _getUser(Long userId) {
        log.debug("_getUser id: {}", userId);
        User user = usersService.getUser(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(user); // Return 201 Created with the created entity
    }

    @Override
    public ResponseEntity<PagedUsers> _getUsers(Integer page, Integer size, List<String> sort) {

        log.debug("_getUsers page: {} size: {},  sort: {}", page, size, sort.toString());
        List<Sort.Order> sortParameter;
        PageRequest pageRequest;
        log.info("Get adresses for pagenumber: {} pagesize: {}, sort: {}", page, size, sort);
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

        PagedUsers pageResponse = new PagedUsers();
        Page<User> usersPage = usersService.getUsersPage(pageRequest);
        pageResponse.setContent(usersPage.getContent());
        pageResponse.setPageNumber(usersPage.getNumber() + 1);
        pageResponse.setPageSize(usersPage.getSize());
        pageResponse.setTotalElements(BigDecimal.valueOf(usersPage.getTotalElements()));
        pageResponse.setTotalPages(usersPage.getTotalPages());

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(pageResponse);
    }

    @Override
    public ResponseEntity<User> _patchUser(Long id, UserBody userBody) {
        log.debug("_patchUser");
        User user = usersService.patch(id, userBody);

        return ResponseEntity.status(HttpStatus.OK)
                .headers(getVersionHeaders())
                .body(user); // Return 201 Created with the created entity
    }

    @Override
    public ResponseEntity<User> _postUser(UserBody userBody) {
        log.debug("_postUser");

        User user = usersService.postUser(false, userBody); // Call the service method

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(getVersionHeaders())
                .body(user); // Return 201 Created with the created entity
    }

}