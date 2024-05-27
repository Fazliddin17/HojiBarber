package com.company.BroBarber.db.repositories;

import com.company.BroBarber.db.domain.Barber;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BarberRepository extends JpaRepository<Barber, Long> {
    Optional<Barber> findByChatId(Long chatId);
    @Query(value = "select * from barbers where level=3", nativeQuery = true)
    Barber findByLevel();
    @Transactional
    @Modifying
    @Query(value = """
            DELETE FROM barbers WHERE id = :id
            """, nativeQuery = true)
    void deleteById(Long id);
    List<Barber> findAll();

    Barber findByFullName(String fullName);
}
