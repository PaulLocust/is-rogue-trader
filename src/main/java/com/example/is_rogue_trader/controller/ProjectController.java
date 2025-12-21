package com.example.is_rogue_trader.controller;

import com.example.is_rogue_trader.dto.CreateProjectRequest;
import com.example.is_rogue_trader.model.entity.Project;
import com.example.is_rogue_trader.model.enums.ProjectStatus;
import com.example.is_rogue_trader.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Проекты", description = "API для управления проектами улучшений. " +
        "При создании проекта со статусом PLANNED триггер автоматически проверяет и списывает ресурсы.")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Создать проект", 
               description = "Создает проект улучшения на планете. " +
                           "Триггер check_project_resources автоматически проверит ресурсы и спишет их.",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Project> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request.getPlanetId(), request.getUpgradeId()));
    }

    @GetMapping("/planet/{planetId}")
    @Operation(summary = "Получить проекты планеты", description = "Возвращает все проекты планеты",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Project>> getProjectsByPlanet(
            @Parameter(description = "ID планеты", required = true) @PathVariable Long planetId) {
        return ResponseEntity.ok(projectService.getProjectsByPlanet(planetId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить проект по ID", description = "Возвращает информацию о проекте",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Project> getProject(
            @Parameter(description = "ID проекта", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Обновить статус проекта", description = "Обновляет статус проекта",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Project> updateProjectStatus(
            @Parameter(description = "ID проекта", required = true) @PathVariable Long id,
            @Parameter(description = "Новый статус", required = true) @RequestParam ProjectStatus status) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, status));
    }
}

