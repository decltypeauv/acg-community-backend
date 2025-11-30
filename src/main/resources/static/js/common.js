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

        // 组件加载完后，初始化全局事件
        initGlobalEvents();
        checkLogin();
        
    } catch (e) {
        console.error("加载组件出错:", e);
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