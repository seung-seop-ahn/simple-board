package com.example.board.service;

import com.example.board.dto.PostAdvertisementDto;
import com.example.board.entity.Advertisement;
import com.example.board.repository.AdvertisementRepository;
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
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository, RedisTemplate<String, Object> redisTemplate) {
        this.advertisementRepository = advertisementRepository;
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

    public Advertisement getAdvertisement(Long id) throws BadRequestException {
        Object cached = this.redisTemplate.opsForHash().get(REDIS_KEY, id);
        if (cached != null) {
            System.out.println("cached");
            return (Advertisement) cached;
        }

        System.out.println("not cached");
        return this.advertisementRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Advertisement not found"));
    }
}
