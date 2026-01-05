const { useState, useEffect } = React;

const API_BASE_URL = 'http://localhost:40000/api';

// User Roles
const UserRole = {
  TRADER: 'TRADER',
  GOVERNOR: 'GOVERNOR',
  ASTROPATH: 'ASTROPATH',
  NAVIGATOR: 'NAVIGATOR'
};

// API Client
const api = {
  async request(endpoint, options = {}) {
    const token = localStorage.getItem('token');
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });
    
    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Ошибка запроса' }));
      throw new Error(error.message || `HTTP ${response.status}`);
    }
    
    return response.json();
  },
  
  async register(email, password, role, additionalData = {}) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password, role, ...additionalData }),
    });
  },
  
  async login(email, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  },
  
  async getEmpireResources(traderId) {
    return this.request(`/empire/${traderId}/resources`);
  },
  
  async getPlanet(planetId) {
    return this.request(`/planets/${planetId}`);
  },
  
  async getAllPlanets() {
    return this.request('/planets');
  },
  
  async getPlanets(traderId) {
    return this.request(`/planets/trader/${traderId}`);
  },
  
  async createPlanet(traderId, name, planetType, loyalty, wealth, industry, resources) {
    return this.request('/planets', {
      method: 'POST',
      body: JSON.stringify({ 
        traderId, 
        name, 
        planetType, 
        loyalty: loyalty || 50.0, 
        wealth: wealth || 0, 
        industry: industry || 0, 
        resources: resources || 0 
      }),
    });
  },
  
  async getRebelliousPlanets(traderId) {
    return this.request(`/planets/trader/${traderId}/rebellious`);
  },
  
  async getPlanetStats(planetId) {
    return this.request(`/planets/${planetId}/stats`);
  },
  
  async getInstalledUpgrades(planetId) {
    return this.request(`/planets/${planetId}/upgrades`);
  },
  
  async canInstallUpgrade(planetId, upgradeId) {
    return this.request(`/planets/${planetId}/can-install/${upgradeId}`);
  },
  
  async getRoutes(navigatorId) {
    return this.request(`/routes/navigator/${navigatorId}`);
  },
  
  async createRoute(fromPlanetId, toPlanetId, navigatorId) {
    return this.request('/routes', {
      method: 'POST',
      body: JSON.stringify({ fromPlanetId, toPlanetId, navigatorId }),
    });
  },
  
  async getEvents() {
    return this.request('/events');
  },
  
  async resolveCrisis(eventId, action, resourcesWealth = 0, resourcesIndustry = 0) {
    return this.request(`/events/${eventId}/resolve`, {
      method: 'POST',
      body: JSON.stringify({ action, resourcesWealth, resourcesIndustry }),
    });
  },
  
  async getProjects(planetId) {
    return this.request(`/projects/planet/${planetId}`);
  },
  
  async createProject(planetId, upgradeId) {
    return this.request('/projects', {
      method: 'POST',
      body: JSON.stringify({ planetId, upgradeId }),
    });
  },
  
  async updateProjectStatus(projectId, status) {
    return this.request(`/projects/${projectId}/status?status=${status}`, {
      method: 'PUT',
    });
  },
  
  async getUpgrades() {
    return this.request('/upgrades');
  },
  
  async getUpgradesByPlanetType(planetType) {
    return this.request(`/upgrades/planet-type/${planetType}`);
  },
  
  async advanceTimeCycle(traderId) {
    return this.request(`/time/advance/${traderId}`, {
      method: 'POST',
    });
  }
};

