<script setup lang="ts">
import { ElPagination } from 'element-plus'

defineProps<{
  page: number
  totalPages: number
}>()

const emit = defineEmits<{
  change: [page: number]
}>()
</script>

<template>
  <div class="app-pagination">
    <span class="page-ratio">{{ page }} / {{ totalPages }}</span>
    <ElPagination
      background
      :current-page="page"
      :page-size="1"
      :pager-count="5"
      :total="totalPages"
      layout="prev, pager, next, jumper"
      @current-change="emit('change', $event)"
    />
  </div>
</template>
