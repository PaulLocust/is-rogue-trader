# Диаграмма классов архитектуры системы

## Общая архитектура системы Rogue Trader

```
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                        │
│                         (REST Controllers)                       │
├─────────────────────────────────────────────────────────────────┤
│  TraderController  │  PlanetController  │  MessageController    │
│  EventController   │  ProjectController │  RouteController      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Business Logic Layer                        │
│                          (Services)                             │
├─────────────────────────────────────────────────────────────────┤
│  TraderService     │  PlanetService     │  MessageService       │
│  EventService      │  ProjectService    │  RouteService          │
│  EmpireService     │  CrisisService     │                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data Access Layer                          │
│                        (Repositories)                           │
├─────────────────────────────────────────────────────────────────┤
│  UserRepository    │  PlanetRepository  │  MessageRepository    │
│  RogueTraderRepo   │  EventRepository   │  ProjectRepository    │
│  GovernorRepo      │  AstropathRepo     │  NavigatorRepo        │
│  RouteRepository   │  UpgradeRepository │                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Entity Layer                               │
│                        (JPA Entities)                           │
├─────────────────────────────────────────────────────────────────┤
│  User              │  Planet           │  Message               │
│  RogueTrader       │  Event            │  Project                │
│  Governor          │  Astropath        │  Navigator             │
│  Route             │  Upgrade          │                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Database Layer                               │
│                    PostgreSQL + PL/pgSQL                        │
├─────────────────────────────────────────────────────────────────┤
│  send_message()    │  resolve_crisis()  │  get_empire_resources()│
│  Triggers          │  Constraints       │  Indexes               │
└─────────────────────────────────────────────────────────────────┘
```

## Детальная структура классов

### Entity Layer (Сущности)

```
User
├── id: Long
├── email: String
├── passwordHash: String
├── role: UserRole (TRADER, GOVERNOR, ASTROPATH, NAVIGATOR)
└── relationships: OneToOne with RogueTrader/Governor/Astropath/Navigator

RogueTrader
├── id: Long
├── user: User (OneToOne)
├── dynastyName: String
├── warrantNumber: String
├── totalWealth: BigDecimal
├── influence: Integer
└── planets: List<Planet> (OneToMany)

Planet
├── id: Long
├── name: String
├── planetType: PlanetType (enum)
├── loyalty: BigDecimal
├── wealth: BigDecimal
├── industry: BigDecimal
├── resources: BigDecimal
├── isRebellious: Boolean
├── trader: RogueTrader (ManyToOne)
└── events: List<Event> (OneToMany)

Message
├── id: Long
├── sender: User (ManyToOne)
├── receiver: User (ManyToOne)
├── content: String
├── sentAt: LocalDateTime
├── delivered: Boolean
└── distorted: Boolean

Event
├── id: Long
├── planet: Planet (ManyToOne)
├── eventType: EventType (enum)
├── severity: Integer
├── description: String
├── resolved: Boolean
└── occurredAt: LocalDateTime

Project
├── id: Long
├── planet: Planet (ManyToOne)
├── upgrade: Upgrade (ManyToOne)
├── startDate: LocalDateTime
├── completionDate: LocalDateTime
└── status: ProjectStatus (enum)

Route
├── id: Long
├── fromPlanet: Planet (ManyToOne)
├── toPlanet: Planet (ManyToOne)
├── navigator: Navigator (ManyToOne)
└── isStable: Boolean

Upgrade
├── id: Long
├── name: String
├── description: String
├── costWealth: BigDecimal
├── costIndustry: BigDecimal
├── costResources: BigDecimal
└── suitableTypes: PlanetType

Governor
├── id: Long
├── user: User (OneToOne)
└── planet: Planet (OneToOne)

Astropath
├── id: Long
├── user: User (OneToOne)
└── psiLevel: Integer

Navigator
├── id: Long
├── user: User (OneToOne)
└── houseName: String
```

### Repository Layer (Репозитории)

```
UserRepository extends JpaRepository<User, Long>
RogueTraderRepository extends JpaRepository<RogueTrader, Long>
PlanetRepository extends JpaRepository<Planet, Long>
MessageRepository extends JpaRepository<Message, Long>
EventRepository extends JpaRepository<Event, Long>
ProjectRepository extends JpaRepository<Project, Long>
RouteRepository extends JpaRepository<Route, Long>
UpgradeRepository extends JpaRepository<Upgrade, Long>
GovernorRepository extends JpaRepository<Governor, Long>
AstropathRepository extends JpaRepository<Astropath, Long>
NavigatorRepository extends JpaRepository<Navigator, Long>
```

### Service Layer (Бизнес-логика)

```
TraderService
├── getTraderById(Long id): RogueTrader
├── getTraderPlanets(Long traderId): List<Planet>
└── updateTraderInfluence(Long traderId, Integer influence): void

PlanetService
├── getPlanetById(Long id): Planet
├── getPlanetsByTrader(Long traderId): List<Planet>
├── updatePlanetLoyalty(Long planetId, BigDecimal loyalty): void
└── getRebelliousPlanets(Long traderId): List<Planet>

MessageService
├── sendMessage(Long senderId, Long receiverId, String content, BigDecimal distortionChance): Integer
│   └── Uses PL/pgSQL: send_message()
└── getMessagesForUser(Long userId): List<Message>

EventService
├── getActiveEvents(Long planetId): List<Event>
├── resolveCrisis(Long eventId, String action, BigDecimal wealth, BigDecimal industry): void
│   └── Uses PL/pgSQL: resolve_crisis()
└── createEvent(Event event): Event

ProjectService
├── createProject(Long planetId, Long upgradeId): Project
├── getProjectsByPlanet(Long planetId): List<Project>
└── updateProjectStatus(Long projectId, ProjectStatus status): void

EmpireService
├── getEmpireResources(Long traderId): EmpireResourcesDTO
│   └── Uses PL/pgSQL: get_empire_resources()
└── calculateTotalInfluence(Long traderId): Integer

RouteService
├── createRoute(Long fromPlanetId, Long toPlanetId, Long navigatorId): Route
├── getRoutesByNavigator(Long navigatorId): List<Route>
└── checkRouteStability(Long routeId): Boolean
```

### Controller Layer (REST API)

```
TraderController
├── GET /api/traders/{id}
├── GET /api/traders/{id}/planets
└── PUT /api/traders/{id}/influence

PlanetController
├── GET /api/planets/{id}
├── GET /api/planets/trader/{traderId}
├── PUT /api/planets/{id}/loyalty
└── GET /api/planets/trader/{traderId}/rebellious

MessageController
├── POST /api/messages
├── GET /api/messages/user/{userId}
└── GET /api/messages/{id}

EventController
├── GET /api/events/planet/{planetId}
├── POST /api/events
├── POST /api/events/{id}/resolve
└── GET /api/events/{id}

ProjectController
├── POST /api/projects
├── GET /api/projects/planet/{planetId}
├── PUT /api/projects/{id}/status
└── GET /api/projects/{id}

EmpireController
├── GET /api/empire/{traderId}/resources
└── GET /api/empire/{traderId}/influence

RouteController
├── POST /api/routes
├── GET /api/routes/navigator/{navigatorId}
└── GET /api/routes/{id}
```

## Использование PL/pgSQL функций

1. **send_message()** - используется в MessageService.sendMessage()
2. **resolve_crisis()** - используется в EventService.resolveCrisis()
3. **get_empire_resources()** - используется в EmpireService.getEmpireResources()

Все функции вызываются через EntityManager.createNativeQuery() или через @Query с nativeQuery=true.

