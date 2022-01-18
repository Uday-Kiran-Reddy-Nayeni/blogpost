package com.uday.project.blogpost.repository;


import com.uday.project.blogpost.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "select * from users u where u.email=:email", nativeQuery = true)
    User findByUserName(@Param("email") String email);
}
