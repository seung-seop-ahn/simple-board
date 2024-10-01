package com.example.board.service;

import com.example.board.dto.PostAdvertisementDto;
import com.example.board.entity.Advertisement;
import com.example.board.entity.AdvertisementClickHistory;
import com.example.board.entity.AdvertisementViewHistory;
import com.example.board.repository.AdvertisementClickHistoryRepository;
import com.example.board.repository.AdvertisementRepository;
import com.example.board.repository.AdvertisementViewHistoryRepository;
import jakarta.transaction.Transactional;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdvertisementService {

    private static final String REDIS_KEY = "Advertisement";

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementViewHistoryRepository advertisementViewHistoryRepository;
    private final AdvertisementClickHistoryRepository advertisementClickHistoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository, AdvertisementViewHistoryRepository advertisementViewHistoryRepository, AdvertisementClickHistoryRepository advertisementClickHistoryRepository, RedisTemplate<String, Object> redisTemplate) {
        this.advertisementRepository = advertisementRepository;
        this.advertisementViewHistoryRepository = advertisementViewHistoryRepository;
        this.advertisementClickHistoryRepository = advertisementClickHistoryRepository;
        this.redisTemplate = redisTemplate;
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
}
