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

        loadRecommendations(); // 加载推荐列表

        // 【新增】加载夏美子装饰 (所有页面生效)
        addShamikoDecor();
        
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
            
            // 【新增】显示铃铛
            const bell = document.getElementById('nav-bell');
            if(bell) bell.style.display = 'flex';

            // 【新增】查询未读数量
            checkUnreadCount();

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
    const u = document.getElementById('r-user').value;
    const n = document.getElementById('r-nick').value;
    const p = document.getElementById('r-pass').value;

    if(!u || !p) { alert("账号和密码必填"); return; }

    const btn = document.querySelector('#registerModal .primary-btn');
    // 防止重复点击
    if(btn) { btn.innerText = "Signing up..."; btn.disabled = true; }

    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: u,
                password: p,
                nickname: n
            })
        });
        const d = await res.json();

        if(d.success) {
            alert("注册成功！请登录");
            switchModal('registerModal', 'loginModal'); // 注册完自动跳到登录框
        } else {
            alert("注册失败: " + d.msg);
        }
    } catch(e) {
        alert("网络错误");
    } finally {
        if(btn) { btn.innerText = "Sign Up"; btn.disabled = false; }
    }
}


async function logout() { await fetch('/api/auth/logout'); location.reload(); }

// --- 推荐系统逻辑 ---
async function loadRecommendations() {
    const widget = document.getElementById('rec-widget');
    const listContainer = document.getElementById('rec-list');
    const titleEl = document.getElementById('rec-title');
    
    // 如果当前页面没有右边栏（比如 profile.html 自己写了右栏），可能找不到元素
    if (!widget || !listContainer) return;

    try {
        const res = await fetch('/api/recommend/sidebar');
        const data = await res.json();

        if (data.list && data.list.length > 0) {
            widget.style.display = 'block'; // 有数据才显示
            titleEl.innerText = data.title; // 显示推荐理由 (e.g. "Because you like Anime")
            
            listContainer.innerHTML = ''; // 清空

            data.list.forEach(topic => {
                // 生成迷你卡片
                const div = document.createElement('div');
                div.style.cssText = 'padding: 10px; border-bottom: 1px solid #edeff1; cursor: pointer; display: flex; align-items: start; gap: 8px;';
                div.onmouseover = () => div.style.backgroundColor = '#f6f7f8';
                div.onmouseout = () => div.style.backgroundColor = 'transparent';
                div.onclick = () => window.location.href = `topic_detail.html?id=${topic.id}`;

                // 如果帖子有图，显示一个小缩略图
                let imgHtml = '';
                if(topic.mediaList && topic.mediaList.length > 0 && topic.mediaList[0].type === 'IMAGE') {
                    imgHtml = `<img src="${topic.mediaList[0].url}" style="width:40px; height:40px; border-radius:4px; object-fit:cover; flex-shrink:0;">`;
                }

                div.innerHTML = `
                    ${imgHtml}
                    <div style="flex:1; min-width:0;">
                        <div style="font-size:13px; font-weight:500; line-height:1.4; overflow:hidden; text-overflow:ellipsis; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical;">${topic.title}</div>
                        <div style="font-size:11px; color:#787c7e; margin-top:2px;">${topic.voteCount || 0} votes</div>
                    </div>
                `;
                listContainer.appendChild(div);
            });
        }
    } catch (e) {
        console.error("推荐加载失败", e);
    }
}

 // 【新增】查询未读数函数
async function checkUnreadCount() {
    try {
        const res = await fetch('/api/notification/unread-count');
        const d = await res.json();
        const badge = document.getElementById('nav-bell-badge');
        if (badge) {
            if (d.count > 0) {
                badge.style.display = 'block';
                badge.innerText = d.count > 99 ? '99+' : d.count;
            } else {
                badge.style.display = 'none';
            }
        }
    } catch(e) {}
}

// --- 新增函数：添加看板娘 ---
function addShamikoDecor() {
    // 1. 创建左边的夏美子 (Shamiko)
    const leftImg = document.createElement('img');
    leftImg.id = 'decor-shamiko';
    // 初始默认为常服，之后由 toggleDarkMode 控制切换
    leftImg.src = '/images/shamiko_casual.png'; 
    leftImg.style.cssText = "position: fixed; bottom: 0; left: 0; width: 250px; z-index: 9999; pointer-events: none; transition: 0.5s;";
    document.body.appendChild(leftImg);

    // 2. 创建右边的桃 (Momo) - 可选
    const rightImg = document.createElement('img');
    rightImg.src = '/images/momo_casual.png'; // 如果你有桃的图
    rightImg.style.cssText = "position: fixed; bottom: 0; right: 0; width: 200px; z-index: 9999; pointer-events: none; transition: 0.5s;";
    document.body.appendChild(rightImg);

    // 立即根据当前模式刷新一下图片
    updateDecorImage();
}

// --- 修改 toggleDarkMode 函数 (完整版) ---
function toggleDarkMode() {
    // 1. 获取当前是什么模式
    const current = document.documentElement.getAttribute('data-theme');
    
    // 2. 定义目标模式 (如果是 dark 就变 light，反之亦然)
    const target = current === 'dark' ? 'light' : 'dark';
    
    // 3. 设置属性 (CSS 会自动变色)
    document.documentElement.setAttribute('data-theme', target);
    
    // 4. 保存到本地
    localStorage.setItem('theme', target);
    
    // 5. 更新开关 UI
    updateToggleUI(target === 'dark');

    // 6. 更新夏美子立绘 (切换变身形态)
    updateDecorImage(); 
}

// --- 新增：根据暗黑模式切换立绘 ---
function updateDecorImage() {
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const shamiko = document.getElementById('decor-shamiko');

    if (shamiko) {
        if (isDark) {
            // 暗黑模式：危机管理形态
            shamiko.src = '/images/shamiko_crisis.png';
            shamiko.style.filter = "drop-shadow(0 0 10px rgba(255, 46, 99, 0.5))"; // 加点发光特效
        } else {
            // 亮色模式：常服
            shamiko.src = '/images/shamiko_casual.png';
            shamiko.style.filter = "none";
        }
    }
}