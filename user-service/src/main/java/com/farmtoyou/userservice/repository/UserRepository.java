package com.farmtoyou.userservice.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.farmtoyou.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findByEmail(String email);
	
}
