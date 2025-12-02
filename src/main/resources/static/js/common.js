// static/js/common.js

// 1. 加载组件 (核心函数)
async function loadComponents() {
    try {
        // --- 加载 Header (导航栏) ---
        const headerPlaceholder = document.getElementById('header-placeholder');
        if (headerPlaceholder) {
            const headerRes = await fetch('/components/header.html');
            if(headerRes.ok) headerPlaceholder.innerHTML = await headerRes.text();
        }

        // --- 加载 Sidebar (左侧栏) ---
        // 关键修改：先检查页面上有没有 id="sidebar-placeholder"
        const sidebarPlaceholder = document.getElementById('sidebar-placeholder');
        if (sidebarPlaceholder) {
            const sidebarRes = await fetch('/components/sidebar.html');
            if(sidebarRes.ok) sidebarPlaceholder.innerHTML = await sidebarRes.text();
        }

         // 3. 【新增】加载右侧 Sidebar
        // 逻辑：只有页面里写了 id="right-placeholder" 才会加载
        // 这样 profile.html 可以不写这个ID，从而保留它自己的个人资料卡
        const rightPlaceholder = document.getElementById('right-placeholder');
        if (rightPlaceholder) {
            const res = await fetch('/components/sidebar-right.html');
            if(res.ok) rightPlaceholder.innerHTML = await res.text();
        }

        // 组件加载完后，初始化全局事件
        initGlobalEvents();
        checkLogin();
        initTheme(); //加载完组件后，立刻应用主题状态
        
    } catch (e) {
        console.error("加载组件出错:", e);
    }
}

// --- 暗黑模式逻辑 ---

// 初始化：页面一加载就检查本地存没存过
function initTheme() {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateToggleUI(savedTheme === 'dark');
}

// 切换：点击开关时触发
function toggleDarkMode() {
    const current = document.documentElement.getAttribute('data-theme');
    const target = current === 'dark' ? 'light' : 'dark';
    
    // 1. 设置属性 (CSS 会自动变色)
    document.documentElement.setAttribute('data-theme', target);
    // 2. 保存到本地 (刷新后还在)
    localStorage.setItem('theme', target);
    // 3. 动一下开关 UI
    updateToggleUI(target === 'dark');
}

// 更新开关的小圆点位置
function updateToggleUI(isDark) {
    const toggle = document.querySelector('.toggle-switch');
    if(toggle) {
        if(isDark) toggle.classList.add('active');
        else toggle.classList.remove('active');
    }
}


// 2. 初始化全局事件 (菜单切换等)
function initGlobalEvents() {
    // 绑定菜单点击
    const userNav = document.getElementById('user-nav');
    if(userNav) userNav.onclick = toggleUserMenu;

    // 绑定点击空白关闭菜单
    document.addEventListener('click', (e) => {
        const menu = document.getElementById('user-menu');
        const btn = document.getElementById('user-nav');
        if(menu && menu.classList.contains('show') && !menu.contains(e.target) && !btn.contains(e.target)) {
            menu.classList.remove('show');
        }
    });

    // 绑定头像上传
    const uploadInput = document.getElementById('avatar-upload-input');
    if(uploadInput) {
        uploadInput.onchange = async function() {
            const file = this.files[0];
            if(!file) return;
            const fd = new FormData(); fd.append('file', file);
            try {
                const res = await fetch('/api/user/update-avatar', { method:'POST', body:fd });
                const d = await res.json();
                if(d.success) location.reload(); else alert(d.msg);
            } catch(e){}
        };
    }
}

// 3. 基础工具函数
function openModal(id) { document.getElementById(id).classList.add('active'); }
function closeModal(id) { document.getElementById(id).classList.remove('active'); }
function switchModal(c, o) { closeModal(c); openModal(o); }
function toggleUserMenu(e) { e.stopPropagation(); document.getElementById('user-menu').classList.toggle('show'); }
function triggerAvatarUpload() { document.getElementById('avatar-upload-input').click(); }

// 4. 认证逻辑
async function checkLogin() {
    try {
        const res = await fetch('/api/auth/check');
        const d = await res.json();
        if(d.isLogin) {
            document.getElementById('guest-nav').style.display = 'none';
            document.getElementById('user-nav').style.display = 'flex';

            // 【新增】显示顶部的 Create 按钮
            const createBtn = document.getElementById('nav-create-btn');
            if(createBtn) createBtn.style.display = 'flex';
            
            const avatar = d.user.avatar || 'https://example.com/default.png';
            document.getElementById('nav-avatar-img').src = avatar;

            // 更新下拉菜单
            if(document.getElementById('drawer-avatar-img')) {
                document.getElementById('drawer-avatar-img').src = avatar;
                document.getElementById('drawer-username').innerText = 'u/' + d.user.nickname;
                
                // 绑定 View Profile 跳转
                document.querySelector('.profile-header').onclick = function() {
                    window.location.href = `profile.html?id=${d.user.id}`;
                };
            }
        }
    } catch(e) {}
}

async function doLogin() {
    const u = document.getElementById('l-user').value;
    const p = document.getElementById('l-pass').value;
    try {
        const res = await fetch('/api/auth/login', {
            method:'POST', headers:{'Content-Type':'application/json'},
            body: JSON.stringify({username:u, password:p})
        });
        const d = await res.json();
        if(d.success) location.reload(); else alert(d.msg);
    } catch(e){}
}

async function doRegister() {
    // ... 简单的注册逻辑，这里省略 ...
    alert("请复用之前的注册逻辑");
}

async function logout() { await fetch('/api/auth/logout'); location.reload(); }