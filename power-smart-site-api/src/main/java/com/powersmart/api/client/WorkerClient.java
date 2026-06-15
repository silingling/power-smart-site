package com.powersmart.api.client;

import com.powersmart.common.entity.Result;
import com.powersmart.worker.entity.Worker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "power-smart-worker")
public interface WorkerClient {
    @GetMapping("/api/v1/workers/{id}")
    Result<Worker> getWorker(@PathVariable Long id);

    @GetMapping("/api/v1/workers")
    Result<List<Worker>> listWorkers(@RequestParam Long projectId,
                                     @RequestParam(required = false) Long teamId,
                                     @RequestParam(required = false) Integer status);
}
