# 墨韵读书平台

一个基于Spring Boot的综合性读书社交平台，采用严格的软件工程架构设计（边界类、控制类、实体类、辅助类），支持多角色用户体系和完整的读书社交功能。

## 一、项目特性

### 1.1 用户角色体系
- **普通用户（User）** - 基础功能：管理书库、书评、参与讨论、写日志
- **学生（Student）** - 继承User：可选择导师、向老师提问、查看推荐图书
- **导师（Teacher）** - 继承User：可推荐图书、创建管理圈子、回答学生问题、管理学生
- **管理员（Admin）** - 继承User：拥有内容删除和用户管理权限

### 1.2 核心功能模块
-  **用户管理** - 注册、登录、个人信息管理、密码重置
-  **图书管理** - 图书添加、修改、查看、搜索、分类管理
-  **书评系统** - 写书评、评论书评、点赞功能
-  **讨论区** - 发起讨论、回复讨论、师生问答
-  **日志管理** - 读书日志、隐私设置、评论功能
-  **圈子功能** - 导师创建和管理圈子、成员管理
-  **师生关系** - 学生申请导师、导师审批管理
-  **图书推荐** - 导师向学生/圈子推荐图书
-  **管理功能** - 管理员删除内容、用户身份管理

## 🏗二、项目架构

### 2.1 架构设计

```
src/main/java/com/moyun/reading/
├── boundary/              # 边界类 - 用户界面层
│   ├── controller/        # MVC控制器
│   │   ├── UserController.java
│   │   ├── BookController.java
│   │   ├── ReviewController.java
│   │   ├── DiscussionController.java
│   │   ├── DiaryController.java
│   │   ├── CircleController.java
│   │   ├── StudentController.java
│   │   ├── TeacherController.java
│   │   └── AdminController.java
│   └── dto/              # 数据传输对象
│       └── UserRegistrationDto.java
├── control/              # 控制类 - 业务逻辑层
│   └── service/          # 业务服务
│       ├── UserService.java
│       ├── BookService.java
│       ├── ReviewService.java
│       ├── DiscussionService.java
│       ├── ReadingDiaryService.java
│       ├── CircleService.java
│       ├── StudentService.java
│       └── TeacherService.java
├── entity/               # 实体类 - 数据模型层
│   ├── base/             # 基础实体
│   │   ├── BaseEntity.java
│   │   └── User.java
│   ├── user/             # 用户相关实体
│   │   ├── Student.java
│   │   ├── Teacher.java
│   │   └── Admin.java
│   ├── book/             # 图书相关实体
│   │   ├── Book.java
│   │   └── BookCategory.java
│   ├── review/           # 书评相关实体
│   │   ├── Review.java
│   │   ├── ReviewComment.java
│   │   └── ReviewLike.java
│   ├── discussion/       # 讨论相关实体
│   │   ├── Discussion.java
│   │   └── DiscussionReply.java
│   ├── diary/            # 日志相关实体
│   │   ├── ReadingDiary.java
│   │   └── DiaryComment.java
│   ├── circle/           # 圈子相关实体
│   │   ├── Circle.java
│   │   └── CircleMember.java
│   └── recommendation/   # 推荐相关实体
│       └── BookRecommendation.java
├── utility/              # 辅助类 - 工具类层
│   └── security/         # 安全工具
│       ├── PasswordUtil.java
│       └── SecurityUtil.java
├── repository/           # 数据访问层
├── config/               # 配置类
│   ├── SecurityConfig.java
│   ├── DatabaseInitializer.java
│   └── DataInitializer.java
└── dto/                  # 数据传输对象
    └── UserProfileUpdateDto.java
```

### 2.2 用户继承关系
```
BaseEntity (基础实体)
└── User (普通用户)
    ├── Student (学生) extends User
    ├── Teacher (导师) extends User  
    └── Admin (管理员) extends User
```

## 三、项目使用

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+ (生产环境)