// Empire Map Component
function EmpireMap({ planets, routes, onPlanetClick, showDetails = true }) {
  const canvasRef = React.useRef(null);

  React.useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    canvas.width = 800;
    canvas.height = 600;
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = Math.min(canvas.width, canvas.height) / 3;
    const angleStep = (2 * Math.PI) / planets.length;

    const planetPositions = new Map();

    planets.forEach((planet, index) => {
      const angle = index * angleStep;
      const x = centerX + radius * Math.cos(angle);
      const y = centerY + radius * Math.sin(angle);
      planetPositions.set(planet.id, { x, y });
    });

    // Draw routes
    ctx.strokeStyle = '#666';
    ctx.lineWidth = 2;
    routes.forEach(route => {
      const from = planetPositions.get(route.fromPlanetId);
      const to = planetPositions.get(route.toPlanetId);
      if (from && to) {
        ctx.beginPath();
        ctx.moveTo(from.x, from.y);
        ctx.lineTo(to.x, to.y);
        ctx.stroke();
      }
    });

    // Draw planets
    planets.forEach(planet => {
      const pos = planetPositions.get(planet.id);
      if (!pos) return;

      ctx.fillStyle = planet.isRebellious ? '#d32f2f' : '#4caf50';
      ctx.beginPath();
      ctx.arc(pos.x, pos.y, 15, 0, 2 * Math.PI);
      ctx.fill();

      ctx.strokeStyle = '#ffd700';
      ctx.lineWidth = 2;
      ctx.stroke();

      if (showDetails) {
        ctx.fillStyle = '#e0e0e0';
        ctx.font = '12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(planet.name, pos.x, pos.y - 25);
      }
    });
  }, [planets, routes, showDetails]);

  return (
    <div style={{ textAlign: 'center', margin: '20px 0' }}>
      <canvas
        ref={canvasRef}
        style={{
          border: '2px solid #444',
          borderRadius: '10px',
          background: '#1e1e2e',
          cursor: 'pointer',
        }}
      />
    </div>
  );
}

// Auth Component
function Auth({ onLogin }) {
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState(UserRole.TRADER);
  const [dynastyName, setDynastyName] = useState('');
  const [warrantNumber, setWarrantNumber] = useState('');
  const [planetId, setPlanetId] = useState('');
  const [psiLevel, setPsiLevel] = useState('5');
  const [houseName, setHouseName] = useState('');
  const [message, setMessage] = useState(null);
  const [loading, setLoading] = useState(false);
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage(null);
    
    try {
      if (isLogin) {
        const response = await api.login(email, password);
        localStorage.setItem('token', response.token || '');
        localStorage.setItem('user', JSON.stringify(response));
        onLogin(response);
      } else {
        const additionalData = {};
        if (role === UserRole.TRADER) {
          additionalData.dynastyName = dynastyName;
          additionalData.warrantNumber = warrantNumber;
        } else if (role === UserRole.GOVERNOR) {
          additionalData.planetId = parseInt(planetId);
        } else if (role === UserRole.ASTROPATH) {
          additionalData.psiLevel = parseInt(psiLevel);
        } else if (role === UserRole.NAVIGATOR) {
          additionalData.houseName = houseName;
        }

        const response = await api.register(email, password, role, additionalData);
        localStorage.setItem('token', response.token);
        setMessage({ type: 'success', text: 'Регистрация успешна! Теперь войдите.' });
        setIsLogin(true);
      }
    } catch (error) {
      setMessage({ type: 'error', text: error.message });
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="auth-section">
      <h2>{isLogin ? 'Вход' : 'Регистрация'}</h2>
      {message && (
        <div className={`message message-${message.type}`}>
          {message.text}
        </div>
      )}
      <form onSubmit={handleSubmit} className="auth-form">
        <div className="form-group">
          <label>Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <div className="form-group">
          <label>Пароль:</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {!isLogin && (
          <>
            <div className="form-group">
              <label>Роль:</label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value)}
                style={{
                  padding: '10px',
                  border: '1px solid #555',
                  borderRadius: '5px',
                  background: '#2d2d44',
                  color: '#e0e0e0',
                }}
              >
                <option value={UserRole.TRADER}>Вольный Торговец</option>
                <option value={UserRole.GOVERNOR}>Губернатор</option>
                <option value={UserRole.ASTROPATH}>Астропат</option>
                <option value={UserRole.NAVIGATOR}>Навигатор</option>
              </select>
            </div>
            {role === UserRole.TRADER && (
              <>
                <div className="form-group">
                  <label>Имя династии:</label>
                  <input
                    type="text"
                    value={dynastyName}
                    onChange={(e) => setDynastyName(e.target.value)}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Номер варранта:</label>
                  <input
                    type="text"
                    value={warrantNumber}
                    onChange={(e) => setWarrantNumber(e.target.value)}
                  />
                </div>
              </>
            )}
            {role === UserRole.GOVERNOR && (
              <div className="form-group">
                <label>ID планеты:</label>
                <input
                  type="number"
                  value={planetId}
                  onChange={(e) => setPlanetId(e.target.value)}
                  required
                />
              </div>
            )}
            {role === UserRole.ASTROPATH && (
              <div className="form-group">
                <label>Уровень пси (1-10):</label>
                <input
                  type="number"
                  min="1"
                  max="10"
                  value={psiLevel}
                  onChange={(e) => setPsiLevel(e.target.value)}
                  required
                />
              </div>
            )}
            {role === UserRole.NAVIGATOR && (
              <div className="form-group">
                <label>Название дома:</label>
                <input
                  type="text"
                  value={houseName}
                  onChange={(e) => setHouseName(e.target.value)}
                  required
                />
              </div>
            )}
          </>
        )}
        <button type="submit" className="btn btn-primary" disabled={loading}>
          {loading ? 'Загрузка...' : (isLogin ? 'Войти' : 'Зарегистрироваться')}
        </button>
        <button
          type="button"
          className="btn btn-secondary"
          onClick={() => {
            setIsLogin(!isLogin);
            setMessage(null);
          }}
        >
          {isLogin ? 'Нет аккаунта? Зарегистрироваться' : 'Уже есть аккаунт? Войти'}
        </button>
      </form>
    </div>
  );
}

