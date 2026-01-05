package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.service.TimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/time")
@RequiredArgsConstructor
@Tag(name = "Время", description = "API для управления временными циклами")
public class TimeController {
    private final TimeService timeService;

    @PostMapping("/advance/{traderId}")
    @Operation(summary = "Пропустить цикл времени", 
               description = "Продвигает время на один цикл: обновляет проекты и генерирует события",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Map<String, String>> advanceTimeCycle(
            @Parameter(description = "ID торговца", required = true) @PathVariable Long traderId) {
        timeService.advanceTimeCycle(traderId);
        return ResponseEntity.ok(Map.of("message", "Время продвинуто на один цикл"));
    }
}

