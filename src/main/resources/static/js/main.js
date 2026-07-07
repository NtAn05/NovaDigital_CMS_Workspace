// Dynamic Data Loading, Authentication, and UI logic for NovaDigital Creative Agency

document.addEventListener("DOMContentLoaded", () => {
  console.log("DOM Loaded");
  
  // 0. Initialize scroll animations
  initScrollAnimations();

  // 1. Inject the Auth Modal into every page
  injectAuthModal();

  // 1b. Inject the Floating Quick Access Panel
  injectQuickPanel();

  // 2. Check Authentication Route Guards
  checkRouteGuard();

  // 3. Update Navbar dynamically based on Authentication
  updateNavbarAuth();

  // 4. Highlight Active Navigation Item
  highlightActiveLink();

  // 5. Detect current page and fetch corresponding data
  const path = window.location.pathname;
  const page = path.substring(path.lastIndexOf('/') + 1) || "index.html";
  console.log("Current page:", page);

  if (page === "services.html") {
    fetchServices();
  } else if (page === "about.html") {
    fetchMembers();
  } else if (page === "portfolio.html") {
    fetchProjects();
  } else if (page === "contact.html") {
    initContactForm();
  } else if (page === "login.html") {
    initLoginForm();
  } else if (page === "register.html") {
    initRegisterForm();
  } else if (page === "admin.html") {
    initAdminDashboard();
  } else if (page === "member-contact.html") {
    // Member page is handled by inline script
  } else if (page === "index.html") {
    // Fetch inbox if user is logged in
    const token = localStorage.getItem("token");
    const email = localStorage.getItem("email"); 
    console.log("Token:", token);
    console.log("Email from localStorage:", email);
    if (token && email) {
      console.log("Calling fetchInbox with email:", email);
      fetchInbox(email);
    }
  }

  // 6. Handle hash-based modal auto-opening (e.g. index.html?error=unauthorized#login)
  const hash = window.location.hash;
  const urlParams = new URLSearchParams(window.location.search);

  if (hash === "#login") {
    openAuthModal("login");
    if (urlParams.get("error") === "unauthorized") {
      setTimeout(() => {
        showModalAlert("Bạn cần đăng nhập để truy cập tính năng này.", false, "modal-login-alert");
      }, 180);
    }
  } else if (hash === "#register") {
    openAuthModal("register");
  } else if (hash === "#registered") {
    openAuthModal("login");
    setTimeout(() => {
      showModalAlert("Đăng ký tài khoản thành công! Hãy đăng nhập.", true, "modal-login-alert");
    }, 180);
  }
});

// =============================================
//  Auth Modal – Injection & Control
// =============================================

function injectAuthModal() {
  const modalHTML = `
    <div id="auth-modal-overlay" class="auth-modal-overlay" role="dialog" aria-modal="true" aria-labelledby="auth-modal-heading">
      <div class="auth-modal">

        <!-- Close button -->
        <button class="auth-modal-close" id="auth-modal-close-btn" aria-label="Đóng">&times;</button>

        <!-- Brand -->
        <div class="auth-modal-brand">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
          <span>NovaDigital</span>
        </div>

        <!-- Tab switcher -->
        <div class="auth-modal-tabs" role="tablist">
          <button class="auth-tab-btn active" id="tab-login" role="tab"
            aria-selected="true" aria-controls="panel-login"
            onclick="switchAuthTab('login')">Đăng Nhập</button>
          <button class="auth-tab-btn" id="tab-register" role="tab"
            aria-selected="false" aria-controls="panel-register"
            onclick="switchAuthTab('register')">Đăng Ký</button>
        </div>

        <!-- ===== LOGIN PANEL ===== -->
        <div class="auth-panel active" id="panel-login" role="tabpanel" aria-labelledby="tab-login">
          <h2 id="auth-modal-heading">Chào Mừng Trở Lại</h2>
          <p class="subtitle">Nhập thông tin để truy cập tài khoản của bạn</p>

          <form id="modal-loginForm" novalidate>
            <div class="form-group">
              <label for="modal-usernameOrEmail">Tên đăng nhập hoặc Email *</label>
              <input type="text" id="modal-usernameOrEmail"
                placeholder="Nhập tên đăng nhập hoặc email" required autocomplete="username">
            </div>
            <div class="form-group">
              <label for="modal-password">Mật khẩu *</label>
              <input type="password" id="modal-password"
                placeholder="••••••••" required autocomplete="current-password">
            </div>
            <button type="submit" class="submit-btn" style="margin-top:0.5rem;">Đăng Nhập</button>
            <div id="modal-login-alert" class="alert-message"></div>
          </form>

          <div class="auth-modal-divider">
            Chưa có tài khoản?
            <a onclick="switchAuthTab('register')">Đăng ký ngay</a>
          </div>
        </div>

        <!-- ===== REGISTER PANEL ===== -->
        <div class="auth-panel" id="panel-register" role="tabpanel" aria-labelledby="tab-register">
          <h2>Tạo Tài Khoản</h2>
          <p class="subtitle">Tham gia NovaDigital để trải nghiệm dịch vụ cao cấp</p>

          <form id="modal-registerForm" novalidate>
            <div class="form-group">
              <label for="modal-username">Tên đăng nhập *</label>
              <input type="text" id="modal-username"
                placeholder="Chọn tên đăng nhập" required minlength="4" maxlength="50" autocomplete="username">
            </div>
            <div class="form-group">
              <label for="modal-fullName">Họ và tên *</label>
              <input type="text" id="modal-fullName"
                placeholder="Nhập họ và tên đầy đủ" required autocomplete="name">
            </div>
            <div class="form-group">
              <label for="modal-email">Địa chỉ Email *</label>
              <input type="email" id="modal-email"
                placeholder="name@domain.com" required autocomplete="email">
            </div>
            <div class="form-group">
              <label for="modal-phone">Số điện thoại (10 chữ số)</label>
              <input type="tel" id="modal-phone"
                placeholder="0123456789" pattern="[0-9]{10}" autocomplete="tel">
            </div>
            <div class="form-group">
              <label for="modal-reg-password">Mật khẩu *</label>
              <input type="password" id="modal-reg-password"
                placeholder="Tối thiểu 6 ký tự" required minlength="6" autocomplete="new-password">
            </div>
            <button type="submit" class="submit-btn" style="margin-top:0.5rem;">Đăng Ký</button>
            <div id="modal-register-alert" class="alert-message"></div>
          </form>

          <div class="auth-modal-divider">
            Đã có tài khoản?
            <a onclick="switchAuthTab('login')">Đăng nhập tại đây</a>
          </div>
        </div>

      </div>
    </div>
  `;

  document.body.insertAdjacentHTML("beforeend", modalHTML);

  // Bind close button
  document.getElementById("auth-modal-close-btn").addEventListener("click", closeAuthModal);

  // Close when clicking on backdrop
  document.getElementById("auth-modal-overlay").addEventListener("click", (e) => {
    if (e.target.id === "auth-modal-overlay") closeAuthModal();
  });

  // Close on Escape key
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeAuthModal();
  });

  // Bind form submit handlers inside the modal
  initModalLoginForm();
  initModalRegisterForm();
}

function openAuthModal(tab = "login") {
  const overlay = document.getElementById("auth-modal-overlay");
  if (!overlay) return;
  overlay.classList.add("is-open");
  document.body.style.overflow = "hidden";
  switchAuthTab(tab);
}

function closeAuthModal() {
  const overlay = document.getElementById("auth-modal-overlay");
  if (!overlay) return;
  overlay.classList.remove("is-open");
  document.body.style.overflow = "";
  // Clear all modal alerts on close
  ["modal-login-alert", "modal-register-alert"].forEach(id => {
    const el = document.getElementById(id);
    if (el) { el.style.display = "none"; el.textContent = ""; el.className = "alert-message"; }
  });
}

function switchAuthTab(tab) {
  const loginTab    = document.getElementById("tab-login");
  const registerTab = document.getElementById("tab-register");
  const loginPanel    = document.getElementById("panel-login");
  const registerPanel = document.getElementById("panel-register");
  if (!loginTab || !registerTab) return;

  if (tab === "login") {
    loginTab.classList.add("active");     loginTab.setAttribute("aria-selected", "true");
    registerTab.classList.remove("active"); registerTab.setAttribute("aria-selected", "false");
    loginPanel.classList.add("active");
    registerPanel.classList.remove("active");
  } else {
    registerTab.classList.add("active");   registerTab.setAttribute("aria-selected", "true");
    loginTab.classList.remove("active");   loginTab.setAttribute("aria-selected", "false");
    registerPanel.classList.add("active");
    loginPanel.classList.remove("active");
  }
}

// Generic alert renderer for modal
function showModalAlert(msg, isSuccess, elementId) {
  const el = document.getElementById(elementId);
  if (!el) return;
  el.textContent = msg;
  el.className = "alert-message";
  el.removeAttribute("style");

  if (isSuccess === true) {
    el.classList.add("alert-success");
    el.style.display = "block";
  } else if (isSuccess === false) {
    el.classList.add("alert-error");
    el.style.display = "block";
  } else {
    // Loading / neutral
    el.style.display = "block";
    el.style.backgroundColor = "#f1f5f9";
    el.style.color = "#334155";
    el.style.border = "1px solid #cbd5e1";
  }
}

