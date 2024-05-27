package com.company.BroBarber.db.repositories;

import com.company.BroBarber.db.domain.Time;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TimeRepository extends JpaRepository<Time, Long> {
    @Transactional
    @Modifying
    @Query(value = """
            DELETE FROM times WHERE barber_id = :barberId
            """, nativeQuery = true)
    void deleteAllByBarberId(Long barberId);
}
