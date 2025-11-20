import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import LogIn from '@/components/views/Login.vue';
import SignUp from '@/components/views/SignUp.vue';
import UserInfo from '@/components/views/UserInfo.vue';
import LeaveUser from '@/components/views/LeaveUser.vue';
import { ROUTES } from '@/constants';

const routes: RouteRecordRaw[] = [
  {
    path: ROUTES.HOME,
    redirect: ROUTES.LOGIN,
  },
  {
    path: ROUTES.LOGIN,
    name: 'LogIn',
    component: LogIn,
  },
  {
    path: ROUTES.SIGNUP,
    name: 'SignUp',
    component: SignUp,
  },
  {
    path: ROUTES.USER_INFO,
    name: 'UserInfo',
    component: UserInfo,
  },
  {
    path: ROUTES.LEAVE_USER,
    name: 'LeaveUser',
    component: LeaveUser,
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