// Login form inside the modal
function initModalLoginForm() {
  const form = document.getElementById("modal-loginForm");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usernameOrEmail = document.getElementById("modal-usernameOrEmail").value.trim();
    const password        = document.getElementById("modal-password").value;

    if (!usernameOrEmail || !password) {
      showModalAlert("Vui lòng điền đầy đủ tên đăng nhập và mật khẩu.", false, "modal-login-alert");
      return;
    }

    try {
      showModalAlert("Đang đăng nhập...", null, "modal-login-alert");

      const response = await fetch("/api/auth/login", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ usernameOrEmail, password })
      });

      const data = await response.json();

      if (response.ok && data.token) {
        localStorage.setItem("token",    data.token);
        localStorage.setItem("username", data.username);
        localStorage.setItem("fullName", data.fullName);
        localStorage.setItem("role",     data.role);
        localStorage.setItem("email",    data.email);

        showModalAlert("Đăng nhập thành công! Đang chuyển hướng...", true, "modal-login-alert");

        setTimeout(() => {
          if (data.role === "ROLE_ADMIN") {
            window.location.href = "admin.html";
          } else if (data.role === "Team_Member" || data.role === "ROLE_MEMBER") {
            window.location.href = "member-contact.html";
          } else {
            const redirectAttempt = localStorage.getItem("redirectAttempt");
            if (redirectAttempt) {
              localStorage.removeItem("redirectAttempt");
              window.location.href = redirectAttempt;
            } else {
              window.location.reload();
            }
          }
        }, 1000);
      } else {
        showModalAlert(data.message || "Đăng nhập thất bại. Kiểm tra lại thông tin.", false, "modal-login-alert");
      }
    } catch (error) {
      console.error("Modal login error:", error);
      showModalAlert("Không thể kết nối đến máy chủ. Vui lòng thử lại.", false, "modal-login-alert");
    }
  });
}

// Register form inside the modal
function initModalRegisterForm() {
  const form = document.getElementById("modal-registerForm");
  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const username = document.getElementById("modal-username").value.trim();
    const fullName = document.getElementById("modal-fullName").value.trim();
    const email    = document.getElementById("modal-email").value.trim();
    const phone    = document.getElementById("modal-phone").value.trim();
    const password = document.getElementById("modal-reg-password").value;

    if (!username || !fullName || !email || !password) {
      showModalAlert("Vui lòng điền các trường bắt buộc.", false, "modal-register-alert");
      return;
    }

    try {
      showModalAlert("Đang đăng ký...", null, "modal-register-alert");

      const response = await fetch("/api/auth/register", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ username, fullName, email, phone, password })
      });

      const data = await response.json();

      if (response.ok && data.success) {
        showModalAlert("Đăng ký thành công! Hãy đăng nhập ngay.", true, "modal-register-alert");
        setTimeout(() => {
          switchAuthTab("login");
          setTimeout(() => {
            showModalAlert("Tài khoản đã được tạo! Hãy đăng nhập.", true, "modal-login-alert");
          }, 80);
        }, 1400);
      } else {
        showModalAlert(
          data.message || "Đăng ký thất bại. Tên đăng nhập hoặc Email có thể đã tồn tại.",
          false, "modal-register-alert"
        );
      }
    } catch (error) {
      console.error("Modal register error:", error);
      showModalAlert("Không thể kết nối đến máy chủ. Vui lòng thử lại.", false, "modal-register-alert");
    }
  });
}

// =============================================
//  Authentication & Route Guard
// =============================================

function checkRouteGuard() {
  const path = window.location.pathname;
  const page = path.substring(path.lastIndexOf('/') + 1) || "index.html";

  const token = localStorage.getItem("token");
  const role  = localStorage.getItem("role");

  // Admin MUST stay in admin.html and cannot access any other page
  if (token && role === "ROLE_ADMIN") {
    if (page !== "admin.html") {
      window.location.href = "admin.html";
      return;
    }
  }

  // Member MUST stay in member-contact.html and cannot access any other page
  if (token && role === "ROLE_MEMBER") {
    if (page !== "member-contact.html") {
      window.location.href = "member-contact.html";
      return;
    }
  }

  // Protected client pages
  const protectedPages = ["services.html", "about.html", "portfolio.html", "contact.html"];

  if (protectedPages.includes(page) && !token) {
    localStorage.setItem("redirectAttempt", page);
    window.location.href = "index.html?error=unauthorized#login";
    return;
  }

  // Admin dashboard guard
  if (page === "admin.html") {
    if (!token || role !== "ROLE_ADMIN") {
      window.location.href = "index.html";
    }
  }

  // Member contact page guard
  if (page === "member-contact.html") {
    if (!token || role !== "ROLE_MEMBER") {
      window.location.href = "index.html";
    }
  }
}

// Highlight Active Nav Item
function highlightActiveLink() {
  const path = window.location.pathname;
  const page = path.substring(path.lastIndexOf('/') + 1) || "index.html";
  const navLinks = document.querySelectorAll(".nav-links a");

  navLinks.forEach(link => {
    const href = link.getAttribute("href");
    if (href === page) {
      link.classList.add("active");
    } else {
      link.classList.remove("active");
    }
  });
}

// Dynamic Navbar authentication update
function updateNavbarAuth() {
  const navLinksContainer = document.querySelector(".nav-links");
  if (!navLinksContainer) return;

  const token    = localStorage.getItem("token");
  const role     = localStorage.getItem("role");
  const fullName = localStorage.getItem("fullName");

  // If Admin, hide all standard navigation links (Home, Services, etc.)
  if (token && role === "ROLE_ADMIN") {
    navLinksContainer.querySelectorAll("li").forEach(li => {
      // Hide standard links. Dynamic auth-items (Dashboard, Logout) will be added back later.
      if (!li.classList.contains("auth-item")) {
        li.style.display = "none";
      }
    });
    // Also hide the logo link to homepage or change it
    const logo = document.getElementById("header-logo");
    if (logo) {
      logo.setAttribute("href", "admin.html");
      logo.onclick = (e) => { e.preventDefault(); }; // Disable clicking logo to go anywhere
      logo.style.cursor = "default";
    }
  } else {
    navLinksContainer.querySelectorAll("li").forEach(li => {
      li.style.display = "";
    });
    const logo = document.getElementById("header-logo");
    if (logo) {
      logo.setAttribute("href", "index.html");
      logo.onclick = null;
      logo.style.cursor = "pointer";
    }
  }

  // Remove any previously-injected auth items
  navLinksContainer.querySelectorAll(".auth-item").forEach(item => item.remove());

  // Remove the default nav-btn from the HTML (will be replaced below)
  const defaultBtn = navLinksContainer.querySelector(".nav-btn");
  if (defaultBtn && defaultBtn.parentElement) {
    defaultBtn.parentElement.remove();
  }

  if (token) {
    // — Admin Dashboard link —
    if (role === "ROLE_ADMIN") {
      const li = document.createElement("li");
      li.className = "auth-item";
      li.innerHTML = `<a href="admin.html">Dashboard</a>`;
      navLinksContainer.appendChild(li);
    } else if (role === "Team_Member" || role === "ROLE_MEMBER") {
      const li = document.createElement("li");
      li.className = "auth-item";
      li.innerHTML = `<a href="member-contact.html">Member Portal</a>`;
      navLinksContainer.appendChild(li);
    }

    // — Greeting —
    const greetLi = document.createElement("li");
    greetLi.className = "auth-item";
    greetLi.innerHTML = `<span style="font-weight:600;font-size:0.95rem;color:var(--text-muted);">Hi, ${escapeHtml(fullName)}</span>`;
    navLinksContainer.appendChild(greetLi);

    // — Logout button —
    const logoutLi = document.createElement("li");
    logoutLi.className = "auth-item";
    logoutLi.innerHTML = `<a href="#" id="logout-btn" class="nav-btn"
      style="background-color:#ef4444;border-color:#ef4444;box-shadow:0 4px 14px rgba(239,68,68,0.25);">Logout</a>`;
    navLinksContainer.appendChild(logoutLi);

    document.getElementById("logout-btn").addEventListener("click", (e) => {
      e.preventDefault();
      logoutUser();
    });
  } else {
    // — Guest: "Get Started Now" button → opens auth modal —
    const li = document.createElement("li");
    li.className = "auth-item";
    li.innerHTML = `<a href="#" id="get-started-btn" class="nav-btn">Get Started Now</a>`;
    navLinksContainer.appendChild(li);

    document.getElementById("get-started-btn").addEventListener("click", (e) => {
      e.preventDefault();
      openAuthModal("login");
    });
  }
}

// User Logout Logic
function logoutUser() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("fullName");
  localStorage.removeItem("role");
  window.location.href = "index.html";
}

// =============================================
//  Standalone Page Forms (login.html / register.html)
// =============================================

