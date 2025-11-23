import { createRouter, createWebHistory } from "vue-router";
import LogIn from '@/components/views/LogIn.vue';
import SignUp from "@/components/views/SignUp.vue";
import UserInfo from "@/components/views/UserInfo.vue";
import LeaveUser from "@/components/views/LeaveUser.vue";

export default createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            redirect: '/login'
        },
        {
            path: '/login',
            name: 'LogIn',
            component: LogIn,
        },
        {
            path: '/signup',
            name: 'SignUp',
            component: SignUp,
        },
        {
            path: '/userinfo',
            name: 'UserInfo',
            component: UserInfo,
        },
        {
            path: '/leaveuser',
            name: 'LeaveUser',
            component: LeaveUser,
        }
    ]
})