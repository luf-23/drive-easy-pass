<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import {
  createRole,
  deleteRole,
  getAdminRoutes,
  getRoles,
  updateRole,
  type RolePayload
} from "@/api/admin";
import type { AppRoute, Role } from "@/types";

const roles = ref<Role[]>([]);
const routes = ref<AppRoute[]>([]);
const loading = ref(false);
const error = ref("");
const editingId = ref<number | null>(null);

const form = reactive<RolePayload>({
  code: "",
  name: "",
  description: "",
  enabled: true,
  routeIds: []
});

const enabledRoutes = computed(() => routes.value.filter(item => item.enabled));

onMounted(loadData);

async function loadData() {
  loading.value = true;
  error.value = "";
  try {
    const [nextRoles, nextRoutes] = await Promise.all([getRoles(), getAdminRoutes()]);
    roles.value = nextRoles;
    routes.value = nextRoutes;
  } catch (err) {
    error.value = err instanceof Error ? err.message : "角色数据加载失败";
  } finally {
    loading.value = false;
  }
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
}

async function saveRole() {
  loading.value = true;
  error.value = "";
  try {
    if (editingId.value) {
      await updateRole(editingId.value, form);
    } else {
      await createRole(form);
    }
    resetForm();
    await loadData();
  } catch (err) {
    error.value = err instanceof Error ? err.message : "角色保存失败";
  } finally {
    loading.value = false;
  }
}

async function removeRole(role: Role) {
  if (!window.confirm(`确认删除角色「${role.name}」？`)) return;
  loading.value = true;
  error.value = "";
  try {
    await deleteRole(role.id);
    await loadData();
  } catch (err) {
    error.value = err instanceof Error ? err.message : "角色删除失败";
  } finally {
    loading.value = false;
  }
}

function routeTitles(role: Role) {
  const titleMap = new Map(routes.value.map(route => [route.id, route.title]));
  return role.routeIds.map(id => titleMap.get(id)).filter(Boolean).join("、") || "未分配";
}
</script>

<template>
  <div class="system-page">
    <div v-if="error" class="message error">{{ error }}</div>

    <section class="system-panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">Role Management</p>
          <h2>角色管理</h2>
        </div>
        <button class="ghost" @click="resetForm">新建角色</button>
      </div>

      <form class="system-form role-form" @submit.prevent="saveRole">
        <label>
          角色编码
          <input v-model="form.code" placeholder="admin" required />
        </label>
        <label>
          角色名称
          <input v-model="form.name" placeholder="管理员" required />
        </label>
        <label class="wide-field">
          描述
          <input v-model="form.description" placeholder="角色职责说明" />
        </label>
        <label class="switch-line">
          <input v-model="form.enabled" type="checkbox" />
          启用
        </label>

        <div class="route-checks wide-field">
          <strong>可访问路由</strong>
          <label v-for="route in enabledRoutes" :key="route.id">
            <input v-model="form.routeIds" :value="route.id" type="checkbox" />
            {{ route.title }}
          </label>
        </div>

        <div class="system-form-actions">
          <button class="primary" :disabled="loading" type="submit">
            {{ editingId ? "保存修改" : "新增角色" }}
          </button>
          <button class="ghost" type="button" @click="resetForm">取消</button>
        </div>
      </form>
    </section>

    <section class="system-panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">Role Table</p>
          <h2>角色清单</h2>
        </div>
        <span class="pill">{{ roles.length }} 个角色</span>
      </div>

      <div class="role-list">
        <article v-for="role in roles" :key="role.id" class="role-item">
          <div>
            <span>{{ role.enabled ? "启用" : "停用" }}</span>
            <h3>{{ role.name }} / {{ role.code }}</h3>
            <p>{{ role.description || "暂无描述" }}</p>
            <small>路由：{{ routeTitles(role) }}</small>
          </div>
          <div class="table-actions">
            <button class="ghost" @click="editRole(role)">编辑</button>
            <button class="danger" @click="removeRole(role)">删除</button>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>