function initLoginForm() {
  const form     = document.getElementById("loginForm");
  const alertMsg = document.getElementById("alertMessage");
  if (!form || !alertMsg) return;

  // Show query-param message
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get("error") === "unauthorized") {
    showAlert("Bạn cần đăng nhập để truy cập tính năng này.", false);
  } else if (urlParams.get("registered") === "true") {
    showAlert("Đăng ký tài khoản thành công! Hãy đăng nhập.", true);
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usernameOrEmail = document.getElementById("usernameOrEmail").value.trim();
    const password        = document.getElementById("password").value;

    if (!usernameOrEmail || !password) {
      showAlert("Vui lòng điền đầy đủ tên đăng nhập và mật khẩu.", false);
      return;
    }

    try {
      showAlert("Đang đăng nhập...", null);

      const response = await fetch("/api/auth/login", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ usernameOrEmail, password })
      });

      const data = await response.json();

      if (response.ok && data.token) {
        localStorage.setItem("token",    data.token);
        localStorage.setItem("username", data.username);
        localStorage.setItem("fullName", data.fullName);
        localStorage.setItem("role",     data.role);
        localStorage.setItem("email",    data.email);

        showAlert("Đăng nhập thành công! Đang chuyển hướng...", true);

        setTimeout(() => {
          if (data.role === "ROLE_ADMIN") {
            window.location.href = "admin.html";
          } else if (data.role === "Team_Member" || data.role === "ROLE_MEMBER") {
            window.location.href = "member-contact.html";
          } else {
            const redirect = localStorage.getItem("redirectAttempt");
            if (redirect) {
              localStorage.removeItem("redirectAttempt");
              window.location.href = redirect;
            } else {
              window.location.href = "index.html";
            }
          }
        }, 1000);
      } else {
        showAlert(data.message || "Đăng nhập thất bại. Kiểm tra lại thông tin.", false);
      }
    } catch (error) {
      console.error("Login error:", error);
      showAlert("Không thể kết nối đến máy chủ. Vui lòng thử lại.", false);
    }
  });

  function showAlert(msg, isSuccess) {
    alertMsg.textContent = msg;
    alertMsg.className = "alert-message";
    if (isSuccess === true) {
      alertMsg.classList.add("alert-success"); alertMsg.style.display = "block";
    } else if (isSuccess === false) {
      alertMsg.classList.add("alert-error"); alertMsg.style.display = "block";
    } else {
      alertMsg.style.display = "block";
      alertMsg.style.backgroundColor = "#f1f5f9";
      alertMsg.style.color = "#334155";
      alertMsg.style.border = "1px solid #cbd5e1";
    }
  }
}

function initRegisterForm() {
  const form     = document.getElementById("registerForm");
  const alertMsg = document.getElementById("alertMessage");
  if (!form || !alertMsg) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const fullName = document.getElementById("fullName").value.trim();
    const email    = document.getElementById("email").value.trim();
    const phone    = document.getElementById("phone").value.trim();
    const password = document.getElementById("password").value;

    if (!username || !fullName || !email || !password) {
      showAlert("Vui lòng điền các trường bắt buộc.", false);
      return;
    }

    try {
      showAlert("Đang đăng ký...", null);

      const response = await fetch("/api/auth/register", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ username, fullName, email, phone, password })
      });

      const data = await response.json();

      if (response.ok && data.success) {
        showAlert("Đăng ký thành công! Đang chuyển đến trang đăng nhập...", true);
        setTimeout(() => { window.location.href = "login.html?registered=true"; }, 1500);
      } else {
        showAlert(data.message || "Đăng ký thất bại. Tên đăng nhập hoặc Email có thể đã tồn tại.", false);
      }
    } catch (error) {
      console.error("Registration error:", error);
      showAlert("Không thể kết nối đến máy chủ. Vui lòng thử lại.", false);
    }
  });

  function showAlert(msg, isSuccess) {
    alertMsg.textContent = msg;
    alertMsg.className = "alert-message";
    if (isSuccess === true) {
      alertMsg.classList.add("alert-success"); alertMsg.style.display = "block";
    } else if (isSuccess === false) {
      alertMsg.classList.add("alert-error"); alertMsg.style.display = "block";
    } else {
      alertMsg.style.display = "block";
      alertMsg.style.backgroundColor = "#f1f5f9";
      alertMsg.style.color = "#334155";
      alertMsg.style.border = "1px solid #cbd5e1";
    }
  }
}

// =============================================
//  Admin Dashboard
// =============================================

function initAdminDashboard() {
  // Logout button in sidebar
  const logoutBtn = document.getElementById("admin-logout-btn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", (e) => { e.preventDefault(); logoutUser(); });
  }

  // Close CRUD modal on overlay click
  const crudOverlay = document.getElementById("crud-modal-overlay");
  if (crudOverlay) {
    crudOverlay.addEventListener("click", (e) => {
      if (e.target === crudOverlay) closeCrudModal();
    });
  }

  // Close confirm modal on overlay click
  const confirmOverlay = document.getElementById("confirm-modal-overlay");
  if (confirmOverlay) {
    confirmOverlay.addEventListener("click", (e) => {
      if (e.target === confirmOverlay) closeConfirmModal();
    });
  }

  // Bind the confirm-delete action button
  const confirmDeleteBtn = document.getElementById("btn-confirm-delete-action");
  if (confirmDeleteBtn) {
    confirmDeleteBtn.addEventListener("click", confirmDelete);
  }

  // Close modals on Escape
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      closeCrudModal();
      closeConfirmModal();
    }
  });

  // Load all data
  fetchAdminContacts();
  fetchAdminUsers();
  fetchAdminMembersTable();
  fetchAdminProjectsTable();
  fetchAdminServicesTable();
}

// =============================================
//  Admin – Panel Switching
// =============================================

function switchAdminPanel(panelName, el) {
  // Deactivate all sidebar links
  document.querySelectorAll(".sidebar-nav a").forEach(a => a.classList.remove("active"));
  if (el) el.classList.add("active");

  // Hide all panels, show target
  document.querySelectorAll(".admin-panel").forEach(p => p.classList.remove("active"));
  const panel = document.getElementById("panel-" + panelName);
  if (panel) panel.classList.add("active");
}

// =============================================
//  Admin – Auth Helpers
// =============================================

function getAdminToken() {
  return localStorage.getItem("token") || "";
}

function adminHeaders() {
  return {
    "Content-Type": "application/json",
    "Authorization": "Bearer " + getAdminToken()
  };
}

// =============================================
//  Admin – Table Filter
// =============================================

function filterTable(tbodyId, query) {
  const tbody = document.getElementById(tbodyId);
  if (!tbody) return;
  const q = query.toLowerCase();
  tbody.querySelectorAll("tr[data-searchable]").forEach(row => {
    const text = (row.getAttribute("data-searchable") || "").toLowerCase();
    row.style.display = text.includes(q) ? "" : "none";
  });
}

// =============================================
//  Admin – CRUD Modal State
// =============================================

let _crudState = { type: null, item: null };
let _deleteState = { type: null, id: null };

// Cache for loaded data (used to pass objects to modal)
const _cache = { users: {}, members: {}, projects: {}, services: {} };

function openCrudModal(type, id) {
  const item = id !== null ? (_cache[type + "s"] || _cache[type])[id] : null;
  _crudState = { type, item };

  const overlay = document.getElementById("crud-modal-overlay");
  const title   = document.getElementById("crud-modal-title");
  const body    = document.getElementById("crud-modal-body");
  const alert   = document.getElementById("crud-alert");
  if (!overlay) return;

  if (alert) { alert.style.display = "none"; alert.textContent = ""; alert.className = "crud-alert alert-message"; }

  const labels = { user: "User", member: "Member", project: "Project", service: "Service" };
  title.textContent = item ? `Edit ${labels[type]}` : `Add New ${labels[type]}`;
  body.innerHTML = buildCrudForm(type, item);

  // Bind file upload trigger for projects or members
  if (type === "project" || type === "member") {
    const fileInputId = type === "project" ? "cf-imageFile" : "cf-avatarFile";
    const urlInputId = type === "project" ? "cf-imageUrl" : "cf-avatarUrl";
    const fileInput = document.getElementById(fileInputId);
    if (fileInput) {
      fileInput.addEventListener("change", async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        
        const formData = new FormData();
        formData.append("file", file);
        
        try {
          showCrudAlert("Đang tải ảnh lên...", null);
          const res = await fetch("/api/upload", {
            method: "POST",
            body: formData
          });
          const data = await res.json().catch(() => ({}));
          if (res.ok && data.url) {
            document.getElementById(urlInputId).value = data.url;
            const preview = document.getElementById("cf-preview");
            if (preview) {
              preview.src = data.url;
              preview.style.display = "block";
            }
            showCrudAlert("✅ Tải ảnh lên thành công!", true);
          } else {
            showCrudAlert(data.message || "Tải ảnh lên thất bại.", false);
          }
        } catch (err) {
          console.error("Upload error:", err);
          showCrudAlert("Không thể tải ảnh lên máy chủ.", false);
        }
      });
    }
  }

  overlay.classList.add("is-open");
  document.body.style.overflow = "hidden";
}

function closeCrudModal() {
  const overlay = document.getElementById("crud-modal-overlay");
  if (overlay) overlay.classList.remove("is-open");
  document.body.style.overflow = "";
  _crudState = { type: null, item: null };
}

// =============================================
//  Admin – Form Builder
// =============================================

