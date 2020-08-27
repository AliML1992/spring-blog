package com.silindele.blog.repository;

import com.silindele.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String s);
    User findByPassword(String p);
    boolean existsByUsername(String u);
    boolean existsByEmail(String e);

}
