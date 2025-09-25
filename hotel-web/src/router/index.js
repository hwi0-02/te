// src/router/index.js
import { createRouter, createWebHistory } from "vue-router"

// Auth & static
import Login from "@/components/user/login_page/Login.vue"
import Register from "@/components/user/login_page/Register.vue"
import ForgotPassword from "@/components/user/login_page/ForgotPassword.vue"
import LoginVerify from "@/components/user/login_page/LoginVerify.vue"
import PasswordReset from "@/components/user/login_page/PasswordReset.vue"
import OAuth2Redirect from "@/components/user/login_page/OAuth2Redirect.vue"
import MainPage from "@/components/user/main_page/MainPage.vue"
import TermsPage from "@/components/user/main_page/Terms.vue"
import PrivacyPage from "@/components/user/main_page/Privacy.vue"

// Hotel search/detail (네가 쓰던 경로 유지)
import Search from "@/components/user/hotel_page/Search.vue"
// 상세는 지연 로딩 권장
const HotelDetailView = () => import("@/components/user/hotel_page/HotelDetailView.vue")

// Checkout pages (요청한 디렉토리)
const ReservationCheckout = () => import("@/components/user/hotel_checkout/ReservationCheckout.vue")
const ReservationResult   = () => import("@/components/user/hotel_checkout/ReservationResult.vue")

// Admin pages
import AdminLayout from '@/components/admin/AdminLayout.vue'
import AdminDashboard from '@/components/admin/AdminDashboard.vue'
import UserManagement from '@/components/admin/UserManagement.vue'
import AdminHotelManagement from '@/components/admin/AdminHotelManagement.vue'
import ReservationMonitoring from '@/components/admin/ReservationMonitoring.vue'
import PaymentManagement from '@/components/admin/PaymentManagement.vue'
import ReviewManagement from '@/components/admin/ReviewManagement.vue'
import CouponManagement from '@/components/admin/CouponManagement.vue'
import SalesManagement from '@/components/admin/SalesManagement.vue'
import HotelManagement from '@/components/admin/HotelManagement.vue'

const routes = [
  { path: "/", component: MainPage },

  // 검색/상세
  { path: "/search", name: "Search", component: Search },
  { path: "/hotels/:id", component: HotelDetailView, props: true },
  { path: "/hotels", redirect: "/hotels/1" },

  // 체크아웃/결과
  { path: "/reservations/:id", name: "ReservationCheckout", component: ReservationCheckout },
  { path: "/reservations/:id/result", name: "ReservationResult", component: ReservationResult },

  {
    path: '/payments/:id',
    name: 'PaymentCheckout',
    component: () => import('@/components/user/hotel_checkout/PaymentCheckout.vue')
  },
  { path: '/payment/success', name :"PaymentSuccess", component: () => import('@/components/user/hotel_checkout/PaymentSuccess.vue')},
  { path: '/payment/fail', name :"PaymentFailure", component: () => import('@/components/user/hotel_checkout/PaymentFailure.vue')},

  // Auth / 정책
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/terms", component: TermsPage },
  { path: "/privacy", component: PrivacyPage },
  { path: "/forgotPassword", component: ForgotPassword },
  { path: "/forgot-password", component: ForgotPassword },
  { path: "/verify", component: LoginVerify },
  { path: "/passwordReset", component: PasswordReset },
  { path: "/password-reset", component: PasswordReset },
  { path: "/oauth2/redirect", component: OAuth2Redirect },

  // Admin
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: AdminDashboard },
      { path: 'users', component: UserManagement },
      { path: 'businesses', component: HotelManagement },
      { path: 'hotels', component: AdminHotelManagement },
      { path: 'reservations', component: ReservationMonitoring },
      { path: 'payments', component: PaymentManagement },
      { path: 'reviews', component: ReviewManagement },
      { path: 'sales', component: SalesManagement },
      { path: 'coupons', component: CouponManagement }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to, from, next) => {
  const userStr = localStorage.getItem('user')
  let role = null
  try {
    role = userStr ? JSON.parse(userStr)?.role : null
  } catch (e) { /* ignore */ }

  if (to.path === '/login' && role === 'ADMIN') {
    return next('/admin')
  }

  if (to.matched.some(r => r.meta?.requiresAdmin)) {
    if (!role) return next('/login')
    if (role !== 'ADMIN') return next('/')
  }
  next()
})

export default router