function buildCrudForm(type, item) {
  const v   = item || {};
  const fld = (id, label, type2, value, extra = "") => `
    <div class="form-group">
      <label for="${id}">${label}</label>
      <input type="${type2}" id="${id}" value="${escapeHtml(String(value || ""))}" ${extra}>
    </div>`;
  const txt = (id, label, value, extra = "") => `
    <div class="form-group">
      <label for="${id}">${label}</label>
      <textarea id="${id}" rows="3" ${extra}>${escapeHtml(String(value || ""))}</textarea>
    </div>`;
  const sel = (id, label, opts, selected) => `
    <div class="form-group">
      <label for="${id}">${label}</label>
      <select id="${id}" class="crud-select">
        ${opts.map(([val, lbl]) => `<option value="${val}" ${selected === val ? "selected" : ""}>${lbl}</option>`).join("")}
      </select>
    </div>`;

  if (type === "user") return `
    ${fld("cf-username",  "Tên đăng nhập *", "text",  v.username, `placeholder="Nhập tên đăng nhập" required ${item ? 'readonly style="background:#f8fafc;cursor:not-allowed;"' : ""}`)}
    ${fld("cf-fullName",  "Họ và tên *",     "text",  v.fullName, 'placeholder="Nhập họ và tên" required')}
    ${fld("cf-email",     "Email *",          "email", v.email,    'placeholder="name@domain.com" required')}
    ${fld("cf-phone",     "Số điện thoại",   "tel",   v.phone,    'placeholder="0123456789" pattern="[0-9]{10}"')}
    ${!item ? fld("cf-password", "Mật khẩu *", "password", "", 'placeholder="Tối thiểu 6 ký tự" required minlength="6"') : ""}
    ${sel("cf-role", "Vai trò *", [["ROLE_USER","User"],["ROLE_ADMIN","Admin"],["Team_Member","Team Member"]], v.role || "ROLE_USER")}
    <div class="form-group" style="width: 100%;">
      <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer; justify-content: flex-start;">
        <input type="checkbox" id="cf-enabled" ${v.enabled !== false ? 'checked' : ''}>
        <span>Trạng thái hoạt động</span>
      </label>
    </div>
  `;

  if (type === "member") return `
    ${fld("cf-name",       "Tên thành viên *",         "text", v.name,        'placeholder="Nguyễn Văn A" required')}
    ${fld("cf-role",       "Chức vụ / Vị trí *",       "text", v.role,        'placeholder="Frontend Developer" required')}
    <div class="form-group">
      <label for="cf-avatarFile">Ảnh đại diện *</label>
      <input type="file" id="cf-avatarFile" accept="image/*" style="width:100%; padding:0.5rem; border:1px dashed var(--border-color); border-radius:var(--radius-sm); background:var(--bg-light); cursor:pointer;">
      <input type="hidden" id="cf-avatarUrl" value="${escapeHtml(String(v.avatarUrl || ""))}">
      ${v.avatarUrl ? `<img id="cf-preview" src="${escapeHtml(v.avatarUrl)}" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: block;">` : `<img id="cf-preview" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: none;">`}
    </div>
    ${fld("cf-facebookUrl","Facebook URL",               "url",  v.facebookUrl, 'placeholder="https://facebook.com/..."')}
    ${fld("cf-githubUrl",  "GitHub URL",                 "url",  v.githubUrl,   'placeholder="https://github.com/..."')}
    ${fld("cf-linkedinUrl","LinkedIn URL",               "url",  v.linkedinUrl, 'placeholder="https://linkedin.com/in/..."')}
  `;

  if (type === "project") return `
    ${fld("cf-title",       "Tên dự án *",   "text", v.title,       'placeholder="Nhập tên dự án" required')}
    ${fld("cf-category",    "Danh mục *",    "text", v.category,    'placeholder="Web Development" required')}
    <div class="form-group">
      <label for="cf-imageFile">Ảnh bìa *</label>
      <input type="file" id="cf-imageFile" accept="image/*" style="width:100%; padding:0.5rem; border:1px dashed var(--border-color); border-radius:var(--radius-sm); background:var(--bg-light); cursor:pointer;">
      <input type="hidden" id="cf-imageUrl" value="${escapeHtml(String(v.imageUrl || ""))}">
      ${v.imageUrl ? `<img id="cf-preview" src="${escapeHtml(v.imageUrl)}" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: block;">` : `<img id="cf-preview" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: none;">`}
    </div>
    ${txt("cf-description", "Mô tả *",               v.description, 'placeholder="Mô tả dự án..." required')}
    ${txt("cf-technologies", "Công nghệ sử dụng", v.technologies, 'placeholder="React, Node.js, MongoDB..."')}
  `;

  if (type === "service") return `
    ${fld("cf-title", "Tên dịch vụ *", "text", v.title, 'placeholder="Nhập tên dịch vụ" required')}
    ${sel("cf-iconUrl", "Loại Icon *",
      [["web","🌐 Web Design"],["design","🎨 UI/UX Design"],["marketing","📊 Marketing"],
       ["mobile","📱 Mobile App"],["branding","🎯 Branding"],["cloud","☁️ Cloud Solutions"]],
      v.iconUrl || "web")}
    ${txt("cf-description", "Mô tả *", v.description, 'placeholder="Mô tả dịch vụ..." required')}
  `;

  return "<p>Unknown type.</p>";
}

// =============================================
//  Admin – CRUD Submit
// =============================================

async function submitCrudForm() {
  const { type, item } = _crudState;
  if (!type) return;

  const isEdit = !!item;
  let payload  = {};
  let valid    = true;

  const g = id => (document.getElementById(id)?.value || "").trim();
  const gv = id => document.getElementById(id)?.value || "";

  if (type === "user") {
    payload = { username: g("cf-username"), fullName: g("cf-fullName"), email: g("cf-email"),
                phone: g("cf-phone"), role: gv("cf-role"), enabled: document.getElementById("cf-enabled")?.checked };
    if (!isEdit) payload.password = gv("cf-password");
    if (!payload.username || !payload.fullName || !payload.email) valid = false;
  }

  if (type === "member") {
    payload = { name: g("cf-name"), role: g("cf-role"), avatarUrl: g("cf-avatarUrl"),
                facebookUrl: g("cf-facebookUrl"), githubUrl: g("cf-githubUrl"), linkedinUrl: g("cf-linkedinUrl") };
    if (!payload.name || !payload.role || !payload.avatarUrl) valid = false;
  }

  if (type === "project") {
    payload = { title: g("cf-title"), category: g("cf-category"), imageUrl: g("cf-imageUrl"),
                description: g("cf-description"), technologies: g("cf-technologies") };
    if (!payload.title || !payload.category || !payload.imageUrl || !payload.description) valid = false;
  }

  if (type === "service") {
    payload = { title: g("cf-title"), iconUrl: gv("cf-iconUrl"), description: g("cf-description") };
    if (!payload.title || !payload.description) valid = false;
  }

  if (!valid) { showCrudAlert("Vui lòng điền đầy đủ các trường bắt buộc (*)", false); return; }

  const eps = { user: "/api/admin/users", member: "/api/members",
                project: "/api/projects", service: "/api/services" };

  const url    = isEdit ? `${eps[type]}/${item.id}` : eps[type];
  const method = isEdit ? "PUT" : "POST";

  try {
    showCrudAlert("Đang xử lý...", null);
    const response = await fetch(url, { method, headers: adminHeaders(), body: JSON.stringify(payload) });
    const data = await response.json().catch(() => ({}));

    if (response.ok) {
      showCrudAlert(isEdit ? "✅ Cập nhật thành công!" : "✅ Thêm mới thành công!", true);
      setTimeout(() => {
        closeCrudModal();
        if (type === "user")    fetchAdminUsers();
        if (type === "member")  fetchAdminMembersTable();
        if (type === "project") fetchAdminProjectsTable();
        if (type === "service") fetchAdminServicesTable();
      }, 700);
    } else {
      showCrudAlert(data.message || "Thao tác thất bại. Vui lòng thử lại.", false);
    }
  } catch (err) {
    console.error("CRUD submit error:", err);
    showCrudAlert("Không thể kết nối đến máy chủ.", false);
  }
}

function showCrudAlert(msg, isSuccess) {
  const el = document.getElementById("crud-alert");
  if (!el) return;
  el.textContent = msg;
  el.className = "crud-alert alert-message";
  el.removeAttribute("style");
  if (isSuccess === true)       { el.classList.add("alert-success"); el.style.display = "block"; }
  else if (isSuccess === false) { el.classList.add("alert-error");   el.style.display = "block"; }
  else { el.style.cssText = "display:block;background:#f1f5f9;color:#334155;border:1px solid #cbd5e1;"; }
}

// =============================================
//  Admin – Confirm Delete
// =============================================

function openDeleteConfirm(type, id, name) {
  _deleteState = { type, id };
  const overlay = document.getElementById("confirm-modal-overlay");
  const text    = document.getElementById("confirm-modal-text");
  if (text) text.textContent = `Bạn có chắc muốn xóa "${name}"? Hành động này không thể hoàn tác.`;
  if (overlay) overlay.classList.add("is-open");
  document.body.style.overflow = "hidden";
}

function closeConfirmModal() {
  const overlay = document.getElementById("confirm-modal-overlay");
  if (overlay) overlay.classList.remove("is-open");
  document.body.style.overflow = "";
  _deleteState = { type: null, id: null };
}

