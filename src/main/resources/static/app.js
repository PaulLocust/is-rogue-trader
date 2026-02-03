const { useState, useEffect } = React;

const API_BASE_URL = 'http://localhost:40000/api';

// User Roles
const UserRole = {
  TRADER: 'TRADER',
  GOVERNOR: 'GOVERNOR',
  ASTROPATH: 'ASTROPATH',
  NAVIGATOR: 'NAVIGATOR'
};

// Message Types
const MessageType = {
  NAVIGATION_REQUEST: 'NAVIGATION_REQUEST',
  UPGRADE_REQUEST: 'UPGRADE_REQUEST',
  CRISIS_RESPONSE: 'CRISIS_RESPONSE',
  RESOURCES_TRANSFER: 'RESOURCES_TRANSFER',    // Для отправки налогов/ресурсов
  STATUS_UPDATE: 'STATUS_UPDATE'               // Для отчетов
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

  // Auth
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

  // Users
  async getUsers() {
    return this.request('/users');
  },

  // Empire
  async getEmpireResources(traderId) {
    return this.request(`/empire/${traderId}/resources`);
  },

  // Planets
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

  // Messages
  async sendMessage(senderId, receiverId, content, messageType, commandId = null, resourcesWealth = 0, resourcesIndustry = 0, resourcesResources = 0, distortionChance = 0.1) {
    const messageTypeValue = typeof messageType === 'string' ? messageType : messageType;

    return this.request('/messages', {
      method: 'POST',
      body: JSON.stringify({
        senderId: parseInt(senderId),
        receiverId: parseInt(receiverId),
        content,
        messageType: messageTypeValue,
        commandId,
        resourcesWealth: resourcesWealth ? parseFloat(resourcesWealth) : 0,
        resourcesIndustry: resourcesIndustry ? parseFloat(resourcesIndustry) : 0,
        resourcesResources: resourcesResources ? parseFloat(resourcesResources) : 0,
        distortionChance: distortionChance ? parseFloat(distortionChance) : 0.1
      }),
    });
  },

  async getMessagesForUser(userId) {
    return this.request(`/messages/user/${userId}`);
  },

  async getMessageById(messageId) {
    return this.request(`/messages/${messageId}`);
  },

  async getPendingMessagesForAstropath(astropathId) {
    return this.request(`/messages/astropath/${astropathId}/pending`);
  },

  async getCommandsForReceiver(receiverId) {
    return this.request(`/messages/receiver/${receiverId}/commands`);
  },

  async markMessageDelivered(messageId) {
    return this.request(`/messages/${messageId}/deliver`, {
      method: 'PUT',
    });
  },

  async markCommandCompleted(messageId) {
    return this.request(`/messages/${messageId}/complete`, {
      method: 'PUT',
    });
  },

  // Events
  async getTraderEvents(traderId) {
    return this.request(`/events/trader/${traderId}`);
  },

  async resolveCrisis(eventId, action, resourcesWealth = 0, resourcesIndustry = 0) {
    return this.request(`/events/${eventId}/resolve`, {
      method: 'POST',
      body: JSON.stringify({ action, wealth: resourcesWealth, industry: resourcesIndustry }),
    });
  },

  // Projects
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

  // Upgrades
  async getUpgrades() {
    return this.request('/upgrades');
  },

  async getUpgradesByPlanetType(planetType) {
    return this.request(`/upgrades/planet-type/${planetType}`);
  },

  // Routes
  async getRoutes(navigatorId) {
    return this.request(`/routes/navigator/${navigatorId}`);
  },

  async getTraderRoutes(traderId) {
    return this.request(`/routes/trader/${traderId}`);
  },

  async createRoute(fromPlanetId, toPlanetId, navigatorId) {
    return this.request('/routes', {
      method: 'POST',
      body: JSON.stringify({ fromPlanetId, toPlanetId, navigatorId }),
    });
  },

  // Time
  async advanceTimeCycle(traderId) {
    return this.request(`/time/advance/${traderId}`, {
      method: 'POST',
    });
  },

  // Traders
  async getTrader(traderId) {
    return this.request(`/traders/${traderId}`);
  },

  // Astropath
  async astropathSendMessage(astropathId, receiverId, content, messageType, commandId, resourcesWealth, resourcesIndustry, resourcesResources, distortionChance) {
    return this.request(`/astropaths/${astropathId}/send`, {
      method: 'POST',
      body: JSON.stringify({
        receiverId,
        content,
        messageType,
        commandId,
        resourcesWealth,
        resourcesIndustry,
        resourcesResources,
        distortionChance
      }),
    });
  },

  async forwardCommand(astropathId, originalMessageId, finalReceiverId) {
    return this.request(`/astropaths/${astropathId}/forward-command?originalMessageId=${originalMessageId}&finalReceiverId=${finalReceiverId}`, {
      method: 'POST',
    });
  }
};

