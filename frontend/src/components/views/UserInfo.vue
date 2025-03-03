<template>
  <article class="content">
    <div class="cont_box">
      <div class="cont_box_item">
        <div class="cont_box_title">
          <h3>Contents</h3>
        </div>
        <div class="tbl detail_tbl">
          <table>
            <tbody>
            <td v-for="key in taste_account_columns()" :key="key">
              <th>{{ key.slice(5) }}</th>
              <tr v-for="item in result" :key="item">
                {{ item[key] || "NULL"}}
              </tr>
            </td>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </article>
  <div>
    <button @click="downloadExcel()" type="button" class="btn_download_excel" id="download_excel">
      <span class="btn_download_excel_text">엑셀 다운로드</span>
    </button>
  </div>
</template>

<script>
import taste_account_columns from "@/data";
import * as XLSX from 'xlsx';

export default {
  data () {
    return {
      result: [],
    }
  },
  mounted() {
    this.getUserInfo();
  },
  methods: {
    taste_account_columns() {
      return taste_account_columns
    },
    getUserInfo() {
      let url = process.env.VUE_APP_baseApiURL + '/account/get/list'
      this.axios.get(url).then(res => {
        this.result = res.data
      }).catch(err => {
        console.log(err);
      })
    },
    downloadExcel() {
      const dataWS = XLSX.utils.json_to_sheet(this.result);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, dataWS, 'Sheet1');
      var filename = "account_info_" + new Date().toJSON() + ".xlsx";
      XLSX.writeFile(wb, filename);
    },
  }
}

</script>

<style>
</style>