async function confirmDelete() {
  const { type, id } = _deleteState;
  if (!type || id === null) return;

  const eps = { user: "/api/admin/users", member: "/api/members",
                project: "/api/projects", service: "/api/services" };
  try {
    const response = await fetch(`${eps[type]}/${id}`, { method: "DELETE", headers: adminHeaders() });
    if (response.ok) {
      closeConfirmModal();
      if (type === "user")    fetchAdminUsers();
      if (type === "member")  fetchAdminMembersTable();
      if (type === "project") fetchAdminProjectsTable();
      if (type === "service") fetchAdminServicesTable();
    } else {
      alert("Xóa thất bại. Vui lòng thử lại.");
    }
  } catch (err) {
    console.error("Delete error:", err);
    alert("Không thể kết nối đến máy chủ.");
  }
}

// =============================================
//  Admin – Table Renderers
// =============================================

async function fetchAdminUsers() {
  const tbody     = document.getElementById("users-table-body");
  const statCount = document.getElementById("stat-users-count");
  if (!tbody) return;

  try {
    const response = await fetch("/api/admin/users", { headers: adminHeaders() });
    if (!response.ok) throw new Error("Failed");
    const users = await response.json();

    if (statCount) statCount.textContent = users.length;
    tbody.innerHTML = "";

    if (!users.length) {
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:2rem;color:var(--text-muted);">Chưa có user nào.</td></tr>`;
      return;
    }

    users.forEach(u => {
      _cache.users[u.id] = u;
      const tr = document.createElement("tr");
      tr.setAttribute("data-searchable", `${u.fullName} ${u.username} ${u.email}`);
      const initials = (u.fullName || "?")[0].toUpperCase();
      
      // Check if user is online (last login within last 5 minutes)
      let isOnline = false;
      let lastLoginText = "—";
      if (u.lastLogin) {
        const lastLogin = new Date(u.lastLogin);
        const now = new Date();
        const diffMinutes = (now - lastLogin) / (1000 * 60);
        isOnline = diffMinutes < 5;
        lastLoginText = lastLogin.toLocaleString("vi-VN");
      }
      
      tr.innerHTML = `
        <td>
          <div class="table-user-cell">
            <div class="user-initials">${initials}</div>
            <div><div class="text-dark-inline">${escapeHtml(u.fullName || "")}</div></div>
          </div>
        </td>
        <td>${escapeHtml(u.username || "")}</td>
        <td>${escapeHtml(u.email || "")}</td>
        <td>${escapeHtml(u.phone || "—")}</td>
        <td><span class="status-badge ${u.role === "ROLE_ADMIN" ? "badge-admin" : (u.role === "Team_Member" ? "badge-member" : "badge-user")}">${u.role === "ROLE_ADMIN" ? "Admin" : (u.role === "Team_Member" ? "Team Member" : "User")}</span></td>
        <td>
          <button class="btn-toggle-status" onclick="toggleUserStatus(${u.id})" style="padding: 4px 12px; border-radius: 20px; border: none; cursor: pointer; font-weight: 600; font-size: 12px; white-space: nowrap; ${u.enabled ? 'background: #ecfdf5; color: #059669;' : 'background: #fef2f2; color: #dc2626;'}">
            ${u.enabled ? 'Hoạt động' : 'Vô hiệu'}
          </button>
        </td>
        <td style="min-width: 140px;">
          <span class="status-badge ${isOnline ? 'badge-online' : 'badge-offline'}">
            ${isOnline ? 'Online' : 'Offline'}
          </span>
          <br><small style="color: #64748b; font-size: 11px; white-space: nowrap;">${lastLoginText}</small>
        </td>
        <td>
          <div class="action-btns">
            <button class="btn-edit"   onclick="openCrudModal('user', ${u.id})">
              <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>Edit
            </button>
            <button class="btn-delete" onclick="openDeleteConfirm('user', ${u.id}, '${escapeHtml(u.fullName || "")}')">
              <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>Delete
            </button>
          </div>
        </td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error("fetchAdminUsers error:", err);
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:2rem;color:#ef4444;">Không thể tải danh sách user.</td></tr>`;
  }
}

async function toggleUserStatus(userId) {
  const user = _cache.users[userId];
  if (!user) return;

  try {
    // Get current user data to preserve all fields except enabled
    const newStatus = !user.enabled;
    const response = await fetch(`/api/admin/users/${userId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({
        enabled: newStatus
      })
    });

    if (response.ok) {
      // Update local cache
      _cache.users[userId].enabled = newStatus;
      // Refresh table
      fetchAdminUsers();
    } else {
      alert("Thao tác thất bại. Vui lòng thử lại.");
    }
  } catch (err) {
    console.error("Toggle status error:", err);
    alert("Không thể kết nối đến máy chủ.");
  }
}

async function fetchAdminMembersTable() {
  const tbody     = document.getElementById("members-table-body");
  const statCount = document.getElementById("stat-members-count");
  if (!tbody) return;

  try {
    const response = await fetch("/api/members");
    if (!response.ok) throw new Error("Failed");
    const members = await response.json();

    if (statCount) statCount.textContent = members.length;
    tbody.innerHTML = "";

    if (!members.length) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:var(--text-muted);">Chưa có thành viên nào.</td></tr>`;
      return;
    }

    members.forEach(m => {
      _cache.members[m.id] = m;
      const tr = document.createElement("tr");
      tr.setAttribute("data-searchable", `${m.name} ${m.role}`);
      const mkLink = url => url ? `<a href="${escapeHtml(url)}" target="_blank" style="color:var(--primary);font-weight:600;">🔗</a>` : "—";
      tr.innerHTML = `
        <td><img src="${escapeHtml(m.avatarUrl || "")}" alt="${escapeHtml(m.name || "")}" class="table-avatar"
              onerror="this.src='https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=60&h=60'"></td>
        <td class="text-dark-inline">${escapeHtml(m.name || "")}</td>
        <td><span class="status-badge badge-active">${escapeHtml(m.role || "")}</span></td>
        <td>${mkLink(m.facebookUrl)}</td>
        <td>${mkLink(m.githubUrl)}</td>
        <td>${mkLink(m.linkedinUrl)}</td>
        <td>
          <div class="action-btns">
            <button class="btn-edit"   onclick="openCrudModal('member', ${m.id})">
              <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>Edit
            </button>
            <button class="btn-delete" onclick="openDeleteConfirm('member', ${m.id}, '${escapeHtml(m.name || "")}')">
              <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>Delete
            </button>
          </div>
        </td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error("fetchAdminMembersTable error:", err);
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:#ef4444;">Không thể tải danh sách thành viên.</td></tr>`;
  }
}

async function fetchAdminProjectsTable() {
  const tbody     = document.getElementById("projects-table-body");
  const statCount = document.getElementById("stat-projects-count");
  if (!tbody) return;

  try {
    const response = await fetch("/api/projects");
    if (!response.ok) throw new Error("Failed");
    const projects = await response.json();

    if (statCount) statCount.textContent = projects.length;
    tbody.innerHTML = "";

    if (!projects.length) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">Chưa có dự án nào.</td></tr>`;
      return;
    }

    projects.forEach(p => {
      _cache.projects[p.id] = p;
      const tr = document.createElement("tr");
      tr.setAttribute("data-searchable", `${p.title} ${p.category} ${p.description}`);
      tr.innerHTML = `
        <td><img src="${escapeHtml(p.imageUrl || "")}" alt="${escapeHtml(p.title || "")}"
              style="width:78px;height:48px;object-fit:cover;border-radius:6px;border:1px solid var(--border-color);"
              onerror="this.src='https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=78&h=48'"></td>
        <td class="text-dark-inline">${escapeHtml(p.title || "")}</td>
        <td><span class="status-badge badge-category">${escapeHtml(p.category || "")}</span></td>
        <td style="max-width:220px;white-space:pre-wrap;">${escapeHtml((p.description || "").substring(0, 100))}${(p.description || "").length > 100 ? "..." : ""}</td>
        <td>
          <div class="action-btns">
            <button class="btn-edit"   onclick="openCrudModal('project', ${p.id})">
              <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>Edit
            </button>
            <button class="btn-delete" onclick="openDeleteConfirm('project', ${p.id}, '${escapeHtml(p.title || "")}')">
              <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>Delete
            </button>
          </div>
        </td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error("fetchAdminProjectsTable error:", err);
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:#ef4444;">Không thể tải danh sách dự án.</td></tr>`;
  }
}

async function fetchAdminServicesTable() {
  const tbody = document.getElementById("services-table-body");
  if (!tbody) return;

  try {
    const response = await fetch("/api/services");
    if (!response.ok) throw new Error("Failed");
    const services = await response.json();

    tbody.innerHTML = "";

    if (!services.length) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:2rem;color:var(--text-muted);">Chưa có dịch vụ nào.</td></tr>`;
      return;
    }

    const iconLabels = { web: "🌐 Web Design", design: "🎨 UI/UX", marketing: "📊 Marketing",
                         mobile: "📱 Mobile", branding: "🎯 Branding", cloud: "☁️ Cloud" };

    services.forEach(s => {
      _cache.services[s.id] = s;
      const tr = document.createElement("tr");
      tr.setAttribute("data-searchable", `${s.title} ${s.description}`);
      tr.innerHTML = `
        <td class="text-dark-inline">${escapeHtml(s.title || "")}</td>
        <td><span class="status-badge badge-active">${iconLabels[s.iconUrl] || escapeHtml(s.iconUrl || "—")}</span></td>
        <td style="max-width:260px;white-space:pre-wrap;">${escapeHtml((s.description || "").substring(0, 100))}${(s.description || "").length > 100 ? "..." : ""}</td>
        <td>
          <div class="action-btns">
            <button class="btn-edit"   onclick="openCrudModal('service', ${s.id})">
              <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>Edit
            </button>
            <button class="btn-delete" onclick="openDeleteConfirm('service', ${s.id}, '${escapeHtml(s.title || "")}')">
              <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>Delete
            </button>
          </div>
        </td>`;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error("fetchAdminServicesTable error:", err);
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:2rem;color:#ef4444;">Không thể tải danh sách dịch vụ.</td></tr>`;
  }
}

async function fetchAdminContacts() {
  const tableBody  = document.getElementById("contacts-table-body");
  const statsCount = document.getElementById("stat-messages-count");
  if (!tableBody) return;

  try {
    const response = await fetch("/api/contacts");
    if (!response.ok) throw new Error("Failed to fetch contact submissions");
    const contacts = await response.json();

    tableBody.innerHTML = "";

    if (contacts.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;">Chưa có lời nhắn liên hệ nào được gửi.</td></tr>`;
      if (statsCount) statsCount.textContent = "0";
      return;
    }

    if (statsCount) statsCount.textContent = contacts.length;

    contacts.forEach(contact => {
      const row  = document.createElement("tr");
      const date = new Date(contact.createdAt).toLocaleDateString("vi-VN", {
        hour: "2-digit", minute: "2-digit",
        day: "2-digit", month: "2-digit", year: "numeric"
      });

      row.innerHTML = `
        <td>
          <div class="text-dark-inline">${escapeHtml(contact.name)}</div>
          <div style="font-size:0.85rem;color:var(--text-muted);">${escapeHtml(contact.email)}</div>
        </td>
        <td class="text-dark-inline">${escapeHtml(contact.title)}</td>
        <td style="max-width:400px;white-space:pre-line;">${escapeHtml(contact.content)}</td>
        <td>${date}</td>
        <td><span class="status-badge ${contact.status === 'DONE' ? 'status-done' : 'status-pending'}">${escapeHtml(contact.status)}</span></td>
      `;
      tableBody.appendChild(row);
    });
  } catch (error) {
    console.error("Error loading admin contacts:", error);
    tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:#ef4444;">Không thể tải danh sách liên hệ. Vui lòng tải lại trang.</td></tr>`;
  }
}

// =============================================
//  Data Fetching – Services / Members / Projects
// =============================================

async function fetchServices() {
  const servicesGrid = document.getElementById("services-grid");
  if (!servicesGrid) return;

  try {
    const response = await fetch("/api/services");
    if (!response.ok) throw new Error("Failed to fetch services");
    const services = await response.json();

    servicesGrid.innerHTML = "";

    const icons = {
      web:       `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>`,
      design:    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"></path><path d="M12 8A4 4 0 1 0 12 16A4 4 0 1 0 12 8Z"></path><path d="M12 2V6"></path><path d="M12 18V22"></path><path d="M4.93 4.93L7.76 7.76"></path><path d="M16.24 16.24L19.07 19.07"></path><path d="M2 12H6"></path><path d="M18 12H22"></path><path d="M4.93 19.07L7.76 16.24"></path><path d="M16.24 7.76L19.07 4.93"></path></svg>`,
      marketing: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>`,
      mobile:    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>`,
      branding:  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><circle cx="12" cy="12" r="6"></circle><circle cx="12" cy="12" r="2"></circle></svg>`,
      cloud:     `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.88 18.04A6 6 0 0 0 6 18a5.5 5.5 0 0 0 .5-10.96 4 4 0 1 0 7.82-1.74l.06-.02a6 6 0 0 0 6.5 12.76z"></path></svg>`
    };

    const accentClasses = {
      web: "service-web", design: "service-design", marketing: "service-marketing",
      mobile: "service-mobile", branding: "service-branding", cloud: "service-cloud"
    };

    services.forEach(service => {
      const card       = document.createElement("div");
      card.className   = "service-card";
      const iconKey    = service.iconUrl || "web";
      const iconSvg    = icons[iconKey] || icons.web;
      const accentClass = accentClasses[iconKey] || accentClasses.web;

      card.innerHTML = `
        <div class="service-icon-wrapper ${accentClass}">${iconSvg}</div>
        <h3>${escapeHtml(service.title)}</h3>
        <p>${escapeHtml(service.description)}</p>
      `;
      servicesGrid.appendChild(card);
    });
  } catch (error) {
    console.error("Error loading services:", error);
    servicesGrid.innerHTML = `<p class="error-msg">Không thể tải danh sách dịch vụ. Vui lòng thử lại sau.</p>`;
  }
}

async function fetchMembers() {
  const teamGrid = document.getElementById("team-grid");
  if (!teamGrid) return;

  try {
    const response = await fetch("/api/members");
    if (!response.ok) throw new Error("Failed to fetch members");
    const members = await response.json();

    teamGrid.innerHTML = "";

    const facebookIcon = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M9 8h-3v4h3v12h5v-12h3.642l.358-4h-4v-1.667c0-.955.192-1.333 1.115-1.333h2.885v-5h-3.808c-3.596 0-5.192 1.583-5.192 4.615v3.385z"/></svg>`;
    const githubIcon   = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>`;
    const linkedinIcon = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-11 19h-3v-11h3v11zm-1.5-12.268c-.966 0-1.75-.779-1.75-1.75s.784-1.75 1.75-1.75 1.75.779 1.75 1.75-.784 1.75-1.75 1.75zm13.5 12.268h-3v-5.604c0-3.368-4-3.113-4 0v5.604h-3v-11h3v1.765c1.396-2.586 7-2.777 7 2.476v6.759z"/></svg>`;

    members.forEach(member => {
      const card      = document.createElement("div");
      card.className  = "member-card";
      const fbLink    = member.facebookUrl ? `<a href="${member.facebookUrl}" target="_blank">${facebookIcon}</a>` : "";
      const ghLink    = member.githubUrl   ? `<a href="${member.githubUrl}"   target="_blank">${githubIcon}</a>`   : "";
      const liLink    = member.linkedinUrl ? `<a href="${member.linkedinUrl}" target="_blank">${linkedinIcon}</a>` : "";

      card.innerHTML = `
        <div class="member-avatar-wrapper">
          <img class="member-avatar" src="${member.avatarUrl}" alt="${escapeHtml(member.name)}"
            onerror="this.src='https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&h=150'">
        </div>
        <h3>${escapeHtml(member.name)}</h3>
        <p class="member-role">${escapeHtml(member.role)}</p>
        <div class="member-socials">${fbLink}${ghLink}${liLink}</div>
      `;
      teamGrid.appendChild(card);
    });
  } catch (error) {
    console.error("Error loading members:", error);
    teamGrid.innerHTML = `<p class="error-msg">Không thể tải danh sách thành viên. Vui lòng thử lại sau.</p>`;
  }
}

function getTechClass(tech) {
  const t = tech.toLowerCase();
  if (t.includes('html') || t.includes('css')) return 'html';
  if (t.includes('javascript') || t.includes('js')) return 'js';
  if (t.includes('react')) return 'react';
  if (t.includes('node')) return 'node';
  if (t.includes('java') || t.includes('spring')) return 'java';
  if (t.includes('sql') || t.includes('database') || t.includes('postgres') || t.includes('mysql')) return 'database';
  return 'default';
}

function openProjectModal(project) {
  const modalOverlay = document.getElementById('project-modal-overlay');
  const modalImage = document.getElementById('project-modal-image');
  const modalCategory = document.getElementById('project-modal-category');
  const modalTitle = document.getElementById('project-modal-title');
  const modalDescription = document.getElementById('project-modal-description');
  const modalTechnologiesWrapper = document.getElementById('project-modal-technologies-wrapper');
  const modalTechnologies = document.getElementById('project-modal-technologies');
  const modalLinkWrapper = document.getElementById('project-modal-link-wrapper');
  const modalLink = document.getElementById('project-modal-link');

  if (!modalOverlay) return;

  modalImage.src = project.imageUrl || 'https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&h=400';
  modalImage.alt = project.title || 'Project Image';
  modalCategory.textContent = project.category || 'Project';
  modalTitle.textContent = project.title || 'Project Title';
  modalDescription.textContent = project.description || 'No description available.';

  if (project.technologies) {
    const techArray = project.technologies.split(',').map(t => t.trim()).filter(t => t);
    modalTechnologies.innerHTML = techArray.map(tech =>
      `<span class="tech-tag ${getTechClass(tech)}">${escapeHtml(tech)}</span>`
    ).join('');
    modalTechnologiesWrapper.style.display = 'block';
  } else {
    modalTechnologiesWrapper.style.display = 'none';
  }

  if (modalLinkWrapper && modalLink) {
    modalLink.href = `https://demo.novadigital.com/${(project.title || '').toLowerCase().replace(/[^a-z0-9]+/g, '-')}`;
    modalLinkWrapper.style.display = 'block';
  }

  modalOverlay.classList.add('is-open');
  document.body.style.overflow = 'hidden';
}

function closeProjectModal() {
  const modalOverlay = document.getElementById('project-modal-overlay');
  if (!modalOverlay) return;
  modalOverlay.classList.remove('is-open');
  document.body.style.overflow = '';
}

let allProjects = [];

function renderFilteredProjects(category) {
    const projectsGrid = document.getElementById("projects-grid");
    if (!projectsGrid) return;

    const filtered = category === "all"
        ? allProjects
        : allProjects.filter(p => (p.category || "").toLowerCase().includes(category.toLowerCase()));

    projectsGrid.innerHTML = "";

    if (filtered.length === 0) {
        projectsGrid.innerHTML = `<p style="text-align: center; grid-column: 1 / -1; color: var(--text-muted); padding: 3rem 0; font-weight: 500;">No projects found in this category.</p>`;
        return;
    }

    filtered.forEach((project, index) => {
        const card = document.createElement("div");
        card.className = "project-card project-card-anim";
        card.style.animationDelay = `${index * 0.06}s`;

        card.innerHTML = `
            <div class="project-image-wrapper">
                <img class="project-image" src="${project.imageUrl}" alt="${escapeHtml(project.title)}"
                    onerror="this.src='https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=500&h=300'">
            </div>
            <div class="project-body">
                <span class="project-category">${escapeHtml(project.category)}</span>
                <h3 class="project-title-clickable">${escapeHtml(project.title)}</h3>
                <p>${escapeHtml(project.description)}</p>
                <div class="project-link">
                    View Details
                    <span class="arrow">➔</span>
                </div>
            </div>
        `;

        const elementsToClick = [
            card.querySelector('.project-title-clickable'),
            card.querySelector('.project-link'),
            card.querySelector('.project-image-wrapper')
        ];
        elementsToClick.forEach(el => {
            if (el) {
                el.addEventListener('click', () => {
                    openProjectModal(project);
                });
            }
        });

        projectsGrid.appendChild(card);
    });
}

async function fetchProjects() {
    const projectsGrid = document.getElementById("projects-grid");
    if (!projectsGrid) return;

    try {
        const response = await fetch("/api/projects");
        if (!response.ok) throw new Error("Failed to fetch projects");
        allProjects = await response.json();

        renderFilteredProjects("all");

        const filterContainer = document.getElementById("project-filters");
        if (filterContainer) {
            const buttons = filterContainer.querySelectorAll(".filter-btn");
            buttons.forEach(btn => {
                btn.addEventListener("click", () => {
                    buttons.forEach(b => b.classList.remove("active"));
                    btn.classList.add("active");
                    renderFilteredProjects(btn.getAttribute("data-filter"));
                });
            });
        }

        const projectModalClose = document.getElementById('project-modal-close');
        const projectModalOverlay = document.getElementById('project-modal-overlay');

        if (projectModalClose) {
            projectModalClose.addEventListener('click', closeProjectModal);
        }

        if (projectModalOverlay) {
            projectModalOverlay.addEventListener('click', (e) => {
                if (e.target === projectModalOverlay) closeProjectModal();
            });
        }

        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') closeProjectModal();
        });
    } catch (error) {
        console.error("Error loading projects:", error);
        projectsGrid.innerHTML = `<p class="error-msg">Could not load projects list. Please try again later.</p>`;
    }
}

// =============================================
//  Contact Form
// =============================================

function initContactForm() {
  const form     = document.getElementById("contactForm");
  const alertMsg = document.getElementById("alertMessage");
  if (!form || !alertMsg) return;

  const serviceSelect = document.getElementById("serviceSelect");
  const serviceGrid = document.getElementById("service-select-grid");
  const selectOverlay = document.getElementById("service-select-overlay");

  if (serviceSelect && serviceGrid) {
    fetch("/api/services")
      .then(res => res.json())
      .then(services => {
        serviceSelect.innerHTML = '<option value="" disabled selected>Chọn dịch vụ muốn thuê...</option>';
        serviceGrid.innerHTML = '';

        const icons = {
          web:       `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>`,
          design:    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"></path><path d="M12 8A4 4 0 1 0 12 16A4 4 0 1 0 12 8Z"></path><path d="M12 2V6"></path><path d="M12 18V22"></path><path d="M4.93 4.93L7.76 7.76"></path><path d="M16.24 16.24L19.07 19.07"></path><path d="M2 12H6"></path><path d="M18 12H22"></path><path d="M4.93 19.07L7.76 16.24"></path><path d="M16.24 7.76L19.07 4.93"></path></svg>`,
          marketing: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>`,
          mobile:    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>`,
          branding:  `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><circle cx="12" cy="12" r="6"></circle><circle cx="12" cy="12" r="2"></circle></svg>`,
          cloud:     `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.88 18.04A6 6 0 0 0 6 18a5.5 5.5 0 0 0 .5-10.96 4 4 0 1 0 7.82-1.74l.06-.02a6 6 0 0 0 6.5 12.76z"></path></svg>`
        };

        services.forEach(service => {
          const option = document.createElement("option");
          option.value = service.title;
          option.textContent = service.title;
          serviceSelect.appendChild(option);

          const card = document.createElement("div");
          card.className = "modal-service-card";
          const iconKey = service.iconUrl || "web";
          const iconSvg = icons[iconKey] || icons.web;

          card.innerHTML = `
            <div class="modal-service-icon">${iconSvg}</div>
            <h4>${escapeHtml(service.title)}</h4>
            <p>${escapeHtml(service.description)}</p>
          `;

          card.addEventListener("click", () => {
            serviceSelect.value = service.title;
            const titleField = document.getElementById("title");
            if (titleField) {
              titleField.value = `Đăng ký dịch vụ: ${service.title}`;
            }
            if (selectOverlay) {
              selectOverlay.classList.remove("is-open");
            }
          });

          serviceGrid.appendChild(card);
        });
      })
      .catch(err => {
        console.error("Error loading services for contact form:", err);
        serviceSelect.innerHTML = '<option value="" disabled selected>Không thể tải dịch vụ</option>';
        serviceGrid.innerHTML = '<div style="grid-column: span 2; text-align: center; color: #ef4444; padding: 2rem;">Không thể tải danh sách dịch vụ. Vui lòng tải lại trang.</div>';
      });
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const name    = document.getElementById("name").value.trim();
    const email   = document.getElementById("email").value.trim();
    const service = document.getElementById("serviceSelect").value;
    const title   = document.getElementById("title").value.trim();
    const content = document.getElementById("content").value.trim();

    if (!name || !email || !service || !title || !content) {
      showAlert("Vui lòng điền đầy đủ các thông tin bắt buộc.", false);
      return;
    }

    try {
      showAlert("Đang gửi tin nhắn...", null);

      // We prefix the title with [Dịch vụ: ...] to record it properly in the database
      const finalTitle = `[Dịch vụ: ${service}] ${title}`;

      const response = await fetch("/api/contacts", {
        method:  "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + (localStorage.getItem("token") || "")
        },
        body:    JSON.stringify({ name, email, title: finalTitle, content })
      });

      const result = await response.json();

      if (response.ok && result.success) {
        showAlert("Cảm ơn bạn! Tin nhắn của bạn đã được gửi đi thành công.", true);
        form.reset();
        // Restore overlay for subsequent clicks if needed, or leave it closed.
      } else {
        showAlert(result.message || "Gửi tin nhắn thất bại. Vui lòng thử lại.", false);
      }
    } catch (error) {
      console.error("Error submitting contact form:", error);
      showAlert("Không thể kết nối đến máy chủ. Vui lòng thử lại sau.", false);
    }
  });

  function showAlert(msg, isSuccess) {
    alertMsg.textContent = msg;
    alertMsg.className = "alert-message";
    if (isSuccess === true) {
      alertMsg.classList.add("alert-success"); alertMsg.style.display = "block";
    } else if (isSuccess === false) {
      alertMsg.classList.add("alert-error"); alertMsg.style.display = "block";
    } else {
      alertMsg.style.display = "block";
      alertMsg.style.backgroundColor = "#f1f5f9";
      alertMsg.style.color = "#334155";
      alertMsg.style.border = "1px solid #cbd5e1";
    }
  }
}

// =============================================
//  Utility
// =============================================

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>'"]/g,
    tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag)
  );
}

