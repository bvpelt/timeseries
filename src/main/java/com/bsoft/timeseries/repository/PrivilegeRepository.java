package com.bsoft.timeseries.repository;


import com.bsoft.timeseries.entity.PrivilegeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivilegeRepository extends PagingAndSortingRepository<PrivilegeEntity, Long>,
        CrudRepository<PrivilegeEntity, Long>,
        JpaSpecificationExecutor<PrivilegeEntity> {

    @Query(value = "SELECT * FROM privilege where name = :privilegename", nativeQuery = true)
    Optional<PrivilegeEntity> findByPrivilegename(final String privilegename);

    @Query(value = "SELECT * FROM privilege where id = :id", nativeQuery = true)
    Optional<PrivilegeEntity> findByPrivilegeId(Long id);

    @Query(value = "SELECT * FROM privilege",
            countQuery = "SELECT * FROM privilege",
            nativeQuery = true)
    List<PrivilegeEntity> findAllByPaged(final Pageable pageable);

    @Query(value = "SELECT * FROM privilege WHERE hash = :hash", nativeQuery = true)
    Optional<PrivilegeEntity> findByHash(@Param("hash") Integer hash);

    @Query(value = "SELECT * FROM privilege",
            countQuery = "SELECT * FROM privilege",
            nativeQuery = true)
    Page<PrivilegeEntity> findAllByPage(PageRequest pageRequest);
}