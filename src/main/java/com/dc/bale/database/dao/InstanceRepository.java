package com.dc.bale.database.dao;

import com.dc.bale.database.entity.Instance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceRepository extends JpaRepository<Instance, Long> {
}
