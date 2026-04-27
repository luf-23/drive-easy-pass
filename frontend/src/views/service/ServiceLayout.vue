<template>
  <div class="service-layout">
    <header class="service-header">
      <div class="brand">驾校报考服务</div>
      <nav>
        <RouterLink to="/service/home">首页</RouterLink>
        <RouterLink to="/service/exam-sites">考场与线路</RouterLink>
        <RouterLink to="/service/signup">在线报名</RouterLink>
        <RouterLink to="/service/profile">个人中心</RouterLink>
      </nav>
    </header>

    <main class="service-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.service-layout {
  min-height: 100vh;
  background: linear-gradient(180deg, #eaf4ff 0%, #f8fbff 44%, #ffffff 100%);
}

.service-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 14px 22px;
  background: #0b3d6b;
  box-shadow: 0 10px 24px rgba(6, 35, 62, 0.24);
}

.brand {
  color: #fff;
  font-size: 18px;
  font-weight: 800;
}

nav {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

nav a {
  padding: 8px 12px;
  border-radius: 999px;
  color: #d7e9ff;
  font-weight: 700;
}

nav a.router-link-active {
  color: #0b3d6b;
  background: #f5b942;
}

.service-main {
  max-width: 1180px;
  margin: 0 auto;
  padding: 22px;
}

@media (max-width: 900px) {
  .service-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
