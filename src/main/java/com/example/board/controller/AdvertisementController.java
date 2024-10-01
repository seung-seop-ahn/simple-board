package com.example.board.controller;

import com.example.board.dto.PostAdvertisementDto;
import com.example.board.entity.Advertisement;
import com.example.board.service.AdvertisementService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @Autowired
    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @PostMapping()
    public ResponseEntity<Advertisement> postAdvertisement(@Valid @RequestBody PostAdvertisementDto dto) {
        Advertisement advertisement = this.advertisementService.postAdvertisement(dto);
        return ResponseEntity.ok(advertisement);
    }

    @GetMapping()
    public ResponseEntity<List<Advertisement>> getAdvertisementList() {
        List<Advertisement> list = this.advertisementService.getAdvertisementList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Advertisement> getAdvertisement(@PathVariable Long id) throws BadRequestException {
        Advertisement advertisement = this.advertisementService.getAdvertisement(id);
        return ResponseEntity.ok(advertisement);
    }
}
