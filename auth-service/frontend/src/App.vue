<template>
  <div id="app">
    <NavBar />
    <router-view />
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted } from 'vue';
import NavBar from '@/components/views/NavBar.vue';
import { useAuth } from '@/composables/useAuth';

export default defineComponent({
  name: 'App',
  components: {
    NavBar,
  },
  setup() {
    const { restoreUser } = useAuth();

    onMounted(() => {
      // 페이지 로드 시 localStorage에서 사용자 정보 복원
      restoreUser();
    });
  },
});
</script>

<style>
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: #f7fafc;
}

#app {
  min-height: 100vh;
}
</style>