// Trader Dashboard Component
function TraderDashboard({ user }) {
  const [empireResources, setEmpireResources] = useState(null);
  const [planets, setPlanets] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [events, setEvents] = useState([]);
  const [selectedPlanet, setSelectedPlanet] = useState(null);
  const [upgrades, setUpgrades] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [showCreatePlanetForm, setShowCreatePlanetForm] = useState(false);
  const [newPlanet, setNewPlanet] = useState({
    name: '',
    planetType: 'AGRI_WORLD',
    loyalty: '50.0',
    wealth: '0',
    industry: '0',
    resources: '0'
  });

  useEffect(() => {
    loadData();
  }, [user.traderId]);

  const loadData = async () => {
    if (!user.traderId) return;
    setLoading(true);
    try {
      const [resources, planetsData, eventsData, upgradesData] = await Promise.all([
        api.getEmpireResources(user.traderId),
        api.getPlanets(user.traderId),
        api.getEvents(),
        api.getUpgrades(),
      ]);
      setEmpireResources(resources);
      setPlanets(planetsData);
      setEvents(eventsData);
      setUpgrades(upgradesData);
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка загрузки данных: ${error.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleAdvanceTime = async () => {
    if (!user.traderId) return;
    try {
      await api.advanceTimeCycle(user.traderId);
      setMessage({ type: 'success', text: 'Время продвинуто на один цикл' });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка: ${error.message}` });
    }
  };

  const handlePlanetClick = async (planet) => {
    setSelectedPlanet(planet);
    try {
      const planetProjects = await api.getProjects(planet.id);
      setProjects(planetProjects);
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка загрузки проектов: ${error.message}` });
    }
  };

  const handleCreateProject = async (planetId, upgradeId) => {
    try {
      await api.createProject(planetId, upgradeId);
      setMessage({ type: 'success', text: 'Проект создан' });
      if (selectedPlanet) {
        const planetProjects = await api.getProjects(selectedPlanet.id);
        setProjects(planetProjects);
      }
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка создания проекта: ${error.message}` });
    }
  };

  const handleResolveCrisis = async (eventId, action) => {
    try {
      await api.resolveCrisis(eventId, action);
      setMessage({ type: 'success', text: 'Кризис разрешен' });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка: ${error.message}` });
    }
  };

  const handleCreatePlanet = async (e) => {
    e.preventDefault();
    if (!user.traderId) return;
    try {
      await api.createPlanet(
        user.traderId,
        newPlanet.name,
        newPlanet.planetType,
        parseFloat(newPlanet.loyalty),
        parseFloat(newPlanet.wealth),
        parseFloat(newPlanet.industry),
        parseFloat(newPlanet.resources)
      );
      setMessage({ type: 'success', text: 'Планета создана успешно' });
      setShowCreatePlanetForm(false);
      setNewPlanet({
        name: '',
        planetType: 'AGRI_WORLD',
        loyalty: '50.0',
        wealth: '0',
        industry: '0',
        resources: '0'
      });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка создания планеты: ${error.message}` });
    }
  };

  if (loading) {
    return <div className="loading">Загрузка данных...</div>;
  }

  return (
    <div>
      {message && (
        <div className={`message message-${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="dashboard">
        {empireResources && (
          <div className="card">
            <h3>Ресурсы Империи</h3>
            <div className="card-content">
              <div className="stat-item">
                <span className="stat-label">Богатство:</span>
                <span className="stat-value">{empireResources.totalWealth.toFixed(2)}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Промышленность:</span>
                <span className="stat-value">{empireResources.totalIndustry.toFixed(2)}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Ресурсы:</span>
                <span className="stat-value">{empireResources.totalResources.toFixed(2)}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Планет:</span>
                <span className="stat-value">{empireResources.planetCount}</span>
              </div>
            </div>
          </div>
        )}

        <div className="card">
          <h3>Управление Временем</h3>
          <div className="card-content">
            <button className="btn btn-primary" onClick={handleAdvanceTime}>
              Пропустить Цикл Времени
            </button>
          </div>
        </div>
      </div>

      <div className="table-container">
        <h3>Карта Империи</h3>
        <EmpireMap planets={planets} routes={routes} onPlanetClick={handlePlanetClick} showDetails={true} />
      </div>

      <div className="table-container">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
          <h3 style={{ margin: 0 }}>Планеты</h3>
          <button 
            className="btn btn-primary" 
            onClick={() => setShowCreatePlanetForm(!showCreatePlanetForm)}
          >
            {showCreatePlanetForm ? 'Отмена' : '+ Добавить Планету'}
          </button>
        </div>
        
        {showCreatePlanetForm && (
          <div style={{ marginBottom: '20px', padding: '20px', border: '1px solid #444', borderRadius: '5px', background: 'rgba(0, 0, 0, 0.3)' }}>
            <h4 style={{ color: '#ffd700', marginBottom: '15px' }}>Создать Новую Планету</h4>
            <form onSubmit={handleCreatePlanet}>
              <div className="form-group">
                <label>Название планеты:</label>
                <input
                  type="text"
                  value={newPlanet.name}
                  onChange={(e) => setNewPlanet({ ...newPlanet, name: e.target.value })}
                  required
                  style={{ width: '100%' }}
                />
              </div>
              <div className="form-group">
                <label>Тип планеты:</label>
                <select
                  value={newPlanet.planetType}
                  onChange={(e) => setNewPlanet({ ...newPlanet, planetType: e.target.value })}
                  style={{
                    padding: '10px',
                    border: '1px solid #555',
                    borderRadius: '5px',
                    background: '#2d2d44',
                    color: '#e0e0e0',
                    width: '100%',
                  }}
                >
                  <option value="AGRI_WORLD">Аграрный Мир</option>
                  <option value="FORGE_WORLD">Кузнечный Мир</option>
                  <option value="MINING_WORLD">Горнодобывающий Мир</option>
                  <option value="CIVILIZED_WORLD">Цивилизованный Мир</option>
                  <option value="DEATH_WORLD">Мир Смерти</option>
                  <option value="HIVE_WORLD">Улей Мир</option>
                  <option value="FEUDAL_WORLD">Феодальный Мир</option>
                </select>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '10px' }}>
                <div className="form-group">
                  <label>Лояльность (0-100):</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    step="0.1"
                    value={newPlanet.loyalty}
                    onChange={(e) => setNewPlanet({ ...newPlanet, loyalty: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Богатство:</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={newPlanet.wealth}
                    onChange={(e) => setNewPlanet({ ...newPlanet, wealth: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Промышленность:</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={newPlanet.industry}
                    onChange={(e) => setNewPlanet({ ...newPlanet, industry: e.target.value })}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Ресурсы:</label>
                  <input
                    type="number"
                    min="0"
                    step="0.01"
                    value={newPlanet.resources}
                    onChange={(e) => setNewPlanet({ ...newPlanet, resources: e.target.value })}
                    required
                  />
                </div>
              </div>
              <button type="submit" className="btn btn-primary" style={{ marginTop: '10px' }}>
                Создать Планету
              </button>
            </form>
          </div>
        )}
        
        <table>
          <thead>
            <tr>
              <th>Название</th>
              <th>Тип</th>
              <th>Лояльность</th>
              <th>Богатство</th>
              <th>Промышленность</th>
              <th>Ресурсы</th>
              <th>Статус</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            {planets.map(planet => (
              <tr key={planet.id}>
                <td>{planet.name}</td>
                <td>{planet.planetType}</td>
                <td>{planet.loyalty.toFixed(1)}%</td>
                <td>{planet.wealth.toFixed(2)}</td>
                <td>{planet.industry.toFixed(2)}</td>
                <td>{planet.resources.toFixed(2)}</td>
                <td>
                  <span className={`status-badge ${planet.isRebellious ? 'status-rebellious' : 'status-loyal'}`}>
                    {planet.isRebellious ? 'Мятежная' : 'Лояльная'}
                  </span>
                </td>
                <td>
                  <button
                    className="btn btn-secondary"
                    onClick={() => handlePlanetClick(planet)}
                    style={{ padding: '6px 12px', fontSize: '12px' }}
                  >
                    Детали
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedPlanet && (
        <div className="table-container">
          <h3>Детали Планеты: {selectedPlanet.name}</h3>
          <div className="card-content">
            <h4>Доступные Улучшения:</h4>
            {upgrades
              .filter(u => u.suitableTypes === selectedPlanet.planetType)
              .map(upgrade => (
                <div key={upgrade.id} style={{ marginBottom: '10px', padding: '10px', border: '1px solid #444', borderRadius: '5px' }}>
                  <strong>{upgrade.name}</strong>
                  <p>{upgrade.description}</p>
                  <p>Стоимость: {upgrade.costWealth} богатства, {upgrade.costIndustry} промышленности, {upgrade.costResources} ресурсов</p>
                  <button
                    className="btn btn-primary"
                    onClick={() => handleCreateProject(selectedPlanet.id, upgrade.id)}
                    style={{ marginTop: '5px' }}
                  >
                    Создать Проект
                  </button>
                </div>
              ))}
          </div>
        </div>
      )}

      {events.filter(e => !e.resolved).length > 0 && (
        <div className="table-container">
          <h3>Кризисные События</h3>
          <table>
            <thead>
              <tr>
                <th>Планета</th>
                <th>Тип</th>
                <th>Серьезность</th>
                <th>Описание</th>
                <th>Действия</th>
              </tr>
            </thead>
            <tbody>
              {events.filter(e => !e.resolved).map(event => (
                <tr key={event.id}>
                  <td>{event.planet?.name || 'N/A'}</td>
                  <td>{event.eventType}</td>
                  <td>{event.severity}/10</td>
                  <td>{event.description}</td>
                  <td>
                    <button
                      className="btn btn-primary"
                      onClick={() => handleResolveCrisis(event.id, 'HELP')}
                      style={{ marginRight: '5px', padding: '6px 12px', fontSize: '12px' }}
                    >
                      Помочь
                    </button>
                    <button
                      className="btn btn-danger"
                      onClick={() => handleResolveCrisis(event.id, 'IGNORE')}
                      style={{ padding: '6px 12px', fontSize: '12px' }}
                    >
                      Игнорировать
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

// Governor Dashboard Component
function GovernorDashboard({ user }) {
  const [planet, setPlanet] = useState(null);
  const [planetStats, setPlanetStats] = useState(null);
  const [installedUpgrades, setInstalledUpgrades] = useState([]);
  const [availableUpgrades, setAvailableUpgrades] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    if (user.planetId) {
      loadPlanetData();
    }
  }, [user.planetId]);

  const loadPlanetData = async () => {
    if (!user.planetId) return;
    setLoading(true);
    try {
      const [planetData, stats, installed, projectsData] = await Promise.all([
        api.getPlanet(user.planetId),
        api.getPlanetStats(user.planetId),
        api.getInstalledUpgrades(user.planetId),
        api.getProjects(user.planetId),
      ]);
      setPlanet(planetData);
      setPlanetStats(stats);
      setInstalledUpgrades(installed);
      setProjects(projectsData);
      
      if (planetData) {
        const available = await api.getUpgradesByPlanetType(planetData.planetType);
        setAvailableUpgrades(available);
      }
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка загрузки данных: ${error.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleCreateProject = async (upgradeId) => {
    if (!user.planetId) return;
    try {
      await api.createProject(user.planetId, upgradeId);
      setMessage({ type: 'success', text: 'Проект создан' });
      loadPlanetData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка создания проекта: ${error.message}` });
    }
  };

  if (loading) {
    return <div className="loading">Загрузка данных...</div>;
  }

  if (!planet || !planetStats) {
    return <div className="loading">Планета не найдена</div>;
  }

  return (
    <div>
      {message && (
        <div className={`message message-${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="table-container">
        <h3>Моя Планета: {planet.name}</h3>
        <div className="card-content">
          <div className="stat-item">
            <span className="stat-label">Тип:</span>
            <span className="stat-value">{planet.planetType}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Лояльность:</span>
            <span className="stat-value">{planetStats.loyalty.toFixed(1)}%</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Богатство:</span>
            <span className="stat-value">{planetStats.wealth.toFixed(2)}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Промышленность:</span>
            <span className="stat-value">{planetStats.industry.toFixed(2)}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Ресурсы:</span>
            <span className="stat-value">{planetStats.resources.toFixed(2)}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Установленных улучшений:</span>
            <span className="stat-value">{planetStats.installedUpgradesCount}</span>
          </div>
          <div className="stat-item">
            <span className="stat-label">Статус:</span>
            <span className="stat-value">
              <span className={`status-badge ${planet.isRebellious ? 'status-rebellious' : 'status-loyal'}`}>
                {planet.isRebellious ? 'Мятежная' : 'Лояльная'}
              </span>
            </span>
          </div>
        </div>
      </div>

      {installedUpgrades.length > 0 && (
        <div className="table-container">
          <h3>Установленные Улучшения</h3>
          <ul style={{ listStyle: 'none', padding: 0 }}>
            {installedUpgrades.map(upgrade => (
              <li key={upgrade.upgradeId} style={{ padding: '10px', marginBottom: '10px', border: '1px solid #444', borderRadius: '5px' }}>
                <strong>{upgrade.upgradeName}</strong>
                {upgrade.upgradeDescription && (
                  <p style={{ color: '#aaa', marginTop: '5px' }}>{upgrade.upgradeDescription}</p>
                )}
              </li>
            ))}
          </ul>
        </div>
      )}

      <div className="table-container">
        <h3>Доступные Улучшения</h3>
        {availableUpgrades
          .filter(u => u.suitableTypes === planet.planetType)
          .map(upgrade => (
            <div key={upgrade.id} style={{ marginBottom: '15px', padding: '15px', border: '1px solid #444', borderRadius: '5px' }}>
              <strong>{upgrade.name}</strong>
              <p>{upgrade.description}</p>
              <p>Стоимость: {upgrade.costWealth} богатства, {upgrade.costIndustry} промышленности, {upgrade.costResources} ресурсов</p>
              <button
                className="btn btn-primary"
                onClick={() => handleCreateProject(upgrade.id)}
                style={{ marginTop: '10px' }}
              >
                Создать Проект Улучшения
              </button>
            </div>
          ))}
      </div>

      {projects.length > 0 && (
        <div className="table-container">
          <h3>Активные Проекты</h3>
          <table>
            <thead>
              <tr>
                <th>Улучшение</th>
                <th>Статус</th>
                <th>Дата начала</th>
              </tr>
            </thead>
            <tbody>
              {projects.map(project => (
                <tr key={project.id}>
                  <td>{project.upgrade?.name || 'N/A'}</td>
                  <td>
                    <span className={`status-badge status-${project.status.toLowerCase().replace('_', '-')}`}>
                      {project.status}
                    </span>
                  </td>
                  <td>{new Date(project.startDate).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

// Navigator Dashboard Component
function NavigatorDashboard({ user }) {
  const [planets, setPlanets] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [selectedFromPlanet, setSelectedFromPlanet] = useState(null);
  const [selectedToPlanet, setSelectedToPlanet] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    loadData();
  }, [user.navigatorId]);

  const loadData = async () => {
    if (!user.navigatorId) return;
    setLoading(true);
    try {
      const [routesData, allPlanets] = await Promise.all([
        api.getRoutes(user.navigatorId),
        api.getAllPlanets(),
      ]);
      setRoutes(routesData);
      setPlanets(allPlanets);
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка загрузки данных: ${error.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRoute = async () => {
    if (!user.navigatorId || !selectedFromPlanet || !selectedToPlanet) return;
    try {
      await api.createRoute(selectedFromPlanet, selectedToPlanet, user.navigatorId);
      setMessage({ type: 'success', text: 'Маршрут создан' });
      setSelectedFromPlanet(null);
      setSelectedToPlanet(null);
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка создания маршрута: ${error.message}` });
    }
  };

  if (loading) {
    return <div className="loading">Загрузка данных...</div>;
  }

  return (
    <div>
      {message && (
        <div className={`message message-${message.type}`}>
          {message.text}
        </div>
      )}

      <div className="table-container">
        <h3>Карта Империи - Маршруты</h3>
        <EmpireMap planets={planets} routes={routes} showDetails={false} />
      </div>

      <div className="table-container">
        <h3>Создать Новый Маршрут</h3>
        <div className="card-content">
          <div className="form-group">
            <label>От планеты:</label>
            <select
              value={selectedFromPlanet || ''}
              onChange={(e) => setSelectedFromPlanet(parseInt(e.target.value))}
              style={{
                padding: '10px',
                border: '1px solid #555',
                borderRadius: '5px',
                background: '#2d2d44',
                color: '#e0e0e0',
                width: '100%',
              }}
            >
              <option value="">Выберите планету</option>
              {planets.map(planet => (
                <option key={planet.id} value={planet.id}>
                  {planet.name}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>К планете:</label>
            <select
              value={selectedToPlanet || ''}
              onChange={(e) => setSelectedToPlanet(parseInt(e.target.value))}
              style={{
                padding: '10px',
                border: '1px solid #555',
                borderRadius: '5px',
                background: '#2d2d44',
                color: '#e0e0e0',
                width: '100%',
              }}
            >
              <option value="">Выберите планету</option>
              {planets
                .filter(p => p.id !== selectedFromPlanet)
                .map(planet => (
                  <option key={planet.id} value={planet.id}>
                    {planet.name}
                  </option>
                ))}
            </select>
          </div>
          <button
            className="btn btn-primary"
            onClick={handleCreateRoute}
            disabled={!selectedFromPlanet || !selectedToPlanet}
          >
            Создать Маршрут
          </button>
        </div>
      </div>

      <div className="table-container">
        <h3>Мои Маршруты</h3>
        <table>
          <thead>
            <tr>
              <th>От</th>
              <th>К</th>
              <th>Стабильность</th>
            </tr>
          </thead>
          <tbody>
            {routes.map(route => (
              <tr key={route.id}>
                <td>{route.fromPlanet?.name || route.fromPlanetId}</td>
                <td>{route.toPlanet?.name || route.toPlanetId}</td>
                <td>
                  <span className={`status-badge ${route.isStable ? 'status-loyal' : 'status-rebellious'}`}>
                    {route.isStable ? 'Стабильный' : 'Нестабильный'}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// Astropath Dashboard Component
function AstropathDashboard({ user }) {
  return (
    <div>
      <div className="table-container">
        <h3>Панель Астропата</h3>
        <div className="card-content">
          <div className="stat-item">
            <span className="stat-label">Уровень пси:</span>
            <span className="stat-value">{user.psiLevel || 'N/A'}</span>
          </div>
          <p style={{ marginTop: '20px', color: '#aaa' }}>
            Астропат может отправлять сообщения через варп с минимальными искажениями.
            Функционал отправки сообщений будет доступен в будущих обновлениях.
          </p>
        </div>
      </div>
    </div>
  );
}

// Main App Component
function App() {
  const [user, setUser] = useState(null);
  
  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    if (savedUser && token) {
      try {
        setUser(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      }
    }
  }, []);
  
  const handleLogin = (userData) => {
    setUser(userData);
  };
  
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };
  
  const renderDashboard = () => {
    if (!user) return null;

    switch (user.role) {
      case UserRole.TRADER:
        return <TraderDashboard user={user} />;
      case UserRole.GOVERNOR:
        return <GovernorDashboard user={user} />;
      case UserRole.NAVIGATOR:
        return <NavigatorDashboard user={user} />;
      case UserRole.ASTROPATH:
        return <AstropathDashboard user={user} />;
      default:
        return <div>Неизвестная роль</div>;
    }
  };
  
  return (
    <div className="container">
      <div className="header">
        <h1>🌟 Rogue Trader - Управление Империей 🌟</h1>
        {user && (
          <div className="user-info">
            <span>Пользователь: {user.email} ({user.role})</span>
            <button className="btn btn-danger logout-btn" onClick={handleLogout}>
              Выйти
            </button>
          </div>
        )}
      </div>
      
      {!user ? (
        <Auth onLogin={handleLogin} />
      ) : (
        renderDashboard()
      )}
    </div>
  );
}

// Render App
ReactDOM.render(<App />, document.getElementById('root'));
