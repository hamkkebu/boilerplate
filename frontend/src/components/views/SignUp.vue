<template>
  <h1>회원가입</h1>
  <div v-for="(key, index) in taste_account_columns()" :key="index">
      <input type="text" class="input_text" :placeholder="key.slice(5)" v-model="dict_columns[key]" />
  </div>
  <div>
    <button @click="signUpSubmit()" type="submit" class="btn_submit" id="submit">
      <span class="btn_text">제출</span>
    </button>
  </div>
</template>

<script>
import taste_account_columns from '@/data.js'

export default {
  data () {
    return {
      dict_columns: taste_account_columns.reduce(function(obj, x) {
        obj[x] = '';
        return obj;
      }, {}),
    }
  },
  methods: {
    taste_account_columns() {
      return taste_account_columns.slice(1)
    },
    signUpSubmit() {
      let url = process.env.VUE_APP_baseApiURL + '/account/create'

      this.axios.post(url, JSON.stringify(this.dict_columns)).then(res => {
        console.log(res);
        this.$router.push('/userinfo')
      }).catch(err => {
        console.log(err);
      })
    },
  }
}
</script>

<style>
</style>