### 3.1 克隆项目
```bash
git clone https://github.com/gglezhuzhu/moyun
cd software_moyun
```

### 3.2 配置数据库
项目使用云端数据库TiDB,项目所需数据库部分都已完成连接，可读写。

### 3.3 运行项目
```bash
#使用Maven（主目录下）
./mvnw clean compile 
./mvnw spring-boot:run
```

### 3.4 访问应用
- 应用地址：http://localhost:8080

### 3.5 测试账号
系统在云端数据库已经有以下账号：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 拥有所有权限 |
| 导师 | teacher | 123456 | 可管理圈子和学生 |
| 学生 | ozx | 123456 | 已有导师(teacher) |
| 普通用户 | normal | 123456 | 基础功能 |

## 四、功能演示

### 4.1 普通用户功能
1. **用户中心** - `/user/profile` - 查看和编辑个人信息
2. **图书管理** - `/books` - 浏览、搜索、添加图书
3. **书评功能** - `/reviews` - 写书评、评论、点赞
4. **讨论区** - `/discussions` - 参与公共讨论
5. **读书日志** - `/diary` - 记录读书心得

### 4.2 学生特有功能
1. **选择导师** - `/student/select-teacher` - 申请成为某位导师的学生
2. **我的申请** - `/student/applications` - 查看申请状态
3. **向老师提问** - `/discussions/create?type=QUESTION` - 私密问答
 
###4.3  导师特有功能
1. **学生管理** - `/teacher/applications` - 审批学生申请
2. **圈子管理** - `/teacher/circles` - 创建和管理读书圈子
3. **图书推荐** - `/teacher/create-recommendation` - 向学生推荐图书
4. **回答问题** - `/teacher/questions` - 回答学生提问

### 4.4 管理员功能
1. **用户管理** - `/admin/users` - 管理用户角色和状态
2. **内容管理** - `/admin/books`, `/admin/reviews`, `/admin/discussions`, `/admin/diaries`
3. **数据统计** - `/admin/dashboard` - 查看平台统计数据

## 五、开发指南

### 5.1 添加新功能流程
1. **创建实体类** - 在`entity`对应包下创建，继承`BaseEntity`
2. **创建Repository** - 在`repository`包下创建JPA接口
3. **创建Service** - 在`control/service`包下实现业务逻辑
4. **创建Controller** - 在`boundary/controller`包下处理请求
5. **创建页面模板** - 在`src/main/resources/templates`下创建Thymeleaf模板

### 5.2 数据库设计原则
- 使用JPA自动建表（`spring.jpa.hibernate.ddl-auto=update`）
- 用户表采用单表继承策略（DTYPE字段区分类型）
- 所有表包含`created_at`、`updated_at`、`deleted`字段
- 使用外键约束保证数据完整性

## 六、API文档

主要页面路由：

| 功能模块 | 路由 | 说明 |
|----------|------|------|
| 首页 | `/` | 展示最新图书和书评 |
| 用户注册 | `/user/register` | 用户注册页面 |
| 用户登录 | `/user/login` | 用户登录页面 |
| 个人中心 | `/user/profile` | 查看个人信息 |
| 图书列表 | `/books` | 浏览所有图书 |
| 图书详情 | `/books/{id}` | 查看图书详情 |
| 书评列表 | `/reviews` | 浏览所有书评 |
| 书评详情 | `/reviews/{id}` | 查看书评详情 |
| 讨论列表 | `/discussions` | 浏览讨论 |
| 日志列表 | `/diary` | 查看公开日志 |
| 圈子列表 | `/circles` | 浏览圈子 |

## 七、联系方式

- 项目维护：gglezhuzhu
- 项目地址：[GitHub仓库](https://github.com/gglezhuzhu/moyun)
- 问题反馈：[Issues](https://github.com/gglezhuzhu/moyun/issues)



**墨韵读书平台** - 让阅读更有温度，让知识更有深度。
