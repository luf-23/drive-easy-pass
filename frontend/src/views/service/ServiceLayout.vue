<template>
  <div class="service-layout">
    <header class="service-header">
      <div class="brand">驾校报考服务</div>
      <nav>
        <RouterLink to="/service/home">首页</RouterLink>
        <RouterLink to="/service/exam-booking">考试预约</RouterLink>
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
  background: linear-gradient(180deg, #eaf4ff 0%, #f8fbff 44%, #fff 100%);
}

.service-header {
  display: flex;
  gap: 20px;
  align-items: center;
  justify-content: space-between;
  padding: 14px 22px;
  background: #0b3d6b;
  box-shadow: 0 10px 24px rgb(6 35 62 / 24%);
}

.brand {
  font-size: 18px;
  font-weight: 800;
  color: #fff;
}

nav {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

nav a {
  padding: 8px 12px;
  font-weight: 700;
  color: #d7e9ff;
  border-radius: 999px;
}

nav a.router-link-active {
  color: #0b3d6b;
  background: #f5b942;
}

.service-main {
  max-width: 1180px;
  padding: 22px;
  margin: 0 auto;
}

@media (width <= 900px) {
  .service-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
