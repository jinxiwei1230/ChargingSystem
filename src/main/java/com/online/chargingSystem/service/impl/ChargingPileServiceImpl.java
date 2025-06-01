package com.online.chargingSystem.service.impl;

import com.online.chargingSystem.entity.ChargingPile;
import com.online.chargingSystem.entity.enums.ChargingPileStatus;
import com.online.chargingSystem.mapper.ChargingPileMapper;
import com.online.chargingSystem.service.ChargingPileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChargingPileServiceImpl implements ChargingPileService {

    @Autowired
    private ChargingPileMapper chargingPileMapper;

    @Override
    public ChargingPile getPileStatus(String pileId) {
        return chargingPileMapper.findById(pileId);
    }

    @Override
    @Transactional
    public boolean reportFault(String pileId, String faultType, String description) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.FAULT);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override
    @Transactional
    public boolean resolveFault(String pileId, String resolution) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.AVAILABLE);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override
    @Transactional
    public boolean powerOn(String pileId) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.AVAILABLE);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override
    @Transactional
    public boolean setParameters(String pileId, Double chargingPower) {
        return chargingPileMapper.updateParameters(pileId, chargingPower) > 0;
    }

    @Override
    @Transactional
    public boolean startChargingPile(String pileId) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.IN_USE);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override
    @Transactional
    public boolean powerOff(String pileId) {
        ChargingPile pile = chargingPileMapper.findById(pileId);
        if (pile == null) {
            return false;
        }
        pile.setStatus(ChargingPileStatus.OFFLINE);
        return chargingPileMapper.update(pile) > 0;
    }

    @Override//重复不用
    public ChargingPile queryPileState(String pileId) {
        return chargingPileMapper.findById(pileId);
    }
} 