package com.example.board.repository;

import com.example.board.entity.AdvertisementViewHistoryStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementViewHistoryStatRepository extends JpaRepository<AdvertisementViewHistoryStat, Long> {
}
