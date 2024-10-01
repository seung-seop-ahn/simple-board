package com.example.board.repository;

import com.example.board.entity.AdvertisementViewHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementViewHistoryRepository extends MongoRepository<AdvertisementViewHistory, Long> {
}
