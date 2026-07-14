package com.example.demo.repository;

import com.example.demo.entity.ResourceAllocation;
import com.example.demo.entity.enums.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {

    List<ResourceAllocation> findByProjectIdOrderByStartDateAscIdAsc(Long projectId);

    @Query("""
            select r from ResourceAllocation r
            where r.user.id = :userId
              and r.status in :statuses
              and r.startDate <= :endDate
              and r.endDate >= :startDate
            order by r.startDate asc, r.id asc
            """)
    List<ResourceAllocation> findCapacityAllocationsOverlapping(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<AllocationStatus> statuses);

    @Query("""
            select r from ResourceAllocation r
            where r.user.id in :userIds
              and r.status in :statuses
              and r.startDate <= :focusDate
              and r.endDate >= :focusDate
            """)
    List<ResourceAllocation> findCapacityAllocationsAtDate(
            @Param("userIds") Collection<Long> userIds,
            @Param("focusDate") LocalDate focusDate,
            @Param("statuses") Collection<AllocationStatus> statuses);
}
