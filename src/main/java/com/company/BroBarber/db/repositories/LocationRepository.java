package com.company.BroBarber.db.repositories;

import com.company.BroBarber.db.domain.Location;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location , Long> {
}
