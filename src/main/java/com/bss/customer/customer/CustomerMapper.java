package com.bss.customer.customer;

import java.time.Instant;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.bss.customer.account.Account;

@Mapper
public interface CustomerMapper {

    void insert(Customer customer);

    void updateContact(@Param("customerId") long customerId,
                       @Param("phone") String phone,
                       @Param("email") String email,
                       @Param("address") String address,
                       @Param("updatedAt") Instant updatedAt);

    List<Account> findAssigned(@Param("consultantId") String consultantId,
                               @Param("offset") int offset,
                               @Param("size") int size);

    long countAssigned(@Param("consultantId") String consultantId);

    List<Long> findAssignedIds(@Param("consultantId") String consultantId);

    List<Account> searchAssigned(@Param("search") CustomerSearch search);

    long countSearchAssigned(@Param("search") CustomerSearch search);

    void updateAssignedConsultant(@Param("customerId") long customerId,
                                  @Param("consultantId") String consultantId,
                                  @Param("updatedAt") Instant updatedAt);

    void clearAssignments(@Param("consultantId") String consultantId, @Param("updatedAt") Instant updatedAt);

    void updateInternal(@Param("customerId") long customerId,
                        @Param("creditStatus") String creditStatus,
                        @Param("collectionStatus") String collectionStatus,
                        @Param("assignedConsultantId") String assignedConsultantId,
                        @Param("updatedAt") Instant updatedAt);

    int countOpenSubscriptions(@Param("customerId") long customerId);
}
