<template>
    <div>
        <h2>Hello! {{this.username}}</h2>
    </div>
</template>

<script>
export default {
    name: 'Profile',

    data() {
        return {
            username: '',
        };
    },

    mounted() {
        this.username = window.sessionStorage.getItem('loginUsername');
    },

    methods: {
        
    },
};
</script>

<style lang="scss" scoped>

</style>