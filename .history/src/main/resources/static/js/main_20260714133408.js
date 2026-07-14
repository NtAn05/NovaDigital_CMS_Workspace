// Dynamic Data Loading, Authentication, and UI logic for NovaDigital Creative Agency

// Clear persistent authentication keys if a new browser session starts (empty sessionStorage)
function initSessionClean() {
  const sessionToken = sessionStorage.getItem("token");
  const localToken = localStorage.getItem("token") || localStorage.getItem("authToken");
  if (!sessionToken && localToken) {
    console.log("New session detected. Clearing persistent auth storage...");
    const authKeys = ["token", "authToken", "username", "fullName", "role", "email", "avatarUrl", "user"];
    authKeys.forEach(key => localStorage.removeItem(key));
  }
}
initSessionClean();

function initTheme() {
  const currentTheme = localStorage.getItem("theme") || "light";
  if (currentTheme === "dark") {
    document.documentElement.classList.add("dark-theme");
  } else {
    document.documentElement.classList.remove("dark-theme");
  }
}
initTheme(); // Initialize theme immediately before DOM fully loads

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

  // 4b. Initialize Hero Text Click animation
  initHeroTextClick();

  // 4c. Initialize Navbar scroll effects (detached floating and show/hide)
  initNavbarScrollEffects();

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
  } else if (page === "rented-project.html") {
    // Handled by inline script
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
  } else if (page === "inbox.html" || page === "index.html") {
    // Fetch inbox if user is logged in
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const email = localStorage.getItem("email") || sessionStorage.getItem("email"); 
    console.log("Token:", token);
    console.log("Email:", email);
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
        showModalAlert("You need to log in to access this feature.", false, "modal-login-alert");
      }, 180);
    }
  } else if (hash === "#register") {
    openAuthModal("register");
  } else if (hash === "#registered") {
    openAuthModal("login");
    setTimeout(() => {
      showModalAlert("Registration successful! Please log in.", true, "modal-login-alert");
    }, 180);
  } else if (hash === "#inbox-section") {
    const section = document.getElementById("inbox-section");
    if (section) {
      setTimeout(() => {
        section.scrollIntoView({ behavior: "smooth" });
      }, 300);
    }
  }
});

// =============================================
//  Auth Modal – Injection & Control
// =============================================

