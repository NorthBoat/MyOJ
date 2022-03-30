<template>

    <div class="container-fluid p-0">
        <link rel="stylesheet" href="/static/css/app.css">
        <h1 class="h3 mb-3">Repository</h1><br>

        <div class="row">

            <div class="col-12 col-xl-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="card-title">题单单</h5>
                        <h6 class="card-subtitle text-muted">Use <code>Java/C++/Golang</code> to solve the problems.</h6>
                    </div>
                    <table class="table table-striped table-hover">
                        <thead>
                            <tr>
                                <th style="width:20%;">题号</th>
                                <th style="width:25%">题目</th>
                                <th style="width:24%">难度</th>
                                <th style="width:25%">点赞</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody v-if="this.got">
                            <tr v-for="item in list" :key="item.num">
                                <td>{{item.num}}</td>
                                <td>{{item.title}}</td>
                                <td>{{item.level}}</td>
                                <td>{{item.thumb}}</td>
                                <td><router-link :to="{name: 'introduce', params: {num: item.num}}">
                                    <el-button type="primary" icon="el-icon-edit"></el-button>
                                </router-link></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>

    </div>
</template>

<script>
export default {
    name: 'Repository',

    data() {
        return {
            list: '',
            got: false,
        };
    },

    mounted() {
        this.$axios.get("/titleList").then(response=>{
            let result = response.data;
            this.list = result.data;
            this.got = true;
        });
        
        
    },

    updated(){
        //console.log("hahaha");
        if(!this.got){
            this.$message.error('请求数据失败');
        }
    },



    methods: {

    },
};
</script>

<style lang="scss" scoped>

</style>