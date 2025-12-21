package com.example.is_rogue_trader.service;

import com.example.is_rogue_trader.model.entity.Planet;
import com.example.is_rogue_trader.model.entity.Project;
import com.example.is_rogue_trader.model.entity.Upgrade;
import com.example.is_rogue_trader.model.enums.ProjectStatus;
import com.example.is_rogue_trader.repository.PlanetRepository;
import com.example.is_rogue_trader.repository.ProjectRepository;
import com.example.is_rogue_trader.repository.UpgradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final PlanetRepository planetRepository;
    private final UpgradeRepository upgradeRepository;

    public List<Project> getProjectsByPlanet(Long planetId) {
        return projectRepository.findByPlanetId(planetId);
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));
    }

    /**
     * Создает проект. Триггер check_project_resources автоматически проверит ресурсы
     * и спишет их при создании проекта со статусом PLANNED
     */
    @Transactional
    public Project createProject(Long planetId, Long upgradeId) {
        Planet planet = planetRepository.findById(planetId)
                .orElseThrow(() -> new RuntimeException("Планета не найдена"));
        Upgrade upgrade = upgradeRepository.findById(upgradeId)
                .orElseThrow(() -> new RuntimeException("Улучшение не найдено"));

        // Проверяем совместимость типа планеты и улучшения
        if (!planet.getPlanetType().equals(upgrade.getSuitableTypes())) {
            throw new IllegalArgumentException("Тип планеты не совместим с типом улучшения");
        }

        Project project = new Project();
        project.setPlanet(planet);
        project.setUpgrade(upgrade);
        project.setStatus(ProjectStatus.PLANNED);

        // Триггер автоматически проверит ресурсы и спишет их
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProjectStatus(Long projectId, ProjectStatus status) {
        Project project = getProjectById(projectId);
        project.setStatus(status);
        if (status == ProjectStatus.COMPLETED) {
            project.setCompletionDate(java.time.LocalDateTime.now());
        }
        return projectRepository.save(project);
    }
}

