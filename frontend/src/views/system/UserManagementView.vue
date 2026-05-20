<script setup lang="tsx">
import { ref, onMounted, reactive } from "vue";
import { message } from "@/utils/message";
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  updateUserStatus,
  resetUserPassword
} from "@/api/userManagement";

defineOptions({
  name: "UserManagement"
});

const searchForm = reactive({
  username: "",
  email: ""
});

const dataList = ref([]);
const loading = ref(false);
const pagination = reactive({
  total: 0,
  pageSize: 10,
  currentPage: 1,
  background: true
});

const columns: TableColumnList = [
  { label: "ID", prop: "id", width: 80 },
  { label: "用户名", prop: "username", minWidth: 120 },
  { label: "邮箱", prop: "email", minWidth: 150 },
  {
    label: "角色",
    prop: "role",
    minWidth: 150
  },
  { label: "创建时间", prop: "createTime", minWidth: 160 },
  {
    label: "状态",
    prop: "status",
    minWidth: 100,
    cellRenderer: ({ row }) => (
      <el-switch
        v-model={row.status}
        active-value={1}
        inactive-value={0}
        onChange={val => handleStatusChange(row, val)}
      />
    )
  },
  { label: "操作", width: 220, slot: "operation", fixed: "right" }
];

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getUserList({
      page: pagination.currentPage,
      size: pagination.pageSize,
      username: searchForm.username || undefined,
      email: searchForm.email || undefined
    });
    if (res?.success) {
      // 适配后端分页数据结果
      dataList.value = res.data.list || res.data.content || res.data || [];
      pagination.total =
        res.data.total || res.data.totalElements || res.data.length || 0;
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const resetSearch = () => {
  searchForm.username = "";
  searchForm.email = "";
  fetchData();
};

const onCurrentChange = (val: number) => {
  pagination.currentPage = val;
  fetchData();
};

const onSizeChange = (val: number) => {
  pagination.pageSize = val;
  fetchData();
};

const handleStatusChange = async (row: any, val: string | number | boolean) => {
  const nextStatus = val === true || val === 1 || val === "1" ? 1 : 0;
  const previousStatus = nextStatus === 1 ? 0 : 1;
  row.status = nextStatus;
  try {
    const res = await updateUserStatus(row.id, nextStatus);
    if (res.success) {
      message("状态更新成功", { type: "success" });
    } else {
      row.status = previousStatus;
    }
  } catch (e) {
    row.status = previousStatus;
  }
};

const handleDelete = async (row: any) => {
  const res = await deleteUser(row.id);
  if (res.success) {
    message("删除成功", { type: "success" });
    fetchData();
  }
};

const handleResetPassword = async (row: any) => {
  const res = await resetUserPassword(row.id, "123456"); // 简单默认或者通过弹窗
  if (res.success) {
    message("密码已重置为: 123456", { type: "success" });
  }
};

// 弹窗逻辑
const dialogVisible = ref(false);
const dialogType = ref<"add" | "edit">("add");
const formRef = ref();
const formData = reactive({
  id: undefined,
  username: "",
  nickname: "",
  email: "",
  password: "",
  role: ""
});

const openDialog = (type: "add" | "edit", row?: any) => {
  dialogType.value = type;
  if (type === "edit" && row) {
    formData.id = row.id;
    formData.username = row.username;
    formData.nickname = row.nickname || "";
    formData.email = row.email;
    formData.role = row.role || "";
    formData.password = "";
  } else {
    formData.id = undefined;
    formData.username = "";
    formData.nickname = "";
    formData.email = "";
    formData.password = "";
    formData.role = "student";
  }
  dialogVisible.value = true;
};

const closeDialog = () => {
  dialogVisible.value = false;
  formRef.value?.resetFields();
};

const submitForm = async () => {
  await formRef.value?.validate(async valid => {
    if (valid) {
      let res;
      if (dialogType.value === "add") {
        res = await createUser(formData);
      } else {
        res = await updateUser(formData.id!, formData);
      }
      if (res.success) {
        message(dialogType.value === "add" ? "新增成功" : "编辑成功", {
          type: "success"
        });
        closeDialog();
        fetchData();
      }
    }
  });
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="main user-mgmt-page">
    <el-card shadow="never" class="mb-4">
      <el-form :inline="true" :model="searchForm" class="bg-bg_color">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable class="w-40!" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable class="w-40!" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="border-none!">
      <div class="mb-4 flex gap-2">
        <el-button type="primary" @click="openDialog('add')">新增用户</el-button>
      </div>

      <pure-table stripe row-key="id" :data="dataList" :columns="columns" :loading="loading" :pagination="pagination"
        @page-current-change="onCurrentChange" @page-size-change="onSizeChange">
        <template #operation="{ row }">
          <el-button link type="primary" size="small" @click="openDialog('edit', row)">编辑</el-button>
          <el-button link type="warning" size="small" @click="handleResetPassword(row)">重置密码</el-button>
          <el-popconfirm title="确定要删除该用户吗？" @confirm="handleDelete(row)">
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </pure-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogType === 'add' ? '新增用户' : '编辑用户'" width="500px"
      @close="closeDialog">
      <el-form ref="formRef" :model="formData" label-width="80px">
        <el-form-item label="用户名" prop="username" :rules="[{ required: true, message: '请输入用户名' }]">
          <el-input v-model="formData.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="默认使用用户名" />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password"
          :rules="[{ required: true, message: '请输入密码' }]">
          <el-input v-model="formData.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role" placeholder="请选择角色" class="w-full">
            <el-option label="管理员" value="admin" />
            <el-option label="教练" value="coach" />
            <el-option label="学员" value="student" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