function injectAuthModal() {
  // Guard: do not inject twice
  if (document.getElementById("auth-modal-overlay")) return;
  const modalHTML = `
    <div id="auth-modal-overlay" class="auth-modal-overlay" role="dialog" aria-modal="true" aria-labelledby="auth-modal-heading">
      <div class="auth-modal">

        <!-- Close button -->
        <button class="auth-modal-close" id="auth-modal-close-btn" aria-label="Close">&times;</button>

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
            onclick="switchAuthTab('login')">Login</button>
          <button class="auth-tab-btn" id="tab-register" role="tab"
            aria-selected="false" aria-controls="panel-register"
            onclick="switchAuthTab('register')">Register</button>
        </div>

        <!-- ===== LOGIN PANEL ===== -->
        <div class="auth-panel active" id="panel-login" role="tabpanel" aria-labelledby="tab-login">
          <h2 id="auth-modal-heading">Welcome Back</h2>
          <p class="subtitle">Enter your details to access your account</p>

          <form id="modal-loginForm" novalidate>
            <div class="form-group">
              <label for="modal-usernameOrEmail">Username or Email *</label>
              <input type="text" id="modal-usernameOrEmail"
                placeholder="Enter username or email" required autocomplete="username">
            </div>
            <div class="form-group">
              <label for="modal-password">Password *</label>
              <input type="password" id="modal-password"
                placeholder="••••••••" required autocomplete="current-password">
            </div>
            <div style="text-align: right; margin-top: -0.5rem; margin-bottom: 1rem;">
              <a href="forgot-password.html" style="font-size: 0.85rem; color: var(--primary); text-decoration: none; font-weight: 500;">Forgot password?</a>
            </div>
            <button type="submit" class="submit-btn" style="margin-top:0.5rem;">Login</button>
            <div id="modal-login-alert" class="alert-message"></div>
          </form>

          <div class="auth-modal-divider">
            Don't have an account?
            <a onclick="switchAuthTab('register')">Register now</a>
          </div>
        </div>

        <!-- ===== REGISTER PANEL ===== -->
        <div class="auth-panel" id="panel-register" role="tabpanel" aria-labelledby="tab-register">
          <h2>Create Account</h2>
          <p class="subtitle">Join NovaDigital to experience premium services</p>

          <form id="modal-registerForm" novalidate>
            <div class="form-group">
              <label for="modal-username">Username *</label>
              <input type="text" id="modal-username"
                placeholder="Choose a username" required minlength="4" maxlength="50" autocomplete="username">
            </div>
            <div class="form-group">
              <label for="modal-fullName">Full Name *</label>
              <input type="text" id="modal-fullName"
                placeholder="Enter your full name" required autocomplete="name">
            </div>
            <div class="form-group">
              <label for="modal-email">Email Address *</label>
              <input type="email" id="modal-email"
                placeholder="name@domain.com" required autocomplete="email">
            </div>
            <div class="form-group">
              <label for="modal-phone">Phone Number (10 digits)</label>
              <input type="tel" id="modal-phone"
                placeholder="0123456789" pattern="[0-9]{10}" autocomplete="tel">
            </div>
            <div class="form-group">
              <label for="modal-reg-password">Password *</label>
              <input type="password" id="modal-reg-password"
                placeholder="Min 6 characters" required minlength="6" autocomplete="new-password">
            </div>
            <button type="submit" class="submit-btn" style="margin-top:0.5rem;">Register</button>
            <div id="modal-register-alert" class="alert-message"></div>
          </form>

          <div class="auth-modal-divider">
            Already have an account?
            <a onclick="switchAuthTab('login')">Login here</a>
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
      showModalAlert("Please enter your username and password.", false, "modal-login-alert");
      return;
    }

    try {
      showModalAlert("Logging in...", null, "modal-login-alert");

      const response = await fetch("/api/auth/login", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ usernameOrEmail, password })
      });

      const data = await response.json();

      if (response.ok && data.token) {
        // Write to localStorage
        localStorage.setItem("token",     data.token);
        localStorage.setItem("authToken", data.token);
        localStorage.setItem("username",  data.username);
        localStorage.setItem("fullName",  data.fullName);
        localStorage.setItem("role",      data.role);
        localStorage.setItem("email",     data.email);
        localStorage.setItem("avatarUrl", data.avatarUrl || "");
        localStorage.setItem("user", JSON.stringify({
          username:  data.username,
          fullName:  data.fullName,
          email:     data.email,
          role:      data.role,
          avatarUrl: data.avatarUrl || null
        }));

        // Write to sessionStorage for route guard and header sync compatibility
        sessionStorage.setItem("token",     data.token);
        sessionStorage.setItem("authToken", data.token);
        sessionStorage.setItem("username",  data.username);
        sessionStorage.setItem("fullName",  data.fullName);
        sessionStorage.setItem("role",      data.role);
        sessionStorage.setItem("email",     data.email);
        sessionStorage.setItem("avatarUrl", data.avatarUrl || "");

        showModalAlert("Login successful! Redirecting...", true, "modal-login-alert");

        setTimeout(() => {
          if (data.role === "ROLE_ADMIN") {
            window.location.href = "admin.html";
          } else if (data.role === "Team_Member" || data.role === "ROLE_MEMBER") {
            window.location.href = "member-contact.html";
          } else {
            const redirectAttempt = sessionStorage.getItem("redirectAttempt");
            if (redirectAttempt) {
              sessionStorage.removeItem("redirectAttempt");
              window.location.href = redirectAttempt;
            } else {
              window.location.reload();
            }
          }
        }, 1000);
      } else {
        showModalAlert(data.message || "Login failed. Please check your credentials.", false, "modal-login-alert");
      }
    } catch (error) {
      console.error("Modal login error:", error);
      showModalAlert("Could not connect to server. Please try again.", false, "modal-login-alert");
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
      showModalAlert("Please fill in all required fields.", false, "modal-register-alert");
      return;
    }

    try {
      showModalAlert("Registering...", null, "modal-register-alert");

      const response = await fetch("/api/auth/register", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ username, fullName, email, phone, password })
      });

      const data = await response.json();

      if (response.ok && data.success) {
        showModalAlert("Registration successful! Please log in.", true, "modal-register-alert");
        setTimeout(() => {
          switchAuthTab("login");
          setTimeout(() => {
            showModalAlert("Account created! Please log in.", true, "modal-login-alert");
          }, 80);
        }, 1400);
      } else {
        showModalAlert(
          data.message || "Registration failed. Username or Email may already exist.",
          false, "modal-register-alert"
        );
      }
    } catch (error) {
      console.error("Modal register error:", error);
      showModalAlert("Could not connect to server. Please try again.", false, "modal-register-alert");
    }
  });
}

// =============================================
//  Authentication & Route Guard
// =============================================

function checkRouteGuard() {
  const path = window.location.pathname;
  const page = path.substring(path.lastIndexOf('/') + 1) || "index.html";

  const token = localStorage.getItem("token") || sessionStorage.getItem("token");
  const role  = localStorage.getItem("role") || sessionStorage.getItem("role");

  // Admin MUST stay in admin.html or user-profile.html
  if (token && role === "ROLE_ADMIN") {
    if (page !== "admin.html" && page !== "user-profile.html") {
      window.location.href = "admin.html";
      return;
    }
  }

  // Member MUST stay in member-contact.html or member-profile.html
  if (token && (role === "ROLE_MEMBER" || role === "Team_Member")) {
    if (page !== "member-contact.html" && page !== "member-profile.html") {
      window.location.href = "member-contact.html";
      return;
    }
  }

  // Protected client pages
  const protectedPages = ["contact.html", "rented-project.html"];

  if (protectedPages.includes(page) && !token) {
    sessionStorage.setItem("redirectAttempt", page);
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
    if (!token || (role !== "ROLE_MEMBER" && role !== "Team_Member")) {
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

  const token    = localStorage.getItem("token") || sessionStorage.getItem("token");
  const role     = localStorage.getItem("role") || sessionStorage.getItem("role");
  const fullName = localStorage.getItem("fullName") || sessionStorage.getItem("fullName");

  const isPortalUser = token && (role === "ROLE_ADMIN" || role === "ROLE_MEMBER" || role === "Team_Member");

  // If Admin or Member, hide all standard navigation links (Home, Services, etc.)
  if (isPortalUser) {
    navLinksContainer.querySelectorAll("li").forEach(li => {
      // Hide standard links. Dynamic auth-items (Dashboard, Logout) will be added back later.
      if (!li.classList.contains("auth-item")) {
        li.style.display = "none";
      }
    });
    // Also hide the logo link to homepage or change it
    const logo = document.getElementById("header-logo");
    if (logo) {
      const targetPage = role === "ROLE_ADMIN" ? "admin.html" : "member-contact.html";
      logo.setAttribute("href", targetPage);
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

  // Get the static "Get Started Now" button (if present in HTML)
  const defaultBtnEl = navLinksContainer.querySelector("#default-get-started");
  const defaultBtnLi = defaultBtnEl ? defaultBtnEl.closest("li") : null;

  if (token) {
    // Hide the static button when user is logged in
    if (defaultBtnLi) defaultBtnLi.style.display = "none";
    const dropdownLi = document.createElement("li");
    dropdownLi.className = "auth-item user-dropdown-container";
    dropdownLi.style.position = "relative";

    const username  = localStorage.getItem("username") || sessionStorage.getItem("username");
    const avatarUrl = localStorage.getItem("avatarUrl") || sessionStorage.getItem("avatarUrl");

    function getInitials(name) {
      if (!name) return "ND";
      const parts = name.trim().split(/\s+/);
      if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    const initials = getInitials(fullName || username);

    let menuItemsHtml = "";
    if (role === "ROLE_ADMIN") {
      menuItemsHtml = `
        <a href="admin.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>
          Dashboard
        </a>
      `;
    } else if (role === "Team_Member" || role === "ROLE_MEMBER") {
      menuItemsHtml = `
        <a href="member-contact.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          Member Portal
        </a>
        <a href="member-profile.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          Profile
        </a>
      `;
    } else {
      menuItemsHtml = `
        <a href="user-profile.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          Profile
        </a>
        <a href="inbox.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
          Inbox
        </a>
        <a href="transaction.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/></svg>
          My Transaction
        </a>
        <a href="rented-project.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><line x1="9" y1="3" x2="9" y2="21"/><line x1="15" y1="3" x2="15" y2="21"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="15" x2="21" y2="15"/></svg>
          My Rented Project
        </a>
      `;
    }

    dropdownLi.innerHTML = `
      <div class="user-avatar-trigger" id="user-avatar-trigger" style="display:flex;align-items:center;gap:0.5rem;cursor:pointer;padding:0.25rem 0;">
        ${avatarUrl ? 
          `<img src="${escapeHtml(avatarUrl)}" alt="Avatar" class="nav-avatar-img" style="width:32px;height:32px;border-radius:50%;object-fit:cover;border:2px solid var(--primary);">` :
          `<div class="nav-avatar-initials" style="width:32px;height:32px;border-radius:50%;background:linear-gradient(135deg, var(--primary) 0%, var(--primary-purple) 100%);color:white;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:0.85rem;">${escapeHtml(initials)}</div>`
        }
        <span class="nav-username-txt" style="font-weight:600;font-size:0.95rem;color:var(--text-muted);">${escapeHtml(fullName || username)}</span>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" width="12" height="12" style="color:var(--text-muted);"><polyline points="6 9 12 15 18 9"></polyline></svg>
      </div>
      <div class="user-dropdown-menu" id="user-dropdown-menu">
        ${menuItemsHtml}
        <hr style="border:none;border-top:1px solid var(--border-color);margin:0.4rem 0;">
        <a href="#" id="dropdown-logout-btn" class="dropdown-item logout-link" style="color:#ef4444 !important;font-weight:600;">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
          Logout
        </a>
      </div>
    `;

    navLinksContainer.appendChild(dropdownLi);

    const trigger = dropdownLi.querySelector("#user-avatar-trigger");
    const menu = dropdownLi.querySelector("#user-dropdown-menu");

    trigger.addEventListener("click", (e) => {
      e.stopPropagation();
      const isVisible = menu.classList.contains("show");
      if (isVisible) {
        menu.classList.remove("show");
      } else {
        // Hide other dropdowns if any
        document.querySelectorAll(".user-dropdown-menu").forEach(m => m.classList.remove("show"));
        menu.classList.add("show");
      }
    });

    document.addEventListener("click", () => {
      menu.classList.remove("show");
    });

    dropdownLi.querySelector("#dropdown-logout-btn").addEventListener("click", (e) => {
      e.preventDefault();
      logoutUser();
    });
  } else {
    // — Guest: bind the static "Get Started Now" button → opens auth modal —
    if (defaultBtnLi) {
      // Make sure the static button is visible
      defaultBtnLi.style.display = "";
      // Remove any previously bound click listeners by cloning
      const freshBtn = defaultBtnEl.cloneNode(true);
      defaultBtnEl.parentNode.replaceChild(freshBtn, defaultBtnEl);
      freshBtn.addEventListener("click", (e) => {
        e.preventDefault();
        openAuthModal("login");
      });
    } else {
      // Fallback: create button dynamically if static one is missing
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

  // Inject theme toggle button next to navbar links
  injectThemeToggle();
}

// User Logout Logic
function logoutUser() {
  localStorage.clear();
  sessionStorage.clear();
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
    showAlert("You need to log in to access this feature.", false);
  } else if (urlParams.get("registered") === "true") {
    showAlert("Account registered successfully! Please log in.", true);
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usernameOrEmail = document.getElementById("usernameOrEmail").value.trim();
    const password        = document.getElementById("password").value;

    if (!usernameOrEmail || !password) {
      showAlert("Please enter your username and password.", false);
      return;
    }

    try {
      showAlert("Logging in...", null);

      const response = await fetch("/api/auth/login", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ usernameOrEmail, password })
      });

      const data = await response.json();

      if (response.ok && data.token) {
        // Store both 'token' (legacy) and 'authToken' (used by new dashboards) in localStorage
        localStorage.setItem("token",     data.token);
        localStorage.setItem("authToken", data.token);
        localStorage.setItem("username",  data.username);
        localStorage.setItem("fullName",  data.fullName);
        localStorage.setItem("role",      data.role);
        localStorage.setItem("email",     data.email);
        localStorage.setItem("avatarUrl", data.avatarUrl || "");
        // Store full user object for PM / Client dashboards
        localStorage.setItem("user", JSON.stringify({
          username:  data.username,
          fullName:  data.fullName,
          email:     data.email,
          role:      data.role,
          avatarUrl: data.avatarUrl || null
        }));

        // Write to sessionStorage for route guard and header sync compatibility
        sessionStorage.setItem("token",     data.token);
        sessionStorage.setItem("authToken", data.token);
        sessionStorage.setItem("username",  data.username);
        sessionStorage.setItem("fullName",  data.fullName);
        sessionStorage.setItem("role",      data.role);
        sessionStorage.setItem("email",     data.email);
        sessionStorage.setItem("avatarUrl", data.avatarUrl || "");

        showAlert("Login successful! Redirecting...", true);

        setTimeout(() => {
          if (data.role === "ROLE_ADMIN") {
            window.location.href = "admin.html";
          } else if (data.role === "ROLE_MEMBER") {
            // Internal team member — goes to PM Dashboard
            window.location.href = "member-contact.html";
          } else if (data.role === "ROLE_USER") {
            // External client — goes to Client Portal
            window.location.href = "client-dashboard.html";
          } else {
            const redirect = sessionStorage.getItem("redirectAttempt");
            if (redirect) {
              sessionStorage.removeItem("redirectAttempt");
              window.location.href = redirect;
            } else {
              window.location.href = "index.html";
            }
          }
        }, 1000);
      } else {
        showAlert(data.message || "Login failed. Please check your credentials.", false);
      }
    } catch (error) {
      console.error("Login error:", error);
      showAlert("Could not connect to server. Please try again.", false);
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
      showAlert("Please fill in all required fields.", false);
      return;
    }

    try {
      showAlert("Registering...", null);

      const response = await fetch("/api/auth/register", {
        method:  "POST",
        headers: { "Content-Type": "application/json" },
        body:    JSON.stringify({ username, fullName, email, phone, password })
      });

      const data = await response.json();

      if (response.ok && data.success) {
        showAlert("Registration successful! Redirecting to login...", true);
        setTimeout(() => { window.location.href = "login.html?registered=true"; }, 1500);
      } else {
        showAlert(data.message || "Registration failed. Username or Email may already exist.", false);
      }
    } catch (error) {
      console.error("Registration error:", error);
      showAlert("Could not connect to server. Please try again.", false);
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
  return sessionStorage.getItem("token") || "";
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
    const urlInputId  = type === "project" ? "cf-imageUrl"  : "cf-avatarUrl";
    const fileInput   = document.getElementById(fileInputId);

    if (fileInput) {
      fileInput.addEventListener("change", async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const preview   = document.getElementById("cf-preview");
        const urlInput  = document.getElementById(urlInputId);

        // Helper: read file as Base64 Data URL (always works, no server needed)
        const readAsDataUrl = (f) => new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload  = () => resolve(reader.result);
          reader.onerror = reject;
          reader.readAsDataURL(f);
        });

        showCrudAlert("Uploading image...", null);

        // 1️⃣ Try server upload first (with auth token)
        try {
          const formData = new FormData();
          formData.append("file", file);

          const res  = await fetch("/api/upload", {
            method: "POST",
            headers: { "Authorization": "Bearer " + getAdminToken() },
            body: formData
          });
          const data = await res.json().catch(() => ({}));

          if (res.ok && data.url) {

            urlInput.value = data.url;
            if (preview) { preview.src = data.url; preview.style.display = "block"; }
            showCrudAlert("✅ Image uploaded successfully!", true);
            return; // done – no need for fallback
          }
          // Server returned non-ok or no url → fall through to Base64
          console.warn("Server upload failed (status:", res.status, "), falling back to Base64.");
        } catch (uploadErr) {
          console.warn("Server upload error, falling back to Base64:", uploadErr);
        }

        // 2️⃣ Fallback: use Base64 Data URL directly
        try {
          const dataUrl = await readAsDataUrl(file);
          urlInput.value = dataUrl;
          if (preview) { preview.src = dataUrl; preview.style.display = "block"; }
          showCrudAlert("✅ Image ready (local preview).", true);
        } catch (b64Err) {
          console.error("Base64 read error:", b64Err);
          showCrudAlert("Could not load image. Please try a different file.", false);
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
    ${fld("cf-username",  "Username *", "text",  v.username, `placeholder="Enter username" required ${item ? 'readonly style="background:#f8fafc;cursor:not-allowed;"' : ""}`)}
    ${fld("cf-fullName",  "Full Name *",     "text",  v.fullName, 'placeholder="Enter full name" required')}
    ${fld("cf-email",     "Email *",          "email", v.email,    'placeholder="name@domain.com" required')}
    ${fld("cf-phone",     "Phone Number",   "tel",   v.phone,    'placeholder="0123456789" pattern="[0-9]{10}"')}
    ${!item ? fld("cf-password", "Password *", "password", "", 'placeholder="Min 6 characters" required minlength="6"') : ""}

    ${sel("cf-role", "Role *", [["ROLE_USER","User"],["ROLE_ADMIN","Admin"],["ROLE_MEMBER","Team Member"]], v.role || "ROLE_USER")}

    <div class="form-group" style="width: 100%;">
      <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer; justify-content: flex-start;">
        <input type="checkbox" id="cf-enabled" ${v.enabled !== false ? 'checked' : ''}>
        <span>Active Status</span>
      </label>
    </div>
  `;

  if (type === "member") return `
    ${fld("cf-name",       "Member Name *",         "text", v.name,        'placeholder="Enter member name" required')}
    ${fld("cf-role",       "Position / Role *",       "text", v.role,        'placeholder="e.g. Frontend Developer" required')}
    <div class="form-group">
      <label for="cf-avatarFile">Avatar Image *</label>
      <input type="file" id="cf-avatarFile" accept="image/*" style="width:100%; padding:0.5rem; border:1px dashed var(--border-color); border-radius:var(--radius-sm); background:var(--bg-light); cursor:pointer;">
      <input type="hidden" id="cf-avatarUrl" value="${escapeHtml(String(v.avatarUrl || ""))}">
      ${v.avatarUrl ? `<img id="cf-preview" src="${escapeHtml(v.avatarUrl)}" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: block;">` : `<img id="cf-preview" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: none;">`}
    </div>
    ${fld("cf-facebookUrl","Facebook URL",               "url",  v.facebookUrl, 'placeholder="https://facebook.com/..."')}
    ${fld("cf-githubUrl",  "GitHub URL",                 "url",  v.githubUrl,   'placeholder="https://github.com/..."')}
    ${fld("cf-linkedinUrl","LinkedIn URL",               "url",  v.linkedinUrl, 'placeholder="https://linkedin.com/in/..."')}
    ${fld("cf-skills",     "Professional Skills",       "text", v.skills,      'placeholder="e.g. Java, React, SQL"')}
    ${fld("cf-projects",   "Projects Worked On",        "text", v.projectsWorked, 'placeholder="e.g. CMS Portal, E-Commerce App"')}
  `;

  if (type === "project") return `
    ${fld("cf-title",       "Project Title *",   "text", v.title,       'placeholder="Enter project title" required')}
    ${fld("cf-category",    "Category *",    "text", v.category,    'placeholder="e.g. Web Development" required')}
    <div class="form-group">
      <label for="cf-imageFile">Cover Image *</label>
      <input type="file" id="cf-imageFile" accept="image/*" style="width:100%; padding:0.5rem; border:1px dashed var(--border-color); border-radius:var(--radius-sm); background:var(--bg-light); cursor:pointer;">
      <input type="hidden" id="cf-imageUrl" value="${escapeHtml(String(v.imageUrl || ""))}">
      ${v.imageUrl ? `<img id="cf-preview" src="${escapeHtml(v.imageUrl)}" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: block;">` : `<img id="cf-preview" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: none;">`}
    </div>
    ${txt("cf-description", "Description *",               v.description, 'placeholder="Project description..." required')}
    ${txt("cf-technologies", "Technologies Used", v.technologies, 'placeholder="e.g. React, Node.js, MongoDB..."')}
  `;

  if (type === "service") return `
    ${fld("cf-title", "Service Title *", "text", v.title, 'placeholder="Enter service title" required')}
    ${sel("cf-iconUrl", "Service Icon *",
      [["web","🌐 Web Design"],["design","🎨 UI/UX Design"],["marketing","📊 Marketing"],
       ["mobile","📱 Mobile App"],["branding","🎯 Branding"],["cloud","☁️ Cloud Solutions"]],
      v.iconUrl || "web")}
    ${txt("cf-description", "Description *", v.description, 'placeholder="Service description..." required')}
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
                facebookUrl: g("cf-facebookUrl"), githubUrl: g("cf-githubUrl"), linkedinUrl: g("cf-linkedinUrl"),
                skills: g("cf-skills"), projects: g("cf-projects") };
    if (!payload.name || !payload.avatarUrl) valid = false;
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

  if (!valid) { showCrudAlert("Please fill in all required fields (*)", false); return; }

  const eps = { user: "/api/admin/users", member: "/api/members",
                project: "/api/projects", service: "/api/services" };

  const url    = isEdit ? `${eps[type]}/${item.id}` : eps[type];
  const method = isEdit ? "PUT" : "POST";

  try {
    showCrudAlert("Processing...", null);
    const response = await fetch(url, { method, headers: adminHeaders(), body: JSON.stringify(payload) });
    const data = await response.json().catch(() => ({}));

    if (response.ok) {
      showCrudAlert(isEdit ? "✅ Updated successfully!" : "✅ Added successfully!", true);
      setTimeout(() => {
        closeCrudModal();
        if (type === "user")    fetchAdminUsers();
        if (type === "member")  fetchAdminMembersTable();
        if (type === "project") fetchAdminProjectsTable();
        if (type === "service") fetchAdminServicesTable();
      }, 700);
    } else {
      showCrudAlert(data.message || "Operation failed. Please try again.", false);
    }
  } catch (err) {
    console.error("CRUD submit error:", err);
    showCrudAlert("Could not connect to server.", false);
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
  if (text) text.textContent = `Are you sure you want to delete "${name}"? This action cannot be undone.`;
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
      alert("Delete failed. Please try again.");
    }
  } catch (err) {
    console.error("Delete error:", err);
    alert("Could not connect to server.");
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
      tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:2rem;color:var(--text-muted);">No users found.</td></tr>`;
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
        lastLoginText = lastLogin.toLocaleString("en-US");
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
        <td><span class="status-badge ${u.role === "ROLE_ADMIN" ? "badge-admin" : (u.role === "Team_Member" || u.role === "ROLE_MEMBER" ? "badge-member" : "badge-user")}">${u.role === "ROLE_ADMIN" ? "Admin" : (u.role === "Team_Member" || u.role === "ROLE_MEMBER" ? "Team Member" : "User")}</span></td>
        <td>
          <button class="btn-toggle-status" onclick="toggleUserStatus(${u.id})" style="padding: 4px 12px; border-radius: 20px; border: none; cursor: pointer; font-weight: 600; font-size: 12px; white-space: nowrap; ${u.enabled ? 'background: #ecfdf5; color: #059669;' : 'background: #fef2f2; color: #dc2626;'}">
            ${u.enabled ? 'Active' : 'Disabled'}
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
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:2rem;color:#ef4444;">Could not load user list.</td></tr>`;
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
      alert("Operation failed. Please try again.");
    }
  } catch (err) {
    console.error("Toggle status error:", err);
    alert("Could not connect to server.");
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
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:var(--text-muted);">No members found.</td></tr>`;
      return;
    }

    members.forEach(m => {
      _cache.members[m.id] = m;
      const tr = document.createElement("tr");
      tr.setAttribute("data-searchable", `${m.name} ${m.role}`);
      const fbSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="#1877F2" viewBox="0 0 24 24" style="vertical-align:middle;"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg>`;
      const ghSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 24 24" style="vertical-align:middle;color:var(--text-dark);"><path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/></svg>`;
      const liSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="#0A66C2" viewBox="0 0 24 24" style="vertical-align:middle;"><path d="M19 0h-14c-2.761 0-5 2.239-5 5v14c0 2.761 2.239 5 5 5h14c2.762 0 5-2.239 5-5v-14c0-2.761-2.238-5-5-5zm-11 19h-3v-11h3v11zm-1.5-12.268c-.966 0-1.75-.779-1.75-1.75s.784-1.75 1.75-1.75 1.75.779 1.75 1.75-.784 1.75-1.75 1.75zm13.5 12.268h-3v-5.604c0-3.368-4-3.113-4 0v5.604h-3v-11h3v1.765c1.396-2.586 7-2.777 7 2.476v6.759z"/></svg>`;

      const mkFbLink = url => url ? `<a href="${escapeHtml(url)}" target="_blank" title="Facebook">${fbSvg}</a>` : "—";
      const mkGhLink = url => url ? `<a href="${escapeHtml(url)}" target="_blank" title="GitHub">${ghSvg}</a>` : "—";
      const mkLiLink = url => url ? `<a href="${escapeHtml(url)}" target="_blank" title="LinkedIn">${liSvg}</a>` : "—";

      tr.innerHTML = `
        <td><img src="${escapeHtml(m.avatarUrl || "")}" alt="${escapeHtml(m.name || "")}" class="table-avatar"
              onerror="this.src='https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=60&h=60'"></td>
        <td class="text-dark-inline">${escapeHtml(m.name || "")}</td>
        <td><span class="status-badge badge-active">${escapeHtml(m.role || "")}</span></td>
        <td>${mkFbLink(m.facebookUrl)}</td>
        <td>${mkGhLink(m.githubUrl)}</td>
        <td>${mkLiLink(m.linkedinUrl)}</td>
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
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:#ef4444;">Could not load member list.</td></tr>`;
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
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">No projects found.</td></tr>`;
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
            <button class="btn-edit" style="background:#2563eb; color:#fff; border-color:#2563eb;" onclick="openMilestoneModal(${p.id})">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:12px;height:12px;margin-right:2px;"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"></path><line x1="4" y1="22" x2="4" y2="15"></line></svg>Milestones
            </button>
            <button class="btn-edit" style="background:#059669; color:#fff; border-color:#059669;" onclick="openAssignmentModal(${p.id}, '${escapeHtml(p.title || '')}')">
              &#128101; Assign
            </button>
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
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:#ef4444;">Could not load project list.</td></tr>`;
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
      tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:2rem;color:var(--text-muted);">No services found.</td></tr>`;
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
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:2rem;color:#ef4444;">Could not load service list.</td></tr>`;
  }
}

