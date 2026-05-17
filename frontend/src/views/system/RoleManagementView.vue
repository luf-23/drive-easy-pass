<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import AppPagination from "@/components/AppPagination.vue";
import {
  createRole,
  deleteRole,
  getAdminRoutes,
  getRoles,
  updateRole,
  type RolePayload
} from "@/api/admin";
import type { AppRoute, Role } from "@/types";

type RouteTreeNode = AppRoute & {
  children?: RouteTreeNode[];
};

const roles = ref<Role[]>([]);
const routes = ref<AppRoute[]>([]);
const loading = ref(false);
const saving = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const page = ref(1);
const pageSize = 8;

const form = reactive<RolePayload>({
  code: "",
  name: "",
  description: "",
  enabled: true,
  routeIds: []
});

const enabledRoutes = computed(() => routes.value.filter(item => item.enabled));
const routeTree = computed(() => buildRouteTree(enabledRoutes.value));
const totalPages = computed(() =>
  Math.max(1, Math.ceil(roles.value.length / pageSize))
);
const pagedRoles = computed(() => {
  const start = (page.value - 1) * pageSize;
  return roles.value.slice(start, start + pageSize);
});

onMounted(loadData);

watch(totalPages, value => {
  if (page.value > value) page.value = value;
});

async function loadData() {
  loading.value = true;
  try {
    const [nextRoles, nextRoutes] = await Promise.all([
      getRoles(),
      getAdminRoutes()
    ]);
    roles.value = nextRoles;
    routes.value = nextRoutes;
  } catch (error) {
    ElMessage.error(
      error instanceof Error ? error.message : "角色数据加载失败"
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
    code: "",
    name: "",
    description: "",
    enabled: true,
    routeIds: []
  });
}

function editRole(role: Role) {
  editingId.value = role.id;
  Object.assign(form, {
    code: role.code,
    name: role.name,
    description: role.description,
    enabled: role.enabled,
    routeIds: [...role.routeIds]
  });
  dialogVisible.value = true;
}

async function saveRole() {
  if (!form.code.trim() || !form.name.trim()) {
    ElMessage.warning("请填写角色编码和角色名称");
    return;
  }

  saving.value = true;
  try {
    const payload: RolePayload = {
      ...form,
      code: form.code.trim(),
      name: form.name.trim(),
      description: form.description.trim(),
      routeIds: [...form.routeIds]
    };

    if (editingId.value) {
      await updateRole(editingId.value, payload);
      ElMessage.success("角色已更新");
    } else {
      await createRole(payload);
      ElMessage.success("角色已新增");
    }

    dialogVisible.value = false;
    resetForm();
    await loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "角色保存失败");
  } finally {
    saving.value = false;
  }
}

async function removeRole(role: Role) {
  try {
    await ElMessageBox.confirm(`确认删除角色「${role.name}」？`, "删除确认", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消"
    });
    await deleteRole(role.id);
    ElMessage.success("角色已删除");
    await loadData();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(error instanceof Error ? error.message : "角色删除失败");
    }
  }
}

function routeTitles(role: Role) {
  const titleMap = new Map(routes.value.map(route => [route.id, route.title]));
  const titles = role.routeIds
    .map(id => titleMap.get(id))
    .filter((title): title is string => Boolean(title));

  return titles.length ? titles.join("、") : "未分配";
}

function buildRouteTree(source: AppRoute[]) {
  const nodeMap = new Map<number, RouteTreeNode>();
  const roots: RouteTreeNode[] = [];

  source.forEach(route => {
    nodeMap.set(route.id, { ...route, children: [] });
  });

  nodeMap.forEach(node => {
    if (node.parentId != null && nodeMap.has(node.parentId)) {
      nodeMap.get(node.parentId)?.children?.push(node);
    } else {
      roots.push(node);
    }
  });

  return roots;
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
            <h2>角色管理</h2>
            <p>维护角色基础信息和可访问路由。</p>
          </div>
          <el-space>
            <el-button :loading="loading" @click="loadData">刷新</el-button>
            <el-button type="primary" @click="openCreate">新增角色</el-button>
          </el-space>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="pagedRoles"
        row-key="id"
        border
        height="100%"
      >
        <el-table-column prop="name" label="角色名称" min-width="140" />
        <el-table-column prop="code" label="角色编码" min-width="130" />
        <el-table-column prop="description" label="描述" min-width="180">
          <template #default="{ row }">
            {{ row.description || "暂无描述" }}
          </template>
        </el-table-column>
        <el-table-column label="可访问路由" min-width="260">
          <template #default="{ row }">
            <el-text line-clamp="2">{{ routeTitles(row) }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'">
              {{ row.enabled ? "启用" : "停用" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="editRole(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="removeRole(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <AppPagination
        v-if="roles.length > pageSize"
        :page="page"
        :total-pages="totalPages"
        @change="goPage"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑角色' : '新增角色'"
      width="680px"
      @closed="resetForm"
    >
      <el-form :model="form" label-width="92px">
        <el-form-item label="角色编码" required>
          <el-input v-model="form.code" placeholder="admin" />
        </el-form-item>
        <el-form-item label="角色名称" required>
          <el-input v-model="form.name" placeholder="管理员" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="角色职责说明"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="form.enabled"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        <el-form-item label="访问路由">
          <el-tree
            v-model:checked-keys="form.routeIds"
            :data="routeTree"
            node-key="id"
            show-checkbox
            default-expand-all
            :props="{ label: 'title', children: 'children' }"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRole">
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

:deep(.el-tree) {
  width: 100%;
  padding: 8px 0;
}
</style>
