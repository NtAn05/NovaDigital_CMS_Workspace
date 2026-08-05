// Dynamic Data Loading, Authentication, and UI logic for NovaDigital Creative Agency


// Clear persistent authentication keys if a new browser session starts (empty sessionStorage)
// Initialize sidebar collapsed state based on localStorage preference
(function () {
  const isCollapsed = localStorage.getItem("adminSidebarCollapsed") === "true";
  if (isCollapsed) {
    document.body.classList.add("sidebar-collapsed");
  }
})();

// Clear persistent authentication keys if a new browser session starts (empty sessionStorage)
function initSessionClean() {
  const sessionToken = sessionStorage.getItem("token");
  if (!sessionToken) {
    const authKeys = ["token", "authToken", "username", "fullName", "role", "email", "avatarUrl", "user", "userId"];
    authKeys.forEach(key => {
      localStorage.removeItem(key);
    });
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

  // 0b. Initialize Mobile Hamburger Menu
  initMobileMenuToggle();

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

  // 4d. Move footer bottom inside footer container
  initFooterMove();

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
            <!-- Captcha anti-spam: shown after 5 failed login attempts -->
            <div id="modal-captcha-section" style="display:none; margin-bottom:1rem; padding:0.85rem; background:linear-gradient(135deg,#fff7ed,#fef3c7); border:1.5px solid #f59e0b; border-radius:12px;">
              <div style="display:flex; align-items:center; gap:0.4rem; font-size:0.8rem; font-weight:600; color:#92400e; margin-bottom:0.6rem;">
                <svg xmlns='http://www.w3.org/2000/svg' width='13' height='13' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'><path d='M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z'></path></svg>
                Security Verification Required
              </div>
              <div style="display:flex; align-items:center; gap:8px; margin-bottom:0.55rem;">
                <div id="modal-captcha-code" style="flex:1; background:#1e293b; border-radius:8px; padding:0.55rem 0.75rem; text-align:center; font-family:'Courier New',monospace; font-size:1.25rem; font-weight:700; letter-spacing:0.25em; color:#38bdf8; text-shadow:0 0 8px rgba(56,189,248,0.5); user-select:none; border:1px solid rgba(56,189,248,0.2);">------</div>
                <button type="button" id="modal-captcha-refresh" title="New code" style="background:none; border:1.5px solid #d97706; border-radius:7px; padding:0.45rem; cursor:pointer; color:#d97706; display:flex; align-items:center;">
                  <svg xmlns='http://www.w3.org/2000/svg' width='14' height='14' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'><polyline points='23 4 23 10 17 10'></polyline><path d='M20.49 15a9 9 0 1 1-2.12-9.36L23 10'></path></svg>
                </button>
              </div>
              <input type="text" id="modal-captcha-answer" placeholder="Type the code above" autocomplete="off" maxlength="8"
                style="width:100%; padding:0.55rem 0.85rem; border:1.5px solid #d97706; border-radius:8px; font-size:0.9rem; background:#fff; box-sizing:border-box; outline:none;">
            </div>
            <button type="submit" class="submit-btn" style="margin-top:0.5rem;">Login</button>
            <div id="modal-login-alert" class="alert-message"></div>
          </form>

          <button type="button" id="modalGoogleSignInBtn" style="
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.75rem;
            padding: 0.85rem 1.5rem;
            background: #fff;
            border: 1.5px solid #dadce0;
            border-radius: 50px;
            font-size: 0.95rem;
            font-weight: 600;
            color: #3c4043;
            cursor: pointer;
            transition: all 0.2s ease;
            margin-top: 1rem;
            box-shadow: 0 1px 3px rgba(0,0,0,0.08);
          " onmouseover="this.style.background='#f8f9fa'" onmouseout="this.style.background='#fff'">
            <svg width="20" height="20" viewBox="0 0 48 48">
              <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
              <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
              <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
              <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
              <path fill="none" d="M0 0h48v48H0z"/>
            </svg>
            Continue with Google
          </button>

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
            <div class="form-group" id="modal-reg-otp-group" style="display: none;">
              <label for="modal-reg-otp">OTP Code * <span id="modal-reg-otp-timer" style="color: #e63946; font-weight: bold; margin-left: 5px;"></span></label>
              <input type="text" id="modal-reg-otp" placeholder="Enter 6-digit OTP sent to your email">
            </div>
            <button type="submit" class="submit-btn" id="modal-reg-submit-btn" style="margin-top:0.5rem;">Register</button>
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

  const mForm = document.getElementById("modal-loginForm");
  if (mForm) mForm.reset();
  const muInput = document.getElementById("modal-usernameOrEmail");
  const mpInput = document.getElementById("modal-password");
  if (muInput) muInput.value = "";
  if (mpInput) mpInput.value = "";

  overlay.classList.add("is-open");
  document.body.style.overflow = "hidden";
  switchAuthTab(tab);
}

function closeAuthModal() {
  const overlay = document.getElementById("auth-modal-overlay");
  if (!overlay) return;

  const mForm = document.getElementById("modal-loginForm");
  if (mForm) mForm.reset();
  const muInput = document.getElementById("modal-usernameOrEmail");
  const mpInput = document.getElementById("modal-password");
  if (muInput) muInput.value = "";
  if (mpInput) mpInput.value = "";

  overlay.classList.remove("is-open");
  document.body.style.overflow = "";
  // Clear all modal alerts on close
  ["modal-login-alert", "modal-register-alert"].forEach(id => {
    const el = document.getElementById(id);
    if (el) { el.style.display = "none"; el.textContent = ""; el.className = "alert-message"; }
  });
}

function switchAuthTab(tab) {
  const loginTab = document.getElementById("tab-login");
  const registerTab = document.getElementById("tab-register");
  const loginPanel = document.getElementById("panel-login");
  const registerPanel = document.getElementById("panel-register");
  if (!loginTab || !registerTab) return;

  if (tab === "login") {
    loginTab.classList.add("active"); loginTab.setAttribute("aria-selected", "true");
    registerTab.classList.remove("active"); registerTab.setAttribute("aria-selected", "false");
    loginPanel.classList.add("active");
    registerPanel.classList.remove("active");
  } else {
    registerTab.classList.add("active"); registerTab.setAttribute("aria-selected", "true");
    loginTab.classList.remove("active"); loginTab.setAttribute("aria-selected", "false");
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

  // ── Captcha state ──
  let modalCaptchaToken = null;
  let modalCaptchaRequired = false;

  const captchaSection = document.getElementById("modal-captcha-section");
  const captchaCodeEl = document.getElementById("modal-captcha-code");
  const captchaInput = document.getElementById("modal-captcha-answer");
  const captchaRefresh = document.getElementById("modal-captcha-refresh");

  async function loadModalCaptcha() {
    if (!captchaCodeEl) return;
    captchaCodeEl.textContent = "...";
    if (captchaInput) captchaInput.value = "";
    try {
      const res = await fetch("/api/auth/captcha");
      const data = await res.json();
      modalCaptchaToken = data.token;
      captchaCodeEl.textContent = data.code.split("").join(" ");
    } catch (_) {
      captchaCodeEl.textContent = "ERROR";
      modalCaptchaToken = null;
    }
  }

  function showModalCaptchaUI() {
    if (!captchaSection || modalCaptchaRequired) return;
    modalCaptchaRequired = true;
    captchaSection.style.display = "block";
    captchaSection.style.animation = "captchaSlideIn 0.35s ease";
    loadModalCaptcha();
  }

  if (captchaRefresh) {
    captchaRefresh.addEventListener("click", loadModalCaptcha);
  }

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usernameOrEmail = document.getElementById("modal-usernameOrEmail").value.trim();
    const password = document.getElementById("modal-password").value;

    if (!usernameOrEmail || !password) {
      showModalAlert("Please enter your username and password.", false, "modal-login-alert");
      return;
    }

    // Validate captcha if required
    if (modalCaptchaRequired) {
      if (!captchaInput || !captchaInput.value.trim()) {
        showModalAlert("Please enter the captcha code.", false, "modal-login-alert");
        return;
      }
      if (!modalCaptchaToken) {
        showModalAlert("Captcha not loaded. Please refresh and try again.", false, "modal-login-alert");
        await loadModalCaptcha();
        return;
      }
    }

    try {
      showModalAlert("Logging in...", null, "modal-login-alert");

      const body = { usernameOrEmail, password };
      if (modalCaptchaRequired && modalCaptchaToken) {
        body.captchaToken = modalCaptchaToken;
        body.captchaAnswer = captchaInput ? captchaInput.value.trim() : "";
      }

      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      const data = await response.json();

      if (response.ok && data.token) {
        // Write to sessionStorage for route guard and header sync compatibility
        sessionStorage.setItem("token", data.token);
        sessionStorage.setItem("authToken", data.token);
        sessionStorage.setItem("username", data.username);
        sessionStorage.setItem("fullName", data.fullName);
        sessionStorage.setItem("role", data.role);
        sessionStorage.setItem("email", data.email);
        sessionStorage.setItem("avatarUrl", data.avatarUrl || "");
        sessionStorage.setItem("user", JSON.stringify({
          username: data.username,
          fullName: data.fullName,
          email: data.email,
          role: data.role,
          avatarUrl: data.avatarUrl || null
        }));

        showModalAlert("Login successful! Redirecting...", true, "modal-login-alert");

        setTimeout(() => {
          if (data.role === "ROLE_RESOURCE") {
            window.location.href = "resource-allocation.html";
          } else if (data.role === "ROLE_ADMIN") {
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
        // Handle captcha-required from server
        if (data.captchaRequired) {
          showModalCaptchaUI();
          if (modalCaptchaRequired) loadModalCaptcha();
        }
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

  let otpSent = false;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const username = document.getElementById("modal-username").value.trim();
    const fullName = document.getElementById("modal-fullName").value.trim();
    const email = document.getElementById("modal-email").value.trim();
    const phone = document.getElementById("modal-phone").value.trim();
    const password = document.getElementById("modal-reg-password").value;

    if (!username || !fullName || !email || !password) {
      showModalAlert("Please fill in all required fields.", false, "modal-register-alert");
      return;
    }

    const otpGroup = document.getElementById("modal-reg-otp-group");
    const otpInput = document.getElementById("modal-reg-otp");
    const submitBtn = document.getElementById("modal-reg-submit-btn");

    if (!otpSent) {
      // Step 1: Send OTP
      try {
        submitBtn.disabled = true;
        const originalText = submitBtn.textContent;
        submitBtn.textContent = "Sending...";
        showModalAlert("Sending OTP to your email...", null, "modal-register-alert");

        const response = await fetch("/api/auth/register-send-otp", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email })
        });

        const data = await response.json();
        submitBtn.disabled = false;

        if (response.ok && data.success) {
          otpSent = true;
          otpGroup.style.display = "block";
          otpInput.required = true;
          otpInput.value = ""; // Clear any old OTP
          submitBtn.textContent = "Confirm OTP & Register";
          document.getElementById("modal-email").readOnly = true;
          showModalAlert("OTP has been sent to your email.", true, "modal-register-alert");

          const timerSpan = document.getElementById("modal-reg-otp-timer");
          let timeLeft = 60;
          timerSpan.textContent = `(${timeLeft}s)`;

          if (form.dataset.timerId) clearInterval(form.dataset.timerId);

          const timerInterval = setInterval(() => {
            timeLeft--;
            if (timeLeft <= 0) {
              clearInterval(timerInterval);
              timerSpan.textContent = "(Expired)";
              submitBtn.disabled = false;
              submitBtn.textContent = "Resend OTP";
              otpSent = false;
              otpInput.required = false;
              otpInput.value = "";
              const parent = otpInput.closest(".form-group") || otpInput.parentElement;
              if (parent) {
                const errorSpan = parent.querySelector(".error-helper-text");
                if (errorSpan) errorSpan.remove();
                otpInput.classList.remove("invalid");
              }
            } else {
              timerSpan.textContent = `(${timeLeft}s)`;
            }
          }, 1000);
          form.dataset.timerId = timerInterval;
        } else {
          submitBtn.textContent = originalText;
          showModalAlert(data.message || "Failed to send OTP.", false, "modal-register-alert");
        }
      } catch (error) {
        console.error("Modal send OTP error:", error);
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
        showModalAlert("Could not connect to server. Please try again.", false, "modal-register-alert");
      }
    } else {
      // Step 2: Register with OTP
      const otp = otpInput.value.trim();
      if (!otp) {
        showModalAlert("Please enter the OTP code.", false, "modal-register-alert");
        return;
      }

      try {
        submitBtn.disabled = true;
        showModalAlert("Registering...", null, "modal-register-alert");

        const response = await fetch("/api/auth/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ username, fullName, email, phone, password, otp })
        });

        const data = await response.json();
        submitBtn.disabled = false;

        if (response.ok && data.success) {
          if (form.dataset.timerId) clearInterval(form.dataset.timerId);
          document.getElementById("modal-reg-otp-timer").textContent = "";
          showModalAlert("Registration successful! Please log in.", true, "modal-register-alert");
          // Reset form state for next time
          otpSent = false;
          otpGroup.style.display = "none";
          otpInput.required = false;
          submitBtn.textContent = "Register";
          document.getElementById("modal-email").readOnly = false;
          form.reset();

          setTimeout(() => {
            switchAuthTab("login");
            setTimeout(() => {
              showModalAlert("Account created! Please log in.", true, "modal-login-alert");
            }, 80);
          }, 1400);
        } else {
          showModalAlert(
            data.message || "Registration failed.",
            false, "modal-register-alert"
          );
        }
      } catch (error) {
        console.error("Modal register error:", error);
        submitBtn.disabled = false;
        showModalAlert("Could not connect to server. Please try again.", false, "modal-register-alert");
      }
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
  const role = localStorage.getItem("role") || sessionStorage.getItem("role");

  // Dedicated Resource Manager only uses the standalone Resource Allocation workspace.
  if (token && role === "ROLE_RESOURCE") {
    const resourceAllowedPages = ["resource-allocation.html", "hr-recruitment.html", "member-profile.html", "user-profile.html"];
    if (!resourceAllowedPages.includes(page)) {
      window.location.href = "resource-allocation.html";
      return;
    }
  }

  // Admin MUST stay in admin.html or user-profile.html
  if (token && role === "ROLE_ADMIN") {
    if (page !== "admin.html" && page !== "user-profile.html" && page !== "admin-messages.html") {
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
  const protectedPages = ["rented-project.html"];

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
  const navLinks = document.querySelectorAll(".nav-links a, .dropdown-item");

  navLinks.forEach(link => {
    const href = link.getAttribute("href");
    if (href === page) {
      link.classList.add("active");
    } else {
      link.classList.remove("active");
    }
  });
}

function initMobileMenuToggle() {
  const navbar = document.querySelector(".navbar");
  const navLinks = document.querySelector(".nav-links");
  if (!navbar || !navLinks) return;

  let toggleBtn = navbar.querySelector(".mobile-menu-toggle");
  if (!toggleBtn) {
    toggleBtn = document.createElement("button");
    toggleBtn.className = "mobile-menu-toggle";
    toggleBtn.setAttribute("aria-label", "Toggle Navigation Menu");
    toggleBtn.innerHTML = `<svg viewBox="0 0 24 24"><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="18" x2="21" y2="18"/></svg>`;
    navbar.appendChild(toggleBtn);
  }

  toggleBtn.onclick = (e) => {
    e.stopPropagation();
    navLinks.classList.toggle("mobile-open");
  };

  navLinks.querySelectorAll("a").forEach(link => {
    link.addEventListener("click", () => {
      navLinks.classList.remove("mobile-open");
    });
  });

  document.addEventListener("click", (e) => {
    if (!navbar.contains(e.target)) {
      navLinks.classList.remove("mobile-open");
    }
  });
}

// Dynamic Navbar authentication update
function updateNavbarAuth() {
  const navLinksContainer = document.querySelector(".nav-links");
  if (!navLinksContainer) return;

  const token = localStorage.getItem("token") || sessionStorage.getItem("token");
  const role = localStorage.getItem("role") || sessionStorage.getItem("role");
  const fullName = localStorage.getItem("fullName") || sessionStorage.getItem("fullName");

  const isPortalUser = token && (role === "ROLE_ADMIN" || role === "ROLE_MEMBER" || role === "Team_Member" || role === "ROLE_RESOURCE");

  // If Admin, Member, or Resource Manager (HR), hide all standard navigation links (Home, Services, etc.)
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
      let targetPage = "member-contact.html";
      if (role === "ROLE_ADMIN") targetPage = "admin.html";
      if (role === "ROLE_RESOURCE") targetPage = "resource-allocation.html";
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

  // Get the static "Start Project" button (if present in HTML)
  const defaultBtnEl = navLinksContainer.querySelector("#default-get-started");
  const defaultBtnLi = defaultBtnEl ? defaultBtnEl.closest("li") : null;

  if (token) {
    // Hide the static button when user is logged in
    if (defaultBtnLi) defaultBtnLi.style.display = "none";
    const dropdownLi = document.createElement("li");
    dropdownLi.className = "auth-item user-dropdown-container";
    dropdownLi.style.position = "relative";

    const username = localStorage.getItem("username") || sessionStorage.getItem("username");
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
    } else if (role === "ROLE_RESOURCE") {
      menuItemsHtml = `
        <a href="resource-allocation.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/></svg>
          Resource Workspace
        </a>
        <a href="member-profile.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          Profile
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
        <a href="my-bookings.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          My Bookings
        </a>
        <a href="my-applications.html" class="dropdown-item">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>
          My Applications
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

    // Notification bell - visible only when logged in, placed right before avatar dropdown
    injectNotificationBell(navLinksContainer, dropdownLi);

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
    // — Guest: bind the static "Start Project" button → opens auth modal —
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
      li.innerHTML = `<a href="#" id="get-started-btn" class="nav-btn">Start Project</a>`;
      navLinksContainer.appendChild(li);
      document.getElementById("get-started-btn").addEventListener("click", (e) => {
        e.preventDefault();
        openAuthModal("login");
      });
    }
  }

  // Inject theme toggle button next to navbar links
  // injectThemeToggle();
}

// User Logout Logic
async function logoutUser() {
  const token = localStorage.getItem("token") || sessionStorage.getItem("token");
  if (token) {
    try {
      await fetch("/api/auth/logout", {
        method: "POST",
        headers: {
          "Authorization": "Bearer " + token,
          "Content-Type": "application/json"
        }
      });
    } catch (e) {
      console.warn("Logout API call failed:", e);
    }
  }

  // Purge storage
  localStorage.clear();
  sessionStorage.clear();

  // Clear chatbot history explicitly if function exists
  if (typeof clearChatbotHistory === "function") {
    clearChatbotHistory();
  }

  // Perform a clean redirect & hard reset to login anchor
  window.location.href = window.location.origin + "/index.html";

  // Force reload if already on index page so Navbar state resets instantly
  if (window.location.pathname.endsWith("index.html") || window.location.pathname === "/") {
    window.location.reload();
  }
}

// =============================================
//  Standalone Page Forms (login.html / register.html)
// =============================================

function initLoginForm() {
  const form = document.getElementById("loginForm");
  const alertMsg = document.getElementById("alertMessage");
  if (!form || !alertMsg) return;

  const uInput = document.getElementById("usernameOrEmail");
  const pInput = document.getElementById("password");

  const clearLoginForm = () => {
    form.reset();
    if (uInput) uInput.value = "";
    if (pInput) pInput.value = "";
  };

  // Force clear inputs on load and delay clear to override browser autofill
  clearLoginForm();
  setTimeout(clearLoginForm, 50);
  setTimeout(clearLoginForm, 200);
  window.addEventListener("pageshow", clearLoginForm);

  // ── Captcha state ──
  let loginCaptchaToken = null;
  let captchaRequired = false;

  const captchaSection = document.getElementById("loginCaptchaSection");
  const captchaCodeEl = document.getElementById("loginCaptchaCode");
  const captchaInput = document.getElementById("loginCaptchaAnswer");
  const captchaRefresh = document.getElementById("loginCaptchaRefresh");

  async function loadLoginCaptcha() {
    if (!captchaCodeEl) return;
    captchaCodeEl.textContent = "...";
    if (captchaInput) captchaInput.value = "";
    try {
      const res = await fetch("/api/auth/captcha");
      const data = await res.json();
      loginCaptchaToken = data.token;
      // Render code as spaced individual characters for stylized look
      captchaCodeEl.textContent = data.code.split("").join(" ");
    } catch (_) {
      captchaCodeEl.textContent = "ERROR";
      loginCaptchaToken = null;
    }
  }

  function showCaptcha() {
    if (!captchaSection || captchaRequired) return;
    captchaRequired = true;
    captchaSection.classList.add("active");
    loadLoginCaptcha();
  }

  if (captchaRefresh) {
    captchaRefresh.addEventListener("click", loadLoginCaptcha);
  }

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
    const password = document.getElementById("password").value;

    if (!usernameOrEmail || !password) {
      showAlert("Please enter your username and password.", false);
      return;
    }

    // Validate captcha fields if required
    if (captchaRequired) {
      if (!captchaInput || !captchaInput.value.trim()) {
        showAlert("Please enter the captcha code.", false);
        return;
      }
      if (!loginCaptchaToken) {
        showAlert("Captcha not loaded. Please refresh the code and try again.", false);
        await loadLoginCaptcha();
        return;
      }
    }

    try {
      showAlert("Logging in...", null);

      const body = { usernameOrEmail, password };
      if (captchaRequired && loginCaptchaToken) {
        body.captchaToken = loginCaptchaToken;
        body.captchaAnswer = captchaInput ? captchaInput.value.trim() : "";
      }

      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
      });

      const data = await response.json();

      if (response.ok && data.token) {
        // Write to sessionStorage for route guard and header sync compatibility
        sessionStorage.setItem("token", data.token);
        sessionStorage.setItem("authToken", data.token);
        sessionStorage.setItem("username", data.username);
        sessionStorage.setItem("fullName", data.fullName);
        sessionStorage.setItem("role", data.role);
        sessionStorage.setItem("email", data.email);
        sessionStorage.setItem("avatarUrl", data.avatarUrl || "");
        sessionStorage.setItem("user", JSON.stringify({
          username: data.username,
          fullName: data.fullName,
          email: data.email,
          role: data.role,
          avatarUrl: data.avatarUrl || null
        }));

        showAlert("Login successful! Redirecting...", true);

        setTimeout(() => {
          if (data.role === "ROLE_RESOURCE") {
            window.location.href = "resource-allocation.html";
          } else if (data.role === "ROLE_ADMIN") {
            window.location.href = "admin.html";
          } else if (data.role === "ROLE_MEMBER") {
            // Internal team member — goes to PM Dashboard
            window.location.href = "member-contact.html";
          } else if (data.role === "ROLE_USER") {
            // External client — goes to Client Portal
            window.location.href = "index.html";
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
        // Handle captcha-required response from server
        if (data.captchaRequired) {
          showCaptcha();
          // If wrong captcha, reload a fresh one
          if (captchaRequired) loadLoginCaptcha();
        }
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
  const form = document.getElementById("registerForm");
  const alertMsg = document.getElementById("alertMessage");
  if (!form || !alertMsg) return;

  let otpSent = false;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const username = document.getElementById("username").value.trim();
    const fullName = document.getElementById("fullName").value.trim();
    const email = document.getElementById("email").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const password = document.getElementById("password").value;

    if (!username || !fullName || !email || !password) {
      showAlert("Please fill in all required fields.", false);
      return;
    }

    const otpGroup = document.getElementById("reg-otp-group");
    const otpInput = document.getElementById("reg-otp");
    const submitBtn = document.getElementById("reg-submit-btn");

    if (!otpSent) {
      // Step 1: Send OTP
      try {
        submitBtn.disabled = true;
        const originalText = submitBtn.textContent;
        submitBtn.textContent = "Sending...";
        showAlert("Sending OTP to your email...", null);

        const response = await fetch("/api/auth/register-send-otp", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email })
        });

        const data = await response.json();
        submitBtn.disabled = false;

        if (response.ok && data.success) {
          otpSent = true;
          otpGroup.style.display = "block";
          otpInput.required = true;
          otpInput.value = ""; // Clear any old OTP
          submitBtn.textContent = "Confirm OTP & Register";
          document.getElementById("email").readOnly = true;
          showAlert("OTP has been sent to your email.", true);

          const timerSpan = document.getElementById("reg-otp-timer");
          let timeLeft = 60;
          timerSpan.textContent = `(${timeLeft}s)`;

          if (form.dataset.timerId) clearInterval(form.dataset.timerId);

          const timerInterval = setInterval(() => {
            timeLeft--;
            if (timeLeft <= 0) {
              clearInterval(timerInterval);
              timerSpan.textContent = "(Expired)";
              submitBtn.disabled = false;
              submitBtn.textContent = "Resend OTP";
              otpSent = false;
              otpInput.required = false;
              otpInput.value = "";
              const parent = otpInput.closest(".floating-form-group") || otpInput.parentElement;
              if (parent) {
                const errorSpan = parent.querySelector(".error-helper-text");
                if (errorSpan) errorSpan.remove();
                otpInput.classList.remove("invalid");
              }
            } else {
              timerSpan.textContent = `(${timeLeft}s)`;
            }
          }, 1000);
          form.dataset.timerId = timerInterval;
        } else {
          submitBtn.textContent = originalText;
          showAlert(data.message || "Failed to send OTP.", false);
        }
      } catch (error) {
        console.error("Send OTP error:", error);
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
        showAlert("Could not connect to server. Please try again.", false);
      }
    } else {
      // Step 2: Register with OTP
      const otp = otpInput.value.trim();
      if (!otp) {
        showAlert("Please enter the OTP code.", false);
        return;
      }

      try {
        submitBtn.disabled = true;
        showAlert("Registering...", null);

        const response = await fetch("/api/auth/register", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ username, fullName, email, phone, password, otp })
        });

        const data = await response.json();
        submitBtn.disabled = false;

        if (response.ok && data.success) {
          if (form.dataset.timerId) clearInterval(form.dataset.timerId);
          document.getElementById("reg-otp-timer").textContent = "";
          showAlert("Registration successful! Redirecting to login...", true);
          setTimeout(() => { window.location.href = "login.html?registered=true"; }, 1500);
        } else {
          showAlert(data.message || "Registration failed.", false);
        }
      } catch (error) {
        console.error("Registration error:", error);
        submitBtn.disabled = false;
        showAlert("Could not connect to server. Please try again.", false);
      }
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
  fetchAdminBookings();
  fetchAdminDashboardStats();

  // Handle cross-page panel routing via URL query parameter
  const urlParams = new URLSearchParams(window.location.search);
  const panelParam = urlParams.get('panel');
  if (panelParam) {
    const navLink = document.getElementById('nav-' + panelParam);
    if (navLink) {
      switchAdminPanel(panelParam, navLink);
    }
  }

  setupAdminSSE();
}

function setupAdminSSE() {
  const token = getAdminToken();
  if (!token) return;

  const eventSource = new EventSource('/api/notifications/stream?token=' + encodeURIComponent(token));

  eventSource.addEventListener('new-booking', (e) => {
    try {
      const data = JSON.parse(e.data);
      showAdminNotificationPopup('New Booking Received!', data.message);
      // Auto refresh bookings if the panel is active
      const panel = document.getElementById('panel-bookings');
      if (panel && panel.classList.contains('active')) {
        fetchAdminBookings();
      }

      // Update the bell icon badge
      if (typeof loadNotificationUnreadCount === 'function') {
        loadNotificationUnreadCount();
      }
      // If the dropdown is open, refresh the list
      const dropdown = document.getElementById('notification-dropdown');
      if (dropdown && dropdown.style.display === 'block' && typeof loadNotificationList === 'function') {
        loadNotificationList();
      }

    } catch (err) {
      console.error('Error parsing SSE new-booking event:', err);
    }
  });

  eventSource.onerror = (err) => {
    console.warn('Admin SSE connection error, it might reconnect automatically.', err);
  };
}

function showAdminNotificationPopup(title, message) {
  // Create a toast notification
  const containerId = 'admin-toast-container';
  let container = document.getElementById(containerId);
  if (!container) {
    container = document.createElement('div');
    container.id = containerId;
    container.style.position = 'fixed';
    container.style.top = '20px';
    container.style.right = '20px';
    container.style.zIndex = '9999';
    container.style.display = 'flex';
    container.style.flexDirection = 'column';
    container.style.gap = '10px';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.style.background = '#fff';
  toast.style.borderLeft = '4px solid #2563eb';
  toast.style.boxShadow = '0 10px 25px rgba(0,0,0,0.1)';
  toast.style.borderRadius = '8px';
  toast.style.padding = '1rem 1.25rem';
  toast.style.minWidth = '300px';
  toast.style.animation = 'fadeUp 0.3s ease';
  toast.style.display = 'flex';
  toast.style.flexDirection = 'column';
  toast.style.cursor = 'pointer';

  toast.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.25rem;">
      <strong style="color: #1e293b; font-size: 0.95rem;">${title}</strong>
      <button style="background: none; border: none; cursor: pointer; color: #94a3b8; font-size: 1.1rem; line-height: 1;">&times;</button>
    </div>
    <div style="color: #475569; font-size: 0.85rem;">${message}</div>
  `;

  // Click to open bookings tab
  toast.addEventListener('click', (e) => {
    if (e.target.tagName !== 'BUTTON') {
      const navLink = document.getElementById('nav-bookings');
      if (navLink) switchAdminPanel('bookings', navLink);
      toast.remove();
    }
  });

  // Close button
  toast.querySelector('button').addEventListener('click', (e) => {
    e.stopPropagation();
    toast.remove();
  });

  container.appendChild(toast);

  // Auto remove after 5 seconds
  setTimeout(() => {
    if (document.body.contains(toast)) {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(-10px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => {
        if (document.body.contains(toast)) toast.remove();
      }, 300);
    }
  }, 5000);
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

  if (panelName === "audit" || panelName === "audit-logs") {
    loadDataUsers(0);
  }
  if (panelName === "bookings") {
    fetchAdminBookings();
  }
  if (panelName === "messages") {
    loadUserMessages();
  }
}

// =============================================
//  Admin – Auth Helpers
// =============================================

function getAdminToken() {
  return localStorage.getItem("token") || sessionStorage.getItem("token") || "";
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

let _crudState = { type: null, item: null, convertingQuoteId: null };
let _deleteState = { type: null, id: null };

// Cache for loaded data (used to pass objects to modal)
const _cache = { users: {}, members: {}, projects: {}, services: {}, bookings: {}, quotations: {} };

async function openCrudModal(type, id) {
  if (type === "project") {
    try {
      const res = await fetch("/api/admin/users", { headers: adminHeaders() });
      if (res.ok) {
        const users = await res.json();
        _cache.users = {}; // Reset cache to get fresh list
        users.forEach(u => _cache.users[u.id] = u);
      }
    } catch (e) { console.warn("Could not pre-fetch users:", e); }
  }

  const item = id !== null ? (_cache[type + "s"] || _cache[type])[id] : null;
  _crudState = { type, item };

  const overlay = document.getElementById("crud-modal-overlay");
  const title = document.getElementById("crud-modal-title");
  const body = document.getElementById("crud-modal-body");
  const alert = document.getElementById("crud-alert");
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

        const preview = document.getElementById("cf-preview");
        const urlInput = document.getElementById(urlInputId);

        // Helper: read file as Base64 Data URL (always works, no server needed)
        const readAsDataUrl = (f) => new Promise((resolve, reject) => {
          const reader = new FileReader();
          reader.onload = () => resolve(reader.result);
          reader.onerror = reject;
          reader.readAsDataURL(f);
        });

        showCrudAlert("Uploading image...", null);

        // 1️⃣ Try server upload first (with auth token)
        try {
          const formData = new FormData();
          formData.append("file", file);

          const res = await fetch("/api/upload", {
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
  _crudState = { type: null, item: null, convertingQuoteId: null };
}

// =============================================
//  Admin – Form Builder
// =============================================

function buildCrudForm(type, item) {
  const v = item || {};
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
    ${fld("cf-username", "Username *", "text", v.username, `placeholder="Enter username" required ${item ? 'readonly style="background:#f8fafc;cursor:not-allowed;"' : ""}`)}
    ${fld("cf-fullName", "Full Name *", "text", v.fullName, 'placeholder="Enter full name" required')}
    ${fld("cf-email", "Email *", "email", v.email, 'placeholder="name@domain.com" required')}
    ${fld("cf-phone", "Phone Number", "tel", v.phone, 'placeholder="0123456789" pattern="[0-9]{10}"')}
    ${!item ? fld("cf-password", "Password *", "password", "", 'placeholder="Min 6 characters" required minlength="6"') : ""}

    ${sel("cf-role", "Role *", [["ROLE_USER", "User"], ["ROLE_MEMBER", "Team Member"]], v.role || "ROLE_USER")}

    <div class="form-group" style="width: 100%;">
      <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer; justify-content: flex-start;">
        <input type="checkbox" id="cf-enabled" ${v.enabled !== false ? 'checked' : ''}>
        <span>Active Status</span>
      </label>
    </div>
  `;

  if (type === "member") return `
    ${fld("cf-name", "Member Name *", "text", v.name, 'placeholder="Enter member name" required')}
    ${fld("cf-role", "Position / Role *", "text", v.role, 'placeholder="e.g. Frontend Developer" required')}
    <div class="form-group">
      <label for="cf-avatarFile">Avatar Image *</label>
      <input type="file" id="cf-avatarFile" accept="image/*" style="width:100%; padding:0.5rem; border:1px dashed var(--border-color); border-radius:var(--radius-sm); background:var(--bg-light); cursor:pointer;">
      <input type="hidden" id="cf-avatarUrl" value="${escapeHtml(String(v.avatarUrl || ""))}">
      ${v.avatarUrl ? `<img id="cf-preview" src="${escapeHtml(v.avatarUrl)}" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: block;">` : `<img id="cf-preview" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: none;">`}
    </div>
    ${fld("cf-facebookUrl", "Facebook URL", "url", v.facebookUrl, 'placeholder="https://facebook.com/..."')}
    ${fld("cf-githubUrl", "GitHub URL", "url", v.githubUrl, 'placeholder="https://github.com/..."')}
    ${fld("cf-linkedinUrl", "LinkedIn URL", "url", v.linkedinUrl, 'placeholder="https://linkedin.com/in/..."')}
    ${fld("cf-skills", "Professional Skills", "text", v.skills, 'placeholder="e.g. Java, React, SQL"')}
    ${fld("cf-projects", "Projects Worked On", "text", v.projectsWorked, 'placeholder="e.g. CMS Portal, E-Commerce App"')}
  `;

  if (type === "project") {
    const userList = Object.values(_cache.users || {}).filter(u => {
      if (!u || !u.role) return false;
      const r = String(u.role).trim().toUpperCase();
      return r === "ROLE_USER" || r === "USER";
    });
    const clientOpts = [["", "-- Select User (Client) * --"]].concat(
      userList.map(u => [String(u.id), `${u.fullName || u.username} (${u.email})`])
    );
    const selectedClientId = v.clientId ? String(v.clientId) : "";

    return `
    ${fld("cf-title", "Project Title *", "text", v.title, 'placeholder="Enter project title" required')}
    ${fld("cf-category", "Category *", "text", v.category, 'placeholder="e.g. Web Development" required')}
    ${sel("cf-clientId", "Link User (Client) *", clientOpts, selectedClientId)}
    ${fld("cf-depositAmount", "Deposit Amount ($) *", "number", v.depositAmount || 0, 'placeholder="Enter deposit amount" min="0" step="0.01" required')}
    <div class="form-group">
      <label for="cf-imageFile">Cover Image *</label>
      <input type="file" id="cf-imageFile" accept="image/*" style="width:100%; padding:0.5rem; border:1px dashed var(--border-color); border-radius:var(--radius-sm); background:var(--bg-light); cursor:pointer;">
      <input type="hidden" id="cf-imageUrl" value="${escapeHtml(String(v.imageUrl || ""))}">
      ${v.imageUrl ? `<img id="cf-preview" src="${escapeHtml(v.imageUrl)}" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: block;">` : `<img id="cf-preview" style="margin-top: 0.75rem; max-width: 150px; height: auto; border-radius: 6px; border: 1px solid var(--border-color); display: none;">`}
    </div>
    ${txt("cf-description", "Description *", v.description, 'placeholder="Project description..." required')}
    ${txt("cf-technologies", "Technologies Used", v.technologies, 'placeholder="e.g. React, Node.js, MongoDB..."')}
  `;
  }

  // MỚI
  if (type === "service") return `
    ${fld("cf-title", "Service Title *", "text", v.title, 'placeholder="Enter service title" required')}
    ${sel("cf-iconUrl", "Service Icon *",
    [["web", "🌐 Web Design"], ["design", "🎨 UI/UX Design"], ["marketing", "📊 Marketing"],
    ["mobile", "📱 Mobile App"], ["branding", "🎯 Branding"], ["cloud", "☁️ Cloud Solutions"]],
    v.iconUrl || "web")}
    ${fld("cf-basePrice", "Price ($) *", "number", v.basePrice != null ? v.basePrice : 0, 'placeholder="Enter service price" min="0" step="0.01" required')}
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
  let payload = {};
  let valid = true;

  const g = id => (document.getElementById(id)?.value || "").trim();
  const gv = id => document.getElementById(id)?.value || "";

  if (type === "user") {
    payload = {
      username: g("cf-username"), fullName: g("cf-fullName"), email: g("cf-email"),
      phone: g("cf-phone"), role: gv("cf-role"), enabled: document.getElementById("cf-enabled")?.checked
    };
    if (!isEdit) payload.password = gv("cf-password");
    if (!payload.username || !payload.fullName || !payload.email) valid = false;
  }

  if (type === "member") {
    payload = {
      name: g("cf-name"), role: g("cf-role"), avatarUrl: g("cf-avatarUrl"),
      facebookUrl: g("cf-facebookUrl"), githubUrl: g("cf-githubUrl"), linkedinUrl: g("cf-linkedinUrl"),
      skills: g("cf-skills"), projects: g("cf-projects")
    };
    if (!payload.name || !payload.avatarUrl) valid = false;
  }

  if (type === "project") {
    payload = {
      title: g("cf-title"),
      category: g("cf-category"),
      imageUrl: g("cf-imageUrl"),
      description: g("cf-description"),
      technologies: g("cf-technologies"),
      clientId: gv("cf-clientId") ? Number(gv("cf-clientId")) : null,
      depositAmount: g("cf-depositAmount") ? Number(g("cf-depositAmount")) : 0.0
    };
    if (!payload.title || !payload.category || !payload.imageUrl || !payload.description || !payload.clientId) valid = false;
  }

  // MỚI
  if (type === "service") {
    const priceVal = g("cf-basePrice");
    payload = {
      title: g("cf-title"),
      iconUrl: gv("cf-iconUrl"),
      description: g("cf-description"),
      basePrice: priceVal !== "" ? Number(priceVal) : 0
    };
    if (!payload.title || !payload.description || priceVal === "" || isNaN(payload.basePrice) || payload.basePrice < 0) {
      valid = false;
    }
  }

  if (!valid) { showCrudAlert("Please fill in all required fields (*)", false); return; }

  const eps = {
    user: "/api/admin/users", member: "/api/members",
    project: "/api/projects", service: "/api/services"
  };

  let url = isEdit ? `${eps[type]}/${item.id}` : eps[type];
  let method = isEdit ? "PUT" : "POST";

  if (_crudState.convertingQuoteId) {
    url = `/api/quotations/${_crudState.convertingQuoteId}/convert-to-project`;
    method = "POST";
  }

  try {
    showCrudAlert("Processing...", null);
    const response = await fetch(url, { method, headers: adminHeaders(), body: JSON.stringify(payload) });
    const data = await response.json().catch(() => ({}));

    if (response.ok) {
      if (_crudState.convertingQuoteId) {
        _crudState.convertingQuoteId = null;
        showCrudAlert("✅ Project created & Quotation converted successfully!", true);
        setTimeout(() => {
          closeCrudModal();
          fetchAdminProjectsTable();
          if (typeof fetchAdminQuotationsTable === "function") fetchAdminQuotationsTable();
        }, 700);
        return;
      }

      if (data && data.id) {
        if (type === "project") _lastUpdatedProjectTime[data.id] = Date.now();
        if (type === "service") _lastUpdatedServiceTime[data.id] = Date.now();
        if (type === "member") _lastUpdatedMemberTime[data.id] = Date.now();
        if (type === "user") _lastUpdatedUserTime[data.id] = Date.now();
      }
      showCrudAlert(isEdit ? "✅ Updated successfully!" : "✅ Added successfully!", true);
      setTimeout(() => {
        closeCrudModal();
        if (type === "user") fetchAdminUsers();
        if (type === "member") fetchAdminMembersTable();
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
  if (isSuccess === true) { el.classList.add("alert-success"); el.style.display = "block"; }
  else if (isSuccess === false) { el.classList.add("alert-error"); el.style.display = "block"; }
  else { el.style.cssText = "display:block;background:#f1f5f9;color:#334155;border:1px solid #cbd5e1;"; }
}

// =============================================
//  Admin – Confirm Delete
// =============================================

function openDeleteConfirm(type, id, name) {
  _deleteState = { type, id };
  const overlay = document.getElementById("confirm-modal-overlay");
  const text = document.getElementById("confirm-modal-text");
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

  const eps = {
    user: "/api/admin/users", member: "/api/members",
    project: "/api/projects", service: "/api/services"
  };
  try {
    const response = await fetch(`${eps[type]}/${id}`, { method: "DELETE", headers: adminHeaders() });
    if (response.ok) {
      closeConfirmModal();
      if (type === "user") fetchAdminUsers();
      if (type === "member") fetchAdminMembersTable();
      if (type === "project") fetchAdminProjectsTable();
      if (type === "service") fetchAdminServicesTable();
    } else {
      showToast("Delete failed. Please try again.", "error");
    }
  } catch (err) {
    console.error("Delete error:", err);
    showToast("Could not connect to server.", "error");
  }
}

// =============================================
//  Admin – Table Renderers
// =============================================

async function fetchAdminUsers() {
  const tbody = document.getElementById("users-table-body");
  const statCount = document.getElementById("stat-users-count");
  if (!tbody) return;

  try {
    const response = await fetch("/api/admin/users", { headers: adminHeaders() });
    const rawUsers = await response.json();
    const users = (rawUsers || []).filter(u => u.role !== "ROLE_ADMIN" && u.role !== "ADMIN");

    users.sort((a, b) => {
      const timeA = _lastUpdatedUserTime[a.id] || 0;
      const timeB = _lastUpdatedUserTime[b.id] || 0;
      if (timeA !== timeB) return timeB - timeA;
      return b.id - a.id;
    });

    if (statCount) statCount.textContent = users.length;

    _paginationState.user.items = users;
    const totalPages = Math.ceil(users.length / PAGE_SIZE);
    if (_paginationState.user.currentPage > totalPages) {
      _paginationState.user.currentPage = Math.max(1, totalPages);
    }

    renderUserTablePage();
  } catch (err) {
    console.error("fetchAdminUsers error:", err);
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:2rem;color:#ef4444;">Could not load user list.</td></tr>`;
  }
}

function renderUserTablePage() {
  const tbody = document.getElementById("users-table-body");
  if (!tbody) return;
  setupPaginationContainer(tbody, "user");

  const users = _paginationState.user.items;
  const currentPage = _paginationState.user.currentPage;
  const startIndex = (currentPage - 1) * PAGE_SIZE;
  const pageUsers = users.slice(startIndex, startIndex + PAGE_SIZE);

  tbody.innerHTML = "";
  if (!pageUsers.length) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;padding:2rem;color:var(--text-muted);">No users found.</td></tr>`;
    renderPaginationControls("user", users.length);
    return;
  }

  pageUsers.forEach(u => {
    _cache.users[u.id] = u;
    const tr = document.createElement("tr");
    tr.setAttribute("data-searchable", `${u.fullName} ${u.username} ${u.email}`);
    const initials = (u.fullName || "?")[0].toUpperCase();

    // Check if user is online (last login within last 5 minutes)
    let isOnline = false;
    let lastLoginHtml = "";
    if (u.lastLogin) {
      const lastLogin = new Date(u.lastLogin);
      const now = new Date();
      const diffMinutes = (now - lastLogin) / (1000 * 60);
      isOnline = diffMinutes < 5;
      lastLoginHtml = `<br><small style="color: #64748b; font-size: 11px; white-space: nowrap;">${lastLogin.toLocaleString("en-US")}</small>`;
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
          ${lastLoginHtml}
        </td>
        <td>
          <div class="action-btns">
            <button class="btn-edit"   onclick="openCrudModal('user', ${u.id})">
              <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
            </button>
            <button class="btn-delete" onclick="openDeleteConfirm('user', ${u.id}, '${escapeHtml(u.fullName || "")}')">
              <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
            </button>
          </div>
        </td>`;
    tbody.appendChild(tr);
  });

  renderPaginationControls("user", users.length);
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
      showToast("Operation failed. Please try again.", "error");
    }
  } catch (err) {
    console.error("Toggle status error:", err);
    showToast("Could not connect to server.", "error");
  }
}

async function fetchAdminMembersTable() {
  const tbody = document.getElementById("members-table-body");
  const statCount = document.getElementById("stat-members-count");
  if (!tbody) return;

  try {
    const response = await fetch("/api/members");
    if (!response.ok) throw new Error("Failed");
    const members = await response.json();

    members.sort((a, b) => {
      const timeA = _lastUpdatedMemberTime[a.id] || 0;
      const timeB = _lastUpdatedMemberTime[b.id] || 0;
      if (timeA !== timeB) return timeB - timeA;
      return b.id - a.id;
    });

    if (statCount) statCount.textContent = members.length;

    _paginationState.member.items = members;
    const totalPages = Math.ceil(members.length / PAGE_SIZE);
    if (_paginationState.member.currentPage > totalPages) {
      _paginationState.member.currentPage = Math.max(1, totalPages);
    }

    renderMemberTablePage();
  } catch (err) {
    console.error("fetchAdminMembersTable error:", err);
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:#ef4444;">Could not load member list.</td></tr>`;
  }
}

function renderMemberTablePage() {
  const tbody = document.getElementById("members-table-body");
  if (!tbody) return;
  setupPaginationContainer(tbody, "member");

  const members = _paginationState.member.items;
  const currentPage = _paginationState.member.currentPage;
  const startIndex = (currentPage - 1) * PAGE_SIZE;
  const pageMembers = members.slice(startIndex, startIndex + PAGE_SIZE);

  tbody.innerHTML = "";
  if (!pageMembers.length) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;padding:2rem;color:var(--text-muted);">No members found.</td></tr>`;
    renderPaginationControls("member", members.length);
    return;
  }

  pageMembers.forEach(m => {
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
            <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          </button>
          <button class="btn-delete" onclick="openDeleteConfirm('member', ${m.id}, '${escapeHtml(m.name || "")}')">
            <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
          </button>
        </div>
      </td>`;
    tbody.appendChild(tr);
  });

  renderPaginationControls("member", members.length);
}

const _lastUpdatedProjectTime = {};
const _lastUpdatedServiceTime = {};
const _lastUpdatedMemberTime = {};
const _lastUpdatedUserTime = {};

const _paginationState = {
  user: { currentPage: 1, items: [] },
  member: { currentPage: 1, items: [] },
  project: { currentPage: 1, items: [] },
  service: { currentPage: 1, items: [] },
  booking: { currentPage: 1, items: [] },
  message: { currentPage: 1, items: [] }
};
const PAGE_SIZE = 8;

function setupPaginationContainer(tbody, type) {
  if (!tbody) return null;
  const container = tbody.closest(".table-container");
  if (!container) return null;

  let bar = container.querySelector(`.pagination-bar`);
  if (!bar) {
    bar = document.createElement("div");
    bar.className = "pagination-bar";
    bar.id = `pagination-bar-${type}`;
    container.appendChild(bar);
  }
  return bar;
}

function renderPaginationControls(type, totalItems) {
  const container = document.getElementById(`pagination-bar-${type}`);
  if (!container) return;

  const state = _paginationState[type];
  const totalPages = Math.ceil(totalItems / PAGE_SIZE);

  if (totalPages <= 1) {
    container.innerHTML = "";
    return;
  }

  const startEntry = (state.currentPage - 1) * PAGE_SIZE + 1;
  const endEntry = Math.min(state.currentPage * PAGE_SIZE, totalItems);

  let buttonsHtml = "";

  // Previous button
  buttonsHtml += `
      <button type="button" class="page-btn" ${state.currentPage === 1 ? "disabled" : ""} onclick="changeTablePage('${type}', ${state.currentPage - 1})">
        &laquo; Prev
      </button>
    `;

  // Page number buttons
  for (let i = 1; i <= totalPages; i++) {
    buttonsHtml += `
        <button type="button" class="page-btn ${state.currentPage === i ? "active" : ""}" onclick="changeTablePage('${type}', ${i})">
          ${i}
        </button>
      `;
  }

  // Next button
  buttonsHtml += `
      <button type="button" class="page-btn" ${state.currentPage === totalPages ? "disabled" : ""} onclick="changeTablePage('${type}', ${state.currentPage + 1})">
        Next &raquo;
      </button>
    `;

  container.innerHTML = `
      <div class="pagination-wrapper" style="display:flex; justify-content:space-between; align-items:center; padding:1rem; border-top:1px solid var(--border-color, #e2e8f0); font-size:0.85rem; color:var(--text-muted, #64748b);">
        <div>
          Showing <strong>${startEntry}</strong> to <strong>${endEntry}</strong> of <strong>${totalItems}</strong> entries
        </div>
        <div style="display:flex; gap:5px;">
          ${buttonsHtml}
        </div>
      </div>
    `;
}

function changeTablePage(type, page) {
  if (!_paginationState[type]) return;
  const totalPages = Math.ceil(_paginationState[type].items.length / PAGE_SIZE);
  if (page < 1 || page > totalPages) return;
  _paginationState[type].currentPage = page;

  if (type === "user") renderUserTablePage();
  if (type === "member") renderMemberTablePage();
  if (type === "project") renderProjectTablePage();
  if (type === "service") renderServiceTablePage();
  if (type === "booking") renderBookingTablePage();
  if (type === "message") renderMessageTablePage();
}
window.changeTablePage = changeTablePage;

async function fetchAdminProjectsTable() {
  const tbody = document.getElementById("projects-table-body");
  const statCount = document.getElementById("stat-projects-count");
  if (!tbody) return;

  try {
    const response = await fetch("/api/projects");
    if (!response.ok) throw new Error("Failed");
    const projects = await response.json();

    projects.sort((a, b) => {
      const timeA = _lastUpdatedProjectTime[a.id] || 0;
      const timeB = _lastUpdatedProjectTime[b.id] || 0;
      if (timeA !== timeB) return timeB - timeA;
      return b.id - a.id;
    });

    if (statCount) statCount.textContent = projects.length;

    _paginationState.project.items = projects;
    const totalPages = Math.ceil(projects.length / PAGE_SIZE);
    if (_paginationState.project.currentPage > totalPages) {
      _paginationState.project.currentPage = Math.max(1, totalPages);
    }

    renderProjectTablePage();
  } catch (err) {
    console.error("fetchAdminProjectsTable error:", err);
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:#ef4444;">Could not load project list.</td></tr>`;
  }
}

function renderProjectTablePage() {
  const tbody = document.getElementById("projects-table-body");
  if (!tbody) return;
  setupPaginationContainer(tbody, "project");

  const projects = _paginationState.project.items;
  const currentPage = _paginationState.project.currentPage;
  const startIndex = (currentPage - 1) * PAGE_SIZE;
  const pageProjects = projects.slice(startIndex, startIndex + PAGE_SIZE);

  tbody.innerHTML = "";
  if (!pageProjects.length) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">No projects found.</td></tr>`;
    renderPaginationControls("project", projects.length);
    return;
  }

  pageProjects.forEach(p => {
    _cache.projects[p.id] = p;
    const tr = document.createElement("tr");
    tr.setAttribute("data-searchable", `${p.title} ${p.category} ${p.clientName || ""}`);
    const clientHtml = p.clientName
      ? `<div style="font-weight:600; color:var(--text-dark);">${escapeHtml(p.clientName)}</div><div style="font-size:0.78rem; color:var(--text-muted);">${escapeHtml(p.clientEmail || "")}</div>`
      : `<span style="color:var(--text-muted); font-style:italic;">Unassigned</span>`;

    tr.innerHTML = `
      <td><img src="${escapeHtml(p.imageUrl || "")}" alt="${escapeHtml(p.title || "")}"
            style="width:78px;height:48px;object-fit:cover;border-radius:6px;border:1px solid var(--border-color);"
            onerror="this.src='https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=78&h=48'"></td>
      <td class="text-dark-inline">${escapeHtml(p.title || "")}</td>
      <td><span class="status-badge badge-category">${escapeHtml(p.category || "")}</span></td>
      <td>${clientHtml}</td>
      <td>
        <div class="action-btns">
          <button class="btn-edit" style="background:#0284c7; color:#fff; border-color:#0284c7;" onclick="openProjectDetailModal(${p.id})">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:12px;height:12px;margin-right:2px;"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          </button>
          <button class="btn-edit"   onclick="openCrudModal('project', ${p.id})">
            <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          </button>
          <button class="btn-delete" onclick="openDeleteConfirm('project', ${p.id}, '${escapeHtml(p.title || "")}')">
            <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
          </button>
        </div>
      </td>`;
    tbody.appendChild(tr);
  });

  renderPaginationControls("project", projects.length);
}

let _currentlyExpandedServiceId = null;

async function fetchAdminServicesTable() {
  const tbody = document.getElementById("services-table-body");
  if (!tbody) return;

  try {
    const response = await fetch("/api/services");
    if (!response.ok) throw new Error("Failed");
    const services = await response.json();

    services.sort((a, b) => {
      const timeA = _lastUpdatedServiceTime[a.id] || 0;
      const timeB = _lastUpdatedServiceTime[b.id] || 0;
      if (timeA !== timeB) return timeB - timeA;
      return b.id - a.id;
    });

    _paginationState.service.items = services;
    const totalPages = Math.ceil(services.length / PAGE_SIZE);
    if (_paginationState.service.currentPage > totalPages) {
      _paginationState.service.currentPage = Math.max(1, totalPages);
    }

    renderServiceTablePage();
  } catch (err) {
    console.error("fetchAdminServicesTable error:", err);
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:#ef4444;">Could not load service list.</td></tr>`;
  }
}

function renderServiceTablePage() {
  const tbody = document.getElementById("services-table-body");
  if (!tbody) return;
  setupPaginationContainer(tbody, "service");

  const services = _paginationState.service.items;
  const currentPage = _paginationState.service.currentPage;
  const startIndex = (currentPage - 1) * PAGE_SIZE;
  const pageServices = services.slice(startIndex, startIndex + PAGE_SIZE);

  tbody.innerHTML = "";
  if (!pageServices.length) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--text-muted);">No services found.</td></tr>`;
    renderPaginationControls("service", services.length);
    return;
  }

  const iconLabels = {
    web: "🌐 Web Design", design: "🎨 UI/UX", marketing: "📊 Marketing",
    mobile: "📱 Mobile", branding: "🎯 Branding", cloud: "☁️ Cloud"
  };

  pageServices.forEach(s => {
    _cache.services[s.id] = s;
    const tr = document.createElement("tr");
    tr.setAttribute("data-searchable", `${s.title} ${s.description}`);
    tr.innerHTML = `
    <td class="text-dark-inline">${escapeHtml(s.title || "")}</td>
    <td><span class="status-badge badge-active">${iconLabels[s.iconUrl] || escapeHtml(s.iconUrl || "—")}</span></td>
    <td style="max-width:220px;white-space:pre-wrap;">${escapeHtml((s.description || "").substring(0, 80))}${(s.description || "").length > 80 ? "..." : ""}</td>
    <td>
  <span style="font-weight:700;color:var(--text-dark);font-size:0.9rem;">$${Number(s.basePrice || 0).toFixed(2)}</span>
    </td>
    </td>
    <td>
      <button type="button" onclick="openAddonModal(${s.id}, '${escapeHtml(s.title).replace(/'/g, "\\'")}')" id="addon-toggle-btn-${s.id}"
        style="background:#eff6ff;border:1px solid #bfdbfe;color:#2563eb;border-radius:6px;padding:0.3rem 0.6rem;font-size:0.78rem;font-weight:600;cursor:pointer;white-space:nowrap;">
        <span id="addon-count-${s.id}">…</span> add-ons
      </button>
    </td>
    <td>
      <div class="action-btns">
        <button class="btn-edit"   onclick="openCrudModal('service', ${s.id})">
          <svg viewBox="0 0 24 24"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
        </button>
        <button class="btn-delete" onclick="openDeleteConfirm('service', ${s.id}, '${escapeHtml(s.title || "")}')">
          <svg viewBox="0 0 24 24"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
        </button>
      </div>
    </td>`;
    tbody.appendChild(tr);

    loadServiceAddonsTable(s.id);
  });

  renderPaginationControls("service", services.length);
}

async function updateServicePriceTable(serviceId) {
  const priceEl = document.getElementById(`table-price-${serviceId}`);
  if (!priceEl) return;

  const price = parseFloat(priceEl.value);
  if (isNaN(price) || price < 0) {
    showToast("Please enter a valid non-negative price", "error");
    return;
  }

  try {
    const res = await fetch(`/api/services/${serviceId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json", ...adminHeaders() },
      body: JSON.stringify({ basePrice: price })
    });

    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      throw new Error(data.message || "Failed to update service price");
    }

    showToast("Service price updated successfully", "success");
    _lastUpdatedServiceTime[serviceId] = Date.now();
    fetchAdminServicesTable();
  } catch (err) {
    showToast(err.message, "error");
  }
}

function toggleAddonManage(serviceId) {
  const row = document.getElementById(`addon-detail-row-${serviceId}`);
  if (!row) return;
  const isHidden = row.style.display === "none";
  row.style.display = isHidden ? "table-row" : "none";

  if (isHidden) {
    _currentlyExpandedServiceId = serviceId;
  } else if (_currentlyExpandedServiceId === serviceId) {
    _currentlyExpandedServiceId = null;
  }

  const btn = document.getElementById(`addon-toggle-btn-${serviceId}`);
  if (btn) {
    if (isHidden) {
      btn.style.background = '#2563eb';
      btn.style.color = '#fff';
    } else {
      btn.style.background = '#eff6ff';
      btn.style.color = '#2563eb';
    }
  }
}



async function loadServiceAddonsTable(serviceId) {
  try {
    const response = await fetch(`/api/services/${serviceId}/addons`);
    if (!response.ok) throw new Error("Failed to load add-ons");
    const addons = await response.json();

    const countEl = document.getElementById(`addon-count-${serviceId}`);
    if (countEl) {
      countEl.textContent = addons.length;
    }

    addons.forEach(a => { _serviceAddonsCache[a.id] = a; });

    const listEl = document.getElementById(`table-addons-list-${serviceId}`);
    if (listEl) {
      renderServiceAddonsTable(serviceId, addons);
    }
  } catch (err) {
    console.error("loadServiceAddonsTable error:", err);
    const countEl = document.getElementById(`addon-count-${serviceId}`);
    if (countEl) countEl.textContent = "0";
  }
}

function renderServiceAddonsTable(serviceId, addons) {
  const listEl = document.getElementById(`table-addons-list-${serviceId}`);
  if (!listEl) return;
  if (!addons.length) {
    listEl.innerHTML = `<span style="color:var(--text-muted);font-size:0.78rem;">No add-ons yet.</span>`;
    return;
  }
  listEl.innerHTML = addons.map(a => `
    <div class="table-addon-row" data-addon-id="${a.id}" style="display:flex;align-items:center;gap:6px;font-size:0.78rem;">
      <span class="text-dark-inline" style="flex:1;min-width:0;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${escapeHtml(a.addonName)}</span>
      <span style="font-weight:600;color:var(--primary,#2563eb);white-space:nowrap;">$${Number(a.priceModifier).toFixed(2)}</span>
      <button type="button" onclick="startEditServiceAddonTable(${serviceId}, ${a.id})" style="background:none;border:none;color:#2563eb;cursor:pointer;font-size:0.72rem;">Edit</button>
      <button type="button" onclick="deleteServiceAddonTable(${serviceId}, ${a.id}, '${escapeHtml(a.addonName).replace(/'/g, "\\'")}')" style="background:none;border:none;color:#ef4444;cursor:pointer;font-size:0.72rem;">Del</button>
    </div>
  `).join("");
}

let _currentAddonServiceId = null;

async function openAddonModal(serviceId, serviceTitle) {
  _currentAddonServiceId = serviceId;

  const titleEl = document.getElementById("addon-modal-title");
  const subtitleEl = document.getElementById("addon-modal-subtitle");
  if (titleEl) titleEl.textContent = `Manage Add-ons`;
  if (subtitleEl) subtitleEl.textContent = serviceTitle;

  // Clear add new form
  const nameInput = document.getElementById("modal-addon-new-name");
  const priceInput = document.getElementById("modal-addon-new-price");
  if (nameInput) nameInput.value = "";
  if (priceInput) priceInput.value = "";

  const overlay = document.getElementById("addon-modal-overlay");
  if (overlay) overlay.classList.add("is-open");

  await loadServiceAddonsModalList(serviceId);
}

function closeAddonModal() {
  const overlay = document.getElementById("addon-modal-overlay");
  if (overlay) overlay.classList.remove("is-open");
  _currentAddonServiceId = null;
  fetchAdminServicesTable(); // Refresh the main table to update addon counts!
}

async function loadServiceAddonsModalList(serviceId) {
  const listEl = document.getElementById("modal-addons-list");
  const countEl = document.getElementById("modal-addon-count");
  if (!listEl) return;

  listEl.innerHTML = `<div style="padding:1.5rem;text-align:center;color:var(--text-muted);font-size:0.9rem;">Loading add-ons...</div>`;

  try {
    const res = await fetch(`/api/services/${serviceId}/addons`);
    if (!res.ok) throw new Error("Failed to load add-ons");
    const addons = await res.json();

    if (countEl) countEl.textContent = addons.length;

    addons.forEach(a => { _serviceAddonsCache[a.id] = a; });

    if (addons.length === 0) {
      listEl.innerHTML = `
          <div style="text-align:center;padding:2rem;background:var(--bg-card,#f8fafc);border:1px dashed var(--border-color,#cbd5e1);border-radius:12px;color:var(--text-muted);font-size:0.88rem;">
            No add-ons for this service yet. Add one above!
          </div>`;
      return;
    }

    listEl.innerHTML = addons.map(a => `
        <div class="table-addon-row" data-addon-id="${a.id}" style="display:flex; justify-content:space-between; align-items:center; padding:0.85rem 1.25rem; background:var(--bg-card,#fff); border:1px solid var(--border-color,#e2e8f0); border-radius:12px; box-shadow:0 1px 3px rgba(0,0,0,0.02); transition: all 0.2s ease;">
          <div style="display:flex; flex-direction:column; gap:2px; flex:1; min-width:0; padding-right:12px;">
            <strong style="color:var(--text-dark, #0f172a); font-size:0.9rem; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">${escapeHtml(a.addonName)}</strong>
            <span style="color:#2563eb; font-weight:700; font-size:0.85rem;">$${Number(a.priceModifier || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
          </div>
          <div style="display:flex; gap:6px;">
            <button type="button" onclick="startEditServiceAddonModalList(${serviceId}, ${a.id})" class="btn-edit" style="padding:0.4rem 0.75rem; font-size:0.78rem; font-weight:600; border-radius:6px; display:inline-flex; align-items:center; gap:4px; height:32px;">
              Edit
            </button>
            <button type="button" onclick="deleteServiceAddonModalList(${serviceId}, ${a.id}, '${escapeHtml(a.addonName).replace(/'/g, "\\'")}')" class="btn-delete" style="padding:0.4rem 0.75rem; font-size:0.78rem; font-weight:600; border-radius:6px; display:inline-flex; align-items:center; gap:4px; height:32px;">
              Delete
            </button>
          </div>
        </div>
      `).join('');
  } catch (err) {
    console.error(err);
    listEl.innerHTML = `<div style="color:#ef4444;text-align:center;padding:1.5rem;font-size:0.9rem;">Error loading add-ons: ${err.message}</div>`;
  }
}

function startEditServiceAddonModalList(serviceId, addonId) {
  const a = _serviceAddonsCache[addonId];
  const row = document.querySelector(`#modal-addons-list .table-addon-row[data-addon-id="${addonId}"]`);
  if (!a || !row) return;

  row.innerHTML = `
      <div style="display:flex; gap:8px; width:100%; align-items:center;">
        <input type="text" id="modal-addon-edit-name-${addonId}" value="${escapeHtml(a.addonName)}" style="flex: 1; padding: 0.5rem 0.75rem; border: 1px solid var(--border-color,#cbd5e1); border-radius: 8px; font-size: 0.85rem; background:var(--bg-card,#fff); color:var(--text-dark,#0f172a);">
        <div style="position: relative; display: flex; align-items: center;">
          <span style="position: absolute; left: 8px; font-size: 0.85rem; color: var(--text-muted);">$</span>
          <input type="number" id="modal-addon-edit-price-${addonId}" value="${a.priceModifier}" min="0" step="0.01" style="width: 80px; padding: 0.5rem 0.5rem 0.5rem 1.5rem; border: 1px solid var(--border-color,#cbd5e1); border-radius: 8px; font-size: 0.85rem; font-weight: 600; background:var(--bg-card,#fff); color:var(--text-dark,#0f172a);">
        </div>
        <button type="button" onclick="saveServiceAddonEditModalList(${serviceId}, ${addonId})" class="btn-save" style="padding:0.5rem 0.85rem; font-size:0.78rem; font-weight:600; border-radius:6px; height:32px; border:none; background:#059669; color:#fff; cursor:pointer;">Save</button>
        <button type="button" onclick="loadServiceAddonsModalList(${serviceId})" class="btn-cancel" style="padding:0.5rem 0.85rem; font-size:0.78rem; font-weight:600; border-radius:6px; height:32px; border:1px solid var(--border-color,#cbd5e1); background:var(--bg-card,#fff); color:var(--text-muted); cursor:pointer;">Cancel</button>
      </div>
    `;
}

async function saveServiceAddonEditModalList(serviceId, addonId) {
  const name = (document.getElementById(`modal-addon-edit-name-${addonId}`)?.value || "").trim();
  const price = parseFloat(document.getElementById(`modal-addon-edit-price-${addonId}`)?.value);

  if (!name || isNaN(price) || price < 0) {
    showToast("Please enter a valid add-on name and non-negative price.", "warning");
    return;
  }

  try {
    const res = await fetch(`/api/service-addons/${addonId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({ addonName: name, priceModifier: price })
    });

    if (!res.ok) throw new Error("Failed to update add-on");
    showToast("Add-on updated successfully", "success");
    _lastUpdatedServiceTime[serviceId] = Date.now();
    await loadServiceAddonsModalList(serviceId);
  } catch (err) {
    showToast(err.message, "error");
  }
}

function deleteServiceAddonModalList(serviceId, addonId, name) {
  showConfirmModal({
    title: "Delete Add-on",
    message: `Are you sure you want to delete "${name}"? This action cannot be undone.`,
    confirmText: "Delete",
    cancelText: "Cancel",
    onConfirm: async () => {
      try {
        const res = await fetch(`/api/service-addons/${addonId}`, {
          method: "DELETE",
          headers: adminHeaders()
        });
        if (!res.ok) throw new Error("Failed to delete add-on");
        showToast("Add-on deleted successfully", "success");
        _lastUpdatedServiceTime[serviceId] = Date.now();
        await loadServiceAddonsModalList(serviceId);
      } catch (err) {
        showToast(err.message, "error");
      }
    }
  });
}

async function addServiceAddonModal() {
  const serviceId = _currentAddonServiceId;
  if (!serviceId) return;

  const nameInput = document.getElementById("modal-addon-new-name");
  const priceInput = document.getElementById("modal-addon-new-price");
  const name = (nameInput?.value || "").trim();
  const price = parseFloat(priceInput?.value);

  if (!name || isNaN(price) || price < 0) {
    showToast("Please enter a valid add-on name and non-negative price.", "warning");
    return;
  }

  try {
    const res = await fetch(`/api/service-addons`, {
      method: "POST",
      headers: adminHeaders(),
      body: JSON.stringify({ serviceId: serviceId, addonName: name, priceModifier: price })
    });

    if (!res.ok) throw new Error("Failed to add add-on");
    if (nameInput) nameInput.value = "";
    if (priceInput) priceInput.value = "";
    showToast("Add-on added successfully!", "success");
    _lastUpdatedServiceTime[serviceId] = Date.now();
    await loadServiceAddonsModalList(serviceId);
  } catch (err) {
    showToast(err.message, "error");
  }
}
function deleteServiceAddonInline(serviceId, addonId, name) {
  showConfirmModal({
    title: "Delete Add-on",
    message: `Are you sure you want to delete "${name}"? This action cannot be undone.`,
    confirmText: "Delete",
    cancelText: "Cancel",
    onConfirm: async () => {
      try {
        const response = await fetch(`/api/service-addons/${addonId}`, {
          method: "DELETE",
          headers: adminHeaders()
        });
        if (!response.ok) throw new Error("Failed to delete add-on");
        showToast("Add-on deleted successfully!", "success");
        loadServiceAddonsForModal(serviceId);
      } catch (err) {
        console.error(err);
        showToast("Failed to delete add-on: " + err.message, "error");
      }
    }
  });
}


async function saveServiceAddonEdit(serviceId, addonId) {
  const name = (document.getElementById(`cf-addon-edit-name-${addonId}`)?.value || "").trim();
  const price = parseFloat(document.getElementById(`cf-addon-edit-price-${addonId}`)?.value);
  if (!name || isNaN(price) || price < 0) {
    showToast("Please enter a valid add-on name and non-negative price.", "warning");
    return;
  }
  try {
    const response = await fetch(`/api/service-addons/${addonId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({ addonName: name, priceModifier: price })
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || "Failed to update add-on");
    showToast("Add-on updated successfully!", "success");
    loadServiceAddonsForModal(serviceId);
  } catch (err) {
    console.error(err);
    showToast("Failed to update add-on: " + err.message, "error");
  }
}




// =============================================
//  Admin – Service Add-on CRUD (per service, inside the Service edit modal)
// =============================================

const _serviceAddonsCache = {};

async function loadServiceAddonsForModal(serviceId) {
  const listEl = document.getElementById("cf-addons-list");
  if (!listEl) return;
  listEl.innerHTML = `<div style="padding:0.75rem;color:var(--text-muted);font-size:0.85rem;">Loading add-ons...</div>`;
  try {
    const response = await fetch(`/api/services/${serviceId}/addons`);
    if (!response.ok) throw new Error("Failed to load add-ons");
    const addons = await response.json();
    addons.forEach(a => { _serviceAddonsCache[a.id] = a; });
    renderServiceAddonsList(serviceId, addons);
  } catch (err) {
    console.error("loadServiceAddonsForModal error:", err);
    listEl.innerHTML = `<div style="padding:0.75rem;color:#ef4444;font-size:0.85rem;">Could not load add-ons.</div>`;
  }
}

function renderServiceAddonsList(serviceId, addons) {
  const listEl = document.getElementById("cf-addons-list");
  if (!listEl) return;
  if (!addons.length) {
    listEl.innerHTML = `<div style="padding:0.75rem;color:var(--text-muted);font-size:0.85rem;">No add-ons yet for this service.</div>`;
    return;
  }
  listEl.innerHTML = addons.map(a => `
      <div class="cf-addon-row" data-addon-id="${a.id}" style="display:flex;align-items:center;gap:0.5rem;padding:0.5rem 0;border-bottom:1px solid var(--border-color);">
        <div style="flex:1;min-width:0;">
          <div class="text-dark-inline" style="font-size:0.9rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${escapeHtml(a.addonName)}</div>
        </div>
        <div style="width:80px;font-weight:600;color:var(--primary,#2563eb);font-size:0.9rem;text-align:right;">$${Number(a.priceModifier).toFixed(2)}</div>
        <div style="display:flex;gap:4px;flex-shrink:0;">
          <button type="button" class="btn-edit" style="padding:0.3rem 0.55rem;font-size:0.75rem;" onclick="startEditServiceAddon(${serviceId}, ${a.id})">Edit</button>
          <button type="button" class="btn-delete" style="padding:0.3rem 0.55rem;font-size:0.75rem;" onclick="deleteServiceAddonInline(${serviceId}, ${a.id}, '${escapeHtml(a.addonName).replace(/'/g, "\\'")}')">Delete</button>
        </div>
      </div>
    `).join("");
}

function startEditServiceAddon(serviceId, addonId) {
  const a = _serviceAddonsCache[addonId];
  const row = document.querySelector(`.cf-addon-row[data-addon-id="${addonId}"]`);
  if (!a || !row) return;
  row.innerHTML = `
      <input type="text" id="cf-addon-edit-name-${addonId}" value="${escapeHtml(a.addonName)}" style="flex:1;min-width:0;padding:0.4rem;border:1px solid var(--border-color);border-radius:6px;font-size:0.85rem;">
      <input type="number" id="cf-addon-edit-price-${addonId}" value="${a.priceModifier}" min="0" step="0.01" style="width:80px;padding:0.4rem;border:1px solid var(--border-color);border-radius:6px;font-size:0.85rem;">
      <div style="display:flex;gap:4px;flex-shrink:0;">
        <button type="button" class="btn-save" style="padding:0.3rem 0.55rem;font-size:0.75rem;" onclick="saveServiceAddonEdit(${serviceId}, ${addonId})">Save</button>
        <button type="button" class="btn-cancel" style="padding:0.3rem 0.55rem;font-size:0.75rem;" onclick="loadServiceAddonsForModal(${serviceId})">Cancel</button>
      </div>
    `;
}

async function saveServiceAddonEdit(serviceId, addonId) {
  const name = (document.getElementById(`cf-addon-edit-name-${addonId}`)?.value || "").trim();
  const price = parseFloat(document.getElementById(`cf-addon-edit-price-${addonId}`)?.value);
  if (!name || isNaN(price) || price < 0) {
    showToast("Please enter a valid add-on name and non-negative price.", "warning");
    return;
  }
  try {
    const response = await fetch(`/api/services/${serviceId}/addons/${addonId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({ addonName: name, priceModifier: price })
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || "Failed to update add-on");
    showToast("Add-on updated successfully!", "success");
    loadServiceAddonsForModal(serviceId);
  } catch (err) {
    console.error(err);
    showToast("Failed to update add-on: " + err.message, "error");
  }
}

function deleteServiceAddonInline(serviceId, addonId, name) {
  showConfirmModal({
    title: "Delete Add-on",
    message: `Are you sure you want to delete "${name}"? This action cannot be undone.`,
    confirmText: "Delete",
    cancelText: "Cancel",
    onConfirm: async () => {
      try {
        const response = await fetch(`/api/services/${serviceId}/addons/${addonId}`, {
          method: "DELETE",
          headers: adminHeaders()
        });
        if (!response.ok) throw new Error("Failed to delete add-on");
        showToast("Add-on deleted successfully!", "success");
        loadServiceAddonsForModal(serviceId);
      } catch (err) {
        console.error(err);
        showToast("Failed to delete add-on: " + err.message, "error");
      }
    }
  });
}

async function addServiceAddon(serviceId) {
  const nameInput = document.getElementById("cf-addon-new-name");
  const priceInput = document.getElementById("cf-addon-new-price");
  const name = (nameInput?.value || "").trim();
  const price = parseFloat(priceInput?.value);
  if (!name || isNaN(price) || price < 0) {
    showToast("Please enter a valid add-on name and non-negative price.", "warning");
    return;
  }
  try {
    const response = await fetch(`/api/services/${serviceId}/addons`, {
      method: "POST",
      headers: adminHeaders(),
      body: JSON.stringify({ addonName: name, priceModifier: price })
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || "Failed to add add-on");
    if (nameInput) nameInput.value = "";
    if (priceInput) priceInput.value = "";
    showToast("Add-on added successfully!", "success");
    loadServiceAddonsForModal(serviceId);
  } catch (err) {
    console.error(err);
    showToast("Failed to add add-on: " + err.message, "error");
  }
}

async function fetchAdminContacts() {
  const tableBody = document.getElementById("contacts-table-body");
  const statsCount = document.getElementById("stat-messages-count");

  try {
    const token = getAdminToken();
    const response = await fetch("/api/contacts", {
      headers: token ? { "Authorization": "Bearer " + token } : {}
    });
    if (!response.ok) throw new Error("Failed to fetch contact submissions");
    const contacts = await response.json();

    if (statsCount) statsCount.textContent = contacts.length;

    if (!tableBody) return;
    tableBody.innerHTML = "";

    if (contacts.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;">No contact messages found.</td></tr>`;
      return;
    }

    if (statsCount) statsCount.textContent = contacts.length;

    contacts.forEach(contact => {
      const row = document.createElement("tr");
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

let allMessages = [];
let filteredMessages = [];
let activeReplyId = null;

async function loadUserMessages() {
  try {
    const token = getAdminToken();
    const response = await fetch("/api/contacts/admin/user-messages", {
      headers: token ? { "Authorization": "Bearer " + token } : {}
    });
    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        showToast("Session expired or unauthorized. Please login again.", "error");
        window.location.href = "login.html";
        return;
      }
      throw new Error("Failed to fetch messages");
    }
    allMessages = await response.json();
    allMessages.sort((a, b) => b.id - a.id);

    // Update Overview stats count dynamically if the element is present
    const statsCount = document.getElementById("stat-messages-count");
    if (statsCount) statsCount.textContent = allMessages.length;

    applyFilters();
  } catch (err) {
    console.error(err);
    const tbody = document.getElementById("messages-table-body");
    if (tbody) {
      tbody.innerHTML = `
          <tr>
            <td colspan="6" style="text-align: center; padding: 2rem; color: #ef4444; font-weight: 600;">
              Error loading messages: ${err.message}
            </td>
          </tr>
        `;
    }
  }
}

function applyFilters() {
  const searchInput = document.getElementById("search-input");
  const filterStatus = document.getElementById("filter-status");
  if (!searchInput || !filterStatus) return;

  const searchTerm = searchInput.value.trim().toLowerCase();
  const statusFilter = filterStatus.value;

  filteredMessages = allMessages.filter(msg => {
    // Status filter
    if (statusFilter === "REPLIED" && !msg.replied) return false;
    if (statusFilter === "UNREPLIED" && msg.replied) return false;

    // Search term filter
    if (searchTerm) {
      const matchUsername = (msg.username || "").toLowerCase().includes(searchTerm);
      const matchFullName = (msg.fullName || "").toLowerCase().includes(searchTerm);
      const matchEmail = (msg.email || "").toLowerCase().includes(searchTerm);
      const matchContent = (msg.content || "").toLowerCase().includes(searchTerm);
      const matchTitle = (msg.title || "").toLowerCase().includes(searchTerm);
      return matchUsername || matchFullName || matchEmail || matchContent || matchTitle;
    }

    return true;
  });

  renderTable();
}

function renderTable() {
  _paginationState.message.items = filteredMessages;
  const totalPages = Math.ceil(filteredMessages.length / PAGE_SIZE);
  if (_paginationState.message.currentPage > totalPages) {
    _paginationState.message.currentPage = Math.max(1, totalPages);
  }
  renderMessageTablePage();
}

function renderMessageTablePage() {
  const tbody = document.getElementById("messages-table-body");
  if (!tbody) return;
  setupPaginationContainer(tbody, "message");

  const messages = _paginationState.message.items;
  const currentPage = _paginationState.message.currentPage;
  const startIndex = (currentPage - 1) * PAGE_SIZE;
  const pageMessages = messages.slice(startIndex, startIndex + PAGE_SIZE);

  if (pageMessages.length === 0) {
    tbody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align: center; padding: 2rem; color: var(--text-muted);">
            No matching client messages found.
          </td>
        </tr>
      `;
    renderPaginationControls("message", messages.length);
    return;
  }

  const role = localStorage.getItem("role") || sessionStorage.getItem("role");
  tbody.innerHTML = pageMessages.map(msg => {
    const dateStr = new Date(msg.createdAt).toLocaleString("en-US", {
      hour: "2-digit", minute: "2-digit",
      day: "2-digit", month: "2-digit", year: "numeric"
    });

    const statusBadge = msg.replied
      ? `<span class="status-badge badge-replied">Replied</span>`
      : `<span class="status-badge badge-unreplied">Unreplied</span>`;

    let replyContentHtml = "";
    if (msg.replied) {
      const repliedDateStr = new Date(msg.repliedAt).toLocaleString("en-US", {
        hour: "2-digit", minute: "2-digit",
        day: "2-digit", month: "2-digit", year: "numeric"
      });
      replyContentHtml = `
          <div class="replied-box">
            <div style="font-weight: 700; margin-bottom: 0.2rem;">Replied on ${repliedDateStr}:</div>
            <p style="margin: 0; white-space: pre-wrap;">${escapeHtml(msg.reply)}</p>
          </div>
        `;
    }

    const isRepliable = !msg.replied && role !== "ROLE_ADMIN";

    return `
        <tr>
          <td>
            <div style="font-weight: 700; color: var(--text-dark);">${escapeHtml(msg.fullName)}</div>
            <div style="font-size: 0.78rem; color: var(--text-muted);">@${escapeHtml(msg.username)}</div>
          </td>
          <td>
            <div style="font-size: 0.85rem; font-weight: 600; color: var(--color-primary, #2563eb);">${escapeHtml(msg.email)}</div>
            <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(msg.phone || "N/A")}</div>
          </td>
          <td style="font-weight: 600; color: var(--text-dark);">${escapeHtml(msg.title || "")}</td>
          <td>
            <p style="margin: 0; line-height: 1.4; white-space: pre-wrap;">${escapeHtml(msg.content || "")}</p>
            ${replyContentHtml}
          </td>
          <td>
            <div>${statusBadge}</div>
            <div style="font-size: 0.72rem; color: var(--text-muted); margin-top: 0.25rem;">Sent: ${dateStr}</div>
          </td>
          <td>
            <div class="action-btns">
              ${isRepliable ? `
                <button class="btn-edit" style="background:var(--color-primary, #2563eb); color:#fff; border:none;" onclick="openReplyDialog(${msg.id}, '${escapeHtml(msg.fullName)}', '${escapeHtml(msg.title)}')">
                  Reply
                </button>
              ` : ''}
              <button class="btn-delete" onclick="deleteMessage(${msg.id})">
                Delete
              </button>
            </div>
          </td>
        </tr>
      `;
  }).join('');

  renderPaginationControls("message", messages.length);
}

function handleSearch() {
  applyFilters();
}

function handleFilter() {
  applyFilters();
}

function openReplyDialog(msgId, senderName, title) {
  activeReplyId = msgId;
  const senderSpan = document.getElementById("reply-dialog-sender");
  const titleSpan = document.getElementById("reply-dialog-msg-title");
  const textarea = document.getElementById("reply-textarea");
  const overlay = document.getElementById("reply-dialog-overlay");

  if (senderSpan) senderSpan.textContent = senderName;
  if (titleSpan) titleSpan.textContent = title;
  if (textarea) textarea.value = "";
  if (overlay) overlay.style.display = "flex";
  if (textarea) textarea.focus();
}

function closeReplyDialog() {
  activeReplyId = null;
  const overlay = document.getElementById("reply-dialog-overlay");
  const textarea = document.getElementById("reply-textarea");
  if (overlay) overlay.style.display = "none";
  if (textarea) textarea.value = "";
}

async function submitReply() {
  if (!activeReplyId) return;
  const textarea = document.getElementById("reply-textarea");
  if (!textarea) return;
  const text = textarea.value.trim();
  if (!text) {
    showToast("Please enter a reply message.", "warning");
    return;
  }

  const btn = document.getElementById("btn-send-reply");
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Sending...";
  }

  try {
    const token = getAdminToken();
    const response = await fetch(`/api/contacts/${activeReplyId}/reply`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": token ? "Bearer " + token : ""
      },
      body: JSON.stringify({ message: text })
    });

    if (!response.ok) throw new Error("Failed to send reply");

    showToast("Reply sent successfully!", "success");
    closeReplyDialog();
    await loadUserMessages();
  } catch (err) {
    console.error(err);
    showToast("Error: " + err.message, "error");
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.textContent = "Send Reply";
    }
  }
}

async function deleteMessage(msgId) {
  showConfirmModal({
    title: "Delete Message",
    message: "Are you sure you want to delete this message? This action cannot be undone.",
    confirmText: "Delete",
    cancelText: "Cancel",
    onConfirm: async () => {
      try {
        const token = getAdminToken();
        const response = await fetch(`/api/contacts/${msgId}`, {
          method: "DELETE",
          headers: token ? { "Authorization": "Bearer " + token } : {}
        });

        if (!response.ok) throw new Error("Failed to delete message");

        showToast("Message deleted successfully!", "success");
        await loadUserMessages();
      } catch (err) {
        console.error(err);
        showToast("Error deleting message: " + err.message, "error");
      }
    }
  });
}

window.switchAdminPanel = switchAdminPanel;
window.loadUserMessages = loadUserMessages;
window.applyFilters = applyFilters;
window.renderTable = renderTable;
window.handleSearch = handleSearch;
window.handleFilter = handleFilter;
window.openReplyDialog = openReplyDialog;
window.closeReplyDialog = closeReplyDialog;
window.submitReply = submitReply;
window.deleteMessage = deleteMessage;

// Service Table Inline Edit & Addons Management
window.updateServicePriceTable = updateServicePriceTable;
window.loadServiceAddonsTable = loadServiceAddonsTable;
window.openAddonModal = openAddonModal;
window.closeAddonModal = closeAddonModal;
window.addServiceAddonModal = addServiceAddonModal;
window.startEditServiceAddonModalList = startEditServiceAddonModalList;
window.saveServiceAddonEditModalList = saveServiceAddonEditModalList;
window.deleteServiceAddonModalList = deleteServiceAddonModalList;
window.loadServiceAddonsModalList = loadServiceAddonsModalList;

// =============================================
//  In-App Notification Bell
// =============================================

function getAuthToken() {
  return localStorage.getItem("token") || sessionStorage.getItem("token") || "";
}

function injectNotificationBell(navLinksContainer, beforeEl) {
  // Avoid duplicate injection if function is re-called (updateNavbarAuth may run multiple times)
  const old = navLinksContainer.querySelector(".notification-bell-container");
  if (old) old.remove();

  const bellLi = document.createElement("li");
  bellLi.className = "auth-item notification-bell-container";
  bellLi.style.position = "relative";
  bellLi.innerHTML = `
    <button id="notification-bell-btn" title="Notifications" style="position:relative;background:none;border:none;cursor:pointer;padding:6px;display:flex;align-items:center;color:var(--text-muted);">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      <span id="notification-badge" style="display:none;position:absolute;top:0;right:0;background:#ef4444;color:#fff;font-size:10px;font-weight:700;min-width:16px;height:16px;border-radius:8px;display:none;align-items:center;justify-content:center;padding:0 3px;">0</span>
    </button>
    <div id="notification-dropdown" style="display:none;position:absolute;top:100%;right:0;margin-top:8px;width:320px;max-height:400px;overflow-y:auto;background:var(--bg-card,#fff);border:1px solid var(--border-color,#e5e7eb);border-radius:12px;box-shadow:0 10px 30px rgba(0,0,0,.15);z-index:1200;">
      <div style="padding:12px 16px;border-bottom:1px solid var(--border-color,#e5e7eb);display:flex;justify-content:space-between;align-items:center;">
        <strong style="font-size:0.9rem;">Notifications</strong>
        <button id="notification-mark-all-btn" style="background:none;border:none;color:#2563eb;font-size:0.78rem;cursor:pointer;">Mark all as read</button>
      </div>
      <div id="notification-list" style="padding:8px;">
        <p style="text-align:center;color:var(--text-muted);font-size:0.85rem;padding:1.5rem 0;">Loading...</p>
      </div>
    </div>
  `;

  navLinksContainer.insertBefore(bellLi, beforeEl);

  const btn = bellLi.querySelector("#notification-bell-btn");
  const dropdown = bellLi.querySelector("#notification-dropdown");

  btn.addEventListener("click", (e) => {
    e.stopPropagation();
    const isOpen = dropdown.style.display === "block";
    document.querySelectorAll("#notification-dropdown").forEach(d => d.style.display = "none");
    if (!isOpen) {
      dropdown.style.display = "block";
      loadNotificationList();
    }
  });
  document.addEventListener("click", () => { dropdown.style.display = "none"; });
  dropdown.addEventListener("click", (e) => e.stopPropagation());

  bellLi.querySelector("#notification-mark-all-btn").addEventListener("click", async () => {
    try {
      await fetch("/api/notifications/read-all", {
        method: "PATCH",
        headers: { "Authorization": "Bearer " + getAuthToken() }
      });
      loadNotificationList();
      loadNotificationUnreadCount();
    } catch (e) { console.error(e); }
  });

  // Load unread notification count on page load, then poll every 30s for near-realtime updates
  loadNotificationUnreadCount();
  if (window._notificationPollInterval) clearInterval(window._notificationPollInterval);
  window._notificationPollInterval = setInterval(loadNotificationUnreadCount, 30000);
}

async function loadNotificationUnreadCount() {
  const badge = document.getElementById("notification-badge");
  if (!badge) return;
  try {
    const res = await fetch("/api/notifications/unread-count", {
      headers: { "Authorization": "Bearer " + getAuthToken() }
    });
    if (!res.ok) return;
    const data = await res.json();
    if (data.count > 0) {
      badge.textContent = data.count > 9 ? "9+" : data.count;
      badge.style.display = "flex";
    } else {
      badge.style.display = "none";
    }
  } catch (e) { /* silently ignore secondary errors */ }
}

async function loadNotificationList() {
  const listEl = document.getElementById("notification-list");
  if (!listEl) return;
  try {
    const res = await fetch("/api/notifications", {
      headers: { "Authorization": "Bearer " + getAuthToken() }
    });
    if (!res.ok) throw new Error("Failed to load notifications");
    const list = await res.json();

    if (!list.length) {
      listEl.innerHTML = `<p style="text-align:center;color:var(--text-muted);font-size:0.85rem;padding:1.5rem 0;">No notifications yet.</p>`;
      return;
    }

    listEl.innerHTML = list.map(n => {
      const resolvedLink = resolveNotificationLink(n);
      return `
        <a href="${escapeHtml(resolvedLink)}" class="notification-item ${n.read ? 'read' : 'unread'}" data-id="${n.id}"
           style="display:block;padding:10px 12px;border-radius:8px;text-decoration:none;margin-bottom:4px;">
          <div class="notif-item-title" style="font-weight:600;font-size:0.85rem;margin-bottom:2px;">${escapeHtml(n.title)}</div>
          <div class="notif-item-msg" style="font-size:0.8rem;margin-top:2px;line-height:1.4;">${escapeHtml(n.message)}</div>
          <div class="notif-item-time" style="font-size:0.72rem;margin-top:4px;">${formatNotificationTime(n.createdAt)}</div>
        </a>
      `;
    }).join("");

    listEl.querySelectorAll(".notification-item").forEach(item => {
      item.addEventListener("click", async (e) => {
        const id = item.dataset.id;
        try {
          await fetch(`/api/notifications/${id}/read`, {
            method: "PATCH",
            headers: { "Authorization": "Bearer " + getAuthToken() }
          });
          loadNotificationUnreadCount();
        } catch (err) { /* do not block navigation if marking read fails */ }
      });
    });
  } catch (e) {
    listEl.innerHTML = `<p style="text-align:center;color:#ef4444;font-size:0.85rem;padding:1.5rem 0;">Could not load notifications.</p>`;
  }
}

function resolveNotificationLink(n) {
  if (!n) return "#";

  // 1. Determine current logged-in user role
  let role = "";
  try {
    const userStr = localStorage.getItem("user") || sessionStorage.getItem("user");
    if (userStr) {
      const u = JSON.parse(userStr);
      role = u.role || "";
    }
  } catch (e) { }

  const isMember = (role === "ROLE_MEMBER" || role === "Team_Member" || role === "ROLE_RESOURCE");
  const isAdmin = (role === "ROLE_ADMIN");

  const title = (n.title || "").toLowerCase();
  const msg = (n.message || "").toLowerCase();
  const storedLink = (n.link || "").toLowerCase().trim();

  // 2. Direct routing if storedLink is a specific valid HTML page
  if (storedLink.endsWith(".html") || storedLink.includes(".html#")) {
    let cleanLink = n.link.startsWith("/") ? n.link.substring(1) : n.link;
    if (cleanLink.includes("my-bookings") || cleanLink.includes("appointments")) {
      return isMember ? "member-contact.html#my-bookings" : "my-bookings.html";
    }
    if (cleanLink.includes("rented-project") && isMember) {
      return "member.html";
    }
    if (cleanLink.includes("member.html") && !isMember && !isAdmin) {
      return "rented-project.html";
    }
    return cleanLink;
  }

  // 3. Keyword-based intelligent routing by Notification Title / Message content

  // A. Booking & Consultation Notifications
  if (title.includes("booking") || title.includes("appointment") || title.includes("consultation") ||
    msg.includes("booking") || msg.includes("appointment") || msg.includes("consultation")) {
    return isMember ? "member-contact.html#my-bookings" : "my-bookings.html";
  }

  // B. Client Request & Contact Message Notifications
  if (title.includes("client request") || title.includes("contact") || title.includes("inquiry") ||
    msg.includes("client request") || msg.includes("contact") || msg.includes("inquiry")) {
    return "member-contact.html";
  }

  // C. Project & Milestone Notifications
  if (title.includes("project") || title.includes("assignment") || title.includes("milestone") ||
    msg.includes("project") || msg.includes("assignment") || msg.includes("milestone")) {
    if (isMember) {
      return "member.html";
    } else if (isAdmin) {
      return "admin.html";
    } else {
      return "rented-project.html";
    }
  }

  // D. Payment & Transaction Notifications
  if (title.includes("payment") || title.includes("transaction") || title.includes("invoice") ||
    msg.includes("payment") || msg.includes("transaction") || msg.includes("invoice")) {
    return "transaction.html";
  }

  // 4. Default Role-based Fallback Routing
  if (isAdmin) return "admin.html";
  if (isMember) return "member.html";
  return "rented-project.html";
}

function formatNotificationTime(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  const diffMs = Date.now() - d.getTime();
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return "just now";
  if (diffMin < 60) return `${diffMin} min ago`;
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr}h ago`;
  const diffDay = Math.floor(diffHr / 24);
  if (diffDay < 7) return `${diffDay}d ago`;
  return d.toLocaleDateString();
}

// =============================================
//  Unified Notification System (Toast, Alert, Confirm)
// =============================================

function ensureToastStyles() {
  if (document.getElementById("toast-system-styles")) return;
  const style = document.createElement("style");
  style.id = "toast-system-styles";
  style.textContent = `
      #toast-container, .toast-container, #toast-notification-container {
        position: fixed;
        bottom: 24px;
        right: 24px;
        z-index: 999999;
        display: flex;
        flex-direction: column;
        gap: 10px;
        pointer-events: none;
        max-width: 380px;
        width: calc(100vw - 48px);
      }
      .toast-item, .toast-msg-item, .live-toast {
        pointer-events: auto;
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 14px 18px;
        border-radius: 12px;
        background: #111827;
        color: #f3f4f6;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(255, 255, 255, 0.12);
        opacity: 0;
        transform: translateY(20px) scale(0.95);
        transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
        font-family: inherit;
        font-size: 0.9rem;
      }
      .toast-item.show, .toast-msg-item.show, .live-toast.show {
        opacity: 1;
        transform: translateY(0) scale(1);
      }
      .toast-item.success, .toast-msg-item.success { border-left: 4px solid #10b981; }
      .toast-item.error, .toast-msg-item.error, .toast-item.danger { border-left: 4px solid #ef4444; }
      .toast-item.warning, .toast-msg-item.warning { border-left: 4px solid #f59e0b; }
      .toast-item.info, .toast-msg-item.info { border-left: 4px solid #3b82f6; }
    `;
  document.head.appendChild(style);
}

function showToast(arg1, arg2, arg3) {
  ensureToastStyles();
  let title = "";
  let message = "";
  let type = "success";

  const validTypes = ["success", "error", "warning", "info", "danger"];
  if (arg2 && validTypes.includes(String(arg2).toLowerCase())) {
    message = arg1 || "";
    type = String(arg2).toLowerCase();
    if (type === "danger") type = "error";
    title = arg3 || "";
  } else if (arg1 && !arg2 && !arg3) {
    message = arg1;
    type = "success";
  } else {
    title = arg1 || "";
    message = arg2 || "";
    type = String(arg3 || "success").toLowerCase();
    if (type === "danger") type = "error";
  }

  let container = document.getElementById("toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-container";
    container.className = "toast-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast-item ${type}`;

  let iconSvg = "";
  if (type === "success") {
    iconSvg = `<svg viewBox="0 0 24 24" style="width:20px;height:20px;stroke:#10b981;fill:none;stroke-width:2.5;flex-shrink:0;"><polyline points="20 6 9 17 4 12"/></svg>`;
  } else if (type === "error") {
    iconSvg = `<svg viewBox="0 0 24 24" style="width:20px;height:20px;stroke:#ef4444;fill:none;stroke-width:2.5;flex-shrink:0;"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>`;
  } else if (type === "warning") {
    iconSvg = `<svg viewBox="0 0 24 24" style="width:20px;height:20px;stroke:#f59e0b;fill:none;stroke-width:2.5;flex-shrink:0;"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>`;
  } else {
    iconSvg = `<svg viewBox="0 0 24 24" style="width:20px;height:20px;stroke:#3b82f6;fill:none;stroke-width:2.5;flex-shrink:0;"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>`;
  }

  const titleHtml = title ? `<div style="font-weight:700;font-size:0.9rem;margin-bottom:2px;">${escapeHtml(title)}</div>` : "";
  toast.innerHTML = `
      ${iconSvg}
      <div style="flex:1;min-width:0;">
        ${titleHtml}
        <div class="toast-message" style="line-height:1.4;">${escapeHtml(message)}</div>
      </div>
      <button onclick="this.parentElement.remove()" style="background:none;border:none;color:#9ca3af;cursor:pointer;font-size:1.1rem;line-height:1;padding:0 0 0 8px;">&times;</button>
    `;

  container.appendChild(toast);
  setTimeout(() => toast.classList.add("show"), 10);
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 400);
  }, 4000);
}

window.showToast = showToast;
window.showSuccessToast = (msg) => showToast(msg, "success");

window.showFormAlert = function (target, message, typeOrIsSuccess = "error") {
  let el = typeof target === "string" ? document.getElementById(target) : target;
  if (!el) return;

  let type = "error";
  if (typeof typeOrIsSuccess === "boolean") {
    type = typeOrIsSuccess ? "success" : "error";
  } else if (typeof typeOrIsSuccess === "string") {
    type = typeOrIsSuccess.toLowerCase();
  }

  const isSuccess = type === "success";
  el.textContent = message;
  el.className = `alert-message ${isSuccess ? "alert-success" : "alert-danger"}`;
  el.style.cssText = `
      display: block;
      padding: 0.75rem 1rem;
      margin: 0.75rem 0;
      border-radius: 8px;
      font-size: 0.88rem;
      font-weight: 500;
      line-height: 1.4;
      transition: all 0.3s ease;
      ${isSuccess
      ? 'background: rgba(16, 185, 129, 0.12); color: #10b981; border: 1px solid rgba(16, 185, 129, 0.3);'
      : 'background: rgba(239, 68, 68, 0.12); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.3);'}
    `;
};

window.clearFormAlert = function (target) {
  let el = typeof target === "string" ? document.getElementById(target) : target;
  if (el) {
    el.style.display = "none";
    el.textContent = "";
  }
};

window.showConfirmModal = function ({ title = "Confirm", message = "Are you sure you want to proceed?", confirmText = "Confirm", cancelText = "Cancel", onConfirm }) {
  let overlay = document.getElementById("global-confirm-modal");
  if (!overlay) {
    overlay = document.createElement("div");
    overlay.id = "global-confirm-modal";
    overlay.style.cssText = `
        position: fixed; inset: 0; z-index: 999999;
        background: rgba(10, 15, 30, 0.75); backdrop-filter: blur(8px);
        display: flex; align-items: center; justify-content: center;
        opacity: 0; visibility: hidden; transition: all 0.25s ease;
      `;
    overlay.innerHTML = `
        <div style="background: #111827; border: 1px solid rgba(255,255,255,0.12); border-radius: 16px; padding: 24px; max-width: 420px; width: 90%; box-shadow: 0 20px 50px rgba(0,0,0,0.5); transform: translateY(20px); transition: all 0.25s ease;" id="confirm-modal-box">
          <h3 id="confirm-modal-title" style="margin: 0 0 10px 0; font-size: 1.15rem; font-weight: 700; color: #fff;"></h3>
          <p id="confirm-modal-msg" style="margin: 0 0 20px 0; font-size: 0.9rem; color: #9ca3af; line-height: 1.5;"></p>
          <div style="display: flex; gap: 12px; justify-content: flex-end;">
            <button id="confirm-modal-cancel" style="padding: 9px 18px; border-radius: 8px; background: transparent; border: 1px solid rgba(255,255,255,0.2); color: #e5e7eb; font-weight: 600; cursor: pointer; transition: all 0.2s;"></button>
            <button id="confirm-modal-ok" style="padding: 9px 18px; border-radius: 8px; background: #ef4444; border: none; color: #fff; font-weight: 600; cursor: pointer; transition: all 0.2s;"></button>
          </div>
        </div>
      `;
    document.body.appendChild(overlay);
  }

  const titleEl = document.getElementById("confirm-modal-title");
  const msgEl = document.getElementById("confirm-modal-msg");
  const cancelBtn = document.getElementById("confirm-modal-cancel");
  const okBtn = document.getElementById("confirm-modal-ok");
  const box = document.getElementById("confirm-modal-box");

  titleEl.textContent = title;
  msgEl.textContent = message;
  cancelBtn.textContent = cancelText;
  okBtn.textContent = confirmText;

  function closeModal() {
    overlay.style.opacity = "0";
    overlay.style.visibility = "hidden";
    box.style.transform = "translateY(20px)";
  }

  cancelBtn.onclick = closeModal;
  overlay.onclick = (e) => { if (e.target === overlay) closeModal(); };

  okBtn.onclick = async () => {
    closeModal();
    if (typeof onConfirm === "function") {
      await onConfirm();
    }
  };

  overlay.style.visibility = "visible";
  overlay.style.opacity = "1";
  box.style.transform = "translateY(0)";
};

async function fetchAdminBookings() {
  fetchAdminQuotationsTable();
  const tbody = document.getElementById("bookings-table-body");
  if (!tbody) return;

  try {
    const token = getAdminToken();
    const response = await fetch("/api/bookings", {
      headers: adminHeaders()
    });
    if (!response.ok) throw new Error("Failed to fetch bookings");
    const bookings = await response.json();

    bookings.sort((a, b) => {
      const timeA = _lastUpdatedBookingTime[a.id] || 0;
      const timeB = _lastUpdatedBookingTime[b.id] || 0;
      if (timeA !== timeB) return timeB - timeA;
      return b.id - a.id;
    });

    if (Object.keys(_cache.services).length === 0) {
      await fetchAdminServicesTable();
    }
    if (Object.keys(_cache.users).length === 0) {
      await fetchAdminUsers();
    }

    _paginationState.booking.items = bookings;
    const totalPages = Math.ceil(bookings.length / PAGE_SIZE);
    if (_paginationState.booking.currentPage > totalPages) {
      _paginationState.booking.currentPage = Math.max(1, totalPages);
    }

    renderBookingTablePage();
  } catch (err) {
    console.error("fetchAdminBookings error:", err);
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:#ef4444;">Could not load bookings.</td></tr>`;
  }
}

function renderBookingTablePage() {
  const tbody = document.getElementById("bookings-table-body");
  if (!tbody) return;
  setupPaginationContainer(tbody, "booking");

  const bookings = _paginationState.booking.items;
  const currentPage = _paginationState.booking.currentPage;
  const startIndex = (currentPage - 1) * PAGE_SIZE;
  const pageBookings = bookings.slice(startIndex, startIndex + PAGE_SIZE);

  tbody.innerHTML = "";
  if (!pageBookings.length) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--text-muted);">No bookings found.</td></tr>`;
    renderPaginationControls("booking", bookings.length);
    return;
  }

  const memberUsers = Object.values(_cache.users).filter(u => u.role === "ROLE_MEMBER");

  pageBookings.forEach(b => {
    _cache.bookings[b.id] = b;

    const tr = document.createElement("tr");
    const client = _cache.users[b.clientId] || { fullName: `User #${b.clientId}`, email: "" };
    const service = _cache.services[b.serviceId] || { title: `Service #${b.serviceId}` };

    tr.setAttribute("data-searchable", `${client.fullName} ${service.title} ${b.appointmentDate} ${b.status}`);

    // Expert select options
    let expertOptions = `<option value="">-- Assign Expert --</option>`;
    memberUsers.forEach(u => {
      const selected = (b.expertId && String(b.expertId) === String(u.id)) ? "selected" : "";
      expertOptions += `<option value="${u.id}" ${selected}>${escapeHtml(u.fullName)}</option>`;
    });

    // Status options
    const statuses = ["PENDING", "CONFIRMED", "PRICING", "CANCELLED", "COMPLETED"];
    let statusOptions = "";
    statuses.forEach(s => {
      const selected = (b.status === s) ? "selected" : "";
      statusOptions += `<option value="${s}" ${selected}>${s}</option>`;
    });

    // Once COMPLETED, admin can no longer touch this booking
    const isLocked = b.status === "COMPLETED";
    // Date/time can only be edited manually while the booking is in CONFIRMED status
    const isEditableSchedule = b.status === "CONFIRMED";

    const scheduleHtml = isEditableSchedule
      ? `<input type="date" id="bkDate-${b.id}" value="${escapeHtml(b.appointmentDate)}" class="admin-select" style="padding:0.3rem;font-size:0.78rem;margin-bottom:4px;width:100%;max-width:140px;">
           <input type="time" id="bkTime-${b.id}" value="${escapeHtml(b.timeSlot ? b.timeSlot.substring(0, 5) : "")}" class="admin-select" style="padding:0.3rem;font-size:0.78rem;width:100%;max-width:140px;margin-bottom:4px;">
           <button type="button" class="btn-add" onclick="saveBookingSchedule(${b.id})" style="padding:0.25rem 0.5rem;font-size:0.72rem;border-radius:6px;cursor:pointer;">Save Date</button>`
      : `<div class="text-dark-inline">${escapeHtml(b.appointmentDate)}</div>
           <div style="font-size:0.85rem;color:var(--text-muted);">${escapeHtml(b.timeSlot ? b.timeSlot.substring(0, 5) : "")}</div>`;

    let extraActions = "";
    if (b.status === "PRICING") {
      extraActions += `<button type="button" class="btn-add" onclick="openQuoteModal(${b.id})" style="padding:0.35rem 0.6rem;font-size:0.8rem;gap:4px;border-radius:6px;cursor:pointer;">
            <svg viewBox="0 0 24 24" style="width:12px;height:12px;"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg> Create Quote
          </button>`;
    }
    if (b.status === "CONFIRMED") {
      extraActions += `<button type="button" class="btn-add" onclick="openBookingEmailModal(${b.id})" style="padding:0.35rem 0.6rem;font-size:0.8rem;gap:4px;border-radius:6px;cursor:pointer;background-color:#4f46e5;">
            <svg viewBox="0 0 24 24" style="width:12px;height:12px;stroke:currentColor;fill:none;stroke-width:2;"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="M22 6l-10 7L2 6"/></svg>
          </button>`;
    }

    tr.innerHTML = `
      <td>
        <div class="text-dark-inline">${escapeHtml(client.fullName)}</div>
        <div style="font-size:0.8rem;color:var(--text-muted);">${escapeHtml(client.email)}</div>
      </td>
      <td class="text-dark-inline">${escapeHtml(service.title)}</td>
      <td>
        ${scheduleHtml}
      </td>
      <td>
        <select class="admin-select" onchange="updateBookingExpert(${b.id}, this.value)" ${isLocked ? "disabled" : ""} style="padding:0.35rem;border-radius:6px;border:1px solid var(--border-color);font-size:0.85rem;width:100%;max-width:160px;background:var(--bg-card);color:var(--text-dark);">
          ${expertOptions}
        </select>
      </td>
      <td>
        <select class="admin-select status-select status-${b.status.toLowerCase()}" onchange="updateBookingStatus(${b.id}, this.value)" ${isLocked ? "disabled" : ""} style="padding:0.35rem;border-radius:6px;border:1px solid var(--border-color);font-weight:600;font-size:0.85rem;width:100%;max-width:130px;">
          ${statusOptions}
        </select>
      </td>
      <td>
        <div style="display:flex;gap:6px;align-items:center;flex-wrap:wrap;">
          <button type="button" class="btn-detail" onclick="openBookingDetailModal(${b.id})" style="padding:0.35rem 0.65rem;font-size:0.8rem;gap:4px;border-radius:6px;background:rgba(37,99,235,0.1);color:#2563eb;border:1px solid rgba(37,99,235,0.22);cursor:pointer;font-weight:600;display:inline-flex;align-items:center;transition:all 0.2s ease;">
            <svg viewBox="0 0 24 24" style="width:13px;height:13px;stroke:currentColor;fill:none;stroke-width:2;"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
          </button>
          ${extraActions}
          ${!isLocked ? `<button type="button" class="btn-delete" onclick="deleteBooking(${b.id})" style="padding:0.35rem 0.6rem;font-size:0.8rem;gap:4px;border-radius:6px;background-color:#ef4444;color:#fff;border:none;cursor:pointer;">
            <svg viewBox="0 0 24 24" style="width:12px;height:12px;stroke:currentColor;fill:none;stroke-width:2;"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>
          </button>` : `<span style="font-size:0.78rem;color:var(--text-muted);font-style:italic;">Done — locked</span>`}
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });

  renderPaginationControls("booking", bookings.length);
}

// =============================================
//  Admin – Consultation Booking Detail Modal
// =============================================

async function openBookingDetailModal(bookingId) {
  const modal = document.getElementById("booking-detail-modal-overlay");
  const container = document.getElementById("bd-modal-body");
  const idLabel = document.getElementById("bd-booking-id");
  if (!modal || !container) return;

  let b = _cache.bookings[bookingId];
  if (!b) {
    try {
      const res = await fetch(`/api/bookings/${bookingId}`, { headers: adminHeaders() });
      if (res.ok) {
        b = await res.json();
        _cache.bookings[bookingId] = b;
      }
    } catch (e) {
      console.error("Fetch single booking error:", e);
    }
  }

  if (!b) {
    showToast("Booking information not found.", "error");
    return;
  }

  if (idLabel) idLabel.textContent = `Booking #${b.id}`;

  const client = _cache.users[b.clientId] || { fullName: `User #${b.clientId}`, email: "—", phone: "—" };
  const service = _cache.services[b.serviceId] || { title: `Service #${b.serviceId}` };
  const expert = b.expertId ? (_cache.users[b.expertId] || { fullName: `Expert #${b.expertId}`, email: "" }) : null;

  let displayMessage = b.messageContent || "";
  let displayAttachmentUrl = b.attachmentUrl || null;

  if (displayMessage) {
    const match = displayMessage.match(/(?:\r?\n)*\s*(?:📎\s*)?Attachment:\s*(https?:\/\/[^\s]+|\/uploads\/[^\s]+)/i);
    if (match) {
      if (!displayAttachmentUrl) {
        displayAttachmentUrl = match[1];
      }
      displayMessage = displayMessage.replace(/(?:\r?\n)*\s*(?:📎\s*)?Attachment:\s*(https?:\/\/[^\s]+|\/uploads\/[^\s]+)/i, '').trim();
    }
  }

  let attachmentHtml = "<span style='color:var(--text-muted); font-size:0.85rem;'>None</span>";
  if (displayAttachmentUrl) {
    const fileName = displayAttachmentUrl.split("/").pop();
    attachmentHtml = `<a href="${escapeHtml(displayAttachmentUrl)}" target="_blank" class="attachment-link" style="display:inline-flex;align-items:center;gap:4px;color:#2563eb;font-weight:600;font-size:0.85rem;text-decoration:none;"><svg viewBox="0 0 24 24" style="width:14px;height:14px;stroke:currentColor;fill:none;stroke-width:2;"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>${escapeHtml(fileName)}</a>`;
  }

  container.innerHTML = `
      <!-- Service & Pricing Summary -->
      <div style="padding: 1.15rem 1.25rem; border: 1px solid var(--border-color); border-radius: 12px; background: var(--bg-white);">
        <div style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); font-weight: 700; margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.4rem;">
          <svg viewBox="0 0 24 24" style="width: 15px; height: 15px; fill: none; stroke: currentColor; stroke-width: 2;"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/></svg>
          Service & Pricing Summary
        </div>
        
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem; padding-bottom: 0.65rem; border-bottom: 1px dashed var(--border-color);">
          <span style="font-size: 0.95rem; font-weight: 700;" class="text-dark-inline">${escapeHtml(service.title)}</span>
          <span style="font-size: 0.83rem; color: var(--text-muted); font-weight: 600;">Client: ${escapeHtml(client.fullName)}</span>
        </div>

        <!-- Editable Base Price -->
        <div style="display: flex; align-items: center; justify-content: space-between; background: var(--bg-light, #f8fafc); padding: 0.65rem 0.85rem; border-radius: 8px; margin-bottom: 0.65rem; border: 1px solid var(--border-color);">
          <span style="font-size: 0.85rem; font-weight: 600; color: var(--text-dark);">Service Base Price ($):</span>
          <div style="display: flex; align-items: center; gap: 6px;">
            <input type="number" id="bd-price-input-${b.id}" value="${Number(b.basePrice || 0)}" min="0" step="0.01" style="width: 95px; padding: 0.35rem 0.55rem; border: 1px solid var(--border-color); border-radius: 6px; font-size: 0.85rem; background: var(--bg-card); color: var(--text-dark);">
            <button type="button" onclick="updateBookingPriceFromModal(${b.id})" style="padding: 0.35rem 0.7rem; font-size: 0.78rem; font-weight: 600; border-radius: 6px; background: #2563eb; color: #fff; border: none; cursor: pointer;">Save</button>
          </div>
        </div>

        <div id="bd-addons-section-${b.id}" style="font-size: 0.83rem; color: var(--text-muted); margin-bottom: 0.65rem;">
          Loading add-ons...
        </div>

        <div style="border-top: 1px solid var(--border-color); padding-top: 0.65rem; margin-top: 0.5rem; display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 800; font-size: 0.95rem;" class="text-dark-inline">Total Amount</span>
          <span style="font-weight: 800; font-size: 1.2rem; color: #2563eb;" id="bd-total-price-val-${b.id}">$${Number(b.totalPrice || 0).toFixed(2)}</span>
        </div>
      </div>

      <!-- Client Request & Attachment -->
      <div style="padding: 1.15rem 1.25rem; border: 1px solid var(--border-color); border-radius: 12px; background: var(--bg-white);">
        <div style="font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--text-muted); font-weight: 700; margin-bottom: 0.65rem; display: flex; align-items: center; gap: 0.4rem;">
          <svg viewBox="0 0 24 24" style="width: 15px; height: 15px; fill: none; stroke: currentColor; stroke-width: 2;"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          Client Request & Message
        </div>
        <div style="font-size: 0.88rem; line-height: 1.5; color: var(--text-dark); white-space: pre-wrap; background: var(--bg-light, #f8fafc); padding: 0.8rem 1rem; border-radius: 8px; border: 1px solid var(--border-color); margin-bottom: 0.75rem;">${escapeHtml(displayMessage || "No specific request/message provided.")}</div>
        
        <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 0.85rem;">
          <span style="font-weight: 600; color: var(--text-muted);">Attachment:</span>
          ${attachmentHtml}
        </div>
      </div>
    `;

  modal.onclick = (e) => {
    if (e.target === modal) closeBookingDetailModal();
  };

  modal.classList.add("is-open");
  document.body.style.overflow = "hidden";

  // Load add-ons detail async
  try {
    const res = await fetch(`/api/bookings/pricing?serviceId=${b.serviceId}`);
    if (res.ok) {
      const data = await res.json();
      const addonsSection = document.getElementById(`bd-addons-section-${b.id}`);
      if (addonsSection) {
        const selectedAddons = (data.addons || []).filter(a => b.addonIds && b.addonIds.includes(a.id));
        if (selectedAddons.length > 0) {
          let addonsListHtml = `<div style="font-size: 0.8rem; font-weight: 600; color: var(--text-muted); margin-bottom: 0.3rem;">Selected Add-ons:</div><ul style="margin: 0; padding-left: 1.2rem; font-size: 0.83rem; color: var(--text-dark);">`;
          selectedAddons.forEach(a => {
            addonsListHtml += `<li><strong>${escapeHtml(a.addonName)}</strong> (+$${Number(a.priceModifier).toFixed(2)})</li>`;
          });
          addonsListHtml += `</ul>`;
          addonsSection.innerHTML = addonsListHtml;
        } else {
          addonsSection.innerHTML = `<span style="font-size:0.83rem; color:var(--text-muted);">Selected Add-ons: None</span>`;
        }
      }
    }
  } catch (e) {
    console.warn("Could not load pricing addons:", e);
  }
}

function closeBookingDetailModal() {
  const modal = document.getElementById("booking-detail-modal-overlay");
  if (modal) modal.classList.remove("is-open");
  document.body.style.overflow = "";
}

async function updateBookingPriceFromModal(bookingId) {
  const input = document.getElementById(`bd-price-input-${bookingId}`);
  if (!input) return;
  const newPrice = parseFloat(input.value);
  if (isNaN(newPrice) || newPrice < 0) {
    showToast("Please enter a valid price.", "error");
    return;
  }

  try {
    const response = await fetch(`/api/bookings/${bookingId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({ basePrice: newPrice })
    });
    if (response.ok) {
      const updated = await response.json();
      _cache.bookings[bookingId] = updated;
      const totalValEl = document.getElementById(`bd-total-price-val-${bookingId}`);
      if (totalValEl) totalValEl.textContent = `$${Number(updated.totalPrice || 0).toFixed(2)}`;
      showToast("Booking price updated successfully!", "success");
      fetchAdminBookings();
    } else {
      const errData = await response.json().catch(() => ({}));
      showToast(errData.message || "Failed to update price.", "error");
    }
  } catch (err) {
    console.error("updateBookingPriceFromModal error:", err);
    showToast("Could not connect to server.", "error");
  }
}

window.openBookingDetailModal = openBookingDetailModal;
window.closeBookingDetailModal = closeBookingDetailModal;
window.updateBookingPriceFromModal = updateBookingPriceFromModal;

const _lastUpdatedBookingTime = {};

async function updateBookingExpert(bookingId, expertId) {
  try {
    const response = await fetch(`/api/bookings/${bookingId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({ expertId: expertId ? Number(expertId) : null })
    });
    if (!response.ok) throw new Error("Failed to update expert");
    _lastUpdatedBookingTime[bookingId] = Date.now();
    showToast("Expert assigned successfully!", "success");
    fetchAdminBookings();
  } catch (err) {
    console.error(err);
    showToast("Failed to assign expert: " + err.message, "error");
  }
}

async function updateBookingStatus(bookingId, status) {
  try {
    const response = await fetch(`/api/bookings/${bookingId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({ status })
    });
    if (!response.ok) throw new Error("Failed to update status");
    _lastUpdatedBookingTime[bookingId] = Date.now();
    showToast("Booking status updated successfully!", "success");
    fetchAdminBookings();
  } catch (err) {
    console.error(err);
    showToast("Failed to update status: " + err.message, "error");
  }
}

// Admin edits the service price of a specific booking; server adds the client's already
// selected add-on(s) on top of it and returns the recalculated final total.
async function updateBookingPrice(bookingId) {
  const input = document.getElementById(`cf-booking-price-${bookingId}`);
  const price = parseFloat(input?.value);
  if (isNaN(price) || price < 0) {
    showToast("Please enter a valid, non-negative price.", "warning");
    return;
  }
  try {
    const response = await fetch(`/api/bookings/${bookingId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({ basePrice: price })
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || "Failed to update price");
    _lastUpdatedBookingTime[bookingId] = Date.now();
    showToast("Price updated — total recalculated with add-on(s)!", "success");
    fetchAdminBookings();
  } catch (err) {
    console.error(err);
    showToast("Failed to update price: " + err.message, "error");
  }
}

async function saveBookingSchedule(bookingId) {
  const dateInput = document.getElementById(`bkDate-${bookingId}`);
  const timeInput = document.getElementById(`bkTime-${bookingId}`);
  if (!dateInput || !timeInput || !dateInput.value || !timeInput.value) {
    showToast("Please pick both a date and a time.", "error");
    return;
  }
  try {
    const response = await fetch(`/api/bookings/${bookingId}`, {
      method: "PUT",
      headers: adminHeaders(),
      body: JSON.stringify({ appointmentDate: dateInput.value, timeSlot: timeInput.value })
    });
    if (!response.ok) throw new Error("Failed to update schedule");
    _lastUpdatedBookingTime[bookingId] = Date.now();
    showToast("Booking date updated — the client has been notified.", "success");
    fetchAdminBookings();
  } catch (err) {
    console.error(err);
    showToast("Failed to update schedule: " + err.message, "error");
  }
}

function openBookingEmailModal(bookingId) {
  const booking = _cache.bookings[bookingId];
  if (!booking) return;
  document.getElementById("bookingEmailId").value = booking.id;
  document.getElementById("bookingEmailMessage").value = "";
  document.getElementById("booking-email-modal-overlay").classList.add("is-open");
}

function closeBookingEmailModal() {
  document.getElementById("booking-email-modal-overlay").classList.remove("is-open");
}

async function submitBookingUpdateEmail() {
  const bookingId = document.getElementById("bookingEmailId").value;
  const message = document.getElementById("bookingEmailMessage").value;
  try {
    const response = await fetch(`/api/bookings/${bookingId}/send-update-email`, {
      method: "POST",
      headers: adminHeaders(),
      body: JSON.stringify({ message })
    });
    if (!response.ok) throw new Error("Failed to send email");
    showToast("Update email sent to the client!", "success");
    closeBookingEmailModal();
  } catch (err) {
    console.error(err);
    showToast("Failed to send email: " + err.message, "error");
  }
}

window.saveBookingSchedule = saveBookingSchedule;
window.openBookingEmailModal = openBookingEmailModal;
window.closeBookingEmailModal = closeBookingEmailModal;
window.submitBookingUpdateEmail = submitBookingUpdateEmail;

async function deleteBooking(bookingId) {
  showConfirmModal({
    title: "Delete Booking",
    message: "Are you sure you want to delete this booking appointment? This action cannot be undone.",
    confirmText: "Delete",
    cancelText: "Cancel",
    onConfirm: async () => {
      try {
        const response = await fetch(`/api/bookings/${bookingId}`, {
          method: "DELETE",
          headers: adminHeaders()
        });
        if (!response.ok) throw new Error("Failed to delete booking");
        showToast("Booking deleted successfully!", "success");
        fetchAdminBookings();
      } catch (err) {
        console.error(err);
        showToast("Failed to delete booking: " + err.message, "error");
      }
    }
  });
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
      web: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>`,
      design: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"></path><path d="M12 8A4 4 0 1 0 12 16A4 4 0 1 0 12 8Z"></path><path d="M12 2V6"></path><path d="M12 18V22"></path><path d="M4.93 4.93L7.76 7.76"></path><path d="M16.24 16.24L19.07 19.07"></path><path d="M2 12H6"></path><path d="M18 12H22"></path><path d="M4.93 19.07L7.76 16.24"></path><path d="M16.24 7.76L19.07 4.93"></path></svg>`,
      marketing: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>`,
      mobile: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>`,
      branding: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><circle cx="12" cy="12" r="6"></circle><circle cx="12" cy="12" r="2"></circle></svg>`,
      cloud: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.88 18.04A6 6 0 0 0 6 18a5.5 5.5 0 0 0 .5-10.96 4 4 0 1 0 7.82-1.74l.06-.02a6 6 0 0 0 6.5 12.76z"></path></svg>`
    };

    const accentClasses = {
      web: "service-web", design: "service-design", marketing: "service-marketing",
      mobile: "service-mobile", branding: "service-branding", cloud: "service-cloud"
    };

    services.forEach(service => {
      const card = document.createElement("div");
      card.className = "service-card";
      const iconKey = service.iconUrl || "web";
      const iconSvg = icons[iconKey] || icons.web;
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
  // Team members are hardcoded directly on about.html page, so this is a no-op
  return;
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
    modalLink.onclick = (e) => {
      e.preventDefault();
      openDemoSliderModal(project);
    };
    modalLinkWrapper.style.display = 'block';
  }

  modalOverlay.classList.add('is-open');
  document.body.style.overflow = 'hidden';

  // Set active project tracker and fetch milestones in real-time
  activeProjectInModal = project.id;
  if (typeof project.id === 'number') {
    fetchAndRenderProjectMilestones(project.id);
  } else {
    const milestonesList = document.getElementById('project-modal-milestones-list');
    if (milestonesList) {
      milestonesList.innerHTML = `<p style="color: var(--text-muted); font-size: 0.9rem; font-style: italic;">No active milestones for this demo project.</p>`;
    }
  }
}

function closeProjectModal() {
  const modalOverlay = document.getElementById('project-modal-overlay');
  if (!modalOverlay) return;
  modalOverlay.classList.remove('is-open');
  document.body.style.overflow = '';

  // Clear active project tracker
  activeProjectInModal = null;
}

let currentSliderIndex = 0;
let sliderImagesList = [];

function openDemoSliderModal(project) {
  const sliderModal = document.getElementById('demo-slider-modal');
  const sliderTitle = document.getElementById('demo-slider-title');
  const sliderTrack = document.getElementById('demo-slider-track');
  const sliderDots = document.getElementById('demo-slider-dots');

  if (!sliderModal || !sliderTrack || !sliderDots) return;

  sliderTitle.textContent = `${project.title || 'Project'} - Live Demo Gallery`;

  // Determine 5 relevant demo images based on project specific demoImages or category
  let imgs = project.demoImages || [];
  if (imgs.length === 0) {
    const category = (project.category || '').toLowerCase();
    if (category.includes('e-commerce') || category.includes('web')) {
      imgs = [
        'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1469037490029-44ab9539d5e1?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1558769132-cb1aea458c5e?auto=format&fit=crop&w=800&q=80'
      ];
    } else {
      imgs = [
        'https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1504868584819-f8e8b4b6d7e3?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1551836022-d5d88e9218df?auto=format&fit=crop&w=800&q=80',
        'https://images.unsplash.com/photo-1531403009284-440f080d1e12?auto=format&fit=crop&w=800&q=80'
      ];
    }
  }

  sliderImagesList = imgs;
  currentSliderIndex = 0;

  // Render slider track images
  sliderTrack.innerHTML = imgs.map(img =>
    `<img src="${img}" class="demo-slide-img" alt="Demo Screen" onerror="this.src='https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&h=400'">`
  ).join('');

  // Render dots
  sliderDots.innerHTML = imgs.map((_, i) =>
    `<span class="demo-slider-dot ${i === 0 ? 'active' : ''}" data-index="${i}"></span>`
  ).join('');

  // Bind dots click
  const dots = sliderDots.querySelectorAll('.demo-slider-dot');
  dots.forEach(dot => {
    dot.onclick = () => {
      const targetIdx = parseInt(dot.getAttribute('data-index'));
      goToSlide(targetIdx);
    };
  });

  updateSliderState();
  sliderModal.classList.add('is-open');

  // Bind close and arrow actions
  document.getElementById('demo-slider-close').onclick = closeDemoSliderModal;
  document.getElementById('demo-slider-prev').onclick = () => goToSlide(currentSliderIndex - 1);
  document.getElementById('demo-slider-next').onclick = () => goToSlide(currentSliderIndex + 1);

  sliderModal.onclick = (e) => {
    if (e.target === sliderModal) closeDemoSliderModal();
  };
}

function goToSlide(index) {
  if (index < 0 || index >= sliderImagesList.length) return;
  currentSliderIndex = index;
  updateSliderState();
}

function updateSliderState() {
  const sliderTrack = document.getElementById('demo-slider-track');
  const prevBtn = document.getElementById('demo-slider-prev');
  const nextBtn = document.getElementById('demo-slider-next');
  const dots = document.querySelectorAll('.demo-slider-dot');

  if (!sliderTrack) return;

  sliderTrack.style.transform = `translateX(-${currentSliderIndex * 100}%)`;

  if (prevBtn) prevBtn.disabled = currentSliderIndex === 0;
  if (nextBtn) nextBtn.disabled = currentSliderIndex === sliderImagesList.length - 1;

  dots.forEach((dot, i) => {
    if (i === currentSliderIndex) {
      dot.classList.add('active');
    } else {
      dot.classList.remove('active');
    }
  });
}

function closeDemoSliderModal() {
  const sliderModal = document.getElementById('demo-slider-modal');
  if (sliderModal) {
    sliderModal.classList.remove('is-open');
  }
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

const staticProjects = [
  {
    id: "static-1",
    title: "Mart06 Fashion System",
    description: "High-performance online shopping platform with seamless automated payment integration and full inventory management.",
    category: "Website E-Commerce",
    technologies: "Java, Spring Boot, MySQL, Thymeleaf, CSS3",
    imageUrl: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=500&h=300",
    demoImages: [
      'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1469037490029-44ab9539d5e1?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1558769132-cb1aea458c5e?auto=format&fit=crop&w=800&q=80'
    ]
  },
  {
    id: "static-2",
    title: "NovaDigital Mobile Portal",
    description: "Premium mobile application for project coordination, client messaging, and real-time SSE milestone progress alerts.",
    category: "Mobile Application",
    technologies: "React Native, Node.js, SSE, MySQL",
    imageUrl: "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?auto=format&fit=crop&w=500&h=300",
    demoImages: [
      'https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1531403009284-440f080d1e12?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1551650975-87deedd944c3?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1611162617213-7d7a39e9b1d7?auto=format&fit=crop&w=800&q=80'
    ]
  },
  {
    id: "static-3",
    title: "CloudPay Analytics Dashboard",
    description: "SaaS analytics dashboard with real-time financial reporting, role-based access control, and Stripe billing integration.",
    category: "Cloud SaaS",
    technologies: "Vue.js, Spring Boot, PostgreSQL, Docker",
    imageUrl: "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=500&h=300",
    demoImages: [
      'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1563013544-824ae1d704d3?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1504868584819-f8e8b4b6d7e3?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1551836022-d5d88e9218df?auto=format&fit=crop&w=800&q=80'
    ]
  },
  {
    id: "static-4",
    title: "Vespera Branding Identity",
    description: "Luxury visual identity design, custom typography, brand guidelines, and assets.",
    category: "Branding Design",
    technologies: "Figma, Illustrator, Brand Strategy",
    imageUrl: "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS-qBs7oaYxuHoIWnqe6f1qtKPyYP9kyNtMlbPXiAS2Hg&s=10",
    demoImages: [
      'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS-qBs7oaYxuHoIWnqe6f1qtKPyYP9kyNtMlbPXiAS2Hg&s=10',
      'https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1586717791821-3f44a563fa4c?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1541462608141-2ffb68df685e?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=800&q=80'
    ]
  },
  {
    id: "static-5",
    title: "Apex SEO & Marketing Campaign",
    description: "High-impact search engine optimization and digital marketing campaign driving 200% growth.",
    category: "Marketing Campaign",
    technologies: "Google Analytics, SEO, SEM, Content Marketing",
    imageUrl: "https://images.unsplash.com/photo-1434626881859-194d67b2b86f?auto=format&fit=crop&w=500&h=300",
    demoImages: [
      'https://images.unsplash.com/photo-1434626881859-194d67b2b86f?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1557804506-669a67965ba0?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1551836022-d5d88e9218df?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1551836022-b5b3bb1945ff?auto=format&fit=crop&w=800&q=80'
    ]
  },
  {
    id: "static-6",
    title: "Aurora Smart Home App",
    description: "Internet of Things (IoT) mobile dashboard controlling smart lighting, temperature, and security.",
    category: "Mobile Application",
    technologies: "Flutter, Firebase, IoT WebSockets",
    imageUrl: "https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=500&h=300",
    demoImages: [
      'https://images.unsplash.com/photo-1558002038-1055907df827?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1543512214-318c7553f230?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1558002038-028f2c2bbd39?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1508847154043-be12a62861c1?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80'
    ]
  },
  {
    id: "static-7",
    title: "Omni Channel Retail System",
    description: "Comprehensive point-of-sale and online catalog syncing automatically with inventory databases.",
    category: "Website Development",
    technologies: "React, Node.js, Express, PostgreSQL",
    imageUrl: "https://images.unsplash.com/photo-1472851294608-062f824d29cc?auto=format&fit=crop&w=500&h=300",
    demoImages: [
      'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1556740758-90de374c12ad?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1472851294608-062f824d29cc?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1507925921958-8a62f3d1a50d?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1563013544-824ae1d704d3?auto=format&fit=crop&w=800&q=80'
    ]
  },
  {
    id: "static-8",
    title: "Stellar UI Component Library",
    description: "A collection of premium, interactive user interface components built with CSS3 and modern JS.",
    category: "UI/UX Design",
    technologies: "HTML5, TailwindCSS, Vue.js, Storybook",
    imageUrl: "https://www.vuescript.com/wp-content/uploads/2024/03/Fully-Styled-And-Customizable-UI-Components-Library-Stellar-370x297.webp",
    demoImages: [
      'https://www.vuescript.com/wp-content/uploads/2024/03/Fully-Styled-And-Customizable-UI-Components-Library-Stellar-370x297.webp',
      'https://images.unsplash.com/photo-1541462608141-2ffb68df685e?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1531403009284-440f080d1e12?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?auto=format&fit=crop&w=800&q=80'
    ]
  },
  {
    id: "static-9",
    title: "Krypton Edge Cloud Cluster",
    description: "Automated infrastructure provisioning using Kubernetes with load balancing and auto-scaling.",
    category: "Cloud Solutions",
    technologies: "Kubernetes, Terraform, AWS, Docker",
    imageUrl: "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=500&h=300",
    demoImages: [
      'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1600132806370-bf17e65e942f?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1597852074816-d933c4d2b988?auto=format&fit=crop&w=800&q=80'
    ]
  }
];

async function fetchProjects() {
  const projectsGrid = document.getElementById("projects-grid");
  if (!projectsGrid) return;

  try {
    const cards = projectsGrid.querySelectorAll(".project-card");

    // Bind click listeners on static cards to open modal
    cards.forEach((card, idx) => {
      const project = staticProjects[idx];
      if (!project) return;

      const elementsToClick = [
        card.querySelector('.project-title-clickable'),
        card.querySelector('.project-link'),
        card.querySelector('.project-image-wrapper')
      ];

      elementsToClick.forEach(el => {
        if (el) {
          el.addEventListener('click', (e) => {
            e.preventDefault();
            openProjectModal(project);
          });
        }
      });
    });

    // Bind filter buttons
    const filterContainer = document.getElementById("project-filters");
    if (filterContainer) {
      const buttons = filterContainer.querySelectorAll(".filter-btn");
      buttons.forEach(btn => {
        btn.addEventListener("click", () => {
          buttons.forEach(b => b.classList.remove("active"));
          btn.classList.add("active");

          const filterValue = btn.getAttribute("data-filter").toLowerCase();
          cards.forEach(card => {
            const cardCat = (card.getAttribute("data-category") || "").toLowerCase();
            if (filterValue === "all" || cardCat.includes(filterValue)) {
              card.style.display = "flex";
            } else {
              card.style.display = "none";
            }
          });
        });
      });
    }

    // Bind modal close events
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
      if (e.key === 'Escape') {
        closeProjectModal();
        closeDemoSliderModal();
      }
    });

  } catch (error) {
    console.error("Error setting up projects page:", error);
  }
}

// =============================================
//  Contact Form
// =============================================

function initContactForm() {
  const form = document.getElementById("contactForm");
  const alertMsg = document.getElementById("alertMessage");
  if (!form || !alertMsg) return;

  const nameInput = document.getElementById("name");
  const emailInput = document.getElementById("email");
  const serviceSelect = document.getElementById("serviceSelect");
  const serviceGrid = document.getElementById("service-select-grid");
  const selectOverlay = document.getElementById("service-select-overlay");

  if (nameInput) {
    nameInput.value = localStorage.getItem("fullName") || sessionStorage.getItem("fullName") || localStorage.getItem("username") || sessionStorage.getItem("username") || "";
    nameInput.readOnly = true;
  }
  if (emailInput) {
    emailInput.value = localStorage.getItem("email") || sessionStorage.getItem("email") || "";
    emailInput.readOnly = true;
  }

  if (serviceSelect && serviceGrid) {
    fetch("/api/services")
      .then(res => res.json())
      .then(services => {
        serviceSelect.innerHTML = '<option value="" disabled selected>Choose a service to hire...</option>';
        serviceGrid.innerHTML = '';

        const icons = {
          web: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>`,
          design: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z"></path><path d="M12 8A4 4 0 1 0 12 16A4 4 0 1 0 12 8Z"></path><path d="M12 2V6"></path><path d="M12 18V22"></path><path d="M4.93 4.93L7.76 7.76"></path><path d="M16.24 16.24L19.07 19.07"></path><path d="M2 12H6"></path><path d="M18 12H22"></path><path d="M4.93 19.07L7.76 16.24"></path><path d="M16.24 7.76L19.07 4.93"></path></svg>`,
          marketing: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>`,
          mobile: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"></rect><line x1="12" y1="18" x2="12.01" y2="18"></line></svg>`,
          branding: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><circle cx="12" cy="12" r="6"></circle><circle cx="12" cy="12" r="2"></circle></svg>`,
          cloud: `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20.88 18.04A6 6 0 0 0 6 18a5.5 5.5 0 0 0 .5-10.96 4 4 0 1 0 7.82-1.74l.06-.02a6 6 0 0 0 6.5 12.76z"></path></svg>`
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

    const name = document.getElementById("name").value.trim();
    const email = document.getElementById("email").value.trim();
    const service = document.getElementById("serviceSelect").value;
    const title = document.getElementById("title").value.trim();
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
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + (localStorage.getItem("token") || sessionStorage.getItem("token") || "")
        },
        body: JSON.stringify({ name, email, title: finalTitle, content })
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
      showConfirmModal({
        title: "Delete Message",
        message: "Are you sure you want to delete this message? This action cannot be undone.",
        confirmText: "Delete",
        cancelText: "Cancel",
        onConfirm: async () => {
          await executeDelete(`/api/contacts/${id}`, "DELETE");
          fetchInbox(email);
        }
      });
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

      showConfirmModal({
        title: "Delete Selected Messages",
        message: `Are you sure you want to delete the ${state.selectedIds.size} selected message(s)? This action cannot be undone.`,
        confirmText: "Delete Selected",
        cancelText: "Cancel",
        onConfirm: async () => {
          const idsArray = Array.from(state.selectedIds);
          const idsParam = idsArray.join(",");
          await executeDelete(`/api/contacts/my?ids=${idsParam}`, "DELETE");
          fetchInbox(email);
        }
      });
    });
  }

  // Handle delete all button
  if (deleteAll) {
    deleteAll.addEventListener("click", async () => {
      showConfirmModal({
        title: "Delete All Messages",
        message: "Are you sure you want to delete ALL messages in your inbox? This action cannot be undone.",
        confirmText: "Delete All",
        cancelText: "Cancel",
        onConfirm: async () => {
          await executeDelete("/api/contacts/my", "DELETE");
          fetchInbox(email);
        }
      });
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
      showToast(result.message || "Deletion successful!", "success");
    } else {
      showToast(result.message || "Failed to delete message(s).", "error");
    }
  } catch (error) {
    console.error("Delete operation failed:", error);
    showToast("An error occurred during deletion. Please try again.", "error");
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

// Shared View Transition API helper for Circular Reveal Theme Toggle
window.toggleThemeWithTransition = function (event, toggleCallback) {
  const rect = (event && event.currentTarget) ? event.currentTarget.getBoundingClientRect() : null;
  const x = rect ? (rect.left + rect.width / 2) : (event ? event.clientX : window.innerWidth / 2);
  const y = rect ? (rect.top + rect.height / 2) : (event ? event.clientY : window.innerHeight / 2);
  document.documentElement.style.setProperty('--click-x', `${x}px`);
  document.documentElement.style.setProperty('--click-y', `${y}px`);

  if (!document.startViewTransition) {
    document.documentElement.classList.add("theme-transitioning");
    toggleCallback();
    setTimeout(() => {
      document.documentElement.classList.remove("theme-transitioning");
    }, 350);
    return;
  }

  document.startViewTransition(toggleCallback);
};

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
    window.toggleThemeWithTransition(e, () => {
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
    if (typeof updateChartTheme === "function") {
      updateChartTheme();
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

  // Append chatbot FAB button inside quick-panel for pixel-perfect vertical alignment and sizing
  const fabEl = document.getElementById("chatbot-fab");
  if (fabEl) {
    fabEl.classList.add("quick-panel-btn");
    quickPanel.appendChild(fabEl);
  }

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

  qThemeBtn.addEventListener("click", (e) => {
    window.toggleThemeWithTransition(e, () => {
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
    if (typeof updateChartTheme === "function") {
      updateChartTheme();
    }
  });

  // Inbox shortcut (hide on admin & hr & member pages or for ROLE_ADMIN)
  const token = localStorage.getItem("token") || sessionStorage.getItem("token");
  const role = localStorage.getItem("role") || sessionStorage.getItem("role");
  const currentPath = window.location.pathname.toLowerCase();

  const isAdminOrHrPage = currentPath.includes("admin") || currentPath.includes("hr") || currentPath.includes("resource") || currentPath.includes("member") || role === "ROLE_ADMIN";

  const quickInbox = document.getElementById("quick-inbox");
  if (token && !isAdminOrHrPage) {
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
  } else if (quickInbox) {
    quickInbox.style.display = "none";
  }
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

// =============================================
//  Audit Logs Dashboard Logic
// =============================================

const AUDIT_PAGE_SIZE = 10;
let auditDataPage = 0;
let auditAuthPage = 0;
let auditModalPage = 0;
let auditModalUsername = '';

function switchAuditTab(tabId) {
  const dataTabBtn = document.getElementById('btn-tab-data');
  const authTabBtn = document.getElementById('btn-tab-auth');
  const dataTab = document.getElementById('dataTab');
  const authTab = document.getElementById('authTab');

  if (tabId === 'dataTab') {
    dataTab.style.display = 'block';
    authTab.style.display = 'none';
    dataTabBtn.style.background = 'linear-gradient(135deg, rgba(37,99,235,0.1), rgba(79,70,229,0.1))';
    dataTabBtn.style.borderColor = 'rgba(37,99,235,0.2)';
    dataTabBtn.style.color = '#2563eb';
    authTabBtn.style.background = 'transparent';
    authTabBtn.style.borderColor = 'transparent';
    authTabBtn.style.color = 'var(--text-muted)';
    loadDataUsers(0);
  } else {
    authTab.style.display = 'block';
    dataTab.style.display = 'none';
    authTabBtn.style.background = 'linear-gradient(135deg, rgba(37,99,235,0.1), rgba(79,70,229,0.1))';
    authTabBtn.style.borderColor = 'rgba(37,99,235,0.2)';
    authTabBtn.style.color = '#2563eb';
    dataTabBtn.style.background = 'transparent';
    dataTabBtn.style.borderColor = 'transparent';
    dataTabBtn.style.color = 'var(--text-muted)';
    loadAuthLogs(0);
  }
}

function formatAuditDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('en-US', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  });
}

function getDataActionBadge(action) {
  const upperAction = action ? action.toUpperCase() : '';
  if (upperAction.includes('CREATE') || upperAction.includes('INSERT')) {
    return `<span class="status-badge badge-active">CREATE</span>`;
  } else if (upperAction.includes('UPDATE')) {
    return `<span class="status-badge badge-user">UPDATE</span>`;
  } else if (upperAction.includes('DELETE')) {
    return `<span style="background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; padding: 0.2rem 0.6rem; border-radius: 50px; font-size: 0.72rem; font-weight: 700; text-transform: uppercase;">DELETE</span>`;
  }
  return `<span class="status-badge status-pending">${action}</span>`;
}

function formatDiff(detailStr) {
  if (!detailStr || detailStr === '[]' || detailStr === 'null') {
    return '<span style="color: var(--text-muted); font-style: italic;">No detailed changes</span>';
  }

  // Check if it's a failed log (starts with [FAILED])
  let isFailed = false;
  let errorMessage = '';
  let jsonPart = detailStr;

  if (detailStr.startsWith('[FAILED]')) {
    isFailed = true;
    const separatorIdx = detailStr.indexOf(' | ');
    if (separatorIdx !== -1) {
      errorMessage = detailStr.substring(8, separatorIdx);
      jsonPart = detailStr.substring(separatorIdx + 3);
    } else {
      errorMessage = detailStr.substring(8);
      jsonPart = '';
    }
  }

  let html = '';
  if (isFailed) {
    html += `
            <div style="background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 0.6rem 0.8rem; margin-bottom: 0.6rem; color: #dc2626; font-size: 0.8rem;">
                <div style="font-weight: 700; display: flex; align-items: center; gap: 6px; margin-bottom: 2px;">
                    <svg viewBox="0 0 24 24" style="width: 14px; height: 14px; stroke: currentColor; fill: none; stroke-width: 2.5;"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                    Operation failed
                </div>
                <div style="font-family: monospace; font-size: 0.75rem; word-break: break-all;">${errorMessage}</div>
            </div>
        `;
  }

  if (!jsonPart || jsonPart === '(no payload)') {
    return html || '<span style="color: var(--text-muted); font-style: italic;">No data</span>';
  }

  try {
    const parsed = JSON.parse(jsonPart);

    if (Array.isArray(parsed)) {
      // Render a beautiful 3-column table like the mockup
      let tableHtml = html + `
                <div style="overflow-x: auto; border: 1px solid var(--border-color); border-radius: 12px; background: var(--bg-light); box-shadow: var(--shadow-sm);">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.8rem;">
                        <thead>
                            <tr style="border-bottom: 1.5px solid var(--border-color); background: rgba(0, 0, 0, 0.02);">
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: var(--text-dark); text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 25%;">FIELD</th>
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: var(--text-dark); text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 37.5%;">BEFORE</th>
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: var(--text-dark); text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 37.5%;">AFTER</th>
                            </tr>
                        </thead>
                        <tbody>
            `;

      parsed.forEach((diff, idx) => {
        const field = diff.field || 'unknown';
        const oldVal = diff.old !== null ? diff.old : '';
        const newVal = diff.new !== null ? diff.new : '';

        const beforeCell = oldVal !== ''
          ? `<span style="display: inline-flex; align-items: center; gap: 4px; background: #fee2e2; color: #dc2626; border: 1px solid #fca5a5; padding: 0.2rem 0.5rem; border-radius: 6px; font-family: monospace; text-decoration: line-through; font-size: 0.78rem;">
                        <svg viewBox="0 0 24 24" style="width:12px;height:12px;stroke:currentColor;fill:none;stroke-width:3;stroke-linecap:round;"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                        ${oldVal}
                       </span>`
          : `<span style="color: var(--text-muted); font-style: italic; font-size: 0.75rem;">(null)</span>`;

        const afterCell = newVal !== ''
          ? `<span style="display: inline-flex; align-items: center; gap: 4px; background: #d1fae5; color: #059669; border: 1px solid #a7f3d0; padding: 0.2rem 0.5rem; border-radius: 6px; font-family: monospace; font-weight: 600; font-size: 0.78rem;">
                        <svg viewBox="0 0 24 24" style="width:12px;height:12px;stroke:currentColor;fill:none;stroke-width:3;stroke-linecap:round;stroke-linejoin:round;"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        ${newVal}
                       </span>`
          : `<span style="color: #ef4444; background: #fef2f2; border: 1px solid #fee2e2; padding: 0.2rem 0.5rem; border-radius: 6px; font-size: 0.75rem; font-weight:600; display: inline-flex; align-items: center; gap: 4px;">
                        <svg viewBox="0 0 24 24" style="width:12px;height:12px;stroke:currentColor;fill:none;stroke-width:2.5;"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                        Deleted
                       </span>`;

        const borderStyle = idx < parsed.length - 1 ? 'border-bottom: 1px solid var(--border-color);' : '';

        tableHtml += `
                    <tr style="${borderStyle}">
                        <td style="padding: 0.75rem 1rem; font-weight: 700; color: #4f46e5; font-size: 0.75rem; font-family: var(--font-heading);">${field.toUpperCase()}</td>
                        <td style="padding: 0.75rem 1rem; background: rgba(254, 242, 242, 0.4);">${beforeCell}</td>
                        <td style="padding: 0.75rem 1rem; background: rgba(236, 253, 245, 0.4);">${afterCell}</td>
                    </tr>
                `;
      });

      tableHtml += `
                        </tbody>
                    </table>
                </div>
            `;
      return tableHtml;
    } else if (typeof parsed === 'object') {
      // Render the JSON payload object in the same 3-column table format!
      let tableHtml = html + `
                <div style="overflow-x: auto; border: 1px solid var(--border-color); border-radius: 12px; background: var(--bg-light); box-shadow: var(--shadow-sm);">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.8rem;">
                        <thead>
                            <tr style="border-bottom: 1.5px solid var(--border-color); background: rgba(0, 0, 0, 0.02);">
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: var(--text-dark); text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 25%;">FIELD</th>
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: var(--text-dark); text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 37.5%;">BEFORE</th>
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: var(--text-dark); text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 37.5%;">AFTER</th>
                            </tr>
                        </thead>
                        <tbody>
            `;

      const entries = Object.entries(parsed).filter(([key]) => key !== 'id' && key !== 'createdAt' && key !== 'updatedAt');
      entries.forEach(([key, val], idx) => {
        let formattedVal = val !== null ? val.toString() : '';
        if (formattedVal.length > 200) {
          formattedVal = formattedVal.substring(0, 197) + '...';
        }

        const beforeCell = `<span style="color: var(--text-muted); font-style: italic; font-size: 0.75rem;">(null)</span>`;
        const afterCell = formattedVal !== ''
          ? `<span style="display: inline-flex; align-items: center; gap: 4px; background: #d1fae5; color: #059669; border: 1px solid #a7f3d0; padding: 0.2rem 0.5rem; border-radius: 6px; font-family: monospace; font-weight: 600; font-size: 0.78rem;">
                        <svg viewBox="0 0 24 24" style="width:12px;height:12px;stroke:currentColor;fill:none;stroke-width:3;stroke-linecap:round;stroke-linejoin:round;"><polyline points="20 6 9 17 4 12"></polyline></svg>
                        ${formattedVal}
                       </span>`
          : `<span style="color: var(--text-muted); font-style: italic; font-size: 0.75rem;">(null)</span>`;

        const borderStyle = idx < entries.length - 1 ? 'border-bottom: 1px solid var(--border-color);' : '';

        tableHtml += `
                    <tr style="${borderStyle}">
                        <td style="padding: 0.75rem 1rem; font-weight: 700; color: #4f46e5; font-size: 0.75rem; font-family: var(--font-heading);">${key.toUpperCase()}</td>
                        <td style="padding: 0.75rem 1rem; background: rgba(254, 242, 242, 0.2);">${beforeCell}</td>
                        <td style="padding: 0.75rem 1rem; background: rgba(236, 253, 245, 0.4);">${afterCell}</td>
                    </tr>
                `;
      });

      tableHtml += `
                        </tbody>
                    </table>
                </div>
            `;
      return tableHtml;
    }
  } catch (e) {
    // Fallback for raw text
    html += `
            <div style="font-family: monospace; font-size: 0.8rem; max-height: 120px; overflow-y: auto; background: var(--bg-light); padding: 0.6rem 0.8rem; border-radius: 8px; border: 1px solid var(--border-color); word-break: break-all; white-space: pre-wrap; color: var(--text-dark);">
                ${jsonPart}
            </div>
        `;
    return html;
  }
}

function getAuthStatusBadge(action) {
  const upperAction = action ? action.toUpperCase() : '';
  if (upperAction === 'LOGIN' || upperAction === 'LOGOUT') {
    return `<span class="status-badge badge-active">Success</span>`;
  } else if (upperAction.includes('FAILED') || upperAction.includes('ERROR')) {
    return `<span style="background: #fef2f2; color: #ef4444; border: 1px solid #fecaca; padding: 0.2rem 0.6rem; border-radius: 50px; font-size: 0.72rem; font-weight: 700; text-transform: uppercase;">Failure</span>`;
  } else if (upperAction === 'CHANGE_PASSWORD') {
    return `<span class="status-badge status-pending">Change Password</span>`;
  }
  return `<span class="status-badge status-pending">${action}</span>`;
}

function getRoleBadge(role) {
  if (!role) return `<span class="status-badge" style="background:#f1f5f9;color:#64748b;border:1px solid #e2e8f0;font-weight:700;">USER</span>`;
  const r = role.toUpperCase().replace('ROLE_', '');
  if (r === 'ADMIN') {
    return `<span class="status-badge badge-admin" style="background:#f3f0ff;color:#7c3aed;border:1px solid #ddd6fe;font-weight:700;">ADMIN</span>`;
  }
  if (r === 'RESOURCE' || r === 'RESOURCE_MANAGER' || r === 'RESOURCE_MGR') {
    return `<span class="status-badge" style="background:#fff7ed;color:#ea580c;border:1px solid #ffedd5;font-weight:700;">RESOURCE MANAGER</span>`;
  }
  if (r === 'MEMBER' || r === 'TEAM_MEMBER') {
    return `<span class="status-badge badge-user" style="background:#eff6ff;color:#2563eb;border:1px solid #bfdbfe;font-weight:700;">TEAM MEMBER</span>`;
  }
  if (r === 'CLIENT') {
    return `<span class="status-badge" style="background:#f0fdf4;color:#16a34a;border:1px solid #bbf7d0;font-weight:700;">CLIENT</span>`;
  }
  return `<span class="status-badge" style="background:#f1f5f9;color:#475569;border:1px solid #cbd5e1;font-weight:700;">${r}</span>`;
}

function renderAuditPagination(containerId, totalPages, currentPage, callbackName) {
  const container = document.getElementById(containerId);
  if (!container) return;
  if (totalPages <= 1) { container.innerHTML = ''; return; }

  const btnStyle = `style="min-width:34px;height:34px;border-radius:8px;border:1px solid var(--border-color);background:#fff;color:var(--text-dark);font-size:0.82rem;font-weight:600;cursor:pointer;display:inline-flex;align-items:center;justify-content:center;transition:all 0.2s;"`;
  const activeBtnStyle = `style="min-width:34px;height:34px;border-radius:8px;border:1px solid #2563eb;background:#2563eb;color:#fff;font-size:0.82rem;font-weight:600;cursor:pointer;display:inline-flex;align-items:center;justify-content:center;"`;
  const disabledStyle = `style="min-width:34px;height:34px;border-radius:8px;border:1px solid #e2e8f0;background:#f8fafc;color:#cbd5e1;font-size:0.82rem;font-weight:600;display:inline-flex;align-items:center;justify-content:center;cursor:not-allowed;"`;

  let html = `<span style="font-size:0.82rem;color:var(--text-muted);margin-right:0.5rem;">Page ${currentPage + 1} / ${totalPages}</span>`;

  // Prev
  if (currentPage === 0) {
    html += `<span ${disabledStyle}>‹</span>`;
  } else {
    html += `<button ${btnStyle} onclick="${callbackName}(${currentPage - 1})">‹</button>`;
  }

  let start = Math.max(0, currentPage - 2);
  let end = Math.min(totalPages - 1, currentPage + 2);

  if (start > 0) {
    html += `<button ${btnStyle} onclick="${callbackName}(0)">1</button>`;
    if (start > 1) html += `<span style="color:var(--text-muted);padding:0 4px;">…</span>`;
  }
  for (let i = start; i <= end; i++) {
    html += `<button ${i === currentPage ? activeBtnStyle : btnStyle} onclick="${callbackName}(${i})">${i + 1}</button>`;
  }
  if (end < totalPages - 1) {
    if (end < totalPages - 2) html += `<span style="color:var(--text-muted);padding:0 4px;">…</span>`;
    html += `<button ${btnStyle} onclick="${callbackName}(${totalPages - 1})">${totalPages}</button>`;
  }

  // Next
  if (currentPage === totalPages - 1) {
    html += `<span ${disabledStyle}>›</span>`;
  } else {
    html += `<button ${btnStyle} onclick="${callbackName}(${currentPage + 1})">›</button>`;
  }

  container.innerHTML = html;
}

async function fetchAuditWithAuth(url) {
  const token = sessionStorage.getItem("token") || localStorage.getItem("token") || "";
  const res = await fetch(url, { headers: { 'Authorization': 'Bearer ' + token } });
  if (!res.ok) throw new Error('Data access error (' + res.status + ')');
  return res.json();
}

function formatJsonObjectToText(obj) {
  if (!obj || typeof obj !== 'object') return String(obj);
  const parts = [];
  Object.entries(obj).forEach(([k, v]) => {
    if (k !== 'id' && k !== 'password' && v !== null && v !== undefined && v !== '') {
      parts.push(`<span style="color:#4f46e5;font-weight:600;">${k}</span>: "${v}"`);
    }
  });
  return parts.slice(0, 6).join(', ');
}

function formatAuditDetailToHumanText(log) {
  let roleText = log.role ? log.role.replace('ROLE_', '').replace(/_/g, ' ') : 'User';
  const rUpper = roleText.toUpperCase().trim();
  if (rUpper === 'RESOURCE' || rUpper === 'RESOURCE MANAGER' || rUpper === 'RESOURCE MGR') {
    roleText = 'Resource Manager';
  } else if (rUpper === 'ADMIN') {
    roleText = 'Admin';
  } else if (rUpper === 'MEMBER' || rUpper === 'TEAM MEMBER') {
    roleText = 'Team Member';
  } else if (rUpper === 'CLIENT') {
    roleText = 'Client';
  }

  const userText = log.username ? log.username : 'User';
  const actionLower = (log.action || 'UPDATE').toLowerCase();
  let tableText = (log.tableName || 'record').replace(/_/g, ' ');
  if (tableText.endsWith('s') && tableText !== 'status' && tableText !== 'contacts') {
    tableText = tableText.substring(0, tableText.length - 1);
  }

  let verb = 'updated';
  if (actionLower.includes('create')) verb = 'created';
  if (actionLower.includes('delete')) verb = 'deleted';

  let detailStr = log.detail || '';

  // Extract JSON part if detailStr has a text prefix (e.g. "ROLE_RESOURCE resource_manager updated...")
  let jsonPart = detailStr;
  const jsonMatch = detailStr.match(/^[^{[]*?({|\[)/);
  if (jsonMatch && jsonMatch.index !== undefined) {
    jsonPart = detailStr.substring(jsonMatch.index + jsonMatch[0].length - 1);
  }

  // Try parsing as JSON array (diff array) or JSON object to extract summary values
  try {
    const clean = jsonPart.replace(/^\[FAILED\]\s*/, '').replace(/^[^{[]*?({|\[)/, '$1');
    const parsed = JSON.parse(clean);

    if (Array.isArray(parsed)) {
      let projectTitle = '';
      let memberName = '';
      let percentage = '';

      parsed.forEach(diff => {
        if (diff.field === 'projectTitle' && diff.new) projectTitle = diff.new;
        if ((diff.field === 'fullName' || diff.field === 'username') && diff.new) memberName = diff.new;
        if (diff.field === 'allocationPercentage' && diff.new) percentage = diff.new + '%';
      });

      if (log.tableName === 'resource_allocations' || tableText.includes('resource allocation')) {
        let msg = `<span style="font-weight:700;color:var(--text-dark);">${roleText}</span> (${userText}) ${verb} resource allocation`;
        if (projectTitle) msg += ` for project <strong style="color:#2563eb;">"${projectTitle}"</strong>`;
        if (memberName) msg += ` assigned to <strong style="color:#059669;">"${memberName}"</strong>`;
        if (percentage) msg += ` (${percentage})`;
        return msg;
      }

      if (log.tableName === 'project_milestones' || tableText.includes('milestone')) {
        let milestoneName = '';
        parsed.forEach(diff => {
          if ((diff.field === 'name' || diff.field === 'title' || diff.field === 'milestoneName') && diff.new) {
            milestoneName = diff.new;
          }
        });
        let msg = `<span style="font-weight:700;color:var(--text-dark);">${roleText}</span> (${userText}) ${verb} milestone`;
        if (milestoneName) msg += ` <strong style="color:#2563eb;">"${milestoneName}"</strong>`;
        return msg;
      }

      return `<span style="font-weight:700;color:var(--text-dark);">${roleText}</span> (${userText}) ${verb} <strong style="color:#2563eb;">${tableText}</strong>`;
    }

    if (typeof parsed === 'object' && parsed !== null) {
      let nameVal = parsed.name || parsed.title || parsed.projectTitle || parsed.fullName || '';
      let msg = `<span style="font-weight:700;color:var(--text-dark);">${roleText}</span> (${userText}) ${verb} <strong style="color:#2563eb;">${tableText}</strong>`;
      if (nameVal) msg += ` <strong style="color:#059669;">"${nameVal}"</strong>`;
      return msg;
    }
  } catch (e) {
    // String format fallback
  }

  return `<span style="font-weight:700;color:var(--text-dark);">${roleText}</span> (${userText}) ${verb} <strong style="color:#2563eb;">${tableText}</strong>`;
}

// Tab 1: Data Changes Logs (Flat list)
async function loadDataUsers(page = 0) {
  auditDataPage = page;
  const tbody = document.getElementById('dataTableBody');
  tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:2rem;color:var(--text-muted);">Loading...</td></tr>`;

  try {
    const data = await fetchAuditWithAuth(`/api/audit/data?page=${page}&size=${AUDIT_PAGE_SIZE}`);

    if (!data.content || data.content.length === 0) {
      tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:2rem;color:var(--text-muted);">No data changes found.</td></tr>`;
      document.getElementById('dataPagination').innerHTML = '';
      return;
    }

    tbody.innerHTML = '';
    data.content.forEach(log => {
      const tr = document.createElement('tr');
      const formattedDetail = formatAuditDetailToHumanText(log);
      const logJsonString = encodeURIComponent(JSON.stringify(log));

      tr.innerHTML = `
                <td style="padding: 0.9rem 1.2rem;">
                    <div style="color:var(--text-dark);font-size:0.88rem;line-height:1.5;">
                        ${formattedDetail}
                    </div>
                    <div style="font-size:0.75rem;color:var(--text-muted);margin-top:0.4rem;display:flex;align-items:center;justify-content:space-between;gap:0.4rem;">
                        <span style="display:inline-flex;align-items:center;gap:0.3rem;">
                            <svg viewBox="0 0 24 24" style="width:13px;height:13px;stroke:currentColor;fill:none;stroke-width:2;"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                            ${formatAuditDate(log.createdAt)}
                        </span>
                        <button onclick="openDetailModal('${logJsonString}')" 
                            style="background:#eff6ff;border:1px solid #bfdbfe;color:#2563eb;padding:3px 10px;border-radius:6px;font-size:0.75rem;font-weight:600;cursor:pointer;display:inline-flex;align-items:center;gap:4px;transition:all 0.2s;"
                            onmouseover="this.style.background='#dbeafe'" onmouseout="this.style.background='#eff6ff'">
                            <svg viewBox="0 0 24 24" style="width:13px;height:13px;stroke:currentColor;fill:none;stroke-width:2;stroke-linecap:round;stroke-linejoin:round;"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                            View details
                        </button>
                    </div>
                </td>
                <td style="color:var(--text-dark);font-weight:600;font-size:0.88rem;white-space:nowrap;">${log.username || '-'}</td>
                <td style="white-space:nowrap;">${getRoleBadge(log.role)}</td>
            `;
      tbody.appendChild(tr);
    });

    renderAuditPagination('dataPagination', data.totalPages, auditDataPage, 'loadDataUsers');
  } catch (error) {
    tbody.innerHTML = `<tr><td colspan="3" style="color:#ef4444;text-align:center;">${error.message}</td></tr>`;
  }
}

// Tab 2: Auth Logs (paginated) - fixed from old data.forEach bug
async function loadAuthLogs(page = 0) {
  auditAuthPage = page;
  const tbody = document.getElementById('authTableBody');
  tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">Loading...</td></tr>`;

  try {
    const data = await fetchAuditWithAuth(`/api/audit/auth?page=${page}&size=${AUDIT_PAGE_SIZE}`);

    if (!data.content || data.content.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:2rem;color:var(--text-muted);">No access history data.</td></tr>`;
      document.getElementById('authPagination').innerHTML = '';
      return;
    }

    tbody.innerHTML = '';
    data.content.forEach(log => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
                <td style="white-space:nowrap;color:var(--text-muted);font-size:0.8rem;">${formatAuditDate(log.createdAt)}</td>
                <td style="font-weight:600;color:var(--text-dark);">${log.username}</td>
                <td>
                    <div style="display:flex;flex-direction:column;gap:0.3rem;align-items:flex-start;">
                        <span style="font-size:0.75rem;color:var(--text-muted);font-weight:700;">${log.action}</span>
                        ${getAuthStatusBadge(log.action)}
                    </div>
                </td>
                <td style="font-family:monospace;font-size:0.85rem;">${log.ipAddress}</td>
                <td>
                    <div style="max-width:250px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;font-size:0.8rem;" title="${log.userAgent}">
                        ${log.userAgent}
                    </div>
                </td>
            `;
      tbody.appendChild(tr);
    });

    renderAuditPagination('authPagination', data.totalPages, auditAuthPage, 'loadAuthLogs');
  } catch (error) {
    tbody.innerHTML = `<tr><td colspan="5" style="color:#ef4444;text-align:center;">${error.message}</td></tr>`;
  }
}

function switchAuditTab(tabId) {
  document.getElementById('dataTab').style.display = tabId === 'dataTab' ? 'block' : 'none';
  document.getElementById('authTab').style.display = tabId === 'authTab' ? 'block' : 'none';

  const btnData = document.getElementById('btn-tab-data');
  const btnAuth = document.getElementById('btn-tab-auth');

  if (tabId === 'dataTab') {
    btnData.style.background = 'linear-gradient(135deg, rgba(37,99,235,0.1), rgba(79,70,229,0.1))';
    btnData.style.border = '1px solid rgba(37,99,235,0.2)';
    btnData.style.color = '#2563eb';

    btnAuth.style.background = 'transparent';
    btnAuth.style.border = '1px solid transparent';
    btnAuth.style.color = 'var(--text-muted)';
    loadDataUsers(0);
  } else {
    btnAuth.style.background = 'linear-gradient(135deg, rgba(37,99,235,0.1), rgba(79,70,229,0.1))';
    btnAuth.style.border = '1px solid rgba(37,99,235,0.2)';
    btnAuth.style.color = '#2563eb';

    btnData.style.background = 'transparent';
    btnData.style.border = '1px solid transparent';
    btnData.style.color = 'var(--text-muted)';
    loadAuthLogs(0);
  }
}

window.loadDataUsers = loadDataUsers;
window.loadAuthLogs = loadAuthLogs;
window.switchAuditTab = switchAuditTab;
window.openDetailModal = openDetailModal;

// Modal: Open and load user-specific data audit logs
function openAuditModal(username) {
  auditModalUsername = username;
  document.getElementById('modalUsernameLabel').innerText = username;
  const overlay = document.getElementById('auditModal');
  overlay.classList.add('is-open');
  loadUserAuditLogs(0);
}

function closeModal() {
  document.getElementById('auditModal').classList.remove('is-open');
}

// Close modal on outside click
document.addEventListener('DOMContentLoaded', () => {
  const overlay = document.getElementById('auditModal');
  if (overlay) {
    overlay.addEventListener('click', function (e) {
      if (e.target === this) closeModal();
    });
  }
  const detailOverlay = document.getElementById('auditDetailModal');
  if (detailOverlay) {
    detailOverlay.addEventListener('click', function (e) {
      if (e.target === this) closeDetailModal();
    });
  }
  const projectDetailOverlay = document.getElementById('project-detail-modal-overlay');
  if (projectDetailOverlay) {
    projectDetailOverlay.addEventListener('click', function (e) {
      if (e.target === this) closeProjectDetailModal();
    });
  }
  const toggle = document.getElementById('detail-only-changes-toggle');
  if (toggle) {
    toggle.addEventListener('change', function () {
      renderComparisonTable();
    });
  }
});

async function loadUserAuditLogs(page = 0) {
  auditModalPage = page;
  const container = document.getElementById('modalLogContainer');
  container.innerHTML = `<div style="text-align:center;padding:2rem;color:var(--text-muted);">Loading data...</div>`;

  try {
    const data = await fetchAuditWithAuth(`/api/audit/data/user/${auditModalUsername}?page=${page}&size=${AUDIT_PAGE_SIZE}`);

    if (!data.content || data.content.length === 0) {
      container.innerHTML = `<div style="text-align:center;padding:2rem;color:var(--text-muted);">This user has no data change activities yet.</div>`;
      document.getElementById('modalPagination').innerHTML = '';
      return;
    }

    container.innerHTML = '';
    data.content.forEach(log => {
      const entry = document.createElement('div');
      entry.className = 'log-entry';
      entry.style.display = 'flex';
      entry.style.alignItems = 'center';
      entry.style.padding = '0.75rem 0';
      entry.style.borderBottom = '1px solid #e5e7eb';

      const dt = log.createdAt ? new Date(log.createdAt) : null;
      const timeStr = dt ? dt.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' }) : '';
      const dateStr = dt ? dt.toLocaleDateString('en-US', { day: '2-digit', month: '2-digit', year: 'numeric' }) : '';

      let summaryText = 'Data changed';
      try {
        const clean = (log.detail || '').replace(/^\[FAILED\]\s*/, '').replace(/^[^{[]*?({|\[)/, '$1');
        const parsed = JSON.parse(clean);
        if (Array.isArray(parsed)) {
          const fields = parsed.map(d => d.field).join(', ');
          summaryText = `Updated fields: <strong style="color:#2563eb;">${fields}</strong>`;
        } else if (typeof parsed === 'object') {
          const fields = Object.keys(parsed).filter(k => k !== 'id' && k !== 'createdAt' && k !== 'updatedAt').join(', ');
          summaryText = `Created with fields: <strong style="color:#059669;">${fields}</strong>`;
        }
      } catch (e) {
        summaryText = log.detail || 'Data changed';
      }

      const logJsonString = encodeURIComponent(JSON.stringify(log));

      entry.innerHTML = `
          <div class="log-time" style="width: 140px; font-weight:600; flex-shrink: 0; font-family: system-ui, -apple-system, sans-serif; font-size:0.82rem;">
              ${timeStr}
              <span class="log-date" style="display:block; font-size:0.72rem; color:#6b7280; font-weight: 500;">${dateStr}</span>
          </div>
          <div class="log-action-col" style="width: 110px; flex-shrink: 0;">${getDataActionBadge(log.action)}</div>
          <div class="log-table-col" style="width: 130px; flex-shrink: 0;">
              <code style="background:#f3f4f6; color:#374151; padding:2px 6px; border-radius:4px; font-size:0.8rem; font-family:monospace;">${log.tableName}</code>
          </div>
          <div class="log-detail-col" style="flex:1; display:flex; justify-content:space-between; align-items:center; gap: 1rem; font-family: system-ui, -apple-system, sans-serif;">
              <span style="font-size:0.85rem; color:#374151;">${summaryText}</span>
              <button onclick="openDetailModal('${logJsonString}')" style="background:#eff6ff; border:1px solid #bfdbfe; color:#2563eb; padding:4px 10px; border-radius:6px; font-size:0.78rem; font-weight:600; cursor:pointer; display:inline-flex; align-items:center; gap:4px; transition:all 0.2s;">
                  View details
              </button>
          </div>
      `;
      container.appendChild(entry);
    });

    renderAuditPagination('modalPagination', data.totalPages, auditModalPage, 'loadUserAuditLogs');
  } catch (error) {
    container.innerHTML = `<div style="color:#ef4444;text-align:center;padding:1rem;">${error.message}</div>`;
  }
}

// Detailed Comparison Modal Logic
let activeDetailLog = null;

function openDetailModal(logJsonString) {
  const log = JSON.parse(decodeURIComponent(logJsonString));
  activeDetailLog = log;

  // Set titles & metadata
  const userTitleEl = document.getElementById('detail-user-title');
  if (userTitleEl) userTitleEl.innerText = log.username || 'N/A';
  document.getElementById('detail-table-title').innerText = log.tableName || 'N/A';

  // Set action badge color & text
  const badge = document.getElementById('detail-action-badge');
  badge.innerText = (log.action || '').toUpperCase();
  if (log.action && log.action.toUpperCase().includes('UPDATE')) {
    badge.style.background = '#eff6ff';
    badge.style.color = '#2563eb';
    badge.style.border = '1px solid #bfdbfe';
  } else if (log.action && log.action.toUpperCase().includes('CREATE')) {
    badge.style.background = '#ecfdf5';
    badge.style.color = '#059669';
    badge.style.border = '1px solid #a7f3d0';
  } else if (log.action && log.action.toUpperCase().includes('DELETE')) {
    badge.style.background = '#fef2f2';
    badge.style.color = '#dc2626';
    badge.style.border = '1px solid #fca5a5';
  } else {
    badge.style.background = '#f3f4f6';
    badge.style.color = '#374151';
    badge.style.border = '1px solid #d1d5db';
  }

  // Set avatar initial
  document.getElementById('detail-avatar').innerText = (log.username || '?').charAt(0).toUpperCase();
  document.getElementById('detail-performed-by').innerText = log.username || 'N/A';

  // Format date and time
  const dt = log.createdAt ? new Date(log.createdAt) : null;
  if (dt) {
    document.getElementById('detail-date').innerText = dt.toLocaleDateString('en-US', { day: '2-digit', month: '2-digit', year: 'numeric' });
    document.getElementById('detail-time').innerText = dt.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }) + ' (Hanoi, Vietnam time)';
  } else {
    document.getElementById('detail-date').innerText = '-';
    document.getElementById('detail-time').innerText = '-';
  }

  // Footer
  document.getElementById('detail-source-ip').innerText = log.ipAddress || 'Unknown';
  // Simple hashCode to generate consistent mock session
  let hash = 0;
  const uaStr = log.userAgent || '';
  for (let i = 0; i < uaStr.length; i++) {
    hash = uaStr.charCodeAt(i) + ((hash << 5) - hash);
  }
  document.getElementById('detail-session-id').innerText = log.userAgent ? ('SESS-' + Math.abs(hash).toString(16).toUpperCase().substring(0, 8)) : 'N/A';

  // Render table
  renderComparisonTable();

  // Open modal
  document.getElementById('auditDetailModal').classList.add('is-open');
}

function closeDetailModal() {
  document.getElementById('auditDetailModal').classList.remove('is-open');
}

function renderComparisonTable() {
  const container = document.getElementById('detail-comparison-table-container');
  if (!activeDetailLog || !activeDetailLog.detail) {
    container.innerHTML = '<span style="color:#6b7280; font-style:italic;">No data</span>';
    return;
  }

  const showOnlyChanges = document.getElementById('detail-only-changes-toggle').checked;
  let detailStr = activeDetailLog.detail;

  // Check if FAILED
  let isFailed = false;
  let errorMessage = '';
  let jsonPart = detailStr;
  if (detailStr.startsWith('[FAILED]')) {
    isFailed = true;
    const separatorIdx = detailStr.indexOf(' | ');
    if (separatorIdx !== -1) {
      errorMessage = detailStr.substring(8, separatorIdx);
      jsonPart = detailStr.substring(separatorIdx + 3);
    } else {
      errorMessage = detailStr.substring(8);
      jsonPart = '';
    }
  }

  let html = '';
  if (isFailed) {
    html += `
            <div style="background: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 0.6rem 0.8rem; margin-bottom: 0.6rem; color: #dc2626; font-size: 0.8rem; font-family: system-ui, -apple-system, sans-serif;">
                <div style="font-weight: 700; display: flex; align-items: center; gap: 6px; margin-bottom: 2px;">
                    Operation failed
                </div>
                <div style="font-family: monospace; font-size: 0.75rem; word-break: break-all;">${errorMessage}</div>
            </div>
        `;
  }

  if (!jsonPart || jsonPart === '(no payload)') {
    container.innerHTML = html + '<span style="color:#6b7280; font-style:italic;">No detailed content</span>';
    return;
  }

  try {
    const clean = jsonPart.replace(/^\[FAILED\]\s*/, '').replace(/^[^{[]*?({|\[)/, '$1');
    const parsed = JSON.parse(clean);

    if (Array.isArray(parsed)) {
      let tableHtml = html + `
                <div style="overflow-x: auto; border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); font-family: system-ui, -apple-system, sans-serif;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.82rem;">
                        <thead>
                            <tr style="border-bottom: 1.5px solid #e5e7eb; background: #f9fafb;">
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: #374151; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 25%;">FIELD</th>
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: #b91c1c; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 37.5%; background: #fef2f2;">BEFORE</th>
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: #047857; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 37.5%; background: #ecfdf5;">AFTER</th>
                            </tr>
                        </thead>
                        <tbody>
            `;

      let rowIndex = 0;
      let renderedCount = 0;

      parsed.forEach((diff) => {
        const field = diff.field || 'unknown';
        const oldVal = diff.old !== null ? String(diff.old) : '';
        const newVal = diff.new !== null ? String(diff.new) : '';
        const isChanged = diff.changed !== false;

        if (showOnlyChanges && !isChanged) {
          return; // Hide unchanged fields
        }

        renderedCount++;
        rowIndex++;

        let beforeContent = '';
        let afterContent = '';
        let cellBgBefore = '#fff';
        let cellBgAfter = '#fff';

        if (!isChanged) {
          // Unchanged row
          cellBgBefore = '#f9fafb';
          cellBgAfter = '#f9fafb';
          beforeContent = `<span style="background: #e5e7eb; color: #4b5563; padding: 0.2rem 0.6rem; border-radius: 6px; font-size: 0.78rem; font-weight: 500;">${newVal || '(empty)'} (No change)</span>`;
          afterContent = `<span style="background: #e5e7eb; color: #4b5563; padding: 0.2rem 0.6rem; border-radius: 6px; font-size: 0.78rem; font-weight: 500;">${newVal || '(empty)'} (No change)</span>`;
        } else {
          // Changed row
          cellBgBefore = '#fef2f2';
          cellBgAfter = '#ecfdf5';

          beforeContent = oldVal !== ''
            ? `<span style="color: #dc2626; text-decoration: line-through; font-family: monospace; font-size: 0.82rem; display: inline-flex; align-items: center; gap: 4px;">
                            ~${oldVal}~
                           </span>`
            : `<span style="color: #9ca3af; font-style: italic; font-size: 0.78rem;">(null)</span>`;

          afterContent = newVal !== ''
            ? `<span style="color: #059669; font-weight: 600; font-family: monospace; font-size: 0.82rem; display: inline-flex; align-items: center; gap: 4px;">
                            ✓ ${newVal}
                           </span>`
            : `<span style="color: #dc2626; background: #fee2e2; border: 1px solid #fca5a5; padding: 0.2rem 0.5rem; border-radius: 6px; font-size: 0.75rem; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
                            Deleted
                           </span>`;
        }

        tableHtml += `
                    <tr style="border-bottom: 1px solid #e5e7eb;">
                        <td style="padding: 0.75rem 1rem; font-weight: 700; color: #4b5563; font-size: 0.78rem; font-family: system-ui, -apple-system, sans-serif;">
                            <span style="color: #9ca3af; margin-right: 8px; font-weight: 400; font-family: monospace;">${rowIndex}</span>
                            ${field.toUpperCase()}
                        </td>
                        <td style="padding: 0.75rem 1rem; background: ${cellBgBefore};">${beforeContent}</td>
                        <td style="padding: 0.75rem 1rem; background: ${cellBgAfter};">${afterContent}</td>
                    </tr>
                `;
      });

      if (renderedCount === 0) {
        tableHtml += `
                    <tr>
                        <td colspan="3" style="text-align: center; color: #9ca3af; padding: 2rem; font-style: italic;">
                            No changes were recorded.
                        </td>
                    </tr>
                `;
      }

      tableHtml += `
                        </tbody>
                    </table>
                </div>
            `;
      container.innerHTML = tableHtml;
    } else if (typeof parsed === 'object') {
      // JSON Object representing a CREATE operation
      let tableHtml = html + `
                <div style="overflow-x: auto; border: 1px solid #e5e7eb; border-radius: 12px; background: #fff; box-shadow: 0 1px 3px rgba(0,0,0,0.05); font-family: system-ui, -apple-system, sans-serif;">
                    <table style="width: 100%; border-collapse: collapse; text-align: left; font-size: 0.82rem;">
                        <thead>
                            <tr style="border-bottom: 1.5px solid #e5e7eb; background: #f9fafb;">
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: #374151; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 25%;">FIELD</th>
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: #b91c1c; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 37.5%; background: #fef2f2;">BEFORE</th>
                                <th style="padding: 0.75rem 1rem; font-weight: 800; color: #047857; text-transform: uppercase; font-size: 0.75rem; letter-spacing: 0.05em; width: 37.5%; background: #ecfdf5;">AFTER</th>
                            </tr>
                        </thead>
                        <tbody>
            `;

      let rowIndex = 0;
      const entries = Object.entries(parsed).filter(([key]) => key !== 'id' && key !== 'createdAt' && key !== 'updatedAt');

      entries.forEach(([key, val]) => {
        rowIndex++;
        let formattedVal = val !== null ? val.toString() : '';
        if (formattedVal.length > 200) {
          formattedVal = formattedVal.substring(0, 197) + '...';
        }

        const beforeCell = `<span style="color: #9ca3af; font-style: italic; font-size: 0.78rem;">(null)</span>`;
        const afterCell = formattedVal !== ''
          ? `<span style="color: #059669; font-weight: 600; font-family: monospace; font-size: 0.82rem; display: inline-flex; align-items: center; gap: 4px;">
                        ✓ ${formattedVal}
                       </span>`
          : `<span style="color: #9ca3af; font-style: italic; font-size: 0.78rem;">(null)</span>`;

        tableHtml += `
                    <tr style="border-bottom: 1px solid #e5e7eb;">
                        <td style="padding: 0.75rem 1rem; font-weight: 700; color: #4b5563; font-size: 0.78rem; font-family: system-ui, -apple-system, sans-serif;">
                            <span style="color: #9ca3af; margin-right: 8px; font-weight: 400; font-family: monospace;">${rowIndex}</span>
                            ${key.toUpperCase()}
                        </td>
                        <td style="padding: 0.75rem 1rem; background: #fef2f2;">${beforeCell}</td>
                        <td style="padding: 0.75rem 1rem; background: #ecfdf5;">${afterCell}</td>
                    </tr>
                `;
      });

      tableHtml += `
                        </tbody>
                    </table>
                </div>
            `;
      container.innerHTML = tableHtml;
    }
  } catch (e) {
    // Fallback to text box
    container.innerHTML = html + `
            <div style="font-family: monospace; font-size: 0.82rem; background: #f9fafb; padding: 1rem; border-radius: 8px; border: 1px solid #e5e7eb; word-break: break-all; white-space: pre-wrap; color: #374151;">
                ${jsonPart}
            </div>
        `;
  }
}

// =========================================================================
// PROJECT MILESTONE ADMIN PANEL LOGIC (UC-12)
// =========================================================================

let currentDetailProjectId = null;

async function openProjectDetailModal(projectId) {
  currentDetailProjectId = projectId;
  const project = _cache.projects[projectId];
  const overlay = document.getElementById("project-detail-modal-overlay");
  if (!overlay) return;

  // Set basic project info
  document.getElementById("project-detail-modal-title").textContent = project ? project.title : "Project Details";
  document.getElementById("project-detail-category-badge").textContent = project ? project.category : "Project";
  document.getElementById("project-detail-description").textContent = project && project.description ? project.description : "No description provided.";
  document.getElementById("project-detail-technologies").textContent = project && project.technologies ? project.technologies : "N/A";

  // Set Client Info
  const clientInfoEl = document.getElementById("project-detail-client-info");
  if (clientInfoEl) {
    const depositAmt = project && project.depositAmount ? project.depositAmount : 0;
    const depositPaid = project && project.depositPaid ? "Paid" : "Unpaid";
    const depositPaidClass = project && project.depositPaid ? "status-COMPLETED" : "status-PENDING";

    if (project && project.clientName) {
      clientInfoEl.innerHTML = `
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <div>
              <div style="font-weight:700; color:var(--text-dark); font-size:0.95rem;">${escapeHtml(project.clientName)}</div>
              <div style="font-size:0.83rem; color:var(--color-primary, #2563eb); font-weight:500;">${escapeHtml(project.clientEmail || "")}</div>
            </div>
            <span class="status-badge badge-user" style="font-size:0.75rem;">Hired Client</span>
          </div>
          <div style="margin-top: 1rem; padding-top: 0.75rem; border-top: 1px dashed var(--border-color); display: flex; justify-content: space-between; align-items: center;">
            <div style="font-size: 0.85rem; color: var(--text-muted);">
              <strong>Deposit Amount:</strong> $${depositAmt.toLocaleString()}
            </div>
            <span class="status-badge ${depositPaidClass}" style="font-size: 0.75rem;">${depositPaid}</span>
          </div>
        `;
    } else {
      clientInfoEl.innerHTML = `
          <span style="color:var(--text-muted); font-style:italic;">No client currently linked to this project.</span>
          <div style="margin-top: 1rem; padding-top: 0.75rem; border-top: 1px dashed var(--border-color); display: flex; justify-content: space-between; align-items: center;">
            <div style="font-size: 0.85rem; color: var(--text-muted);">
              <strong>Deposit Amount:</strong> $${depositAmt.toLocaleString()}
            </div>
            <span class="status-badge ${depositPaidClass}" style="font-size: 0.75rem;">${depositPaid}</span>
          </div>
        `;
    }
  }

  // Render Milestones
  const milestoneContainer = document.getElementById("project-detail-milestones-list");
  if (milestoneContainer) {
    milestoneContainer.innerHTML = `<p style="color:var(--text-muted); font-size:0.85rem;">Loading milestones...</p>`;
    try {
      const res = await fetch(`/api/projects/${projectId}/milestones`);
      if (res.ok) {
        const milestones = await res.json();
        if (milestones && milestones.length > 0) {
          milestoneContainer.innerHTML = milestones.map(m => {
            const statusClass = (m.status || "").toLowerCase();
            const isCompleted = m.status === 'COMPLETED';
            return `
                <div style="border:1px solid var(--border-color); border-radius:8px; padding:0.85rem; background:var(--bg-light, #f8fafc);">
                  <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.4rem;">
                    <span style="font-weight:700; font-size:0.9rem; color:var(--text-dark);">${escapeHtml(m.name)}</span>
                    <span class="status-badge ${statusClass}">${escapeHtml(m.status)}</span>
                  </div>
                  ${m.description ? `<p style="margin:0 0 0.5rem 0; font-size:0.83rem; color:var(--text-muted);">${escapeHtml(m.description)}</p>` : ''}
                  <div style="display:flex; align-items:center; gap:0.75rem;">
                    <div style="flex:1; height:6px; background:#cbd5e1; border-radius:9999px; overflow:hidden;">
                      <div style="height:100%; width:${m.progressPercentage}%; background:${isCompleted ? '#10b981' : '#2563eb'}; border-radius:9999px;"></div>
                    </div>
                    <span style="font-size:0.78rem; font-weight:700; color:var(--text-dark);">${m.progressPercentage}%</span>
                  </div>
                  ${m.dueDate ? `<div style="font-size:0.75rem; color:var(--text-muted); margin-top:0.4rem;">Due: ${escapeHtml(m.dueDate)}</div>` : ''}
                </div>
              `;
          }).join('');
        } else {
          milestoneContainer.innerHTML = `<p style="color:var(--text-muted); font-size:0.85rem;">No milestones defined for this project.</p>`;
        }
      } else {
        milestoneContainer.innerHTML = `<p style="color:var(--text-muted); font-size:0.85rem;">No milestones defined for this project.</p>`;
      }
    } catch (err) {
      milestoneContainer.innerHTML = `<p style="color:#ef4444; font-size:0.85rem;">Could not load milestones.</p>`;
    }
  }

  overlay.classList.add("is-open");
  document.body.style.overflow = "hidden";
}

function closeProjectDetailModal() {
  const overlay = document.getElementById("project-detail-modal-overlay");
  if (overlay) overlay.classList.remove("is-open");
  document.body.style.overflow = "";
  currentDetailProjectId = null;
}

function openMilestoneManagementFromDetail() {
  if (currentDetailProjectId) {
    const pid = currentDetailProjectId;
    closeProjectDetailModal();
    openMilestoneModal(pid);
  }
}

window.openProjectDetailModal = openProjectDetailModal;
window.closeProjectDetailModal = closeProjectDetailModal;
window.openMilestoneManagementFromDetail = openMilestoneManagementFromDetail;

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
                    ${m.status === 'COMPLETED' ? 'background:rgba(16,185,129,0.12);color:#10b981' : m.status === 'IN_PROGRESS' ? 'background:rgba(59,130,246,0.12);color:#3b82f6' : m.status === 'BLOCKED' ? 'background:rgba(239,68,68,0.12);color:#ef4444' : 'background:rgba(100,116,139,0.12);color:#64748b'}">
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
  showConfirmModal({
    title: "Delete Milestone",
    message: `Are you sure you want to delete milestone '${milestoneName}'?`,
    confirmText: "Delete",
    cancelText: "Cancel",
    onConfirm: async () => {
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
          showToast(data.message || "Failed to delete milestone.", "error");
          return;
        }

        showToast("Milestone deleted successfully!", "success");
        fetchAndRenderAdminMilestones(currentAdminProjectId);
        closeAuditTrail();
      } catch (err) {
        console.error("deleteAdminMilestone error:", err);
        showToast("An error occurred while deleting milestone.", "error");
      }
    }
  });
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
      document.body.classList.add("header-is-detached");
    } else {
      header.classList.remove("header-detached");
      document.body.classList.remove("header-is-detached");
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

// =============================================
//  Google Sign-In Logic (TikTok-style Popup Flow)
// =============================================
function initGoogleSignIn() {
  const modalGoogleBtn = document.getElementById('modalGoogleSignInBtn');
  if (modalGoogleBtn) {
    modalGoogleBtn.addEventListener('click', (e) => {
      e.preventDefault();
      loginWithGooglePopup();
    });
  }

  const loginGoogleBtn = document.getElementById('googleSignInBtn');
  if (loginGoogleBtn) {
    loginGoogleBtn.addEventListener('click', (e) => {
      e.preventDefault();
      loginWithGooglePopup();
    });
  }
}

function loadGoogleSDKAndLogin() {
  return new Promise((resolve, reject) => {
    // Nếu SDK đã sẵn sàng → resolve ngay
    if (typeof google !== 'undefined' && google.accounts && google.accounts.oauth2) {
      resolve();
      return;
    }
    // Nếu script chưa được inject → inject mới
    const GSI_SRC = 'https://accounts.google.com/gsi/client';
    let script = document.querySelector(`script[src="${GSI_SRC}"]`);
    if (!script) {
      script = document.createElement('script');
      script.src = GSI_SRC;
      script.async = true;
      script.defer = true;
      document.head.appendChild(script);
    }
    // Polling tối đa 10 giây
    let attempts = 0;
    const maxAttempts = 100; // 100 × 100ms = 10s
    const interval = setInterval(() => {
      attempts++;
      if (typeof google !== 'undefined' && google.accounts && google.accounts.oauth2) {
        clearInterval(interval);
        resolve();
      } else if (attempts >= maxAttempts) {
        clearInterval(interval);
        reject(new Error('Google Sign-In SDK failed to load.'));
      }
    }, 100);
  });
}

function loginWithGooglePopup() {
  const clientId = "675937212349-d7ihb1c7a0u53no9d71cdt0jcmjrbil9.apps.googleusercontent.com";
  const modalAlert = document.getElementById("modal-login-alert") || document.getElementById("alertMessage");

  // Hiển thị trạng thái đang tải SDK nếu cần
  if (typeof google === 'undefined' || !google.accounts || !google.accounts.oauth2) {
    if (modalAlert) {
      modalAlert.textContent = "Loading Google Sign-In...";
      modalAlert.classList.remove("alert-error", "alert-success");
      modalAlert.style.display = "block";
    }
  }

  loadGoogleSDKAndLogin()
    .then(() => {
      _doGooglePopup(clientId, modalAlert);
    })
    .catch((err) => {
      console.error(err);
      if (modalAlert) {
        modalAlert.textContent = "Could not load Google Sign-In. Please check your internet connection and try again.";
        modalAlert.classList.add("alert-error");
        modalAlert.style.display = "block";
      } else {
        showToast("Google Sign-In SDK not loaded.", "error");
      }
    });
}

function _doGooglePopup(clientId, modalAlert) {

  if (modalAlert) {
    modalAlert.textContent = "Waiting for Google login...";
    modalAlert.classList.remove("alert-error", "alert-success");
    modalAlert.style.display = "block";
  }

  // Helper: ẩn thông báo "Waiting..." khi user hủy
  function clearWaitingMessage() {
    if (modalAlert && modalAlert.textContent === "Waiting for Google login...") {
      modalAlert.style.display = "none";
      modalAlert.textContent = "";
    }
  }

  try {
    const client = google.accounts.oauth2.initTokenClient({
      client_id: clientId,
      scope: 'email profile',
      // Gọi khi user chọn tài khoản thành công hoặc có lỗi từ Google
      callback: (response) => {
        if (response && response.access_token) {
          processGoogleToken(response.access_token, modalAlert);
        } else {
          // Có response nhưng không có access_token → lỗi khác
          if (modalAlert) {
            modalAlert.textContent = "Google login failed. Please try again.";
            modalAlert.classList.add("alert-error");
            modalAlert.style.display = "block";
          }
        }
      },
      // Gọi khi user đóng popup (bấm X) hoặc có lỗi OAuth
      error_callback: (err) => {
        if (err && err.type === 'popup_closed') {
          // User tự đóng popup → ẩn thông báo, không hiện lỗi
          clearWaitingMessage();
        } else if (err && err.type === 'popup_failed_to_open') {
          if (modalAlert) {
            modalAlert.textContent = "Could not open Google login. Please allow popups for this site.";
            modalAlert.classList.add("alert-error");
            modalAlert.style.display = "block";
          }
        } else {
          // Lỗi khác → ẩn thông báo waiting
          clearWaitingMessage();
          console.warn("Google OAuth error:", err);
        }
      }
    });
    client.requestAccessToken();
  } catch (error) {
    console.error("Google initTokenClient error:", error);
    clearWaitingMessage();
    if (modalAlert) {
      modalAlert.textContent = "Could not open Google login window.";
      modalAlert.classList.add("alert-error");
      modalAlert.style.display = "block";
    }
  }
}



async function processGoogleToken(accessToken, modalAlert) {
  try {
    if (modalAlert) modalAlert.textContent = "Authenticating with server...";

    const res = await fetch("/api/auth/google", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ credential: accessToken })
    });

    const data = await res.json();
    if (res.ok && data.token) {
      sessionStorage.setItem("token", data.token);
      sessionStorage.setItem("authToken", data.token);
      sessionStorage.setItem("username", data.username);
      sessionStorage.setItem("fullName", data.fullName);
      sessionStorage.setItem("role", data.role);
      sessionStorage.setItem("email", data.email);
      sessionStorage.setItem("avatarUrl", data.avatarUrl || "");
      sessionStorage.setItem("user", JSON.stringify({
        username: data.username,
        fullName: data.fullName,
        email: data.email,
        role: data.role,
        avatarUrl: data.avatarUrl || null
      }));

      if (modalAlert) {
        modalAlert.textContent = "Login successful! Redirecting...";
        modalAlert.classList.add("alert-success");
      }

      setTimeout(() => {
        if (data.role === "ROLE_ADMIN") window.location.href = "admin.html";
        else if (data.role === "Team_Member" || data.role === "ROLE_MEMBER") window.location.href = "member-contact.html";
        else {
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
      if (modalAlert) {
        modalAlert.textContent = "Login failed: " + (data.message || "");
        modalAlert.classList.add("alert-error");
      } else {
        showToast("Login failed: " + (data.message || ""), "error");
      }
    }
  } catch (error) {
    console.error("Google login error:", error);
    if (modalAlert) {
      modalAlert.textContent = "Could not connect to server.";
      modalAlert.classList.add("alert-error");
    }
  }
}

window.addEventListener('load', () => {
  initGoogleSignIn();
});

// Dynamically move footer-bottom inside footer-container for unified layout
function initFooterMove() {
  const container = document.querySelector(".footer-container");
  const bottom = document.querySelector(".footer-bottom");
  if (container && bottom) {
    container.appendChild(bottom);
  }
}

// =============================================
//  Admin – Dashboard Overview Analytics
// =============================================
let myRevenueChartInstance = null;

function getChartColors() {
  const isDark = document.documentElement.classList.contains("dark-theme");
  return {
    textColor: isDark ? "#94a3b8" : "#64748b",
    gridColor: isDark ? "#272e48" : "#e2e8f0",
    tooltipBg: isDark ? "#161b2b" : "#ffffff",
    tooltipBorder: isDark ? "#272e48" : "#e2e8f0",
    gradientStart: isDark ? "rgba(99, 102, 241, 0.4)" : "rgba(37, 99, 235, 0.35)",
    gradientEnd: isDark ? "rgba(99, 102, 241, 0.0)" : "rgba(37, 99, 235, 0.0)",
    borderColor: isDark ? "#6366f1" : "#2563eb"
  };
}

function updateChartTheme() {
  if (myRevenueChartInstance) {
    const colors = getChartColors();
    myRevenueChartInstance.options.scales.x.grid.color = colors.gridColor;
    myRevenueChartInstance.options.scales.x.ticks.color = colors.textColor;
    myRevenueChartInstance.options.scales.y.grid.color = colors.gridColor;
    myRevenueChartInstance.options.scales.y.ticks.color = colors.textColor;
    myRevenueChartInstance.options.plugins.tooltip.backgroundColor = colors.tooltipBg;
    myRevenueChartInstance.options.plugins.tooltip.borderColor = colors.tooltipBorder;
    myRevenueChartInstance.options.plugins.tooltip.titleColor = document.documentElement.classList.contains("dark-theme") ? "#f8fafc" : "#0f172a";
    myRevenueChartInstance.options.plugins.tooltip.bodyColor = document.documentElement.classList.contains("dark-theme") ? "#f8fafc" : "#0f172a";

    const canvas = document.getElementById("revenueChart");
    if (canvas) {
      const ctx = canvas.getContext("2d");
      if (ctx) {
        const gradient = ctx.createLinearGradient(0, 0, 0, 300);
        gradient.addColorStop(0, colors.gradientStart);
        gradient.addColorStop(1, colors.gradientEnd);
        myRevenueChartInstance.data.datasets[0].backgroundColor = gradient;
      }
    }
    myRevenueChartInstance.data.datasets[0].borderColor = colors.borderColor;
    myRevenueChartInstance.data.datasets[0].pointBackgroundColor = colors.borderColor;

    myRevenueChartInstance.update();
  }
}

async function fetchAdminDashboardStats() {
  const chartCanvas = document.getElementById("revenueChart");
  if (!chartCanvas) return;

  try {
    const token = getAdminToken();
    const response = await fetch("/api/admin/dashboard-stats", {
      headers: token ? { "Authorization": "Bearer " + token } : {}
    });
    if (!response.ok) throw new Error("Failed to fetch dashboard stats");
    const data = await response.json();

    // Set counts in cards
    const cardMessages = document.getElementById("stat-messages-count");
    const cardUsers = document.getElementById("stat-users-count");
    const cardMembers = document.getElementById("stat-members-count");
    const cardProjects = document.getElementById("stat-projects-count");
    const cardServices = document.getElementById("stat-services-count");
    const cardTransactions = document.getElementById("stat-transactions-count");

    if (cardMessages) cardMessages.textContent = data.messagesCount ?? "—";
    if (cardUsers) cardUsers.textContent = data.accountsCount ?? "—";
    if (cardMembers) cardMembers.textContent = data.membersCount ?? "—";
    if (cardProjects) cardProjects.textContent = data.projectsCount ?? "—";
    if (cardServices) cardServices.textContent = data.servicesCount ?? "—";
    if (cardTransactions) cardTransactions.textContent = data.transactionsCount ?? "—";

    // Render the chart
    renderRevenueChart(data.revenueData);
  } catch (err) {
    console.error("fetchAdminDashboardStats error:", err);
  }
}

function renderRevenueChart(revenueData) {
  const canvas = document.getElementById("revenueChart");
  if (!canvas) return;
  const ctx = canvas.getContext("2d");
  if (!ctx) return;

  const months = Object.keys(revenueData);
  const amounts = Object.values(revenueData);

  const colors = getChartColors();

  if (myRevenueChartInstance) {
    myRevenueChartInstance.destroy();
  }

  // Create gradient
  const gradient = ctx.createLinearGradient(0, 0, 0, 300);
  gradient.addColorStop(0, colors.gradientStart);
  gradient.addColorStop(1, colors.gradientEnd);

  myRevenueChartInstance = new Chart(ctx, {
    type: "line",
    data: {
      labels: months.map(m => {
        const parts = m.split("-");
        return parts.length === 2 ? `Month ${parts[1]}/${parts[0]}` : m;
      }),
      datasets: [{
        label: "Revenue (USD)",
        data: amounts,
        borderColor: colors.borderColor,
        borderWidth: 3,
        backgroundColor: gradient,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: colors.borderColor,
        pointBorderColor: "#ffffff",
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: colors.tooltipBg,
          titleColor: document.documentElement.classList.contains("dark-theme") ? "#f8fafc" : "#0f172a",
          bodyColor: document.documentElement.classList.contains("dark-theme") ? "#f8fafc" : "#0f172a",
          borderColor: colors.tooltipBorder,
          borderWidth: 1,
          padding: 12,
          displayColors: false,
          callbacks: {
            label: function (context) {
              let value = context.raw || 0;
              return " Revenue: $" + value.toLocaleString();
            }
          }
        }
      },
      scales: {
        x: {
          grid: {
            color: colors.gridColor,
            drawBorder: false
          },
          ticks: {
            color: colors.textColor,
            font: {
              family: "Inter",
              size: 11,
              weight: 500
            }
          }
        },
        y: {
          grid: {
            color: colors.gridColor,
            drawBorder: false
          },
          ticks: {
            color: colors.textColor,
            font: {
              family: "Inter",
              size: 11,
              weight: 500
            },
            callback: function (value) {
              return "$" + value.toLocaleString();
            }
          }
        }
      }
    }
  });
}



window.toggleSidebar = function () {
  document.body.classList.toggle("sidebar-collapsed");
  const isCollapsed = document.body.classList.contains("sidebar-collapsed");
  localStorage.setItem("adminSidebarCollapsed", isCollapsed);
}

// =======================================================
// UI/UX GLOBAL SCRIPTING OVERHAUL (TOASTS & VALIDATIONS)
// =======================================================

// 1. Toast Notification system
window.showToast = function (title, message, type = "success") {
  let container = document.getElementById("toast-notification-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "toast-notification-container";
    container.className = "toast-notification-container";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `toast-msg-item ${type}`;

  let iconSvg = "";
  if (type === "success") {
    iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#24A148" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>`;
  } else if (type === "error") {
    iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#DA1E28" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>`;
  } else {
    iconSvg = `<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0F62FE" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`;
  }

  toast.innerHTML = `
      <div class="toast-icon">${iconSvg}</div>
      <div class="toast-content">
        <div class="toast-title" style="margin:0;font-weight:700;font-size:0.9rem;">${title}</div>
        <div class="toast-message" style="margin:2px 0 0 0;font-size:0.8rem;color:var(--text-muted);">${message}</div>
      </div>
    `;

  container.appendChild(toast);

  // Auto-remove after 4 seconds
  setTimeout(() => {
    toast.classList.add("fade-out");
    setTimeout(() => {
      toast.remove();
    }, 300);
  }, 4000);
};

// 2. Form live validation
function getValidationMessage(input) {
  if (input.validity.valueMissing) {
    return "This field is required.";
  }
  if (input.validity.typeMismatch) {
    if (input.type === "email") return "Please enter a valid email address.";
    if (input.type === "url") return "Please enter a valid URL.";
  }
  if (input.validity.tooShort) {
    return `Please enter at least ${input.minLength} characters.`;
  }
  if (input.validity.patternMismatch) {
    return "Input format does not match requirements.";
  }
  return input.validationMessage || "Invalid input field value.";
}

function validateField(input) {
  const parent = input.closest(".floating-form-group") || input.parentElement;
  let errorSpan = parent.querySelector(".error-helper-text");

  if (input.type === "select-one") {
    if (input.value !== "") {
      input.classList.add("has-value");
    } else {
      input.classList.remove("has-value");
    }
  }

  if (!input.checkValidity()) {
    input.classList.add("input-error");
    if (!errorSpan) {
      errorSpan = document.createElement("span");
      errorSpan.className = "error-helper-text";
      parent.appendChild(errorSpan);
    }
    errorSpan.textContent = getValidationMessage(input);
  } else {
    input.classList.remove("input-error");
    if (errorSpan) {
      errorSpan.remove();
    }
  }
}

// Setup validation events on inputs
function setupLiveFormValidation() {
  const forms = document.querySelectorAll("form");
  forms.forEach(form => {
    const inputs = form.querySelectorAll("input, textarea, select");
    inputs.forEach(input => {
      if (input.tagName === "SELECT") {
        input.addEventListener("change", () => validateField(input));
      }

      input.addEventListener("blur", () => validateField(input));
      input.addEventListener("input", () => {
        if (input.classList.contains("input-error")) {
          validateField(input);
        }
      });
    });

    // Validate all on submit
    form.addEventListener("submit", (e) => {
      let isFormValid = true;
      inputs.forEach(input => {
        validateField(input);
        if (!input.checkValidity()) {
          isFormValid = false;
        }
      });

      if (!isFormValid) {
        e.preventDefault();
        window.showToast("Validation Error", "Please check the highlighted fields above.", "error");
      }
    });
  });
}

// Run on DOM loaded
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", setupLiveFormValidation);
} else {
  setupLiveFormValidation();
}
const CHATBOT_HISTORY_KEY = 'nova_chatbot_history';

const CHATBOT_SYSTEM_PROMPT =
  "You are Nova AI, a support assistant for NovaDigital Agency's website ONLY. " +
  "Your knowledge is strictly limited to: NovaDigital's services " +
  "STRICT RULE: If a question is not about NovaDigital or this website — including general knowledge, coding help, " +
  "other companies, personal advice, math, current events, or any topic outside the list above — you MUST NOT answer it. " +
  "Instead, reply briefly that you can only help with questions about NovaDigital's services and this website, " +
  "and ask if they'd like help with one of those instead. Do not attempt to be helpful on off-topic requests, " +
  "even if you know the answer. Be warm, professional, and concise.";

// Cached config from backend
let _chatbotConfig = null;

async function getChatbotConfig() {
  if (_chatbotConfig) return _chatbotConfig;
  try {
    const res = await fetch('/api/chatbot/config');
    if (!res.ok) throw new Error('Config fetch failed');
    _chatbotConfig = await res.json();
    return _chatbotConfig;
  } catch (e) {
    console.error('[Chatbot] Could not fetch config:', e);
    return null;
  }
}

async function callGroqDirectly(userMessage) {
  const config = await getChatbotConfig();
  if (!config || !config.apiKey) throw new Error('[Debug] Could not fetch /api/chatbot/config — is the server running?');

  const apiKey = config.apiKey;
  const groqModel = config.model || 'llama-3.3-70b-versatile';
  const GROQ_URL = 'https://api.groq.com/openai/v1/chat/completions';

  try {
    const res = await fetch(GROQ_URL, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${apiKey}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model: groqModel,
        messages: [
          { role: 'system', content: CHATBOT_SYSTEM_PROMPT },
          { role: 'user', content: userMessage }
        ]
      })
    });

    if (res.ok) {
      const data = await res.json();
      return data?.choices?.[0]?.message?.content || 'I received an empty response.';
    }

    const errBody = await res.json().catch(() => ({}));
    throw new Error(`Groq HTTP ${res.status}: ${JSON.stringify(errBody)}`);
  } catch (e) {
    throw new Error(`[Debug] Groq failed: ${e.message}`);
  }
}

function initChatbot() {
  const fab = document.getElementById('chatbot-fab');
  const windowEl = document.getElementById('chatbot-window');
  const closeBtn = document.getElementById('chatbot-close');
  const messagesContainer = document.getElementById('chatbot-messages');
  const inputEl = document.getElementById('chatbot-input');
  const sendBtn = document.getElementById('chatbot-send');

  if (!fab || !windowEl) return;

  // ── History helpers (sessionStorage: survives page nav, clears on tab close / logout) ──
  function loadHistory() {
    try { return JSON.parse(sessionStorage.getItem(CHATBOT_HISTORY_KEY) || '[]'); }
    catch { return []; }
  }
  function saveHistory(history) {
    try { sessionStorage.setItem(CHATBOT_HISTORY_KEY, JSON.stringify(history)); } catch { }
  }
  function addToHistory(sender, text) {
    const h = loadHistory();
    h.push({ sender, text, time: Date.now() });
    if (h.length > 100) h.splice(0, h.length - 100);
    saveHistory(h);
  }

  // ── Render one bubble ──
  function renderBubble(sender, text) {
    const div = document.createElement('div');
    div.className = 'chatbot-msg ' + (sender === 'user' ? 'chatbot-msg-user' : 'chatbot-msg-bot');
    div.textContent = text;
    messagesContainer.appendChild(div);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }

  // ── Restore persisted conversation ──
  const history = loadHistory();
  if (history.length === 0) {
    const welcome = 'Hello! I am Nova AI. How can I help you today?';
    renderBubble('bot', welcome);
    addToHistory('bot', welcome);
  } else {
    history.forEach(m => renderBubble(m.sender, m.text));
  }

  // Enhance header icon with 3D robot avatar if available
  const headerTitle = windowEl.querySelector('.chatbot-header-title');
  if (headerTitle && !headerTitle.querySelector('.chatbot-header-avatar')) {
    const avatarImg = document.createElement('img');
    avatarImg.src = '/images/ai_robot_waving.jpg';
    avatarImg.alt = 'Nova AI';
    avatarImg.className = 'chatbot-header-avatar';
    avatarImg.style.cssText = 'width: 26px; height: 26px; border-radius: 50%; object-fit: cover; border: 1.5px solid rgba(255, 255, 255, 0.7); box-shadow: 0 0 8px rgba(56, 189, 248, 0.5); margin-right: 4px;';
    headerTitle.prepend(avatarImg);
  }

  let isGreetingShowing = false;
  let greetingTimer = null;

  function openChatWindowDirectly() {
    windowEl.classList.add('active');
    inputEl.focus();
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }

  function show3DAIGreetingThenOpenChat() {
    if (isGreetingShowing) return;

    let overlay = document.getElementById('ai-greeting-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'ai-greeting-overlay';
      overlay.className = 'ai-greeting-overlay';
      overlay.innerHTML = `
                <div class="ai-greeting-card" id="ai-greeting-card">
                    <button type="button" class="ai-greeting-close" id="ai-greeting-close" title="Close">&times;</button>
                    <div class="ai-greeting-img-wrapper">
                        <img src="/images/ai_robot_waving.jpg" alt="Nova AI Robot Waving" class="ai-greeting-img">
                        <div class="ai-wave-hand-badge">👋</div>
                    </div>
                    <div class="ai-greeting-body">
                        <h3 class="ai-greeting-title">Hi! I'm Nova AI 👋</h3>
                        <p class="ai-greeting-subtitle">Nice to meet you! Connecting to chatbox...</p>
                        <div class="ai-greeting-btn">
                            <span>Start chatting</span>
                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
                        </div>
                    </div>
                </div>
            `;
      document.body.appendChild(overlay);

      const card = overlay.querySelector('.ai-greeting-card');

      const proceedToChat = () => {
        overlay.classList.remove('active');
        overlay.classList.add('leaving');
        setTimeout(() => {
          overlay.classList.remove('leaving');
          isGreetingShowing = false;
          openChatWindowDirectly();
        }, 250);
      };

      card.addEventListener('click', (e) => {
        if (e.target.closest('#ai-greeting-close')) {
          overlay.classList.remove('active');
          isGreetingShowing = false;
          return;
        }
        proceedToChat();
      });

      overlay._proceedToChat = proceedToChat;
    }

    isGreetingShowing = true;
    overlay.classList.remove('leaving');
    overlay.classList.add('active');
  }

  // ── Toggle open/close ──
  fab.addEventListener('click', () => {
    if (windowEl.classList.contains('active')) {
      windowEl.classList.remove('active');
      if (isGreetingShowing) {
        const overlay = document.getElementById('ai-greeting-overlay');
        if (overlay) overlay.classList.remove('active');
        isGreetingShowing = false;
      }
    } else {
      show3DAIGreetingThenOpenChat();
    }
  });
  closeBtn.addEventListener('click', () => windowEl.classList.remove('active'));

  // ── Send message ──
  const sendMessage = async () => {
    const text = inputEl.value.trim();
    if (!text || sendBtn.disabled) return;

    inputEl.value = '';
    sendBtn.disabled = true;

    renderBubble('user', text);
    addToHistory('user', text);

    // Typing indicator
    const typingDiv = document.createElement('div');
    typingDiv.className = 'chatbot-msg chatbot-msg-bot chatbot-typing';
    typingDiv.innerHTML = '<span></span><span></span><span></span>';
    messagesContainer.appendChild(typingDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    try {
      const reply = await callGroqDirectly(text);
      typingDiv.remove();
      renderBubble('bot', reply);
      addToHistory('bot', reply);
    } catch (err) {
      console.error('[Chatbot] Error:', err);
      typingDiv.remove();
      // Show real error in chat for debugging
      const msg = err.message || 'Sorry, I am currently unavailable. Please try again later.';
      renderBubble('bot', msg);
      addToHistory('bot', msg);
    } finally {
      sendBtn.disabled = false;
      inputEl.focus();
    }
  };

  sendBtn.addEventListener('click', sendMessage);
  inputEl.addEventListener('keypress', e => {
    if (e.key === 'Enter') { e.preventDefault(); sendMessage(); }
  });
}

// Clear chatbot history on logout (called by logoutUser which already does sessionStorage.clear())
function clearChatbotHistory() {
  sessionStorage.removeItem(CHATBOT_HISTORY_KEY);
}

window.initChatbot = initChatbot;
window.clearChatbotHistory = clearChatbotHistory;

// =============================================
//  Admin - Quotation Logic
// =============================================

function openQuoteModal(bookingId) {
  const booking = _cache.bookings[bookingId];
  if (!booking) {
    showToast("Booking data not found", "error");
    return;
  }
  const service = _cache.services[booking.serviceId] || { title: "Unknown Service" };

  document.getElementById("quoteBookingId").value = booking.id;
  document.getElementById("quoteTitle").value = service.title + " Package";
  document.getElementById("quoteSubtotal").value = Number(booking.totalPrice || booking.basePrice || 0).toFixed(2);
  document.getElementById("quoteItemName").value = service.title;
  document.getElementById("quoteDiscount").value = "0";
  document.getElementById("quoteTax").value = "10";
  document.getElementById("quoteDeposit").value = "20";
  document.getElementById("quoteNotes").value = "";

  calculateQuoteTotal();
  document.getElementById("quote-modal-overlay").classList.add("is-open");
}

function closeQuoteModal() {
  document.getElementById("quote-modal-overlay").classList.remove("is-open");
}

function calculateQuoteTotal() {
  const subtotal = parseFloat(document.getElementById("quoteSubtotal").value) || 0;
  const discountPercent = parseFloat(document.getElementById("quoteDiscount").value) || 0;
  const taxPercent = 10.0; // Fixed 10%

  const discountAmount = subtotal * (discountPercent / 100);
  const taxAmount = (subtotal - discountAmount) * (taxPercent / 100);
  const total = subtotal - discountAmount + taxAmount;

  document.getElementById("quoteTotalAmount").value = total > 0 ? total.toFixed(2) : "0.00";
}

async function submitQuote() {
  const title = document.getElementById("quoteTitle").value;
  const bookingId = document.getElementById("quoteBookingId").value;
  if (!title) {
    showToast("Please enter a quotation title", "error");
    return;
  }

  const subtotal = parseFloat(document.getElementById("quoteSubtotal").value) || 0;
  const discountPercent = parseFloat(document.getElementById("quoteDiscount").value) || 0;
  const discountAmount = subtotal * (discountPercent / 100);
  const taxAmount = (subtotal - discountAmount) * 0.10; // 10% fixed
  const totalAmount = parseFloat(document.getElementById("quoteTotalAmount").value) || 0;
  const depositPercentage = parseFloat(document.getElementById("quoteDeposit").value) || 20;
  const notes = document.getElementById("quoteNotes").value;
  const itemName = document.getElementById("quoteItemName").value;

  const adminId = localStorage.getItem("userId") || sessionStorage.getItem("userId") || 1;

  const requestPayload = {
    title,
    subtotal,
    discountAmount,
    taxAmount,
    totalAmount,
    depositPercentage,
    notes,
    items: [
      {
        itemName: itemName,
        description: "Main service package based on booking",
        quantity: 1,
        unitPrice: subtotal,
        subtotal: subtotal
      }
    ]
  };

  const btn = document.querySelector("#quote-modal-overlay .btn-save");
  const originalText = btn.innerText;
  btn.innerText = "Processing...";
  btn.disabled = true;

  try {
    // 1. Create quote
    const createRes = await fetch(`/api/quotations/from-booking/${bookingId}?adminId=${adminId}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...adminHeaders()
      },
      body: JSON.stringify(requestPayload)
    });

    if (!createRes.ok) throw new Error("Failed to create quote");
    const createdQuote = await createRes.json();

    // 2. Send email
    btn.innerText = "Sending Email...";
    const emailRes = await fetch(`/api/quotations/${createdQuote.id}/send-email`, {
      method: "POST",
      headers: adminHeaders()
    });

    if (!emailRes.ok) throw new Error("Failed to send quote email");

    showToast("Quotation created and sent successfully to the client!", "success");
    closeQuoteModal();
    fetchAdminBookings();
  } catch (err) {
    console.error("Quote error:", err);
    showToast(err.message || "An error occurred", "error");
  } finally {
    btn.innerText = originalText;
    btn.disabled = false;
  }
}

window.openQuoteModal = openQuoteModal;
window.closeQuoteModal = closeQuoteModal;
window.calculateQuoteTotal = calculateQuoteTotal;
window.submitQuote = submitQuote;

// =============================================
//  Admin - Quotations Table & 1-Click Convert Modal
// =============================================
async function fetchAdminQuotationsTable() {
  const tbody = document.getElementById("quotations-table-body");
  if (!tbody) return;

  try {
    const response = await fetch("/api/quotations", {
      headers: adminHeaders()
    });
    if (!response.ok) throw new Error("Failed to fetch quotations");
    const quotations = await response.json();

    if (Object.keys(_cache.users).length === 0) {
      await fetchAdminUsers();
    }

    tbody.innerHTML = "";

    if (!quotations.length) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:2rem;color:var(--text-muted);">No quotations found.</td></tr>`;
      return;
    }

    quotations.forEach(q => {
      _cache.quotations[q.id] = q;

      const client = q.client || _cache.users[q.clientId] || { fullName: `User #${q.clientId || ''}`, email: "" };
      const clientName = client.fullName || client.username || "Client";
      const totalAmt = q.totalAmount ? Number(q.totalAmount).toFixed(2) : "0.00";
      const depositPct = q.depositPercentage != null ? q.depositPercentage : 20;
      const depositVal = (Number(totalAmt) * depositPct / 100).toFixed(2);

      let statusBadge = `<span class="badge badge-secondary" style="padding:0.3rem 0.6rem;border-radius:6px;font-weight:600;font-size:0.75rem;background:#94a3b8;color:#fff;">${escapeHtml(q.status)}</span>`;
      let actionBtn = "";

      if (q.status === "APPROVED") {
        statusBadge = `<span class="badge badge-success" style="padding:0.3rem 0.6rem;border-radius:6px;font-weight:600;font-size:0.75rem;background:#10b981;color:#fff;">APPROVED BY CLIENT</span>`;
        actionBtn = `<button type="button" class="btn-add" onclick="openConvertQuoteModal(${q.id})" style="padding:0.35rem 0.75rem;font-size:0.8rem;gap:6px;border-radius:6px;cursor:pointer;background:#059669;color:#fff;font-weight:600;display:inline-flex;align-items:center;">
            <svg viewBox="0 0 24 24" style="width:14px;height:14px;stroke:currentColor;fill:none;stroke-width:2;"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg> Convert to Project
          </button>`;
      } else if (q.status === "CONVERTED") {
        statusBadge = `<span class="badge badge-purple" style="padding:0.3rem 0.6rem;border-radius:6px;font-weight:600;font-size:0.75rem;background:#8b5cf6;color:#fff;">CONVERTED</span>`;
        actionBtn = `<span style="font-size:0.78rem;color:var(--text-muted);font-style:italic;">Project Active</span>`;
      } else if (q.status === "PROPOSED") {
        statusBadge = `<span class="badge badge-info" style="padding:0.3rem 0.6rem;border-radius:6px;font-weight:600;font-size:0.75rem;background:#3b82f6;color:#fff;">PROPOSED (SENT)</span>`;
      }

      const tr = document.createElement("tr");
      tr.setAttribute("data-searchable", `${q.quoteCode} ${q.title} ${clientName} ${q.status}`);

      tr.innerHTML = `
        <td>
          <div class="text-dark-inline" style="font-weight:600;color:var(--text-dark);">${escapeHtml(q.quoteCode || '')}</div>
          <div style="font-size:0.82rem;color:var(--text-muted);">${escapeHtml(q.title || '')}</div>
        </td>
        <td>
          <div class="text-dark-inline">${escapeHtml(clientName)}</div>
          <div style="font-size:0.8rem;color:var(--text-muted);">${escapeHtml(client.email || '')}</div>
        </td>
        <td style="font-weight:600;color:#10b981;">$${totalAmt}</td>
        <td>
          <div style="font-weight:600;">$${depositVal}</div>
          <div style="font-size:0.78rem;color:var(--text-muted);">${depositPct}% Deposit</div>
        </td>
        <td>${statusBadge}</td>
        <td>${actionBtn}</td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error("fetchAdminQuotationsTable error:", err);
  }
}

async function openConvertQuoteModal(quoteId) {
  let quote = _cache.quotations ? _cache.quotations[quoteId] : null;
  if (!quote) {
    try {
      const res = await fetch(`/api/quotations/${quoteId}`, { headers: adminHeaders() });
      if (res.ok) quote = await res.json();
    } catch (e) { console.error("Error fetching quote:", e); }
  }

  if (!quote) {
    showToast("Quote data not found", "error");
    return;
  }

  if (Object.keys(_cache.users).length === 0) {
    await fetchAdminUsers();
  }

  const clientId = quote.client ? quote.client.id : (quote.clientId || "");
  const totalAmt = quote.totalAmount || 0;
  const depPct = quote.depositPercentage != null ? quote.depositPercentage : 20;
  const depVal = (totalAmt * depPct / 100).toFixed(2);
  const serviceName = quote.booking && quote.booking.serviceName ? quote.booking.serviceName : "Web Development";

  const prefilledItem = {
    title: quote.title,
    category: serviceName,
    clientId: clientId,
    depositAmount: depVal,
    imageUrl: "https://images.unsplash.com/photo-1460925895917-afdab827c52f?auto=format&fit=crop&w=800&h=400",
    description: quote.notes || ("Project converted automatically from quotation " + quote.quoteCode),
    technologies: "Java, Spring Boot, HTML, CSS, JavaScript"
  };

  _crudState = { type: "project", item: null, convertingQuoteId: quoteId };

  const overlay = document.getElementById("crud-modal-overlay");
  const titleEl = document.getElementById("crud-modal-title");
  const bodyEl = document.getElementById("crud-modal-body");
  const alertEl = document.getElementById("crud-alert");

  if (!overlay) return;
  if (alertEl) { alertEl.style.display = "none"; alertEl.textContent = ""; alertEl.className = "crud-alert alert-message"; }

  titleEl.textContent = `Convert Quotation (${quote.quoteCode}) to Project`;
  bodyEl.innerHTML = buildCrudForm("project", prefilledItem);

  const fileInput = document.getElementById("cf-imageFile");
  if (fileInput) {
    fileInput.addEventListener("change", async (e) => {
      const file = e.target.files[0];
      if (!file) return;
      const preview = document.getElementById("cf-preview");
      const urlInput = document.getElementById("cf-imageUrl");
      try {
        const reader = new FileReader();
        reader.onload = () => {
          urlInput.value = reader.result;
          if (preview) { preview.src = reader.result; preview.style.display = "block"; }
        };
        reader.readAsDataURL(file);
      } catch (err) { }
    });
  }

  overlay.classList.add("is-open");
  document.body.style.overflow = "hidden";
}

window.fetchAdminQuotationsTable = fetchAdminQuotationsTable;
window.openConvertQuoteModal = openConvertQuoteModal;

// =============================================
//  Admin - Quotation SSE Listener
// =============================================
function initQuotationSSE() {
  const eventSource = new EventSource('/api/sse/stream');

  eventSource.addEventListener('QUOTE_APPROVED', (e) => {
    try {
      const audio = new Audio('https://www.soundjay.com/buttons/sounds/bell-ringing-05.mp3');
      audio.play().catch(err => console.log('Audio play prevented by browser:', err));
    } catch (e) { }

    showToast(e.data, "success");

    if (document.getElementById("panel-bookings")?.classList.contains("active")) {
      fetchAdminBookings();
    }
  });

  eventSource.onerror = (err) => {
    console.log("SSE error for quotations", err);
  };
}

if (document.getElementById("panel-bookings")) {
  initQuotationSSE();
}
window.clearChatbotHistory = clearChatbotHistory;
