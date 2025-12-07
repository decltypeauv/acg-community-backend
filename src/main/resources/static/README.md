# 🌸 ACG Reddit (Shamiko Theme)
一个基于 Spring Boot 的二次元社区论坛，致敬《街角魔族》的夏美子主题。
实现了仿 Reddit 的 UI 布局、暗黑模式切换、以及完整的社区互动功能。

## 📸 项目截图
> ![Light Mode](light.png)

## 🛠️ 技术栈
- **后端**: Java 17, Spring Boot 3, Spring Data JPA
- **数据库**: MySQL 8.0
- **前端**: 原生 HTML5/CSS3/JavaScript (采用模块化设计), Fetch API
- **部署**: Docker

## ✨ 核心功能
1. **用户系统**: 注册、登录、个人中心、头像上传、资料修改。
2. **内容发布**: 支持图文混排、视频上传、多文件预览、分类选择。
3. **互动模块**: 帖子/评论的点赞与踩（实时反馈）、无限级评论回复（楼中楼）。
4. **视觉体验**: 
   - 完美复刻 Reddit 三栏响应式布局。
   - 组件化前端架构 (Header/Sidebar 复用)。

## 🚀 如何运行
1. 克隆项目到本地。
2. 配置 `application.properties` 中的 MySQL 数据库信息。
3. 运行 `AcgBackendApplication.java`。
4. 浏览器访问 `http://localhost:8080`。

---
*Created by [limsuig] - 2025*