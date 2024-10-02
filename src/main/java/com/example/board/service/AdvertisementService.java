package com.example.board.service;

import com.example.board.dto.AdvertisementViewHistoryResult;
import com.example.board.dto.PostAdvertisementDto;
import com.example.board.entity.Advertisement;
import com.example.board.entity.AdvertisementClickHistory;
import com.example.board.entity.AdvertisementViewHistory;
import com.example.board.entity.AdvertisementViewHistoryStat;
import com.example.board.repository.AdvertisementClickHistoryRepository;
import com.example.board.repository.AdvertisementRepository;
import com.example.board.repository.AdvertisementViewHistoryRepository;
import com.example.board.repository.AdvertisementViewHistoryStatRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdvertisementService {

    private static final String REDIS_KEY = "Advertisement";

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementViewHistoryRepository advertisementViewHistoryRepository;
    private final AdvertisementClickHistoryRepository advertisementClickHistoryRepository;
    private final AdvertisementViewHistoryStatRepository advertisementViewHistoryStatRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository, AdvertisementViewHistoryRepository advertisementViewHistoryRepository, AdvertisementClickHistoryRepository advertisementClickHistoryRepository, AdvertisementViewHistoryStatRepository advertisementViewHistoryStatRepository, RedisTemplate<String, Object> redisTemplate, MongoTemplate mongoTemplate) {
        this.advertisementRepository = advertisementRepository;
        this.advertisementViewHistoryRepository = advertisementViewHistoryRepository;
        this.advertisementClickHistoryRepository = advertisementClickHistoryRepository;
        this.advertisementViewHistoryStatRepository = advertisementViewHistoryStatRepository;
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional
    public Advertisement postAdvertisement(PostAdvertisementDto dto) {
        Advertisement advertisement = new Advertisement();
        advertisement.setTitle(dto.getTitle());
        advertisement.setContents(dto.getContents());
        advertisement.setIsVisible(dto.getIsVisible());
        advertisement.setStartDate(dto.getStartDate());
        advertisement.setEndDate(dto.getEndDate());

        Advertisement savedAdvertisement = this.advertisementRepository.save(advertisement);
        this.redisTemplate.opsForHash().put(REDIS_KEY, savedAdvertisement.getId(), savedAdvertisement);

        return savedAdvertisement;
    }

    public List<Advertisement> getAdvertisementList() {
        // todo: cache
        return this.advertisementRepository.findAll();
    }

    public Advertisement getAdvertisement(Long id, String username, String ip, Boolean isTrueView) throws BadRequestException {
        this.insertAdvertisementViewHistory(id, username, ip, isTrueView);

        Object cached = this.redisTemplate.opsForHash().get(REDIS_KEY, id);
        if (cached != null) {
            System.out.println("cached");
            return (Advertisement) cached;
        }

        System.out.println("not cached");
        return this.advertisementRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Advertisement not found"));
    }

    public void clickAdvertisement(Long advertisementId, String username, String ip) {
        AdvertisementClickHistory advertisementClickHistory = new AdvertisementClickHistory();
        advertisementClickHistory.setAdvertisementId(advertisementId);
        advertisementClickHistory.setUsername(username);
        advertisementClickHistory.setIp(ip);

        this.advertisementClickHistoryRepository.save(advertisementClickHistory);
    }

    private void insertAdvertisementViewHistory(Long advertisementId, String username, String ip, Boolean isTrueView) {
        AdvertisementViewHistory advertisementViewHistory = new AdvertisementViewHistory();
        advertisementViewHistory.setAdvertisementId(advertisementId);
        advertisementViewHistory.setUsername(username);
        advertisementViewHistory.setIp(ip);
        advertisementViewHistory.setIsTrueView(isTrueView != null && isTrueView);

        this.advertisementViewHistoryRepository.save(advertisementViewHistory);
    }

    public List<AdvertisementViewHistoryResult> getAdViewHistoryGroupedByAdId() {
        List<AdvertisementViewHistoryResult> usernameResult = this.getAdvertisementViewHistoryGroupedByAdvertisementIdAndUsername();
        List<AdvertisementViewHistoryResult> ipResult = this.getAdvertisementViewHistoryGroupedByAdvertisementIdAndIp();

        HashMap<Long, Integer> totalResult = new HashMap<>();
        for (AdvertisementViewHistoryResult result : usernameResult) {
            totalResult.put(result.getAdvertisementId(), result.getCount());
        }
        for (AdvertisementViewHistoryResult result : ipResult) {
            totalResult.merge(result.getAdvertisementId(), result.getCount(), Integer::sum);
        }

        List<AdvertisementViewHistoryResult> result = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : totalResult.entrySet()) {
            AdvertisementViewHistoryResult history = new AdvertisementViewHistoryResult();
            history.setAdvertisementId(entry.getKey());
            history.setCount(entry.getValue());
            result.add(history);
        }

//        this.insertAdvertisementViewHistoryStat(result);
        return result;
    }

    private List<AdvertisementViewHistoryResult> getAdvertisementViewHistoryGroupedByAdvertisementIdAndUsername() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(9);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(9);

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("createdDate")
                        .gte(startOfDay)
                        .lt(endOfDay)
                        .and("username").exists(true)
        );

        GroupOperation groupOperation = Aggregation.group("advertisementId")
                .addToSet("username").as("uniqueUsernames");

        ProjectionOperation projectionOperation = Aggregation.project()
                .andExpression("_id").as("advertisementId")
                .andExpression("size(uniqueUsernames)").as("count");

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation, projectionOperation);
        AggregationResults<AdvertisementViewHistoryResult> results = this.mongoTemplate.aggregate(aggregation, "ad_view_history", AdvertisementViewHistoryResult.class);

        return results.getMappedResults();
    }

    private List<AdvertisementViewHistoryResult> getAdvertisementViewHistoryGroupedByAdvertisementIdAndIp() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN).plusHours(9);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).plusHours(9);

        MatchOperation matchOperation = Aggregation.match(
                Criteria.where("createdDate")
                        .gte(startOfDay)
                        .lt(endOfDay)
                        .and("username").exists(false)
        );

        GroupOperation groupOperation = Aggregation.group("advertisementId")
                .addToSet("ip").as("uniqueIp");

        ProjectionOperation projectionOperation = Aggregation.project()
                .andExpression("_id").as("advertisementId")
                .andExpression("size(uniqueIp)").as("count");

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, groupOperation, projectionOperation);
        AggregationResults<AdvertisementViewHistoryResult> results = this.mongoTemplate.aggregate(aggregation, "ad_view_history", AdvertisementViewHistoryResult.class);

        return results.getMappedResults();
    }

    public void insertAdvertisementViewHistoryStat(List<AdvertisementViewHistoryResult> result) {
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        List<AdvertisementViewHistoryStat> stats = new ArrayList<>();
        for (AdvertisementViewHistoryResult item : result) {
            AdvertisementViewHistoryStat stat = new AdvertisementViewHistoryStat();
            stat.setAdvertisementId(item.getAdvertisementId());
            stat.setCount((long) item.getCount());
            stat.setDt(now);

            stats.add(stat);
        }

        this.advertisementViewHistoryStatRepository.saveAll(stats);
    }
}
