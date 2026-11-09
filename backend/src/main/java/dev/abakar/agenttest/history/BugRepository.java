package dev.abakar.agenttest.history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BugRepository extends JpaRepository<BugEntity, Long> {

    List<BugEntity> findByRunSiteAndStatus(String site, ValidationStatus status);
}
