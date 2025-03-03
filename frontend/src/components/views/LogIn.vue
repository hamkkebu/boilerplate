<template>
  <h1>로그인</h1>
  <div>
    <input type="text" class="input_text" placeholder="ID" v-model="user_id" />
  </div>
  <div>
    <input type="password" class="input_text" placeholder="Password" v-model="user_pw" />
  </div>
  <div>
    <button @click="logInSubmit()" type="submit" class="btn_login" id="login">
      <span class="btn_login_text">로그인</span>
    </button>
  </div>
</template>

<script>
export default {
  methods: {
    logInSubmit() {
      this.$router.push({path: '/signup'})
    },
  }
}
</script>

<style>
</style>