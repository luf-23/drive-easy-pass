<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AppPagination from "@/components/AppPagination.vue";
import {
  createAdminRoute,
  deleteAdminRoute,
  getAdminRoutes,
  updateAdminRoute,
  type AppRoutePayload
} from "@/api/admin";
import type { AppRoute } from "@/types";

type RouteForm = AppRoutePayload;

const routes = ref<AppRoute[]>([]);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const page = ref(1);
const pageSize = 8;

const form = reactive<RouteForm>({
  path: "",
  name: "",
  title: "",
  parentId: null,
  redirect: "",
  component: "",
  icon: "",
  rankNo: 0,
  enabled: true
});

const parentOptions = computed(() =>
  routes.value.filter(item => item.id !== editingId.value)
);
const totalPages = computed(() =>
  Math.max(1, Math.ceil(routes.value.length / pageSize))
);
const pagedRoutes = computed(() => {
  const start = (page.value - 1) * pageSize;
  return routes.value.slice(start, start + pageSize);
});

onMounted(loadRoutes);

watch(totalPages, value => {
  if (page.value > value) page.value = value;
});

async function loadRoutes() {
  loading.value = true;
  try {
    routes.value = await getAdminRoutes();
  } catch (error) {
    ElMessage.error(
      error instanceof Error ? error.message : "路由列表加载失败"
    );
  } finally {
    loading.value = false;
  }
}

function openCreate() {
  resetForm();
  dialogVisible.value = true;
}

function resetForm() {
  editingId.value = null;
  Object.assign(form, {
    path: "",
    name: "",
    title: "",
    parentId: null,
    redirect: "",
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
    redirect: route.redirect,
    component: route.component,
    icon: route.icon,
    rankNo: route.rankNo,
    enabled: route.enabled
  });
  dialogVisible.value = true;
}

async function saveRoute() {
  if (!form.path.trim() || !form.name.trim() || !form.title.trim()) {
    ElMessage.warning("请填写路径、路由名和菜单标题");
    return;
  }

  saving.value = true;
  try {
    const payload: RouteForm = {
      ...form,
      path: form.path.trim(),
      name: form.name.trim(),
      title: form.title.trim(),
      redirect: form.redirect.trim(),
      component: form.component.trim(),
      icon: form.icon.trim()
    };

    if (editingId.value) {
      await updateAdminRoute(editingId.value, payload);
      ElMessage.success("路由已更新");
    } else {
      await createAdminRoute(payload);
      ElMessage.success("路由已新增");
    }

    dialogVisible.value = false;
    resetForm();
    await loadRoutes();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "路由保存失败");
  } finally {
    saving.value = false;
  }
}

async function removeRoute(route: AppRoute) {
  try {
    await ElMessageBox.confirm(`确认删除路由「${route.title}」？`, "删除确认", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消"
    });
    await deleteAdminRoute(route.id);
    ElMessage.success("路由已删除");
    await loadRoutes();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error instanceof Error ? error.message : "路由删除失败");
    }
  }
}

function parentTitle(parentId: number | null) {
  if (parentId == null) return "无";
  return (
    routes.value.find(item => item.id === parentId)?.title ?? `#${parentId}`
  );
}
function goPage(value: number) {
  page.value = Math.min(Math.max(value, 1), totalPages.value);
}
</script>

<template>
  <div class="system-manage-page">
    <el-card class="management-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <h2>路由管理</h2>
            <p>维护后台菜单和页面路由。</p>
          </div>
          <el-space>
            <el-button :loading="loading" @click="loadRoutes">刷新</el-button>
            <el-button type="primary" @click="openCreate">新增路由</el-button>
          </el-space>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="pagedRoutes"
        row-key="id"
        border
        height="100%"
      >
        <el-table-column prop="title" label="菜单标题" min-width="150" />
        <el-table-column prop="path" label="路径" min-width="220" />
        <el-table-column prop="name" label="路由名" min-width="160" />
        <el-table-column label="父级" min-width="130">
          <template #default="{ row }">
            {{ parentTitle(row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="component" label="组件" min-width="170" />
        <el-table-column prop="rankNo" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? "启用" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="editRoute(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="removeRoute(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <AppPagination
        v-if="routes.length > pageSize"
        :page="page"
        :total-pages="totalPages"
        @change="goPage"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑路由' : '新增路由'"
      width="640px"
      @closed="resetForm"
    >
      <el-form :model="form" label-width="92px">
        <el-form-item label="路径" required>
          <el-input
            v-model="form.path"
            placeholder="/operation/system/routes"
          />
        </el-form-item>
        <el-form-item label="路由名" required>
          <el-input v-model="form.name" placeholder="RouteManagement" />
        </el-form-item>
        <el-form-item label="菜单标题" required>
          <el-input v-model="form.title" placeholder="路由管理" />
        </el-form-item>
        <el-form-item label="父级">
          <el-select v-model="form.parentId" clearable placeholder="无父级">
            <el-option
              v-for="item in parentOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="重定向">
          <el-input
            v-model="form.redirect"
            placeholder="/operation/dashboard"
          />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="form.component" placeholder="RouteManagement" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="ep/menu" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.rankNo" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.enabled"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRoute">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.system-manage-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.management-card {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  min-height: 0;
}

:deep(.management-card > .el-card__body) {
  display: flex;
  flex: 1 1 auto;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

:deep(.el-table) {
  flex: 1 1 auto;
  min-height: 0;
}

.card-header {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.card-header p {
  margin: 4px 0 0;
  color: var(--el-text-color-secondary);
}

:deep(.el-select) {
  width: 100%;
}
</style>
