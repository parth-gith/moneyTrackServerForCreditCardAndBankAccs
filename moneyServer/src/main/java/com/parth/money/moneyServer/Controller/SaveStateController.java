package com.parth.money.moneyServer.Controller;

import com.parth.money.moneyServer.Entity.SaveState;
import com.parth.money.moneyServer.Entity.SaveStateResponse;
import com.parth.money.moneyServer.Repository.SaveStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/moneyServer")
public class SaveStateController {

    private static final String LIVE_STATUS = "LIVE";
    private static final String AUDIT_STATUS = "AUDIT";
    private static final String BANK_SAVE_STATE_TYPE = "BANK";

    @Autowired
    SaveStateRepository saveStateRepository;

    @PostMapping("/saveState")
    public SaveState saveState(@RequestBody SaveState saveState) {
        if (saveState.getSaveStateId() == null || saveState.getSaveStateId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "saveStateId is required");
        }
        saveState.setStatus(LIVE_STATUS);

        return saveStateRepository.findById(saveState.getSaveStateId())
                .map(existingSaveState -> saveStateRepository.save(updateExistingSaveState(existingSaveState, saveState)))
                .orElseGet(() -> saveStateRepository.save(saveState));
    }

    @GetMapping("/saveState/{id}")
    public SaveStateResponse getSaveStateById(@PathVariable String id) {
        return saveStateRepository.findBySaveStateIdAndStatus(id, LIVE_STATUS)
                .map(saveState -> new SaveStateResponse(true, saveState))
                .orElse(new SaveStateResponse(false, null));
    }

    @PostMapping("/saveState/{id}/markAsAudit")
    public SaveStateResponse markAsAudit(@PathVariable String id, @RequestBody(required = false) SaveState saveStateRequest) {
        return saveStateRepository.findBySaveStateIdAndStatus(id, LIVE_STATUS)
                .map(saveState -> {
                    saveState.setStatus(AUDIT_STATUS);
                    if (saveStateRequest != null && saveStateRequest.getLastUpdated() != null) {
                        saveState.setLastUpdated(saveStateRequest.getLastUpdated());
                    }
                    if (BANK_SAVE_STATE_TYPE.equalsIgnoreCase(saveState.getSaveStateType())) {
                        String liveSaveStateId = saveState.getSaveStateId();
                        saveState.setSaveStateId(liveSaveStateId + "-" + UUID.randomUUID());
                        SaveState auditSaveState = saveStateRepository.save(saveState);
                        saveStateRepository.deleteById(liveSaveStateId);
                        return new SaveStateResponse(true, auditSaveState);
                    }
                    return new SaveStateResponse(true, saveStateRepository.save(saveState));
                })
                .orElse(new SaveStateResponse(false, null));
    }

    private SaveState updateExistingSaveState(SaveState existingSaveState, SaveState newSaveState) {
        existingSaveState.setStatus(LIVE_STATUS);
        if (newSaveState.getLastUpdated() != null) {
            existingSaveState.setLastUpdated(newSaveState.getLastUpdated());
        }
        if (newSaveState.getSaveStateRows() != null) {
            existingSaveState.setSaveStateRows(newSaveState.getSaveStateRows());
        }
        return existingSaveState;
    }
}
