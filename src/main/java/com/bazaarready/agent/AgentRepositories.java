package com.bazaarready.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface AgentSessionRepository extends JpaRepository<AgentSession, UUID> {
}

interface AgentActionRepository extends JpaRepository<AgentAction, UUID> {

    List<AgentAction> findBySessionIdOrderByStepNo(UUID sessionId);

    Optional<AgentAction> findFirstBySessionIdOrderByStepNoDesc(UUID sessionId);

    int countBySessionId(UUID sessionId);
}
