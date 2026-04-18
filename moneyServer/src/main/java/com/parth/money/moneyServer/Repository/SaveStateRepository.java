package com.parth.money.moneyServer.Repository;

import com.parth.money.moneyServer.Entity.SaveState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaveStateRepository extends MongoRepository<SaveState, String> {
    Optional<SaveState> findBySaveStateIdAndStatus(String saveStateId, String status);
}
