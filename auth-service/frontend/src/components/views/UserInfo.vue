<template>
  <div class="userinfo-container">
    <div class="userinfo-card">
      <div class="userinfo-header">
        <h1 class="userinfo-title">My Profile</h1>
        <p class="userinfo-subtitle">내 정보</p>
      </div>

      <div class="action-bar">
        <div class="user-count">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
          <span>회원 정보</span>
        </div>
        <button @click="downloadExcel()" type="button" class="btn-download">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
            <polyline points="7 10 12 15 17 10"></polyline>
            <line x1="12" y1="15" x2="12" y2="3"></line>
          </svg>
          <span>엑셀 다운로드</span>
        </button>
      </div>

      <div class="table-container" v-if="result.length > 0">
        <div class="user-card" v-for="(user, index) in result" :key="index">
          <div class="user-card-header">
            <div class="user-avatar">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
            </div>
            <div class="user-basic-info">
              <h3>{{ user.firstName }} {{ user.lastName }}</h3>
              <p>@{{ user.nickname }}</p>
            </div>
            <div class="user-id-badge">
              ID: {{ user.username }}
            </div>
          </div>

          <div class="user-card-body">
            <div class="info-grid">
              <div class="info-item">
                <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
                <div>
                  <span class="info-label">이메일</span>
                  <span class="info-value">{{ user.email || 'N/A' }}</span>
                </div>
              </div>

              <div class="info-item">
                <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                </svg>
                <div>
                  <span class="info-label">전화번호</span>
                  <span class="info-value">{{ user.phone || 'N/A' }}</span>
                </div>
              </div>

              <div class="info-item full-width">
                <svg class="info-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                  <circle cx="12" cy="10" r="3"></circle>
                </svg>
                <div>
                  <span class="info-label">주소</span>
                  <span class="info-value">
                    {{ [user.street1, user.street2, user.city, user.state, user.country, user.zip].filter(Boolean).join(', ') || 'N/A' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="empty-state" v-else>
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
          <circle cx="9" cy="7" r="4"></circle>
          <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
          <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
        </svg>
        <h3>등록된 회원이 없습니다</h3>
        <p>새로운 회원을 등록해주세요.</p>
      </div>

      <div class="navigation-buttons">
        <button @click="goToLogout" class="btn-secondary">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
            <polyline points="16 17 21 12 16 7"></polyline>
            <line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
          <span>로그아웃</span>
        </button>
        <button @click="goToLeave" class="btn-danger">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
          <span>회원탈퇴</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import * as XLSX from 'xlsx';
import apiClient from '@/api/client';
import { useAuth } from '@/composables/useAuth';
import { useApi } from '@/composables/useApi';
import { API_ENDPOINTS, ROUTES } from '@/constants';
import { formatDate } from '@/utils/date.utils';
import type { Sample } from '@/types/domain.types';

export default defineComponent({
  name: 'UserInfo',
  setup() {
    const router = useRouter();
    const { currentUser } = useAuth();
    const { loading, execute } = useApi<Sample>();
    const result = ref<Sample[]>([]);

    const getUserInfo = async () => {
      if (!currentUser.value?.username) {
        alert('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
        router.push(ROUTES.LOGIN);
        return;
      }

      const data = await execute(() =>
        apiClient.get(API_ENDPOINTS.USER_BY_USERNAME(currentUser.value!.username))
      );

      if (data) {
        result.value = [data]; // 단일 사용자 정보를 배열로 변환
      }
    };

    const downloadExcel = () => {
      if (result.value.length === 0) {
        alert('다운로드할 데이터가 없습니다.');
        return;
      }

      const dataWS = XLSX.utils.json_to_sheet(result.value);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, dataWS, 'Sheet1');
      const filename = `my_account_info_${formatDate(new Date().toISOString(), 'YYYYMMDD_HHmmss')}.xlsx`;
      XLSX.writeFile(wb, filename);
    };

    const goToLogout = () => {
      router.push(ROUTES.LOGIN);
    };

    const goToLeave = () => {
      router.push(ROUTES.LEAVE_USER);
    };

    onMounted(() => {
      getUserInfo();
    });

    return {
      result,
      loading,
      getUserInfo,
      downloadExcel,
      goToLogout,
      goToLeave,
    };
  },
});
</script>

<style scoped>
.userinfo-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 60px 20px;
}

.userinfo-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 48px 40px;
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.userinfo-header {
  text-align: center;
  margin-bottom: 32px;
}

.userinfo-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.userinfo-subtitle {
  color: #718096;
  font-size: 14px;
  margin: 0;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e2e8f0;
}

.user-count {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4a5568;
  font-size: 16px;
  font-weight: 600;
}

.user-count svg {
  width: 24px;
  height: 24px;
  color: #667eea;
}

.btn-download {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-download svg {
  width: 18px;
  height: 18px;
}

.btn-download:hover {
  background: #38a169;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(72, 187, 120, 0.4);
}

.table-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
  max-height: 600px;
  overflow-y: auto;
  padding-right: 8px;
}

.table-container::-webkit-scrollbar {
  width: 8px;
}

.table-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.table-container::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 10px;
}

.user-card {
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.user-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.user-card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: white;
  border-bottom: 2px solid #e2e8f0;
}

.user-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar svg {
  width: 32px;
  height: 32px;
  color: white;
}

.user-basic-info {
  flex: 1;
}

.user-basic-info h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  color: #1a202c;
}

.user-basic-info p {
  margin: 0;
  color: #718096;
  font-size: 14px;
}

.user-id-badge {
  padding: 6px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.user-card-body {
  padding: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-icon {
  width: 20px;
  height: 20px;
  color: #667eea;
  flex-shrink: 0;
  margin-top: 2px;
}

.info-item > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.info-label {
  font-size: 12px;
  color: #a0aec0;
  font-weight: 600;
  text-transform: uppercase;
}

.info-value {
  font-size: 14px;
  color: #2d3748;
  word-break: break-word;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #a0aec0;
}

.empty-state svg {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 20px;
  color: #718096;
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

.navigation-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn-secondary,
.btn-danger {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary svg,
.btn-danger svg {
  width: 18px;
  height: 18px;
}

.btn-secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.btn-secondary:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.btn-danger {
  background: #f56565;
  color: white;
}

.btn-danger:hover {
  background: #e53e3e;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(245, 101, 101, 0.4);
}

@media (max-width: 768px) {
  .userinfo-card {
    padding: 32px 24px;
  }

  .userinfo-title {
    font-size: 28px;
  }

  .action-bar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .user-card-header {
    flex-wrap: wrap;
  }

  .user-id-badge {
    width: 100%;
    text-align: center;
  }
}
</style>
