<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { request } from '../../services/api'
import type { ExamVenueDTO } from '../../types'

const router = useRouter()
const venues = ref<ExamVenueDTO[]>([])
const keyword = ref('')
const examType = ref('')
const loading = ref(false)

const loadVenues = async () => {
  loading.value = true
  try {
    venues.value = await request<ExamVenueDTO[]>('/api/exam/venues')
  } catch (err) {
    console.error('加载考场失败', err)
  } finally {
    loading.value = false
  }
}

const search = async () => {
  loading.value = true
  try {
    const url = keyword.value
      ? `/api/exam/venues/search?keyword=${encodeURIComponent(keyword.value)}`
      : '/api/exam/venues'
    venues.value = await request<ExamVenueDTO[]>(url)
  } catch (err) {
    console.error('搜索失败', err)
  } finally {
    loading.value = false
  }
}

const filterByType = async () => {
  if (!examType.value) return loadVenues()
  loading.value = true
  try {
    venues.value = await request<ExamVenueDTO[]>(`/api/exam/venues/type/${examType.value}`)
  } catch (err) {
    console.error('筛选失败', err)
  } finally {
    loading.value = false
  }
}

const goDetail = (id: number) => {
  router.push(`/exam/venue/${id}`)
}

onMounted(loadVenues)
</script>

<template>
  <main class="venue-page">
    <h1>🚗 考场信息</h1>

    <section class="filter-bar">
      <input
        v-model="keyword"
        placeholder="搜索考场名称或地址"
        @keyup.enter="search"
      />
      <select v-model="examType" @change="filterByType">
        <option value="">全部类型</option>
        <option value="科目一">科目一</option>
        <option value="科目二">科目二</option>
        <option value="科目三">科目三</option>
        <option value="科目四">科目四</option>
      </select>
      <button class="primary" @click="search">🔍 搜索</button>
    </section>

    <section class="venue-list">
      <div v-if="loading" class="loading">加载中...</div>
      <div
        v-for="venue in venues"
        :key="venue.id"
        class="venue-card"
        @click="goDetail(venue.id)"
      >
        <h3>{{ venue.venueName }}</h3>
        <p>📍 {{ venue.address }}</p>
        <p>🏷️ {{ venue.examType }}</p>
        <p>🅿️ 可预约：{{ venue.availableSlots }}/{{ venue.totalSlots }}</p>
        <p>📞 {{ venue.contactPhone || '暂无' }}</p>
        <button class="primary">查看详情 →</button>
      </div>
      <p v-if="!loading && venues.length === 0" class="empty">暂无考场信息</p>
    </section>
  </main>
</template>

<style scoped>
.venue-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
}

.filter-bar input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.filter-bar select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button.primary {
  padding: 8px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button.primary:hover {
  background: #40a9ff;
}

.venue-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.venue-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.venue-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #1890ff;
}

.venue-card h3 {
  margin: 0 0 10px 0;
}

.venue-card p {
  margin: 6px 0;
  color: #666;
}

.loading,
.empty {
  text-align: center;
  color: #999;
  padding: 50px;
}
</style>