// =============================================
//  Inbox
// =============================================

async function fetchInbox(email) {
  console.log("fetchInbox called with email:", email);
  const inboxSection = document.getElementById("inbox-section");
  const inboxContainer = document.getElementById("inbox-container");
  console.log("inboxSection element:", inboxSection);
  console.log("inboxContainer element:", inboxContainer);
  if (!inboxSection || !inboxContainer) return;

  try {
    const apiUrl = `/api/contacts/my?email=${encodeURIComponent(email)}`;
    console.log("Calling API:", apiUrl);
    const token = localStorage.getItem("token") || "";
    const response = await fetch(apiUrl, {
      headers: { "Authorization": "Bearer " + token }
    });
    console.log("Response status:", response.status);
    if (!response.ok) throw new Error(`Failed to fetch inbox: ${response.status} ${response.statusText}`);
    const contacts = await response.json();
    console.log("Contacts received:", contacts);

    inboxContainer.innerHTML = "";

    if (contacts.length === 0) {
      inboxContainer.innerHTML = `
        <div style="text-align:center;padding:3rem;color:var(--text-muted);">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width:64px;height:64px;margin:0 auto 1rem;opacity:0.5;"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"></path></svg>
          <p>Chưa có tin nhắn nào. Bạn có thể gửi tin nhắn ở trang Contact để kiểm tra.</p>
        </div>
      `;
    } else {
      contacts.forEach(contact => {
        const card = document.createElement("div");
        card.style.cssText = "background:white;border-radius:12px;padding:1.5rem;box-shadow:0 1px 3px rgba(0,0,0,0.1);border:1px solid #e2e8f0;";

        const createdAt = new Date(contact.createdAt).toLocaleDateString("vi-VN", {
          hour: "2-digit", minute: "2-digit",
          day: "2-digit", month: "2-digit", year: "numeric"
        });

        const repliedAt = contact.repliedAt ? new Date(contact.repliedAt).toLocaleDateString("vi-VN", {
          hour: "2-digit", minute: "2-digit",
          day: "2-digit", month: "2-digit", year: "numeric"
        }) : null;

        card.innerHTML = `
          <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:1rem;">
            <div>
              <h3 style="font-size:1.125rem;font-weight:700;color:var(--text-dark);margin:0 0 0.25rem;">${escapeHtml(contact.title)}</h3>
              <p style="font-size:0.875rem;color:var(--text-muted);margin:0;">Gửi vào ${createdAt}</p>
            </div>
            <span class="status-badge ${contact.status === 'DONE' ? 'status-done' : 'status-pending'}" style="padding:0.35rem 0.75rem;font-size:0.75rem;">${escapeHtml(contact.status)}</span>
          </div>
          <div style="background:#f8fafc;padding:1rem;border-radius:8px;margin-bottom:1rem;">
            <h4 style="font-size:0.875rem;font-weight:600;color:var(--text-dark);margin:0 0 0.5rem;">Tin nhắn của bạn:</h4>
            <p style="font-size:0.875rem;color:var(--text-muted);margin:0;white-space:pre-line;">${escapeHtml(contact.content)}</p>
          </div>
          ${contact.reply ? `
            <div style="background:#ecfdf5;padding:1rem;border-radius:8px;border:1px solid #a7f3d0;">
              <h4 style="font-size:0.875rem;font-weight:600;color:#059669;margin:0 0 0.5rem;">Phản hồi từ đội ngũ${repliedAt ? ` (${repliedAt})` : ''}:</h4>
              <p style="font-size:0.875rem;color:#065f46;margin:0;white-space:pre-line;">${escapeHtml(contact.reply)}</p>
            </div>
          ` : `
            <div style="text-align:center;padding:1rem;color:var(--text-muted);font-size:0.875rem;">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width:24px;height:24px;margin:0 auto 0.5rem;opacity:0.5;"><path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              <p>Đang chờ phản hồi...</p>
            </div>
          `}
        `;
        inboxContainer.appendChild(card);
      });
    }

    inboxSection.style.display = "block";
    console.log("Inbox section displayed");

    // Update quick inbox badge
    const quickInbox = document.getElementById("quick-inbox");
    if (quickInbox) {
      const inboxBadge = quickInbox.querySelector(".quick-inbox-badge");
      const hasReplies = contacts.some(c => c.reply);
      if (hasReplies && inboxBadge) {
        inboxBadge.style.display = "block";
      } else if (inboxBadge) {
        inboxBadge.style.display = "none";
      }
    }
  } catch (error) {
    console.error("Error loading inbox:", error);
    inboxContainer.innerHTML = `
      <div style="text-align:center;padding:3rem;color:#ef4444;">
        <p>Không thể tải hộp thư. Lỗi: ${error.message}</p>
      </div>
    `;
    inboxSection.style.display = "block";
  }
}

