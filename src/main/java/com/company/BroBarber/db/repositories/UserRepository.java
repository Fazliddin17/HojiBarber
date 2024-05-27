package com.company.BroBarber.db.repositories;

import com.company.BroBarber.db.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByChatId(Long chatId);
}
