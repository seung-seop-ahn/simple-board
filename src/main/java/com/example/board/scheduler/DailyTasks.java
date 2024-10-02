package com.example.board.scheduler;

import com.example.board.dto.AdvertisementViewHistoryResult;
import com.example.board.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DailyTasks {

    private final AdvertisementService advertisementService;

    @Autowired
    public DailyTasks(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void insertAdvertisementViewHistoryStat() {
        List< AdvertisementViewHistoryResult> result = this.advertisementService.getAdViewHistoryGroupedByAdId();
        this.advertisementService.insertAdvertisementViewHistoryStat(result);
    }

}
