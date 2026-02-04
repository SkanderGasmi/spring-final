package com.project.back_end.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.back_end.models.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * Find an admin by their username.
     * 
     * @param username the username to search for
     * @return the Admin entity with the matching username, or null if not found
     */
    Admin findByUsername(String username);
}