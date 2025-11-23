package com.aquarius.crypto.repository;

import com.aquarius.crypto.model.User;;
import org.springframework.data.repository.PagingAndSortingRepository;;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Long> {
    List<User> findAll();

    User findByUsername(String userName);

    User save(User user);
}
