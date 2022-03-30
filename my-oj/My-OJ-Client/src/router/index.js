import Vue from 'vue'
import Router from 'vue-router'

import Main from '../views/Main'
import Lost from '../views/Lost'

import Login from '../views/login/Login'
import Register from '../views/login/Register'
import Improve from '../views/login/Improve'

import Hello from '../views/content/Hello'

import Repository from '../views/content/Repository'
import Detail from '../views/content/detail/Detail'
import Introduce from '../views/content/detail/Introduce'
import Result from '../views/content/detail/Result'
import Comment from '../views/content/detail/Comment'

import Discuss from '../views/content/Discuss'
import Topic from '../views/content/topic/Topic'


Vue.use(Router)

export default new Router({
  //mode: 'history',
  routes: [
    {
      path: '/login',
      component: Login,
      name: 'login'
    },
    {
      path: '/main',
      component: Main,
      name: 'main',
      redirect: '/main/hello',
      children:[
        {path: '/main/hello', component: Hello, name: 'hello'},

        {path: '/main/repository', component: Repository, name: 'repository'},
        {
          path: '/main/detail',
          redirect: '/main/repository',
          component: Detail,
          name: 'detail',
          props: true,
          children: [
            {path: '/main/detail/introduce/:num', component: Introduce, name: 'introduce'},
            {path: '/main/detail/result/:num', component: Result, name: 'result'},
            {path: '/main/detail/comment/:num', component: Comment, name: 'comment'}
          ]
        },

        {path: '/main/discuss', component: Discuss, name: 'discuss'},
        {
          path: '/main/topic',
          component: Topic, 
          name: 'topic',
          redirect: '/main/discuss',
          children: [
            {path: '/main/topic/detail/:num', component: Topic, name: 'topic'}
          ]
        },
      ]
    },
    {
      path: '/',
      redirect: '/main',
    },
    {
      path: '/register',
      component: Register,
      name: 'register'
    },
    {
      path: '/improve',
      component: Improve,
      name: 'improve'
    },
    {
      path: '/*',
      component: Lost,
      name: 'lost'
    }
  ]
})