// =============================================
// Scroll Animation Initialization
// =============================================
function initScrollAnimations() {
  // Handle both animate-on-scroll and scroll-animate classes
  const animatedElements1 = document.querySelectorAll('.animate-on-scroll');
  const animatedElements2 = document.querySelectorAll('.scroll-animate');
  
  const observerOptions = {
    root: null,
    rootMargin: '0px 0px -50px 0px',
    threshold: 0.1
  };
  
  const observer = new IntersectionObserver((entries, index) => {
    entries.forEach((entry, idx) => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate');
        setTimeout(() => {
          entry.target.classList.add('visible');
        }, idx * 100);
        observer.unobserve(entry.target);
      }
    });
  }, observerOptions);
  
  animatedElements1.forEach(el => observer.observe(el));
  animatedElements2.forEach(el => observer.observe(el));
}

// =============================================
// Theme Switcher (Dark/Light Mode)
// =============================================
function injectThemeToggle() {
  const navbar = document.querySelector(".navbar");
  if (!navbar) return;
  if (document.getElementById("theme-toggle-btn")) return;

  const toggleBtn = document.createElement("button");
  toggleBtn.id = "theme-toggle-btn";
  toggleBtn.setAttribute("aria-label", "Toggle dark/light theme");
  toggleBtn.style.cssText = `background:none;border:none;cursor:pointer;padding:0.5rem;display:inline-flex;align-items:center;justify-content:center;transition:var(--transition);margin-left:0.5rem;outline:none;border-radius:50%;width:38px;height:38px;`;

  const sunIcon = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:20px;height:20px;color:#eab308;"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>`;
  const moonIcon = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:20px;height:20px;color:#6366f1;"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>`;

  const currentTheme = localStorage.getItem("theme") || "light";
  if (currentTheme === "dark") {
    document.documentElement.classList.add("dark-theme");
    toggleBtn.innerHTML = sunIcon;
  } else {
    document.documentElement.classList.remove("dark-theme");
    toggleBtn.innerHTML = moonIcon;
  }

  toggleBtn.addEventListener("click", (e) => {
    e.preventDefault();
    const isDark = document.documentElement.classList.contains("dark-theme");
    if (isDark) {
      document.documentElement.classList.remove("dark-theme");
      localStorage.setItem("theme", "light");
      toggleBtn.innerHTML = moonIcon;
    } else {
      document.documentElement.classList.add("dark-theme");
      localStorage.setItem("theme", "dark");
      toggleBtn.innerHTML = sunIcon;
    }
  });

  const navLinks = document.querySelector(".nav-links");
  if (navLinks) {
    const li = document.createElement("li");
    li.id = "theme-toggle-li";
    li.className = "auth-item";
    li.style.display = "inline-flex";
    li.style.alignItems = "center";
    li.appendChild(toggleBtn);
    navLinks.appendChild(li);
  } else {
    navbar.appendChild(toggleBtn);
  }
}

