<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>관리자 대시보드</h1>
      <p class="subtitle">사용자 및 시스템 관리</p>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid" v-if="stats">
      <div class="stat-card">
        <div class="stat-icon users">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-label">전체 사용자</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon active">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
            <polyline points="22 4 12 14.01 9 11.01"></polyline>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.activeUsers }}</div>
          <div class="stat-label">활성 사용자</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon admin">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
            <path d="M2 17l10 5 10-5"></path>
            <path d="M2 12l10 5 10-5"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.adminUsers + stats.developerUsers }}</div>
          <div class="stat-label">관리자</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon deleted">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.deletedUsers }}</div>
          <div class="stat-label">탈퇴 사용자</div>
        </div>
      </div>
    </div>

    <!-- 탭 메뉴 -->
    <div class="tabs">
      <button
        class="tab"
        :class="{ active: activeTab === 'users' }"
        @click="activeTab = 'users'"
      >
        활성 사용자
      </button>
      <button
        class="tab"
        :class="{ active: activeTab === 'deleted' }"
        @click="activeTab = 'deleted'; loadDeletedUsers()"
      >
        탈퇴 사용자
      </button>
    </div>

    <!-- 사용자 목록 -->
    <div class="user-table-container">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>로딩 중...</p>
      </div>

      <div v-else-if="error" class="error-message">
        {{ error }}
      </div>

      <table v-else class="user-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>아이디</th>
            <th>이름</th>
            <th>이메일</th>
            <th>권한</th>
            <th>상태</th>
            <th>가입일</th>
            <th v-if="activeTab === 'users'">작업</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in currentUsers" :key="user.userId">
            <td>{{ user.userId }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.firstName }} {{ user.lastName }}</td>
            <td>{{ user.email }}</td>
            <td>
              <span class="role-badge" :class="user.role.toLowerCase()">
                {{ getRoleLabel(user.role) }}
              </span>
            </td>
            <td>
              <span class="status-badge" :class="user.isActive ? 'active' : 'inactive'">
                {{ user.isActive ? '활성' : '비활성' }}
              </span>
            </td>
            <td>{{ formatDate(user.createdAt) }}</td>
            <td v-if="activeTab === 'users'" class="actions">
              <button
                class="btn-action"
                @click="openRoleModal(user)"
                :disabled="user.username === currentUser?.username"
              >
                권한 변경
              </button>
              <button
                class="btn-action"
                :class="user.isActive ? 'danger' : 'success'"
                @click="toggleUserStatus(user)"
                :disabled="user.username === currentUser?.username"
              >
                {{ user.isActive ? '비활성화' : '활성화' }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 권한 변경 모달 -->
    <div v-if="showRoleModal" class="modal-overlay" @click="closeRoleModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>권한 변경</h2>
          <button class="modal-close" @click="closeRoleModal">×</button>
        </div>
        <div class="modal-body">
          <p><strong>{{ selectedUser?.username }}</strong> 사용자의 권한을 변경합니다.</p>
          <div class="role-options">
            <label class="role-option">
              <input type="radio" v-model="newRole" value="USER" />
              <span>일반 사용자 (USER)</span>
            </label>
            <label class="role-option">
              <input type="radio" v-model="newRole" value="DEVELOPER" />
              <span>개발자 (DEVELOPER)</span>
            </label>
            <label class="role-option">
              <input type="radio" v-model="newRole" value="ADMIN" />
              <span>관리자 (ADMIN)</span>
            </label>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeRoleModal">취소</button>
          <button class="btn-primary" @click="updateUserRole">변경</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth';
import apiClient from '@/api/client';
import { API_ENDPOINTS } from '@/constants';
import type { UserResponse, UserStatsResponse, UserRole } from '@/types/domain.types';

export default defineComponent({
  name: 'AdminDashboard',
  setup() {
    const { currentUser } = useAuth();
    const loading = ref(false);
    const error = ref('');
    const activeTab = ref<'users' | 'deleted'>('users');

    const users = ref<UserResponse[]>([]);
    const deletedUsers = ref<UserResponse[]>([]);
    const stats = ref<UserStatsResponse | null>(null);

    const showRoleModal = ref(false);
    const selectedUser = ref<UserResponse | null>(null);
    const newRole = ref<UserRole>('USER');

    const currentUsers = computed(() => {
      return activeTab.value === 'users' ? users.value : deletedUsers.value;
    });

    const loadStats = async () => {
      try {
        const response = await apiClient.get(API_ENDPOINTS.ADMIN.STATS);
        stats.value = response.data.data;
      } catch (err: any) {
        console.error('Failed to load stats:', err);
      }
    };

    const loadUsers = async () => {
      loading.value = true;
      error.value = '';
      try {
        const response = await apiClient.get(API_ENDPOINTS.ADMIN.USERS);
        users.value = response.data.data;
      } catch (err: any) {
        error.value = err.response?.data?.error?.message || '사용자 목록을 불러오는데 실패했습니다.';
      } finally {
        loading.value = false;
      }
    };

    const loadDeletedUsers = async () => {
      if (deletedUsers.value.length > 0) return;

      loading.value = true;
      error.value = '';
      try {
        const response = await apiClient.get(API_ENDPOINTS.ADMIN.DELETED_USERS);
        deletedUsers.value = response.data.data;
      } catch (err: any) {
        error.value = err.response?.data?.error?.message || '탈퇴 사용자 목록을 불러오는데 실패했습니다.';
      } finally {
        loading.value = false;
      }
    };

    const openRoleModal = (user: UserResponse) => {
      selectedUser.value = user;
      newRole.value = user.role;
      showRoleModal.value = true;
    };

    const closeRoleModal = () => {
      showRoleModal.value = false;
      selectedUser.value = null;
      newRole.value = 'USER';
    };

    const updateUserRole = async () => {
      if (!selectedUser.value) return;

      try {
        await apiClient.put(
          API_ENDPOINTS.ADMIN.USER_ROLE(selectedUser.value.username),
          null,
          {
            params: { role: newRole.value }
          }
        );

        alert('권한이 변경되었습니다.');
        closeRoleModal();
        await loadUsers();
        await loadStats();
      } catch (err: any) {
        alert(err.response?.data?.error?.message || '권한 변경에 실패했습니다.');
      }
    };

    const toggleUserStatus = async (user: UserResponse) => {
      const action = user.isActive ? '비활성화' : '활성화';
      if (!confirm(`${user.username} 사용자를 ${action}하시겠습니까?`)) return;

      try {
        await apiClient.put(
          API_ENDPOINTS.ADMIN.USER_ACTIVE(user.username),
          null,
          {
            params: { isActive: !user.isActive }
          }
        );

        alert(`사용자가 ${action}되었습니다.`);
        await loadUsers();
        await loadStats();
      } catch (err: any) {
        alert(err.response?.data?.error?.message || `${action}에 실패했습니다.`);
      }
    };

    const getRoleLabel = (role: UserRole): string => {
      const labels: Record<UserRole, string> = {
        USER: '사용자',
        ADMIN: '관리자',
        DEVELOPER: '개발자',
      };
      return labels[role] || role;
    };

    const formatDate = (dateString: string): string => {
      const date = new Date(dateString);
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      });
    };

    onMounted(() => {
      loadStats();
      loadUsers();
    });

    return {
      currentUser,
      loading,
      error,
      activeTab,
      users,
      deletedUsers,
      stats,
      currentUsers,
      showRoleModal,
      selectedUser,
      newRole,
      loadDeletedUsers,
      openRoleModal,
      closeRoleModal,
      updateUserRole,
      toggleUserStatus,
      getRoleLabel,
      formatDate,
    };
  },
});
</script>