// Empire Map Component (исправленная версия)
function EmpireMap({ planets, routes, onPlanetClick, showDetails = true, interactive = false }) {
  const canvasRef = React.useRef(null);
  const [selectedPlanet, setSelectedPlanet] = React.useState(null);
  const [hoveredPlanet, setHoveredPlanet] = React.useState(null);

  const getPlanetColor = (planet) => {
    if (planet.isRebellious) return '#d32f2f';
    if (planet.loyalty < 30) return '#ff5252';
    if (planet.loyalty < 50) return '#ff9800';
    if (planet.planetType === 'AGRI_WORLD') return '#4caf50';
    if (planet.planetType === 'FORGE_WORLD') return '#f44336';
    if (planet.planetType === 'MINING_WORLD') return '#795548';
    if (planet.planetType === 'HIVE_WORLD') return '#9c27b0';
    if (planet.planetType === 'DEATH_WORLD') return '#607d8b';
    return '#2196f3';
  };

  const getPlanetPosition = (planetId) => {
    const canvas = canvasRef.current;
    if (!canvas || !planets || planets.length === 0) return null;

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = Math.min(canvas.width, canvas.height) / 3;
    const angleStep = (2 * Math.PI) / Math.max(planets.length, 1);

    const index = planets.findIndex(p => p.id === planetId);
    if (index === -1) return null;

    const angle = index * angleStep;
    return {
      x: centerX + radius * Math.cos(angle),
      y: centerY + radius * Math.sin(angle)
    };
  };

  const handleCanvasClick = (e) => {
    if (!interactive || !planets) return;

    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const clickedPlanet = planets.find(planet => {
      const pos = getPlanetPosition(planet.id);
      if (!pos) return false;

      const distance = Math.sqrt(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y, 2));
      return distance <= 20;
    });

    if (clickedPlanet) {
      setSelectedPlanet(clickedPlanet);
      if (onPlanetClick) onPlanetClick(clickedPlanet);
    }
  };

  const handleMouseMove = (e) => {
    if (!interactive || !planets) return;

    const canvas = canvasRef.current;
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    const hovered = planets.find(planet => {
      const pos = getPlanetPosition(planet.id);
      if (!pos) return false;

      const distance = Math.sqrt(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y, 2));
      return distance <= 15;
    });

    setHoveredPlanet(hovered);
    if (canvas) {
      canvas.style.cursor = hovered ? 'pointer' : 'default';
    }
  };

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || !planets) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    canvas.width = canvas.clientWidth;
    canvas.height = canvas.clientHeight;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = Math.min(canvas.width, canvas.height) / 3;
    const angleStep = (2 * Math.PI) / Math.max(planets.length, 1);

    const planetPositions = new Map();

    planets.forEach((planet, index) => {
      const angle = index * angleStep;
      const x = centerX + radius * Math.cos(angle);
      const y = centerY + radius * Math.sin(angle);
      planetPositions.set(planet.id, { x, y });
    });

    if (routes && routes.length > 0) {
      routes.forEach(route => {
        const from = planetPositions.get(route.fromPlanet?.id || route.fromPlanetId);
        const to = planetPositions.get(route.toPlanet?.id || route.toPlanetId);

        if (from && to) {
          if (!route.isStable) {
            ctx.strokeStyle = '#ff6b6b';
            ctx.setLineDash([5, 5]);
          } else {
            ctx.strokeStyle = '#666';
            ctx.setLineDash([]);
          }

          ctx.lineWidth = route.isStable ? 2 : 1;
          ctx.beginPath();
          ctx.moveTo(from.x, from.y);
          ctx.lineTo(to.x, to.y);
          ctx.stroke();
          ctx.setLineDash([]);
        }
      });
    }

    planets.forEach((planet) => {
      const pos = planetPositions.get(planet.id);
      if (!pos) return;

      const { x, y } = pos;

      ctx.fillStyle = getPlanetColor(planet);
      ctx.beginPath();
      ctx.arc(x, y, 15, 0, 2 * Math.PI);
      ctx.fill();

      ctx.strokeStyle = '#444';
      ctx.lineWidth = 1;

      if (selectedPlanet && selectedPlanet.id === planet.id) {
        ctx.strokeStyle = '#ffd700';
        ctx.lineWidth = 3;
      } else if (hoveredPlanet && hoveredPlanet.id === planet.id) {
        ctx.strokeStyle = '#4fc3f7';
        ctx.lineWidth = 2;
      }

      ctx.stroke();

      const icon = getPlanetIcon(planet.planetType);
      ctx.fillStyle = '#fff';
      ctx.font = '12px Arial';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(icon, x, y);

      if (showDetails) {
        ctx.fillStyle = '#e0e0e0';
        ctx.font = 'bold 12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(planet.name, x, y - 30);

        ctx.fillStyle = planet.loyalty < 30 ? '#ff5252' : planet.loyalty < 50 ? '#ff9800' : '#4caf50';
        ctx.font = '10px Arial';
        ctx.fillText(`${planet.loyalty ? planet.loyalty.toFixed(0) : 0}%`, x, y + 25);
      }
    });
  }, [planets, routes, selectedPlanet, hoveredPlanet, showDetails]);

  const getPlanetIcon = (type) => {
    switch(type) {
      case 'AGRI_WORLD': return '🌾';
      case 'FORGE_WORLD': return '⚒️';
      case 'MINING_WORLD': return '⛏️';
      case 'HIVE_WORLD': return '🏙️';
      case 'DEATH_WORLD': return '☠️';
      case 'CIVILIZED_WORLD': return '🏛️';
      case 'FEUDAL_WORLD': return '⚔️';
      default: return '🪐';
    }
  };

  return (
      <div style={{ textAlign: 'center', margin: '20px 0', position: 'relative' }}>
        <canvas
            ref={canvasRef}
            onClick={handleCanvasClick}
            onMouseMove={handleMouseMove}
            style={{
              width: '100%',
              height: '600px',
              border: '2px solid #444',
              borderRadius: '10px',
              background: '#1e1e2e',
              cursor: interactive ? 'pointer' : 'default',
            }}
        />

        {selectedPlanet && interactive && (
            <div style={{
              position: 'absolute',
              top: '20px',
              right: '20px',
              background: 'rgba(0, 0, 0, 0.8)',
              padding: '15px',
              borderRadius: '10px',
              border: '1px solid #444',
              maxWidth: '300px',
              zIndex: 100
            }}>
              <h4 style={{ color: '#ffd700', marginBottom: '10px' }}>
                {selectedPlanet.name}
              </h4>
              <p><strong>Тип:</strong> {getPlanetTypeDisplay(selectedPlanet.planetType)}</p>
              <p><strong>Лояльность:</strong> {selectedPlanet.loyalty ? selectedPlanet.loyalty.toFixed(1) : '0.0'}%</p>
              <p><strong>Ресурсы:</strong>
                💰{selectedPlanet.wealth ? selectedPlanet.wealth.toFixed(0) : 0}
                ⚙️{selectedPlanet.industry ? selectedPlanet.industry.toFixed(0) : 0}
                ⛏️{selectedPlanet.resources ? selectedPlanet.resources.toFixed(0) : 0}
              </p>
              <p>
                <strong>Статус:</strong>
                <span className={`status-badge ${selectedPlanet.isRebellious ? 'status-rebellious' : 'status-loyal'}`}>
              {selectedPlanet.isRebellious ? 'Мятежная' : 'Лояльная'}
            </span>
              </p>
            </div>
        )}

        <div style={{
          display: 'flex',
          justifyContent: 'center',
          flexWrap: 'wrap',
          gap: '15px',
          marginTop: '15px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
            <div style={{ width: '15px', height: '15px', background: '#4caf50', borderRadius: '50%' }}></div>
            <span>Лояльная планета</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
            <div style={{ width: '15px', height: '15px', background: '#ff9800', borderRadius: '50%' }}></div>
            <span>Сомнительная</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
            <div style={{ width: '15px', height: '15px', background: '#d32f2f', borderRadius: '50%' }}></div>
            <span>Мятежная</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
            <div style={{ width: '2px', height: '15px', background: '#666' }}></div>
            <span>Стабильный маршрут</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
            <div style={{ width: '2px', height: '15px', background: '#ff6b6b', border: '1px dashed #ff6b6b' }}></div>
            <span>Нестабильный маршрут</span>
          </div>
        </div>
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

// Trader Dashboard Component с добавленной кнопкой создания планет
function TraderDashboard({ user }) {
  const [empireResources, setEmpireResources] = useState(null);
  const [planets, setPlanets] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [events, setEvents] = useState([]);
  const [selectedPlanet, setSelectedPlanet] = useState(null);
  const [upgrades, setUpgrades] = useState([]);
  const [users, setUsers] = useState([]);
  const [commands, setCommands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [showCreatePlanetModal, setShowCreatePlanetModal] = useState(false);
  const [newPlanet, setNewPlanet] = useState({
    name: '',
    planetType: 'AGRI_WORLD',
    loyalty: '50.0',
    wealth: '0',
    industry: '0',
    resources: '0'
  });

  // Модальные окна
  const [showCreateCommandModal, setShowCreateCommandModal] = useState(false);
  const [showCrisisModal, setShowCrisisModal] = useState(false);
  const [showRouteModal, setShowRouteModal] = useState(false);

  // Данные для модалок
  const [newCommand, setNewCommand] = useState({
    type: 'UPGRADE_REQUEST',
    receiverId: '',
    planetId: '',
    upgradeId: '',
    astropathId: '',
    resourcesWealth: 0,
    resourcesIndustry: 0,
    resourcesResources: 0,
    content: ''
  });

  const [selectedEvent, setSelectedEvent] = useState(null);
  const [crisisResources, setCrisisResources] = useState({ wealth: 1000, industry: 500 });

  const [routeData, setRouteData] = useState({
    fromPlanetId: '',
    toPlanetId: '',
    navigatorId: '',
    astropathId: ''
  });

  useEffect(() => {
    if (user.traderId) {
      loadData();
    }
  }, [user.traderId]);

  const loadData = async () => {
    if (!user.traderId) return;
    setLoading(true);
    try {
      const [resources, planetsData, eventsData, upgradesData, usersData, messages, routesData] = await Promise.all([
        api.getEmpireResources(user.traderId),
        api.getPlanets(user.traderId),
        api.getTraderEvents(user.traderId),
        api.getUpgrades(),
        api.getUsers(),
        api.getMessagesForUser(user.id || user.userId || user.traderId),
        api.getTraderRoutes(user.traderId)
      ]);
      setEmpireResources(resources);
      setPlanets(planetsData);
      setEvents(eventsData);
      setUpgrades(upgradesData);
      setUsers(usersData);
      setRoutes(routesData);

      // Фильтруем команды, созданные торговцем
      const traderCommands = messages.filter(msg =>
          msg.sender && (msg.sender.id === user.id || msg.sender.id === user.userId)
      );
      setCommands(traderCommands);
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка загрузки данных: ${error.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleAdvanceTime = async () => {
    if (!user.traderId) return;

    // Проверяем, есть ли невыполненные команды
    const pendingCommands = commands.filter(cmd => !cmd.completed);
    if (pendingCommands.length > 0) {
      setMessage({
        type: 'error',
        text: 'Нельзя пропустить время: есть невыполненные команды!'
      });
      return;
    }

    try {
      await api.advanceTimeCycle(user.traderId);
      setMessage({ type: 'success', text: 'Время продвинуто на один цикл' });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка: ${error.message}` });
    }
  };

  const handlePlanetClick = (planet) => {
    setSelectedPlanet(planet);
  };

  const handleCreatePlanet = async () => {
    try {
      if (!newPlanet.name.trim()) {
        setMessage({ type: 'error', text: 'Введите название планеты' });
        return;
      }

      await api.createPlanet(
          user.traderId,
          newPlanet.name,
          newPlanet.planetType,
          parseFloat(newPlanet.loyalty),
          parseFloat(newPlanet.wealth),
          parseFloat(newPlanet.industry),
          parseFloat(newPlanet.resources)
      );

      setMessage({ type: 'success', text: 'Планета успешно создана!' });
      setShowCreatePlanetModal(false);
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

  const handleCreateUpgradeCommand = async () => {
    // Проверяем ВСЕ необходимые поля, включая астропата
    if (!newCommand.planetId || !newCommand.upgradeId || !newCommand.astropathId) {
      setMessage({ type: 'error', text: 'Заполните все поля, включая выбор астропата!' });
      return;
    }

    try {
      const upgrade = upgrades.find(u => u.id == newCommand.upgradeId);
      const planet = planets.find(p => p.id == newCommand.planetId);

      const content = `Постройка улучшения "${upgrade.name}" на планете ${planet.name}. ` +
          `Стоимость: 💰${upgrade.costWealth} ⚙️${upgrade.costIndustry} ⛏️${upgrade.costResources}`;

      // Отправляем команду ВЫБРАННОМУ астропату
      await api.sendMessage(
          user.id || user.userId,
          newCommand.astropathId,  // <-- ВАЖНО: используем выбранного астропата
          content,
          MessageType.UPGRADE_REQUEST,
          newCommand.planetId,  // commandId = planetId
          upgrade.costWealth,
          upgrade.costIndustry,
          upgrade.costResources,
          0.1
      );

      setMessage({ type: 'success', text: 'Команда на улучшение отправлена астропату' });
      setShowCreateCommandModal(false);
      // Сброс формы
      setNewCommand({
        type: 'UPGRADE_REQUEST',
        receiverId: '',
        planetId: '',
        upgradeId: '',
        astropathId: '',  // <-- сброс ID астропата
        resourcesWealth: 0,
        resourcesIndustry: 0,
        resourcesResources: 0,
        content: ''
      });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка создания команды: ${error.message}` });
    }
  };

  // В состоянии TraderDashboard добавьте:
  const [crisisAstropathId, setCrisisAstropathId] = useState('');

// Обновите handleResolveCrisis:
  const handleResolveCrisis = async (action) => {
    if (!selectedEvent || !crisisAstropathId) {  // <-- Проверяем выбран ли астропат
      setMessage({ type: 'error', text: 'Выберите астропата!' });
      return;
    }

    try {
      const senderId = user.id || user.userId;
      if (!senderId) {
        throw new Error('ID пользователя не найден');
      }

      // Отправляем команду астропату
      const content = action === 'HELP'
          ? `Разрешение кризиса на планете ${selectedEvent.planet?.name}. Выделено ресурсов: 💰${crisisResources.wealth} ⚙️${crisisResources.industry}`
          : `Игнорирование кризиса на планете ${selectedEvent.planet?.name}`;

      await api.sendMessage(
          senderId,
          crisisAstropathId,  // <-- Используем выбранного астропата
          content,
          MessageType.CRISIS_RESPONSE,
          selectedEvent.id,
          action === 'HELP' ? crisisResources.wealth : 0,
          action === 'HELP' ? crisisResources.industry : 0,
          0,
          0.15
      );

      setMessage({
        type: 'success',
        text: `Команда на ${action === 'HELP' ? 'помощь' : 'игнорирование'} отправлена астропату`
      });
      setShowCrisisModal(false);
      setSelectedEvent(null);
      setCrisisAstropathId('');  // <-- Сброс ID астропата
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка: ${error.message}` });
    }
  };

  const handleCreateRouteCommand = async () => {
    // Проверяем ВСЕ поля включая астропата
    if (!routeData.fromPlanetId || !routeData.toPlanetId ||
        !routeData.navigatorId || !routeData.astropathId) {
      setMessage({ type: 'error', text: 'Выберите планеты, навигатора и астропата!' });
      return;
    }

    try {
      const fromPlanet = planets.find(p => p.id == routeData.fromPlanetId);
      const toPlanet = planets.find(p => p.id == routeData.toPlanetId);
      const senderId = user.id || user.userId;

      if (!senderId) {
        throw new Error('ID пользователя не найден');
      }

      // Включаем ID планет в текст, чтобы навигатор мог распознать команду
      const content =
          `Прокладка варп-маршрута от планеты ${fromPlanet.name} (ID: ${fromPlanet.id}) ` +
          `к планете ${toPlanet.name} (ID: ${toPlanet.id}) для навигатора ID: ${routeData.navigatorId}`;

      // Отправляем команду ВЫБРАННОМУ астропату
      await api.sendMessage(
          senderId,
          routeData.astropathId,  // <-- ВАЖНО: используем выбранного астропата
          content,
          MessageType.NAVIGATION_REQUEST,
          routeData.navigatorId,  // commandId = navigatorId
          0, 0, 0, 0.1
      );

      setMessage({ type: 'success', text: 'Команда на прокладку маршрута отправлена астропату' });
      setShowRouteModal(false);
      setRouteData({
        fromPlanetId: '',
        toPlanetId: '',
        navigatorId: '',
        astropathId: ''
      });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка: ${error.message}` });
    }
  };

  const getCompletedCommandsCount = () => {
    return commands.filter(cmd => cmd.completed).length;
  };

  const getPendingCommandsCount = () => {
    return commands.filter(cmd => !cmd.completed).length;
  };

  if (loading) {
    return <div className="loading">Загрузка данных империи...</div>;
  }

  return (
      <div>
        {message && (
            <div className={`message message-${message.type}`}>
              {message.text}
            </div>
        )}

        {/* Модальное окно создания планеты */}
        {showCreatePlanetModal && (
            <div className="modal-overlay">
              <div className="modal-content">
                <h3>🌍 Создать новую планету</h3>

                <div className="form-group">
                  <label>Название планеты:</label>
                  <input
                      type="text"
                      value={newPlanet.name}
                      onChange={(e) => setNewPlanet({...newPlanet, name: e.target.value})}
                      placeholder="Введите название планеты"
                      required
                  />
                </div>

                <div className="form-group">
                  <label>Тип планеты:</label>
                  <select
                      value={newPlanet.planetType}
                      onChange={(e) => setNewPlanet({...newPlanet, planetType: e.target.value})}
                  >
                    <option value="AGRI_WORLD">🌾 Аграрный Мир</option>
                    <option value="FORGE_WORLD">⚒️ Кузнечный Мир</option>
                    <option value="MINING_WORLD">⛏️ Горнодобывающий Мир</option>
                    <option value="CIVILIZED_WORLD">🏛️ Цивилизованный Мир</option>
                    <option value="DEATH_WORLD">☠️ Мир Смерти</option>
                    <option value="HIVE_WORLD">🏙️ Улей Мир</option>
                    <option value="FEUDAL_WORLD">⚔️ Феодальный Мир</option>
                  </select>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                  <div className="form-group">
                    <label>Лояльность (0-100):</label>
                    <input
                        type="number"
                        min="0"
                        max="100"
                        step="0.1"
                        value={newPlanet.loyalty}
                        onChange={(e) => setNewPlanet({...newPlanet, loyalty: e.target.value})}
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
                        onChange={(e) => setNewPlanet({...newPlanet, wealth: e.target.value})}
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
                        onChange={(e) => setNewPlanet({...newPlanet, industry: e.target.value})}
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
                        onChange={(e) => setNewPlanet({...newPlanet, resources: e.target.value})}
                        required
                    />
                  </div>
                </div>

                <div className="modal-actions">
                  <button className="btn btn-primary" onClick={handleCreatePlanet}>
                    🚀 Создать Планету
                  </button>
                  <button className="btn btn-secondary" onClick={() => setShowCreatePlanetModal(false)}>
                    Отмена
                  </button>
                </div>
              </div>
            </div>
        )}

        {/* Модальное окно создания команды */}
        {showCreateCommandModal && (
            <div className="modal-overlay">
              <div className="modal-content">
                <h3>📋 Создать новую команду</h3>
                <div className="form-group">
                  <label>Тип команды:</label>
                  <select
                      value={newCommand.type}
                      onChange={(e) => setNewCommand({ ...newCommand, type: e.target.value })}
                  >
                    <option value="UPGRADE_REQUEST">🏗️ Постройка улучшения</option>
                  </select>
                </div>

                {newCommand.type === 'UPGRADE_REQUEST' && (
                    <>
                      <div className="form-group">
                        <label>Планета:</label>
                        <select
                            value={newCommand.planetId}
                            onChange={(e) => {
                              const planetId = e.target.value;
                              setNewCommand({...newCommand, planetId});
                            }}
                        >
                          <option value="">Выберите планету</option>
                          {planets.map(p => (
                              <option key={p.id} value={p.id}>{p.name}</option>
                          ))}
                        </select>
                      </div>

                      {newCommand.planetId && (
                          <div className="form-group">
                            <label>Улучшение:</label>
                            <select
                                value={newCommand.upgradeId}
                                onChange={(e) => setNewCommand({...newCommand, upgradeId: e.target.value})}
                            >
                              <option value="">Выберите улучшение</option>
                              {upgrades
                                  .filter(u => u.suitableTypes === planets.find(p => p.id == newCommand.planetId)?.planetType)
                                  .map(u => (
                                      <option key={u.id}
                                              value={u.id}>{u.name} (💰{u.costWealth} ⚙️{u.costIndustry} ⛏️{u.costResources})</option>
                                  ))}
                            </select>
                          </div>
                      )}

                      <div className="form-group">
                        <label>Астропат для отправки:</label>
                        <select
                            value={newCommand.astropathId}
                            onChange={(e) => setNewCommand({...newCommand, astropathId: e.target.value})}
                            required
                        >
                          <option value="">Выберите астропата</option>
                          {users
                              .filter(u => u.role === UserRole.ASTROPATH)
                              .map(u => (
                                  <option key={u.id} value={u.id}>
                                    {u.email} (Уровень пси: {u.psiLevel || 5})
                                  </option>
                              ))}
                        </select>
                      </div>
                    </>
                )}

                <div className="modal-actions">
                  <button className="btn btn-primary" onClick={handleCreateUpgradeCommand}>
                    📨 Отправить астропату
                  </button>
                  <button className="btn btn-secondary" onClick={() => setShowCreateCommandModal(false)}>
                    Отмена
                  </button>
                </div>
              </div>
            </div>
        )}

        {/* Модальное окно кризиса */}
        {showCrisisModal && selectedEvent && (
            <div className="modal-overlay">
              <div className="modal-content">
                <h3>🚨 Кризисная ситуация</h3>
                <p><strong>Планета:</strong> {selectedEvent.planet?.name}</p>
                <p><strong>Тип:</strong> {getEventTypeDisplay(selectedEvent.eventType)}</p>
                <p><strong>Серьезность:</strong> {selectedEvent.severity}/10</p>
                <p><strong>Описание:</strong> {selectedEvent.description}</p>

                <div className="form-group">
                  <label>Ресурсы для помощи (богатство):</label>
                  <input
                      type="number"
                      value={crisisResources.wealth}
                      onChange={(e) => setCrisisResources({...crisisResources, wealth: parseInt(e.target.value) || 0})}
                      min="0"
                  />
                </div>
                <div className="form-group">
                  <label>Ресурсы для помощи (промышленность):</label>
                  <input
                      type="number"
                      value={crisisResources.industry}
                      onChange={(e) => setCrisisResources({
                        ...crisisResources,
                        industry: parseInt(e.target.value) || 0
                      })}
                      min="0"
                  />
                </div>

                <div className="modal-actions">
                  <button className="btn btn-primary" onClick={() => handleResolveCrisis('HELP')}>
                    Помочь (выделить ресурсы)
                  </button>
                  <button className="btn btn-danger" onClick={() => handleResolveCrisis('IGNORE')}>
                    Игнорировать
                  </button>
                  <button className="btn btn-secondary" onClick={() => {
                    setShowCrisisModal(false);
                    setSelectedEvent(null);
                  }}>
                    Отмена
                  </button>
                </div>

                <div className="form-group">
                  <label>Астропат для отправки:</label>
                  <select
                      value={crisisAstropathId}
                      onChange={(e) => setCrisisAstropathId(e.target.value)}
                      required
                  >
                    <option value="">Выберите астропата</option>
                    {users
                        .filter(u => u.role === UserRole.ASTROPATH)
                        .map(u => (
                            <option key={u.id} value={u.id}>
                              {u.email} (Уровень пси: {u.psiLevel || 5})
                            </option>
                        ))}
                  </select>
                </div>
              </div>
            </div>
        )}

        {/* Модальное окно маршрута */}
        {showRouteModal && (
            <div className="modal-overlay">
              <div className="modal-content">
                <h3>🛤️ Создать маршрут</h3>

                <div className="form-group">
                  <label>От планеты:</label>
                  <select
                      value={routeData.fromPlanetId}
                      onChange={(e) => setRouteData({...routeData, fromPlanetId: e.target.value})}
                  >
                    <option value="">Выберите планету</option>
                    {planets.map(p => (
                        <option key={p.id} value={p.id}>{p.name}</option>
                    ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>К планете:</label>
                  <select
                      value={routeData.toPlanetId}
                      onChange={(e) => setRouteData({...routeData, toPlanetId: e.target.value})}
                  >
                    <option value="">Выберите планету</option>
                    {planets
                        .filter(p => p.id != routeData.fromPlanetId)
                        .map(p => (
                            <option key={p.id} value={p.id}>{p.name}</option>
                        ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Навигатор:</label>
                  <select
                      value={routeData.navigatorId}
                      onChange={(e) => setRouteData({...routeData, navigatorId: e.target.value})}
                  >
                    <option value="">Выберите навигатора</option>
                    {users
                        .filter(u => u.role === UserRole.NAVIGATOR)
                        .map(u => (
                            <option key={u.id} value={u.id}>{u.email}</option>
                        ))}
                  </select>
                </div>

                <div className="form-group">
                  <label>Астропат для отправки:</label>
                  <select
                      value={routeData.astropathId}
                      onChange={(e) => setRouteData({...routeData, astropathId: e.target.value})}
                      required
                  >
                    <option value="">Выберите астропата</option>
                    {users
                        .filter(u => u.role === UserRole.ASTROPATH)
                        .map(u => (
                            <option key={u.id} value={u.id}>
                              {u.email} (Уровень пси: {u.psiLevel || 5})
                            </option>
                        ))}
                  </select>
                </div>

                <div className="modal-actions">
                  <button className="btn btn-primary" onClick={handleCreateRouteCommand}>
                    📨 Отправить команду
                  </button>
                  <button className="btn btn-secondary" onClick={() => setShowRouteModal(false)}>
                    Отмена
                  </button>
                </div>
              </div>
            </div>
        )}

        <div className="dashboard">
          {empireResources && (
              <div className="card">
                <h3>🏰 Ресурсы Империи</h3>
                <div className="card-content">
                  <div className="stat-item">
                    <span className="stat-label">Богатство:</span>
                    <span
                        className="stat-value">💰{empireResources.totalWealth ? empireResources.totalWealth.toFixed(2) : '0'}</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-label">Промышленность:</span>
                    <span className="stat-value">⚙️{empireResources.totalIndustry ? empireResources.totalIndustry.toFixed(2) : '0'}</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-label">Ресурсы:</span>
                    <span className="stat-value">⛏️{empireResources.totalResources ? empireResources.totalResources.toFixed(2) : '0'}</span>
                  </div>
                  <div className="stat-item">
                    <span className="stat-label">Планет:</span>
                    <span className="stat-value">🪐{empireResources.planetCount || 0}</span>
                  </div>
                </div>
              </div>
          )}

          <div className="card">
            <h3>⏱️ Управление Временем</h3>
            <div className="card-content">
              <button
                  className="btn btn-primary"
                  onClick={handleAdvanceTime}
                  disabled={getPendingCommandsCount() > 0}
              >
                ⏩ Пропустить Цикл Времени
                {getPendingCommandsCount() > 0 && ` (${getPendingCommandsCount()} команд в работе)`}
              </button>
              <p style={{ marginTop: '10px', color: '#aaa', fontSize: '12px' }}>
                {getPendingCommandsCount() > 0
                    ? 'Завершите все команды перед пропуском времени'
                    : 'При пропуске времени: собираются налоги, строятся проекты, генерируются события'}
              </p>
            </div>
          </div>

          <div className="card">
            <h3>📋 Мои Команды</h3>
            <div className="card-content">
              <div className="stat-item">
                <span className="stat-label">Всего команд:</span>
                <span className="stat-value">{commands.length}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Выполнено:</span>
                <span className="stat-value" style={{ color: '#4caf50' }}>{getCompletedCommandsCount()}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">В работе:</span>
                <span className="stat-value" style={{ color: '#ff9800' }}>{getPendingCommandsCount()}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="dashboard" style={{ marginTop: '20px' }}>
          <button className="btn btn-primary" onClick={() => setShowCreatePlanetModal(true)}>
            🌍 Создать Планету
          </button>
          <button className="btn btn-primary" onClick={() => setShowCreateCommandModal(true)}>
            🏗️ Создать улучшение
          </button>
          <button className="btn btn-primary" onClick={() => setShowRouteModal(true)}>
            🛤️ Прокладка маршрута
          </button>
        </div>

        <div className="table-container">
          <h3>🗺️ Карта Империи</h3>
          <EmpireMap
              planets={planets}
              routes={routes}
              onPlanetClick={handlePlanetClick}
              showDetails={true}
              interactive={true}
          />
        </div>

        <div className="table-container">
          <h3>🪐 Планеты Империи</h3>
          <table>
            <thead>
            <tr>
              <th>Название</th>
              <th>Тип</th>
              <th>Лояльность</th>
              <th>Ресурсы</th>
              <th>Статус</th>
              <th>Действия</th>
            </tr>
            </thead>
            <tbody>
            {planets.map(planet => (
                <tr key={planet.id}>
                  <td>
                    <strong>{planet.name}</strong>
                    {planet.isRebellious && <span style={{ color: '#ff6b6b', marginLeft: '5px' }}>🔥</span>}
                  </td>
                  <td>{getPlanetTypeDisplay(planet.planetType)}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                      <div style={{
                        width: '60px',
                        height: '8px',
                        background: planet.loyalty < 30 ? '#d32f2f' : planet.loyalty < 50 ? '#ff9800' : '#4caf50',
                        borderRadius: '4px',
                        overflow: 'hidden'
                      }}>
                        <div style={{
                          width: `${planet.loyalty}%`,
                          height: '100%',
                          background: planet.loyalty < 30 ? '#ff6b6b' : planet.loyalty < 50 ? '#ffb74d' : '#81c784'
                        }}></div>
                      </div>
                      <span>{planet.loyalty ? planet.loyalty.toFixed(1) : '0'}%</span>
                    </div>
                  </td>
                  <td>
                    💰{planet.wealth ? planet.wealth.toFixed(0) : 0}
                    ⚙️{planet.industry ? planet.industry.toFixed(0) : 0}
                    ⛏️{planet.resources ? planet.resources.toFixed(0) : 0}
                  </td>
                  <td>
                  <span className={`status-badge ${planet.isRebellious ? 'status-rebellious' : 'status-loyal'}`}>
                    {planet.isRebellious ? '🔥 Мятежная' : '🤝 Лояльная'}
                  </span>
                  </td>
                  <td>
                    <button
                        className="btn btn-secondary"
                        onClick={() => {
                          setSelectedPlanet(planet);
                          setNewCommand({...newCommand, planetId: planet.id});
                          setShowCreateCommandModal(true);
                        }}
                        style={{ padding: '6px 12px', fontSize: '12px', marginRight: '5px' }}
                    >
                      🏗️ Улучшить
                    </button>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>

        {events.filter(e => !e.resolved).length > 0 && (
            <div className="table-container">
              <h3>🚨 Кризисные События</h3>
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
                      <td>{event.planet?.name || 'Неизвестная планета'}</td>
                      <td>
                    <span className={`event-type event-${event.eventType ? event.eventType.toLowerCase() : ''}`}>
                      {getEventTypeDisplay(event.eventType)}
                    </span>
                      </td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                          <div style={{
                            width: '60px',
                            height: '8px',
                            background: '#333',
                            borderRadius: '4px',
                            overflow: 'hidden'
                          }}>
                            <div style={{
                              width: `${(event.severity || 5) * 10}%`,
                              height: '100%',
                              background: (event.severity || 5) > 7 ? '#d32f2f' : (event.severity || 5) > 4 ? '#ff9800' : '#4caf50'
                            }}></div>
                          </div>
                          <span>{(event.severity || 5)}/10</span>
                        </div>
                      </td>
                      <td>{event.description}</td>
                      <td>
                        <button
                            className="btn btn-danger"
                            onClick={() => {
                              setSelectedEvent(event);
                              setShowCrisisModal(true);
                            }}
                            style={{ padding: '6px 12px', fontSize: '12px' }}
                        >
                          🚨 Решить
                        </button>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
        )}

        {commands.length > 0 && (
            <div className="table-container">
              <h3>📋 История Команд</h3>
              <table>
                <thead>
                <tr>
                  <th>Тип</th>
                  <th>Содержание</th>
                  <th>Получатель</th>
                  <th>Статус</th>
                  <th>Дата</th>
                </tr>
                </thead>
                <tbody>
                {commands.map(cmd => (
                    <tr key={cmd.id}>
                      <td>{getMessageTypeDisplay(cmd.messageType)}</td>
                      <td>{cmd.content}</td>
                      <td>{cmd.receiver?.email || 'Астропат'}</td>
                      <td>
                    <span className={`status-badge ${cmd.completed ? 'status-completed' : 'status-in-progress'}`}>
                      {cmd.completed ? '✅ Выполнена' : '⏳ В работе'}
                    </span>
                        {cmd.distorted && (
                            <span className="status-badge status-rebellious" style={{ marginLeft: '5px' }}>
                        Искажена
                      </span>
                        )}
                      </td>
                      <td>{new Date(cmd.sentAt).toLocaleString()}</td>
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
  const [commands, setCommands] = useState([]);
  const [upgrades, setUpgrades] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  useEffect(() => {
    if (user.id && user.planetId) {
      loadData();
    }
  }, [user.id, user.planetId]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [planetData, commandsData, upgradesData] = await Promise.all([
        api.getPlanet(user.planetId),
        api.getCommandsForReceiver(user.id || user.userId),
        api.getUpgrades()
      ]);
      setPlanet(planetData);
      setCommands(commandsData);
      setUpgrades(upgradesData);
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка загрузки данных: ${error.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleExecuteCommand = async (messageId, commandType) => {
    try {
      // Помечаем команду как выполненную
      await api.markCommandCompleted(messageId);

      if (commandType === 'UPGRADE_REQUEST') {
        setMessage({ type: 'success', text: 'Улучшение построено' });
      } else if (commandType === 'CRISIS_RESPONSE') {
        setMessage({ type: 'success', text: 'Кризис решен' });
      }

      // Отправляем отчет астропату
      const astropath = commands.find(cmd => cmd.sender?.role === UserRole.ASTROPATH)?.sender;
      if (astropath) {
        await api.sendMessage(
            user.id || user.userId,
            astropath.id,
            'Команда выполнена успешно',
            MessageType.STATUS_UPDATE,
            messageId,
            0, 0, 0, 0.1
        );
      }

      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка выполнения: ${error.message}` });
    }
  };

  const handleSendTaxes = async () => {
    if (!planet) return;

    try {
      const taxAmount = planet.wealth * 0.1; // 10% налогов
      const content = `Отправлены налоги с планеты ${planet.name}: 💰${taxAmount.toFixed(2)}`;

      // Находим астропата
      const commandsData = await api.getCommandsForReceiver(user.id || user.userId);
      const astropath = commandsData.find(cmd => cmd.sender?.role === UserRole.ASTROPATH)?.sender;

      if (astropath) {
        const senderId = user.id || user.userId;
        if (!senderId) {
          throw new Error('ID пользователя не найден');
        }

        await api.sendMessage(
            senderId,
            astropath.id,
            content,
            MessageType.RESOURCES_TRANSFER,
            null,
            taxAmount,
            0, 0, 0.1
        );
        setMessage({ type: 'success', text: 'Налоги отправлены через астропата' });
      } else {
        setMessage({ type: 'error', text: 'Астропат не найден' });
      }
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка отправки налогов: ${error.message}` });
    }
  };

  if (loading) {
    return <div className="loading">Загрузка данных планеты...</div>;
  }

  if (!planet) {
    return (
        <div className="loading">
          <p>Планета не найдена</p>
          <p>ID планеты: {user.planetId}</p>
          <p>Обратитесь к вольному торговцу для назначения на планету</p>
        </div>
    );
  }

  return (
      <div>
        {message && (
            <div className={`message message-${message.type}`}>
              {message.text}
            </div>
        )}

        <div className="dashboard">
          <div className="card">
            <h3>👑 Моя Планета</h3>
            <div className="card-content">
              <h4 style={{ color: '#ffd700' }}>{planet.name}</h4>
              <div className="stat-item">
                <span className="stat-label">Тип:</span>
                <span className="stat-value">{getPlanetTypeDisplay(planet.planetType)}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Лояльность населения:</span>
                <span className="stat-value" style={{ color: planet.loyalty < 50 ? '#ff9800' : '#4caf50' }}>
                {planet.loyalty ? planet.loyalty.toFixed(1) : '0'}%
              </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Статус:</span>
                <span className="stat-value">
                <span className={`status-badge ${planet.isRebellious ? 'status-rebellious' : 'status-loyal'}`}>
                  {planet.isRebellious ? '🔥 Мятежная' : '🤝 Лояльная'}
                </span>
              </span>
              </div>
            </div>
          </div>

          <div className="card">
            <h3>💰 Ресурсы</h3>
            <div className="card-content">
              <div className="stat-item">
                <span className="stat-label">Богатство:</span>
                <span className="stat-value">💰{planet.wealth ? planet.wealth.toFixed(2) : '0'}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Промышленность:</span>
                <span className="stat-value">⚙️{planet.industry ? planet.industry.toFixed(2) : '0'}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Сырьевые ресурсы:</span>
                <span className="stat-value">⛏️{planet.resources ? planet.resources.toFixed(2) : '0'}</span>
              </div>
              <button
                  className="btn btn-primary"
                  onClick={handleSendTaxes}
                  style={{ marginTop: '10px' }}
                  disabled={!planet.wealth || planet.wealth < 100}
              >
                💸 Отправить Налоги (10%)
              </button>
            </div>
          </div>
        </div>

        {commands.filter(cmd => !cmd.completed).length > 0 && (
            <div className="table-container">
              <h3>📋 Команды для выполнения</h3>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))', gap: '15px' }}>
                {commands
                    .filter(cmd => !cmd.completed)
                    .map(cmd => (
                        <div key={cmd.id} style={{
                          padding: '20px',
                          border: '1px solid #444',
                          borderRadius: '5px',
                          background: 'rgba(0, 0, 0, 0.3)'
                        }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '10px' }}>
                            <div>
                              <strong style={{ color: '#ffd700' }}>{getMessageTypeDisplay(cmd.messageType)}</strong>
                              <p style={{ color: '#aaa', marginTop: '5px' }}>{cmd.content}</p>
                              <p style={{ color: '#666', fontSize: '12px', marginTop: '5px' }}>
                                От: {cmd.sender?.email || 'Вольный торговец'}
                              </p>
                            </div>
                            <div style={{ textAlign: 'right' }}>
                              {cmd.resourcesWealth > 0 && (
                                  <div style={{ color: '#4caf50', fontSize: '12px' }}>💰 {cmd.resourcesWealth}</div>
                              )}
                              {cmd.resourcesIndustry > 0 && (
                                  <div style={{ color: '#2196f3', fontSize: '12px' }}>⚙️ {cmd.resourcesIndustry}</div>
                              )}
                              {cmd.resourcesResources > 0 && (
                                  <div style={{ color: '#795548', fontSize: '12px' }}>⛏️ {cmd.resourcesResources}</div>
                              )}
                            </div>
                          </div>

                          <button
                              className="btn btn-primary"
                              onClick={() => handleExecuteCommand(cmd.id, cmd.messageType)}
                              style={{ width: '100%', marginTop: '10px' }}
                          >
                            ✅ Выполнить команду
                          </button>
                        </div>
                    ))}
              </div>
            </div>
        )}

        <div className="table-container">
          <h3>✅ Выполненные команды</h3>
          {commands.filter(cmd => cmd.completed).length === 0 ? (
              <p style={{ color: '#aaa', textAlign: 'center', padding: '20px' }}>
                Нет выполненных команд
              </p>
          ) : (
              <table>
                <thead>
                <tr>
                  <th>Тип</th>
                  <th>Содержание</th>
                  <th>Ресурсы</th>
                  <th>Дата выполнения</th>
                </tr>
                </thead>
                <tbody>
                {commands
                    .filter(cmd => cmd.completed)
                    .map(cmd => (
                        <tr key={cmd.id}>
                          <td>{getMessageTypeDisplay(cmd.messageType)}</td>
                          <td>{cmd.content}</td>
                          <td>
                            {cmd.resourcesWealth > 0 && `💰${cmd.resourcesWealth} `}
                            {cmd.resourcesIndustry > 0 && `⚙️${cmd.resourcesIndustry} `}
                            {cmd.resourcesResources > 0 && `⛏️${cmd.resourcesResources}`}
                          </td>
                          <td>{new Date(cmd.sentAt).toLocaleDateString()}</td>
                        </tr>
                    ))}
                </tbody>
              </table>
          )}
        </div>

        <div className="table-container">
          <h3>📈 Доступные улучшения для этой планеты</h3>
          <p style={{ color: '#aaa', marginBottom: '15px' }}>
            Тип планеты: <strong>{getPlanetTypeDisplay(planet.planetType)}</strong>
          </p>

          {upgrades
              .filter(u => u.suitableTypes === planet.planetType)
              .length === 0 ? (
              <p style={{ color: '#ff9800', textAlign: 'center', padding: '20px' }}>
                Нет доступных улучшений для данного типа планеты
              </p>
          ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '15px' }}>
                {upgrades
                    .filter(u => u.suitableTypes === planet.planetType)
                    .map(upgrade => (
                        <div key={upgrade.id} style={{
                          padding: '15px',
                          border: '1px solid #444',
                          borderRadius: '5px',
                          background: 'rgba(0, 0, 0, 0.3)'
                        }}>
                          <strong style={{ color: '#ffd700' }}>{upgrade.name}</strong>
                          <p style={{ color: '#aaa', marginTop: '5px', fontSize: '14px' }}>
                            {upgrade.description}
                          </p>
                          <div style={{ marginTop: '10px' }}>
                            <span style={{ color: '#4caf50', fontSize: '12px' }}>💰 {upgrade.costWealth}</span>
                            <span style={{ color: '#2196f3', fontSize: '12px', marginLeft: '10px' }}>⚙️ {upgrade.costIndustry}</span>
                            <span style={{ color: '#795548', fontSize: '12px', marginLeft: '10px' }}>⛏️ {upgrade.costResources}</span>
                          </div>
                        </div>
                    ))}
              </div>
          )}
        </div>
      </div>
  );
}

// Navigator Dashboard Component
function NavigatorDashboard({ user }) {
  const [planets, setPlanets] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [commands, setCommands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);

  const [routeForm, setRouteForm] = useState({
    fromPlanetId: '',
    toPlanetId: ''
  });

  useEffect(() => {
    if (user.id || user.navigatorId) {
      loadData();
    }
  }, [user.id, user.navigatorId]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [planetsData, routesData, commandsData] = await Promise.all([
        api.getAllPlanets(),
        api.getRoutes(user.id || user.navigatorId),
        api.getCommandsForReceiver(user.id || user.userId || user.navigatorId)
      ]);
      setPlanets(planetsData);
      setRoutes(routesData);
      setCommands(commandsData);
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка загрузки данных: ${error.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRoute = async () => {
    if (!routeForm.fromPlanetId || !routeForm.toPlanetId) {
      setMessage({ type: 'error', text: 'Выберите обе планеты!' });
      return;
    }

    try {
      await api.createRoute(routeForm.fromPlanetId, routeForm.toPlanetId, user.id || user.navigatorId);
      setMessage({ type: 'success', text: 'Маршрут успешно создан' });
      setRouteForm({ fromPlanetId: '', toPlanetId: '' });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка создания маршрута: ${error.message}` });
    }
  };

  const handleExecuteRouteCommand = async (messageId) => {
    try {
      const command = commands.find(cmd => cmd.id === messageId);
      if (!command) return;

      const content = command.content;

      // Улучшенный парсинг - ищем ID планет и навигатора
      const planetIdRegex = /ID:\s*(\d+)/g;
      const matches = [...content.matchAll(planetIdRegex)];

      let fromPlanetId, toPlanetId, navigatorId;

      if (matches.length >= 2) {
        fromPlanetId = parseInt(matches[0][1]);
        toPlanetId = parseInt(matches[1][1]);

        // Ищем ID навигатора
        const navigatorMatch = content.match(/навигатора ID:\s*(\d+)/);
        navigatorId = navigatorMatch ? parseInt(navigatorMatch[1]) :
            (user.id || user.navigatorId);
      } else {
        // Старый способ парсинга
        const oldFromMatch = content.match(/от планеты[^\d]*(\d+)/);
        const oldToMatch = content.match(/к планете[^\d]*(\d+)/);
        fromPlanetId = oldFromMatch ? parseInt(oldFromMatch[1]) : null;
        toPlanetId = oldToMatch ? parseInt(oldToMatch[1]) : null;
        navigatorId = user.id || user.navigatorId;
      }

      if (fromPlanetId && toPlanetId) {
        await api.createRoute(fromPlanetId, toPlanetId, navigatorId);
        await api.markCommandCompleted(messageId);
        setMessage({ type: 'success', text: 'Маршрут проложен, команда выполнена' });
        loadData();
      } else {
        setMessage({ type: 'error', text: 'Не удалось распознать ID планет' });
      }
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка выполнения: ${error.message}` });
    }
  };

  if (loading) {
    return <div className="loading">Загрузка карты навигации...</div>;
  }

  return (
      <div>
        {message && (
            <div className={`message message-${message.type}`}>
              {message.text}
            </div>
        )}

        <div className="dashboard">
          <div className="card">
            <h3>🧭 Навигатор</h3>
            <div className="card-content">
              <div className="stat-item">
                <span className="stat-label">Дом навигаторов:</span>
                <span className="stat-value">{user.houseName || 'Не указан'}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Прокладынных маршрутов:</span>
                <span className="stat-value">{routes.length}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Стабильных маршрутов:</span>
                <span className="stat-value">{routes.filter(r => r.isStable).length}</span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Команд в работе:</span>
                <span className="stat-value" style={{ color: '#ff9800' }}>
                {commands.filter(cmd => !cmd.completed).length}
              </span>
              </div>
            </div>
          </div>
        </div>

        <div className="table-container">
          <h3>🗺️ Карта Сектора Коронус</h3>
          <EmpireMap
              planets={planets}
              routes={routes}
              showDetails={false}
          />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginTop: '20px' }}>
          <div className="table-container">
            <h3>🛤️ Создать Маршрут</h3>
            <div className="form-group">
              <label>От планеты:</label>
              <select
                  value={routeForm.fromPlanetId}
                  onChange={(e) => setRouteForm({...routeForm, fromPlanetId: e.target.value})}
                  style={{
                    padding: '10px',
                    border: '1px solid #555',
                    borderRadius: '5px',
                    background: '#2d2d44',
                    color: '#e0e0e0',
                    width: '100%',
                  }}
              >
                <option value="">Выберите планету отправления</option>
                {planets.map(planet => (
                    <option key={planet.id} value={planet.id}>
                      {planet.name} ({getPlanetTypeDisplay(planet.planetType)})
                    </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>К планете:</label>
              <select
                  value={routeForm.toPlanetId}
                  onChange={(e) => setRouteForm({...routeForm, toPlanetId: e.target.value})}
                  style={{
                    padding: '10px',
                    border: '1px solid #555',
                    borderRadius: '5px',
                    background: '#2d2d44',
                    color: '#e0e0e0',
                    width: '100%',
                  }}
              >
                <option value="">Выберите планету назначения</option>
                {planets
                    .filter(p => p.id != routeForm.fromPlanetId)
                    .map(planet => (
                        <option key={planet.id} value={planet.id}>
                          {planet.name} ({getPlanetTypeDisplay(planet.planetType)})
                        </option>
                    ))}
              </select>
            </div>

            <button
                className="btn btn-primary"
                onClick={handleCreateRoute}
                disabled={!routeForm.fromPlanetId || !routeForm.toPlanetId}
                style={{ width: '100%', marginTop: '15px' }}
            >
              🧭 Проложить Варп-Маршрут
            </button>
          </div>

          {commands.filter(cmd => !cmd.completed).length > 0 && (
              <div className="table-container">
                <h3>📋 Команды от торговца</h3>
                <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                  {commands
                      .filter(cmd => !cmd.completed)
                      .map(cmd => (
                          <div key={cmd.id} style={{
                            padding: '15px',
                            marginBottom: '10px',
                            border: '1px solid #444',
                            borderRadius: '5px',
                            background: 'rgba(0, 0, 0, 0.3)'
                          }}>
                            <p style={{ color: '#e0e0e0' }}>{cmd.content}</p>
                            <p style={{ color: '#666', fontSize: '12px', marginTop: '5px' }}>
                              От: {cmd.sender?.email || 'Вольный торговец'}
                            </p>
                            <button
                                className="btn btn-primary"
                                onClick={() => handleExecuteRouteCommand(cmd.id)}
                                style={{ width: '100%', marginTop: '10px' }}
                            >
                              🧭 Выполнить команду
                            </button>
                          </div>
                      ))}
                </div>
              </div>
          )}
        </div>

        <div className="table-container">
          <h3>📋 Мои Маршруты</h3>
          {routes.length === 0 ? (
              <p style={{ color: '#ff9800', textAlign: 'center', padding: '20px' }}>
                Вы еще не создали ни одного маршрута
              </p>
          ) : (
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
                      <td>
                        <strong>{route.fromPlanet?.name || 'Неизвестно'}</strong>
                        <div style={{ fontSize: '12px', color: '#aaa' }}>
                          {route.fromPlanet ? getPlanetTypeDisplay(route.fromPlanet.planetType) : ''}
                        </div>
                      </td>
                      <td>
                        <strong>{route.toPlanet?.name || 'Неизвестно'}</strong>
                        <div style={{ fontSize: '12px', color: '#aaa' }}>
                          {route.toPlanet ? getPlanetTypeDisplay(route.toPlanet.planetType) : ''}
                        </div>
                      </td>
                      <td>
                    <span className={`status-badge ${route.isStable ? 'status-loyal' : 'status-rebellious'}`}>
                      {route.isStable ? '✅ Стабильный' : '⚠️ Нестабильный'}
                    </span>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
          )}
        </div>
      </div>
  );
}

// Astropath Dashboard Component
function AstropathDashboard({ user }) {
  const [pendingMessages, setPendingMessages] = useState([]);
  const [deliveredMessages, setDeliveredMessages] = useState([]);
  const [traderCommands, setTraderCommands] = useState([]);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState(null);
  const [selectedMessage, setSelectedMessage] = useState(null);

  // Для пересылки команд
  const [forwardData, setForwardData] = useState({
    messageId: '',
    finalReceiverId: ''
  });

  useEffect(() => {
    if (user.id || user.astropathId) {
      loadData();
    }
  }, [user.id, user.astropathId]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [usersData, messagesData] = await Promise.all([
        api.getUsers(),
        api.getMessagesForUser(user.id || user.userId || user.astropathId)
      ]);

      setUsers(usersData);

      // Фильтруем сообщения
      const allMessages = messagesData || [];
      setPendingMessages(allMessages.filter(msg =>
          msg.receiver && msg.receiver.id === (user.id || user.astropathId) && !msg.delivered
      ));
      setDeliveredMessages(allMessages.filter(msg =>
          msg.sender && msg.sender.id === (user.id || user.astropathId) && msg.delivered
      ));

      // Команды от торговца
      setTraderCommands(allMessages.filter(msg =>
          msg.sender && msg.sender.role === UserRole.TRADER &&
          msg.receiver && msg.receiver.id === (user.id || user.astropathId)
      ));
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка загрузки данных: ${error.message}` });
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async (originalMessage, finalReceiverId) => {
    try {
      const astropathId = user.id || user.astropathId;
      if (!astropathId) {
        throw new Error('ID астропата не найден');
      }

      // Отправляем сообщение конечному получателю
      await api.astropathSendMessage(
          astropathId,
          finalReceiverId,
          originalMessage.content,
          originalMessage.messageType,
          originalMessage.commandId,
          originalMessage.resourcesWealth,
          originalMessage.resourcesIndustry,
          originalMessage.resourcesResources,
          user.psiLevel < 4 ? 0.3 : user.psiLevel < 7 ? 0.2 : 0.1
      );

      // Помечаем оригинальное сообщение как доставленное астропату
      await api.markMessageDelivered(originalMessage.id);

      setMessage({ type: 'success', text: 'Сообщение отправлено через варп' });
      setForwardData({ messageId: '', finalReceiverId: '' });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка отправки: ${error.message}` });
    }
  };

  const handleForwardCommand = async () => {
    if (!forwardData.messageId || !forwardData.finalReceiverId) {
      setMessage({ type: 'error', text: 'Выберите сообщение и получателя!' });
      return;
    }

    try {
      const astropathId = user.id || user.astropathId;
      if (!astropathId) {
        throw new Error('ID астропата не найден');
      }

      const originalMessage = [...pendingMessages, ...traderCommands]
          .find(msg => msg.id == forwardData.messageId);

      if (!originalMessage) {
        throw new Error('Сообщение не найдено');
      }

      await api.forwardCommand(
          astropathId,
          forwardData.messageId,
          forwardData.finalReceiverId
      );

      setMessage({ type: 'success', text: 'Команда переслана получателю' });
      setForwardData({ messageId: '', finalReceiverId: '' });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка пересылки: ${error.message}` });
    }
  };

  const handleDeliverMessage = async (messageId) => {
    try {
      await api.markMessageDelivered(messageId);
      setMessage({ type: 'success', text: 'Сообщение отмечено доставленным' });
      loadData();
    } catch (error) {
      setMessage({ type: 'error', text: `Ошибка: ${error.message}` });
    }
  };

  const getRecipientForMessage = (msg) => {
    if (msg.messageType === 'UPGRADE_REQUEST' || msg.messageType === 'CRISIS_RESPONSE') {
      // Находим губернатора планеты
      const planetId = msg.commandId || extractPlanetIdFromContent(msg.content);
      const governor = users.find(u =>
          u.role === UserRole.GOVERNOR && u.planetId == planetId
      );
      return governor;
    } else if (msg.messageType === 'NAVIGATION_REQUEST') {
      return users.find(u => u.role === UserRole.NAVIGATOR);
    }
    return null;
  };

  const extractPlanetIdFromContent = (content) => {
    const match = content.match(/планет[аеыу] (\d+)/i);
    return match ? match[1] : null;
  };

  if (loading) {
    return <div className="loading">Загрузка коммуникационной сети...</div>;
  }

  return (
      <div>
        {message && (
            <div className={`message message-${message.type}`}>
              {message.text}
            </div>
        )}

        <div className="dashboard">
          <div className="card">
            <h3>🔮 Панель Астропата</h3>
            <div className="card-content">
              <div className="stat-item">
                <span className="stat-label">Уровень пси:</span>
                <span className="stat-value" style={{
                  color: (user.psiLevel || 5) >= 7 ? '#4caf50' :
                      (user.psiLevel || 5) >= 4 ? '#ff9800' : '#d32f2f'
                }}>
                {user.psiLevel || 5}/10
              </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Шанс искажения:</span>
                <span className="stat-value">
                {(user.psiLevel || 5) < 4 ? 'Высокий (30%)' :
                    (user.psiLevel || 5) < 7 ? 'Средний (20%)' : 'Низкий (10%)'}
              </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Сообщений в очереди:</span>
                <span className="stat-value" style={{ color: '#ff9800' }}>
                {pendingMessages.length + traderCommands.length}
              </span>
              </div>
              <div className="stat-item">
                <span className="stat-label">Доставлено сообщений:</span>
                <span className="stat-value">{deliveredMessages.length}</span>
              </div>
            </div>
          </div>

          <div className="card">
            <h3>⚠️ Предупреждение</h3>
            <div className="card-content">
              <p style={{ color: '#ff9800', fontSize: '14px' }}>
                Варп нестабилен. Сообщения могут быть искажены при прохождении через него.
                Ваш уровень пси: {user.psiLevel || 5}/10 ({((user.psiLevel || 5) * 10)}% эффективности)
              </p>
            </div>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginTop: '20px' }}>
          <div className="table-container">
            <h3>📥 Входящие сообщения</h3>
            {pendingMessages.length === 0 && traderCommands.length === 0 ? (
                <p style={{ color: '#aaa', textAlign: 'center', padding: '20px' }}>
                  Нет новых сообщений
                </p>
            ) : (
                <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                  {[...pendingMessages, ...traderCommands].map(msg => {
                    const recipient = getRecipientForMessage(msg);
                    return (
                        <div
                            key={msg.id}
                            style={{
                              padding: '15px',
                              marginBottom: '10px',
                              border: '1px solid #444',
                              borderRadius: '5px',
                              background: 'rgba(0, 0, 0, 0.3)',
                              cursor: 'pointer'
                            }}
                            onClick={() => setSelectedMessage({...msg, recipient})}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                              <strong style={{ color: '#e0e0e0' }}>От: {msg.sender?.email || 'Неизвестно'}</strong>
                              <p style={{ color: '#aaa', marginTop: '5px', fontSize: '14px' }}>
                                {msg.content}
                              </p>
                              <p style={{ color: '#666', fontSize: '12px', marginTop: '5px' }}>
                                Тип: {getMessageTypeDisplay(msg.messageType)}
                              </p>
                              {recipient && (
                                  <p style={{ color: '#4caf50', fontSize: '12px', marginTop: '5px' }}>
                                    Получатель: {recipient.email} ({getRoleDisplay(recipient.role)})
                                  </p>
                              )}
                            </div>
                            <div style={{ textAlign: 'right' }}>
                              <div style={{ fontSize: '12px', color: '#aaa' }}>
                                {new Date(msg.sentAt).toLocaleString()}
                              </div>
                            </div>
                          </div>

                          <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                            {recipient && (
                                <button
                                    className="btn btn-primary"
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleSendMessage(msg, recipient.id);
                                    }}
                                    style={{ flex: 1 }}
                                >
                                  📨 Отправить {recipient.role === 'GOVERNOR' ? 'губернатору' : 'навигатору'}
                                </button>
                            )}
                            <button
                                className="btn btn-secondary"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  setForwardData({...forwardData, messageId: msg.id});
                                }}
                                style={{ flex: 1 }}
                            >
                              ↩️ Переслать
                            </button>
                          </div>
                        </div>
                    );
                  })}
                </div>
            )}
          </div>

          <div className="table-container">
            <h3>📤 Переслать команду</h3>
            <div className="form-group">
              <label>Сообщение:</label>
              <select
                  value={forwardData.messageId}
                  onChange={(e) => setForwardData({...forwardData, messageId: e.target.value})}
                  style={{
                    padding: '10px',
                    border: '1px solid #555',
                    borderRadius: '5px',
                    background: '#2d2d44',
                    color: '#e0e0e0',
                    width: '100%'
                  }}
              >
                <option value="">Выберите сообщение</option>
                {[...pendingMessages, ...traderCommands].map(msg => (
                    <option key={msg.id} value={msg.id}>
                      От {msg.sender?.email}: {msg.content.substring(0, 50)}...
                    </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Конечный получатель:</label>
              <select
                  value={forwardData.finalReceiverId}
                  onChange={(e) => setForwardData({...forwardData, finalReceiverId: e.target.value})}
                  style={{
                    padding: '10px',
                    border: '1px solid #555',
                    borderRadius: '5px',
                    background: '#2d2d44',
                    color: '#e0e0e0',
                    width: '100%'
                  }}
              >
                <option value="">Выберите получателя</option>
                {users
                    .filter(u => u.id !== user.id && (u.role === UserRole.GOVERNOR || u.role === UserRole.NAVIGATOR))
                    .map(u => (
                        <option key={u.id} value={u.id}>
                          {u.email} ({getRoleDisplay(u.role)})
                        </option>
                    ))}
              </select>
            </div>

            <button
                className="btn btn-primary"
                onClick={handleForwardCommand}
                disabled={!forwardData.messageId || !forwardData.finalReceiverId}
                style={{ width: '100%', marginTop: '10px' }}
            >
              🌌 Переслать через Варп
            </button>
          </div>
        </div>

        <div className="table-container" style={{ marginTop: '20px' }}>
          <h3>✅ Доставленные сообщения</h3>
          {deliveredMessages.length === 0 ? (
              <p style={{ color: '#aaa', textAlign: 'center', padding: '20px' }}>
                Нет доставленных сообщений
              </p>
          ) : (
              <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                {deliveredMessages.map(msg => (
                    <div
                        key={msg.id}
                        style={{
                          padding: '15px',
                          marginBottom: '10px',
                          border: '1px solid #444',
                          borderRadius: '5px',
                          background: msg.distorted ? 'rgba(211, 47, 47, 0.1)' : 'rgba(0, 0, 0, 0.3)',
                          cursor: 'pointer'
                        }}
                        onClick={() => setSelectedMessage(msg)}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <strong style={{ color: '#e0e0e0' }}>Кому: {msg.receiver?.email || 'Неизвестно'}</strong>
                          <p style={{
                            color: msg.distorted ? '#ff6b6b' : '#aaa',
                            marginTop: '5px',
                            fontSize: '14px'
                          }}>
                            {msg.content}
                          </p>
                        </div>
                        <div style={{ textAlign: 'right' }}>
                          <div style={{ fontSize: '12px', color: '#aaa' }}>
                            {new Date(msg.sentAt).toLocaleString()}
                          </div>
                          <div style={{
                            fontSize: '12px',
                            color: msg.distorted ? '#ff6b6b' : '#4caf50',
                            marginTop: '5px'
                          }}>
                            {msg.distorted ? '⚠️ Искажено в варпе' : '✅ Успешно доставлено'}
                          </div>
                        </div>
                      </div>
                    </div>
                ))}
              </div>
          )}
        </div>

        {selectedMessage && (
            <div className="modal-overlay">
              <div className="modal-content">
                <h3 style={{ color: '#ffd700', marginBottom: '20px' }}>
                  📨 Детали сообщения
                </h3>
                <div style={{ marginBottom: '15px' }}>
                  <strong style={{ color: '#aaa' }}>Отправитель:</strong>
                  <p style={{ color: '#e0e0e0' }}>{selectedMessage.sender?.email || 'Неизвестно'}</p>
                </div>
                <div style={{ marginBottom: '15px' }}>
                  <strong style={{ color: '#aaa' }}>Получатель:</strong>
                  <p style={{ color: '#e0e0e0' }}>
                    {selectedMessage.recipient
                        ? `${selectedMessage.recipient.email} (${getRoleDisplay(selectedMessage.recipient.role)})`
                        : selectedMessage.receiver?.email || 'Неизвестно'}
                  </p>
                </div>
                <div style={{ marginBottom: '15px' }}>
                  <strong style={{ color: '#aaa' }}>Тип:</strong>
                  <p style={{ color: '#e0e0e0' }}>{getMessageTypeDisplay(selectedMessage.messageType)}</p>
                </div>
                <div style={{ marginBottom: '15px' }}>
                  <strong style={{ color: '#aaa' }}>Содержание:</strong>
                  <p style={{
                    color: selectedMessage.distorted ? '#ff6b6b' : '#e0e0e0',
                    padding: '10px',
                    background: 'rgba(0, 0, 0, 0.3)',
                    borderRadius: '5px',
                    marginTop: '5px'
                  }}>
                    {selectedMessage.content}
                  </p>
                </div>
                {selectedMessage.resourcesWealth > 0 && (
                    <div style={{ marginBottom: '15px' }}>
                      <strong style={{ color: '#aaa' }}>Ресурсы:</strong>
                      <p style={{ color: '#e0e0e0' }}>
                        💰{selectedMessage.resourcesWealth}
                        ⚙️{selectedMessage.resourcesIndustry}
                        ⛏️{selectedMessage.resourcesResources}
                      </p>
                    </div>
                )}
                <div style={{ marginBottom: '20px' }}>
                  <strong style={{ color: '#aaa' }}>Дата отправки:</strong>
                  <p style={{ color: '#e0e0e0' }}>{new Date(selectedMessage.sentAt).toLocaleString()}</p>
                </div>
                <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                  <button
                      className="btn btn-secondary"
                      onClick={() => setSelectedMessage(null)}
                  >
                    Закрыть
                  </button>
                </div>
              </div>
            </div>
        )}
      </div>
  );
}

// Helper functions
function getPlanetTypeDisplay(type) {
  const types = {
    'AGRI_WORLD': '🌾 Аграрный Мир',
    'FORGE_WORLD': '⚒️ Кузнечный Мир',
    'MINING_WORLD': '⛏️ Горнодобывающий Мир',
    'CIVILIZED_WORLD': '🏛️ Цивилизованный Мир',
    'DEATH_WORLD': '☠️ Мир Смерти',
    'HIVE_WORLD': '🏙️ Улей Мир',
    'FEUDAL_WORLD': '⚔️ Феодальный Мир'
  };
  return types[type] || type;
}

function getMessageTypeDisplay(type) {
  const types = {
    'NAVIGATION_REQUEST': '🛤️ Прокладка маршрута',
    'UPGRADE_REQUEST': '🏗️ Постройка улучшения',
    'CRISIS_RESPONSE': '🚨 Решение кризиса',
    'RESOURCES_TRANSFER': '💰 Передача ресурсов',
    'STATUS_UPDATE': '📊 Статус выполнения'
  };
  return types[type] || type;
}

function getEventTypeDisplay(type) {
  const types = {
    'INSURRECTION': '🔥 Мятеж',
    'NATURAL_DISASTER': '🌪️ Природная катастрофа',
    'ECONOMIC_CRISIS': '📉 Экономический кризис',
    'EXTERNAL_THREAT': '🛡️ Внешняя угроза'
  };
  return types[type] || type;
}

function getRoleDisplay(role) {
  const roles = {
    'TRADER': 'Вольный Торговец',
    'GOVERNOR': 'Губернатор',
    'ASTROPATH': 'Астропат',
    'NAVIGATOR': 'Навигатор'
  };
  return roles[role] || role;
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
        return <div>Неизвестная роль пользователя</div>;
    }
  };

  return (
      <div className="container">
        <div className="header">
          <h1>🌟 Rogue Trader - Управление Империей 🌟</h1>
          <p style={{ color: '#aaa', marginBottom: '10px' }}>
            Информационная система для управления торговой империей вольного торговца WH40k
          </p>
          {user && (
              <div className="user-info">
                <div>
                  <span style={{ color: '#ffd700' }}>{user.email}</span>
                  <span style={{ color: '#4fc3f7', marginLeft: '10px' }}>
                ({getRoleDisplay(user.role)})
              </span>
                  {user.dynastyName && (
                      <span style={{ color: '#81c784', marginLeft: '10px' }}>
                  Династия: {user.dynastyName}
                </span>
                  )}
                  {user.houseName && (
                      <span style={{ color: '#81c784', marginLeft: '10px' }}>
                  Дом: {user.houseName}
                </span>
                  )}
                  {user.psiLevel && (
                      <span style={{ color: '#81c784', marginLeft: '10px' }}>
                  Уровень пси: {user.psiLevel}/10
                </span>
                  )}
                </div>
                <button className="btn btn-danger logout-btn" onClick={handleLogout}>
                  🚪 Выйти
                </button>
              </div>
          )}
        </div>

        {!user ? (
            <Auth onLogin={handleLogin} />
        ) : (
            renderDashboard()
        )}

        <footer style={{
          marginTop: '40px',
          padding: '20px',
          textAlign: 'center',
          color: '#666',
          borderTop: '1px solid #444'
        }}>
          <p>Rogue Trader Information System © 2025</p>
          <p style={{ fontSize: '12px' }}>
            Империя бесконечна, власть - вечна. Служите Императору!
          </p>
        </footer>

        {/* CSS стили */}
        <style>{`
        .container {
          max-width: 1400px;
          margin: 0 auto;
          padding: 20px;
          font-family: 'Arial', sans-serif;
          background: #1e1e2e;
          color: #e0e0e0;
          min-height: 100vh;
        }
        
        .header {
          background: linear-gradient(135deg, #2d2d44 0%, #1a1a2e 100%);
          padding: 20px;
          border-radius: 10px;
          margin-bottom: 30px;
          border: 1px solid #444;
        }
        
        .user-info {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 10px;
          padding-top: 10px;
          border-top: 1px solid #444;
        }
        
        .auth-section {
          max-width: 500px;
          margin: 50px auto;
          padding: 30px;
          background: #2d2d44;
          border-radius: 10px;
          border: 1px solid #444;
        }
        
        .auth-form {
          display: flex;
          flex-direction: column;
          gap: 15px;
        }
        
        .form-group {
          display: flex;
          flex-direction: column;
          gap: 5px;
        }
        
        .form-group label {
          color: '#aaa';
          font-size: 14px;
        }
        
        .form-group input,
        .form-group select,
        .form-group textarea {
          padding: 10px;
          border: 1px solid #555;
          border-radius: 5px;
          background: #1e1e2e;
          color: #e0e0e0;
          font-size: 14px;
        }
        
        .form-group input:focus,
        .form-group select:focus,
        .form-group textarea:focus {
          outline: none;
          border-color: #4fc3f7;
        }
        
        .btn {
          padding: 10px 20px;
          border: none;
          border-radius: 5px;
          cursor: pointer;
          font-size: 14px;
          font-weight: bold;
          transition: all 0.3s;
          text-align: center;
        }
        
        .btn-primary {
          background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
          color: white;
        }
        
        .btn-primary:hover {
          background: linear-gradient(135deg, #1976d2 0%, #0d47a1 100%);
          transform: translateY(-2px);
        }
        
        .btn-primary:disabled {
          background: #666;
          cursor: not-allowed;
          transform: none;
        }
        
        .btn-secondary {
          background: #555;
          color: white;
        }
        
        .btn-secondary:hover {
          background: #666;
          transform: translateY(-2px);
        }
        
        .btn-danger {
          background: linear-gradient(135deg, #d32f2f 0%, #b71c1c 100%);
          color: white;
        }
        
        .btn-danger:hover {
          background: linear-gradient(135deg, #b71c1c 0%, #7f0000 100%);
          transform: translateY(-2px);
        }
        
        .logout-btn {
          padding: 8px 16px;
          font-size: 12px;
        }
        
        .dashboard {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
          gap: 20px;
          margin-bottom: 30px;
        }
        
        .card {
          background: #2d2d44;
          border-radius: 10px;
          padding: 20px;
          border: 1px solid #444;
          transition: all 0.3s;
        }
        
        .card:hover {
          border-color: #4fc3f7;
          transform: translateY(-5px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
        }
        
        .card h3 {
          color: #ffd700;
          margin-top: 0;
          margin-bottom: 15px;
          font-size: 18px;
          border-bottom: 1px solid #444;
          padding-bottom: 10px;
        }
        
        .card-content {
          display: flex;
          flex-direction: column;
          gap: 10px;
        }
        
        .stat-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          border-bottom: 1px solid rgba(255, 255, 255, 0.05);
        }
        
        .stat-label {
          color: #aaa;
          font-size: 14px;
        }
        
        .stat-value {
          color: #4fc3f7;
          font-weight: bold;
          font-size: 16px;
        }
        
        .table-container {
          background: #2d2d44;
          border-radius: 10px;
          padding: 20px;
          margin-bottom: 30px;
          border: 1px solid #444;
        }
        
        .table-container h3 {
          color: #ffd700;
          margin-top: 0;
          margin-bottom: 20px;
          font-size: 20px;
        }
        
        table {
          width: 100%;
          border-collapse: collapse;
          background: #1e1e2e;
          border-radius: 5px;
          overflow: hidden;
        }
        
        table thead {
          background: #333;
        }
        
        table th {
          padding: 15px;
          text-align: left;
          color: #ffd700;
          font-weight: bold;
          border-bottom: 2px solid #444;
        }
        
        table td {
          padding: 12px 15px;
          border-bottom: 1px solid #444;
          color: #e0e0e0;
        }
        
        table tr:hover {
          background: rgba(79, 195, 247, 0.1);
        }
        
        .status-badge {
          display: inline-block;
          padding: 4px 8px;
          border-radius: 15px;
          font-size: 12px;
          font-weight: bold;
        }
        
        .status-loyal {
          background: rgba(76, 175, 80, 0.2);
          color: #4caf50;
          border: 1px solid #4caf50;
        }
        
        .status-rebellious {
          background: rgba(211, 47, 47, 0.2);
          color: #d32f2f;
          border: 1px solid #d32f2f;
        }
        
        .status-completed {
          background: rgba(76, 175, 80, 0.2);
          color: #4caf50;
          border: 1px solid #4caf50;
        }
        
        .status-in-progress {
          background: rgba(255, 152, 0, 0.2);
          color: #ff9800;
          border: 1px solid #ff9800;
        }
        
        .event-type {
          display: inline-block;
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 12px;
        }
        
        .event-insurrection {
          background: rgba(211, 47, 47, 0.2);
          color: #d32f2f;
        }
        
        .event-natural_disaster {
          background: rgba(255, 152, 0, 0.2);
          color: #ff9800;
        }
        
        .message {
          padding: 15px;
          border-radius: 5px;
          margin-bottom: 20px;
          border: 1px solid;
        }
        
        .message-success {
          background: rgba(76, 175, 80, 0.1);
          border-color: #4caf50;
          color: #4caf50;
        }
        
        .message-error {
          background: rgba(211, 47, 47, 0.1);
          border-color: #d32f2f;
          color: #d32f2f;
        }
        
        .message-info {
          background: rgba(33, 150, 243, 0.1);
          border-color: #2196f3;
          color: #2196f3;
        }
        
        .loading {
          text-align: center;
          padding: 50px;
          color: #4fc3f7;
          font-size: 18px;
        }
        
        .modal-overlay {
          position: fixed;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.8);
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 1000;
          padding: 20px;
        }
        
        .modal-content {
          background: #2d2d44;
          padding: 30px;
          border-radius: 10px;
          border: 1px solid #444;
          max-width: 500px;
          width: 100%;
          max-height: 90vh;
          overflow-y: auto;
        }
        
        .modal-content h3 {
          color: #ffd700;
          margin-top: 0;
          margin-bottom: 20px;
        }
        
        .modal-actions {
          display: flex;
          gap: 10px;
          margin-top: 20px;
          justify-content: flex-end;
        }
        
        @media (max-width: 768px) {
          .dashboard {
            grid-template-columns: 1fr;
          }
          
          .container {
            padding: 10px;
          }
        }
      `}</style>
      </div>
  );
}

// Render App
ReactDOM.render(<App />, document.getElementById('root'));