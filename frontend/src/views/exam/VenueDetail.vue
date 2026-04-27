<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { request } from '../../services/api'
import type { ExamVenueDTO, ExamRouteDTO, ExamScheduleDTO } from '../../types'

const route = useRoute()
const router = useRouter()
const venueId = Number(route.params.id)

const venue = ref<ExamVenueDTO | null>(null)
const routes = ref<ExamRouteDTO[]>([])
const schedules = ref<ExamScheduleDTO[]>([])
const loading = ref(false)

const loadDetail = async () => {
  loading.value = true
  try {
    const [vRes, rRes, sRes] = await Promise.all([
      request<ExamVenueDTO>(`/api/exam/venues/${venueId}`),
      request<ExamRouteDTO[]>(`/api/exam/routes/venue/${venueId}`),
      request<ExamScheduleDTO[]>(`/api/exam/venues/${venueId}/schedules`),
    ])
    venue.value = vRes
    routes.value = rRes
    schedules.value = sRes
  } catch (err) {
    console.error('加载考场详情失败', err)
  } finally {
    loading.value = false
  }
}

const reserve = async (scheduleId: number) => {
  try {
    await request(`/api/exam/reserve`, {
      method: 'POST',
      body: JSON.stringify({ scheduleId }),
    })
    if (venue.value?.examType) {
      localStorage.setItem('reservedExamType', venue.value.examType)
    }
    alert('预约成功！')
    loadDetail()
  } catch (err) {
    alert(err instanceof Error ? err.message : '预约失败')
  }
}
const cancelReserve = async (scheduleId: number) => {
  if (!confirm('确定取消该场次的预约吗？')) return
  try {
    const reservations = await request<any[]>(`/api/exam/my-reservations`)
    const reservation = reservations.find(
      (r: any) => r.scheduleId === scheduleId && r.status === 'RESERVED'
    )
    if (!reservation) {
      alert('未找到该场次的预约记录')
      return
    }
    await request(`/api/exam/cancel/${reservation.id}`, { method: 'POST' })

    // 检查是否还有其他预约
    const remaining = reservations.filter(
      (r: any) => r.status === 'RESERVED' && r.id !== reservation.id
    )
    if (remaining.length > 0) {
      localStorage.setItem('reservedExamType', remaining[0].examType)
    } else {
      localStorage.removeItem('reservedExamType')
    }

    alert('取消成功！')
    location.reload()
  } catch (err) {
    alert(err instanceof Error ? err.message : '取消失败')
  }
}

onMounted(loadDetail)
</script>

<template>
  <main class="detail-page">
    <button class="btn-back" @click="router.back()">← 返回</button>

    <div v-if="loading" class="loading">加载中...</div>

    <template v-if="venue">
      <section class="venue-info">
        <h1>{{ venue.venueName }}</h1>
        <p>📍 {{ venue.address }}</p>
        <p>🏷️ 考试类型：{{ venue.examType }}</p>
        <p>🅿️ 考位：{{ venue.availableSlots }}/{{ venue.totalSlots }}</p>
        <p>📞 电话：{{ venue.contactPhone || '暂无' }}</p>
        <p>🕐 营业时间：{{ venue.businessHours || '暂无' }}</p>
        <p>🏢 设施：{{ venue.facilities || '暂无' }}</p>
        <p>📝 线路描述：{{ venue.routeDescription || '暂无' }}</p>
      </section>

      <section v-if="routes.length > 0" class="routes-section">
        <h2>🗺️ 考试线路图</h2>
        <div v-for="route in routes" :key="route.id" class="route-card">
          <h3>{{ route.routeName }} ({{ route.routeNumber }})</h3>
          <p>{{ route.description }}</p>
          <p>起点：{{ route.startPoint }} → 终点：{{ route.endPoint }}</p>
          <p>距离：{{ route.distance }}m | 难度：{{ route.difficulty }}</p>
          <p>要点：{{ route.points }}</p>
        </div>
      </section>

      <section class="schedules-section">
        <h2>📅 可预约考试时间</h2>
        <div v-for="s in schedules" :key="s.id" class="schedule-card">
          <div class="schedule-info">
            <strong>{{ s.examDate }}</strong>
            <span>{{ s.startTime }} - {{ s.endTime }}</span>
            <span>{{ s.examType }}</span>
            <span class="slots">剩余：{{ s.availableSlots }}/{{ s.totalSlots }}</span>
          </div>
          <div style="display: flex; gap: 8px;">
            <button
              style="padding: 8px 20px; background: #1890ff; color: white; border: none; border-radius: 4px; cursor: pointer;"
              :disabled="s.availableSlots <= 0"
              @click="reserve(s.id)"
            >
              {{ s.availableSlots > 0 ? '预约' : '已满' }}
            </button>
            <button
              style="padding: 8px 20px; background: #ff4d4f; color: white; border: none; border-radius: 4px; cursor: pointer;"
              @click="cancelReserve(s.id)"
            >
              取消
            </button>
          </div>
        </div>
        <p v-if="schedules.length === 0" class="empty">暂无可用考试安排</p>
      </section>
    </template>
  </main>
</template>

<style scoped>
.detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.btn-back {
  padding: 8px 16px;
  background: #f0f0f0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-bottom: 20px;
}

.venue-info {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.venue-info p {
  margin: 8px 0;
  color: #555;
}

.routes-section,
.schedules-section {
  margin-top: 24px;
}

.route-card,
.schedule-card {
  border: 1px solid #eee;
  padding: 15px;
  border-radius: 8px;
  margin: 10px 0;
}

.schedule-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.schedule-info {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.slots {
  color: #1890ff;
  font-weight: bold;
}

button.primary {
  padding: 8px 20px;
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button.primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.loading,
.empty {
  text-align: center;
  color: #999;
  padding: 50px;
}
</style>
