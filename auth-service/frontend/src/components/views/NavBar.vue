<template>
  <nav class="navbar">
    <div class="navbar-container">
      <div class="navbar-brand">
        <router-link :to="ROUTES.HOME" class="brand-link">
          <span class="brand-logo">🔐</span>
          <span class="brand-name">Auth Service</span>
        </router-link>
      </div>

      <div class="navbar-menu" :class="{ 'is-active': menuActive }">
        <div class="navbar-start">
          <router-link
            v-if="!isAuthenticated"
            :to="ROUTES.SIGNUP"
            class="navbar-item"
            @click="closeMenu"
          >
            회원가입
          </router-link>

          <router-link
            v-if="isAuthenticated"
            :to="ROUTES.USER_INFO"
            class="navbar-item"
            @click="closeMenu"
          >
            내 정보
          </router-link>

          <router-link
            v-if="isAuthenticated && isAdmin"
            :to="ROUTES.ADMIN_DASHBOARD"
            class="navbar-item admin-link"
            @click="closeMenu"
          >
            <svg class="icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 2L2 7l10 5 10-5-10-5z"></path>
              <path d="M2 17l10 5 10-5"></path>
              <path d="M2 12l10 5 10-5"></path>
            </svg>
            관리자
          </router-link>
        </div>

        <div class="navbar-end">
          <div v-if="isAuthenticated" class="navbar-user">
            <span class="user-info">
              <span class="user-name">{{ currentUser?.firstName }} {{ currentUser?.lastName }}</span>
              <span class="user-role" :class="currentUser?.role?.toLowerCase()">
                {{ getRoleLabel(currentUser?.role) }}
              </span>
            </span>
            <button class="btn-logout" @click="handleLogout">
              로그아웃
            </button>
          </div>

          <router-link
            v-else
            :to="ROUTES.LOGIN"
            class="btn-login"
            @click="closeMenu"
          >
            로그인
          </router-link>
        </div>
      </div>

      <button class="navbar-burger" @click="toggleMenu" :class="{ 'is-active': menuActive }">
        <span></span>
        <span></span>
        <span></span>
      </button>
    </div>
  </nav>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import { ROUTES } from '@/constants';
import type { UserRole } from '@/types/domain.types';

export default defineComponent({
  name: 'NavBar',
  setup() {
    const router = useRouter();
    const { currentUser, isAuthenticated, logout } = useAuth();
    const menuActive = ref(false);

    const isAdmin = computed(() => {
      const role = currentUser.value?.role;
      return role === 'ADMIN' || role === 'DEVELOPER';
    });

    const toggleMenu = () => {
      menuActive.value = !menuActive.value;
    };

    const closeMenu = () => {
      menuActive.value = false;
    };

    const handleLogout = async () => {
      if (confirm('로그아웃 하시겠습니까?')) {
        await logout();
        closeMenu();
        router.push(ROUTES.LOGIN);
      }
    };

    const getRoleLabel = (role?: UserRole): string => {
      if (!role) return '';
      const labels: Record<UserRole, string> = {
        USER: '사용자',
        ADMIN: '관리자',
        DEVELOPER: '개발자',
      };
      return labels[role] || role;
    };

    return {
      ROUTES,
      currentUser,
      isAuthenticated,
      isAdmin,
      menuActive,
      toggleMenu,
      closeMenu,
      handleLogout,
      getRoleLabel,
    };
  },
});
</script>

<style scoped>
.navbar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.navbar-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64px;
}

.navbar-brand {
  flex-shrink: 0;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: white;
  font-weight: 700;
  font-size: 20px;
  transition: opacity 0.2s;
}

.brand-link:hover {
  opacity: 0.9;
}

.brand-logo {
  font-size: 28px;
}

.brand-name {
  font-weight: 700;
}

.navbar-menu {
  display: flex;
  align-items: center;
  gap: 32px;
  flex: 1;
  justify-content: space-between;
  margin-left: 48px;
}

.navbar-start,
.navbar-end {
  display: flex;
  align-items: center;
  gap: 24px;
}

.navbar-item {
  color: white;
  text-decoration: none;
  font-weight: 600;
  font-size: 15px;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.navbar-item:hover {
  background: rgba(255, 255, 255, 0.15);
}

.navbar-item.router-link-active {
  background: rgba(255, 255, 255, 0.2);
}

.navbar-item.admin-link {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.navbar-item.admin-link:hover {
  background: rgba(255, 255, 255, 0.2);
}

.icon {
  width: 18px;
  height: 18px;
}

.navbar-user {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-role {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.user-role.admin {
  background: rgba(229, 62, 62, 0.9);
}

.user-role.developer {
  background: rgba(214, 158, 46, 0.9);
}

.btn-logout {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: rgba(255, 255, 255, 0.3);
}

.btn-login {
  padding: 8px 20px;
  background: white;
  color: #667eea;
  text-decoration: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-login:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.navbar-burger {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  width: 40px;
  height: 40px;
  position: relative;
}

.navbar-burger span {
  display: block;
  height: 2px;
  width: 24px;
  background: white;
  margin: 5px auto;
  transition: all 0.3s;
  border-radius: 2px;
}

.navbar-burger.is-active span:nth-child(1) {
  transform: translateY(7px) rotate(45deg);
}

.navbar-burger.is-active span:nth-child(2) {
  opacity: 0;
}

.navbar-burger.is-active span:nth-child(3) {
  transform: translateY(-7px) rotate(-45deg);
}

@media (max-width: 768px) {
  .navbar-burger {
    display: block;
  }

  .navbar-menu {
    display: none;
    position: absolute;
    top: 64px;
    left: 0;
    right: 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    flex-direction: column;
    align-items: stretch;
    gap: 0;
    margin: 0;
    padding: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .navbar-menu.is-active {
    display: flex;
  }

  .navbar-start,
  .navbar-end {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
    width: 100%;
  }

  .navbar-item {
    width: 100%;
    justify-content: center;
  }

  .navbar-user {
    flex-direction: column;
    gap: 12px;
    padding: 12px 0;
  }

  .user-info {
    flex-direction: column;
    gap: 4px;
  }

  .btn-logout,
  .btn-login {
    width: 100%;
    text-align: center;
  }
}
</style>