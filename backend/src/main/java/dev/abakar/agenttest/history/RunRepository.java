package dev.abakar.agenttest.history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RunRepository extends JpaRepository<RunEntity, Long> {

    List<RunEntity> findAllByOrderByIdDesc();
}
