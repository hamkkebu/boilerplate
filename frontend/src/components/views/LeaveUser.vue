<template>
  <h1>회원탈퇴</h1>
  <div>
    <input type="text" class="input_text" placeholder="ID" v-model="user_id" />
  </div>
  <div>
    <button @click="leaveSubmit()" type="submit" class="btn_leave" id="leave">
      <span class="btn_leave_text">회원탈퇴</span>
    </button>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      user_id: this.user_id
    }
  },
  methods: {
    leaveSubmit() {
      let url = process.env.VUE_APP_baseApiURL + '/account/delete/' + this.user_id
      axios.delete(url).then(res => {
        console.log(res);
        this.$router.push('/userinfo')
      }).catch(err => {
        console.log(err)
      })
    }
  }
}
</script>

<style>

</style>