async function fetchAdminContacts() {
  const tableBody  = document.getElementById("contacts-table-body");
  const statsCount = document.getElementById("stat-messages-count");
  if (!tableBody) return;

  try {
    const token = getAdminToken();
    const response = await fetch("/api/contacts", {
      headers: token ? { "Authorization": "Bearer " + token } : {}
    });
    if (!response.ok) throw new Error("Failed to fetch contact submissions");
    const contacts = await response.json();

    tableBody.innerHTML = "";

    if (contacts.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;">No contact messages found.</td></tr>`;
      if (statsCount) statsCount.textContent = "0";
      return;
    }

    if (statsCount) statsCount.textContent = contacts.length;

    contacts.forEach(contact => {
      const row  = document.createElement("tr");
      const date = new Date(contact.createdAt).toLocaleDateString("en-US", {
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
    tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:#ef4444;">Could not load contacts list. Please reload the page.</td></tr>`;
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
    servicesGrid.innerHTML = `<p class="error-msg">Could not load services. Please try again later.</p>`;
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
    teamGrid.innerHTML = `<p class="error-msg">Could not load members. Please try again later.</p>`;
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
  
  // Set active project tracker and fetch milestones in real-time
  activeProjectInModal = project.id;
  fetchAndRenderProjectMilestones(project.id);
}

function closeProjectModal() {
  const modalOverlay = document.getElementById('project-modal-overlay');
  if (!modalOverlay) return;
  modalOverlay.classList.remove('is-open');
  document.body.style.overflow = '';
  
  // Clear active project tracker
  activeProjectInModal = null;
}

// Global tracking for currently open modal project
let activeProjectInModal = null;

// Fetch and render milestones timeline inside project modal
async function fetchAndRenderProjectMilestones(projectId) {
  const container = document.getElementById('project-modal-milestones-list');
  if (!container) return;

  container.innerHTML = `<p style="color: var(--text-muted); font-size: 0.9rem;">Loading milestones...</p>`;

  try {
    const response = await fetch(`/api/projects/${projectId}/milestones`);
    if (!response.ok) throw new Error("Failed to load milestones");
    const milestones = await response.json();

    if (!milestones || milestones.length === 0) {
      container.innerHTML = `<p style="color: var(--text-muted); font-size: 0.9rem;">No milestones defined for this project.</p>`;
      return;
    }

    container.innerHTML = milestones.map(m => {
      const statusClass = m.status.toLowerCase();
      const isCompleted = m.status === 'COMPLETED';
      return `
        <div class="milestone-item" id="milestone-item-${m.id}">
          <div class="milestone-dot ${statusClass}" id="milestone-dot-${m.id}"></div>
          <div class="milestone-header">
            <span class="milestone-name">${escapeHtml(m.name)}</span>
            <span class="milestone-status-badge ${statusClass}" id="milestone-badge-${m.id}">${escapeHtml(m.status)}</span>
          </div>
          ${m.description ? `<p class="milestone-desc">${escapeHtml(m.description)}</p>` : ''}
          <div class="milestone-progress-container">
            <div class="milestone-progress-bg">
              <div class="milestone-progress-fill ${isCompleted ? 'completed' : ''}" 
                   id="milestone-progress-fill-${m.id}" 
                   style="width: ${m.progressPercentage}%"></div>
            </div>
            <span class="milestone-progress-text" id="milestone-progress-text-${m.id}">${m.progressPercentage}%</span>
          </div>
          ${m.dueDate ? `
            <div class="milestone-due">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
              Due: ${m.dueDate}
            </div>
          ` : ''}
        </div>
      `;
    }).join('');
  } catch (err) {
    console.error("fetchAndRenderProjectMilestones error:", err);
    container.innerHTML = `<p style="color: #ef4444; font-size: 0.9rem;">Could not load milestones.</p>`;
  }
}

// Initialize Server-Sent Events stream for real-time milestone updates
function initMilestoneSSE() {
  const toastContainer = document.getElementById('live-toast-container');
  if (!toastContainer) return; // Only run on pages that have the toast container

  const eventSource = new EventSource('/api/milestones/stream');

  eventSource.addEventListener('connected', (e) => {
    console.log("SSE Connection live:", JSON.parse(e.data).message);
  });

  eventSource.addEventListener('milestone-update', (e) => {
    try {
      const payload = JSON.parse(e.data);
      console.log("Live milestone event received:", payload);

      // 1. Display Toast notification
      showLiveToast(payload.eventType, payload.mutationSummary);

      // 2. If this update belongs to the active project in the open modal, update UI in real-time
      if (activeProjectInModal === payload.projectId && payload.milestone) {
        const m = payload.milestone;
        
        // Update progress bar
        const progressFill = document.getElementById(`milestone-progress-fill-${m.id}`);
        const progressText = document.getElementById(`milestone-progress-text-${m.id}`);
        if (progressFill && progressText) {
          progressFill.style.width = `${m.progressPercentage}%`;
          progressText.textContent = `${m.progressPercentage}%`;
          if (m.status === 'COMPLETED') {
            progressFill.classList.add('completed');
          } else {
            progressFill.classList.remove('completed');
          }
        }

        // Update status badge
        const badge = document.getElementById(`milestone-badge-${m.id}`);
        const dot = document.getElementById(`milestone-dot-${m.id}`);
        if (badge && dot) {
          badge.className = 'milestone-status-badge';
          dot.className = 'milestone-dot';

          const statusClass = m.status.toLowerCase();
          badge.classList.add(statusClass);
          dot.classList.add(statusClass);

          badge.textContent = m.status;
        }

        // Add a temporary highlight animation to the milestone element
        const item = document.getElementById(`milestone-item-${m.id}`);
        if (item) {
          item.style.transition = 'background-color 0.3s ease';
          item.style.backgroundColor = 'rgba(37, 99, 235, 0.08)';
          setTimeout(() => {
            item.style.backgroundColor = 'transparent';
          }, 1000);
        }
      }
    } catch (err) {
      console.error("Error handling SSE event:", err);
    }
  });

  eventSource.onerror = (err) => {
    console.warn("SSE connection encountered an error, reconnecting...", err);
  };
}

function showLiveToast(eventType, message) {
  const container = document.getElementById('live-toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = 'live-toast';
  
  let title = 'Project Milestone Update';
  let iconSVG = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2z"></polygon><polyline points="2 17 12 22 22 17"></polyline><polyline points="2 12 12 17 22 12"></polyline></svg>`;

  if (eventType === 'MILESTONE_CREATED') {
    title = 'New Milestone Added';
    toast.classList.add('success');
    iconSVG = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="16"></line><line x1="8" y1="12" x2="16" y2="12"></line></svg>`;
  } else if (eventType === 'MILESTONE_UPDATED') {
    title = 'Milestone Sync Status';
    toast.classList.add('success');
  } else if (eventType === 'MILESTONE_DELETED') {
    title = 'Milestone Removed';
  }

  toast.innerHTML = `
    <div class="live-toast-icon">${iconSVG}</div>
    <div class="live-toast-body">
      <div class="live-toast-title">${escapeHtml(title)}</div>
      <div class="live-toast-message">${escapeHtml(message)}</div>
    </div>
    <button class="live-toast-close" onclick="this.parentElement.remove()">&times;</button>
  `;

  container.appendChild(toast);

  // Trigger animation reflow
  setTimeout(() => {
    toast.classList.add('show');
  }, 10);

  // Auto remove toast after 5 seconds
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => {
      toast.remove();
    }, 400);
  }, 5000);
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

<<<<<<< Updated upstream
        if (projectModalClose) {
            projectModalClose.addEventListener('click', closeProjectModal);
        }

=======
>>>>>>> Stashed changes
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

  const nameInput = document.getElementById("name");
  const emailInput = document.getElementById("email");
  const serviceSelect = document.getElementById("serviceSelect");
  const serviceGrid = document.getElementById("service-select-grid");
  const selectOverlay = document.getElementById("service-select-overlay");

  if (nameInput) {
    nameInput.value = sessionStorage.getItem("fullName") || sessionStorage.getItem("username") || "";
    nameInput.readOnly = true;
  }
  if (emailInput) {
    emailInput.value = sessionStorage.getItem("email") || "";
    emailInput.readOnly = true;
  }

  if (serviceSelect && serviceGrid) {
    fetch("/api/services")
      .then(res => res.json())
      .then(services => {
        serviceSelect.innerHTML = '<option value="" disabled selected>Choose a service to hire...</option>';
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
              titleField.value = `Register service: ${service.title}`;
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
        serviceSelect.innerHTML = '<option value="" disabled selected>Could not load services</option>';
        serviceGrid.innerHTML = '<div style="grid-column: span 2; text-align: center; color: #ef4444; padding: 2rem;">Could not load services. Please reload the page.</div>';
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
      showAlert("Please fill in all required fields.", false);
      return;
    }

    try {
      showAlert("Sending message...", null);

      // We prefix the title with [Service: ...] to record it properly in the database
      const finalTitle = `[Service: ${service}] ${title}`;

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
        showAlert("Thank you! Your message has been sent successfully.", true);
        form.reset();
        // Restore overlay for subsequent clicks if needed, or leave it closed.
      } else {
        showAlert(result.message || "Failed to send message. Please try again.", false);
      }
    } catch (error) {
      console.error("Error submitting contact form:", error);
      showAlert("Could not connect to server. Please try again later.", false);
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
  const toolbar = document.getElementById("inbox-toolbar");

  if (!inboxSection || !inboxContainer) return;

  // Initialize state
  if (!window.inboxState) {
    window.inboxState = {
      contacts: [],
      currentPage: 1,
      pageSize: 5,
      selectedIds: new Set(),
      email: email
    };
    initInboxEventListeners(email);
  } else {
    window.inboxState.email = email;
  }

  try {
    const apiUrl = `/api/contacts/my?email=${encodeURIComponent(email)}`;
    const token = sessionStorage.getItem("token") || localStorage.getItem("token") || "";

    const response = await fetch(apiUrl, {
      headers: { "Authorization": "Bearer " + token }
    });
    if (!response.ok) throw new Error(`Failed to fetch inbox: ${response.status}`);
    
    const contacts = await response.json();
    window.inboxState.contacts = contacts;
    window.inboxState.selectedIds.clear(); // Reset selections
    
    // Update "Select All" checkbox
    const selectAllCheckbox = document.getElementById("select-all-checkbox");
    if (selectAllCheckbox) selectAllCheckbox.checked = false;

    renderInboxPage();

    // Show/hide toolbar based on message count
    if (toolbar) {
      toolbar.style.display = contacts.length > 0 ? "flex" : "none";
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
        <p>Could not load inbox. Error: ${error.message}</p>
      </div>
    `;
    inboxSection.style.display = "block";
  }
}

function renderInboxPage() {
  const container = document.getElementById("inbox-container");
  const paginationContainer = document.getElementById("inbox-pagination");
  if (!container) return;

  const state = window.inboxState;
  const contacts = state.contacts;
  
  if (contacts.length === 0) {
    container.innerHTML = `
      <div style="text-align:center;padding:3rem;color:var(--text-muted);">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="width:64px;height:64px;margin:0 auto 1rem;opacity:0.5;"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"></path></svg>
        <p>No messages found. You can send a message on the Contact page to test.</p>
      </div>
    `;
    if (paginationContainer) paginationContainer.style.display = "none";
    updateDeleteSelectedBtnState();
    return;
  }

  // Calculate page bounds
  const totalItems = contacts.length;
  const totalPages = Math.ceil(totalItems / state.pageSize);
  
  // Guard current page
  if (state.currentPage > totalPages) {
    state.currentPage = Math.max(1, totalPages);
  }
  
  const startIndex = (state.currentPage - 1) * state.pageSize;
  const endIndex = Math.min(startIndex + state.pageSize, totalItems);
  const pageItems = contacts.slice(startIndex, endIndex);

  container.innerHTML = "";

  pageItems.forEach(contact => {
    const card = document.createElement("div");
    card.className = "inbox-card";

    const createdAt = new Date(contact.createdAt).toLocaleDateString("en-US", {
      hour: "2-digit", minute: "2-digit",
      day: "2-digit", month: "2-digit", year: "numeric"
    });

    const repliedAt = contact.repliedAt ? new Date(contact.repliedAt).toLocaleDateString("en-US", {
      hour: "2-digit", minute: "2-digit",
      day: "2-digit", month: "2-digit", year: "numeric"
    }) : null;

    const isChecked = state.selectedIds.has(contact.id) ? "checked" : "";

    card.innerHTML = `
      <div style="display:flex; gap: 1.25rem; align-items: flex-start;">
        <input type="checkbox" class="message-checkbox" data-id="${contact.id}" ${isChecked} style="width: 18px; height: 18px; cursor: pointer; accent-color: #00f0ff; margin-top: 0.25rem; flex-shrink: 0;">
        <div style="flex-grow: 1; min-width: 0;">
          <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:1rem;gap:1rem;flex-wrap:wrap;">
            <div>
              <h3 style="font-size:1.125rem;font-weight:700;color:var(--text-dark);margin:0 0 0.25rem;">${escapeHtml(contact.title)}</h3>
              <p style="font-size:0.875rem;color:var(--text-muted);margin:0;">Sent at ${createdAt}</p>
            </div>
            <div style="display:flex; align-items:center; gap:0.75rem;">
              <span class="status-badge ${contact.status === 'DONE' ? 'status-done' : 'status-pending'}" style="padding:0.35rem 0.75rem;font-size:0.75rem;">${escapeHtml(contact.status)}</span>
              <button class="delete-single-btn" data-id="${contact.id}" style="background:transparent; border:none; color:#f87171; cursor:pointer; padding:0.35rem; border-radius:50%; transition:all 0.2s ease; display:flex; align-items:center; justify-content:center;" title="Delete Message">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
              </button>
            </div>
          </div>
          <div class="inbox-message-box">
            <h4 style="font-size:0.875rem;font-weight:600;color:var(--text-dark);margin:0 0 0.5rem;">Your message:</h4>
            <p style="font-size:0.875rem;color:var(--text-muted);margin:0;white-space:pre-line;">${escapeHtml(contact.content)}</p>
          </div>
          ${contact.reply ? `
            <div class="inbox-reply-box">
              <h4 style="font-size:0.875rem;font-weight:600;color:#059669;margin:0 0 0.5rem;">Response from team${repliedAt ? ` (${repliedAt})` : ''}:</h4>
              <p style="font-size:0.875rem;color:#065f46;margin:0;white-space:pre-line;">${escapeHtml(contact.reply)}</p>
            </div>
          ` : `
            <div class="inbox-pending-box">
              <h4 style="font-size:0.875rem;font-weight:600;color:#d97706;margin:0 0 0.5rem;">Status:</h4>
              <p style="font-size:0.875rem;color:#b45309;margin:0;">Awaiting response...</p>
            </div>
          `}
        </div>
      </div>
    `;
    container.appendChild(card);
  });

  // Render pagination controls
  if (paginationContainer) {
    if (totalPages <= 1) {
      paginationContainer.style.display = "none";
    } else {
      paginationContainer.style.display = "flex";
      paginationContainer.innerHTML = "";

      // Previous button
      const prevBtn = document.createElement("button");
      prevBtn.className = "pagination-btn";
      prevBtn.innerHTML = "&laquo; Prev";
      prevBtn.disabled = state.currentPage === 1;
      prevBtn.style.cssText = "background:rgba(255,255,255,0.05); color:var(--text-dark); border:1px solid var(--border-color); padding:0.4rem 1rem; border-radius:50px; font-weight:600; font-size:0.85rem; cursor:pointer; transition:all 0.2s;";
      if (prevBtn.disabled) {
        prevBtn.style.opacity = "0.4";
        prevBtn.style.cursor = "not-allowed";
      } else {
        prevBtn.addEventListener("click", () => {
          state.currentPage--;
          renderInboxPage();
        });
      }
      paginationContainer.appendChild(prevBtn);

      // Page numbers
      for (let i = 1; i <= totalPages; i++) {
        const pageBtn = document.createElement("button");
        pageBtn.className = "pagination-btn";
        pageBtn.textContent = i;
        pageBtn.style.cssText = "width:36px; height:36px; display:flex; align-items:center; justify-content:center; border-radius:50%; font-weight:600; font-size:0.85rem; border:1px solid var(--border-color); transition:all 0.2s; cursor:pointer;";
        
        if (i === state.currentPage) {
          pageBtn.style.background = "linear-gradient(135deg, #00f0ff, #0070f3)";
          pageBtn.style.color = "#fff";
          pageBtn.style.borderColor = "transparent";
          pageBtn.style.boxShadow = "0 0 10px rgba(0, 240, 255, 0.3)";
        } else {
          pageBtn.style.background = "rgba(255,255,255,0.05)";
          pageBtn.style.color = "var(--text-dark)";
          pageBtn.addEventListener("click", () => {
            state.currentPage = i;
            renderInboxPage();
          });
        }
        paginationContainer.appendChild(pageBtn);
      }

      // Next button
      const nextBtn = document.createElement("button");
      nextBtn.className = "pagination-btn";
      nextBtn.innerHTML = "Next &raquo;";
      nextBtn.disabled = state.currentPage === totalPages;
      nextBtn.style.cssText = "background:rgba(255,255,255,0.05); color:var(--text-dark); border:1px solid var(--border-color); padding:0.4rem 1rem; border-radius:50px; font-weight:600; font-size:0.85rem; cursor:pointer; transition:all 0.2s;";
      if (nextBtn.disabled) {
        nextBtn.style.opacity = "0.4";
        nextBtn.style.cursor = "not-allowed";
      } else {
        nextBtn.addEventListener("click", () => {
          state.currentPage++;
          renderInboxPage();
        });
      }
      paginationContainer.appendChild(nextBtn);
    }
  }

  updateDeleteSelectedBtnState();
}

function updateDeleteSelectedBtnState() {
  const deleteSelectedBtn = document.getElementById("delete-selected-btn");
  if (!deleteSelectedBtn) return;
  
  const state = window.inboxState;
  const count = state.selectedIds.size;
  
  if (count > 0) {
    deleteSelectedBtn.removeAttribute("disabled");
    deleteSelectedBtn.style.background = "rgba(239, 68, 68, 0.3)";
    deleteSelectedBtn.style.color = "#fca5a5";
    deleteSelectedBtn.style.borderColor = "rgba(239, 68, 68, 0.6)";
  } else {
    deleteSelectedBtn.setAttribute("disabled", "true");
    deleteSelectedBtn.style.background = "rgba(239, 68, 68, 0.15)";
    deleteSelectedBtn.style.color = "#f87171";
    deleteSelectedBtn.style.borderColor = "rgba(239, 68, 68, 0.3)";
    deleteSelectedBtn.style.opacity = "0.5";
  }
}

function initInboxEventListeners(email) {
  const container = document.getElementById("inbox-container");
  const selectAll = document.getElementById("select-all-checkbox");
  const deleteSelected = document.getElementById("delete-selected-btn");
  const deleteAll = document.getElementById("delete-all-btn");

  if (!container) return;

  // Handle single item deletion and individual checkboxes delegation
  container.addEventListener("click", async (e) => {
    // Check if clicked delete single button
    const deleteBtn = e.target.closest(".delete-single-btn");
    if (deleteBtn) {
      const id = deleteBtn.getAttribute("data-id");
      if (confirm("Are you sure you want to delete this message? This action cannot be undone.")) {
        await executeDelete(`/api/contacts/${id}`, "DELETE");
        fetchInbox(email);
      }
      return;
    }
    
    // Check if clicked individual checkbox
    const checkbox = e.target.closest(".message-checkbox");
    if (checkbox) {
      const id = parseInt(checkbox.getAttribute("data-id"), 10);
      const state = window.inboxState;
      if (checkbox.checked) {
        state.selectedIds.add(id);
      } else {
        state.selectedIds.delete(id);
      }
      
      // Update select-all checkbox state
      if (selectAll) {
        const visibleCheckboxes = container.querySelectorAll(".message-checkbox");
        const allChecked = Array.from(visibleCheckboxes).every(cb => cb.checked);
        selectAll.checked = allChecked && visibleCheckboxes.length > 0;
      }
      
      updateDeleteSelectedBtnState();
    }
  });

  // Handle select all checkbox
  if (selectAll) {
    selectAll.addEventListener("change", (e) => {
      const state = window.inboxState;
      const checked = e.target.checked;
      
      // Select/deselect items on the CURRENT page
      const currentCheckboxes = container.querySelectorAll(".message-checkbox");
      currentCheckboxes.forEach(checkbox => {
        checkbox.checked = checked;
        const id = parseInt(checkbox.getAttribute("data-id"), 10);
        if (checked) {
          state.selectedIds.add(id);
        } else {
          state.selectedIds.delete(id);
        }
      });
      
      updateDeleteSelectedBtnState();
    });
  }

  // Handle delete selected button
  if (deleteSelected) {
    deleteSelected.addEventListener("click", async () => {
      const state = window.inboxState;
      if (state.selectedIds.size === 0) return;
      
      if (confirm(`Are you sure you want to delete the ${state.selectedIds.size} selected message(s)? This action cannot be undone.`)) {
        const idsArray = Array.from(state.selectedIds);
        const idsParam = idsArray.join(",");
        await executeDelete(`/api/contacts/my?ids=${idsParam}`, "DELETE");
        fetchInbox(email);
      }
    });
  }

  // Handle delete all button
  if (deleteAll) {
    deleteAll.addEventListener("click", async () => {
      if (confirm("Are you sure you want to delete ALL messages in your inbox? This action cannot be undone.")) {
        await executeDelete("/api/contacts/my", "DELETE");
        fetchInbox(email);
      }
    });
  }
}

async function executeDelete(url, method) {
  try {
    const token = sessionStorage.getItem("token") || localStorage.getItem("token") || "";
    const response = await fetch(url, {
      method: method,
      headers: {
        "Authorization": "Bearer " + token
      }
    });
    const result = await response.json();
    if (response.ok && result.success !== false) {
      alert(result.message || "Deletion successful!");
    } else {
      alert(result.message || "Failed to delete message(s).");
    }
  } catch (error) {
    console.error("Delete operation failed:", error);
    alert("An error occurred during deletion. Please try again.");
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
    <a id="quick-inbox" href="inbox.html" class="quick-panel-btn" aria-label="Inbox" style="display:none;">
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
    if (quickInbox) {
      quickInbox.style.display = "flex";
      quickInbox.addEventListener("click", (e) => {
        const path = window.location.pathname;
        const page = path.substring(path.lastIndexOf('/') + 1) || "index.html";
        if (page === "inbox.html") {
          e.preventDefault();
          const section = document.getElementById("inbox-section");
          if (section) {
            section.scrollIntoView({ behavior: "smooth" });
          }
        }
      });
    }
  }
}

// =========================================================================
// PROJECT MILESTONE ADMIN PANEL LOGIC (UC-12)
// =========================================================================

let currentAdminProjectId = null;

function openMilestoneModal(projectId) {
  currentAdminProjectId = projectId;
  
  const project = _cache.projects[projectId];
  const titleEl = document.getElementById("milestone-project-title");
  if (titleEl && project) {
    titleEl.textContent = `Manage Milestones: ${project.title}`;
  }

  const overlay = document.getElementById("milestone-modal-overlay");
  if (overlay) {
    overlay.classList.add("is-open");
  }

  // Clear form and audit logs panel
  const form = document.getElementById("admin-milestone-form");
  if (form) form.reset();
  closeAuditTrail();

  fetchAndRenderAdminMilestones(projectId);
}

function closeMilestoneModal() {
  currentAdminProjectId = null;
  const overlay = document.getElementById("milestone-modal-overlay");
  if (overlay) {
    overlay.classList.remove("is-open");
  }
  closeAuditTrail();
}

async function fetchAndRenderAdminMilestones(projectId) {
  const container = document.getElementById("admin-milestones-list");
  if (!container) return;

  container.innerHTML = `<p style="color: var(--text-muted); text-align:center; padding: 2rem 0; font-size:0.85rem;">Loading milestones...</p>`;

  try {
    const response = await fetch(`/api/projects/${projectId}/milestones`);
    if (!response.ok) throw new Error("Failed to load milestones");
    const milestones = await response.json();

    if (!milestones || milestones.length === 0) {
      container.innerHTML = `<p style="color: var(--text-muted); text-align:center; padding: 2rem 0; font-size:0.85rem;">No milestones found. Create one on the right panel!</p>`;
      return;
    }

    container.innerHTML = milestones.map(m => {
      return `
        <div class="milestone-row-box" id="admin-milestone-row-${m.id}">
          <div style="display:flex; justify-content:space-between; align-items:center; gap:0.5rem;">
            <div>
              <strong style="color:var(--text-dark); font-size:0.9rem; display:block;">${escapeHtml(m.name)}</strong>
              ${m.description ? `<span style="color:var(--text-muted); font-size:0.75rem; display:block; margin-top:0.1rem;">${escapeHtml(m.description)}</span>` : ''}
              ${m.dueDate ? `<span style="color:var(--text-muted); font-size:0.7rem; display:block; margin-top:0.15rem;">📅 Due: ${m.dueDate}</span>` : ''}
            </div>
            <div style="display:flex; gap:0.35rem; align-items:center;">
              <span class="status-badge status-${m.status}" 
                    style="font-size:0.75rem; font-weight:700; padding:3px 10px; border-radius:20px; text-transform:uppercase;
                    ${m.status==='COMPLETED'?'background:rgba(16,185,129,0.12);color:#10b981':m.status==='IN_PROGRESS'?'background:rgba(59,130,246,0.12);color:#3b82f6':m.status==='BLOCKED'?'background:rgba(239,68,68,0.12);color:#ef4444':'background:rgba(100,116,139,0.12);color:#64748b'}">
                ${m.status.replace('_', ' ')}
              </span>
            </div>
          </div>

          <!-- Static progress bar instead of range slider -->
          <div style="display:flex; align-items:center; gap:0.5rem; margin: 0.5rem 0;">
            <span style="font-size:0.75rem; color:var(--text-muted); font-weight:600;">Progress:</span>
            <div style="flex:1; height:6px; background:#e2e8f0; border-radius:10px; overflow:hidden;">
              <div style="width:${m.progressPercentage}%; height:100%; background:linear-gradient(90deg, #3b82f6, #06b6d4); border-radius:10px;"></div>
            </div>
            <span style="font-size:0.75rem; font-weight:700; color:#2563eb;">${m.progressPercentage}%</span>
          </div>

          <div style="display:flex; justify-content:space-between; align-items:center; margin-top:0.25rem;">
            <button onclick="openAuditTrail(${m.id}, '${escapeHtml(m.name)}')" 
                    style="background:none; border:none; color:#2563eb; font-size:0.75rem; font-weight:600; cursor:pointer; display:flex; align-items:center; gap:0.15rem;">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:12px;height:12px;"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>
              View logs
            </button>
            <button onclick="deleteAdminMilestone(${m.id}, '${escapeHtml(m.name)}')" 
                    style="background:none; border:none; color:#ef4444; font-size:0.75rem; font-weight:600; cursor:pointer; display:flex; align-items:center; gap:0.15rem;">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width:12px;height:12px;"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
              Delete
            </button>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    console.error("fetchAndRenderAdminMilestones error:", err);
    container.innerHTML = `<p style="color:#ef4444; text-align:center; padding: 2rem 0; font-size:0.85rem;">Could not load milestones list.</p>`;
  }
}


async function deleteAdminMilestone(milestoneId, milestoneName) {
  if (!currentAdminProjectId) return;
  if (!confirm(`Are you sure you want to delete milestone '${milestoneName}'?`)) return;

  const token = localStorage.getItem("token") || sessionStorage.getItem("token");
  try {
    const response = await fetch(`/api/projects/${currentAdminProjectId}/milestones/${milestoneId}`, {
      method: "DELETE",
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });

    if (!response.ok) {
      const data = await response.json();
      alert(data.message || "Failed to delete milestone.");
      return;
    }

    fetchAndRenderAdminMilestones(currentAdminProjectId);
    closeAuditTrail();
  } catch (err) {
    console.error("deleteAdminMilestone error:", err);
    alert("An error occurred while deleting milestone.");
  }
}

async function openAuditTrail(milestoneId, milestoneName) {
  if (!currentAdminProjectId) return;

  const wrapper = document.getElementById("admin-milestone-audit-wrapper");
  const nameEl = document.getElementById("audit-milestone-name");
  const list = document.getElementById("admin-milestone-audit-list");

  if (!wrapper || !nameEl || !list) return;

  nameEl.textContent = milestoneName;
  wrapper.style.display = "block";
  list.innerHTML = `<p style="color:var(--text-muted); font-size:0.8rem;">Loading mutation logs...</p>`;

  try {
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
    const response = await fetch(`/api/projects/${currentAdminProjectId}/milestones/${milestoneId}/logs`, {
      headers: {
        "Authorization": `Bearer ${token}`
      }
    });

    if (!response.ok) throw new Error("Failed");
    const logs = await response.json();

    if (!logs || logs.length === 0) {
      list.innerHTML = `<p style="color:var(--text-muted); font-size:0.8rem; text-align:center;">No mutation history logs recorded yet.</p>`;
      return;
    }

    list.innerHTML = logs.map(l => {
      const date = new Date(l.performedAt).toLocaleString();
      let detail = "";
      let labelClass = "sync";

      if (l.actionType === "CREATE") {
        detail = `Created milestone as '${escapeHtml(l.newValue)}'`;
        labelClass = "create";
      } else if (l.actionType === "DELETE") {
        detail = `Deleted milestone '${escapeHtml(l.oldValue)}'`;
        labelClass = "delete";
      } else {
        detail = `Changed <b>${escapeHtml(l.fieldName)}</b> from <i>"${escapeHtml(l.oldValue)}"</i> to <i>"${escapeHtml(l.newValue)}"</i>`;
      }

      return `
        <div class="audit-log-line ${labelClass}">
          [${date}] <b>${escapeHtml(l.performedBy)}</b>: ${detail}
        </div>
      `;
    }).join('');

  } catch (err) {
    console.error("openAuditTrail error:", err);
    list.innerHTML = `<p style="color:#ef4444; font-size:0.8rem;">Could not load audit logs.</p>`;
  }
}

function closeAuditTrail() {
  const wrapper = document.getElementById("admin-milestone-audit-wrapper");
  if (wrapper) {
    wrapper.style.display = "none";
  }
}

// =============================================
//  ASSIGNMENT MODAL — Admin-only project assignments
// =============================================

let currentAssignmentProjectId = null;

async function openAssignmentModal(projectId, projectTitle) {
  currentAssignmentProjectId = projectId;
  document.getElementById("assignment-project-title").textContent = `👥 Assignments: ${projectTitle}`;
  
  const overlay = document.getElementById("assignment-modal-overlay");
  if (overlay) {
    overlay.classList.add("is-open");
  }
  await loadAssignmentData(projectId);
}

function closeAssignmentModal() {
  const overlay = document.getElementById("assignment-modal-overlay");
  if (overlay) {
    overlay.classList.remove("is-open");
  }
  currentAssignmentProjectId = null;
}

async function loadAssignmentData(projectId) {
  const token = localStorage.getItem("token") || localStorage.getItem("authToken");
  const headers = { "Authorization": `Bearer ${token}` };

  // Load existing assignments and clients
  const [assignRes, clientRes, allUsersRes] = await Promise.all([
    fetch(`/api/projects/${projectId}/assignments`, { headers }),
    fetch(`/api/projects/${projectId}/clients`,     { headers }),
    fetch(`/api/admin/users`,                        { headers }).catch(() => ({ ok: false }))
  ]);

  const assignments = assignRes.ok ? await assignRes.json() : [];
  const clients     = clientRes.ok ? await clientRes.json() : [];
  const allUsers    = allUsersRes.ok ? await allUsersRes.json() : [];

  renderAssignmentList(assignments);
  renderClientList(clients);
  populateMemberDropdown(allUsers.filter(u => u.role === "ROLE_MEMBER"), assignments.map(a => a.userId));
  populateClientDropdown(allUsers.filter(u => u.role === "ROLE_USER"), clients.map(c => c.userId));
}

function renderAssignmentList(assignments) {
  const el = document.getElementById("assignment-member-list");
  if (!assignments.length) {
    el.innerHTML = '<p style="color:#64748b;font-size:0.85rem;">No members assigned yet.</p>';
    return;
  }
  el.innerHTML = assignments.map(a => `
    <div style="display:flex;align-items:center;justify-content:space-between;padding:8px 10px;background:#fff;border:1px solid #e2e8f0;border-radius:8px;">
      <div>
        <strong style="font-size:0.88rem;">${escapeHtml(a.fullName)}</strong>
        <span style="font-size:0.75rem;color:#64748b;margin-left:6px;">(${escapeHtml(a.username)})</span><br/>
        <span style="font-size:0.75rem;padding:2px 8px;border-radius:10px;font-weight:700;${a.projectRole==='PM'?'background:rgba(37,99,235,.12);color:#2563eb':'background:rgba(245,158,11,.12);color:#d97706'}">
          ${a.projectRole === 'PM' ? '★ PM' : '⚙ STAFF'}
        </span>
      </div>
      <button onclick="removeAssignment(${a.userId})" style="background:none;border:none;color:#ef4444;cursor:pointer;font-size:0.75rem;text-decoration:underline;">Remove</button>
    </div>`).join('');
}

function renderClientList(clients) {
  const el = document.getElementById("assignment-client-list");
  if (!clients.length) {
    el.innerHTML = '<p style="color:#64748b;font-size:0.85rem;">No clients linked yet.</p>';
    return;
  }
  el.innerHTML = clients.map(c => `
    <div style="display:flex;align-items:center;justify-content:space-between;padding:8px 10px;background:#fff;border:1px solid #e2e8f0;border-radius:8px;">
      <div>
        <strong style="font-size:0.88rem;">${escapeHtml(c.fullName)}</strong>
        <span style="font-size:0.75rem;color:#64748b;margin-left:6px;">${escapeHtml(c.email)}</span>
      </div>
      <button onclick="removeClient(${c.userId})" style="background:none;border:none;color:#ef4444;cursor:pointer;font-size:0.75rem;text-decoration:underline;">Unlink</button>
    </div>`).join('');
}

function populateMemberDropdown(members, alreadyAssignedIds) {
  const sel = document.getElementById("assign-user-select");
  sel.innerHTML = '<option value="">— Select a member —</option>';
  members.forEach(u => {
    if (!alreadyAssignedIds.includes(u.id)) {
      sel.innerHTML += `<option value="${u.id}">${escapeHtml(u.fullName)} (${escapeHtml(u.username)})</option>`;
    }
  });
}

function populateClientDropdown(users, alreadyLinkedIds) {
  const sel = document.getElementById("assign-client-select");
  sel.innerHTML = '<option value="">— Select a client —</option>';
  users.forEach(u => {
    if (!alreadyLinkedIds.includes(u.id)) {
      sel.innerHTML += `<option value="${u.id}">${escapeHtml(u.fullName)} (${escapeHtml(u.email)})</option>`;
    }
  });
}

async function submitAssignMember() {
  const userId = document.getElementById("assign-user-select").value;
  const role   = document.getElementById("assign-role-select").value;
  if (!userId) { alert("Please select a member."); return; }

  const token = localStorage.getItem("token") || localStorage.getItem("authToken");
  const res = await fetch(`/api/projects/${currentAssignmentProjectId}/assignments`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
    body: JSON.stringify({ userId: parseInt(userId), projectRole: role })
  });
  const data = await res.json();
  if (res.ok) {
    await loadAssignmentData(currentAssignmentProjectId);
  } else {
    alert(data.message || "Failed to assign member.");
  }
}

async function submitAssignClient() {
  const userId = document.getElementById("assign-client-select").value;
  if (!userId) { alert("Please select a client."); return; }

  const token = localStorage.getItem("token") || localStorage.getItem("authToken");
  const res = await fetch(`/api/projects/${currentAssignmentProjectId}/clients`, {
    method: "POST",
    headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
    body: JSON.stringify({ userId: parseInt(userId) })
  });
  const data = await res.json();
  if (res.ok) {
    await loadAssignmentData(currentAssignmentProjectId);
  } else {
    alert(data.message || "Failed to link client.");
  }
}

async function removeAssignment(userId) {
  if (!confirm("Remove this member from the project?")) return;
  const token = localStorage.getItem("token") || localStorage.getItem("authToken");
  await fetch(`/api/projects/${currentAssignmentProjectId}/assignments/${userId}`, {
    method: "DELETE",
    headers: { "Authorization": `Bearer ${token}` }
  });
  await loadAssignmentData(currentAssignmentProjectId);
}

async function removeClient(userId) {
  if (!confirm("Unlink this client from the project?")) return;
  const token = localStorage.getItem("token") || localStorage.getItem("authToken");
  await fetch(`/api/projects/${currentAssignmentProjectId}/clients/${userId}`, {
    method: "DELETE",
    headers: { "Authorization": `Bearer ${token}` }
  });
  await loadAssignmentData(currentAssignmentProjectId);
}

// Hero H1 text click animation
function initHeroTextClick() {
  const heroH1 = document.querySelector(".hero-content h1");
  if (!heroH1) return;

  heroH1.style.cursor = "pointer";
  heroH1.addEventListener("click", () => {
    if (heroH1.classList.contains("hero-text-clicked")) return;
    heroH1.classList.add("hero-text-clicked");
    setTimeout(() => {
      heroH1.classList.remove("hero-text-clicked");
    }, 800);
  });
}

// Navbar scroll effects (detached floating and scroll-to-hide)
function initNavbarScrollEffects() {
  const header = document.querySelector("header");
  if (!header) return;

  let lastScrollY = window.scrollY;
  const scrollThreshold = 10; // minimum scroll down/up before hiding/showing
  const detachThreshold = 30; // scroll Y position where navbar detaches

  window.addEventListener("scroll", () => {
    const currentScrollY = window.scrollY;

    // 1. Detach/Attach logic
    if (currentScrollY > detachThreshold) {
      header.classList.add("header-detached");
    } else {
      header.classList.remove("header-detached");
    }

    // 2. Hide/Show logic (Scroll-to-hide)
    // Only trigger if we scrolled more than the threshold
    if (Math.abs(currentScrollY - lastScrollY) > scrollThreshold) {
      if (currentScrollY > lastScrollY && currentScrollY > 100) {
        // Scrolling down -> hide navbar
        header.classList.add("header-hidden");
      } else {
        // Scrolling up -> show navbar
        header.classList.remove("header-hidden");
      }
    }

    lastScrollY = currentScrollY;
  });
}