<style scoped>
.admin-dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 40px 20px;
}

.dashboard-header {
  margin-bottom: 32px;
}

.dashboard-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #718096;
  font-size: 16px;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 28px;
  height: 28px;
}

.stat-icon.users {
  background: #e6f2ff;
  color: #3182ce;
}

.stat-icon.active {
  background: #d4f4dd;
  color: #38a169;
}

.stat-icon.admin {
  background: #fef3c7;
  color: #d69e2e;
}

.stat-icon.deleted {
  background: #fed7d7;
  color: #e53e3e;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  margin-top: 4px;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 2px solid #e2e8f0;
}

.tab {
  padding: 12px 24px;
  background: none;
  border: none;
  font-size: 16px;
  font-weight: 600;
  color: #718096;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
}

.tab:hover {
  color: #667eea;
}

.tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
}

.user-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.loading, .error-message {
  padding: 60px 20px;
  text-align: center;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  color: #e53e3e;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th {
  background: #f7fafc;
  padding: 16px;
  text-align: left;
  font-weight: 600;
  color: #2d3748;
  font-size: 14px;
  border-bottom: 2px solid #e2e8f0;
}

.user-table td {
  padding: 16px;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
}

.user-table tbody tr:hover {
  background: #f7fafc;
}

.role-badge, .status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.role-badge.user {
  background: #e6f2ff;
  color: #3182ce;
}

.role-badge.admin {
  background: #fed7d7;
  color: #e53e3e;
}

.role-badge.developer {
  background: #fef3c7;
  color: #d69e2e;
}

.status-badge.active {
  background: #d4f4dd;
  color: #38a169;
}

.status-badge.inactive {
  background: #e2e8f0;
  color: #718096;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-action {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  background: #667eea;
  color: white;
}

.btn-action:hover:not(:disabled) {
  background: #5568d3;
  transform: translateY(-1px);
}

.btn-action.danger {
  background: #e53e3e;
}

.btn-action.danger:hover:not(:disabled) {
  background: #c53030;
}

.btn-action.success {
  background: #38a169;
}

.btn-action.success:hover:not(:disabled) {
  background: #2f855a;
}

.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1a202c;
}

.modal-close {
  background: none;
  border: none;
  font-size: 32px;
  color: #718096;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
}

.modal-close:hover {
  color: #1a202c;
}

.modal-body {
  padding: 24px;
}

.role-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.role-option:hover {
  border-color: #667eea;
  background: #f7fafc;
}

.role-option input[type="radio"] {
  cursor: pointer;
}

.modal-footer {
  padding: 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-primary, .btn-secondary {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
}

.btn-secondary {
  background: #e2e8f0;
  color: #2d3748;
}

.btn-secondary:hover {
  background: #cbd5e0;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .user-table {
    font-size: 12px;
  }

  .user-table th,
  .user-table td {
    padding: 12px 8px;
  }

  .actions {
    flex-direction: column;
  }
}
</style>