// =============================================
// Floating Quick Access Panel
// =============================================
function injectQuickPanel() {
  if (document.getElementById("quick-panel")) return;

  const quickPanel = document.createElement("div");
  quickPanel.id = "quick-panel";
  quickPanel.className = "quick-panel";

  const currentTheme = localStorage.getItem("theme") || "light";

  quickPanel.innerHTML = `
    <button id="quick-theme-toggle" class="quick-panel-btn" aria-label="Toggle Theme">
      <svg class="sun-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="display:${currentTheme === 'dark' ? 'block' : 'none'};width:20px;height:20px;color:#eab308;"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>
      <svg class="moon-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="display:${currentTheme === 'light' ? 'block' : 'none'};width:20px;height:20px;color:#6366f1;"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>
    </button>
    <a id="quick-inbox" href="/inbox.html" class="quick-panel-btn" aria-label="Inbox" style="display:none;">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="width:20px;height:20px;"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
      <span class="quick-inbox-badge" style="display:none;"></span>
    </a>
    <button id="quick-scroll-top" class="quick-panel-btn" aria-label="Scroll to top" style="opacity:0;pointer-events:none;transition:all 0.3s ease;">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:20px;height:20px;"><polyline points="18 15 12 9 6 15"></polyline></svg>
    </button>
  `;

  document.body.appendChild(quickPanel);

  // Scroll to top
  const scrollTopBtn = document.getElementById("quick-scroll-top");
  window.addEventListener("scroll", () => {
    if (window.scrollY > 300) {
      scrollTopBtn.style.opacity = "1";
      scrollTopBtn.style.pointerEvents = "auto";
    } else {
      scrollTopBtn.style.opacity = "0";
      scrollTopBtn.style.pointerEvents = "none";
    }
  });
  scrollTopBtn.addEventListener("click", () => window.scrollTo({ top: 0, behavior: "smooth" }));

  // Theme toggle
  const qThemeBtn = document.getElementById("quick-theme-toggle");
  const sunIcon = qThemeBtn.querySelector(".sun-icon");
  const moonIcon = qThemeBtn.querySelector(".moon-icon");

  qThemeBtn.addEventListener("click", () => {
    const isDark = document.documentElement.classList.contains("dark-theme");
    if (isDark) {
      document.documentElement.classList.remove("dark-theme");
      localStorage.setItem("theme", "light");
      sunIcon.style.display = "none";
      moonIcon.style.display = "block";
    } else {
      document.documentElement.classList.add("dark-theme");
      localStorage.setItem("theme", "dark");
      sunIcon.style.display = "block";
      moonIcon.style.display = "none";
    }
    // Sync with header toggle
    const headerToggle = document.getElementById("theme-toggle-btn");
    if (headerToggle) {
      const hSun = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:20px;height:20px;color:#eab308;"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>`;
      const hMoon = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:20px;height:20px;color:#6366f1;"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>`;
      headerToggle.innerHTML = isDark ? hMoon : hSun;
    }
  });

  // Inbox shortcut (show only if logged in)
  const token = localStorage.getItem("token") || sessionStorage.getItem("token");
  if (token) {
    const quickInbox = document.getElementById("quick-inbox");
    if (quickInbox) quickInbox.style.display = "flex";
  }
}
