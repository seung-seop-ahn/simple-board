package com.example.board.controller;

import com.example.board.dto.PostAdvertisementDto;
import com.example.board.entity.Advertisment;
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
    public ResponseEntity<Advertisment> postAdvertisement(@Valid @RequestBody PostAdvertisementDto dto) {
        Advertisment advertisment = this.advertisementService.postAdvertisement(dto);
        return ResponseEntity.ok(advertisment);
    }

    @GetMapping()
    public ResponseEntity<List<Advertisment>> getAdvertisementList() {
        List<Advertisment> list = this.advertisementService.getAdvertisementList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Advertisment> getAdvertisement(@PathVariable Long id) throws BadRequestException {
        Advertisment advertisment = this.advertisementService.getAdvertisement(id);
        return ResponseEntity.ok(advertisment);
    }
}
