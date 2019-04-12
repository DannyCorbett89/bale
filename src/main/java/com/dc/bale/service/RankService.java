package com.dc.bale.service;

import com.dc.bale.database.dao.FcRankRepository;
import com.dc.bale.database.entity.FcRank;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RankService {
    @NonNull
    private FcRankRepository rankRepository;

    public List<FcRank> listRanks() {
        return rankRepository.findAll();
    }

    public List<FcRank> listEnabledRanks() {
        return rankRepository.findAllByEnabledTrue();
    }

    public void setRanksEnabled(List<FcRank> ranks) {
        Map<Long, Boolean> enabledMap = ranks.stream().collect(Collectors.toMap(FcRank::getId, FcRank::isEnabled));
        List<FcRank> ranksFromDatabase = rankRepository.findAll(enabledMap.keySet());
        List<FcRank> updatedRanks = ranksFromDatabase.stream()
                .peek(rank -> rank.setEnabled(enabledMap.get(rank.getId())))
                .collect(Collectors.toList());
        rankRepository.save(updatedRanks);
    }
}