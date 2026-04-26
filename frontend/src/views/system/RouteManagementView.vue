<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import {
  createAdminRoute,
  deleteAdminRoute,
  getAdminRoutes,
  updateAdminRoute,
  type AppRoutePayload
} from "@/api/admin";
import type { AppRoute } from "@/types";

const routes = ref<AppRoute[]>([]);
const loading = ref(false);
const error = ref("");
const editingId = ref<number | null>(null);

const form = reactive<AppRoutePayload>({
  path: "",
  name: "",
  title: "",
  parentId: null,
  component: "",
  icon: "",
  rankNo: 0,
  enabled: true
});

const parentOptions = computed(() =>
  routes.value.filter(item => item.id !== editingId.value)
);

onMounted(loadRoutes);

async function loadRoutes() {
  loading.value = true;
  error.value = "";
  try {
    routes.value = await getAdminRoutes();
  } catch (err) {
    error.value = err instanceof Error ? err.message : "路由列表加载失败";
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  editingId.value = null;
  Object.assign(form, {
    path: "",
    name: "",
    title: "",
    parentId: null,
    component: "",
    icon: "",
    rankNo: 0,
    enabled: true
  });
}

function editRoute(route: AppRoute) {
  editingId.value = route.id;
  Object.assign(form, {
    path: route.path,
    name: route.name,
    title: route.title,
    parentId: route.parentId,
    component: route.component,
    icon: route.icon,
    rankNo: route.rankNo,
    enabled: route.enabled
  });
}

async function saveRoute() {
  loading.value = true;
  error.value = "";
  try {
    if (editingId.value) {
      await updateAdminRoute(editingId.value, form);
    } else {
      await createAdminRoute(form);
    }
    resetForm();
    await loadRoutes();
  } catch (err) {
    error.value = err instanceof Error ? err.message : "路由保存失败";
  } finally {
    loading.value = false;
  }
}

async function removeRoute(route: AppRoute) {
  if (!window.confirm(`确认删除路由「${route.title}」？`)) return;
  loading.value = true;
  error.value = "";
  try {
    await deleteAdminRoute(route.id);
    await loadRoutes();
  } catch (err) {
    error.value = err instanceof Error ? err.message : "路由删除失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="system-page">
    <div v-if="error" class="message error">{{ error }}</div>

    <section class="system-panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">Route Management</p>
          <h2>路由管理</h2>
        </div>
        <button class="ghost" @click="resetForm">新建路由</button>
      </div>

      <form class="system-form" @submit.prevent="saveRoute">
        <label>
          路径
          <input v-model="form.path" placeholder="/operation/system/routes" required />
        </label>
        <label>
          路由名
          <input v-model="form.name" placeholder="RouteManagement" required />
        </label>
        <label>
          菜单标题
          <input v-model="form.title" placeholder="路由管理" required />
        </label>
        <label>
          父级
          <select v-model="form.parentId">
            <option :value="null">无父级</option>
            <option v-for="item in parentOptions" :key="item.id" :value="item.id">
              {{ item.title }}
            </option>
          </select>
        </label>
        <label>
          组件标识
          <input v-model="form.component" placeholder="SystemRouteView" />
        </label>
        <label>
          图标
          <input v-model="form.icon" placeholder="ep/menu" />
        </label>
        <label>
          排序
          <input v-model.number="form.rankNo" min="0" type="number" />
        </label>
        <label class="switch-line">
          <input v-model="form.enabled" type="checkbox" />
          启用
        </label>
        <div class="system-form-actions">
          <button class="primary" :disabled="loading" type="submit">
            {{ editingId ? "保存修改" : "新增路由" }}
          </button>
          <button class="ghost" type="button" @click="resetForm">取消</button>
        </div>
      </form>
    </section>

    <section class="system-panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">Route Table</p>
          <h2>路由清单</h2>
        </div>
        <span class="pill">{{ routes.length }} 条</span>
      </div>

      <div class="system-table">
        <div class="system-table-row system-table-head">
          <span>标题</span>
          <span>路径</span>
          <span>路由名</span>
          <span>排序</span>
          <span>状态</span>
          <span>操作</span>
        </div>
        <div v-for="route in routes" :key="route.id" class="system-table-row">
          <span>{{ route.title }}</span>
          <span>{{ route.path }}</span>
          <span>{{ route.name }}</span>
          <span>{{ route.rankNo }}</span>
          <span>{{ route.enabled ? "启用" : "停用" }}</span>
          <span class="table-actions">
            <button class="ghost" @click="editRoute(route)">编辑</button>
            <button class="danger" @click="removeRoute(route)">删除</button>
          </span>
        </div>
      </div>
    </section>
  </div>
</template>
