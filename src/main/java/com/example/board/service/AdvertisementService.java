package com.example.board.service;

import com.example.board.dto.PostAdvertisementDto;
import com.example.board.entity.Advertisment;
import com.example.board.repository.AdvertisementRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    public Advertisment postAdvertisement(PostAdvertisementDto dto) {
        Advertisment advertisment = new Advertisment();
        advertisment.setTitle(dto.getTitle());
        advertisment.setContents(dto.getContents());
        advertisment.setIsVisible(dto.getIsVisible());
        advertisment.setStartDate(dto.getStartDate());
        advertisment.setEndDate(dto.getEndDate());

        return this.advertisementRepository.save(advertisment);
    }

    public List<Advertisment> getAdvertisementList() {
        return this.advertisementRepository.findAll();
    }

    public Advertisment getAdvertisement(Long id) throws BadRequestException {
        return this.advertisementRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Advertisement not found"));
    }
}
